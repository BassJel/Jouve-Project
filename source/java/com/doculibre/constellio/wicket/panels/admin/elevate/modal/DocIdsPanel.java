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
package com.doculibre.constellio.wicket.panels.admin.elevate.modal;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.ElevateServices;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceUtils;
import com.doculibre.constellio.wicket.models.RecordModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.utils.WicketResourceUtils;

@SuppressWarnings("serial")
public class DocIdsPanel extends AjaxPanel {
	
	final IModel docIdsModel;

	public DocIdsPanel(String id, final String queryText, final boolean elevatedDocs) {
		super(id);
		
		docIdsModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				List<String> docIds = new ArrayList<String>();
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
				if (elevatedDocs) {
					SimpleSearch querySimpleSearch = elevateServices.toSimpleSearch(queryText);
					docIds = elevateServices.getElevatedDocIds(querySimpleSearch);
				} else {
					docIds = elevateServices.getExcludedDocIds(collection);
				}
				return docIds;
			}
		};
		
		add(new ListView("docIds", docIdsModel) {
			@Override
			protected void populateItem(ListItem item) {
				final String docId = (String) item.getModelObject();

                AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                RecordCollection collection = collectionAdminPanel.getCollection();
				RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
				Record record = recordServices.get(docId, collection);
				if (record == null) {
			        FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
			        if (collection.isFederationOwner()) {
			            List<RecordCollection> includedCollections = federationServices.listIncludedCollections(collection);
			            for (RecordCollection includedCollection : includedCollections) {
			                record = recordServices.get(docId, includedCollection);
			                if (record != null) {
			                    break;
			                }
			            }
			        }
			        if (record == null && collection.isIncludedInFederation()) {
			            List<RecordCollection> ownerCollections = federationServices.listOwnerCollections(collection);
			            for (RecordCollection ownerCollection : ownerCollections) {
                            record = recordServices.get(docId, ownerCollection);
                            if (record != null) {
                                break;
                            }
			            }
			        }
				}
				if (record != null) {
	                final RecordModel recordModel = new RecordModel(record);
	                String displayURL = record.getDisplayUrl();
	                item.add(new Label("docId", displayURL));
	                
	                WebMarkupContainer deleteLink = new AjaxLink("deleteLink") {
	                    @Override
	                    public void onClick(AjaxRequestTarget target) {
	                        AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
	                        RecordCollection collection = collectionAdminPanel.getCollection();
	                        Record record = recordModel.getObject();
	                        
	                        ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();

	            			SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
	            			try {
	                        	ConstellioPersistenceUtils.beginTransaction();                
		                        if (elevatedDocs) {
		        					SimpleSearch querySimpleSearch = elevateServices.toSimpleSearch(queryText);
		                            elevateServices.cancelElevation(record, querySimpleSearch);
		                        } else {
		                            elevateServices.cancelExclusion(record, collection);
		                        }
	                            try {
									solrServer.commit();
								} catch (Throwable t) {
									try {
										solrServer.rollback();
									} catch (Exception e) {
										throw new RuntimeException(t);
									}
								}
	            			} finally {
	            				ConstellioPersistenceUtils.finishTransaction(false);
	            			}

	                        Component parent;
	                        if (elevatedDocs) {
	        					SimpleSearch querySimpleSearch = elevateServices.toSimpleSearch(queryText);
	                            boolean empty = elevateServices.getElevatedDocIds(querySimpleSearch).isEmpty();
	                            if (empty) {
	                            	ModalWindow.closeCurrent(target);
		                        	parent = null;
	                            } else {
	                            	parent = WicketResourceUtils.findOutputMarkupIdParent(DocIdsPanel.this);
	                            }
	                        } else {
                            	parent = WicketResourceUtils.findOutputMarkupIdParent(DocIdsPanel.this);
	                        }
	                        if (parent != null) {
		                        target.addComponent(parent);
	                        }
	                    }

	                    @Override
	                    protected IAjaxCallDecorator getAjaxCallDecorator() {
	                        return new AjaxCallDecorator() {
	                            @Override
	                            public CharSequence decorateScript(CharSequence script) {
	                                String confirmMsg = getLocalizer().getString("confirmDelete", DocIdsPanel.this);
	                                return "if (confirm('" + confirmMsg + "')) {" + script + "} else { return false; }";
	                            }
	                        };
	                    }

                        @Override
                        public void detachModels() {
                            recordModel.detach();
                            super.detachModels();
                        }
	                };
	                item.add(deleteLink);
				} else {
				    item.setVisible(false);
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isVisible() {
		List<String> docIds = (List<String>) docIdsModel.getObject();
		return !docIds.isEmpty();
	}

}
