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

@SuppressWarnings("serial")
@Entity
public class ConnectorTypeMetaMapping extends BaseConstellioEntity {

    private String metaName;

    private String indexFieldName;

    private Boolean uniqueKey;

    private Boolean defaultSearchField;

    private Boolean indexed;

    private Boolean multiValued;

    private FieldType fieldType;

    private Analyzer analyzer;

    @Column(nullable = false)
    public String getMetaName() {
        return metaName;
    }

    public void setMetaName(String name) {
        this.metaName = name;
    }

    public String getIndexFieldName() {
        return indexFieldName;
    }

    public void setIndexFieldName(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }

    public Boolean isUniqueKey() {
        return uniqueKey != null && uniqueKey;
    }

    public void setUniqueKey(Boolean uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public Boolean isDefaultSearchField() {
        return defaultSearchField != null && defaultSearchField;
    }

    public void setDefaultSearchField(Boolean defaultSearchField) {
        this.defaultSearchField = defaultSearchField;
    }

    public Boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(Boolean indexed) {
        this.indexed = indexed;
    }

    public Boolean isMultiValued() {
        return multiValued;
    }

    public void setMultiValued(Boolean multiValued) {
        this.multiValued = multiValued;
    }

    @ManyToOne
    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @ManyToOne
    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    private ConnectorType connectorType;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(ConnectorType connectorType) {
        this.connectorType = connectorType;
    }

    public IndexField createIndexField() {
        IndexField indexField;
        if (uniqueKey != null) {
            indexField = null;
        } else if (defaultSearchField != null) {
            indexField = null;
        } else {
            indexField = new IndexField();
            indexField.setName(indexFieldName);

            if (indexed != null) {
                indexField.setIndexed(indexed);
            }
            if (multiValued != null) {
                indexField.setMultiValued(multiValued);
            }
            if (fieldType != null) {
                indexField.setFieldType(fieldType);
            }
            if (analyzer != null) {
                indexField.setAnalyzer(analyzer);
            }
        }
        return indexField;
    }

}
