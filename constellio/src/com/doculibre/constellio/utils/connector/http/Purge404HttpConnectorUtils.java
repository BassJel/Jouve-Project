package com.doculibre.constellio.utils.connector.http;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceUtils;

public class Purge404HttpConnectorUtils {
	
	private static int getResponseCode(String urlString) throws MalformedURLException, IOException {
	    URL u = new URL(urlString); 
	    HttpURLConnection huc =  (HttpURLConnection)  u.openConnection(); 
	    huc.setRequestMethod("GET"); 
	    huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
	    huc.connect();
	    int responseCode = huc.getResponseCode();
	    huc.disconnect();
	    return responseCode;
	}
	
	public static void main(String[] args) throws Exception {
		ConstellioPersistenceUtils.beginTransaction();
		
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
		
		List<String> deletedURLs = new ArrayList<String>();
		for (RecordCollection collection : collectionServices.list()) {
			for (ConnectorInstance connectorInstance : collection.getConnectorInstances()) {
				ConnectorType connectorType = connectorInstance.getConnectorType();
				if (ConnectorType.CONNECTOR_TYPE_HTTP.equals(connectorType.getName())) {
					System.out.println("###############################################################################");
					System.out.println("# Purging records for connector " + connectorInstance.getDisplayName() + " (" + connectorInstance.getName() + ")");
					
					Map<String, Object> criteria = new HashMap<String, Object>();
					criteria.put("connectorInstance", connectorInstance);
					List<Record> records = recordServices.list(criteria);
					System.out.println("# " + records.size() + " records");
					for (Record record : records) {
						try {
							String urlString = record.getDisplayUrl();
							System.out.println(urlString);
							int responseCode = getResponseCode(urlString);
							if (responseCode == 404) {
								System.err.println("Deleted");
								record.setDeleted(true);
								recordServices.makePersistent(record);
								deletedURLs.add(urlString);
							}
						} catch (ConnectException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		System.err.println("Deleted URLs (" + deletedURLs.size() + ")");
		for (String deletedURL : deletedURLs) {
			System.err.println(deletedURL);
		}
		
		ConstellioPersistenceUtils.finishTransaction(true);
	}

}
