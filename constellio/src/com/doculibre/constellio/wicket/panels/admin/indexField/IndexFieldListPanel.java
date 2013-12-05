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
package com.doculibre.constellio.wicket.panels.admin.indexField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.holders.CheckBoxHolder;
import com.doculibre.constellio.wicket.components.holders.ModalLinkHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.models.admin.AdminCollectionIndexFieldsModel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;
import com.doculibre.constellio.wicket.panels.admin.indexField.values.EditIndexFieldLabelledValuesPanel;

@SuppressWarnings("serial")
public class IndexFieldListPanel extends SingleColumnCRUDPanel {

    public IndexFieldListPanel(String id) {
        super(id, 10);
        setModel(new AdminCollectionIndexFieldsModel(this));
    }

    @Override
    protected List<IColumn> getDataColumns() {
        List<IColumn> dataColumns = new ArrayList<IColumn>();

        AjaxOnUpdateHandler updateIndexFieldHandler = new AjaxOnUpdateHandler() {
            @Override
            public void onUpdate(ReloadableEntityModel<IndexField> entityModel, AjaxRequestTarget target) {
                updateIndexField(entityModel.getObject(), target);
            }
        };

        dataColumns.add(getAjaxEditableLabelColumn("name"));
        dataColumns.add(getAjaxCheckboxColumn("indexed", updateIndexFieldHandler));
        dataColumns.add(getAjaxCheckboxColumn("multiValued", updateIndexFieldHandler));
        
        dataColumns.add(new HeaderlessColumn() {
            @Override
            public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                cellItem.add(new ModalLinkHolder(componentId, new StringResourceModel("labelledValues", IndexFieldListPanel.this, null)) {
                    @Override
                    protected WebMarkupContainer newLink(String id) { 
                            return new AjaxLink(id) {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                ModalWindow labelledValuesModal = getModalWindow();
                                IndexField indexField = (IndexField) rowModel.getObject();
                                EditIndexFieldLabelledValuesPanel editLabelledValuesModal = 
                                    new EditIndexFieldLabelledValuesPanel(labelledValuesModal.getContentId(), indexField);
                                labelledValuesModal.setContent(editLabelledValuesModal);
                                labelledValuesModal.show(target);
                            }
                        };
                    }
                });
            }
        });

        return dataColumns;
    }

    private IColumn getAjaxEditableLabelColumn(final String propertyName) {
        return new AbstractColumn(new StringResourceModel(propertyName, this, null)) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                IndexField indexField = (IndexField) rowModel.getObject();

                final ReloadableEntityModel<IndexField> entityModel = new ReloadableEntityModel<IndexField>(
                    indexField);
                cellItem.setModel(new CompoundPropertyModel(entityModel));

                cellItem
                    .add(new AjaxEditableLabel(componentId, new PropertyModel(entityModel, propertyName)) {
                        @Override
                        protected void onSubmit(AjaxRequestTarget target) {
                            updateIndexField(entityModel.getObject(), target);
                            super.onSubmit(target);
                        }

                        @Override
                        public void detachModels() {
                            entityModel.detach();
                            super.detachModels();
                        }

                        @Override
                        public boolean isEnabled() {
                            IndexField indexField = entityModel.getObject();
                            return super.isEnabled() && !indexField.isInternalField();
                        }
                    });
            }
        };
    }

    private IColumn getAjaxCheckboxColumn(final String propertyName,
        final AjaxOnUpdateHandler ajaxOnUpdateHandler) {
        return new AbstractColumn(new StringResourceModel(propertyName, this, null)) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                IndexField indexField = (IndexField) rowModel.getObject();

                final ReloadableEntityModel<IndexField> entityModel = new ReloadableEntityModel<IndexField>(
                    indexField);
                cellItem.setModel(new CompoundPropertyModel(entityModel));

                cellItem.add(new CheckBoxHolder(componentId) {
                    @Override
                    protected WebMarkupContainer newInput(String id) {
                        return new AjaxCheckBox(id, new PropertyModel(entityModel, propertyName)) {
                            @Override
                            protected void onUpdate(AjaxRequestTarget target) {
                                ajaxOnUpdateHandler.onUpdate(entityModel, target);
                            }

                            @Override
                            public void detachModels() {
                                entityModel.detach();
                                super.detachModels();
                            }

                            @Override
                            public boolean isEnabled() {
                                IndexField indexField = entityModel.getObject();
                                return super.isEnabled() && !indexField.isInternalField();
                            }
                        };
                    }
                });
            }
        };
    }

    private void updateIndexField(IndexField indexField, AjaxRequestTarget target) {
        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        indexFieldServices.makePersistent(indexField);
        entityManager.getTransaction().commit();

        target.addComponent(findParent(AdminIndexFieldsPanel.class));
    }

    @Override
    protected BaseCRUDServices<? extends ConstellioEntity> getServices() {
        return ConstellioSpringUtils.getIndexFieldServices();
    }

    @Override
    protected WebMarkupContainer createAddContent(String id) {
        IndexField indexField = new IndexField();
        return new AddEditIndexFieldPanel(id, indexField);
    }

    @Override
    protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
        IndexField indexField = (IndexField) entityModel.getObject();
        return new AddEditIndexFieldPanel(id, indexField);
    }

    @Override
    protected String getDetailsLabel(Object entity) {
        return null;
    }

    @Override
    protected void onClickDeleteLink(IModel entityModel, AjaxRequestTarget target, int index) {
        IndexField indexField = (IndexField) entityModel.getObject();
        RecordCollection collection = indexField.getRecordCollection();
        collection.getIndexFields().remove(indexField);

        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();

        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        indexFieldServices.makeTransient(indexField);
        entityManager.getTransaction().commit();
        target.addComponent(findParent(AdminIndexFieldsPanel.class));
    }

    @Override
    protected boolean isEditLink(IModel rowItemModel, int index) {
        return true;
    }

    private static interface AjaxOnUpdateHandler extends Serializable {

        void onUpdate(ReloadableEntityModel<IndexField> entityModel, AjaxRequestTarget target);

    }

}
