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
package com.doculibre.constellio.wicket.components.resource;

import java.io.File;

import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.defaults.DefaultConstellioPlugin;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class ThemeResourceFinder implements IResourceFinder {
    
    private IResourceFinder defaultResourceFinder;
    
    public ThemeResourceFinder(IResourceFinder defaultResourceFinder) {
        this.defaultResourceFinder = defaultResourceFinder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IResourceStream find(Class clazz, String pathname) {
        IResourceStream result;
        String relPackagePath = "com/doculibre/constellio/wicket/";
        SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();
        SearchInterfaceConfig searchInterfaceConfig = searchInterfaceConfigServices.get();
        String skin = searchInterfaceConfig.getSkin();
        if (pathname.startsWith(relPackagePath)) {
            {
                String subPathname = "skins/" + skin + "/" + pathname.substring(relPackagePath.length());
                result = null;
                File pluginsDir = PluginFactory.getPluginsDir();
                String adjustedPathname = subPathname.replace("/", File.separator);
                for (String pluginName : ConstellioSpringUtils.getAvailablePluginNames()) {
                    File pluginDir = new File(pluginsDir, pluginName);
                    File resourcePath = new File(pluginDir, adjustedPathname);
                    if (resourcePath.exists()) {
                        result = new FileResourceStream(resourcePath);
                        break;
                    }
                }
                if (result == null) {
                    File defaultPluginDir = new File(pluginsDir, DefaultConstellioPlugin.NAME);
                    File defaultResourcePath = new File(defaultPluginDir, adjustedPathname);
                    if (defaultResourcePath.exists()) {
                        result = new FileResourceStream(defaultResourcePath);
                    } else {
                        result = defaultResourceFinder.find(clazz, pathname);
                    }
                }   
            }
            if (result == null) {
                String subPathname = pathname.substring(relPackagePath.length());
                result = null;
                File pluginsDir = PluginFactory.getPluginsDir();
                String adjustedPathname = subPathname.replace("/", File.separator);
                for (String pluginName : ConstellioSpringUtils.getAvailablePluginNames()) {
                    File pluginDir = new File(pluginsDir, pluginName);
                    File resourcePath = new File(pluginDir, adjustedPathname);
                    if (resourcePath.exists()) {
                        result = new FileResourceStream(resourcePath);
                        break;
                    }
                }
                if (result == null) {
                    File defaultPluginDir = new File(pluginsDir, DefaultConstellioPlugin.NAME);
                    File defaultResourcePath = new File(defaultPluginDir, adjustedPathname);
                    if (defaultResourcePath.exists()) {
                        result = new FileResourceStream(defaultResourcePath);
                    } else {
                        result = defaultResourceFinder.find(clazz, pathname);
                    }
                }
            }
        } else {
            result = defaultResourceFinder.find(clazz, pathname);
        }
        return result;
    }

}
