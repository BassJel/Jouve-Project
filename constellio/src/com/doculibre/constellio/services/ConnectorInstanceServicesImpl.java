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
package com.doculibre.constellio.services;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.ConnectorTypeMetaMapping;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.init.InitConnectorInstancePlugin;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class ConnectorInstanceServicesImpl extends BaseCRUDServicesImpl<ConnectorInstance> implements
    ConnectorInstanceServices {

    public ConnectorInstanceServicesImpl(EntityManager entityManager) {
        super(ConnectorInstance.class, entityManager);
    }

    @Override
    public ConnectorInstance get(String connectorName) {
        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put("name", connectorName);
        return get(criteria);
    }

    @Override
    public ConnectorInstance makePersistent(ConnectorInstance connectorInstance) {
        boolean adding = connectorInstance.getId() == null;
        RecordCollection collection = connectorInstance.getRecordCollection();
        if (adding) {
            ConnectorType connectorType = connectorInstance.getConnectorType();
            for (ConnectorTypeMetaMapping metaMapping : connectorType.getMetaMappings()) {
                IndexField indexField;
                if (metaMapping.isUniqueKey()) {
                    indexField = collection.getUniqueKeyIndexField();
                } else if (metaMapping.isDefaultSearchField()) {
                    indexField = collection.getDefaultSearchIndexField();
                } else {
                    String indexFieldName = metaMapping.getIndexFieldName();
                    if (indexFieldName != null) {
                        indexField = collection.getIndexField(indexFieldName);
                        if (indexField == null) {
                            indexField = metaMapping.createIndexField();
                            if (indexField != null) {
                                collection.addIndexField(indexField);
                            }
                        }
                    } else {
                        indexField = null;
                    }
                }
                if (indexField != null) {
                    ConnectorInstanceMeta meta = new ConnectorInstanceMeta();
                    meta.setName(metaMapping.getMetaName());
                    connectorInstance.addConnectorInstanceMeta(meta);
                    meta.addIndexField(indexField);
                }
            }
            InitConnectorInstancePlugin initPlugin = PluginFactory
                .getPlugin(InitConnectorInstancePlugin.class);
            if (initPlugin != null) {
                initPlugin.init(connectorInstance);
            }
        }
        ConnectorInstance result = super.makePersistent(connectorInstance);
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        collectionServices.makePersistent(collection, adding);
        return result;
    }

    @Override
    public ConnectorInstance makeTransient(ConnectorInstance entity) {
        boolean stoppedIndexingManager = false;
        IndexingManager indexingManager = IndexingManager.get(entity.getRecordCollection());
        if (indexingManager.isActive()) {
            indexingManager.stopManaging();
            stoppedIndexingManager = true;
            while (indexingManager.isActive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }

        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        recordServices.deleteRecords(entity);
        
        String sqlIndexFieldMetaMapping = "DELETE FROM IndexFieldMetaMapping WHERE meta_id IN"
            + " (SELECT cim.id FROM ConnectorInstanceMeta cim WHERE cim.connectorInstance_id=?)";
        Query indexFieldMetaMappingQuery = getEntityManager().createNativeQuery(sqlIndexFieldMetaMapping);
        indexFieldMetaMappingQuery.setParameter(1, entity.getId());
        indexFieldMetaMappingQuery.executeUpdate();

        ConnectorInstance result = super.makeTransient(entity);
        
        // Throw any database related exception before modifying Solr files
        getEntityManager().flush();
        IndexingManager.remove(entity);

        if (stoppedIndexingManager) {
            indexingManager.startIndexing();
        }

        final String connectorName = entity.getName();
        Runnable connectorManagerRunnable = new Runnable() {
            @Override
            public void run() {
                ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
                    .getConnectorManagerServices();
                ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
                if (connectorManagerServices.isExistingConnector(connectorManager, connectorName)) {
                    if (connectorManagerServices.isConnectorEnabled(connectorManager, connectorName)) {
                        connectorManagerServices.disableConnector(connectorManager, connectorName);
                    }
                    connectorManagerServices.removeConnector(connectorManager, connectorName);
                }
            }
        };
        new Thread(connectorManagerRunnable).start();
        return result;
    }

}
