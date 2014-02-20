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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.doculibre.constellio.entities.ConstellioEntity;

public class BaseCRUDServicesImpl<T extends ConstellioEntity> extends BaseServicesImpl implements BaseCRUDServices<T> {
	
	private Class<T> entityClass;
	
	public BaseCRUDServicesImpl(Class<T> entityClass, EntityManager entityManager) {
		super(entityManager);
		this.entityClass = entityClass;
	}

    @Override
    public List<T> list() {
        return list(-1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> list(Collection<Number> ids) {
        StringBuffer sb = new StringBuffer("from " + entityClass.getName() + " o");
        sb.append(" where o.id IN (");
        boolean first = true;
        for (Number id : ids) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(id);
            first = false;
        }
        sb.append(")");
        Query query = getEntityManager().createQuery(sb.toString());
        return query.getResultList();
    }
	
	@Override
	public List<T> list(int maxResults) {
		return list(null, null, maxResults);
	}

    @Override
    public List<T> list(String orderByProperty, Boolean orderByAsc) {
        return list(orderByProperty, orderByAsc, -1);
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<T> list(String orderByProperty, Boolean orderByAsc, int maxResults) {
		StringBuffer sb = new StringBuffer("from " + entityClass.getName() + " o");
		if (orderByProperty == null) {
		    orderByProperty = "id";
		}
        sb.append(" order by " + orderByProperty);
        if (orderByAsc == null) {
            orderByAsc = Boolean.TRUE; 
        }
        sb.append(orderByAsc ? " asc" : " desc");
		Query query = getEntityManager().createQuery(sb.toString());
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
		return query.getResultList();
	}

    @Override
    public List<T> list(Map<String, Object> criteria) {
        return list(criteria, -1);
    }

	@Override
	public List<T> list(Map<String, Object> criteria, int maxResults) {
		return list(criteria, null, null, maxResults);
	}

    @Override
    public List<T> list(Map<String, Object> criteria, String orderByProperty, Boolean orderByAsc) {
        return list(criteria, orderByProperty, orderByAsc, -1);
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<T> list(Map<String, Object> criteria, String orderByProperty,
			Boolean orderByAsc, int maxResults) {
		StringBuffer sb = new StringBuffer("from " + entityClass.getName() + " o");
		if (criteria != null && !criteria.isEmpty()) {
			sb.append(" where ");
			int index = 0;
			for (String propertyName : criteria.keySet()) {
				if (index == criteria.size() - 1) { 
					//Last Criteria
					sb.append("o." + propertyName + "=?" + index);
				} else {
					sb.append("o." + propertyName + "=?" + index + " AND ");
				}				
				index++;
			}
		}
        if (orderByProperty == null) {
            orderByProperty = "id";
        }
        sb.append(" order by " + orderByProperty);
        if (orderByAsc == null) {
            orderByAsc = Boolean.TRUE; 
        }
        sb.append(orderByAsc ? " asc" : " desc");
		
		Query query = getEntityManager().createQuery(sb.toString());
		if (criteria != null && !criteria.isEmpty()) {
			int index = 0;
			for (Object propertyValue : criteria.values()) {
				query.setParameter(index++, propertyValue);
			}
		}
		if (maxResults != -1) {
		    query.setMaxResults(maxResults);
		}
		return (List<T>) query.getResultList();
	}

	@Override
	public T get(Long id) {
	    return (T) getEntityManager().find(entityClass, id);
	}

	@Override
	public T makePersistent(T entity) {
		getEntityManager().persist(entity);
		return entity;
	}

	@Override
	public T makeTransient(T entity) {
		getEntityManager().remove(entity);
		return entity;
	}

	@Override
	public void clear() {
		getEntityManager().clear();
	}

	@Override
	public void flush() {
		getEntityManager().flush();
	}

	@Override
	public T merge(T entity) {
		getEntityManager().merge(entity);
		return entity;
	}

	@Override
	public void refresh(T entity) {
		getEntityManager().refresh(entity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(Map<String, Object> criteria) {
	    T singleResult;
		StringBuffer sb = new StringBuffer("from " + entityClass.getName() + " o");
		if (criteria != null && !criteria.isEmpty()) {
			sb.append(" where ");
			int index = 1;
			for (String propertyName : criteria.keySet()) {
				if (index == criteria.size()) { 
					//Last Criteria
					sb.append("o." + propertyName + "=?" + index);
				} else {
					sb.append("o." + propertyName + "=?" + index + " AND ");
				}				
				index++;
			}
		}
		Query query = getEntityManager().createQuery(sb.toString());
		query.setMaxResults(1);
		if (criteria != null && !criteria.isEmpty()) {
			int index = 1;
			for (Object propertyValue : criteria.values()) {
				query.setParameter(index++, propertyValue);
			}
		}
		List<T> resultList = query.getResultList();
		if (!resultList.isEmpty()) {
		    singleResult = resultList.get(0);
		} else {
		    singleResult = null;
		}
		return singleResult;
	}

	@Override
	public boolean isRemoveable(T t) {
		return true;
	}
	
}
