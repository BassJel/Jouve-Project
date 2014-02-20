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
package com.doculibre.constellio.indexing;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import org.apache.commons.lang.time.DateUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.FederationRecordDeletionRequired;
import com.doculibre.constellio.entities.FederationRecordIndexingRequired;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.indexing.IndexingPlugin;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.status.StatusManager;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;

public class IndexingManager {

    private static final Logger LOGGER = Logger.getLogger(IndexingManager.class.getName());

    private boolean stoppedManaging = false;
    private boolean indexingAll = false;
    private boolean deletingAll = false;
    private boolean optimizing = false;

    private Date lastIndexModification;
    private boolean changeSinceLastOptimization = false;

    private Long collectionId;
    private Set<Long> deletableConnectorInstanceIds = new HashSet<Long>();
    //private IndexingThread indexingThread = new IndexingThread();
    
    private Set<IndexingPlugin> indexingPlugins = new HashSet<IndexingPlugin>();

    private static Map<Long, IndexingManager> indexingManagers = new HashMap<Long, IndexingManager>();

    private void init() {
        stoppedManaging = false;
        indexingAll = false;
        deletingAll = false;
        optimizing = false;
        lastIndexModification = new Date();
    }

    public static synchronized IndexingManager get(RecordCollection collection) {
        IndexingManager indexingManager = indexingManagers.get(collection.getId());
        if (indexingManager == null) {
            indexingManager = new IndexingManager(collection);
            indexingManagers.put(collection.getId(), indexingManager);
        }
        return indexingManager;
    }

    public static synchronized void remove(RecordCollection collection) {
        FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
        List<RecordCollection> federationCollections = federationServices.listOwnerCollections(collection);
        federationCollections.add(collection);
        for (RecordCollection federationCollection : federationCollections) {
            IndexingManager indexingManager = indexingManagers.get(federationCollection.getId());
            if (indexingManager != null && indexingManager.isActive()) {
                indexingManager.stopManaging();
                // Wait for the indexing to stop
                while (indexingManager.isActive()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        LOGGER.throwing(IndexingManager.class.getName(), "remove", e);
                    }
                }
            }
        }
        indexingManagers.remove(collection.getId());
    }

    public static synchronized void remove(ConnectorInstance connectorInstance) {
        FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
        RecordCollection collection = connectorInstance.getRecordCollection();
        List<RecordCollection> federationCollections = federationServices.listOwnerCollections(collection);
        federationCollections.add(collection);
        for (RecordCollection federationCollection : federationCollections) {
            IndexingManager indexingManager = indexingManagers.get(federationCollection.getId());
            if (indexingManager != null) {
                Long connectorInstanceId = connectorInstance.getId();
                indexingManager.deletableConnectorInstanceIds.add(connectorInstanceId);
                // Wait for the deletion to stop
                while (indexingManager.isActive()
                    && indexingManager.deletableConnectorInstanceIds.contains(connectorInstanceId)) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        LOGGER.throwing(IndexingManager.class.getName(), "remove", e);
                    }
                }
            }
        }
        StatusManager.removing(connectorInstance);
    }

    private IndexingManager(RecordCollection collection) {
        this.collectionId = collection.getId();
    }

    public boolean isActive() {
    	return false;
        //return indexingThread.isAlive();
    }

    public synchronized void startIndexing() {
        init();
//        try {
//            indexingThread.start();
//        } catch (IllegalThreadStateException e) {
//            indexingThread = new IndexingThread();
//            indexingThread.start();
//        }
    }

    public synchronized void startIndexing(boolean indexAll) {
        init();
        this.indexingAll = indexAll;
//        try {
//            indexingThread.start();
//        } catch (IllegalThreadStateException e) {
//            indexingThread = new IndexingThread();
//            indexingThread.start();
//        }
    }

    public synchronized void deleteAll() {
        deletingAll = true;
    }

    public synchronized void reindexAll() {
        indexingAll = true;
    }

    public synchronized void stopManaging() {
        stoppedManaging = true;
        for (IndexingPlugin indexingPlugin : indexingPlugins) {
			indexingPlugin.shutdown();
		}
    }

    public synchronized void optimize() {
        optimizing = true;
    }

    public synchronized boolean isOptimizing() {
        return optimizing;
    }

//    private class IndexingThread extends Thread {
//
//        private static final int MAX_RESULTS = 100;
//
//        public IndexingThread() {
//            super();
//            setPriority(4);
//        }
//
//        private void resetOnException() {
//            deletingAll = false;
//            indexingAll = false;
//            optimizing = false;
//            stoppedManaging = true;
//        }
//
//        @Override
//        public void run() {
//            List<IndexingPlugin> registeredIndexingPlugins = PluginFactory.getPlugins(IndexingPlugin.class);
//			indexingPlugins.addAll(registeredIndexingPlugins);
//        	int failCount = 0;
//        	int maxFailCount = 100;
//            while (!stoppedManaging) {
//                try {
//                    RecordCollectionServices collectionServices = ConstellioSpringUtils
//                        .getRecordCollectionServices();
//                    SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
//                    RecordCollection collection = collectionServices.get(collectionId);
//                    // Initial loading
//                    if (collection != null) {
//                        SolrServer solrServer = solrServices.getSolrServer(collection);
//                        if (solrServer != null) {
//                            EntityManager entityManager = ConstellioPersistenceContext
//                                .getCurrentEntityManager();
//                            try {
//                                if (deletingAll || indexingAll) {
//                                    deleteAllDocs(deletingAll);
//                                }
//                                if (!deletableConnectorInstanceIds.isEmpty()) {
//                                    deleteConnectorInstanceDocs();
//                                }
//                                deleteMarkedDocs();
//                                insertDocs();
//                                if (optimizing || isOptimizationPossible()) {
//                                    optimize();
//                                }
//                                try {
//                                    // Wait one second before looking for modifications
//                                    Thread.sleep(1000);
//                                } catch (InterruptedException e) {
//                                    LOGGER.throwing(IndexingManager.class.getName(), "run", e);
//                                    throw new RuntimeException(e);
//                                }
//                                failCount = 0;
//                            } catch (RuntimeException e) {
//                                if (entityManager.getTransaction().isActive()) {
//                                    entityManager.getTransaction().rollback();
//                                }
//                                throw e;
//                            } finally {
//                                entityManager.close();
//                            }
//                        } else {
//                            LOGGER.severe("SolrServer is null for collection " + collection.getName());
//                            break;
//                        }
//                    } else {
//                        LOGGER.severe("RecordCollection is null for id " + collectionId);
//                        break;
//                    }
//                } catch (RollbackException e) {
//                    failCount++;
//                    if (failCount > maxFailCount) {
//                        resetOnException();
//                        LOGGER.log(Level.SEVERE, "Stopped managing collection for id " + collectionId, e);
//                        throw e;
//                    } else {
//                    	LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                    	// Give one second to recover...
//                    	try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e1) {
//							LOGGER.severe(e1.getMessage());
//						}
//                    }
//                } catch (RuntimeException e) {
//                    failCount++;
//                    if (failCount > maxFailCount) {
//                        resetOnException();
//                        LOGGER.log(Level.SEVERE, "Stopped managing collection for id " + collectionId, e);
//                        throw e;
//                    } else {
//                    	LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                    	// Give one second to recover...
//                    	try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e1) {
//							LOGGER.severe(e1.getMessage());
//						}
//                    }
//                }
//            }
//            LOGGER.log(Level.FINE, "Stopped managing collection for id " + collectionId);
//        }
//
//        private void deleteAllDocs(boolean deleteDatabaseRecords) {
//            RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
//            SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
//            RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
//            RecordCollection collection = collectionServices.get(collectionId);
//            // Initial loading
//            if (collection != null) {
//                SolrServer solrServer = solrServices.getSolrServer(collection);
//                if (solrServer != null) {
//                    try {
//                        solrServer.deleteByQuery("*:*");
//                        solrServer.optimize(true, true);
//                        changeSinceLastOptimization = false;
//                        lastIndexModification = new Date();
//                    } catch (SolrServerException e) {
//                        LOGGER.throwing(IndexingManager.class.getName(), "deleteAllDocs", e);
//                        throw new RuntimeException(e);
//                    } catch (IOException e) {
//                        LOGGER.throwing(IndexingManager.class.getName(), "deleteAllDocs", e);
//                        throw new RuntimeException(e);
//                    }
//
//                    if (deleteDatabaseRecords) {
//                        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
//                        if (!entityManager.getTransaction().isActive()) {
//                            entityManager.getTransaction().begin();
//                        }
//                        recordServices.deleteRecords(collection);
//                        entityManager.getTransaction().commit();
//                        entityManager.clear();
//                        collection = entityManager.find(RecordCollection.class, collection.getId());
//
//                        StatusManager.deletingAll(collection);
//                    }
//                    deletingAll = false;
//                } else {
//                    LOGGER.log(Level.FINE, "SolrServer is null, impossible to delete docs for collection "
//                        + collection.getName());
//                }
//            }
//        }
//
//        private void deleteConnectorInstanceDocs() {
//            RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
//            SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
//            RecordCollection collection = collectionServices.get(collectionId);
//            // Initial loading
//            if (collection != null) {
//                SolrServer solrServer = solrServices.getSolrServer(collection);
//                if (solrServer != null) {
//                    try {
//                        for (Iterator<Long> it = deletableConnectorInstanceIds.iterator(); it.hasNext();) {
//                            Long connectorInstanceId = it.next();
//                            solrServer.deleteByQuery(IndexField.CONNECTOR_INSTANCE_ID_FIELD + ":"
//                                + connectorInstanceId);
//                            it.remove();
//                        }
//                        solrServer.optimize(true, true);
//                        changeSinceLastOptimization = false;
//                        lastIndexModification = new Date();
//                    } catch (SolrServerException e) {
//                        LOGGER.throwing(IndexingManager.class.getName(), "deleteConnectorInstanceDocs", e);
//                        throw new RuntimeException(e);
//                    } catch (IOException e) {
//                        LOGGER.throwing(IndexingManager.class.getName(), "deleteConnectorInstanceDocs", e);
//                        throw new RuntimeException(e);
//                    }
//                } else {
//                    LOGGER.log(Level.FINE, "SolrServer is null, impossible to delete docs for collection "
//                        + collection.getName());
//                }
//            }
//        }
//
//        private void deleteMarkedDocs() {
//            RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
//            SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
//            RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
//            FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
//
//            RecordCollection collection = collectionServices.get(collectionId);
//            // Initial loading
//            if (collection != null) {
//                SolrServer solrServer = solrServices.getSolrServer(collection);
//                if (solrServer != null) {
//                    List<Record> excludedOrDeletedRecords = recordServices.listMarkedForExclusionOrDeletion(
//                        collection, MAX_RESULTS);
//                    while (!excludedOrDeletedRecords.isEmpty()) {
//                        for (Iterator<Record> it = excludedOrDeletedRecords.iterator(); it.hasNext();) {
//                            Record excludedOrDeletedRecord = it.next();
//                            String indexId = excludedOrDeletedRecord.getUrl();
//                            if (indexId != null) {
//                                LOGGER.log(Level.INFO, "Deleting doc " + indexId);
//                                try {
//                                    solrServer.deleteById(indexId);
//                                    changeSinceLastOptimization = true;
//                                    lastIndexModification = new Date();
//                                } catch (SolrServerException e) {
//                                    LOGGER.throwing(IndexingManager.class.getName(), "deleteMarkedDocs", e);
//                                    throw new RuntimeException(e);
//                                } catch (IOException e) {
//                                    LOGGER.throwing(IndexingManager.class.getName(), "deleteMarkedDocs", e);
//                                    throw new RuntimeException(e);
//                                }
//                            } else {
//                                LOGGER.severe("No indexId for record " + excludedOrDeletedRecord.getId()
//                                    + " (" + excludedOrDeletedRecord.getUrl() + ")");
//                            }
//
//                            EntityManager entityManager = ConstellioPersistenceContext
//                                .getCurrentEntityManager();
//                            if (!entityManager.getTransaction().isActive()) {
//                                entityManager.getTransaction().begin();
//                            }
//
//                            excludedOrDeletedRecord = entityManager.find(Record.class,
//                                excludedOrDeletedRecord.getId());
//
//                            RecordCollection recordCollection = excludedOrDeletedRecord
//                                .getConnectorInstance().getRecordCollection();
//                            if (collection.equals(recordCollection)) {
//                                if (excludedOrDeletedRecord.isDeleted()) {
//                                    if (collection.isIncludedInFederation()) {
//                                        // The last federation owner will have the responsibility to
//                                        // delete
//                                        // the record
//                                        List<RecordCollection> ownerCollections = federationServices
//                                            .listOwnerCollections(collection);
//                                        for (RecordCollection ownerCollection : ownerCollections) {
//                                            federationServices.addFederationDeletionRequired(
//                                                excludedOrDeletedRecord, ownerCollection);
//                                        }
//                                    }
//                                    LOGGER.log(Level.FINE, "Deleting record "
//                                        + excludedOrDeletedRecord.getId());
//                                    recordServices.makeTransient(excludedOrDeletedRecord);
//                                    StatusManager.deleting(excludedOrDeletedRecord);
//                                } else {
//                                    LOGGER.log(Level.FINE, "Marking record "
//                                        + excludedOrDeletedRecord.getId() + " as excluded");
//                                    recordServices.markRecordAsExcluded(excludedOrDeletedRecord);
//                                    if (collection.isIncludedInFederation()) {
//                                        List<RecordCollection> ownerCollections = federationServices
//                                            .listOwnerCollections(collection);
//                                        for (RecordCollection ownerCollection : ownerCollections) {
//                                            federationServices.addFederationDeletionRequired(
//                                                excludedOrDeletedRecord, ownerCollection);
//                                        }
//                                    }
//                                    recordServices.makePersistent(excludedOrDeletedRecord);
//                                    StatusManager.excluding(excludedOrDeletedRecord);
//                                }
//                            }
//                            entityManager.getTransaction().commit();
//                            entityManager.clear();
//                            collection = entityManager.find(RecordCollection.class, collection.getId());
//                        }
//                        try {
//                            LOGGER.log(Level.FINE, "Committing solrServer after delete");
//                            solrServer.commit();
//                        } catch (SolrServerException e) {
//                            try {
//                                solrServer.rollback();
//                            } catch (SolrServerException e2) {
//                            } catch (IOException e2) {
//                            }
//                            LOGGER.throwing(IndexingManager.class.getName(), "deleteMarkedDocs", e);
//                            throw new RuntimeException(e);
//                        } catch (IOException e) {
//                            try {
//                                solrServer.rollback();
//                            } catch (SolrServerException e2) {
//                            } catch (IOException e2) {
//                            }
//                            LOGGER.throwing(IndexingManager.class.getName(), "deleteMarkedDocs", e);
//                            throw new RuntimeException(e);
//                        }
//                        excludedOrDeletedRecords = recordServices.listMarkedForExclusionOrDeletion(
//                            collection, MAX_RESULTS);
//                    }
//
//                    if (collection.isFederationOwner()) {
//                        List<FederationRecordDeletionRequired> exclusionOrDeletionRequiredList = federationServices
//                            .listMarkedForExclusionOrDeletion(collection, MAX_RESULTS);
//                        while (!exclusionOrDeletionRequiredList.isEmpty()) {
//                            for (Iterator<FederationRecordDeletionRequired> it = exclusionOrDeletionRequiredList
//                                .iterator(); it.hasNext();) {
//                                FederationRecordDeletionRequired exclusionOrDeletionRequired = it.next();
//                                String indexId = exclusionOrDeletionRequired.getRecordUrl();
//                                if (indexId != null) {
//                                    LOGGER.log(Level.INFO, "Deleting doc " + indexId);
//                                    try {
//                                        solrServer.deleteById(indexId);
//                                        changeSinceLastOptimization = true;
//                                        lastIndexModification = new Date();
//                                    } catch (SolrServerException e) {
//                                        LOGGER.throwing(IndexingManager.class.getName(), "deleteMarkedDocs",
//                                            e);
//                                        throw new RuntimeException(e);
//                                    } catch (IOException e) {
//                                        LOGGER.throwing(IndexingManager.class.getName(), "deleteMarkedDocs",
//                                            e);
//                                        throw new RuntimeException(e);
//                                    }
//                                } else {
//                                    LOGGER.severe("No indexId for record "
//                                        + exclusionOrDeletionRequired.getId() + " ("
//                                        + exclusionOrDeletionRequired.getRecordUrl() + ")");
//                                }
//
//                                EntityManager entityManager = ConstellioPersistenceContext
//                                    .getCurrentEntityManager();
//                                if (!entityManager.getTransaction().isActive()) {
//                                    entityManager.getTransaction().begin();
//                                }
//
//                                exclusionOrDeletionRequired = entityManager.find(
//                                    FederationRecordDeletionRequired.class, exclusionOrDeletionRequired
//                                        .getId());
//
//                                entityManager.remove(exclusionOrDeletionRequired);
//                                entityManager.getTransaction().commit();
//                                entityManager.clear();
//
//                                collection = entityManager.find(RecordCollection.class, collection.getId());
//                                exclusionOrDeletionRequiredList = federationServices
//                                    .listMarkedForExclusionOrDeletion(collection, MAX_RESULTS);
//                            }
//                        }
//                    }
//                } else {
//                    LOGGER.log(Level.FINE, "SolrServer is null, impossible to delete docs for collection "
//                        + collection.getName());
//                }
//            } else if (collection == null) {
//                LOGGER.log(Level.FINE, "Collection is null, impossible to delete docs");
//            }
//        }
//
//        private void insertDocs() {
//            RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
//            SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
//            RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
//            FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
//
//            RecordCollection collection = collectionServices.get(collectionId);
//            if (collection != null && !collection.isSynchronizationRequired()) {
//                SolrServer solrServer = solrServices.getSolrServer(collection);
//                if (solrServer != null) {
//                    List<Record> recordsToIndex = recordServices.listMarkedForUpdateIndex(collection,
//                        MAX_RESULTS);
//                    if (!recordsToIndex.isEmpty()) {
//                        for (Iterator<Record> it = recordsToIndex.iterator(); it.hasNext();) {
//                            // Content of database has changed and may not match the index structure
//                            if (collection.isSynchronizationRequired()) {
//                                LOGGER.log(Level.INFO,
//                                    "Stopping insertDocs() because synchronization is required");
//                                break;
//                            }
//                            EntityManager entityManager = ConstellioPersistenceContext
//                                .getCurrentEntityManager();
//                            if (!entityManager.getTransaction().isActive()) {
//                                entityManager.getTransaction().begin();
//                            }
//                            Record record = it.next();
//
//                            record = entityManager.find(Record.class, record.getId());
//
//                            insertDoc(record, collection, solrServer);
//                            entityManager.getTransaction().commit();
//                            entityManager.clear();
//                            collection = entityManager.find(RecordCollection.class, collection.getId());
//                        }
//                        try {
//                            LOGGER.log(Level.FINE, "Committing solrServer after insert");
//                            solrServer.commit();
//                        } catch (SolrServerException e) {
//                            try {
//                                solrServer.rollback();
//                            } catch (SolrServerException e2) {
//                            } catch (IOException e2) {
//                            }
//                            LOGGER.throwing(IndexingManager.class.getName(), "insertDocs", e);
//                            throw new RuntimeException(e);
//                        } catch (IOException e) {
//                            try {
//                                solrServer.rollback();
//                            } catch (SolrServerException e2) {
//                            } catch (IOException e2) {
//                            }
//                            LOGGER.throwing(IndexingManager.class.getName(), "insertDocs", e);
//                            throw new RuntimeException(e);
//                        }
//                    }
//
//                    List<FederationRecordIndexingRequired> indexingRequiredList = federationServices
//                        .listMarkedForUpdateIndex(collection, MAX_RESULTS);
//                    if (!indexingRequiredList.isEmpty()) {
//                        for (Iterator<FederationRecordIndexingRequired> it = indexingRequiredList.iterator(); it
//                            .hasNext();) {
//                            // Content of database has changed and may not match the index structure
//                            if (collection.isSynchronizationRequired()) {
//                                LOGGER.log(Level.INFO,
//                                    "Stopping insertDocs() because synchronization is required");
//                                break;
//                            }
//
//                            FederationRecordIndexingRequired indexingRequired = it.next();
//                            Record record = recordServices.get(indexingRequired.getRecordId(), collection);
//
//                            EntityManager entityManager = ConstellioPersistenceContext
//                                .getCurrentEntityManager();
//                            if (!entityManager.getTransaction().isActive()) {
//                                entityManager.getTransaction().begin();
//                            }
//
//                            indexingRequired = entityManager.find(FederationRecordIndexingRequired.class,
//                                indexingRequired.getId());
//
//                            if (record != null) {
//                                insertDoc(record, collection, solrServer);
//                            }
//                            entityManager.remove(indexingRequired);
//                            entityManager.getTransaction().commit();
//                            entityManager.clear();
//                            collection = entityManager.find(RecordCollection.class, collection.getId());
//                        }
//                        try {
//                            LOGGER.log(Level.FINE, "Committing solrServer after insert");
//                            solrServer.commit();
//                        } catch (SolrServerException e) {
//                            try {
//                                solrServer.rollback();
//                            } catch (SolrServerException e2) {
//                            } catch (IOException e2) {
//                            }
//                            LOGGER.throwing(IndexingManager.class.getName(), "insertDocs", e);
//                            throw new RuntimeException(e);
//                        } catch (IOException e) {
//                            try {
//                                solrServer.rollback();
//                            } catch (SolrServerException e2) {
//                            } catch (IOException e2) {
//                            }
//                            LOGGER.throwing(IndexingManager.class.getName(), "insertDocs", e);
//                            throw new RuntimeException(e);
//                        }
//                    }
//
//                    if (indexingAll) {
//                        // If we were indexing all, we stop now
//                        indexingAll = false;
//                        // optimize();
//                    }
//                } else {
//                    LOGGER.log(Level.FINE, "SolrServer is null, impossible to insert docs for collection "
//                        + collection.getName());
//                }
//            } else if (collection == null) {
//                LOGGER.log(Level.FINE, "Collection is null, impossible to insert docs");
//            } else if (collection.isSynchronizationRequired()) {
//                LOGGER.log(Level.FINE, "Synchronization required, impossible to insert docs");
//            }
//        }
//
//        private void insertDoc(Record record, RecordCollection collection, SolrServer solrServer) {
//            RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
//            FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
//            IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
//
//            // Attach to current transaction
//            SolrInputDocument doc = new SolrInputDocument();
//
//            for (IndexingPlugin indexingPlugin : indexingPlugins) {
//                indexingPlugin.beforeIndexing(record, doc);
//                // Save any change performed
//                recordServices.makePersistent(record);
//            }
//
//            // Might have been excluded or deleted by a plugin
//            if (!record.isExcluded() && !record.isDeleted()) {
//            	indexFieldServices.populateSolrDoc(doc, record, collection);
//
//                Date lastIndexed = new Date();
//                doc.addField(IndexField.LAST_INDEXED_FIELD, lastIndexed);
//
//                try {
//                    LOGGER.log(Level.INFO, "Inserting doc " + record.getUrl());
//                    solrServer.add(doc);
//                    changeSinceLastOptimization = true;
//                    lastIndexModification = new Date();
//                } catch (SolrServerException e) {
//                    LOGGER.throwing(IndexingManager.class.getName(), "insertDocs", e);
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    LOGGER.throwing(IndexingManager.class.getName(), "insertDocs", e);
//                    throw new RuntimeException(e);
//                }
//
//                RecordCollection recordCollection = record.getConnectorInstance().getRecordCollection();
//                if (collection.equals(recordCollection)) {
//                    LOGGER.log(Level.FINE, "Marking record " + record.getId() + " as updated");
//                    record.setUpdateIndex(false);
//                    record.setLastIndexed(lastIndexed);
//                    if (collection.isIncludedInFederation()) {
//                        List<RecordCollection> ownerCollections = federationServices.listOwnerCollections(collection);
//                        for (RecordCollection ownerCollection : ownerCollections) {
//                            federationServices.addFederationIndexingRequired(record, ownerCollection);
//                        }
//                    }
//                    recordServices.makePersistent(record);
//                }
//                StatusManager.indexing(record, lastIndexed, collection);
//            } else {
//                LOGGER.log(Level.FINE, "Skipping excluded record : " + record.getUrl());
//            }
//        }
//
//        private void optimize() {
//            RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
//            SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
//            RecordCollection collection = collectionServices.get(collectionId);
//            if (collection != null && !collection.isSynchronizationRequired()) {
//                SolrServer solrServer = solrServices.getSolrServer(collection);
//                if (solrServer != null) {
//                    try {
//                        solrServer.optimize(true, true);
//                    } catch (SolrServerException e) {
//                        LOGGER.throwing(IndexingManager.class.getName(), "optimize", e);
//                        throw new RuntimeException(e);
//                    } catch (IOException e) {
//                        LOGGER.throwing(IndexingManager.class.getName(), "optimize", e);
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//            optimizing = false;
//            changeSinceLastOptimization = false;
//        }
//
//        private boolean isOptimizationPossible() {
//            boolean optimizationPossible;
//            if (changeSinceLastOptimization) {
//                // One minute after last change
//                Date delayAfterLastChange = DateUtils.addMinutes(lastIndexModification, 1);
//                Date now = new Date();
//                // At least one minute since last modification to the index
//                if (now.after(delayAfterLastChange)) {
//                    optimizationPossible = true;
//                } else {
//                    optimizationPossible = false;
//                }
//            } else {
//                optimizationPossible = false;
//            }
//            return optimizationPossible;
//        }
//    }

}
