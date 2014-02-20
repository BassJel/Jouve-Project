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
package com.doculibre.constellio.wicket.panels.header;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.wicket.components.holders.LinkHolder;
import com.doculibre.constellio.wicket.links.AdminLinkHolder;
import com.doculibre.constellio.wicket.links.SearchLinkHolder;
import com.doculibre.constellio.wicket.links.SignInLinkHolder;
import com.doculibre.constellio.wicket.links.SignOutLinkHolder;
import com.doculibre.constellio.wicket.links.SwitchLocaleLinkHolder;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class BaseSearchHistoryPagePreHeaderPanel extends Panel {

    public BaseSearchHistoryPagePreHeaderPanel(String id, Page owner) {
        super(id);

        final String collectionName = RequestCycle.get().getPageParameters().getString(SimpleSearch.COLLECTION_NAME);
        
        add(newLogoImg("logoImg"));
        add(new SearchLinkHolder("searchLinkHolder"));
        add(new AdminLinkHolder("adminLinkHolder"));
        add(new SignInLinkHolder("signInLinkHolder"));
        add(new SignOutLinkHolder("signOutLinkHolder"));
        add(new SwitchLocaleLinkHolder("switchLocaleLinkHolder"));
        add(new Label("firstNameLastName", new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ConstellioUser user = ConstellioSession.get().getUser();
                if (user == null) {
                	return new StringResourceModel("anonymous", BaseSearchHistoryPagePreHeaderPanel.this, null).getObject();
                } else {
                	return user.getFirstName() + " " + user.getLastName();
                }
                
            }
        }));
        add(new Label("username", new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ConstellioUser user = ConstellioSession.get().getUser();
                return user == null ? "" : user.getUsername();
            }
        }));
        add(new LinkHolder("backLinkHolder", new StringResourceModel("back", this, null)) {
            @Override
            protected WebMarkupContainer newLink(String id) {
                return new Link(id) {
                    @Override
                    public void onClick() {
                        List<SimpleSearch> searchHistory = ConstellioSession.get().getSearchHistory(collectionName);
                        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                        int sizeHistory = searchHistory.size();
                        if (sizeHistory > 0) {
                            // Relaunches last search
                            SimpleSearch simpleSearch = searchHistory.get(sizeHistory - 1);
                            SimpleSearch clone = simpleSearch.clone();
                            setResponsePage(pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone));
                        } else {
                            setResponsePage(pageFactoryPlugin.getSearchFormPage(), new PageParameters(SimpleSearch.COLLECTION_NAME
                                + "=" + collectionName));
                        }
                    }
                };
            }
        });
        add(new LinkHolder("newSearchLinkHolder", new StringResourceModel("newSearch", this, null)) {
            @Override
            protected WebMarkupContainer newLink(String id) {
                PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                return new BookmarkablePageLink(id, pageFactoryPlugin.getSearchFormPage(), new PageParameters(SimpleSearch.COLLECTION_NAME
                    + "=" + collectionName));
            }
        });
    }
    
    protected Component newLogoImg(String id) {
        return BasePageHeaderPanel.newSmallLogo(id);
    }

}
