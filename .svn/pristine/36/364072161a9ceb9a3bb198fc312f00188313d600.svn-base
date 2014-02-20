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

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.lang.PropertyResolver;

@SuppressWarnings("serial")
public class FindParentPropertyModel extends LoadableDetachableModel {

	private Component component;
	private Class<? extends Component> parentClass;
	private String propertyName;
	
	public FindParentPropertyModel(
			Component component, 
			Class<? extends Component> parentClass, 
			String propertyName) {
		this.component = component;
		this.parentClass = parentClass;
		this.propertyName = propertyName;
	}
	
	@Override
	protected Object load() {
		Component parentComponent = component.findParent(parentClass);
		return PropertyResolver.getValue(propertyName, parentComponent);
	}

}
