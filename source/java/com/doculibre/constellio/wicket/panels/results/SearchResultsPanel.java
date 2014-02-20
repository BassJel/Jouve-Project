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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.resource.ByteArrayResource;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.search.TransfertSearchResultsPlugin;
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
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class SearchResultsPanel extends Panel {

    protected static String DATA_VIEW_ID = "searchResults";

    private DataView dataView;
    
    private AjaxLink transfertLink;
    
    private WebMarkupContainer transfertPanel;
    
    private AjaxCheckBox allNoneCheckbox;
    
    private Label errorMessageLabel;
    
    private String errorMessage;
    
    private boolean transfertActive;
    
    private boolean allNone;
    
    public SearchResultsPanel(String id, final SearchResultsDataProvider dataProvider) {
        super(id);
        setOutputMarkupId(true);
        
        List<TransfertSearchResultsPlugin> plugins = PluginFactory.getPlugins(TransfertSearchResultsPlugin.class);
        
        this.transfertLink = new AjaxLink("transfertLink") {
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				transfertPanel.setVisible(true);
				transfertLink.setVisible(false);
				transfertActive = true;
				target.addComponent(SearchResultsPanel.this);
			}
		};
		transfertLink.setVisible(!plugins.isEmpty() && ConstellioSession.get().getUser() != null && ConstellioSpringUtils.getIntelliGIDServiceInfo() != null);
		add(transfertLink);
		
		this.transfertPanel = new WebMarkupContainer("transfertPanel");
		transfertPanel.setOutputMarkupId(true);
		transfertPanel.setVisible(false);
		add(transfertPanel);
		
		IModel allNoneModel = new Model() {
			
			@Override
			public Object getObject() {
				return allNone;
			}
			
			@Override
			public void setObject(Object object) {
				allNone = (Boolean) object;
				if (allNone) {
					Iterator it = dataProvider.iterator(0, dataProvider.size());
					while(it.hasNext()) {
						SolrDocument document = (SolrDocument) it.next();
						dataProvider.getSimpleSearch().getSelectedResults().add(new Long(document.getFieldValue(IndexField.RECORD_ID_FIELD).toString()));
					}
				} else {
					dataProvider.getSimpleSearch().getSelectedResults().clear();
				}
			}
			
		};
		
		allNoneCheckbox = new AjaxCheckBox("allNoneCheckbox", allNoneModel) {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.addComponent(SearchResultsPanel.this);
			}
		};
		allNoneCheckbox.setOutputMarkupId(true);
		transfertPanel.add(allNoneCheckbox);
		
		errorMessageLabel = new Label("errorMessageLabel", new PropertyModel(this, "errorMessage"));
		errorMessageLabel.setOutputMarkupId(true);
		transfertPanel.add(errorMessageLabel);
		
		transfertPanel.add(new ListView("transfertButtons", plugins) {
			
			@Override
			protected void populateItem(ListItem item) {
				final TransfertSearchResultsPlugin plugin = ((TransfertSearchResultsPlugin) item.getModelObject());
				AjaxLink link = new AjaxLink("launchTransfertLink") {
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						errorMessage = null;
						List<Record> records = new ArrayList<Record>();
						SimpleSearch search = dataProvider.getSimpleSearch();
						RecordCollection collection = ConstellioSpringUtils.getRecordCollectionServices().get(search.getCollectionName());
						for(Long recordId : search.getSelectedResults()) {
							records.add(ConstellioSpringUtils.getRecordServices().get(recordId, collection));
						}
						
						if (!records.isEmpty()) {
							doTransfert(plugin, target, records);
						}
						
					}

					private void doTransfert(
							final TransfertSearchResultsPlugin plugin,
							AjaxRequestTarget target, List<Record> records) {
						List<Record> transferedRecords = new ArrayList<Record>();
						List<String> ids = new ArrayList<String>();
						for(Record record : records) {
							try {
								ids.add(plugin.transfert(record));
								transferedRecords.add(record);
							} catch(Throwable t) {
								t.printStackTrace();
								errorMessage = t.getMessage();
								break;
							}
						}
						if (errorMessage != null) {
							try {
								for(int i = 0; i < ids.size(); i++) {
									plugin.cancel(transferedRecords.get(i), ids.get(i));
								}
							} catch(Throwable t) {
								t.printStackTrace();
							}
						} else {
							plugin.afterTransfert(transferedRecords, ids);
						}
						target.addComponent(errorMessageLabel);
					}
				};
				item.add(link);
				link.add(new Label("launchTransfertLabel", plugin.getLabelText()));
			}
		});
        
        this.dataView = new DataView(DATA_VIEW_ID, dataProvider, dataProvider.getResultsPerPage()) {
            @Override
            protected void populateItem(Item item) {
                SolrDocument doc = (SolrDocument) item.getModelObject();
                final Long id;
                RecordServices recordServices = ConstellioSpringUtils.getRecordServices();

                if (doc.getFieldValue(IndexField.RECORD_ID_FIELD) != null) {
                    Record record = recordServices.get(doc);
                    id = record.getId();
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
                    id = null;
                }

                item.add(getSearchResultPanel("searchResultPanel", item, dataProvider));
                item.add(new SearchResultTaggingPanel("taggingPanel", doc, dataProvider));
                
                newRecordCheckbox(dataProvider, item, id);
            }

			private void newRecordCheckbox(
					final SearchResultsDataProvider dataProvider, Item item,
					final Long id) {
				IModel isChecked = new Model() {
                	@Override
                	public Object getObject() {
                		return id != null && dataProvider.getSimpleSearch().getSelectedResults().contains(id);
                	}
                	
                	@Override
                	public void setObject(Object object) {
                		if (id != null) {
	                		Boolean state = (Boolean) object;
	                		dataProvider.getSimpleSearch().getSelectedResults().remove(id);
	                		if (state) {
	                			dataProvider.getSimpleSearch().getSelectedResults().add(id);
	                		} else {
	                			allNone = false;
	                		}
                		}
                	}
                };
                
                item.add(new AjaxCheckBox("checkedResults", isChecked) {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						target.addComponent(allNoneCheckbox);
					}
					
					@Override
					public boolean isVisible() {
						return transfertActive;
					}
					
				});
			}
        };
        add(dataView);
        dataView.setOutputMarkupId(true);
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
