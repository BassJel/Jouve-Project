package com.doculibre.constellio.solr.handler.component;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.Config;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.util.DOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CategorizationValidation {

	private boolean isSolrCloud;
	private String coreName;
	private RestIndexSchema restIndexSchema;
	private Set<Categorization> categorizations;
	private CloudSolrServer cloudSolrServer;
	private HttpSolrServer httpSolrServer;
	
	
	public CategorizationValidation(String solrHome, String coreName,String serverHostPort, boolean isSolrCloud){
		this.isSolrCloud=isSolrCloud;
		this.coreName=coreName;
		if(isSolrCloud==true && (serverHostPort==null || serverHostPort.isEmpty()))
		{
			System.err.println("Parameter zkHost is not set.");
			return;
		}
		
		try {
			if(isSolrCloud){
				cloudSolrServer = new CloudSolrServer(serverHostPort);
				cloudSolrServer.setDefaultCollection(coreName);
				cloudSolrServer.setZkClientTimeout(5000);
				cloudSolrServer.setZkConnectTimeout(5000);
				restIndexSchema = new RestIndexSchema(cloudSolrServer);
			}
			else {
				//CoreContainer.Initializer initializer =  new CoreContainer.Initializer();
//				CoreContainer coreContainer = new CoreContainer(solrHome);
//				coreContainer.load();
//				embeddedSolrServer = new EmbeddedSolrServer(coreContainer, coreName);
//				indexSchema= coreContainer.getCore(coreName).getLatestSchema();
				httpSolrServer = new HttpSolrServer("http://"+serverHostPort+"/solr/");
				restIndexSchema = new RestIndexSchema(httpSolrServer);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public boolean validate(Config config){
		loadCategorization(config);
		for (Categorization categorization : categorizations) {
			String destField = categorization.getDestField();
			if (destField != null && !destField.isEmpty() && validateFieldExist(destField)) {
				for (CategorizationRule categorizationRule : categorization.getCategorizationRules()) {
					String srcField = categorizationRule.getSrcField();
					if (srcField != null && !srcField.isEmpty() && validateFieldExist(destField)) {
						if(!validateFieldTypeMatched(destField, categorizationRule.getMatchRegexpIndexedValues())){
							System.err.println("matchRegexpIndexedValues is not compatible with destField in the rule with Values[srcField,regex]="+categorizationRule.getSrcField()+","+categorizationRule.getMatchRegexp()+" of the categorization "+categorization.getName());
							return false;
						}
					}
					else {
						System.err.println("srcField is not valid in the rule with Values[srcField,regex]="+categorizationRule.getSrcField()+","+categorizationRule.getMatchRegexp()+" of the categorization "+categorization.getName());
						return false;
					}
				}
			}
			else {
				System.err.println("destField is not valid in the categorization "+categorization.getName());
				return false;
			}
		}
			
		return true;
	}
	
	private boolean validateFieldExist(String fieldName){
		return restIndexSchema.hasExplicitField(fieldName) || restIndexSchema.isDynamicField(fieldName);
	}
	
	private boolean validateFieldTypeMatched(String fieldName, Set<String> matchRegexpIndexedValues){
		return (matchRegexpIndexedValues.size()==1)||(matchRegexpIndexedValues.size()>1 && restIndexSchema.isMultiValued(fieldName));
	}
	
	// load up the category configuration
	private void loadCategorization(Config config){
		XPath xpath = XPathFactory.newInstance().newXPath();
		Set<Categorization> categorizations = new HashSet<Categorization>();
		NodeList nodes = (NodeList) config.evaluate("categories/category", XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			Categorization categorization = new Categorization();
			String name = DOMUtil.getAttr(node, "name", "missing category 'name'");
			categorization.setName(name);
			String destField = DOMUtil.getAttr(node, "destField", "missing category 'destField'");
			categorization.setDestField(destField);

			NodeList children = null;
			try {
				children = (NodeList) xpath.evaluate("rule", node, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "category requires '<rule .../>' child");
			}

			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				CategorizationRule categorizationRule = new CategorizationRule();
				String srcField = DOMUtil.getAttr(child, "srcField", "missing 'srcField'");
				categorizationRule.setSrcField(srcField);
				String matchRegexp = DOMUtil.getAttr(child, "regex", "missing 'regex'");
				categorizationRule.setMatchRegexp(matchRegexp);

				NodeList grandChildren = null;
				try {
					grandChildren = (NodeList) xpath.evaluate("value", child, XPathConstants.NODESET);
				} catch (XPathExpressionException e) {
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "rule requires '<value .../>' child");
				}

				for (int k = 0; k < grandChildren.getLength(); k++) {
					Node grandChild = grandChildren.item(k);
					String value = DOMUtil.getAttr(grandChild, "value", "missing 'value'");
					categorizationRule.addMatchRegexpIndexedValue(value);
				}

				categorization.addCategorizationRule(categorizationRule);
			}

			categorizations.add(categorization);
		}
		this.categorizations=categorizations;
	}
	
	public void reloadConfiguration(){
			try {
				if(isSolrCloud){
//					SolrQuery solrQuery = new SolrQuery();
//					solrQuery.setParam(CommonParams.QT, "/admin/cores");
//					solrQuery.setParam("action", "RELOAD");
//					cloudSolrServer.query(solrQuery);
					CoreAdminRequest.reloadCore(coreName, cloudSolrServer);
				}
				else{
//					embeddedSolrServer.getCoreContainer().reload(coreName);
					CoreAdminRequest.reloadCore(coreName,httpSolrServer);
				}
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
