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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.breadcrumb.BreadCrumbBar;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.CollectionListPanel;
import com.doculibre.constellio.wicket.panels.admin.searchInterface.AdminSearchInterfacePanel;
import com.doculibre.constellio.wicket.panels.admin.server.AdminServerPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class AdminTopMenuPanel extends TabbedPanel {

    private BreadCrumbBar breadCrumbBar;

    @SuppressWarnings("unchecked")
    public AdminTopMenuPanel(String id) {
        super(id, new ArrayList<ITab>());
        setOutputMarkupId(true);

        add(new AbstractBehavior() {
            @Override
            public void renderHead(IHeaderResponse response) {
                // StringBuffer js = new StringBuffer();
                // js.append("if (makeNiceTitles) {\r\n");
                // js.append("    makeNiceTitles();\r\n");
                // js.append("}");
                // response.renderJavascript(js, "niceTitles");
                super.renderHead(response);
            }
        });
        breadCrumbBar = new BreadCrumbBar("breadCrumbs") {
            @Override
            protected String getSeparatorMarkup() {
                return "> ";
            }
        };
        add(breadCrumbBar);

        List<ITab> adminTabs = getTabs();
        adminTabs.add(new AbstractTab(new StringResourceModel("collectionsManagement", this, null)) {
            @Override
            public Panel getPanel(String panelId) {
                return new CollectionListPanel(panelId);
            }
        });

        ConstellioSession session = ConstellioSession.get();
        ConstellioUser user = session.getUser();
        if (user.isAdmin()) {
            adminTabs.add(new AbstractTab(new StringResourceModel("searchInterface", this, null)) {
                @Override
                public Panel getPanel(String panelId) {
                    return new AdminSearchInterfacePanel(panelId);
                }
            });
            adminTabs.add(new AbstractTab(new StringResourceModel("serverManagement", this, null)) {
                @Override
                public Panel getPanel(String panelId) {
                    return new AdminServerPanel(panelId);
                }
            });
        }
    }

    @Override
    protected WebMarkupContainer newLink(String linkId, final int index) {
        WebMarkupContainer link = super.newLink(linkId, index);
        link.add(new AttributeModifier("id", true, new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return getSelectedTab() == index ? "current" : null;
            }
        }));
        return link;
    }

    public BreadCrumbBar getBreadCrumbBar() {
        return breadCrumbBar;
    }

    @Override
    public void setSelectedTab(int index) {
        super.setSelectedTab(index);
        setBreadCrumbs();
    }

    @Override
    protected void onBeforeRender() {
        if (breadCrumbBar.allBreadCrumbParticipants().isEmpty()) {
            setBreadCrumbs();
        }
        super.onBeforeRender();
    }

    private void setBreadCrumbs() {
        breadCrumbBar.allBreadCrumbParticipants().clear();
        breadCrumbBar.setActive(new IBreadCrumbParticipant() {
            @Override
            public void onActivate(IBreadCrumbParticipant previous) {
                Component currentTabContent = get(TAB_PANEL_ID);
                if (currentTabContent instanceof AdminCollectionPanel) {
                    TabbedPanel subTabPanel = (TabbedPanel) currentTabContent;
                    int tab = subTabPanel.getSelectedTab();

                    // If the selected tab is -1, it means the panel has been
                    // created within the same request. The user has just
                    // selected a collection and we don't want to return to
                    // collection's list page
                    if (tab != -1) {
                        setSelectedTab(0);
                    }
                } else if (currentTabContent instanceof TabbedPanel) {
                    TabbedPanel subTabPanel = (TabbedPanel) currentTabContent;
                    subTabPanel.setSelectedTab(0);
                }
            }

            @Override
            public String getTitle() {
                int selectedTab = getSelectedTab();
                if (selectedTab == -1) {
                    selectedTab = 0;
                }
                ITab topMenuTab = (ITab) getTabs().get(selectedTab);
                String title = topMenuTab.getTitle().getObject().toString();
                return title;
            }

            @Override
            public Component getComponent() {
                Component currentTabContent = get(TAB_PANEL_ID);
                return currentTabContent;
            }

        });
    }

    public void replaceTabContent(Panel newTabContent) {
        Panel tabContentPanel = (Panel) get(TabbedPanel.TAB_PANEL_ID);
        tabContentPanel.replaceWith(newTabContent);
        setBreadCrumbs();
    }

    public void goToServerTab(int verticalTab) {
        setSelectedTab(2);
        AdminLeftMenuPanel leftTab = (AdminLeftMenuPanel) get(TAB_PANEL_ID);
        leftTab.setSelectedTab(verticalTab);
    }

    public void goToSearchInterfaceTab(int verticalTab) {
        setSelectedTab(1);
        AdminLeftMenuPanel leftTab = (AdminLeftMenuPanel) get(TAB_PANEL_ID);
        leftTab.setSelectedTab(verticalTab);
    }

}
