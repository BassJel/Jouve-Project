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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.lucene.impl.FreeTextTagIndexHelper;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class FreeTextTagServicesImpl extends BaseCRUDServicesImpl<FreeTextTag> implements FreeTextTagServices {

    public FreeTextTagServicesImpl(EntityManager entityManager) {
        super(FreeTextTag.class, entityManager);
    }

    @Override
    public FreeTextTag get(String freeText) {
        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put("freeText", freeText);
        return super.get(criteria);
    }

    @Override
    public FreeTextTag makePersistent(FreeTextTag entity) {
        FreeTextTag result = super.makePersistent(entity);
        FreeTextTagIndexHelper indexHelper = ConstellioSpringUtils.getFreeTextTagIndexHelper();
        indexHelper.addOrUpdate(result);
        return result;
    }

    @Override
    public FreeTextTag makeTransient(FreeTextTag entity) {
//        String qlRecordTag = "DELETE FROM RecordTag WHERE freeTextTag = :freeTextTag";
//        EntityManager entityManager = getEntityManager();
//        Query queryRecordTag = entityManager.createQuery(qlRecordTag);
//        queryRecordTag.setParameter("freeTextTag", entity);
//        queryRecordTag.executeUpdate();

        FreeTextTagIndexHelper indexHelper = ConstellioSpringUtils.getFreeTextTagIndexHelper();
        indexHelper.delete(entity);
        
        FreeTextTag result = super.makeTransient(entity);
        return result;
    }

    @Override
    public Set<FreeTextTag> search(String input) {
        Set<FreeTextTag> returnedSearchResults = new HashSet<FreeTextTag>();
        if (StringUtils.isBlank(input)) {
            // input = "*:*";//FIXME : plutôt *
        }
        FreeTextTagIndexHelper indexHelper = ConstellioSpringUtils.getFreeTextTagIndexHelper();
        List<FreeTextTag> searchResults = indexHelper.search(input);
        for (int i = 0; i < 100 && i < searchResults.size(); i++) {
            FreeTextTag searchResult = searchResults.get(i);
            if (searchResult != null) {
                returnedSearchResults.add(searchResult);
            }
        }
        indexHelper.release(searchResults);
        return returnedSearchResults;
    }

}
