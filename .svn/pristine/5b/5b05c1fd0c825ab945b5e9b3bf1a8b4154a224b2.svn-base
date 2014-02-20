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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.RecordMeta;
import com.doculibre.constellio.entities.RecordTag;
import com.doculibre.constellio.services.util.ServicesTestUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class RecordServicesTest extends BaseCRUDServicesTest<Record> {

	private ConnectorInstance connectorInstance1;
	private ConnectorInstance connectorInstance2;
	private RecordCollection recordCollection1;
	private ConnectorType connectorType1;
	private ConnectorType connectorType2;

	/**
	 * compte le nombre de record qui sont à réindexer/non (selon champ
	 * updateIndex) et même qui sont marqués pour la suppression (si deleted est
	 * à true). Avec la condition : (excluded = false OR excluded = null)
	 */
	@Test
	public void testCountAndListExcluded() {
		RecordServices recordServices = (RecordServices) services;
		final Record record1 = getCompleteEntities().get(0);
		final Record record2 = getCompleteEntities().get(1);

		doTransaction(new Runnable() {
			@Override
			public void run() {
				record1.setExcluded(false);
				record1.setDeleted(false);
				record1.setUpdateIndex(true);
				services.makePersistent(record1);

				record2.setExcluded(false);
				record2.setDeleted(false);
				record2.setUpdateIndex(true);
				services.makePersistent(record2);
			}
		});
		// All records
		Assert.assertEquals(services.list().size(),
				recordServices.count(recordCollection1));

		// All update index, not excluded and not deleted
		Assert.assertEquals(2,
				recordServices.countMarkedForUpdateIndex(recordCollection1));

//		// All update index, not excluded and deleted
//		Assert.assertEquals(0,
//				recordServices.count(recordCollection1, true, true));

		// All excluded
		Assert.assertEquals(0, recordServices.listExcluded(recordCollection1)
				.size());

		doTransaction(new Runnable() {
			@Override
			public void run() {
				record1.setExcluded(true);
				services.makePersistent(record1);
			}
		});

		// All records
		Assert.assertEquals(services.list().size(),
				recordServices.count(recordCollection1));

		// All update index, not excluded and not deleted
		Assert.assertEquals(1,
				recordServices.countMarkedForUpdateIndex(recordCollection1));

//		// All update index, not excluded and deleted
//		Assert.assertEquals(0,
//				recordServices.count(recordCollection1, true, true));

		// All excluded
		Assert.assertEquals(1, recordServices.listExcluded(recordCollection1)
				.size());

		doTransaction(new Runnable() {
			@Override
			public void run() {
				record1.setExcluded(false);
				record1.setDeleted(true);
				services.makePersistent(record1);
			}
		});
		// All records
		Assert.assertEquals(services.list().size(),
				recordServices.count(recordCollection1));

		// All update index, not excluded and not deleted
		Assert.assertEquals(2,
				recordServices.countMarkedForUpdateIndex(recordCollection1));

		// All update index, not excluded and deleted
//		Assert.assertEquals(1,
//				recordServices.count(recordCollection1, true, true));

		// All excluded
		Assert.assertEquals(0, recordServices.listExcluded(recordCollection1)
				.size());
	}

	@Test
	public void deleteConnectorInstanceRecords() {
		final RecordServices services = ConstellioSpringUtils
				.getRecordServices();
		getCompleteEntities();

		Assert.assertEquals(2, services.count(recordCollection1));

		doTransaction(new Runnable() {
			@Override
			public void run() {
				services.deleteRecords(connectorInstance1);
			}
		});

		Assert.assertEquals(1, services.count(recordCollection1));

		doTransaction(new Runnable() {
			@Override
			public void run() {
				services.deleteRecords(connectorInstance2);
			}
		});
		Assert.assertEquals(0, services.count(recordCollection1));
	}

	@Test
	public void deleteRecordCollectionRecords() {
		final RecordServices services = ConstellioSpringUtils
				.getRecordServices();
		getCompleteEntities();

		Assert.assertEquals(2, services.count(recordCollection1));

		doTransaction(new Runnable() {
			@Override
			public void run() {
				services.deleteRecords(recordCollection1);
			}
		});

		Assert.assertEquals(0, services.count(recordCollection1));
	}

	/*@Test
	public void updateRecordBoost() {
		RecordServices services = ConstellioSpringUtils.getRecordServices();
		Record record = ServicesTestUtils.persistExampleRecord();

		// par defaut boost a 1.0
		boolean answer = services.updateRecordBoost(record);
		Assert.assertEquals(false, answer);

		record.setBoost(2.0);
		Assert.assertTrue(services.updateRecordBoost(record));

	}*/

	@SuppressWarnings("unchecked")
	@Test
	public void deleteAutomaticRecordTags() throws ParseException {
		final RecordServices services = ConstellioSpringUtils
				.getRecordServices();
		final List<Record> records = getCompleteEntities();
		final Record record1 = records.get(0);
		final Record record2 = records.get(1);
		final Record record3 = ServicesTestUtils.persistExampleRecord(3,
				connectorInstance1);

		final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		final Date date1 = dateFormat.parse("10/04/2003");
		final Date date2 = dateFormat.parse("10/14/2003");
		final Date date3 = dateFormat.parse("04/10/2012");

		doTransaction(new Runnable() {
			@Override
			public void run() {
				record1.setLastAutomaticTagging(date1);
				RecordTag tag1 = new RecordTag();
				FreeTextTag freeTextTag = new FreeTextTag();
				freeTextTag.setFreeText("A");
				tag1.setFreeTextTag(freeTextTag);
				tag1.setManual(false);
				record1.addRecordTag(tag1);
				entityManager.persist(freeTextTag);
				entityManager.persist(tag1);
				entityManager.merge(record1);
			}
		});

		doTransaction(new Runnable() {
			@Override
			public void run() {
				record2.setLastAutomaticTagging(date2);

				RecordTag tag2 = new RecordTag();
				FreeTextTag freeTextTag = new FreeTextTag();
				freeTextTag.setFreeText("B");
				tag2.setFreeTextTag(freeTextTag);
				tag2.setManual(false);

				record2.addRecordTag(tag2);
				entityManager.persist(freeTextTag);
				entityManager.persist(tag2);
				entityManager.merge(record2);
			}
		});

		doTransaction(new Runnable() {
			@Override
			public void run() {

				record3.setLastAutomaticTagging(date3);
				RecordTag tag3 = new RecordTag();
				FreeTextTag freeTextTag = new FreeTextTag();
				freeTextTag.setFreeText("C");
				tag3.setFreeTextTag(freeTextTag);
				tag3.setManual(false);
				record3.addRecordTag(tag3);
				entityManager.persist(freeTextTag);
				entityManager.persist(tag3);
				entityManager.merge(record3);
			}
		});

		String sqlTag = "from RecordTag ";

		List<RecordTag> result = entityManager.createQuery(sqlTag)
				.getResultList();

		for (RecordTag recordTag : result) {
			Record record = recordTag.getRecord();
			System.out.println(record.getLastAutomaticTagging()
					+ " record's collection id "
					+ record.getConnectorInstance().getRecordCollection()
							.getId());
		}

		doTransaction(new Runnable() {
			@Override
			public void run() {
				services.deleteAutomaticRecordTags(recordCollection1, date2);
			}
		});

		// FIXME : Les données de la BD sont correctement modifiées, mais pas
		// les entitées attachées, il est alors nécessaire de faire un clear().
		// Est-ce acceptable?
		entityManager.clear();

		result = entityManager.createQuery(sqlTag).getResultList();
		System.out.println("\nAFTER DELETE : collection id "
				+ recordCollection1.getId() + " " + date2);
		for (RecordTag recordTag : result) {
			Record record = recordTag.getRecord();
			System.out.println(record.getLastAutomaticTagging()
					+ " record's collection id "
					+ record.getConnectorInstance().getRecordCollection()
							.getId());
		}

		services.refresh(record1);
		services.refresh(record2);
		services.refresh(record3);

		Assert.assertEquals(2, result.size());

		System.out.println("tags record 1");
		for (RecordTag recordTag : services.get(record1.getId())
				.getRecordTags()) {
			System.out.println(recordTag.getFreeTextTag().getFreeText() + ":"
					+ recordTag.getRecord().getLastAutomaticTagging());
		}

		Assert.assertEquals(1, services.get(record1.getId()).getRecordTags()
				.size());

		System.out.println("tags record 2");
		for (RecordTag recordTag : services.get(record2.getId())
				.getRecordTags()) {
			System.out.println(recordTag.getFreeTextTag().getFreeText() + ":"
					+ recordTag.getRecord().getLastAutomaticTagging());
		}
		Assert.assertEquals(1, services.get(record2.getId()).getRecordTags()
				.size());

		System.out.println("tags record 3");
		for (RecordTag recordTag : services.get(record3.getId())
				.getRecordTags()) {
			System.out.println(recordTag.getFreeTextTag().getFreeText() + ":"
					+ recordTag.getRecord().getLastAutomaticTagging());
		}
		Assert.assertEquals(0, services.get(record3.getId()).getRecordTags()
				.size());

		// FIXME: les RecordTag sont supprimés de leur table mais restent
		// attachés aux records!

	}

	@Override
	protected RecordServices getServices() {
		return ConstellioSpringUtils.getRecordServices();
	}

	@Override
	public void constructSomeIncompleteEntities(List<Record> entities) {
		// no url
		Record record = new Record();
		record.setLastModified(new Date());
		record.setConnectorInstance(connectorInstance1);
		entities.add(record);

		// no last modified
		record = new Record();
		record.setUrl("http://www.lost.com");
		record.setConnectorInstance(connectorInstance1);
		entities.add(record);

		// no connector instance
		record = new Record();
		record.setUrl("http://www.lost.com");
		record.setLastModified(new Date());
		entities.add(record);
	}

	@Override
	public void constructSomeCompleteEntities(List<Record> entities) {
		Record record = new Record();
		record.setUrl("http://www.perdu.com");
		record.setConnectorInstance(connectorInstance1);

		record.setBoost(1.0);
		record.setDeleted(false);
		record.setLastFetched(new Date());
		record.setLastIndexed(new Date());
		record.setLastModified(new Date());
		record.setMimetype("HTTP");
		record.setParsedContent("hi!");
		record.setUpdateIndex(true);
		entities.add(record);

		record = new Record();
		record.setUpdateIndex(false);
		record.setUrl("http://www.lost.com");
		record.setLastModified(new Date());
		record.setConnectorInstance(connectorInstance2);

		for (RecordMeta meta : ServicesTestUtils
				.createSomeMetas(connectorInstance2)) {
			record.addContentMeta(meta);
		}
		for (RecordMeta meta : ServicesTestUtils
				.createSomeMetas(connectorInstance2)) {
			record.addExternalMeta(meta);
		}
		entities.add(record);
	}

	@Override
	public void loadAllTestBaseData() {
		super.loadAllTestBaseData();

		recordCollection1 = ServicesTestUtils.persistExampleRecordCollection(1);

		connectorType1 = ServicesTestUtils.persistExampleConnectorType(1,
				connectorManager);
		connectorInstance1 = ServicesTestUtils.persistExampleConnectorInstance(
				1, connectorType1, recordCollection1);

		connectorType2 = ServicesTestUtils.persistExampleConnectorType(2,
				connectorManager);
		connectorInstance2 = ServicesTestUtils.persistExampleConnectorInstance(
				2, connectorType2, recordCollection1);
	}

}
