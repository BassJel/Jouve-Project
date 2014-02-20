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
package com.doculibre.constellio.wicket.panels.admin.searchInterface;

import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.wicket.panels.admin.resultPanels.ConnectorResultClassesPanel;
import com.doculibre.constellio.wicket.panels.admin.searchInterface.autocomplete.AdminAutocompletePanel;
import com.doculibre.constellio.wicket.panels.admin.searchInterface.context.SearchInterfaceContextListPanel;
import com.doculibre.constellio.wicket.panels.admin.searchInterface.ga.AdminGoogleAnalyticsPanel;
import com.doculibre.constellio.wicket.panels.admin.searchInterface.generalOptions.GeneralOptionsPanel;
import com.doculibre.constellio.wicket.panels.admin.searchInterface.generalOptions.advancedSearch.AdvancedSearchOptionsListPanel;
import com.doculibre.constellio.wicket.panels.admin.searchInterface.theme.ThemeConfigPanel;
import com.doculibre.constellio.wicket.panels.admin.searchResultField.AdminSearchResultFieldsPanel;
import com.doculibre.constellio.wicket.panels.admin.tabs.AdminLeftMenuPanel;


@SuppressWarnings("serial")
public class AdminSearchInterfacePanel extends AdminLeftMenuPanel {

	public AdminSearchInterfacePanel(String id) {
		super(id);
	}

	@Override
	protected void fillTabs(List<ITab> leftMenuTabs) {
        leftMenuTabs.add(new AbstractTab(new StringResourceModel("options", this, null)) {
            @Override
            public Panel getPanel(String panelId) {
                return new GeneralOptionsPanel(panelId);
            }
        });
        leftMenuTabs.add(new AbstractTab(new StringResourceModel("searchInterfaceContexts", this, null)) {
            @Override
            public Panel getPanel(String panelId) {
                return new SearchInterfaceContextListPanel(panelId);
            }
        });  
        leftMenuTabs.add(new AbstractTab(new StringResourceModel("autocomplete", this, null)) {
            @Override
            public Panel getPanel(String panelId) {
                return new AdminAutocompletePanel(panelId);
            }
        });  
        leftMenuTabs.add(new AbstractTab(new StringResourceModel("theme", this, null)) {
            @Override
            public Panel getPanel(String panelId) {
                return new ThemeConfigPanel(panelId);
            }
        }); 
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("logo", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new LogoConfigPanel(panelId);
			}
		});	
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("advancedSearchOptions", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new AdvancedSearchOptionsListPanel(panelId);
			}
		});	
        leftMenuTabs.add(new AbstractTab(new StringResourceModel("searchResultFields", this, null)) {
            @Override
            public Panel getPanel(String panelId) {
                return new AdminSearchResultFieldsPanel(panelId);
            }
        }); 
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("wicketPanel", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new ConnectorResultClassesPanel(panelId);
			}
		});	
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("googleAnalytics", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new AdminGoogleAnalyticsPanel(panelId);
			}
		});	
	}

}
