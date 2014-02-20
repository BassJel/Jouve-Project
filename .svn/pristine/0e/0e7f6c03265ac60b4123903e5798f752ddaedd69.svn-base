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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang.StringUtils;

import com.doculibre.constellio.utils.ConstellioNameUtils;


/**
 * A connector instance represents the configuration for a connector type. It
 * can be an email account, an ECM account, etc. This instance relates to a
 * specific instance in a connector manager.
 * The name of the connector instance must be unique.
 */
@Entity
@SuppressWarnings("serial")
public class ConnectorInstance extends BaseConstellioEntity {
	
	private String name;
	
	private String displayName;
	
	private String searchResultPanelClassName;
    
    private String initInstanceHandlerClassName;

    private ConnectorType connectorType;

	private RecordCollection recordCollection;
	
	private CredentialGroup credentialGroup;

    private Set<ConnectorInstanceMeta> connectorInstanceMetas = new HashSet<ConnectorInstanceMeta>();
		
	/**
	 * Provides a name for this connector instance. This name cannot be blank
	 * (null/empty/blank) and must be unique. The
	 * name must match the "[a-z_][a-z0-9_-]*" regular expression pattern.
	 * 
	 * @return
	 */
	@Column(nullable = false, unique = true, updatable = false)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Name provided cannot blank (null/empty/blank)");
		}
		if (!ConstellioNameUtils.isValidName(name)) {
			throw new IllegalArgumentException(
					"Name does not validate against regular expression : \"" + ConstellioNameUtils.NAME_PATTERN + "\" :" + name);
		}
		this.name = name;
	}

	@Column(nullable = false)
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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

	/**
	 * Links to the connector type of this connector instance.
	 * 
	 * @return
	 */
	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public ConnectorType getConnectorType() {
		return connectorType;
	}

	public void setConnectorType(ConnectorType connectorType) {
		this.connectorType = connectorType;
	}

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public RecordCollection getRecordCollection() {
		return recordCollection;
	}
	
	public void setRecordCollection(RecordCollection recordCollection) {
		this.recordCollection = recordCollection;
	}
    
    @ManyToOne
	public CredentialGroup getCredentialGroup() {
        return credentialGroup;
    }

    public void setCredentialGroup(CredentialGroup credentialGroup) {
        this.credentialGroup = credentialGroup;
    }

	@OneToMany(mappedBy = "connectorInstance", cascade = { CascadeType.ALL }, orphanRemoval = true)
	public Set<ConnectorInstanceMeta> getConnectorInstanceMetas() {
		return connectorInstanceMetas;
	}

	public void setConnectorInstanceMetas(Set<ConnectorInstanceMeta> connectorInstanceMetas) {
		this.connectorInstanceMetas = connectorInstanceMetas;
	}
	
	public void addConnectorInstanceMeta(ConnectorInstanceMeta connectorInstanceMeta) {
		this.connectorInstanceMetas.add(connectorInstanceMeta);
		connectorInstanceMeta.setConnectorInstance(this);
	}
	
	public ConnectorInstanceMeta getMeta(Long id) {
		ConnectorInstanceMeta match = null;
		for (ConnectorInstanceMeta connectorInstanceMeta : getConnectorInstanceMetas()) {
			if (connectorInstanceMeta.getId().equals(id)) {
				match = connectorInstanceMeta;
				break;
			}
		}
		return match;
	}
	
	public ConnectorInstanceMeta getOrCreateMeta(String name) {	
		ConnectorInstanceMeta match = null;
		for (ConnectorInstanceMeta connectorInstanceMeta : getConnectorInstanceMetas()) {
			if (connectorInstanceMeta.getName().toLowerCase().equals(name.toLowerCase())) {
				match = connectorInstanceMeta;
				break;
			}
		}
		if (match == null) {
			match = new ConnectorInstanceMeta();
			match.setName(name);
			addConnectorInstanceMeta(match);
		}
		return match;
	}
	
}
