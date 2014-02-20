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
package com.doculibre.constellio.plugins;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.impl.PluginManagerImpl;
import net.xeoh.plugins.base.impl.classpath.ClassPathManager;
import net.xeoh.plugins.base.impl.classpath.ClassPathManagerUtils;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.doculibre.constellio.plugins.api.ConstellioPlugin;
import com.doculibre.constellio.plugins.defaults.DefaultConstellioPlugin;
import com.doculibre.constellio.utils.ClasspathUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class PluginFactory {

    private static Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();

    private static PluginManager pm;
    
    private static void initPluginManager() {
        if (pm == null) {
            pm = PluginManagerFactory.createPluginManager();
            File classesDir = ClasspathUtils.getClassesDir();

            pm.addPluginsFrom(classesDir.toURI());

            File pluginsDir = getPluginsDir();
            File[] pluginDirs = pluginsDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    boolean accept;
                    if (pathname.isFile()) {
                        accept = false;
                    } else if (DefaultConstellioPlugin.NAME.equals(pathname)) {
                        accept = true;
                    } else {
                        List<String> availablePluginNames = ConstellioSpringUtils.getAvailablePluginNames();
                        accept = availablePluginNames.contains(pathname.getName());
                    }
                    return accept;
                }
            });
            if (pluginDirs == null){
            	return;
            }
            for (File pluginDir : pluginDirs) {
                // Plugin root dir jars
                Collection<File> pluginJarFiles = FileUtils.listFiles(pluginDir, new String[] { "jar" },
                    false);
                // Accept only one root dir jar
                File pluginJarFile = pluginJarFiles.isEmpty() ? null : pluginJarFiles.iterator().next();
                if (pluginJarFile != null) {
                    URI pluginJarFileURI = pluginJarFile.toURI();
                    pm.addPluginsFrom(pluginJarFileURI);

                    PluginManagerImpl pmImpl = (PluginManagerImpl) pm;
                    ClassPathManager classPathManager = pmImpl.getClassPathManager();
                    ClassLoader classLoader = ClassPathManagerUtils.getClassLoader(classPathManager,
                        pluginJarFile);
                    classLoaders.add(classLoader);

                    File pluginLibDir = new File(pluginDir, "lib");
                    if (pluginLibDir.exists() && pluginLibDir.isDirectory()) {
                        Collection<File> pluginDependencies = FileUtils.listFiles(pluginLibDir,
                            new String[] { "jar" }, false);
                        ClassPathManagerUtils.addJarDependencies(classPathManager, pluginJarFile,
                            pluginDependencies);
                    }
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
                pm.addPluginsFrom(constellioJarFileURI);
			}
        }
    }

    public static <P extends ConstellioPlugin> P getPlugin(Class<P> pluginClass) {
        return getPlugin(pluginClass, false);
    }

    public static <P extends ConstellioPlugin> P getDefaultPlugin(Class<P> pluginClass) {
        return getPlugin(pluginClass, true);
    }

    private static <P extends ConstellioPlugin> P getPlugin(Class<P> pluginClass, boolean onlyDefault) {
        List<P> matches = getPlugins(pluginClass, onlyDefault);
        return !matches.isEmpty() ? matches.get(0) : null;
    }

    public static <P extends ConstellioPlugin> List<P> getPlugins(Class<P> pluginClass) {
        return getPlugins(pluginClass, false);
    }

    public static <P extends ConstellioPlugin> List<P> getDefaultPlugins(Class<P> pluginClass) {
        return getPlugins(pluginClass, true);
    }

    private static <P extends ConstellioPlugin> List<P> getPlugins(Class<P> pluginClass, boolean onlyDefault) {
        List<P> matches = new ArrayList<P>();
        P defaultPlugin = null;

        initPluginManager();
        PluginManagerUtil pmu = new PluginManagerUtil(pm);
        for (P impl : pmu.getPlugins(pluginClass)) {
            // ClassLoader classLoader = impl.getClass().getClassLoader();
            // classLoaders.add(classLoader);
            if (DefaultConstellioPlugin.NAME.equals(impl.getName())) {
                defaultPlugin = impl;
            } else if (!onlyDefault) {
                matches.add(impl);
            }
        }
        if (matches.isEmpty()) {
            if (defaultPlugin != null) {
                matches.add(defaultPlugin);
            }
        } else {
            // If many plugins are found, they are sorted in the order they are configured in constellio.xml
            // (the last has priority over the previous)
            Collections.sort(matches, new Comparator<ConstellioPlugin>() {
                @Override
                public int compare(ConstellioPlugin o1, ConstellioPlugin o2) {
                    List<String> availablePluginNames = ConstellioSpringUtils.getAvailablePluginNames();
                    Integer indexOfPluginName1 = availablePluginNames.indexOf(o1.getName());
                    Integer indexOfPluginName2 = availablePluginNames.indexOf(o2.getName());
                    return indexOfPluginName1.compareTo(indexOfPluginName2);
                }
            });
        }
        return matches;
    }

    public static File getPluginsDir() {
        File webinfDir = ClasspathUtils.getWebinfDir();
        return new File(webinfDir, "plugins");
    }

    public static boolean isValidPlugin(String name) {
        boolean validPlugin;
        File pluginsDir = getPluginsDir();
        File pluginDir = new File(pluginsDir, name);
        if (!pluginDir.exists() || pluginDir.isFile()) {
            validPlugin = false;
        } else {
            Collection<File> pluginJarFiles = FileUtils.listFiles(pluginDir, new String[] { "jar" }, false);
            // Accept only one root dir jar
            File pluginJarFile = pluginJarFiles.isEmpty() ? null : pluginJarFiles.iterator().next();
            if (pluginJarFile != null) {
                validPlugin = true;
            } else {
                validPlugin = false;
            }
        }
        return validPlugin;
    }

    public static Set<ClassLoader> getClassLoaders() {
        initPluginManager();
        return classLoaders;
    }

}
