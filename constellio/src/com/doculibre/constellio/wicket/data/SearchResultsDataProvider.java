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
package com.doculibre.constellio.wicket.data;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.SearchServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class SearchResultsDataProvider implements ISimpleSearchDataProvider {

	private SimpleSearch simpleSearch;
	//FIXME: question est ce que le fait que plusieurs utilisateurs de Constellio lance des requetes en meme temps ne pose pas de pb?
	private int indexBeforeDetach;
	
	private int resultsPerPage;
	
	private transient QueryResponse queryResponse;

	public SearchResultsDataProvider(SimpleSearch simpleSearch, int resultsPerPage) {
		this.simpleSearch = simpleSearch;
		this.resultsPerPage = resultsPerPage;
	}

	@SuppressWarnings("rawtypes")
	public Iterator iterator(int first, int count) {
		Iterator iterator;
		if (queryResponse == null || indexBeforeDetach != first) {
			indexBeforeDetach = first;
			loadQueryResponse(first, count);
		}
		if (queryResponse != null) {
            iterator = queryResponse.getResults().iterator();
		} else {
		    iterator = new ArrayList().iterator();
		}
		return iterator;
	}

	public IModel model(Object object) {
		return new CompoundPropertyModel((SolrDocument) object);
	}

	public int size() {
		int size;
		if (queryResponse == null) {
			loadQueryResponse(indexBeforeDetach, resultsPerPage);
		}
		if (queryResponse != null) {
            size = (int) queryResponse.getResults().getNumFound();
		} else {
		    size = 0;
		}
		return size;
	}

	public void detach() {
		queryResponse = null;
	}

	public SimpleSearch getSimpleSearch() {
		return simpleSearch;
	}

	public int getStart() {
		return indexBeforeDetach;
	}

	public int getResultsPerPage() {
		return resultsPerPage;
	}

	public QueryResponse getQueryResponse() {
		if (simpleSearch.isQueryValid() &&queryResponse == null 
				&& ((simpleSearch.getQuery() != null)
						|| !simpleSearch.getSearchedFacets().isEmpty() || simpleSearch
						.getCloudKeyword() != null || simpleSearch.getAdvancedSearchRule() != null)) {
			loadQueryResponse(indexBeforeDetach, resultsPerPage);
		}
		return queryResponse;
	}
	
	protected void loadQueryResponse(int first, int count) {
		SearchServices searchServices = ConstellioSpringUtils.getSearchServices();
		ConstellioUser user = ConstellioSession.get().getUser();
		queryResponse = searchServices.search(simpleSearch, first, count, user);
	}

}
