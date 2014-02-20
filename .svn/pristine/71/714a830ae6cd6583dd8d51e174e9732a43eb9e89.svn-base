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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.ConnectorTypeMetaMapping;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.ConnectorTypeMetaMappingServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.holders.CheckBoxHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;

@SuppressWarnings("serial")
public class ConnectorTypeMetaMappingListPanel extends SingleColumnCRUDPanel {
	
	private ReloadableEntityModel<ConnectorType> connectorTypeModel;

	public ConnectorTypeMetaMappingListPanel(String id, ConnectorType connectorType) {
		super(id);
		this.connectorTypeModel = new ReloadableEntityModel<ConnectorType>(connectorType);
		
		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				ConnectorType connectorType = connectorTypeModel.getObject();
				return new ArrayList<ConnectorTypeMetaMapping>(connectorType.getMetaMappings());
			}
		});
	}
	
	@Override
	public void detachModels() {
		connectorTypeModel.detach();
		super.detachModels();
	}

	@Override
    protected List<IColumn> getDataColumns() {
        List<IColumn> dataColumns = new ArrayList<IColumn>();
        
        dataColumns.add(getAjaxEditableLabelColumn("metaName"));
        dataColumns.add(getAjaxEditableLabelColumn("indexFieldName"));
        dataColumns.add(getAjaxCheckboxColumn("indexed"));
        //dataColumns.add(getAjaxCheckboxColumn("stored"));
        dataColumns.add(getAjaxCheckboxColumn("multiValued"));
        
        return dataColumns;
    }
	
	private IColumn getAjaxEditableLabelColumn(final String propertyName) {
	    return new AbstractColumn(new StringResourceModel(propertyName, this, null)) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                ConnectorTypeMetaMapping metaMapping = (ConnectorTypeMetaMapping) rowModel.getObject();
                
                final ReloadableEntityModel<ConnectorTypeMetaMapping> entityModel = 
                    new ReloadableEntityModel<ConnectorTypeMetaMapping>(metaMapping); 
                cellItem.setModel(new CompoundPropertyModel(entityModel));
                
                cellItem.add(new AjaxEditableLabel(componentId, new PropertyModel(entityModel, propertyName)) {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target) {
                        updateMetaMapping(entityModel.getObject(), target);
                        super.onSubmit(target);
                    }

                    @Override
                    public void detachModels() {
                        entityModel.detach();
                        super.detachModels();
                    }
                });
            }
        };
	}
    
    private IColumn getAjaxCheckboxColumn(final String propertyName) {
        return new AbstractColumn(new StringResourceModel(propertyName, this, null)) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                ConnectorTypeMetaMapping metaMapping = (ConnectorTypeMetaMapping) rowModel.getObject();
                
                final ReloadableEntityModel<ConnectorTypeMetaMapping> entityModel = 
                    new ReloadableEntityModel<ConnectorTypeMetaMapping>(metaMapping); 
                cellItem.setModel(new CompoundPropertyModel(entityModel));
                
                cellItem.add(new CheckBoxHolder(componentId) {
                    @Override
                    protected WebMarkupContainer newInput(String id) {
                        return new AjaxCheckBox(id, new PropertyModel(entityModel, propertyName)) {
                            @Override
                            protected void onUpdate(AjaxRequestTarget target) {
                                ConnectorTypeMetaMapping metaMapping = entityModel.getObject();
                                updateMetaMapping(metaMapping, target);
                            }

                            @Override
                            public void detachModels() {
                                entityModel.detach();
                                super.detachModels();
                            }
                        };
                    }
                });
            }
        };
    }

	private void updateMetaMapping(ConnectorTypeMetaMapping metaMapping, AjaxRequestTarget target) {
		ConnectorTypeMetaMappingServices metaMappingServices = 
			ConstellioSpringUtils.getConnectorTypeMetaMappingServices();
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		metaMappingServices.makePersistent(metaMapping);
		entityManager.getTransaction().commit();
		
		target.addComponent(this);
	}

	@Override
	protected BaseCRUDServices<ConnectorTypeMetaMapping> getServices() {
		return ConstellioSpringUtils.getConnectorTypeMetaMappingServices();
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		ConnectorTypeMetaMapping metaMapping = new ConnectorTypeMetaMapping();
		return new AddEditConnectorTypeMetaMappingPanel(id, metaMapping);
	}

	@Override
	protected WebMarkupContainer createEditContent(String id,
			IModel entityModel, int index) {
		ConnectorTypeMetaMapping metaMapping = (ConnectorTypeMetaMapping) entityModel.getObject();
		return new AddEditConnectorTypeMetaMappingPanel(id, metaMapping);
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		return null;
	}
	
	public ConnectorType getConnectorType() {
	    return connectorTypeModel.getObject();
	}

}
