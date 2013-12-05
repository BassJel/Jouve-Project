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
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.servlet.ConnectorManagerGetServlet;
import com.google.enterprise.connector.servlet.ConnectorMessageCode;
import com.google.enterprise.connector.servlet.ServletUtil;

/**
 * Admin servlet to get the config form to edit with pre-filled data for a given
 * existing connector name and language.
 * 
 */
public class GetConnectorConfig extends ConnectorManagerGetServlet {

	private static final long serialVersionUID = -5453963497072487302L;

	private static final Logger LOGGER = Logger.getLogger(GetConnectorConfig.class.getName());

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
	 * Handler for doGet in order to do unit tests. Returns the connector config
	 * form with pre-filled data.
	 * 
	 */
	public static void handleDoGet(String connectorName, String language, Manager manager, PrintWriter out) {
		ConnectorMessageCode status = new ConnectorMessageCode();

		ServletUtil.writeRootTag(out, false);
		ServletUtil.writeMessageCode(out, status);
		ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONFIGURE_RESPONSE, false);

		try {
			Map connectorConfig = manager.getConnectorConfig(connectorName);
			for (Iterator it = connectorConfig.keySet().iterator(); it.hasNext();) {
				String paramName = (String) it.next();
				String paramValue = (String) connectorConfig.get(paramName);

				String attributes = "name=\"" + paramName + "\" value=\"" + paramValue + "\"";
				ServletUtil.writeXMLTagWithAttrs(out, 2, ServletUtil.XMLTAG_PARAMETERS, attributes, true);
			}

		} catch (ConnectorNotFoundException e) {
			status = new ConnectorMessageCode(ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND, connectorName);
			LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_CONNECTOR_NOT_FOUND, e);
		}

		ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONFIGURE_RESPONSE, true);
		ServletUtil.writeRootTag(out, true);
	}

}
