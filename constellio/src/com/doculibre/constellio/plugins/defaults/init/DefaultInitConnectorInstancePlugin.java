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
package com.doculibre.constellio.plugins.defaults.init;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.plugins.api.init.InitConnectorInstancePlugin;
import com.doculibre.constellio.plugins.defaults.DefaultConstellioPlugin;
import com.doculibre.constellio.utils.connector.ConnectorPropertyInheritanceResolver;
import com.doculibre.constellio.utils.connector.init.InitConnectorInstanceHandler;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class DefaultInitConnectorInstancePlugin extends DefaultConstellioPlugin implements
    InitConnectorInstancePlugin {

    @Override
    public void init(ConnectorInstance connectorInstance) {
        InitConnectorInstanceHandler initHandler = ConnectorPropertyInheritanceResolver
            .newInheritedClassPropertyInstance(connectorInstance, "initInstanceHandlerClassName");
        if (initHandler != null) {
            initHandler.init(connectorInstance);
        }
    }

}
