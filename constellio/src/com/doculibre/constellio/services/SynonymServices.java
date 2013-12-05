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

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SynonymList;

public interface SynonymServices extends BaseCRUDServices<SynonymList> {
	
	List<String> getSynonyms(String text, String collectionName);
	
	List<List<String>> getSynonyms(String collectionName);
	
	//necessaire si : un utilisateur ajoute un analyseur basé sur le filtre des synonymes de solr (il sera donc basé sur le fichier et non la BD)
	void writeSynonymsFile(RecordCollection collection);
	
}
