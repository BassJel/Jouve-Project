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
package com.doculibre.constellio.entities.acl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.doculibre.constellio.entities.BaseConstellioEntity;
import com.doculibre.constellio.entities.RecordCollection;

@SuppressWarnings("serial")
@Entity
public class PolicyACL extends BaseConstellioEntity {
    
    private String name;
    
    private Date uploadDate;
    
    private RecordCollection recordCollection;
    
    private Set<PolicyACLEntry> entries = new HashSet<PolicyACLEntry>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    @ManyToOne
    public RecordCollection getRecordCollection() {
        return recordCollection;
    }

    public void setRecordCollection(RecordCollection collection) {
        this.recordCollection = collection;
    }

    @OneToMany(mappedBy = "policy", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
    public Set<PolicyACLEntry> getEntries() {
        return entries;
    }

    public void setEntries(Set<PolicyACLEntry> entries) {
        this.entries = entries;
    }
    
    public void addEntry(PolicyACLEntry entry) {
        this.entries.add(entry);
        entry.setPolicy(this);
    }

}
