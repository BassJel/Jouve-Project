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

import javax.persistence.EntityManager;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchResultField;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class SearchResultFieldServicesImpl extends BaseCRUDServicesImpl<SearchResultField> implements
    SearchResultFieldServices {

    public SearchResultFieldServicesImpl(EntityManager entityManager) {
        super(SearchResultField.class, entityManager);
    }

    @Override
    public SearchResultField makeTransient(SearchResultField entity) {
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordCollection collection = entity.getRecordCollection();
        collection.getSearchResultFields().remove(entity);
        collectionServices.makePersistent(collection, false);
        return entity;
    }

}
