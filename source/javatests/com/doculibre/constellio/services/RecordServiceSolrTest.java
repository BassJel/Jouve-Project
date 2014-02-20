package com.doculibre.constellio.services;

import java.net.MalformedURLException;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import com.doculibre.constellio.entities.ParsedContent;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;

public class RecordServiceSolrTest extends TestCase {
	private SolrServer solrServer;
	private Record record;

	public void setUp() {
		 String url = "http://localhost:8983/solr";
		 solrServer = new HttpSolrServer ( url );
		System.out.println("ok");
		record = buildRecord();
	}
	
	public void testmakePersistent(){
		
		RecordServicesSolrImpl rssi = new RecordServicesSolrImpl();
		rssi.makePersistent(record, solrServer);
		assertEquals("non", "non");
	}
	
	public void testGetRecord(){
		
		
		RecordServicesSolrImpl rssi = new RecordServicesSolrImpl();
		Record record = rssi.get(123L, (RecordCollection) null, solrServer);
		System.out.println(record.getUrl());
		assertEquals("http://www.constellio.com", record.getUrl());
	}
	
	
	public Record buildRecord(){
		
		
		
		Record record = new Record();
		record.setId(123L);
		//record.setConnectorInstance(connectorInstance);
		record.setAuthmethod("setMethod");
		record.setBoost(1.0);
		record.setComputeACLEntries(true);
		record.setDeleted(false);
		record.setExcluded(true);
		
		record.setExcludedEffective(true);
		record.setLang("Fr");
		record.setLastAutomaticTagging(new Date());
		record.setLastFetched(new Date());
		record.setLastIndexed(new Date());
		record.setLastModified(new Date());
		record.setMimetype("Text/html");
		ParsedContent content = new ParsedContent();
		content.setContent("la vie est belle");
		content.setId(10L);
		content.setRecord(record);
		record.setParsedContent(content);
		record.setPublicRecord(false);
		record.setUpdateIndex(true);
		record.setUrl("http://www.constellio.com");
		return record;
	}
	

}
