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
package com.doculibre.constellio.wicket.panels.admin.collection;

import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.admin.AdminCollectionMenuItemsPlugin;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.acl.PolicyACLListPanel;
import com.doculibre.constellio.wicket.panels.admin.categorization.CategorizationListPanel;
import com.doculibre.constellio.wicket.panels.admin.collectionPermission.CollectionPermissionListPanel;
import com.doculibre.constellio.wicket.panels.admin.connector.ConnectorListPanel;
import com.doculibre.constellio.wicket.panels.admin.credentialGroup.CredentialGroupListPanel;
import com.doculibre.constellio.wicket.panels.admin.elevate.ElevateQueryListPanel;
import com.doculibre.constellio.wicket.panels.admin.facets.FacetListPanel;
import com.doculibre.constellio.wicket.panels.admin.featuredLink.FeaturedLinkListPanel;
import com.doculibre.constellio.wicket.panels.admin.federation.IncludedCollectionListPanel;
import com.doculibre.constellio.wicket.panels.admin.indexField.AdminIndexFieldsPanel;
import com.doculibre.constellio.wicket.panels.admin.indexing.AdminIndexingPanel;
import com.doculibre.constellio.wicket.panels.admin.relevance.RelevancePanel;
import com.doculibre.constellio.wicket.panels.admin.solrConfig.SolrConfigPanel;
import com.doculibre.constellio.wicket.panels.admin.spellchecker.SpellCheckerConfigPanel;
import com.doculibre.constellio.wicket.panels.admin.stats.CollectionStatsPanel;
import com.doculibre.constellio.wicket.panels.admin.synonyms.SynonymListPanel;
import com.doculibre.constellio.wicket.panels.admin.tabs.AdminLeftMenuPanel;
import com.doculibre.constellio.wicket.panels.admin.thesaurus.AddEditThesaurusPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class AdminCollectionPanel extends AdminLeftMenuPanel {

    public static final int INDEXING_MANAGEMENT_PANEL = 0;
    public static final int CONNECTORS_MANAGEMENT_PANEL = 1;
    public static final int FACETS_MANAGEMENT_PANEL = 2;
    public static final int FEDERATED_SEARCH_MANAGEMENT_PANEL = 3;
    public static final int INDEX_FIELDS_MANAGEMENT_PANEL = 4;
    public static final int SUBCOLLECTIONS_MANAGEMENT_PANEL = 5;
    public static final int STATS_PANEL = 6;
    public static final int AUTORIZATIONS_MANAGEMENT_PANEL = 7;
    public static final int FEATURED_LINKS_MANAGEMENT_PANEL = 8;
    public static final int ELEVATE_MANAGEMENT_PANEL = 9;
    public static final int SYNONYMS_MANAGEMENT_PANEL = 10;
    public static final int SPELLCHECKER_MANAGEMENT_PANEL = 11;
    public static final int THESAURI_MANAGEMENT_PANEL = 12;
    public static final int RELEVANCE_MANAGEMENT_PANEL = 13;
    public static final int SOLR_CONFIG_MANAGEMENT_PANEL = 14;

    public AdminCollectionPanel(String id, RecordCollection collection) {
        super(id, new ReloadableEntityModel<RecordCollection>(RecordCollection.class, collection.getId()));
    }

    @Override
    protected void onBeforeRender() {
        Integer selectedTab = (Integer) getModelObject();
        if (selectedTab == null) {
            this.replaceWith(new CollectionListPanel(getId()));
        } else {
            super.onBeforeRender();
        }
    }

    public RecordCollection getCollection() {
        return (RecordCollection) getExtraModelObject();
    }

    @Override
    protected void fillTabs(List<ITab> leftMenuTabs) {
        RecordCollection collection = (RecordCollection) getExtraModelObject();
        if (collection != null) {
            ConstellioSession session = ConstellioSession.get();
            ConstellioUser user = session.getUser();

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs.add(new AbstractTab(new StringResourceModel("indexingManagement", this, null)) {
                    @Override
                    public Panel getPanel(String panelId) {
                        return new AdminIndexingPanel(panelId);
                    }
                });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs
                    .add(new AbstractTab(new StringResourceModel("connectorsManagement", this, null)) {
                        @Override
                        public Panel getPanel(String panelId) {
                            return new ConnectorListPanel(panelId);
                        }
                    });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs
                    .add(new AbstractTab(new StringResourceModel("federatedSearchManagement", this, null)) {
                        @Override
                        public Panel getPanel(String panelId) {
                            return new IncludedCollectionListPanel(panelId);
                        }
                    });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs.add(new AbstractTab(new StringResourceModel("facetsManagement", this, null)) {
                    @Override
                    public Panel getPanel(String panelId) {
                        return new FacetListPanel(panelId);
                    }
                });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs
                    .add(new AbstractTab(new StringResourceModel("indexFieldsManagement", this, null)) {
                        @Override
                        public Panel getPanel(String panelId) {
                            return new AdminIndexFieldsPanel(panelId);
                        }
                    });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs.add(new AbstractTab(new StringResourceModel("categorizationsManagement", this,
                    null)) {
                    @Override
                    public Panel getPanel(String panelId) {
                        return new CategorizationListPanel(panelId);
                    }
                });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs.add(new AbstractTab(new StringResourceModel("stats", this, null)) {
                    @Override
                    public Panel getPanel(String panelId) {
                        RecordCollection collection = getCollection();
                        return new CollectionStatsPanel(panelId, collection.getName());
                    }
                });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs.add(new AbstractTab(new StringResourceModel("authorizationsManagement", this,
                    null)) {
                    @Override
                    public Panel getPanel(String panelId) {
                        return new CollectionPermissionListPanel(panelId);
                    }
                });
            }

            if (user.hasCollaborationPermission(collection)) {
                leftMenuTabs.add(new AbstractTab(new StringResourceModel("featuredLinksManagement", this,
                    null)) {
                    @Override
                    public Panel getPanel(String panelId) {
                        return new FeaturedLinkListPanel(panelId);
                    }
                });
            }

            if (user.hasCollaborationPermission(collection)) {
                leftMenuTabs.add(new AbstractTab(new StringResourceModel("elevateManagement", this, null)) {
                    @Override
                    public Panel getPanel(String panelId) {
                        return new ElevateQueryListPanel(panelId);
                    }
                });
            }

            if (user.hasCollaborationPermission(collection)) {
                leftMenuTabs.add(new AbstractTab(new StringResourceModel("synonymsManagement", this, null)) {
                    @Override
                    public Panel getPanel(String panelId) {
                        return new SynonymListPanel(panelId);
                    }
                });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs.add(new AbstractTab(
                    new StringResourceModel("spellCheckerManagement", this, null)) {
                    @Override
                    public Panel getPanel(String panelId) {
                        return new SpellCheckerConfigPanel(panelId);
                    }
                });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs.add(new AbstractTab(new StringResourceModel("thesaurusManagement", this, null)) {
                    @Override
                    public Panel getPanel(String panelId) {
                        return new AddEditThesaurusPanel(panelId);
                    }
                });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs.add(new AbstractTab(new StringResourceModel("relevancePanel", this, null)) {
                    @Override
                    public Panel getPanel(String panelId) {
                        return new RelevancePanel(panelId);
                    }
                });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs
                    .add(new AbstractTab(new StringResourceModel("solrConfigManagement", this, null)) {
                        @Override
                        public Panel getPanel(String panelId) {
                            return new SolrConfigPanel(panelId);
                        }
                    });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs
                    .add(new AbstractTab(new StringResourceModel("policyACLManagement", this, null)) {
                        @Override
                        public Panel getPanel(String panelId) {
                            return new PolicyACLListPanel(panelId);
                        }
                    });
            }

            if (user.hasAdminPermission(collection)) {
                leftMenuTabs
                    .add(new AbstractTab(new StringResourceModel("credentialGroupManagement", this, null)) {
                        @Override
                        public Panel getPanel(String panelId) {
                            return new CredentialGroupListPanel(panelId);
                        }
                    });
            }

            List<AdminCollectionMenuItemsPlugin> menuItemsPlugins = PluginFactory
                .getPlugins(AdminCollectionMenuItemsPlugin.class);
            for (AdminCollectionMenuItemsPlugin menuItemsPlugin : menuItemsPlugins) {
                menuItemsPlugin.handle(leftMenuTabs, collection);
            }
        }
    }

}
