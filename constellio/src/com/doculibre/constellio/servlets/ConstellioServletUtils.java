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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SearchedFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

/**
 * Some
 * 
 * @author francisbaril
 * 
 */
public class ConstellioServletUtils {

	/**
	 * Inspired by http://www.java2s.com/Code/Java/Servlets/PasswordServlet.htm
	 * 
	 * @param request
	 * @return
	 */
	public static ConstellioUser getAuthentifiedUser(HttpServletRequest request) {

		String username = request.getParameter(ServletsConstants.USER_PARAM);
		String clientDigest = request
				.getParameter(ServletsConstants.DIGEST_PARAM);

		UserServices userServices = ConstellioSpringUtils.getUserServices();
		ConstellioUser user = userServices.get(username);
		if (user != null) {
			String passwordHash = user.getPasswordHash();

			if (!isEqual(passwordHash.getBytes(),
					HexUtils.hexToBytes(clientDigest))) {
				user = null;
			}

		}

		return user;
	}

	private static boolean isEqual(byte[] one, byte[] two) {
		if (one.length != two.length)
			return false;
		for (int i = 0; i < one.length; i++)
			if (one[i] != two[i])
				return false;
		return true;
	}

	public static RecordCollection getCollection(HttpServletRequest request) {
		RecordCollectionServices collectionServices = ConstellioSpringUtils
				.getRecordCollectionServices();

		String collectionName = request
				.getParameter(ServletsConstants.RECORD_COLLECTION_PARAM);
		if (collectionName == null) {
			List<RecordCollection> collections = collectionServices.list();
			if (collections.size() != 1) {
				throw new RuntimeException("The parameter "
						+ ServletsConstants.RECORD_COLLECTION_PARAM
						+ " is required for multiple collections server");
			} else {
				return collections.get(0);
			}
		}

		RecordCollection collection = collectionServices.get(collectionName);
		if (collection == null) {
			throw new RuntimeException("No such collection : " + collectionName);
		}
		return collection;
	}

	public static void respondWithMessage(HttpServletResponse response,
			String message) {

		response.setContentType("text/plain");
		response.setContentLength(message.length());

		PrintWriter pw;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		pw.println(message);

	}

	public static void respondWithExceptionMessage(
			HttpServletResponse response, Exception ex) {

		response.setContentType("text/plain");
		PrintWriter pw;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ex.printStackTrace(pw);

	}

	public static byte[] makeDigest(String user, String password) {
		return ConstellioUser.getHash(password).getBytes();
	}

	public static NamedList<Object> getErrorNamedList(Exception e,
			SolrParams solrParams) {
		NamedList<Object> responseHeader = new NamedList<Object>();
		responseHeader.add(ServletsConstants.RESPONSE_STATUS, 500);

		NamedList<Object> error = new NamedList<Object>();
		error.add(ServletsConstants.MESSAGE, e.getMessage());
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.flush();
		pw.close();
		error.add(ServletsConstants.STACK_TRACE, sw.getBuffer().toString());

		addParams(responseHeader, solrParams);

		NamedList<Object> nl = new NamedList<Object>();
		nl.add(ServletsConstants.RESPONSE_HEADER, responseHeader);
		nl.add(ServletsConstants.ERROR, error);
		return nl;
	}

	public static NamedList<Object> getSuccessfulNamedList(
			SolrParams solrParams) {
		NamedList<Object> responseHeader = new NamedList<Object>();
		responseHeader.add(ServletsConstants.RESPONSE_STATUS, 0);
		NamedList<Object> nl = new NamedList<Object>();
		nl.add(ServletsConstants.RESPONSE_HEADER, responseHeader);
		addParams(responseHeader, solrParams);
		return nl;
	}

	private static void addParams(NamedList<Object> responseHeader,
			SolrParams solrParams) {
		NamedList<Object> params = new NamedList<Object>();
		responseHeader.add("params", params);
		Iterator<String> enumParams = solrParams.getParameterNamesIterator();
		while (enumParams.hasNext()) {
			String param = enumParams.next();
			if (!param.equals(ServletsConstants.DIGEST_PARAM)) {
				String[] values = solrParams.getParams(param);
				params.add(param, values.length > 1 ? Arrays.asList(values)
						: values[0]);
			}
		}
	}

	public static void completeSearch(ConstellioUser user,
			SimpleSearch simpleSearch, HttpServletRequest request) {
		String query = request.getParameter("q");
		List<String> facetQueries = toList(request, "fq");
		RecordCollection collection = getCollection(request);
		completeSearch(user, simpleSearch, query, facetQueries, collection);
	}

	public static List<String> toList(HttpServletRequest request, String param,
			boolean explodeIfOneValue) {
		String[] arr = request.getParameterValues(param);
		if (arr == null) {
			return new ArrayList<String>();
		} else {
			if (explodeIfOneValue && arr.length == 1) {
				return Arrays.asList(arr[0].split(","));
			}
			return Arrays.asList(arr);
		}
	}

	public static Integer getIntValue(HttpServletRequest request, String param,
			int defaultValue) {
		String attr = request.getParameter(param);
		return attr == null ? defaultValue : Integer.valueOf(attr);
	}

	public static Boolean getBooleanValue(HttpServletRequest request,
			String param, boolean defaultValue) {
		String attr = request.getParameter(param);
		return attr == null ? defaultValue : attr.equals("true");
	}

	public static List<String> toList(HttpServletRequest request, String param) {
		return toList(request, param, false);
	}

	public static void completeSearch(ConstellioUser user,
			SimpleSearch simpleSearch, String query, List<String> facetQueries,
			RecordCollection collection) {
		simpleSearch.setQuery(query);

		for (String fq : facetQueries) {
			int splitter = fq.indexOf(":");
			if (splitter == -1) {
				throw new RuntimeException("Invalid facet query : " + fq);
			}
			String facetName = fq.substring(0, splitter);
			String facetValue = fq.substring(splitter + 1);
			SearchedFacet searchedFacet = simpleSearch
					.getSearchedFacet(facetName);
			if (searchedFacet == null) {
				SearchableFacet searchableFacet = new SearchableFacet();
				searchableFacet.setName(facetName);
				// Field facets value are already in the corerct
				// format. Being a query, the values of this searchable facet
				// will not be converted.
				searchableFacet.setQuery(true);
				searchableFacet.setMultiValued(true);
				searchedFacet = new SearchedFacet(searchableFacet);
				simpleSearch.getSearchedFacets().add(searchedFacet);
			}
			searchedFacet.getIncludedValues().add(facetName + ":" + facetValue);
		}
	}
}
