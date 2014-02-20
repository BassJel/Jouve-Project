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
package com.doculibre.constellio.utils.connector.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.doculibre.constellio.entities.CollectionFacet;
import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.I18NLabel;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public abstract class InitDefaultConnectorHandler implements InitConnectorInstanceHandler {

    @Override
    public void init(ConnectorInstance connectorInstance) {
        initDefaultConnectorInstance(connectorInstance);
        initCustomConnectorInstance(connectorInstance);
    }

    protected void initDefaultConnectorInstance(ConnectorInstance connectorInstance) {
        RecordCollection collection = connectorInstance.getRecordCollection();
        
        CollectionFacet languageFacet = createFieldFacetIfNecessary(collection, IndexField.LANGUAGE_FIELD);
        if (languageFacet != null) {
            for (Locale availableLocale : Locale.getAvailableLocales()) {
                for (Locale supportedLocale : ConstellioSpringUtils.getSupportedLocales()) {
                    String languageCode = availableLocale.getLanguage();
                    String languageName = availableLocale.getDisplayLanguage(supportedLocale);
                    languageFacet.setLabelledValue(languageCode, languageName, supportedLocale);
                }
            }
        }
        createFieldFacetIfNecessary(collection, IndexField.FREE_TEXT_TAGGING_FIELD);
    }
    
    protected CollectionFacet createFieldFacetIfNecessary(RecordCollection collection, String fieldName) {
        return createFieldFacetIfNecessary(collection, fieldName, false);
    }

    protected CollectionFacet createFieldFacetIfNecessary(RecordCollection collection, String fieldName, boolean booleanField) {
        CollectionFacet facet;
        if (!collection.hasFieldFacet(fieldName) && collection.getIndexField(fieldName) != null) {
            facet = createFieldFacet(collection, fieldName);
            if (booleanField) {
                createBooleanValueLabels(facet);
            }
        } else {
            facet = null;
        }
        return facet;
    }

    public boolean hasQueryFacet(RecordCollection collection, String fieldName) {
        boolean facetExists = false;
        for (CollectionFacet facet : collection.getCollectionFacets()) {
            if (facet.isQueryFacet()) {
                for (I18NLabel labelledValue : facet.getLabelledValues()) {
                    String facetValue = labelledValue.getKey();
                    if (facetValue.indexOf(fieldName) != -1) {
                        facetExists = true;
                        break;
                    }
                }
            }
        }
        return facetExists;
    }

    protected CollectionFacet createFieldFacet(RecordCollection collection, String fieldName) {
        IndexField indexField = collection.getIndexField(fieldName);
        CollectionFacet facet = new CollectionFacet();
        facet.setFacetType(CollectionFacet.FIELD_FACET);
        facet.setFacetField(indexField);
        for (Locale locale : ConstellioSpringUtils.getSupportedLocales()) {
            String facetName = getLabel(fieldName, locale);
            facet.setName(facetName, locale);
        }
        collection.addCollectionFacet(facet);
        return facet;
    }

    protected CollectionFacet createDateQueryFacet(RecordCollection collection, String fieldName) {
        CollectionFacet dateFacet = new CollectionFacet();
        dateFacet.setFacetType(CollectionFacet.QUERY_FACET);
        for (Locale locale : ConstellioSpringUtils.getSupportedLocales()) {
            String facetName = getLabel(fieldName, locale);
            String oneDayLabel = getLabel("oneDay", locale);
            String sevenDaysLabel = getLabel("sevenDays", locale);
            String thirtyDaysLabel = getLabel("thirtyDays", locale);
            dateFacet.setName(facetName, locale);
            dateFacet.setLabelledValue(fieldName + ":[NOW-001DAYS TO NOW]", oneDayLabel, locale);
            dateFacet.setLabelledValue(fieldName + ":[NOW-007DAYS TO NOW]", sevenDaysLabel, locale);
            dateFacet.setLabelledValue(fieldName + ":[NOW-030DAYS TO NOW]", thirtyDaysLabel, locale);
        }
        collection.addCollectionFacet(dateFacet);
        return dateFacet;
    }
    
    protected CollectionFacet createDateQueryFacetIfNecessary(RecordCollection collection, String fieldName) {
        CollectionFacet facet;
        if (!hasQueryFacet(collection, fieldName) && collection.getIndexField(fieldName) != null) {
            facet = createDateQueryFacet(collection, fieldName);
        } else {
            facet = null;
        }
        return facet;
    }
    
    protected void createBooleanValueLabels(CollectionFacet facet) {
        for (Locale locale : ConstellioSpringUtils.getSupportedLocales()) {
            String trueLabel = getLabel("true", locale);
            String falseLabel = getLabel("false", locale);
            facet.setLabelledValue("true", trueLabel, locale);
            facet.setLabelledValue("false", falseLabel, locale);
        }
    }
    
    protected String getLabel(String key, Locale locale) {
        String label = null;
        List<ResourceBundle> bundles = findBundles(locale);
        for (ResourceBundle bundle : bundles) {
            try {
                label = bundle.getString(key);
                if (label != null) {
                    break;
                }
            } catch (MissingResourceException e) {
                // Ignore exception
            }
        }
        return label;
    }

    protected List<ResourceBundle> findBundles(Locale locale) {
        List<ResourceBundle> bundles = new ArrayList<ResourceBundle>();
        Class<? extends Object> currentClass = getClass();
        while (currentClass != Object.class) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(currentClass.getName(), locale);
                if (bundle != null) {
                    bundles.add(bundle);
                } 
            } catch (MissingResourceException e) {
                //Ignore error
            }
            currentClass = currentClass.getSuperclass();
        }
        return bundles;
    }

    protected abstract void initCustomConnectorInstance(ConnectorInstance connectorInstance);

}
