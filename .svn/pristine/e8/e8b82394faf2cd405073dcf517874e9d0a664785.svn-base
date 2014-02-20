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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.junit.Test;

import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.google.enterprise.connector.scheduler.Schedule;

public class ConnectorManagerServicesTests {

    private static final String COLLECTION_NAME = "constellio-test-collection";
    private static final String CONNECTOR_TYPE = "helloworld-connector";
    private static final Locale LOCALE = Locale.FRENCH;
    private static final String SCHEDULE_1 = ":999:100:12-13:13-14:14-15";
    private static final String SCHEDULE_2 = ":888:111:14-15:17-18:20-21";

    @Test
    public final void testGetConfigFormSnippet() {
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
        String configFormSnippet = connectorManagerServices.getConfigFormSnippet(connectorManager,
            CONNECTOR_TYPE, "hwc1", LOCALE);
        System.out.println("testGetConfigFormSnippet");
        System.out.println(configFormSnippet);
        assertTrue(configFormSnippet.toLowerCase().indexOf("input") != -1);
    }

    @Test
    public final void testGetConnectorTypes() {
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
        List<String> connectorTypes = connectorManagerServices.getConnectorTypes(connectorManager);
        System.out.println("testGetConnectorTypes");
        System.out.println(connectorTypes);
        assertTrue(!connectorTypes.isEmpty());
    }

    @Test
    public final void testGetConnectorInstanceNames() {
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
        List<String> connectorInstanceNames = connectorManagerServices.getConnectorInstanceNames(
            connectorManager, COLLECTION_NAME);
        System.out.println("testGetConnectorInstanceNames");
        System.out.println(connectorInstanceNames);
        assertTrue(!connectorInstanceNames.isEmpty());
    }

    @Test
    public final void testGenerateConnectorName() {
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
        String connectorName = connectorManagerServices.generateConnectorName(connectorManager,
            COLLECTION_NAME, CONNECTOR_TYPE);
        System.out.println("testGenerateConnectorName");
        System.out.println(connectorName);

        StringTokenizer st = new StringTokenizer(connectorName, "_");

        assertTrue(st.countTokens() == 3);
        assertTrue(st.nextToken().equals(COLLECTION_NAME));
        assertTrue(st.nextToken().equals(CONNECTOR_TYPE));

        String currentTimeMillisToken = st.nextToken();
        try {
            Long.parseLong(currentTimeMillisToken);
        } catch (NumberFormatException e) {
            fail("Expected System.currentTimeMillis() : " + currentTimeMillisToken);
        }
    }

    @Test
    public final void testGetConnectorType() {
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
        String connectorName = connectorManagerServices.generateConnectorName(connectorManager,
            COLLECTION_NAME, CONNECTOR_TYPE);
        connectorManagerServices.createConnector(connectorManager, COLLECTION_NAME, CONNECTOR_TYPE,
            new HashMap<String, String[]>(), Locale.FRENCH);
        String connectorType = connectorManagerServices.getConnectorType(connectorManager, connectorName);
        System.out.println("testGetConnectorType");
        System.out.println(connectorType);

        connectorManagerServices.removeConnector(connectorManager, connectorName);

        assertTrue(connectorType.equals(CONNECTOR_TYPE));
    }

    @Test
    public final void testCreateConnector() {
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
        String connectorName = connectorManagerServices.generateConnectorName(connectorManager,
            COLLECTION_NAME, CONNECTOR_TYPE);
        connectorManagerServices.createConnector(connectorManager, COLLECTION_NAME, CONNECTOR_TYPE,
            new HashMap<String, String[]>(), Locale.FRENCH);
        System.out.println("testCreateConnector");
        System.out.println(connectorName);

        assertTrue(connectorManagerServices.getConnectorInstanceNames(connectorManager, COLLECTION_NAME)
            .contains(connectorName));

        connectorManagerServices.removeConnector(connectorManager, connectorName);

        assertTrue(connectorName != null);
    }

    @Test
    public final void testUpdateConnector() {
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
        String connectorName = connectorManagerServices.generateConnectorName(connectorManager,
            COLLECTION_NAME, CONNECTOR_TYPE);
        connectorManagerServices.createConnector(connectorManager, COLLECTION_NAME, CONNECTOR_TYPE,
            new HashMap<String, String[]>(), Locale.FRENCH);
        System.out.println("testUpdateConnector");
        System.out.println(connectorName);

        connectorManagerServices.updateConnector(connectorManager, connectorName,
            new HashMap<String, String[]>(), Locale.FRENCH);

        assertTrue(connectorManagerServices.getConnectorInstanceNames(connectorManager, COLLECTION_NAME)
            .contains(connectorName));

        connectorManagerServices.removeConnector(connectorManager, connectorName);
    }

    @Test
    public final void testRemoveConnector() {
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
        String connectorName = connectorManagerServices.generateConnectorName(connectorManager,
            COLLECTION_NAME, CONNECTOR_TYPE);
        connectorManagerServices.createConnector(connectorManager, COLLECTION_NAME, CONNECTOR_TYPE,
            new HashMap<String, String[]>(), Locale.FRENCH);
        System.out.println("testRemoveConnector");
        System.out.println(connectorName);

        connectorManagerServices.removeConnector(connectorManager, connectorName);

        assertTrue(!connectorManagerServices.getConnectorInstanceNames(connectorManager, COLLECTION_NAME)
            .contains(connectorName));
    }

    @Test
    public final void testSchedule() {
        ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
            .getConnectorManagerServices();
        ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
        String connectorName = connectorManagerServices.generateConnectorName(connectorManager,
            COLLECTION_NAME, CONNECTOR_TYPE);
        connectorManagerServices.createConnector(connectorManager, COLLECTION_NAME, CONNECTOR_TYPE,
            new HashMap<String, String[]>(), Locale.FRENCH);
        System.out.println("testSetSchedule");
        System.out.println(connectorName);

        Schedule schedule1 = new Schedule(connectorName + SCHEDULE_1);
        connectorManagerServices.setSchedule(connectorManager, connectorName, schedule1);
        Schedule retrievedSchedule1 = connectorManagerServices.getSchedule(connectorManager, connectorName);

        Schedule schedule2 = new Schedule(connectorName + SCHEDULE_2);
        connectorManagerServices.setSchedule(connectorManager, connectorName, schedule2);
        Schedule retrievedSchedule2 = connectorManagerServices.getSchedule(connectorManager, connectorName);

        connectorManagerServices.removeConnector(connectorManager, connectorName);

        assertTrue(retrievedSchedule1 != null);
        assertTrue(retrievedSchedule1.toString().equals(connectorName + SCHEDULE_1));
        assertTrue(retrievedSchedule2 != null);
        assertTrue(retrievedSchedule2.toString().equals(connectorName + SCHEDULE_2));
    }

}
