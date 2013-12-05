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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

import javax.persistence.EntityManager;
import javax.persistence.PessimisticLockException;
import javax.persistence.Query;

import org.apache.commons.lang.NotImplementedException;
import org.apache.solr.common.SolrDocument;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.RecordMeta;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.google.enterprise.connector.spi.SpiConstants;

public class RecordServicesImpl extends BaseCRUDServicesImpl<Record> implements RecordServices {

    public RecordServicesImpl(EntityManager entityManager) {
        super(Record.class, entityManager);
    }

	@Override
	public Record get(SolrDocument doc) {
		return super.get(new Long(doc.getFieldValue(IndexField.RECORD_ID_FIELD).toString()));
	}

	@Override
	public Record get(Long id, RecordCollection collection) {
		return super.get(id);
	}

    @SuppressWarnings("unchecked")
    @Override
    public final Record get(String url, ConnectorInstance connectorInstance) {
        Query query = this.getEntityManager().createQuery(
            "FROM Record r WHERE r.url = :url AND r.connectorInstance = :connectorInstance");
        query.setParameter("url", url);
        query.setParameter("connectorInstance", connectorInstance);
        List<Record> results = query.getResultList();
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Record get(String url, RecordCollection collection) {
        Query query = this.getEntityManager().createQuery(
            "FROM Record r WHERE r.url = :url AND r.connectorInstance.recordCollection = :recordCollection");
        query.setParameter("url", url);
        query.setParameter("recordCollection", collection);
        List<Record> results = query.getResultList();
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    @Override
    public int count(ConnectorInstance connectorInstance) {
        int attempts = 0;
        while (true) {
            try {
                String ql = "SELECT COUNT(r.id) FROM Record r WHERE r.connectorInstance = :connectorInstance";
                Query query = this.getEntityManager().createQuery(ql);
                query.setParameter("connectorInstance", connectorInstance);
                return ((Number) query.getSingleResult()).intValue();
            } catch (PessimisticLockException e) {
                attempts++;
                if (attempts > 100) {
                    throw e;
                }
            }
        }
    }

    @Override
    public int count(RecordCollection collection) {
        return count(collection, false, false);
    }

    @Override
    public int countMarkedForUpdateIndex(RecordCollection collection) {
        return count(collection, true, false);
    }

    @Override
    public int countMarkedForExclusionOrDeletion(RecordCollection collection) {
        return count(collection, false, true);
    }

    private int count(RecordCollection collection, Boolean updateIndex, Boolean deleted) {
        int attempts = 0;
        while (true) {
            try {
                String ql = "SELECT COUNT(r.id) FROM Record r WHERE r.connectorInstance.recordCollection = :recordCollection";
                if (Boolean.TRUE.equals(updateIndex)) {
                    ql += " AND updateIndex = :updateIndex AND (excluded = false OR excluded = null)";
                }
                if (Boolean.TRUE.equals(deleted)) {
                    ql += " AND deleted = :deleted";
                }
                Query query = this.getEntityManager().createQuery(ql);
                query.setParameter("recordCollection", collection);
                if (updateIndex != null && Boolean.TRUE.equals(updateIndex)) {
                    query.setParameter("updateIndex", updateIndex);
                }
                if (deleted != null && Boolean.TRUE.equals(deleted)) {
                    query.setParameter("deleted", deleted);
                }
                return ((Number) query.getSingleResult()).intValue();
            } catch (PessimisticLockException e) {
                attempts++;
                if (attempts > 100) {
                    throw e;
                }
            }
        }
    }

    @Override
    public List<Record> list(RecordCollection collection) {
        return list(collection, -1, false, false);
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<Record> list(ConnectorInstance connectorInstance) {
        String ql = "FROM Record r WHERE r.connectorInstance = :connectorInstance";
        ql += " ORDER BY r.id ASC";
        Query query = this.getEntityManager().createQuery(ql);
        query.setParameter("connectorInstance", connectorInstance);
        return (List<Record>) query.getResultList();
	}

    @Override
    public List<Record> listMarkedForUpdateIndex(RecordCollection collection, int maxResults) {
        List<Record> markedForUpdateIndex = list(collection, maxResults, true, false);
        return markedForUpdateIndex;
    }

    @Override
    public List<Record> listMarkedForExclusionOrDeletion(RecordCollection collection, int maxResults) {
        List<Record> markedForExclusionOrDeletion = list(collection, maxResults, false, true);
        return markedForExclusionOrDeletion;
    }

    @SuppressWarnings("unchecked")
    private List<Record> list(RecordCollection collection, int maxResults, Boolean updateIndex,
        Boolean excludedOrDeleted) {
        String ql = "FROM Record r WHERE r.connectorInstance.recordCollection = :recordCollection";
        if (Boolean.TRUE.equals(updateIndex)) {
            ql += " AND updateIndex = true AND ((deleted = false OR deleted = null) AND (excluded = false OR excluded = null))";
        }
        if (Boolean.TRUE.equals(excludedOrDeleted)) {
            ql += " AND (deleted = true OR (excluded = true AND excludedEffective = false))";
        }
        ql += " ORDER BY r.id ASC";
        Query query = this.getEntityManager().createQuery(ql);
        query.setParameter("recordCollection", collection);
        if (maxResults >= 0) {
            query.setMaxResults(maxResults);
        }
        return (List<Record>) query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Record> listExcluded(RecordCollection collection) {
        String ql = "FROM Record r WHERE excluded=true AND r.connectorInstance.recordCollection = :recordCollection";
        ql += " ORDER BY r.id DESC";
        Query query = this.getEntityManager().createQuery(ql);
        query.setParameter("recordCollection", collection);
        return (List<Record>) query.getResultList();
    }

    @Override
    public void markRecordsForUpdateIndex(RecordCollection collection) {
        // Bug in JTA prevents us from doing a single update with connectorInstance.recordCollection
        for (ConnectorInstance connectorInstance : collection.getConnectorInstances()) {
            String ql = "UPDATE Record SET updateIndex=true WHERE connectorInstance = :connectorInstance";
            Query query = this.getEntityManager().createQuery(ql);
            query.setParameter("connectorInstance", connectorInstance);
            query.executeUpdate();
        }
        IndexingManager indexingManager = IndexingManager.get(collection);
        if (!indexingManager.isActive()) {
            indexingManager.startIndexing(true);
        } else {
            indexingManager.reindexAll();
        }
    }

    @Override
    public void markRecordsForDeletion(RecordCollection collection) {
        // Bug in JTA prevents us from doing a single update with connectorInstance.recordCollection
        for (ConnectorInstance connectorInstance : collection.getConnectorInstances()) {
            String ql = "UPDATE Record SET deleted=true WHERE connectorInstance = :connectorInstance";
            Query query = this.getEntityManager().createQuery(ql);
            query.setParameter("connectorInstance", connectorInstance);
            query.executeUpdate();
        }
        IndexingManager indexingManager = IndexingManager.get(collection);
        if (!indexingManager.isActive()) {
            indexingManager.startIndexing(true);
        }
    }

    @Override
    public void markRecordForExclusion(Record record) {
        record.setExcluded(true);
        record.setExcludedEffective(false);
        makePersistent(record);
    }

    @Override
    public void markRecordAsExcluded(Record record) {
        record.setExcluded(true);
        record.setExcludedEffective(true);
        makePersistent(record);
    }

    @Override
    public void cancelExclusion(Record record) {
        record.setExcluded(false);
        record.setExcludedEffective(false);
        record.setUpdateIndex(true);
        makePersistent(record);
    }

    @Override
    public void deleteRecords(ConnectorInstance connectorInstance) {
        String sqlContentMeta = "DELETE FROM Record_ContentMetas WHERE record_id IN"
            + " (SELECT r.id FROM Record r, ConnectorInstance ci WHERE r.connectorInstance_id=ci.id AND ci.id=?)";
        Query contentMetaQuery = getEntityManager().createNativeQuery(sqlContentMeta);
        contentMetaQuery.setParameter(1, connectorInstance.getId());
        contentMetaQuery.executeUpdate();

        String sqlExternalMeta = "DELETE FROM Record_ExternalMetas WHERE record_id IN"
            + " (SELECT r.id FROM Record r, ConnectorInstance ci WHERE r.connectorInstance_id=ci.id AND ci.id=?)";
        Query externalMetaQuery = getEntityManager().createNativeQuery(sqlExternalMeta);
        externalMetaQuery.setParameter(1, connectorInstance.getId());
        externalMetaQuery.executeUpdate();

        String sqlMeta = "DELETE FROM RecordMeta WHERE record_id IN"
            + " (SELECT r.id FROM Record r, ConnectorInstance ci WHERE r.connectorInstance_id=ci.id AND ci.id=?)";
        Query metaQuery = getEntityManager().createNativeQuery(sqlMeta);
        metaQuery.setParameter(1, connectorInstance.getId());
        metaQuery.executeUpdate();

        String sqlTag = "DELETE FROM RecordTag WHERE record_id IN"
            + " (SELECT r.id FROM Record r, ConnectorInstance ci WHERE r.connectorInstance_id=ci.id AND ci.id=?)";
        Query tagQuery = getEntityManager().createNativeQuery(sqlTag);
        tagQuery.setParameter(1, connectorInstance.getId());
        tagQuery.executeUpdate();

        String sqlRawContent = "DELETE FROM RawContent WHERE record_id"
            + " IN (SELECT r.id FROM Record r, ConnectorInstance ci WHERE r.connectorInstance_id=ci.id AND ci.id=?)";
        Query rawConentQuery = getEntityManager().createNativeQuery(sqlRawContent);
        rawConentQuery.setParameter(1, connectorInstance.getId());
        rawConentQuery.executeUpdate();

        String sqlFederationIndexingRequired = "DELETE FROM FederationRecordIndexingRequired WHERE recordId"
            + " IN (SELECT r.id FROM Record r, ConnectorInstance ci WHERE r.connectorInstance_id=ci.id AND ci.id=?)";
        Query rawFederationIndexingRequired = getEntityManager().createNativeQuery(
            sqlFederationIndexingRequired);
        rawFederationIndexingRequired.setParameter(1, connectorInstance.getId());
        rawFederationIndexingRequired.executeUpdate();

        String sqlFederationDeletionRequired = "DELETE FROM FederationRecordDeletionRequired WHERE recordId"
            + " IN (SELECT r.id FROM Record r, ConnectorInstance ci WHERE r.connectorInstance_id=ci.id AND ci.id=?)";
        Query rawFederationDeletionRequired = getEntityManager().createNativeQuery(
            sqlFederationDeletionRequired);
        rawFederationDeletionRequired.setParameter(1, connectorInstance.getId());
        rawFederationDeletionRequired.executeUpdate();

        Query recordQuery = this.getEntityManager().createQuery(
            "DELETE FROM Record r WHERE r.connectorInstance = :connectorInstance");
        recordQuery.setParameter("connectorInstance", connectorInstance);
        recordQuery.executeUpdate();
    }

    @Override
    public void deleteRecords(RecordCollection collection) {
        for (ConnectorInstance connectorInstance : collection.getConnectorInstances()) {
            deleteRecords(connectorInstance);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Record> getPendingExclusions(RecordCollection collection) {
        String ql = "FROM Record r WHERE r.connectorInstance.recordCollection=:recordCollection AND"
            + " r.excluded=true AND r.excludedEffective=false";
        Query query = getEntityManager().createQuery(ql);
        query.setParameter("recordCollection", collection);
        return query.getResultList();
    }

    @Override
    public void deleteAutomaticRecordTags(RecordCollection collection, Date newStartTaggingDate) {
        int attempts = 0;
        while (true) {
            try {
                String sqlTag;
                if (newStartTaggingDate == null) {
                    sqlTag = "DELETE FROM RecordTag WHERE manual=? AND record_id IN"
                        + " (SELECT r.id FROM Record r, ConnectorInstance ci, RecordCollection rc"
                        + " WHERE r.connectorInstance_id=ci.id AND ci.recordCollection_id=rc.id AND rc.id=?)";
                } else {
                    sqlTag = "DELETE FROM RecordTag WHERE manual=? AND record_id IN"
                        + " (SELECT r.id FROM Record r, ConnectorInstance ci, RecordCollection rc "
                        + "WHERE r.connectorInstance_id=ci.id AND ci.recordCollection_id=rc.id AND rc.id=? AND"
                        + " (r.lastAutomaticTagging > ? OR r.lastAutomaticTagging IS NULL))";
                }
                Query tagQuery = getEntityManager().createNativeQuery(sqlTag);
                tagQuery.setParameter(1, Boolean.FALSE);
                tagQuery.setParameter(2, collection.getId());
                if (newStartTaggingDate != null) {
                    tagQuery.setParameter(3, newStartTaggingDate);
                }
                tagQuery.executeUpdate();

                String sqlRecord;
                if (newStartTaggingDate == null) {
                    sqlRecord = "UPDATE Record r SET r.lastAutomaticTagging = null WHERE connectorInstance_id IN"
                        + " (SELECT ci.id FROM ConnectorInstance ci, RecordCollection rc WHERE ci.recordCollection_id=rc.id AND rc.id=?)";
                } else {
                    sqlRecord = "UPDATE Record r SET r.lastAutomaticTagging = null WHERE connectorInstance_id IN"
                        + " (SELECT ci.id FROM ConnectorInstance ci, RecordCollection rc WHERE ci.recordCollection_id=rc.id AND"
                        + " rc.id=?) AND (r.lastAutomaticTagging > ? OR r.lastAutomaticTagging IS NULL)";
                }
                Query recordQuery = getEntityManager().createNativeQuery(sqlRecord);
                recordQuery.setParameter(1, collection.getId());
                if (newStartTaggingDate != null) {
                    recordQuery.setParameter(2, newStartTaggingDate);
                }
                recordQuery.executeUpdate();
                break;
            } catch (PessimisticLockException e) {
                attempts++;
                if (attempts > 100) {
                    throw e;
                }
            }
        }
    }

    @Override
    public void markRecordsForComputeACLEntries(RecordCollection collection) {
        // Bug in JTA prevents us from doing a single update with connectorInstance.recordCollection
        for (ConnectorInstance connectorInstance : collection.getConnectorInstances()) {
            String ql = "UPDATE Record SET computeACLEntries=true WHERE connectorInstance = :connectorInstance";
            Query query = this.getEntityManager().createQuery(ql);
            query.setParameter("connectorInstance", connectorInstance);
            query.executeUpdate();
        }
        markRecordsForUpdateIndex(collection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ConnectorInstance> getConnectorInstances(List<Record> records) {
        StringBuffer sql = new StringBuffer("SELECT DISTINCT r.connectorInstance_id FROM Record r");
        sql.append(" where r.id IN (");
        boolean first = true;
        for (Record record : records) {
            if (!first) {
                sql.append(", ");
            }
            sql.append(record.getId());
            first = false;
        }
        sql.append(")");
        Query query = getEntityManager().createNativeQuery(sql.toString());
        List<Number> connectorIds = query.getResultList();
        ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils
            .getConnectorInstanceServices();
        return connectorInstanceServices.list(connectorIds);
    }

    @Override
    public Float computeBoost(Record record) {
        // 1. Calcul du boost du a l appartenance a la collection
        RecordCollection collection = record.getConnectorInstance().getRecordCollection();
        RecordCollectionServices recordCollectionServices = ConstellioSpringUtils
            .getRecordCollectionServices();
        Float boostAssociatedWithCollection = recordCollectionServices.getBoost(collection, record)
            .floatValue();
        // faire le produit des boosts des sous collections
        FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
        if (collection.isFederationOwner()) {
            List<RecordCollection> includedCollections = federationServices
                .listIncludedCollections(collection);
            for (RecordCollection includedCollection : includedCollections) {
                boostAssociatedWithCollection *= recordCollectionServices
                    .getBoost(includedCollection, record).floatValue();
            }
        }

        // faire le produite des boosts des collections mères!!!
        if (collection.isIncludedInFederation()) {
            List<RecordCollection> ownerCollections = federationServices.listOwnerCollections(collection);
            for (RecordCollection includedCollection : ownerCollections) {
                boostAssociatedWithCollection *= recordCollectionServices
                    .getBoost(includedCollection, record).floatValue();
            }
        }

        // 2. Boost fourni par le connecteur
        // FIXME quoi faire pour une collection fédérée?
        List<RecordMeta> boosts = record.getMetas(IndexFieldServices.BOOST_FIELD_PREFIX);
        Float boostAssociatedWithConnector = 1.0f;
        if (boosts != null) {
            for (RecordMeta boost : boosts) {
                boostAssociatedWithConnector *= Float.valueOf(boost.getContent());
            }
        }
        return boostAssociatedWithCollection * boostAssociatedWithConnector;
    }

    @Override
    public Float computeFieldBoost(Record record, IndexField indexField) {
        Float boostAssociatedWithIndexField = 1.0f;
        String indexFieldName = indexField.getName();

        Set<String> indexFieldAssociatedMetaNames = new HashSet<String>();
        if (indexFieldName.equals(IndexField.UNIQUE_KEY_FIELD)) {
            indexFieldAssociatedMetaNames.add(SpiConstants.PROPNAME_DOCID);
        } else {
            if (indexFieldName.equals(IndexField.DEFAULT_SEARCH_FIELD)) {
                indexFieldAssociatedMetaNames.add(SpiConstants.PROPNAME_CONTENT);
            } else {
                indexFieldAssociatedMetaNames = indexField.getMetaNames();
            }

        }

        for (String metaName : indexFieldAssociatedMetaNames) {
            List<RecordMeta> boosts = record.getMetas(IndexFieldServices.BOOST_FIELD_PREFIX + metaName);

            if (boosts != null) {
                for (RecordMeta boost : boosts) {
                    boostAssociatedWithIndexField *= Float.valueOf(boost.getContent());
                }
            }
        }

        // Ajout du boost des fields ajoutés via l'interface de gestion de la pertinence des champs
        Float boostFieldAddedThrowInterface = indexField.getBoost();
        if (boostFieldAddedThrowInterface == null) {
            boostFieldAddedThrowInterface = new Float(0F);
        }

        return boostAssociatedWithIndexField * boostFieldAddedThrowInterface;
    }

    @SuppressWarnings("unchecked")
	@Override
	public List<Record> listIndexedRecordsSince(RecordCollection collection, Date startDate) {
        String ql = "FROM Record r WHERE r.connectorInstance.recordCollection = :recordCollection AND" +
                " r.lastIndexed IS NOT NULL AND r.lastIndexed >= :startDate";
        Query query = this.getEntityManager().createQuery(ql);
        query.setParameter("recordCollection", collection);
        query.setParameter("startDate", startDate);
        return (List<Record>) query.getResultList();
	}

    @SuppressWarnings("unchecked")
	@Override
	public List<Record> listTraversedRecordsSince(ConnectorInstance connectorInstance, Date startDate) {
		String ql = "FROM Record r WHERE r.connectorInstance = :connectorInstance AND r.lastFetched >= :startDate";
        Query query = this.getEntityManager().createQuery(ql);
        query.setParameter("connectorInstance", connectorInstance);
        query.setParameter("startDate", startDate);
        return (List<Record>) query.getResultList();
	}

    @SuppressWarnings("unchecked")
	@Override
	public List<Record> listLastTraversedRecords(ConnectorInstance connectorInstance, int maxSize) {
        String ql = "FROM Record r WHERE r.connectorInstance = :connectorInstance ORDER BY lastFetched DESC";
        Query query = this.getEntityManager().createQuery(ql);
        query.setParameter("connectorInstance", connectorInstance);
        query.setMaxResults(maxSize);
        List<Record> results = (List<Record>) query.getResultList();
        return results;
	}

	@Override
	public List<Record> list(Collection<Number> ids, RecordCollection collection) {
		return super.list(ids);
	}

	@Override
	public void makePersistent(List<Record> records, ConnectorInstance connectorInstance) {
		for (Record record : records) {
			makePersistent(record);
		}
	}

	@Override
	public ReadWriteLock getLock(String collectionName) {
		throw new NotImplementedException();
	}

//	@Override
//	public void makePersistent(List<Record> records, RecordCollection collection) {
//		for (Record record : records) {
//			makePersistent(record);
//		}
//	}

    /*
     * @Override
     * public void updateRecordBoost(RecordCollection collection) {
     * List<Record> collectionRecords = list(collection, -1, null, null);
     * for (Record record : collectionRecords) {
     * boolean updated = updateRecordBoost(record);
     * if (updated) {
     * record.setUpdateIndex(true);
     * makePersistent(record);
     * }
     * }
     * }
     * @Override
     * public boolean updateRecordBoost(Record record) {
     * RecordCollection collection = record.getConnectorInstance().getRecordCollection();
     * Double boostBefore = record.getBoost();
     * Double boostAfter = ConstellioSpringUtils.getRecordCollectionServices().getBoost(collection, record);
     * if (boostAfter == null) {
     * if (boostBefore != null) {
     * record.setBoost(boostAfter);
     * merge(record);
     * return true;
     * } else {
     * return false;
     * }
     * } else {
     * if (boostBefore == null || !boostAfter.equals(boostBefore)) {
     * record.setBoost(boostAfter);
     * merge(record);
     * return true;
     * } else {
     * return false;
     * }
     * }
     * }
     * @Override
     * public Record makePersistent(Record record) {
     * RecordCollection collection = record.getConnectorInstance().getRecordCollection();
     * Double newBoost = ConstellioSpringUtils.getRecordCollectionServices().getBoost(collection, record);
     * record.setBoost(newBoost);
     * return super.makePersistent(record);
     * }
     */

}
