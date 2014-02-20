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

import javax.persistence.EntityManager;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.CredentialGroup;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class CredentialGroupServicesImpl extends BaseCRUDServicesImpl<CredentialGroup> implements CredentialGroupServices {

    public CredentialGroupServicesImpl(EntityManager entityManager) {
        super(CredentialGroup.class, entityManager);
    }

    @Override
    public CredentialGroup makeTransient(CredentialGroup entity) {
        ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils.getConnectorInstanceServices();
        for (ConnectorInstance connectorInstance : entity.getConnectorInstances()) {
            connectorInstance.setCredentialGroup(null);
            connectorInstanceServices.makePersistent(connectorInstance);
        }
        return super.makeTransient(entity);
    }

}
