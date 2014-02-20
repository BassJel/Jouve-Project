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

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.SearchRulesFactory;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.SimpleParams;
import com.doculibre.constellio.wicket.panels.search.SearchFormCollectionPanel;
import com.doculibre.constellio.wicket.panels.search.SearchFormPanel;
import com.doculibre.constellio.wicket.panels.status.UserStatusPanel;
import com.doculibre.constellio.wicket.utils.SimpleParamsUtils;

public class SearchFormPage extends BaseSearchPage {
    
    public SearchFormPage() {
        super(new SimpleSearch(), null);
        initComponents();
    }
    
    public SearchFormPage(PageParameters params) {
        super(getSimpleSearch(params), params);
        initComponents();
    }
    
    private void initComponents() {
        SimpleSearch simpleSearch = getSimpleSearch();
        
        add(new SearchFormCollectionPanel("collectionSearchPanel"));
        
        WebMarkupContainer contenuContainer = new WebMarkupContainer("contenuContainer");
        if (simpleSearch.getAdvancedSearchRule() != null) {
        	contenuContainer.add(new SimpleAttributeModifier("style", "width:530px; margin-left:0px;"));
        }
        
        
        add(contenuContainer);
        UserStatusPanel userStatusPanel = new UserStatusPanel("userStatusPanel", new Model(simpleSearch));
        if (simpleSearch.getAdvancedSearchRule() != null) {
        	userStatusPanel.add(new SimpleAttributeModifier("style", "width:100%;"));
        }
        contenuContainer.add(new SearchFormPanel("searchFormPanel", new Model(simpleSearch)));
        contenuContainer.add(userStatusPanel);
    }
    
    public static SimpleSearch getSimpleSearch(PageParameters params) {
    	SimpleSearch simpleSearch = new SimpleSearch(params.getString(SimpleSearch.COLLECTION_NAME));
    	
    	SimpleSearch completeSearch = SimpleSearch.toSimpleSearch(SimpleParamsUtils.toSimpleParams(params));
    	simpleSearch.setAdvancedSearchRule(completeSearch.getAdvancedSearchRule());
    	return simpleSearch;
    }
    
    public static PageParameters getParameters(SimpleSearch simpleSearch) {
    	SimpleParams params = new SimpleParams();
    	params.add(SimpleSearch.COLLECTION_NAME, simpleSearch.getCollectionName());
    	if (simpleSearch.getAdvancedSearchRule() != null) {
			RecordCollectionServices recordCollectionServices = ConstellioSpringUtils.getRecordCollectionServices();
			RecordCollection recordCollection = recordCollectionServices.get(simpleSearch.getCollectionName());
    		params.addAll(SearchRulesFactory.getInitialSearchRuleFor(recordCollection).toSimpleParams(true));
    	}
        PageParameters pageParameters = SimpleParamsUtils.toPageParameters(params);
        return pageParameters;
    }

}
