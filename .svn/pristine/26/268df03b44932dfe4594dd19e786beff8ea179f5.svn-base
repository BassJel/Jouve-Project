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
package com.doculibre.constellio.entities.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@SuppressWarnings("serial")
public class FacetValue implements Serializable {
	
	public static final String CONCAT_DELIM = ";";

	private SearchableFacet searchableFacet;
	private String value;
	private String valueToClusterLabel = null;
	private int docCount;
	
	private List<FacetValue> subValues = new ArrayList<FacetValue>();

	public FacetValue() {}
	
	public FacetValue(SearchableFacet searchableFacet, String value) {
		this(searchableFacet, value, -1);
	}
	
	public FacetValue(SearchableFacet searchableFacet, String value, int docCount) {
		this.searchableFacet = searchableFacet;
		this.value = value;
		this.docCount = docCount;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getDocCount() {
		return docCount;
	}

	public void setDocCount(int docCount) {
		this.docCount = docCount;
	}

	public SearchableFacet getSearchableFacet() {
		return searchableFacet;
	}

	public void setSearchableFacet(SearchableFacet searchableFacet) {
		this.searchableFacet = searchableFacet;
	}
	
	public String getLabel(Locale locale) {
		if (valueToClusterLabel != null){
			//c un cluster
			return valueToClusterLabel;
		}
		String result;
		if (searchableFacet != null) {
			result = searchableFacet.getPossibleValueLabel(value, locale);
			if (result == null) {
				result = value;
			}
		} else {
			result = value;
		}
		return result;
	}
	
	public List<FacetValue> getSubValues() {
		return subValues;
	}

	public void setSubValues(List<FacetValue> subValues) {
		this.subValues = subValues;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((searchableFacet == null) ? 0 : searchableFacet.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FacetValue other = (FacetValue) obj;
		if (searchableFacet == null) {
			if (other.searchableFacet != null)
				return false;
		} else if (!searchableFacet.equals(other.searchableFacet))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[searchableFacet: " + searchableFacet + ", value: " + value + ", docCount: " + docCount + ", subValues: " + subValues + "]";
	}

	public void setValueToClusterLabel(String valueToClusterLabel) {
		this.valueToClusterLabel = valueToClusterLabel;
	}

	public String getValueToClusterLabel() {
		return valueToClusterLabel;
	}

}
