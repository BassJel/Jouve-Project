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

import java.util.Date;
import java.util.List;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;

public interface StatusServices {

    int countTraversedRecords(ConnectorInstance connectorInstance);
    
    int countTraversedRecords(RecordCollection collection);
    
    int countIndexedRecords(RecordCollection collection);
    
    List<Record> listIndexedRecordsSince(RecordCollection collection, Date startDate);
    
    List<Record> listTraversedRecordsSince(ConnectorInstance connectorInstance, Date startDate);
    
    List<Record> listLastIndexedRecords(RecordCollection collection, int maxSize);
    
    List<Record> listLastTraversedRecords(ConnectorInstance connectorInstance, int maxSize);
    
    Date getLastTraversalDate(ConnectorInstance connectorInstance);
    
    String getSizeOnDisk(RecordCollection collection);
    
}
