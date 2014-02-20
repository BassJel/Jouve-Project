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
package com.doculibre.constellio.wicket.renderers;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * 
 * @author francisbaril
 * 
 */
@SuppressWarnings("serial")
public class StringResourceChoiceRenderer implements IChoiceRenderer {
	
	private String prefix;
	private Component component;

	/**
	 * 
	 * @param prefix
	 *            can be your wicketId or any other string you desire. this
	 *            value is used as a prefix in order to get the localized option
	 *            value.
	 */
	public StringResourceChoiceRenderer(String prefix) {
		this.prefix = prefix;
	}
	
	/**
	 * 
	 * @param prefix
	 *            can be your wicketId or any other string you desire. this
	 *            value is used as a prefix in order to get the localized option
	 *            value.
	 */
	public StringResourceChoiceRenderer(String context, Component component) {
		this.prefix = context;
		this.component = component;
	}
	
	public StringResourceChoiceRenderer(Component component) {
		this.component = component;
	}

	public Object getDisplayValue(Object object) {
		String path;
		if (prefix != null) {
			path = prefix + "." + ((String) object);
		} else {
			path = (String) object;
		}
			
		if (component == null) {
			return new ResourceModel(path).getObject();
		} else {
			return new StringResourceModel(path, component, null).getObject();
		}
		
	}

	/**
	 * This basic implementation use the entire string value as the option key
	 */
	public String getIdValue(Object object, int index) {
		return (String) object;
	}
}
