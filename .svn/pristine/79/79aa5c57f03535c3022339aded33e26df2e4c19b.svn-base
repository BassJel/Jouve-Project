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
package com.doculibre.constellio.wicket.components.locale;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.entities.ConstellioLabelledEntity;

@SuppressWarnings("serial")
public class LocalePropertyModel extends Model {

    private IModel labelledEntityModel;
    private String labelKey;
    private String propertyName;
    private Locale locale;

    public LocalePropertyModel(IModel labelledEntityModel, String labelKey) {
        this(labelledEntityModel, labelKey, null, null);
    }

    public LocalePropertyModel(IModel labelledEntityModel, String labelKey, Locale locale) {
        this(labelledEntityModel, labelKey, null, locale);
    }

    public LocalePropertyModel(IModel labelledEntityModel, String labelKey, String propertyName) {
        this(labelledEntityModel, labelKey, propertyName, null);
    }

    public LocalePropertyModel(IModel labelledEntityModel, String labelKey, String propertyName, Locale locale) {
        this.labelledEntityModel = labelledEntityModel;
        this.labelKey = labelKey;
        this.propertyName = propertyName;
        this.locale = locale;
    }

    @Override
    public Object getObject() {
        Locale getterLocale = locale;
        if (getterLocale == null) {
            getterLocale = Session.get().getLocale();
        }
        String label;
        Object entity = labelledEntityModel.getObject();
        if (propertyName == null && entity instanceof ConstellioLabelledEntity) {
            ConstellioLabelledEntity labelledEntity = (ConstellioLabelledEntity) labelledEntityModel.getObject();
            label = labelledEntity.getLabel(labelKey, getterLocale);
        } else {
            String methodName = "get" + StringUtils.capitalize(propertyName);
            Method getter;
            try {
                getter = entity.getClass().getMethod(methodName, String.class, Locale.class);
                label = (String) getter.invoke(entity, labelKey, getterLocale);
            } catch (SecurityException e) {
                throw new WicketRuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new WicketRuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new WicketRuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new WicketRuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new WicketRuntimeException(e);
            }
        }
        return label;
    }

    @Override
    public void setObject(Serializable object) {
        Locale setterLocale = locale;
        if (setterLocale == null) {
            setterLocale = Session.get().getLocale();
        }
        Object entity = labelledEntityModel.getObject();
        if (propertyName == null && entity instanceof ConstellioLabelledEntity) {
            ConstellioLabelledEntity labelledEntity = (ConstellioLabelledEntity) labelledEntityModel.getObject();
            labelledEntity.setLabel(labelKey, (String) object, setterLocale);
        } else {
            String methodName = "set" + StringUtils.capitalize(propertyName);
            Method setter;
            try {
                setter = entity.getClass().getMethod(methodName, String.class, String.class,
                    Locale.class);
                setter.invoke(entity, labelKey, (String) object, setterLocale);
            } catch (SecurityException e) {
                throw new WicketRuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new WicketRuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new WicketRuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new WicketRuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new WicketRuntimeException(e);
            }
        }
    }

    @Override
    public void detach() {
        labelledEntityModel.detach();
        super.detach();
    }

}
