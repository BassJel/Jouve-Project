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
package com.doculibre.constellio.search;

import java.util.Iterator;
import java.util.List;

import com.doculibre.constellio.services.SynonymServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class SynonymUtils {
	
	public static final String addSynonyms(String text, String solrServerName, boolean exactExpression) {
		String textAndSynonyms;
		SynonymServices synonymServices = ConstellioSpringUtils.getSynonymServices();
		List<String> synonyms = synonymServices.getSynonyms(text, solrServerName);
		if (!synonyms.isEmpty()) {
			StringBuffer sbSynonyms = new StringBuffer();
			for (Iterator<String> it = synonyms.iterator(); it.hasNext();) {
				String synonym = it.next();
				if (exactExpression) {
					sbSynonyms.append("\"");
				}
				sbSynonyms.append(synonym);
				if (exactExpression) {
					sbSynonyms.append("\"");
				}
				if (it.hasNext()) {
					sbSynonyms.append(" OR ");
				}
			}
			textAndSynonyms = sbSynonyms.toString();
		} else {
		    if (exactExpression) {
	            textAndSynonyms = "\"" + text + "\"";
		    } else {
	            textAndSynonyms = text;
		    }
		}
		return textAndSynonyms;
	}

}
