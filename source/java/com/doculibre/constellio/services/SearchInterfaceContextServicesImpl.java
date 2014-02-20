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

import com.doculibre.constellio.entities.searchInterface.SearchInterfaceContext;
import com.doculibre.constellio.entities.searchInterface.SearchInterfaceContextParam;

public class SearchInterfaceContextServicesImpl extends BaseCRUDServicesImpl<SearchInterfaceContext> implements SearchInterfaceContextServices {

	public SearchInterfaceContextServicesImpl(EntityManager entityManager) {
		super(SearchInterfaceContext.class, entityManager);
	}

	@Override
	public SearchInterfaceContext getByCurl(String curl) {
		Map<String, Object> criteria = new HashMap<String, Object>();
		criteria.put("curlValue", curl);
		return get(criteria);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SearchInterfaceContext> listFirstLevel() {
		StringBuffer sb = new StringBuffer();
		sb.append("from ");
		sb.append(SearchInterfaceContext.class.getName());
		sb.append(" where parentContext_id is null");
		Query query = getEntityManager().createQuery(sb.toString());
		return (List<SearchInterfaceContext>) query.getResultList();
	}

	@Override
	public SearchInterfaceContextParam getParam(Long contextParamId) {
		return getEntityManager().find(SearchInterfaceContextParam.class, contextParamId);
	}

	@Override
	public void init() {
	}

	@Override
	public SearchInterfaceContextParam makePersistent(SearchInterfaceContextParam contextParam) {
		getEntityManager().persist(contextParam);
		return contextParam;
	}

	@Override
	public SearchInterfaceContextParam makeTransient(SearchInterfaceContextParam contextParam) {
		getEntityManager().remove(contextParam);
		return contextParam;
	}

}
