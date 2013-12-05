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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public class ResourceBundleUtils {
    
    public static Set<String> getKeys(String prefix, Class<? extends Object> clazz) {
        Set<String> keys = new HashSet<String>();
        String baseName = clazz.getName();
        ClassLoader loader = clazz.getClassLoader();
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, Locale.getDefault(), loader);
        Enumeration<String> keysEnumeration = bundle.getKeys();
        while (keysEnumeration.hasMoreElements()) {
            String key = keysEnumeration.nextElement();
            if (prefix == null || key.startsWith(prefix)) {
                keys.add(key);
            }
        }
        return keys;
    }
    
    public static String getString(String key, Locale locale, Class<? extends Object> clazz) {
        String baseName = clazz.getName();
        ClassLoader loader = clazz.getClassLoader();
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, loader);
        return bundle.getString(key);
    }

}
