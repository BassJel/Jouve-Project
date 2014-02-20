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

import java.io.Serializable;
import java.util.Date;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

@SuppressWarnings("serial")
public class StatsSearcher implements Serializable {

	public QueryResponse getMostPopularQueries(String collectionName, SolrServer server, Date startDate,
			Date endDate, int start, int row) throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setStart(start);
		solrQuery.setRows(row);
		solrQuery.setSort("freq", SolrQuery.ORDER.desc);
		StringBuffer querySB = new StringBuffer("collection:" + collectionName + " recherche:recherche");
//		querySB.append(" date:[");
//		querySB.append(StatsCompiler.format(startDate));
//		querySB.append(" TO ");
//		querySB.append(StatsCompiler.format(endDate));
//		querySB.append("]");
		solrQuery.setQuery(querySB.toString());
		return server.query(solrQuery);
	}

	public QueryResponse getMostPopularQueriesZeroRes(String collectionName, SolrServer server,
			Date startDate, Date endDate, int start, int row) throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setStart(start);
		solrQuery.setRows(row);
		solrQuery.setSort("freq", SolrQuery.ORDER.desc);
		StringBuffer querySB = new StringBuffer("collection:" + collectionName + " zero:true");
//		querySB.append(" date:[");
//		querySB.append(StatsCompiler.format(startDate));
//		querySB.append(" TO ");
//		querySB.append(StatsCompiler.format(endDate));
//		querySB.append("]");
		solrQuery.setQuery(querySB.toString());
		return server.query(solrQuery);
	}

	public QueryResponse getMostPopularQueriesWithRes(String collectionName, SolrServer server,
			Date startDate, Date endDate, int start, int row) throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setStart(start);
		solrQuery.setRows(row);
		solrQuery.setSort("freq", SolrQuery.ORDER.desc);
		StringBuffer querySB = new StringBuffer("collection:" + collectionName + " zero:false");
//		querySB.append(" date:[");
//		querySB.append(StatsCompiler.format(startDate));
//		querySB.append(" TO ");
//		querySB.append(StatsCompiler.format(endDate));
//		querySB.append("]");
		solrQuery.setQuery(querySB.toString());
		return server.query(solrQuery);
	}

	public QueryResponse getMostPopularQueriesWithClick(String collectionName, SolrServer server,
			Date startDate, Date endDate, int start, int row) throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setStart(start);
		solrQuery.setRows(row);
		solrQuery.setSort("freq", SolrQuery.ORDER.desc);
		StringBuffer querySB = new StringBuffer("collection:" + collectionName + " clickstr:notzero");
//		querySB.append(" date:[");
//		querySB.append(StatsCompiler.format(startDate));
//		querySB.append(" TO ");
//		querySB.append(StatsCompiler.format(endDate));
//		querySB.append("]");
		solrQuery.setQuery(querySB.toString());
		return server.query(solrQuery);
	}

	public QueryResponse getMostPopularQueriesWithoutClick(String collectionName, SolrServer server,
			Date startDate, Date endDate, int start, int row) throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setStart(start);
		solrQuery.setRows(row);
		solrQuery.setSort("freq", SolrQuery.ORDER.desc);
		StringBuffer querySB = new StringBuffer("collection:" + collectionName + " clickstr:zero");
//		querySB.append(" date:[");
//		querySB.append(StatsCompiler.format(startDate));
//		querySB.append(" TO ");
//		querySB.append(StatsCompiler.format(endDate));
//		querySB.append("]");
		solrQuery.setQuery(querySB.toString());
		return server.query(solrQuery);
	}

	public QueryResponse getQueryLog(String collectionName, SolrServer server, Date startDate, Date endDate,
			int start, int row) throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setStart(start);
		solrQuery.setRows(row);
		solrQuery.setSort("date", SolrQuery.ORDER.desc);
		StringBuffer querySB = new StringBuffer("collection:" + collectionName + " recherche:recherche");
		querySB.append(" date:[");
		querySB.append(StatsCompiler.format(startDate));
		querySB.append(" TO ");
		querySB.append(StatsCompiler.format(endDate));
		querySB.append("]");
		solrQuery.setQuery(querySB.toString());
		return server.query(solrQuery);
	}

	// La liste des URL des documents les plus consult� pour une requête
	public QueryResponse getMostClickedDocsForQuery(String collectionName, String query, SolrServer server,
			int start, int row) throws SolrServerException {
		String escapedQuery = StatsCompiler.escape(query);
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setStart(start);
		solrQuery.setRows(row);
		solrQuery.setSort("nbclick", SolrQuery.ORDER.desc);
		StringBuffer querySB = new StringBuffer("collectionName:" + collectionName + " query:\"" + escapedQuery + "\"");
		solrQuery.setQuery(querySB.toString());
		return server.query(solrQuery);
	}

	// La liste des requêtes les plus utilisées pour une url
	public QueryResponse getMostClickedQueriesForURL(String collectionName, String url, SolrServer server,
			int start, int row) throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setStart(start);
		solrQuery.setRows(row);
		solrQuery.setSort("nbclick", SolrQuery.ORDER.desc);
		StringBuffer querySB = new StringBuffer("collectionName:" + collectionName + " url:\"" + url + "\"");
		solrQuery.setQuery(querySB.toString());
		return server.query(solrQuery);
	}

}
