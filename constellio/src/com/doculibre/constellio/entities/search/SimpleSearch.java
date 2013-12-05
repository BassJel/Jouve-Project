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
package com.doculibre.constellio.entities.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;

import com.doculibre.analyzer.FrenchAnalyzer;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.entities.search.advanced.SearchRulesFactory;
import com.doculibre.constellio.search.SolrFacetUtils;
import com.doculibre.constellio.search.SynonymUtils;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.SimpleParams;

@SuppressWarnings("serial")
public class SimpleSearch implements Serializable, Cloneable {
    
    public static final String SEARCH_ALL = "*:*";
    
    public static final String COLLECTION_NAME = "collectionName";
	
	public static final String ALL_WORDS = "allWords";
	public static final String AT_LEAST_ONE_WORD = "atLeastOneWord";
	public static final String EXACT_EXPRESSION = "exactExpression";
	
	public static final String SORT_RELEVANCE = null;
	public static final String SORT_ASCENDING = "asc";
	public static final String SORT_DESCENDING = "desc";
	
    private String collectionName;
	
    private String query; // Filled with form text field value
	
    private String searchType = ALL_WORDS; // Default
    
    private Locale singleSearchLocale;

    private int page = 0;

	private List<SearchedFacet> searchedFacets = new ArrayList<SearchedFacet>(); 

	private CloudKeyword cloudKeyword; 

    private boolean refinedSearch = false;
	
	private String sortField;
	
	private String sortOrder;
    
    private Map<String, Boolean> facetFolding = new HashMap<String, Boolean>();
	
	private Map<String, String> facetSort = new HashMap<String, String>();
	
	private Long featuredLinkId = null;
	
    private Set<String> tags = new HashSet<String>();

    private Map<String, Integer> facetPages = new HashMap<String, Integer>();
    
    private SearchRule advancedSearchRule;
    
    private String searchLogDocId;
    
	public SimpleSearch() {
        super();
    }
    
    public SimpleSearch(String collectionName) {
        super();
        this.collectionName = collectionName;
    }
	
	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getQuery() {
		if (advancedSearchRule == null) {
			return query;
		} else if (advancedSearchRule.isValid()){
			return advancedSearchRule.toLuceneQuery();
		} else {
			throw new RuntimeException("Invalid Lucene Query");
		}
	}
	
	public boolean isQueryValid() {
		return advancedSearchRule == null || advancedSearchRule.isValid();
	}
    
    public void setQuery(String query) {
        if (query != null) {
            query = query.trim();
        }
        this.query = query;
    }
    
    public String getEscapedQuery() {
        String escapedQuery;
        if (query != null && !SEARCH_ALL.equals(query)) {
            escapedQuery = query.replace(":", "\\:");
            // FIXME
            QueryParser queryParser = new QueryParser(Version.LUCENE_34, IndexField.DEFAULT_SEARCH_FIELD,
                new FrenchAnalyzer(Version.LUCENE_34));
            try {
                queryParser.parse(escapedQuery);
            } catch (ParseException e) {
                escapedQuery = QueryParser.escape(escapedQuery);
            }
        } else {
            escapedQuery = query;
        }
        return escapedQuery;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public Locale getSingleSearchLocale() {
        return singleSearchLocale;
    }

    public void setSingleSearchLocale(Locale singleSearchLocale) {
        this.singleSearchLocale = singleSearchLocale;
        SearchedFacet languageFacet = getSearchedFacet(IndexField.LANGUAGE_FIELD);
        if (languageFacet != null) {
            languageFacet.getIncludedValues().clear();
            languageFacet.getIncludedValues().add(singleSearchLocale.getLanguage());
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
    
    public void initFacetPages() {
        facetPages.clear();
    }
    
    public int getFacetPage(String searchableFacetName) {
        Integer facetPage = facetPages.get(searchableFacetName);
        return facetPage == null ? 0 : facetPage;
    }

	public void setFacetPage(String searchableFacetName, int page) {
		facetPages.put(searchableFacetName, page);
	}
	
	public void clearPages() {
	    page = 0;
	    facetPages.clear();
	}
	
	public void clearFacetFoldingAndSorting() {
		facetFolding.clear();
		facetSort.clear();
	}
    
    public boolean isEmpty() {
        boolean result;
        if (StringUtils.isNotEmpty(query)) {
            result = false;
        } else if (!searchedFacets.isEmpty()) {
        	result = true;
        	for (SearchedFacet searchedFacet : searchedFacets) {
				if (!searchedFacet.getIncludedValues().isEmpty()) {
					result = false;
					break;
				}
			}
        } else if (cloudKeyword != null) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }
	
	public void removeSearchedFacet(SearchableFacet searchableFacet) {
		for (Iterator<SearchedFacet> it = searchedFacets.iterator(); it.hasNext();) {
			SearchedFacet searchedFacet = it.next();
			if (searchedFacet.getSearchableFacet().equals(searchableFacet)) {
				it.remove();
			}
		}
	}
	
	//TODO ici changement
	public void addSearchedFacet(SearchableFacet searchableFacet, FacetValue facetValue) {
		SearchedFacet existingFacet = null;
		for (SearchedFacet searchedFacet : searchedFacets) {
			if (searchedFacet.getSearchableFacet().equals(searchableFacet)) {
				existingFacet = searchedFacet;
				break;
			}
		}
		if (existingFacet == null) {
			existingFacet = new SearchedFacet(searchableFacet);
			searchedFacets.add(existingFacet);
		}
		String value = facetValue.getValue();
		if (!existingFacet.getIncludedValues().contains(value)) {
			existingFacet.getIncludedValues().add(value);
			String clusterLabel = facetValue.getValueToClusterLabel();
//			String clusterLabel = searchableFacet.getPossibleValuesLabels().get(value).get(this.locale);
			existingFacet.getClustersLabels().add(clusterLabel);
		}
	}
	
	//TODO ici aussi changement 
	public void excludeSearchedFacet(SearchableFacet searchableFacet, FacetValue facetValue) {
		SearchedFacet existingFacet = null;
		for (SearchedFacet searchedFacet : searchedFacets) {
			if (searchedFacet.getSearchableFacet().equals(searchableFacet)) {
				existingFacet = searchedFacet;
				break;
			}
		}
		if (existingFacet == null) {
			existingFacet = new SearchedFacet(searchableFacet);
			searchedFacets.add(existingFacet);
		}
		String value = facetValue.getValue();
		if (existingFacet.getIncludedValues().contains(value)) {
		    existingFacet.getIncludedValues().remove(value);
		} else if (!existingFacet.getExcludedValues().contains(value)) {
			existingFacet.getExcludedValues().add(value);
			String clusterLabel = facetValue.getValueToClusterLabel();
//			String clusterLabel = searchableFacet.getPossibleValuesLabels().get(value).get(this.locale);
			existingFacet.getClustersLabels().add(clusterLabel);
		}
	}
	
	public List<SearchedFacet> getSearchedFacets() {
		return searchedFacets;
	}
	
	public SearchedFacet getSearchedFacet(String facetName) {
		SearchedFacet existingFacet = null;
		for (SearchedFacet searchedFacet : searchedFacets) {
			if (searchedFacet.getSearchableFacet().getName().equals(facetName)) {
				existingFacet = searchedFacet;
				break;
			}
		}
		return existingFacet;
	}

    public boolean isFacetApplied() {
        boolean facetApplied = false;
        for (SearchedFacet searchedFacet : getSearchedFacets()) {
            if (!searchedFacet.getIncludedValues().isEmpty() || !searchedFacet.getExcludedValues().isEmpty()) {
                facetApplied = true;
                break;
            }
        }
        return facetApplied;
    }
    
	public SearchedFacet getCluster() {
		SearchedFacet cluster = null;
		for (SearchedFacet searchedFacet : searchedFacets) {
			if (searchedFacet.getSearchableFacet().isCluster()) {
				cluster = searchedFacet;
				break;
			}
		}
		return cluster;
	}
	
	public CloudKeyword getCloudKeyword() {
		return cloudKeyword;
	}

	public void setCloudKeyword(CloudKeyword cloudKeyword) {
		this.cloudKeyword = cloudKeyword;
	}

	public boolean isRefinedSearch() {
		return refinedSearch;
	}

	public void setRefinedSearch(boolean refinedSearch) {
		this.refinedSearch = refinedSearch;
	}

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
    
    public Boolean isFacetFolded(String facetName) {
        return facetFolding.get(facetName);
    }
    
    public void setFacetFolded(String facetName, boolean folded) {
        facetFolding.put(facetName, folded);
    }
    
    public String getFacetSort(String facetName) {
        String sort = facetSort.get(facetName);
        return sort != null ? sort : SearchableFacet.SORT_NB_RESULTS;
    }
    
    public void setFacetSort(String facetName, String sort) {
        facetSort.put(facetName, sort);
    }

    public Long getFeaturedLinkId() {
        return featuredLinkId;
    }

    public void setFeaturedLinkId(Long featuredLinkId) {
        this.featuredLinkId = featuredLinkId;
    }
	
	public String getLuceneQuery() {
		return getLuceneQuery(true);
	}
	
	public String getLuceneQuery(boolean withFacets) {
		return getLuceneQuery(withFacets, withFacets);
	}
	
	public String getLuceneQuery(boolean withSingleValuedFacets, boolean withMultiValuedFacets) {
		StringBuffer sb = new StringBuffer();

		boolean queryStarted = false;
		if (advancedSearchRule == null) {
		if (StringUtils.isNotEmpty(collectionName) && StringUtils.isNotEmpty(query)) {
		    String escapedQuery = getEscapedQuery();
			sb.append("(");
			if (SEARCH_ALL.equals(query)) {
			    sb.append(escapedQuery);
			    queryStarted = true;
			} else if (AT_LEAST_ONE_WORD.equals(searchType)) {
                String[] terms = escapedQuery.split(" ");
                for(int i=0; i < terms.length; i++){
                    sb.append(SynonymUtils.addSynonyms(terms[i], collectionName, false));
                    queryStarted = true;
                    if(i < terms.length - 1){
                        sb.append(" OR ");
                    }
                }
			} else if (EXACT_EXPRESSION.equals(searchType)) {
				sb.append(SynonymUtils.addSynonyms(escapedQuery, collectionName, true));
				queryStarted = true;
			} else {
				// TOUS_LES_MOTS
				sb.append(SynonymUtils.addSynonyms(escapedQuery, collectionName, false));
				queryStarted = true;
			}
			sb.append(")");
		} 
        
        if (!tags.isEmpty()) {
            if (!queryStarted) {
                queryStarted = true;
            } else {
                sb.append(" AND ");
            }
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
		} else {
			if (advancedSearchRule.isValid()) {
				sb.append(advancedSearchRule.toLuceneQuery());
			}
			queryStarted = true;
			return sb.toString();
		}
		
		if (withSingleValuedFacets || withMultiValuedFacets) {
			for (SearchedFacet searchedFacet : searchedFacets) {
				SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
				if ((searchableFacet.isMultiValued() && withMultiValuedFacets) || (!searchableFacet.isMultiValued() && withSingleValuedFacets)) {
					if (!searchableFacet.isCluster()) {
						if (searchableFacet.isQuery()) {
							if (!searchedFacet.getIncludedValues().isEmpty()) {
								if (queryStarted) {
									sb.append(" +");
								} else {
									queryStarted = true;
								}
								boolean first = true;
								sb.append("(");
								for (String includedValue : searchedFacet.getIncludedValues()) {
									if (first) {
										first = false;
									} else {
										sb.append(" OR ");
									}
									sb.append("");
									sb.append(includedValue); // Nom requ�te
									sb.append("");
								}
								sb.append(")");
							}
						} else {
							String facetName = searchableFacet.getName();
							if (!searchedFacet.getIncludedValues().isEmpty()) {
								if (queryStarted) {
									sb.append(" +");
								} else {
									queryStarted = true;
								}
								boolean first = true;
								sb.append(facetName);
								sb.append(":(");
								for (String includedValue : searchedFacet.getIncludedValues()) {
									if (first) {
										first = false;
									} else {
										sb.append(" OR ");
									}
									sb.append("\"");
									sb.append(correctFacetValue(includedValue));
									sb.append("\"");
								}
								sb.append(")");
							}
						}
					}
				}
			}
			
			for (SearchedFacet searchedFacet : searchedFacets) {
				SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
				if ((searchableFacet.isMultiValued() && withMultiValuedFacets) || (!searchableFacet.isMultiValued() && withSingleValuedFacets)) {
					if (!searchableFacet.isCluster()) {
						String facetName = searchableFacet.getName();
						for (String excludedValue : searchedFacet.getExcludedValues()) {
							if (queryStarted) {
								sb.append(" ");
							} else {
								queryStarted = true;
							}
							sb.append("NOT ");
							if (searchableFacet.isQuery()) {
								sb.append(correctFacetValue(excludedValue));
							} else {
								sb.append(facetName);
								sb.append(":\"");
								sb.append(correctFacetValue(excludedValue));
								sb.append("\"");
							}
						}
					}
				}
			}
			
			SearchedFacet cluster = getCluster();
			if (cluster != null) {
				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				RecordCollection collection = collectionServices.get(collectionName);
				IndexField uniqueKeyIndexField = collection.getUniqueKeyIndexField();
				
				for (String includedValue : cluster.getIncludedValues()) {
					if (queryStarted) {
						sb.append(" +");
					} else {
						queryStarted = true;
					}
					boolean first = true;
					sb.append(uniqueKeyIndexField.getName());
					sb.append(":(");
					
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
					sb.append(")");
				}
				for (String excludedValue : cluster.getExcludedValues()) {
					if (queryStarted) {
						sb.append(" ");
					} else {
						queryStarted = true;
					}
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
			}
			
			if (cloudKeyword != null) {
				if (queryStarted) {
					sb.append(" +");
				} else {
					queryStarted = true;
				}
				sb.append("keyword:\"");
				sb.append(cloudKeyword.getKeyword());
				sb.append("\"");
			}
			
			if (singleSearchLocale != null && StringUtils.isNotBlank(singleSearchLocale.getLanguage())) {
                if (queryStarted) {
                    sb.append(" +");
                } else {
                    queryStarted = true;
                }
                sb.append(IndexField.LANGUAGE_FIELD);
                sb.append(":\"");
                sb.append(singleSearchLocale.getLanguage());
                sb.append("\"");
			}
		}
		String luceneQuery = sb.toString();
		int countOpen = StringUtils.countMatches(luceneQuery, "(");
        int countClose = StringUtils.countMatches(luceneQuery, ")");
        if (countOpen == 1 && countClose == 1 && luceneQuery.startsWith("(") && luceneQuery.endsWith(")")) {
            luceneQuery = luceneQuery.substring(1, luceneQuery.length() - 1);
        } 
		return luceneQuery;
	}

	public static String correctFacetValue(String facetValue) {
		facetValue = facetValue.replace("\\", "\\\\");
		facetValue = facetValue.replace("+", "\\+");
		facetValue = facetValue.replace("-", "\\-");
		facetValue = facetValue.replace("&&", "\\&&");
		facetValue = facetValue.replace("||", "\\||");
		facetValue = facetValue.replace("!", "\\!");
		facetValue = facetValue.replace("(", "\\(");
		facetValue = facetValue.replace(")", "\\)");
		facetValue = facetValue.replace("{", "\\{");
		facetValue = facetValue.replace("}", "\\}");
		facetValue = facetValue.replace("[", "\\[");
		facetValue = facetValue.replace("]", "\\]");
		facetValue = facetValue.replace("^", "\\^");
		facetValue = facetValue.replace("\"", "\\\"");
		facetValue = facetValue.replace("~", "\\~");
		facetValue = facetValue.replace("*", "\\*");
		facetValue = facetValue.replace("?", "\\?");
		facetValue = facetValue.replace(":", "\\:");
		return facetValue;
	}
	
	@Override
	public String toString() {
		return getLuceneQuery();
	}

    @Override
    public SimpleSearch clone() {
        SimpleSearch clone = new SimpleSearch();
        clone.collectionName = collectionName;
        clone.query = query;
        clone.searchType = searchType;
        clone.singleSearchLocale = singleSearchLocale;
        clone.page = page;
        clone.searchLogDocId = searchLogDocId;
        
        for (SearchedFacet searchedFacet : searchedFacets) {
            clone.searchedFacets.add(searchedFacet.clone());
        }

        clone.facetPages = new HashMap<String, Integer>();
        for(String facet : facetPages.keySet()) {
        	clone.facetPages.put(facet, facetPages.get(facet));
        }
        
        if (cloudKeyword != null) {
            clone.cloudKeyword = cloudKeyword.clone();
        }
        
        clone.refinedSearch = refinedSearch;
        clone.sortField = sortField;
        clone.sortOrder = sortOrder;
        clone.tags.addAll(tags);
        clone.facetFolding.putAll(this.facetFolding);
        clone.facetSort.putAll(facetSort);
        
        clone.featuredLinkId = featuredLinkId;
        if (advancedSearchRule != null) {
        	clone.advancedSearchRule = advancedSearchRule.cloneRule();
        }
        
        return clone;
    }
    
	public SimpleParams toSimpleParams() {
		return toSimpleParams(false);
	}
	
	public SimpleParams toSimpleParams(boolean onlyAdvancedSearchRuleTypes) {
	    SimpleParams params = new SimpleParams();
	    params.add("collectionName", collectionName);
	    params.add("query", query);
	    params.add("searchType", searchType);
	    if (searchLogDocId != null) {
	    	params.add("searchLogDocId", searchLogDocId);
	    }
	    
        if (singleSearchLocale != null) {
            params.add("singleSearchLocale", singleSearchLocale.toString());
        }
        params.add("page", "" + page);

        if (advancedSearchRule != null) {
        	params.addAll(advancedSearchRule.toSimpleParams(onlyAdvancedSearchRuleTypes));
        }
        
        List<SearchableFacet> searchableFacets = SolrFacetUtils.getSearchableFacets(this);
        for (SearchableFacet searchableFacet : searchableFacets) {
            String facetName = searchableFacet.getName();
            String sort = getFacetSort(facetName);
            Boolean folded = isFacetFolded(facetName);
            if (!facetSort.isEmpty() && sort != null) {
                params.add(facetName + ".sort", sort);
            }
            if (!facetFolding.isEmpty() && folded != null) {
                params.add(facetName + ".folded", "" + folded);
            }
            if (!facetPages.isEmpty() && facetPages.get(facetName) != null ) {
            	params.add(facetName + ".page", "" + facetPages.get(facetName));
            }
        }
        
	    for (SearchedFacet searchedFacet : searchedFacets) {
            SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
            String facetName = searchableFacet.getName();
            List<String> includedValues = searchedFacet.getIncludedValues();
            List<String> excludedValues = searchedFacet.getExcludedValues();
            boolean queryFacet = searchableFacet.isQuery();
            boolean cloudKeywordFacet = searchableFacet.isCloudKeyword();
            boolean clusterFacet = searchableFacet.isCluster();
            params.add(facetName + ".includedValues", includedValues);
            params.add(facetName + ".excludedValues", excludedValues);
            if (queryFacet) {
                params.add(facetName + ".queryFacet", "" + queryFacet);
            }
            if (cloudKeywordFacet) {
                params.add(facetName + ".cloudKeywordFacet", "" + cloudKeywordFacet);
            }
            if (clusterFacet) {
                params.add(facetName + ".clusterFacet", "" + clusterFacet);
                params.add(facetName + ".clustersLabels", searchedFacet.getClustersLabels());
            }
        }
	    if (cloudKeyword != null) {
	        String keyword = cloudKeyword.getKeyword();
	        int weight = cloudKeyword.getWeight();
	        params.add("keyword", keyword);
	        params.add("keyword.weight", "" + weight);
	    }
	    for (String tag : tags) {
            params.add("tag", tag);
        }
	    if (refinedSearch) {
	        params.add("refinedSearch", Boolean.toString(refinedSearch));
	    }
	    if (sortField != null) {
	        params.add("sortField", sortField);
	    }
	    if (sortOrder != null) {
	        params.add("sortOrder", sortOrder);
	    }
	    if (featuredLinkId != null) {
	        params.add("featuredLinkId", featuredLinkId.toString());
	    }
	    return params;
	}
	
	public static SimpleSearch toSimpleSearch(String paramsQuery) {
		SimpleParams params = new SimpleParams();
		params.parse(paramsQuery);
		return toSimpleSearch(params);
	}
	
	private static Set<SearchableFacet> getPotentialSearchableFacets(SimpleParams params) {
		Set<SearchableFacet> potentialFacets = new HashSet<SearchableFacet>();
		Set<String> potentialFacetNames = new HashSet<String>();
		
		String collectionName = params.getString("collectionName");
		List<SearchableFacet> searchableFacets = SolrFacetUtils.getSearchableFacets(collectionName);
        for (SearchableFacet searchableFacet : searchableFacets) {
            String facetName = searchableFacet.getName();
            potentialFacetNames.add(facetName);
            potentialFacets.add(searchableFacet);
        }

//        String[] facetSuffixes = { ".includedValues", ".excludedValues", ".queryFacet", ".cloudKeywordFacet", ".clusterFacet" };
//        for (String paramName : params.keySet()) {
//        	loop2: for (String facetSuffix : facetSuffixes) {
//        		// FIXME
//        		if (paramName.endsWith(facetSuffix) && !paramName.startsWith("lang")) {
//        			String potentialFacetName = paramName.substring(0, paramName.lastIndexOf(facetSuffix));
//        			if (!potentialFacetNames.contains(potentialFacetName)) {
//        				SearchableFacet potentialFacet = new SearchableFacet(potentialFacetName);
//        				if (paramName.endsWith(".queryFacet")) {
//        					potentialFacet.setQuery(true);
//        				} else if (paramName.endsWith(".cloudKeywordFacet")) {
//        					potentialFacet.setCloudKeyword(true);
//        				} else if (paramName.endsWith(".clusterFacet")) {
//        					potentialFacet.setCluster(true);
//        				}
//        				potentialFacets.add(potentialFacet);
//        			}
//        			break loop2;
//        		}
//			}
//		}
		return potentialFacets;
	}
	
	public static SimpleSearch toSimpleSearch(SimpleParams params) {
	    SimpleSearch simpleSearch = new SimpleSearch();
	    String collectionName = params.getString("collectionName");
	    simpleSearch.collectionName = collectionName;

	    String[] queryWords = StringUtils.split(params.getString("query"), ' ');
	    String query = StringUtils.join(queryWords, ' ');
	    simpleSearch.query = query;

        String searchType = params.getString("searchType");
        simpleSearch.searchType = searchType;
        
        String searchLogDocId = params.getString("searchLogDocId");
        simpleSearch.searchLogDocId = searchLogDocId;
        
        String singleSearchLocaleStr = params.getString("singleSearchLocale");
        if (singleSearchLocaleStr != null) {
            simpleSearch.singleSearchLocale = new Locale(singleSearchLocaleStr);
        }
        
        String pageStr = params.getString("page");
        if (pageStr != null) {
            simpleSearch.page = Integer.parseInt(pageStr);
        }

        if (params.getString(SearchRule.ROOT_PREFIX + SearchRule.DELIM + SearchRule.PARAM_TYPE) != null) {
        	simpleSearch.advancedSearchRule = SearchRulesFactory.constructSearchRule(params, null, SearchRule.ROOT_PREFIX);
        }

        Set<SearchableFacet> searchableFacets = getPotentialSearchableFacets(params);
        for (SearchableFacet searchableFacet : searchableFacets) {
        	String facetName = searchableFacet.getName();
            SearchedFacet searchedFacet = new SearchedFacet(searchableFacet);
            
            List<String> includedValues = params.getList(facetName  +".includedValues");
            for (String includedValue : includedValues) {
                if (StringUtils.isNotEmpty(includedValue)) {
                    searchedFacet.getIncludedValues().add(includedValue);
                }
            }
            
            List<String> excludedValues = params.getList(facetName  +".excludedValues");
            for (String excludedValue : excludedValues) {
                if (StringUtils.isNotEmpty(excludedValue)) {
                    searchedFacet.getExcludedValues().add(excludedValue);
                }
            }
            
            String facetSort = params.getString(facetName  +".sort");
            if (facetSort != null) {
                simpleSearch.setFacetSort(facetName, facetSort);
            }

            String foldedStr = params.getString(facetName + ".folded");
            if (foldedStr != null) {
                simpleSearch.setFacetFolded(facetName, Boolean.valueOf(foldedStr));
            }
            
            String queryFacetStr = params.getString(facetName + ".queryFacet");
            if (queryFacetStr != null) {
                searchableFacet.setQuery(Boolean.valueOf(queryFacetStr));
            }
            
            String cloudKeywordFacetStr = params.getString(facetName + ".cloudKeywordFacet");
            if (cloudKeywordFacetStr != null) {
                searchableFacet.setCloudKeyword(Boolean.valueOf(cloudKeywordFacetStr));
            }

            String clusterFacetStr = params.getString(facetName + ".clusterFacet");
            if (clusterFacetStr != null) {
                searchableFacet.setCluster(Boolean.valueOf(clusterFacetStr));
                //a ajouter aussi dans l autre
                List<String> clustersLabels = params.getList(facetName  + ".clustersLabels");
                if (clustersLabels != null){
                	for (String clusterLabel : clustersLabels) {
                        if (StringUtils.isNotEmpty(clusterLabel)) {
                            searchedFacet.getClustersLabels().add(clusterLabel);
                        }
                    }
                }
                
            }
            
            String currentPage = params.getString(facetName + ".page");
            if (currentPage != null) {
                simpleSearch.setFacetPage(facetName, Integer.valueOf(currentPage));
            }
            
            simpleSearch.searchedFacets.add(searchedFacet);
	    }
	    
	    String keyword = params.getString("keyword");
	    String weightStr = params.getString("keyword.weight");
	    if (keyword != null) {
	        CloudKeyword cloudKeyword = new CloudKeyword(keyword, Integer.parseInt(weightStr));
	        simpleSearch.cloudKeyword = cloudKeyword;
	    }
	    
	    List<String> tags = params.getList("tag");
	    simpleSearch.tags.addAll(tags);
	    
	    String refinedSearchStr = params.getString("refinedSearch");
	    if (refinedSearchStr != null) {
	        simpleSearch.refinedSearch = Boolean.valueOf(refinedSearchStr);
	    }
	    String sortField = params.getString("sortField");
	    String sortOrder = params.getString("sortOrder");
	    simpleSearch.sortField = sortField;
	    simpleSearch.sortOrder = sortOrder;
	    
	    String featuredLinkIdStr = params.getString("featuredLinkId");
	    if (featuredLinkIdStr != null) {
	        try {
	            simpleSearch.featuredLinkId = new Long(featuredLinkIdStr);
            } catch (NumberFormatException e) {
                // Ignore
            }
	    }
	    
	    return simpleSearch;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public SearchRule getAdvancedSearchRule() {
		return advancedSearchRule;
	}

	public void setAdvancedSearchRule(SearchRule advancedSearchRule) {
		this.advancedSearchRule = advancedSearchRule;
	}
    
    public String getSearchLogDocId() {
		return searchLogDocId;
	}

	public void setSearchLogDocId(String searchLogDocId) {
		this.searchLogDocId = searchLogDocId;
	}
	
}
