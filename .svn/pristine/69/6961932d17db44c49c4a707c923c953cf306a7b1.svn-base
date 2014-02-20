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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.apache.commons.lang.StringUtils;

/**
 * Description the connector manager information.
 */
@SuppressWarnings("serial")
@Entity
public class ConnectorManager extends BaseConstellioEntity {
	
	private String name;
	
	private String description;
	
	private String url;
	
	private Set<ConnectorType> connectorTypes = new HashSet<ConnectorType>();
	
	/**
	 * Provides a human-readable name for this connector manager. This name cannot be blank (null/empty/blank) and must be unique.
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
		this.name = name;
	}
	
	/**
	 * Provides a description for this connector manager. 
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Indicates the location/URL for the connector manager. This must a valid URL.
	 * 
	 * @return
	 */
	@Column(nullable = false, unique = true)
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) throws MalformedURLException {
		//Validate Url
		new URL(url);
		this.url = url;
	}
	
	/**
	 * Lists the connector types provided by this connector manager.
	 * 
	 * @return
	 */
	//FIXME Validate cascade...
	@OneToMany (mappedBy = "connectorManager", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
	public Set<ConnectorType> getConnectorTypes() {
		return connectorTypes;
	}
	
	public void setConnectorTypes(Set<ConnectorType> connectorTypes) {
		this.connectorTypes = connectorTypes;
	}
	
	public void addConnectorType(ConnectorType connectorType) {
		this.connectorTypes.add(connectorType);
		connectorType.setConnectorManager(this);
	}
	
}
