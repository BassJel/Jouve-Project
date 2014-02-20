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
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.FilterClass;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class FilterClassServicesImpl extends BaseCRUDServicesImpl<FilterClass> implements FilterClassServices {

	public FilterClassServicesImpl(EntityManager entityManager) {
		super(FilterClass.class, entityManager);
	}
	
	@Override
	public FilterClass get(String className) {
		Map<String, Object> criteria = new HashMap<String, Object>();
		criteria.put("className", className);
		return get(criteria);
	}

	@Override
	public void init() {
		List<FilterClass> entities = list();
		if (entities.isEmpty()) {
			ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
			ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
			for (String filterClassName : FilterClass.DEFAULT_VALUES) {
				FilterClass filterClass = new FilterClass();
				filterClass.setClassName(filterClassName);
				filterClass.setConnectorManager(connectorManager);
				makePersistent(filterClass);
			}
		}
	}

	@Override
	public boolean isRemoveable(FilterClass t) {
		Query query = getEntityManager().createQuery("from AnalyzerFilter a where filterClass_id=:id");
		query.setParameter("id", t.getId());
		query.setMaxResults(1);
		boolean noAnalyzerAttached = query.getResultList().isEmpty();
		
		return noAnalyzerAttached;
	}

}
