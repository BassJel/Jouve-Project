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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.solr.handler.ConstellioSolrQueryParams;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.FileSizeUtils;

public class StatusServicesImpl extends BaseServicesImpl implements StatusServices {

    private static final Logger LOGGER = Logger.getLogger(StatusServicesImpl.class.getName());

    public StatusServicesImpl(EntityManager entityManager) {
        super(entityManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int countIndexedRecords(RecordCollection collection) {
        int count;
        SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
        SolrServer solrServer = solrServices.getSolrServer(collection);
        if (solrServer != null) {
            SolrQuery query = new SolrQuery();
            query.setQueryType("/admin/luke");
            query.setParam("show", "schema");
            try {
                QueryResponse queryResponse = solrServer.query(query);
                NamedList<Object> index = (NamedList<Object>) queryResponse.getResponse().get("index");
                if (index != null) {
                    Integer numDocs = (Integer) index.get("numDocs");
                    count = numDocs != null ? numDocs : 0;
                } else {
                    count = 0;
                }
            } catch (SolrServerException e) {
                throw new RuntimeException(e);
            }
        } else {
            count = 0;
        }
        return count;
    }

    @Override
    public int countTraversedRecords(ConnectorInstance connectorInstance) {
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        return recordServices.count(connectorInstance);
    }

    @Override
    public int countTraversedRecords(RecordCollection collection) {
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        return recordServices.count(collection);
    }

    @Override
    public List<Record> listIndexedRecordsSince(RecordCollection collection, Date startDate) {
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        return recordServices.listIndexedRecordsSince(collection, startDate);
    }

    @Override
    public String getSizeOnDisk(RecordCollection collection) {
        String formattedSize;
        if (collection != null) {
            File solrCoreIndexDir = SolrCoreContext.getSolrCoreIndexDir(collection.getName());
            try {
                formattedSize = FileSizeUtils.formatSize(solrCoreIndexDir, 2);
			} catch (Exception e) {
	            formattedSize = FileSizeUtils.formatSize(0, 2);			
	        }
        } else {
            formattedSize = FileSizeUtils.formatSize(0, 2);
        }
        return formattedSize;
    }

    @Override
    public List<Record> listTraversedRecordsSince(ConnectorInstance connectorInstance, Date startDate) {
    	RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
    	return recordServices.listTraversedRecordsSince(connectorInstance, startDate);
    }

    @Override
    public List<Record> listLastIndexedRecords(RecordCollection collection, int maxSize) {
        List<Record> lastIndexedRecords;
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        
        String luceneQuery = SimpleSearch.SEARCH_ALL;
        SolrQuery solrQuery = new SolrQuery(luceneQuery);
        solrQuery.setQueryType("standard");
        solrQuery.setRows(maxSize);
        solrQuery.setSortField(IndexField.LAST_INDEXED_FIELD, ORDER.desc);
        
        String collectionName = collection.getName();
        solrQuery.setParam(ConstellioSolrQueryParams.LUCENE_QUERY, luceneQuery);
        solrQuery.setParam(ConstellioSolrQueryParams.COLLECTION_NAME, collectionName);
        
        SolrServer server = SolrCoreContext.getSolrServer(collectionName);
        if (server != null && collection.getIndexField(IndexField.LAST_INDEXED_FIELD) != null) {
            try {
                QueryResponse queryResponse = server.query(solrQuery);
                SolrDocumentList results = queryResponse.getResults();
                List<Number> recordIds = new ArrayList<Number>();
                for (SolrDocument result : results) {
                    Long recordId = new Long(result.getFieldValue(IndexField.RECORD_ID_FIELD).toString());
                    recordIds.add(recordId);
                }
                if (!recordIds.isEmpty()) {
                    lastIndexedRecords = recordServices.list(recordIds, collection);
                } else {
                    lastIndexedRecords = new ArrayList<Record>();
                }
            } catch (SolrServerException e) {
                throw new RuntimeException(e);
            }
        } else if (!collection.isSynchronizationRequired()) {
            String msg = "No SolrServer available for collection id " + collection.getId();
            LOGGER.log(Level.SEVERE, msg);
            lastIndexedRecords = new ArrayList<Record>();
        } else if (collection.getIndexField(IndexField.LAST_INDEXED_FIELD) == null) {
            String msg = "No " + IndexField.LAST_INDEXED_FIELD + " index field for collection id " + collection.getId();
            LOGGER.log(Level.SEVERE, msg);
            lastIndexedRecords = new ArrayList<Record>();
        } else {
            lastIndexedRecords = new ArrayList<Record>();
        }
        return lastIndexedRecords;
    }

    @Override
    public List<Record> listLastTraversedRecords(ConnectorInstance connectorInstance, int maxSize) {
    	RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
    	return recordServices.listLastTraversedRecords(connectorInstance, maxSize);
    }

    @Override
    public Date getLastTraversalDate(ConnectorInstance connectorInstance) {
        List<Record> lastTraversedRecord = listLastTraversedRecords(connectorInstance, 1);
        return !lastTraversedRecord.isEmpty() ? lastTraversedRecord.get(0).getLastFetched() : null;
    }

}
