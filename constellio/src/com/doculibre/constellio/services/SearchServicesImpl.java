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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.CloudKeyword;
import com.doculibre.constellio.entities.search.FacetValue;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SearchedFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.search.SynonymUtils;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.solr.handler.ConstellioSolrQueryParams;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class SearchServicesImpl implements SearchServices {

    private static final Logger LOGGER = Logger.getLogger(SearchServicesImpl.class.getName());

    @Override
    public QueryResponse search(SimpleSearch simpleSearch, int start, int rows, ConstellioUser user) {
        return search(simpleSearch, start, rows, new SearchParams(), user);
    }

    @Override
    public QueryResponse search(SimpleSearch simpleSearch, int start, int rows, SearchParams searchParams,
        ConstellioUser user) {
        QueryResponse queryResponse;

        String collectionName = simpleSearch.getCollectionName();
        if (collectionName != null) {
            RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
            RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
            RecordCollection collection = collectionServices.get(collectionName);

            SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
            Boolean usesDisMax = solrServices.usesDisMax(collection);

            SolrQuery query;
            if (!collection.isOpenSearch()) {
                query = toSolrQuery(simpleSearch, usesDisMax, true, true);
            } else {
                query = toSolrQuery(simpleSearch, usesDisMax, false, true);
            }
            // displayQuery(query);

            String luceneQuery = simpleSearch.getLuceneQuery();
            query.setParam(ConstellioSolrQueryParams.LUCENE_QUERY, luceneQuery);
            query.setParam(ConstellioSolrQueryParams.SIMPLE_SEARCH, simpleSearch.toSimpleParams().toString());
            query.setParam(ConstellioSolrQueryParams.COLLECTION_NAME, collectionName);
            if (user != null) {
                query.setParam(ConstellioSolrQueryParams.USER_ID, "" + user.getId());
            }

            String queryString = query.getQuery();
            if (StringUtils.isEmpty(queryString)) {
                queryString = SimpleSearch.SEARCH_ALL;
            }

            List<Record> pendingExclusions = recordServices.getPendingExclusions(collection);
            while (!pendingExclusions.isEmpty()) {
                IndexingManager indexingManager = IndexingManager.get(collection);
                if (indexingManager.isActive()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    pendingExclusions = recordServices.getPendingExclusions(collection);
                } else {
                    return null;
                }
            }

            // SolrQuery query = new SolrQuery();
            query.set("collectionName", simpleSearch.getCollectionName());
            // query.setQuery(luceneQuery);

            // nb résultats par page
            query.setRows(rows);

            // page de début
            query.setStart(start);
            query.setHighlight(searchParams.isHighlightingEnabled());
            if (searchParams.isHighlightingEnabled()) {
                query.setHighlightFragsize(searchParams.getFragsize());
                query.setHighlightSnippets(searchParams.getSnippets());
            }

            if (simpleSearch.getSortField() != null) {
                ORDER order = SimpleSearch.SORT_DESCENDING.equals(simpleSearch.getSortOrder()) ? ORDER.desc
                    : ORDER.asc;
                IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
                IndexField indexField = indexFieldServices.get(simpleSearch.getSortField(), collection);
                if (indexField != null) {
                    IndexField sortIndexField = indexFieldServices.getSortFieldOf(indexField);
                    if (sortIndexField != null) {
                        query.setSortField(sortIndexField.getName(), order);
                    }
                }
            }

            if (collection.isOpenSearch()) {
                query.setParam("openSearchURL", collection.getOpenSearchURL());
                Locale locale = simpleSearch.getSingleSearchLocale();
                if (locale != null) {
                    query.setParam("lang", locale.getLanguage());
                }
            }

            if (searchParams.getHighlightedFields() == null) {
                IndexField defaultSearchField = collection.getDefaultSearchIndexField();
                query.addHighlightField(defaultSearchField.getName());
                for (CopyField copyFieldDest : defaultSearchField.getCopyFieldsDest()) {
                    IndexField copyIndexFieldSource = copyFieldDest.getIndexFieldSource();
                    if (copyIndexFieldSource != null && !copyIndexFieldSource.isTitleField()
                        && copyIndexFieldSource.isHighlighted()) {
                        query.addHighlightField(copyIndexFieldSource.getName());
                    }
                }
                IndexField titleField = collection.getTitleIndexField();
                if (titleField != null && titleField.isHighlighted()) {
                    query.addHighlightField(titleField.getName());
                }
            } else {
                for (String highlightedField : searchParams.getHighlightedFields()) {
                    IndexField field = collection.getIndexField(highlightedField);
                    if (field != null) {
                        query.addHighlightField(highlightedField);
                    }
                }
            }
            SolrServer server = SolrCoreContext.getSolrServer(collectionName);
            if (server != null) {
                try {
                    // displayQuery(query);
                    queryResponse = server.query(query);
                } catch (SolrServerException e) {
                    queryResponse = null;
                }
            } else {
                queryResponse = null;
            }

//            if (queryResponse != null && !collection.isOpenSearch()) {
//                StatsCompiler statCompiler = StatsCompiler.getInstance();
//                try {
//                    statCompiler.saveStats(collectionName, SolrLogContext.getStatsSolrServer(),
//                        SolrLogContext.getStatsCompileSolrServer(), queryResponse, luceneQuery);
//                } catch (SolrServerException e) {
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }

        } else {
            queryResponse = null;
        }

        // improveQueryResponse(collectionName, queryResponse);
        // System.out.println("Response size" + queryResponse.getResults().getNumFound());
        return queryResponse;
    }

    // private void displayQuery(SolrQuery query) {
    // System.out.println("text : " + query.getQuery());
    // System.out.println("operateur : " + query.getParams("q.op"));
    //		
    // String[] filters = query.getFilterQueries();
    // if (filters != null){
    // System.out.println("filtres");
    // for (String filtre : filters){
    // System.out.println("\t" + filtre);
    // }
    // }
    //		
    // }

    public static SolrQuery toSolrQuery(SimpleSearch simpleSearch, boolean useDismax,
        boolean withMultiValuedFacets, boolean withSingleValuedFacets) {
        return toSolrQuery(simpleSearch, useDismax, withMultiValuedFacets, withSingleValuedFacets, false);
    }

    public static SolrQuery toSolrQuery(SimpleSearch simpleSearch, boolean useDismax,
        boolean withMultiValuedFacets, boolean withSingleValuedFacets, boolean notIncludedOnly) {
        SolrQuery query = new SolrQuery();

        boolean addSynonyms = !SolrServices.synonymsFilterActivated;
        if (addSynonyms) {
            addQueryTextAndOperatorWithSynonyms(simpleSearch, query, useDismax);
        } else {
            addQueryTextAndOperatorWithoutSynonyms(simpleSearch, query, useDismax);
        }

        // FIXME confirmer avec Vincent:
        // 1. que les tags sont vraiment a ajouter par defaut (meme pour openSearch)
        // 2. separer les tags par des AND et non des OU
        addTagsTo(simpleSearch, query);

        boolean addFacets = withMultiValuedFacets || withSingleValuedFacets;
        if (addFacets) {
            addFacetsTo(simpleSearch, query, withMultiValuedFacets, withSingleValuedFacets, notIncludedOnly);
        }

        return query;
    }

    private static void addQueryTextAndOperatorWithSynonyms(SimpleSearch simpleSearch, SolrQuery query,
        boolean useDismax) {
        String collectionName = simpleSearch.getCollectionName();
        if (StringUtils.isNotEmpty(collectionName)) {
            if (simpleSearch.getAdvancedSearchRule() == null) {
                String textQuery = getTextQuery(simpleSearch);
                if (StringUtils.isNotEmpty(textQuery)) {
                    String searchType = simpleSearch.getSearchType();
                    StringBuffer sb = new StringBuffer();
                    // sb.append("(");
                    if (SimpleSearch.SEARCH_ALL.equals(textQuery)) {
                        sb.append(textQuery);
                        if (useDismax) {
                            // Non valide avec disMax => disMax doit etre desactivee
                            query.setQueryType(SolrServices.DEFAULT_DISTANCE_NAME);
                            LOGGER
                                .warning("Dismax is replaced by the default distance since the former does not allow wildcard");
                        }
                    } else if (SimpleSearch.EXACT_EXPRESSION.equals(searchType)) {
                        // FIXME a corriger : si "n" terms avec chacun "m" synonyms => traiter les combinaison
                        // de
                        // synonymes
                        // Sinon solution simple: synonymes de l'expression (solution prise pour l'instant)
                        String textAndSynonyms = SynonymUtils.addSynonyms(textQuery, collectionName, true);
                        sb.append(textAndSynonyms);// SynonymUtils.addSynonyms(textQuery,
                        // collectionName)
                    } else {
                        // TOUS_LES_MOTS OU AU_MOINS_UN_MOT
                        String operator;
                        if (SimpleSearch.AT_LEAST_ONE_WORD.equals(searchType)) {
                            operator = "OR";
                        } else {
                            operator = "AND";
                        }
                        String[] terms = textQuery.split(" ");
                        for (int i = 0; i < terms.length; i++) {
                            String term = terms[i];
                            String termAndSynonyms = SynonymUtils.addSynonyms(term, collectionName, false);
                            if (term.equals(termAndSynonyms)) {
                                sb.append(term);
                            } else {
                                sb.append("(" + termAndSynonyms + ")");
                            }
                            if (i < terms.length - 1) {
                                // sb.append(operator);
                                sb.append(" " + operator + " ");
                            }
                        }
                    }
                    // sb.append(")");
                    query.setQuery(sb.toString());
                }
            } else {
                query.setQuery(simpleSearch.getLuceneQuery());
            }
        }
    }

    private static String getTextQuery(SimpleSearch simpleSearch) {
        String textQuery = simpleSearch.getEscapedQuery();
        if (textQuery == null) {
            textQuery = "";
        }
        return textQuery;
    }

    private static void addQueryTextAndOperatorWithoutSynonyms(SimpleSearch simpleSearch, SolrQuery query,
        boolean useDismax) {

        String collectionName = simpleSearch.getCollectionName();
        if (StringUtils.isNotEmpty(collectionName)) {
            if (simpleSearch.getAdvancedSearchRule() == null) {
                String textQuery = getTextQuery(simpleSearch);
                if (StringUtils.isNotEmpty(textQuery)) {
                    String searchType = simpleSearch.getSearchType();
                    if (SimpleSearch.SEARCH_ALL.equals(textQuery)) {
                        query.setQuery(textQuery);
                        // FIXME : AND ou Operateur par defaut?
                        query.setParam("q.op", "AND");
                        if (useDismax) {
                            // Non valide avec disMax => disMax doit etre desactivee
                            query.setQueryType(SolrServices.DEFAULT_DISTANCE_NAME);
                            LOGGER
                                .warning("Dismax is replaced by the default distance since the former does not allow wildcard");
                        }
                    } else if (SimpleSearch.AT_LEAST_ONE_WORD.equals(searchType)) {
                        query.setQuery(textQuery);
                        // Operateur OR
                        query.setParam("q.op", "OR");
                        if (useDismax) {
                            query.setParam("mm", "0");
                        }
                    } else if (SimpleSearch.EXACT_EXPRESSION.equals(searchType)) {
                        query.setQuery("\"" + textQuery + "\"");
                        if (useDismax) {
                            // FIXME il faut faire quoi avec dismax?
                        }
                    } else {
                        if (SimpleSearch.ALL_WORDS.equals(searchType)) {
                            query.setQuery(textQuery);
                            // Operateur AND
                            query.setParam("q.op", "AND");
                            if (useDismax) {
                                query.setParam("mm", "100");
                            }
                        } else {
                            throw new RuntimeException("Invalid searchType " + searchType);
                        }
                    }
                }
            } else {
                query.setQuery(simpleSearch.getLuceneQuery());
            }
        }
    }

    private static void addTagsTo(SimpleSearch simpleSearch, SolrQuery query) {
        StringBuffer sb = new StringBuffer();
        Set<String> tags = simpleSearch.getTags();
        if (!tags.isEmpty()) {
            sb.append("(");
            for (Iterator<String> it = tags.iterator(); it.hasNext();) {
                String tag = it.next();
                sb.append("(");
                sb.append(IndexField.FREE_TEXT_TAGGING_FIELD + ":" + tag);
                sb.append(" OR ");
                sb.append(IndexField.THESAURUS_TAGGING_FIELD + ":" + tag);
                sb.append(")");
                if (it.hasNext()) {
                    sb.append(" AND ");
                }
            }
            sb.append(")");
        }
        query.addFilterQuery(sb.toString());
    }

    private static void addFacetsTo(SimpleSearch simpleSearch, SolrQuery query,
        boolean withMultiValuedFacets, boolean withSingleValuedFacets, boolean notIncludedOnly) {
        List<SearchedFacet> searchedFacets = simpleSearch.getSearchedFacets();
        for (SearchedFacet searchedFacet : searchedFacets) {
            SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
            if ((searchableFacet.isMultiValued() && withMultiValuedFacets)
                || (!searchableFacet.isMultiValued() && withSingleValuedFacets)) {
                if (!searchableFacet.isCluster()) {
                    if (searchableFacet.isQuery()) {
                        if (!searchedFacet.getIncludedValues().isEmpty()) {
                            StringBuffer sb = new StringBuffer("");
                            if (notIncludedOnly) {
                                sb.append("{!tag=dt}");
                                // sb.append("{!tag=");
                                // boolean first = true;
                                // for (String includedValue : searchedFacet.getIncludedValues()) {
                                // if (first) {
                                // first = false;
                                // } else {
                                // sb.append(",");
                                // }
                                // sb.append(includedValue);
                                // }
                                // sb.append("}");
                            }
                            sb.append("(");
                            boolean first = true;
                            for (String includedValue : searchedFacet.getIncludedValues()) {
                                if (first) {
                                    first = false;
                                } else {
                                    sb.append(" OR ");
                                }
                                sb.append(includedValue);
                            }
                            sb.append(")");
                            query.addFilterQuery(sb.toString());
                        }
                    } else {
                        String facetName = searchableFacet.getName();
                        if (!searchedFacet.getIncludedValues().isEmpty()) {
                            StringBuffer sb = new StringBuffer();
                            if (notIncludedOnly) {
                                sb.append("{!tag=dt}");
                                // StringBuffer sbTag = new StringBuffer();
                                // sbTag.append("{!tag=");
                                // boolean first = true;
                                // for (String includedValue : searchedFacet.getIncludedValues()) {
                                // if (first) {
                                // first = false;
                                // } else {
                                // sbTag.append(",");
                                // }
                                // sbTag.append(includedValue);
                                // }
                                // sbTag.append("}");
                                // sb.append(sbTag);
                            }
                            sb.append(facetName + ":(");
                            boolean first = true;
                            for (String includedValue : searchedFacet.getIncludedValues()) {
                                if (first) {
                                    first = false;
                                } else {
                                    sb.append(" OR ");
                                }
                                sb.append("\"");
                                sb.append(SimpleSearch.correctFacetValue(includedValue));
                                sb.append("\"");
                            }
                            sb.append(")");
                            query.addFilterQuery(sb.toString());
                        }
                    }
                }
            }
        }

        // valeurs exclues
        for (SearchedFacet searchedFacet : searchedFacets) {
            SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
            if (!searchableFacet.isCluster() && !searchedFacet.getExcludedValues().isEmpty()) {
                StringBuffer sb = new StringBuffer();
                String facetName = searchableFacet.getName();
                for (String excludedValue : searchedFacet.getExcludedValues()) {
                    sb.append("NOT ");
                    if (searchableFacet.isQuery()) {
                        sb.append(SimpleSearch.correctFacetValue(excludedValue));
                    } else {
                        sb.append(facetName);
                        sb.append(":\"");
                        sb.append(SimpleSearch.correctFacetValue(excludedValue));
                        sb.append("\"");
                    }
                }
                String sbToString = sb.toString();
                if (!sbToString.isEmpty()) {
                    query.addFilterQuery(sb.toString());
                }
            }
        }

        SearchedFacet cluster = simpleSearch.getCluster();
        if (cluster != null) {
            RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
            RecordCollection collection = collectionServices.get(simpleSearch.getCollectionName());
            IndexField uniqueKeyIndexField = collection.getUniqueKeyIndexField();

            if (!cluster.getIncludedValues().isEmpty()) {
                StringBuilder sb = new StringBuilder(uniqueKeyIndexField.getName() + ":(");
                for (String includedValue : cluster.getIncludedValues()) {
                    boolean first = true;
                    StringTokenizer st = new StringTokenizer(includedValue, FacetValue.CONCAT_DELIM);
                    while (st.hasMoreTokens()) {
                        String docId = st.nextToken();
                        if (first) {
                            first = false;
                        } else {
                            sb.append(" OR ");
                        }
                        sb.append("\"");
                        sb.append(docId);
                        sb.append("\"");
                    }
                }
                sb.append(")");
                query.addFilterQuery(sb.toString());
            }
            if (!cluster.getExcludedValues().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String excludedValue : cluster.getExcludedValues()) {
                    StringTokenizer st = new StringTokenizer(excludedValue, FacetValue.CONCAT_DELIM);
                    while (st.hasMoreTokens()) {
                        String docId = st.nextToken();
                        sb.append("NOT ");
                        sb.append(uniqueKeyIndexField.getName());
                        sb.append(":\"");
                        sb.append(docId);
                        sb.append("\"");
                        if (st.hasMoreTokens()) {
                            sb.append(" ");
                        }
                    }
                }
                query.addFilterQuery(sb.toString());
            }
        }

        CloudKeyword cloudKeyword = simpleSearch.getCloudKeyword();
        if (cloudKeyword != null) {
            query.addFilterQuery("keyword:\"" + cloudKeyword.getKeyword() + "\"");
        }

        Locale singleSearchLocale = simpleSearch.getSingleSearchLocale();
        if (singleSearchLocale != null && StringUtils.isNotBlank(singleSearchLocale.getLanguage())) {
            query.addFilterQuery(IndexField.LANGUAGE_FIELD + ":\"" + singleSearchLocale.getLanguage() + "\"");
        }
    }
}
