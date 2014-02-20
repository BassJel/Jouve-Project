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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.doculibre.constellio.entities.CollectionFacet;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.I18NLabel;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SearchedFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.solr.context.SolrLogContext;
import com.doculibre.constellio.solr.handler.ConstellioSolrQueryParams;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;

public class FacetServicesImpl extends BaseCRUDServicesImpl<CollectionFacet> implements FacetServices {

    public FacetServicesImpl(EntityManager entityManager) {
        super(CollectionFacet.class, entityManager);
    }

    public QueryResponse search(SimpleSearch simpleSearch, int start, int row, boolean notIncludedOnly, ConstellioUser user) {
        return search(simpleSearch, start, row, true, notIncludedOnly, null, null, user);
    }

    @Override
    public QueryResponse search(SimpleSearch simpleSearch, int start, int row,
        boolean includeSingleValueFacets, boolean notIncludedOnly, List<String> customFieldFacets, List<String> customQueryFacets,
        ConstellioUser user) {

    	String solrServerName = simpleSearch.getCollectionName();
    	SolrQuery query = toSolrQuery(simpleSearch, start, row, includeSingleValueFacets, notIncludedOnly, customFieldFacets, customQueryFacets, user);

        SolrServer server = SolrCoreContext.getSolrServer(solrServerName);
        QueryResponse queryResponse;
        if (server != null) {
            try {
                queryResponse = server.query(query);
            } catch (SolrServerException e) {
                queryResponse = null;
            }
        } else {
            queryResponse = null;
        }
        return queryResponse;
    }
    
    public static SolrQuery toSolrQuery(SimpleSearch simpleSearch, int start, int row,
            boolean includeSingleValueFacets, boolean notIncludedOnly, List<String> customFieldFacets, List<String> customQueryFacets,
            ConstellioUser user)  {
    	   String solrServerName = simpleSearch.getCollectionName();
           RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
           RecordCollection collection = collectionServices.get(solrServerName);
           SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
           Boolean usesDisMax = solrServices.usesDisMax(collection);
    	SolrQuery query;
        if (!collection.isOpenSearch()) {
            query = SearchServicesImpl.toSolrQuery(simpleSearch, usesDisMax, true, includeSingleValueFacets, notIncludedOnly);
        } else {
            query = SearchServicesImpl.toSolrQuery(simpleSearch, usesDisMax, false, true, false);
        }

        query.setParam(ConstellioSolrQueryParams.COLLECTION_NAME, simpleSearch.getCollectionName());
        query.setParam(ConstellioSolrQueryParams.LUCENE_QUERY, simpleSearch.getLuceneQuery(
            includeSingleValueFacets, true));
        query.setParam(ConstellioSolrQueryParams.SIMPLE_SEARCH, simpleSearch.toSimpleParams().toString());
        if (user != null) {
            query.setParam(ConstellioSolrQueryParams.USER_ID, "" + user.getId());
        }

        if (StringUtils.isEmpty(query.getQuery())) {
            query.setQuery(SimpleSearch.SEARCH_ALL);
            query.setRequestHandler("/elevate");
        }

        query.set("shards.qt", "/elevate");
		query.setRequestHandler("/elevate");
        
        query.setRows(row);
        query.setStart(start);
        query.setHighlight(true);
        query.setHighlightFragsize(100);
        query.setHighlightSnippets(2);

        query.setFacet(true);
        query.setFacetLimit(400);
        query.setFacetMinCount(1);

        if (collection.isOpenSearch()) {
            query.setParam("openSearchURL", collection.getOpenSearchURL());
            Locale locale = simpleSearch.getSingleSearchLocale();
            if (locale != null) {
                query.setParam("lang", locale.getLanguage());
            }
        } else {
            for (CollectionFacet collectionFacet : collection.getCollectionFacets()) {
                if (customFieldFacets == null && collectionFacet.isFieldFacet()) {
                    IndexField indexField = collectionFacet.getFacetField();
                    String indexFieldName = indexField.getName();
                    if (!notIncludedOnly) {
                        query.addFacetField(indexFieldName);
                    } else {
                        SearchedFacet searchedFacet = simpleSearch.getSearchedFacet(indexFieldName);
                        if (searchedFacet != null) {
                            if (!searchedFacet.getIncludedValues().isEmpty()) {
                                StringBuffer sbEx = new StringBuffer();
                                sbEx.append("{!ex=dt}");
//                                sbEx.append("{!ex=");
//                                boolean first = true;
//                                for (String includedValue : searchedFacet.getIncludedValues()) {
//                                    if (first) {
//                                        first = false;
//                                    } else {
//                                        sbEx.append(",");
//                                    }
//                                    sbEx.append(includedValue); 
//                                }
//                                sbEx.append("}");
//                                query.setParam("facet.field", sbEx.toString() + indexFieldName);
                                query.addFacetField(sbEx.toString() + indexFieldName);
                            } else {
                                query.addFacetField(indexFieldName);
                            }
                        }
                    }
                } else if (customQueryFacets == null && collectionFacet.isQueryFacet()) {
                    // Modification Rida, remplacement de collectionFacet.getLabels() par
                    // collectionFacet.getLabelledValues()
                    // for (I18NLabel valueLabels : collectionFacet.getLabels()) {
                    for (I18NLabel valueLabels : collectionFacet.getLabelledValues()) {
                        String facetQuery = valueLabels.getKey();
                        query.addFacetQuery(facetQuery);
                    }
                }
            }
            if (customFieldFacets != null) {
                for (String facetField : customFieldFacets) {
                    if (!notIncludedOnly) {
                        query.addFacetField(facetField);
                    } else {
                        StringBuffer sbEx = new StringBuffer();
                        sbEx.append("{!ex=dt}");
//                        sbEx.append("{!ex=");
//                        boolean first = true;
//                        for (String includedValue : searchedFacet.getIncludedValues()) {
//                            if (first) {
//                                first = false;
//                            } else {
//                                sbEx.append(",");
//                            }
//                            sbEx.append(includedValue); 
//                        }
//                        sbEx.append("}");
                        query.setParam("facet.field", sbEx.toString() + facetField);
                    }
                }
            }

            if (customQueryFacets != null) {
                for (String facetQuery : customQueryFacets) {
                    query.addFacetQuery(facetQuery);
                }
            }
        }
        
        return query;
    }

    @Override
    public List<SearchableFacet> getFacets(String collectionName) {
        List<SearchableFacet> searchableFacets = new ArrayList<SearchableFacet>();

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
        RecordCollection collection = collectionServices.get(collectionName);

        int i = 0;
        for (CollectionFacet facet : collection.getCollectionFacets()) {
            Map<Locale, String> facetLabels = new HashMap<Locale, String>();
            for (Locale collectionLocale : collection.getLocales()) {
                facetLabels.put(collectionLocale, facet.getName(collectionLocale));
            }
            if (facet.isClusterFacet()) {
                SearchableFacet clusterFacet = new SearchableFacet("" + facet.getId(), facetLabels, facet
                    .isSortable(), facet.isMultiValued());
                clusterFacet.setCluster(true);
                searchableFacets.add(clusterFacet);
            } else if (facet.isCloudKeywordFacet()) {
                IndexField indexField = facet.getFacetField();
                SearchableFacet cloudKeywordFacet = new SearchableFacet(indexField.getName(), facetLabels,
                    facet.isSortable(), facet.isMultiValued());
                cloudKeywordFacet.setCloudKeyword(true);
                searchableFacets.add(cloudKeywordFacet);
            } else if (facet.isQueryFacet()) {
                if (!facet.getLabelledValues().isEmpty()) {
                    SearchableFacet queryFacet = new SearchableFacet("facetQuery" + i, facetLabels, facet
                        .isSortable(), facet.isMultiValued());
                    queryFacet.setQuery(true);
                    for (I18NLabel labelledValue : facet.getLabelledValues()) {
                        for (Locale labelledValueLocale : labelledValue.getValues().keySet()) {
                            queryFacet.addPossibleValueLabel(labelledValue.getKey(), labelledValue
                                .getValue(labelledValueLocale), labelledValueLocale);
                        }
                    }
                    searchableFacets.add(queryFacet);
                }
            } else {
                IndexField indexField = facet.getFacetField();
                SearchableFacet fieldFacet = new SearchableFacet(indexField.getName(), facetLabels, facet
                    .isSortable(), facet.isMultiValued());
                for (Locale locale : ConstellioSpringUtils.getSupportedLocales()) {
                    Map<String, String> defaultLabels = indexFieldServices.getDefaultLabelledValues(indexField, locale);
                    for (I18NLabel labelledValue : facet.getLabelledValues()) {
                        String facetValueLabel = labelledValue.getValue(locale);
                        fieldFacet.addPossibleValueLabel(labelledValue.getKey(), facetValueLabel, locale);
                        defaultLabels.remove(facetValueLabel);
                    }
                    for (String defaultFacetValue : defaultLabels.keySet()) {
                        String defaultLabel = defaultLabels.get(defaultFacetValue);
                        fieldFacet.addPossibleValueLabel(defaultFacetValue, defaultLabel, locale);
                    }
                }
                searchableFacets.add(fieldFacet);
            }
            i++;
        }
        return searchableFacets;
    }

    @Override
    public List<String> getValues(CollectionFacet facet) {
        return suggestValues(facet, null);
    }

    @Override
    public List<String> suggestValues(CollectionFacet facet, String text) {
        // Si dismax la desactiver
        SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
        RecordCollection collection = facet.getRecordCollection();
        boolean usesDisMax = solrServices.usesDisMax(collection);
        if (usesDisMax) {
            solrServices.resetDefaultDistance(collection);
        }

        List<String> values = new ArrayList<String>();
        if (facet.isFieldFacet()) {
            IndexField indexField = facet.getFacetField();
            IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
            values = indexFieldServices.suggestValues(indexField, text);
        }

        // reactiver dismax si elle a ete desactive
        if (usesDisMax) {
            solrServices.updateDismax(collection);
        }

        return values;
    }

    @Override
    public CollectionFacet makePersistent(CollectionFacet entity) {
        CollectionFacet result = super.makePersistent(entity);
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordCollection collection = entity.getRecordCollection();
        collectionServices.makePersistent(collection, false);
        return result;
    }

    @Override
    public CollectionFacet makeTransient(CollectionFacet entity) {
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordCollection collection = entity.getRecordCollection();
        collection.getCollectionFacets().remove(entity);
        collectionServices.makePersistent(collection, false);
        return entity;
    }

    public static void main(String[] args) {
        try {
            File solrCoresDir = new File(
                "C:\\dev\\workspace_searchengine\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\constellio\\WEB-INF\\constellio\\collections");
            SolrCoreContext.init();
            SolrLogContext.init();

            RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
            FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
            RecordCollection webCollection = collectionServices.get("web");
            System.out.println(webCollection.getName() + "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
            for (IndexField indexField : webCollection.getIndexFields()) {
                CollectionFacet facet = new CollectionFacet();
                facet.setFacetType(CollectionFacet.FIELD_FACET);
                facet.setRecordCollection(webCollection);
                facet.setFacetField(indexField);
                System.out.println("Field : " + indexField.getName());
                System.out.println("  Toutes les valeurs : ");
                List<String> values = facetServices.getValues(facet);
                for (String value : values) {
                    System.out.println("    - " + value);
                }

                System.out.println("  Suggestions : ");
                List<String> suggestions = facetServices.suggestValues(facet, "bonjour");
                for (String value : suggestions) {
                    System.out.println("    - " + value);
                }
            }
            EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
            entityManager.close();

            SolrCoreContext.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    @Override
    public CollectionFacet get(String name, RecordCollection collection) {
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("name", name);
        criterias.put("recordCollection", collection);
        return get(criterias);
    }
}
