package com.doculibre.constellio.plugins.api.wicket.search;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.List;

import jcifs.smb.SmbFile;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.xmlbeans.impl.common.IOUtil;

import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.pages.smb.SmbServletPage;
import com.doculibre.constellio.wicket.session.ConstellioSession;
import com.doculibre.intelligid.addin.model.DocumentDetail;
import com.doculibre.intelligid.addin.model.IntelliGIDServerInfo;
import com.doculibre.intelligid.addin.services.StatelessIntelliGIDServerServices;

@PluginImplementation
public class IntelliGIDTransfertSearchResultsPlugin implements
		TransfertSearchResultsPlugin, Serializable {
	
	@Override
	public String getName() {
		return "Plugin de transfert vers IntelliGID";
	}

	@Override
	public String getLabelText() {
		return "Copier vers IntelliGID";
	}

	@Override
	public String transfert(Record record) {

		String id = null;
		
		try {
			id = "" + uploadDocuments(record);
			

		} catch(Throwable t) {
			handleException(record, t);
		}
		return id;
	}
	
	public void afterTransfert(List<Record> records, List<String> ids) {
		String intelligidURL = ConstellioSpringUtils.getIntelliGIDServiceInfo().getIntelligidUrl();
		String intelligidPage = intelligidURL + "/app/assignerDossier/documents/" + StringUtils.join(ids.toArray(), ",");
		RequestCycle.get().setRequestTarget(new RedirectRequestTarget(intelligidPage));
	}

	private void handleException(Record record, Throwable t) {
		String generalMessage = "Impossible de transférer le document \"" + record.getDisplayUrl() + "\" : ";
		
		Throwable rootCause = t;
		while (rootCause.getCause() != null) {
			rootCause = rootCause.getCause();
		}
		String causeMessage = rootCause.getMessage();
		
		if (rootCause instanceof ConnectException) {
			causeMessage = "Impossible de se connecter à IntelliGID";
		}
		
		String allCancelled = ". Veuillez noter que le transfert de tous les documents sélectionnés ont été annulés.";
		
		throw new RuntimeException(generalMessage + causeMessage + allCancelled, t);
	}
	
	public void cancel(Record record, String id) {
		StatelessIntelliGIDServerServices services = newStatelessServices();
		services.cancelSave(Long.valueOf(id));
	}

	private long uploadDocuments(Record record) {

		StatelessIntelliGIDServerServices services = newStatelessServices();
		
		DocumentDetail documentDetail = new DocumentDetail();
		String name = null;
		String path = null;
		File tempFile = null;
		
		InputStream in = null;
		OutputStream out = null;
		try {
			try {
				SmbFile file = SmbServletPage.getSmbFile(record.getId(), record.getConnectorInstance().getRecordCollection().getName());
				name = file.getName();
				path = file.getCanonicalPath();
				in = new BufferedInputStream(file.getInputStream());
				
			} catch(IllegalArgumentException e) {
				File file = new File(record.getDisplayUrl());
				name = file.getName();
				path = file.getPath();
				in = new BufferedInputStream(new FileInputStream(file));
			}
			
			File tempDir = new File(System.getProperty("java.io.tmpdir"));
			tempFile = new File(tempDir, name);
			out = new BufferedOutputStream(new FileOutputStream(tempFile));
			IOUtil.copyCompletely(in, out);
			documentDetail.setFile(tempFile);
			
		} catch (Exception e) {
			if (tempFile != null) {
				FileUtils.deleteQuietly(tempFile);
			}
			throw new RuntimeException(e);
		} finally {
			if (in != null) {
				IOUtils.closeQuietly(in);
			}
			if (out != null) {
				IOUtils.closeQuietly(out);
			}
		}
		
		path = path.replace("smb:", "");

		try {
			services.save(documentDetail);
		} finally {
			FileUtils.deleteQuietly(tempFile);
		}
		return documentDetail.getId().longValue();
	}

	private StatelessIntelliGIDServerServices newStatelessServices() {
		String currentUser = ConstellioSession.get().getUser().getUsername();
		StatelessIntelliGIDServerServices services = new StatelessIntelliGIDServerServices(
				newServerInfo(currentUser));
		return services;
	}

	private IntelliGIDServerInfo newServerInfo(String currentUser) {
		IntelliGIDServerInfo serverInfo = new IntelliGIDServerInfo();
		serverInfo.setIntelligidUrl(ConstellioSpringUtils.getIntelliGIDServiceInfo().getIntelligidUrl());
		serverInfo.setUser(currentUser);
		return serverInfo;
	}
	
}
