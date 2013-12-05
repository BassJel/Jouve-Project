package com.doculibre.constellio.wicket.panels.admin.indexField.dto;

import org.apache.wicket.model.IDetachable;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.wicket.models.EntityModel;

@SuppressWarnings("serial")
public class ConnectorInstanceMetaDTO implements IDetachable {
	
	private Long connectorInstanceMetaId;
	
	private String name;
	
	private EntityModel<ConnectorInstance> connectorInstanceModel;
	
	public ConnectorInstanceMetaDTO(ConnectorInstanceMeta connectorInstanceMeta) {
		this.connectorInstanceMetaId = connectorInstanceMeta.getId();
		this.name = connectorInstanceMeta.getName();
		this.connectorInstanceModel = new EntityModel<ConnectorInstance>(connectorInstanceMeta.getConnectorInstance());
	}
	
	public ConnectorInstanceMeta toConnectorInstanceMeta() {
		ConnectorInstanceMeta connectorInstanceMeta = new ConnectorInstanceMeta();
		connectorInstanceMeta.setId(connectorInstanceMetaId);
		connectorInstanceMeta.setName(name);
		connectorInstanceMeta.setConnectorInstance(getConnectorInstance());
		return connectorInstanceMeta;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ConnectorInstance getConnectorInstance() {
		return connectorInstanceModel.getObject();
	}

	public void setConnectorInstance(ConnectorInstance connectorInstance) {
        if (this.connectorInstanceModel == null) {
            this.connectorInstanceModel = new EntityModel<ConnectorInstance>(connectorInstance);
        } else {
            this.connectorInstanceModel.setObject(connectorInstance);
        }
	}

	@Override
	public void detach() {
		connectorInstanceModel.detach();
	}

}
