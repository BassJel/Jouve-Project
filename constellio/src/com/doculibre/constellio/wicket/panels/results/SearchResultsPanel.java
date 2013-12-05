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
package com.doculibre.constellio.wicket.panels.results;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.resource.ByteArrayResource;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.services.ConnectorInstanceServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.connector.ConnectorPropertyInheritanceResolver;
import com.doculibre.constellio.wicket.data.SearchResultsDataProvider;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.paging.ConstellioPagingNavigator;
import com.doculibre.constellio.wicket.panels.admin.connector.ConnectorListPanel;
import com.doculibre.constellio.wicket.panels.results.tagging.SearchResultTaggingPanel;

@SuppressWarnings("serial")
public class SearchResultsPanel extends Panel {

    protected static String DATA_VIEW_ID = "searchResults";

    private DataView dataView;

    public SearchResultsPanel(String id, final SearchResultsDataProvider dataProvider) {
        super(id);
        this.dataView = new DataView(DATA_VIEW_ID, dataProvider, dataProvider.getResultsPerPage()) {
            @Override
            protected void populateItem(Item item) {
                SolrDocument doc = (SolrDocument) item.getModelObject();

                RecordServices recordServices = ConstellioSpringUtils.getRecordServices();

                if (doc.getFieldValue(IndexField.RECORD_ID_FIELD) != null) {
                    Record record = recordServices.get(doc);
                    if (record != null) {
                        ConnectorInstance connectorInstance = record.getConnectorInstance();

                        final ReloadableEntityModel<ConnectorInstance> connectorInstanceModel = new ReloadableEntityModel<ConnectorInstance>(
                            connectorInstance);
                        ConnectorType connectorType = connectorInstance.getConnectorType();
                        ResourceReference imageResourceReference = new ResourceReference("connectorType_" + connectorType.getId()) {
                            @Override
                            protected Resource newResource() {
                                ConnectorInstance connectorInstance = (ConnectorInstance) connectorInstanceModel.getObject();
                                ConnectorType connectorType = connectorInstance.getConnectorType();
                                Resource imageResource;
                                byte[] iconBytes = connectorType.getIconFileContent();
                                // Convert resource path to absolute path relative to base package
                                if (iconBytes != null) {
                                    imageResource = new ByteArrayResource("image", iconBytes);
                                } else {
                                    imageResource = PackageResource.get(ConnectorListPanel.class,
                                        "default_icon.gif");
                                }
                                return imageResource;
                            }
                        };

                        item.add(new NonCachingImage("icon", imageResourceReference) {
                            @Override
                            public void detachModels() {
                                connectorInstanceModel.detach();
                                super.detachModels();
                            }

                            @Override
                            public boolean isVisible() {
                                boolean visible = super.isVisible();
                                if (visible) {
                                    SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils
                                        .getSearchInterfaceConfigServices();
                                    SearchInterfaceConfig searchInterfaceConfig = searchInterfaceConfigServices.get();
                                    visible = searchInterfaceConfig.isUseIconsInSearchResults();
                                }
                                return visible;
                            }
                        });
                    } else {
                        item.setVisible(false);
                    }
                } else {
                    item.add(new WebMarkupContainer("icon").setVisible(false));
                }

                item.add(getSearchResultPanel("searchResultPanel", item, dataProvider));
                item.add(new SearchResultTaggingPanel("taggingPanel", doc, dataProvider));
            }
        };
        add(dataView);
        int page = dataProvider.getSimpleSearch().getPage();
        int pageCount = dataView.getPageCount();
        dataView.setCurrentPage(page == -1 ? pageCount - 1 : page);

        ConstellioPagingNavigator pagingNavigator = new ConstellioPagingNavigator("resultsPN", dataView);
        add(pagingNavigator);

        add(new WebMarkupContainer("noResultsMessage") {
            @Override
            public boolean isVisible() {
                return dataProvider.size() == 0;
            }
        });
    }

    public DataView getDataView() {
        return dataView;
    }

    @SuppressWarnings("rawtypes")
	protected Panel getSearchResultPanel(String id, Item item, SearchResultsDataProvider dataProvider) {
        Panel searchResultPanel;
        SolrDocument doc = (SolrDocument) item.getModelObject();
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils
            .getConnectorInstanceServices();

        String collectionName = dataProvider.getSimpleSearch().getCollectionName();
        RecordCollection collection = collectionServices.get(collectionName);
        if (!collection.isOpenSearch()) {
            Long connectorInstanceId = new Long(doc.getFieldValue(IndexField.CONNECTOR_INSTANCE_ID_FIELD)
                .toString());
            ConnectorInstance connectorInstance = connectorInstanceServices.get(connectorInstanceId);
            Class[] constructorParamTypes = { String.class, SolrDocument.class,
                SearchResultsDataProvider.class };
            Object[] args = { id, doc, dataProvider };
            searchResultPanel = ConnectorPropertyInheritanceResolver.newInheritedClassPropertyInstance(
                connectorInstance, "searchResultPanelClassName", constructorParamTypes, args);
            if (searchResultPanel == null) {
                searchResultPanel = new DefaultSearchResultPanel(id, doc, dataProvider);
            }
        } else {
            searchResultPanel = new OpenSearchResultPanel(id, doc, dataProvider);
        }
        return searchResultPanel;
    };

}
