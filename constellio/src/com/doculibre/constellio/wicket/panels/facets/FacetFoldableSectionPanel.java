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

import javax.servlet.http.Cookie;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.wicket.panels.fold.CookieFoldableSectionPanel;

@SuppressWarnings("serial")
public abstract class FacetFoldableSectionPanel extends CookieFoldableSectionPanel {

    private static final String PREFIX = "facet.";
    
    private String cookieName;

    public FacetFoldableSectionPanel(String id, String sectionTitleKey, String cookieName) {
        super(id, sectionTitleKey);
        this.cookieName = PREFIX + cookieName;
    }

    public FacetFoldableSectionPanel(String id, IModel sectionTitleModel, String cookieName) {
        super(id, sectionTitleModel);
        this.cookieName = PREFIX + cookieName;
    }

    public FacetFoldableSectionPanel(String id, String sectionTitleKey, SearchableFacet searchableFacet) {
        super(id, sectionTitleKey);
        this.cookieName = PREFIX + searchableFacet.getName();
    }

    public FacetFoldableSectionPanel(String id, IModel sectionTitleModel, SearchableFacet searchableFacet) {
        super(id, sectionTitleModel);
        this.cookieName = PREFIX + searchableFacet.getName();
    }

    @Override
    protected String getCookieName() {
        return cookieName;
    }

    @Override
    protected WebMarkupContainer newFoldableSectionContainer(String id) {
        WebMarkupContainer foldableSectionContainer = new WebMarkupContainer(id);
        foldableSectionContainer.add(new AttributeModifier("style", true, new LoadableDetachableModel() {
            @Override
            protected Object load() {
                String display;
                if (isOpened()) {
                    display = "inline";
                } else {
                    display = "none";
                }
                return "display:" + display;
            }
        }));
        return foldableSectionContainer;
    }

    @Override
    protected WebMarkupContainer newToggleLink(String id) {
        WebMarkupContainer jsToggleLink = new WebMarkupContainer(id);
        jsToggleLink.add(new AttributeModifier("onclick", true, new LoadableDetachableModel() {
            @Override
            protected Object load() {
                WebMarkupContainer foldableSectionContainer = getFoldableSectionContainer();
                Image toggleImg = getToggleImg();
                String foldableSectionContainerId = foldableSectionContainer.getMarkupId();
                String toggleImgId = toggleImg.getMarkupId();
                CharSequence openedImgURL = urlFor(OPENED_IMG_RESOURCE_REFERENCE);
                CharSequence closedImgURL = urlFor(CLOSED_IMG_RESOURCE_REFERENCE);
                String cookieName = getCookieName();
                // function toggleSection(foldableSectionContainerId, toggleImgId, openedImgURL, closedImgURL,
                // cookieName)
                StringBuffer js = new StringBuffer();
                js.append("toggleSection('");
                js.append(foldableSectionContainerId);
                js.append("', '");
                js.append(toggleImgId);
                js.append("', '");
                js.append(openedImgURL);
                js.append("', '");
                js.append(closedImgURL);
                js.append("', '");
                js.append(cookieName);
                js.append("')");
                return js;
            }
        }));
        return jsToggleLink;
    }
    
    public static void clearCookies() {
        Cookie[] facetCookies = getCookies(PREFIX);
        if (facetCookies != null) {
            for (Cookie cookie : facetCookies) {
                clear(cookie);
            }
        }
    }

}
