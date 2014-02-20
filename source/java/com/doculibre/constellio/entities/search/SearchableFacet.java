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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("serial")
public class SearchableFacet implements Serializable, Cloneable {
	
	public static final String SORT_NB_RESULTS = "sortNbResults";
	public static final String SORT_ALPHA = "sortAlpha";

	private String name;
	private Map<Locale, String> labels = new HashMap<Locale, String>();
	private boolean query;
	private boolean cluster;
	private boolean cloudKeyword;
	private boolean sortable;
	private boolean multiValued;

	private List<FacetValue> values = new ArrayList<FacetValue>();

	private Map<String, Map<Locale, String>> possibleValuesLabels = new HashMap<String, Map<Locale, String>>();

    public SearchableFacet() {
	}

    public SearchableFacet(String name) {
    	this.name = name;
	}

	public SearchableFacet(String name, Map<Locale, String> labels, boolean sortable, boolean multiValued) {
		this.name = name;
		this.labels = labels;
		this.sortable = sortable;
		this.multiValued = multiValued;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Locale, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<Locale, String> labels) {
		this.labels = labels;
	}

	public boolean isQuery() {
		return query;
	}

	public void setQuery(boolean query) {
		this.query = query;
	}

	public boolean isCluster() {
		return cluster;
	}

	public void setCluster(boolean cluster) {
		this.cluster = cluster;
	}

	public boolean isCloudKeyword() {
		return cloudKeyword;
	}

	public void setCloudKeyword(boolean cloudKeyword) {
		this.cloudKeyword = cloudKeyword;
	}

    public void addPossibleValueLabel(String value, String label, Locale locale) {
        Map<Locale, String> valueLabels = getPossibleValuesLabels().get(value);
        if (valueLabels == null) {
            valueLabels = new HashMap<Locale, String>();
            possibleValuesLabels.put(value, valueLabels);
        }
        valueLabels.put(locale, label);
        addPossibleValueLabels(value, valueLabels);
    }

	public void addPossibleValueLabels(String value, Map<Locale, String> labels) {
		possibleValuesLabels.put(value, labels);
	}

	public String getPossibleValueLabel(String valeur, Locale locale) {
		return possibleValuesLabels.get(valeur) == null ? null
				: (String) possibleValuesLabels.get(valeur).get(locale);
	}

	public Map<String, Map<Locale, String>> getPossibleValuesLabels() {
		return possibleValuesLabels;
	}
	
	public List<FacetValue> getValues() {
		return values;
	}

	public void setValues(List<FacetValue> values) {
		this.values = values;
	}

	@Override
	protected SearchableFacet clone() {
		SearchableFacet clone = new SearchableFacet();
		clone.name = name;
		clone.labels = new HashMap<Locale, String>(labels);
		clone.query = query;
		clone.cluster = cluster;
		clone.cloudKeyword = cloudKeyword;
		clone.possibleValuesLabels.putAll(possibleValuesLabels);
		clone.values.addAll(this.values);
		return clone;
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SearchableFacet other = (SearchableFacet) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (other.name == null) {
			return false;
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "[name: " + name + ", labels: " + labels + "]";
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

}
