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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@SuppressWarnings("serial")
@Entity
public class Analyzer extends BaseConstellioEntity {

	private TokenizerClass tokenizerClass;

	private AnalyzerClass analyzerClass;

	private Set<AnalyzerFilter> filters = new HashSet<AnalyzerFilter>();

	@ManyToOne
	public TokenizerClass getTokenizerClass() {
		return tokenizerClass;
	}

	public void setTokenizerClass(TokenizerClass tokenizerClass) {
		this.tokenizerClass = tokenizerClass;
	}

	@ManyToOne
	public AnalyzerClass getAnalyzerClass() {
		return analyzerClass;
	}

	public void setAnalyzerClass(AnalyzerClass analyzerClass) {
		this.analyzerClass = analyzerClass;
	}

	@OneToMany(mappedBy = "analyzer", cascade = { CascadeType.ALL }, orphanRemoval = true)
	public Set<AnalyzerFilter> getFilters() {
		return filters;
	}

	public void setFilters(Set<AnalyzerFilter> filters) {
		this.filters = filters;
	}
	
	public void addFilter(AnalyzerFilter filter) {
		this.filters.add(filter);
		filter.setAnalyzer(this);
	}

}
