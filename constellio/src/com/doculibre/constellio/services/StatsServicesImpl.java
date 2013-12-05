package com.doculibre.constellio.services;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.DateUtil;

import com.doculibre.constellio.entities.CollectionStatsFilter;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.RecordMeta;
import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.solr.context.SolrLogContext;
import com.doculibre.constellio.stats.report.StatsConstants;
import com.doculibre.constellio.utils.AnalyzerUtils;
import com.doculibre.constellio.utils.ConstellioDateUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.SimpleParams;
import com.google.enterprise.connector.spi.SpiConstants;

/**
 * Three indexes:
 * 1) Search log: Index where search queries are written in real time as they occur
 *    - id: random
 *    - collectionName
 *    - simpleSearchId
 *    - simpleSearch: simpleSearch.toSimpleParams().toString()
 *    - simpleSearchQueryAnalyzed: simpleSearch.toSimpleParams(queryAnalyzed).toString()
 *    - numFound
 *    - responseTime
 *    - searchDate
 *    
 * 2) Click log: Index to store all the clicks that occur for a given search query for a given moment (populated in real time)
 *    - id: random
 *    - collectionName
 *    - searchLogDocId
 *    - displayUrl
 *    - clickDate
 *    
 * 3) Search compile log: Performance enhancement index, computes all instances of a search query per day, month and year
 *    - id: random
 *    - collectionName
 *    - simpleSearchId
 *    - simpleSearch: simpleSearch.toSimpleParams().toString()
 *    - simpleSearchQueryAnalyzed: simpleSearch.toSimpleParams(queryAnalyzed).toString()
 *    - searchPeriod: ["day"|"month"|"year"] + [yearMonthDay|yearMonth|year]
 *    - hasResult
 *    - hasClick
 *    - searchCount: The number of times this query has occurred for a given day|month|year
 *    - clickCount: The number of times a click has occurred for this query for a given day|month|year
 *    
 * 4) Click compile log: Performance enhancement index, computes all clicks for a search query per day, month and year
 *    - id: random
 *    - collectionName
 *    - simpleSearchId
 *    - simpleSearch: simpleSearch.toSimpleParams().toString()
 *    - simpleSearchQueryAnalyzed: simpleSearch.toSimpleParams(queryAnalyzed).toString()
 *    - searchPeriod: ["day"|"month"|"year"] + [yearMonthDay|yearMonth|year]
 *    - displayUrl
 *    - clickCount: The number of times a click has occurred for this query for a given day|month|year
 * 
 * @author Vincent Dussault
 */
public class StatsServicesImpl implements StatsServices {
	
	private static final String SEP = "==========";
	
	private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final Logger LOGGER = Logger.getLogger(StatsServicesImpl.class.getName());
    private static final Logger LOGGER_SEARCH = Logger.getLogger(StatsServicesImpl.class.getName() + ".search");
    private static final Logger LOGGER_CLICK = Logger.getLogger(StatsServicesImpl.class.getName() + ".click");
	
	private enum PERIOD_TYPE { DAY, MONTH, YEAR };
	
	private static class Period {
		
		private PERIOD_TYPE periodType;
		private Date dayInPeriod;
		
		public Period(PERIOD_TYPE periodType, Date dayInPeriod) {
			super();
			this.periodType = periodType;
			this.dayInPeriod = dayInPeriod;
		}
		
		private int getFactor() {
			int factor;
			if (periodType.equals(PERIOD_TYPE.DAY)) {
				factor = 1;
			} else if (periodType.equals(PERIOD_TYPE.MONTH)) {
				factor = 31;
			} else {
				factor = 365;
			}
			return factor;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			String periodTypeStr;
			String sdfPattern;
			if (periodType.equals(PERIOD_TYPE.DAY)) {
				periodTypeStr = "day";
				sdfPattern = "yyyyMMdd";
			} else if (periodType.equals(PERIOD_TYPE.MONTH)) {
				periodTypeStr = "month";
				sdfPattern = "yyyyMM";
			} else {
				periodTypeStr = "year";
				sdfPattern = "yyyy";
			}
			sb.append(periodTypeStr);
			sb.append(new SimpleDateFormat(sdfPattern).format(dayInPeriod));
			return sb.toString();
		}
		
	}
	
	private static class PeriodCountMap {
		
		private Map<String, Integer> counts = new HashMap<String, Integer>();
		private SolrDocument solrDocument;
		
		public PeriodCountMap(SolrDocument solrDocument) {
			super();
			this.solrDocument = solrDocument;
		}
		
		private Integer getPeriodCount(String periodStr) {
			return counts.get(periodStr);
		}
		
		private void setPeriodCount(String periodStr, int periodCount) {
			counts.put(periodStr, periodCount);
		}

		public SolrDocument getSolrDocument() {
			return solrDocument;
		}
		
	}

	/**
	 * Initialize Solr cores.
	 * 
	 * @see com.doculibre.constellio.services.StatsServices#init()
	 */
	@Override
	public synchronized void init() {
		SolrLogContext.init();
		// REMOVE!!!!
//		recompile();
	}

	/**
	 * Shutdown Solr cores.
	 * 
	 * @see com.doculibre.constellio.services.StatsServices#shutdown()
	 */
	@Override
	public synchronized void shutdown() {
		SolrLogContext.shutdown();
	}

	/**
	 * Optimize the four Solr cores. Will slow down search and clicks!!!
	 * 
	 * @see com.doculibre.constellio.services.StatsServices#optimize()
	 */
	@Override
	public synchronized void optimize() {
		try {
			SolrLogContext.getSearchLogSolrServer().optimize();
			SolrLogContext.getClickLogSolrServer().optimize();
			SolrLogContext.getSearchCompileLogSolrServer().optimize();
			SolrLogContext.getClickCompileLogSolrServer().optimize();
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void recompile() { 
		rewriteIds();
		
		rebuildCache = new HashMap<SolrServer, Map<Map<String, Object>, SolrDocument>>();
		
		SolrServer searchLogSolrServer = SolrLogContext.getSearchLogSolrServer();
		SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
		SolrServer clickCompileLogSolrServer = SolrLogContext.getClickCompileLogSolrServer();
		
		deleteAll(searchCompileLogSolrServer);
		deleteAll(clickCompileLogSolrServer);
		
		commit(searchCompileLogSolrServer);
		commit(clickCompileLogSolrServer);
		optimize();
		
		Set<String> ignored = new HashSet<String>();
		
		Date now = new Date();
		Date startDate = DateUtils.addYears(now, -100); // Adjust code in 100 years...
		Date endDate = ConstellioDateUtils.getEndOfDay(now);
		
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		for (RecordCollection collection : collectionServices.list()) {
			if (!collection.isOpenSearch() && !ignored.contains(collection.getName())) {
				System.out.println("Retrieving queries for " + collection.getName());
				List<SolrDocument> allQueries = getQueries(collection, startDate, endDate, false, Integer.MAX_VALUE);
				System.out.println("Retrieved " + allQueries.size() + " queries for " + collection.getName());
				int i = 0;
				for (SolrDocument doc : allQueries) {
					if (i++ % 100 == 0) {
						System.out.println(i + " of " + allQueries.size());
					}
					String simpleSearchStr = doc.getFieldValue(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH).toString();
					SimpleSearch simpleSearch = SimpleSearch.toSimpleSearch(simpleSearchStr);
					if (!isIgnored(simpleSearch)) {
						Date searchDate = (Date) doc.getFieldValue(StatsConstants.INDEX_FIELD_SEARCH_DATE);
						Integer numFound = Integer.parseInt(doc.getFieldValue(StatsConstants.INDEX_FIELD_NUM_FOUND).toString());
						
						compileSearch(simpleSearch, numFound, new Period(PERIOD_TYPE.DAY, searchDate));
						compileSearch(simpleSearch, numFound, new Period(PERIOD_TYPE.MONTH, searchDate));
						compileSearch(simpleSearch, numFound, new Period(PERIOD_TYPE.YEAR, searchDate));
						
//						commit(searchCompileLogSolrServer);
					}
				}
				commit(searchCompileLogSolrServer);
				
				List<SolrDocument> allClicks = getClicks(collection, startDate, endDate, false, Integer.MAX_VALUE);
				System.out.println("Retrieved " + allClicks.size() + " clicks for " + collection.getName());
				int j = 0;
				for (SolrDocument doc : allClicks) {
					if (j++ % 100 == 0) {
						System.out.println(j + " of " + allClicks.size());
					}
					Date clickDate = (Date) doc.getFieldValue(StatsConstants.INDEX_FIELD_CLICK_DATE);
					String searchLogDocId = doc.getFieldValue(StatsConstants.INDEX_FIELD_SEARCH_LOG_DOC_ID).toString();

					if (doc.getFieldValue(StatsConstants.INDEX_FIELD_RECORD_URL) != null) {
						String recordUrl = doc.getFieldValue(StatsConstants.INDEX_FIELD_RECORD_URL).toString();
						String displayUrl = doc.getFieldValue(StatsConstants.INDEX_FIELD_DISPLAY_URL).toString();
						Record record = new Record();
						record.setUrl(recordUrl);
						
						ConnectorInstanceMeta displayUrlConnectorInstanceMeta = new ConnectorInstanceMeta();
						displayUrlConnectorInstanceMeta.setName(SpiConstants.PROPNAME_DISPLAYURL);
						RecordMeta displayUrlMeta = new RecordMeta();
						displayUrlMeta.setContent(displayUrl);
						displayUrlMeta.setConnectorInstanceMeta(displayUrlConnectorInstanceMeta);
						record.addExternalMeta(displayUrlMeta);
						
						Map<String, Object> searchLogUniqueKey = new HashMap<String, Object>();
						searchLogUniqueKey.put(StatsConstants.INDEX_FIELD_ID, searchLogDocId);
						
						SolrDocument searchLogDoc = get(searchLogSolrServer, searchLogUniqueKey, true);
						if (searchLogDoc != null) {
							String simpleSearchStr = searchLogDoc.getFieldValue(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH).toString();
							SimpleSearch simpleSearch = SimpleSearch.toSimpleSearch(simpleSearchStr);
							if (!isIgnored(simpleSearch)) {
								compileClick(record, searchLogDoc, new Period(PERIOD_TYPE.DAY, clickDate));
								compileClick(record, searchLogDoc, new Period(PERIOD_TYPE.MONTH, clickDate));
								compileClick(record, searchLogDoc, new Period(PERIOD_TYPE.YEAR, clickDate));
								
//								commit(searchCompileLogSolrServer);
//								commit(clickCompileLogSolrServer);
							}
						} else {
							System.out.println("Search Log Doc not found : " + searchLogDocId);
						}
					} else {
						System.out.println("No record url : " + searchLogDocId);
					}
				}
				commit(searchCompileLogSolrServer);
				commit(clickCompileLogSolrServer);
			}
		}
		optimize();
		rebuildCache = null;
	}
	
	private void rewriteIds() {
		SolrServer searchLogSolrServer = SolrLogContext.getSearchLogSolrServer();
		SolrServer clickLogSolrServer = SolrLogContext.getClickLogSolrServer();
		SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
		SolrServer clickCompileLogSolrServer = SolrLogContext.getClickCompileLogSolrServer();
		
		String[] searchLogEscapeFields = { StatsConstants.INDEX_FIELD_ID, StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID };
		String[] clickLogEscapeFields = { StatsConstants.INDEX_FIELD_ID, StatsConstants.INDEX_FIELD_SEARCH_LOG_DOC_ID };
		String[] searchCompileLogEscapeFields = { StatsConstants.INDEX_FIELD_ID, StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID };
		String[] clickCompileLogEscapeFields = { StatsConstants.INDEX_FIELD_ID, StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID };

		System.out.println("Rewriting ids for search_log");
		rewriteIds(searchLogSolrServer, searchLogEscapeFields);
		System.out.println("Rewriting ids for click_log");
		rewriteIds(clickLogSolrServer, clickLogEscapeFields);
		System.out.println("Rewriting ids for search_compile_log");
		rewriteIds(searchCompileLogSolrServer, searchCompileLogEscapeFields);
		System.out.println("Rewriting ids for click_compile_log");
		rewriteIds(clickCompileLogSolrServer, clickCompileLogEscapeFields);
		System.out.println("Optimizing...");
		optimize();
	}
	
	private void rewriteIds(SolrServer solrServer, String[] escapeFields) {
		List<SolrDocument> allDocs = listAll(solrServer);
		int i = 0;
		for (SolrDocument doc : allDocs) {
			if (i++ % 100 == 0) {
				System.out.println(i + " of " + allDocs.size());
			}
			String id = doc.getFieldValue(StatsConstants.INDEX_FIELD_ID).toString();
			delete(solrServer, id);
			
			SolrInputDocument inputDoc = toInputDocument(doc);
			for (String escapeField : escapeFields) {
				String previousValue = inputDoc.getFieldValue(escapeField).toString();
				String escapedValue = escape(previousValue);
				inputDoc.setField(escapeField, escapedValue);
			}
			add(solrServer, inputDoc);
		}
		commit(solrServer);
	} 
	
	private static SolrInputDocument toInputDocument(SolrDocument doc) {
		SolrInputDocument inputDoc = new SolrInputDocument();
		for (String fieldName : doc.getFieldNames()) {
			Object fieldValue = doc.getFieldValue(fieldName);
			inputDoc.setField(fieldName, fieldValue);
		}
		return inputDoc;
	}
	
	private static SolrDocument toSolrDocument(SolrInputDocument inputDoc) {
		SolrDocument doc = new SolrDocument();
		for (String fieldName : inputDoc.getFieldNames()) {
			Object fieldValue = inputDoc.getFieldValue(fieldName);
			doc.setField(fieldName, fieldValue);
		}
		return doc;
	}
	
	private static Map<SolrServer, Map<Map<String, Object>, SolrDocument>> rebuildCache;
	
	private static SolrDocument getFromRebuildCache(SolrServer solrServer, Map<String, Object> fieldNamesAndValues) {
		SolrDocument result;
		if (rebuildCache != null) {
			Map<Map<String, Object>, SolrDocument> solrServerCache = rebuildCache.get(solrServer);
			if (solrServerCache == null) {
				solrServerCache = new HashMap<Map<String, Object>, SolrDocument>();
				rebuildCache.put(solrServer, solrServerCache);
			}
			result = solrServerCache.get(fieldNamesAndValues);
		} else {
			result = null;
		}
		return result;
	}
	
	private static void saveInRebuildCache(SolrInputDocument inputDoc, SolrServer solrServer, Map<String, Object> fieldNamesAndValues) {
		SolrDocument doc = toSolrDocument(inputDoc);
		saveInRebuildCache(doc, solrServer, fieldNamesAndValues);
	}
	
	private static void saveInRebuildCache(SolrDocument doc, SolrServer solrServer, Map<String, Object> fieldNamesAndValues) {
		if (rebuildCache != null) {
			Map<Map<String, Object>, SolrDocument> solrServerCache = rebuildCache.get(solrServer);
			if (solrServerCache == null) {
				solrServerCache = new HashMap<Map<String, Object>, SolrDocument>();
				rebuildCache.put(solrServer, solrServerCache);
			}
			solrServerCache.put(fieldNamesAndValues, doc);
		}
	}
	
	private static SolrDocument get(SolrServer solrServer, Map<String, Object> fieldNamesAndValues) {
		return get(solrServer, fieldNamesAndValues, false);
	}
	
	private static SolrDocument get(SolrServer solrServer, Map<String, Object> fieldNamesAndValues, boolean loadIfNotCached) {
		SolrDocument result;
		if (rebuildCache != null) {
			result = getFromRebuildCache(solrServer, fieldNamesAndValues);
			if (result == null && loadIfNotCached){
				SolrDocumentList results = list(solrServer, fieldNamesAndValues, 1);
				if (!results.isEmpty()) {
					result = results.get(0);
					saveInRebuildCache(result, solrServer, fieldNamesAndValues);
				} else {
					result = null;
				}
			}
		}  else {
			SolrDocumentList results = list(solrServer, fieldNamesAndValues, 1);
			if (!results.isEmpty()) {
				result = results.get(0);
			} else {
				result = null;
			}
		}
		return result;
	}
	
	private static SolrDocumentList list(SolrServer solrServer, Map<String, Object> criterias, int maxRows) {
		return list(solrServer, criterias, maxRows, null);
	}
	
	private static SolrDocumentList list(SolrServer solrServer, Map<String, Object> criterias, int maxRows, String appendCustomCritera) {
		return list(solrServer, criterias, maxRows, null, appendCustomCritera);
	}
	
	@SuppressWarnings("unchecked")
	private static SolrDocumentList list(SolrServer solrServer, Map<String, Object> criterias, int maxRows, String sortFieldName, String appendCustomCritera) {
		StringBuffer querySB = new StringBuffer();
		for (Iterator<String> it = criterias.keySet().iterator(); it.hasNext();) {
			String fieldName = it.next();
			Object fieldValue = criterias.get(fieldName);
			querySB.append(fieldName);
			querySB.append(":");
			if (fieldValue instanceof List) {
				List<Object> listFieldValue = (List<Object>) fieldValue;
				querySB.append("(");
				for (Iterator<Object> it2 = listFieldValue.iterator(); it2.hasNext();) {
					Object listFieldValueItem = it2.next();
					String listFieldValueItemStr = listFieldValueItem.toString();
					if (!listFieldValueItemStr.endsWith("*:*")) {
						listFieldValueItemStr = StringUtils.replace(listFieldValueItemStr, " ", "+");
						listFieldValueItemStr = QueryParser.escape(listFieldValueItemStr);
						querySB.append(listFieldValueItemStr);
					} else {
						querySB.append(listFieldValueItemStr);
					}
					if (it2.hasNext()) {
						querySB.append(" OR ");
					}
				}
				querySB.append(")");
			} else {
				String fieldValueStr = fieldValue.toString();
				if (!fieldValueStr.endsWith("*:*")) {
					fieldValueStr = StringUtils.replace(fieldValueStr, " ", "+");
					fieldValueStr = QueryParser.escape(fieldValueStr);
					querySB.append("\"");
					querySB.append(fieldValueStr);
					querySB.append("\"");
				} else {
					querySB.append(fieldValueStr);
				}
			}
			if (it.hasNext()) {
				querySB.append(" ");
			}
		}
		
		if (StringUtils.isNotBlank(appendCustomCritera)) {
			if (querySB.length() > 0) {
				querySB.append(" ");
			}
			querySB.append(appendCustomCritera);
		}
		
		SolrQuery solrQuery = new SolrQuery(querySB.toString());
		solrQuery.setRows(maxRows);
		if (sortFieldName != null) {
			solrQuery.setSortField(sortFieldName, ORDER.desc);
		}
		QueryResponse queryResponse;
		try {
			queryResponse = solrServer.query(solrQuery);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
		SolrDocumentList results = queryResponse.getResults();
		return results;
	}
	
	private static SolrDocumentList listAll(SolrServer solrServer) {
		Map<String, Object> criterias = new HashMap<String, Object>();
		return list(solrServer, criterias, Integer.MAX_VALUE, "*:*");
	}
	
	private static void delete(SolrServer solrServer, String id) {
		try {
			solrServer.deleteByQuery(StatsConstants.INDEX_FIELD_ID + ":\"" + id + "\"");
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void deleteAll(SolrServer solrServer) {
		try {
			solrServer.deleteByQuery("*:*");
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void add(SolrServer solrServer, SolrInputDocument...docs) {
        try {
        	for (SolrInputDocument doc : docs) {
                solrServer.add(doc);
			}
        } catch (Exception e) {
            rollback(solrServer, false);
            LOGGER.error(StatsServicesImpl.class.getName() + ".add", e);
            if (e instanceof RuntimeException) {
            	throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }  
	}
	
	private static void update(SolrServer solrServer, SolrDocument...docs) {
        try {
        	for (SolrDocument doc : docs) {
        		String id = (String) doc.getFieldValue(StatsConstants.INDEX_FIELD_ID);
                delete(solrServer, id);
                SolrInputDocument inputDoc = toInputDocument(doc);
                add(solrServer, inputDoc);
			}
        } catch (Exception e) {
            rollback(solrServer, false);
            LOGGER.error(StatsServicesImpl.class.getName() + ".update", e);
            if (e instanceof RuntimeException) {
            	throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }    
	}
	
	private static void commit(SolrServer solrServer) {
        try {
            solrServer.commit();
        } catch (Exception e) {
            rollback(solrServer, false);
            LOGGER.error(StatsServicesImpl.class.getName() + ".commit", e);
            if (e instanceof RuntimeException) {
            	throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }    
	}
	
	private static  void rollback(SolrServer solrServer, boolean throwOnException) {
        try {
            solrServer.rollback();
        } catch (SolrServerException e) {
        	if (throwOnException) {
                LOGGER.error(StatsServicesImpl.class.getName() + ".rollback", e);
                throw new RuntimeException(e);
        	}
        } catch (IOException e) {
        	if (throwOnException) {
                LOGGER.error(StatsServicesImpl.class.getName() + ".rollback", e);
                throw new RuntimeException(e);
        	}
        }
	}
	
	public boolean isIgnored(SimpleSearch simpleSearch) {
		boolean ignored;
		String collectionName = simpleSearch.getCollectionName();
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = collectionServices.get(collectionName);
		CollectionStatsFilter statsFilter = collection.getStatsFilter();
		if (statsFilter != null) {
			String query = simpleSearch.getQuery();
			boolean excluded = false;
			for (String queryExcludeRegexp : statsFilter.getQueryExcludeRegexps()) {
				try {
					Pattern pattern = Pattern.compile(queryExcludeRegexp, Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(query);
					if (matcher.matches()) {
						excluded = true;
						break;
					}
				} catch (Exception e) {
					LOGGER.warn("Invalid query exclude regexp for collection id " + collection.getId() + " : " + queryExcludeRegexp);
				}
			}
			ignored = excluded;
		} else {
			ignored = false;
		}
		
		return ignored;
	}
	
	@Override
	public String logSearch(SimpleSearch simpleSearch, QueryResponse queryResponse, String ipAddress) {
		return logSearch(simpleSearch, queryResponse.getResults().getNumFound(), queryResponse.getQTime(), ipAddress);
	}

	/**
	 * 1) Add entry in search_log
	 *    - id: random
	 *    - collectionName
	 *    - simpleSearchId
	 *    - simpleSearch: simpleSearch.toSimpleParams().toString()
	 *    - simpleSearchQueryAnalyzed: simpleSearch.toSimpleParams(queryAnalyzed).toString()
	 *    - numFound
	 *    - responseTime
	 *    - searchDate
	 *    
	 * 2) Add/update entry in query_compile_log for day, month and year
	 *    
	 * @see com.doculibre.constellio.services.StatsServices#logSearch(com.doculibre.constellio.entities.search.SimpleSearch, long, long, String)
	 */
	@Override
	public String logSearch(SimpleSearch simpleSearch, long numFound, long responseTime, String ipAddress) {
		return logSearch(simpleSearch, numFound, responseTime, new Date(), ipAddress);
	}

	private String logSearch(final SimpleSearch simpleSearch, final long numFound, final long responseTime, final Date searchDate, String ipAddress) {
		String collectionName = simpleSearch.getCollectionName();
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = collectionServices.get(collectionName);
		final String searchLogDocId;
		if (!collection.isOpenSearch()) {
			searchLogDocId = generateSearchLogDocId(simpleSearch);

			String queryText = simpleSearch.getQuery();
			String queryTextAnalyzed = AnalyzerUtils.analyze(queryText, collection);

			int searchPage = simpleSearch.getPage();
			String simpleSearchId = getSimpleSearchId(simpleSearch);
			String simpleSearchStr = getSimpleSearchStr(simpleSearch, false);
			String simpleSearchQueryAnalyzedStr = getSimpleSearchStr(simpleSearch, true);
			
			StringBuffer logSB = new StringBuffer(SEP);
			logSB.append("\n");
			logSB.append("Search query");
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_ID);
			logSB.append(":");
			logSB.append(searchLogDocId);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_COLLECTION_NAME);
			logSB.append(":");
			logSB.append(collectionName);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID);
			logSB.append(":");
			logSB.append(simpleSearchId);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH);
			logSB.append(":");
			logSB.append(simpleSearchStr);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_QUERY_ANALYZED);
			logSB.append(":");
			logSB.append(simpleSearchQueryAnalyzedStr);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_QUERY_TEXT);
			logSB.append(":");
			logSB.append(queryText);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_QUERY_TEXT_ANALYZED);
			logSB.append(":");
			logSB.append(queryTextAnalyzed);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_NUM_FOUND);
			logSB.append(":");
			logSB.append(numFound);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_RESPONSE_TIME);
			logSB.append(":");
			logSB.append(responseTime);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_SEARCH_DATE);
			logSB.append(":");
			logSB.append(formatDate(searchDate));
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_SEARCH_PAGE);
			logSB.append(":");
			logSB.append(searchPage);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_IP_ADDRESS);
			logSB.append(":");
			logSB.append(ipAddress);
			
			LOGGER_SEARCH.info(logSB.toString());
		} else {
			searchLogDocId = "";
		}
		return searchLogDocId;
	}

//	public synchronized String logSearch(final SimpleSearch simpleSearch, final long numFound, final long responseTime, final Date searchDate) {
//		String collectionName = simpleSearch.getCollectionName();
//		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
//		RecordCollection collection = collectionServices.get(collectionName);
//		final String searchLogDocId;
//		if (!collection.isOpenSearch()) {
//			searchLogDocId = generateSearchLogDocId(simpleSearch);
//
//			String queryText = simpleSearch.getQuery();
//			String queryTextAnalyzed = AnalyzerUtils.analyze(queryText, collection);
//
//			int searchPage = simpleSearch.getPage();
//			String simpleSearchId = getSimpleSearchId(simpleSearch);
//			String simpleSearchStr = getSimpleSearchStr(simpleSearch, false);
//			String simpleSearchQueryAnalyzedStr = getSimpleSearchStr(simpleSearch, true);
//			
//			// Log
//			SolrInputDocument doc = new SolrInputDocument();
//			doc.setField(StatsConstants.INDEX_FIELD_ID, searchLogDocId);
//			doc.setField(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionName);
//			doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID, simpleSearchId);
//			doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH, simpleSearchStr);
//			doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_QUERY_ANALYZED, simpleSearchQueryAnalyzedStr);
//			doc.setField(StatsConstants.INDEX_FIELD_QUERY_TEXT, queryText);
//			doc.setField(StatsConstants.INDEX_FIELD_QUERY_TEXT_ANALYZED, queryTextAnalyzed);
//			doc.setField(StatsConstants.INDEX_FIELD_NUM_FOUND, numFound);
//			doc.setField(StatsConstants.INDEX_FIELD_RESPONSE_TIME, responseTime);
//			doc.setField(StatsConstants.INDEX_FIELD_SEARCH_DATE, searchDate);
//			doc.setField(StatsConstants.INDEX_FIELD_SEARCH_PAGE, searchPage);
//			
//			SolrServer searchLogSolrServer = SolrLogContext.getSearchLogSolrServer();
//			add(searchLogSolrServer, doc);
//			commit(searchLogSolrServer);
//			
//			// Compile		
//			compileSearch(simpleSearch, numFound, new Period(PERIOD_TYPE.DAY, searchDate));
//			compileSearch(simpleSearch, numFound, new Period(PERIOD_TYPE.MONTH, searchDate));
//			compileSearch(simpleSearch, numFound, new Period(PERIOD_TYPE.YEAR, searchDate));
//			
//			SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
//			commit(searchCompileLogSolrServer);
//		} else {
//			searchLogDocId = "";
//		}
//		return searchLogDocId;
//	}
	
	/*
	 *    - id: random
	 *    - collectionName
	 *    - simpleSearchId
	 *    - simpleSearch: simpleSearch.toSimpleParams().toString()
	 *    - simpleSearchQueryAnalyzed: simpleSearch.toSimpleParams(queryAnalyzed).toString()
	 *    - searchPeriod: ["day"|"month"|"year"] + [yearMonthDay|yearMonth|year]
	 *    - hasResult
	 *    - hasClick
	 *    - searchCount: The number of times this query has occurred for a given day|month|year
	 *    - clickCount: The number of times a click has occurred for this query for a given day|month|year
	 */
	private static synchronized void compileSearch(SimpleSearch simpleSearch, long numFound, Period period) {
		String simpleSearchId = getSimpleSearchId(simpleSearch);
		if (simpleSearchId != null) {
			String periodStr = period.toString();
			boolean hasResult = numFound > 0;
			
			Map<String, Object> uniqueKey = new HashMap<String, Object>();
			uniqueKey.put(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID, simpleSearchId);
			uniqueKey.put(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, periodStr);
			uniqueKey.put(StatsConstants.INDEX_FIELD_HAS_RESULT, hasResult);

			SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
			SolrDocument existingDoc = get(searchCompileLogSolrServer, uniqueKey);
			if (existingDoc != null) {
				long existingSearchCount = Long.parseLong(existingDoc.getFieldValue(StatsConstants.INDEX_FIELD_SEARCH_COUNT).toString());
				long newSearchCount = existingSearchCount + 1;
				existingDoc.setField(StatsConstants.INDEX_FIELD_SEARCH_COUNT, newSearchCount);
				update(searchCompileLogSolrServer, existingDoc);
			} else {
				String searchCompileLogDocId = generateSearchCompileLogDocId(simpleSearch);
				String collectionName = simpleSearch.getCollectionName();
				String simpleSearchStr = getSimpleSearchStr(simpleSearch, false);
				String simpleSearchQueryAnalyzedStr = getSimpleSearchStr(simpleSearch, true);
				
				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				RecordCollection collection = collectionServices.get(collectionName);
				String queryText = simpleSearch.getQuery();
				String queryTextAnalyzed = AnalyzerUtils.analyze(queryText, collection);

				SolrInputDocument doc = new SolrInputDocument();
				doc.setField(StatsConstants.INDEX_FIELD_ID, searchCompileLogDocId);
				doc.setField(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionName);
				doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID, simpleSearchId);
				doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH, simpleSearchStr);
				doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_QUERY_ANALYZED, simpleSearchQueryAnalyzedStr);
				doc.setField(StatsConstants.INDEX_FIELD_QUERY_TEXT, queryText);
				doc.setField(StatsConstants.INDEX_FIELD_QUERY_TEXT_ANALYZED, queryTextAnalyzed);
				doc.setField(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, periodStr);
				doc.setField(StatsConstants.INDEX_FIELD_HAS_RESULT, hasResult);
				doc.setField(StatsConstants.INDEX_FIELD_HAS_CLICK, false);
				doc.setField(StatsConstants.INDEX_FIELD_SEARCH_COUNT, 1);
				doc.setField(StatsConstants.INDEX_FIELD_CLICK_COUNT, 0);
				add(searchCompileLogSolrServer, doc);
				
				saveInRebuildCache(doc, searchCompileLogSolrServer, uniqueKey);
			}
		}
	}

	/**
	 * 1) Add entry in click_log
	 *    - id: random
	 *    - collectionName
	 *    - searchLogDocId
	 *    - displayUrl
	 *    - clickDate
	 * 2) Add/update entry in click_compile_log for day, month and year
	 * 
	 * @see com.doculibre.constellio.services.StatsServices#logClick(com.doculibre.constellio.entities.RecordCollection, com.doculibre.constellio.entities.Record, java.lang.String, String)
	 */
	@Override
	public String logClick(RecordCollection collection, Record record, String searchLogDocId, String ipAddress) {
		return logClick(collection, record, searchLogDocId, new Date(), ipAddress);
	}
	
	private String logClick(RecordCollection collection, Record record, String searchLogDocId, Date clickDate, String ipAddress) {
		String clickLogDocId;
		if (!collection.isOpenSearch()) {
			clickLogDocId = generateClickLogDocId(record, searchLogDocId);
					
			String collectionName = collection.getName();
			String recordUrl = record.getUrl();
			String displayUrl = record.getDisplayUrl();
					
			StringBuffer logSB = new StringBuffer(SEP);
			logSB.append("\n");
			logSB.append("Search result click");
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_ID);
			logSB.append(":");
			logSB.append(clickLogDocId);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_COLLECTION_NAME);
			logSB.append(":");
			logSB.append(collectionName);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_SEARCH_LOG_DOC_ID);
			logSB.append(":");
			logSB.append(searchLogDocId);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_RECORD_URL);
			logSB.append(":");
			logSB.append(recordUrl);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_DISPLAY_URL);
			logSB.append(":");
			logSB.append(displayUrl);
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_CLICK_DATE);
			logSB.append(":");
			logSB.append(formatDate(clickDate));
			logSB.append("\n");
			logSB.append(StatsConstants.INDEX_FIELD_IP_ADDRESS);
			logSB.append(":");
			logSB.append(ipAddress);
			
			LOGGER_CLICK.info(logSB.toString());
		} else {
			clickLogDocId = "";
		}
		return clickLogDocId;
	}
	
//	public synchronized String logClick(RecordCollection collection, Record record, String searchLogDocId, Date clickDate) {
//		String clickLogDocId;
//		if (!collection.isOpenSearch()) {
//			
//			Map<String, Object> searchLogUniqueKey = new HashMap<String, Object>();
//			searchLogUniqueKey.put(StatsConstants.INDEX_FIELD_ID, searchLogDocId);
//			
//			SolrServer searchLogSolrServer = SolrLogContext.getSearchLogSolrServer();
//			SolrDocument searchLogDoc = get(searchLogSolrServer, searchLogUniqueKey);
//			if (searchLogDoc != null) {
//				String simpleSearchStr = searchLogDoc.getFieldValue(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH).toString();
//				SimpleSearch simpleSearch = SimpleSearch.toSimpleSearch(simpleSearchStr);
//				if (!isIgnored(simpleSearch)) {
//					clickLogDocId = generateClickLogDocId(record, searchLogDocId);
//					
//					String collectionName = collection.getName();
//					String recordUrl = record.getUrl();
//					String displayUrl = record.getDisplayUrl();
//					
//					// Log
//					SolrInputDocument doc = new SolrInputDocument();
//					doc.setField(StatsConstants.INDEX_FIELD_ID, clickLogDocId);
//					doc.setField(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionName);
//					doc.setField(StatsConstants.INDEX_FIELD_SEARCH_LOG_DOC_ID, searchLogDocId);
//					doc.setField(StatsConstants.INDEX_FIELD_RECORD_URL, recordUrl);
//					doc.setField(StatsConstants.INDEX_FIELD_DISPLAY_URL, displayUrl);
//					doc.setField(StatsConstants.INDEX_FIELD_CLICK_DATE, clickDate);
//					
//					SolrServer clickLogSolrServer = SolrLogContext.getClickLogSolrServer();
//					add(clickLogSolrServer, doc);
//					commit(clickLogSolrServer);
//					
//					// Compile		
//					compileClick(record, searchLogDoc, new Period(PERIOD_TYPE.DAY, clickDate));
//					compileClick(record, searchLogDoc, new Period(PERIOD_TYPE.MONTH, clickDate));
//					compileClick(record, searchLogDoc, new Period(PERIOD_TYPE.YEAR, clickDate));
//
//					SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
//					SolrServer clickCompileLogSolrServer = SolrLogContext.getClickCompileLogSolrServer();
//					commit(searchCompileLogSolrServer);
//					commit(clickCompileLogSolrServer);
//				} else {
//					clickLogDocId = "";
//				}
//			} else {
//				clickLogDocId = "";
//			}	
//		} else {
//			clickLogDocId = "";
//		}
//		return clickLogDocId;
//	}
	
	/*
	 * 3) Search compile log:
	 *    - id: random
	 *    - collectionName
	 *    - simpleSearchId
	 *    - simpleSearch: simpleSearch.toSimpleParams().toString()
	 *    - simpleSearchQueryAnalyzed: simpleSearch.toSimpleParams(queryAnalyzed).toString()
	 *    - searchPeriod: ["day"|"month"|"year"] + [yearMonthDay|yearMonth|year]
	 *    - hasResult
	 *    - hasClick
	 *    - searchCount: The number of times this query has occurred for a given day|month|year
	 *    - clickCount: The number of times a click has occurred for this query for a given day|month|year
	 *    
	 * 4) Click compile log: 
	 *    - id: random
	 *    - collectionName
	 *    - simpleSearchId
	 *    - simpleSearch: simpleSearch.toSimpleParams().toString()
	 *    - simpleSearchQueryAnalyzed: simpleSearch.toSimpleParams(queryAnalyzed).toString()
	 *    - searchPeriod: ["day"|"month"|"year"] + [yearMonthDay|yearMonth|year]
	 *    - displayUrl
	 *    - clickCount: The number of times a click has occurred for this query for a given day|month|year
	 */
	private static synchronized void compileClick(Record record, SolrDocument searchLogDoc, Period period) {
		String simpleSearchId = (String) searchLogDoc.getFieldValue(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID);
		String periodStr = period.toString();
		String recordUrl = record.getUrl();
		String displayUrl = record.getDisplayUrl();
		
		SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
		SolrServer clickCompileLogSolrServer = SolrLogContext.getClickCompileLogSolrServer();
		
		// Update click information in search compile log
		Map<String, Object> searchCompileLogUniqueKey = new HashMap<String, Object>();
		searchCompileLogUniqueKey.put(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID, simpleSearchId);
		searchCompileLogUniqueKey.put(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, periodStr);
		
		SolrDocument searchCompileLogDoc = get(searchCompileLogSolrServer, searchCompileLogUniqueKey, true);
		if (searchCompileLogDoc != null) {
			long existingClickCount = Long.parseLong(searchCompileLogDoc.getFieldValue(StatsConstants.INDEX_FIELD_CLICK_COUNT).toString());
			long newClickCount = existingClickCount + 1;
			searchCompileLogDoc.setField(StatsConstants.INDEX_FIELD_HAS_CLICK, true);
			searchCompileLogDoc.setField(StatsConstants.INDEX_FIELD_CLICK_COUNT, newClickCount);
			update(searchCompileLogSolrServer, searchCompileLogDoc);
			
			// Update click information in click compile log
			Map<String, Object> clickCompileLogUniqueKey = new HashMap<String, Object>();
			clickCompileLogUniqueKey.put(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID, simpleSearchId);
			clickCompileLogUniqueKey.put(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, periodStr);
			clickCompileLogUniqueKey.put(StatsConstants.INDEX_FIELD_RECORD_URL, recordUrl);

			SolrDocument existingDoc = get(clickCompileLogSolrServer, clickCompileLogUniqueKey);
			if (existingDoc != null) {
				existingDoc.setField(StatsConstants.INDEX_FIELD_CLICK_COUNT, newClickCount);
				update(clickCompileLogSolrServer, existingDoc);
			} else {
				String clickCompileLogDocId = generateClickCompileLogDocId(record, simpleSearchId);
				String collectionName = (String) searchLogDoc.getFieldValue(StatsConstants.INDEX_FIELD_COLLECTION_NAME);
				String simpleSearchStr = (String) searchLogDoc.getFieldValue(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH);
				String simpleSearchQueryAnalyzedStr = (String) searchLogDoc.getFieldValue(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_QUERY_ANALYZED);
				String queryText = (String) searchLogDoc.getFieldValue(StatsConstants.INDEX_FIELD_QUERY_TEXT);
				String queryTextAnalyzed = (String) searchLogDoc.getFieldValue(StatsConstants.INDEX_FIELD_QUERY_TEXT_ANALYZED);
				
				SolrInputDocument doc = new SolrInputDocument();
				doc.setField(StatsConstants.INDEX_FIELD_ID, clickCompileLogDocId);
				doc.setField(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionName);
				doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID, simpleSearchId);
				doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH, simpleSearchStr);
				doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_QUERY_ANALYZED, simpleSearchQueryAnalyzedStr);
				doc.setField(StatsConstants.INDEX_FIELD_QUERY_TEXT, queryText);
				doc.setField(StatsConstants.INDEX_FIELD_QUERY_TEXT_ANALYZED, queryTextAnalyzed);
				doc.setField(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, periodStr);
				doc.setField(StatsConstants.INDEX_FIELD_RECORD_URL, recordUrl);
				doc.setField(StatsConstants.INDEX_FIELD_DISPLAY_URL, displayUrl);
				doc.setField(StatsConstants.INDEX_FIELD_CLICK_COUNT, 1);
				add(clickCompileLogSolrServer, doc);
				
				saveInRebuildCache(doc, clickCompileLogSolrServer, clickCompileLogUniqueKey);
			}
		} else {
			System.out.println("Search Compile Log Doc not found : " + searchCompileLogUniqueKey);
		}
	}

	private static List<Period> listPeriods(Date startDate, Date endDate) {
		List<Period> periods = new ArrayList<Period>();
		Date now = new Date();
		Date todayBeginning = ConstellioDateUtils.getBeginningOfDay(now);
		Date todayEnd = ConstellioDateUtils.getEndOfDay(now);
		if (endDate.equals(todayBeginning) || endDate.after(todayBeginning)) {
			endDate = ConstellioDateUtils.getLastDayOfYear(todayEnd);
		}
		
		if (startDate != null && endDate != null && !startDate.after(endDate) && !startDate.after(todayBeginning)) {
			if (ConstellioDateUtils.isSameYear(startDate, endDate)) {
				if (ConstellioDateUtils.isSameDay(startDate, endDate)) {
					// 1 period
					periods.add(new Period(PERIOD_TYPE.DAY, startDate));
				} else if (ConstellioDateUtils.isSameMonth(startDate, endDate)) {
					if (ConstellioDateUtils.isFirstDayOfMonth(startDate) && ConstellioDateUtils.isLastDayOfMonth(endDate)) {
						// Full month
						periods.add(new Period(PERIOD_TYPE.MONTH, startDate));
					} else if (ConstellioDateUtils.isFirstDayOfMonth(startDate) && 
							(ConstellioDateUtils.isSameDay(now, endDate) || endDate.after(todayEnd))) {
						// Month so far
						periods.add(new Period(PERIOD_TYPE.MONTH, startDate));
					} else {
						// Days between start date and end date as long as it's not after today
						Date dayInMonth = startDate;
						while (!ConstellioDateUtils.isSameDay(dayInMonth, endDate)) {
							if (dayInMonth.after(todayEnd)) {
								break;
							} else {
								periods.add(new Period(PERIOD_TYPE.DAY, dayInMonth));
								dayInMonth = DateUtils.addDays(dayInMonth, 1);
							}
						}
						if (!endDate.after(todayEnd)) {
							periods.add(new Period(PERIOD_TYPE.DAY, endDate));
						}	
					}
				} else {
					// Same year, different months
					Date dayInYear = startDate;
					
					// Deal with first month
					if (ConstellioDateUtils.isFirstDayOfMonth(startDate)) {
						// Complete month
						periods.add(new Period(PERIOD_TYPE.MONTH, dayInYear));
						dayInYear = DateUtils.addMonths(dayInYear, 1);
					} else {
						// Remaining days in first month
						while (!ConstellioDateUtils.isLastDayOfMonth(dayInYear)) {
							if (dayInYear.after(todayEnd)) {
								break;
							} else {
								periods.add(new Period(PERIOD_TYPE.DAY, dayInYear));
								dayInYear = DateUtils.addDays(dayInYear, 1);
							}
						}
						// Last day of month
						if (!dayInYear.after(todayEnd)) {
							periods.add(new Period(PERIOD_TYPE.DAY, dayInYear));
						}
					}

					// No need to add extra periods if we've already reached the end of our effective period
					if (!dayInYear.after(todayEnd)) {
						// First day of second month
						dayInYear = DateUtils.addDays(dayInYear, 1);
						// Months between second month and month of end date
						while (!ConstellioDateUtils.isSameMonth(dayInYear, endDate)) {
							if (dayInYear.after(todayEnd)) {
								break;
							} else {
								periods.add(new Period(PERIOD_TYPE.MONTH, dayInYear));
								dayInYear = DateUtils.addMonths(dayInYear, 1);
							}
						}

						if (!dayInYear.after(todayEnd)) {
							// Deal with last month
							if (ConstellioDateUtils.isLastDayOfMonth(endDate)) {
								// Complete month
								periods.add(new Period(PERIOD_TYPE.MONTH, dayInYear));
							} else {
								Date dayInMonth = ConstellioDateUtils.getFirstDayOfMonth(dayInYear);
								// Remaining days in last month
								while (!ConstellioDateUtils.isSameDay(dayInMonth, endDate)) {
									if (dayInMonth.after(todayEnd)) {
										break;
									} else {
										periods.add(new Period(PERIOD_TYPE.DAY, dayInMonth));
										dayInMonth = DateUtils.addDays(dayInMonth, 1);
									}
								}
	
								if (!endDate.after(todayEnd)) {
									periods.add(new Period(PERIOD_TYPE.DAY, endDate));
								}	
							}
						}	
					}
				}
			// Different years	
			} else {
				// Deal with first year
				if (ConstellioDateUtils.isFirstDayOfYear(startDate)) {
					// Whole year
					periods.add(new Period(PERIOD_TYPE.YEAR, startDate));
				} else {
					Date dayInYear = startDate;
					// Deal with first month
					if (ConstellioDateUtils.isFirstDayOfMonth(startDate)) {
						// Whole month
						periods.add(new Period(PERIOD_TYPE.MONTH, dayInYear));
						dayInYear = DateUtils.addMonths(dayInYear, 1);
					} else {
						// Remaining days in first month
						while (!ConstellioDateUtils.isLastDayOfMonth(dayInYear)) {
							periods.add(new Period(PERIOD_TYPE.DAY, dayInYear));
							dayInYear = DateUtils.addDays(dayInYear, 1);
						}
						// Last day of month
						periods.add(new Period(PERIOD_TYPE.DAY, dayInYear));
					}

					// Months between second month and month of end date
					dayInYear = DateUtils.addDays(dayInYear, 1);
					while (ConstellioDateUtils.isSameYear(dayInYear, startDate) && 
							!ConstellioDateUtils.isLastMonthOfYear(dayInYear)) {
						periods.add(new Period(PERIOD_TYPE.MONTH, dayInYear));
						dayInYear = DateUtils.addMonths(dayInYear, 1);
					}
					if (ConstellioDateUtils.isSameYear(dayInYear, startDate)) {
						// Last month of first year
						periods.add(new Period(PERIOD_TYPE.MONTH, dayInYear));
					}
				}

				// Deal with years between second year and year of end date
				Date dayInYear = DateUtils.addYears(startDate, 1);
				while (!ConstellioDateUtils.isSameYear(dayInYear, endDate)) {
					if (dayInYear.after(todayEnd)) {
						break;
					} else {
						periods.add(new Period(PERIOD_TYPE.YEAR, dayInYear));
						dayInYear = DateUtils.addYears(dayInYear, 1);
					}
				}
				
				// Deal with last year
				if (ConstellioDateUtils.isLastDayOfYear(endDate)) {
					// Whole year
					periods.add(new Period(PERIOD_TYPE.YEAR, endDate));
				} else {
					dayInYear = ConstellioDateUtils.getFirstDayOfYear(dayInYear);
					
					// Deal with months before month of end date
					while (!ConstellioDateUtils.isSameMonth(dayInYear, endDate)) {
						periods.add(new Period(PERIOD_TYPE.MONTH, dayInYear));
						dayInYear = DateUtils.addMonths(dayInYear, 1);
					}
					
					// Deal with last month
					if (ConstellioDateUtils.isLastDayOfMonth(endDate)) {
						// Complete month
						periods.add(new Period(PERIOD_TYPE.MONTH, dayInYear));
					} else {
						Date dayInMonth = ConstellioDateUtils.getFirstDayOfMonth(dayInYear);
						// Remaining days in last month
						while (!ConstellioDateUtils.isSameDay(dayInMonth, endDate)) {
							if (dayInMonth.after(todayEnd)) {
								break;
							} else {
								periods.add(new Period(PERIOD_TYPE.DAY, dayInMonth));
								dayInMonth = DateUtils.addDays(dayInMonth, 1);
							}
						}

						if (!endDate.after(todayEnd)) {
							periods.add(new Period(PERIOD_TYPE.DAY, endDate));
						}	
					}
				}
			}	
		}
		return periods;
	}
	
	private static void merge(Map<String, PeriodCountMap> compileMap, SolrDocumentList addedList, String countField) {
		for (SolrDocument addedDoc : addedList) {
			String simpleSearchQueryAnalyzedStr = addedDoc.getFieldValue(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_QUERY_ANALYZED).toString();
			String periodStr = addedDoc.getFieldValue(StatsConstants.INDEX_FIELD_SEARCH_PERIOD).toString();
			int periodCount = Integer.parseInt(addedDoc.getFieldValue(countField).toString());
			PeriodCountMap periodCountMap = compileMap.get(simpleSearchQueryAnalyzedStr);
			if (periodCountMap == null) {
				periodCountMap = new PeriodCountMap(addedDoc);
				compileMap.put(simpleSearchQueryAnalyzedStr, periodCountMap);
			}
			periodCountMap.setPeriodCount(periodStr, periodCount);
		}
	}

	private static int getPeriodCountFieldValue(
			SimpleSearch simpleSearch, 
			List<Period> periods, 
			boolean includeFederatedCollections, 
			String countFieldName, 
			SolrServer compileLogSolrServer) {
		String collectionName = simpleSearch.getCollectionName();
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = collectionServices.get(collectionName);
		
		List<String> collectionNames = new ArrayList<String>();
		collectionNames.add(collection.getName());
		if (includeFederatedCollections && collection.isFederationOwner()) {
			FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
			List<RecordCollection> federationCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection federationCollection : federationCollections) {
				collectionNames.add(federationCollection.getName());
			}
		}

		String simpleSearchStrAnalyzed = getSimpleSearchStr(simpleSearch, true);
		StringBuffer customCriteria = new StringBuffer();
		customCriteria.append(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_QUERY_ANALYZED + ":\"" + QueryParser.escape(simpleSearchStrAnalyzed) + "\"");
		customCriteria.append(" ");
		customCriteria.append(StatsConstants.INDEX_FIELD_SEARCH_PERIOD + ":(");
		for (Iterator<Period> it = periods.iterator(); it.hasNext();) {
			Period period = it.next();
			customCriteria.append(period.toString());
			if (it.hasNext()) {
				customCriteria.append(" OR ");
			}
		}
		customCriteria.append(")");
		
		Map<String, Object> criterias = new HashMap<String, Object>();
		criterias.put(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionNames);
		
		SolrDocumentList periodResults = list(compileLogSolrServer, criterias, periods.size(), countFieldName, customCriteria.toString());
		int count = 0;
		for (SolrDocument result : periodResults) {
			count += Integer.parseInt(result.getFieldValue(countFieldName).toString());
		}
		return count;
	}
	
	private static SolrDocumentList sortAndReduce(
			Date startDate, 
			Date endDate, 
			boolean includeFederatedCollections, 
			SolrServer compileLogSolrServer, 
			Map<String, PeriodCountMap> compileMap, 
			int maxSize, 
			final String countFieldName) {
		SolrDocumentList solrDocumentList = new SolrDocumentList();

		List<Period> periods = listPeriods(startDate, endDate);
		for (String simpleSearchQueryAnalyzedStr : compileMap.keySet()) {
			PeriodCountMap periodCountMap = compileMap.get(simpleSearchQueryAnalyzedStr);
			int compileCount = 0;
			List<Period> missingPeriodsForCount = new ArrayList<Period>();
			for (Period period : periods) {
				Integer periodCount = periodCountMap.getPeriodCount(period.toString());
				if (periodCount == null) {
					missingPeriodsForCount.add(period);
				} else {
					compileCount += periodCount;
				}
			}
			
			if (!missingPeriodsForCount.isEmpty()) {
				SimpleSearch simpleSearch = SimpleSearch.toSimpleSearch(simpleSearchQueryAnalyzedStr);
				int missingPeriodsCount = getPeriodCountFieldValue(
						simpleSearch, 
						missingPeriodsForCount, 
						includeFederatedCollections, 
						countFieldName, 
						compileLogSolrServer);
				compileCount += missingPeriodsCount;
			}
			
			SolrDocument solrDoc = periodCountMap.getSolrDocument();
			solrDoc.setField(countFieldName, compileCount);
			solrDocumentList.add(solrDoc);
		}
		Collections.sort(solrDocumentList, new Comparator<SolrDocument>() {
			@Override
			public int compare(SolrDocument o1, SolrDocument o2) {
				Integer count1 = Integer.parseInt(o1.getFieldValue(countFieldName).toString());
				Integer count2 = Integer.parseInt(o2.getFieldValue(countFieldName).toString());
				return -count1.compareTo(count2);
			}
		});
		if (solrDocumentList.size() > maxSize) {
			List<SolrDocument> removableDocs = new ArrayList<SolrDocument>(solrDocumentList.subList(maxSize, solrDocumentList.size()));
			solrDocumentList.removeAll(removableDocs);
		}
		solrDocumentList.setNumFound(solrDocumentList.size());
		return solrDocumentList;
	}
	
	private static String getSimpleSearchStr(SimpleSearch simpleSearch, boolean analyzed) {
		SimpleSearch clone = simpleSearch.clone();
		clone.clearPages();
		clone.clearFacetFoldingAndSorting();
		clone.setSearchLogDocId(null);
		if (analyzed) {
			String collectionName = simpleSearch.getCollectionName();
			RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
			RecordCollection collection = collectionServices.get(collectionName);
			if (collection == null) {
				return null;
			}
			String query = clone.getQuery();
			String queryAnalyzed = AnalyzerUtils.analyze(query, collection);
			clone.setQuery(queryAnalyzed);
		}
		SimpleParams simpleParams = clone.toSimpleParams();
		return simpleParams.toString();
	}
    
    private static String getSimpleSearchId(SimpleSearch simpleSearch) {
        String simpleSearchId = getSimpleSearchStr(simpleSearch, true);
        if (simpleSearchId != null) {
            simpleSearchId = digest(simpleSearchId.getBytes());
        }
        return simpleSearchId;
    }

    private static String generateSearchLogDocId(SimpleSearch simpleSearch) {
        String simpleSearchId = getSimpleSearchId(simpleSearch);
        String uniqueId = simpleSearchId + "_" + newRandomString();
        uniqueId = digest(uniqueId.getBytes());
        return uniqueId;
    }

    private static String generateClickLogDocId(Record record, String simpleSearchId) {
    	String uniqueId = simpleSearchId + "____" + record.getUrl() + "_" + newRandomString();
    	uniqueId = digest(uniqueId.getBytes());
    	return uniqueId;
    }

    private static String generateSearchCompileLogDocId(SimpleSearch simpleSearch) {
        String simpleSearchId = getSimpleSearchId(simpleSearch);
        String uniqueId = simpleSearchId + "_" + newRandomString();
        uniqueId = digest(uniqueId.getBytes());
        return uniqueId;
    }

    private static String generateClickCompileLogDocId(Record record, String simpleSearchId) {
    	String uniqueId = simpleSearchId + "____" + record.getUrl() + "_" + newRandomString();
    	uniqueId = digest(uniqueId.getBytes());
    	return uniqueId;
    }

    private static String newRandomString() {
        long timeMillis = System.currentTimeMillis();
        int randomInt = (int) (Math.random() * Integer.MAX_VALUE);
        return "" + timeMillis + randomInt;
    }

    private static String digest(byte[] content) {
        String digestString;
        try {
            MessageDigest shaDigester = MessageDigest.getInstance("SHA");
            shaDigester.update(content);
            byte[] shaDigest = shaDigester.digest();
            digestString = new String(Base64.encodeBase64(shaDigest));
		    digestString = escape(digestString);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return digestString;
    }
    
    private static String escape(String text) {
        StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < text.length(); i++) {
	    	char c = text.charAt(i);
	      	// These characters are part of the query syntax and must be escaped
	      	if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':'
	    	  || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'
	        	|| c == '*' || c == '?' || c == '|' || c == '&' 
	        	|| c == ' ' || c == '/' || c == '=') {
	      		sb.append('_');
	      	} else {
	    	  	sb.append(c);
	      	}
	    }
	    return sb.toString();
    }
	
	private static String format(Date date) {
        DateFormat dv = DateUtil.getThreadLocalDateFormat();
        return dv.format(date);
	}
	
	@Override
	public SolrDocumentList getQueries(RecordCollection collection, Date startDate, Date endDate,
			boolean includeFederatedCollections, int maxRows) {
		List<String> collectionNames = new ArrayList<String>();
		collectionNames.add(collection.getName());
		if (includeFederatedCollections && collection.isFederationOwner()) {
			FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
			List<RecordCollection> federationCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection federationCollection : federationCollections) {
				collectionNames.add(federationCollection.getName());
			}
		}
		
		StringBuffer querySB = new StringBuffer();
		querySB.append(StatsConstants.INDEX_FIELD_COLLECTION_NAME);
		querySB.append(":(");
		for (Iterator<String> it = collectionNames.iterator(); it.hasNext();) {
			String collectionName = it.next();
			querySB.append(collectionName);
			if (it.hasNext()) {
				querySB.append(" OR ");
			}
		}
		querySB.append(") ");
		querySB.append(StatsConstants.INDEX_FIELD_SEARCH_DATE);
		querySB.append(":[");
		querySB.append(format(startDate));
		querySB.append(" TO ");
		querySB.append(format(endDate));
		querySB.append("]");

		SolrQuery solrQuery = new SolrQuery(querySB.toString());
		solrQuery.setSortField(StatsConstants.INDEX_FIELD_SEARCH_DATE, ORDER.desc);
		solrQuery.setRows(maxRows);

		SolrServer searchLogSolrServer = SolrLogContext.getSearchLogSolrServer();
		QueryResponse queryResponse;
		try {
			queryResponse = searchLogSolrServer.query(solrQuery);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
		SolrDocumentList results = queryResponse.getResults();
		return results;
	}
	
	public SolrDocumentList getClicks(RecordCollection collection, Date startDate, Date endDate,
			boolean includeFederatedCollections, int maxRows) {
		List<String> collectionNames = new ArrayList<String>();
		collectionNames.add(collection.getName());
		if (includeFederatedCollections && collection.isFederationOwner()) {
			FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
			List<RecordCollection> federationCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection federationCollection : federationCollections) {
				collectionNames.add(federationCollection.getName());
			}
		}
		
		StringBuffer querySB = new StringBuffer();
		querySB.append(StatsConstants.INDEX_FIELD_COLLECTION_NAME);
		querySB.append(":(");
		for (Iterator<String> it = collectionNames.iterator(); it.hasNext();) {
			String collectionName = it.next();
			querySB.append(collectionName);
			if (it.hasNext()) {
				querySB.append(" OR ");
			}
		}
		querySB.append(") ");
		querySB.append(StatsConstants.INDEX_FIELD_CLICK_DATE);
		querySB.append(":[");
		querySB.append(format(startDate));
		querySB.append(" TO ");
		querySB.append(format(endDate));
		querySB.append("]");

		SolrQuery solrQuery = new SolrQuery(querySB.toString());
		solrQuery.setSortField(StatsConstants.INDEX_FIELD_CLICK_DATE, ORDER.desc);
		solrQuery.setRows(maxRows);

		SolrServer clickLogSolrServer = SolrLogContext.getClickLogSolrServer();
		QueryResponse queryResponse;
		try {
			queryResponse = clickLogSolrServer.query(solrQuery);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
		SolrDocumentList results = queryResponse.getResults();
		return results;
	}

	@Override
	public int getNbClicks(RecordCollection collection, String searchLogDocId) {
		Map<String, Object> queryClicksKey = new HashMap<String, Object>();
		queryClicksKey.put(StatsConstants.INDEX_FIELD_SEARCH_LOG_DOC_ID, searchLogDocId);
		SolrServer clickLogSolrServer = SolrLogContext.getClickLogSolrServer();
		SolrDocumentList results = list(clickLogSolrServer, queryClicksKey, Integer.MAX_VALUE);
		return results.size();
	}
	
	/*
	 *    - id: random
	 *    - collectionName
	 *    - simpleSearchId
	 *    - simpleSearch: simpleSearch.toSimpleParams().toString()
	 *    - simpleSearchQueryAnalyzed: simpleSearch.toSimpleParams(queryAnalyzed).toString()
	 *    - searchPeriod: ["day"|"month"|"year"] + [yearMonthDay|yearMonth|year]
	 *    - displayUrl
	 *    - clickCount: The number of times a click has occurred for this query for a given day|month|year
	 */
	@Override
	public SolrDocument getMostClickedDocument(SimpleSearch simpleSearch, Date startDate, Date endDate,
			boolean includeFederatedCollections) {
		SolrDocumentList resultList = getMostClickedDocuments(simpleSearch, startDate, endDate, includeFederatedCollections, 1, 1);
		SolrDocument result = !resultList.isEmpty() ? resultList.get(0) : null;
		return result;
	}

	@Override
	public SolrDocumentList getMostClickedDocuments(SimpleSearch simpleSearch, Date startDate, Date endDate,
		boolean includeFederatedCollections, int minClicks, int maxRows) {		
		Map<String, PeriodCountMap> compileMap = new HashMap<String, PeriodCountMap>();
		SolrServer clickCompileLogSolrServer = SolrLogContext.getClickCompileLogSolrServer();
		List<Period> periods = listPeriods(startDate, endDate);

		String customCriteria;
		if (minClicks > 1) {
			customCriteria = StatsConstants.INDEX_FIELD_CLICK_COUNT + ":[" + minClicks + " TO *]";
		} else {
			customCriteria = null;
		}
		
		String simpleSearchId = getSimpleSearchId(simpleSearch);
		for (Period period : periods) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID, simpleSearchId);
			criterias.put(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, period.toString());
			
			int periodMaxRows = maxRows > 1 ? maxRows : 15; // Get 15, just to be safe when merging...
			SolrDocumentList periodResults = list(clickCompileLogSolrServer, criterias, periodMaxRows, StatsConstants.INDEX_FIELD_CLICK_COUNT, customCriteria);
			merge(compileMap, periodResults, StatsConstants.INDEX_FIELD_CLICK_COUNT);
		}
		SolrDocumentList resultList = sortAndReduce(
				startDate, 
				endDate, 
				includeFederatedCollections, 
				clickCompileLogSolrServer,
				compileMap, 
				1, 
				StatsConstants.INDEX_FIELD_CLICK_COUNT);
		return resultList;
	}

	@Override
	public SolrDocumentList getMostPopularQueries(RecordCollection collection, Date startDate, Date endDate,
			boolean includeFederatedCollections, int maxRows) {
		Map<String, PeriodCountMap> compileMap = new HashMap<String, PeriodCountMap>();
		SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
		List<Period> periods = listPeriods(startDate, endDate);
		
		List<String> collectionNames = new ArrayList<String>();
		collectionNames.add(collection.getName());
		if (includeFederatedCollections && collection.isFederationOwner()) {
			FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
			List<RecordCollection> federationCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection federationCollection : federationCollections) {
				collectionNames.add(federationCollection.getName());
			}
		}
		for (Period period : periods) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionNames);
			criterias.put(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, period.toString());
			
			int periodMaxRows = maxRows + period.getFactor();
			SolrDocumentList periodResults = list(searchCompileLogSolrServer, criterias, periodMaxRows, StatsConstants.INDEX_FIELD_SEARCH_COUNT, null);
			merge(compileMap, periodResults, StatsConstants.INDEX_FIELD_SEARCH_COUNT);
		}
		SolrDocumentList resultList = sortAndReduce(
				startDate, 
				endDate, 
				includeFederatedCollections, 
				searchCompileLogSolrServer,
				compileMap, 
				maxRows, 
				StatsConstants.INDEX_FIELD_SEARCH_COUNT);
		return resultList;
	}

	@Override
	public List<String> getMostPopularQueriesAutocomplete(String text, RecordCollection collection, int maxRows) {
		List<String> suggestions = new ArrayList<String>();
		if (text.length() >= 3 && !text.contains("*:*") && !collection.isOpenSearch()) {
			Date endDate = new Date();
			Date startDate = DateUtils.addYears(endDate, -1);
			startDate = ConstellioDateUtils.getFirstDayOfYear(startDate);
			
			Map<String, PeriodCountMap> compileMap = new HashMap<String, PeriodCountMap>();
			SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
			List<Period> periods = listPeriods(startDate, endDate);

			String analyzedText = AnalyzerUtils.analyze(text, collection);
			if (StringUtils.isNotBlank(analyzedText)) {
				StringBuffer customCriteriaSB = new StringBuffer();
//				customCriteria.append("(");
				customCriteriaSB.append(StatsConstants.INDEX_FIELD_QUERY_TEXT_ANALYZED + ":" + QueryParser.escape(analyzedText) + "*");
//				customCriteria.append(" OR ");
//				customCriteria.append(StatsConstants.INDEX_FIELD_QUERY_TEXT + ":" + text.trim() + "*");
//				customCriteria.append(")");
				String customCriteria = customCriteriaSB.toString();
				
				for (Period period : periods) {
					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.put(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, period.toString());
					
					int periodMaxRows = maxRows;
					SolrDocumentList periodResults = list(searchCompileLogSolrServer, criterias, periodMaxRows, StatsConstants.INDEX_FIELD_SEARCH_COUNT, customCriteria);
					merge(compileMap, periodResults, StatsConstants.INDEX_FIELD_SEARCH_COUNT);
				}
				SolrDocumentList resultList = sortAndReduce(
						startDate, 
						endDate, 
						true, 
						searchCompileLogSolrServer,
						compileMap, 
						maxRows, 
						StatsConstants.INDEX_FIELD_SEARCH_COUNT);
				
				SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();
				SearchInterfaceConfig searchInterfaceConfig = searchInterfaceConfigServices.get();
				Integer autoCompleteMinQueries = searchInterfaceConfig.getAutocompleteMinQueries();
				if (autoCompleteMinQueries == null || autoCompleteMinQueries < 1) {
					autoCompleteMinQueries = 1;
				}
				
				List<String> analyzedSuggestions = new ArrayList<String>();
				for (SolrDocument result : resultList) {
					String analyzedQueryText = result.getFieldValue(StatsConstants.INDEX_FIELD_QUERY_TEXT_ANALYZED).toString();
					String queryText = result.getFieldValue(StatsConstants.INDEX_FIELD_QUERY_TEXT).toString();
					queryText = queryText.toLowerCase();
					int count = Integer.valueOf(result.getFieldValue(StatsConstants.INDEX_FIELD_SEARCH_COUNT).toString());
					if (count >= autoCompleteMinQueries) {
						if (analyzedQueryText.startsWith(analyzedText) && !analyzedSuggestions.contains(analyzedQueryText)) {
							analyzedSuggestions.add(analyzedQueryText);
							if (!suggestions.contains(queryText)) {
								suggestions.add(queryText);
							}
						}
					}
				}
			}
		}
		return suggestions;
	}

	@Override
	public SolrDocumentList getMostPopularQueriesWithResults(RecordCollection collection, Date startDate,
			Date endDate, boolean includeFederatedCollections, int maxRows) {
		Map<String, PeriodCountMap> compileMap = new HashMap<String, PeriodCountMap>();
		SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
		List<Period> periods = listPeriods(startDate, endDate);

		List<String> collectionNames = new ArrayList<String>();
		collectionNames.add(collection.getName());
		if (includeFederatedCollections && collection.isFederationOwner()) {
			FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
			List<RecordCollection> federationCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection federationCollection : federationCollections) {
				collectionNames.add(federationCollection.getName());
			}
		}
		for (Period period : periods) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionNames);
			criterias.put(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, period.toString());
			criterias.put(StatsConstants.INDEX_FIELD_HAS_RESULT, true);
			
			int periodMaxRows = maxRows + period.getFactor();
			SolrDocumentList periodResults = list(searchCompileLogSolrServer, criterias, periodMaxRows, StatsConstants.INDEX_FIELD_SEARCH_COUNT, null);
			merge(compileMap, periodResults, StatsConstants.INDEX_FIELD_SEARCH_COUNT);
		}
		SolrDocumentList resultList = sortAndReduce(
				startDate, 
				endDate, 
				includeFederatedCollections, 
				searchCompileLogSolrServer,
				compileMap, 
				maxRows, 
				StatsConstants.INDEX_FIELD_SEARCH_COUNT);
		return resultList;
	}

	@Override
	public SolrDocumentList getMostPopularQueriesWithoutResults(RecordCollection collection, Date startDate,
			Date endDate, boolean includeFederatedCollections, int maxRows) {
		Map<String, PeriodCountMap> compileMap = new HashMap<String, PeriodCountMap>();
		SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
		List<Period> periods = listPeriods(startDate, endDate);

		List<String> collectionNames = new ArrayList<String>();
		collectionNames.add(collection.getName());
		if (includeFederatedCollections && collection.isFederationOwner()) {
			FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
			List<RecordCollection> federationCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection federationCollection : federationCollections) {
				collectionNames.add(federationCollection.getName());
			}
		}
		for (Period period : periods) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionNames);
			criterias.put(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, period.toString());
			criterias.put(StatsConstants.INDEX_FIELD_HAS_RESULT, false);
			
			int periodMaxRows = maxRows + period.getFactor();
			SolrDocumentList periodResults = list(searchCompileLogSolrServer, criterias, periodMaxRows, StatsConstants.INDEX_FIELD_SEARCH_COUNT, null);
			merge(compileMap, periodResults, StatsConstants.INDEX_FIELD_SEARCH_COUNT);
		}
		SolrDocumentList resultList = sortAndReduce(
				startDate, 
				endDate, 
				includeFederatedCollections, 
				searchCompileLogSolrServer,
				compileMap, 
				maxRows, 
				StatsConstants.INDEX_FIELD_SEARCH_COUNT);
		return resultList;
	}

	@Override
	public SolrDocumentList getMostPopularQueriesWithClick(RecordCollection collection, Date startDate,
			Date endDate, boolean includeFederatedCollections, int maxRows) {
		Map<String, PeriodCountMap> compileMap = new HashMap<String, PeriodCountMap>();
		SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
		List<Period> periods = listPeriods(startDate, endDate);

		List<String> collectionNames = new ArrayList<String>();
		collectionNames.add(collection.getName());
		if (includeFederatedCollections && collection.isFederationOwner()) {
			FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
			List<RecordCollection> federationCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection federationCollection : federationCollections) {
				collectionNames.add(federationCollection.getName());
			}
		}
		for (Period period : periods) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionNames);
			criterias.put(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, period.toString());
			criterias.put(StatsConstants.INDEX_FIELD_HAS_CLICK, true);
			
			int periodMaxRows = maxRows + period.getFactor();
			SolrDocumentList periodResults = list(searchCompileLogSolrServer, criterias, periodMaxRows, StatsConstants.INDEX_FIELD_SEARCH_COUNT, null);
			merge(compileMap, periodResults, StatsConstants.INDEX_FIELD_SEARCH_COUNT);
		}
		SolrDocumentList resultList = sortAndReduce(
				startDate, 
				endDate, 
				includeFederatedCollections, 
				searchCompileLogSolrServer,
				compileMap, 
				maxRows, 
				StatsConstants.INDEX_FIELD_SEARCH_COUNT);
		return resultList;
	}

	@Override
	public SolrDocumentList getMostPopularQueriesWithoutClick(RecordCollection collection, Date startDate,
			Date endDate, boolean includeFederatedCollections, int maxRows) {
		Map<String, PeriodCountMap> compileMap = new HashMap<String, PeriodCountMap>();
		SolrServer searchCompileLogSolrServer = SolrLogContext.getSearchCompileLogSolrServer();
		List<Period> periods = listPeriods(startDate, endDate);

		List<String> collectionNames = new ArrayList<String>();
		collectionNames.add(collection.getName());
		if (includeFederatedCollections && collection.isFederationOwner()) {
			FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
			List<RecordCollection> federationCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection federationCollection : federationCollections) {
				collectionNames.add(federationCollection.getName());
			}
		}
		for (Period period : periods) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionNames);
			criterias.put(StatsConstants.INDEX_FIELD_SEARCH_PERIOD, period.toString());
			criterias.put(StatsConstants.INDEX_FIELD_HAS_CLICK, false);
			
			int periodMaxRows = maxRows + period.getFactor();
			SolrDocumentList periodResults = list(searchCompileLogSolrServer, criterias, periodMaxRows, StatsConstants.INDEX_FIELD_SEARCH_COUNT, null);
			merge(compileMap, periodResults, StatsConstants.INDEX_FIELD_SEARCH_COUNT);
		}
		SolrDocumentList resultList = sortAndReduce(
				startDate, 
				endDate, 
				includeFederatedCollections, 
				searchCompileLogSolrServer,
				compileMap, 
				maxRows, 
				StatsConstants.INDEX_FIELD_SEARCH_COUNT);
		return resultList;
	}
	
	private static String formatDate(Date date) {
		return date != null ? new SimpleDateFormat(DATE_PATTERN).format(date) : "";
	}
	
	public static void main(String[] args) {
		Date startDate = ConstellioDateUtils.toDate(2012, 1, 31);
		Date endDate = ConstellioDateUtils.toDate(2012, 2, 26);
		List<Period> periods = listPeriods(startDate, endDate);
		for (Period period : periods) {
			System.out.println(period);
		}
	}

}
