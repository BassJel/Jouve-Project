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
package com.doculibre.constellio.wicket.links;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.SearchRulesFactory;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.pages.SearchFormPage;
import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public class SwitchSearchMethod extends AjaxPanel {

	public SwitchSearchMethod(String id, SimpleSearch search) {
		super(id);
		boolean isSimple = search.getAdvancedSearchRule() == null;
		SimpleSearch clonedSimpleSearch = search.clone();
		String linkLabelResource;
		
		RecordCollectionServices recordCollectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection recordCollection = recordCollectionServices.get(search.getCollectionName());
		setVisible(recordCollection != null && recordCollection.isAdvancedSearchEnabled());
		
		if (isSimple) {
			linkLabelResource = "toAdvancedSearch";
			clonedSimpleSearch.setAdvancedSearchRule(SearchRulesFactory.getInitialSearchRuleFor(recordCollection));
		} else {
			linkLabelResource = "toSimpleSearch";
			clonedSimpleSearch.setAdvancedSearchRule(null);
		}

        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
		Link link = new BookmarkablePageLink("link", pageFactoryPlugin.getSearchFormPage(), SearchFormPage.getParameters(clonedSimpleSearch));
		link.add(new Label("label", new StringResourceModel(linkLabelResource, SwitchSearchMethod.this, null)));
		add(link);
	}

    @Override
    public boolean isVisible() {
        SearchInterfaceConfig searchInterfaceConfig = ConstellioSpringUtils.getSearchInterfaceConfigServices().get();
        return super.isVisible() && searchInterfaceConfig.isAdvancedSearchEnabled();
    }

}
