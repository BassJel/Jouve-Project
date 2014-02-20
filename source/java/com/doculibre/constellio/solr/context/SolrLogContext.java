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

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.cloud.ClusterState;
import org.apache.solr.common.cloud.ZkStateReader;

import com.doculibre.constellio.services.SolrServicesImpl;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class SolrLogContext {

	private static final String SEARCH_LOG = "_search_log";
	private static final String CLICK_LOG = "_click_log";
	private static final String SEARCH_COMPILE_LOG = "_search_compile_log";
	private static final String CLICK_COMPILE_LOG = "_click_compile_log";
	private static final String[] LOG_CORE_NAMES = { SEARCH_LOG, CLICK_LOG, SEARCH_COMPILE_LOG, CLICK_COMPILE_LOG };

	private static Map<String, SolrServer> coreServers = new HashMap<String, SolrServer>();
	
	private static CloudSolrServer mainSolrServer;

	public static void init() {
		if (mainSolrServer == null) {
			try {
				mainSolrServer = new CloudSolrServer(ConstellioSpringUtils.getZooKeeperAddress());
				mainSolrServer.setZkClientTimeout(ConstellioSpringUtils.getZooKeeperClientTimeout());
				mainSolrServer.setZkConnectTimeout(ConstellioSpringUtils.getZooKeeperConTimeout());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		initCores();
	}

	private static synchronized void initCores() {
//		try {

			// do not use CoreAdminRequest
			mainSolrServer.connect();
			ZkStateReader reader = mainSolrServer.getZkStateReader();
			ClusterState state = reader.getClusterState();

			// since we only read log collections, we do not do the synchronization
			Set<String> collectionNames = state.getCollections();
			for (String collectionName : LOG_CORE_NAMES) {
				if (!collectionNames.contains(collectionName)) {
					int numReplicationFactor = ConstellioSpringUtils.getSolrReplicationFactor();
					// 1 shard is ok
					SolrServicesImpl.createCollectionInCloud(collectionName, collectionName, 1, numReplicationFactor);
				}
				setHttpSolrServer(collectionName, ConstellioSpringUtils.getSolrServerAddress());
			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
		
	}

	public static synchronized void shutdown() {
		if (mainSolrServer != null) {
			mainSolrServer.shutdown();
		}

		for (SolrServer solrServer : coreServers.values()) {
			solrServer.shutdown();
		}
		coreServers.clear();
	}
	
	private static SolrServer setHttpSolrServer(String coreName, String solrServerUrl) {
		HttpSolrServer solrServer = new HttpSolrServer(solrServerUrl + "/" + coreName);
		solrServer.setConnectionTimeout(ConstellioSpringUtils.getSolrServerConTimeout());
		solrServer.setSoTimeout(ConstellioSpringUtils.getSolrServerSoTimeout());
		coreServers.put(coreName, solrServer);
		return solrServer;
	}

	private static SolrServer getSolrServer(String coreName) {
		return coreServers.get(coreName);
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
