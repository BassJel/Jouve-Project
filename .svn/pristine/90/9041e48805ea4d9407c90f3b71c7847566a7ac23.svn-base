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
package com.doculibre.constellio.services.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import junit.framework.TestCase;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.DateUtil;
import org.junit.Assert;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.entities.FieldTypeClass;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.RecordMeta;
import com.doculibre.constellio.entities.relevance.BoostRule;
import com.doculibre.constellio.entities.relevance.RecordCollectionBoost;
import com.doculibre.constellio.feedprotocol.RFC822DateUtil;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.BaseCRUDServicesImpl;
import com.doculibre.constellio.services.FieldTypeClassServices;
import com.doculibre.constellio.services.FieldTypeServices;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.utils.ClasspathUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.google.enterprise.connector.spi.Value;

public class ServicesTestUtils {
    protected static final Logger LOGGER = Logger.getLogger(ServicesTestUtils.class.getName());
    protected static EntityTransaction entityTransaction;
    protected static EntityManager entityManager;

    private ServicesTestUtils() {

    }

    /**
     * NEVER call this method from a web application!!
     */
    public static void init() {

        if (System.getProperty("base-persistence-file") == null) {
            System.setProperty("base-persistence-file", "persistence_tests.xml");
        }
        if (System.getProperty("test-bd-name") == null) {
            System.setProperty("test-bd-name", "constellio_derby");
        }
        File tempWebInfFolder = ClasspathUtils.getWorkDir();
        if (tempWebInfFolder.exists()) {
            deleteDir(tempWebInfFolder);
        }

        try {
            entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
            entityTransaction = entityManager.getTransaction();
            if (!entityTransaction.isActive()) {
                entityTransaction.begin();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void cleanup() {
        try {
            if (entityTransaction.isActive()) {
                entityTransaction.commit();
            }
        } catch (RuntimeException e) {
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            TestCase.fail("Impossible to commit last transaction");
        } finally {
            try {
                entityManager.close();
            } finally {
                try {
                    String testDB = System.getProperty("test-bd-name");
                    DriverManager.getConnection("jdbc:derby:" + System.getProperty("workDirFrontSlash")
                        + "/db/" + testDB + ";shutdown=true");
                } catch (SQLException e) {
                    // It's a feature!
                }
                System.gc();
                ConstellioPersistenceContext.close();
            }
        }
        File tempWebInfFolder = ClasspathUtils.getWorkDir();
        if (tempWebInfFolder.exists()) {
            deleteDir(tempWebInfFolder);
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public static RecordCollection persistExampleRecordCollection() {
        return persistExampleRecordCollection(0);
    }

    public static RecordCollection persistExampleRecordCollection(int i) {
        final String name = "recordCollection-test" + i;
        RecordCollectionServices recordCollectionServices = ConstellioSpringUtils
            .getRecordCollectionServices();
        RecordCollection recordCollection = new RecordCollection();
        recordCollection.setName(name);
        recordCollection.setPosition(i);
        recordCollectionServices.makePersistent(recordCollection, true);
        return recordCollection;
    }

    public static ConnectorManager persistExampleConnectorManager() {
        final String name = "connectorManagers-test";
        ConnectorManager connectorManager = new ConnectorManager();
        connectorManager.setName(name);
        connectorManager.setDescription("A connection manager");
        try {
            connectorManager.setUrl("http://example.com/connectionManager");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            entityManager.persist(connectorManager);
        }
        return connectorManager;
    }

    public static ConnectorType persistExampleConnectorType(ConnectorManager connectorManager) {
        return persistExampleConnectorType(0, connectorManager);
    }

    public static ConnectorType persistExampleConnectorType(int i, ConnectorManager connectorManager) {
        final String name = "connectorType-test-" + i;
        ConnectorType connectorType = new ConnectorType();
        connectorType.setName(name);

        connectorManager.addConnectorType(connectorType);
        entityManager.persist(connectorManager);

        return connectorType;
    }

    public static ConnectorInstance persistExampleConnectorInstance(int conncetorInstanceId,
        ConnectorType connectorType, RecordCollection recordCollection) {

        final String name = "connectorInstance-" + conncetorInstanceId;
        ConnectorInstance connectorInstance = new ConnectorInstance();
        connectorInstance.setName(name);
        connectorInstance.setDisplayName(name);
        connectorInstance.setConnectorType(connectorType);
        connectorInstance.setRecordCollection(recordCollection);

        entityManager.persist(connectorInstance);
        connectorType.addConnectorInstance(connectorInstance);
        recordCollection.addConnectorInstance(connectorInstance);

        return connectorInstance;
    }

    public static Record persistExampleRecord(int recordId, ConnectorInstance connectorInstance) {

        Record record = new Record();
        String url = "http://example.com/" + recordId;
        record.setUrl(url);
        record.setAuthmethod("httpbasic" + recordId);
        record.setParsedContent("content" + recordId);
        record.setLastModified(new Date());

        record.setConnectorInstance(connectorInstance);
        record.setMimetype("text/html");
        entityManager.persist(record);
        record.setConnectorInstance(connectorInstance);

        return record;
    }

    public static Record persistExampleRecord() {
        ConnectorManager connectorManager = persistExampleConnectorManager();
        ConnectorType connectorType = persistExampleConnectorType(connectorManager);

        ConnectorInstance connectorInstance = persistExampleConnectorInstance(0, connectorType,
            persistExampleRecordCollection());

        return persistExampleRecord(0, connectorInstance);
    }

    public static List<RecordMeta> createSomeMetas(ConnectorInstance connectorInstance) {
        List<RecordMeta> metas = new ArrayList<RecordMeta>();
        for (int i = 0; i < 5; i++) {
            RecordMeta meta = new RecordMeta();
            String name = "name-" + Math.random();
            ConnectorInstanceMeta connectorInstanceMeta = connectorInstance.getOrCreateMeta(name);
            meta.setConnectorInstanceMeta(connectorInstanceMeta);
            meta.setContent("content-" + Math.random());
            metas.add(meta);
        }
        return metas;
    }

    public static RecordMeta newMeta(ConnectorInstance connectorInstance, int metaId) {
        RecordMeta meta = new RecordMeta();
        String name = "name-" + metaId;
        ConnectorInstanceMeta connectorInstanceMeta = connectorInstance.getOrCreateMeta(name);
        meta.setConnectorInstanceMeta(connectorInstanceMeta);
        meta.setContent("content-" + Math.random());
        return meta;
    }

    public static ConnectorInstanceMeta persistExampleConnectorInstanceMeta(String name,
        ConnectorInstance connectorInstance) {
        ConnectorInstanceMeta connectorInstanceMeta = new ConnectorInstanceMeta();
        connectorInstanceMeta.setName(name);
        connectorInstanceMeta.setConnectorInstance(connectorInstance);
        entityManager.persist(connectorInstanceMeta);
        return connectorInstanceMeta;
    }

    public static ConnectorInstanceMeta persistExampleConnectorInstanceMeta(int i,
        ConnectorInstance connectorInstance) {
        return persistExampleConnectorInstanceMeta("connectorInstanceMeta-" + 1, connectorInstance);

    }

    public static IndexField getPersistedExampleIndexField(String name,
        ConnectorInstanceMeta connectorInstanceMeta, RecordCollection collection) {

        FieldTypeServices services = ConstellioSpringUtils.getFieldTypeServices();
        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
        IndexField indexField = indexFieldServices.get(name, collection);
        if (indexField == null) {
            ConnectorManager connectorManager = connectorInstanceMeta.getConnectorInstance()
                .getConnectorType().getConnectorManager();
            indexField = new IndexField();

            indexField.setName(name);
            indexField.addConnectorInstanceMeta(connectorInstanceMeta);
            collection.addIndexField(indexField);
            // TODO: a donner en parametres:
            FieldType fieldType = services.get("solr.StrField");
            indexField.setFieldType(fieldType);
            indexField.setIndexed(true);
            indexField.setMultiValued(true);
            entityManager.persist(indexField);
            // fieldType name="string" class="solr.StrField" sortMissingLast="true"
            // omitNorms="true"
            fieldType = services.get("string");
            indexField.setFieldType(fieldType);
            indexField.setIndexed(true);
            indexField.setMultiValued(true);
        }

        return indexField;
    }

    private static FieldTypeClass getPersistedExampleFieldTypeClass(String nom,
        ConnectorManager connectorManager) {
        FieldTypeClassServices services = ConstellioSpringUtils.getFieldTypeClassServices();

        FieldTypeClass fieldTypeClass = services.get(nom);
        if (fieldTypeClass == null) {
            fieldTypeClass = new FieldTypeClass();
            fieldTypeClass.setClassName(nom);
            fieldTypeClass.setConnectorManager(connectorManager);
            services.makePersistent(fieldTypeClass);
        }
        return fieldTypeClass;
    }

    public static RecordCollectionBoost persistExampleRecordCollectionBoost(String name,
        IndexField indexField, RecordCollection collection) {
        RecordCollectionBoost collectionBoost = new RecordCollectionBoost();

        collectionBoost.setName(name);
        collectionBoost.setAssociatedField(indexField);
        collection.addRecordCollectionBoost(collectionBoost);
        entityManager.persist(collectionBoost);
        return collectionBoost;
    }

    public static BoostRule persistNewBoostRule(RecordCollectionBoost collectionBoost) {
        BoostRule boostRule = new BoostRule();
        collectionBoost.addBoostRule(boostRule);
        entityManager.persist(boostRule);
        return boostRule;
    }

    // Use this method to remove the entities that do not override method
    // makeTransient
    public void deleteSimpleEntities(Class<? extends ConstellioEntity> entityClass) {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        List<? extends ConstellioEntity> list = list(entityClass);
        for (Object entity : list) {
            entityManager.remove(entity);
        }
        entityManager.getTransaction().commit();
    }

    // FIXME voir avec Vincent
    public static void addRecordToSolr(Record record) {
        ConnectorInstance connectorInstance = record.getConnectorInstance();
        RecordCollection collection = connectorInstance.getRecordCollection();

        SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
        SolrServer solrServer = solrServices.getSolrServer(collection);
        if (solrServer == null) {
            solrServices.updateSchemaFields(collection);
            solrServices.initCore(collection);
            solrServer = solrServices.getSolrServer(collection);
        }
        if (solrServer == null) {
            throw new RuntimeException("solr n est pas correctement initialisé");
        }

        SolrInputDocument doc = new SolrInputDocument();
        if (!record.isExcluded()) {
            if (record.getBoost() != null) {
                doc.setDocumentBoost(new Float(record.getBoost()));
            }
            for (String internalIndexFieldName : IndexField.INTERNAL_FIELDS) {
                addField(doc, record, internalIndexFieldName);
            }
            for (IndexField indexField : collection.getIndexFields()) {
                if (!indexField.isInternalField()) {
                    addFieldIfPossible(doc, record, indexField.getName());
                }
            }

            try {
                LOGGER.log(Level.INFO, "Inserting doc " + record.getUrl());
                solrServer.add(doc);
            } catch (SolrServerException e) {
                LOGGER.throwing(IndexingManager.class.getName(), "insertDocs", e);
                throw new RuntimeException(e);
            } catch (IOException e) {
                LOGGER.throwing(IndexingManager.class.getName(), "insertDocs", e);
                throw new RuntimeException(e);
            }

            LOGGER.log(Level.INFO, "Marking record " + record.getId() + " as updated");
            record.setUpdateIndex(false);
            record.setLastIndexed(new Date());
            entityManager.persist(record);

        } else {
            LOGGER.log(Level.INFO, "Skipping excluded record : " + record.getUrl());
        }

        try {
            solrServer.commit();
        } catch (SolrServerException e) {
            LOGGER.throwing(ServicesTestUtils.class.getName(), "addRecordToSolr", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.throwing(ServicesTestUtils.class.getName(), "addRecordToSolr", e);
            throw new RuntimeException(e);
        }
    }

    private static void addField(SolrInputDocument doc, Record record, String indexFieldName) {
        IndexField indexField = record.getConnectorInstance().getRecordCollection().getIndexField(
            indexFieldName);
        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
        List<Object> fieldValues = indexFieldServices.extractFieldValues(record, indexField);
        for (Object fieldValue : fieldValues) {
            try {
                Calendar calendar = Value.iso8601ToCalendar(String.valueOf(fieldValue));
                fieldValue = DateUtil.getThreadLocalDateFormat().format(calendar.getTime());
            } catch (ParseException e) {
                // Try with RFC
                try {
                    Date contentAsDate = RFC822DateUtil.parse(String.valueOf(fieldValue));
                    // Convert RFC822 (Google connector dates) to Solr dates
                    fieldValue = DateUtil.getThreadLocalDateFormat().format(contentAsDate);
                } catch (ParseException ee) {
                    // Ignored
                }
            }
            doc.addField(indexFieldName, fieldValue);
        }
    }

    private static void addFieldIfPossible(SolrInputDocument doc, Record record, String indexFieldName) {
        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
        ConnectorInstance connectorInstance = record.getConnectorInstance();
        RecordCollection collection = connectorInstance.getRecordCollection();
        IndexField indexField = collection.getIndexField(indexFieldName);
        List<Object> fieldValues = indexFieldServices.extractFieldValues(record, indexField);
        if (indexField.isMultiValued() || fieldValues.size() == 1) {
            addField(doc, record, indexFieldName);
        }
    }

    public static RecordMeta newRecordMeta(ConnectorInstanceMeta connectorInstanceMeta) {
        RecordMeta recordMeta = new RecordMeta();
        recordMeta.setConnectorInstanceMeta(connectorInstanceMeta);
        return recordMeta;
    }

    public static Object getEntity(String name, Class<? extends ConstellioEntity> entityClass) {
        String sb = "from " + entityClass.getName() + " o where o.name=?";
        Query query = entityManager.createQuery(sb);
        query.setParameter(1, name);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static Object getEntity(int id, Class<?> entityClass) {
        return entityManager.find(entityClass, id);
    }

    public static List<? extends ConstellioEntity> list(Class<? extends ConstellioEntity> entityClass) {
        BaseCRUDServices<? extends ConstellioEntity> services = getServices(entityClass);
        return services.list();
        // String sb = "from " + entityClass.getName();
        // Query query = getEntityManager().createQuery(sb);
        // return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public static void deleteEntities(Class<? extends ConstellioEntity> entityClass) {
        BaseCRUDServices<ConstellioEntity> services = (BaseCRUDServices<ConstellioEntity>) getServices(entityClass);
        for (ConstellioEntity entity : services.list()) {
            services.makeTransient(entity);
        }
    }

    @SuppressWarnings("unchecked")
    private static BaseCRUDServices<? extends ConstellioEntity> getServices(
        Class<? extends ConstellioEntity> entity) {
        String servicesMethodName = "get" + entity.getSimpleName() + "Services";
        Class params[] = {};
        Object paramsObj[] = {};
        Method method;
        try {
            method = ConstellioSpringUtils.class.getDeclaredMethod(servicesMethodName, params);
        } catch (SecurityException e) {
            method = null;
        } catch (NoSuchMethodException e) {
            method = null;
        }
        if (method == null) {
            LOGGER.info("No services associated with entity" + entity.getSimpleName());
            return getBaseCRUDServicesImpl(entity);
        } else {
            try {
                return (BaseCRUDServices<? extends ConstellioEntity>) method.invoke(
                    ConstellioSpringUtils.class, paramsObj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final Map<String, BaseCRUDServices<? extends ConstellioEntity>> simpleServicesMap = new HashMap<String, BaseCRUDServices<? extends ConstellioEntity>>();

    @SuppressWarnings("unchecked")
    private static BaseCRUDServices<? extends ConstellioEntity> getBaseCRUDServicesImpl(
        Class<? extends ConstellioEntity> entityClass) {
        String name = entityClass.getSimpleName();
        BaseCRUDServices<? extends ConstellioEntity> services = simpleServicesMap.get(name);
        if (services == null) {
            services = new BaseCRUDServicesImpl(entityClass, entityManager);
            simpleServicesMap.put(name, services);
        }
        return services;
    }

    public static void main(String[] argv) {
        init();
        BaseCRUDServices<? extends ConstellioEntity> services = getServices(RecordCollection.class);
        BaseCRUDServices<? extends ConstellioEntity> services2 = ConstellioSpringUtils
            .getRecordCollectionServices();
        Assert.assertEquals(services2.getClass().getName(), services.getClass().getName());

        services = getServices(ConnectorInstanceMeta.class);
        services2 = simpleServicesMap.get(ConnectorInstanceMeta.class.getSimpleName());
        Assert.assertEquals(services2.getClass().getName(), services.getClass().getName());
        cleanup();
    }
}
