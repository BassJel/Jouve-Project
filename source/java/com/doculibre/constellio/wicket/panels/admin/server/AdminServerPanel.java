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
package com.doculibre.constellio.wicket.panels.admin.server;

import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.wicket.panels.admin.analyzerClass.AnalyzerClassListPanel;
import com.doculibre.constellio.wicket.panels.admin.connectorTypeMeta.AdminConnectorTypeMetaMappingsPanel;
import com.doculibre.constellio.wicket.panels.admin.fieldType.FieldTypeListPanel;
import com.doculibre.constellio.wicket.panels.admin.fieldTypeClass.FieldTypeClassListPanel;
import com.doculibre.constellio.wicket.panels.admin.filterClass.FilterClassListPanel;
import com.doculibre.constellio.wicket.panels.admin.group.GroupListPanel;
import com.doculibre.constellio.wicket.panels.admin.server.hardware.HardwarePanel;
import com.doculibre.constellio.wicket.panels.admin.solrConfig.SolrConfigPanel;
import com.doculibre.constellio.wicket.panels.admin.tabs.AdminLeftMenuPanel;
import com.doculibre.constellio.wicket.panels.admin.tagging.FreeTextTagListPanel;
import com.doculibre.constellio.wicket.panels.admin.tokenizerClass.TokenizerClassListPanel;
import com.doculibre.constellio.wicket.panels.admin.user.UserListPanel;

@SuppressWarnings("serial")
public class AdminServerPanel extends AdminLeftMenuPanel {

	public AdminServerPanel(String id) {
		super(id);
	}

	@Override
	protected void fillTabs(List<ITab> leftMenuTabs) {
        leftMenuTabs.add(new AbstractTab(new StringResourceModel("hardware", this, null)) {
            @Override
            public Panel getPanel(String panelId) {
                return new HardwarePanel(panelId);
            }
        });
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("userManagement", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new UserListPanel(panelId);
			}
		});
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("groupManagement", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new GroupListPanel(panelId);
			}
		});
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("connectorTypeFieldsManagement", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new AdminConnectorTypeMetaMappingsPanel(panelId);
			}
		});
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("fieldTypesManagement", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new FieldTypeListPanel(panelId);
			}
		});
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("fieldTypeClassManagement", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new FieldTypeClassListPanel(panelId);
			}
		});
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("analyzerClassManagement", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new AnalyzerClassListPanel(panelId);
			}
		});
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("tokenizerClassManagement", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new TokenizerClassListPanel(panelId);
			}
		});
		leftMenuTabs.add(new AbstractTab(new StringResourceModel("filterClassManagement", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new FilterClassListPanel(panelId);
			}
		});
        leftMenuTabs.add(new AbstractTab(new StringResourceModel("freeTextTagging", this, null)) {
            @Override
            public Panel getPanel(String panelId) {
                return new FreeTextTagListPanel(panelId);
            }
        });
        leftMenuTabs.add(new AbstractTab(new StringResourceModel("solrConfigManagement", this, null)) {
            @Override
            public Panel getPanel(String panelId) {
                return new SolrConfigPanel(panelId);
            }
        });
	}

}
