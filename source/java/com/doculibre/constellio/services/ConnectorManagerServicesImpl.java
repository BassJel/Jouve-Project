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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.CredentialGroup;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.UserCredentials;
import com.doculibre.constellio.utils.AsciiUtils;
import com.doculibre.constellio.utils.ConnectorManagerRequestUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.EncryptionUtils;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.scheduler.ScheduleTime;
import com.google.enterprise.connector.scheduler.ScheduleTimeInterval;
import com.google.enterprise.connector.servlet.ConnectorMessageCode;
import com.google.enterprise.connector.servlet.ServletUtil;

public class ConnectorManagerServicesImpl extends BaseCRUDServicesImpl<ConnectorManager> implements
    ConnectorManagerServices {

    private static final Logger LOGGER = Logger.getLogger(ConnectorManagerServicesImpl.class.getName());

    public ConnectorManagerServicesImpl(EntityManager entityManager) {
        super(ConnectorManager.class, entityManager);
    }

    public static final String DEFAUT_NAME = "default";

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getConfig(ConnectorManager connectorManager, String connectorName) {
        Map<String, String> connectorConfigMap = new HashMap<String, String>();
        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
        Element xml = ConnectorManagerRequestUtils
            .sendGet(connectorManager, "/getConnectorConfig", paramsMap);
        List<Element> connectorConfigElements = xml.element(ServletUtil.XMLTAG_CONFIGURE_RESPONSE).elements(
            ServletUtil.XMLTAG_PARAMETERS);
        for (Element connectorConfigElement : connectorConfigElements) {
            String name = connectorConfigElement.attributeValue("name");
            String value = connectorConfigElement.attributeValue("value");
            connectorConfigMap.put(name, value);
        }
        return connectorConfigMap;
    }

    @Override
    public String getConfigFormSnippet(ConnectorManager connectorManager, String connectorType,
        String connectorName, Locale locale) {
        Element xml;

        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put(ServletUtil.QUERY_PARAM_LANG, locale.getLanguage());

        if (connectorName == null) {
            // New connector
            paramsMap.put(ServletUtil.XMLTAG_CONNECTOR_TYPE, connectorType);
            xml = ConnectorManagerRequestUtils.sendGet(connectorManager, "/getConfigForm", paramsMap);
        } else {
            // Existing connector
            paramsMap.put(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
            xml = ConnectorManagerRequestUtils.sendGet(connectorManager, "/getConnectorConfigToEdit",
                paramsMap);
        }

        Element formSnippet = xml.element(ServletUtil.XMLTAG_CONFIGURE_RESPONSE).element(
            ServletUtil.XMLTAG_FORM_SNIPPET);
        CDATA cdata = (CDATA) formSnippet.node(0);
        String configFormSnippetText = cdata.getStringValue();

        return configFormSnippetText;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getConnectorTypes(ConnectorManager connectorManager) {
        // On utilise le connectorManager par défaut.
        Element xml = ConnectorManagerRequestUtils.sendGet(connectorManager, "/getConnectorList", null);
        List<Element> connectorTypeElements = xml.element(ServletUtil.XMLTAG_CONNECTOR_TYPES).elements(
            ServletUtil.XMLTAG_CONNECTOR_TYPE);
        List<String> connectorTypes = new ArrayList<String>();
        for (Iterator<Element> it = connectorTypeElements.iterator(); it.hasNext();) {
            Element connectorTypeElement = (Element) it.next();
            connectorTypes.add(connectorTypeElement.getTextTrim());
        }
        return connectorTypes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getConnectorInstanceNames(ConnectorManager connectorManager, String collectionName) {
        List<String> connectorInstanceNames = new ArrayList<String>();
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordCollection collection = collectionServices.get(collectionName);
        if (collection != null) {
            Element xml = ConnectorManagerRequestUtils.sendGet(connectorManager, "/getConnectorInstanceList",
                null);
            // ConnectorInstances / ConnectorInstance / ConnectorName
            if (xml.element(ServletUtil.XMLTAG_CONNECTOR_INSTANCES) != null) {
                List<Element> connectorIntanceElements = xml.element(ServletUtil.XMLTAG_CONNECTOR_INSTANCES)
                    .elements(ServletUtil.XMLTAG_CONNECTOR_INSTANCE);
                for (Iterator<Element> it = connectorIntanceElements.iterator(); it.hasNext();) {
                    Element connectorInstanceElement = (Element) it.next();
                    Element connectorInstanceNameElement = connectorInstanceElement
                        .element(ServletUtil.XMLTAG_CONNECTOR_NAME);
                    String connectorInstanceName = connectorInstanceNameElement.getTextTrim();
                    if (collection.getConnectorInstance(connectorInstanceName) != null) {
                        connectorInstanceNames.add(connectorInstanceName);
                    }
                }
            }
        }
        return connectorInstanceNames;
    }

    @Override
    public String generateConnectorName(ConnectorManager connectorManager, String collectionName,
        String connectorType) {
        String normalizedCollectionName = AsciiUtils.convertNonAscii(collectionName);
        return (connectorManager.getName() + "_" + normalizedCollectionName + "_" + connectorType + "_" + System
            .currentTimeMillis()).toLowerCase();
    }

    @Override
    public String getConnectorType(ConnectorManager connectorManager, String connectorName) {
        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName.toLowerCase());
        Element xml = ConnectorManagerRequestUtils
            .sendGet(connectorManager, "/getConnectorStatus", paramsMap);
        Element connectorTypeElement = xml.element(ServletUtil.XMLTAG_CONNECTOR_STATUS).element(
            ServletUtil.XMLTAG_CONNECTOR_TYPE);
        String connectoryType = connectorTypeElement.getTextTrim();
        return connectoryType;
    }

    @Override
    public Element createConnector(ConnectorManager connectorManager, String connectorName,
        String connectorType, Map<String, String[]> requestParams, Locale locale) {
        return setConnectorConfig(connectorManager, connectorName, connectorType, requestParams, false,
            locale);
    }

    @Override
    public Element updateConnector(ConnectorManager connectorManager, String connectorName,
        Map<String, String[]> requestParams, Locale locale) {
        String connectorType = getConnectorType(connectorManager, connectorName);
        return setConnectorConfig(connectorManager, connectorName, connectorType, requestParams, true, locale);
    }

    private Element setConnectorConfig(ConnectorManager connectorManager, String connectorName,
        String connectorType, Map<String, String[]> requestParams, boolean update, Locale locale) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement(ServletUtil.XMLTAG_CONNECTOR_CONFIG);

        root.addElement(ServletUtil.QUERY_PARAM_LANG).addText(locale.getLanguage());
        root.addElement(ServletUtil.XMLTAG_CONNECTOR_NAME).addText(connectorName);
        root.addElement(ServletUtil.XMLTAG_CONNECTOR_TYPE).addText(connectorType);
        root.addElement(ServletUtil.XMLTAG_UPDATE_CONNECTOR).addText(Boolean.toString(update));

        for (String paramName : requestParams.keySet()) {
            if (!paramName.startsWith("wicket:")) {
                String[] paramValues = requestParams.get(paramName);
                for (String paramValue : paramValues) {
                    Element paramElement = root.addElement(ServletUtil.XMLTAG_PARAMETERS);
                    paramElement.addAttribute("name", paramName);
                    paramElement.addAttribute("value", paramValue);
                }
            }
        }

        Element response = ConnectorManagerRequestUtils.sendPost(connectorManager, "/setConnectorConfig",
            document);
        Element statusIdElement = response.element(ServletUtil.XMLTAG_STATUSID);
        if (statusIdElement != null) {
            String statusId = statusIdElement.getTextTrim();
            if (!statusId.equals("" + ConnectorMessageCode.SUCCESS)) {
                return response;
            } else {
                BackupServices backupServices = ConstellioSpringUtils.getBackupServices();
                backupServices.backupConfig(connectorName, connectorType);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void removeConnector(ConnectorManager connectorManager, String connectorName) {
        String connectorType = getConnectorType(connectorManager, connectorName);

        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
        ConnectorManagerRequestUtils.sendGet(connectorManager, "/removeConnector", paramsMap);

        BackupServices backupServices = ConstellioSpringUtils.getBackupServices();
        if (backupServices.hasConfigBackup(connectorName, connectorType)) {
            backupServices.deleteConfigBackup(connectorName, connectorType);
        }
    }

    @Override
    public Schedule getDefaultSchedule() {
        Schedule schedule = new Schedule();
        schedule.setDisabled(false);
        schedule.setLoad(1000); // number of documents to traverse per minute
        schedule.setRetryDelayMillis(5000); // 5 seconds
        schedule.getTimeIntervals().add(new ScheduleTimeInterval(new ScheduleTime(0), new ScheduleTime(0)));
        return schedule;
    }

    @Override
    public Schedule getSchedule(ConnectorManager connectorManager, String connectorName) {
        Schedule schedule;

        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
        try {
            Element xml = ConnectorManagerRequestUtils.sendGet(connectorManager, "/getSchedule", paramsMap);
            Element connectorStatusElement = xml.element(ServletUtil.XMLTAG_CONNECTOR_STATUS);
            Element connectorScheduleElement = connectorStatusElement
                .element(ServletUtil.XMLTAG_CONNECTOR_SCHEDULES);

            String scheduleTxt = connectorScheduleElement.getTextTrim();
            if (!StringUtils.isEmpty(scheduleTxt)) {
                schedule = new Schedule(scheduleTxt);
            } else {
                schedule = null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while trying to get schedule", e);
            schedule = null;
        }
        return schedule;
    }

    @Override
    public void setSchedule(ConnectorManager connectorManager, String connectorName, Schedule schedule) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement(ServletUtil.XMLTAG_CONNECTOR_SCHEDULES);
        root.addElement(ServletUtil.XMLTAG_CONNECTOR_NAME).addText(connectorName);
        if (schedule.isDisabled()) {
            root.addElement(ServletUtil.XMLTAG_DISABLED).addText(Boolean.toString(schedule.isDisabled()));
        }
        root.addElement(ServletUtil.XMLTAG_LOAD).addText(Integer.toString(schedule.getLoad()));
        root.addElement(ServletUtil.XMLTAG_DELAY).addText(Integer.toString(schedule.getRetryDelayMillis()));
        root.addElement(ServletUtil.XMLTAG_TIME_INTERVALS).addText(schedule.getTimeIntervalsAsString());
        ConnectorManagerRequestUtils.sendPost(connectorManager, "/setSchedule", document);

        String connectorType = getConnectorType(connectorManager, connectorName);
        BackupServices backupServices = ConstellioSpringUtils.getBackupServices();
        backupServices.backupConfig(connectorName, connectorType);
    }

    @Override
    public void restartTraversal(ConnectorManager connectorManager, String connectorName) {
        Schedule schedule = getSchedule(connectorManager, connectorName);
        if (schedule != null && schedule.isDisabled()) {
            schedule.setDisabled(false);
            setSchedule(connectorManager, connectorName, schedule);
        }
        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
        ConnectorManagerRequestUtils.sendGet(connectorManager, "/restartConnectorTraversal", paramsMap);
    }

    @Override
    public List<ScheduleTimeInterval> createDefaultTimeIntervals() {
        List<ScheduleTimeInterval> timeIntervals = new ArrayList<ScheduleTimeInterval>();
        ScheduleTime startTime = new ScheduleTime(0);
        ScheduleTime endTime = new ScheduleTime(0);
        timeIntervals.add(new ScheduleTimeInterval(startTime, endTime));
        return timeIntervals;
    }

    @Override
    public String getCollectionConnectorType(ConnectorManager connectorManager, String collectionName) {
        String collectionConnectorTypeName;
        List<String> connectorInstanceNames = getConnectorInstanceNames(connectorManager, collectionName);
        if (connectorInstanceNames.isEmpty()) {
            collectionConnectorTypeName = null;
        } else {
            String firstInstanceName = connectorInstanceNames.get(0);
            collectionConnectorTypeName = getConnectorType(connectorManager, firstInstanceName);
        }
        return collectionConnectorTypeName;
    }

    @Override
    public void synchronizeWithDatabase(ConnectorManager connectorManager) {
        ConnectorTypeServices connectorTypeServices = ConstellioSpringUtils.getConnectorTypeServices();

        // Add missing connector types
        List<String> connectorTypeNames = getConnectorTypes(connectorManager);
        for (String connectorTypeName : connectorTypeNames) {
            ConnectorType connectorType = connectorTypeServices.get(connectorTypeName);
            if (connectorType == null) {
                connectorType = new ConnectorType();
                connectorType.setName(connectorTypeName);

                File connectorsDir = ConstellioSpringUtils.getGoogleConnectorsDir();
                File connectorTypeDir = new File(connectorsDir, connectorType.getName());

                File iconFile = new File(connectorTypeDir, "icon.gif");
                if (iconFile.exists()) {
                    try {
                        byte[] iconBytes = FileUtils.readFileToByteArray(iconFile);
                        connectorType.setIconFileContent(iconBytes);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                File connectorTypeMetaXmlFile = new File(connectorTypeDir, "connectorTypeMeta.xml");
                if (connectorTypeMetaXmlFile.exists()) {
                    String path = connectorTypeMetaXmlFile.toURI().toString();
                    BeanFactory connectorTypeMeta = new FileSystemXmlApplicationContext(path);
                    if (connectorTypeMeta.containsBean("searchResultPanelClassName")) {
                        String searchResultPanelClassName = (String) connectorTypeMeta
                            .getBean("searchResultPanelClassName");
                        connectorType.setSearchResultPanelClassName(searchResultPanelClassName);
                    }
                    if (connectorTypeMeta.containsBean("initInstanceHandlerClassName")) {
                        String initInstancePluginClassName = (String) connectorTypeMeta
                            .getBean("initInstanceHandlerClassName");
                        connectorType.setInitInstanceHandlerClassName(initInstancePluginClassName);
                    }
                }

                connectorType.setConnectorManager(connectorManager);
                connectorTypeServices.makePersistent(connectorType);
            }
        }

        // Remove deleted connector types
        List<ConnectorType> dbConnectorType = connectorTypeServices.list();
        for (ConnectorType connectorType : dbConnectorType) {
            if (!connectorTypeNames.contains(connectorType.getName())) {
                connectorTypeServices.makeTransient(connectorType);
            }
        }

        ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils
            .getConnectorInstanceServices();
        BackupServices backupServices = ConstellioSpringUtils.getBackupServices();
        List<ConnectorInstance> connectorInstances = connectorInstanceServices.list();
        for (ConnectorInstance connectorInstance : connectorInstances) {
            String connectorName = connectorInstance.getName();
            String connectorTypeName = connectorInstance.getConnectorType().getName();
            boolean existingConnector = isExistingConnector(connectorManager, connectorName);
            boolean hasConfigBackup = backupServices.hasConfigBackup(connectorName, connectorTypeName);
            if (!existingConnector && hasConfigBackup) {
                backupServices.restoreConfigBackup(connectorName, connectorTypeName);
            } else if (existingConnector && !hasConfigBackup) {
                backupServices.backupConfig(connectorName, connectorTypeName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final ConnectorManager get(String name) {
        Query query = this.getEntityManager().createQuery("from ConnectorManager c where c.name = :name");
        query.setParameter("name", name);
        List<ConnectorManager> results = query.getResultList();
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    @Override
    public ConnectorManager getDefaultConnectorManager() {
        return this.get(DEFAUT_NAME);
    }

    @Override
    public ConnectorManager createDefaultConnectorManager() {
        ConnectorManager connectorManager = new ConnectorManager();
        connectorManager.setName(DEFAUT_NAME);
        try {
            URL defaultConnectorManagerURL = ConstellioSpringUtils.getDefaultConnectorManagerURL();
            connectorManager.setUrl(defaultConnectorManagerURL.toString());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Invalid URL for default ConnectorManager", e);
        }
        this.getEntityManager().persist(connectorManager);
        return connectorManager;
    }

    @Override
    public boolean isConnectorEnabled(ConnectorManager connectorManager, String connectorName) {
        Schedule schedule = getSchedule(connectorManager, connectorName);
        return schedule != null && !schedule.isDisabled();
    }

    @Override
    public void enableConnector(ConnectorManager connectorManager, String connectorName) {
        Schedule schedule = getSchedule(connectorManager, connectorName);
        schedule.setDisabled(false);
        setSchedule(connectorManager, connectorName, schedule);
    }

    @Override
    public void disableConnector(ConnectorManager connectorManager, String connectorName) {
        Schedule schedule = getSchedule(connectorManager, connectorName);
        if (schedule != null) {
            schedule.setDisabled(true);
            setSchedule(connectorManager, connectorName, schedule);
            Map<String, String> paramsMap = new HashMap<String, String>();
            paramsMap.put(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
            ConnectorManagerRequestUtils.sendGet(connectorManager, "/stopConnector", paramsMap);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Record> authorizeByConnector(List<Record> records,
        Collection<UserCredentials> userCredentialsList, ConnectorManager connectorManager) {
        List<Record> authorizedRecords = new ArrayList<Record>();

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement(ServletUtil.XMLTAG_AUTHZ_QUERY);
        Element connectorQueryElement = root.addElement(ServletUtil.XMLTAG_CONNECTOR_QUERY);

        Map<ConnectorInstance, UserCredentials> credentialsMap = new HashMap<ConnectorInstance, UserCredentials>();
        Set<ConnectorInstance> connectorsWithoutCredentials = new HashSet<ConnectorInstance>();
        Map<String, Record> recordsByURLMap = new HashMap<String, Record>();
        boolean recordToValidate = false;
        for (Record record : records) {
            // Use to accelerate the matching between response urls and actual entities
            recordsByURLMap.put(record.getUrl(), record);
            ConnectorInstance connectorInstance = record.getConnectorInstance();
            UserCredentials connectorCredentials = credentialsMap.get(connectorInstance);
            if (connectorCredentials == null && !connectorsWithoutCredentials.contains(connectorInstance)) {
                RecordCollection collection = connectorInstance.getRecordCollection();
                for (CredentialGroup credentialGroup : collection.getCredentialGroups()) {
                    if (credentialGroup.getConnectorInstances().contains(connectorInstance)) {
                        for (UserCredentials userCredentials : userCredentialsList) {
                            if (userCredentials.getCredentialGroup().equals(credentialGroup)) {
                                connectorCredentials = userCredentials;
                                credentialsMap.put(connectorInstance, userCredentials);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            if (connectorCredentials == null) {
                connectorsWithoutCredentials.add(connectorInstance);
                LOGGER.warning("Missing credentials for connector " + connectorInstance.getName());
            } else {
                String username = connectorCredentials.getUsername();
                if (StringUtils.isNotBlank(username)) {
                    String password = EncryptionUtils.decrypt(connectorCredentials.getEncryptedPassword());
                    String domain = connectorCredentials.getDomain();
                    Element identityElement = connectorQueryElement.addElement(ServletUtil.XMLTAG_IDENTITY);
                    identityElement.setText(username);
                    if (StringUtils.isNotBlank(domain)) {
                        identityElement.addAttribute(ServletUtil.XMLTAG_DOMAIN_ATTRIBUTE, domain);
                    }
                    identityElement.addAttribute(ServletUtil.XMLTAG_PASSWORD_ATTRIBUTE, password);

                    Element resourceElement = identityElement.addElement(ServletUtil.XMLTAG_RESOURCE);
                    resourceElement.setText(record.getUrl());
                    resourceElement.addAttribute(ServletUtil.XMLTAG_CONNECTOR_NAME_ATTRIBUTE,
                        connectorInstance.getName());
                    recordToValidate = true;
                }
            }
        }

        if (recordToValidate) {
            Element response = ConnectorManagerRequestUtils.sendPost(connectorManager, "/authorization",
                document);
            Element authzResponseElement = response.element(ServletUtil.XMLTAG_AUTHZ_RESPONSE);
            List<Element> answerElements = authzResponseElement.elements(ServletUtil.XMLTAG_ANSWER);
            for (Element answerElement : answerElements) {
                Element decisionElement = answerElement.element(ServletUtil.XMLTAG_DECISION);
                boolean permit = decisionElement.getTextTrim().equals("Permit");
                if (permit) {
                    Element resourceElement = answerElement.element(ServletUtil.XMLTAG_RESOURCE);
                    String recordUrl = resourceElement.getTextTrim();
                    Record record = recordsByURLMap.get(recordUrl);
                    authorizedRecords.add(record);
                }
            }
        }
        return authorizedRecords;
    }

    @Override
    public boolean isExistingConnector(ConnectorManager connectorManager, String connectorName) {
        boolean existingConnector;
        try {
            getConnectorType(connectorManager, connectorName);
            existingConnector = true;
        } catch (Exception e) {
            existingConnector = false;
        }
        return existingConnector;
    }

}
