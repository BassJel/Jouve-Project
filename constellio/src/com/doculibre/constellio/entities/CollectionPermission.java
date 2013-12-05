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
public class CollectionPermission extends BaseConstellioEntity {
	
	private ConstellioGroup constellioGroup;
	
	private ConstellioUser constellioUser;

	private RecordCollection recordCollection; 
	
	private boolean search;
    
    private boolean collaboration;

    private boolean admin;

	@ManyToOne
	public ConstellioGroup getConstellioGroup() {
		return constellioGroup;
	}

	public void setConstellioGroup(ConstellioGroup constellioGroup) {
		this.constellioGroup = constellioGroup;
		if (constellioGroup != null) {
			setConstellioUser(null);
		}
	}

	@ManyToOne
	public ConstellioUser getConstellioUser() {
		return constellioUser;
	}

	public void setConstellioUser(ConstellioUser constellioUser) {
		this.constellioUser = constellioUser;
		if (constellioUser != null) {
			setConstellioGroup(null);
		}
	}

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public RecordCollection getRecordCollection() {
		return recordCollection;
	}

	public void setRecordCollection(RecordCollection recordCollection) {
		this.recordCollection = recordCollection;
	}

	public boolean isSearch() {
		return search;
	}

	public void setSearch(boolean search) {
		this.search = search;
	}
    
    public boolean isCollaboration() {
        return collaboration;
    }

    public void setCollaboration(boolean collaboration) {
        this.collaboration = collaboration;
    }

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

}
