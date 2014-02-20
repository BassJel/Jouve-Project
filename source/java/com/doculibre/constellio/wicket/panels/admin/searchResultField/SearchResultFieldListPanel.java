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
package com.doculibre.constellio.wicket.panels.admin.searchResultField;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchResultField;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.SearchResultFieldServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.holders.ImgLinkHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.pages.BaseConstellioPage;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;

@SuppressWarnings("serial")
public class SearchResultFieldListPanel extends SingleColumnCRUDPanel {
    
    private ReloadableEntityModel<RecordCollection> collectionModel;

	public SearchResultFieldListPanel(String id, RecordCollection collection) {
		super(id);
		this.collectionModel = new ReloadableEntityModel<RecordCollection>(collection);
		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
			    RecordCollection collection = collectionModel.getObject();
			    return collection.getSearchResultFields();
			}
		});
	}

    @Override
    protected List<IColumn> getDataColumns() {
        List<IColumn> dataColumns = super.getDataColumns();
        
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
                                SearchResultFieldServices searchResultFieldServices = ConstellioSpringUtils.getSearchResultFieldServices();
                                SearchResultField searchResultField = (SearchResultField) rowModel.getObject();
                                RecordCollection collection = searchResultField.getRecordCollection();
    
                                List<SearchResultField> searchResultFields = collection.getSearchResultFields();
                                int index = searchResultFields.indexOf(searchResultField);
                                // Cannot move higher!
                                if (index > 0) {
                                    SearchResultField previousSearchResultField = searchResultFields.set(index - 1, searchResultField);
                                    searchResultFields.set(index, previousSearchResultField);
                                    EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                                    if (!entityManager.getTransaction().isActive()) {
                                        entityManager.getTransaction().begin();
                                    }
                                    searchResultFieldServices.makePersistent(searchResultField);
                                    searchResultFieldServices.makePersistent(previousSearchResultField);
                                    entityManager.getTransaction().commit();
                                    entityManager.clear();

                                    target.addComponent(findParent(AdminSearchResultFieldsPanel.class));
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
                                SearchResultFieldServices searchResultFieldServices = ConstellioSpringUtils.getSearchResultFieldServices();
                                SearchResultField searchResultField = (SearchResultField) rowModel.getObject();
                                RecordCollection collection = searchResultField.getRecordCollection();

                                List<SearchResultField> searchResultFields = collection.getSearchResultFields();
                                int index = searchResultFields.indexOf(searchResultField);
                                // Cannot move lower!
                                if (index < searchResultFields.size() - 1) {
                                    SearchResultField previousSearchResultField = searchResultFields.set(index + 1, searchResultField);
                                    searchResultFields.set(index, previousSearchResultField);
                                    EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                                    if (!entityManager.getTransaction().isActive()) {
                                        entityManager.getTransaction().begin();
                                    }
                                    searchResultFieldServices.makePersistent(searchResultField);
                                    searchResultFieldServices.makePersistent(previousSearchResultField);
                                    entityManager.getTransaction().commit();
                                    entityManager.clear();

                                    target.addComponent(findParent(AdminSearchResultFieldsPanel.class));
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
		return new AddSearchResultFieldPanel(id, new SearchResultField());
	}

    @Override
    protected boolean isEditColumn() {
        return false;
    }

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
	    return null;
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		SearchResultField searchResultField = (SearchResultField) entity;
		IndexField indexField = searchResultField.getIndexField();
		return indexField.getName();
	}

    @Override
    public void detachModels() {
        collectionModel.detach();
        super.detachModels();
    }

	@Override
	protected BaseCRUDServices<SearchResultField> getServices() {
		return ConstellioSpringUtils.getSearchResultFieldServices();
	}
	
	public RecordCollection getCollection() {
	    return collectionModel.getObject();
	}

}
