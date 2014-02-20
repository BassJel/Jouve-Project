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
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@SuppressWarnings("serial")
@Entity
public class CategorizationRule extends BaseConstellioEntity {

	private String matchRegexp;

	private IndexField indexField;
	
	private Categorization categorization;

	private Set<String> matchRegexpIndexedValues = new HashSet<String>();

	@Column(nullable = false)
	public String getMatchRegexp() {
		return matchRegexp;
	}
	
	public void setMatchRegexp(String matchRegexp) {
		this.matchRegexp = matchRegexp;
	}

	/**
	 * Normally, we would have JoinColumn(nullable = false, updatable = false)
	 * 
	 * However, for cascade delete purposes, we cannot use these.
	 * 
	 * @return
	 */
	@ManyToOne
	public IndexField getIndexField() {
		return indexField;
	}
	
	public void setIndexField(IndexField indexField) {
		this.indexField = indexField;
	}

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public Categorization getCategorization() {
		return categorization;
	}
	
	public void setCategorization(Categorization subindexField) {
		this.categorization = subindexField;
	}

	@ElementCollection 	
    @CollectionTable(name="CategorizationRule_IndexedValues", joinColumns=@JoinColumn(name="categorizationRule_id"))
    @Column(name="indexedValue")
	public Set<String> getMatchRegexpIndexedValues() {
		return matchRegexpIndexedValues;
	}
	
	public void setMatchRegexpIndexedValues(Set<String> matchRegexpIndexedValues) {
		this.matchRegexpIndexedValues = matchRegexpIndexedValues;
	}
	
	public void addMatchRegexpIndexedValue(String matchRegexpIndexValue) {
		this.matchRegexpIndexedValues.add(matchRegexpIndexValue);
	}

	//FIXME Don't rely on indexField.hashCode() (external object)
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((indexField == null) ? 0 : indexField.hashCode());
		result = prime * result
				+ ((matchRegexp == null) ? 0 : matchRegexp.hashCode());
		result = prime
				* result
				+ ((matchRegexpIndexedValues == null) ? 0
						: matchRegexpIndexedValues.hashCode());
		return result;
	}

	//FIXME Don't rely on indexField and matchRegexpIndexedValues (external object)
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (super.equals(obj))
			return true;
		if (getClass() != obj.getClass())
			return false;
		CategorizationRule other = (CategorizationRule) obj;
		if (indexField == null) {
			if (other.indexField != null)
				return false;
		} else if (!indexField.equals(other.indexField))
			return false;
		if (matchRegexp == null) {
			if (other.matchRegexp != null)
				return false;
		} else if (!matchRegexp.equals(other.matchRegexp))
			return false;
		if (matchRegexpIndexedValues == null) {
			if (other.matchRegexpIndexedValues != null)
				return false;
		} else if (!matchRegexpIndexedValues
				.equals(other.matchRegexpIndexedValues))
			return false;
		return true;
	}
	
}
