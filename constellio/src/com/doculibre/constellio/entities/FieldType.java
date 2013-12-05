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

import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@SuppressWarnings("serial")
@Entity
public class FieldType extends BaseConstellioEntity {
    
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    
    public static final String INTEGER = "integer";
    public static final String LONG = "long";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    
    public static final String SINT = "sint";
    public static final String SLONG = "slong";
    public static final String SFLOAT = "sfloat";
    public static final String SDOUBLE = "sdouble";

    public static final String DATE = "date";
    public static final String RANDOM = "random";

    public static final String TEXT = "text";
    public static final String TEXT_GREEK = "text_greek";
    public static final String TEXT_WS = "text_ws";
    public static final String TEXT_FR = "text_fr";
    public static final String TEXT_ES = "text_es";
    public static final String TEXT_TIGHT = "text_tight";
    public static final String TEXT_SPELL = "text_spell";
    public static final String STRING_SPELL = "string_spell";
    public static final String URL = "url";

    public static final String ALPHA_ONLY_SORT = "alphaOnlySort";
    public static final String IGNORED = "ignored";
	
    public static final List<String> SORTABLE_FIELD_TYPES = Arrays.asList(new String[]{BOOLEAN, SINT, SLONG, SFLOAT, SDOUBLE, DATE, ALPHA_ONLY_SORT});
    
	private String name;
	
	private Integer positionIncrementGap;
	
	private Boolean sortMissingLast;
	
	private Boolean omitNorms;

	private Boolean indexed;
	
	private Boolean multiValued;
	
	private Integer precisionStep;

	private Analyzer analyzer;

	private Analyzer queryAnalyzer;

	private FieldTypeClass fieldTypeClass;

	private ConnectorManager connectorManager;

	@Column(nullable = false, unique = true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPositionIncrementGap() {
		return positionIncrementGap;
	}

	public void setPositionIncrementGap(Integer positionIncrementGap) {
		this.positionIncrementGap = positionIncrementGap;
	}

	public Boolean getSortMissingLast() {
		return sortMissingLast;
	}

	public void setSortMissingLast(Boolean sortMissingLast) {
		this.sortMissingLast = sortMissingLast;
	}

	public Boolean getOmitNorms() {
		return omitNorms;
	}

	public void setOmitNorms(Boolean omitNorms) {
		this.omitNorms = omitNorms;
	}

	public Boolean getIndexed() {
		return indexed;
	}

	public void setIndexed(Boolean indexed) {
		this.indexed = indexed;
	}

	public Boolean getMultiValued() {
		return multiValued;
	}

	public void setMultiValued(Boolean multiValued) {
		this.multiValued = multiValued;
	}

	public Integer getPrecisionStep() {
		return precisionStep;
	}
	
	public void setPrecisionStep(Integer precisionStep) {
		this.precisionStep = precisionStep;
	}

	@OneToOne(cascade = { CascadeType.ALL })
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	@OneToOne(cascade = { CascadeType.ALL })
	public Analyzer getQueryAnalyzer() {
		return queryAnalyzer;
	}

	public void setQueryAnalyzer(Analyzer queryAnalyzer) {
		this.queryAnalyzer = queryAnalyzer;
	}

	@ManyToOne
	@JoinColumn(nullable = false)
	public FieldTypeClass getFieldTypeClass() {
		return fieldTypeClass;
	}

	public void setFieldTypeClass(FieldTypeClass fieldTypeClass) {
		this.fieldTypeClass = fieldTypeClass;
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
