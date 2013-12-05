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
package com.doculibre.constellio.wicket.panels.admin.facets;

import java.util.ArrayList;
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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.CollectionFacet;
import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.FacetServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.holders.ImgLinkHolder;
import com.doculibre.constellio.wicket.components.holders.ModalLinkHolder;
import com.doculibre.constellio.wicket.pages.BaseConstellioPage;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;
import com.doculibre.constellio.wicket.panels.admin.facets.values.EditFacetLabelledValuesPanel;

@SuppressWarnings("serial")
public class FacetListPanel extends SingleColumnCRUDPanel {

    public FacetListPanel(String id) {
        super(id);
        super.setModel(new LoadableDetachableModel() {
            @Override
            protected Object load() {
                AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                RecordCollection collection = collectionAdminPanel.getCollection();
                return new ArrayList<CollectionFacet>(collection.getCollectionFacets());
            }
        });
    }

    @Override
    protected List<IColumn> getDataColumns() {
	    List<IColumn> dataColumns = super.getDataColumns();
	    
	    dataColumns.add(new HeaderlessColumn() {
            @Override
            public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                cellItem.add(new ModalLinkHolder(componentId, new StringResourceModel("labelledValues", FacetListPanel.this, null)) {
                    @Override
                    protected WebMarkupContainer newLink(String id) {
                            return new AjaxLink(id) {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                ModalWindow labelledValuesModal = getModalWindow();
                                CollectionFacet facet = (CollectionFacet) rowModel.getObject();
                                EditFacetLabelledValuesPanel editLabelledValuesModal = 
                                    new EditFacetLabelledValuesPanel(labelledValuesModal.getContentId(), facet);
                                labelledValuesModal.setContent(editLabelledValuesModal);
                                labelledValuesModal.show(target);
                            }
    
                            @Override
                            public boolean isVisible() {
                                CollectionFacet facet = (CollectionFacet) rowModel.getObject();
                                return facet.isFieldFacet();
                            }
                        };
                    }
                });
            }
        });
        
        dataColumns.add(new HeaderlessColumn() {
            @Override
            public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                cellItem.add(new ModalLinkHolder(componentId, new StringResourceModel("queries", FacetListPanel.this, null)) {
                    @Override
                    protected WebMarkupContainer newLink(String id) {
                            return new AjaxLink(id) {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                ModalWindow queriesModal = getModalWindow();
                                CollectionFacet facet = (CollectionFacet) rowModel.getObject();
                                EditFacetLabelledValuesPanel editQueriesPanel = 
                                    new EditFacetLabelledValuesPanel(queriesModal.getContentId(), facet);
                                queriesModal.setContent(editQueriesPanel);
                                queriesModal.show(target);
                            }
    
                            @Override
                            public boolean isVisible() {
                                CollectionFacet facet = (CollectionFacet) rowModel.getObject();
                                return facet.isQueryFacet();
                            }
                        };
                    }       
                });
            }
        });
        
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
                                FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
                                CollectionFacet facet = (CollectionFacet) rowModel.getObject();
                                RecordCollection collection = facet.getRecordCollection();
    
                                List<CollectionFacet> collectionFacets = collection.getCollectionFacets();
                                int index = collectionFacets.indexOf(facet);
                                // Cannot move higher!
                                if (index > 0) {
                                    CollectionFacet previousFacet = collectionFacets.set(index - 1, facet);
                                    collectionFacets.set(index, previousFacet);
                                    EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                                    if (!entityManager.getTransaction().isActive()) {
                                        entityManager.getTransaction().begin();
                                    }
                                    facetServices.makePersistent(facet);
                                    facetServices.makePersistent(previousFacet);
                                    entityManager.getTransaction().commit();
                                    entityManager.clear();

                                    target.addComponent(findParent(AdminCollectionPanel.class));
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
                                FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
                                CollectionFacet facet = (CollectionFacet) rowModel.getObject();
                                RecordCollection collection = facet.getRecordCollection();

                                List<CollectionFacet> collectionFacets = collection.getCollectionFacets();
                                int index = collectionFacets.indexOf(facet);
                                // Cannot move lower!
                                if (index < collectionFacets.size() - 1) {
                                    CollectionFacet previousFacet = collectionFacets.set(index + 1, facet);
                                    collectionFacets.set(index, previousFacet);
                                    EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                                    if (!entityManager.getTransaction().isActive()) {
                                        entityManager.getTransaction().begin();
                                    }
                                    facetServices.makePersistent(facet);
                                    facetServices.makePersistent(previousFacet);
                                    entityManager.getTransaction().commit();
                                    entityManager.clear();

                                    target.addComponent(findParent(AdminCollectionPanel.class));
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
	    
	    return dataColumns;
    }

    @Override
    protected WebMarkupContainer createAddContent(String id) {
        return new AddEditFacetPanel(id, new CollectionFacet());
    }

    @Override
    protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
        CollectionFacet facet = (CollectionFacet) entityModel.getObject();
        return new AddEditFacetPanel(id, facet);
    }

    @Override
    protected String getDetailsLabel(Object entity) {
        CollectionFacet facet = (CollectionFacet) entity;
        RecordCollection collection = facet.getRecordCollection();
        Locale displayLocale = collection.getDisplayLocale(getLocale());
        return facet.getName(displayLocale);
    }

    @Override
    protected BaseCRUDServices<? extends ConstellioEntity> getServices() {
        return ConstellioSpringUtils.getFacetServices();
    }

}
