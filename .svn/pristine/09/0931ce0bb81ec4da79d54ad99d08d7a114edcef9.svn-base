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

@SuppressWarnings("serial")
public class SearchedFacet implements Serializable, Cloneable {
	
	private SearchableFacet searchableFacet;

	private List<String> includedValues = new ArrayList<String>();

	private List<String> excludedValues = new ArrayList<String>();
	
	private List<String> clustersLabels = new ArrayList<String>();

	public SearchedFacet(SearchableFacet searchableFacet) {
		this.searchableFacet = searchableFacet;
	}

	public SearchableFacet getSearchableFacet() {
		return searchableFacet;
	}
	
	public List<String> getIncludedValues() {
		return includedValues;
	}
	
	public List<String> getExcludedValues() {
		return excludedValues;
	}
	
	public List<String> getClustersLabels() {
		return clustersLabels;
	}

	@Override
	protected SearchedFacet clone() {
		SearchableFacet cloneSearchableFacet = searchableFacet.clone();
		SearchedFacet clone = new SearchedFacet(cloneSearchableFacet);
		clone.includedValues.addAll(includedValues);
		clone.excludedValues.addAll(excludedValues);
		clone.clustersLabels.addAll(clustersLabels);
		return clone;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("[searchableFacet: ");
		sb.append(searchableFacet.toString());
		sb.append(", includedValues: ");
		sb.append(includedValues.toString());
		sb.append(", excludedValues: ");
		sb.append(excludedValues.toString());
		sb.append(", clustersLabels: ");
		sb.append(clustersLabels.toString());
		sb.append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((searchableFacet == null) ? 0 : searchableFacet.hashCode());
		result = prime * result + ((excludedValues == null) ? 0 : excludedValues.hashCode());
		result = prime * result + ((includedValues == null) ? 0 : includedValues.hashCode());
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
		SearchedFacet other = (SearchedFacet) obj;
		if (searchableFacet == null) {
			if (other.searchableFacet != null)
				return false;
		} else if (!searchableFacet.equals(other.searchableFacet))
			return false;
		if (excludedValues == null) {
			if (other.excludedValues != null)
				return false;
		} else if (!excludedValues.equals(other.excludedValues))
			return false;
		if (includedValues == null) {
			if (other.includedValues != null)
				return false;
		} else if (!includedValues.equals(other.includedValues))
			return false;
		return true;
	}

}
