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

import com.doculibre.constellio.entities.CollectionFederation;
import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.CredentialGroup;
import com.doculibre.constellio.entities.FederationRecordDeletionRequired;
import com.doculibre.constellio.entities.FederationRecordIndexingRequired;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;

public interface FederationServices extends BaseCRUDServices<CollectionFederation> {

    List<RecordCollection> listIncludedCollections(RecordCollection federationOwner);

    List<RecordCollection> listOwnerCollections(RecordCollection includedCollection);

    List<ConnectorInstance> listConnectors(RecordCollection federationOwner);

    List<CredentialGroup> listCredentialGroups(RecordCollection federationOwner);

    boolean isConflict(String indexFieldName, RecordCollection federationOwner,
        RecordCollection includedCollection);

    IndexField copy(IndexField indexField, RecordCollection federationOwner);

    int countTraversedRecords(RecordCollection federationOwner);

    void addFederationIndexingRequired(Record record, RecordCollection federationOwner);
    
    void addFederationDeletionRequired(Record record, RecordCollection federationOwner);

    List<FederationRecordIndexingRequired> listMarkedForUpdateIndex(RecordCollection collection,
        int maxResults);

    List<FederationRecordDeletionRequired> listMarkedForExclusionOrDeletion(RecordCollection collection,
        int maxResults);

}
