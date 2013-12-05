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
package com.doculibre.constellio.entities.relevance;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.doculibre.constellio.entities.BaseConstellioEntity;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;

@SuppressWarnings("serial")
@Entity
public class RecordCollectionBoost extends BaseConstellioEntity {
	
	private String name;

	private RecordCollection recordCollection;

//	private IndexField indexField;
	private IndexField associatedField;
	
	private Set<BoostRule> boostRules = new HashSet<BoostRule>();

	@Column(nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public RecordCollection getRecordCollection() {
		return recordCollection;
	}
	public void setRecordCollection(RecordCollection recordCollection) {
		this.recordCollection = recordCollection;
	}

	@OneToMany(mappedBy = "recordCollectionBoost", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	public Set<BoostRule> getBoostRules() {
		return boostRules;
	}
	
	public void setBoostRules(Set<BoostRule> boostRules) {
		this.boostRules = boostRules;
	}
	
	public void addBoostRule(BoostRule boostRule) {
		this.boostRules.add(boostRule);
		boostRule.setRecordCollectionBoost(this);
	}

	@OneToOne(fetch = FetchType.EAGER)//cascade = CascadeType.ALL, , orphanRemoval = true
	public IndexField getAssociatedField() {
		return associatedField;
	}

	public void setAssociatedField(IndexField associatedField) {
		this.associatedField = associatedField;
	}

}

