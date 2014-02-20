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
package com.doculibre.constellio.wicket.panels.admin.resultPanels;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.services.ConnectorInstanceServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.results.DefaultSearchResultPanel;
import com.doculibre.constellio.wicket.renderers.StringResourceChoiceRenderer;

@SuppressWarnings("serial")
public class AddEditResultClassPanel extends SaveCancelFormPanel {

    private static final String CUSTOM_CLASS = "customClass";
    private static final String DEFAULT_CLASS = "defaultClass";
    private static final String DEFAULT_CONNECTOR_TYPE_CLASS = "defaultConnectorTypeClass";

    private ReloadableEntityModel<ConnectorInstance> connectorModel;

    private DropDownChoice defaultOrCustomDropDown;

    private TextField customClassTextField;

    private Label defaultClassLabel;

    public AddEditResultClassPanel(String id, ConnectorInstance connector) {
        super(id, true);
        this.connectorModel = new ReloadableEntityModel<ConnectorInstance>(connector);

        String defaultConnectorTypeClass = connector.getConnectorType().getSearchResultPanelClassName();

        IModel customClassModel = new Model();

        final IModel choice = new Model();
        if (connector.getSearchResultPanelClassName() != null) {
            choice.setObject(CUSTOM_CLASS);
            customClassModel.setObject(connector.getSearchResultPanelClassName());
        } else if (defaultConnectorTypeClass != null) {
            choice.setObject(DEFAULT_CONNECTOR_TYPE_CLASS);
        } else {
            choice.setObject(DEFAULT_CLASS);
        }

        List<String> choices = new ArrayList<String>();
        String defaultClass = DefaultSearchResultPanel.class.getCanonicalName();
        if (defaultConnectorTypeClass != null) {
            defaultClass = defaultConnectorTypeClass;
            choices.add(DEFAULT_CONNECTOR_TYPE_CLASS);
        } else {
            choices.add(DEFAULT_CLASS);
        }
        choices.add(CUSTOM_CLASS);

        final Form form = getForm();

        // Remplacer les composantes directement par ajax crée des problèmes
        // Remplacer le panel au complet crée un autre problème avec firefox
        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);

        this.customClassTextField = new TextField("customClass", customClassModel) {
            @Override
            public boolean isVisible() {
                boolean visible = CUSTOM_CLASS.equals((String) choice.getObject());
                return visible;
            }
        };

        this.defaultClassLabel = new Label("defaultClass", defaultClass) {
            @Override
            public boolean isVisible() {
                return !CUSTOM_CLASS.equals((String) choice.getObject());
            }
        };

        this.defaultOrCustomDropDown = new DropDownChoice("defaultClassRadio", choice, choices,
            new StringResourceChoiceRenderer(this));
        this.defaultOrCustomDropDown.add(new OnChangeAjaxBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.addComponent(container);
            }
        });

        form.add(defaultOrCustomDropDown);
        form.add(container);
        container.add(customClassTextField);
        container.add(defaultClassLabel);
    }

    @Override
    protected void onSave(AjaxRequestTarget target) {
        ConnectorInstance connector = connectorModel.getObject();
        ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils
            .getConnectorInstanceServices();
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        String choice = (String) defaultOrCustomDropDown.getModelObject();
        if (CUSTOM_CLASS.equals(choice)) {
            String customClass = (String) customClassTextField.getModelObject();
            connector.setSearchResultPanelClassName(customClass);
        } else {
            connector.setSearchResultPanelClassName(null);
        }
        connectorInstanceServices.merge(connector);
        entityManager.getTransaction().commit();
    }

    @Override
    protected IModel getTitleModel() {
        return new StringResourceModel("edit", this, null);
    }

    @Override
    protected Component newReturnComponent(String id) {
        return null;
    }

    @Override
    protected void defaultReturnAction(AjaxRequestTarget target) {
        super.defaultReturnAction(target);
        ConnectorResultClassesPanel connectorResultClassesListPanel = (ConnectorResultClassesPanel) findParent(ConnectorResultClassesPanel.class);
        target.addComponent(connectorResultClassesListPanel);
    }

}
