package com.doculibre.constellio.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.services.RecordServicesSolrImpl;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

@SuppressWarnings("serial")
public class LazyLoadSolrRecordList extends LazyLoadSolrDocumentList<Record> {

	public LazyLoadSolrRecordList(ModifiableSolrParams solrParams, SolrServer solrServer) {
		super(solrParams, solrServer);
	}

	@Override
	protected Record convert(SolrDocument solrDoc) {
		RecordServicesSolrImpl recordServices = (RecordServicesSolrImpl) ConstellioSpringUtils.getRecordServices();
		return recordServices.populateRecord(solrDoc);
	}

}
