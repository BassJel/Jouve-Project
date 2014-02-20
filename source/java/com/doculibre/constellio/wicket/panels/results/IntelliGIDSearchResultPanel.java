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
package com.doculibre.constellio.wicket.panels.results;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.basic.Label;

import com.doculibre.constellio.wicket.data.SearchResultsDataProvider;
import com.ibm.icu.text.DecimalFormat;

@SuppressWarnings("serial")
public class IntelliGIDSearchResultPanel extends DefaultSearchResultPanel {
	
	public IntelliGIDSearchResultPanel(String id, SolrDocument doc, final SearchResultsDataProvider dataProvider) {
		super(id, doc, dataProvider);
		
		String contentLength = (String) doc.getFieldValue("contentLength");
		String type = (String) doc.getFieldValue("subType");

		String contentLengthKBStr;
		if (StringUtils.isNotBlank(contentLength)) {
			long contentLengthBytes;
			try {
				contentLengthBytes = Long.valueOf(contentLength);
			} catch (NumberFormatException e) {
				contentLengthBytes = -1;
			}
			double contentLengthKB = (double) contentLengthBytes / 1000;
			DecimalFormat contentLengthKBFormatter = new DecimalFormat();
			contentLengthKBFormatter.setMinimumFractionDigits(0);
			contentLengthKBFormatter.setMaximumFractionDigits(0);
			contentLengthKBStr = contentLengthKBFormatter.format(contentLengthKB);
			if ("0".equals(contentLengthKBStr)) {
				contentLengthKBStr = null;
			}
		} else {
			contentLengthKBStr = null;
		}

		add(new Label("contentLength", contentLengthKBStr + " KB").setVisible(contentLengthKBStr != null));
		add(new Label("type", type).setVisible(StringUtils.isNotBlank(type)));
		
		
	}

}
