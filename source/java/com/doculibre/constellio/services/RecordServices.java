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
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.search.DocSet;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;

public interface RecordServices extends BaseCRUDServices<Record> {
	
	Record get(SolrDocument doc);

    Record get(Long id, RecordCollection collection);
	
	Record get(String url, ConnectorInstance connectorInstance);

    Record get(String url, RecordCollection collection);

    int count(ConnectorInstance connectorInstance);
    
    int count(RecordCollection collection);
	
	int countMarkedForUpdateIndex(RecordCollection collection);
	
	int countMarkedForExclusionOrDeletion(RecordCollection collection);
	
    List<Record> list(RecordCollection collection);
    
    List<Record> list(ConnectorInstance connectorInstance);

	List<Record> list(Collection<Number> ids, RecordCollection collection);
	
	List<Record> listMarkedForUpdateIndex(RecordCollection collection, int maxResults);
    
    List<Record> listMarkedForExclusionOrDeletion(RecordCollection collection, int maxResults);
	
    List<Record> listExcluded(RecordCollection collection);
    
	void markRecordsForUpdateIndex(RecordCollection collection);
	
	void markRecordsForDeletion(RecordCollection collection);
    
    void markRecordForExclusion(Record record);

    void markRecordAsExcluded(Record record);
    
    void cancelExclusion(Record record);
    
    void deleteRecords(ConnectorInstance connectorInstance);
	
	void deleteRecords(RecordCollection collection);
    
    List<Record> getPendingExclusions(RecordCollection collection);
    
    //supprimer tous les tags automatiques des records modifiés après la date {@param newStartTaggingDate}
    //Si la date donnée est "null" tous les tags automatiques seront supprimés
    void deleteAutomaticRecordTags(RecordCollection collection, Date newStartTaggingDate);
    
    void markRecordsForComputeACLEntries(RecordCollection collection);
    
    List<ConnectorInstance> getConnectorInstances(List<Record> records);
    
    Float computeBoost(Record record);
    
    Float computeFieldBoost(Record record, IndexField indexField);

	List<Record> listTraversedRecordsSince(ConnectorInstance connectorInstance, Date startDate);

	List<Record> listIndexedRecordsSince(RecordCollection collection, Date startDate);
	
	List<Record> listLastTraversedRecords(ConnectorInstance connectorInstance, int maxSize);
    
	/*boolean updateRecordBoost(Record record);

	void updateRecordBoost(RecordCollection collection);*/
	
	void makePersistent(List<Record> records, ConnectorInstance connectorInstance);

	ReadWriteLock getLock(String collectionName);

	//void makePersistent(List<Record> records, RecordCollection collection);
    
}
