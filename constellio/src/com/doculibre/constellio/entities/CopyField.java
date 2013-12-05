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
import javax.persistence.ManyToOne;

@SuppressWarnings("serial")
@Entity
public class CopyField extends BaseConstellioEntity {

	private IndexField indexFieldSource;

	private IndexField indexFieldDest;
	
	private Integer maxChars;
	
	private Boolean sourceAllFields = false;

    @ManyToOne 
	public IndexField getIndexFieldSource() {
		return indexFieldSource;
	}
	
	public void setIndexFieldSource(IndexField indexField) {
		this.indexFieldSource = indexField;
		if (indexField != null) {
		    sourceAllFields = false;
		}
	}

	@ManyToOne 
	public IndexField getIndexFieldDest() {
		return indexFieldDest;
	}

	public void setIndexFieldDest(IndexField indexFieldDest) {
		this.indexFieldDest = indexFieldDest;
	}

	public Integer getMaxChars() {
		return maxChars;
	}

	public void setMaxChars(Integer maxChar) {
		this.maxChars = maxChar;
	}

    public Boolean isSourceAllFields() {
        return Boolean.TRUE.equals(sourceAllFields);
    }

    public void setSourceAllFields(Boolean sourceAllFields) {
        this.sourceAllFields = sourceAllFields;
        if (Boolean.TRUE.equals(sourceAllFields)) {
            if (indexFieldSource != null) {
                if (indexFieldSource.getCopyFieldsDest().contains(this)) {
                    indexFieldSource.getCopyFieldsDest().remove(this);
                }
                indexFieldSource = null;
            }
        }
    }
	
}
