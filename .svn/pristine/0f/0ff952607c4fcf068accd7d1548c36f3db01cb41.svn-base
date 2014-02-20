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
package com.doculibre.constellio.wicket.panels.admin.connectorTypeMeta;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.ConnectorTypeMetaMapping;
import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.services.ConnectorTypeMetaMappingServices;
import com.doculibre.constellio.services.FieldTypeServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.fieldType.AddEditFieldTypePanel;

@SuppressWarnings("serial")
public class AddEditConnectorTypeMetaMappingPanel extends SaveCancelFormPanel {

    private ReloadableEntityModel<ConnectorTypeMetaMapping> metaMappingModel;
    
    public AddEditConnectorTypeMetaMappingPanel(String id, ConnectorTypeMetaMapping metaMapping) {
        super(id, true);
        this.metaMappingModel = new ReloadableEntityModel<ConnectorTypeMetaMapping>(metaMapping);

        Form form = getForm();
        form.setModel(new CompoundPropertyModel(metaMappingModel));

        TextField metaNameField = new RequiredTextField("metaName");
        metaNameField.add(new StringValidator.MaximumLengthValidator(255));
        form.add(metaNameField);

        TextField indexFieldNameField = new RequiredTextField("indexFieldName");
        indexFieldNameField.add(new StringValidator.MaximumLengthValidator(255));
        form.add(indexFieldNameField);

        final CheckBox indexedCheckbox = new CheckBox("indexed");
        form.add(indexedCheckbox);

//        final CheckBox storedCheckbox = new CheckBox("stored");
//        form.add(storedCheckbox);

        final CheckBox multiValuedCheckbox = new CheckBox("multiValued");
        form.add(multiValuedCheckbox);

        final ModalWindow fieldTypeModal = new ModalWindow("fieldTypeModal");
        form.add(fieldTypeModal);
        fieldTypeModal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);

        IModel fieldTypesModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
                return fieldTypeServices.list();
            }
        };

        IChoiceRenderer fieldTypeRenderer = new ChoiceRenderer("name");

        final DropDownChoice fieldTypeField = new DropDownChoice("fieldType", fieldTypesModel,
            fieldTypeRenderer);
        form.add(fieldTypeField);
        fieldTypeField.setOutputMarkupId(true);

        AjaxLink addFieldTypeLink = new AjaxLink("addFieldTypeLink") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                AddEditFieldTypePanel addEditFieldTypePanel = new AddEditFieldTypePanel(fieldTypeModal
                    .getContentId(), new FieldType(), fieldTypeField);
                fieldTypeModal.setContent(addEditFieldTypePanel);
                fieldTypeModal.show(target);
            }
        };
        form.add(addFieldTypeLink);
    }

    @Override
    public void detachModels() {
        metaMappingModel.detach();
        super.detachModels();
    }

	@Override
	protected IModel getTitleModel() {
        return new LoadableDetachableModel() {
			@Override
			protected Object load() {
		        String titleKey = metaMappingModel.getObject().getId() == null ? "add" : "edit";
		        return new StringResourceModel(titleKey,AddEditConnectorTypeMetaMappingPanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
        ConnectorTypeMetaMapping metaMapping = metaMappingModel.getObject();

        ConnectorTypeMetaMappingListPanel listPanel = (ConnectorTypeMetaMappingListPanel) findParent(ConnectorTypeMetaMappingListPanel.class);
        ConnectorType connectorType = listPanel.getConnectorType();
        metaMapping.setConnectorType(connectorType);
        
        ConnectorTypeMetaMappingServices metaMappingServices = ConstellioSpringUtils
            .getConnectorTypeMetaMappingServices();
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        metaMappingServices.makePersistent(metaMapping);
        entityManager.getTransaction().commit();
	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		ModalWindow modalWindow = (ModalWindow) findParent(ModalWindow.class);
        AjaxPanel parent = (AjaxPanel) modalWindow.findParent(AjaxPanel.class);
        target.addComponent(parent);
	}

}
