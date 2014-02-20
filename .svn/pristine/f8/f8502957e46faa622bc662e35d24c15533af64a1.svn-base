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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@SuppressWarnings("serial")
@Entity
public class Categorization extends BaseConstellioEntity {
	
	private String name;

	private RecordCollection recordCollection;

	private IndexField indexField;
	
	private Set<CategorizationRule> categorizationRules = new HashSet<CategorizationRule>();

	@Column(nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne
	public IndexField getIndexField() {
		return indexField;
	}
	
	public void setIndexField(IndexField indexField) {
		this.indexField = indexField;
	}

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public RecordCollection getRecordCollection() {
		return recordCollection;
	}
	public void setRecordCollection(RecordCollection recordCollection) {
		this.recordCollection = recordCollection;
	}

	@OneToMany(mappedBy = "categorization", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	public Set<CategorizationRule> getCategorizationRules() {
		return categorizationRules;
	}
	
	public void setCategorizationRules(Set<CategorizationRule> categorizationRules) {
		this.categorizationRules = categorizationRules;
	}
	
	public void addCategorizationRule(CategorizationRule categorizationRule) {
		this.categorizationRules.add(categorizationRule);
		categorizationRule.setCategorization(this);
	}
	
}
