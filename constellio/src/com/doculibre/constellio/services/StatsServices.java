package com.doculibre.constellio.services;

import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;

public interface StatsServices {
	
	void init();
	
	void shutdown();
	
	void optimize();
	
	void recompile();
	
	boolean isIgnored(SimpleSearch simpleSearch);
	
	String logSearch(SimpleSearch simpleSearch, QueryResponse queryResponse, String ipAddress);
	
	String logSearch(SimpleSearch simpleSearch, long numFound, long responseTime, String ipAddress);
	
	String logClick(RecordCollection collection, Record record, String searchLogDocId, String ipAddress);
	
	int getNbClicks(RecordCollection collection, String searchLogDocId);
	
	SolrDocumentList getQueries(RecordCollection collection, Date startDate, Date endDate, boolean includeFederatedCollections, int maxRows);
	
	SolrDocument getMostClickedDocument(SimpleSearch simpleSearch, Date startDate, Date endDate, boolean includeFederatedCollections);
	
	SolrDocumentList getMostClickedDocuments(SimpleSearch simpleSearch, Date startDate, Date endDate, boolean includeFederatedCollections, int minClicks, int maxRows);

	SolrDocumentList getMostPopularQueries(RecordCollection collection, Date startDate, Date endDate, boolean includeFederatedCollections, int maxRows);
	
	SolrDocumentList getMostPopularQueriesWithResults(RecordCollection collection, Date startDate, Date endDate, boolean includeFederatedCollections, int maxRows);
	
	SolrDocumentList getMostPopularQueriesWithoutResults(RecordCollection collection, Date startDate, Date endDate, boolean includeFederatedCollections, int maxRows);
	
	SolrDocumentList getMostPopularQueriesWithClick(RecordCollection collection, Date startDate, Date endDate, boolean includeFederatedCollections, int maxRows);
	
	SolrDocumentList getMostPopularQueriesWithoutClick(RecordCollection collection, Date startDate, Date endDate, boolean includeFederatedCollections, int maxRows);
	
	List<String> getMostPopularQueriesAutocomplete(String text, RecordCollection collection, int maxResults);

}
