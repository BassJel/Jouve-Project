/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doculibre.constellio.solr.handler.component;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SearchComponent implementation which provides support for generate log
 * index.
 * 
 * 
 * @since solr 1.3
 */
public class SearchLogComponent extends SearchComponent implements SolrCoreAware {
	private static final Logger LOG = LoggerFactory.getLogger(SearchLogComponent.class);

	@SuppressWarnings("unchecked")
	protected String searchLogCoreName;
	protected int commitThreshold;
	protected int localPort;
	protected HttpSolrServer searchLogServer;
	protected List<SolrInputDocument> searchLogCache;

	public static void main(String[] args) {
		String shardUrl = "192.168.88.1:8983/solr/collection1/|192.168.88.1:8900/solr/collection1/";
		String[] shardUrlStrs = shardUrl.split("\\|")[0].split("/");
		String collectionName = shardUrlStrs[shardUrlStrs.length - 1];
		System.out.println(collectionName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void init(NamedList args) {
		LOG.info("Initializing searchLogComponent");
		super.init(args);
		searchLogCoreName = StringUtils.defaultString((String) args.get("coreName"), "search_log");
		commitThreshold = (int) args.get("commitThreshold");
		searchLogCache = new ArrayList<SolrInputDocument>();
		localPort = 0;
		searchLogServer = null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void prepare(ResponseBuilder rb) throws IOException {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void process(ResponseBuilder rb) throws IOException {
		SolrParams params = rb.req.getParams();
		// Date startTime =new Date();

		String event = params.get("event");
		String ids = params.get("ids");
		String shardUrl = params.get("shard.url");

		// filter the query warming clause and di request
		if (!((event != null && event.equals("firstSearcher")) || (ids != null))) {
			String[] shardUrlStrs = shardUrl.split("\\\\|")[0].split("/");
			String collectionName = shardUrlStrs[shardUrlStrs.length - 1];
			String queryText = rb.getQueryString();
			String queryTextAnalyzed = queryText;
			// String queryTextAnalyzed =
			// AnalyzerUtils.analyze(queryText,collection);
			int searchPage = params.getInt("page", 0);
			String simpleSearchStr = getSimpleSearchStr(params);
			String simpleSearchId = getSimpleSearchId(simpleSearchStr);
			String searchLogDocId = generateSearchLogDocId(simpleSearchId);
			
			String simpleSearchQueryAnalyzedStr = params.toString();
			// String simpleSearchQueryAnalyzedStr
			// =getSimpleSearchStr(simpleSearch, true);
			long timeCost = rb.rsp.getEndTime() - rb.req.getStartTime();

			SolrInputDocument doc = new SolrInputDocument();
			doc.setField(StatsConstants.INDEX_FIELD_ID, searchLogDocId);
			doc.setField(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionName);
			doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_ID, simpleSearchId);
			doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH, simpleSearchStr);
			doc.setField(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH_QUERY_ANALYZED, simpleSearchQueryAnalyzedStr);
			doc.setField(StatsConstants.INDEX_FIELD_QUERY_TEXT, queryText);
			doc.setField(StatsConstants.INDEX_FIELD_QUERY_TEXT_ANALYZED, queryTextAnalyzed);
			doc.setField(StatsConstants.INDEX_FIELD_NUM_FOUND, rb.getNumberDocumentsFound());
			doc.setField(StatsConstants.INDEX_FIELD_RESPONSE_TIME, timeCost);
			doc.setField(StatsConstants.INDEX_FIELD_SEARCH_DATE, new Date());
			doc.setField(StatsConstants.INDEX_FIELD_SEARCH_PAGE, searchPage);

			try {
//				searchLogCache.add(doc);
//				if (searchLogCache.size() >= commitThreshold) {
					int port = Integer.parseInt(shardUrl.substring(shardUrl.indexOf(":") + 1, shardUrl.indexOf("/")));
					if (searchLogServer == null || localPort != port) {
						localPort = port;
						searchLogServer = new HttpSolrServer("http://localhost:" + localPort + "/solr/" + searchLogCoreName);
					}
//					searchLogServer.add(searchLogCache);
					searchLogServer.add(doc);
					searchLogServer.commit();
//					searchLogCache.clear();
//				}
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println("premier phase:"+timeCost);
		}
		// Date endTime =new Date();
		// System.out.println("total time:"+(endTime.getTime()-startTime.getTime()));
	}

	private static String getSimpleSearchStr(SolrParams params) {
//		return params.toString();
	    StringBuffer result= new StringBuffer();
	    
	    for(Iterator<String> it=params.getParameterNamesIterator(); it.hasNext(); ) {
	      final String name = it.next();
	      if(name.equals("shard.url")) continue;
	      final String [] values = params.getParams(name);
	      if(values.length==1) {
	        result.append(name+"="+values[0]+"&");
	      } else {
	        // currently no reason not to use the same array
	        result.append(name+"="+values+"&");
	      }
	    }
	    return result.toString();
	}

	private static String getSimpleSearchId(String simpleSearchId) {
		if (simpleSearchId != null) {
			simpleSearchId = digest(simpleSearchId.getBytes());
		}
		return simpleSearchId;
	}

	private static String generateSearchLogDocId(String simpleSearchStr) {
		String uniqueId = simpleSearchStr + "_" + newRandomString();
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
			if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':' || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~' || c == '*'
					|| c == '?' || c == '|' || c == '&' || c == ' ' || c == '/' || c == '=') {
				sb.append('_');
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	@Override
	public void inform(SolrCore core) {
	}

	// ///////////////////////////////////////////
	// / SolrInfoMBean
	// //////////////////////////////////////////

	@Override
	public String getDescription() {
		return "A search log component";
	}

	@Override
	public String getSource() {
		return "$URL: http://svn.apache.org/repos/asf/lucene/dev/branches/lucene_solr_4_4/solr/core/src/java/org/apache/solr/handler/component/SearchLogComponent.java $";
	}
}
