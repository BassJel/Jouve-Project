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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ObjectUtils;

import com.doculibre.constellio.entities.Categorization;
import com.doculibre.constellio.entities.CategorizationRule;
import com.doculibre.constellio.entities.CollectionFacet;
import com.doculibre.constellio.entities.CollectionFederation;
import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.entities.CredentialGroup;
import com.doculibre.constellio.entities.FederationRecordDeletionRequired;
import com.doculibre.constellio.entities.FederationRecordIndexingRequired;
import com.doculibre.constellio.entities.I18NLabel;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.EqualityUtils;

public class FederationServicesImpl extends BaseCRUDServicesImpl<CollectionFederation> implements
    FederationServices {

    public FederationServicesImpl(EntityManager entityManager) {
        super(CollectionFederation.class, entityManager);
    }

    @Override
    public IndexField copy(IndexField indexField, RecordCollection federationOwner) {
        IndexField copy = new IndexField();
        federationOwner.addIndexField(copy);
        copy.setAnalyzer(indexField.getAnalyzer());
        copy.setBoost(indexField.getBoost());
        copy.setBoostDismax(indexField.getBoostDismax());
        copy.setDynamicField(indexField.isDynamicField());
        copy.setFieldType(indexField.getFieldType());
        copy.setHighlighted(indexField.isHighlighted());
        copy.setIndexed(indexField.isIndexed());
        copy.setInternalField(indexField.isInternalField());
        copy.setMultiValued(indexField.isMultiValued());
        copy.setName(indexField.getName());
        copy.setSortable(indexField.isSortable());

        for (Categorization categorization : indexField.getCategorizations()) {
            Categorization categorizationCopy = new Categorization();
            federationOwner.addCategorization(categorizationCopy);
            IndexField categorizationIndexField = categorization.getIndexField();
            IndexField categorizationCopyIndexField = federationOwner.getIndexField(categorizationIndexField
                .getName());
            if (categorizationCopyIndexField == null) {
                // Recursive call
                categorizationCopyIndexField = copy(categorizationIndexField, federationOwner);
            }
            categorizationCopy.setIndexField(categorizationCopyIndexField);
            categorizationCopy.setName(categorization.getName());
            federationOwner.addCategorization(categorizationCopy);
            for (CategorizationRule categorizationRule : categorization.getCategorizationRules()) {
                CategorizationRule categorizationRuleCopy = new CategorizationRule();
                categorizationCopy.addCategorizationRule(categorizationRuleCopy);
                IndexField categorizationRuleIndexField = categorizationRule.getIndexField();
                IndexField categorizationRuleCopyIndexField = federationOwner
                    .getIndexField(categorizationRuleIndexField.getName());
                if (categorizationRuleCopyIndexField == null) {
                    // Recursive call
                    categorizationRuleCopyIndexField = copy(categorizationRuleIndexField, federationOwner);
                }
                categorizationRuleCopy.setIndexField(categorizationRuleCopyIndexField);
                categorizationRuleCopy.setMatchRegexp(categorizationRule.getMatchRegexp());
                categorizationRuleCopy.getMatchRegexpIndexedValues().addAll(
                    categorizationRule.getMatchRegexpIndexedValues());
            }
        }

        for (ConnectorInstanceMeta meta : indexField.getConnectorInstanceMetas()) {
            copy.addConnectorInstanceMeta(meta);
        }

        // No need to copy copyFieldsSource
        for (CopyField copyFieldDest : indexField.getCopyFieldsDest()) {
            CopyField copyFieldDestCopy = new CopyField();
            copy.addCopyFieldDest(copyFieldDestCopy);
            copyFieldDestCopy.setIndexFieldSource(copyFieldDest.getIndexFieldSource());
            copyFieldDestCopy.setMaxChars(copyFieldDest.getMaxChars());
            copyFieldDestCopy.setSourceAllFields(copyFieldDest.isSourceAllFields());
        }

        for (I18NLabel label : indexField.getLabels()) {
            for (Locale locale : ConstellioSpringUtils.getSupportedLocales()) {
                copy.setLabel(label.getKey(), label.getValue(locale), locale);
            }
        }

        for (I18NLabel labelledValue : indexField.getLabelledValues()) {
            for (Locale locale : ConstellioSpringUtils.getSupportedLocales()) {
                copy.setLabelledValue(labelledValue.getKey(), labelledValue.getValue(locale), locale);
            }
        }
        
        return copy;
    }

    @Override
    public int countTraversedRecords(RecordCollection federationOwner) {
        StatusServices statusServices = ConstellioSpringUtils.getStatusServices();
        List<RecordCollection> federationCollections = listIncludedCollections(federationOwner);
        federationCollections.add(federationOwner);
        int count = 0;
        for (RecordCollection federationCollection : federationCollections) {
            count += statusServices.countTraversedRecords(federationCollection);
        }
        return count;
    }

    @Override
    public boolean isConflict(String indexFieldName, RecordCollection federationOwner,
        RecordCollection includedCollection) {
        boolean conflict = false;

        IndexField indexField = includedCollection.getIndexField(indexFieldName);
        IndexField indexField2 = federationOwner.getIndexField(indexFieldName);
        if (indexField != null && indexField2 != null) {
            if ((indexField.getAnalyzer() != null || indexField2.getAnalyzer() != null)
                && !ObjectUtils.equals(indexField.getAnalyzer(), indexField2.getAnalyzer())) {
                conflict = true;
            }
            if ((indexField.getBoost() != null || indexField2.getBoost() != null)
                && !ObjectUtils.equals(indexField.getBoost(), indexField2.getBoost())) {
                conflict = true;
            }
            if ((indexField.getBoostDismax() != null || indexField2.getBoostDismax() != null)
                    && !ObjectUtils.equals(indexField.getBoostDismax(), indexField2.getBoostDismax())) {
                    conflict = true;
            }
            if (indexField.isDynamicField() != indexField2.isDynamicField()) {
                conflict = true;
            }
            if ((indexField.getFieldType() != null || indexField2.getFieldType() != null)
                && !ObjectUtils.equals(indexField.getFieldType(), indexField2.getFieldType())) {
                conflict = true;
            }
            if (indexField.isHighlighted() != indexField2.isHighlighted()) {
                conflict = true;
            }
            if (indexField.isIndexed() != indexField2.isIndexed()) {
                conflict = true;
            }
            if (indexField.isInternalField() != indexField2.isInternalField()) {
                conflict = true;
            }
            if (indexField.isMultiValued() != indexField2.isMultiValued()) {
                conflict = true;
            }
            if ((indexField.getName() != null || indexField2.getName() != null)
                && !ObjectUtils.equals(indexField.getName(), indexField2.getName())) {
                conflict = true;
            }
            if (indexField.isSortable() != indexField2.isSortable()) {
                conflict = true;
            }
        }

        return conflict;
    }

    @Override
    public List<ConnectorInstance> listConnectors(RecordCollection federationOwner) {
        List<ConnectorInstance> connectors = new ArrayList<ConnectorInstance>();
        List<RecordCollection> federationCollections = listIncludedCollections(federationOwner);
        federationCollections.add(federationOwner);
        for (RecordCollection federationCollection : federationCollections) {
            connectors.addAll(federationCollection.getConnectorInstances());
        }
        return connectors;
    }

    @Override
    public List<CredentialGroup> listCredentialGroups(RecordCollection federationOwner) {
        List<CredentialGroup> credentialGroups = new ArrayList<CredentialGroup>();
        List<RecordCollection> federationCollections = listIncludedCollections(federationOwner);
        federationCollections.add(federationOwner);
        for (RecordCollection federationCollection : federationCollections) {
            credentialGroups.addAll(federationCollection.getCredentialGroups());
        }
        return credentialGroups;
    }

    @Override
    public List<RecordCollection> listIncludedCollections(RecordCollection federationOwner) {
        List<RecordCollection> allIncludedCollections = new ArrayList<RecordCollection>();
        for (RecordCollection includedCollection : federationOwner.getIncludedCollections()) {
            if (!allIncludedCollections.contains(includedCollection)) {
                allIncludedCollections.add(includedCollection);
                addIncludedCollections(includedCollection, allIncludedCollections);
            }
        }
        return allIncludedCollections;
    }

    private static void addIncludedCollections(RecordCollection ownerCollection,
        List<RecordCollection> allIncludedCollections) {
        for (RecordCollection includedCollection : ownerCollection.getIncludedCollections()) {
            if (!allIncludedCollections.contains(includedCollection)) {
                allIncludedCollections.add(includedCollection);
                // Recursive call
                addIncludedCollections(includedCollection, allIncludedCollections);
            }
        }
    }

    @Override
    public List<RecordCollection> listOwnerCollections(RecordCollection includedCollection) {
        List<RecordCollection> allOwnerCollections = new ArrayList<RecordCollection>();
        for (RecordCollection ownerCollection : includedCollection.getOwnerCollections()) {
            if (!allOwnerCollections.contains(ownerCollection)) {
                allOwnerCollections.add(ownerCollection);
                addOwnerCollections(ownerCollection, allOwnerCollections);
            }
        }
        return allOwnerCollections;
    }

    private static void addOwnerCollections(RecordCollection includedCollection,
        List<RecordCollection> allOwnerCollections) {
        for (RecordCollection ownerCollection : includedCollection.getOwnerCollections()) {
            if (!allOwnerCollections.contains(ownerCollection)) {
                allOwnerCollections.add(ownerCollection);
                // Recursive call
                addOwnerCollections(ownerCollection, allOwnerCollections);
            }
        }
    }

    @Override
    public CollectionFederation makePersistent(CollectionFederation entity) {
        boolean add = entity.getId() == null;
        if (!add) {
            throw new RuntimeException("Entity cannot be updated after creation");
        }
        CollectionFederation result = super.makePersistent(entity);
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        RecordCollection federationOwner = entity.getOwnerCollection();
        RecordCollection includedCollection = entity.getIncludedCollection();

        boolean indexFieldAdded = false;
        // Should always be the case
        for (IndexField indexField : includedCollection.getIndexFields()) {
            if (federationOwner.getIndexField(indexField.getName()) == null) {
                copy(indexField, federationOwner);
                indexFieldAdded = true;
            }
        }

        boolean facetCreated = false;
        for (CollectionFacet collectionFacet : includedCollection.getCollectionFacets()) {
            if (!existsInFederationOwner(collectionFacet, federationOwner)) {
                copy(collectionFacet, federationOwner);
                facetCreated = true;
            }
        }

        federationOwner.addIncludedCollectionFederation(entity);

        boolean synchronizationRequired = indexFieldAdded || facetCreated;
        collectionServices.makePersistent(federationOwner, synchronizationRequired);
        if (recordServices.count(federationOwner) > 0) {
            recordServices.markRecordsForUpdateIndex(federationOwner);
        }
        List<RecordCollection> includedSubcollections = listIncludedCollections(federationOwner);
        for (RecordCollection includedSubcollection : includedSubcollections) {
            if (recordServices.count(includedSubcollection) > 0) {
                recordServices.markRecordsForUpdateIndex(includedSubcollection);
            }
        }
        return result;
    }

    private boolean existsInFederationOwner(CollectionFacet includedFacet, RecordCollection federationOwner) {
        boolean result = false;
        for (CollectionFacet ownerFacet : federationOwner.getCollectionFacets()) {
            IndexField indexField1 = includedFacet.getFacetField();
            IndexField indexField2 = ownerFacet.getFacetField();
            if (matches(indexField1, indexField2)) {
                result = true;
                break;
            } else if (indexField1 == null && indexField2 == null) {
                if (includedFacet.getFacetType().equals(ownerFacet.getFacetType())) {
                    if (includedFacet.isQueryFacet()) {
                        boolean allMatchingQueries = true;
                        loop2: for (I18NLabel labelledValue : includedFacet.getLabelledValues()) {
                            String includedFacetValue = labelledValue.getKey();
                            boolean valueExists = false;
                            loop3: for (I18NLabel ownerLabelledValue : ownerFacet.getLabelledValues()) {
                                String ownerFacetValue = ownerLabelledValue.getKey();
                                if (ownerFacetValue.equals(includedFacetValue)) {
                                    valueExists = true;
                                    break loop3;
                                }
                            }
                            if (!valueExists) {
                                allMatchingQueries = false;
                                break loop2;
                            }
                        }
                        result = allMatchingQueries;
                    } else {
                        String[] propertyNames = { "carrotNumDescriptions", "clusteringEngine" };
                        if (EqualityUtils.areEqualOrBothNullProperties(propertyNames, includedFacet,
                            ownerFacet)
                            && matchesOrBothNull("carrotSnippetField", includedFacet, ownerFacet)
                            && matchesOrBothNull("carrotTitleField", includedFacet, ownerFacet)
                            && matchesOrBothNull("carrotUrlField", includedFacet, ownerFacet)) {
                            result = true;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean matches(IndexField indexField1, IndexField indexField2) {
        boolean result;
        if (indexField1 != null && indexField2 != null && indexField1.getName().equals(indexField2.getName())) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    private boolean matchesOrBothNull(String indexFieldPropertyName, CollectionFacet includedFacet,
        CollectionFacet ownerFacet) {
        boolean result;
        try {
            IndexField indexField1 = (IndexField) PropertyUtils.getProperty(includedFacet,
                indexFieldPropertyName);
            IndexField indexField2 = (IndexField) PropertyUtils.getProperty(ownerFacet,
                indexFieldPropertyName);
            result = matchesOrBothNull(indexField1, indexField2);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private boolean matchesOrBothNull(IndexField indexField1, IndexField indexField2) {
        boolean result;
        if (matches(indexField1, indexField2)) {
            result = true;
        } else if (indexField1 == null && indexField2 == null) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    private CollectionFacet copy(CollectionFacet includedFacet, RecordCollection federationOwner) {
        CollectionFacet copy = new CollectionFacet();
        copy.setFacetType(includedFacet.getFacetType());
        copy.setCarrotNumDescriptions(includedFacet.getCarrotNumDescriptions());
        copy.setClusteringEngine(includedFacet.getClusteringEngine());
        copy.setClusteringUseCollection(includedFacet.isClusteringUseCollection());
        copy.setClusteringUseDocSet(includedFacet.isClusteringUseDocSet());
        copy.setClusteringUseSearchResults(includedFacet.isClusteringUseSearchResults());
        copy.setHideEmptyValues(includedFacet.isHideEmptyValues());
        copy.setMultiValued(includedFacet.isMultiValued());
        copy.setSortable(includedFacet.isSortable());

        IndexField facetField = includedFacet.getFacetField();
        if (facetField != null) {
            IndexField facetFieldCopy = federationOwner.getIndexField(facetField.getName());
            copy.setFacetField(facetFieldCopy);
        }

        IndexField carrotSnippetField = includedFacet.getCarrotSnippetField();
        if (carrotSnippetField != null) {
            IndexField carrotSnippetFieldCopy = federationOwner.getIndexField(carrotSnippetField.getName());
            copy.setCarrotSnippetField(carrotSnippetFieldCopy);
        }

        IndexField carrotTitleField = includedFacet.getCarrotTitleField();
        if (carrotTitleField != null) {
            IndexField carrotTitleFieldCopy = federationOwner.getIndexField(carrotTitleField.getName());
            copy.setCarrotTitleField(carrotTitleFieldCopy);
        }

        IndexField carrotUrlField = includedFacet.getCarrotUrlField();
        if (carrotUrlField != null) {
            IndexField carrotUrlFieldCopy = federationOwner.getIndexField(carrotUrlField.getName());
            copy.setCarrotUrlField(carrotUrlFieldCopy);
        }

        for (I18NLabel label : includedFacet.getLabels()) {
            for (Locale locale : ConstellioSpringUtils.getSupportedLocales()) {
                copy.setLabel(label.getKey(), label.getValue(locale), locale);
            }
        }

        for (I18NLabel labelledValue : includedFacet.getLabelledValues()) {
            for (Locale locale : ConstellioSpringUtils.getSupportedLocales()) {
                copy.setLabelledValue(labelledValue.getKey(), labelledValue.getValue(locale), locale);
            }
        }

        federationOwner.addCollectionFacet(copy);
        return copy;
    }

    @Override
    public CollectionFederation makeTransient(CollectionFederation entity) {
        RecordCollection federationOwner = entity.getOwnerCollection();
        federationOwner.getIncludedCollectionFederations().remove(entity);
        CollectionFederation result = super.makeTransient(entity);
        return result;
    }

    @Override
    public void addFederationDeletionRequired(Record record, RecordCollection federationOwner) {
        Long recordId = record.getId();
        String recordUrl = record.getUrl();
        String ql = "FROM FederationRecordDeletionRequired frdr WHERE frdr.ownerCollection = :ownerCollection AND frdr.recordId = :recordId AND frdr.recordUrl = :recordUrl";
        Query query = this.getEntityManager().createQuery(ql);
        query.setParameter("ownerCollection", federationOwner);
        query.setParameter("recordId", recordId);
        query.setParameter("recordUrl", recordUrl);
        if (query.getResultList().isEmpty()) {
            FederationRecordDeletionRequired deletionRequired = new FederationRecordDeletionRequired();
            deletionRequired.setRecordId(recordId);
            deletionRequired.setRecordUrl(recordUrl);
            deletionRequired.setOwnerCollection(federationOwner);
            getEntityManager().persist(deletionRequired);
        }
    }

    @Override
    public void addFederationIndexingRequired(Record record, RecordCollection federationOwner) {
        Long recordId = record.getId();
        String ql = "FROM FederationRecordIndexingRequired frir WHERE frir.ownerCollection = :ownerCollection AND frir.recordId = :recordId";
        Query query = this.getEntityManager().createQuery(ql);
        query.setParameter("ownerCollection", federationOwner);
        query.setParameter("recordId", recordId);
        if (query.getResultList().isEmpty()) {
            FederationRecordIndexingRequired indexingRequired = new FederationRecordIndexingRequired();
            indexingRequired.setRecordId(recordId);
            indexingRequired.setOwnerCollection(federationOwner);
            getEntityManager().persist(indexingRequired);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FederationRecordIndexingRequired> listMarkedForUpdateIndex(RecordCollection collection,
        int maxResults) {
        List<FederationRecordIndexingRequired> markedForUpdateIndex = new ArrayList<FederationRecordIndexingRequired>();
        if (collection.isFederationOwner()) {
            int remainingMaxResults = maxResults - markedForUpdateIndex.size();
            String ql = "FROM FederationRecordIndexingRequired frir WHERE frir.ownerCollection = :ownerCollection";
            Query query = this.getEntityManager().createQuery(ql);
            query.setParameter("ownerCollection", collection);
            if (maxResults >= 0) {
                query.setMaxResults(remainingMaxResults);
            }
            List<FederationRecordIndexingRequired> federationIndexingRequired = query.getResultList();
            for (FederationRecordIndexingRequired indexingRequired : federationIndexingRequired) {
                markedForUpdateIndex.add(indexingRequired);
            }
        }
        return markedForUpdateIndex;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FederationRecordDeletionRequired> listMarkedForExclusionOrDeletion(
        RecordCollection collection, int maxResults) {
        List<FederationRecordDeletionRequired> markedForExclusionOrDeletion = new ArrayList<FederationRecordDeletionRequired>();
        if (collection.isFederationOwner()) {
            int remainingMaxResults = maxResults - markedForExclusionOrDeletion.size();
            String ql = "FROM FederationRecordDeletionRequired frdr WHERE frdr.ownerCollection = :ownerCollection";
            Query query = this.getEntityManager().createQuery(ql);
            query.setParameter("ownerCollection", collection);
            if (maxResults >= 0) {
                query.setMaxResults(remainingMaxResults);
            }
            List<FederationRecordDeletionRequired> federationDeletionRequired = query.getResultList();
            for (FederationRecordDeletionRequired deletionRequired : federationDeletionRequired) {
                markedForExclusionOrDeletion.add(deletionRequired);
            }
        }
        return markedForExclusionOrDeletion;
    }

}
