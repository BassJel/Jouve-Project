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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SearchedFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.search.SolrFacetUtils;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SearchServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.NumberFormatUtils;
import com.doculibre.constellio.wicket.components.holders.LinkHolder;
import com.doculibre.constellio.wicket.components.sort.AutoHidePagingNavigator;
import com.doculibre.constellio.wicket.components.sort.SortableListDataProvider;
import com.doculibre.constellio.wicket.components.sort.ULAutoHidePagingNavigator;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.models.SortableListModel;
import com.doculibre.constellio.wicket.models.VisibleCollectionsModel;
import com.doculibre.constellio.wicket.pages.BaseSearchPage;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class CollectionSearchFacetPanel extends AjaxPanel {

    private VisibleCollectionsModel visibleCollectionsModel;

    private SortableListModel<CollectionResult> facetableCollectionsModel;

    public CollectionSearchFacetPanel(String id) {
    	this(id, null);
    }
    
    public CollectionSearchFacetPanel(String id, IDataProvider searchResultsDataProvider) {
        super(id);

        int itemsPerPage = 10;

        visibleCollectionsModel = new VisibleCollectionsModel();
        facetableCollectionsModel = newFacetableCollectionsModel(searchResultsDataProvider);
        IDataProvider dataProvider = new SortableListDataProvider(facetableCollectionsModel);
        DataView dataView = new DataView("collections", dataProvider, itemsPerPage) {
            @Override
            protected void populateItem(Item item) {
            	int index = item.getIndex();
            	String itemStyleClass = getItemStyleClass(index);
            	if (StringUtils.isNotBlank(itemStyleClass)) {
            		item.add(new SimpleAttributeModifier("class", itemStyleClass));
            	}
            	
                CollectionResult collectionResult = (CollectionResult) item.getModelObject();
                Long collectionId = collectionResult.collectionId;
                final Long numFound = collectionResult.numFound;
                final ReloadableEntityModel<RecordCollection> collectionModel = new ReloadableEntityModel<RecordCollection>(
                    RecordCollection.class, collectionId);
                IModel labelModel = new LoadableDetachableModel() {
                    @Override
                    protected Object load() {
                        RecordCollection collection = collectionModel.getObject();
                        return getCollectionTitle(collection, numFound);
                    }
                };
                item.add(new LinkHolder("collectionLinkHolder", labelModel) {
                    @Override
                    protected WebMarkupContainer newLink(String id) {
                        RecordCollection collection = collectionModel.getObject();
                        BaseSearchPage baseSearchPage = (BaseSearchPage) CollectionSearchFacetPanel.this.getPage();
                        SimpleSearch simpleSearch = baseSearchPage.getSimpleSearch();

                        SimpleSearch newSearch = simpleSearch.clone();
                        newSearch.setCollectionName(collection.getName());
                        newSearch.clearPages();
//                        newSearch.getSearchedFacets().clear();

                        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                        Class<? extends BaseSearchPage> pageClass = pageFactoryPlugin.getSearchResultsPage();
                        PageParameters params = SearchResultsPage.getParameters(newSearch);

                        WebMarkupContainer link = newCollectionLink(id, collection, pageClass, params);
                        link.add(new AttributeModifier("class", true, new LoadableDetachableModel() {
                            @Override
                            protected Object load() {
                                RecordCollection collection = collectionModel.getObject();
                                BaseSearchPage baseSearchPage = (BaseSearchPage) getPage();
                                String collectionName = baseSearchPage.getSimpleSearch().getCollectionName();
                                return collection.getName().equals(collectionName) ? getSelectedStyleClass() : null;
                            }
                        }));
                        return link;
                    }

                    @Override
                    public void detachModels() {
                        collectionModel.detach();
                        super.detachModels();
                    }

                    @Override
                    protected Component newLabel(String id, IModel labelModel) {
                        return super.newLabel(id, labelModel).setEscapeModelStrings(false);
                    }
                });
            }
        };
        add(dataView);

        AutoHidePagingNavigator pager = new ULAutoHidePagingNavigator("pager", dataView);
        add(pager);
    }
    
    protected WebMarkupContainer newCollectionLink(
    		String id, 
    		RecordCollection collection, 
    		Class<? extends BaseSearchPage> pageClass, 
    		PageParameters params) {
    	return new BookmarkablePageLink(id, pageClass, params);
    }

    protected SortableListModel<CollectionResult> newFacetableCollectionsModel(
    		final IDataProvider searchResultsDataProvider) {
        return new SortableListModel<CollectionResult>() {
            @Override
            protected List<CollectionResult> load(String orderByProperty, Boolean orderByAsc) {
                List<CollectionResult> collectionResults = new ArrayList<CollectionResult>();
                BaseSearchPage baseSearchPage = (BaseSearchPage) getPage();
                
                SimpleSearch simpleSearch = baseSearchPage.getSimpleSearch();
                boolean search = !simpleSearch.isEmpty();
                
//                if (StringUtils.isNotBlank(simpleSearch.getLuceneQuery(true))) {
                    RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
//                    FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
                    FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
                    SearchServices searchServices = ConstellioSpringUtils.getSearchServices();

                    List<RecordCollection> visibleCollections = visibleCollectionsModel.getObject();
                    Map<RecordCollection, List<RecordCollection>> federationCollections = new HashMap<RecordCollection, List<RecordCollection>>();
                    for (RecordCollection visibleCollection : visibleCollections) {
                        if (visibleCollection.isFederationOwner()) {
                            List<RecordCollection> includedCollections = federationServices.listIncludedCollections(visibleCollection);
                            List<RecordCollection> includedVisibleCollections = new ArrayList<RecordCollection>();
                            federationCollections.put(visibleCollection, includedVisibleCollections);
                            for (RecordCollection includedCollection : includedCollections) {
                                if (visibleCollections.contains(includedCollection)) {
                                    includedVisibleCollections.add(includedCollection);
                                }
                            }
                        }
                    }

                    Map<RecordCollection, Long> numFoundFederation = new HashMap<RecordCollection, Long>();
                    if (search && searchResultsDataProvider != null) {
                    	String collectionName = simpleSearch.getCollectionName();
                    	RecordCollection searchCollection = collectionServices.get(collectionName);
                    	long resultsCount = searchResultsDataProvider.size();
                    	numFoundFederation.put(searchCollection, resultsCount);
                    }
                    
//                    for (RecordCollection federationOwner : federationCollections.keySet()) {
//                        SimpleSearch clone = simpleSearch.clone();
//                        clone.setCollectionName(federationOwner.getName());
//                        clone.clearPages();
//                        
//                        List<String> customFieldFacets = new ArrayList<String>();
//                        CollectionFacet collectionIdFacet = federationOwner.getFieldFacet(IndexField.COLLECTION_ID_FIELD);
//                        if (collectionIdFacet == null) {
//                            customFieldFacets.add(IndexField.COLLECTION_ID_FIELD);
//                        }
//                        if (!federationCollections.get(federationOwner).isEmpty()) {
//                            ConstellioUser user = ConstellioSession.get().getUser();
//                            if (search) {
//                                QueryResponse queryResponse = facetServices.search(clone, 0, 1, true,
//                                        false, customFieldFacets, null, user);
//                                if (queryResponse != null) {
//                                    SolrDocumentList results = queryResponse.getResults();
//                                    Long numFound = results.getNumFound();
//                                    numFoundFederation.put(federationOwner, numFound);
//
//                                    FacetField facetField = queryResponse.getFacetField(IndexField.COLLECTION_ID_FIELD);
//                                    List<Count> facetCounts = facetField != null ? facetField.getValues() : null;
//                                    if (facetCounts != null) {
//                                        for (Count facetCount : facetCounts) {
//                                            Long includedCollectionId = new Long(facetCount.getName());
//                                            RecordCollection includedCollection = collectionServices.get(includedCollectionId);
//                                            if (includedCollection != null
//                                                && !includedCollection.equals(federationOwner)) {
//                                                Long includedCollectionNumFound = facetCount.getCount();
//                                                numFoundFederation.put(includedCollection, includedCollectionNumFound);
//                                            }
//                                        }
//                                        for (RecordCollection includedCollection : federationCollections.get(federationOwner)) {
//                                            if (includedCollection != null
//                                                && numFoundFederation.get(includedCollection) == null) {
//                                                numFoundFederation.put(includedCollection, 0L);
//                                            }
//                                        }
//                                    }
//                                }
//                            } else {
//                                for (RecordCollection includedCollection : federationCollections.get(federationOwner)) {
//                                    if (includedCollection != null) {
//                                        numFoundFederation.put(includedCollection, -1L);
//                                    }
//                                }
//                            }
//                        }
//                    }

                    for (RecordCollection visibleCollection : visibleCollections) {
                        Long numFound = numFoundFederation.get(visibleCollection);
                        if (numFound == null) {
                        	String originalCollectionName = simpleSearch.getCollectionName();
                        	String collectionName = visibleCollection.getName();
                            SimpleSearch clone = simpleSearch.clone();
                            clone.clearPages();
                            clone.setCollectionName(collectionName);
                            
                            if (!collectionName.equals(originalCollectionName)) {
                        		List<SearchableFacet> searchableFacets = SolrFacetUtils.getSearchableFacets(collectionName);
                        		Set<String> searchableFacetNames = new HashSet<String>();
                        		for (SearchableFacet searchableFacet : searchableFacets) {
									String facetName = searchableFacet.getName();
									searchableFacetNames.add(facetName);
								}
                            	List<SearchedFacet> searchedFacets = clone.getSearchedFacets();
                            	for (Iterator<SearchedFacet> it = searchedFacets.iterator(); it.hasNext();) {
									SearchedFacet searchedFacet = it.next();
									SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
									String facetName = searchableFacet.getName();
									if (!searchableFacetNames.contains(facetName)) {
										it.remove();
									}
								}
                            }
                            if (search) {
                                ConstellioUser user = ConstellioSession.get().getUser();
                                QueryResponse queryResponse = searchServices.search(clone, 0, 1, user);
                                if (queryResponse != null) {
                                    numFound = queryResponse.getResults().getNumFound();
                                } else {
                                    numFound = 0L;
                                }
                            } else {
                        		numFound = -1L;
                            }
                        }
                        // if (numFound > 0) {
                        collectionResults.add(new CollectionResult(visibleCollection.getId(), numFound,
                        visibleCollection.isOpenSearch()));
                        // }
                    }
                    // Collections.sort(collectionResults);
//                }
                return collectionResults;
            }
        };
    }

    @Override
    public boolean isVisible() {
        return facetableCollectionsModel.getObject().size() > 0;
    }

    @Override
    public void detachModels() {
        visibleCollectionsModel.detach();
        super.detachModels();
    }
    
    protected String getSelectedStyleClass() {
    	return "selected";
    }
    
    protected String getItemStyleClass(int index) {
    	return null;
    }
    
    protected String getCollectionTitle(RecordCollection collection, long numFound) {
        Locale displayLocale = collection.getDisplayLocale(getLocale());
        String numFoundFormatted = NumberFormatUtils.format(numFound, displayLocale);
        String collectionTitle = collection.getTitle(displayLocale);
        int depthInFederation = collection.getDepthInFederation();
        for (int i = 0; i < depthInFederation; i++) {
            collectionTitle = "&nbsp;&nbsp;" + collectionTitle;
        }
        return numFound != -1 ? collectionTitle + " (" + numFoundFormatted + ")" : collectionTitle;
    }
    
    protected static final class CollectionResult implements Serializable, Comparable<CollectionResult> {

        private Long collectionId;
        private Long numFound;
        private Boolean openSearch;

        public CollectionResult(Long collectionId, Long numFound, Boolean openSearch) {
            super();
            this.collectionId = collectionId;
            this.numFound = numFound;
            this.openSearch = openSearch;
        }

        @Override
        public int compareTo(CollectionResult o) {
            int result;
            if (!openSearch && o.openSearch) {
                result = -1;
            } else if (openSearch && !o.openSearch) {
                result = 1;
            } else {
                result = -numFound.compareTo(o.numFound);
            }
            return result;
        }

    }

}
