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
import java.util.Locale;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;

public interface AutocompleteServices {
	/**
	 * the sort of the suggested values
	 * count => according to their frequency
	 * index => according to their order in solr index
	 */
	final String SORT_ORDER = "count";  
	//Max terms suggested 
	final String TERMS_LIMIT = "10";
	//Fréquence min du term dans l'index pour l'afficher dans la liste de l'autocomplete
	final String TERMS_MINCOUNT = "1";
	
	
	
	/**
	 * Suggests at max MAX_TERMS terms sorted according to SORT_ORDER for term that begins with
	 * term.  
	 * @param term 
	 * @param indexField - the search will be based on this field
	 * @return ordred list of terms suggested : use .getName(i) and .getVal(i) resp. to get term i and its frequency in the index 
	 */
	NamedList<Object> suggest(String term, IndexField indexField);
	
	Boolean setAutoCompleteToField(IndexField indexField);
	
	Boolean removeAutoCompleteFromField(IndexField indexField);


	void onDocumentAddToAutoCompleteField(SolrInputDocument doc, IndexField indexField, Record record);
	
	//Pour gerer l'autocomplete sur les requetes les plus populaires (traitement lié à l'index des stats)
	void onQueryAdd(SolrInputDocument docCompile, String query);


	void onAutoCompleteFieldTypeChanged(IndexField indexField);
	
    List<IndexField> getAutoCompleteIndexFields(RecordCollection collection);
	
	//Pour gerer l'autocomplete sur les requetes les plus populaires (traitement lié à l'index des stats)
	List<String> suggestSimpleSearch(String term, RecordCollection collection, Locale locale);
    
    void blacklistAutocomplete(String term);
    
    List<String> getBlacklistedAutocompleteTerms();
    
    void cancelBlacklistedAutocomplete(String term);

}
