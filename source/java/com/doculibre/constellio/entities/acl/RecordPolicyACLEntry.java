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

import javax.persistence.ManyToOne;

import com.doculibre.constellio.entities.BaseConstellioEntity;
import com.doculibre.constellio.entities.Record;

@SuppressWarnings("serial")
public class RecordPolicyACLEntry extends BaseConstellioEntity {
    
    private Record record;
    
    private PolicyACLEntry entry;

    @ManyToOne
    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    @ManyToOne
    public PolicyACLEntry getEntry() {
        return entry;
    }

    public void setEntry(PolicyACLEntry entry) {
        this.entry = entry;
    }

}