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
package com.doculibre.constellio.status;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.StatusServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class StatusManager {

    private static final int MAX_SIZE = 10;

    private static Map<Long, List<RecordStatus>> collectionIndexedRecordStatusMap = new HashMap<Long, List<RecordStatus>>();
    private static Map<Long, List<RecordStatus>> connectorTraversedRecordStatusMap = new HashMap<Long, List<RecordStatus>>();
    private static Map<Long, Integer> connectorTraversedRecordCounts = new HashMap<Long, Integer>();

    private StatusManager() {
    }

    public static synchronized void init() {
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        StatusServices statusServices = ConstellioSpringUtils.getStatusServices();

        List<RecordCollection> collections = collectionServices.list();
        for (RecordCollection collection : collections) {
            if (!collection.isOpenSearch()) {
                List<RecordStatus> collectionIndexedRecordStatuses = getCollectionIndexedRecordStatuses(collection);
                // Should always be the case...
                if (collectionIndexedRecordStatuses.isEmpty()) {
                    List<Record> lastIndexedRecords = statusServices.listLastIndexedRecords(collection, MAX_SIZE);
                    for (Record indexedRecord : lastIndexedRecords) {
                        if (indexedRecord.getLastIndexed() != null) {
                            collectionIndexedRecordStatuses.add(new RecordStatus(indexedRecord, indexedRecord
                                .getLastIndexed()));
                        }
                    }
                }
                for (ConnectorInstance connectorInstance : collection.getConnectorInstances()) {
                    List<RecordStatus> connectorTraversedRecordStatuses = getConnectorTraversedRecordStatuses(connectorInstance);
                    // If it is not empty, then the FeedProcessor has already begun traversal for this
                    // connector
                    if (connectorTraversedRecordStatuses.isEmpty()) {
                        List<Record> lastTraversedRecords = statusServices.listLastTraversedRecords(
                            connectorInstance, MAX_SIZE);
                        for (Record traversedRecord : lastTraversedRecords) {
                            connectorTraversedRecordStatuses.add(new RecordStatus(traversedRecord,
                                traversedRecord.getLastFetched()));
                        }
                    }
                    adjustTraversedRecordCount(connectorInstance, 0);
                }
            }
        }
    }

    private static List<RecordStatus> getCollectionIndexedRecordStatuses(
        RecordCollection collection) {
        Long collectionId = collection.getId();
        List<RecordStatus> collectionIndexedRecordStatuses = collectionIndexedRecordStatusMap.get(collectionId);
        if (collectionIndexedRecordStatuses == null) {
            collectionIndexedRecordStatuses = new ArrayList<RecordStatus>();
            collectionIndexedRecordStatusMap.put(collectionId, collectionIndexedRecordStatuses);
        }
        return collectionIndexedRecordStatuses;
    }

    private static List<RecordStatus> getConnectorTraversedRecordStatuses(
        ConnectorInstance connectorInstance) {
        Long connectorInstanceId = connectorInstance.getId();
        List<RecordStatus> connectorTraversedRecordStatuses = connectorTraversedRecordStatusMap.get(connectorInstanceId);
        if (connectorTraversedRecordStatuses == null) {
            connectorTraversedRecordStatuses = new ArrayList<RecordStatus>();
            connectorTraversedRecordStatusMap.put(connectorInstanceId, connectorTraversedRecordStatuses);
        }
        return connectorTraversedRecordStatuses;
    }
    
    private static int getTraversedRecordCount(ConnectorInstance connectorInstance) {
        Integer traversedRecordCount = connectorTraversedRecordCounts.get(connectorInstance.getId());
        if (traversedRecordCount == null) {
            StatusServices statusServices = ConstellioSpringUtils.getStatusServices();
            traversedRecordCount = statusServices.countTraversedRecords(connectorInstance);
        }    
        connectorTraversedRecordCounts.put(connectorInstance.getId(), traversedRecordCount);
        return traversedRecordCount;
    }
    
    private static void adjustTraversedRecordCount(ConnectorInstance connectorInstance, int adjustment) {
        Integer traversedRecordCount = getTraversedRecordCount(connectorInstance);
        traversedRecordCount += adjustment;
        connectorTraversedRecordCounts.put(connectorInstance.getId(), traversedRecordCount);
    }

    public static void removing(RecordCollection collection) {
        for (ConnectorInstance connectorInstance : collection.getConnectorInstances()) {
            connectorTraversedRecordCounts.remove(connectorInstance.getId());
        }
        collectionIndexedRecordStatusMap.remove(collection.getId());
    }

    public static void removing(ConnectorInstance connectorInstance) {
        connectorTraversedRecordCounts.remove(connectorInstance.getId());
        connectorTraversedRecordStatusMap.remove(connectorInstance.getId());
    }

    public static int countTraversedRecords(RecordCollection collection) {
//        int count;
//        if (collection.isFederationOwner()) {
//            FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
//            count = federationServices.countTraversedRecords(collection);
//        } else {
//            StatusServices statsServices = ConstellioSpringUtils.getStatusServices();
//            count = statsServices.countTraversedRecords(collection);
//        }
//        return count;
        int count = 0;
        for (ConnectorInstance connectorInstance : collection.getConnectorInstances()) {
            count += getTraversedRecordCount(connectorInstance);
        }
        if (collection.isFederationOwner()) {
            FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
            List<RecordCollection> includedCollections = federationServices.listIncludedCollections(collection);
            for (RecordCollection includedCollection : includedCollections) {
                for (ConnectorInstance connectorInstance : includedCollection.getConnectorInstances()) {
                    count += getTraversedRecordCount(connectorInstance);
                }
            }
        }
        return count;
    }

    public static int countIndexedRecords(RecordCollection collection) {
        StatusServices statusServices = ConstellioSpringUtils.getStatusServices();
        return statusServices.countIndexedRecords(collection);
    }

    public static List<String> listLastIndexedRecords(RecordCollection collection) {
        List<String> lastIndexedRecords = new ArrayList<String>();
        try {
	        List<RecordStatus> collectionIndexedRecordStatuses = getCollectionIndexedRecordStatuses(collection);
	        for (RecordStatus recordStatus : collectionIndexedRecordStatuses) {
	            lastIndexedRecords.add(recordStatus.logInfo);
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return lastIndexedRecords;
    }

    public static List<String> listLastTraversedRecords(ConnectorInstance connectorInstance) {
        List<String> lastIndexedRecords = new ArrayList<String>();
        List<RecordStatus> connectorTraversedRecordStatuses = getConnectorTraversedRecordStatuses(connectorInstance);
        for (RecordStatus recordStatus : connectorTraversedRecordStatuses) {
            lastIndexedRecords.add(recordStatus.logInfo);
        }
        return lastIndexedRecords;
    }

    public static Date getLastTraversalDate(ConnectorInstance connectorInstance) {
        Date lastTraversalDate;
        List<RecordStatus> connectorTraversedRecordStatuses = getConnectorTraversedRecordStatuses(connectorInstance);
        if (!connectorTraversedRecordStatuses.isEmpty()) {
            lastTraversalDate = connectorTraversedRecordStatuses.get(0).logDate;
        } else {
            lastTraversalDate = null;
        }
        return lastTraversalDate;
    }

    public static String getSizeOnDisk(RecordCollection collection) {
        StatusServices statusServices = ConstellioSpringUtils.getStatusServices();
        return statusServices.getSizeOnDisk(collection);
    }

    public static void traversing(Record record, boolean newRecord) {
        RecordStatus recordStatus = new RecordStatus(record, record.getLastFetched());
        List<RecordStatus> connectorTraversedRecordStatuses = getConnectorTraversedRecordStatuses(record
            .getConnectorInstance());
        connectorTraversedRecordStatuses.remove(recordStatus);
        connectorTraversedRecordStatuses.add(0, recordStatus);
        sortAndTrim(connectorTraversedRecordStatuses);
        if (newRecord) {
            adjustTraversedRecordCount(record.getConnectorInstance(), +1);
        }
    }
    
    public static void deleting(Record record) {
        adjustTraversedRecordCount(record.getConnectorInstance(), -1);
    }
    
    public static void excluding(Record record) {
    }
    
    public static void deletingAll(RecordCollection collection) {
        for (ConnectorInstance connectorInstance : collection.getConnectorInstances()) {
            connectorTraversedRecordCounts.put(connectorInstance.getId(), 0);
        }
    }

    public static void indexing(Record record, Date lastIndexed, RecordCollection collection) {
        RecordStatus recordStatus = new RecordStatus(record, lastIndexed);
        List<RecordStatus> collectionIndexedRecordStatuses = getCollectionIndexedRecordStatuses(collection);
        collectionIndexedRecordStatuses.remove(recordStatus);
        collectionIndexedRecordStatuses.add(0, recordStatus);
        sortAndTrim(collectionIndexedRecordStatuses);
    }
    
    public static void reindexingAll(RecordCollection collection) {
        List<RecordStatus> collectionIndexedRecordStatuses = getCollectionIndexedRecordStatuses(collection);
        collectionIndexedRecordStatuses.clear();
    }

    private static void sortAndTrim(List<RecordStatus> recordStatuses) {
        Collections.sort(recordStatuses, new Comparator<RecordStatus>() {
            @Override
            public int compare(RecordStatus o1, RecordStatus o2) {
                // Sort descending
                return o2.logDate.compareTo(o1.logDate);
            }
        });
        if (recordStatuses.size() > MAX_SIZE) {
            List<RecordStatus> extraRecordStatuses = recordStatuses.subList(MAX_SIZE, recordStatuses.size());
            recordStatuses.removeAll(extraRecordStatuses);
        }
    }

    private static class RecordStatus {

        private Long recordId;

        private String logInfo;

        private Date logDate;

        public RecordStatus(Record record, Date logDate) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            StringBuffer line = new StringBuffer();
            line.append(sdf.format(logDate));
            line.append("\n");
            String displayUrl = record.getDisplayUrl();
            line.append(displayUrl);
            this.recordId = record.getId();
            this.logInfo = line.toString();
            this.logDate = logDate;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((recordId == null) ? 0 : recordId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RecordStatus other = (RecordStatus) obj;
            if (recordId == null) {
                if (other.recordId != null)
                    return false;
            } else if (!recordId.equals(other.recordId))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return logInfo;
        }

    }

}
