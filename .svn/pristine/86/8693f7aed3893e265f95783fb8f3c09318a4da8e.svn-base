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
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@SuppressWarnings("serial")
@Entity
public class SolrConfig extends BaseConstellioEntity implements Cloneable {
	private Cache filterCacheConfig;
	private Cache queryResultCacheConfig;
	private Cache documentCacheConfig;
	private Cache fieldValueCacheConfig;
	private Boolean useFilterForSortedQuery;
	private Integer queryResultWindowSize;
	private Integer hashDocSetMaxSize;
	private Float hashDocSetLoadFactor;
	private RecordCollection recordCollection;

	@OneToOne(cascade = { CascadeType.ALL })
	public Cache getFilterCacheConfig() {
		return filterCacheConfig;
	}

	public void setFilterCacheConfig(Cache filterCacheConfig) {
		this.filterCacheConfig = filterCacheConfig;
	}

	@OneToOne(cascade = { CascadeType.ALL })
	public Cache getQueryResultCacheConfig() {
		return queryResultCacheConfig;
	}

	public void setQueryResultCacheConfig(Cache queryResultCacheConfig) {
		this.queryResultCacheConfig = queryResultCacheConfig;
	}

	@OneToOne(cascade = { CascadeType.ALL })
	public Cache getDocumentCacheConfig() {
		return documentCacheConfig;
	}

	public void setDocumentCacheConfig(Cache documentCacheConfig) {
		this.documentCacheConfig = documentCacheConfig;
	}

	@OneToOne(cascade = { CascadeType.ALL })
	public Cache getFieldValueCacheConfig() {
		return fieldValueCacheConfig;
	}

	public void setFieldValueCacheConfig(Cache fieldValueCacheConfig) {
		this.fieldValueCacheConfig = fieldValueCacheConfig;
	}

	public Integer getQueryResultWindowSize() {
		return queryResultWindowSize;
	}

	public void setQueryResultWindowSize(Integer queryResultWindowSize) {
		this.queryResultWindowSize = queryResultWindowSize;
	}

	public Integer getHashDocSetMaxSize() {
		return hashDocSetMaxSize;
	}

	public void setHashDocSetMaxSize(Integer hashDocSetMaxSize) {
		this.hashDocSetMaxSize = hashDocSetMaxSize;
	}

	public Float getHashDocSetLoadFactor() {
		return hashDocSetLoadFactor;
	}

	public void setHashDocSetLoadFactor(Float hashDocSetLoadFactor) {
		this.hashDocSetLoadFactor = hashDocSetLoadFactor;
	}

	public void setRecordCollection(RecordCollection recordCollection) {
		this.recordCollection = recordCollection;
	}

	@OneToOne
	public RecordCollection getRecordCollection() {
		return recordCollection;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		SolrConfig clone = (SolrConfig) super.clone();
		clone.setId(null);
		clone.setDocumentCacheConfig(documentCacheConfig == null ? null : (Cache) documentCacheConfig.clone());
		clone.setFieldValueCacheConfig(fieldValueCacheConfig == null ? null : (Cache) fieldValueCacheConfig.clone());
		clone.setFilterCacheConfig(filterCacheConfig == null ? null : (Cache) filterCacheConfig.clone());
		clone.setQueryResultCacheConfig(queryResultCacheConfig == null ? null : (Cache) queryResultCacheConfig.clone());
		return clone;
	}

	public Boolean getUseFilterForSortedQuery() {
		return useFilterForSortedQuery;
	}

	public void setUseFilterForSortedQuery(Boolean useFilterForSortedQuery) {
		this.useFilterForSortedQuery = useFilterForSortedQuery;
	}

}
