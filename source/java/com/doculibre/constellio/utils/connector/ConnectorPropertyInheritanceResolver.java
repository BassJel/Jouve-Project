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
package com.doculibre.constellio.utils.connector;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.plugins.PluginAwareClassLoader;

public class ConnectorPropertyInheritanceResolver {

    @SuppressWarnings("unchecked")
    public static <T extends Object> T newInheritedClassPropertyInstance(ConnectorInstance connectorInstance,
        String propertyName) {
        return (T) newInheritedClassPropertyInstance(connectorInstance, propertyName, null, null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> T newInheritedClassPropertyInstance(ConnectorInstance connectorInstance,
        String propertyName, Class[] paramTypes, Object[] args) {
        T inheritedClassPropertyInstance;
        String propertyValue = getStringPropertyValue(connectorInstance, propertyName);
        if (StringUtils.isEmpty(propertyValue)) {
            propertyValue = getStringPropertyValue(connectorInstance.getConnectorType(), propertyName);
        }
        if (StringUtils.isNotEmpty(propertyValue)) {
            PluginAwareClassLoader pluginAwareClassLoader = new PluginAwareClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(pluginAwareClassLoader);
                Class<T> propertyValueClass = (Class<T>) Class.forName(propertyValue);
                Constructor<T> constructor = propertyValueClass.getConstructor(paramTypes);
                inheritedClassPropertyInstance = constructor.newInstance(args);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(pluginAwareClassLoader.getDefaultClassLoader());
            }
        } else {
            inheritedClassPropertyInstance = null;
        }
        return inheritedClassPropertyInstance;
    }

    private static String getStringPropertyValue(Object bean, String propertyName) {
        try {
            PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(bean.getClass(),
                propertyName);
            return (String) propertyDescriptor.getReadMethod().invoke(bean);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
