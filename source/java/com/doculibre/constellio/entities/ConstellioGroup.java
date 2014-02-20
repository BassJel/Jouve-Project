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
import javax.persistence.OneToMany;


@SuppressWarnings("serial")
@Entity
public class ConstellioGroup extends BaseConstellioEntity {
	
	private String name;
	
	private Set<GroupParticipation> participations = new HashSet<GroupParticipation>();
	
	private Set<CollectionPermission> collectionPermissions = new HashSet<CollectionPermission>();
	
	@Column(nullable = false, unique = true)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(mappedBy="constellioGroup", cascade={CascadeType.ALL}, orphanRemoval = true)
	public Set<GroupParticipation> getParticipations() {
		return participations;
	}
	
	public void setParticipations(Set<GroupParticipation> participations) {
		this.participations = participations;
	}
	
	public void addParticipation(GroupParticipation participation) {
		this.participations.add(participation);
		participation.setConstellioGroup(this);
	}

	@OneToMany(mappedBy="constellioGroup", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
	public Set<CollectionPermission> getCollectionPermissions() {
		return collectionPermissions;
	}
	
	public void setCollectionPermissions(Set<CollectionPermission> collectionPermissions) {
		this.collectionPermissions = collectionPermissions;
	}
	
	public void addCollectionPermission(CollectionPermission collectionPermission) {
		this.collectionPermissions.add(collectionPermission);
		collectionPermission.setConstellioGroup(this);
	}
	
}
