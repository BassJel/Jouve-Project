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

import com.doculibre.constellio.entities.IndexField;

@SuppressWarnings("serial")
@Entity
public class AdvancedSearchEnabledRule extends BaseConstellioEntity {

	private RecordCollection recordCollection;
	
	private IndexField indexField;
	
	public void setIndexField(IndexField indexField) {
		this.indexField = indexField;
	}

	@ManyToOne
	public IndexField getIndexField() {
		return indexField;
	}

	@ManyToOne
	public RecordCollection getRecordCollection() {
		return recordCollection;
	}

	public void setRecordCollection(RecordCollection recordCollection) {
		this.recordCollection = recordCollection;
	}
	
}
