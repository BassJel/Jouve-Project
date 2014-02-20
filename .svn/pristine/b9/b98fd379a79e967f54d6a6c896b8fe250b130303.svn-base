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
package com.doculibre.constellio.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.util.SimpleOrderedMap;

import com.doculibre.constellio.entities.CollectionFacet;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.CloudKeyword;
import com.doculibre.constellio.entities.search.FacetValue;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.ClusteringServices;
import com.doculibre.constellio.services.FacetServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.data.FacetsDataProvider;

public class SolrFacetUtils {

    public static List<SearchableFacet> getSearchableFacets(SimpleSearch simpleSearch) {
    	return getSearchableFacets(simpleSearch.getCollectionName());
    }

    public static List<SearchableFacet> getSearchableFacets(String collectionName) {
        List<SearchableFacet> searchableFacets;

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordCollection collection = collectionServices.get(collectionName);
        if (collection != null) {
            FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
            searchableFacets = facetServices.getFacets(collectionName);
        } else {
            searchableFacets = new ArrayList<SearchableFacet>();
        }
        return searchableFacets;
    }

    public static List<FacetValue> getPossibleValues(SearchableFacet searchableFacet,
        FacetsDataProvider dataProvider, String solrCoreName, ConstellioUser user) {
        List<FacetValue> possibleValues = new ArrayList<FacetValue>();
        QueryResponse queryResponse = dataProvider.getQueryResponse();
        if (queryResponse != null) {
            if (searchableFacet.isQuery()) {
                Map<String, Integer> facetQuery = queryResponse.getFacetQuery();
                Map<String, Map<Locale, String>> possibleValueLabels = searchableFacet
                    .getPossibleValuesLabels();
                for (String queryName : possibleValueLabels.keySet()) {
                    int queryCount = facetQuery.get(queryName);
                    if (queryCount > 0) {
                        possibleValues.add(new FacetValue(searchableFacet, queryName, queryCount));
                    }
                }
            } else if (searchableFacet.isCluster()) {
                SimpleSearch simpleSearch = dataProvider.getSimpleSearch();

                RecordCollectionServices collectionServices = ConstellioSpringUtils
                    .getRecordCollectionServices();
                String collectionName = simpleSearch.getCollectionName();
                RecordCollection collection = collectionServices.get(collectionName);
                CollectionFacet collectionFacet = collection.getCollectionFacet(new Long(searchableFacet
                    .getName()));

                ClusteringServices clusteringServices = ConstellioSpringUtils.getClusteringServices();
                List<SimpleOrderedMap<Object>> clusters = clusteringServices.cluster(simpleSearch,
                    collectionFacet, 0, ClusteringServices.maxResultsToConsider, user);
                List<FacetValue> facetValues = searchableFacet.getValues();
                for (SimpleOrderedMap<Object> cluster : clusters) {
                    FacetValue facetValue = getFacetValue(cluster, searchableFacet, collection.getLocales());
                    facetValues.add(facetValue);
                }
                // if (!facetValues.isEmpty()) {
                // searchableFacets.add(clusterFacet);
                // }
                possibleValues.addAll(searchableFacet.getValues());
            } else if (searchableFacet.isCloudKeyword()) {
                // No values
            } else if (queryResponse != null) {
                FacetField facetField = queryResponse.getFacetField(searchableFacet.getName());
                List<Count> facetCounts = facetField != null ? facetField.getValues() : null;
                if (facetCounts != null) {
                    RecordCollectionServices collectionServices = ConstellioSpringUtils
                        .getRecordCollectionServices();
                    FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
                    SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
                    String collectionName = simpleSearch.getCollectionName();
                    RecordCollection collection = collectionServices.get(collectionName);
                    CollectionFacet collectionFacet = collection.getFieldFacet(searchableFacet.getName());

                    // Special cases
                    Set<String> ignoredValues = new HashSet<String>();
                    if (IndexField.COLLECTION_ID_FIELD.equals(searchableFacet.getName())) {
                        ignoredValues.add("" + collection.getId());
                    }
                    Set<String> notEmptyFacetValues = new HashSet<String>();
                    for (Count facetCount : facetCounts) {
                        String facetCountName = facetCount.getName();
                        if (!ignoredValues.contains(facetCountName) && StringUtils.isNotEmpty(facetCountName)) {
                            possibleValues.add(new FacetValue(searchableFacet, facetCountName,
                                (int) facetCount.getCount()));
                            notEmptyFacetValues.add(facetCountName);
                        }
                    }
                    if (!collectionFacet.isHideEmptyValues()) {
                        List<String> allFacetValues = facetServices.getValues(collectionFacet);
                        for (String facetValue : allFacetValues) {
                            if (!ignoredValues.contains(facetValue)
                                && !notEmptyFacetValues.contains(facetValue)) {
                                possibleValues.add(new FacetValue(searchableFacet, facetValue, 0));
                            }
                        }
                    }
                }
            }
        }
        return possibleValues;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static FacetValue getFacetValue(SimpleOrderedMap cluster, SearchableFacet clusterFacet,
        Collection<Locale> locales) {
        List<String> docIds = (List<String>) cluster.get("docs");
        List<String> labels = (List<String>) cluster.get("labels");
        StringBuffer concatenatedDocIds = new StringBuffer();
        for (String docId : docIds) {
            concatenatedDocIds.append(QueryParser.escape(docId));
            concatenatedDocIds.append(FacetValue.CONCAT_DELIM);
        }
        StringBuffer concatenatedLabels = new StringBuffer();
        for (String label : labels) {
            concatenatedLabels.append(label);// QueryParser.escape(docId));
            concatenatedLabels.append(FacetValue.CONCAT_DELIM);
        }
        FacetValue facetValue = new FacetValue();
        facetValue.setSearchableFacet(clusterFacet);

        List<SimpleOrderedMap<Object>> subClusters = (List<SimpleOrderedMap<Object>>) cluster.get("clusters");
        if (subClusters != null && !subClusters.isEmpty()) {
            int count = 0;
            StringBuffer value = new StringBuffer();
            StringBuffer valueToClusterLabel = new StringBuffer();
            for (SimpleOrderedMap<Object> subCluster : subClusters) {
                // Recursive call
                FacetValue subClusterValue = getFacetValue(subCluster, clusterFacet, locales);
                facetValue.getSubValues().add(subClusterValue);
                count += subClusterValue.getDocCount();
                value.append(subClusterValue.getValue());
                valueToClusterLabel.append(subClusterValue.getValueToClusterLabel());
            }
            facetValue.setDocCount(count);
            facetValue.setValue(value.toString());
            facetValue.setValueToClusterLabel(valueToClusterLabel.toString());
        } else {
            facetValue.setDocCount(docIds.size());
            facetValue.setValue(concatenatedDocIds.toString());
            facetValue.setValueToClusterLabel(concatenatedLabels.toString());
        }
        for (Locale locale : locales) {
            clusterFacet.addPossibleValueLabel(facetValue.getValue(), concatenatedLabels.toString(), locale);// labels.get(0)
        }
        return facetValue;
    }

    public static List<CloudKeyword> getCloudKeywords(SearchableFacet searchableFacet,
        FacetsDataProvider dataProvider) {
        List<CloudKeyword> cloudKeywords = new ArrayList<CloudKeyword>();
        QueryResponse queryResponse = dataProvider.getQueryResponse();
        if (queryResponse != null) {
            FacetField facetField = queryResponse.getFacetField(searchableFacet.getName());
            List<Count> facetCounts = facetField.getValues();
            if (facetCounts != null) {
                for (Count facetCount : facetCounts) {
                    cloudKeywords.add(new CloudKeyword(facetCount.getName(), (int) facetCount.getCount()));
                }
            }
        }
        return cloudKeywords;
    }

}
