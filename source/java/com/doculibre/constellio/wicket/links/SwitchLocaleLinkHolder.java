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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.components.holders.LinkHolder;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class SwitchLocaleLinkHolder extends AjaxPanel {

    public SwitchLocaleLinkHolder(String id) {
        super(id);
        
        IModel languagesModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ConstellioSession session = ConstellioSession.get();
                Locale currentLocale = session.getLocale();
                List<Locale> supportedLocales = ConstellioSpringUtils.getSupportedLocales();
                List<Locale> linkLocales = new ArrayList<Locale>();
                for (Locale supportedLocale : supportedLocales) {
                    if (!supportedLocale.getLanguage().equals(currentLocale.getLanguage())) {
                        linkLocales.add(supportedLocale);
                    }
                }
                return linkLocales;
            }
        };
        add(new ListView("languages", languagesModel) {
            @Override
            protected void populateItem(ListItem item) {
                final Locale locale = (Locale) item.getModelObject();
                String language = locale.getLanguage().toUpperCase();
                item.add(new LinkHolder("linkHolder", new Model(language)) {
                    @Override
                    protected WebMarkupContainer newLink(String id) {
                        Link link = new Link(id) {
                            @Override
                            public void onClick() {
                                ConstellioSession.get().changeLocale(locale);
                            }
                        };
                        link.add(new SimpleAttributeModifier("class", "langue"));
                        return link;
                    }
                });
            }
        });
    }

    @Override
    public boolean isVisible() {
        List<Locale> supportedLocales = ConstellioSpringUtils.getSupportedLocales();
        return !ConstellioSession.get().isPortletMode() && supportedLocales.size() > 1;
    }

}
