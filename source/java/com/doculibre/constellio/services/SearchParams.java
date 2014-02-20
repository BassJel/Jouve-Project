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
package com.doculibre.constellio.services;

import java.util.List;

public class SearchParams {
	
	private boolean highlightingEnabled;
	
	private int fragsize = 120;
	
	private int snippets = 3;
	
	private List<String> highlightedFields;

	public int getFragsize() {
		return fragsize;
	}

	public void setFragsize(int fragsize) {
		this.fragsize = fragsize;
	}

	public int getSnippets() {
		return snippets;
	}

	public void setSnippets(int snippets) {
		this.snippets = snippets;
	}

	public List<String> getHighlightedFields() {
		return highlightedFields;
	}

	public void setHighlightedFields(List<String> highlightedFields) {
		this.highlightedFields = highlightedFields;
	}

	public void setHighlightingEnabled(boolean highlightingEnabled) {
		this.highlightingEnabled = highlightingEnabled;
	}

	public boolean isHighlightingEnabled() {
		return highlightingEnabled;
	}
}
