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
package com.doculibre.constellio.wicket.panels.facets;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.search.CloudKeyword;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.search.SolrFacetUtils;
import com.doculibre.constellio.wicket.data.FacetsDataProvider;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;

@SuppressWarnings("serial")
public class CloudTagPanel extends Panel {
	
	private IModel cloudKeywordsModel;
	
	private int min;
	private int max;
	
	public CloudTagPanel(String id, final SearchableFacet searchableFacet, final FacetsDataProvider dataProvider) {
		super(id);
		
		cloudKeywordsModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				// Ordre décroissant de fréquence
				List<CloudKeyword> cloudKeywords = SolrFacetUtils.getCloudKeywords(searchableFacet, dataProvider);
				if (!cloudKeywords.isEmpty()) {
					max = (int) cloudKeywords.get(0).getWeight();
					min = (int) cloudKeywords.get(cloudKeywords.size() - 1).getWeight();
				} else {
					max = min = 0;
				}
				// Ordre alphabétique
				Collections.sort(cloudKeywords);
				return cloudKeywords;
			}
		};

		add(new ListView("couldKeywords", cloudKeywordsModel) {
			@Override
			protected void populateItem(ListItem item) {
				final CloudKeyword cloudKeyword = (CloudKeyword) item.getModelObject();

                SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
                SimpleSearch clone = simpleSearch.clone();
                clone.setCloudKeyword(cloudKeyword);
//              ConstellioSession.get().addSearchHistory(clone);

                PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
				Link addCloudKeywordLink = new BookmarkablePageLink("addCloudKeywordLink", pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone));
				item.add(addCloudKeywordLink);

				Label keywordLabel = new Label("keyword", cloudKeyword.getKeyword());
				addCloudKeywordLink.add(keywordLabel);
				keywordLabel.add(new SimpleAttributeModifier("style", getLinkStyle(cloudKeyword.getWeight(),
						max, min)));
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isVisible() {
		List<CloudKeyword> cloudKeywords = (List<CloudKeyword>) cloudKeywordsModel.getObject();
		return super.isVisible() && !cloudKeywords.isEmpty();
	}

	public String getLinkStyle(int count, int max, int min) {
		String style1 = "font-size: 10px; font-weight: normal;";
		String style2 = "font-size: 11px; font-weight: normal;";
		String style3 = "font-size: 12px; font-weight: normal;";
		String style4 = "font-size: 13px; font-weight: normal;";
		String style5 = "font-size: 14px; font-weight: normal;";
		String style6 = "font-size: 15px; font-weight: normal;";

		long weight = 0;
		long range = 0;
		weight = max - min;
		if (weight < 6) {
			range = 1;
		} else {
			range = weight / 6;
		}

		if (count >= 0 && count <= range) {
			return style1;
		} else if (count >= (range * 1) + 1 && count <= (range * 1) + range) {
			return style2;
		} else if (count >= (range * 2) + 1 && count <= (range * 2) + range) {
			return style3;
		} else if (count >= (range * 3) + 1 && count <= (range * 3) + range) {
			return style4;
		} else if (count >= (range * 4) + 1 && count <= (range * 4) + range) {
			return style5;
		} else {
			return style6;
		}
	}

}
