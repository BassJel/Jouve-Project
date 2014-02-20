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
package com.doculibre.constellio.wicket.components.locale;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

@SuppressWarnings("serial")
public class LocaleNameLabel extends Label {

	public LocaleNameLabel(String id, Locale locale) {
		this(id, locale, false);
	}

	public LocaleNameLabel(String id, final Locale locale, final boolean addParenthesis) {
		super(id);
		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				String localeName = StringUtils.capitalize(locale.getDisplayLanguage(getLocale()));
				if (addParenthesis) {
					localeName = "(" + localeName + ")";
				}
				return localeName;
			}
		});
	}

}
