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

import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;

public interface ElevateServices {
	
	String toElevateQueryId(SimpleSearch simpleSearch);
	
	SimpleSearch toSimpleSearch(String elevateQueryId);
	
	void elevate(Record record, SimpleSearch simpleSearch);
	
	boolean isElevated(Record record, SimpleSearch simpleSearch);
	
	void cancelElevation(Record record, SimpleSearch simpleSearch);
	
	void exclude(Record record, RecordCollection collection, SimpleSearch simpleSearch);
	
	void cancelExclusion(Record record, RecordCollection collection);
	
	void deleteQuery(SimpleSearch simpleSearch);
	
	List<String> getElevatedQueries(Record record, RecordCollection collection);
	
	List<String> getQueries(String collectionName);
	
	List<String> getElevatedDocIds(SimpleSearch simpleSearch);
	
	List<String> getExcludedDocIds(RecordCollection collection);

}
