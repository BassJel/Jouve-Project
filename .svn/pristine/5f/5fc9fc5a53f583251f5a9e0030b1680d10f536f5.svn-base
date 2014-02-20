package com.doculibre.constellio.solr.handler.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.IndexSchema.DynamicField;
import org.apache.solr.search.ReturnFields;

public class RestIndexSchema {

	private Map<String, Boolean> fields;
	private Map<String, Boolean> dynamicFields;

	public RestIndexSchema(SolrServer solrServer) {
		fields = new HashMap<String, Boolean>();
		dynamicFields = new HashMap<String, Boolean>();

		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setParam(CommonParams.QT, "/schema/fields");
		solrQuery.setParam("showDefaults", true);
		try {
			QueryResponse queryResponse = solrServer.query(solrQuery);
			NamedList<Object> response = queryResponse.getResponse();
			List<SimpleOrderedMap<Object>> fieldInfos = (List<SimpleOrderedMap<Object>>) response.get(IndexSchema.FIELDS);
			for (SimpleOrderedMap<Object> fieldInfo : fieldInfos) {
				fields.put(fieldInfo.get(IndexSchema.NAME).toString(), Boolean.getBoolean(fieldInfo.get("multiValued").toString()));
			}

		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		solrQuery.setParam(CommonParams.QT, "/schema/dynamicfields");
		solrQuery.setParam("showDefaults", true);
		try {
			QueryResponse queryResponse = solrServer.query(solrQuery);
			NamedList<Object> response = queryResponse.getResponse();
			List<SimpleOrderedMap<Object>> fieldInfos = (List<SimpleOrderedMap<Object>>) response.get(IndexSchema.DYNAMIC_FIELDS);
			for (SimpleOrderedMap<Object> fieldInfo : fieldInfos) {
				dynamicFields.put(fieldInfo.get(IndexSchema.NAME).toString(), Boolean.getBoolean(fieldInfo.get("multiValued").toString()));
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean hasExplicitField(String fieldName) {
		if (fields.containsKey(fieldName)) {
			return true;
		}

		for (String df : dynamicFields.keySet()) {
			if (fieldName.equals(df))
				return true;
		}

		return false;
	}
	
	public boolean isDynamicField(String fieldName) {
		if (fields.containsKey(fieldName)) {
			return true;
		}

		for (String df : dynamicFields.keySet()) {
			if(df.startsWith("*")){
				if(fieldName.endsWith(df.substring(1))) return true;
			}
			else if(df.endsWith("*")){
				if(fieldName.startsWith((df.substring(0,df.length()-1)))) return true;
			}
			else{
				if(fieldName.equals(df)) return true;
			}
		}

		return false;
	}

	public boolean isMultiValued(String fieldName) {
		if (fields.containsKey(fieldName)) {
			return fields.get(fieldName);
		}

		for (String df : dynamicFields.keySet()) {
			if(df.startsWith("*")){
				if(fieldName.endsWith(df.substring(1))) return dynamicFields.get(df);;
			}
			else if(df.endsWith("*")){
				if(fieldName.startsWith((df.substring(0,df.length()-1)))) return dynamicFields.get(df);;
			}
			else{
				if(fieldName.equals(df)) return dynamicFields.get(df);;
			}	
		}
		return false;
	}
}
