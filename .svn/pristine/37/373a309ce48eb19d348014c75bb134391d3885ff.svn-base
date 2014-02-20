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

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import com.doculibre.constellio.wicket.components.holders.ImgLinkHolder;
import com.doculibre.constellio.wicket.components.holders.ModalImgLinkHolder;
import com.doculibre.constellio.wicket.pages.BaseConstellioPage;

@SuppressWarnings("serial")
public abstract class CRUDPanel<T> extends DataPanel<T> {
    
    public static int MODAL_HEIGHT = 500;
    public int MODAL_WIDTH = 595;
    public static String CSS_MODAL = ModalWindow.CSS_CLASS_GRAY;
    
    private AbstractLink addLink;
    
    private ModalWindow addModal;

    public CRUDPanel(String id, int rowsPerPage) {
        super(id, rowsPerPage);
        initComponents();
    }

    public CRUDPanel(String id) {
        super(id);
        initComponents();
    }
    
    private void initComponents() {
        if (isUseModalsAddEdit()) {
            addLink = new AjaxLink("addLink") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    String addMsg = getLocalizer().getString("add", CRUDPanel.this);
                    addModal.setTitle(addMsg);
                    
                    WebMarkupContainer addContent = createAddContent(addModal.getContentId());
                    addModal.setContent(addContent);
                    addModal.show(target);
                }

                @Override
                public boolean isVisible() {
                    return super.isVisible() && isAddLink();
                }
            };
        } else {
            addLink = new Link("addLink") {
                @Override
                public void onClick() {
                    WebMarkupContainer addContent = createAddContent(CRUDPanel.this.getId());
                    CRUDPanel.this.replaceWith(addContent);
                }

                @Override
                public boolean isVisible() {
                    return super.isVisible() && isAddLink();
                }
            };
        }
        add(addLink);
        
        addModal = new ModalWindow("addModal");
        addModal.setInitialHeight(MODAL_HEIGHT);
        addModal.setInitialWidth(MODAL_WIDTH);
        addModal.setCssClassName(CSS_MODAL);
        add(addModal);
    }

    @Override
    protected List<IColumn> getColumns() {
        List<IColumn> columns = new ArrayList<IColumn>();
        List<IColumn> dataColumns = getDataColumns();
        columns.addAll(dataColumns);
        if (isEditColumn()) {
            IColumn editLinkColumn = getEditLinkColumn();
            if (editLinkColumn != null) {
                columns.add(editLinkColumn);
            }
        }
        if (isDeleteColumn()) {
            IColumn deleteLinkColumn = getDeleteLinkColumn();
            if (deleteLinkColumn != null) {
                columns.add(deleteLinkColumn);
            }
        }
        return columns;
    }
    
    protected IColumn getEditLinkColumn() {
        return new HeaderlessColumn() {
            @Override
            public void populateItem(Item cellItem, String componentId, final IModel rowItemModel) {
                Item rowItem = (Item) cellItem.findParent(Item.class);
                final int rowIndex = getFirstRowItemAbsoluteIndex() + rowItem.getIndex();
                cellItem.add(new ModalImgLinkHolder(componentId) {
                    @Override
                    public WebMarkupContainer newLink(String id) {
                        if (isUseModalsAddEdit()) {  
                            return new AjaxLink(id) {
                                @Override
                                public void onClick(AjaxRequestTarget target) {
                                    ModalWindow editModal = getModalWindow();
                                    editModal.setInitialHeight(MODAL_HEIGHT);
                                    editModal.setInitialWidth(MODAL_WIDTH);
                                    String editMsg = getLocalizer().getString("edit", CRUDPanel.this);
                                    editModal.setTitle(editMsg);
                                    
                                    WebMarkupContainer editContent = 
                                        createEditContent(editModal.getContentId(), rowItemModel, rowIndex);
                                    editModal.setContent(editContent);
                                    editModal.show(target);
                                }

                                @Override
                                public boolean isVisible() {
                                    return super.isVisible() && isEditLink(rowItemModel, rowIndex);
                                }
                            };
                        } else {  
                            return new Link(id) {
                                @Override
                                public void onClick() {
                                    WebMarkupContainer editContent = createEditContent(CRUDPanel.this.getId(), rowItemModel, rowIndex);
                                    CRUDPanel.this.replaceWith(editContent);
                                }

                                @Override
                                public boolean isVisible() {
                                    return super.isVisible() && isEditLink(rowItemModel, rowIndex);
                                }
                            };
                        }  
                    }

                    @Override
                    protected Component newImg(String id) {
                        return new NonCachingImage(id, new ResourceReference(BaseConstellioPage.class, "images/ico_crayon.png"));
                    }
                });
            }
            
            @Override
            public String getCssClass() {
                return "aligncenter width50px";
            }
        };
    }

    protected IColumn getDeleteLinkColumn() {
        return new HeaderlessColumn() {
            @Override
            public void populateItem(Item cellItem, String componentId, final IModel rowItemModel) {
                Item rowItem = (Item) cellItem.findParent(Item.class);
                final int rowIndex = getFirstRowItemAbsoluteIndex() + rowItem.getIndex();
                cellItem.add(new ImgLinkHolder(componentId) {
                    @Override
                    public WebMarkupContainer newLink(String id) {
                        return createDeleteLink(id, rowItemModel, rowIndex);
                    }

                    @Override
                    protected Component newImg(String id) {
                        return new NonCachingImage(id, new ResourceReference(BaseConstellioPage.class, "images/ico_poubelle.png"));
                    }
                });
            }
            
            @Override
            public String getCssClass() {
                return "aligncenter width50px";
            }
        };
    }

    protected WebMarkupContainer createDeleteLink(String id, final IModel rowItemModel, final int index) {
        return new AjaxLink(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                onClickDeleteLink(rowItemModel, target, index);
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new AjaxCallDecorator() {
                    @Override
                    public CharSequence decorateScript(CharSequence script) {
                        String confirmMsg = getLocalizer().getString("confirmDelete", CRUDPanel.this);
                        return "if (confirm('" + confirmMsg + "')) {" + script + "} else { return false; }";
                    }
                };
            }

            @Override
            public boolean isVisible() {
                return super.isVisible() && isDeleteLink(rowItemModel, index);
            }
        };
    }
    
    protected boolean isUseModals() {
        return true;
    }
    
    protected boolean isUseModalsAddEdit() {
        return isUseModals();
    }
    
    protected boolean isAddLink() {
        return true;
    }
    
    protected boolean isEditColumn() {
        return true;
    }
    
    protected boolean isDeleteColumn() {
        return true;
    }
    
    protected boolean isEditLink(IModel rowItemModel, int index) {
        return true;
    }
    
    protected boolean isDeleteLink(IModel rowItemModel, int index) {
        return true;
    }
    
    protected abstract List<IColumn> getDataColumns();
    
    protected abstract WebMarkupContainer createAddContent(String id);
    
    protected abstract WebMarkupContainer createEditContent(String id, IModel rowItemModel, int index);
    
    protected abstract void onClickDeleteLink(IModel rowItemModel, AjaxRequestTarget target, int index);
    
}
