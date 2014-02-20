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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@SuppressWarnings("serial")
@Entity
public class CredentialGroup extends BaseConstellioEntity {
    
    private String name;

    private RecordCollection recordCollection;
    
    private Set<ConnectorInstance> connectorInstances = new HashSet<ConnectorInstance>();
    
    private Set<UserCredentials> userCredentials = new HashSet<UserCredentials>();
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne
    public RecordCollection getRecordCollection() {
        return recordCollection;
    }

    public void setRecordCollection(RecordCollection recordCollection) {
        this.recordCollection = recordCollection;
    }

    @OneToMany(mappedBy = "credentialGroup")
    public Set<ConnectorInstance> getConnectorInstances() {
        return connectorInstances;
    }

    public void setConnectorInstances(Set<ConnectorInstance> connectorInstances) {
        this.connectorInstances = connectorInstances;
    }
    
    public void addConnectorInstance(ConnectorInstance connectorInstance) {
        this.connectorInstances.add(connectorInstance);
        connectorInstance.setCredentialGroup(this);
    }

    @OneToMany(mappedBy = "credentialGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval=true)
    public Set<UserCredentials> getUserCredentials() {
        return userCredentials;
    }

    public void setUserCredentials(Set<UserCredentials> userCredentials) {
        this.userCredentials = userCredentials;
    }
    
    public void addUserCredentials(UserCredentials credentials) {
        this.userCredentials.add(credentials);
        credentials.setCredentialGroup(this);
    }

}
