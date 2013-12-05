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
package com.doculibre.constellio.wicket.panels.admin.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.breadcrumb.BreadCrumbBar;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public abstract class AdminLeftMenuPanel extends TabbedPanel {
    
    private IModel extraModel;
    
    public AdminLeftMenuPanel(String id) {
        this(id, null);
    }
        
	@SuppressWarnings("unchecked")
	public AdminLeftMenuPanel(String id, IModel extraModel) {
		super(id, new ArrayList<ITab>());
		this.extraModel = extraModel;
		setOutputMarkupId(true);
		
		add(new AbstractBehavior() {
			@Override
			public void renderHead(IHeaderResponse response) {
//				StringBuffer js = new StringBuffer();
//				js.append("if (makeNiceTitles) {\r\n");
//				js.append("    makeNiceTitles();\r\n");
//				js.append("}");
//				response.renderJavascript(js, "niceTitles");
				super.renderHead(response);
			}
		});
		
		List<ITab> leftMenuTabs = getTabs();
		fillTabs(leftMenuTabs);
		
		add(new Label("title", new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ITab selectedTab = (ITab) getTabs().get(getSelectedTab());
                String title = selectedTab.getTitle().getObject().toString();
                return title;
            }
        }));
	}

    @Override
    public void setSelectedTab(int index) {
        super.setSelectedTab(index);
        setBreadCrumbs();
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
    }

    private void setBreadCrumbs() {
        AdminTopMenuPanel topMenuPanel = (AdminTopMenuPanel) findParent(AdminTopMenuPanel.class);
        BreadCrumbBar breadCrumbBar = topMenuPanel.getBreadCrumbBar();
        int breadCrumbCount = breadCrumbBar.allBreadCrumbParticipants().size();
        if (breadCrumbCount == 2) {
            breadCrumbBar.allBreadCrumbParticipants().remove(1);
        }
        breadCrumbBar.setActive(new IBreadCrumbParticipant() {
            @Override
            public void onActivate(IBreadCrumbParticipant previous) {
                // Nothing to do
            }
            
            @Override
            public String getTitle() {
                int selectedTab = getSelectedTab();
                if (selectedTab == -1) {
                    selectedTab = 0;
                }
                ITab leftMenuTab = (ITab) getTabs().get(selectedTab);
                String leftMenuTitle = leftMenuTab.getTitle().getObject().toString();
                
                AdminTopMenuPanel adminTopMenuPanel = (AdminTopMenuPanel) findParent(AdminTopMenuPanel.class);
                if (adminTopMenuPanel.getSelectedTab() == 0) {
                    Component currentTabContent = adminTopMenuPanel.get(TAB_PANEL_ID);
                    if (currentTabContent instanceof AdminCollectionPanel) {
                        AdminCollectionPanel adminCollectionPanel = (AdminCollectionPanel) currentTabContent;
                        RecordCollection collection = adminCollectionPanel.getCollection();
                        Locale displayLocale = collection.getDisplayLocale(getLocale());
                        String collectionTitle = collection.getTitle(displayLocale);
                        leftMenuTitle = collectionTitle + " > " + leftMenuTitle;
                    }
                }
                return leftMenuTitle;
            }
            
            @Override
            public Component getComponent() {
                return get(TabbedPanel.TAB_PANEL_ID);
            }
        });
    }

    @Override
    public void detachModels() {
        if (extraModel != null) {
            extraModel.detach();
        }
        super.detachModels();
    }
    
    public Object getExtraModelObject() {
        return extraModel != null ? extraModel.getObject() : null;
    }

    protected abstract void fillTabs(List<ITab> leftMenuTabs);

}
