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
package com.doculibre.constellio.wicket.panels.admin.indexField.meta;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.models.EntityModel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.indexField.dto.ConnectorInstanceMetaDTO;

@SuppressWarnings("serial")
public class AddMetaPanel extends Panel {
	
	private EntityModel<ConnectorInstance> connectorInstanceModel = new EntityModel<ConnectorInstance>();
	private EntityModel<ConnectorInstanceMeta> metaModel = new EntityModel<ConnectorInstanceMeta>();

	public AddMetaPanel(String id) {
		super(id);

		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);
		
		Form form = new Form("form");
		add(form);
		form.add(new SetFocusBehavior(form));

        String titleKey = "add";
        IModel titleModel = new StringResourceModel(titleKey, this, null);
        form.add(new Label("title", titleModel));
		
		IModel connectorInstancesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				return new ArrayList<ConnectorInstance>(collection.getConnectorInstances());
			}
		};
		
		IModel connectorInstanceMetasModel = new LoadableDetachableModel() {
			@SuppressWarnings("unchecked")
			@Override
			protected Object load() {
				ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
				List<ConnectorInstanceMeta> connectorInstanceMetas = new ArrayList<ConnectorInstanceMeta>();
				if (connectorInstance != null) {
					MetaListPanel metaListPanel = 
						(MetaListPanel) findParent(MetaListPanel.class);
					List<ConnectorInstanceMetaDTO> metas = (List<ConnectorInstanceMetaDTO>) metaListPanel.getModelObject();
					for (ConnectorInstanceMeta meta : connectorInstance.getConnectorInstanceMetas()) {
						if (!hasMeta(meta, metas)) {
							connectorInstanceMetas.add(meta);
						}
					}
				}
				return connectorInstanceMetas;
			}
		};

		IChoiceRenderer connectorInstanceRenderer = new ChoiceRenderer("displayName");
		IChoiceRenderer metaRenderer = new ChoiceRenderer("name");
		
		final DropDownChoice connectorInstanceMeta = new DropDownChoice("connectorInstanceMeta", metaModel, connectorInstanceMetasModel, metaRenderer);
		connectorInstanceMeta.setRequired(true);
		connectorInstanceMeta.setOutputMarkupId(true);
		form.add(connectorInstanceMeta);
		
		DropDownChoice connectorInstance = new DropDownChoice("connectorInstance", connectorInstanceModel, connectorInstancesModel, connectorInstanceRenderer);
		connectorInstance.setRequired(true);
		connectorInstance.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.addComponent(connectorInstanceMeta);
			}
		});
		form.add(connectorInstance);

		AjaxButton submitButton = new AjaxButton("submitButton") {
			@SuppressWarnings("unchecked")
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				ConnectorInstanceMeta meta = metaModel.getObject();
				
				MetaListPanel metaListPanel = 
					(MetaListPanel) findParent(MetaListPanel.class);
				
				List<ConnectorInstanceMetaDTO> metas = (List<ConnectorInstanceMetaDTO>) metaListPanel.getModelObject();
				metas.add(new ConnectorInstanceMetaDTO(meta));
				
				ModalWindow modalWindow = (ModalWindow) findParent(ModalWindow.class);
				modalWindow.close(target);

				target.addComponent(metaListPanel);
			}

			@Override
			protected void onError(final AjaxRequestTarget target, Form form) {
				target.addComponent(feedback);
			}
		};
		form.add(submitButton);

		Button cancelButton = new AjaxButton("cancelButton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {
				ModalWindow modalWindow = (ModalWindow) findParent(ModalWindow.class);
				modalWindow.close(target);
			}
		}.setDefaultFormProcessing(false);
		form.add(cancelButton);
	}

	@Override
	public void detachModels() {
		connectorInstanceModel.detach();
		metaModel.detach();
		super.detachModels();
	}
    
    private boolean hasMeta(ConnectorInstanceMeta meta, List<ConnectorInstanceMetaDTO> metas) {
        boolean hasMeta = false;
        for (ConnectorInstanceMetaDTO existingMeta : metas) {
            if (existingMeta.getName().equals(meta.getName())) {
                ConnectorInstance metaConnector = meta.getConnectorInstance();
                ConnectorInstance existingConnector = existingMeta.getConnectorInstance();
                if (metaConnector.equals(existingConnector)) {
                    hasMeta = true;
                    break;
                }
            }
        }
        return hasMeta;
    }

}
