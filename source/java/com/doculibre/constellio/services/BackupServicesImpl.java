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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.doculibre.constellio.utils.ClasspathUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class BackupServicesImpl implements BackupServices {
    
    @Override
    public synchronized void backupConfig(String connectorName, String connectorTypeName) {
        File connectorInstanceBackupDir = getConnectorInstanceBackupDir(connectorName, connectorTypeName);
        if (hasConfigBackup(connectorName, connectorTypeName)) {
            deleteConfigBackup(connectorName, connectorTypeName);
        }
        connectorInstanceBackupDir.mkdirs();
        List<File> sourceFiles = listConfigFiles(connectorName, connectorTypeName);
        for (File sourceFile : sourceFiles) {
            try {
                FileUtils.copyFileToDirectory(sourceFile, connectorInstanceBackupDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public synchronized boolean hasConfigBackup(String connectorName, String connectorTypeName) {
        boolean existingBackup;
        File connectorInstanceBackupDir = getConnectorInstanceBackupDir(connectorName, connectorTypeName);
        if (connectorInstanceBackupDir.exists()) {
            File connectorInstanceXml = new File(connectorInstanceBackupDir, "connectorInstance.xml");
            File connectorProperties = new File(connectorInstanceBackupDir, connectorName + ".properties");
            File connectorSchedule = new File(connectorInstanceBackupDir, connectorName + "_schedule.txt");
            if (connectorInstanceXml.exists() && connectorProperties.exists() && connectorSchedule.exists()) {
                existingBackup = true;
            } else {
                existingBackup = false;
            }
        } else {
            existingBackup = false;
        }
        return existingBackup;
    }

    @Override
    public synchronized void restoreConfigBackup(String connectorName, String connectorTypeName) {
        File connectorInstanceDir = getConnectorInstanceDir(connectorName, connectorTypeName);
        List<File> backupFiles = listBackupFiles(connectorName, connectorTypeName);
        for (File backupFile : backupFiles) {
            try {
                FileUtils.copyFileToDirectory(backupFile, connectorInstanceDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public synchronized void deleteConfigBackup(String connectorName, String connectorTypeName) {
        File connectorInstanceBackupDir = getConnectorInstanceBackupDir(connectorName, connectorTypeName);
        if (connectorInstanceBackupDir.exists()) {
            try {
                FileUtils.deleteDirectory(connectorInstanceBackupDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private static List<File> listConfigFiles(String connectorName, String connectorTypeName) {
        List<File> configFiles = new ArrayList<File>();
        File connectorInstanceDir = getConnectorInstanceDir(connectorName, connectorTypeName);
        File connectorInstanceXml = new File(connectorInstanceDir, "connectorInstance.xml");
        File connectorProperties = new File(connectorInstanceDir, connectorName + ".properties");
        File connectorSchedule = new File(connectorInstanceDir, connectorName + "_schedule.txt");
        if (connectorInstanceXml.exists()) {
            configFiles.add(connectorInstanceXml);
        }
        if (connectorProperties.exists()) {
            configFiles.add(connectorProperties);
        }
        if (connectorSchedule.exists()) {
            configFiles.add(connectorSchedule);
        }
        return configFiles;
    }
    
    private static File getConnectorInstanceDir(String connectorName, String connectorTypeName) {
        File connectorTypeDir = getConnectorTypeDir(connectorName, connectorTypeName);
        return new File(connectorTypeDir, connectorName);
    }
    
    private static File getConnectorTypeDir(String connectorName, String connectorTypeName) {
        File googleConnectorsDir = getGoogleConnectorsDir();
        return new File(googleConnectorsDir, connectorTypeName);
    }
    
    private static File getGoogleConnectorsDir() {
        return ConstellioSpringUtils.getGoogleConnectorsDir();
    }
    
    private static List<File> listBackupFiles(String connectorName, String connectorTypeName) {
        List<File> backupFiles = new ArrayList<File>();
        File connectorInstanceBackupDir = getConnectorInstanceBackupDir(connectorName, connectorTypeName);
        File connectorInstanceXml = new File(connectorInstanceBackupDir, "connectorInstance.xml");
        File connectorProperties = new File(connectorInstanceBackupDir, connectorName + ".properties");
        File connectorSchedule = new File(connectorInstanceBackupDir, connectorName + "_schedule.txt");
        if (connectorInstanceXml.exists()) {
            backupFiles.add(connectorInstanceXml);
        }
        if (connectorProperties.exists()) {
            backupFiles.add(connectorProperties);
        }
        if (connectorSchedule.exists()) {
            backupFiles.add(connectorSchedule);
        }
        return backupFiles;
    }
    
    private static File getConnectorInstanceBackupDir(String connectorName, String connectorTypeName) {
        File connectorTypeBackupDir = getConnectorTypeBackupDir(connectorName, connectorTypeName);
        return new File(connectorTypeBackupDir, connectorName);
    }
    
    private static File getConnectorTypeBackupDir(String connectorName, String connectorTypeName) {
        File googleConnectorsBackupDir = getGoogleConnectorsBackupDir();
        return new File(googleConnectorsBackupDir, connectorTypeName);
    }
    
    private static File getGoogleConnectorsBackupDir() {
        File webinfDir = ClasspathUtils.getWebinfDir();
        return new File(webinfDir, "connectors-backup");
    }

}
