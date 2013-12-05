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
package com.doculibre.constellio.wicket.panels.admin.crud;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.holders.ModalLinkHolder;
import com.doculibre.constellio.wicket.models.SortableListModel;
import com.doculibre.constellio.wicket.models.SortablePropertyListModel;

@SuppressWarnings("serial")
public abstract class SingleColumnCRUDPanel extends CRUDPanel<Object> {

	public SingleColumnCRUDPanel(String id) {
		super(id);
	}

	public SingleColumnCRUDPanel(String id, int rowsPerPage) {
        super(id, rowsPerPage);
    }

    @Override
    protected List<IColumn> getDataColumns() {
	    List<IColumn> dataColumns = new ArrayList<IColumn>();
        IColumn detailsColumn = new HeaderlessColumn() {
            @Override
            public void populateItem(Item cellItem, String componentId, final IModel rowItemModel) {
                Item rowItem = (Item) cellItem.findParent(Item.class);
                final int rowIndex = getFirstRowItemAbsoluteIndex() + rowItem.getIndex();
                cellItem.add(new ModalLinkHolder(componentId) {
                    @Override
                    public WebMarkupContainer newLink(String id) {
                        AbstractLink link;
                        if (isUseModalsDetails()) {
                            link = new AjaxLink(id) {
                                @Override
                                public void onClick(AjaxRequestTarget target) {
                                    ModalWindow detailsModal = getModalWindow();
                                    detailsModal.setInitialHeight(MODAL_HEIGHT);
                                    detailsModal.setInitialWidth(MODAL_WIDTH);
                                    detailsModal.setTitle(getLabelModel());
                                    onClickDetailsLink(rowItemModel, target, detailsModal, rowIndex);
                                }
                            };
                        } else {
                            link = new Link(id) {
                                @Override
                                public void onClick() {
                                    onClickDetailsLink(rowItemModel, null, null, rowIndex);
                                }
                            };
                        }
                        link.setEnabled(isDetailsLink(rowItemModel, rowIndex));
                        return link;
                    }

                    @Override
                    protected Component newLabel(String id, IModel labelModel) {
                        return createDetailsLabelComponent(id, rowItemModel, rowIndex);
                    }
                });
            }

			@Override
			public Component getHeader(String componentId) {
				Component header = createDetailsColumnHeader(componentId);
				if (header == null) {
					header = super.getHeader(componentId);
				}
				return header;
			}
        };
        dataColumns.add(detailsColumn);
        return dataColumns;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SortableListModel<Object> getSortableListModel() {
        return new SortablePropertyListModel<Object>(new LoadableDetachableModel() {
            @Override
            protected Object load() {
                IModel model = SingleColumnCRUDPanel.this.getModel();
                List<Object> modelObjects = (List<Object>) model.getObject();
                return modelObjects;
            }
        });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	protected void onClickDeleteLink(IModel entityModel, AjaxRequestTarget target, int index) {
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		BaseCRUDServices services = getServices();
		ConstellioEntity entity = (ConstellioEntity) entityModel.getObject();
		services.makeTransient(entity);
		entityManager.getTransaction().commit();
		target.addComponent(SingleColumnCRUDPanel.this);			
	}

    protected void onClickDetailsLink(IModel entityModel, AjaxRequestTarget target, ModalWindow detailsModal, int index) {
        String detailsContentId;
        if (detailsModal != null) {
            detailsContentId = detailsModal.getContentId();
        } else {
            detailsContentId = getId();
        }
        
        WebMarkupContainer detailsContent = createDetailsContent(detailsContentId, entityModel);
        if (detailsContent != null) {
            if (detailsModal != null) {
                detailsModal.setContent(detailsContent);
                detailsModal.show(target);
            } else {
                this.replaceWith(detailsContent);
            }
        }
    }
    
    protected Component createDetailsColumnHeader(String id) {
    	return null;
    }
    
    protected Component createDetailsLabelComponent(String id, IModel entityModel, int index) {
        String detailsLabel = getDetailsLabel(entityModel.getObject());
        return new Label(id, detailsLabel);
    }
    
    protected WebMarkupContainer createDetailsContent(String id, IModel entityModel) {
        return null;
    }
	
	protected boolean isDetailsLink(IModel entityModel, int index) {
		return false;
	}

    protected IModel getTitleModel() {
        return null;
    }
    
    protected boolean isUseModalsDetails() {
        return isUseModals();
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected boolean isDeleteLink(IModel rowItemModel, int index) {
		BaseCRUDServices services = getServices();
		ConstellioEntity entity = (ConstellioEntity) rowItemModel.getObject();
		return super.isDeleteLink(rowItemModel, index) && (services == null || services.isRemoveable(entity));
	}

    protected abstract String getDetailsLabel(Object entity);
	
	protected abstract BaseCRUDServices<? extends ConstellioEntity> getServices();

}
