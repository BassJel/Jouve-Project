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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

/**
 * A simple representation for a Meta element in a metadata set. 
 * This entity is referred by Record. 
 */
@SuppressWarnings("serial")
public class RecordMeta extends BaseConstellioEntity {
	
	private String content;

	private Record record;
	
	private ConnectorInstanceMeta connectorInstanceMeta;
	
	@Lob
	@Column (length = 6 * 1024 * 1024)
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Normally, we would have JoinColumn(nullable = false, updatable = false)
	 * 
	 * However, for cascade delete purposes, we cannot use these.
	 * 
	 * @return
	 */
	@ManyToOne
	public Record getRecord() {
		return record;
	}
	
	public void setRecord(Record record) {
		this.record = record;
	}

    /**
     * It is very likely that the ConnectorInstanceMeta objects will be added 
     * while fetching records. Hence why the cascade is performed this way.
     * 
     * @return
     */
    @ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
//	@JoinColumn(nullable = false, updatable = false)
	public ConnectorInstanceMeta getConnectorInstanceMeta() {
		return connectorInstanceMeta;
	}

	public void setConnectorInstanceMeta(ConnectorInstanceMeta connectorInstanceMeta) {
		this.connectorInstanceMeta = connectorInstanceMeta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((connectorInstanceMeta == null) ? 0 : connectorInstanceMeta
						.hashCode());
		result = prime * result + ((record == null) ? 0 : record.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (super.equals(obj))
			return true;
		if (getClass() != obj.getClass())
			return false;
		RecordMeta other = (RecordMeta) obj;
		if (connectorInstanceMeta == null) {
			if (other.connectorInstanceMeta != null)
				return false;
		} else if (!connectorInstanceMeta.equals(other.connectorInstanceMeta))
			return false;
		if (record == null) {
			if (other.record != null)
				return false;
		} else if (!record.equals(other.record))
			return false;
		return true;
	}
	
}
