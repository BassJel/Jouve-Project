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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.derby.tools.sysinfo;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.cloud.ClusterState;
import org.apache.solr.common.cloud.OnReconnect;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.params.CollectionParams.CollectionAction;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.apache.solr.common.util.NamedList;
import org.apache.zookeeper.ZooKeeper;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.opensearch.OpenSearchSolrServer;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class SolrCoreContext {

	public static final String DEFAULT_COLLECTION_NAME = "_constellio_default";

	private static Map<String, SolrServer> coreServers = new HashMap<String, SolrServer>();
	private static Set<String> userCoreNames = new HashSet<String>();

	private static CloudSolrServer mainSolrServer;
	private static SolrZkClient zkClient;

	public static synchronized void init() {
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

		if (zkClient == null) {
			zkClient = new SolrZkClient(ConstellioSpringUtils.getZooKeeperAddress(), ConstellioSpringUtils.getZooKeeperClientTimeout(), ConstellioSpringUtils.getZooKeeperConTimeout(),
					new OnReconnect() {
						@Override
						public void command() {
						}
					});
		}
	}

	public static void main(String args[]) {
		HttpSolrServer server = new HttpSolrServer("http://localhost:8983/solr");
		server.setConnectionTimeout(1000);
		server.setSoTimeout(1000);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			server.ping();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static synchronized void initCores() {
		try {

			// do not use CoreAdminRequest
			Set<String> collectionNameSet = new HashSet<String>();
			mainSolrServer.connect();
			ZkStateReader reader = mainSolrServer.getZkStateReader();
			ClusterState state = reader.getClusterState();

			// do synchronization between coreServers and solrCloud
			for (String collectionName : state.getCollections()) {
				if (!collectionName.startsWith("_") || DEFAULT_COLLECTION_NAME.equals(collectionName)) {
					// "_xxx" is for system only, not for users
					collectionNameSet.add(collectionName);
				}
			}

			Map<String, String> aliasMap = reader.getAliases().getCollectionAliasMap();
			if (aliasMap != null) {
				for (String aliasName : aliasMap.keySet()) {
					if (!aliasName.startsWith("_")) {
						// "_xxx" is for system only, not for users
						collectionNameSet.remove(aliasMap.get(aliasName));
						collectionNameSet.add(aliasName);
					}
				}
			}

			for (String collectionName : collectionNameSet) {
				if (!coreServers.containsKey(collectionName)) {
					setHttpSolrServer(collectionName, ConstellioSpringUtils.getSolrServerAddress());
				}
			}

			userCoreNames.clear();
			Iterator<Map.Entry<String, SolrServer>> iter = coreServers.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, SolrServer> entry = iter.next();
				if (!collectionNameSet.contains(entry.getKey())) {
					entry.getValue().shutdown();
					iter.remove();
				}
				else if(!DEFAULT_COLLECTION_NAME.equals(entry.getKey())){
					userCoreNames.add(entry.getKey());
				}
			}
			

			// CoreAdminRequest adminRequest = new CoreAdminRequest();
			// adminRequest.setAction(CoreAdminAction.STATUS);
			// CoreAdminResponse adminResponse =
			// adminRequest.process(solrServer);
			// NamedList<NamedList<Object>> coreStatus =
			// adminResponse.getCoreStatus();
			// for (Object core : coreStatus) {
			// String coreName = StringUtils.substringBefore(core.toString(),
			// "=");
			// if (!coreName.startsWith("_"))// "_xxx" is for system only, like
			// "_log", "_fetch_db"
			// setHttpSolrServer(coreName, ((HttpSolrServer)
			// solrServer).getBaseURL());
			// }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static synchronized void shutdown() {
		if (mainSolrServer != null) {
			mainSolrServer.shutdown();
		}

		for (SolrServer solrServer : coreServers.values()) {
			solrServer.shutdown();
		}
		coreServers.clear();

		if (zkClient != null) {
			zkClient.close();
		}
	}

	private static SolrServer setHttpSolrServer(String coreName, String solrServerUrl) {
		HttpSolrServer solrServer = new HttpSolrServer(solrServerUrl + "/" + coreName);
		solrServer.setConnectionTimeout(ConstellioSpringUtils.getSolrServerConTimeout());
		solrServer.setSoTimeout(ConstellioSpringUtils.getSolrServerSoTimeout());
		coreServers.put(coreName, solrServer);
		return solrServer;
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
		if (collection != null) {
			return getSolrServer(collection);
		} else {
			return coreServers.get(collectionName);
		}
	}

	// Pour tests sans collection
	public static SolrServer getSolrServerUtil(String coreName) {
		return coreServers.get(coreName);
	}

	public static synchronized CloudSolrServer getMainSolrServer() {
		mainSolrServer.connect();
		return mainSolrServer;
	}

	public static SolrZkClient getSolrZkClient() {
		return zkClient;
	}

	public static Set<String> getCoreNames() {
		return userCoreNames;
	}
}
