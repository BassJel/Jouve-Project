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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

public class PluginAwareClassLoader extends ClassLoader {
    
    private ClassLoader defaultClassLoader;
    
    public PluginAwareClassLoader() {
        defaultClassLoader = Thread.currentThread().getContextClassLoader();
        while (defaultClassLoader instanceof PluginAwareClassLoader) {
            PluginAwareClassLoader nested = (PluginAwareClassLoader) defaultClassLoader;
            defaultClassLoader = nested.getDefaultClassLoader();
        }
    }

    public ClassLoader getDefaultClassLoader() {
        return defaultClassLoader;
    }

    public void setDefaultClassLoader(ClassLoader defaultClassLoader) {
        this.defaultClassLoader = defaultClassLoader;
    }
    
    protected Set<ClassLoader> getExtraClassLoaders() {
        Set<ClassLoader> extraClassLoaders = PluginFactory.getClassLoaders();
        extraClassLoaders.add(PluginAwareClassLoader.class.getClassLoader());
        return extraClassLoaders;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz;
        Throwable throwable;
        try {
            clazz = defaultClassLoader.loadClass(name);
            throwable = null;
        } catch (Throwable t) {
            clazz = null;
            throwable = t;
        }
        if (clazz == null) {
            for (ClassLoader extraClassLoader : getExtraClassLoaders()) {
                try {
                    clazz = extraClassLoader.loadClass(name);
                    if (clazz != null) {
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    // Ignore, already handled
                }
            }
        }
        if (clazz == null && throwable != null) {
            if (throwable instanceof ClassNotFoundException) {
                throw (ClassNotFoundException) throwable;
            } else if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else {
                throw new RuntimeException(throwable);
            }
        } 
        return clazz;
    }

    @Override
    protected URL findResource(String name) {
        URL resource;
        Throwable throwable;
        try {
            resource = defaultClassLoader.getResource(name);
            throwable = null;
        } catch (Throwable t) {
            resource = null;
            throwable = t;
        }
        if (resource == null) {
            for (ClassLoader extraClassLoader : getExtraClassLoaders()) {
                resource = extraClassLoader.getResource(name);
                if (resource != null) {
                    break;
                }
            }
        }
        if (resource == null && throwable != null) {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else {
                throw new RuntimeException(throwable);
            }
        } 
        return resource;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> resources;
        Throwable throwable;
        try {
            resources = defaultClassLoader.getResources(name);
            throwable = null;
        } catch (Throwable t) {
            resources = null;
            throwable = t;
        }
        if (resources == null) {
            for (ClassLoader extraClassLoader : getExtraClassLoaders()) {
                resources = extraClassLoader.getResources(name);
                if (resources != null) {
                    break;
                }
            }
        }
        if (resources == null && throwable != null) {
            if (throwable instanceof IOException) {
                throw (IOException) throwable;
            } else if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else {
                throw new RuntimeException(throwable);
            }
        } 
        return resources;
    }

}
