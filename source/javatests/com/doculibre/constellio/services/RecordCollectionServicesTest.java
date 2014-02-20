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

import junit.framework.Assert;

import org.junit.Test;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.RecordMeta;
import com.doculibre.constellio.entities.relevance.BoostRule;
import com.doculibre.constellio.entities.relevance.RecordCollectionBoost;
import com.doculibre.constellio.services.util.ServicesTestUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class RecordCollectionServicesTest extends BaseCRUDServicesTest<RecordCollection>{
	
	@Test
	public void testGetBoost() {
		
		List<RecordCollection> recordCollections = getCompleteEntities();
		RecordCollection recordCollection1 = recordCollections.get(0);
		
		ConnectorType connectorType = ServicesTestUtils.persistExampleConnectorType(connectorManager);
		ConnectorInstance connectorInstance = ServicesTestUtils.persistExampleConnectorInstance(0, connectorType, recordCollection1);
		
		ConnectorInstanceMeta connectorInstanceMeta = ServicesTestUtils.persistExampleConnectorInstanceMeta(1, connectorInstance);

		IndexField indexField =  ServicesTestUtils.getPersistedExampleIndexField("indexField1", connectorInstanceMeta, recordCollection1);
		IndexField indexField2 =  ServicesTestUtils.getPersistedExampleIndexField("indexField2", connectorInstanceMeta, recordCollection1);
		
		connectorInstanceMeta.addIndexField(indexField);
		connectorInstanceMeta.addIndexField(indexField2);
		
		RecordCollectionBoost recordCollectionDocumentBoosts = ServicesTestUtils.persistExampleRecordCollectionBoost("name", indexField, recordCollection1);
		
		BoostRule boostRule1 = ServicesTestUtils.persistNewBoostRule(recordCollectionDocumentBoosts);
		double keyBoost = 1.5;
		boostRule1.setBoost(keyBoost);
		boostRule1.setRegex("key");
		
		BoostRule boostRule2 = ServicesTestUtils.persistNewBoostRule(recordCollectionDocumentBoosts);
		double lolBoost = 2.5;
		boostRule2.setBoost(lolBoost);
		boostRule2.setRegex("lol");
		
		Record keyPlusLolRecord = ServicesTestUtils.persistExampleRecord(0, connectorInstance);
		
		RecordMeta recordMeta = ServicesTestUtils.newRecordMeta(connectorInstanceMeta);
		recordMeta.setContent("key lol");
		keyPlusLolRecord.addExternalMeta(recordMeta );
		ServicesTestUtils.addRecordToSolr(keyPlusLolRecord);
		
		Record keyRecord = ServicesTestUtils.persistExampleRecord(1, connectorInstance);
		
		RecordMeta recordMeta1 = ServicesTestUtils.newRecordMeta(connectorInstanceMeta);
		recordMeta1.setContent("key ");
		keyRecord.addExternalMeta(recordMeta1);
		
		ServicesTestUtils.addRecordToSolr(keyRecord);
		
		Record noneRecord = ServicesTestUtils.persistExampleRecord(2, connectorInstance);
		
		RecordMeta recordMeta2 = ServicesTestUtils.newRecordMeta(connectorInstanceMeta);
		recordMeta2.setContent("coco ");
		keyRecord.addExternalMeta(recordMeta2);
		
		ServicesTestUtils.addRecordToSolr(noneRecord);
		
		Double boost = ConstellioSpringUtils.getRecordCollectionServices().getBoost(recordCollection1, keyPlusLolRecord);
		print(boost);
		Assert.assertEquals(keyBoost * lolBoost, boost);
		
		boost = ConstellioSpringUtils.getRecordCollectionServices().getBoost(recordCollection1, keyRecord);
		print(boost);
		Assert.assertEquals(keyBoost, boost);
		
		boost = ConstellioSpringUtils.getRecordCollectionServices().getBoost(recordCollection1, noneRecord);
		print(boost);
		Assert.assertEquals(0, boost);
		
		//TODO Plus de tests avec un autre indexField
		//TODO fermer les serveurs solr
	}

	private void print(Object string) {
		System.out.println(string);
	}

	@Override
	public void constructSomeIncompleteEntities(List<RecordCollection> entities) {
		//No name
		RecordCollection recordCollection = new RecordCollection();
		entities.add(recordCollection);
	}

	@Override
	public void constructSomeCompleteEntities(List<RecordCollection> entities) {
		RecordCollection recordCollection = new RecordCollection();
		recordCollection.setName("collection-test1");
//		recordCollection.setOpenSearchURL("someurl");
		entities.add(recordCollection);
		
		/*recordCollection = new RecordCollection();
		recordCollection.setName("collection-test2");
		entities.add(recordCollection);*/
	}

	@Override
	protected BaseCRUDServices<RecordCollection> getServices() {
		return ConstellioSpringUtils.getRecordCollectionServices();
	}

}
