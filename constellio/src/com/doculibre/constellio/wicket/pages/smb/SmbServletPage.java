
package com.doculibre.constellio.wicket.pages.smb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import jcifs.smb.SmbFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequestCycle;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.CredentialGroup;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.UserCredentials;
import com.doculibre.constellio.services.AuthorizationServices;
import com.doculibre.constellio.services.ConnectorManagerServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.EncryptionUtils;
import com.doculibre.constellio.wicket.session.ConstellioSession;

public class SmbServletPage extends WebPage {
	
	public static final String RECORD_ID = "recordID";
	public static final String COLLECTION = "collection";
	
	public SmbServletPage(PageParameters parameters) {
        super(parameters);
        
        
        String recordID = parameters.getString(RECORD_ID);
        if (StringUtils.isBlank(recordID)) {
            throw new WicketRuntimeException(RECORD_ID + " parameter is required");
        } 
        String collectionName = parameters.getString(COLLECTION);
        if (StringUtils.isBlank(collectionName)) {
            throw new WicketRuntimeException(COLLECTION + " parameter is required");
        } 
        
        File downloadedSmbFile = getSmbFile(Long.valueOf(recordID), collectionName);
        
        WebRequestCycle requestCycle = (WebRequestCycle) RequestCycle.get();
        HttpServletResponse response = requestCycle.getWebResponse().getHttpServletResponse();
		response.setContentType("application/octet-stream" );
		
		response.setContentLength( (int)downloadedSmbFile .length() );
		response.setHeader( "Content-Disposition", "attachment; filename=\"" + downloadedSmbFile.getName() + "\"" );
		
		try{
			InputStream is = new BufferedInputStream(new FileInputStream(downloadedSmbFile));
			IOUtils.copy(is, response.getOutputStream());
			IOUtils.closeQuietly(is);
			FileUtils.deleteQuietly(downloadedSmbFile);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
    }


    public static  File getSmbFile(Long recordID, String collectionName) {
    	RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();  
    	RecordCollection collection = collectionServices.get(collectionName);
    	RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        Record record = recordServices.get(recordID, collection);
        ConstellioSession session = ConstellioSession.get();
        ConstellioUser user = session.getUser();
        AuthorizationServices authorizationServices = ConstellioSpringUtils.getAuthorizationServices();
        if(!authorizationServices.isAuthorized(record, user)){
        	throw new RuntimeException("Vous n'avez pas accès au document demandé!");
        }
        ConnectorInstance connectorInstance = record.getConnectorInstance();
        ConnectorType connectorType = connectorInstance.getConnectorType();
        String connectorTypeName = connectorType.getName();
        if(!connectorTypeName.equals("FileConnectorType")){
        	throw new RuntimeException("Not implemented with connector type :" + connectorTypeName);
        }
        CredentialGroup credentialGroup = connectorInstance.getCredentialGroup();
        Set<UserCredentials> userCredentials = credentialGroup.getUserCredentials();
        String smbPassword = null;
        String smbUser = null;
        //TODO More efficient request
        for (UserCredentials cred : userCredentials) {
        	if (cred.getUser().getId() == user.getId()) {
        		smbUser = cred.getUsername();
        		smbPassword = EncryptionUtils.decrypt(cred.getEncryptedPassword());
        		break;
        	}
        }
        String smbPath = record.getDisplayUrl();
        try {
        	smbPath = URLDecoder.decode(smbPath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        SmbFile smbFile = SMBUtils.getSmbFile(smbUser, smbPassword, smbPath);
        return SMBUtils.copySmbFileToConnectorInstanceDirectory(smbFile, connectorInstance);
	}


	@Override
    protected void onRender(MarkupStream markupStream) {
        // Do nothing since response writer was already used
    }

}
