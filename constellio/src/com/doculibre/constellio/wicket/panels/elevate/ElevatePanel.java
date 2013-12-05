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
package com.doculibre.constellio.wicket.panels.elevate;

import javax.persistence.EntityManager;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.services.ElevateServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceUtils;
import com.doculibre.constellio.wicket.components.holders.ModalImgLinkHolder;
import com.doculibre.constellio.wicket.models.RecordModel;
import com.doculibre.constellio.wicket.pages.BaseConstellioPage;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;
import com.doculibre.constellio.wicket.panels.elevate.queries.EditRecordElevatedQueriesPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class ElevatePanel extends Panel {
    
    private RecordModel recordModel;
    
    private String collectionName;

	public ElevatePanel(String id, Record record, final SimpleSearch simpleSearch) {
		super(id);
		this.recordModel = new RecordModel(record);
		
		// Query without facets
		collectionName = simpleSearch.getCollectionName();
		
		Link elevateLink = new Link("elevateLink") {
			@Override
			public void onClick() {
				ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
                EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                if (!entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().begin();
                }
                Record record = recordModel.getObject();
                elevateServices.elevate(record, simpleSearch);
                entityManager.getTransaction().commit();

                PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                RequestCycle.get().setResponsePage(pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(simpleSearch));
			}
		};
		add(elevateLink);
		elevateLink.add(new WebMarkupContainer("elevateImg") {
			@Override
			public boolean isVisible() {
				ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
				Record record = recordModel.getObject();
				return !elevateServices.isElevated(record, simpleSearch);
			}
		});
		elevateLink.add(new WebMarkupContainer("elevatedImg") {
			@Override
			public boolean isVisible() {
				ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
                Record record = recordModel.getObject();
				return elevateServices.isElevated(record, simpleSearch);
			}
		});
		
		add(new ModalImgLinkHolder("elevatedQueriesLinkHolder") {
			@Override
			protected WebMarkupContainer newLink(String id) {
				return new AjaxLink(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						Record record = recordModel.getObject();
						ModalWindow elevatedQueriesModal = getModalWindow();
						elevatedQueriesModal.setContent(new EditRecordElevatedQueriesPanel(elevatedQueriesModal.getContentId(), record, simpleSearch));
						elevatedQueriesModal.show(target);
					}
				};
			}
			
			@Override
			protected Component newImg(String id) {
                Image image = new NonCachingImage(id, new ResourceReference(BaseConstellioPage.class, "images/ico_plus.gif"));
                return image;
			}

			@Override
			protected IModel getAltModel() {
				return new StringResourceModel("elevation", this, null);
			}
		});
		
		add(new Link("restoreLink") {
			@Override
			public void onClick() {
				ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				String collectionName = simpleSearch.getCollectionName();
				RecordCollection collection = collectionServices.get(collectionName);

    			SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
    			try {
                	ConstellioPersistenceUtils.beginTransaction();                
                    Record record = recordModel.getObject();
                    elevateServices.cancelElevation(record, simpleSearch);
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

                PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                RequestCycle.get().setResponsePage(pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(simpleSearch));
			}

			@Override
			public boolean isVisible() {
				ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
                Record record = recordModel.getObject();
				return elevateServices.isElevated(record, simpleSearch);
			}
		});
		
		add(new Link("excludeLink") {
			@Override
			public void onClick() {
				ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				RecordCollection collection = collectionServices.get(collectionName);

    			SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
    			try {
                	ConstellioPersistenceUtils.beginTransaction();                
                    Record record = recordModel.getObject();
                    elevateServices.exclude(record, collection);
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
		        recordModel.detach();
		        int attempts = 0;
		        while (!recordModel.getObject().isExcludedEffective()) {
		            recordModel.detach();
		            try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new WicketRuntimeException(e);
                    }
		            attempts++;
		            if (attempts > 10) {
		                // Give up waiting after one second
		                break;
		            }
		        }
		        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
		        RequestCycle.get().setResponsePage(pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(simpleSearch));
			}

			@Override
			public boolean isVisible() {
				ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
                Record record = recordModel.getObject();
				return !elevateServices.isElevated(record, simpleSearch);
			}
		});
	}

	@Override
	public boolean isVisible() {
		boolean visible = super.isVisible();
		if (visible) {
		    RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		    RecordCollection collection = collectionServices.get(collectionName);
		    ConstellioSession session = ConstellioSession.get();
		    if (!session.isSessionInvalidated() && session.isSignedIn()) {
			    ConstellioUser user = session.getUser();
				visible = user != null && user.hasCollaborationPermission(collection);
		    } else {
		    	visible = false;
		    }
		} 
		return visible;
	}

    @Override
    public void detachModels() {
        recordModel.detach();
        super.detachModels();
    }

}
