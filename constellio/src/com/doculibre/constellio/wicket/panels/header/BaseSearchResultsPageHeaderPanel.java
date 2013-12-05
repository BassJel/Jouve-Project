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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.facets.CollectionSearchFacetPanel;
import com.doculibre.constellio.wicket.panels.search.SearchFormCollectionPanel;
import com.doculibre.constellio.wicket.panels.search.SearchFormPanel;
import com.doculibre.constellio.wicket.panels.status.UserStatusPanel;

@SuppressWarnings("serial")
public class BaseSearchResultsPageHeaderPanel extends BasePageHeaderPanel {

    
	private boolean advancedForm = false;
    private UserStatusPanel userStatusPanel;
    private SearchFormPanel searchFormPanel;
    private Panel collectionFacetPanel;
    private WebMarkupContainer contenuContainer;
    private WebMarkupContainer userStatusContainer;

    public BaseSearchResultsPageHeaderPanel(String id, Page owner) {
        super(id, owner);

        SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils
            .getSearchInterfaceConfigServices();
        SearchInterfaceConfig config = searchInterfaceConfigServices.get();
        
        contenuContainer = new WebMarkupContainer("contenuContainer");
        add(contenuContainer);
        
        //These containers are only used to change css styles
        userStatusContainer = new WebMarkupContainer("userStatusContainer");
        contenuContainer.add(userStatusContainer);
        
        WebMarkupContainer collectionsFacetContainer = new WebMarkupContainer("collectionsFacetContainer");
        add(collectionsFacetContainer);
        
        WebMarkupContainer logoRechContainer = new WebMarkupContainer("logoRechContainer") {
        	@Override
        	public boolean isVisible() {
        		return super.isVisible() ;//&& !isAdvancedForm();
        	}
        };
        if (isAdvancedForm()) {
        	logoRechContainer.add(new SimpleAttributeModifier("style", "width:120px"));
        }
        logoRechContainer.add(new AttributeModifier("style", true, new LoadableDetachableModel() {
			
			@Override
			protected Object load() {
				return isAdvancedForm() ? "width:160px" : "width:240px";
			}
		}));
        add(logoRechContainer);
             
        logoRechContainer.add(newLogoImg("logoImg"));
        
        userStatusContainer.add(userStatusPanel = new UserStatusPanel("userStatusPanel", new PropertyModel(owner, "simpleSearch")));
        if (config.isShowCollectionsInResultFacets()) {
            collectionsFacetContainer.add(collectionFacetPanel = new SearchFormCollectionPanel(
            "collectionsFacetPanel"));
        } else {
            collectionsFacetContainer.add(collectionFacetPanel = new CollectionSearchFacetPanel(
            "collectionsFacetPanel"));
        }
        contenuContainer.add(searchFormPanel = new SearchFormPanel("searchFormPanel", new PropertyModel(owner, "simpleSearch")));

        if (!config.isShowCollectionsInResultFacets()) {
            userStatusContainer.add(new SimpleAttributeModifier("style", "margin-top:0px;"));
            collectionsFacetContainer.add(new SimpleAttributeModifier("style", "margin-left:52px;"));
            logoRechContainer.setVisible(false);
        } else {
            collectionsFacetContainer.setVisible(false);
        }
    }

    public UserStatusPanel getUserStatusPanel() {
        return userStatusPanel;
    }

    public SearchFormPanel getSearchFormPanel() {
        return searchFormPanel;
    }

    public Panel getCollectionFacetPanel() {
        return collectionFacetPanel;
    }

    protected Component newLogoImg(String id) {
        return BasePageHeaderPanel.newSmallLogo(id);
    }

	public boolean isAdvancedForm() {
		return advancedForm;
	}

	public void setAdvancedForm(boolean advancedForm) {
		this.advancedForm = advancedForm;
	}

	@Override
	protected void onBeforeRender() {
		String style;
		if (isAdvancedForm()) {
			style = "vertical-align: top; width:100%; margin-left:0px;";
		} else {
			style = "vertical-align: top;";
		}
		contenuContainer.add(new SimpleAttributeModifier("style", style));
		
        if (isAdvancedForm()) {
        	userStatusPanel.add(new SimpleAttributeModifier("style", "width:100%;"));
        	String idContainer = isAdvancedForm() ? "contenuRechAvancee" : "contenuRech";
        	userStatusContainer.add(new SimpleAttributeModifier("id", idContainer));
        }
		
		super.onBeforeRender();
	}

}
