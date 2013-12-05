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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@SuppressWarnings("serial")
@Entity
public class GroupParticipation extends BaseConstellioEntity {
	
	private ConstellioUser constellioUser;
	
	private ConstellioGroup constellioGroup;

	public GroupParticipation() {
		super();
	}

	public GroupParticipation(ConstellioUser user, ConstellioGroup group) {
		super();
		this.constellioUser = user;
		this.constellioGroup = group;
	}

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public ConstellioUser getConstellioUser() {
		return constellioUser;
	}
	
	public void setConstellioUser(ConstellioUser user) {
		this.constellioUser = user;
	}

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public ConstellioGroup getConstellioGroup() {
		return constellioGroup;
	}
	
	public void setConstellioGroup(ConstellioGroup group) {
		this.constellioGroup = group;
	}

}
