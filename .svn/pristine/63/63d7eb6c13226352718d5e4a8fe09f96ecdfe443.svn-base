package com.doculibre.constellio.wicket.models;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.commons.lang.SerializationUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.services.RecordServicesSolrImpl;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.SolrDocumentUtils;

@SuppressWarnings("serial")
public class RecordModel extends LoadableDetachableModel {
	
	private Long id;
	private Long collectionId;
	private Record record;
	private byte[] serializedRecord;
	private SolrInputDocument solrInputDoc;
	
	public RecordModel(Record record) {
		this.record = record;
	}

	@Override
	protected final Object load() {
		Record result;
		if (serializedRecord != null) {
		    record = (Record) SerializationUtils.deserialize(serializedRecord);
			serializedRecord = null;
			result = record;
		}
		if (solrInputDoc != null) {
			RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        	RecordServicesSolrImpl recordServicesSolrImpl = (RecordServicesSolrImpl) recordServices;
        	SolrDocument solrDoc = SolrDocumentUtils.toSolrDocument(solrInputDoc);
        	result = record = recordServicesSolrImpl.populateRecord(solrDoc);
			solrInputDoc = null;
		} else if (record != null) {
		    result = record;
		} else {
			RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
			RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
			RecordCollection collection = collectionServices.get(collectionId);
			result = record = recordServices.get(id, collection);
		}
		return result;
	}

	@Override
	public void detach() {
		prepareForSerialization();
		super.detach();
	}

    private void writeObject(ObjectOutputStream oos) throws IOException {
        prepareForSerialization();
        oos.defaultWriteObject();
    }
	
    private void prepareForSerialization() {
        if (record != null) {
            if (record.getId() != null) {
                RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
                if (recordServices instanceof RecordServicesSolrImpl) {
                	RecordCollection collection = record.getConnectorInstance().getRecordCollection();
                	RecordServicesSolrImpl recordServicesSolrImpl = (RecordServicesSolrImpl) recordServices;
                	IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
    				
                	solrInputDoc = new SolrInputDocument();
    				recordServicesSolrImpl.populateSolrDoc(record, solrInputDoc);
    				indexFieldServices.populateSolrDoc(solrInputDoc, record, collection);
    				
                    record = null;
                } else {
                    id = record.getId();
                    collectionId = record.getConnectorInstance().getRecordCollection().getId();
                    record = null;
                    serializedRecord = null;
                }
            } else {
                serializedRecord = SerializationUtils.serialize(record);
                record = null;
            }
        }
	}

	@Override
	public Record getObject() {
		return (Record) super.getObject();
	}

}
