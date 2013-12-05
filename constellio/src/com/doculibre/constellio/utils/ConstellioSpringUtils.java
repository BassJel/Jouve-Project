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
package com.doculibre.constellio.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

import com.doculibre.constellio.lucene.impl.FreeTextTagIndexHelper;
import com.doculibre.constellio.lucene.impl.SkosIndexHelper;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.beans.BeanFactoryPlugin;
import com.doculibre.constellio.services.ACLServices;
import com.doculibre.constellio.services.AnalyzerClassServices;
import com.doculibre.constellio.services.AnalyzerServices;
import com.doculibre.constellio.services.AuthorizationServices;
import com.doculibre.constellio.services.AutocompleteServices;
import com.doculibre.constellio.services.BackupServices;
import com.doculibre.constellio.services.CategorizationServices;
import com.doculibre.constellio.services.ClusteringServices;
import com.doculibre.constellio.services.CollectionPermissionServices;
import com.doculibre.constellio.services.ConnectorInstanceServices;
import com.doculibre.constellio.services.ConnectorManagerServices;
import com.doculibre.constellio.services.ConnectorTypeMetaMappingServices;
import com.doculibre.constellio.services.ConnectorTypeServices;
import com.doculibre.constellio.services.ConstellioInitServices;
import com.doculibre.constellio.services.CopyFieldServices;
import com.doculibre.constellio.services.CredentialGroupServices;
import com.doculibre.constellio.services.ElevateServices;
import com.doculibre.constellio.services.FacetServices;
import com.doculibre.constellio.services.FeaturedLinkServices;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.FieldTypeClassServices;
import com.doculibre.constellio.services.FieldTypeServices;
import com.doculibre.constellio.services.FilterClassServices;
import com.doculibre.constellio.services.FreeTextTagServices;
import com.doculibre.constellio.services.GroupServices;
import com.doculibre.constellio.services.ImportExportServices;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.services.MetaNameServices;
import com.doculibre.constellio.services.RawContentServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.services.SearchInterfaceContextServices;
import com.doculibre.constellio.services.SearchResultFieldServices;
import com.doculibre.constellio.services.SearchServices;
import com.doculibre.constellio.services.SkosServices;
import com.doculibre.constellio.services.SolrConfigServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.services.StatsServices;
import com.doculibre.constellio.services.StatusServices;
import com.doculibre.constellio.services.SynonymServices;
import com.doculibre.constellio.services.TokenizerClassServices;
import com.doculibre.constellio.services.UserServices;

public class ConstellioSpringUtils {
	
	private static ApplicationContext applicationContext;
	
	private static void loadApplicationContext() {
		if (applicationContext == null) {
            applicationContext = new ClassPathXmlApplicationContext("constellio.xml");
		}
	}
	
	public static void setApplicationContext(ApplicationContext applicationContext) {
		ConstellioSpringUtils.applicationContext = applicationContext;
	}

	@SuppressWarnings("unchecked")
    public static final <T extends Object> T getBean(String name) {
		loadApplicationContext();
		return (T) applicationContext.getBean(name);
	}

	@SuppressWarnings("unchecked")
    public static final <T extends Object> T getPluggableBean(String name) {
        T bean;
	    BeanFactoryPlugin beanFactoryPlugin = PluginFactory.getPlugin(BeanFactoryPlugin.class);
	    if (beanFactoryPlugin != null) {
	        bean = (T) beanFactoryPlugin.getBean(name);
	    } else {
	        bean = (T) getBean(name);
	    }
	    return bean;
	}
	
	public static Resource[] getResources(String locationPattern) {
		loadApplicationContext();
		try {
			return applicationContext.getResources(locationPattern);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Map<String, Object> getConfig() {
		return getBean("config");
	}
	
	public static String getDictionaries() {
	    return (String) getConfig().get("dictionaries");
	}
    
    @SuppressWarnings("unchecked")
    public static List<String> getFileDownloadDirs() {
        return (List<String>) getConfig().get("fileDownloadDirs");
    }
	
	public static URL getDefaultConnectorManagerURL() {
	    String defaultConnectorManager = (String) getConfig().get("defaultConnectorManager");
	    try {
            return new URL(defaultConnectorManager);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
	}
	
	@SuppressWarnings("unchecked")
	public static List<Locale> getSupportedLocales() {
		List<Locale> supportedLocales = new ArrayList<Locale>();
		List<String> supportedLanguages = (List<String>) getConfig().get("supportedLanguages");
		for (String supportedLanguage : supportedLanguages) {
			supportedLocales.add(new Locale(supportedLanguage));
		}
		return supportedLocales;
	}
    
    @SuppressWarnings("unchecked")
    public static List<Locale> getSearchableLocales() {
        List<Locale> searchableLocales = new ArrayList<Locale>();
        List<String> searchableLanguages = (List<String>) getConfig().get("searchableLanguages");
        if (searchableLanguages != null) {
            for (String searchableLanguage : searchableLanguages) {
                searchableLocales.add(new Locale(searchableLanguage));
            }
        }
        return searchableLocales;
    }

    public static Locale getDefaultLocale() {
        return getSupportedLocales().get(0);
    }
    
    public static ACLServices getACLServices() {
        return getPluggableBean("aclServices");
    }

    public static List<String> getAvailablePluginNames() {
        return getBean("availablePluginNames");
    }
	
	public static AnalyzerClassServices getAnalyzerClassServices() {
		return getPluggableBean("analyzerClassServices");
	}
	
	public static AnalyzerServices getAnalyzerServices() {
		return getPluggableBean("analyzerServices");
	}
    
    public static AuthorizationServices getAuthorizationServices() {
        return getPluggableBean("authorizationServices");
    }
    
    public static BackupServices getBackupServices() {
        return getPluggableBean("backupServices");
    }

	public static ClusteringServices getClusteringServices() {
		return getPluggableBean("clusteringServices");
	}
	
	public static ConnectorInstanceServices getConnectorInstanceServices() {
		return getPluggableBean("connectorInstanceServices");
	}
	
	public static ConnectorManagerServices getConnectorManagerServices() {
		return getPluggableBean("connectorManagerServices");
	}

	public static ConnectorTypeMetaMappingServices getConnectorTypeMetaMappingServices() {
		return getPluggableBean("connectorTypeMetaMappingServices");
	}
	
	public static ConnectorTypeServices getConnectorTypeServices() {
		return getPluggableBean("connectorTypeServices");
	}
    
    public static ConstellioInitServices getConstellioInitServices() {
        return getPluggableBean("constellioInitServices");
    }
    
    public static CredentialGroupServices getCredentialGroupServices() {
        return getPluggableBean("credentialGroupServices");
    }
	
	public static ElevateServices getElevateServices() {
		return getPluggableBean("elevateServices");
	}
	
	public static FacetServices getFacetServices() {
		return getPluggableBean("facetServices");
	}
	
	public static FeaturedLinkServices getFeaturedLinkServices() {
		return getPluggableBean("featuredLinkServices");
	}

    public static FederationServices getFederationServices() {
        return getPluggableBean("federationServices");
    }
	
	public static FieldTypeClassServices getFieldTypeClassServices() {
		return getPluggableBean("fieldTypeClassServices");
	}
	
	public static FieldTypeServices getFieldTypeServices() {
		return getPluggableBean("fieldTypeServices");
	}
	
	public static FilterClassServices getFilterClassServices() {
		return getPluggableBean("filterClassServices");
	}

    public static FreeTextTagServices getFreeTextTagServices() {
        return getPluggableBean("freeTextTagServices");
    }

    public static FreeTextTagIndexHelper getFreeTextTagIndexHelper() {
        return getPluggableBean("freeTextTagIndexHelper");
    }
	
	public static GroupServices getGroupServices() {
		return getPluggableBean("groupServices");
	}
	
	public static CollectionPermissionServices getCollectionPermissionServices() {
		return getPluggableBean("collectionPermissionServices");
	}

	public static ImportExportServices getImportExportServices() {
		return getPluggableBean("importExportServices");
	}

	public static IndexFieldServices getIndexFieldServices() {
		return getPluggableBean("indexFieldServices");
	}
	
	public static MetaNameServices getMetaNameServices() {
		return getPluggableBean("metaNameServices");
	}

	public static RawContentServices getRawContentServices() {
		return getPluggableBean("rawContentServices");
	}
	
	public static RecordCollectionServices getRecordCollectionServices() {
		return getPluggableBean("recordCollectionServices");
	}
	
	public static RecordServices getRecordServices() {
		return getPluggableBean("recordServices");
	}
	
	public static SearchInterfaceConfigServices getSearchInterfaceConfigServices() {
		return getPluggableBean("searchInterfaceConfigServices");
	}

    public static SearchResultFieldServices getSearchResultFieldServices() {
        return getPluggableBean("searchResultFieldServices");
    }
	
	public static SearchServices getSearchServices() {
		return getPluggableBean("searchServices");
	}
	
	public static SkosIndexHelper getSkosIndexHelper() {
        return getPluggableBean("skosIndexHelper");
	}

    public static SkosServices getSkosServices() {
        return getPluggableBean("skosServices");
    }

	public static SolrServices getSolrServices() {
		return getPluggableBean("solrServices");
	}
	
	public static SolrConfigServices getSolrConfigServices() {
		return getPluggableBean("solrConfigServices");
	}

    public static StatsServices getStatsServices() {
        return getPluggableBean("statsServices");
    }

    public static StatusServices getStatusServices() {
        return getPluggableBean("statusServices");
    }
	
	public static CategorizationServices getCategorizationServices() {
		return getPluggableBean("categorizationServices");
	}
	
	public static SynonymServices getSynonymServices() {
		return getPluggableBean("synonymServices");
	}
	
	public static TokenizerClassServices getTokenizerClassServices() {
		return getPluggableBean("tokenizerClassServices");
	}
	
	public static UserServices getUserServices() {
		return getPluggableBean("userServices");
	}

	public static AutocompleteServices getAutocompleteServices() {
		return getPluggableBean("autocompleteServices");
	}
	
	public static CopyFieldServices getCopyFieldServices() {
		return getPluggableBean("copyFieldServices");
	}
	
	public static SearchInterfaceContextServices getSearchInterfaceContextServices() {
		return getPluggableBean("searchInterfaceContextServices");
	}
	
	public static String getPersistentDirPath() {
		String persistentDirPath;
		Map<String, Object> config = getConfig();
		if (config != null) {
			persistentDirPath = (String) config.get("persistentDir");
		} else {
			persistentDirPath = null;
		}
		return persistentDirPath;
	}	
	
	public static File getGoogleConnectorsDir() {
		String persistentDirPath = getPersistentDirPath();
		return new File(persistentDirPath,  "connectors");
	}
	
	public static int getFeedProcessorThreads() {
		Map<String, Object> config = getConfig();
		String feedProcessorThreads = (String) config.get("feedProcessorThreads");
		try {
			return new Integer(feedProcessorThreads).intValue();
		} catch (Exception e) {
			return 5; 
		}
	} 
	
	public static String getSolrLogServer() {
		String solrLogServer;
		Map<String, Object> config = getConfig();
		if (config != null) {
			solrLogServer = (String) config.get("solrLogServer");
		} else {
			solrLogServer = null;
		}
		return solrLogServer;
	}
	
}
