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
import org.xml.sax.SAXException;

import com.doculibre.constellio.utils.ClasspathUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class SolrLogContext {
	
	private static final String SEARCH_LOG = "search_log";
	
	private static final String CLICK_LOG = "click_log";
	
	private static final String SEARCH_COMPILE_LOG = "search_compile_log";
	
	private static final String CLICK_COMPILE_LOG = "click_compile_log";
	
	private static final String[] CORE_NAMES = {
		SEARCH_LOG, CLICK_LOG, SEARCH_COMPILE_LOG, CLICK_COMPILE_LOG
	};
	
	private static File solrLogsRootDir;

	private static CoreContainer cores = new CoreContainer();

    private static Map<String, SolrServer> coreServers = new HashMap<String, SolrServer>();
	
	private static File getDefaultSolrLogsRootDir() {
		File statsDir = ClasspathUtils.getStatsDir();
		return statsDir;
	}
	
	public static void init() throws ParserConfigurationException, IOException, SAXException {
		if (solrLogsRootDir == null) {
			solrLogsRootDir = getDefaultSolrLogsRootDir();
		}
		init(solrLogsRootDir);
	}
	
	public static synchronized void init(File solrLogsRootDir) throws ParserConfigurationException, IOException, SAXException {
		SolrLogContext.solrLogsRootDir = solrLogsRootDir;
		String solrLogServer = ConstellioSpringUtils.getSolrLogServer();
		if (solrLogServer != null) {
			for (String coreName : CORE_NAMES) {
				if (!solrLogServer.endsWith("/")) {
					solrLogServer += "/";
				}
				String coreUrl = solrLogServer + coreName;
				setHttpSolrServer(coreName, coreUrl);
			}
		} else {
			File solrXml = new File(solrLogsRootDir, "solr.xml");
			String solrLogsRootDirPAth = solrLogsRootDir.getPath();
			cores.createAndLoad(solrLogsRootDirPAth, solrXml);
			
			for (String coreName : CORE_NAMES) {
				// Default
				setEmbeddedSolrServer(coreName);
			}
		}	
	}
    
    public static synchronized void shutdown() {
        if (cores != null) {
            cores.shutdown();
        }
    }
	
	public static File getSolrLogsRootDir() {
		return solrLogsRootDir;
	}
	
	private static SolrServer getSolrServer(String coreName) {
		return coreServers.get(coreName);
	}
	
	private static void registerSolrServer(String coreName, SolrServer solrServer) {
		coreServers.put(coreName, solrServer);
	}
	
	private static void setEmbeddedSolrServer(String coreName) {
		registerSolrServer(coreName, new EmbeddedSolrServer(cores, coreName));
	}
	
	private static void setHttpSolrServer(String coreName, String coreUrl) throws MalformedURLException {
		registerSolrServer(coreName, new HttpSolrServer(coreUrl));
	}
	
	public static SolrServer getSearchLogSolrServer() {
		return getSolrServer(SEARCH_LOG);
	}
	
	public static SolrServer getClickLogSolrServer() {
		return getSolrServer(CLICK_LOG);
	}
	
	public static SolrServer getSearchCompileLogSolrServer() {
		return getSolrServer(SEARCH_COMPILE_LOG);
	}
	
	public static SolrServer getClickCompileLogSolrServer() {
		return getSolrServer(CLICK_COMPILE_LOG);
	}
	
}
