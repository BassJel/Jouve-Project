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
package com.doculibre.constellio.wicket.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import com.doculibre.constellio.entities.search.SimpleSearch;

@SuppressWarnings("serial")
public class EmptySearchResultsDataProvider extends SearchResultsDataProvider {
	
	private List<SolrDocument> emptyList = new ArrayList<SolrDocument>();

	public EmptySearchResultsDataProvider(SimpleSearch simpleSearch, int resultsPerPage) {
		super(simpleSearch, resultsPerPage);
	}

	@SuppressWarnings("rawtypes")
	public Iterator iterator(int first, int count) {
		return emptyList.iterator();
	}

	public IModel model(Object object) {
		return new CompoundPropertyModel((SolrDocument) object);
	}

	public int size() {
		return 0;
	}

	public void detach() {
	}

}
