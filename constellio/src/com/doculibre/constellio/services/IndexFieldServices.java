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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;

public interface IndexFieldServices extends BaseCRUDServices<IndexField> {
	
	String BOOST_FIELD_PREFIX = "boost_";
	
	IndexField makePersistent(IndexField entity, boolean synchronizationRequired);

    IndexField newConnectorInstanceIdField(RecordCollection collection);
    IndexField newConnectorTypeIdField(RecordCollection collection);
    IndexField newCollectionIdField(RecordCollection collection);
    IndexField newDefaultSearchField(RecordCollection collection);
    IndexField newDisplayUrlField(RecordCollection collection);
    IndexField newFreeTextTaggingField(RecordCollection collection);
    IndexField newLanguageField(RecordCollection collection);
    IndexField newLastIndexedField(RecordCollection collection);
    IndexField newLastModifiedField(RecordCollection collection);
    IndexField newMimeTypeField(RecordCollection collection);
    IndexField newParsedContentField(RecordCollection collection);
    IndexField newRecordIdField(RecordCollection collection);
    IndexField newThesaurusTaggingField(RecordCollection collection);
    IndexField newTitleField(RecordCollection collection);
    IndexField newUniqueKeyField(RecordCollection collection);
    IndexField newUrlField(RecordCollection collection);
    IndexField newPublicRecordField(RecordCollection collection);
    
    IndexField newAuthmethodField(RecordCollection collection);
    IndexField newBoostField(RecordCollection collection);
    IndexField newLastAutomaticTaggingField(RecordCollection collection);
    IndexField newLastFetchedField(RecordCollection collection);
    IndexField newComputeACLEntriesField(RecordCollection collection);
    IndexField newDeletedField(RecordCollection collection);
    IndexField newExcludedField(RecordCollection collection);
    IndexField newExcludedEffectiveField(RecordCollection collection);
    IndexField newUpdateIndexField(RecordCollection collection);
    IndexField newAclEntryField(RecordCollection collection);
    IndexField newMetaContentField(RecordCollection collection);
    IndexField newMetaExternalField(RecordCollection collection);
    IndexField newDTField(RecordCollection collection);
    IndexField newBLField(RecordCollection collection);
    IndexField newRecordTagFreeField(RecordCollection collection);
    IndexField newRecordTagSkosField(RecordCollection collection);
 
    
	
    List<String> suggestValues(IndexField field, String text);
    List<String> suggestValues(IndexField field);
    
    IndexField get(String name, RecordCollection collection);
    
    List<IndexField> getIndexFieldAlimentedBy(String metaName, RecordCollection collection);
    
    IndexField getFirstIndexFieldAlimentedOnlyBy(String metaName, RecordCollection collection);
    
    List<IndexField> getSortableIndexFields(RecordCollection collection);
    
    IndexField getSortFieldOf(IndexField indexField);
    
    IndexField newSortFieldFor(IndexField indexField);
    
    Object extractFieldValue(Record record, IndexField indexField);
    List<Object> extractFieldValues(Record record, IndexField indexField);
    
    Map<String, String> getDefaultLabelledValues(IndexField indexField, Locale locale);
    
    void populateSolrDoc(SolrInputDocument doc, Record record, RecordCollection collection);
    
}
