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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.io.IOUtils;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SynonymList;
import com.doculibre.constellio.utils.ClasspathUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;

public class SynonymServicesImpl extends BaseCRUDServicesImpl<SynonymList> implements SynonymServices {

	public SynonymServicesImpl(EntityManager entityManager) {
		super(SynonymList.class, entityManager);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getSynonyms(String text, String collectionName) {
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT SLS.synonym FROM SynonymList_Synonyms SLS, SynonymList SL, RecordCollection RC"); 
		sql.append("  WHERE SLS.synonymList_id=SL.id AND SL.recordCollection_id=RC.id"); 
		sql.append(" AND RC.id = (");
		sql.append("   SELECT SL2.recordCollection_id FROM SynonymList_Synonyms SLS2, SynonymList SL2, RecordCollection RC2"); 
		sql.append("   WHERE SLS2.synonymList_id=SL2.id AND SL2.recordCollection_id=RC2.id AND SL2.id = SL.id");
		sql.append("   AND RC.name=? AND SLS2.synonym=?");
		sql.append(" )");

		Query sqlQuery = entityManager.createNativeQuery(sql.toString());
		sqlQuery.setParameter(1, collectionName);
//		RecordCollection collection = ConstellioSpringUtils.getRecordCollectionServices().get(collectionName);
//		Locale locale = collection.getLocales();
		sqlQuery.setParameter(2, text.toLowerCase());
		
		List<String> synonyms = sqlQuery.getResultList();
		return synonyms;
	}
	
	
	@Override
	public List<List<String>> getSynonyms(String collectionName) {
		List<List<String>> result = new ArrayList< List<String>>();
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = collectionServices.get(collectionName);
		if (collection == null){
			return null;
		}
		Set<SynonymList> synonyms = collection.getSynonymLists();
		for (SynonymList syn : synonyms){
			result.add(syn.getSynonyms());
		}
		return result;
	}

	@Override
	public SynonymList makePersistent(SynonymList entity) {
		SynonymList result = super.makePersistent(entity);
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = entity.getRecordCollection();
		collectionServices.makePersistent(collection, false);
		return result;
	}

	@Override
	public SynonymList makeTransient(SynonymList entity) {
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = entity.getRecordCollection();
		collection.getSynonymLists().remove(entity);
		collectionServices.makePersistent(collection, false);
		return entity;
	}

	@Override
	public void writeSynonymsFile(RecordCollection collection) {
		List<List<String>> collectionSynonyms = getSynonyms(collection.getName());
    	List<String> lines = new ArrayList<String>();
    	for (List<String> syn : collectionSynonyms){
    		StringBuilder synDefLine = new StringBuilder();
    		for(String term : syn){
    			synDefLine.append(term + ", ");
    		}
    		String synDefLineString = synDefLine.toString();
    		if (! synDefLineString.isEmpty()){
    			synDefLineString = org.apache.commons.lang.StringUtils.substringBeforeLast(synDefLineString, ",");
    			lines.add(synDefLineString);
    		}
    	}
		//TODO : lors que les filtres auront les parametres necessaires 
    	//Pour chacun des analyseurs qui a un filtre de synonymes : "solr.SynonymFilterFactory"
    	//1. lire le fichier des synonymes (dans conf ou data) (le creer le cas echeant)
		//2. remplacer son contenu avec lines
    	File synonymsFile = ClasspathUtils.getSynonymsFile(collection);
    	
    	FileOutputStream ecraserSynonymsFile = null;
	        
		try {
			ecraserSynonymsFile = new FileOutputStream(synonymsFile.getAbsoluteFile());
			IOUtils.writeLines(lines, System.getProperty("line.separator"), ecraserSynonymsFile);
			ecraserSynonymsFile.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(ecraserSynonymsFile);
		}
	}

}
