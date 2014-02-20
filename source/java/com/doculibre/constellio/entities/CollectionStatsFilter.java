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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@SuppressWarnings("serial")
@Entity
public class CollectionStatsFilter extends BaseConstellioEntity {

	private RecordCollection recordCollection;
	
	private Set<String> queryExcludeRegexps = new HashSet<String>();

    @ManyToOne
	public RecordCollection getRecordCollection() {
		return recordCollection;
	}
	
	public void setRecordCollection(RecordCollection recordCollection) {
		this.recordCollection = recordCollection;
	}

	@ElementCollection 	
    @CollectionTable(name="CollectionStatsFilter_QueryExcludeRegexps", joinColumns=@JoinColumn(name="collectionStatsFilter_id"))
    @Column(name="excludeRegexp")
    public Set<String> getQueryExcludeRegexps() {
		return queryExcludeRegexps;
	}

	public void setQueryExcludeRegexps(Set<String> excludeRegexps) {
		this.queryExcludeRegexps = excludeRegexps;
	}

}
