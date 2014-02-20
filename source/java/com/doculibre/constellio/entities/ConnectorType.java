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
package com.doculibre.constellio.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang.StringUtils;

@SuppressWarnings("serial")
@Entity
public class ConnectorType extends BaseConstellioEntity {

	public static final String CONNECTOR_TYPE_DOCUMENTUM = "documentum-connector";
	public static final String CONNECTOR_TYPE_FILENET = "filenet-connector";
	public static final String CONNECTOR_TYPE_LIVELINK = "livelink-connector";
	public static final String CONNECTOR_TYPE_MAIL = "mail-connector";
	public static final String CONNECTOR_TYPE_SHAREPOINT = "sharepoint-connector";
	public static final String CONNECTOR_TYPE_WEB = "web-connector";
    public static final String CONNECTOR_TYPE_HTTP = "http-connector";
	
	private String name;
	
	private String searchResultPanelClassName;
    
    private String initInstanceHandlerClassName;

    private byte[] iconFileContent;
	
	private ConnectorManager connectorManager;

	private Set<ConnectorInstance> connectorInstances = new HashSet<ConnectorInstance>();

	private Set<ConnectorTypeMetaMapping> metaMappings = new HashSet<ConnectorTypeMetaMapping>();

	/**
	 * Provides a name for this connector manager. This name cannot be blank
	 * (null/empty/blank) and must be unique for the connection manager.
	 * This names must be unique for the connector manager.
	 * 
	 * @return
	 */
	@Column(nullable = false, updatable = false)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Name provided cannot blank (null/empty/blank)");
		}
		this.name = name;
	}
	
	public String getSearchResultPanelClassName() {
		return searchResultPanelClassName;
	}
	
	public void setSearchResultPanelClassName(String searchResultPanelClassName) {
		this.searchResultPanelClassName = searchResultPanelClassName;
	}

    public String getInitInstanceHandlerClassName() {
        return initInstanceHandlerClassName;
    }

    public void setInitInstanceHandlerClassName(String initInstanceHandlerClassName) {
        this.initInstanceHandlerClassName = initInstanceHandlerClassName;
    }

	@Lob
	@Column (length = 1024 * 1024)
	public byte[] getIconFileContent() {
		return iconFileContent;
	}
	
	public void setIconFileContent(byte[] iconFileContent) {
		this.iconFileContent = iconFileContent;
	}

	@ManyToOne
	public ConnectorManager getConnectorManager() {
		return connectorManager;
	}
	
	public void setConnectorManager(ConnectorManager connectorManager) {
		this.connectorManager = connectorManager;
	}

	@OneToMany(mappedBy = "connectorType", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	public Set<ConnectorInstance> getConnectorInstances() {
		return connectorInstances;
	}
	
	public void setConnectorInstances(Set<ConnectorInstance> connectorInstances) {
		this.connectorInstances = connectorInstances;
	}
	
	public void addConnectorInstance(ConnectorInstance connectorInstance) {
		this.connectorInstances.add(connectorInstance);
		connectorInstance.setConnectorType(this);
	}

	@OneToMany(mappedBy = "connectorType", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	public Set<ConnectorTypeMetaMapping> getMetaMappings() {
		return metaMappings;
	}

	public void setMetaMappings(Set<ConnectorTypeMetaMapping> metaMappings) {
		this.metaMappings = metaMappings;
	}
	
	public void addMetaMapping(ConnectorTypeMetaMapping metaMapping) {
		this.metaMappings.add(metaMapping);
		metaMapping.setConnectorType(this);
	}

}
