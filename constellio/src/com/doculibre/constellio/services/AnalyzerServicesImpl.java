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
import javax.persistence.Query;

import com.doculibre.constellio.entities.Analyzer;

public class AnalyzerServicesImpl extends BaseCRUDServicesImpl<Analyzer> implements AnalyzerServices {

	public AnalyzerServicesImpl(EntityManager entityManager) {
		super(Analyzer.class, entityManager);
	}

	@Override
	public boolean isRemoveable(Analyzer t) {
		Query query1 = getEntityManager().createQuery("from AnalyzerFilter a where analyzer_id=:id");
		query1.setParameter("id", t.getId());
		query1.setMaxResults(1);
		boolean noAnalyzerFilterAttached = query1.getResultList().isEmpty();
		
		Query query2 = getEntityManager().createQuery("from FieldType a where analyzer_id=:id or queryAnalyzer_id=:id");
		query2.setParameter("id", t.getId());
		query2.setMaxResults(1);
		boolean noFieldTypeAttached = query2.getResultList().isEmpty();
		
		return noAnalyzerFilterAttached && noFieldTypeAttached;
	}

}
