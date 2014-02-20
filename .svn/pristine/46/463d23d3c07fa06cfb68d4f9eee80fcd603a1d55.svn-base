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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;

import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.services.StatsServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.data.SearchResultsDataProvider;

@SuppressWarnings("serial")
public class OpenSearchResultPanel extends Panel {
	
	public OpenSearchResultPanel(String id, SolrDocument doc, final SearchResultsDataProvider dataProvider) {
		super(id);

		String title = (String) doc.getFieldValue("title");
		String excerpt = (String) doc.getFieldValue("description");
		final String url = (String) doc.getFieldValue("link");
        final String collectionName = dataProvider.getSimpleSearch().getCollectionName();
        final String searchLogDocId = dataProvider.getSimpleSearch().getSearchLogDocId();
        
        Link titleLink = new Link("titleLink") {
            @Override
            public void onClick() {
                RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
                RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
                StatsServices statsServices = ConstellioSpringUtils.getStatsServices();
                
//                RecordCollection collection = collectionServices.get(collectionName);
//                Record record = recordServices.get(recordUrl, collection);
//                
//                statsServices.logClick(collection, record, searchLogDocId);
                getRequestCycle().setRequestTarget(new RedirectRequestTarget(url));
            }
        };

        // Add title
        if (StringUtils.isEmpty(title)) {
            title = url;
            if (title.length() > 120) {
                title = title.substring(0, 120) + " ...";
            }
        }

        Label titre = new Label("title", title);
        titleLink.add(titre.setEscapeModelStrings(false));
        add(titleLink);

        Label excerptLbl = new Label("excerpt", excerpt);
        add(excerptLbl.setEscapeModelStrings(false));
        add(new Label("url", url));
    }

}
