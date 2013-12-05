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

import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.FacetServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class FacetsDataProvider implements ISimpleSearchDataProvider {

    private SimpleSearch simpleSearch;
    private int indexBeforeDetach;
    private int resultsPerPage;
    private boolean notIncludedOnly;

    // Null value means database facets will be added to the query
    private List<String> customFieldFacets;
    private List<String> customQueryFacets;

    private transient QueryResponse queryResponse;

    private boolean filterFacetValues = true;

    public FacetsDataProvider(SimpleSearch simpleSearch, int resultsPerPage, boolean filterFacetValues, boolean notIncludedOnly) {
        this.simpleSearch = simpleSearch;
        this.resultsPerPage = resultsPerPage;
        this.filterFacetValues = filterFacetValues;
        this.notIncludedOnly = notIncludedOnly;
    }

    public FacetsDataProvider(SimpleSearch simpleSearch, int resultsPerPage, List<String> customFieldFacets,
        List<String> customQueryFacets, boolean filterFacetValues) {
        this(simpleSearch, resultsPerPage, filterFacetValues, false);
        this.customFieldFacets = customFieldFacets;
        this.customQueryFacets = customQueryFacets;
    }

    @SuppressWarnings("rawtypes")
    public Iterator iterator(int first, int count) {
        if (queryResponse == null || indexBeforeDetach != first) {
            indexBeforeDetach = first;
            queryResponse = loadQueryResponse(simpleSearch, first, count, customFieldFacets,
                customQueryFacets, filterFacetValues, notIncludedOnly);
        }
        return queryResponse.getResults().iterator();
    }

    public IModel model(Object object) {
        return new CompoundPropertyModel((SolrDocument) object);
    }

    public int size() {
        if (queryResponse == null) {
            queryResponse = loadQueryResponse(simpleSearch, indexBeforeDetach, resultsPerPage,
                customFieldFacets, customQueryFacets, filterFacetValues, notIncludedOnly);
        }
        return (int) queryResponse.getResults().getNumFound();
    }

    private static QueryResponse loadQueryResponse(SimpleSearch simpleSeach, int start, int row,
        List<String> customFieldFacets, List<String> customQueryFacets, boolean filterFacetValues, boolean notIncludedOnly) {
        FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
        ConstellioUser user = ConstellioSession.get().getUser();
        return facetServices.search(simpleSeach, start, row, filterFacetValues, notIncludedOnly, customFieldFacets,
            customQueryFacets, user);
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
		if (simpleSearch.isQueryValid() && queryResponse == null
				&& (!simpleSearch.getSearchedFacets().isEmpty() 
						|| simpleSearch.getCloudKeyword() != null || (simpleSearch.getQuery() != null || simpleSearch.getAdvancedSearchRule() != null))) {
            queryResponse = loadQueryResponse(simpleSearch, indexBeforeDetach, resultsPerPage,
                customFieldFacets, customQueryFacets, filterFacetValues, notIncludedOnly);
        }
        return queryResponse;
    }

}
