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
package com.doculibre.constellio.wicket.panels.search;

import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.wicket.components.holders.LinkHolder;
import com.doculibre.constellio.wicket.components.sort.AutoHidePagingNavigator;
import com.doculibre.constellio.wicket.components.sort.SortableListDataProvider;
import com.doculibre.constellio.wicket.components.sort.ULAutoHidePagingNavigator;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.models.SortableListModel;
import com.doculibre.constellio.wicket.models.VisibleCollectionsModel;
import com.doculibre.constellio.wicket.pages.BaseSearchPage;
import com.doculibre.constellio.wicket.pages.SearchFormPage;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;
import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public class SearchFormCollectionPanel extends AjaxPanel {

    private SortableListModel<RecordCollection> collectionsModel;

    public SearchFormCollectionPanel(String id) {
        super(id);

        int itemsPerPage = 10;

        collectionsModel = new VisibleCollectionsModel();
        IDataProvider dataProvider = new SortableListDataProvider(collectionsModel);
        DataView dataView = new DataView("collections", dataProvider, itemsPerPage) {
            @Override
            protected void populateItem(Item item) {
                RecordCollection collection = (RecordCollection) item.getModelObject();
                final ReloadableEntityModel<RecordCollection> collectionModel = new ReloadableEntityModel<RecordCollection>(
                    collection);
                IModel labelModel = new LoadableDetachableModel() {
                    @Override
                    protected Object load() {
                        RecordCollection collection = collectionModel.getObject();
                        Locale displayLocale = collection.getDisplayLocale(getLocale());
                        String collectionTitle = collection.getTitle(displayLocale);
                        int depthInFederation = collection.getDepthInFederation();
                        for (int i = 0; i < depthInFederation; i++) {
                            collectionTitle = "&nbsp;&nbsp;" + collectionTitle;
                        }
                        return collectionTitle;
                    }
                };
                item.add(new LinkHolder("collectionLinkHolder", labelModel) {
                    @Override
                    protected WebMarkupContainer newLink(String id) {
                        RecordCollection collection = collectionModel.getObject();
                        BaseSearchPage baseSearchPage = (BaseSearchPage) SearchFormCollectionPanel.this.getPage();
                        SimpleSearch clone = baseSearchPage.getSimpleSearch().clone();
                        clone.setCollectionName(collection.getName());

                        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                        Class<? extends BaseSearchPage> searchPageClass;
                        PageParameters searchPageParams;
                        if (pageFactoryPlugin.isSearchResultsPage(baseSearchPage)) {
                            searchPageClass = pageFactoryPlugin.getSearchResultsPage();
                            searchPageParams = SearchResultsPage.getParameters(clone);
                        } else {
                            searchPageClass = pageFactoryPlugin.getSearchFormPage();
                            searchPageParams = SearchFormPage.getParameters(clone);
                        }
                        Link link = new BookmarkablePageLink(id, searchPageClass, searchPageParams);
                        link.add(new AttributeModifier("class", true, new LoadableDetachableModel() {
                            @Override
                            protected Object load() {
                                RecordCollection collection = collectionModel.getObject();
                                BaseSearchPage baseSearchPage = (BaseSearchPage) getPage();
                                String collectionName = baseSearchPage.getSimpleSearch().getCollectionName();
                                return collection.getName().equals(collectionName) ? "selected" : null;
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

    @Override
    public boolean isVisible() {
        return collectionsModel.getObject().size() > 1;
    }

}
