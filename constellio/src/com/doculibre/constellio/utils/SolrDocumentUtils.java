package com.doculibre.constellio.utils;

import java.util.Collection;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

public class SolrDocumentUtils {
	
	public static SolrInputDocument toSolrInputDocument(SolrDocument solrDoc) {
		SolrInputDocument solrInputDoc = new SolrInputDocument();
		for (String fieldName : solrDoc.getFieldNames()) {
			Collection<Object> fieldValues = solrDoc.getFieldValues(fieldName);
			for (Object fieldValue : fieldValues) {
				solrInputDoc.addField(fieldName, fieldValue);
			}
		}
		return solrInputDoc;
	}
	
	public static SolrDocument toSolrDocument(SolrInputDocument solrInputDoc) {
		SolrDocument solrDoc = new SolrDocument();
		for (String fieldName : solrInputDoc.getFieldNames()) {
			Collection<Object> fieldValues = solrInputDoc.getFieldValues(fieldName);
			for (Object fieldValue : fieldValues) {
				solrDoc.addField(fieldName, fieldValue);
			}
		}
		return solrDoc;
	}
	
	public static SolrDocument toSolrDocument(Document luceneDoc) {
		SolrDocument solrDoc = new SolrDocument();
		for (Fieldable field : luceneDoc.getFields()) {
			String fieldName = field.name();
			String[] fieldValues = luceneDoc.getValues(fieldName);
			for (String fieldValue : fieldValues) {
				solrDoc.addField(fieldName, fieldValue);
			}
		}
		return solrDoc;
	}
	
	public static SolrInputDocument toSolrInputDocument(Document luceneDoc) {
		SolrInputDocument solrInputDoc = new SolrInputDocument();
		for (Fieldable field : luceneDoc.getFields()) {
			String fieldName = field.name();
			String[] fieldValues = luceneDoc.getValues(fieldName);
			for (String fieldValue : fieldValues) {
				solrInputDoc.addField(fieldName, fieldValue);
			}
		}
		return solrInputDoc;
	}

}
