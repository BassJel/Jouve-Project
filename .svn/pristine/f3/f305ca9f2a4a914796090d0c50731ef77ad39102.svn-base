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

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.doculibre.constellio.entities.BaseConstellioEntity;
import com.doculibre.constellio.entities.RecordCollection;

@SuppressWarnings("serial")
@Entity
public class ResultsRelevance extends BaseConstellioEntity {
	private boolean active = false;
	
	private int minClicks;
	
	private int maxResults;

	private RecordCollection recordCollection;



	@OneToOne
	public RecordCollection getRecordCollection() {
		return recordCollection;
	}
	
	public void setRecordCollection(RecordCollection recordCollection) {
		this.recordCollection = recordCollection;
	}


	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}
	public void setMinClicks(int minClicks) {
		this.minClicks = minClicks;
	}
	public int getMinClicks() {
		return minClicks;
	}
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
	public int getMaxResults() {
		return maxResults;
	}


}
