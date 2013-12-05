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
package com.doculibre.constellio.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.doculibre.constellio.entities.ConnectorType;

public class SolrSchemaUtils {

	private static String SOLR_SCHEMA_PATTERN = "classpath*:config/schema.xml";

	public static IndexSchema getSchema(ConnectorType connectorType) {
		IndexSchema connectorTypeSchema;
		
		String targetSchemaName;
		if (connectorType != null) {
			targetSchemaName = connectorType.getName();
		} else {
			targetSchemaName = "constellio"; // Default
		}
		
		Resource[] classpathResources = ConstellioSpringUtils.getResources(SOLR_SCHEMA_PATTERN);
		if (classpathResources != null) {
			connectorTypeSchema = null;
			for (int i = 0; i < classpathResources.length; i++) {
				InputStream resourceInput = null;
				try {
					Resource resource = classpathResources[i];
					resourceInput = resource.getInputStream();
					Document schema = new SAXReader().read(resourceInput);
					Element schemaElement = schema.getRootElement();
					String schemaName = schemaElement.attributeValue("name");
					if (targetSchemaName.equals(schemaName)) {
						resourceInput = resource.getInputStream();
						
						SolrConfig dummySolrConfig;
						URL dummySolrConfigURL = SolrSchemaUtils.class.getClassLoader().getResource("config"+ File.separator + "solrdefault" + File.separator);
						File dummySolrConfigDir = new File(dummySolrConfigURL.toURI());
						File dummySolrConfigFile = new File(dummySolrConfigDir, "conf" + File.separator + "solrconfig.xml");
						dummySolrConfig = new SolrConfig(dummySolrConfigDir.getPath(), dummySolrConfigFile.getPath(), null);
						
						connectorTypeSchema = new IndexSchema(dummySolrConfig, null, new InputSource(resourceInput));
						break;
					}
				} catch (DocumentException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}  catch (ParserConfigurationException e) {
					throw new RuntimeException(e);
				} catch (SAXException e) {
					throw new RuntimeException(e);
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}catch (Exception e) {
					if (e instanceof RuntimeException) {
						throw (RuntimeException) e;
					} else {
						throw new RuntimeException(e);
					}
				} finally {
					IOUtils.closeQuietly(resourceInput);
				}
			}
		} else {
			connectorTypeSchema = null;
		}
		return connectorTypeSchema;
	}
	
	public static void main(String[] args) throws Exception {
		ConnectorType connectorType = new ConnectorType();
		connectorType.setName("mail");
		IndexSchema schema = getSchema(connectorType);
		Map<String, FieldType> fieldTypes = schema.getFieldTypes();
		for (String key : fieldTypes.keySet()) {
			FieldType fieldType = fieldTypes.get(key);
			System.out.print(key + ":");
			System.out.print(fieldType.getClass());
			System.out.print("\n");
			System.out.print(fieldType.getAnalyzer().getClass());
		}
	}
	
}
