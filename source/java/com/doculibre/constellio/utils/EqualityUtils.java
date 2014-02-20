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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.beanutils.BeanPredicate;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.EqualPredicate;

public class EqualityUtils {

    public static <T extends Object> boolean contains(Collection<T> collection, T bean, String propertyName) {
        Object propertyValue;
        try {
            propertyValue = PropertyUtils.getProperty(bean, propertyName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        EqualPredicate equalityPredicate = new EqualPredicate(propertyValue);
        BeanPredicate beanPredicate = new BeanPredicate(propertyName, equalityPredicate);
        return CollectionUtils.exists(collection, beanPredicate);
    }
    
    public static boolean isEqualOrBothNullProperty(String propertyName, Object bean1, Object bean2) {
        boolean result;
        try {
            Object propertyValue1 = PropertyUtils.getProperty(bean1, propertyName);
            Object propertyValue2 = PropertyUtils.getProperty(bean2, propertyName);
            if (propertyValue1 == null && propertyValue2 == null) {
                result = true;
            } else if ((propertyValue1 == null && propertyValue2 != null)
                || (propertyValue1 != null && propertyValue2 == null)) {
                result = false;
            } else {
                result = propertyValue1.equals(propertyValue2);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    public static boolean areEqualOrBothNullProperties(String[] propertyNames, Object bean1, Object bean2) {
        boolean result = true;
        for (String propertyName : propertyNames) {
            result = isEqualOrBothNullProperty(propertyName, bean1, bean2);
            if (!result) {
                break;
            }
        }
        return result;
    }

}
