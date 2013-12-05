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
package com.doculibre.constellio.wicket.panels.admin.credentialGroup;

import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.CredentialGroup;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.CredentialGroupServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.models.EntityModel;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class AddEditCredentialGroupPanel extends SaveCancelFormPanel {

    private EntityModel<CredentialGroup> credentialGroupModel;

    private boolean isCreation;

    private TextField nameField;
    private CheckGroup connectorInstancesCheckGroup;

    private ListView connectorInstancesListView;

    public AddEditCredentialGroupPanel(String id, CredentialGroup credentialGroup) {
        super(id, true);
        this.credentialGroupModel = new EntityModel<CredentialGroup>(credentialGroup);
        this.isCreation = credentialGroup.getId() == null;

        Form form = getForm();
        form.setModel(new CompoundPropertyModel(credentialGroupModel));
        form.add(new SetFocusBehavior(form));

        nameField = new RequiredTextField("name");
        nameField.add(new StringValidator.MaximumLengthValidator(255));

        connectorInstancesCheckGroup = new CheckGroup("connectorInstances", new PropertyModel(
            credentialGroupModel, "connectorInstances"));

        IModel connectorInstancesModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                AdminCollectionPanel adminCollectionPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                RecordCollection collection = adminCollectionPanel.getCollection();
                return new ArrayList<ConnectorInstance>(collection.getConnectorInstances());
            }
        };
        connectorInstancesListView = new ListView("connectorInstancesListView", connectorInstancesModel) {
            @Override
            protected void populateItem(ListItem item) {
                ConnectorInstance connectorInstance = (ConnectorInstance) item.getModelObject();
                Check check = new Check("check", new ReloadableEntityModel<ConnectorInstance>(
                    connectorInstance));
                Label label = new Label("label", connectorInstance.getDisplayName());
                item.add(check);
                item.add(label);
            }
        };
        connectorInstancesListView.setReuseItems(true);

        form.add(nameField);
        form.add(connectorInstancesCheckGroup);
        connectorInstancesCheckGroup.add(connectorInstancesListView);
    }

    @Override
    public void detachModels() {
        credentialGroupModel.detach();
        super.detachModels();
    }

    @Override
    protected IModel getTitleModel() {
        return new LoadableDetachableModel() {
            @Override
            protected Object load() {
                String titleKey = isCreation ? "add" : "edit";
                return new StringResourceModel(titleKey, AddEditCredentialGroupPanel.this, null).getObject();
            }
        };
    }

    @Override
    protected Component newReturnComponent(String id) {
        return new CredentialGroupListPanel(getId());
    }

    @Override
    protected void onSave(AjaxRequestTarget target) {
        CredentialGroup credentialGroup = credentialGroupModel.getObject();

        CredentialGroupServices credentialGroupServices = ConstellioSpringUtils.getCredentialGroupServices();
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        if (isCreation) {
            AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
            RecordCollection collection = collectionAdminPanel.getCollection();
            collection.addCredentialGroup(credentialGroup);
        }
        for (ConnectorInstance connectorInstance : credentialGroup.getConnectorInstances()) {
            connectorInstance.setCredentialGroup(credentialGroup);
        }
        credentialGroupServices.makePersistent(credentialGroup);
        entityManager.getTransaction().commit();
    }

}
