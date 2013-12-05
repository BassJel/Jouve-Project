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
package com.doculibre.constellio.utils.connector.init;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import jxl.common.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.CopyFieldServices;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.utils.xml.SolrShemaXmlReader;

public class InitSolrConnectorHandler extends InitDefaultConnectorHandler {
	private static final Logger LOG = Logger.getLogger(InitSolrConnectorHandler.class);

	@Override
	protected void initCustomConnectorInstance(
			ConnectorInstance connectorInstance) {
        File connectorsDir = ConstellioSpringUtils.getGoogleConnectorsDir();
        File connectorTypeDir = new File(connectorsDir, connectorInstance.getConnectorType().getName());
        File connectorInstanceDir = new File(connectorTypeDir, connectorInstance.getName());
        File schemaFile = new File(connectorInstanceDir, "schema.xml");
        
        Document schemaDocument = SolrShemaXmlReader.readDocument(schemaFile);
        
		//	Ajouter les fieldType manquants
        SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
        Boolean newFields = solrServices.createMissingFieldTypes(schemaDocument);
        if (newFields){
        	//Mettre a jour la collection par défaut
        	solrServices.updateSchemaFieldTypes(null);
        }
        
        // 1. lire les metas et les transformer en champs d index
        
        Map<String, Map<String, String>> fields = SolrShemaXmlReader.readFields(schemaDocument, true);
        List<String> metasToEscapeList = new ArrayList<String>(IndexField.DEFAULT_CONSIDERED_METAS);
        try{
        	metasToEscapeList.add(SolrShemaXmlReader.getUniqueKeyField(schemaDocument));
        	//enleV à la suite de la demande de Rida
//            metasToEscapeList.add(SolrShemaXmlReader.getDefaultSearchField(schemaDocument));
        } catch(DocumentException e){
        	throw new RuntimeException(e);
        }
        
        
        addFields(connectorInstance, fields, false, metasToEscapeList);
        ;
        
        //traiter les champs dynamiques
        fields = SolrShemaXmlReader.readDynamicFields(schemaDocument, true);
        
        metasToEscapeList = new ArrayList<String>();
        addFields(connectorInstance, fields, true, metasToEscapeList);
        
        //traiter les copyField
        addCopyFields(schemaDocument, connectorInstance.getRecordCollection());
        
		
		//FIXME 2. idealement ajouter les facette de schemaDocument
		
		
//		createFieldFacetIfNecessary(collection, IndexField.MIME_TYPE_FIELD);
//        createDateQueryFacetIfNecessary(collection, IndexField.LAST_MODIFIED_FIELD);
	}

	@SuppressWarnings("unchecked")
    private void addCopyFields(Document schemaDocument, RecordCollection collection) {
		CopyFieldServices copyFieldServices = ConstellioSpringUtils.getCopyFieldServices();
		
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		
		List<Element> dynamicFieldsElement = schemaDocument.getRootElement().elements("copyField");
        for (Element elem : dynamicFieldsElement){
        	List<String> currentCopyField = new ArrayList<String>(); 
        	String source = elem.attributeValue("source");
        	currentCopyField.add(source);
        	String destination = elem.attributeValue("dest");
        	currentCopyField.add(destination);
        	String maxCharsString = elem.attributeValue("maxChars");
			
			Integer maxChars = null;
			if(maxCharsString != null){
				maxChars = Integer.valueOf(maxCharsString);
			}
			
			List<CopyField> newCopyFields = null;
			try{
				newCopyFields = copyFieldServices.newCopyFields(collection, source, destination, maxChars);
				for (CopyField newCopyField : newCopyFields){
					copyFieldServices.makePersistent(newCopyField);
				}
			}catch(Exception e){
				LOG.warn("CopyField associated with "+ source + ", " + destination + " not added due to the following :");
				LOG.warn(e.getMessage());
			}
			
		}
		
		entityManager.getTransaction().commit();
	}

	private void addFields(ConnectorInstance connectorInstance,
			Map<String, Map<String, String>> fields, Boolean dynamicFields, List<String> espcapeFieldsList) {
		RecordCollection collection = connectorInstance.getRecordCollection();
        for(String metaName : fields.keySet()){
			if (espcapeFieldsList.contains(metaName)){
				//escape car il sera NORMALEMENT traité par Constellio
				continue;
			}
			ConnectorInstanceMeta connectorInstanceMeta = connectorInstance.getOrCreateMeta(metaName);
			
			IndexField indexField = new IndexField();
			indexField.setDynamicField(dynamicFields);
			indexField.setName(metaName);
			collection.addIndexField(indexField);
			
			indexField.addConnectorInstanceMeta(connectorInstanceMeta);
			
			Map<String, String> properties = fields.get(metaName);
			
			String indexed = properties.get("indexed");
			indexField.setIndexed(indexed != null && indexed.equals("true"));
			
			String multiValued = properties.get("multiValued");
			indexField.setMultiValued(multiValued != null && multiValued.equals("true"));
			
			String typeName = properties.get("type");
			FieldType fieldType = ConstellioSpringUtils.getFieldTypeServices().get(typeName);
			indexField.setFieldType(fieldType);
			
			makePersistentIfPossible(indexField);
		}
	}

    private IndexField makePersistentIfPossible(IndexField indexField) {
        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
        FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
        
        RecordCollection collection = indexField.getRecordCollection();
        
        if (collection.isIncludedInFederation()) {
            List<String> conflicts = new ArrayList<String>();
            List<IndexField> newFederationFields = new ArrayList<IndexField>();
            List<RecordCollection> ownerCollections = federationServices.listOwnerCollections(collection);
            for (RecordCollection ownerCollection : ownerCollections) {
                String indexFieldName = indexField.getName();
                IndexField ownerIndexField = ownerCollection.getIndexField(indexFieldName);
                if (ownerIndexField == null) {
                    IndexField copy = federationServices.copy(indexField, ownerCollection);
                    newFederationFields.add(copy);
                }
                if (federationServices.isConflict(indexFieldName, ownerCollection, collection)) {
                    conflicts.add(ownerCollection.getName());
                }
            }
            if (conflicts.isEmpty()) {
                indexFieldServices.makePersistent(indexField);
                for (IndexField newFederationField : newFederationFields) {
                    indexFieldServices.makePersistent(newFederationField);
                }
            } else {
                StringBuilder errorMessage = new StringBuilder();
                for (String collectionTitle : conflicts) {
                    errorMessage.append( "Conflict : " + collectionTitle + ";");
                }
                throw new RuntimeException(errorMessage.toString());
            }
        } else {
            indexFieldServices.makePersistent(indexField);
        }
        
        return indexField;
    }
	

}
