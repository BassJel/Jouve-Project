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

import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.facets.FacetDisplayPlugin;
import com.doculibre.constellio.search.SolrFacetUtils;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.data.FacetsDataProvider;
import com.doculibre.constellio.wicket.data.SearchResultsDataProvider;

@SuppressWarnings("serial")
public class FacetsPanel extends Panel {

    private FacetsDataProvider facetsDataProvider;
    private FacetsDataProvider notIncludedFacetsDataProvider;

    private ListView facetListView;

    public FacetsPanel(String id, final SearchResultsDataProvider dataProvider) {
        super(id);

        final SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
//        facetsDataProvider = new FacetsDataProvider(simpleSearch, dataProvider.getResultsPerPage(), true,
//            false);
        notIncludedFacetsDataProvider = new FacetsDataProvider(simpleSearch,
                    dataProvider.getResultsPerPage(), true, true);
        facetsDataProvider = notIncludedFacetsDataProvider;

        add(new CollectionSearchFacetPanel("collectionSearchFacetPanel") {
            @Override
            public boolean isVisible() {
                SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils
                    .getSearchInterfaceConfigServices();
                SearchInterfaceConfig config = searchInterfaceConfigServices.get();
                return config.isShowCollectionsInResultFacets();
            }
        });
        add(new FacetFoldableSectionPanel("currentSearchPanel", new StringResourceModel("currentSearch",
            this, null), "currentSearch") {
            @Override
            protected Component newFoldableSection(String id) {
                return new CurrentSearchPanel(id, dataProvider.getSimpleSearch());
            }

            @Override
            protected String getTitleSectionStyleClass() {
                return "currentSearchSectionTitle";
            }
        });

        //It does nothing, except not hiding the facet column when there's nothing in it
        SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils
        .getSearchInterfaceConfigServices();
        SearchInterfaceConfig config = searchInterfaceConfigServices.get();
        add(new WebMarkupContainer("dontHideWhenEmptyPlaceHolder").setVisible(!config.isHideEmptyFacetColumn()));
        
        List<SearchableFacet> searchableFacets = SolrFacetUtils.getSearchableFacets(simpleSearch);

        facetListView = new ListView("searchableFacets", searchableFacets) {
            @Override
            protected void populateItem(ListItem item) {
                String id = "facetPanel";
                int index = item.getIndex();
                final SearchableFacet searchableFacet = (SearchableFacet) item.getModelObject();

                FacetFoldableSectionPanel foldableSectionPanel = new FacetFoldableSectionPanel(id,
                    new LoadableDetachableModel() {
                        @Override
                        protected Object load() {
                            RecordCollectionServices collectionServices = ConstellioSpringUtils
                                .getRecordCollectionServices();
                            String collectionName = simpleSearch.getCollectionName();
                            RecordCollection collection = collectionServices.get(collectionName);
                            Locale displayLocale = collection.getDisplayLocale(getLocale());
                            return searchableFacet.getLabels().get(displayLocale);
                        }
                    }, searchableFacet) {
                    @Override
                    protected Component newFoldableSection(String id) {
                        Panel facetPanel;
                        if (searchableFacet.isCloudKeyword()) {
                            facetPanel = new CloudTagPanel(id, searchableFacet, facetsDataProvider);
                        } else if (searchableFacet.isCluster()) {
                            facetPanel = new FacetPanel(id, searchableFacet, facetsDataProvider,
                                notIncludedFacetsDataProvider);// new
                            // ClusterPanel(id,
                            // searchableFacet,
                            // facetsDataProvider);
                        } else {
                            facetPanel = new FacetPanel(id, searchableFacet, facetsDataProvider,
                                notIncludedFacetsDataProvider);
                        }
                        return facetPanel;
                    }

                    @Override
                    public boolean isOpened() {
                        return Boolean.FALSE.equals(simpleSearch.isFacetFolded(searchableFacet.getName()));
                    }

                    @Override
                    protected void onToggle(AjaxRequestTarget target) {
                        boolean folded = !Boolean.FALSE.equals(simpleSearch.isFacetFolded(searchableFacet
                            .getName()));
                        simpleSearch.setFacetFolded(searchableFacet.getName(), !folded);
                        super.onToggle(target);
                    }
                };
                item.add(foldableSectionPanel);

                FacetDisplayPlugin facetDisplayPlugin = PluginFactory.getPlugin(FacetDisplayPlugin.class);
                Boolean facetFolded = simpleSearch.isFacetFolded(searchableFacet.getName());
                if (index == 0 && facetFolded == null) {
                    if (facetDisplayPlugin == null || facetDisplayPlugin.isUnfoldedAtStart(searchableFacet)) {
                        simpleSearch.setFacetFolded(searchableFacet.getName(), Boolean.FALSE);
                    }
                } else {
                    Boolean readOpenCookie = foldableSectionPanel.readOpenCookie();
                    if (readOpenCookie != null) {
                        simpleSearch.setFacetFolded(searchableFacet.getName(), !readOpenCookie);
                    } else if (facetDisplayPlugin == null
                        || facetDisplayPlugin.isUnfoldedAtStart(searchableFacet)) {
                        simpleSearch.setFacetFolded(searchableFacet.getName(), Boolean.FALSE);
                    }
                }
            }
        };
        facetListView.setReuseItems(true);
        add(facetListView);
    }

    public void resetFacet(final SearchableFacet searchableFacet) {
        Class<? extends Panel> panelClass;
        if (searchableFacet.isCloudKeyword()) {
            panelClass = CloudTagPanel.class;
        } else if (searchableFacet.isCluster()) {
            panelClass = ClusterPanel.class;
        } else {
            panelClass = FacetPanel.class;
        }

        facetListView.visitChildren(panelClass, new IVisitor() {
            @Override
            public Object component(Component component) {
                if (searchableFacet.isCloudKeyword()) {
                    CloudTagPanel cloudTagPanel = (CloudTagPanel) component;
                    cloudTagPanel.replaceWith(new CloudTagPanel(cloudTagPanel.getId(), searchableFacet,
                        facetsDataProvider));
                    return STOP_TRAVERSAL;
                } else if (searchableFacet.isCluster()) {
                    ClusterPanel clusterPanel = (ClusterPanel) component;
                    clusterPanel.replaceWith(new ClusterPanel(clusterPanel.getId(), searchableFacet,
                        facetsDataProvider));
                    return STOP_TRAVERSAL;
                } else {
                    FacetPanel facetPanel = (FacetPanel) component;
                    SearchableFacet otherFacet = facetPanel.getSearchableFacet();
                    if (searchableFacet.equals(otherFacet)) {
                        facetPanel.replaceWith(new FacetPanel(facetPanel.getId(), searchableFacet,
                            facetsDataProvider, notIncludedFacetsDataProvider));
                        return STOP_TRAVERSAL;
                    }
                }
                return CONTINUE_TRAVERSAL;
            }
        });
    }

    @Override
    public void detachModels() {
        facetsDataProvider.detach();
        notIncludedFacetsDataProvider.detach();
    }

}
