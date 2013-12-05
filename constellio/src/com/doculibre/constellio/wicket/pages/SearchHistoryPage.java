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
package com.doculibre.constellio.wicket.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.CloudKeyword;
import com.doculibre.constellio.entities.search.FacetValue;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SearchedFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.NumberFormatUtils;
import com.doculibre.constellio.wicket.data.SearchResultsDataProvider;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class SearchHistoryPage extends BaseConstellioPage {

    public SearchHistoryPage(PageParameters params) {
        super(params); 

        add(new WebMarkupContainer("cloudKeyword.header") {
            @Override
            public boolean isVisible() {
                return false;
            }
        });

        IModel historiqueRechercheModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return ConstellioSession.get().getCombinedSearchHistory();
            }
        };
        add(new ListView("searchHistory", historiqueRechercheModel) {
            @Override
            protected void populateItem(ListItem item) {
                final SimpleSearch simpleSearch = (SimpleSearch) item.getModelObject();
                final String collectionName = simpleSearch.getCollectionName();
                RecordCollectionServices collectionServices = ConstellioSpringUtils
                .getRecordCollectionServices();
                RecordCollection collection = collectionServices.get(collectionName);
                final Locale displayLocale = collection.getDisplayLocale(getLocale());
                
                final int index = item.getIndex();

                item.add(new Label("query", simpleSearch.getQuery()));

                IModel includedFacetValuesModel = new LoadableDetachableModel() {
                    @Override
                    protected Object load() {
                        List<FacetValue> includedFacetValues = new ArrayList<FacetValue>();
                        List<SearchedFacet> searchedFacets = simpleSearch.getSearchedFacets();
                        for (SearchedFacet searchedFacet : searchedFacets) {
                            SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
                            for (String includedValue : searchedFacet.getIncludedValues()) {
                                includedFacetValues.add(new FacetValue(searchableFacet, includedValue));
                            }
                        }
                        return includedFacetValues;
                    }
                };
                item.add(new ListView("includedFacetValues", includedFacetValuesModel) {
                    @Override
                    protected void populateItem(ListItem item) {
                        final FacetValue facetValue = (FacetValue) item.getModelObject();

                        RecordCollectionServices collectionServices = ConstellioSpringUtils
                            .getRecordCollectionServices();
                        String collectionName = simpleSearch.getCollectionName();
                        RecordCollection collection = collectionServices.get(collectionName);
                        Locale displayLocale = collection.getDisplayLocale(getLocale());

                        StringBuffer label = new StringBuffer();
                        label.append(facetValue.getSearchableFacet().getLabels().get(displayLocale));
                        label.append(" : ");
                        label.append(facetValue.getLabel(displayLocale));

                        item.add(new Label("label", label.toString()));
                    }
                });

                IModel excludedFacetValuesModel = new LoadableDetachableModel() {
                    @Override
                    protected Object load() {
                        List<FacetValue> excludedFacetValues = new ArrayList<FacetValue>();
                        List<SearchedFacet> searchedFacets = simpleSearch.getSearchedFacets();
                        for (SearchedFacet searchedFacet : searchedFacets) {
                            SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
                            for (String excludedFacetValue : searchedFacet.getExcludedValues()) {
                                excludedFacetValues.add(new FacetValue(searchableFacet, excludedFacetValue));
                            }
                        }
                        return excludedFacetValues;
                    }
                };
                
                item.add(new ListView("excludedFacetValues", excludedFacetValuesModel) {
                    @Override
                    protected void populateItem(ListItem item) {
                        final FacetValue facetValue = (FacetValue) item.getModelObject();

                        

                        StringBuffer label = new StringBuffer();
                        label.append(facetValue.getSearchableFacet().getLabels().get(displayLocale));
                        label.append(" : ");
                        label.append(facetValue.getLabel(displayLocale));
                        item.add(new Label("label", label.toString()));
                    }
                });

                item.add(new Label("noFacet", "S/O") {
                    @Override
                    public boolean isVisible() {
                        return simpleSearch.isFacetApplied();
                    }
                });

                IModel cloudKeywordModel = new LoadableDetachableModel() {
                    @Override
                    protected Object load() {
                        String libelle;
                        CloudKeyword cloudKeyword = simpleSearch.getCloudKeyword();
                        if (cloudKeyword != null) {
                            libelle = cloudKeyword.getKeyword();
                        } else {
                            libelle = "S/O";
                        }
                        return libelle;
                    }
                };
                item.add(new Label("cloudKeyword.keyword", cloudKeywordModel) {
                    @Override
                    public boolean isVisible() {
                        return false;
                    }
                });
                
                item.add(new Label("collectionName", collection.getTitle(displayLocale)));

                SearchResultsDataProvider dataProvider = new SearchResultsDataProvider(simpleSearch, 10);
                int nbResultats = dataProvider.size();
                dataProvider.detach();
                item.add(new Label("resultsCount", NumberFormatUtils.format(nbResultats, getLocale())));

                item.add(new Link("relaunchSearchLink") {
                    @Override
                    public void onClick() {
                        SimpleSearch clone = simpleSearch.clone();
                        // int sizeHistorique =
                        // ConstellioSession.get().getSearchHistory(solrServerName).size();
                        // Pas la dernière recherche de l'historique
                        // if (index < sizeHistorique - 1) {
                        // ConstellioSession.get().addSearchHistory(clone);
                        // }
                        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                        setResponsePage(pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone));
                    }
                });

                item.add(new Link("deleteSearchLink") {
                    @Override
                    public void onClick() {
                        ConstellioSession.get().removeSearchHistory(index);
                    }
                });
            }
        });
    }

}
