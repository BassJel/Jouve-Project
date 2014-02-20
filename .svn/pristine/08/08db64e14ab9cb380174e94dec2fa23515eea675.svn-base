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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.utils.AnalyzerUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.sun.beans.WeakCache;

public class ElevateServicesImpl implements ElevateServices {

	// Map<collectionName, Map<text, List<elevatedDocId>>>
	private Map<String, Map<String, List<String>>> elevationCache = new HashMap<String, Map<String, List<String>>>();

	// Map<collectionName, Map<text, List<excludedDocId>>>
	private Map<String, Map<String, List<String>>> exclusionCache = new HashMap<String, Map<String, List<String>>>();

	// Map<analyzedTextQuery, displayTextQuery>
	private Map<String, String> displayQueriesCache = new HashMap<String, String>();

	@Override
	public String toElevateQueryId(SimpleSearch simpleSearch) {
//		String queryText = simpleSearch.getQuery();
//		String collectionName = simpleSearch.getCollectionName();
//		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
//		RecordCollection collection = collectionServices.get(collectionName);
//		String queryTextAnalyzed = AnalyzerUtils.analyze(queryText, collection);
//		SimpleSearch clone = simpleSearch.clone();
//		clone.setQuery(queryTextAnalyzed);
//		clearNonElevateQueryIdData(clone);
//		return clone.toSimpleParams().toString();
		return simpleSearch.getQuery();
	}

	private String toDisplayTextQuery(SimpleSearch simpleSearch) {
		SimpleSearch clone = simpleSearch.clone();
		clearNonElevateQueryIdData(clone);
		return clone.toSimpleParams().toString();
	}

	private void clearNonElevateQueryIdData(SimpleSearch simpleSearch) {
		simpleSearch.clearPages();
		simpleSearch.clearFacetFoldingAndSorting();
		simpleSearch.setSearchLogDocId(null);
	}

	@Override
	public SimpleSearch toSimpleSearch(String elevateQueryId) {
		return SimpleSearch.toSimpleSearch(elevateQueryId);
	}

	@Override
	public synchronized void elevate(Record record, SimpleSearch simpleSearch) {
		String collectionName = simpleSearch.getCollectionName();
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
		RecordCollection collection = collectionServices.get(collectionName);
		elevate(record, simpleSearch, collection);
		if (collection.isFederationOwner()) {
			RecordCollection recordCollection = record.getConnectorInstance().getRecordCollection();
			if (!collection.equals(recordCollection)) {
				SimpleSearch clone = simpleSearch.clone();
				clone.setCollectionName(recordCollection.getName());
				elevate(record, clone, recordCollection);
			}
		}
		if (collection.isIncludedInFederation()) {
			List<RecordCollection> ownerCollections = federationServices.listOwnerCollections(collection);
			for (RecordCollection ownerCollection : ownerCollections) {
				SimpleSearch clone = simpleSearch.clone();
				clone.setCollectionName(ownerCollection.getName());
				elevate(record, clone, ownerCollection);
			}
		}
	}

	private synchronized void elevate(Record record, SimpleSearch simpleSearch, RecordCollection collection) {
		String elevateQueryId = toElevateQueryId(simpleSearch);
		String displayTextQuery = toDisplayTextQuery(simpleSearch);
		String collectionName = collection.getName();
		readCache(collectionName);

		Map<String, List<String>> solrCoreElevationCache = elevationCache.get(collectionName);
		List<String> queryElevationCache = solrCoreElevationCache.get(elevateQueryId);
		if (queryElevationCache == null) {
			queryElevationCache = new ArrayList<String>();
			solrCoreElevationCache.put(elevateQueryId, queryElevationCache);
			displayQueriesCache.put(elevateQueryId, displayTextQuery);
		}

		String docId = record.getUrl();
		int indexOfDocId = queryElevationCache.indexOf(docId);
		if (indexOfDocId > 0) {
			queryElevationCache.remove(indexOfDocId);
			queryElevationCache.add(indexOfDocId - 1, docId);
		} else if (indexOfDocId == -1) {
			queryElevationCache.add(docId);
		}
		updateElevationFile(elevateQueryId, displayTextQuery, collectionName);
	}

	@Override
	public synchronized boolean isElevated(Record record, SimpleSearch simpleSearch) {
		String collectionName = simpleSearch.getCollectionName();
		String elevateQueryId = toElevateQueryId(simpleSearch);
		readCache(collectionName);
		Map<String, List<String>> solrCoreElevationCache = elevationCache.get(collectionName);
		List<String> queryElevationCache = solrCoreElevationCache.get(elevateQueryId);
		return queryElevationCache != null && queryElevationCache.contains(record.getUrl());
	}

	@Override
	public synchronized void cancelElevation(Record record, SimpleSearch simpleSearch) {
		String collectionName = simpleSearch.getCollectionName();
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
		RecordCollection collection = collectionServices.get(collectionName);
		cancelElevation(record, simpleSearch, collection);
		if (collection.isFederationOwner()) {
			List<RecordCollection> includedCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection includedCollection : includedCollections) {
				SimpleSearch clone = simpleSearch.clone();
				clone.setCollectionName(includedCollection.getName());
				cancelElevation(record, clone, includedCollection);
			}
		}
		if (collection.isIncludedInFederation()) {
			List<RecordCollection> ownerCollections = federationServices.listOwnerCollections(collection);
			for (RecordCollection ownerCollection : ownerCollections) {
				SimpleSearch clone = simpleSearch.clone();
				clone.setCollectionName(ownerCollection.getName());
				cancelElevation(record, clone, ownerCollection);
			}
		}
	}

	private synchronized void cancelElevation(Record record, SimpleSearch simpleSearch, RecordCollection collection) {
		String elevateQueryId = toElevateQueryId(simpleSearch);
		String displayTextQuery = toDisplayTextQuery(simpleSearch);
		String collectionName = collection.getName();
		readCache(collectionName);

		Map<String, List<String>> solrCoreElevationCache = elevationCache.get(collectionName);
		List<String> queryElevationCache = solrCoreElevationCache.get(elevateQueryId);
		if (queryElevationCache == null) {
			queryElevationCache = new ArrayList<String>();
			solrCoreElevationCache.put(elevateQueryId, queryElevationCache);
		}

		int indexOfDocId = queryElevationCache.indexOf(record.getUrl());
		if (indexOfDocId != -1) {
			queryElevationCache.remove(indexOfDocId);
			updateElevationFile(elevateQueryId, displayTextQuery, collectionName);
		}
	}

	@Override
	public synchronized void exclude(Record record, RecordCollection collection, SimpleSearch simpleSearch) {
		// We should modify both DB and solr...
//		String collectionName = simpleSearch.getCollectionName();
//		String elevateQueryId = toElevateQueryId(simpleSearch);
//		String displayTextQuery = toDisplayTextQuery(simpleSearch);
//		
//		readCache(collectionName);
//
//		Map<String, List<String>> solrCoreExclusionCache = exclusionCache.get(collectionName);
//		List<String> queryExclusionCache = solrCoreExclusionCache.get(elevateQueryId);
//		if (queryExclusionCache == null) {
//			queryExclusionCache = new ArrayList<String>();
//			solrCoreExclusionCache.put(elevateQueryId, queryExclusionCache);
//		}
//
//		if (!queryExclusionCache.contains(record.getUrl())) {
//			queryExclusionCache.add(record.getUrl());
//			updateElevationFile(elevateQueryId, displayTextQuery, collectionName);
//		}
		
		RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
		recordServices.markRecordForExclusion(record);
	}

	private synchronized void updateElevationFile(String queryText, String queryDisplayText, String collectionName) {
		Element newQueryElement = buildQueryElementFromCache(queryText, queryDisplayText, collectionName);
		replaceQueryElement(queryText, newQueryElement, collectionName);
		reloadCore(collectionName);
	}

	private void reloadCore(String collectionName) {
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set(CommonParams.QT, "/admin/collections");
			params.set(CommonParams.ACTION, "RELOAD");
			String realCollectionName;
			if(SolrServicesImpl.isAliasInCloud(collectionName))
			{
				realCollectionName = SolrServicesImpl.getRealCollectionInCloud(collectionName);
			}
			else {
				realCollectionName = collectionName;
			}
			params.set("name", realCollectionName);
			SolrCoreContext.getMainSolrServer().query(params);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		// CoreContainer coreContainer = SolrCoreContext.getCores();
		// SolrCore solrCore = coreContainer.getCore(collectionName);
		// SearchHandler searchHandler = (SearchHandler)
		// solrCore.getRequestHandler("/elevate");
		// List<SearchComponent> searchComponents =
		// searchHandler.getComponents();
		// SolrCoreAware elevationComponent = null;
		// for (SearchComponent searchComponent : searchComponents) {
		// if (searchComponent instanceof ConstellioElevationComponent) {
		// elevationComponent = (ConstellioElevationComponent) searchComponent;
		// break;
		// } else if (searchComponent instanceof QueryElevationComponent) {
		// elevationComponent = (QueryElevationComponent) searchComponent;
		// break;
		// }
		// }
		// elevationComponent.inform(solrCore);
	}

	private Element buildQueryElementFromCache(String queryText, String queryDisplayText, String collectionName) {
		Element queryElement;

		Map<String, List<String>> solrCoreElevationCache = elevationCache.get(collectionName);
		List<String> queryElevationCache = solrCoreElevationCache.get(queryText);

		Map<String, List<String>> solrCoreExclusionCache = exclusionCache.get(collectionName);
		List<String> queryExclusionCache = solrCoreExclusionCache.get(queryText);

		if ((queryElevationCache != null && !queryElevationCache.isEmpty()) || (queryExclusionCache != null && !queryExclusionCache.isEmpty())) {
			queryElement = DocumentHelper.createElement("query");
			queryElement.addAttribute("text", queryText);
			queryElement.addAttribute("displayText", queryDisplayText);
			if (queryElevationCache != null) {
				for (String elevatedDocId : queryElevationCache) {
					Element docElement = DocumentHelper.createElement("doc");
					docElement.addAttribute("id", elevatedDocId);
					queryElement.add(docElement);
				}
			}

			if (queryExclusionCache != null) {
				for (String excludedDocId : queryExclusionCache) {
					Element docElement = DocumentHelper.createElement("doc");
					docElement.addAttribute("id", excludedDocId);
					docElement.addAttribute("exclude", "true");
					queryElement.add(docElement);
				}
			}
		} else {
			queryElement = null;
		}
		return queryElement;
	}

	@SuppressWarnings("unchecked")
	private synchronized void readCache(String collectionName) {
		Map<String, List<String>> solrCoreElevationCache = elevationCache.get(collectionName);
		if (solrCoreElevationCache == null) {
			solrCoreElevationCache = new HashMap<String, List<String>>();
			elevationCache.put(collectionName, solrCoreElevationCache);

			Map<String, List<String>> solrCoreExclusionCache = new HashMap<String, List<String>>();
			exclusionCache.put(collectionName, solrCoreExclusionCache);

			Document document = SolrServicesImpl.readXMLConfigInCloud(collectionName, "elevate.xml");

			Element rootElement = document.getRootElement();
			for (Element queryElement : (List<Element>) rootElement.elements("query")) {
				String queryText = queryElement.attributeValue("text");
				String queryDisplayText = queryElement.attributeValue("displayText");
				if (queryDisplayText != null && displayQueriesCache.get(queryText) == null) {
					displayQueriesCache.put(queryText, queryDisplayText);
				}

				List<String> elevatedDocIds = new ArrayList<String>();
				List<String> excludedDocIds = new ArrayList<String>();

				for (Element docElement : (List<Element>) queryElement.elements("doc")) {
					String docId = docElement.attributeValue("id");
					if ("true".equals(docElement.attributeValue("exclude"))) {
						excludedDocIds.add(docId);
					} else {
						elevatedDocIds.add(docId);
					}
				}
				if (!elevatedDocIds.isEmpty()) {
					solrCoreElevationCache.put(queryText, elevatedDocIds);
				}
				if (!excludedDocIds.isEmpty()) {
					solrCoreExclusionCache.put(queryText, excludedDocIds);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized void replaceQueryElement(String queryText, Element newQueryElement, String collectionName) {
		Document document = SolrServicesImpl.readXMLConfigInCloud(collectionName, "elevate.xml");
		Element rootElement = document.getRootElement();
		int beanNodeIndex = -1;
		int i = 0;
		for (Node rootContent : (List<Node>) rootElement.content()) {
			if (rootContent instanceof Element) {
				Element possibleBeanElement = (Element) rootContent;
				if (queryText.equals(possibleBeanElement.attributeValue("text"))) {
					beanNodeIndex = i;
					break;
				}
			}
			i++;
		}

		if (beanNodeIndex != -1) {
			if (newQueryElement != null) {
				rootElement.content().set(beanNodeIndex, newQueryElement);
			} else {
				rootElement.content().remove(beanNodeIndex);
			}
		} else if (newQueryElement != null) {
			rootElement.add(newQueryElement);
		}

		// lets write to a file
		SolrServicesImpl.writeXMLConfigInCloud(collectionName, "elevate.xml", document);
	}

	@Override
	public void cancelExclusion(Record record, RecordCollection collection) {
		// readCache(collectionName);
		//
		// Map<String, List<String>> solrCoreExclusionCache =
		// exclusionCache.get(collectionName);
		// List<String> queryExclusionCache =
		// solrCoreExclusionCache.get(queryText);
		// if (queryExclusionCache == null) {
		// queryExclusionCache = new ArrayList<String>();
		// solrCoreExclusionCache.put(queryText, queryExclusionCache);
		// }
		//
		// int indexOfDocId = queryExclusionCache.indexOf(docId);
		// if (indexOfDocId != -1) {
		// queryExclusionCache.remove(indexOfDocId);
		// updateElevationFile(queryText, collectionName);
		// }
		RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
		recordServices.cancelExclusion(record);
	}

	@Override
	public List<String> getQueries(String collectionName) {
		List<String> queries = new ArrayList<String>();
		SolrServer solrServer = SolrCoreContext.getSolrServer(collectionName);
		if (solrServer != null) {
			readCache(collectionName);

			Map<String, List<String>> solrCoreElevationCache = elevationCache.get(collectionName);
			Map<String, List<String>> solrCoreExclusionCache = exclusionCache.get(collectionName);

			for (String queryText : solrCoreElevationCache.keySet()) {
				String queryDisplayText = displayQueriesCache.get(queryText);
				List<String> docIds = solrCoreElevationCache.get(queryText);
				if (!docIds.isEmpty() && !queries.contains(queryDisplayText)) {
					queries.add(queryDisplayText);
				}
			}
			for (String queryText : solrCoreExclusionCache.keySet()) {
				String queryDisplayText = displayQueriesCache.get(queryText);
				List<String> docIds = solrCoreExclusionCache.get(queryText);
				if (!docIds.isEmpty() && !queries.contains(queryDisplayText)) {
					queries.add(queryDisplayText);
				}
			}

			Collections.sort(queries);
		}
		return queries;
	}

	@Override
	public List<String> getElevatedQueries(Record record, RecordCollection collection) {
		String collectionName = collection.getName();
		String recordUrl = record.getUrl();
		List<String> queries = new ArrayList<String>();
		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		if (solrServer != null) {
			readCache(collectionName);
			Map<String, List<String>> solrCoreElevationCache = elevationCache.get(collectionName);
			for (String queryText : solrCoreElevationCache.keySet()) {
				String queryDisplayText = displayQueriesCache.get(queryText);
				List<String> docIds = solrCoreElevationCache.get(queryText);
				if (!docIds.isEmpty() && docIds.contains(recordUrl) && !queries.contains(queryDisplayText)) {
					queries.add(queryDisplayText);
				}
			}
			Collections.sort(queries);
		}
		return queries;
	}

	@Override
	public List<String> getElevatedDocIds(SimpleSearch simpleSearch) {
		String collectionName = simpleSearch.getCollectionName();
		String elevateQueryId = toElevateQueryId(simpleSearch);
		readCache(collectionName);

		Map<String, List<String>> solrCoreElevationCache = elevationCache.get(collectionName);
		List<String> queryElevationCache = solrCoreElevationCache.get(elevateQueryId);
		if (queryElevationCache == null) {
			queryElevationCache = new ArrayList<String>();
			solrCoreElevationCache.put(elevateQueryId, queryElevationCache);
		}

		return queryElevationCache;
	}

	@Override
	public List<String> getExcludedDocIds(RecordCollection collection) {
		// readCache(collectionName);
		//
		// Map<String, List<String>> solrCoreExclusionCache =
		// exclusionCache.get(collectionName);
		// List<String> queryExclusionCache =
		// solrCoreExclusionCache.get(queryText);
		// if (queryExclusionCache == null) {
		// queryExclusionCache = new ArrayList<String>();
		// solrCoreExclusionCache.put(queryText, queryExclusionCache);
		// }
		//
		// return queryExclusionCache;
		Set<String> excludedDocIds = new HashSet<String>();
		excludedDocIds.addAll(getExcludedRecordDocIds(collection));

		FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
		if (collection.isFederationOwner()) {
			List<RecordCollection> includedCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection includedCollection : includedCollections) {
				excludedDocIds.addAll(getExcludedRecordDocIds(includedCollection));
			}
		}
		if (collection.isIncludedInFederation()) {
			List<RecordCollection> ownerCollections = federationServices.listOwnerCollections(collection);
			for (RecordCollection ownerCollection : ownerCollections) {
				excludedDocIds.addAll(getExcludedRecordDocIds(ownerCollection));
			}
		}
		return new ArrayList<String>(excludedDocIds);
	}

	private List<String> getExcludedRecordDocIds(RecordCollection collection) {
		List<String> exludedDocIds = new ArrayList<String>();
		RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
		List<Record> excludedRecords = recordServices.listExcluded(collection);
		if(excludedRecords!=null){
			for (Record excludedRecord : excludedRecords) {
				exludedDocIds.add(excludedRecord.getUrl());
			}
		}
		return exludedDocIds;
	}

	@Override
	public void deleteQuery(SimpleSearch simpleSearch) {
		String collectionName = simpleSearch.getCollectionName();
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
		RecordCollection collection = collectionServices.get(collectionName);
		deleteQuery(simpleSearch, collection);
		if (collection.isFederationOwner()) {
			List<RecordCollection> includedCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection includedCollection : includedCollections) {
				SimpleSearch clone = simpleSearch.clone();
				clone.setCollectionName(includedCollection.getName());
				deleteQuery(clone, includedCollection);
			}
		}
		if (collection.isIncludedInFederation()) {
			List<RecordCollection> ownerCollections = federationServices.listOwnerCollections(collection);
			for (RecordCollection ownerCollection : ownerCollections) {
				SimpleSearch clone = simpleSearch.clone();
				clone.setCollectionName(ownerCollection.getName());
				deleteQuery(clone, ownerCollection);
			}
		}
	}

	private void deleteQuery(SimpleSearch simpleSearch, RecordCollection collection) {
		String collectionName = simpleSearch.getCollectionName();
		String elevateQueryId = toElevateQueryId(simpleSearch);
		String displayTextQuery = toDisplayTextQuery(simpleSearch);
		readCache(collectionName);
		Map<String, List<String>> solrCoreElevationCache = elevationCache.get(collectionName);
		solrCoreElevationCache.remove(elevateQueryId);
		Map<String, List<String>> solrCoreExclusionCache = exclusionCache.get(collectionName);
		solrCoreExclusionCache.remove(elevateQueryId);
		updateElevationFile(elevateQueryId, displayTextQuery, collectionName);
	}

}
