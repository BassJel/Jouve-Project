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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.RawContent;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class RawContentServicesImpl extends BaseCRUDServicesImpl<RawContent> implements RawContentServices {

    public RawContentServicesImpl(EntityManager entityManager) {
        super(RawContent.class, entityManager);
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<RawContent> getRawContents(Record record) {
		Query query = getEntityManager().createQuery("FROM RawContent WHERE record_id=:id ORDER BY id ASC");
		query.setParameter("id", record.getId());
		return query.getResultList();
	}

	@Override
	public void setRawContents(Record record, Collection<RawContent> rawContents) {
		deleteRawContents(record);
		for (RawContent rawContent : rawContents) {
			rawContent.setRecordId(record.getId());
			makePersistent(rawContent);
		}
	}

	@Override
	public void deleteRawContents(Record record) {
        List<RawContent> rawContents = getRawContents(record);
        for (RawContent rawContent : rawContents) {
			makeTransient(rawContent);
		}
	}

	@Override
	public void deleteRawContents(ConnectorInstance connectorInstance) {
		RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
		List<Record> records = recordServices.list(connectorInstance);
		List<Long> recordIds = new ArrayList<Long>();
		for (Record record : records) {
			recordIds.add(record.getId());
		}
	    StringBuffer sqlRawContent = new StringBuffer("DELETE FROM RawContent WHERE record_id IN (");
	    for (Iterator<Long> it = recordIds.iterator(); it.hasNext();) {
			Long recordId = it.next();
			sqlRawContent.append(recordId);
			if (it.hasNext()) {
				sqlRawContent.append(",");
			}
		}
	    sqlRawContent.append(")");
		Query rawConentQuery = getEntityManager().createNativeQuery(sqlRawContent.toString());
		rawConentQuery.executeUpdate();
		
//        String sqlRawContent = "DELETE FROM RawContent WHERE record_id"
//            + " IN (SELECT r.id FROM Record r, ConnectorInstance ci WHERE r.connectorInstance_id=ci.id AND ci.id=?)";
//        Query rawConentQuery = getEntityManager().createNativeQuery(sqlRawContent);
//        rawConentQuery.setParameter(1, connectorInstance.getId());
//        rawConentQuery.executeUpdate();
	}

}
