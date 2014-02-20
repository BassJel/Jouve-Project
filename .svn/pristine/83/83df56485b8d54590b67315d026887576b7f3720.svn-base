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

import java.util.Locale;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.constants.MailConnectorConstants;

public class InitMailConnectorHandler extends InitDefaultConnectorHandler {

    @Override
    protected void initCustomConnectorInstance(ConnectorInstance connectorInstance) {
        RecordCollection collection = connectorInstance.getRecordCollection();
        collection.getIndexField(MailConnectorConstants.FIELD_RECEIVEDDATE).setSortable(true);
        collection.getIndexField(MailConnectorConstants.FIELD_RECEIVEDDATE).setLabel("label", "Received date", Locale.ENGLISH);
        collection.getIndexField(MailConnectorConstants.FIELD_RECEIVEDDATE).setLabel("label", "Date de réception", Locale.FRENCH);
        createFieldFacetIfNecessary(collection, MailConnectorConstants.FIELD_SENDER);
        createFieldFacetIfNecessary(collection, MailConnectorConstants.FIELD_RECIPIENTS);
        createFieldFacetIfNecessary(collection, MailConnectorConstants.FIELD_FOLDER);
        createFieldFacetIfNecessary(collection, MailConnectorConstants.FIELD_WITHATTACHMENT, true);
        createDateQueryFacetIfNecessary(collection, MailConnectorConstants.FIELD_RECEIVEDDATE);
    }

}
