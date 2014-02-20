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
package com.doculibre.constellio.wicket.components.form;

import java.util.Map;

import org.apache.wicket.Response;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteTextRenderer;

@SuppressWarnings("serial")
public class WordsAndValueAutoCompleteRenderer extends AbstractAutoCompleteTextRenderer {

	@SuppressWarnings("rawtypes")
	@Override
	protected String getTextValue(Object object) {
		if (object == null) {
			return "";
			
		} else if (object instanceof String) {
			return (String) object;
			
		} else if (object instanceof Map.Entry) {
			Map.Entry entry = (Map.Entry) object;
			return entry.getKey().toString();
			
		} else {
			return object.toString();
		}
	}
	
	protected String getTextValue(String word, Object value) {
		return word + "\t[" + value.toString() + "]";
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void renderChoice(Object object, Response response,
			String criteria) {
		
		if (object instanceof Map.Entry) {
			Map.Entry entry = (Map.Entry) object;
			response.write(getTextValue(entry.getKey().toString(), entry.getValue()));
			
		} else {
			response.write(getTextValue(object));
		}
	}
	
	@Override
	protected CharSequence getOnSelectJavascriptExpression(Object item) {
		return super.getOnSelectJavascriptExpression(item);
	}
}
