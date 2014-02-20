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
package com.doculibre.constellio.wicket.panels.admin.connector;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
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
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.dom4j.Element;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.scheduler.SerializableSchedule;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.services.ConnectorInstanceServices;
import com.doculibre.constellio.services.ConnectorManagerServices;
import com.doculibre.constellio.services.ConnectorTypeServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.ConnectorInstanceConfigFormSnippet;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.connector.schedule.TimeIntervalListPanel;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.servlet.ServletUtil;

@SuppressWarnings("serial")
public class AddEditConnectorPanel extends SaveCancelFormPanel {

    private ReloadableEntityModel<ConnectorInstance> connectorInstanceModel;

    private ConnectorInstanceConfigFormSnippet configFormSnippet;

    private SerializableSchedule serializableSchedule;

    private boolean add;

    private CheckBox disabledField;

    public AddEditConnectorPanel(String id, ConnectorInstance connectorInstance) {
        super(id, true);
        this.connectorInstanceModel = new ReloadableEntityModel<ConnectorInstance>(connectorInstance);

        add = connectorInstance.getId() == null;

        Form form = getForm();
        form.setModel(new CompoundPropertyModel(connectorInstanceModel));

        form.add(new RequiredTextField("displayName"));

        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
        Schedule schedule;
        if (connectorInstance.getName() != null) {
            schedule = connectorManagerServices.getSchedule(connectorManager,
                connectorInstance.getName());
            if (schedule == null) {
                schedule = connectorManagerServices.getDefaultSchedule();
            }
        } else {
            schedule = connectorManagerServices.getDefaultSchedule();
        }
        serializableSchedule = new SerializableSchedule(schedule);

        IModel typesModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ConnectorTypeServices connectorTypeServices = ConstellioSpringUtils
                    .getConnectorTypeServices();
                return connectorTypeServices.list();
            }
        };

        IChoiceRenderer typeRenderer = new ChoiceRenderer() {
            @Override
            public Object getDisplayValue(Object object) {
                String displayValue;
                ConnectorType connectorType = (ConnectorType) object;
                displayValue = connectorType.getName();
                return displayValue;
            }
        };

        final DropDownChoice typeField = new DropDownChoice("connectorType", typesModel, typeRenderer);
        form.add(typeField);

        ConnectorTypeServices connectorTypeServices = ConstellioSpringUtils.getConnectorTypeServices();
        List<ConnectorType> connectorTypes = connectorTypeServices.list();
        if (connectorTypes.size() == 1) {
            typeField.setModelObject(connectorTypes.get(0));
        }

        IModel loadModel = new PropertyModel(serializableSchedule, "load");
        form.add(new TextField("load", loadModel));

        IModel retryDelayMillisModel = new PropertyModel(serializableSchedule, "retryDelayMillis");
        form.add(new TextField("retryDelayMillis", retryDelayMillisModel));

        IModel disabledModel = new PropertyModel(serializableSchedule, "disabled");
        disabledField = new CheckBox("disabled", disabledModel);
        disabledField.setOutputMarkupId(true);
        form.add(disabledField);

        final IModel connectorTypeNameModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
                ConnectorType connectorType = connectorInstance.getConnectorType();
                return connectorType != null ? connectorType.getName() : null;
            }
        };
        configFormSnippet = new ConnectorInstanceConfigFormSnippet("configFormSnippet",
            connectorTypeNameModel, connectorInstance.getName());
        form.add(configFormSnippet);

        typeField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                configFormSnippet.reset();
                target.addComponent(configFormSnippet);
            }
        });

        form.add(new TimeIntervalListPanel("timeIntervalsPanel"));
    }

    public SerializableSchedule getSchedule() {
        return serializableSchedule;
    }

    @Override
    public void detachModels() {
        connectorInstanceModel.detach();
        super.detachModels();
    }

    @Override
    protected IModel getTitleModel() {
        return new LoadableDetachableModel() {
            @Override
            protected Object load() {
                String titleKey = add ? "add" : "edit";
                IModel titleModel = new StringResourceModel(titleKey, AddEditConnectorPanel.this, null);
                return titleModel.getObject();
            }
        };
    }

    @Override
    protected Component newReturnComponent(String id) {
        return new ConnectorListPanel(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onSave(AjaxRequestTarget target) {
        AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
        RecordCollection collection = collectionAdminPanel.getCollection();

        ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils
            .getConnectorInstanceServices();
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        ConnectorTypeServices connectorTypeServices = ConstellioSpringUtils.getConnectorTypeServices();

        ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
        ConnectorType connectorType = connectorInstance.getConnectorType();

        // Ugly workaround... Avoid lazy loading exceptions since connectorType has been set in a
        // previous (ajax) request
        connectorType = connectorTypeServices.get(connectorType.getId());
        connectorInstance.setConnectorType(connectorType);

        RequestCycle requestCycle = RequestCycle.get();
        WebRequest webRequest = (WebRequest) requestCycle.getRequest();
        Map<String, String[]> params = webRequest.getHttpServletRequest().getParameterMap();

        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();

        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        String connectorName = connectorInstance.getName();
        Element errorElement;
        Locale locale = getLocale();
        if (connectorInstance.getId() == null) {
            // Create
            connectorName = connectorManagerServices.generateConnectorName(connectorManager, collection
                .getName(), connectorType.getName());
            connectorInstance.setName(connectorName);
            errorElement = connectorManagerServices.createConnector(connectorManager, connectorName,
                connectorType.getName(), params, locale);
        } else {
            errorElement = connectorManagerServices.updateConnector(connectorManager, connectorName, params, locale);
        }

        
        if (errorElement == null) {
            boolean synchronizeIndex;
            if (collection.getConnectorInstances().isEmpty() || !collection.isSynchronizationRequired()) {
                synchronizeIndex = true;
            } else {
                synchronizeIndex = false;
            }
            collection.addConnectorInstance(connectorInstance);
            connectorInstanceServices.makePersistent(connectorInstance);
            
            if (!entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }
            
            if (synchronizeIndex) {
                RecordCollectionServices collectionServices = ConstellioSpringUtils
                    .getRecordCollectionServices();
                SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
                solrServices.updateSchemaFields(collection);
                solrServices.initCore(collection);
                collectionServices.markSynchronized(collection);
                
                IndexingManager indexingManager = IndexingManager.get(collection);
                if (!indexingManager.isActive()) {
                    indexingManager.startIndexing();
                }
            }

            // Schedule
            Schedule schedule = serializableSchedule.toSchedule();
            // Ensure
            schedule.setConnectorName(connectorName);
            connectorManagerServices.setSchedule(connectorManager, connectorName, schedule);

            entityManager.getTransaction().commit();
            if (!entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }
        } else {
            Element formSnippetElement = errorElement.element(ServletUtil.XMLTAG_CONFIGURE_RESPONSE).element(
                ServletUtil.XMLTAG_FORM_SNIPPET);
            if (formSnippetElement != null) {
                configFormSnippet.setInvalidFormSnippetElement(formSnippetElement);
                target.addComponent(configFormSnippet);
            }
            Element errorMessageElement = errorElement.element(ServletUtil.XMLTAG_CONFIGURE_RESPONSE)
                .element(ServletUtil.XMLTAG_MESSAGE);
            if (errorMessageElement != null) {
                String errorMessage = errorMessageElement.getText();
                getForm().error(errorMessage);
            } else {
                String statusId = errorElement.element(ServletUtil.XMLTAG_STATUSID).getTextTrim();
                getForm().error("Status Id : " + statusId);
            }
        }
    }

    public Component getDisabledScheduleField() {
        return disabledField;
    }

}
