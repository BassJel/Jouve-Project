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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.resource.ByteArrayResource;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.components.holders.ImgHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;

@SuppressWarnings("serial")
public class ConnectorListPanel extends SingleColumnCRUDPanel {

    public ConnectorListPanel(String id) {
        super(id);

        super.setModel(new LoadableDetachableModel() {
            @Override
            protected Object load() {
                AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                RecordCollection collection = collectionAdminPanel.getCollection();
                return new ArrayList<ConnectorInstance>(collection.getConnectorInstances());
            }
        });
    }

    @Override
    protected WebMarkupContainer createAddContent(String id) {
        return new AddEditConnectorPanel(id, new ConnectorInstance());
    }

    @Override
    protected List<IColumn> getDataColumns() {
        List<IColumn> dataColumns = super.getDataColumns();
        dataColumns.add(0, new HeaderlessColumn() {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                ConnectorInstance connectorInstance = (ConnectorInstance) rowModel.getObject();
                final ReloadableEntityModel<ConnectorInstance> connectorInstanceModel = new ReloadableEntityModel<ConnectorInstance>(
                    connectorInstance);
                cellItem.add(new ImgHolder(componentId) {
                    @Override
                    protected Image newImg(String id) {
                        ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
                        ConnectorType connectorType = connectorInstance.getConnectorType();
                        ResourceReference imageResourceReference = new ResourceReference("connectorType_"
                            + connectorType.getId()) {
                            @Override
                            protected Resource newResource() {
                                ConnectorInstance connectorInstance = (ConnectorInstance) connectorInstanceModel
                                    .getObject();
                                ConnectorType connectorType = connectorInstance.getConnectorType();
                                Resource imageResource;
                                byte[] iconBytes = connectorType.getIconFileContent();
                                // Convert resource path to absolute path relative to base package
                                if (iconBytes != null) {
                                    imageResource = new ByteArrayResource("image", iconBytes);
                                } else {
                                    imageResource = PackageResource.get(ConnectorListPanel.class, "default_icon.gif");
                                }
                                return imageResource;
                            }
                        };
                        return new NonCachingImage(id, imageResourceReference);
                    }
                    
                    @Override
                    public void detachModels() {
                        connectorInstanceModel.detach();
                        super.detachModels();
                    }
                });
            }
        });
        return dataColumns;
    }

    @Override
    protected void onClickDetailsLink(IModel entityModel, AjaxRequestTarget target, ModalWindow detailsModal,
        int index) {

    }

    @Override
    protected WebMarkupContainer createEditContent(String id, final IModel entityModel, int index) {
        ConnectorInstance connectorInstance = (ConnectorInstance) entityModel.getObject();
        return new AddEditConnectorPanel(id, connectorInstance);
    }

    @Override
    protected String getDetailsLabel(Object entity) {
        ConnectorInstance connectorInstance = (ConnectorInstance) entity;
        String displayConfig = connectorInstance.getDisplayName();
        return displayConfig;
    }

    @Override
    protected IModel getTitleModel() {
        return new LoadableDetachableModel() {
            @Override
            protected Object load() {
                AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                RecordCollection collection = collectionAdminPanel.getCollection();
                Locale displayLocale = collection.getDisplayLocale(getLocale());
                String title = MapVariableInterpolator.interpolate(getLocalizer().getString("panelTitle",
                    ConnectorListPanel.this), new MicroMap("collectionName", collection
                    .getTitle(displayLocale)));
                return title;
            }
        };
    }

    @Override
    protected BaseCRUDServices<ConnectorInstance> getServices() {
        return ConstellioSpringUtils.getConnectorInstanceServices();
    }

    @Override
    protected boolean isUseModals() {
        return false;
    }

}
