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

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.schema.IndexSchema;
import org.dom4j.Document;

import com.doculibre.constellio.entities.RecordCollection;

public interface SolrServices {
	public static final String DISMAX_ATTRIBUTE_NAME = "/dismaxQ";
	public static final String DEFAULT_DISTANCE_NAME = "/elevate";
	public static final String AUTOCOMPLETE_QUERY_NAME = "/terms";
	//permet de passer à la version de Constellio qui utilise le filtre des synonymes (dans un analyzer Solr)
	//laisser à false car incomplet (Cf. details Jira):
	public static final boolean synonymsFilterActivated = false;
	
	SolrServer getSolrServer(RecordCollection collection);
	
	SolrServer initCore(RecordCollection collection);
    
    void cleanCores() throws IOException;
	
	void deleteCore(RecordCollection collection);
    
    void deleteCore(String collectionName);
	
	void initSchemaFieldTypes();
	
	void updateSchemaFieldTypes();
	
	public void updateSchemaFieldTypes(RecordCollection collection);
	
	void initConnectorTypeFields();

	void updateSchemaFields(RecordCollection collection);
	
	void updateConfig();
	
	IndexSchema getSchema(RecordCollection collection);
	
	RecordCollection importSchema(File schemaFile);
	
	SolrDocument get(String docId, RecordCollection collection);
	
	void updateDismax(RecordCollection collection);
	
	void resetDefaultDistance(RecordCollection collection);
	
	//return true if the active distance is DISMAX_ATTRIBUTE_NAME
	boolean usesDisMax(RecordCollection collection) ;
	
	Boolean createMissingFieldTypes(Document schemaDocument);
	
	
}
