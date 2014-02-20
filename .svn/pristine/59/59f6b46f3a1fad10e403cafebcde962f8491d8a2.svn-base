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
package com.doculibre.constellio.plugins.defaults.wicket.global;

import java.util.ArrayList;
import java.util.List;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.behavior.HeaderContributor;

import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.plugins.api.wicket.global.GlobalThemePlugin;
import com.doculibre.constellio.plugins.defaults.DefaultConstellioPlugin;
import com.doculibre.constellio.wicket.pages.BaseConstellioPage;
import com.doculibre.constellio.wicket.panels.header.BaseAdminPageHeaderPanel;
import com.doculibre.constellio.wicket.panels.header.BaseSearchFormPageHeaderPanel;
import com.doculibre.constellio.wicket.panels.header.BaseSearchHistoryPageHeaderPanel;
import com.doculibre.constellio.wicket.panels.header.BaseSearchResultsPageHeaderPanel;

@SuppressWarnings("serial")
@PluginImplementation
public class DefaultGlobalThemePlugin extends DefaultConstellioPlugin implements GlobalThemePlugin {

    @Override
    public List<AbstractHeaderContributor> getHeaderContributors(Page page) {
        List<AbstractHeaderContributor> headerContributors = new ArrayList<AbstractHeaderContributor>();
        headerContributors.add(HeaderContributor.forCss(BaseConstellioPage.class, "css/constellio_base.css"));
        return headerContributors;
    }

    @Override
    public Component getFooterComponent(String id, Page page) {
        return new DefaultGlobalThemeFooter(id);
    }

    @Override
    public Component getHeaderComponent(String id, Page page) {
        Component headerComponent;
        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
        if (pageFactoryPlugin.isAdminPage(page)) {
            headerComponent = new BaseAdminPageHeaderPanel(id, page);
        } else if (pageFactoryPlugin.isSearchFormPage(page)){
            headerComponent = new BaseSearchFormPageHeaderPanel(id, page);
        } else if (pageFactoryPlugin.isSearchHistoryPage(page)){
            headerComponent = new BaseSearchHistoryPageHeaderPanel(id, page);
        } else if (pageFactoryPlugin.isSearchResultsPage(page)) {
            headerComponent = new BaseSearchResultsPageHeaderPanel(id, page);
        } else {
            headerComponent = null;
        }
        return headerComponent;
    }

}
