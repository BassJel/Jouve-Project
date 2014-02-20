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

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.junit.Test;

import com.doculibre.constellio.entities.Categorization;
import com.doculibre.constellio.entities.CategorizationRule;
import com.doculibre.constellio.entities.CollectionFacet;
import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.RecordMeta;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.services.util.ServicesTestUtils;
import com.doculibre.constellio.services.util.TestCaseNotImplementedException;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class IndexFieldServicesTest extends BaseCRUDServicesTest<IndexField> {

	private RecordCollection recordCollection1;
	private ConnectorInstance connectorInstance1;
	private ConnectorType connectorType1;

	@Test
	public void testNewField() {

		Map<String, String> methodNames = new HashMap<String, String>();
		methodNames.put("newUniqueKeyField", IndexField.UNIQUE_KEY_FIELD);
		methodNames.put("newParsedContentField",
				IndexField.PARSED_CONTENT_FIELD);
		methodNames.put("newDefaultSearchField",
				IndexField.DEFAULT_SEARCH_FIELD);
		methodNames.put("newRecordIdField", IndexField.RECORD_ID_FIELD);
		methodNames.put("newConnectorInstanceIdField",
				IndexField.CONNECTOR_INSTANCE_ID_FIELD);
		methodNames.put("newConnectorTypeIdField",
				IndexField.CONNECTOR_TYPE_ID_FIELD);
		methodNames.put("newCollectionIdField", IndexField.COLLECTION_ID_FIELD);
		methodNames.put("newFreeTextTaggingField",
				IndexField.FREE_TEXT_TAGGING_FIELD);
		methodNames.put("newThesaurusTaggingField",
				IndexField.THESAURUS_TAGGING_FIELD);
		methodNames.put("newLanguageField", IndexField.LANGUAGE_FIELD);
		methodNames.put("newMimeTypeField", IndexField.MIME_TYPE_FIELD);
		methodNames.put("newTitleField", IndexField.TITLE_FIELD);
		methodNames.put("newUrlField", IndexField.URL_FIELD);
		methodNames.put("newLastModifiedField", IndexField.LAST_MODIFIED_FIELD);

		final RecordCollection collection = ServicesTestUtils
				.persistExampleRecordCollection();
		Class<?> params[] = { RecordCollection.class };
		Object paramsObj[] = { collection };

		for (Map.Entry<String, String> methodNameAndParam : methodNames
				.entrySet()) {
			String methodName = methodNameAndParam.getKey();
			String indexFieldType = methodNameAndParam.getValue();
			Method currentMethod = null;
			try {
				currentMethod = services.getClass().getDeclaredMethod(
						methodName, params);
				final IndexField idxF1 = (IndexField) currentMethod.invoke(
						services, paramsObj);
				final IndexField idxF2 = (IndexField) currentMethod.invoke(
						services, paramsObj);

				doTransaction(new Runnable() {
					@Override
					public void run() {
						collection.addIndexField(idxF1);
						services.makePersistent(idxF1);
						collection.addIndexField(idxF2);
						services.makePersistent(idxF2);
					}
				});

				Assert.assertTrue(methodName + " : ids should be different ",
						idxF1.getId() != idxF2.getId());
				Assert.assertEquals("incorrect name for " + methodName,
						indexFieldType, idxF1.getName());
				Assert.assertEquals("incorrect name for " + methodName,
						indexFieldType, idxF2.getName());

				doTransaction(new Runnable() {
					@Override
					public void run() {
						services.makeTransient(idxF1);
						services.makeTransient(idxF2);
					}
				});

			} catch (AssertionFailedError e) {
				throw e;

			} catch (Exception e) {
				throw new RuntimeException("Exception occured for "
						+ methodName);
			}

		}
	}

	@Test
	public void testSuggestValuesIndexField() throws InterruptedException {
		getCompleteEntities();
		IndexFieldServices indexFieldServices = (IndexFieldServices) services;
		IndexField indexField = indexFieldServices.get("string",
				recordCollection1);
		final ConnectorInstanceMeta meta = ServicesTestUtils
				.persistExampleConnectorInstanceMeta("test", connectorInstance1);
		indexField.addConnectorInstanceMeta(meta);
		
		SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
		solrServices.updateSchemaFields(recordCollection1);
	
		doTransaction(new Runnable() {
			@Override
			public void run() {
				Record record1 = new Record();
				record1.setConnectorInstance(connectorInstance1);
				RecordMeta contentMeta = new RecordMeta();
				contentMeta.setRecord(record1);
				contentMeta.setContent("this is a test");
				contentMeta.setConnectorInstanceMeta(meta);
				record1.setLastModified(new Date());
				record1.addContentMeta(contentMeta);
				record1.setUrl("http://www.doculibre.com");
				RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
				recordServices.makePersistent(record1);
				
				recordServices.markRecordsForUpdateIndex(recordCollection1);
			}
		});
		
		IndexingManager.get(recordCollection1).reindexAll();
		//TestCase.assertTrue(IndexingManager.get(recordCollection1).isActive());
		
		Thread.sleep(1000 * 5);
		
		List<String> values = indexFieldServices.suggestValues(indexField);
		TestCase.assertEquals(1, values.size());
		
		throw new TestCaseNotImplementedException();
	}

	@Test
	public void testGetStringRecordCollection() {
		IndexFieldServices indexFieldServices = (IndexFieldServices) services;
		List<IndexField> fields = getCompleteEntities();
		TestCase.assertEquals(fields.get(0), indexFieldServices.get(
				fields.get(0).getName(), recordCollection1));
		TestCase.assertEquals(fields.get(1), indexFieldServices.get(
				fields.get(1).getName(), recordCollection1));
	}

	@Test
	public void testSorting() {
		getCompleteEntities();
		final IndexFieldServices indexFieldServices = (IndexFieldServices) services;

		for (String sortableFieldType : FieldType.SORTABLE_FIELD_TYPES) {
			try {
				IndexField sortableField = indexFieldServices.get(
						sortableFieldType, recordCollection1);
				TestCase.assertEquals(sortableField,
						indexFieldServices.getSortFieldOf(sortableField));
				TestCase.assertTrue(sortableField == indexFieldServices
						.getSortFieldOf(sortableField));
			} catch (Throwable t) {
				throw new RuntimeException("Exception for sort field of "
						+ sortableFieldType, t);
			}
		}

		testSortField(FieldType.INTEGER, FieldType.SINT);
		testSortField(FieldType.DOUBLE, FieldType.SDOUBLE);
		testSortField(FieldType.FLOAT, FieldType.SFLOAT);
		testSortField(FieldType.LONG, FieldType.SLONG);
		testSortField(FieldType.STRING, FieldType.ALPHA_ONLY_SORT);
		testSortField(FieldType.TEXT, FieldType.ALPHA_ONLY_SORT);

		TestCase.assertEquals(0,
				indexFieldServices.getSortableIndexFields(recordCollection1)
						.size());
		doTransaction(new Runnable() {
			@Override
			public void run() {
				IndexField f = indexFieldServices.get(FieldType.INTEGER,
						recordCollection1);
				f.setSortable(true);
				services.makePersistent(f);
			}
		});
		TestCase.assertEquals(1,
				indexFieldServices.getSortableIndexFields(recordCollection1)
						.size());

		TestCase.assertEquals(indexFieldServices.get(FieldType.INTEGER,
				recordCollection1),
				indexFieldServices.getSortableIndexFields(recordCollection1)
						.get(0));
	}

	private void testSortField(String fieldType, String expectedSortType) {
		IndexFieldServices indexFieldServices = (IndexFieldServices) services;
		try {
			IndexField indexField = indexFieldServices.get(fieldType,
					recordCollection1);
			TestCase.assertNull(indexFieldServices.getSortFieldOf(indexField));
			beginTransaction();
			IndexField sortField = indexFieldServices
					.newSortFieldFor(indexField);
			commitTransaction();
			TestCase.assertNotNull(sortField);
			TestCase.assertEquals(expectedSortType, sortField.getFieldType()
					.getName());
			TestCase.assertEquals(sortField.getName(), indexField.getName()
					+ "_sort");
			TestCase.assertEquals(sortField.getRecordCollection(),
					indexField.getRecordCollection());
			TestCase.assertTrue(sortField.isIndexed());
			TestCase.assertEquals(1, sortField.getCopyFieldsDest().size());
			TestCase.assertEquals(indexField, sortField.getCopyFieldsDest()
					.iterator().next().getIndexFieldSource());
			TestCase.assertEquals(sortField, sortField.getCopyFieldsDest()
					.iterator().next().getIndexFieldDest());
			TestCase.assertEquals(indexField, indexField.getCopyFieldsSource()
					.iterator().next().getIndexFieldSource());
			TestCase.assertEquals(sortField, indexField.getCopyFieldsSource()
					.iterator().next().getIndexFieldDest());
			TestCase.assertEquals(sortField,
					indexFieldServices.getSortFieldOf(indexField));
		} catch (Throwable t) {
			throw new RuntimeException("Exception for sort field of "
					+ fieldType, t);
		}
	}

	@Override
	public void constructSomeIncompleteEntities(List<IndexField> entities) {
		// No name
		IndexField indexField = new IndexField();
		indexField.setRecordCollection(recordCollection1);
		entities.add(indexField);

		// No record collection
		indexField = new IndexField();
		indexField.setName("incompleteField");
		entities.add(indexField);
	}

	@Override
	public void constructSomeCompleteEntities(List<IndexField> entities) {
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils
				.getFieldTypeServices();

		IndexField indexField = new IndexField();
		indexField.setName("firstField");
		indexField.setFieldType(fieldTypeServices.get(FieldType.STRING));
		indexField.setRecordCollection(recordCollection1);
		recordCollection1.addIndexField(indexField);
		entities.add(indexField);

		indexField = new IndexField();
		indexField.setName("secondField");
		indexField.setFieldType(fieldTypeServices.get(FieldType.INTEGER));
		indexField.setRecordCollection(recordCollection1);
		recordCollection1.addIndexField(indexField);
		entities.add(indexField);

		for (FieldType fieldType : fieldTypeServices.list()) {
			indexField = new IndexField();
			indexField.setName(fieldType.getName());
			indexField.setFieldType(fieldType);
			indexField.setRecordCollection(recordCollection1);
			recordCollection1.addIndexField(indexField);
			entities.add(indexField);
		}
	}

	@Override
	protected int getInitialEntityCount() {
		return recordCollection1.getIndexFields().size();
	}

	@Override
	protected void makeUnremoveable(int caseNumber, IndexField entity) {
		switch (caseNumber) {
		case 0:
			ConnectorInstanceMeta meta = ServicesTestUtils
					.persistExampleConnectorInstanceMeta("test",
							connectorInstance1);
			entity.addConnectorInstanceMeta(meta);
			break;

		case 1:
			entity.addCopyFieldSource(getCompleteEntities().get(1));
			break;

		case 2:
			entity.addCopyFieldDest(getCompleteEntities().get(1));
			break;

		case 3:
			entity.setInternalField(true);
			break;

		case 4:
			// Referenced by a categorization
			Categorization categorization = new Categorization();
			categorization.setName("test");
			categorization.setRecordCollection(recordCollection1);
			categorization.setIndexField(entity);
			entityManager.persist(categorization);
			break;

		case 5:
			// Referenced by a categorization rule
			categorization = new Categorization();
			categorization.setName("test");
			categorization.setRecordCollection(recordCollection1);
			categorization.setIndexField(getCompleteEntities().get(1));
			entityManager.persist(categorization);

			CategorizationRule categorizationRule = new CategorizationRule();
			categorizationRule.setCategorization(categorization);
			categorizationRule.setMatchRegexp("[e]");
			categorizationRule.setIndexField(entity);
			entityManager.persist(categorizationRule);
			break;

		case 6:
			// Referenced by a facet
			CollectionFacet facet = new CollectionFacet();
			facet.setName("my facet", Locale.ENGLISH);
			facet.setFacetField(entity);
			facet.setFacetType(CollectionFacet.FIELD_FACET);

			recordCollection1.addCollectionFacet(facet);
			ConstellioSpringUtils.getFacetServices().makePersistent(facet);
			entityManager.merge(recordCollection1);
			break;

		default:
			super.makeUnremoveable(caseNumber, entity);
			break;
		}

	}

	@Override
	protected BaseCRUDServices<IndexField> getServices() {
		return ConstellioSpringUtils.getIndexFieldServices();
	}

	@Override
	public void loadAllTestBaseData() {
		super.loadAllTestBaseData();
		recordCollection1 = ServicesTestUtils.persistExampleRecordCollection(1);
		connectorType1 = ServicesTestUtils
				.persistExampleConnectorType(connectorManager);
		connectorInstance1 = ServicesTestUtils.persistExampleConnectorInstance(
				1, connectorType1, recordCollection1);
	}
}
