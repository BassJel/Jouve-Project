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
package com.doculibre.constellio.wicket.panels.facets;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.FacetValue;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SearchedFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.pages.SearchFormPage;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;

@SuppressWarnings("serial")
public class CurrentSearchPanel extends Panel {

    public CurrentSearchPanel(String id, final SimpleSearch simpleSearch) {
        super(id, new CompoundPropertyModel(simpleSearch));

        final SimpleSearch newSearch = new SimpleSearch();
        newSearch.setCollectionName(simpleSearch.getCollectionName());
        newSearch.setSingleSearchLocale(simpleSearch.getSingleSearchLocale());
        // ConstellioSession.get().addSearchHistory(simpleSearch);

        if (simpleSearch.getAdvancedSearchRule() != null && !simpleSearch.getAdvancedSearchRule().isValid()) {
        	setVisible(false);
        }
        
        IModel queryModel = new LoadableDetachableModel() {
			
			@Override
			protected Object load() {
				if (simpleSearch.getAdvancedSearchRule() != null) {
					return new StringResourceModel("advancedSearchLabel", CurrentSearchPanel.this, null).getString();
				} else {
					return simpleSearch.getQuery();
				}
			}
		};
        
        add(new Label("query", queryModel) {
            @Override
            public boolean isVisible() {
            	boolean valid = simpleSearch.isQueryValid();
                return  valid && (simpleSearch.getQuery() != null || simpleSearch.getAdvancedSearchRule() != null);
            }
        });

        SimpleSearch cloneDeleteQuery = simpleSearch.clone();
        cloneDeleteQuery.setRefinedSearch(true);
        cloneDeleteQuery.setQuery(null);

        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
        Class<? extends Page> deleteQueryLinkPage;
        PageParameters deleteQueryLinkParams;
        if (StringUtils.isNotBlank(cloneDeleteQuery.getLuceneQuery())) {
            deleteQueryLinkPage = pageFactoryPlugin.getSearchResultsPage();
            deleteQueryLinkParams = SearchResultsPage.getParameters(cloneDeleteQuery);
        } else {
            deleteQueryLinkPage = pageFactoryPlugin.getSearchFormPage();
            deleteQueryLinkParams = SearchFormPage.getParameters(newSearch);
        }
        add(new BookmarkablePageLink("deleteQueryLink", deleteQueryLinkPage, deleteQueryLinkParams) {
            @Override
            public boolean isVisible() {
                return simpleSearch.isQueryValid() && simpleSearch.getQuery() != null && simpleSearch.getAdvancedSearchRule() == null;
            }
        });

        WebMarkupContainer tagsSection = new WebMarkupContainer("tagsSection") {
            @Override
            public boolean isVisible() {
                return !simpleSearch.getTags().isEmpty();
            }
        };
        add(tagsSection);

        IModel tagsModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return new ArrayList<String>(simpleSearch.getTags());
            }
        };
        tagsSection.add(new ListView("tags", tagsModel) {
            @Override
            protected void populateItem(ListItem item) {
                final String tag = item.getModelObjectAsString();
                final SimpleSearch cloneRemoveTag = simpleSearch.clone();
                cloneRemoveTag.getTags().remove(tag);

                PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                Class<? extends Page> removeTagPage;
                PageParameters removeTagParams;
                if (StringUtils.isNotBlank(cloneRemoveTag.getLuceneQuery())) {
                    removeTagPage = pageFactoryPlugin.getSearchResultsPage();
                    removeTagParams = SearchResultsPage.getParameters(cloneRemoveTag);
                } else {
                    removeTagPage = pageFactoryPlugin.getSearchFormPage();
                    removeTagParams = SearchFormPage.getParameters(newSearch);
                }

                item.add(new Label("tag", tag));
                item.add(new BookmarkablePageLink("removeTagLink", removeTagPage, removeTagParams));
            }
        });

        WebMarkupContainer searchedFacetsSection = new WebMarkupContainer("searchedFacetsSection") {
            @Override
            public boolean isVisible() {
                return simpleSearch.isFacetApplied();
            }
        };
        add(searchedFacetsSection);

        IModel includedFacetValuesModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                List<FacetValue> facetIncludedValues = new ArrayList<FacetValue>();
                List<SearchedFacet> searchedFacets = simpleSearch.getSearchedFacets();
                for (SearchedFacet searchedFacet : searchedFacets) {
                    SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
                    boolean isCluster = searchableFacet.isCluster();
                    if (isCluster){
                    	int i = 0;
                    	for (String includedValue : searchedFacet.getIncludedValues()) {
                        	FacetValue facetValue = new FacetValue(searchableFacet, includedValue);
                        	String valueToClusterLabel = searchedFacet.getClustersLabels().get(i);
                            facetValue.setValueToClusterLabel(valueToClusterLabel);
                            facetIncludedValues.add(facetValue);
                            i++;
                        }
                    } else {
                    	for (String includedValue : searchedFacet.getIncludedValues()) {
                        	FacetValue facetValue = new FacetValue(searchableFacet, includedValue);
                            facetIncludedValues.add(facetValue);
                        }
                    }
                }
                return facetIncludedValues;
            }
        };
        searchedFacetsSection.add(new ListView("includedFacetValuesModel", includedFacetValuesModel) {
            @Override
            protected void populateItem(ListItem item) {
                FacetValue facetValue = (FacetValue) item.getModelObject();

                RecordCollectionServices collectionServices = ConstellioSpringUtils
                    .getRecordCollectionServices();
                String collectionName = simpleSearch.getCollectionName();
                RecordCollection collection = collectionServices.get(collectionName);
                Locale displayLocale = collection.getDisplayLocale(getLocale());

                StringBuffer facetValueLabel = new StringBuffer();
                facetValueLabel.append(facetValue.getSearchableFacet().getLabels().get(displayLocale));
                facetValueLabel.append(" : ");
                facetValueLabel.append(facetValue.getLabel(displayLocale));

                item.add(new Label("label", facetValueLabel.toString()));

                SimpleSearch cloneDeleteIncludedFacet = simpleSearch.clone();
                SearchableFacet searchableFacet = facetValue.getSearchableFacet();
                SearchedFacet searchedFacet = cloneDeleteIncludedFacet.getSearchedFacet(searchableFacet.getName());
                
                searchedFacet.getIncludedValues().remove(facetValue.getValue());

                if (searchedFacet.getIncludedValues().isEmpty()
                    && searchedFacet.getExcludedValues().isEmpty()) {
                    cloneDeleteIncludedFacet.getSearchedFacets().remove(searchedFacet);
                }

                // TODO FIXME
//                FacetsPanel facetsPanel = (FacetsPanel) findParent(FacetsPanel.class);
//                facetsPanel.resetFacet(searchableFacet);

                PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                Class<? extends Page> deleteIncludedFacetPage;
                PageParameters deleteIncludedFacetParams;
                if (StringUtils.isNotBlank(cloneDeleteIncludedFacet.getLuceneQuery())) {
                    // ConstellioSession.get().addSearchHistory(clone);
                    deleteIncludedFacetPage = pageFactoryPlugin.getSearchResultsPage();
                    deleteIncludedFacetParams = SearchResultsPage.getParameters(cloneDeleteIncludedFacet);
                } else {
                    deleteIncludedFacetPage = pageFactoryPlugin.getSearchFormPage();
                    deleteIncludedFacetParams = SearchFormPage.getParameters(newSearch);
                }
                item.add(new BookmarkablePageLink("deleteIncludedFacetLink", deleteIncludedFacetPage, deleteIncludedFacetParams));
            }
        });

        IModel excludedFacetValuesModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                List<FacetValue> excludedFacetValues = new ArrayList<FacetValue>();
                List<SearchedFacet> searchedFacets = simpleSearch.getSearchedFacets();

                for (SearchedFacet searchedFacet : searchedFacets) {
                    SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
                    boolean isCluster = searchableFacet.isCluster();
					if (isCluster) {
						int i = 0;
						for (String excludedValue : searchedFacet.getExcludedValues()) {
							FacetValue facetValue = new FacetValue(searchableFacet, excludedValue);
							String valueToClusterLabel = searchedFacet.getClustersLabels().get(i);
							facetValue.setValueToClusterLabel(valueToClusterLabel);
							excludedFacetValues.add(facetValue);
							i++;
						}
					} else {
						for (String excludedValue : searchedFacet.getExcludedValues()) {
							excludedFacetValues.add(new FacetValue(searchableFacet, excludedValue));
						}
					}
                }
                return excludedFacetValues;
            }
        };
        searchedFacetsSection.add(new ListView("excludedFacetValuesModel", excludedFacetValuesModel) {
            @Override
            protected void populateItem(ListItem item) {
                FacetValue facetValue = (FacetValue) item.getModelObject();

                RecordCollectionServices collectionServices = ConstellioSpringUtils
                    .getRecordCollectionServices();
                String collectionName = simpleSearch.getCollectionName();
                RecordCollection collection = collectionServices.get(collectionName);
                Locale displayLocale = collection.getDisplayLocale(getLocale());

                StringBuffer facetValueLabel = new StringBuffer();
                facetValueLabel.append(facetValue.getSearchableFacet().getLabels().get(displayLocale));
                facetValueLabel.append(" : ");
                facetValueLabel.append(facetValue.getLabel(displayLocale));

                item.add(new Label("label", facetValueLabel.toString()));

                SimpleSearch cloneDeleteExcludedFacet = simpleSearch.clone();
                SearchableFacet searchableFacet = facetValue.getSearchableFacet();
                SearchedFacet searchedFacet = cloneDeleteExcludedFacet.getSearchedFacet(searchableFacet.getName());
                searchedFacet.getExcludedValues().remove(facetValue.getValue());

                // TODO FIXME
//                FacetsPanel facetsPanel = (FacetsPanel) findParent(FacetsPanel.class);
//                facetsPanel.resetFacet(searchableFacet);

                if (searchedFacet.getIncludedValues().isEmpty()
                    && searchedFacet.getExcludedValues().isEmpty()) {
                    cloneDeleteExcludedFacet.getSearchedFacets().remove(searchedFacet);
                }

                PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                Class<? extends Page> deleteExcludedFacetPage;
                PageParameters deleteExcludedFacetParams;
                if (StringUtils.isNotBlank(cloneDeleteExcludedFacet.getLuceneQuery())) {
                    deleteExcludedFacetPage = pageFactoryPlugin.getSearchResultsPage();
                    deleteExcludedFacetParams = SearchResultsPage.getParameters(cloneDeleteExcludedFacet);
                } else {
                    deleteExcludedFacetPage = pageFactoryPlugin.getSearchFormPage();
                    deleteExcludedFacetParams = SearchFormPage.getParameters(newSearch);
                }
                item.add(new BookmarkablePageLink("deleteExcludedFacetLink", deleteExcludedFacetPage, deleteExcludedFacetParams));
            }
        });

        add(new Label("cloudKeyword.keyword") {
            @Override
            public boolean isVisible() {
                return simpleSearch.getCloudKeyword() != null;
            }
        });

        SimpleSearch cloneDeleteCloudKeyword = simpleSearch.clone();
        cloneDeleteCloudKeyword.setCloudKeyword(null);

        Class<? extends Page> deleteCloudQueryPage;
        PageParameters deleteCloudQueryParams;
        if (StringUtils.isNotBlank(cloneDeleteCloudKeyword.getLuceneQuery())) {
            deleteCloudQueryPage = pageFactoryPlugin.getSearchResultsPage();
            deleteCloudQueryParams = SearchResultsPage.getParameters(cloneDeleteCloudKeyword);
        } else {
            deleteCloudQueryPage = pageFactoryPlugin.getSearchFormPage();
            deleteCloudQueryParams = SearchFormPage.getParameters(newSearch);
        }
        add(new BookmarkablePageLink("deleteCloudKeywordLink", deleteCloudQueryPage, deleteCloudQueryParams) {
            @Override
            public boolean isVisible() {
                return simpleSearch.getCloudKeyword() != null;
            }
        });
    }
    
	@Override
	public boolean isVisible() {
		SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils
				.getSearchInterfaceConfigServices();
		return super.isVisible() && searchInterfaceConfigServices.get().isCurrentSearchFacet();
	}
}
