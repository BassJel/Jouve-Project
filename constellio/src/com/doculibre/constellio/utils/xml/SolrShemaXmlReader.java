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
package com.doculibre.constellio.utils.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.services.FieldTypeServices;
import com.doculibre.constellio.services.SolrServicesImpl;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class SolrShemaXmlReader{
	
	private static final Logger LOG = Logger.getLogger(SolrShemaXmlReader.class.getName());
	
	private SolrShemaXmlReader(){
		
	}

	public static Document readDocument(File file){
        if (!file.exists()) {
        	throw new RuntimeException(file.getAbsolutePath() + "does not exist!");
        }
        try {
			Document schemaDocument = new SAXReader().read(file);
			return schemaDocument;
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getUniqueKeyField(Document schemaDocument)
			throws DocumentException {
		String defaultUniqueKeyName = "id";
		String uniqueKeyFieldName = defaultUniqueKeyName;
		Element uniqueKeyElement = schemaDocument.getRootElement().element(
				"uniqueKey");
		if (uniqueKeyElement != null) {
			uniqueKeyFieldName = uniqueKeyElement.getText();
		}

		return uniqueKeyFieldName;

	}

	public static String getDefaultSearchField(Document schemaDocument)
			throws DocumentException {
		String defaultSearchFieldName;
		Element defaultSearchFieldElement = schemaDocument.getRootElement()
				.element("defaultSearchField");
		if (defaultSearchFieldElement != null) {
			defaultSearchFieldName = defaultSearchFieldElement.getText();
		} else {
			defaultSearchFieldName = null;
		}
		return defaultSearchFieldName;

	}
			
		//noms des fields et leurs attributs + si checkTypes = true => verifie si les types donnés existent dans constellio 
	private static Map<String, Map<String, String>> readFields(Document schemaDocument, Boolean readDynamicFields, Boolean checkTypes){
		Map<String, Map<String, String>>  returnList = new HashMap<String, Map<String, String>>();
            //AnalyzerClassServices analyzerClassServices = ConstellioSpringUtils.getAnalyzerClassServices();
            FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
            if (schemaDocument != null) {

                Element fieldsElement = schemaDocument.getRootElement().element("fields");
                
                List<Element> fieldElements;
                if(readDynamicFields){
                	fieldElements= fieldsElement.elements("dynamicField");
                }else{ 
                	fieldElements = fieldsElement.elements("field");
                }
                for (Iterator<Element> it = fieldElements.iterator(); it.hasNext();) {
                    Element fieldElement = it.next();

                    if (checkTypes){
                    	/*String analyzerClassName = fieldElement.attributeValue("analyzer");
                        if (analyzerClassName != null) {
    						AnalyzerClass analyzerClass = analyzerClassServices.get(analyzerClassName);
                            if (analyzerClass == null) {
                            	throw new RuntimeException("New Analyzers must be defined throught Constellio Interface");
                            }
                        }*/
                        String typeName = fieldElement.attributeValue("type");
                        if (typeName == null){
                        	throw new RuntimeException("A Field must have a type");
                        }
                        FieldType fieldType = fieldTypeServices.get(typeName);
                        if (fieldType == null) {
                        	throw new RuntimeException("New Field type " + typeName +" must be defined throught Constellio Interface");
                        }
            		}
                    String fieldName= fieldElement.attributeValue("name");
                    if (fieldName == null){
                    	throw new RuntimeException("A Field must have a name");
                    }
                    
                    List<Attribute> attributes = fieldElement.attributes();
                    Map<String, String> attributesToMap = new HashMap<String, String>();
                    for (Attribute att : attributes){
                    	if (! att.getQualifiedName().equals("name")){
                    		attributesToMap.put(att.getQualifiedName(), att.getValue());
                    	}
                    }
                    
                    returnList.put(fieldName, attributesToMap);
                }
            }
            return returnList;
	}
	
	
	public static Map<String, Map<String, String>> readFields(Document schemaDocument, Boolean checkTypes){
		return readFields(schemaDocument, false, checkTypes);
	}
	
	public static Map<String, Map<String, String>> readDynamicFields(Document schemaDocument, Boolean checkTypes){
		return readFields(schemaDocument, true, checkTypes);
	}
	public static void main(String[] args){
		String schemaXml = "C:\\Users\\bnouha1\\Desktop\\t\\test\\schema.xml";
		File file = new File(schemaXml);
		
		Document schemaDocument = readDocument(file);
//		SolrServicesImpl.createMissingFieldTypes(schemaDocument);
		
		Map<String, Map<String, String>> fields = SolrShemaXmlReader.readFields(schemaDocument, false);
 		for(String field :fields.keySet()){
 			Map<String, String> atts = fields.get(field);
 			System.out.println("\n" + field);
			for(String att: atts.keySet()){
				System.out.print(att + " " + atts.get(att) + ";");
			}
		}
		fields = SolrShemaXmlReader.readDynamicFields(schemaDocument, false);
		
		for(String field :fields.keySet()){
 			Map<String, String> atts = fields.get(field);
 			System.out.println("\n" + field);
			for(String att: atts.keySet()){
				System.out.print(att + " " + atts.get(att) + ";");
			}
		}
	}


	// retourne pour chaque copyField : sa source, destination et maxChar
	
	public static List<List<String>> readCopyFields(Document schemaDocument) {
		List<List<String>> returnList = new ArrayList<List<String>>();
		if (schemaDocument != null) {

            List<Element> dynamicFieldsElement = schemaDocument.getRootElement().elements("copyField");
            for (Element elem : dynamicFieldsElement){
            	List<String> currentCopyField = new ArrayList<String>(); 
            	String source = elem.attributeValue("source");
            	currentCopyField.add(source);
            	String destination = elem.attributeValue("dest");
            	currentCopyField.add(destination);
            	String maxChars = elem.attributeValue("maxChars");
            	if (maxChars != null){
            		currentCopyField.add(maxChars);
            	}
            	
            	returnList.add(currentCopyField);
            }
		}
		return returnList;
	}

//	<defaultSearchField>contents</defaultSearchField>
//	<uniqueKey>fullname</uniqueKey>

}
