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
package com.doculibre.constellio.services;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.admin.LukeRequestHandler;

import com.doculibre.constellio.entities.Categorization;
import com.doculibre.constellio.entities.CategorizationRule;
import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.RecordMeta;
import com.doculibre.constellio.entities.RecordTag;
import com.doculibre.constellio.entities.acl.PolicyACLEntry;
import com.doculibre.constellio.entities.acl.RecordPolicyACLEntry;
import com.doculibre.constellio.entities.skos.SkosConcept;
import com.doculibre.constellio.feedprotocol.RFC822DateUtil;
import com.doculibre.constellio.resources.ApplicationResources;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.ResourceBundleUtils;
import com.google.enterprise.connector.spi.Value;

public class IndexFieldServicesImpl extends BaseCRUDServicesImpl<IndexField> implements IndexFieldServices {

    private static final float DEFAULT_TITLE_BOOST = 1.5f;
    private static final float DEFAULT_SEARCHFIELD_BOOST = 1.0f;

    private static final Logger LOGGER = Logger.getLogger(IndexFieldServicesImpl.class.getName());

    public IndexFieldServicesImpl(EntityManager entityManager) {
        super(IndexField.class, entityManager);
    }

    @Override
    public IndexField makePersistent(IndexField entity) {
        return makePersistent(entity, true);
    }

    @Override
    public IndexField makePersistent(IndexField entity, boolean synchronizationRequired) {
        IndexField result = super.makePersistent(entity);
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordCollection collection = entity.getRecordCollection();
        collectionServices.makePersistent(collection, synchronizationRequired);
        return result;
    }

    @Override
    public IndexField makeTransient(IndexField entity) {
        String[] facetIndexFieldProperties = { "facetField", "carrotTitleField", "carrotUrlField",
            "carrotSnippetField" };
        for (String facetIndexFieldProperty : facetIndexFieldProperties) {
            Query queryFacetField = getEntityManager().createQuery(
                "DELETE FROM CollectionFacet WHERE " + facetIndexFieldProperty + "=:indexField");
            queryFacetField.setParameter("indexField", entity);
            queryFacetField.executeUpdate();
        }

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordCollection collection = entity.getRecordCollection();
        collection.getIndexFields().remove(entity);
        collectionServices.makePersistent(collection, true);
        return entity;
    }

    @Override
    public IndexField newUniqueKeyField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField uniqueKeyField = new IndexField();
        uniqueKeyField.setInternalField(true);
        uniqueKeyField.setName(IndexField.UNIQUE_KEY_FIELD);
        uniqueKeyField.setFieldType(stringFieldType);
        uniqueKeyField.setIndexed(true);
        uniqueKeyField.setMultiValued(false);
        uniqueKeyField.setHighlighted(true);
        return uniqueKeyField;
    }

    @Override
    public IndexField newParsedContentField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType textFieldType = fieldTypeServices.get(FieldType.TEXT_FR);
        IndexField parsedContentField = new IndexField();
        parsedContentField.setInternalField(true);
        parsedContentField.setName(IndexField.PARSED_CONTENT_FIELD);
        parsedContentField.setFieldType(textFieldType);
        parsedContentField.setIndexed(true);
        parsedContentField.setMultiValued(false);
        parsedContentField.setHighlighted(true);
        return parsedContentField;
    }

    @Override
    public IndexField newDefaultSearchField(RecordCollection collection) {
        IndexField parsedContentField = collection.getIndexField(IndexField.PARSED_CONTENT_FIELD);
        IndexField defaultSearchField = new IndexField();
        defaultSearchField.setInternalField(true);
        defaultSearchField.setName(IndexField.DEFAULT_SEARCH_FIELD);
        defaultSearchField.setFieldType(parsedContentField.getFieldType());
        defaultSearchField.setIndexed(true);
        defaultSearchField.setMultiValued(true);
        defaultSearchField.setBoost(DEFAULT_SEARCHFIELD_BOOST);
        defaultSearchField.addCopyFieldDestSourceAll();
        defaultSearchField.setHighlighted(true);
        return defaultSearchField;
    }

    @Override
    public IndexField newRecordIdField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField recordIdField = new IndexField();
        recordIdField.setInternalField(true);
        recordIdField.setName(IndexField.RECORD_ID_FIELD);
        recordIdField.setFieldType(stringFieldType);
        recordIdField.setIndexed(true);
        recordIdField.setHighlighted(true);
        return recordIdField;
    }

    @Override
    public IndexField newConnectorInstanceIdField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField connectorInstanceIdField = new IndexField();
        connectorInstanceIdField.setInternalField(true);
        connectorInstanceIdField.setName(IndexField.CONNECTOR_INSTANCE_ID_FIELD);
        connectorInstanceIdField.setFieldType(stringFieldType);
        connectorInstanceIdField.setIndexed(true);
        connectorInstanceIdField.setHighlighted(true);
        return connectorInstanceIdField;
    }

    @Override
    public IndexField newConnectorTypeIdField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField connectorTypeIdField = new IndexField();
        connectorTypeIdField.setInternalField(true);
        connectorTypeIdField.setName(IndexField.CONNECTOR_TYPE_ID_FIELD);
        connectorTypeIdField.setFieldType(stringFieldType);
        connectorTypeIdField.setIndexed(true);
        connectorTypeIdField.setHighlighted(true);
        return connectorTypeIdField;
    }

    @Override
    public IndexField newCollectionIdField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField collectionIdField = new IndexField();
        collectionIdField.setInternalField(true);
        collectionIdField.setName(IndexField.COLLECTION_ID_FIELD);
        collectionIdField.setFieldType(stringFieldType);
        collectionIdField.setIndexed(true);
        collectionIdField.setMultiValued(true);
        collectionIdField.setHighlighted(true);
        return collectionIdField;
    }

    @Override
    public IndexField newFreeTextTaggingField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField taggingField = new IndexField();
        taggingField.setInternalField(true);
        taggingField.setName(IndexField.FREE_TEXT_TAGGING_FIELD);
        taggingField.setFieldType(stringFieldType);
        taggingField.setIndexed(true);
        taggingField.setMultiValued(true);
        taggingField.setHighlighted(true);
        return taggingField;
    }

    @Override
    public IndexField newThesaurusTaggingField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField taggingField = new IndexField();
        taggingField.setInternalField(true);
        taggingField.setName(IndexField.THESAURUS_TAGGING_FIELD);
        taggingField.setFieldType(stringFieldType);
        taggingField.setIndexed(true);
        taggingField.setMultiValued(true);
        taggingField.setHighlighted(true);
        return taggingField;
    }

    @Override
    public IndexField newLanguageField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField languageField = new IndexField();
        languageField.setInternalField(true);
        languageField.setName(IndexField.LANGUAGE_FIELD);
        languageField.setFieldType(stringFieldType);
        languageField.setIndexed(true);
        languageField.setMultiValued(true);
        languageField.setHighlighted(true);
        return languageField;
    }

    @Override
    public IndexField newMimeTypeField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField mimeTypeField = new IndexField();
        mimeTypeField.setInternalField(true);
        mimeTypeField.setName(IndexField.MIME_TYPE_FIELD);
        mimeTypeField.setFieldType(stringFieldType);
        mimeTypeField.setIndexed(true);
        mimeTypeField.setMultiValued(false);
        mimeTypeField.setHighlighted(true);
        return mimeTypeField;
    }

    @Override
    public IndexField newTitleField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType textFieldType = fieldTypeServices.get(FieldType.TEXT_FR);
        IndexField titleField = new IndexField();
        titleField.setInternalField(true);
        titleField.setName(IndexField.TITLE_FIELD);
        titleField.setFieldType(textFieldType);
        titleField.setIndexed(true);
        titleField.setMultiValued(false);
        titleField.setBoost(DEFAULT_TITLE_BOOST);
        titleField.setHighlighted(true);
        return titleField;
    }

    @Override
    public IndexField newUrlField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField urlField = new IndexField();
        urlField.setInternalField(true);
        urlField.setName(IndexField.URL_FIELD);
        urlField.setFieldType(stringFieldType);
        urlField.setIndexed(true);
        urlField.setMultiValued(false);
        urlField.setHighlighted(true);
        return urlField;
    }

    @Override
    public IndexField newDisplayUrlField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField displayUrlField = new IndexField();
        displayUrlField.setInternalField(true);
        displayUrlField.setName(IndexField.DISPLAY_URL_FIELD);
        displayUrlField.setFieldType(stringFieldType);
        displayUrlField.setIndexed(true);
        displayUrlField.setMultiValued(false);
        displayUrlField.setHighlighted(true);
        return displayUrlField;
    }

    @Override
    public IndexField newLastIndexedField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType fieldType = fieldTypeServices.get(FieldType.DATE);
        IndexField lastModifiedField = new IndexField();
        lastModifiedField.setInternalField(true);
        lastModifiedField.setName(IndexField.LAST_INDEXED_FIELD);
        lastModifiedField.setFieldType(fieldType);
        lastModifiedField.setIndexed(true);
        lastModifiedField.setMultiValued(false);
        lastModifiedField.setHighlighted(true);
        return lastModifiedField;
    }

    @Override
    public IndexField newLastModifiedField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType fieldType = fieldTypeServices.get(FieldType.DATE);
        IndexField lastModifiedField = new IndexField();
        lastModifiedField.setInternalField(true);
        lastModifiedField.setName(IndexField.LAST_MODIFIED_FIELD);
        lastModifiedField.setFieldType(fieldType);
        lastModifiedField.setIndexed(true);
        lastModifiedField.setMultiValued(false);
        lastModifiedField.setHighlighted(true);
        return lastModifiedField;
    }

    @Override
    public boolean isRemoveable(IndexField t) {
        return !t.isInternalField();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> suggestValues(IndexField indexField, String text) {
        List<String> values = new ArrayList<String>();
        RecordCollection collection = indexField.getRecordCollection();
        SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
        SolrServer solrServer = solrServices.getSolrServer(collection);
        if (solrServer != null) {
            SolrQuery query = new SolrQuery();
            query.setRequestHandler("/admin/luke");
            query.setParam(CommonParams.FL, indexField.getName());
            query.setParam(LukeRequestHandler.NUMTERMS, "" + 100);

            if (text != null) {
                query.setQuery(indexField.getName() + ":" + text + "*");
            }
            if (collection.isOpenSearch()) {
                query.setParam("openSearchURL", collection.getOpenSearchURL());
            }

            try {
                QueryResponse queryResponse = solrServer.query(query);
                NamedList<Object> fields = (NamedList<Object>) queryResponse.getResponse().get("fields");
                if (fields != null) {
                    NamedList<Object> field = (NamedList<Object>) fields.get(indexField.getName());
                    if (field != null) {
                        NamedList<Object> topTerms = (NamedList<Object>) field.get("topTerms");
                        if (topTerms != null) {
                            for (Map.Entry<String, Object> topTerm : topTerms) {
                                String topTermKey = topTerm.getKey();
                                if (text == null || topTermKey.toLowerCase().startsWith(text.toLowerCase())) {
                                    values.add(topTerm.getKey());
                                }
                            }
                        }
                    }
                }
            } catch (SolrServerException e) {
                throw new RuntimeException(e);
            }
        }
        return values;
    }

    @Override
    public List<String> suggestValues(IndexField field) {
        return suggestValues(field, null);
    }

    @Override
    public IndexField get(String name, RecordCollection collection) {
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("name", name);
        criterias.put("recordCollection", collection);
        IndexField indexField = get(criterias);
        if (indexField == null) {
        	criterias.put("name", name + "*");
        	indexField = get(criterias);
        }
        return indexField;
    }

    @Override
    public List<IndexField> getSortableIndexFields(RecordCollection collection) {
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("sortable", true);
        criterias.put("recordCollection", collection);
        return list(criterias);
    }

    private String getSortFieldTypeNameFor(String fieldTypeName) {
        String sortFieldTypeName;
        if (FieldType.SORTABLE_FIELD_TYPES.contains(fieldTypeName)) {
            // Dates are sortable, as are booleans.
            sortFieldTypeName = null;
        } else if (fieldTypeName.equals(FieldType.INTEGER)) {
            sortFieldTypeName = FieldType.SINT;

        } else if (fieldTypeName.equals(FieldType.DOUBLE)) {
            sortFieldTypeName = FieldType.SDOUBLE;

        } else if (fieldTypeName.equals(FieldType.LONG)) {
            sortFieldTypeName = FieldType.SLONG;

        } else if (fieldTypeName.equals(FieldType.FLOAT)) {
            sortFieldTypeName = FieldType.SFLOAT;

        } else if (fieldTypeName.equals(FieldType.STRING)) {
            sortFieldTypeName = FieldType.ALPHA_ONLY_SORT;

        } else if (fieldTypeName.equals(FieldType.TEXT)) {
            sortFieldTypeName = FieldType.ALPHA_ONLY_SORT;

        } else {
            sortFieldTypeName = FieldType.ALPHA_ONLY_SORT;
        }
        return sortFieldTypeName;
    }

    @Override
    public IndexField getSortFieldOf(IndexField indexField) {
        String fieldTypeName = indexField.getFieldType().getName();
        String sortFieldTypeName = getSortFieldTypeNameFor(fieldTypeName);
        if (sortFieldTypeName == null) {
            return indexField;
        }
        FieldTypeServices fieldTypeClassServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType alphaOnlySortType = fieldTypeClassServices.get(sortFieldTypeName);
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("recordCollection", indexField.getRecordCollection());
        criterias.put("fieldType", alphaOnlySortType);

        IndexField zeSortField = null;
        findSortField: for (IndexField aSortField : list(criterias)) {
            for (CopyField copyField : aSortField.getCopyFieldsDest()) {
                if (copyField.getIndexFieldSource().equals(indexField)) {
                    zeSortField = aSortField;
                    break findSortField;
                }
            }
        }
        return zeSortField;
    }

    @Override
    public IndexField newSortFieldFor(IndexField indexField) {
        String fieldTypeName = indexField.getFieldType().getName();
        String sortFieldTypeName = getSortFieldTypeNameFor(fieldTypeName);
        FieldTypeServices fieldTypeClassServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType alphaOnlySortType = fieldTypeClassServices.get(sortFieldTypeName);
        IndexField sortField = new IndexField();
        CopyField copyField = new CopyField();
        copyField.setIndexFieldSource(indexField);
        copyField.setIndexFieldDest(sortField);
        indexField.addCopyFieldSource(copyField);
        sortField.addCopyFieldDest(copyField);
        sortField.setIndexed(true);
        sortField.setName(indexField.getName() + "_sort");
        sortField.setFieldType(alphaOnlySortType);
        sortField.setRecordCollection(indexField.getRecordCollection());
        makePersistent(sortField);
        makePersistent(indexField);
        fieldTypeClassServices.makePersistent(alphaOnlySortType);
        RecordCollectionServices recordCollectionServices = ConstellioSpringUtils
            .getRecordCollectionServices();
        recordCollectionServices.makePersistent(indexField.getRecordCollection(), true);
        return sortField;
    }

    @Override
    public IndexField newPublicRecordField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType booleanFieldType = fieldTypeServices.get(FieldType.BOOLEAN);
        IndexField publicRecordField = new IndexField();
        publicRecordField.setInternalField(true);
        publicRecordField.setName(IndexField.PUBLIC_RECORD_FIELD);
        publicRecordField.setFieldType(booleanFieldType);
        publicRecordField.setIndexed(true);
        publicRecordField.setHighlighted(true);
        return publicRecordField;
    }
    
    @Override
    public IndexField newParentPathField(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField parentPathField = new IndexField();
        parentPathField.setInternalField(true);
        parentPathField.setName(IndexField.PARENT_PATH_FIELD);
        parentPathField.setFieldType(stringFieldType);
        parentPathField.setIndexed(true);
        parentPathField.setMultiValued(false);
        parentPathField.setHighlighted(true);
        return parentPathField;
    }
    
    @Override
    public IndexField newMd5Field(RecordCollection collection) {
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField parentPathField = new IndexField();
        parentPathField.setInternalField(true);
        parentPathField.setName(IndexField.MD5);
        parentPathField.setFieldType(stringFieldType);
        parentPathField.setIndexed(true);
        parentPathField.setMultiValued(true);
        parentPathField.setHighlighted(true);
        return parentPathField;
    }

    @Override
    public List<Object> extractFieldValues(Record record, IndexField indexField) {
        return extractFieldValues(record, indexField, true, new HashMap<IndexField, List<Object>>());
    }

    private List<Object> extractFieldValues(
    		Record record, 
    		IndexField indexField, 
    		boolean addCategorizationValues, 
    		Map<IndexField, List<Object>> extractedValues) {
        List<Object> fieldValuesList = extractedValues.get(indexField);
        if (fieldValuesList == null) {
            Set<Object> fieldValues = new HashSet<Object>();
            ConnectorInstance connectorInstance = record.getConnectorInstance();
            ConnectorType connectorType = connectorInstance.getConnectorType();
            RecordCollection indexedCollection = indexField.getRecordCollection();
            String indexFieldName = indexField.getName();
            if (IndexField.CONNECTOR_INSTANCE_ID_FIELD.equals(indexFieldName)) {
                fieldValues.add(connectorInstance.getId());
            } else if (IndexField.COLLECTION_ID_FIELD.equals(indexFieldName)) {
                RecordCollection collection = record.getConnectorInstance().getRecordCollection();
                fieldValues.add(collection.getId());
                if (collection.isIncludedInFederation()) {
                    FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
                    for (RecordCollection ownerCollection : federationServices.listOwnerCollections(collection)) {
                        fieldValues.add(ownerCollection.getId());
                    }
                }
            } else if (IndexField.CONNECTOR_TYPE_ID_FIELD.equals(indexFieldName)) {
                fieldValues.add(connectorType.getId());
            } else if (IndexField.RECORD_ID_FIELD.equals(indexFieldName)) {
                fieldValues.add(record.getId());
            } else if (IndexField.UNIQUE_KEY_FIELD.equals(indexFieldName)) {
                fieldValues.add(record.getUrl());
            } else if (IndexField.PARSED_CONTENT_FIELD.equals(indexFieldName)) {
                fieldValues.add(record.getParsedContent().getContent());
            } else if (IndexField.LANGUAGE_FIELD.equals(indexFieldName)) {
                fieldValues.add(record.getLang());
            } else if (IndexField.MIME_TYPE_FIELD.equals(indexFieldName)) {
                fieldValues.add(record.getMimetype());
            } else if (IndexField.URL_FIELD.equals(indexFieldName)) {
                fieldValues.add(record.getUrl());
            } else if (IndexField.DISPLAY_URL_FIELD.equals(indexFieldName)) {
                fieldValues.add(record.getDisplayUrl());
            } else if (IndexField.LAST_INDEXED_FIELD.equals(indexFieldName)) {
                fieldValues.add(record.getLastIndexed());
            } else if (IndexField.LAST_MODIFIED_FIELD.equals(indexFieldName)) {
                fieldValues.add(record.getLastModified());
            } else if (IndexField.TITLE_FIELD.equals(indexFieldName)) {
                if (record.getDisplayTitle() != null) {
                    fieldValues.add(record.getDisplayTitle());
                }
            } else if (IndexField.FREE_TEXT_TAGGING_FIELD.equals(indexFieldName)) {
                for (RecordTag recordTag : record.getRecordTags()) {
                    if (!recordTag.isExcluded()) {
                        FreeTextTag freeTextTag = recordTag.getFreeTextTag();
                        if (freeTextTag != null) {
                            String tagValue = freeTextTag.getFreeText();
                            fieldValues.add(tagValue);
                        }
                    }
                }
            } else if (IndexField.THESAURUS_TAGGING_FIELD.equals(indexFieldName)) {
                for (RecordTag recordTag : record.getRecordTags()) {
                    if (!recordTag.isExcluded()) {
                        for (Locale locale : ConstellioSpringUtils.getSupportedLocales()) {
                            SkosConcept skosConcept = recordTag.getSkosConcept();
                            if (skosConcept != null) {
                                String tagValue = skosConcept.getPrefLabel(locale);
                                fieldValues.add(tagValue);
                            }
                        }
                    }
                }
            } else if (IndexField.PUBLIC_RECORD_FIELD.equals(indexFieldName)) {
                boolean publicRecord;
                if (!record.isPublicRecord() || !record.getRecordPolicyACLEntries().isEmpty()) {
                    publicRecord = false;
                } else {
                    publicRecord = true;
                }
                fieldValues.add(publicRecord);
            } else {
                Collection<ConnectorInstanceMeta> metas = indexField.getConnectorInstanceMetas(connectorInstance);
                for (ConnectorInstanceMeta connectorInstanceMeta : metas) {
                    String metaName = connectorInstanceMeta.getName();
                    List<RecordMeta> recordsMetas = record.getMetas(metaName);
                    for (RecordMeta recordMeta : recordsMetas) {
                        String metaContent = recordMeta.getContent();
                        fieldValues.add(metaContent);
                    }
                }
            }

            if (addCategorizationValues) {
                // Add categorization values
                for (Categorization categorization : indexField.getCategorizations()) {
                    for (CategorizationRule categorizationRule : categorization.getCategorizationRules()) {
                        IndexField sourceIndexField = categorizationRule.getIndexField();
                        if (sourceIndexField != null) {
                            // FIXME Recursive call problematic if two fields feed each other
                            List<Object> sourceIndexFieldValues = extractFieldValues(record, sourceIndexField, true, extractedValues);
                            if (sourceIndexFieldValues != null) {
                                for (Object sourceIndexFieldValue : sourceIndexFieldValues) {
                                    String matchRegexp = categorizationRule.getMatchRegexp();
                                    Pattern pattern = Pattern.compile(matchRegexp, Pattern.CASE_INSENSITIVE);
                                    Matcher matcher = pattern.matcher(sourceIndexFieldValue.toString());
                                    if (matcher.find()) {
                                        Set<String> indexedValues = categorizationRule.getMatchRegexpIndexedValues();
                                        fieldValues.addAll(indexedValues);
                                    }
                                }
                            }
                        } else {
                        	String msg = "Null index field for categorization rule id : " + categorizationRule.getId();
                        	LOGGER.severe(msg);
//                        	throw new RuntimeException(msg);
                        }
                    }
                }
            }

            for (CopyField copyField : indexField.getCopyFieldsDest()) {
                IndexField indexFieldSource = copyField.getIndexFieldSource();
                if (indexFieldSource != null  && !isCopyFieldConflict(indexField, indexFieldSource)) {
                    // FIXME Recursive call problematic if two fields feed each other
                    List<Object> indexFieldSourceValues = extractFieldValues(record, indexFieldSource);
                    fieldValues.addAll(indexFieldSourceValues);
                } else if (copyField.isSourceAllFields()) {
                    if (!indexField.isParsedContentField()) {
                        fieldValues.add(record.getParsedContent().getContent());
                    }
                    for (IndexField otherIndexField : indexedCollection.getIndexFields()) {
                        if (!isCopyFieldConflict(indexField, otherIndexField) && 
                        		!otherIndexField.equals(indexField) && 
                        		!otherIndexField.isParsedContentField()) {
                            // FIXME Recursive call problematic if two fields feed each other
                            List<Object> indexFieldSourceValues = extractFieldValues(record, otherIndexField);
                            fieldValues.addAll(indexFieldSourceValues);
                        }
                    }
                }
            }
            fieldValuesList = new ArrayList<Object>(fieldValues);
            extractedValues.put(indexField, fieldValuesList);
        }

        return fieldValuesList;
    }

    private boolean isCopyFieldConflict(IndexField indexField1, IndexField indexField2) {
    	boolean conflict;
    	boolean indexField1FeedsIndexField2 = false;
    	boolean indexField2FeedsIndexField1 = false;
    	
    	// 1) Obtenir la liste des copy fields alimentant indexField1
    	for (CopyField indexField1CopyField : indexField1.getCopyFieldsDest()) {
        	// 2) Vérifier si indexField2 fait partie des champs alimentant indexField2
    		if (indexField1CopyField.isSourceAllFields()) {
    			indexField2FeedsIndexField1 = true;
    		} else {
    			IndexField indexField1SourceField = indexField1CopyField.getIndexFieldSource();
    			indexField2FeedsIndexField1 = indexField1SourceField.equals(indexField2);
    		}
			if (indexField2FeedsIndexField1) {
				break;
			}
    	}
    	
    	if (indexField2 != null) {
        	// 3) Obtenir la liste des copy fields alimentant indexField2
        	for (CopyField indexField2CopyField : indexField2.getCopyFieldsDest()) {
            	// 4) Vérifier si indexField1 fait partie des champs alimentant indexField1
        		if (indexField2CopyField.isSourceAllFields()) {
        			indexField1FeedsIndexField2 = true;
        		} else {
        			IndexField indexField1SourceField = indexField2CopyField.getIndexFieldSource();
        			indexField1FeedsIndexField2 = indexField1SourceField.equals(indexField1);
        		}
    			if (indexField1FeedsIndexField2) {
    				break;
    			}
        	}
        	conflict = indexField1FeedsIndexField2 && indexField2FeedsIndexField1;
    	} else {
        	conflict = false;
    	}
    	
    	return conflict;
    }
    
    @Override
    public Object extractFieldValue(Record record, IndexField indexField) {
        List<Object> fieldValues = extractFieldValues(record, indexField);
        return !fieldValues.isEmpty() ? fieldValues.get(0) : null;
    }

    @Override
    public List<IndexField> getIndexFieldAlimentedBy(String metaName, RecordCollection collection) {
        List<IndexField> indexFields = new ArrayList<IndexField>();

        for (IndexField indexField : collection.getIndexFields()) {
            if (indexField.getMetaNames().contains(metaName)) {
                indexFields.add(indexField);
            }

        }

        List<IndexField> destFields = new ArrayList<IndexField>();
        for (IndexField indexField : indexFields) {
            for (IndexField alimentedField : getAlimentedField(indexField)) {
                if (!indexFields.contains(alimentedField) && !destFields.contains(alimentedField)) {
                    destFields.add(alimentedField);
                }
            }
        }
        indexFields.addAll(destFields);

        return indexFields;
    }

    private List<IndexField> getAlimentedField(IndexField indexField) {
        List<IndexField> alimentedFields = new ArrayList<IndexField>();
        alimentedFields.add(indexField);

        for (CopyField copyField : indexField.getCopyFieldsDest()) {
            for (IndexField destField : getAlimentedField(copyField.getIndexFieldDest())) {
                if (!alimentedFields.contains(destField)) {
                    alimentedFields.add(destField);
                }
            }
        }

        for (IndexField aField : indexField.getRecordCollection().getIndexFields()) {
            if (!alimentedFields.contains(aField)) {
                for (CopyField sourceCopyField : aField.getCopyFieldsSource()) {
                    if (sourceCopyField.isSourceAllFields()) {
                        alimentedFields.add(aField);
                        break;
                    }
                }
            }
        }

        return alimentedFields;
    }

    @Override
    public IndexField getFirstIndexFieldAlimentedOnlyBy(String metaName, RecordCollection collection) {
        for (IndexField indexField : collection.getIndexFields()) {
            if (indexField.getMetaNames().size() == 1 && indexField.getMetaNames().contains(metaName)) {
                return indexField;
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getDefaultLabelledValues(IndexField indexField, Locale locale) {
        Map<String, String> labels = new HashMap<String, String>();
        String indexFieldName = indexField.getName();
        if (IndexField.COLLECTION_ID_FIELD.equals(indexFieldName)) {
            RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
            for (RecordCollection collection : collectionServices.list()) {
                Locale displayLocale = collection.getDisplayLocale(locale);
                labels.put("" + collection.getId(), collection.getTitle(displayLocale));
            }
        } else if (IndexField.CONNECTOR_INSTANCE_ID_FIELD.equals(indexFieldName)) {
            ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils
                .getConnectorInstanceServices();
            for (ConnectorInstance connectorInstance : connectorInstanceServices.list()) {
                labels.put("" + connectorInstance.getId(), connectorInstance.getDisplayName());
            }
        } else if (IndexField.CONNECTOR_TYPE_ID_FIELD.equals(indexFieldName)) {
            ConnectorTypeServices connectorTypeServices = ConstellioSpringUtils.getConnectorTypeServices();
            for (ConnectorType connectorType : connectorTypeServices.list()) {
                labels.put("" + connectorType.getId(), connectorType.getName());
            }
        } else if (IndexField.LANGUAGE_FIELD.equals(indexFieldName)) {
            for (Locale availableLocale : Locale.getAvailableLocales()) {
                labels.put(availableLocale.getLanguage(), StringUtils.capitalize(availableLocale
                    .getDisplayLanguage(locale)));
            }
        } else if (IndexField.MIME_TYPE_FIELD.equals(indexFieldName)) {
            // Source : http://www.w3schools.com/media/media_mimeref.asp
            String prefix = "mimeType.";
            Set<String> mimeTypeResourceKeys = ResourceBundleUtils
                .getKeys(prefix, ApplicationResources.class);
            for (String mimeTypeResourceKey : mimeTypeResourceKeys) {
                String mimeType = mimeTypeResourceKey.substring(prefix.length());
                String mimeTypeLabel = ResourceBundleUtils.getString(mimeTypeResourceKey, locale,
                    ApplicationResources.class);
                labels.put(mimeType, mimeTypeLabel);
            }
        }
        return labels;
    }

	@Override
	public void populateSolrDoc(SolrInputDocument doc, Record record, RecordCollection collection) {
		Map<IndexField, List<Object>> extractedValues = new HashMap<IndexField, List<Object>>();
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        ACLServices aclServices = ConstellioSpringUtils.getACLServices();
        if (record.isComputeACLEntries()) {
            record.getRecordPolicyACLEntries().clear();
            List<PolicyACLEntry> aclEntries = aclServices.computeACLEntries(record);
            for (PolicyACLEntry entry : aclEntries) {
                RecordPolicyACLEntry recordEntry = new RecordPolicyACLEntry();
                recordEntry.setRecord(record);
                recordEntry.setEntry(entry);
                record.addRecordPolicyACLEntry(recordEntry);
            }
            record.setComputeACLEntries(false);
        }
        Float boost = recordServices.computeBoost(record);
        if (boost != null) {
            doc.setDocumentBoost(boost);
        }
        for (String internalIndexFieldName : IndexField.INTERNAL_FIELDS) {
            // Special case
            if (!IndexField.LAST_INDEXED_FIELD.equals(internalIndexFieldName)) {
                addField(doc, record, internalIndexFieldName, collection, extractedValues);
            }
        }
        addAutocompleteFields(doc, record, collection);
        for (IndexField indexField : collection.getIndexFields()) {
            if (!indexField.isInternalField() && !indexField.isDynamicField()) {
                addFieldIfPossible(doc, record, indexField.getName(), collection, extractedValues);
            }
        }
//
//        Date lastIndexed = new Date();
//        doc.addField(IndexField.LAST_INDEXED_FIELD, lastIndexed);
	}

    private void addAutocompleteFields(SolrInputDocument doc, Record record, RecordCollection collection) {
        AutocompleteServices autocompleteServices = ConstellioSpringUtils.getAutocompleteServices();
        for (IndexField indexField : collection.getIndexFields()) {
            if (indexField.isAutocompleted()) {
                autocompleteServices.onDocumentAddToAutoCompleteField(doc, indexField, record);
            }
        }
    }

    private void addField(
    		SolrInputDocument doc, 
    		Record record, 
    		String indexFieldName, 
    		RecordCollection collection, 
    		Map<IndexField, List<Object>> extractedValues) {
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        IndexField indexField = collection.getIndexField(indexFieldName);
        if (indexField != null) {
            List<Object> fieldValues = extractFieldValues(record, indexField);
            if (fieldValues != null && fieldValues.size() > 0) {
                Object fieldValue = correctDate(fieldValues.get(0));
                Float fieldBoost = recordServices.computeFieldBoost(record, indexField);
                if (fieldBoost == null) {
                    doc.addField(indexFieldName, fieldValue);
                } else {
                    doc.addField(indexFieldName, fieldValue, fieldBoost);
                }
                for (int i = 1; i < fieldValues.size(); i++) {
                    fieldValue = correctDate(fieldValues.get(i));
                    doc.addField(indexFieldName, fieldValue);
                }
            }
        } else {
        	String msg = "Missing index field : \"" + indexFieldName + "\" for collection id :" + collection.getId();
        	throw new RuntimeException(msg);
        }
    }

    private Object correctDate(Object mayBeDate) {
        try {
            Calendar calendar = Value.iso8601ToCalendar(String.valueOf(mayBeDate));
            return DateUtil.getThreadLocalDateFormat().format(calendar.getTime());
        } catch (ParseException e) {
            // Try with RFC
            try {
                Date contentAsDate = RFC822DateUtil.parse(String.valueOf(mayBeDate));
                // Convert RFC822 (Google connector dates) to Solr dates
                return DateUtil.getThreadLocalDateFormat().format(contentAsDate);
            } catch (ParseException ee) {
                // Ignored or is not a date
                return mayBeDate;
            }
        }
    }

    private void addFieldIfPossible(
    		SolrInputDocument doc, 
    		Record record, 
    		String indexFieldName, 
    		RecordCollection collection, 
    		Map<IndexField, List<Object>> extractedValues) {
        IndexField indexField = collection.getIndexField(indexFieldName);
        List<Object> fieldValues = extractFieldValues(record, indexField);
        if (indexField.isMultiValued() || fieldValues.size() == 1) {
            addField(doc, record, indexFieldName, collection, extractedValues);
        }
    }

	@Override
	public IndexField newAuthmethodField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType fieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_AUTHMETHOD_FIELD);
        indexField.setFieldType(fieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(false);
        return indexField;
		
	}

	@Override
	public IndexField newBoostField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType fieldType = fieldTypeServices.get(FieldType.DOUBLE);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_BOOST_FIELD);
        indexField.setFieldType(fieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(false);
        return indexField;
	}

	@Override
	public IndexField newLastAutomaticTaggingField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType fieldType = fieldTypeServices.get(FieldType.DATE);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_LAST_AUTOMATIC_TAGGING_FIELD);
        indexField.setFieldType(fieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(false);
        return indexField;
	}

	@Override
	public IndexField newLastFetchedField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType fieldType = fieldTypeServices.get(FieldType.DATE);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_LAST_FETCHED_FIELD);
        indexField.setFieldType(fieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(false);
        return indexField;
	}

	@Override
	public IndexField newComputeACLEntriesField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType fieldType = fieldTypeServices.get(FieldType.BOOLEAN);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_COMPUTE_ACL_ENTRIES_FIELD);
        indexField.setFieldType(fieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(false);
        return indexField;
	}

	@Override
	public IndexField newDeletedField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType fieldType = fieldTypeServices.get(FieldType.BOOLEAN);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_DELETED_FIELD);
        indexField.setFieldType(fieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(false);
        return indexField;
	}

	@Override
	public IndexField newExcludedField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType fieldType = fieldTypeServices.get(FieldType.BOOLEAN);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_EXCLUDED_FIELD);
        indexField.setFieldType(fieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(false);
        return indexField;
	}

	@Override
	public IndexField newUpdateIndexField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType booleanFieldType = fieldTypeServices.get(FieldType.BOOLEAN);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_UPDATE_INDEX_FIELD);
        indexField.setFieldType(booleanFieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(false);
        return indexField;
	}

	@Override
	public IndexField newAclEntryField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_ACL_ENTRY_FIELD);
        indexField.setFieldType(stringFieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(true);
        return indexField;
	}

	@Override
	public IndexField newDTField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType fieldType = fieldTypeServices.get(FieldType.DATE);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_DT_FIELD);
        indexField.setFieldType(fieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(true);
        indexField.setDynamicField(true);
        return indexField;
	}

	@Override
	public IndexField newBLField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType booleanFieldType = fieldTypeServices.get(FieldType.BOOLEAN);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_BL_FIELD);
        indexField.setFieldType(booleanFieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(true);
        indexField.setDynamicField(true);
        return indexField;
	}

	@Override
	public IndexField newMetaContentField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_META_CONTENT_FIELD + "*");
        indexField.setFieldType(stringFieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(true);
        indexField.setDynamicField(true);
        return indexField;
	}

	@Override
	public IndexField newMetaExternalField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_META_EXTERNAL_FIELD + "*");
        indexField.setFieldType(stringFieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(true);
        indexField.setDynamicField(true);
        return indexField;
	}

	@Override
	public IndexField newRecordTagFreeField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_RECORD_TAG_FREE_FIELD + "*");
        indexField.setFieldType(stringFieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(true);
        indexField.setDynamicField(true);
        return indexField;
	}

	@Override
	public IndexField newRecordTagSkosField(RecordCollection collection) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
        IndexField indexField = new IndexField();
        indexField.setInternalField(true);
        indexField.setName(IndexField.DB_RECORD_TAG_SKOS_FIELD + "*");
        indexField.setFieldType(stringFieldType);
        indexField.setIndexed(true);
        indexField.setMultiValued(true);
        indexField.setDynamicField(true);
        return indexField;
	}
    
}
