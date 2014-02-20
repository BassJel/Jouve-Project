// Copyright (C) 2006-2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.doculibre.constellio.connector.servlet;

import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.servlet.ConnectorManagerGetServlet;
import com.google.enterprise.connector.servlet.ConnectorMessageCode;
import com.google.enterprise.connector.servlet.ServletUtil;

/**
 * Admin servlet to get the connector schedule for a given connector.
 * 
 * Same as GetConnectorStatus, but keeps the delay (no legacy)
 */
public class StopConnector extends ConnectorManagerGetServlet {

	private static final long serialVersionUID = 920731978874276219L;

	private static final Logger LOGGER = Logger.getLogger(StopConnector.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.enterprise.connector.servlet.ConnectorManagerGetServlet#
	 * processDoGet(java.lang.String, java.lang.String,
	 * com.google.enterprise.connector.manager.Manager, java.io.PrintWriter)
	 */
	protected void processDoGet(String connectorName, String lang, Manager manager, PrintWriter out) {
		handleDoGet(connectorName, lang, manager, out);
	}

	/**
	 * Handler for doGet in order to do unit tests. Returns the connector
	 * status.
	 * 
	 * @param connectorName
	 * @param manager
	 * @param out
	 *            PrintWriter where the response is written
	 */
	public static void handleDoGet(String connectorName, String lang, Manager manager, PrintWriter out) {
		ConnectorMessageCode status = new ConnectorMessageCode();
		ConnectorStatus connectorStatus = manager.getConnectorStatus(connectorName);
		if (connectorStatus == null) {
			status = new ConnectorMessageCode(ConnectorMessageCode.RESPONSE_NULL_CONNECTOR_STATUS, connectorName);
			LOGGER.warning("Connector manager returns no status for " + connectorName);
		}

        try {
            String connectorTypeName = connectorStatus.getType();
            Map<String, String> configData = manager.getConnectorConfig(connectorName);
            manager.setConnectorConfig(connectorName, connectorTypeName, configData, lang, true);
        } catch (ConnectorNotFoundException e) {
            status = new ConnectorMessageCode(
                ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND, connectorName);
            LOGGER.log(
                Level.WARNING, ServletUtil.LOG_EXCEPTION_CONNECTOR_NOT_FOUND, e);
        } catch (ConnectorExistsException e) {
            status = new ConnectorMessageCode(
                ConnectorMessageCode.EXCEPTION_CONNECTOR_EXISTS, connectorName);
            LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_CONNECTOR_EXISTS, e);
        } catch (PersistentStoreException e) {
            status.setMessageId(ConnectorMessageCode.EXCEPTION_PERSISTENT_STORE);
            LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_PERSISTENT_STORE, e);
        } catch (InstantiatorException e) {
            status.setMessageId(ConnectorMessageCode.EXCEPTION_INSTANTIATOR);
            LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_INSTANTIATOR, e);
        } catch (Throwable t) {
            status.setMessageId(ConnectorMessageCode.EXCEPTION_THROWABLE);
            LOGGER.log(Level.WARNING, "", t);
        }

        ServletUtil.writeRootTag(out, false);
		ServletUtil.writeMessageCode(out, status);
		if (connectorStatus != null) {
			ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_STATUS, false);
			ServletUtil.writeXMLElement(out, 2, ServletUtil.XMLTAG_CONNECTOR_NAME, connectorStatus.getName());
			ServletUtil.writeXMLElement(out, 2, ServletUtil.XMLTAG_CONNECTOR_TYPE, connectorStatus.getType());
			ServletUtil.writeXMLElement(out, 2, ServletUtil.XMLTAG_STATUS, Integer.toString(connectorStatus.getStatus()));
			ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_STATUS, true);
		}
		ServletUtil.writeRootTag(out, true);
	}

}
