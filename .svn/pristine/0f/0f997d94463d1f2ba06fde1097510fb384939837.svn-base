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
package com.doculibre.constellio.wicket.panels.admin.indexing;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import javax.persistence.EntityManager;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.time.Duration;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.services.ConnectorManagerServices;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.status.StatusManager;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceUtils;
import com.doculibre.constellio.wicket.components.form.LoggingTextArea;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class AdminIndexingPanel extends AjaxPanel {

	private long refreshTimeMillis = 500;
	private Link manageConnectorsLink;
	private Link synchronizeIndexFieldsLink;
	private Link manageIndexFieldsLink;
	private Label recordCountLabel;
	private Link deleteAllLink;
	private Label indexedRecordCountLabel;
	private WebMarkupContainer controlIndexingButtons;
	private Link reindexAllLink;
	private Link resumeIndexingLink;
	private Link optimizeLink;
	private Label indexSizeOnDiskLabel;
	private LoggingTextArea latestIndexedRecordsTextArea;
	private IModel connectorsModel;
	private ListView connectorTraversalStatesListView;
	private ListView connectorTraversalTextAreasListView;

	public AdminIndexingPanel(String id) {
		super(id);

		add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND));
		// add(new AbstractAjaxTimerBehavior(Duration.ONE_SECOND) {
		// @Override
		// protected void onTimer(AjaxRequestTarget target) {
		// // Do nothing, will prevent page from expiring
		// }
		// });

		connectorsModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				List<ConnectorInstance> connectors;
				RecordCollection collection = getRecordCollection();
				if (collection.isFederationOwner()) {
					FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
					connectors = federationServices.listConnectors(collection);
				} else {
					connectors = new ArrayList<ConnectorInstance>(collection.getConnectorInstances());
				}
				return connectors;
			}
		};

		manageConnectorsLink = new Link("manageConnectorsLink") {
			@Override
			public void onClick() {
				AdminCollectionPanel adminCollectionPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				adminCollectionPanel.setSelectedTab(AdminCollectionPanel.CONNECTORS_MANAGEMENT_PANEL);
			}

			@Override
			public boolean isVisible() {
				boolean visible = super.isVisible();
				if (visible) {
					RecordCollection collection = getRecordCollection();
					visible = collection.getConnectorInstances().isEmpty();
				}
				return visible;
			}
		};

		synchronizeIndexFieldsLink = new Link("synchronizeIndexFieldsLink") {
			@Override
			public void onClick() {
				RecordCollection collection = getRecordCollection();

				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				SolrServices solrServices = ConstellioSpringUtils.getSolrServices();

				solrServices.updateSchemaFields(collection);
				solrServices.initCore(collection);

				EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
				if (!entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().begin();
				}
				collectionServices.markSynchronized(collection);
				entityManager.getTransaction().commit();

				IndexingManager indexingManager = IndexingManager.get(collection);
				if (!indexingManager.isActive()) {
					indexingManager.startIndexing();
				}
			}

			@Override
			public boolean isVisible() {
				boolean visible = super.isVisible();
				if (visible) {
					RecordCollection collection = getRecordCollection();
					visible = collection.isSynchronizationRequired();
				}
				return visible;
			}
		};

		manageIndexFieldsLink = new Link("manageIndexFieldsLink") {
			@Override
			public void onClick() {
				AdminCollectionPanel adminCollectionPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				adminCollectionPanel.setSelectedTab(AdminCollectionPanel.INDEX_FIELDS_MANAGEMENT_PANEL);
			}
		};

		recordCountLabel = new Label("recordCount", new LoadableDetachableModel() {
			@Override
			protected Object load() {
				RecordCollection collection = getRecordCollection();
				return StatusManager.countTraversedRecords(collection);
			}
		});

		deleteAllLink = new Link("deleteAllLink") {
			@Override
			public void onClick() {
				RecordCollection collection = getRecordCollection();
				RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
				FederationServices federationServices = ConstellioSpringUtils.getFederationServices();

				ReadWriteLock collectionLock = recordServices.getLock(collection.getName());
				collectionLock.writeLock().lock();
				try {

					ConstellioPersistenceUtils.beginTransaction();
					recordServices.markRecordsForDeletion(collection);
					if (collection.isFederationOwner()) {
						List<RecordCollection> includedCollections = federationServices.listIncludedCollections(collection);
						for (RecordCollection includedCollection : includedCollections) {
							recordServices.markRecordsForDeletion(includedCollection);
						}
					}
					SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
					try {
						solrServer.commit();
						solrServer.optimize();
					} catch (Throwable t) {
						try {
							solrServer.rollback();
						} catch (Exception e) {
							throw new RuntimeException(t);
						}
					}
				} finally {
					try {
						ConstellioPersistenceUtils.finishTransaction(false);
					} finally {
						collectionLock.writeLock().unlock();
					}
				}

				// RecordCollection collection = getRecordCollection();
				//
				// ConnectorManagerServices connectorManagerServices =
				// ConstellioSpringUtils.getConnectorManagerServices();
				// ConnectorManager connectorManager =
				// connectorManagerServices.getDefaultConnectorManager();
				// for (ConnectorInstance connectorInstance :
				// collection.getConnectorInstances()) {
				// String connectorName = connectorInstance.getName();
				// connectorManagerServices.disableConnector(connectorManager,
				// connectorName);
				// }
				//
				// IndexingManager indexingManager =
				// IndexingManager.get(collection);
				// indexingManager.deleteAll();
				// indexingManager.optimize();
				// while (indexingManager.isOptimizing()) {
				// try {
				// Thread.sleep(200);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				// }
			}

			@Override
			protected CharSequence getOnClickScript(CharSequence url) {
				String confirmMsg = getLocalizer().getString("confirmDeleteAll", AdminIndexingPanel.this).replace("'", "\\'");
				return "if (confirm('" + confirmMsg + "')) { window.location.href='" + url + "';} else { return false; }";
			}
		};

		indexedRecordCountLabel = new Label("indexedRecordCount", new LoadableDetachableModel() {
			@Override
			protected Object load() {
				RecordCollection collection = getRecordCollection();
				return StatusManager.countIndexedRecords(collection);
			}
		});

		controlIndexingButtons = new WebMarkupContainer("controlIndexingButtons");
		controlIndexingButtons.setOutputMarkupId(true);

		reindexAllLink = new Link("reindexAllLink") {
			@Override
			public void onClick() {
				RecordCollection collection = getRecordCollection();
				final String collectioName = collection.getName();
				final Long collectionId = collection.getId();
				
				
				new Thread() {
					@Override
					public void run() {
						RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
						ReadWriteLock collectionLock = recordServices.getLock(collectioName);
						collectionLock.writeLock().lock();
						try {
							EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
							if (!entityManager.getTransaction().isActive()) {
								entityManager.getTransaction().begin();
							}

							RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
							FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
							RecordCollection collection = collectionServices.get(collectionId);
							try {
								recordServices.markRecordsForUpdateIndex(collection);
								if (collection.isFederationOwner()) {
									List<RecordCollection> includedCollections = federationServices.listIncludedCollections(collection);
									for (RecordCollection includedCollection : includedCollections) {
										recordServices.markRecordsForUpdateIndex(includedCollection);
									}
								}
								SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
								try {
									// solrServer.commit();
									solrServer.optimize();
								} catch (Throwable t) {
									try {
										solrServer.rollback();
									} catch (Exception e) {
										throw new RuntimeException(t);
									}
								}
							} finally {
								ConstellioPersistenceUtils.finishTransaction(false);
							}
						} finally {
							collectionLock.writeLock().unlock();
						}
					}

				}.start();
			}

			@Override
			protected CharSequence getOnClickScript(CharSequence url) {
				String confirmMsg = getLocalizer().getString("confirmReindexAll", AdminIndexingPanel.this).replace("'", "\\'");
				return "if (confirm('" + confirmMsg + "')) { window.location.href='" + url + "';} else { return false; }";
			}
		};

		resumeIndexingLink = new Link("resumeIndexingLink") {
			@Override
			public void onClick() {
				RecordCollection collection = getRecordCollection();
				IndexingManager indexingManager = IndexingManager.get(collection);
				if (!indexingManager.isActive()) {
					indexingManager.startIndexing(false);
				}
			}

			@Override
			public boolean isVisible() {
				// boolean visible = super.isVisible();
				// if (visible) {
				// RecordCollection collection = getRecordCollection();
				// IndexingManager indexingManager =
				// IndexingManager.get(collection);
				// visible = !collection.isSynchronizationRequired() &&
				// !indexingManager.isActive();
				// }
				// return visible;
				return false;
			}
		};

		optimizeLink = new Link("optimizeLink") {
			@Override
			public void onClick() {
				RecordCollection collection = getRecordCollection();
				final Long collectionId = collection.getId();
				new Thread() {
					@Override
					public void run() {
						RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
						RecordCollection collection = collectionServices.get(collectionId);
						SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
						try {
							solrServer.optimize();
						} catch (Throwable t) {
							try {
								solrServer.rollback();
							} catch (Exception e) {
								throw new RuntimeException(t);
							}
						}
						// IndexingManager indexingManager =
						// IndexingManager.get(collection);
						// if (indexingManager.isActive() &&
						// !indexingManager.isOptimizing()) {
						// indexingManager.optimize();
						// }
					}
				}.start();
			}

			@Override
			protected CharSequence getOnClickScript(CharSequence url) {
				String confirmMsg = getLocalizer().getString("confirmOptimize", AdminIndexingPanel.this).replace("'", "\\'");
				return "if (confirm('" + confirmMsg + "')) { window.location.href='" + url + "';} else { return false; }";
			}

			@Override
			public boolean isVisible() {
				// boolean visible = super.isVisible();
				// if (visible) {
				// RecordCollection collection = getRecordCollection();
				// IndexingManager indexingManager =
				// IndexingManager.get(collection);
				// visible = indexingManager.isActive();
				// }
				// return visible;
				return true;
			}

			@Override
			public boolean isEnabled() {
				// boolean enabled = super.isEnabled();
				// if (enabled) {
				// RecordCollection collection = getRecordCollection();
				// IndexingManager indexingManager =
				// IndexingManager.get(collection);
				// enabled = indexingManager.isActive() &&
				// !indexingManager.isOptimizing();
				// }
				// return enabled;
				return true;
			}
		};

		indexSizeOnDiskLabel = new Label("indexSizeOnDisk", new LoadableDetachableModel() {
			@Override
			protected Object load() {
				RecordCollection collection = getRecordCollection();
				return StatusManager.getSizeOnDisk(collection);
			}
		});

		connectorTraversalStatesListView = new ListView("connectorTraversalStates", connectorsModel) {
			@Override
			protected void populateItem(ListItem item) {
				ConnectorInstance connectorInstance = (ConnectorInstance) item.getModelObject();
				final ReloadableEntityModel<ConnectorInstance> connectorInstanceModel = new ReloadableEntityModel<ConnectorInstance>(connectorInstance);
				Label displayNameLabel = new Label("displayName", connectorInstance.getDisplayName());
				Label lastTraversalDateLabel = new Label("latestTraversalDate", new LoadableDetachableModel() {
					@Override
					protected Object load() {
						ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
						Date lastTraversalDate = StatusManager.getLastTraversalDate(connectorInstance);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						return lastTraversalDate != null ? sdf.format(lastTraversalDate) : "---";
					}

					@Override
					public void detach() {
						connectorInstanceModel.detach();
						super.detach();
					}
				});

				Link restartTraversalLink = new Link("restartTraversalLink") {
					@Override
					public void onClick() {
						ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
						ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
						ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
						connectorManagerServices.restartTraversal(connectorManager, connectorInstance.getName());
					}

					@Override
					protected CharSequence getOnClickScript(CharSequence url) {
						String confirmMsg = getLocalizer().getString("confirmRestartTraversal", AdminIndexingPanel.this).replace("'", "\\'");
						return "if (confirm('" + confirmMsg + "')) { window.location.href='" + url + "';} else { return false; }";
					}

					@Override
					public boolean isVisible() {
						ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
						RecordCollection connectorInstanceCollection = connectorInstance.getRecordCollection();
						ConstellioUser user = ConstellioSession.get().getUser();
						return super.isVisible() && user.hasAdminPermission(connectorInstanceCollection);
					}
				};

				Link disableConnectorLink = new Link("disableConnectorLink") {
					@Override
					public void onClick() {
						ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
						String connectorName = connectorInstance.getName();
						ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
						ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
						connectorManagerServices.disableConnector(connectorManager, connectorName);
					}

					@Override
					protected CharSequence getOnClickScript(CharSequence url) {
						String confirmMsg = getLocalizer().getString("confirmDisableConnector", AdminIndexingPanel.this).replace("'", "\\'");
						return "if (confirm('" + confirmMsg + "')) { window.location.href='" + url + "';} else { return false; }";
					}

					@Override
					public boolean isVisible() {
						boolean visible = super.isVisible();
						if (visible) {
							ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
							String connectorName = connectorInstance.getName();
							ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
							ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
							visible = connectorManagerServices.isConnectorEnabled(connectorManager, connectorName);
							if (visible) {
								RecordCollection connectorInstanceCollection = connectorInstance.getRecordCollection();
								ConstellioUser user = ConstellioSession.get().getUser();
								visible = user.hasAdminPermission(connectorInstanceCollection);
							}
						}
						return visible;
					}
				};

				Link enableConnectorLink = new Link("enableConnectorLink") {
					@Override
					public void onClick() {
						ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
						String connectorName = connectorInstance.getName();
						ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
						ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
						connectorManagerServices.enableConnector(connectorManager, connectorName);
					}

					@Override
					protected CharSequence getOnClickScript(CharSequence url) {
						String confirmMsg = getLocalizer().getString("confirmEnableConnector", AdminIndexingPanel.this).replace("'", "\\'");
						return "if (confirm('" + confirmMsg + "')) { window.location.href='" + url + "';} else { return false; }";
					}

					@Override
					public boolean isVisible() {
						boolean visible = super.isVisible();
						if (visible) {
							ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
							String connectorName = connectorInstance.getName();
							ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
							ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
							visible = !connectorManagerServices.isConnectorEnabled(connectorManager, connectorName);
							if (visible) {
								RecordCollection connectorInstanceCollection = connectorInstance.getRecordCollection();
								ConstellioUser user = ConstellioSession.get().getUser();
								visible = user.hasAdminPermission(connectorInstanceCollection);
							}
						}
						return visible;
					}
				};

				item.add(displayNameLabel);
				item.add(lastTraversalDateLabel);
				item.add(restartTraversalLink);
				item.add(disableConnectorLink);
				item.add(enableConnectorLink);
			}
		};

		latestIndexedRecordsTextArea = new LoggingTextArea("latestIndexedRecordsTextArea", new LoadableDetachableModel() {
			@Override
			protected Object load() {
				RecordCollection collection = getRecordCollection();
				return StatusManager.listLastIndexedRecords(collection);
			}
		}, refreshTimeMillis);

		connectorTraversalTextAreasListView = new ListView("connectorTraversalTextAreas", connectorsModel) {
			@Override
			protected void populateItem(ListItem item) {
				ConnectorInstance connectorInstance = (ConnectorInstance) item.getModelObject();
				final ReloadableEntityModel<ConnectorInstance> connectorInstanceModel = new ReloadableEntityModel<ConnectorInstance>(connectorInstance);
				Label displayNameLabel = new Label("displayName", connectorInstance.getDisplayName());

				LoggingTextArea traversalTextArea = new LoggingTextArea("traversalTextArea", new LoadableDetachableModel() {
					@Override
					protected Object load() {
						ConnectorInstance connectorInstance = connectorInstanceModel.getObject();
						return StatusManager.listLastTraversedRecords(connectorInstance);
					}
				}, refreshTimeMillis) {
					@Override
					public void detachModels() {
						connectorInstanceModel.detach();
						super.detachModels();
					}
				};

				item.add(displayNameLabel);
				item.add(traversalTextArea);
			}
		};
		// connectorTextAreasListView.setReuseItems(true);

		add(manageConnectorsLink);
		add(synchronizeIndexFieldsLink);
		add(manageIndexFieldsLink);
		add(recordCountLabel);
		add(deleteAllLink);
		add(indexedRecordCountLabel);
		add(controlIndexingButtons);
		controlIndexingButtons.add(reindexAllLink);
		controlIndexingButtons.add(resumeIndexingLink);
		controlIndexingButtons.add(optimizeLink);
		add(indexSizeOnDiskLabel);
		add(connectorTraversalStatesListView);
		add(latestIndexedRecordsTextArea);
		add(connectorTraversalTextAreasListView);
	}

	@Override
	public void detachModels() {
		connectorsModel.detach();
		super.detachModels();
	}

	private RecordCollection getRecordCollection() {
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		RecordCollection collection = collectionAdminPanel.getCollection();
		return collection;
	}
}
