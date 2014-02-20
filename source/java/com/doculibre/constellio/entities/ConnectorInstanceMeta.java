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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@SuppressWarnings("serial")
@Entity
public class ConnectorInstanceMeta extends BaseConstellioEntity {
	
	private String name;
	
	private ConnectorInstance connectorInstance;
	
	private Set<IndexFieldMetaMapping> indexFieldMappings = new HashSet<IndexFieldMetaMapping>();

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne 
	@JoinColumn (nullable = false, updatable = false)
	public ConnectorInstance getConnectorInstance() {
		return connectorInstance;
	}

	public void setConnectorInstance(ConnectorInstance connectorInstance) {
		this.connectorInstance = connectorInstance;
	}

    @OneToMany(mappedBy = "meta", cascade = { CascadeType.ALL }, orphanRemoval = true)
    public Set<IndexFieldMetaMapping> getIndexFieldMappings() {
        return indexFieldMappings;
    }

    public void setIndexFieldMappings(Set<IndexFieldMetaMapping> indexFieldMappings) {
        this.indexFieldMappings = indexFieldMappings;
    }
	
	public void addIndexField(IndexField indexField) {
	    IndexFieldMetaMapping indexFieldMapping = new IndexFieldMetaMapping();
	    indexFieldMapping.setMeta(this);
	    indexFieldMapping.setIndexField(indexField);
	    indexFieldMappings.add(indexFieldMapping);
	}

//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((name == null) ? 0 : name.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (super.equals(obj))
//            return true;
//        if (getClass() != obj.getClass())
//            return false;
//        ConnectorInstanceMeta other = (ConnectorInstanceMeta) obj;
//        if (name == null) {
//            if (other.name != null)
//                return false;
//        } else if (!name.equals(other.name))
//            return false;
//        return true;
//    }

}
