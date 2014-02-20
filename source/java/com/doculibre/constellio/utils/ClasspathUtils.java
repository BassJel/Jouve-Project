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
import java.net.URISyntaxException;
import java.net.URL;

import com.doculibre.constellio.entities.RecordCollection;

public class ClasspathUtils {
    
    public static File getClassesDir() {
        URL classesOrBinDirURL = ClasspathUtils.class.getClassLoader().getResource("");
        File classesOrBinDir;
        try {
            classesOrBinDir = new File(classesOrBinDirURL.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return classesOrBinDir;
    }
    
    public static File getWebinfDir() {
        File webinfDir;
        File classesOrBinDir = getClassesDir();
        if (classesOrBinDir.getName().equals("bin")) {
            File projectDir = classesOrBinDir.getParentFile();
            File webContentDir = new File(projectDir, "WebContent");
            webinfDir = new File(webContentDir, "WEB-INF");
        } else {
            webinfDir = classesOrBinDir.getParentFile();
        }
        return webinfDir;
    }
    
    /**
     * For a web application, it's the web-inf dir. For tests, it's a temporary folder.
     * We don't want unit test to modify contellio project files
     */
    public static File getWorkDir() {
        File workDir;
        File classesOrBinDir = getClassesDir();
        if (classesOrBinDir.getName().equals("bin")) {
        	File webInfDir = getWebinfDir();
            workDir = new File(classesOrBinDir, "temp");
            if (!workDir.exists()) {
            	workDir.mkdir();
            }
            	//default Solr core
            File constellioDir = new File(workDir, "constellio");
            if (!constellioDir.exists()) {
            	FileUtils.copyDirectory(new File(webInfDir, "constellio"), constellioDir);
            }
            workDir.deleteOnExit();
        } else {
        	workDir = getWebinfDir();
        }
        return workDir;
    }
    
	public static File getCollectionsRootDir() {
		File persistentDir = getPersistentDir();
		File collectionsDir = new File(persistentDir, "collections");
		collectionsDir.mkdirs();
		return collectionsDir;
	}
	
	public static File getCollectionRootDir(RecordCollection collection) {
		File collectionsDir = getCollectionsRootDir();
		File collectionDir = new File(collectionsDir, collection.getName());
		return collectionDir;
	}
	
	public static File getPersistentDir() {
		File persistentDir;
		String persistentDirPath = ConstellioSpringUtils.getPersistentDirPath();
		if (persistentDirPath != null) {
			persistentDir = new File(persistentDirPath);
		} else {
			persistentDir = getWebinfDir();
		}
		return persistentDir;
	}
    
//	public static File getStatsDir() {
//		File persistentDir = getPersistentDir();
//		File statsDir = new File(persistentDir, "stats");
//		statsDir.mkdirs();
//		return statsDir;
//	}
    
	public static File getPluginsDir() {
		File persistentDir = getPersistentDir();
		File pluginsDir = new File(persistentDir, "plugins");
		pluginsDir.mkdirs();
		return pluginsDir;
	}
    
	public static File getLuceneIndexesDir() {
		File persistentDir = getPersistentDir();
		File luceneIndexesDir = new File(persistentDir, "lucene_indexes");
		luceneIndexesDir.mkdirs();
		return luceneIndexesDir;
	}
	
	public static File getSynonymsFile(RecordCollection collection) {
		File collectionDir = getCollectionRootDir(collection);
		File synonymsFile = new File(collectionDir, "conf" + File.separator + "synonyms.txt");
		return synonymsFile;
	}
	
	public static void main(String[] args) throws Exception {
        System.out.println(getClassesDir());
    }

}
