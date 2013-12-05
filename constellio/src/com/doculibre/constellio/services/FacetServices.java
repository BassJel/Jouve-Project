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

import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;

import com.doculibre.constellio.entities.CollectionFacet;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;

public interface FacetServices extends BaseCRUDServices<CollectionFacet> {

    QueryResponse search(SimpleSearch simpleSearch, int start, int row, boolean excludePrefix,
        ConstellioUser user);

    QueryResponse search(SimpleSearch simpleSearch, int start, int row, boolean includeSingleValueFacets,
        boolean excludePrefix, List<String> customFieldFacets, List<String> customQueryFacets,
        ConstellioUser user);

    List<SearchableFacet> getFacets(String collectionName);

    List<String> suggestValues(CollectionFacet facet, String text);

    List<String> getValues(CollectionFacet facet);

    CollectionFacet get(String name, RecordCollection collection);
}
