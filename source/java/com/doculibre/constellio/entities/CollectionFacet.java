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
import java.util.Locale;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@SuppressWarnings("serial")
@Entity
public class CollectionFacet extends ConstellioLabelledEntity {

	public static final String FIELD_FACET = "field";
	public static final String QUERY_FACET = "query";
	public static final String CLUSTER_FACET = "cluster";
	public static final String CLOUD_KEYWORD_FACET = "cloudKeyword";
	public static final String[] FACET_TYPES = { FIELD_FACET, QUERY_FACET , CLUSTER_FACET/*, CLOUD_KEYWORD_FACET*/ };
	
	public static final String CLUSTERING_ENGINE_LINGO = "lingo"; 
	//FIXME Disabled
	//public static final String CLUSTERING_ENGINE_LINGO3G = "lingo3g"; 
	public static final String CLUSTERING_ENGINE_STC = "stc"; 
	public static final String[] CLUSTERING_ENGINES = { CLUSTERING_ENGINE_LINGO, /*CLUSTERING_ENGINE_LINGO3G,*/ CLUSTERING_ENGINE_STC };
	
	private static final String LABEL_NAME = "name";

	private String facetType;
	
	private String clusteringEngine = CLUSTERING_ENGINE_LINGO;

	private IndexField facetField;
	
	private boolean clusteringUseSearchResults = true;

	private boolean clusteringUseCollection = false;

	private boolean clusteringUseDocSet = false;

	private boolean carrotProduceSummary = false;
	
	private boolean sortable = true;
	
	private boolean multiValued = true;
	
	private boolean pageable = true;

    private boolean hideEmptyValues = true;

    private int carrotNumDescriptions = 100;
	
	private boolean carrotOutputSubclusters = true;

	private IndexField carrotTitleField;

	private IndexField carrotUrlField;

	private IndexField carrotSnippetField;

	private RecordCollection recordCollection;
	
	private Set<I18NLabel> labels = new HashSet<I18NLabel>();
	
	private Set<I18NLabel> labelledValues = new HashSet<I18NLabel>();

	@Column(nullable = false, updatable = false)
	public String getFacetType() {
		return facetType;
	}

	public void setFacetType(String facetType) {
		if (facetType != null) {
			boolean valid = false;
			for (int i = 0; i < FACET_TYPES.length; i++) {
				String validFacetType = FACET_TYPES[i];
				if (validFacetType.equals(facetType)) {
					valid = true;
					break;
				}
			}
			if (!valid) {
				throw new RuntimeException("Invalid facet type : " + facetType);
			}
		} 
		this.facetType = facetType;
	}
	
	@Transient
	public boolean isFieldFacet() {
		return facetType != null && FIELD_FACET.equals(facetType);
	}
	
	@Transient
	public boolean isQueryFacet() {
		return facetType != null && QUERY_FACET.endsWith(facetType);
	}
	
	@Transient
	public boolean isCloudKeywordFacet() {
		return facetType != null && CLOUD_KEYWORD_FACET.endsWith(facetType);
	}
	
	@Transient
	public boolean isClusterFacet() {
		return facetType != null && CLUSTER_FACET.endsWith(facetType);
	}

	@ManyToOne
	public IndexField getFacetField() {
		return facetField;
	}

	public void setFacetField(IndexField facetField) {
		this.facetField = facetField;
	}

	@ManyToOne
	public IndexField getCarrotTitleField() {
		return carrotTitleField;
	}

	public void setCarrotTitleField(IndexField clusterTitleField) {
		this.carrotTitleField = clusterTitleField;
	}

	@ManyToOne
	public IndexField getCarrotUrlField() {
		return carrotUrlField;
	}

	public void setCarrotUrlField(IndexField clusterUrlField) {
		this.carrotUrlField = clusterUrlField;
	}

	public String getClusteringEngine() {
		return clusteringEngine;
	}

	public void setClusteringEngine(String carrotAlgorithm) {
		this.clusteringEngine = carrotAlgorithm;
	}
	
	
	public boolean isClusteringUseSearchResults() {
		return clusteringUseSearchResults;
	}

	public void setClusteringUseSearchResults(boolean clusteringUseSearchResults) {
		this.clusteringUseSearchResults = clusteringUseSearchResults;
	}

	public boolean isClusteringUseCollection() {
		return clusteringUseCollection;
	}

	public void setClusteringUseCollection(boolean clusteringUseCollection) {
		this.clusteringUseCollection = clusteringUseCollection;
	}

	public boolean isClusteringUseDocSet() {
		return clusteringUseDocSet;
	}

	public void setClusteringUseDocSet(boolean clusteringUseDocSet) {
		this.clusteringUseDocSet = clusteringUseDocSet;
	}

	public boolean isCarrotProduceSummary() {
		return carrotProduceSummary;
	}

	public void setCarrotProduceSummary(boolean carrotProduceSummary) {
		this.carrotProduceSummary = carrotProduceSummary;
	}

	public int getCarrotNumDescriptions() {
		return carrotNumDescriptions;
	}

	public void setCarrotNumDescriptions(int carrotNumDescriptions) {
		this.carrotNumDescriptions = carrotNumDescriptions;
	}

	public boolean isCarrotOutputSubclusters() {
		return carrotOutputSubclusters;
	}

	public void setCarrotOutputSubclusters(boolean carrotOutputSubclusters) {
		this.carrotOutputSubclusters = carrotOutputSubclusters;
	}

	@ManyToOne
	public IndexField getCarrotSnippetField() {
		return carrotSnippetField;
	}

	public void setCarrotSnippetField(IndexField carrotSnippetField) {
		this.carrotSnippetField = carrotSnippetField;
	}

    @ManyToOne
    @JoinColumn(name="recordCollection_id", insertable=false, updatable=false, nullable=false)
	public RecordCollection getRecordCollection() {
		return recordCollection;
	}
	
	public void setRecordCollection(RecordCollection recordCollection) {
		this.recordCollection = recordCollection;
	}

	@Override
	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "CollectionFacet_Labels", joinColumns = { @JoinColumn(name = "collectionFacet_id") }, inverseJoinColumns = { @JoinColumn(name = "label_id") })
	public Set<I18NLabel> getLabels() {
		return this.labels;
	}

	@Override
	protected void setLabels(Set<I18NLabel> labels) {
		this.labels = labels;
	}

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "CollectionFacet_LabelledValues", joinColumns = { @JoinColumn(name = "collectionFacet_id") }, inverseJoinColumns = { @JoinColumn(name = "label_id") })
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

	public String getName(Locale locale) {
		return getLabel(LABEL_NAME, locale);
	}

	public void setName(String value, Locale locale) {
		setLabel(LABEL_NAME, value, locale);
	}

	public boolean isSortable() {
		return sortable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}

	public boolean isMultiValued() {
		return multiValued;
	}
    
    public boolean isPageable() {
        return pageable;
    }

    public void setPageable(boolean pageable) {
        this.pageable = pageable;
    }
    
    public boolean isHideEmptyValues() {
        return hideEmptyValues;
    }

    public void setHideEmptyValues(boolean hideEmptyValues) {
        this.hideEmptyValues = hideEmptyValues;
    }

}
