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
package com.doculibre.constellio.entities;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyColumn;

@SuppressWarnings("serial")
@Entity
public class I18NLabel extends BaseConstellioEntity {
	
	private String key;
	
	private Map<Locale, String> values = new HashMap<Locale, String>();

	@Column (length = 255, name="labelKey")
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	@ElementCollection(targetClass = String.class)
	@CollectionTable(name =  "I18NLabel_Values")
	@MapKeyColumn(name="locale", length = 128)
	@MapKeyClass(Locale.class)
	@Column(name="value", length = 10 * 1024)
	public Map<Locale, String> getValues() {
		return values;
	}
	
	public void setValues(Map<Locale, String> values) {
		this.values = values;
	}
	
	public String getValue(Locale locale) {
		return values.get(locale);
	}
	
	public void setValue(String value, Locale locale) {
		values.put(locale, value);
	}

}
