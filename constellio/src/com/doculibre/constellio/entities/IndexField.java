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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.google.enterprise.connector.spi.SpiConstants;

@SuppressWarnings("serial")
@Entity
public class IndexField extends ConstellioLabelledEntity {

    public static final String PREFIX = "doc_";
    public static final String COLLECTION_ID_FIELD = PREFIX + "collectionId";
    public static final String CONNECTOR_INSTANCE_ID_FIELD = PREFIX + "connectorInstanceId";
    public static final String CONNECTOR_TYPE_ID_FIELD = PREFIX + "connectorTypeId";
    public static final String DEFAULT_SEARCH_FIELD = PREFIX + "defaultSearchField";
    public static final String FREE_TEXT_TAGGING_FIELD = PREFIX + "freeTextTagging";
    public static final String LAST_INDEXED_FIELD = PREFIX + "lastIndexed_dt";
    public static final String LAST_MODIFIED_FIELD = PREFIX + "lastModified_dt";
    public static final String LANGUAGE_FIELD = PREFIX + "language";
    public static final String PARSED_CONTENT_FIELD = PREFIX + "parsedContent";
    public static final String RECORD_ID_FIELD = PREFIX + "recordId";
    public static final String THESAURUS_TAGGING_FIELD = PREFIX + "thesaurusTagging";
    public static final String TITLE_FIELD = PREFIX + "title";
    public static final String UNIQUE_KEY_FIELD = PREFIX + "uniqueKey";
    public static final String URL_FIELD = PREFIX + "url";
    public static final String DISPLAY_URL_FIELD = PREFIX + "displayUrl";
    public static final String MIME_TYPE_FIELD = PREFIX + "mimeType";
    public static final String PUBLIC_RECORD_FIELD = PREFIX + "publicRecord";
    
    public static final String DB_AUTHMETHOD_FIELD = PREFIX + "authmethod";
    public static final String DB_BOOST_FIELD = PREFIX + "boost";
    public static final String DB_LAST_AUTOMATIC_TAGGING_FIELD = PREFIX + "lastAutomaticTagging_dt";
    public static final String DB_LAST_FETCHED_FIELD = PREFIX + "lastFetched_dt";
    public static final String DB_COMPUTE_ACL_ENTRIES_FIELD = PREFIX + "computeACLEntries_bl";
    public static final String DB_DELETED_FIELD = PREFIX + "deleted_bl";
    public static final String DB_EXCLUDED_FIELD = PREFIX + "excluded_bl";
    public static final String DB_EXCLUDED_EFFECTIVE_FIELD = PREFIX + "excludedEffective_bl";
    public static final String DB_UPDATE_INDEX_FIELD = PREFIX + "updateIndex_bl";
    public static final String DB_ACL_ENTRY_FIELD = PREFIX + "aclEntry";
    public static final String DB_DT_FIELD = "*_dt";
    public static final String DB_BL_FIELD = "*_bl";
    public static final String DB_META_CONTENT_FIELD = "metaContent_";
    public static final String DB_META_EXTERNAL_FIELD = "metaExternal_";
    public static final String DB_RECORD_TAG_FREE_FIELD = "recordTag_Free_";
    public static final String DB_RECORD_TAG_SKOS_FIELD = "recordTag_Skos_";
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
    
    // Persistent fields
    
    
    public static final String[] INTERNAL_FIELDS = {
        COLLECTION_ID_FIELD,
        CONNECTOR_INSTANCE_ID_FIELD,
        CONNECTOR_TYPE_ID_FIELD,
        DEFAULT_SEARCH_FIELD,
        FREE_TEXT_TAGGING_FIELD,
        LAST_INDEXED_FIELD,
        LAST_MODIFIED_FIELD,
        LANGUAGE_FIELD,
        PARSED_CONTENT_FIELD,
        RECORD_ID_FIELD,
        THESAURUS_TAGGING_FIELD,
        TITLE_FIELD,
        UNIQUE_KEY_FIELD,
        URL_FIELD,
        DISPLAY_URL_FIELD,
        MIME_TYPE_FIELD,
        PUBLIC_RECORD_FIELD,
        DB_AUTHMETHOD_FIELD,
        DB_BOOST_FIELD,
        DB_LAST_AUTOMATIC_TAGGING_FIELD ,
        DB_LAST_FETCHED_FIELD,
        DB_COMPUTE_ACL_ENTRIES_FIELD,
        DB_DELETED_FIELD,
        DB_EXCLUDED_FIELD,
        DB_EXCLUDED_EFFECTIVE_FIELD,
        DB_UPDATE_INDEX_FIELD,
        DB_ACL_ENTRY_FIELD,
        DB_META_CONTENT_FIELD,
        DB_META_EXTERNAL_FIELD,
        DB_RECORD_TAG_FREE_FIELD,
        DB_RECORD_TAG_SKOS_FIELD
    };
    
	public static final List<String> DEFAULT_CONSIDERED_METAS = Arrays.asList(new String[]{
			SpiConstants.PROPNAME_CONTENT,
			SpiConstants.PROPNAME_DOCID
	});
    
    public static final String LABEL_TITLE = "label";
    
    private static final char[] FORBIDDEN_CHARS = { '+', '-', '&', '|', '!', '(', ')', '{', '}',
        '[', ']', '^', '"', '~', '*', '?', ':', '\\' };

    private boolean internalField;
    
    private String name;

    private boolean dynamicField;

    private boolean indexed;

    private boolean multiValued;
	
	private boolean sortable;
	   
	private boolean highlighted = true;
	
	private Float boost = 1.f;
	
	private Float boostDismax = 1.f;

	private FieldType fieldType;

    private Analyzer analyzer;

    private RecordCollection recordCollection;
    
    private Boolean autocompleted = false;
    
    private IndexField autocompleteDestination = null;
    
    private Set<SearchResultField> searchResultFields = new HashSet<SearchResultField>();

    @OneToOne(orphanRemoval=true)
	public IndexField getAutocompleteDestination() {
		return autocompleteDestination;
	}

	public void setAutocompleteDestination(IndexField autocompleteDestination) {
		this.autocompleteDestination = autocompleteDestination;
	}

	public Boolean getAutocompleted() {
		return autocompleted;
	}

	private Set<Categorization> categorizations = new HashSet<Categorization>();
    
    private Set<IndexFieldMetaMapping> metaMappings = new HashSet<IndexFieldMetaMapping>();

    private Set<CopyField> copyFieldsSource = new HashSet<CopyField>();

    private Set<CopyField> copyFieldsDest = new HashSet<CopyField>();

	private Set<I18NLabel> labels = new HashSet<I18NLabel>();
    
    private Set<I18NLabel> labelledValues = new HashSet<I18NLabel>();
    
    /**
     * Constellio mechanic dependent field. Will not be editable in the administration console
     * 
     * @return
     */
    public boolean isInternalField() {
        return internalField;
    }

    public void setInternalField(boolean internalField) {
        this.internalField = internalField;
    }

    @Transient
    public boolean isAnalyzingCustomizable() {
        boolean analyzingCustomizable;
        if (isInternalField()) {
            if (isParsedContentField() || isDefaultSearchField() || isTitleField()) {
                analyzingCustomizable = true;
            } else {
                analyzingCustomizable = false;
            }
        } else {
            analyzingCustomizable = true;
        }
        return analyzingCustomizable;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDynamicField() {
        return dynamicField;
    }

    public void setDynamicField(boolean dynamicField) {
        this.dynamicField = dynamicField;
    }

    @Transient
    public boolean isUniqueKey() {
        return UNIQUE_KEY_FIELD.equals(name);
    }

    @Transient
    public boolean isParsedContentField() {
        return PARSED_CONTENT_FIELD.equals(name);
    }

    @Transient
    public boolean isDefaultSearchField() {
        return DEFAULT_SEARCH_FIELD.equals(name);
    }

    @Transient
    public boolean isTitleField() {
        return TITLE_FIELD.equals(name);
    }

    @Column(nullable = false)
    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    @Column(nullable = false)
    public boolean isMultiValued() {
        return multiValued;
    }

    public void setMultiValued(boolean multiValued) {
        this.multiValued = multiValued;
    }

	public boolean isSortable() {
		return sortable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public void setBoost(Float boost) {
		this.boost = boost;
	}

	public Float getBoost() {
		if (boost == null){
			return 1.0F;
		} else {
			return boost;
		}
	}
	
    public Float getBoostDismax() {
		if (boostDismax == null){
			return 1.0F;
		} else {
			return boostDismax;
		}
	}

	public void setBoostDismax(Float dismaxBoost) {
		this.boostDismax = dismaxBoost;
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

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    public RecordCollection getRecordCollection() {
        return recordCollection;
    }

    public void setRecordCollection(RecordCollection recordCollection) {
        this.recordCollection = recordCollection;
    }

    @OneToMany(mappedBy = "indexField", cascade = { CascadeType.ALL }, orphanRemoval = true)
    public Set<Categorization> getCategorizations() {
        return categorizations;
    }

    public void setCategorizations(Set<Categorization> categorizations) {
        this.categorizations = categorizations;
    }
    
    public void addCategorization(Categorization categorization) {
        this.categorizations.add(categorization);
        categorization.setIndexField(this);
    }

    @OneToMany(mappedBy = "indexField", cascade = { CascadeType.ALL }, orphanRemoval = true)
    public Set<IndexFieldMetaMapping> getMetaMappings() {
        return metaMappings;
    }

    public void setMetaMappings(Set<IndexFieldMetaMapping> metaMappings) {
        this.metaMappings = metaMappings;
    }

    public void addConnectorInstanceMeta(ConnectorInstanceMeta connectorInstanceMeta) {
        IndexFieldMetaMapping metaMapping = new IndexFieldMetaMapping();
        metaMapping.setMeta(connectorInstanceMeta);
        metaMapping.setIndexField(this);
        metaMappings.add(metaMapping);
    }
    
    public void removeConnectorInstanceMeta(ConnectorInstanceMeta connectorInstanceMeta) {
        for (Iterator<IndexFieldMetaMapping> it = metaMappings.iterator(); it.hasNext();) {
            IndexFieldMetaMapping metaMapping = it.next();
            ConnectorInstanceMeta meta = metaMapping.getMeta();
            if (meta.equals(connectorInstanceMeta)) {
                it.remove();
            }
        }
    }

    public Set<ConnectorInstanceMeta> getConnectorInstanceMetas(ConnectorInstance connectorInstance) {
        Set<ConnectorInstanceMeta> matchingMetas = new HashSet<ConnectorInstanceMeta>();
        for (IndexFieldMetaMapping metaMapping : metaMappings) {
            ConnectorInstanceMeta meta = metaMapping.getMeta();
            if (connectorInstance.equals(meta.getConnectorInstance())) {
                matchingMetas.add(meta);
            }
        }
        return Collections.unmodifiableSet(matchingMetas);
    }

    @Transient
    public Set<ConnectorInstanceMeta> getConnectorInstanceMetas() {
        Set<ConnectorInstanceMeta> metas = new HashSet<ConnectorInstanceMeta>();
        for (IndexFieldMetaMapping metaMapping : metaMappings) {
            ConnectorInstanceMeta meta = metaMapping.getMeta();
            metas.add(meta);
        }
        return Collections.unmodifiableSet(metas);
    }

    @Transient
    public Set<String> getMetaNames() {
        Set<String> metaNames = new HashSet<String>();
        for (IndexFieldMetaMapping metaMapping : metaMappings) {
            ConnectorInstanceMeta meta = metaMapping.getMeta();
            metaNames.add(meta.getName());
        }
        return metaNames;
    }
    
	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "IndexField_Labels", joinColumns = { @JoinColumn(name = "indexField_id") }, inverseJoinColumns = { @JoinColumn(name = "label_id") })
	public Set<I18NLabel> getLabels() {
		return this.labels;
	}
	
	@Override
	protected void setLabels(Set<I18NLabel> labels) {
		this.labels = labels;		
	}

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(name = "IndexField_LabelledValues", joinColumns = { @JoinColumn(name = "indexField_id") }, inverseJoinColumns = { @JoinColumn(name = "label_id") })
    public Set<I18NLabel> getLabelledValues() {
        return this.labelledValues;
    }

    protected void setLabelledValues(Set<I18NLabel> labelledValues) {
        this.labelledValues = labelledValues;
    }
    
    public String getLabelledValue(String key, Locale locale) {
        I18NLabel matchingLabel = null;
        for (I18NLabel label : getLabelledValues()) {
            if (label.getKey().equals(key)) {
                matchingLabel = label;
                break;
            }
        }
        return matchingLabel != null ? matchingLabel.getValue(locale) : null;
    }
    
    public void setLabelledValue(String key, String value, Locale locale) {
        I18NLabel matchingLabel = null;
        for (I18NLabel label : getLabelledValues()) {
            if (label.getKey().equals(key)) {
                matchingLabel = label;
                break;
            }
        }
        if (matchingLabel == null) {
            matchingLabel = new I18NLabel();
            matchingLabel.setKey(key);
            this.getLabelledValues().add(matchingLabel);
        }
        matchingLabel.setValue(value, locale);
    }
	
	@Transient
	public String getTitle(Locale locale) {
        return getLabel(LABEL_TITLE, locale);
	}
	
    /**
     * Copy fields that copy content from the current index field.
     * 
     * @return
     */
    @OneToMany(mappedBy = "indexFieldSource", cascade = { CascadeType.ALL }, orphanRemoval = true)
    public Set<CopyField> getCopyFieldsSource() {
        return copyFieldsSource;
    }

    public void setCopyFieldsSource(Set<CopyField> copyFieldSources) {
        this.copyFieldsSource = copyFieldSources;
    }

    public void addCopyFieldSource(CopyField copyFieldSource) {
        this.copyFieldsSource.add(copyFieldSource);
        copyFieldSource.setIndexFieldSource(this);
    }

    public void addCopyFieldSource(IndexField copyIndexFieldDest) {
        CopyField copyFieldSource = new CopyField();
        copyFieldSource.setIndexFieldDest(copyIndexFieldDest);
        this.copyFieldsSource.add(copyFieldSource);
        copyFieldSource.setIndexFieldSource(this);
    }

    /**
     * Copy fields that copy content in the current index field.
     * 
     * @return
     */
    @OneToMany(mappedBy = "indexFieldDest", cascade = { CascadeType.ALL }, orphanRemoval = true)
    public Set<CopyField> getCopyFieldsDest() {
        return copyFieldsDest;
    }

    public void setCopyFieldsDest(Set<CopyField> copyFieldsDest) {
        this.copyFieldsDest = copyFieldsDest;
    }

    public void addCopyFieldDest(CopyField copyFieldDest) {
        this.copyFieldsDest.add(copyFieldDest);
        copyFieldDest.setIndexFieldDest(this);
    }

    public void addCopyFieldDest(IndexField copyIndexFieldSource) {
        CopyField copyFieldDest = new CopyField();
        copyFieldDest.setIndexFieldSource(copyIndexFieldSource);
        this.copyFieldsDest.add(copyFieldDest);
        copyFieldDest.setIndexFieldDest(this);
    }

    public void addCopyFieldDestSourceAll() {
        CopyField copyFieldDest = new CopyField();
        copyFieldDest.setSourceAllFields(true);
        this.copyFieldsDest.add(copyFieldDest);
        copyFieldDest.setIndexFieldDest(this);
    }
    
    public static boolean isValidName(String name) {
        return StringUtils.containsNone(name, FORBIDDEN_CHARS);
    }
    
    public static String normalize(String name) {
        String normalizedName = name;
        if (!isValidName(name)) {
            for (char forbiddenChar : FORBIDDEN_CHARS) {
                normalizedName = normalizedName.replace(forbiddenChar, '_');
            }
        }
        return normalizedName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        IndexField other = (IndexField) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

	public boolean isHighlighted() {
		return highlighted;
	}

	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}


	public void setAutocompleted(Boolean autocompleted) {
		this.autocompleted = autocompleted;
		this.autocompleteDestination = null;
	}

	public Boolean isAutocompleted() {
		return autocompleted;
	}

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<SearchResultField> getSearchResultFields() {
        return searchResultFields;
    }

    public void setSearchResultFields(Set<SearchResultField> searchResultFields) {
        this.searchResultFields = searchResultFields;
    }

}
