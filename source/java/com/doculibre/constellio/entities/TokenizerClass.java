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
 * solr.KeywordTokenizerFactory
 * solr.LetterTokenizerFactory
 * solr.WhitespaceTokenizerFactory
 * solr.LowerCaseTokenizerFactory
 * solr.StandardTokenizerFactory
 * solr.HTMLStripWhitespaceTokenizerFactory
 * solr.HTMLStripStandardTokenizerFactory
 * solr.PatternTokenizerFactory
 * 
 * @author Vincent Dussault
 */
@SuppressWarnings("serial")
@Entity
public class TokenizerClass extends BaseConstellioEntity {

	public static final String[] DEFAULT_VALUES = {
		"solr.KeywordTokenizerFactory",
		"solr.LetterTokenizerFactory",
		"solr.WhitespaceTokenizerFactory",
		"solr.LowerCaseTokenizerFactory",
		"solr.StandardTokenizerFactory",
		"solr.HTMLStripWhitespaceTokenizerFactory",
		"solr.HTMLStripStandardTokenizerFactory",
		"solr.PatternTokenizerFactory"
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
