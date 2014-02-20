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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.ComponentModel;

import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.wicket.components.holders.LinkHolder;
import com.doculibre.constellio.wicket.pages.BaseSearchPage;

@SuppressWarnings("serial")
public class SearchLinkHolder extends LinkHolder {

    public SearchLinkHolder(String id) {
        super(id, new ComponentModel() {
            @Override
            protected Object getObject(Component component) {
                return component.getLocalizer().getString("search", component);
            }
        });
    }

    @Override
    public WebMarkupContainer newLink(String id) {
        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
        return new BookmarkablePageLink(id, pageFactoryPlugin.getSearchFormPage());
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && !(getPage() instanceof BaseSearchPage);
    }

}
