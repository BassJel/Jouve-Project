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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

import com.doculibre.constellio.plugins.api.wicket.global.GlobalThemePlugin;


public class TestPluginSerialization {
    
    public static void main(String[] args) throws Exception {
        GlobalThemePlugin plugin = PluginFactory.getPlugin(GlobalThemePlugin.class);
        System.out.println(plugin);
        final ClassLoader classLoader = plugin.getClass().getClassLoader();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(plugin);
        byte[] serializedPlugin = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedPlugin);
        ObjectInputStream ois = new ObjectInputStream(bais) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
                ClassNotFoundException {
                String className = desc.getName();
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    try {
                        return classLoader.loadClass(className);
                    } catch (ClassNotFoundException e2) {
                        throw e;
                    }
                }
            }
        };
        GlobalThemePlugin deserializedPlugin = (GlobalThemePlugin) ois.readObject();
        System.out.println(deserializedPlugin);
    }

}
