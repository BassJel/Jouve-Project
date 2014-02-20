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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.doculibre.constellio.entities.BaseConstellioEntity;
import com.doculibre.constellio.entities.ConstellioGroup;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.IndexField;

@SuppressWarnings("serial")
@Entity
public class PolicyACLEntry extends BaseConstellioEntity {

    private String matchRegexp;

    private IndexField indexField;
    
    private PolicyACL policy;

    private Set<PolicyACLEntryUser> entryUsers = new HashSet<PolicyACLEntryUser>();

    private Set<PolicyACLEntryGroup> entryGroups = new HashSet<PolicyACLEntryGroup>();

    public String getMatchRegexp() {
        return matchRegexp;
    }

    public void setMatchRegexp(String regexp) {
        this.matchRegexp = regexp;
    }

    @ManyToOne
    public IndexField getIndexField() {
        return indexField;
    }

    public void setIndexField(IndexField indexField) {
        this.indexField = indexField;
    }

    @ManyToOne
    public PolicyACL getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyACL policy) {
        this.policy = policy;
    }

    @OneToMany(mappedBy="entry", cascade = CascadeType.ALL, orphanRemoval=true)
    public Set<PolicyACLEntryUser> getEntryUsers() {
        return entryUsers;
    }

    public void setEntryUsers(Set<PolicyACLEntryUser> entryUsers) {
        this.entryUsers = entryUsers;
    }
    
    public void addEntryUser(PolicyACLEntryUser entryUser) {
        this.entryUsers.add(entryUser);
        entryUser.setEntry(this);
    }

    @OneToMany(mappedBy="entry", cascade = CascadeType.ALL, orphanRemoval=true)
    public Set<PolicyACLEntryGroup> getEntryGroups() {
        return entryGroups;
    }

    public void setEntryGroups(Set<PolicyACLEntryGroup> entryGroups) {
        this.entryGroups = entryGroups;
    }
    
    public void addEntryGroup(PolicyACLEntryGroup entryGroup) {
        this.entryGroups.add(entryGroup);
        entryGroup.setEntry(this);
    }

    @Transient
    public Set<ConstellioUser> getUsers() {
        Set<ConstellioUser> users = new HashSet<ConstellioUser>();
        for (PolicyACLEntryUser entryUser : getEntryUsers()) {
            users.add(entryUser.getUser());
        }
        return Collections.unmodifiableSet(users);
    }
    
    public void setUsers(Set<ConstellioUser> users) {
        entryUsers.clear();
        for (ConstellioUser user : users) {
            addUser(user);
        }
    }
    
    public void addUser(ConstellioUser user) {
        PolicyACLEntryUser entryUser = new PolicyACLEntryUser();
        entryUser.setUser(user);
        addEntryUser(entryUser);
    }

    @Transient
    public Set<ConstellioGroup> getGroups() {
        Set<ConstellioGroup> groups = new HashSet<ConstellioGroup>();
        for (PolicyACLEntryGroup entryGroup : getEntryGroups()) {
            groups.add(entryGroup.getGroup());
        }
        return Collections.unmodifiableSet(groups);
    }
    
    public void setGroups(Set<ConstellioGroup> groups) {
        entryGroups.clear();
        for (ConstellioGroup group : groups) {
            addGroup(group);
        }
    }
    
    public void addGroup(ConstellioGroup group) {
        PolicyACLEntryGroup entryGroup = new PolicyACLEntryGroup();
        entryGroup.setGroup(group);
        addEntryGroup(entryGroup);
    }
    
    /**
     * Examples : 
     * example.com/docsite user:jane user:sue user:wilson group:chicagodoc group:texasdoc
     * mycompany.com/engsite group:eng
     * mycompany.com/salessite group:sales user:yvette
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(matchRegexp);
        for (ConstellioGroup group : getGroups()) {
            sb.append(" group:" + group.getName());
        }
        for (ConstellioUser user : getUsers()) {
            sb.append(" user:" + user.getUsername());
        }
        return sb.toString();
    }

}
