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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Element;

import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.UserCredentials;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.scheduler.ScheduleTimeInterval;

public interface ConnectorManagerServices extends BaseCRUDServices<ConnectorManager> {
    
	Map<String, String> getConfig(ConnectorManager connectorManager, String connectorName);
	
	String getConfigFormSnippet(ConnectorManager connectorManager, String connectorType, String connectorName, Locale locale);
	
	List<String> getConnectorTypes(ConnectorManager connectorManager);
	
	List<String> getConnectorInstanceNames(ConnectorManager connectorManager, String collectionName);
	
	String generateConnectorName(ConnectorManager connectorManager, String collectionName, String connectorType);
	
	String getConnectorType(ConnectorManager connectorManager, String connectorName);
	
	Element createConnector(ConnectorManager connectorManager, String connectorName, String connectorType, Map<String, String[]> params, Locale locale);
	
	Element updateConnector(ConnectorManager connectorManager, String connectorName, Map<String, String[]> params, Locale locale);
	
	void removeConnector(ConnectorManager connectorManager, String connectorName);
	
	Schedule getDefaultSchedule();
	
	Schedule getSchedule(ConnectorManager connectorManager, String connectorName);
	
	void setSchedule(ConnectorManager connectorManager, String connectorName, Schedule schedule);
	
	List<ScheduleTimeInterval> createDefaultTimeIntervals();

	String getCollectionConnectorType(ConnectorManager connectorManager, String collectionName);

	void synchronizeWithDatabase(ConnectorManager connectorManager);
	
	ConnectorManager get(String name);
	
	ConnectorManager getDefaultConnectorManager();
	
	ConnectorManager createDefaultConnectorManager();
	
	void restartTraversal(ConnectorManager connectorManager, String connectorName);
    
    boolean isConnectorEnabled(ConnectorManager connectorManager, String connectorName);

    void disableConnector(ConnectorManager connectorManager, String connectorName);

    void enableConnector(ConnectorManager connectorManager, String connectorName);
    
    boolean isExistingConnector(ConnectorManager connectorManager, String connectorName);
    
    List<Record> authorizeByConnector(List<Record> records, Collection<UserCredentials> userCredentialsList, ConnectorManager connectorManager);

}
