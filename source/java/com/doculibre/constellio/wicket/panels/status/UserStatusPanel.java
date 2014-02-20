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
package com.doculibre.constellio.wicket.panels.status;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.wicket.components.holders.LinkHolder;
import com.doculibre.constellio.wicket.links.AdminLinkHolder;
import com.doculibre.constellio.wicket.links.SignInLinkHolder;
import com.doculibre.constellio.wicket.links.SignOutLinkHolder;
import com.doculibre.constellio.wicket.links.SwitchLocaleLinkHolder;
import com.doculibre.constellio.wicket.links.SwitchSearchMethod;
import com.doculibre.constellio.wicket.pages.SearchFormPage;
import com.doculibre.constellio.wicket.pages.SearchHistoryPage;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class UserStatusPanel extends Panel {

    public UserStatusPanel(String id, final IModel simpleSearchModel) {
        super(id);

        add(new AdminLinkHolder("adminLinkHolder"));
        add(new SignInLinkHolder("signInLinkHolder"));
        add(new SignOutLinkHolder("signOutLinkHolder"));
        add(new SwitchLocaleLinkHolder("switchLocaleLinkHolder"));
        //TODO Disactivate if the user do not want it
        add(new SwitchSearchMethod("switchSearchMethod", (SimpleSearch) simpleSearchModel.getObject()));
        add(new LinkHolder("searchHistoryLinkHolder", new StringResourceModel("searchHistory", this, null)) {
            @Override
            protected WebMarkupContainer newLink(String id) {
                SimpleSearch simpleSearch = (SimpleSearch) simpleSearchModel.getObject();
                return new BookmarkablePageLink(id, SearchHistoryPage.class, new PageParameters(
                    SimpleSearch.COLLECTION_NAME + "=" + simpleSearch.getCollectionName()));
            }

            @Override
            public boolean isVisible() {
                SimpleSearch simpleSearch = (SimpleSearch) simpleSearchModel.getObject();
                String collectionName = simpleSearch.getCollectionName();
                return super.isVisible() && collectionName != null && !ConstellioSession.get().getSearchHistory(collectionName).isEmpty();
            }

            @Override
            public void detachModels() {
                simpleSearchModel.detach();
                super.detachModels();
            }
        });
        add(new LinkHolder("newSearchLinkHolder", new StringResourceModel("newSearch", this, null)) {
            @Override
            protected WebMarkupContainer newLink(String id) {
                PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                SimpleSearch simpleSearch = (SimpleSearch) simpleSearchModel.getObject();
                return new BookmarkablePageLink(id, pageFactoryPlugin.getSearchFormPage(), SearchFormPage.getParameters(simpleSearch));
            }

            @Override
            public boolean isVisible() {
            	PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                return super.isVisible() && !(pageFactoryPlugin.isSearchFormPage(UserStatusPanel.this.getPage()));
            }
        });
    }

}
