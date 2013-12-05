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
package com.doculibre.constellio.wicket.panels.admin.collection;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.holders.ImgLinkHolder;
import com.doculibre.constellio.wicket.components.holders.LinkHolder;
import com.doculibre.constellio.wicket.pages.BaseConstellioPage;
import com.doculibre.constellio.wicket.pages.SearchFormPage;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;
import com.doculibre.constellio.wicket.panels.admin.tabs.AdminTopMenuPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class CollectionListPanel extends SingleColumnCRUDPanel {

    public CollectionListPanel(String id) {
        super(id);
        setModel(new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ConstellioUser currentUser = ConstellioSession.get().getUser();
                RecordCollectionServices collectionServices = ConstellioSpringUtils
                    .getRecordCollectionServices();
                List<RecordCollection> collections = collectionServices.list();
                for (Iterator<RecordCollection> it = collections.iterator(); it.hasNext();) {
                    RecordCollection collection = it.next();
                    if (!currentUser.hasCollaborationPermission(collection) && 
                            !currentUser.hasAdminPermission(collection)) {
                        it.remove();
                    }
                }
                return collections;
            }
        });
    }

    @Override
    protected boolean isAddLink() {
        ConstellioUser currentUser = ConstellioSession.get().getUser();
        return currentUser.isAdmin();
    }

    @Override
    protected boolean isDeleteLink(IModel rowItemModel, int index) {
        ConstellioUser currentUser = ConstellioSession.get().getUser();
        return currentUser.isAdmin();
    }

    @Override
    protected boolean isEditLink(IModel rowItemModel, int index) {
        ConstellioUser currentUser = ConstellioSession.get().getUser();
        return currentUser.isAdmin();
    }

    @Override
    protected List<IColumn> getDataColumns() {
        List<IColumn> dataColumns = super.getDataColumns();
        
        IColumn descriptionColumn = new PropertyColumn(new StringResourceModel("description", this, null),
            "description") {
            @Override
            protected IModel createLabelModel(final IModel embeddedModel) {
                return new LoadableDetachableModel() {
                    @Override
                    protected Object load() {
                        RecordCollection collection = (RecordCollection) embeddedModel.getObject();
                        return collection.getDescription(getLocale());
                    }
                };
            }

        };
        dataColumns.add(descriptionColumn);
        // Move up
        dataColumns.add(new HeaderlessColumn() {
            @Override
            public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                cellItem.add(new ImgLinkHolder(componentId) {
                    @Override
                    protected WebMarkupContainer newLink(String id) {
                        return new AjaxLink(id) {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
                                RecordCollection collection = (RecordCollection) rowModel.getObject();
                                List<RecordCollection> collections = collectionServices.list();
                                int index = collections.indexOf(collection);
                                // Cannot move higher!
                                if (index > 0) {
                                	RecordCollection previousCollection = collections.get(index - 1);
                                	Integer previousPosition = previousCollection.getPosition();
                                	Integer currentPosition = collection.getPosition();
                                	collection.setPosition(previousPosition);
                                	previousCollection.setPosition(currentPosition);
                                	
                                    EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                                    if (!entityManager.getTransaction().isActive()) {
                                        entityManager.getTransaction().begin();
                                    }
                                    collectionServices.makePersistent(collection, false);
                                    collectionServices.makePersistent(previousCollection, false);
                                    entityManager.getTransaction().commit();
                                    entityManager.clear();

                                    target.addComponent(findParent(CollectionListPanel.class));
                                }
                            }
                        };
                    }

                    @Override
                    protected Component newImg(String id) {
                        return new NonCachingImage(id, new ResourceReference(BaseConstellioPage.class, "images/up.png"));
                    }       
                });
            }
        });

        // Move down
        dataColumns.add(new HeaderlessColumn() {
            @Override
            public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                cellItem.add(new ImgLinkHolder(componentId) {
                    @Override
                    protected WebMarkupContainer newLink(String id) {
                        return new AjaxLink(id) {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
                                RecordCollection collection = (RecordCollection) rowModel.getObject();
                                List<RecordCollection> collections = collectionServices.list();
                                int index = collections.indexOf(collection);
                                // Cannot move lower!
                                if (index < collections.size() - 1) {
                                	RecordCollection nextCollection = collections.get(index + 1);
                                	Integer nextPosition = nextCollection.getPosition();
                                	Integer currentPosition = collection.getPosition();
                                	collection.setPosition(nextPosition);
                                	nextCollection.setPosition(currentPosition);
                                	
                                    EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                                    if (!entityManager.getTransaction().isActive()) {
                                        entityManager.getTransaction().begin();
                                    }
                                    collectionServices.makePersistent(collection, false);
                                    collectionServices.makePersistent(nextCollection, false);
                                    entityManager.getTransaction().commit();
                                    entityManager.clear();

                                    target.addComponent(findParent(CollectionListPanel.class));
                                }
                            }
                        };
                    }

                    @Override
                    protected Component newImg(String id) {
                        return new NonCachingImage(id, new ResourceReference(BaseConstellioPage.class, "images/down.png"));
                    }     
                });
            }
        });
        
        IColumn searchColumn = new HeaderlessColumn() {
			@Override
            public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
				cellItem.add(new LinkHolder(componentId) {
					@Override
					protected WebMarkupContainer newLink(String id) {
                        RecordCollection collection = (RecordCollection) rowModel.getObject();
						SimpleSearch simpleSearch = new SimpleSearch();
						simpleSearch.setCollectionName(collection.getName());
						PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
						return new BookmarkablePageLink(id, pageFactoryPlugin.getSearchFormPage(), SearchFormPage.getParameters(simpleSearch));
					}

					@Override
					protected IModel getLabelModel() {
						return new StringResourceModel("search", null);
					}
				});
			}
        };
        dataColumns.add(searchColumn);
        return dataColumns;
    }

    @Override
    protected WebMarkupContainer createAddContent(String id) {
        return new AddEditCollectionPanel(id, new RecordCollection());
    }

    @Override
    protected void onClickDetailsLink(IModel entityModel, AjaxRequestTarget target, ModalWindow detailsModal,
        int index) {
        RecordCollection collection = (RecordCollection) entityModel.getObject();
        if (!collection.isOpenSearch()) {
            AdminTopMenuPanel adminTabsPanel = (AdminTopMenuPanel) findParent(AdminTopMenuPanel.class);
            AdminCollectionPanel adminCollectionPanel = new AdminCollectionPanel(TabbedPanel.TAB_PANEL_ID, collection);
            adminTabsPanel.replaceTabContent(adminCollectionPanel);
            adminCollectionPanel.setSelectedTab(0);
            if (target != null) {
                target.addComponent(adminTabsPanel);
            }
        }
    }

    @Override
    protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
        RecordCollection docCollection = (RecordCollection) entityModel.getObject();
        return new AddEditCollectionPanel(id, docCollection);
    }

    @Override
    protected String getDetailsLabel(Object entity) {
        RecordCollection collection = (RecordCollection) entity;
        Locale displayLocale = collection.getDisplayLocale(getLocale());
        return collection.getTitle(displayLocale);
    }

    @Override
    protected boolean isDetailsLink(IModel entityModel, int index) {
        return true;
    }

    @Override
    protected BaseCRUDServices<RecordCollection> getServices() {
        return ConstellioSpringUtils.getRecordCollectionServices();
    }

    @Override
    protected boolean isUseModals() {
        return false;
    }

}
