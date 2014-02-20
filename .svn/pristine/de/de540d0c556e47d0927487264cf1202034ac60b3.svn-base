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
 * solr.StandardFilterFactory
 * solr.LowerCaseFilterFactory
 * solr.TrimFilterFactory
 * solr.StopFilterFactory
 * solr.KeepWordFilterFactory
 * solr.LengthFilterFactory
 * solr.PorterStemFilterFactory
 * solr.EnglishPorterFilterFactory
 * solr.SnowballPorterFilterFactory
 * solr.WordDelimiterFilterFactory
 * solr.SynonymFilterFactory
 * solr.RemoveDuplicatesTokenFilterFactory
 * solr.ISOLatin1AccentFilterFactory
 * solr.PhoneticFilterFactory
 * solr.ShingleFilterFactory
 * solr.PositionFilterFactory
 * solr.ReversedWildcardFilterFactory
 * solr.PatternReplaceFilterFactory
 * solr.DoubleMetaphoneFilterFactory
 * solr.DelimitedPayloadTokenFilterFactory
 * 
 * @author Vincent Dussault
 */
@SuppressWarnings("serial")
@Entity
public class FilterClass extends BaseConstellioEntity {
	
	public static final String[] DEFAULT_VALUES = {
		"solr.StandardFilterFactory",
		"solr.LowerCaseFilterFactory",
		"solr.TrimFilterFactory",
		"solr.StopFilterFactory",
		"solr.KeepWordFilterFactory",
		"solr.LengthFilterFactory",
		"solr.PorterStemFilterFactory",
		"solr.EnglishPorterFilterFactory",
		"solr.SnowballPorterFilterFactory",
		"solr.WordDelimiterFilterFactory",
		"solr.SynonymFilterFactory",
		"solr.RemoveDuplicatesTokenFilterFactory",
		"solr.ISOLatin1AccentFilterFactory",
		"solr.PhoneticFilterFactory",
		"solr.ShingleFilterFactory",
//		PositionFilterFactory",
//		ReversedWildcardFilterFactory",
		"solr.PatternReplaceFilterFactory"
//		DoubleMetaphoneFilterFactory",
//		DelimitedPayloadTokenFilterFactory"
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
