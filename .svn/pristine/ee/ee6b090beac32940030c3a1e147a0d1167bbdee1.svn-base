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
import java.util.List;

import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class AuthorizationServicesImpl implements AuthorizationServices {

    @Override
    public boolean isAuthorized(Record record, ConstellioUser user) {
        boolean authorized;
        ACLServices aclServices = ConstellioSpringUtils.getACLServices();
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        if (aclServices.hasACLPermission(record, user)) {
            authorized = true;
        } else {
            List<Record> records = new ArrayList<Record>();
            records.add(record);
            ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
            List<Record> authorizedRecords = connectorManagerServices.authorizeByConnector(records,
                user.getUserCredentials(), connectorManager);
            authorized = !authorizedRecords.isEmpty();
        }
        return authorized;
    }

}
