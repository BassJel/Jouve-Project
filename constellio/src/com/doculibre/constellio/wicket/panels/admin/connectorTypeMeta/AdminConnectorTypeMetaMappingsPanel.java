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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.services.ConnectorTypeServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.fold.FoldableSectionPanel;

@SuppressWarnings("serial")
public class AdminConnectorTypeMetaMappingsPanel extends AjaxPanel {

    public AdminConnectorTypeMetaMappingsPanel(String id) {
        super(id);

        IModel connectorTypesModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ConnectorTypeServices connectorTypeServices = ConstellioSpringUtils
                    .getConnectorTypeServices();
                return connectorTypeServices.list();
            }
        };
        add(new ListView("connectorTypes", connectorTypesModel) {
            @Override
            protected void populateItem(ListItem item) {
                ConnectorType connectorType = (ConnectorType) item.getModelObject();
                final ReloadableEntityModel<ConnectorType> connectorTypeModel = new ReloadableEntityModel<ConnectorType>(
                    connectorType);
                FoldableSectionPanel foldableSectionPanel = 
                    new FoldableSectionPanel("crudPanel", new PropertyModel(connectorTypeModel, "name")) {
                    @Override
                    protected Component newFoldableSection(String id) {
                        ConnectorType connectorType = connectorTypeModel.getObject();
                        return new ConnectorTypeMetaMappingListPanel(id, connectorType);
                    }

                    @Override
                    public void detachModels() {
                        connectorTypeModel.detach();
                        super.detachModels();
                    }
                };
                item.add(foldableSectionPanel);
                foldableSectionPanel.setOpened(false);
            }
        });
    }

}
