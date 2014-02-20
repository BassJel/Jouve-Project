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
import com.doculibre.constellio.entities.TokenizerClass;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class TokenizerClassServicesImpl extends BaseCRUDServicesImpl<TokenizerClass> implements TokenizerClassServices {

	public TokenizerClassServicesImpl(EntityManager entityManager) {
		super(TokenizerClass.class, entityManager);
	}
	
	@Override
	public TokenizerClass get(String className) {
		Map<String, Object> criteria = new HashMap<String, Object>();
		criteria.put("className", className);
		return get(criteria);
	}

	@Override
	public void init() {
		List<TokenizerClass> entities = list();
		if (entities.isEmpty()) {
			ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
			ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
			for (String tokenizerClassName : TokenizerClass.DEFAULT_VALUES) {
				TokenizerClass tokenizerClass = new TokenizerClass();
				tokenizerClass.setClassName(tokenizerClassName);
				tokenizerClass.setConnectorManager(connectorManager);
				makePersistent(tokenizerClass, false);
			}
		}
	}

	@Override
	public TokenizerClass makePersistent(TokenizerClass entity) {
		return makePersistent(entity, true);
	}

	@Override
	public TokenizerClass makeTransient(TokenizerClass entity) {
		return makeTransient(entity, true);
	}

	@Override
	public TokenizerClass makePersistent(TokenizerClass entity, boolean updateSolr) {
		TokenizerClass result = super.makePersistent(entity);
		if (updateSolr) {
			SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
			solrServices.updateSchemaFieldTypes();
		}
		return result;
	}

	@Override
	public TokenizerClass makeTransient(TokenizerClass entity, boolean updateSolr) {
		TokenizerClass result = super.makeTransient(entity);
		if (updateSolr) {
			SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
			solrServices.updateSchemaFieldTypes();
		}
		return result;
	}

	@Override
	public boolean isRemoveable(TokenizerClass t) {
		Query query = getEntityManager().createQuery("from Analyzer a where tokenizerClass_id=:id");
		query.setParameter("id", t.getId());
		query.setMaxResults(1);
		boolean noAnalyzerAttached = query.getResultList().isEmpty();
		
		return noAnalyzerAttached;
	}

}
