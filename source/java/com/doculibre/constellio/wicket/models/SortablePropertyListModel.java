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
package com.doculibre.constellio.wicket.models;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.PropertyResolver;

/**
 * 
 * 
 * @author Vincent Dussault
 */
@SuppressWarnings("serial")
public class SortablePropertyListModel<T> extends SortableListModel<T> {
	
	private IModel listModel;
	
	public SortablePropertyListModel(List<T> listModelObject) {
		this(new Model((Serializable) listModelObject));
	}
	
	public SortablePropertyListModel(IModel listModel) {
		this.listModel = listModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<T> load(final String orderByProperty, final Boolean orderByAsc) {
		List<T> listModelObject = (List<T>) listModel.getObject();
		if (orderByProperty != null) {
			Collections.sort(listModelObject, new Comparator<T>() {
				@Override
				public int compare(T o1, T o2) {
					int result;
					Object value1 = PropertyResolver.getValue(orderByProperty, o1);
					Object value2 = PropertyResolver.getValue(orderByProperty, o2);
					if (value1 != null && value2 == null) {
						result = -1;
					} else if (value1 == null && value2 != null) {
						result = 1;
					} else if (value1 == null && value2 == null) {
						result = 0;
					} else if (value1 instanceof Comparable && value2 instanceof Comparable) {
						Comparable<Object> comparable1 = (Comparable<Object>) value1;
						Comparable<Object> comparable2 = (Comparable<Object>) value2;
						result = comparable1.compareTo(comparable2);
					} else {
						// Use toString...
						result = value1.toString().compareTo(value2.toString());
					}
					if (Boolean.FALSE.equals(orderByAsc)) {
						result = -result;
					}
					return result;
				}
			});
		}
		return listModelObject;
	}

	@Override
	protected void onDetach() {
		listModel.detach();
		super.onDetach();
	}
    
}
