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

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;

public class InitHttpConnectorHandler extends InitDefaultConnectorHandler {

    /* (non-Javadoc)
     * @see com.doculibre.constellio.plugins.defaults.init.InitDefaultConnectorInstancePlugin#initCustomConnectorInstance(com.doculibre.constellio.entities.ConnectorInstance)
     * 
     * document de la journée
     * des 7 derniers jours
     * du mois
     * bref une facette date calculée
     * format du doc
     */
    @Override
    protected void initCustomConnectorInstance(ConnectorInstance connectorInstance) {
        RecordCollection collection = connectorInstance.getRecordCollection();
        createFieldFacetIfNecessary(collection, IndexField.MIME_TYPE_FIELD);
        createDateQueryFacetIfNecessary(collection, IndexField.LAST_MODIFIED_FIELD);
    }

}
