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

import com.doculibre.constellio.entities.Categorization;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class CategorizationServicesImpl extends BaseCRUDServicesImpl<Categorization> implements CategorizationServices {
	
	public CategorizationServicesImpl(EntityManager entityManager) {
		super(Categorization.class, entityManager);
	}
	
	@Override
	public Categorization makePersistent(Categorization entity) {
		Categorization result = super.makePersistent(entity);
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = entity.getRecordCollection();
		collectionServices.makePersistent(collection, false);
		return result;
	}

	@Override
	public Categorization makeTransient(Categorization entity) {
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = entity.getRecordCollection();
		collection.getCategorizations().remove(entity);
		collectionServices.makePersistent(collection, false);
		return entity;
	}
	
}
