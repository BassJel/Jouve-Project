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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * solr.BCDIntField 
 * solr.BCDLongField 
 * solr.BCDStrField 
 * solr.BinaryField 
 * solr.BoolField 
 * solr.ByteField 
 * solr.CompressableField 
 * solr.CopyField 
 * solr.DateField 
 * solr.DoubleField 
 * solr.ExternalFileField 
 * solr.FieldType 
 * solr.FloatField 
 * solr.IndexSchema 
 * solr.IntField 
 * solr.LegacyDateField 
 * solr.LongField 
 * solr.RandomSortField 
 * solr.SchemaField 
 * solr.ShortField 
 * solr.SimilarityFactory 
 * solr.SortableDoubleField 
 * solr.SortableFloatField 
 * solr.SortableIntField 
 * solr.SortableLongField 
 * solr.StrField 
 * solr.TextField 
 * solr.TrieDateField 
 * solr.TrieDoubleField 
 * solr.TrieField 
 * solr.TrieFloatField 
 * solr.TrieIntField 
 * solr.TrieLongField 
 * solr.UUIDField
 * 
 * @author Vincent Dussault
 */
@SuppressWarnings("serial")
@Entity
public class FieldTypeClass extends BaseConstellioEntity {
	
	public static final String[] DEFAULT_VALUES = {
		 "solr.BCDIntField",
		"solr.BCDLongField",
		"solr.BCDStrField",
//		 BinaryField",
		"solr.BoolField",
		"solr.ByteField",
		"solr.CompressableField",
//		 CopyField",
		"solr.DateField",
		"solr.DoubleField",
		"solr.ExternalFileField",
		"solr.FieldType",
		"solr.FloatField",
		"solr.IndexSchema",
		"solr.IntField",
//		 LegacyDateField",
		"solr.LongField",
		"solr.RandomSortField",
		"solr.SchemaField",
		"solr.ShortField",
		"solr.SimilarityFactory",
		"solr.SortableDoubleField",
		"solr.SortableFloatField",
		"solr.SortableIntField",
		"solr.SortableLongField",
		"solr.StrField",
		"solr.TextField"
//		 TrieDateField.class.getName(),
//		 TrieDoubleField.class.getName(),
//		 TrieField.class.getName(),
//		 TrieFloatField.class.getName(),
//		 TrieIntField.class.getName(),
//		 TrieLongField.class.getName(),
//		 UUIDField.class.getName()
	};

	private String className;

	private ConnectorManager connectorManager;

	@Column(nullable = false)
	public String getClassName() {
		return className;
	}

	public void setClassName(String fieldTypeClass) {
		this.className = fieldTypeClass;
	}

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public ConnectorManager getConnectorManager() {
		return connectorManager;
	}

	public void setConnectorManager(ConnectorManager connectorManager) {
		this.connectorManager = connectorManager;
	}

}
