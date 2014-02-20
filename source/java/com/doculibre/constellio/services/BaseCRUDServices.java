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

import com.doculibre.constellio.entities.ConstellioEntity;

public interface BaseCRUDServices<T extends ConstellioEntity> {
    
    List<T> list();
	
    List<T> list(Collection<Number> ids);
    
    List<T> list(int maxResults);
    
    List<T> list(String orderByProperty, Boolean orderByAsc);
	
	List<T> list(String orderByProperty, Boolean orderByAsc, int maxResults);
    
    List<T> list(Map<String, Object> criteria);
	
	List<T> list(Map<String, Object> criteria, int maxResults);
    
    List<T> list(Map<String, Object> criteria, String orderByProperty, Boolean orderByAsc);
	
	List<T> list(Map<String, Object> criteria, String orderByProperty, Boolean orderByAsc, int maxResults);
	
    T get(Long id);
	
	T get(Map<String, Object> criteria);
	
	T makePersistent(T t);
	
	T makeTransient(T t);
	
	T merge(T t);
	
	void clear();
	
	void refresh(T t);
	
	void flush();

	boolean isRemoveable(T t);
	
}
