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
package com.doculibre.constellio.wicket.pages;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.models.VisibleCollectionsModel;

public abstract class BaseSearchPage extends BaseConstellioPage {
    
    private Locale locale;

    public BaseSearchPage(SimpleSearch simpleSearch, PageParameters params) {
        super(new Model(simpleSearch));

        if (params != null) {
        	WebRequest webRequest = getWebRequestCycle().getWebRequest();
            String displayLang = webRequest.getHttpServletRequest().getParameter(DISPLAY_LANG_PARAM);
            if (StringUtils.isNotBlank(displayLang)) {
                for (Locale supportedLocale : ConstellioSpringUtils.getSupportedLocales()) {
                    if (supportedLocale.getLanguage().equals(displayLang)) {
                        this.locale = supportedLocale;
                        break;
                    }
                }
            }
        }
        
        VisibleCollectionsModel visibleCollectionsModel = new VisibleCollectionsModel();
        if (StringUtils.isEmpty(simpleSearch.getCollectionName())) {
            List<RecordCollection> visibleCollections = visibleCollectionsModel.getObject(null, null);
            if (!visibleCollections.isEmpty()) {
                simpleSearch.setCollectionName(visibleCollections.get(0).getName());
            }
        }
    }

    public SimpleSearch getSimpleSearch() {
        return (SimpleSearch) getModelObject();
    }

    @Override
    public Locale getLocale() {
        return this.locale != null ? this.locale : super.getLocale();
    }

}
