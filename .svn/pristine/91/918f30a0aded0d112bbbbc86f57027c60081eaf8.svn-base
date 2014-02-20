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

public class MetaNameServicesImpl implements MetaNameServices {

	@Override
	public String normalizeIndexFieldName(String metaName) {
		if (metaName == null) {
			return null;
		}
		StringBuffer indexFieldName = new StringBuffer();
		boolean nextCharUpperCase = false;
		for (int i = 0; i < metaName.length(); i++) {
			char c = metaName.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				if (nextCharUpperCase) {
					indexFieldName.append(Character.toUpperCase(c));
					nextCharUpperCase = false;
				} else {
					indexFieldName.append(c);
				}
			} else if (c == ' ' || c == '-' || c == '_') {
				nextCharUpperCase = true;
			}
		}
		return indexFieldName.toString();
	}

}
