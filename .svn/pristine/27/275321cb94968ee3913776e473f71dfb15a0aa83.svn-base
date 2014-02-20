package com.doculibre.constellio.solr.handler.component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.Config;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.search.function.FileFloatSource.ReloadCacheRequestHandler;
import org.apache.zookeeper.ZooKeeper;

import com.google.common.io.Files;

public class Executor {

	public static final String SOLRHOME = "D:/doculibre/testdeploy/solr-4.4.0/example2/solr";
	public static final String CORENAME = "collection1";
	public static final String CONFFILE = "category.xml";
	public static final String CONFNAME_ZOOKEEPER = "collection1";
	public static final String ZKHOSTPORT = "localhost:9983";
	public static final String SOLRHOSTPORT = "localhost:8983";
	
	public static void main(String[] args)
	{
		boolean isSolrCloud = true;
		
		System.setProperty("solr.solr.home", SOLRHOME);
		try {
			Config config = new Config(null, SOLRHOME + "/" + CORENAME + "/conf/" +CONFFILE);
			CategorizationValidation validation= new CategorizationValidation(SOLRHOME, CORENAME, ZKHOSTPORT, isSolrCloud);
//			if(validation.validate(config))
			{
				if(isSolrCloud){
					ZooKeeper zooKeeper = new ZooKeeper(ZKHOSTPORT, 3000, null);
					zooKeeper.setData("/configs/"+CONFNAME_ZOOKEEPER+"/"+CONFFILE, Files.toByteArray(new File(SOLRHOME + "/" + CORENAME + "/conf/" +CONFFILE)), -1);
					validation.reloadConfiguration();
				}
				else {
					validation.reloadConfiguration();
				}
			}
//			else {
//				System.err.println(CONFFILE + " can not be validated!");
//			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
}
