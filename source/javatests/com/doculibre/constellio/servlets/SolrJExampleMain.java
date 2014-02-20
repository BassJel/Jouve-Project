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
package com.doculibre.constellio.servlets;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.servlet.SolrRequestParsers;

/**
 * A simple main to illustrate how to execute a request using SolrJ
 * 
 * @author francisbaril
 * 
 */
public class SolrJExampleMain {

	private static final String myServer = "http://localhost:8080/constellio/app";

	private static final String myCollection = "test";

	// Can be set to 'on', 'off' or 'constellio' to include Constellio's facets
	private static final String facet = "constellio";

	// q=...
	private static final String query = "open source";

	private static final int start = 0;
	private static final int nbDocuments = 11;

	public static void main(String[] args) throws MalformedURLException,
			SolrServerException {

		// Prepare the SolrServer. Right now, the default SolrJ's ResponseParser
		// isn't supported by Constellio.
		HttpSolrServer server = new HttpSolrServer(myServer);
		server.setParser(new XMLResponseParser());

		// Do the same query three times using three different method
		System.out
				.println("= = = = = = = = = = = = = = = = = First way to execute a query = = = = = = = = = = = = = = = = =");
		print(doFirstQuery(server));
		System.out
				.println("= = = = = = = = = = = = = = = = = Second way to execute a query = = = = = = = = = = = = = = = = =");
		print(doSecondQuery(server));
		System.out
				.println("= = = = = = = = = = = = = = = = = Third way to execute query = = = = = = = = = = = = = = = = =");
		print(doThirdQuery(server));
		System.out
				.println("= = = = = = = = = = = = = = = = = Using SpellChecker = = = = = = = = = = = = = = = = =");
		print(spellCheck(server, "opn sorce source"));
	}

	/**
	 * Do the query using a StringBuffer
	 */
	public static QueryResponse doFirstQuery(SolrServer server)
			throws SolrServerException {
		StringBuffer request = new StringBuffer();
		request.append("collectionName=" + myCollection);
		request.append("&facet=" + facet);
		request.append("&q=" + query);
		request.append("&start=" + start);
		request.append("&rows=" + nbDocuments);
		SolrParams solrParams = SolrRequestParsers.parseQueryString(request
				.toString());

		return server.query(solrParams);
	}

	/**
	 * Do the query using a ModifiableSolrParams
	 */
	public static QueryResponse doSecondQuery(SolrServer server)
			throws SolrServerException {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set("collectionName", myCollection);
		solrParams.set("facet", facet);
		solrParams.set("q", query);
		solrParams.set("start", start);
		solrParams.set("rows", nbDocuments);
		return server.query(solrParams);
	}

	/**
	 * Do the query using a SolrQuery
	 */
	public static QueryResponse doThirdQuery(SolrServer server)
			throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery(query);
		solrQuery.set("collectionName", myCollection);
		solrQuery.set("facet", facet);
		solrQuery.setStart(start);
		solrQuery.setRows(nbDocuments);
		return server.query(solrQuery);
	}

	/**
	 * Do the query using a SolrQuery
	 */
	public static QueryResponse spellCheck(SolrServer server, String badQuery)
			throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery(badQuery);
		solrQuery.set("collectionName", myCollection);

		// qt=spellcheck || qt=spellchecker
		solrQuery.setRequestHandler("spellcheck");
		return server.query(solrQuery);
	}

	/**
	 * Print documents and facets
	 * 
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	public static void print(QueryResponse response) {
		SolrDocumentList docs = response.getResults();
		if (docs != null) {
			System.out.println(docs.getNumFound() + " documents found, "
					+ docs.size() + " returned : ");
			for (int i = 0; i < docs.size(); i++) {
				SolrDocument doc = docs.get(i);
				System.out.println("\t" + doc.toString());
			}
		}

		List<FacetField> fieldFacets = response.getFacetFields();
		if (fieldFacets != null && fieldFacets.isEmpty()) {
			System.out.println("\nField Facets : ");
			for (FacetField fieldFacet : fieldFacets) {
				System.out.print("\t" + fieldFacet.getName() + " :\t");
				if (fieldFacet.getValueCount() > 0) {
					for (Count count : fieldFacet.getValues()) {
						System.out.print(count.getName() + "["
								+ count.getCount() + "]\t");
					}
				}
				System.out.println("");
			}
		}

		Map<String, Integer> queryFacets = response.getFacetQuery();
		if (queryFacets != null && !queryFacets.isEmpty()) {
			System.out.println("\nQuery facets : ");
			for (String queryFacet : queryFacets.keySet()) {
				System.out.println("\t" + queryFacet + "\t["
						+ queryFacets.get(queryFacet) + "]");
			}
			System.out.println("");
		}

		NamedList<NamedList<Object>> spellCheckResponse = (NamedList<NamedList<Object>>) response
				.getResponse().get("spellcheck");

		if (spellCheckResponse != null) {
			Iterator<Entry<String, NamedList<Object>>> wordsIterator = spellCheckResponse
					.iterator();

			while (wordsIterator.hasNext()) {
				Entry<String, NamedList<Object>> entry = wordsIterator.next();
				String word = entry.getKey();
				NamedList<Object> spellCheckWordResponse = entry.getValue();
				boolean correct = spellCheckWordResponse.get("frequency")
						.equals(1);
				System.out.println("Word: " + word + ",\tCorrect?: " + correct);
				NamedList<Integer> suggestions = (NamedList<Integer>) spellCheckWordResponse
						.get("suggestions");
				if (suggestions != null && suggestions.size() > 0) {
					System.out.println("Suggestions : ");
					Iterator<Entry<String, Integer>> suggestionsIterator = suggestions
							.iterator();
					while (suggestionsIterator.hasNext()) {
						System.out.println("\t"
								+ suggestionsIterator.next().getKey());
					}

				}
				System.out.println("");
			}

		}

	}

}
