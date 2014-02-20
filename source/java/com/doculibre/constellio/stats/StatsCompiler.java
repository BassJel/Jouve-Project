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
package com.doculibre.constellio.stats;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest.ACTION;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.DateUtil;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class StatsCompiler {
	
    private static StatsCompiler compiler;

    private StatsCompiler() {
    }

    public static StatsCompiler getInstance() {
        if (compiler == null) {
            compiler = new StatsCompiler();
        }
        return compiler;
    }

	public static String escape(String s) {
//		QueryParser.escape FIXME
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':' || c == '^'
					|| c == '[' || c == ']' || c == '"' || c == '{' || c == '}' || c == '~' || c == '*'
					|| c == '?' || c == '|' || c == '&') {
//				sb.append('\\');
//				sb.append(c);
				sb.append('_');
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String format(Date date) {
        DateFormat dv = DateUtil.getThreadLocalDateFormat();
        return dv.format(date).replace("-", "");
	}
	
    public synchronized void saveStats(SimpleSearch simpleSearch, SolrServer indexJournal,
        SolrServer indexCompile, QueryResponse res) throws SolrServerException,
        IOException {
    	String collectionName = simpleSearch.getCollectionName();
    	String luceneQuery = simpleSearch.getLuceneQuery();
    	
        GregorianCalendar calendar = new GregorianCalendar();
        Date time = new Date();
        calendar.setTime(time);
        String query = luceneQuery;
        String escapedQuery = escape(query);
        long nbRes = res.getResults().getNumFound();
        long qTime = res.getQTime();
        String desplayDate = time.toString();
        String searchDate = format(time);
        String queryWithParams = simpleSearch.toSimpleParams().toString();
        
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", desplayDate + query);
        doc.addField("query", query);
        doc.addField("queryWithParams", queryWithParams);
        doc.addField("nbres", "" + nbRes);
        doc.addField("qtime", "" + qTime);
        doc.addField("dateaffiche", desplayDate);
        doc.addField("date", searchDate);
        doc.addField("recherche", "recherche");
        doc.addField("collection", collectionName);

        UpdateRequest up = new UpdateRequest();
        up.setAction(ACTION.COMMIT, true, true);
        up.add(doc);

        up.process(indexJournal);

        String compileId = "collection_" + collectionName + " id_" + escapedQuery;
        SolrQuery solrQuery = new SolrQuery();
        // Requête Lucene
        solrQuery.setQuery("id:\"" + compileId + "\"");
        // nb résultats par page
        solrQuery.setRows(15);
        // page de début
        solrQuery.setStart(0);
        QueryResponse qr = indexCompile.query(solrQuery);
        if (qr.getResults().getNumFound() > 0) {
            SolrDocument sd = qr.getResults().get(0);
            long freq = (Long) sd.getFieldValue("freq");
            long click = (Long) sd.getFieldValue("click");
            // indexCompile.deleteById(query);
            SolrInputDocument docCompile = new SolrInputDocument();
            docCompile.addField("id", compileId);
            docCompile.addField("query", query);
            if (!((String) sd.getFieldValue("nbres")).equals("0")) {
                ConstellioSpringUtils.getAutocompleteServices().onQueryAdd(docCompile, query);
            }
            docCompile.addField("freq", freq + 1);
            docCompile.addField("nbres", "" + nbRes);
            docCompile.addField("recherche", "recherche");
            docCompile.addField("collection", collectionName);
            if (nbRes == 0) {
                docCompile.addField("zero", "true");
            } else {
                docCompile.addField("zero", "false");
            }
            docCompile.addField("click", click);
            if (click == 0) {
                docCompile.addField("clickstr", "zero");
            } else {
                docCompile.addField("clickstr", "notzero");
            }
            up.clear();
            up.setAction(ACTION.COMMIT, true, true);
            up.add(docCompile);

            up.process(indexCompile);
        } else {
            SolrInputDocument docCompile = new SolrInputDocument();
            docCompile.addField("id", compileId);
            docCompile.addField("query", query);
            if (nbRes != 0) {
                ConstellioSpringUtils.getAutocompleteServices().onQueryAdd(docCompile, query);
            }
            docCompile.addField("freq", 1);
            docCompile.addField("recherche", "recherche");
            docCompile.addField("collection", collectionName);
            docCompile.addField("nbres", "" + nbRes);
            if (nbRes == 0) {
                docCompile.addField("zero", "true");
            } else {
                docCompile.addField("zero", "false");
            }
            docCompile.addField("click", 0);
            docCompile.addField("clickstr", "zero");
            up.clear();
            up.setAction(ACTION.COMMIT, true, true);
            up.add(docCompile);

            up.process(indexCompile);
        }
    }

    public synchronized void computeClick(String collectionName, SolrServer indexCompile,
        SimpleSearch simpleSearch) throws SolrServerException, IOException {
        String query = simpleSearch.getLuceneQuery();
        String escapedQuery = escape(query);
        String compileId = "collection_" + collectionName + " id_" + escapedQuery;
        
        SolrQuery solrQuery = new SolrQuery();
        // Requête Lucene
        solrQuery.setQuery("id:\"" + compileId + "\"");
        // nb résultats par page
        solrQuery.setRows(15);
        // page de début
        solrQuery.setStart(0);
        
        QueryResponse qr = indexCompile.query(solrQuery);
        if (qr.getResults().getNumFound() > 0) {
            SolrDocument sd = qr.getResults().get(0);
            long click = (Long) sd.getFieldValue("click");
            indexCompile.deleteById(escapedQuery);
            SolrInputDocument docCompile = new SolrInputDocument();
            docCompile.addField("id", compileId);
            docCompile.addField("query", query);
            if (!((String) sd.getFieldValue("nbres")).equals("0")) {
                ConstellioSpringUtils.getAutocompleteServices().onQueryAdd(docCompile, query);
            }
            docCompile.addField("freq", (Long) sd.getFieldValue("freq"));
            docCompile.addField("recherche", "recherche");
            docCompile.addField("zero", (String) sd.getFieldValue("zero"));
            docCompile.addField("nbres", (String) sd.getFieldValue("nbres"));
            docCompile.addField("click", (click + 1));
            docCompile.addField("clickstr", "notzero");
            docCompile.addField("collection", collectionName);
            UpdateRequest up = new UpdateRequest();
            up.setAction(ACTION.COMMIT, true, true);
            up.add(docCompile);
            up.process(indexCompile);
        }
    }

    public synchronized void computeClickUrl(String collectionName, String url, String recordURL,
        SolrServer indexurl, SimpleSearch simpleSearch) throws SolrServerException, IOException {
        String query = simpleSearch.getLuceneQuery();
        String escapedQuery = escape(query);
        String compileId = "url_" + url + " collection_" + collectionName + " id_" + escapedQuery;
        SolrQuery solrQuery = new SolrQuery();
        // Requête Lucene
        solrQuery.setQuery("id:\"" + compileId + "\"");
        // nb résultats par page
        solrQuery.setRows(15);
        // page de début
        solrQuery.setStart(0);
        QueryResponse qr = indexurl.query(solrQuery);
        if (qr.getResults().getNumFound() > 0) {
            SolrDocument sd = qr.getResults().get(0);
            long nbClick = (Long) sd.getFieldValue("nbclick");
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", compileId);
            doc.addField("query", escapedQuery);
            doc.addField("url", url);
            doc.addField("nbclick", nbClick + 1);
            doc.addField("recordURL", recordURL);
            doc.addField("collectionName", collectionName);
            UpdateRequest up = new UpdateRequest();
            up.setAction(ACTION.COMMIT, true, true);
            up.add(doc);
            up.process(indexurl);
        } else {
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", compileId);
            doc.addField("query", escapedQuery);
            doc.addField("url", url);
            doc.addField("nbclick", 0);
            doc.addField("recordURL", recordURL);
            doc.addField("collectionName", collectionName);
            UpdateRequest up = new UpdateRequest();
            up.setAction(ACTION.COMMIT, true, true);
            up.add(doc);
            up.process(indexurl);
        }
    }

    public String createQuery(ListOrderedMap m_queries) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object s : m_queries.keyList()) {
            if ((i > 0) && (m_queries.size() > 1)) {
                sb.append(" ");
            }
            sb.append(s);

            i++;
        }
        return sb.toString();
    }

}
