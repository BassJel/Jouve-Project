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

import java.util.Locale;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.doculibre.constellio.entities.skos.SkosConcept;

@SuppressWarnings("serial")
public class RecordTag extends BaseConstellioEntity {
    
    private Boolean manual;
    
    private Boolean excluded;

    private Record record;
    
    private FreeTextTag freeTextTag;
    
    private SkosConcept skosConcept;

    public Boolean getManual() {
        return manual;
    }

    public void setManual(Boolean manual) {
        this.manual = manual;
    }
    
    @Transient
    public boolean isManual() {
        return Boolean.TRUE.equals(manual);
    }

    public Boolean getExcluded() {
        return excluded;
    }
    
    @Transient
    public boolean isExcluded() {
        return Boolean.TRUE.equals(excluded);
    }

    public void setExcluded(Boolean excluded) {
        this.excluded = excluded;
    }

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public FreeTextTag getFreeTextTag() {
        return freeTextTag;
    }

    public void setFreeTextTag(FreeTextTag freeTextTag) {
        this.freeTextTag = freeTextTag;
    }

    public SkosConcept getSkosConcept() {
        return skosConcept;
    }

    public void setSkosConcept(SkosConcept skosConcept) {
        this.skosConcept = skosConcept;
    }

    /**
     * Utility method
     * 
     * @return
     */
    @Transient
    public String getName(Locale locale) {
        String name;
        if (freeTextTag != null) {
            name = freeTextTag.getFreeText();
        } else if (skosConcept != null) {
            name = skosConcept.getPrefLabel(locale);
        } else {
            name = null;
        }
        return name;
    }

}
