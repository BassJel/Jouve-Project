
package com.doculibre.constellio.wicket.pages.smb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import org.apache.commons.io.FileUtils;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.utils.ClasspathUtils;

/**
 * @author Nouha
 */
public class SMBUtils {

	private SMBUtils() {
		super();
	}
	
	public static SmbFile getSmbFile(String user, String password, String smbPath){
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(smbPath, user, password);
        SmbFile smbFile = null;
		try {
			smbFile = new SmbFile(smbPath, auth);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return smbFile;
	}
	
	public static InputStream getInputStream(SmbFile smbFile) throws SmbException, MalformedURLException, UnknownHostException{
		SmbFileInputStream inputStream = new SmbFileInputStream(smbFile);
		return inputStream;
	}
	
	public static File copySmbFileToDirectory(SmbFile smbFile, String directoryPath){
		if(smbFile == null){
			throw new RuntimeException("Invalid null File :" + smbFile);
		}
		File dir = new File(directoryPath);
		if(!dir.isDirectory()){
			throw new RuntimeException("Invalid directory :" + directoryPath);
		}
		String filePath = dir.getAbsolutePath() + File.separator + smbFile.getName();
		try {
			SmbFileInputStream inputStream = new SmbFileInputStream(smbFile);
			FileUtils.copyInputStreamToFile(inputStream, new File(filePath));
			inputStream.close();
			return new File(filePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static File copySmbFileToConnectorInstanceDirectory(SmbFile smbFile, ConnectorInstance connectorInstance){
		File webinfDir = ClasspathUtils.getWebinfDir();
        File connectorsDir = new File(webinfDir, "connectors");
        File connectorTypeDir = new File(connectorsDir, connectorInstance.getConnectorType().getName());
        File connectorInstanceDir = new File(connectorTypeDir, connectorInstance.getName());
        String directoryPath = connectorInstanceDir.getAbsolutePath();
        return copySmbFileToDirectory(smbFile, directoryPath);
	}

	public static void main(String[]args) throws IOException{
		String user = "";
		String password = "";
        String smbPath = "smb://DOCULIBRE-W520/wicket/DSC03651.JPG";
        String directoryPath = "C:\\Doculibre\\2012";
        
        SmbFile smbFile = SMBUtils.getSmbFile(user, password, smbPath);
        SMBUtils.copySmbFileToDirectory(smbFile, directoryPath);
	}
}

