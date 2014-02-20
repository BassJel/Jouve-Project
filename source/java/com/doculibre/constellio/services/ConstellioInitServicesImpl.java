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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.wicket.authorization.strategies.role.Roles;

import com.doculibre.constellio.entities.Cache;
import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.entities.SolrConfig;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.lucene.impl.FreeTextTagIndexHelper;
import com.doculibre.constellio.lucene.impl.SkosIndexHelper;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.status.StatusManager;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.izpack.UsersXmlFileUtils;

public class ConstellioInitServicesImpl implements ConstellioInitServices {

    @Override
    public void init() {
        AnalyzerClassServices analyzerClassServices = ConstellioSpringUtils.getAnalyzerClassServices();
        FieldTypeClassServices fieldTypeClassServices = ConstellioSpringUtils.getFieldTypeClassServices();
        FilterClassServices filterClassServices = ConstellioSpringUtils.getFilterClassServices();
        TokenizerClassServices tokenizerClassServices = ConstellioSpringUtils.getTokenizerClassServices();
        SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
        SolrConfigServices solrConfigServices = ConstellioSpringUtils.getSolrConfigServices();
        SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();
        SearchInterfaceContextServices searchInterfaceContextServices = ConstellioSpringUtils.getSearchInterfaceContextServices();
        StatsServices statsServices = ConstellioSpringUtils.getStatsServices();

        SolrCoreContext.init();
        statsServices.init();
//        statsServices.recompile();
        
        try {
            solrServices.cleanCores();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        UserServices userServices = ConstellioSpringUtils.getUserServices();
//        ConstellioUser dataUser = userServices.get("admin");
        ConstellioUser dataUser = userServices.getFirstAdminUser();
        Boolean atLeastOneAdmin = (dataUser != null);
        if (!atLeastOneAdmin) {
        	atLeastOneAdmin = addUsersFromFile();
        	if (!atLeastOneAdmin){
                dataUser = new ConstellioUser("admin", "password", ConstellioSpringUtils.getDefaultLocale());
                dataUser.setFirstName("System");
                dataUser.setLastName("Administrator");
                dataUser.getRoles().add(Roles.ADMIN);
                userServices.makePersistent(dataUser);
        	}
        }
        
        if (solrConfigServices.getDefaultConfig() == null) {
        	SolrConfig config = new SolrConfig();
        	
        	Cache defaultFilterCache = new Cache();
        	defaultFilterCache.setCacheClass("solr.search.LRUCache");
        	defaultFilterCache.setSize(16384);
        	defaultFilterCache.setInitialSize(4096);
        	defaultFilterCache.setAutowarmCount(4096);
        	config.setFilterCacheConfig(defaultFilterCache);
        	
        	Cache defaultQueryResultCache = new Cache();
        	defaultQueryResultCache.setCacheClass("solr.search.LRUCache");
        	defaultQueryResultCache.setSize(16384);
        	defaultQueryResultCache.setInitialSize(4096);
        	defaultQueryResultCache.setAutowarmCount(1024);
        	config.setQueryResultCacheConfig(defaultQueryResultCache);
        	
        	Cache defaultDocumentCache = new Cache();
        	defaultDocumentCache.setCacheClass("solr.search.LRUCache");
        	defaultDocumentCache.setSize(16384);
        	defaultDocumentCache.setInitialSize(16384);
        	config.setDocumentCacheConfig(defaultDocumentCache);
        	
        	config.setUseFilterForSortedQuery(true);
        	config.setQueryResultWindowSize(50);
        	config.setHashDocSetMaxSize(3000);
        	config.setHashDocSetLoadFactor(0.75f);
        	
        	solrConfigServices.makePersistent(config);
        }
        
        
        if (searchInterfaceConfigServices.get() == null) {
        	SearchInterfaceConfig config = new SearchInterfaceConfig();
        	searchInterfaceConfigServices.makePersistent(config);
        }
        searchInterfaceContextServices.init();
        
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
        if (connectorManager == null) {
            connectorManager = connectorManagerServices.createDefaultConnectorManager();
        } else {
            URL defaultConnectorManagerURL = ConstellioSpringUtils.getDefaultConnectorManagerURL();
            try {
                connectorManager.setUrl(defaultConnectorManagerURL.toString());
                connectorManagerServices.makePersistent(connectorManager);
            } catch (MalformedURLException e) {
               throw new RuntimeException(e);
            }
        }
        connectorManagerServices.synchronizeWithDatabase(connectorManager);

        analyzerClassServices.init();
        fieldTypeClassServices.init();
        filterClassServices.init();
        tokenizerClassServices.init();
        solrServices.initSchemaFieldTypes();
        solrServices.initConnectorTypeFields();

        StatusManager.init();
        
        SkosIndexHelper skosIndexHelper = ConstellioSpringUtils.getSkosIndexHelper();
        if (skosIndexHelper.isEmpty()) {
        	skosIndexHelper.rebuild();
        }
        FreeTextTagIndexHelper freeTagIndexHelper = ConstellioSpringUtils.getFreeTextTagIndexHelper();
        if (freeTagIndexHelper.isEmpty()) {
        	freeTagIndexHelper.rebuild();
        }
        
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        List<RecordCollection> collections = collectionServices.list();
        for (RecordCollection collection : collections) {
            if (!collection.isOpenSearch()) {
                IndexingManager indexingManager = IndexingManager.get(collection);
                if (!indexingManager.isActive()) {
                    indexingManager.startIndexing();
                }
            }
        }
    }

    private Boolean addUsersFromFile() {
    	List<ConstellioUser> usersList = UsersXmlFileUtils.readUsers();
    	if (usersList == null){
    		return false;
    	}
    	
    	UserServices userServices = ConstellioSpringUtils.getUserServices();
    	Boolean atLeastOneAdminAdded = false;
    	for(ConstellioUser user : usersList){
    		userServices.makePersistent(user);
    		if(user.isAdmin()){
    			atLeastOneAdminAdded = true;
    		}
    	}

		return atLeastOneAdminAdded;
	}

	@Override
    public void shutdown() {
        SolrCoreContext.shutdown();
        
        StatsServices statsServices = ConstellioSpringUtils.getStatsServices();
        statsServices.shutdown();
        
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        List<RecordCollection> collections = collectionServices.list();
        for (RecordCollection collection : collections) {
            IndexingManager indexingManager = IndexingManager.get(collection);
            if (indexingManager.isActive()) {
                indexingManager.stopManaging();
            }
            int attempts = 0;
            while (indexingManager.isActive() && attempts < 100) {
                try {
                    attempts++;
                    // Wait for thread to stop
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
