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
package com.doculibre.constellio.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.cloud.ZkController;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.tika.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.BaseElement;

import com.doculibre.constellio.entities.Analyzer;
import com.doculibre.constellio.entities.AnalyzerClass;
import com.doculibre.constellio.entities.AnalyzerFilter;
import com.doculibre.constellio.entities.Cache;
import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.ConnectorTypeMetaMapping;
import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.entities.FieldTypeClass;
import com.doculibre.constellio.entities.FilterClass;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SolrConfig;
import com.doculibre.constellio.entities.TokenizerClass;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.utils.ClasspathUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class SolrServicesImpl implements SolrServices {

	public static void main(String[] args) {

		try {
			SolrCoreContext.init();
			// RecordCollection collection=new RecordCollection();
			// collection.setName("test");
			// SolrServer solrServer=SolrCoreContext.getSolrServer(collection);
			// SolrQuery query=new SolrQuery();
			// query.setQuery("doculibre");
			// // query.set("enableElevation", "true");
			// query.setFields("doc_url,[elevated]");
			// query.set("shards.qt", "/elevate");
			// query.setRequestHandler("/elevate");
			// QueryResponse queryResponse=solrServer.query(query);
			// long numFound = queryResponse.getResults().getNumFound();
			// System.out.println(numFound);

			// byte[] data =
			// SolrCoreContext.getSolrZkClient().getData(ZkController.CONFIGS_ZKNODE
			// + "/" +
			// "test" + "/" + "schema.xml", null, null, true);
			// File myTempFile = Files.createTempFile("config_",
			// ".tmp").toFile();
			// FileOutputStream outputStream = new FileOutputStream(myTempFile);
			// outputStream.write(data);
			// outputStream.close();
			// System.out.println(myTempFile.getAbsolutePath());
			// CloudSolrServer mainSolrServer = new
			// CloudSolrServer("localhost:9983");
			// mainSolrServer.setZkClientTimeout(3000);
			// mainSolrServer.setZkConnectTimeout(3000);
			// SolrCoreContext.init();
			// RecordCollection collection = new RecordCollection();
			// collection.setName("test");
			// SolrServer solrServer =
			// SolrCoreContext.getSolrServer(collection);
			// SolrQuery query = new SolrQuery();
			// // query.setRequestHandler("/admin/luke");
			// // query.setParam("show", "schema");
			// query.setParam(CommonParams.Q, "*:*");
			// query.setParam(CommonParams.ROWS, "0");
			// try {
			// QueryResponse queryResponse = solrServer.query(query);
			// long numFound = queryResponse.getResults().getNumFound();
			// System.out.println(numFound);
			// } catch (SolrServerException e) {
			// throw new RuntimeException(e);
			// }

			// ModifiableSolrParams params = new ModifiableSolrParams();
			// params.set(CommonParams.QT, "/admin/collections");
			// params.set(CommonParams.ACTION, "RELOAD");
			// params.set("name", "bbbb");
			// QueryResponse response=mainSolrServer.query(params);
			// System.out.println(response.getResponseHeader());
			// HttpSolrServer solrServer=new
			// HttpSolrServer("http://192.168.88.1:8983/solr/bbbb");
			// ModifiableSolrParams solrParams = new ModifiableSolrParams();
			// solrParams.set("collectionName", "bbbb");
			// solrParams.set("q", "doc_url:*");
			// solrParams.set("start", 0);
			// solrParams.set("rows", 10);
			// QueryResponse response = solrServer.query(solrParams);
			// System.out.println(response.getResults().size());

			// CloudSolrServer
			// cloudSolrServer=(CloudSolrServer)SolrCoreContext.getMainSolrServer();
			// cloudSolrServer.setDefaultCollection("tttt");
			// ModifiableSolrParams params = new ModifiableSolrParams();
			// params.set(CommonParams.QT, "/admin/collections");
			// params.set(CommonParams.ACTION, "RELOAD");
			// params.set("name", "bbbb");
			// QueryResponse response=cloudSolrServer.query(params);
			// System.out.println(response.getResponseHeader().toString());
			// // Thread.sleep(1000);
			// ModifiableSolrParams solrParams = new ModifiableSolrParams();
			// solrParams.set("collectionName", "bbbb");
			// solrParams.set("q", "doc_url:*");
			// solrParams.set("start", 0);
			// solrParams.set("rows", 10);
			// response = cloudSolrServer.query(solrParams);
			// System.out.println(response.getResults().size());

			HttpSolrServer solrServer = new HttpSolrServer("http://ec2-54-204-186-243.compute-1.amazonaws.com:8983/solr/gouv");
			SolrInputDocument doc = new SolrInputDocument();
			doc.setField("doc_uniqueKey", "rouleroule");
			doc.setField("ministereOrganisme", "testministereOrganisme");
			doc.setField("sousCollection", "testsousCollection");
			solrServer.add(doc);

			solrServer.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// try {
		// SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
		// byte[] data = zkClient.getData(ZkController.CONFIGS_ZKNODE + "/" +
		// "abc" + "/" + "schema.xml", null, null, true);
		// File myTempFile = Files.createTempFile("config_", ".tmp").toFile();
		// FileOutputStream outputStream = new FileOutputStream(myTempFile);
		// outputStream.write(data);
		// outputStream.close();
		// System.out.println(myTempFile.getAbsolutePath());
		// } catch (KeeperException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// ModifiableSolrParams params = new ModifiableSolrParams();
		// params.set(CommonParams.QT, "/replication");
		// params.set("command", "details");
		//
		//
		// CloudSolrServer cloudSolrServer =
		// (CloudSolrServer)SolrCoreContext.getMainSolrServer();
		// ClusterState
		// clusterState=cloudSolrServer.getZkStateReader().getClusterState();
		// StringBuffer finalSize=new StringBuffer();
		// for(Slice slice:clusterState.getActiveSlices("abc")){
		// Replica replica=clusterState.getLeader("abc", slice.getName());
		// System.out.println(slice.getName()+"!!!"+replica);
		// StringUtils.substringBefore(replica.getNodeName(), "_");
		// HttpSolrServer solrServer= new
		// HttpSolrServer("http://"+StringUtils.substringBefore(replica.getNodeName(),
		// "_")+"/solr"+"/abc");
		// try {
		// QueryResponse response = solrServer.query(params);
		// finalSize.append(((NamedList)response.getResponse().get("details")).get("indexSize")+",");
		// } catch (SolrServerException e) {
		// e.printStackTrace();
		// }
		// }
		// System.out.println(finalSize);

		// SolrCoreContext.init();
		// SolrServicesImpl impl = new SolrServicesImpl();
		// RecordCollection rc = new RecordCollection();
		// rc.setName("abc");
		// String collectionName = rc.getName();
		// try {
		// SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
		// byte[] data = zkClient.getData(ZkController.CONFIGS_ZKNODE + "/" +
		// collectionName + "/schema.xml", null, null, true);
		// Document solrconfigXmlDocument = new SAXReader().read(new
		// ByteArrayInputStream(data));
		//
		// Element rootElement = solrconfigXmlDocument.getRootElement();
		// rootElement.addAttribute("name", collectionName);
		//
		// OutputFormat format = OutputFormat.createPrettyPrint();
		// ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// XMLWriter writer = new XMLWriter(outputStream, format);
		// writer.write("");
		// writer.close();
		//
		// zkClient.setData(ZkController.CONFIGS_ZKNODE + "/" + collectionName +
		// "/schema.xml", outputStream.toByteArray(), true);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// impl.ensureCore(rc);

		// try {
		// SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
		// File myTempDir = Files.createTempDirectory("constellio_").toFile();
		// ZkController.downloadConfigDir(zkClient,"adsf", myTempDir);
		// System.out.println(myTempDir.getAbsolutePath());

		// CloudSolrServer solrServer = new
		// CloudSolrServer("localhost:9983");
		// solrServer.setDefaultCollection("abc");

		// SolrServicesImpl impl = new SolrServicesImpl();
		// RecordCollection collection = new RecordCollection();
		// collection.setName("def");
		// SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
		// File myTempDir =
		// Files.createTempDirectory("constellio_").toFile();
		// ZkController.downloadConfigDir(zkClient, "abc", myTempDir);
		// // we do not change the "name" value in schema.xml, it is not
		// // very important
		// ZkController.uploadConfigDir(zkClient, myTempDir, "def");
		// FileUtils.deleteDirectory(myTempDir);
		//
		//
		// ModifiableSolrParams params = new ModifiableSolrParams();
		// params.set(CommonParams.QT, "/admin/collections");
		// params.set(CommonParams.ACTION, "CREATE");
		// params.set("name", "abc");
		// params.set("numShards", 2);
		// SolrCoreContext.getMainSolrServer().query(params);
		// Thread.sleep(10000);
		// SolrCoreContext.initCores();

		// HttpSolrServer solrServer = (HttpSolrServer)
		// SolrCoreContext.getSolrServer(collection);
		// ModifiableSolrParams solrParams = new ModifiableSolrParams();
		// solrParams.set("collectionName", "def");
		// solrParams.set("q", "*:*");
		// solrParams.set("start", 0);
		// solrParams.set("rows", 10);
		// QueryResponse response = solrServer.query(solrParams);
		// System.out.println(response.getResults().size());

		// SolrInputDocument doc = new SolrInputDocument();
		// int count = 20000;
		// while (count < 25000) {
		// doc.setField("id", count++);
		// solrServer.add(doc);
		// if (count % 10 == 0)
		// solrServer.commit();
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// HttpSolrServer solrServer =new
		// HttpSolrServer("http://localhost:8900/solr");
		// CoreAdminRequest adminRequest = new CoreAdminRequest();
		// adminRequest.setAction(CoreAdminAction.STATUS);
		// CoreAdminResponse adminResponse = adminRequest.process(solrServer);
		// NamedList<NamedList<Object>> coreStatus = adminResponse
		// .getCoreStatus();
		// for (Object core : coreStatus) {
		// System.out.println(core);
		// }
		// }catch(Exception e){
		// e.printStackTrace();
		// }
		// try {
		// CloudSolrServer solrServer=new CloudSolrServer("localhost:9983");
		// solrServer.setDefaultCollection("myCollection4");

		// SolrZkClient zkClient=SolrCoreContext.getSolrZkClient();
		// try {
		// File myTempDir = Files.createTempDirectory("constellio_").toFile();
		// ZkController.downloadConfigDir(zkClient,
		// SolrCoreContext.getDefaultCoreName(), myTempDir);
		// ZkController.uploadConfigDir(zkClient, myTempDir, "myCollection");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

	}

	// if core does not exist, we will create it
	private void ensureCore(RecordCollection collection) {
		// SolrCoreContext.initCores();
		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		if (solrServer == null) {// create a new core
			int numReplicationFactor = ConstellioSpringUtils.getSolrReplicationFactor();
			createCollectionInCloud(collection.getName(), null, collection.getNumShards(), numReplicationFactor);
			SolrCoreContext.initCores();
		}
	}

	@Override
	public SolrServer initCore(RecordCollection collection) {
		ensureCore(collection);
		reloadCollectionInCloud(collection.getName());
		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		return solrServer;
	}

	public void cleanCores() {
		// FIXME: not considering opensearch()
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		List<String> deleteCollectionList = new ArrayList<>();
		for (String coreName : SolrCoreContext.getCoreNames()) {
			deleteCollectionList.add(coreName);
		}

		for (RecordCollection collection : collectionServices.list()) {
			if (deleteCollectionList.contains(collection.getName())) {
				deleteCollectionList.remove(collection.getName());
			}
		}

		for (String coreName : deleteCollectionList) {
			deleteCore(coreName);
		}
	}

	@Override
	public synchronized void deleteCore(RecordCollection collection) {
		deleteCore(collection.getName());
	}

	@Override
	public synchronized void deleteCore(String collectionName) {
		// SolrCoreContext.initCores();
		SolrServer solrServer = SolrCoreContext.getSolrServer(collectionName);
		if (solrServer != null) {
			if (isAliasInCloud(collectionName)) {
				String realCollectionName = getRealCollectionInCloud(collectionName);
				deleteAliasInCloud(collectionName);
				deleteCollectionInCloud(realCollectionName);
				SolrCoreContext.initCores();
			} else {
				deleteCollectionInCloud(collectionName);
				SolrCoreContext.initCores();
			}
		}
	}

	public void updateConfig() {
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		// Default
		updateConfig(null);
		for (RecordCollection collection : collectionServices.list()) {
			updateConfig(collection);
		}
	}

	private void updateConfig(RecordCollection collection) {
		
		SolrConfig config = collection.getSolrConfiguration();
		SolrConfigServices solrConfigServices = ConstellioSpringUtils.getSolrConfigServices();
		SolrConfig defaultConfig = solrConfigServices.getDefaultConfig();

			if (config == null) {
				config = defaultConfig;
			}
			ensureCore(collection);
			String collectionName = collection.getName();
		

		Document solrConfigDocument = readSolrConfig(collection);
		Element rootElement = solrConfigDocument.getRootElement();
		Element queryElement = rootElement.element("query");
		if (queryElement == null) {
			queryElement = rootElement.addElement("query");
		}
		updateNotNullConfigCache(queryElement, "fieldValueCache", (Cache) getPropertyValue(config, defaultConfig, "fieldValueCacheConfig"));
		updateNotNullConfigCache(queryElement, "filterCache", (Cache) getPropertyValue(config, defaultConfig, "filterCacheConfig"));
		updateNotNullConfigCache(queryElement, "queryResultCache", (Cache) getPropertyValue(config, defaultConfig, "queryResultCacheConfig"));
		updateNotNullConfigCache(queryElement, "documentCache", (Cache) getPropertyValue(config, defaultConfig, "documentCacheConfig"));

		addNotNullElement(queryElement, "useFilterForSortedQuery", getPropertyValue(config, defaultConfig, "useFilterForSortedQuery"));
		addNotNullElement(queryElement, "queryResultWindowSize", getPropertyValue(config, defaultConfig, "queryResultWindowSize"));
		Element hashDocSet = queryElement.element("HashDocSet");
		if (hashDocSet != null) {
			queryElement.remove(hashDocSet);
		}
		Object hashDocSetMaxSize = getPropertyValue(config, defaultConfig, "hashDocSetMaxSize");
		Object hashDocSetLoadFactor = getPropertyValue(config, defaultConfig, "hashDocSetLoadFactor");
		if (hashDocSetMaxSize != null || hashDocSetLoadFactor != null) {
			hashDocSet = queryElement.addElement("HashDocSet");
			addNotNullAttribute(hashDocSet, "maxSize", hashDocSetMaxSize);
			addNotNullAttribute(hashDocSet, "loadFactor", hashDocSetLoadFactor);
		}
		writeSolrConfig(collection, solrConfigDocument);
		initCore(collection);

	}

	private void updateNotNullConfigCache(Element rootElement, String cacheType, Cache cache) {
		removeAll(rootElement, cacheType);
		if (cache != null) {
			Element cacheElement = rootElement.addElement(cacheType);
			addNotNullAttribute(cacheElement, "class", cache.getCacheClass());
			addNotNullAttribute(cacheElement, "size", cache.getSize());
			addNotNullAttribute(cacheElement, "initialSize", cache.getInitialSize());
			addNotNullAttribute(cacheElement, "autowarmCount", cache.getAutowarmCount());
			addNotNullAttribute(cacheElement, "acceptableSize", cache.getAcceptableSize());
			addNotNullAttribute(cacheElement, "minSize", cache.getMinSize());
			addNotNullAttribute(cacheElement, "regenerator", cache.getRegeneratorClass());
			addNotNullAttribute(cacheElement, "cleanupThread", cache.isCleanupThread());
		}
	}

	@SuppressWarnings("unchecked")
	public static void removeAll(Element element, String code) {
		for (Iterator<Element> it = element.elementIterator(code); it.hasNext();) {
			it.next();
			it.remove();
		}
	}

	public static Document readXMLConfigInCloud(String collectionName, String fileName) {
		String realCollectionName;
		if (SolrServicesImpl.isAliasInCloud(collectionName)) {
			realCollectionName = SolrServicesImpl.getRealCollectionInCloud(collectionName);
		} else {
			realCollectionName = collectionName;
		}
		Document schemaDocument = null;
		try {
			SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
			byte[] data = zkClient.getData(ZkController.CONFIGS_ZKNODE + "/" + realCollectionName + "/" + fileName, null, null, true);
			schemaDocument = new SAXReader(false).read(new ByteArrayInputStream(data));
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return schemaDocument;
	}

	public static void writeXMLConfigInCloud(String collectionName, String fileName, Document schemaDocument) {
		String realCollectionName;
		if (SolrServicesImpl.isAliasInCloud(collectionName)) {
			realCollectionName = SolrServicesImpl.getRealCollectionInCloud(collectionName);
		} else {
			realCollectionName = collectionName;
		}
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			XMLWriter writer = new XMLWriter(outputStream, format);
			writer.write(schemaDocument);
			writer.close();
			SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
			zkClient.setData(ZkController.CONFIGS_ZKNODE + "/" + realCollectionName + "/" + fileName, outputStream.toByteArray(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static File readPlainConfigInCloud(String collectionName, String fileName) {
		String realCollectionName;
		if (SolrServicesImpl.isAliasInCloud(collectionName)) {
			realCollectionName = SolrServicesImpl.getRealCollectionInCloud(collectionName);
		} else {
			realCollectionName = collectionName;
		}
		File myTempFile = null;
		try {
			SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
			byte[] data = zkClient.getData(ZkController.CONFIGS_ZKNODE + "/" + realCollectionName + "/" + fileName, null, null, true);
			myTempFile = Files.createTempFile("config_", ".temp").toFile();
			FileOutputStream outputStream = new FileOutputStream(myTempFile);
			outputStream.write(data);
			outputStream.close();
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return myTempFile;
	}

	public static byte[] readPlainConfigInCloud(String fileName) throws KeeperException, InterruptedException {
		SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
		if (zkClient.exists(ZkController.CONFIGS_ZKNODE + "/" + fileName, true)) {
			byte[] data = zkClient.getData(ZkController.CONFIGS_ZKNODE + "/" + fileName, null, null, true);
			return data;
		}
		return null;
	}

	public static void writePlainConfigInCloud(String collectionName, String fileName, File file) {
		String realCollectionName;
		if (SolrServicesImpl.isAliasInCloud(collectionName)) {
			realCollectionName = SolrServicesImpl.getRealCollectionInCloud(collectionName);
		} else {
			realCollectionName = collectionName;
		}
		try {
			SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
			zkClient.setData(ZkController.CONFIGS_ZKNODE + "/" + realCollectionName + "/" + fileName, Files.readAllBytes(file.toPath()), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writePlainConfigInCloud(String fileName, byte[] ouput) {
		try {
			SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
			if (!zkClient.exists(ZkController.CONFIGS_ZKNODE + "/" + fileName, true)) {
				zkClient.create(ZkController.CONFIGS_ZKNODE + "/" + fileName, ouput, CreateMode.PERSISTENT, true);
			} else {
				zkClient.setData(ZkController.CONFIGS_ZKNODE + "/" + fileName, ouput, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Document readSchema(File schemaFile) {
		Document schemaDocument;
		if (schemaFile.exists()) {
			try {
				schemaDocument = new SAXReader(false).read(schemaFile);
			} catch (DocumentException e) {
				throw new RuntimeException(e);
			}
		} else {
			schemaDocument = null;
		}
		return schemaDocument;
	}

	private static Document readSchema(String collectionName) {
		if (SolrCoreContext.DEFAULT_COLLECTION_NAME.equals(collectionName)) {
			return readConstellioDefaultSchema();
		}
		return readXMLConfigInCloud(collectionName, "schema.xml");
	}
	private static void writeSchema(String collectionName, Document schemaDocument) {
		if (SolrCoreContext.DEFAULT_COLLECTION_NAME.equals(collectionName)) {
			writeConstellioDefaultSchema(schemaDocument);
			return;
		}
		writeXMLConfigInCloud(collectionName, "schema.xml", schemaDocument);
	}

	private static Document readSolrConfig(RecordCollection collection) {
		String collectionName = collection.getName();
		return readXMLConfigInCloud(collectionName, "solrconfig.xml");
	}

	private static void writeSolrConfig(RecordCollection collection, Document solrConfigDocument) {
		String collectionName = collection.getName();
		writeXMLConfigInCloud(collectionName, "solrconfig.xml", solrConfigDocument);
	}

	public static synchronized void createCollectionInCloud(String collectionName, String configName, int numShards, int replicationFactor) {
		try {
			SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
			
			File myTempDir = Files.createTempDirectory("constellio_").toFile();
			try {
				ZkController.downloadConfigDir(zkClient, configName, myTempDir);
			} catch (NoNodeException nne) {
				//
			}	
			
			if (myTempDir.list().length == 0) {
				File localCollectionsDir = new File(ClasspathUtils.getCollectionsRootDir(), collectionName);
				if (!localCollectionsDir.exists()) {
					localCollectionsDir = new File(ClasspathUtils.getCollectionsRootDir(), SolrCoreContext.DEFAULT_COLLECTION_NAME);
				}
				FileUtils.copyDirectory(new File(localCollectionsDir, "conf"), myTempDir);
			}
			
			ZkController.uploadConfigDir(zkClient, myTempDir, collectionName);
			FileUtils.deleteDirectory(myTempDir);

			CloudSolrServer cloudSolrServer = (CloudSolrServer) SolrCoreContext.getMainSolrServer();
//			int liveNodeNum = cloudSolrServer.getZkStateReader().getClusterState().getLiveNodes().size();
//			// we assume that maxShardsPerNode=1 FIXME ...
//			if (numShards <= 0 || replicationFactor <= 0 || numShards * replicationFactor > liveNodeNum) {
//				numShards = liveNodeNum;
//				replicationFactor = 1;
//			}
			// we use collection REST API, not CoreAdmin
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set(CommonParams.QT, "/admin/collections");
			params.set(CommonParams.ACTION, "CREATE");
			params.set("name", collectionName);
			params.set("numShards", numShards);
			params.set("replicationFactor", replicationFactor);
			cloudSolrServer.query(params);
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static synchronized void deleteCollectionInCloud(String collectionName) {
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set(CommonParams.QT, "/admin/collections");
			params.set(CommonParams.ACTION, "DELETE");
			params.set("name", collectionName);
			SolrCoreContext.getMainSolrServer().query(params);

			SolrZkClient zkClient = SolrCoreContext.getSolrZkClient();
			zkClient.clean(ZkController.CONFIGS_ZKNODE + "/" + collectionName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static synchronized void reloadCollectionInCloud(String collectionName) {
		String realCollectionName;
		if (SolrServicesImpl.isAliasInCloud(collectionName)) {
			realCollectionName = SolrServicesImpl.getRealCollectionInCloud(collectionName);
		} else {
			realCollectionName = collectionName;
		}
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set(CommonParams.QT, "/admin/collections");
			params.set(CommonParams.ACTION, "RELOAD");
			params.set("name", realCollectionName);
			CloudSolrServer mainSolrServer = SolrCoreContext.getMainSolrServer();
			QueryResponse response = mainSolrServer.query(params);
			System.out.println(response.getResponse().toString());

			// mainSolrServer = new CloudSolrServer("localhost:9983");
			// mainSolrServer.setZkClientTimeout(3000);
			// mainSolrServer.setZkConnectTimeout(3000);
			// params = new ModifiableSolrParams();
			// params.set(CommonParams.QT, "/admin/collections");
			// params.set(CommonParams.ACTION, "RELOAD");
			// params.set("name", collectionName);
			// response=mainSolrServer.query(params);
			// System.out.println(response.getResponse().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static synchronized void createAliasInCloud(String aliasName, String collectionName) {
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set(CommonParams.QT, "/admin/collections");
			params.set(CommonParams.ACTION, "CREATEALIAS");
			params.set("name", aliasName);
			params.set("collections", collectionName);
			SolrCoreContext.getMainSolrServer().query(params);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}

	public static synchronized void deleteAliasInCloud(String aliasName) {
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set(CommonParams.QT, "/admin/collections");
			params.set(CommonParams.ACTION, "DELETEALIAS");
			params.set("name", aliasName);
			SolrCoreContext.getMainSolrServer().query(params);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}

	public static String getRealCollectionInCloud(String aliasName) {
		CloudSolrServer cloudSolrServer = (CloudSolrServer) SolrCoreContext.getMainSolrServer();
		Map<String, String> aliasMap = cloudSolrServer.getZkStateReader().getAliases().getCollectionAliasMap();
		return aliasMap.get(aliasName);
	}

	public static boolean isAliasInCloud(String aliasName) {
		CloudSolrServer cloudSolrServer = (CloudSolrServer) SolrCoreContext.getMainSolrServer();
		Map<String, String> aliasMap = cloudSolrServer.getZkStateReader().getAliases().getCollectionAliasMap();
		if (aliasMap != null && aliasMap.containsKey(aliasName)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized void updateSchemaFields(RecordCollection collection) {
		ensureCore(collection);
		Document schemaDocument = readSchema(collection.getName());
		if (schemaDocument != null) {
			updateSchemaFields(collection, schemaDocument);
		}
	}

	@SuppressWarnings("unchecked")
	private void updateSchemaFields(RecordCollection collection, Document schemaDocument) {
		Element fieldsElement = schemaDocument.getRootElement().element("fields");
		List<Element> fieldElements = fieldsElement.elements("field");
		for (Iterator<Element> it = fieldElements.iterator(); it.hasNext();) {
			it.next();
			it.remove();
		}

		List<Element> copyFields = schemaDocument.getRootElement().elements("copyField");
		for (Iterator<Element> it = copyFields.iterator(); it.hasNext();) {
			it.next();
			it.remove();
		}

		List<Element> dynamicFieldElements = fieldsElement.elements("dynamicField");
		for (Iterator<Element> it = dynamicFieldElements.iterator(); it.hasNext();) {
			it.next();
			it.remove();
		}

		List<String> addedFieldNames = new ArrayList<String>();
		Collection<IndexField> indexFields = collection.getIndexFields();
		for (IndexField indexField : indexFields) {
			if (!addedFieldNames.contains(indexField.getName())) {
				addedFieldNames.add(indexField.getName());

				FieldType fieldType = indexField.getFieldType();
				Analyzer analyzer = indexField.getAnalyzer();
				if (fieldType == null) {
					throw new RuntimeException(indexField.getName() + " has no type");
				}
				Element fieldElement;
				if (indexField.isDynamicField()) {
					fieldElement = DocumentHelper.createElement("dynamicField");
				} else {
					fieldElement = DocumentHelper.createElement("field");
				}
				fieldsElement.add(fieldElement);
				addNotNullAttribute(fieldElement, "name", indexField.getName());
				addNotNullAttribute(fieldElement, "type", fieldType.getName());
				addNotNullAttribute(fieldElement, "indexed", indexField.isIndexed());
				addNotNullAttribute(fieldElement, "stored", true);
				addNotNullAttribute(fieldElement, "multiValued", indexField.isMultiValued());
				if (analyzer != null) {
					fieldElement.addAttribute("analyzer", analyzer.getAnalyzerClass().getClassName());
				}
			}
		}

		// used by solrcloud
		Element fieldElement = DocumentHelper.createElement("field");
		addNotNullAttribute(fieldElement, "name", "_version_");
		addNotNullAttribute(fieldElement, "type", "long");
		addNotNullAttribute(fieldElement, "indexed", true);
		addNotNullAttribute(fieldElement, "stored", true);
		fieldsElement.add(fieldElement);

		List<Element> uniqueKeyElements = schemaDocument.getRootElement().elements("uniqueKey");
		for (Iterator<Element> it = uniqueKeyElements.iterator(); it.hasNext();) {
			it.next();
			it.remove();
		}
		IndexField uniqueKeyField = collection.getUniqueKeyIndexField();
		if (uniqueKeyField != null) {
			Element uniqueKeyElement = DocumentHelper.createElement("uniqueKey");
			uniqueKeyElement.setText(uniqueKeyField.getName());
			schemaDocument.getRootElement().add(uniqueKeyElement);
		}

		List<Element> defaultSearchFieldElements = schemaDocument.getRootElement().elements("defaultSearchField");
		for (Iterator<Element> it = defaultSearchFieldElements.iterator(); it.hasNext();) {
			it.next();
			it.remove();
		}
		IndexField defaultSearchField = collection.getDefaultSearchIndexField();
		if (defaultSearchField != null) {
			Element defaultSearchFieldElement = DocumentHelper.createElement("defaultSearchField");
			defaultSearchFieldElement.setText(defaultSearchField.getName());
			schemaDocument.getRootElement().add(defaultSearchFieldElement);
		}

		List<Element> solrQueryParserElements = schemaDocument.getRootElement().elements("solrQueryParser");
		for (Iterator<Element> it = solrQueryParserElements.iterator(); it.hasNext();) {
			it.next();
			it.remove();
		}
		String queryParserOperator = collection.getQueryParserOperator();
		if (queryParserOperator != null) {
			Element solrQueryParserElement = DocumentHelper.createElement("solrQueryParser");
			solrQueryParserElement.addAttribute("defaultOperator", queryParserOperator);
			schemaDocument.getRootElement().add(solrQueryParserElement);
		}

		for (IndexField indexField : indexFields) {
			if (!indexField.isDynamicField()) {
				for (CopyField copyFieldDest : indexField.getCopyFieldsDest()) {
					Element copyFieldElement = DocumentHelper.createElement("copyField");
					String source;
					if (copyFieldDest.isSourceAllFields()) {
						source = "*";
					} else {
						IndexField indexFieldSource = copyFieldDest.getIndexFieldSource();
						source = indexFieldSource.getName();
					}
					copyFieldElement.addAttribute("source", source);
					copyFieldElement.addAttribute("dest", copyFieldDest.getIndexFieldDest().getName());
					addNotNullAttribute(copyFieldElement, "maxChars", copyFieldDest);
					// Ajout Rida
					schemaDocument.getRootElement().add(copyFieldElement);
				}
			}
		}

		writeSchema(collection.getName(), schemaDocument);
	}

	@Override
	public synchronized void initSchemaFieldTypes() {
		Document schemaDocument = readConstellioDefaultSchema();
		if (schemaDocument == null) {
			throw new RuntimeException("Schema not found");
		}
		createMissingFieldTypes(schemaDocument);
	}
	
	private static Document readConstellioDefaultSchema() {
		Document schemaDocument = readSchema(new File(ClasspathUtils.getCollectionsRootDir() + File.separator + SolrCoreContext.DEFAULT_COLLECTION_NAME + File.separator + "conf" + File.separator + "schema.xml"));
		return schemaDocument;
	}
	
	private static void writeConstellioDefaultSchema(Document schemaDocument) {
		File schemaFile = new File(ClasspathUtils.getCollectionsRootDir() + File.separator + SolrCoreContext.DEFAULT_COLLECTION_NAME + File.separator + "conf" + File.separator + "schema.xml");
		FileOutputStream fos = null;
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			fos = new FileOutputStream(schemaFile);
			XMLWriter writer = new XMLWriter(fos, format);
			writer.write(schemaDocument);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

	@Override
	public synchronized void updateSchemaFieldTypes() {
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		// Default
		updateSchemaFieldTypes(null);
		for (RecordCollection collection : collectionServices.list()) {
			if (!collection.isOpenSearch()) {
				updateSchemaFieldTypes(collection);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateSchemaFieldTypes(RecordCollection collection) {
		String collectionName = SolrCoreContext.DEFAULT_COLLECTION_NAME;
		if (collection != null) {
			collectionName = collection.getName();
		}
		Document schemaDocument = readSchema(collectionName);
		Element typesElement = schemaDocument.getRootElement().element("types");
		List<Element> fieldTypeElements = typesElement.elements("fieldType");
		for (Iterator<Element> it = fieldTypeElements.iterator(); it.hasNext();) {
			it.next();
			it.remove();
		}
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
		for (FieldType fieldType : fieldTypeServices.list()) {
			Element fieldTypeElement = DocumentHelper.createElement("fieldType");
			typesElement.add(fieldTypeElement);
			FieldTypeClass fieldTypeClass = fieldType.getFieldTypeClass();

			addNotNullAttribute(fieldTypeElement, "name", fieldType);
			addNotNullAttribute(fieldTypeElement, "class", fieldTypeClass.getClassName());
			addNotNullAttribute(fieldTypeElement, "indexed", fieldType);
			// Always true
			addNotNullAttribute(fieldTypeElement, "stored", true);
			addNotNullAttribute(fieldTypeElement, "multiValued", fieldType);
			addNotNullAttribute(fieldTypeElement, "omitNorms", fieldType);
			addNotNullAttribute(fieldTypeElement, "precisionStep", fieldType);
			addNotNullAttribute(fieldTypeElement, "positionIncrementGap", fieldType);
			addNotNullAttribute(fieldTypeElement, "sortMissingLast", fieldType);

			Analyzer analyzer = fieldType.getAnalyzer();
			Analyzer queryAnalyzer = fieldType.getQueryAnalyzer();

			if (analyzer != null) {
				Element analyzerElement = DocumentHelper.createElement("analyzer");
				fieldTypeElement.add(analyzerElement);
				if (queryAnalyzer != null) {
					analyzerElement.addAttribute("type", "index");
				}

				AnalyzerClass analyzerClass = analyzer.getAnalyzerClass();
				if (analyzerClass != null) {
					analyzerElement.addAttribute("class", analyzerClass.getClassName());
				}

				TokenizerClass tokenizerClass = analyzer.getTokenizerClass();
				if (tokenizerClass != null) {
					Element tokenizerElement = DocumentHelper.createElement("tokenizer");
					analyzerElement.add(tokenizerElement);
					tokenizerElement.addAttribute("class", tokenizerClass.getClassName());
				}

				Collection<AnalyzerFilter> filters = analyzer.getFilters();
				for (AnalyzerFilter filter : filters) {
					FilterClass filterClass = filter.getFilterClass();
					Element filterElement = DocumentHelper.createElement("filter");
					analyzerElement.add(filterElement);
					filterElement.addAttribute("class", filterClass.getClassName());
					addNotNullAttribute(filterElement, "catenateAll", filter);
					addNotNullAttribute(filterElement, "catenateWords", filter);
					addNotNullAttribute(filterElement, "delimiter", filter);
					addNotNullAttribute(filterElement, "enablePositionIncrements", filter);
					addNotNullAttribute(filterElement, "encoder", filter);
					addNotNullAttribute(filterElement, "expand", filter);
					addNotNullAttribute(filterElement, "generateNumberParts", filter);
					addNotNullAttribute(filterElement, "generateWordParts", filter);
					addNotNullAttribute(filterElement, "ignoreCase", filter);
					addNotNullAttribute(filterElement, "inject", filter);
					addNotNullAttribute(filterElement, "language", filter);
					addNotNullAttribute(filterElement, "pattern", filter);
					if (filter.getProtectedText() != null) {
						filterElement.addAttribute("protected", filter.getProtectedText());
					}
					addNotNullAttribute(filterElement, "replace", filter);
					addNotNullAttribute(filterElement, "replacement", filter);
					addNotNullAttribute(filterElement, "splitOnCaseChange", filter);
					if (filter.getSynonymsText() != null) {
						filterElement.addAttribute("synonyms", filter.getSynonymsText());
					}
					if (filter.getWordsText() != null) {
						filterElement.addAttribute("words", filter.getWordsText());
					}
				}
			}

			if (queryAnalyzer != null) {
				Element analyzerElement = DocumentHelper.createElement("analyzer");
				fieldTypeElement.add(analyzerElement);
				analyzerElement.addAttribute("type", "query");

				AnalyzerClass analyzerClass = queryAnalyzer.getAnalyzerClass();
				if (analyzerClass != null) {
					analyzerElement.addAttribute("class", analyzerClass.getClassName());
				}

				TokenizerClass tokenizerClass = queryAnalyzer.getTokenizerClass();
				if (tokenizerClass != null) {
					Element tokenizerElement = DocumentHelper.createElement("tokenizer");
					analyzerElement.add(tokenizerElement);
					tokenizerElement.addAttribute("class", tokenizerClass.getClassName());
				}

				Collection<AnalyzerFilter> filters = queryAnalyzer.getFilters();
				for (AnalyzerFilter filter : filters) {
					FilterClass filterClass = filter.getFilterClass();
					Element filterElement = DocumentHelper.createElement("filter");
					analyzerElement.add(filterElement);
					filterElement.addAttribute("class", filterClass.getClassName());
					addNotNullAttribute(filterElement, "catenateAll", filter);
					addNotNullAttribute(filterElement, "catenateWords", filter);
					addNotNullAttribute(filterElement, "delimiter", filter);
					addNotNullAttribute(filterElement, "enablePositionIncrements", filter);
					addNotNullAttribute(filterElement, "encoder", filter);
					addNotNullAttribute(filterElement, "expand", filter);
					addNotNullAttribute(filterElement, "generateNumberParts", filter);
					addNotNullAttribute(filterElement, "generateWordParts", filter);
					addNotNullAttribute(filterElement, "ignoreCase", filter);
					addNotNullAttribute(filterElement, "inject", filter);
					addNotNullAttribute(filterElement, "language", filter);
					addNotNullAttribute(filterElement, "pattern", filter);
					if (filter.getProtectedText() != null) {
						filterElement.addAttribute("protected", filter.getProtectedText());
					}
					addNotNullAttribute(filterElement, "replace", filter);
					addNotNullAttribute(filterElement, "replacement", filter);
					addNotNullAttribute(filterElement, "splitOnCaseChange", filter);
					if (filter.getSynonymsText() != null) {
						filterElement.addAttribute("synonyms", filter.getSynonymsText());
					}
					if (filter.getWordsText() != null) {
						filterElement.addAttribute("words", filter.getWordsText());
					}
				}
			}
		}
		writeSchema(collectionName, schemaDocument);
		if (collection != null) {
			// for _default_constellio, we do not need initialize
			initCore(collection);
		}
	}

	private static void addNotNullAttribute(Element element, String attributeName, ConstellioEntity entity) {
		try {
			Method getter = findGetter(entity.getClass(), attributeName);
			Object propertyValue = getter.invoke(entity, (Object[]) null);
			addNotNullAttribute(element, attributeName, propertyValue);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private static void addNotNullAttribute(Element element, String attributeName, Object attributeValue) {
		if (attributeValue != null) {
			element.addAttribute(attributeName, attributeValue.toString());
		}
	}

	private static Object getPropertyValue(Object value, Object valueIfNull, String property) {
		try {
			Object propertyValue = null;
			if (value != null) {
				Method getter = findGetter(value.getClass(), property);
				propertyValue = getter.invoke(value, (Object[]) null);
			}
			if (valueIfNull != null && propertyValue == null) {
				Method getter = findGetter(valueIfNull.getClass(), property);

				propertyValue = getter.invoke(valueIfNull, (Object[]) null);

			}
			return propertyValue;
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private static Element addNotNullElement(Element element, String tag, Object content) {
		removeAll(element, tag);
		if (content != null) {
			Element innerElement = element.addElement(tag);
			innerElement.setText(content.toString());
			return innerElement;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private static void setProperty(Element element, String attributeName, ConstellioEntity entity) {
		try {
			Method getter = findGetter(entity.getClass(), attributeName);
			Class returnType = getter.getReturnType();
			String setterName = "set" + StringUtils.capitalize(attributeName);
			Method setter = entity.getClass().getMethod(setterName, returnType);

			String attributeValue = element.attributeValue(attributeName);
			Object setterValue;
			if (StringUtils.isEmpty(attributeValue)) {
				if (boolean.class.equals(returnType)) {
					setterValue = false;
				} else if (int.class.equals(returnType)) {
					setterValue = 0;
				} else if (returnType.isPrimitive()) {
					throw new UnsupportedOperationException(setterName + "(" + returnType + ")");
				} else {
					setterValue = null;
				}
			} else if (boolean.class.equals(returnType) || Boolean.class.equals(returnType)) {
				setterValue = Boolean.valueOf(attributeValue);
			} else if (int.class.equals(returnType) || Integer.class.equals(returnType)) {
				setterValue = Integer.valueOf(attributeValue);
			} else if (String.class.equals(returnType)) {
				setterValue = attributeValue;
			} else {
				throw new UnsupportedOperationException(setterName + "(" + returnType + ")");
			}
			setter.invoke(entity, setterValue);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Method findGetter(Class clazz, String propertyName) throws SecurityException, NoSuchMethodException {
		try {
			String getterName = "get" + StringUtils.capitalize(propertyName);
			return clazz.getMethod(getterName, (Class[]) null);
		} catch (NoSuchMethodException e) {
			String getterName = "is" + StringUtils.capitalize(propertyName);
			return clazz.getMethod(getterName, (Class[]) null);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void initConnectorTypeFields() {
		ConnectorTypeServices connectorTypeServices = ConstellioSpringUtils.getConnectorTypeServices();
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
		AnalyzerClassServices analyzerClassServices = ConstellioSpringUtils.getAnalyzerClassServices();

		for (ConnectorType connectorType : connectorTypeServices.list()) {
			Collection<ConnectorTypeMetaMapping> connectorTypeMetaMappings = connectorType.getMetaMappings();
			if (connectorTypeMetaMappings.isEmpty()) {
				File connectorsDir = ConstellioSpringUtils.getGoogleConnectorsDir();
				File connectorTypeDir = new File(connectorsDir, connectorType.getName());
				File schemaFile = new File(connectorTypeDir, "schema.xml");

				// // we must ensure that collection with the name of
				// connectorType
				// // exists in solrcloud
				// Document schemaDocument =
				// readSchema(connectorType.getName());
				if (schemaFile.exists()) {
					Document schemaDocument = readSchema(schemaFile);
					if (schemaDocument != null) {
						String defaultUniqueKeyName = "id";
						String uniqueKeyFieldName = defaultUniqueKeyName;
						Element uniqueKeyElement = schemaDocument.getRootElement().element("uniqueKey");
						if (uniqueKeyElement != null) {
							uniqueKeyFieldName = uniqueKeyElement.getText();
						}

						String defaultSearchFieldName;
						Element defaultSearchFieldElement = schemaDocument.getRootElement().element("defaultSearchField");
						if (defaultSearchFieldElement != null) {
							defaultSearchFieldName = defaultSearchFieldElement.getText();
						} else {
							defaultSearchFieldName = null;
						}

						Element fieldsElement = schemaDocument.getRootElement().element("fields");
						List<Element> fieldElements = fieldsElement.elements("field");
						for (Iterator<Element> it = fieldElements.iterator(); it.hasNext();) {
							Element fieldElement = it.next();

							ConnectorTypeMetaMapping connectorTypeMetaMapping = new ConnectorTypeMetaMapping();
							connectorType.addMetaMapping(connectorTypeMetaMapping);

							String metaName = fieldElement.attributeValue("name");
							connectorTypeMetaMapping.setMetaName(metaName);
							if (metaName.equals(uniqueKeyFieldName)) {
								connectorTypeMetaMapping.setUniqueKey(true);
							}

							if (defaultSearchFieldName != null && metaName.equals(defaultSearchFieldName)) {
								connectorTypeMetaMapping.setDefaultSearchField(true);
							}

							String indexFieldName = IndexField.normalize(metaName);
							connectorTypeMetaMapping.setIndexFieldName(indexFieldName);

							setProperty(fieldElement, "indexed", connectorTypeMetaMapping);
							// Defaults to true
							// setProperty(fieldElement, "stored",
							// connectorTypeMetaMapping);
							setProperty(fieldElement, "multiValued", connectorTypeMetaMapping);

							String analyzerClassName = fieldElement.attributeValue("analyzer");
							if (analyzerClassName != null) {
								AnalyzerClass analyzerClass = analyzerClassServices.get(analyzerClassName);
								if (analyzerClass == null) {
									analyzerClass = new AnalyzerClass();
									analyzerClass.setClassName(analyzerClassName);
									analyzerClassServices.makePersistent(analyzerClass, false);
								}
								Analyzer analyzer = new Analyzer();
								analyzer.setAnalyzerClass(analyzerClass);
								connectorTypeMetaMapping.setAnalyzer(analyzer);
							}

							String typeName = fieldElement.attributeValue("type");
							FieldType fieldType = fieldTypeServices.get(typeName);
							if (fieldType != null) {
								connectorTypeMetaMapping.setFieldType(fieldType);
								if (fieldType.getIndexed() != null) {
									connectorTypeMetaMapping.setIndexed(fieldType.getIndexed());
								}
								if (fieldType.getMultiValued() != null) {
									connectorTypeMetaMapping.setMultiValued(fieldType.getMultiValued());
								}
							}
						}
					}
				}
			}
		}
	}

	// @Override
	// public IndexSchema getSchema(RecordCollection collection) {
	// return
	// SolrCoreContext.getCores().getCore(collection.getName()).getSchema();
	// }

	// @SuppressWarnings("unchecked")
	// @Override
	// public RecordCollection importSchema(File schemaFile) {
	// RecordCollection collection = new RecordCollection();
	//
	// Document schemaDocument = readSchema(schemaFile);
	// createMissingFieldTypes(schemaDocument);
	//
	// String defaultUniqueKeyName = "id";
	// String uniqueKeyFieldName = defaultUniqueKeyName;
	// Element uniqueKeyElement =
	// schemaDocument.getRootElement().element("uniqueKey");
	// if (uniqueKeyElement != null) {
	// uniqueKeyFieldName = uniqueKeyElement.getText();
	// }
	//
	// String defaultSearchFieldName;
	// Element defaultSearchFieldElement =
	// schemaDocument.getRootElement().element("defaultSearchField");
	// if (defaultSearchFieldElement != null) {
	// defaultSearchFieldName = defaultSearchFieldElement.getText();
	// } else {
	// defaultSearchFieldName = null;
	// }
	//
	// Element fieldsElement =
	// schemaDocument.getRootElement().element("fields");
	// if (fieldsElement != null) {
	// List<Element> fieldElements = fieldsElement.elements("field");
	// for (Iterator<Element> it = fieldElements.iterator(); it.hasNext();) {
	// Element fieldElement = it.next();
	// IndexField indexField = importIndexField(fieldElement,
	// uniqueKeyFieldName, defaultUniqueKeyName, defaultSearchFieldName);
	// if (indexField != null) {
	// collection.addIndexField(indexField);
	// }
	// }
	//
	// List<Element> dynamicFieldElements =
	// fieldsElement.elements("dynamicField");
	// for (Iterator<Element> it = dynamicFieldElements.iterator();
	// it.hasNext();) {
	// Element fieldElement = it.next();
	// IndexField indexField = importIndexField(fieldElement,
	// uniqueKeyFieldName, defaultUniqueKeyName, defaultSearchFieldName);
	// if (indexField != null) {
	// collection.addIndexField(indexField);
	// }
	// }
	// }
	//
	// return collection;
	// }
	//
	// private IndexField importIndexField(Element fieldElement, String
	// uniqueKeyFieldName, String defaultUniqueKeyName, String
	// defaultSearchFieldName) {
	// IndexField indexField;
	//
	// String name = fieldElement.attributeValue("name");
	// if (name.equals(uniqueKeyFieldName)) {
	// indexField = null;
	// } else if (defaultSearchFieldName != null &&
	// name.equals(defaultSearchFieldName)) {
	// indexField = null;
	// } else {
	// indexField = new IndexField();
	// indexField.setName(name);
	//
	// indexField.setDynamicField(fieldElement.getName().equals("dynamicField"));
	//
	// FieldTypeServices fieldTypeServices =
	// ConstellioSpringUtils.getFieldTypeServices();
	// AnalyzerClassServices analyzerClassServices =
	// ConstellioSpringUtils.getAnalyzerClassServices();
	//
	// setProperty(fieldElement, "indexed", indexField);
	// setProperty(fieldElement, "stored", indexField);
	// setProperty(fieldElement, "multiValued", indexField);
	//
	// String analyzerClassName = fieldElement.attributeValue("analyzer");
	// if (analyzerClassName != null) {
	// AnalyzerClass analyzerClass =
	// analyzerClassServices.get(analyzerClassName);
	// if (analyzerClass == null) {
	// analyzerClass = new AnalyzerClass();
	// analyzerClass.setClassName(analyzerClassName);
	// analyzerClassServices.makePersistent(analyzerClass, false);
	// }
	// Analyzer analyzer = new Analyzer();
	// analyzer.setAnalyzerClass(analyzerClass);
	// indexField.setAnalyzer(analyzer);
	// }
	//
	// String typeName = fieldElement.attributeValue("type");
	// FieldType fieldType = fieldTypeServices.get(typeName);
	// if (fieldType != null) {
	// indexField.setFieldType(fieldType);
	// if (fieldType.getIndexed() != null) {
	// indexField.setIndexed(fieldType.getIndexed());
	// }
	// if (fieldType.getMultiValued() != null) {
	// indexField.setMultiValued(fieldType.getMultiValued());
	// }
	// }
	// }
	// return indexField;
	// }

	@Override
	@SuppressWarnings("unchecked")
	public Boolean createMissingFieldTypes(Document schemaDocument) {
		Boolean newFielTypes = false;
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
		ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
		FieldTypeClassServices fieldTypeClassServices = ConstellioSpringUtils.getFieldTypeClassServices();
		FilterClassServices filterClassServices = ConstellioSpringUtils.getFilterClassServices();
		TokenizerClassServices tokenizerClassServices = ConstellioSpringUtils.getTokenizerClassServices();
		AnalyzerClassServices analyzerClassServices = ConstellioSpringUtils.getAnalyzerClassServices();

		ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();

		Element typesElement = schemaDocument.getRootElement().element("types");
		if (typesElement != null) {
			List<Element> fieldTypeElements = typesElement.elements("fieldType");
			for (Element fieldTypeElement : fieldTypeElements) {
				String name = fieldTypeElement.attributeValue("name");
				FieldType fieldType = fieldTypeServices.get(name);
				if (fieldType == null) {
					newFielTypes = true;
					fieldType = new FieldType();
					fieldType.setName(name);

					String fieldTypeClassName = fieldTypeElement.attributeValue("class");
					FieldTypeClass fieldTypeClass = fieldTypeClassServices.get(fieldTypeClassName);
					if (fieldTypeClass == null) {
						fieldTypeClass = new FieldTypeClass();
						fieldTypeClass.setClassName(fieldTypeClassName);
						fieldTypeClass.setConnectorManager(connectorManager);
						fieldTypeClassServices.makePersistent(fieldTypeClass);
					}
					fieldType.setFieldTypeClass(fieldTypeClass);
					fieldType.setConnectorManager(connectorManager);

					setProperty(fieldTypeElement, "indexed", fieldType);
					// Always true
					// setProperty(fieldTypeElement, "stored", fieldType);
					setProperty(fieldTypeElement, "multiValued", fieldType);
					setProperty(fieldTypeElement, "omitNorms", fieldType);
					setProperty(fieldTypeElement, "precisionStep", fieldType);
					setProperty(fieldTypeElement, "positionIncrementGap", fieldType);
					setProperty(fieldTypeElement, "sortMissingLast", fieldType);

					List<Element> analyzerElements = fieldTypeElement.elements("analyzer");
					Element analyzerElement = null;
					Element queryAnalyzerElement = null;
					for (Element element : analyzerElements) {
						String type = element.attributeValue("type");
						if (type == null || type.equals("index")) {
							analyzerElement = element;
						} else if (type.equals("query")) {
							queryAnalyzerElement = element;
						}
					}

					if (analyzerElement != null) {
						Analyzer analyzer = new Analyzer();
						fieldType.setAnalyzer(analyzer);

						String analyzerClassName = analyzerElement.attributeValue("class");
						if (analyzerClassName != null) {
							AnalyzerClass analyzerClass = analyzerClassServices.get(analyzerClassName);
							if (analyzerClass == null) {
								analyzerClass = new AnalyzerClass();
								analyzerClass.setClassName(analyzerClassName);
								analyzerClass.setConnectorManager(connectorManager);
								analyzerClassServices.makePersistent(analyzerClass, false);
							}
							analyzer.setAnalyzerClass(analyzerClass);
						}

						Element tokenizerElement = analyzerElement.element("tokenizer");
						if (tokenizerElement != null) {
							String tokenizerClassName = tokenizerElement.attributeValue("class");
							TokenizerClass tokenizerClass = tokenizerClassServices.get(tokenizerClassName);
							if (tokenizerClass == null) {
								tokenizerClass = new TokenizerClass();
								tokenizerClass.setClassName(tokenizerClassName);
								tokenizerClass.setConnectorManager(connectorManager);
								tokenizerClassServices.makePersistent(tokenizerClass, false);
							}
							analyzer.setTokenizerClass(tokenizerClass);
						}

						for (Element filterElement : (List<Element>) analyzerElement.elements("filter")) {
							AnalyzerFilter filter = new AnalyzerFilter();
							filter.setAnalyzer(analyzer);
							analyzer.getFilters().add(filter);

							String filterClassName = filterElement.attributeValue("class");
							FilterClass filterClass = filterClassServices.get(filterClassName);
							if (filterClass == null) {
								filterClass = new FilterClass();
								filterClass.setClassName(filterClassName);
								filterClass.setConnectorManager(connectorManager);
								filterClassServices.makePersistent(filterClass);
							}
							filter.setFilterClass(filterClass);

							setProperty(filterElement, "catenateAll", filter);
							setProperty(filterElement, "catenateWords", filter);
							setProperty(filterElement, "delimiter", filter);
							setProperty(filterElement, "enablePositionIncrements", filter);
							setProperty(filterElement, "encoder", filter);
							setProperty(filterElement, "expand", filter);
							setProperty(filterElement, "generateNumberParts", filter);
							setProperty(filterElement, "generateWordParts", filter);
							setProperty(filterElement, "ignoreCase", filter);
							setProperty(filterElement, "inject", filter);
							setProperty(filterElement, "language", filter);
							setProperty(filterElement, "pattern", filter);
							if (filterElement.attribute("protected") != null) {
								filter.setProtectedText(filterElement.attributeValue("protected"));
							}
							setProperty(filterElement, "replace", filter);
							setProperty(filterElement, "replacement", filter);
							setProperty(filterElement, "splitOnCaseChange", filter);
							if (filterElement.attribute("synonyms") != null) {
								filter.setSynonymsText(filterElement.attributeValue("synonyms"));
							}
							if (filterElement.attribute("words") != null) {
								filter.setWordsText(filterElement.attributeValue("words"));
							}
						}
					}

					if (queryAnalyzerElement != null) {
						Analyzer queryAnalyzer = new Analyzer();
						fieldType.setQueryAnalyzer(queryAnalyzer);

						String analyzerClassName = queryAnalyzerElement.attributeValue("class");
						if (analyzerClassName != null) {
							AnalyzerClass analyzerClass = analyzerClassServices.get(analyzerClassName);
							if (analyzerClass == null) {
								analyzerClass = new AnalyzerClass();
								analyzerClass.setClassName(analyzerClassName);
								analyzerClass.setConnectorManager(connectorManager);
								analyzerClassServices.makePersistent(analyzerClass, false);
							}
							queryAnalyzer.setAnalyzerClass(analyzerClass);
						}

						Element tokenizerElement = queryAnalyzerElement.element("tokenizer");
						if (tokenizerElement != null) {
							String tokenizerClassName = tokenizerElement.attributeValue("class");
							TokenizerClass tokenizerClass = tokenizerClassServices.get(tokenizerClassName);
							if (tokenizerClass == null) {
								tokenizerClass = new TokenizerClass();
								tokenizerClass.setClassName(tokenizerClassName);
								tokenizerClass.setConnectorManager(connectorManager);
								tokenizerClassServices.makePersistent(tokenizerClass, false);
							}
							queryAnalyzer.setTokenizerClass(tokenizerClass);
						}

						for (Element filterElement : (List<Element>) queryAnalyzerElement.elements("filter")) {
							AnalyzerFilter filter = new AnalyzerFilter();
							filter.setAnalyzer(queryAnalyzer);
							queryAnalyzer.getFilters().add(filter);

							String filterClassName = filterElement.attributeValue("class");
							FilterClass filterClass = filterClassServices.get(filterClassName);
							if (filterClass == null) {
								filterClass = new FilterClass();
								filterClass.setClassName(filterClassName);
								filterClass.setConnectorManager(connectorManager);
								filterClassServices.makePersistent(filterClass);
							}
							filter.setFilterClass(filterClass);

							setProperty(filterElement, "catenateAll", filter);
							setProperty(filterElement, "catenateWords", filter);
							setProperty(filterElement, "delimiter", filter);
							setProperty(filterElement, "enablePositionIncrements", filter);
							setProperty(filterElement, "encoder", filter);
							setProperty(filterElement, "expand", filter);
							setProperty(filterElement, "generateNumberParts", filter);
							setProperty(filterElement, "generateWordParts", filter);
							setProperty(filterElement, "ignoreCase", filter);
							setProperty(filterElement, "inject", filter);
							setProperty(filterElement, "language", filter);
							setProperty(filterElement, "pattern", filter);
							if (filterElement.attribute("protected") != null) {
								filter.setProtectedText(filterElement.attributeValue("protected"));
							}
							setProperty(filterElement, "replace", filter);
							setProperty(filterElement, "replacement", filter);
							setProperty(filterElement, "splitOnCaseChange", filter);
							if (filterElement.attribute("synonyms") != null) {
								filter.setSynonymsText(filterElement.attributeValue("synonyms"));
							}
							if (filterElement.attribute("words") != null) {
								filter.setWordsText(filterElement.attributeValue("words"));
							}
						}
					}

					fieldTypeServices.makePersistent(fieldType);
				}
			}
		}
		return newFielTypes;
	}

	@Override
	public SolrDocument get(String docId, RecordCollection collection) {
		SolrDocument doc;
		SolrServer solrServer = getSolrServer(collection);
		SolrQuery query = new SolrQuery();
		String escapedDocId = ClientUtils.escapeQueryChars(docId);
		query.setQuery(IndexField.UNIQUE_KEY_FIELD + ":" + escapedDocId + "");
		try {
			QueryResponse queryResponse = solrServer.query(query);
			SolrDocumentList solrDocumentList = queryResponse.getResults();
			if (!solrDocumentList.isEmpty()) {
				doc = solrDocumentList.get(0);
			} else {
				doc = null;
			}
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
		return doc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void resetDefaultDistance(RecordCollection collection) {
		ensureCore(collection);

		Document solrConfigDocument = readSolrConfig(collection);
		Element root = solrConfigDocument.getRootElement();

		// 1. remove all requestHandler with name DISMAX_ATTRIBUTE_NAME
		for (Iterator<Element> it = root.elementIterator("requestHandler"); it.hasNext();) {
			Element currentRequestHandlerElement = it.next();
			String currentSearchComponentName = currentRequestHandlerElement.attribute("name").getText();
			if (currentSearchComponentName.equals(DISMAX_ATTRIBUTE_NAME)) {
				it.remove();
			}
		}
		// 2. set requestHandler with name DEFAULT_DISTANCE_NAME as the
		// default distance
		for (Iterator<Element> it = root.elementIterator("requestHandler"); it.hasNext();) {
			Element currentRequestHandlerElement = it.next();
			String currentSearchComponentName = currentRequestHandlerElement.attribute("name").getText();
			if (currentSearchComponentName.equals(DEFAULT_DISTANCE_NAME)) {
				Attribute defaultAttribute = currentRequestHandlerElement.attribute("default");
				if (defaultAttribute != null) {
					defaultAttribute.setText("true");
				} else {
					currentRequestHandlerElement.addAttribute("default", "true");
				}
				break;
			}
		}

		writeSolrConfig(collection, solrConfigDocument);
		initCore(collection);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateDismax(RecordCollection collection) {
		ensureCore(collection);

		Element dismaxElement = getDismaxElement(collection);
		Document solrConfigDocument = readSolrConfig(collection);
		Element root = solrConfigDocument.getRootElement();

		boolean defaultSearchFieldFound = false;
		// 1. keep only one requestHandler with name DISMAX_ATTRIBUTE_NAME
		for (Iterator<Element> it = root.elementIterator("requestHandler"); it.hasNext();) {
			Element currentRequestHandlerElement = it.next();
			String currentSearchComponentName = currentRequestHandlerElement.attribute("name").getText();
			if (currentSearchComponentName.equals(DISMAX_ATTRIBUTE_NAME)) {
				// first copy other fields that are not defaults if the
				// query was set as default
				if (!defaultSearchFieldFound) {
					Attribute defaultAttribute = currentRequestHandlerElement.attribute("default");
					if (defaultAttribute != null && defaultAttribute.getText().equals("true")) {
						defaultSearchFieldFound = true;
						defaultAttribute.setText("false");
						List<Element> elements = currentRequestHandlerElement.elements();
						for (Element element : elements) {
							if (element.attribute("name") != null && !element.attribute("name").getValue().equals("defaults")) {
								BaseElement cloneElement = new BaseElement(element.getName());
								cloneElement.appendAttributes(element);
								cloneElement.appendContent(element);
								dismaxElement.add(cloneElement);
							}
						}
					}
				}
				it.remove();
			}
		}
		if (!defaultSearchFieldFound) {
			// 2. add the parameters of the default RequestHandler to dismax
			// requestHandler (escape the parameter with name="defaults")
			for (Iterator<Element> it = root.elementIterator("requestHandler"); it.hasNext();) {
				Element currentRequestHandlerElement = it.next();
				Attribute defaultAttribute = currentRequestHandlerElement.attribute("default");
				if (defaultAttribute != null && defaultAttribute.getText().equals("true")) {
					defaultAttribute.setText("false");
					List<Element> elements = currentRequestHandlerElement.elements();
					for (Element element : elements) {
						if (element.attribute("name") != null && !element.attribute("name").getValue().equals("defaults")) {
							BaseElement cloneElement = new BaseElement(element.getName());
							cloneElement.appendAttributes(element);
							cloneElement.appendContent(element);
							dismaxElement.add(cloneElement);
						}
					}
					break;
				}
			}
		}

		root.add(dismaxElement);

		writeSolrConfig(collection, solrConfigDocument);
		initCore(collection);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean usesDisMax(RecordCollection collection) {
		ensureCore(collection);

		Document solrconfigXmlDocument = readSolrConfig(collection);
		Element root = solrconfigXmlDocument.getRootElement();

		for (Iterator<Element> it = root.elementIterator("requestHandler"); it.hasNext();) {
			Element currentRequestHandlerElement = it.next();
			String currentSearchComponentName = currentRequestHandlerElement.attribute("name").getText();
			if (currentSearchComponentName.equals(DISMAX_ATTRIBUTE_NAME)) {
				return true;
			}
		}
		return false;
	}

	private Element getDismaxElement(RecordCollection collection) {
		BaseElement disMaxElement = new BaseElement("requestHandler");
		disMaxElement.addAttribute("name", DISMAX_ATTRIBUTE_NAME);
		disMaxElement.addAttribute("class", "solr.SearchHandler");
		disMaxElement.addAttribute("default", "true");

		Element lst = disMaxElement.addElement("lst");
		lst.addAttribute("name", "defaults");

		Element defType = lst.addElement("str");
		defType.addAttribute("name", "defType");
		defType.setText("edismax");// au lieu de "dismax" pour utiliser le
									// plugin
		// :com.doculibre.constellio.solr.handler.component.DisMaxQParserPlugin

		Element qf = lst.addElement("str");
		qf.addAttribute("name", "qf");

		StringBuilder boostsDismaxField = new StringBuilder();
		for (IndexField current : collection.getIndexedIndexFields()) {
			if (current.getBoostDismax() != 1 || IndexField.DEFAULT_SEARCH_FIELD.equals(current.getName())) {
				boostsDismaxField.append(" " + current.getName() + "^" + current.getBoostDismax());
			}
		}
		qf.setText(boostsDismaxField.toString());

		Element tie = lst.addElement("float");
		tie.addAttribute("name", "tie");
		tie.setText("0.01");

		// <requestHandler name="dismax" class="solr.SearchHandler"
		// default="true">
		// <lst name="defaults">
		// <str name="defType">dismax</str>
		// <str name="qf">a_name a_alias^0.8 a_member_name^0.4</str>
		// <float name="tie">0.1</float>
		// </lst>
		// </requestHandler>

		return disMaxElement;
	}

	@Override
	public SolrServer getSolrServer(RecordCollection collection) {
		return SolrCoreContext.getSolrServer(collection);
	}

}
