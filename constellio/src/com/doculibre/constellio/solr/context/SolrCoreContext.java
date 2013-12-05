/**
 * Constellio, Open Source Enterprise Search
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.doculibre.constellio.solr.context;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.xml.sax.SAXException;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.opensearch.OpenSearchSolrServer;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ClasspathUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class SolrCoreContext {
	
	private static File solrCoresRootDir;

	private static CoreContainer cores = new CoreContainer();
    
    public static final String DEFAULT_COLLECTION_NAME = "constellio_default";
	
	private static Map<String, SolrServer> coreServers = new HashMap<String, SolrServer>();
	
	public static synchronized void init() throws ParserConfigurationException, IOException, SAXException {
		if (solrCoresRootDir == null) {
			solrCoresRootDir = ClasspathUtils.getCollectionsRootDir();
		}
		init(solrCoresRootDir);
	}
	
	public static synchronized void init(File solrCoresRootDir) throws ParserConfigurationException, IOException, SAXException {
		SolrCoreContext.solrCoresRootDir = solrCoresRootDir;
		File solrXml = new File(solrCoresRootDir, "solr.xml");
		CoreContainer.createAndLoad(solrCoresRootDir.getPath(), solrXml);
		
		for (String coreName : cores.getCoreNames()) {
			// Default
			setEmbeddedSolrServer(coreName);
		}
	}
    
    public static synchronized void shutdown() {
        if (cores != null) {
            cores.shutdown();
        }
    }
    
    public static synchronized void clear() {
    	solrCoresRootDir = null;
    	cores = new CoreContainer();
        coreServers = new HashMap<String, SolrServer>();
    }
	
	public static File getSolrCoresRootDir() {
		return solrCoresRootDir;
	}
	
	public static File getSolrCoreRootDir(String coreName) {
	    String coreDirName;
	    if (coreName != null) {
	    	RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
	    	RecordCollection collection = collectionServices.get(coreName);
	        coreDirName = collection != null ? collection.getEffectiveInstanceDirName() : coreName;
	    } else {
	        coreDirName = DEFAULT_COLLECTION_NAME;
	    }
	    return new File(getSolrCoresRootDir(), coreDirName);
	}
	
	public static File getSolrCoreIndexDir(String coreName) {
	    File solrCoreRootDir = getSolrCoreRootDir(coreName);
	    return new File(solrCoreRootDir, "data" + File.separator + "index");
	}

	public static CoreContainer getCores() {
		return cores;
	}
	
	public static SolrServer getSolrServer(ConnectorInstance connectorInstance) {
		return getSolrServer(connectorInstance.getRecordCollection());
	}
	
	public static SolrServer getSolrServer(RecordCollection collection) {
	    if (collection != null && collection.isOpenSearch()) {
	        return new OpenSearchSolrServer();
	    } else {
	        return coreServers.get(collection.getName());
	    }
	}
	
	public static SolrServer getSolrServer(String collectionName) {
	    RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
	    RecordCollection collection = collectionServices.get(collectionName);
		return getSolrServer(collection);
	}
	
	//Pour tests sans collection
	public static SolrServer getSolrServerUtil(String coreName) {
	    return coreServers.get(coreName);
	}
	
	public static void registerSolrServer(String coreName, SolrServer solrServer) {
		coreServers.put(coreName, solrServer);
	}
	
	public static SolrServer setEmbeddedSolrServer(String coreName) {
		SolrServer solrServer = new EmbeddedSolrServer(cores, coreName);
		registerSolrServer(coreName, solrServer);
		return solrServer;
	}
	
	public static SolrServer setHttpSolrServer(String coreName, String url) throws MalformedURLException {
		SolrServer solrServer = new HttpSolrServer(url);
		registerSolrServer(coreName, solrServer);
		return solrServer;
	}
	
	public static void removeCore(String coreName) {
		SolrCore core = cores.getCore(coreName);
		if (core != null) {
			core.closeSearcher();
			core.close();
		}
		cores.remove(coreName);
		coreServers.remove(coreName);
	}
	
}
