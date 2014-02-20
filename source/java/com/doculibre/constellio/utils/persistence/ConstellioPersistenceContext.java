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
package com.doculibre.constellio.utils.persistence;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.doculibre.constellio.plugins.PluginAwareClassLoader;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.utils.ClasspathUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class ConstellioPersistenceContext {

	private final static String ENTITY_MANAGER_FACTORY_NAME = "constellio";
	
	private static EntityManagerFactory ENTITY_MANAGER_FACTORY;
	
	private final static ThreadLocal<EntityManager> ENTITY_MANAGERS = new ThreadLocal<EntityManager>();
	
	static {
	    File classesDir = ClasspathUtils.getClassesDir();
        File webinfDir = ClasspathUtils.getWebinfDir();
	    File webappDir = webinfDir.getParentFile();
	    File workDir = ClasspathUtils.getWorkDir();
	    String workDirPath = workDir.getAbsolutePath();
        String workDirPathFrontSlash = workDir.getAbsolutePath().replace("\\", "/");
	    String classesDirPath = classesDir.getAbsolutePath();
	    String classesDirPathFrontSlash = classesDir.getAbsolutePath().replace("\\", "/");
	    String constellioDirPath = webappDir.getAbsolutePath();
        String constellioDirPathFrontSlash = webappDir.getAbsolutePath().replace("\\", "/");
        
        System.setProperty("workDir", workDirPath);
        System.setProperty("workDirFrontSlash", workDirPathFrontSlash);
        System.setProperty("classesDir", classesDirPath);
        System.setProperty("classesDirFrontSlash", classesDirPathFrontSlash);
        System.setProperty("constellioDir", constellioDirPath);
	    System.setProperty("constellioDirFrontSlash", constellioDirPathFrontSlash);
	    generatePersistenceFile();
	}
	
    private static void generatePersistenceFile() {
    	//Usefull for unit testing
    	String basePersistenceFileName = System.getProperty("base-persistence-file");
    	if (basePersistenceFileName == null) {
    		basePersistenceFileName = "persistence_derby.xml";
    	}
    	
        File classesDir = ClasspathUtils.getClassesDir();
        File metaInfDir = new File(classesDir, "META-INF");
        File persistenceFile = new File(metaInfDir, "persistence.xml");
        File persistenceBaseFile = new File(metaInfDir, basePersistenceFileName);
        if (persistenceFile.exists()) {
            persistenceFile.delete();
        }

        // FIXME Using text files rather than Dom4J because of empty xmlns attribute generated...
        try {
            String persistenceBaseText = FileUtils.readFileToString(persistenceBaseFile);
            StringBuffer sbJarFile = new StringBuffer("\n");
            File pluginsDir = PluginFactory.getPluginsDir();
            for (String availablePluginName : ConstellioSpringUtils.getAvailablePluginNames()) {
                if (PluginFactory.isValidPlugin(availablePluginName)) {
                    File pluginDir = new File(pluginsDir, availablePluginName);
                    File pluginJarFile = new File(pluginDir, availablePluginName + ".jar");
                    String pluginJarURI = pluginJarFile.toURI().toString();
                    sbJarFile.append("\n");
                    sbJarFile.append("<jar-file>" + pluginJarURI + "</jar-file>");
                }
            }
            
            File webInfDir = ClasspathUtils.getWebinfDir();
            File libDir = new File(webInfDir, "lib");
            File[] contellioJarFiles = libDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    boolean accept;
                    if (pathname.isDirectory()) {
                        accept = false;
                    } else {
                        List<String> availablePluginNames = ConstellioSpringUtils.getAvailablePluginNames();
                        String jarNameWoutExtension = FilenameUtils.removeExtension(pathname.getName());
                        accept = availablePluginNames.contains(jarNameWoutExtension);
                    }    
                    return accept;
                }
            });
            for (File constellioJarFile : contellioJarFiles) {
                URI constellioJarFileURI = constellioJarFile.toURI();
                sbJarFile.append("\n");
                sbJarFile.append("<jar-file>" + constellioJarFileURI + "</jar-file>");
			}
            
            String persistenceText = persistenceBaseText.replaceAll("</provider>", "</provider>" + sbJarFile);
            FileUtils.writeStringToFile(persistenceFile, persistenceText);
//            persistenceFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
    
    private static synchronized void init() {
		if (ENTITY_MANAGER_FACTORY == null) {
		    PluginAwareClassLoader pluginAwareClassLoader = new PluginAwareClassLoader();
		    Thread.currentThread().setContextClassLoader(pluginAwareClassLoader);
		    ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory(ENTITY_MANAGER_FACTORY_NAME);
		    Thread.currentThread().setContextClassLoader(pluginAwareClassLoader.getDefaultClassLoader());
		}
    }
	
	public static synchronized EntityManager getCurrentEntityManager() {
		init();
		EntityManager em = ENTITY_MANAGERS.get();
		if (em == null || !em.isOpen()) {
			em = ENTITY_MANAGER_FACTORY.createEntityManager();
			ENTITY_MANAGERS.set(em);
		}
		return em;
	}
	
	public static synchronized void setCurrentEntityManager(EntityManager entityManager) {
		init();
		ENTITY_MANAGERS.set(entityManager);
	}
	
	public synchronized static void close() {
		if (ENTITY_MANAGER_FACTORY.isOpen()) {
			ENTITY_MANAGER_FACTORY.close();
		}
		ENTITY_MANAGER_FACTORY = null;
	}
}
