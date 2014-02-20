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

import java.math.BigInteger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import com.doculibre.constellio.entities.FeaturedLink;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.utils.AnalyzerUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;

public class FeaturedLinkServicesImpl extends BaseCRUDServicesImpl<FeaturedLink> implements FeaturedLinkServices {

	public FeaturedLinkServicesImpl(EntityManager entityManager) {
		super(FeaturedLink.class, entityManager);
	}

	@Override
	public FeaturedLink suggest(String text, RecordCollection collection) {		    
        FeaturedLink suggestion;
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        
        String analyzedText;
        try {
			analyzedText =  AnalyzerUtils.analyze(text, collection);
		} catch (Exception e) {
			analyzedText = null;
		}
        
        if (StringUtils.isNotBlank(analyzedText)) {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT FL.id FROM FeaturedLink FL, FeaturedLink_KeywordsAnalyzed FLK, RecordCollection RC");
            sql.append(" WHERE FLK.FeaturedLink_id=FL.id AND FL.RecordCollection_id=RC.id");
            sql.append(" AND RC.id=? AND FLK.keyword=?");
            
            Query sqlQuery = entityManager.createNativeQuery(sql.toString());
            sqlQuery.setMaxResults(1);
            sqlQuery.setParameter(1, collection.getId());
            sqlQuery.setParameter(2, analyzedText);
            
            try {
                BigInteger featuredLinkId = (BigInteger) sqlQuery.getSingleResult();
                suggestion = get(featuredLinkId.longValue());
            } catch (NoResultException e) {
                suggestion = null;
            }
        } else {
        	suggestion = null;
        }
        return suggestion;
    }

	@Override
	public FeaturedLink makePersistent(FeaturedLink entity) {
		FeaturedLink result = super.makePersistent(entity);
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = entity.getRecordCollection();
		collectionServices.makePersistent(collection, false);
		return result;
	}

	@Override
	public FeaturedLink makeTransient(FeaturedLink entity) {
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = entity.getRecordCollection();
		collection.getFeaturedLinks().remove(entity);
		collectionServices.makePersistent(collection, false);
		return entity;
	}

}
