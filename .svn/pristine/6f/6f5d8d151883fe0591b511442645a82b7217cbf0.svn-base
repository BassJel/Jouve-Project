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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.doculibre.constellio.constants.ManifoldCFConstants;
import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SolrConfig;
import com.doculibre.constellio.entities.relevance.BoostRule;
import com.doculibre.constellio.entities.relevance.RecordCollectionBoost;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class RecordCollectionServicesImpl extends BaseCRUDServicesImpl<RecordCollection> implements
    RecordCollectionServices {

    public RecordCollectionServicesImpl(EntityManager entityManager) {
        super(RecordCollection.class, entityManager);
    }

    @Override
    public RecordCollection get(String name) {
        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put("name", name);
        return get(criteria);
    }

    @Override
    public RecordCollection makePersistent(RecordCollection collection) {
        return makePersistent(collection, true);
    }

    @Override
    public RecordCollection makePersistent(RecordCollection collection, boolean synchronizationRequired) {
    	if (collection.getPosition() == null) {
    		List<RecordCollection> collectionLastPosition = list("position", false, 1);
    		int position  = collectionLastPosition.isEmpty() ? 0 : collectionLastPosition.get(0).getPosition() + 1;
    		collection.setPosition(position);
    	}

        if (synchronizationRequired) {
            collection.setSynchronizationRequired(true);
        }

        RecordCollection result = defaultMakePersistent(collection);

    	if (collection.getSolrConfiguration() == null) {
    		SolrConfig config = new SolrConfig();
    		config.setRecordCollection(collection);
    		collection.setSolrConfiguration(config);
    		SolrConfigServices solrConfigServices = ConstellioSpringUtils.getSolrConfigServices();
    		solrConfigServices.makePersistent(config);
    		makePersistent(collection, false);
    	}
        return result;
    }

    @Override
    public RecordCollection makeTransient(RecordCollection collection) {
        IndexingManager.remove(collection);

        ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils.getConnectorInstanceServices();
        for (ConnectorInstance connectorInstance : collection.getConnectorInstances()) {
            connectorInstanceServices.makeTransient(connectorInstance);
        }

        RecordCollection result = super.makeTransient(collection);
        if (collection.getSolrConfiguration() != null) {
        	getEntityManager().remove(collection.getSolrConfiguration());
        }
        if (collection.getThesaurus() != null) {
            SkosServices skosServices = ConstellioSpringUtils.getSkosServices();
            skosServices.makeTransient(collection.getThesaurus());
        }
        // Throw any database related exception before modifying Solr files
        getEntityManager().flush();

        final String collectionName = collection.getName();
        Runnable solrRunnable = new Runnable() {
            @Override
            public void run() {
                SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
                solrServices.deleteCore(collectionName);
            }
        };
        new Thread(solrRunnable).start();
        
        return result;
    }

    @Override
    public void markSynchronized(RecordCollection collection) {
        collection.setSynchronizationRequired(false);
        defaultMakePersistent(collection);
    }

    private RecordCollection defaultMakePersistent(RecordCollection collection) {
        if (collection.getLocales().isEmpty()) {
            List<Locale> supportedLocales = ConstellioSpringUtils.getSupportedLocales();
            collection.getLocales().addAll(supportedLocales);
        }
        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
        
        // Will ensure that we have a unique key field, recreating it if it was deleted
        IndexField uniqueKeyField = collection.getIndexField(IndexField.UNIQUE_KEY_FIELD);
        if (uniqueKeyField == null) {
            uniqueKeyField = indexFieldServices.newUniqueKeyField(collection);
            collection.addIndexField(uniqueKeyField);
        }
        
        // Will ensure that we have a parsed content field, recreating it if it was deleted
        IndexField parsedContentField = collection.getIndexField(IndexField.PARSED_CONTENT_FIELD);
        if (parsedContentField == null) {
            parsedContentField = indexFieldServices.newParsedContentField(collection);
            collection.addIndexField(parsedContentField);
        }

        // Will ensure that we have a language field, recreating it if it was deleted
        IndexField languageField = collection.getIndexField(IndexField.LANGUAGE_FIELD);
        if (languageField == null) {
            languageField = indexFieldServices.newLanguageField(collection);
            collection.addIndexField(languageField);
        }

        // Will ensure that we have a mime type field, recreating it if it was deleted
        IndexField mimeTypeField = collection.getIndexField(IndexField.MIME_TYPE_FIELD);
        if (mimeTypeField == null) {
            mimeTypeField = indexFieldServices.newMimeTypeField(collection);
            collection.addIndexField(mimeTypeField);
        }

        // Will ensure that we have a title field, recreating it if it was deleted
        IndexField titleField = collection.getIndexField(IndexField.TITLE_FIELD);
        if (titleField == null) {
            titleField = indexFieldServices.newTitleField(collection);
            collection.addIndexField(titleField);
        }

        // Will ensure that we have a url field, recreating it if it was deleted
        IndexField urlField = collection.getIndexField(IndexField.URL_FIELD);
        if (urlField == null) {
            urlField = indexFieldServices.newUrlField(collection);
            collection.addIndexField(urlField);
        }

        // Will ensure that we have a default url field, recreating it if it was deleted
        IndexField defaultUrlField = collection.getIndexField(IndexField.DISPLAY_URL_FIELD);
        if (defaultUrlField == null) {
        	defaultUrlField = indexFieldServices.newDisplayUrlField(collection);
            collection.addIndexField(defaultUrlField);
        }

        // Will ensure that we have a last indexed field, recreating it if it was deleted
        IndexField lastIndexedField = collection.getIndexField(IndexField.LAST_INDEXED_FIELD);
        if (lastIndexedField == null) {
            lastIndexedField = indexFieldServices.newLastIndexedField(collection);
            collection.addIndexField(lastIndexedField);
        }

        // Will ensure that we have a last modified field, recreating it if it was deleted
        IndexField lastModifiedField = collection.getIndexField(IndexField.LAST_MODIFIED_FIELD);
        if (lastModifiedField == null) {
            lastModifiedField = indexFieldServices.newLastModifiedField(collection);
            collection.addIndexField(lastModifiedField);
        }
        
        // Will ensure that we have a default search field, recreating it if it was deleted
        IndexField defaultSearchField = collection.getIndexField(IndexField.DEFAULT_SEARCH_FIELD);
        if (defaultSearchField == null) {
            defaultSearchField = indexFieldServices.newDefaultSearchField(collection);
            collection.addIndexField(defaultSearchField);
        }
        
        // Will ensure that we have a connector instance id field, recreating it if it was deleted
        IndexField recordIdField = collection.getIndexField(IndexField.RECORD_ID_FIELD);
        if (recordIdField == null) {
            recordIdField = indexFieldServices.newRecordIdField(collection);
            collection.addIndexField(recordIdField);
        }
        
        // Will ensure that we have a connector instance id field, recreating it if it was deleted
        IndexField connectorInstanceIdField = collection.getIndexField(IndexField.CONNECTOR_INSTANCE_ID_FIELD);
        if (connectorInstanceIdField == null) {
            connectorInstanceIdField = indexFieldServices.newConnectorInstanceIdField(collection);
            collection.addIndexField(connectorInstanceIdField);
        }

        // Will ensure that we have a connector type id field, recreating it if it was deleted
        IndexField connectorTypeIdField = collection.getIndexField(IndexField.CONNECTOR_TYPE_ID_FIELD);
        if (connectorTypeIdField == null) {
            connectorTypeIdField = indexFieldServices.newConnectorTypeIdField(collection);
            collection.addIndexField(connectorTypeIdField);
        }

        // Will ensure that we have a collection id field, recreating it if it was deleted
        IndexField collectionIdField = collection.getIndexField(IndexField.COLLECTION_ID_FIELD);
        if (collectionIdField == null) {
            collectionIdField = indexFieldServices.newCollectionIdField(collection);
            collection.addIndexField(collectionIdField);
        }

        // Will ensure that we have a public record field, recreating it if it was deleted
        IndexField publicRecordField = collection.getIndexField(IndexField.PUBLIC_RECORD_FIELD);
        if (publicRecordField == null) {
            publicRecordField = indexFieldServices.newPublicRecordField(collection);
            collection.addIndexField(publicRecordField);
        }
        
        // Will ensure that we have a parent field, recreating it if it was deleted
        IndexField parentPathField = collection.getIndexField(IndexField.PARENT_PATH_FIELD);
        if (parentPathField == null) {
        	parentPathField = indexFieldServices.newParentPathField(collection);
            collection.addIndexField(parentPathField);
        }
        
     // Will ensure that we have a parent field, recreating it if it was deleted
        IndexField newMd5Field = collection.getIndexField(IndexField.MD5);
        if (newMd5Field == null) {
        	newMd5Field = indexFieldServices.newMd5Field(collection);
            collection.addIndexField(newMd5Field);
        }

        // Will ensure that we have a free text tagging field, recreating it if it was deleted
        IndexField freeTextTaggingField = collection.getIndexField(IndexField.FREE_TEXT_TAGGING_FIELD);
        if (freeTextTaggingField == null) {
            freeTextTaggingField = indexFieldServices.newFreeTextTaggingField(collection);
            collection.addIndexField(freeTextTaggingField);
        }
        
        
        IndexField authmethod = collection.getIndexField(IndexField.DB_AUTHMETHOD_FIELD);
        if (authmethod == null) {
        	authmethod = indexFieldServices.newAuthmethodField(collection);
            collection.addIndexField(authmethod);
        }
        
        IndexField boost = collection.getIndexField(IndexField.DB_BOOST_FIELD);
        if (boost == null) {
        	boost = indexFieldServices.newBoostField(collection);
            collection.addIndexField(boost);
        }
        
        IndexField lastAutomaticTaggingField = collection.getIndexField(IndexField.DB_LAST_AUTOMATIC_TAGGING_FIELD);
        if (lastAutomaticTaggingField == null) {
        	lastAutomaticTaggingField = indexFieldServices.newLastAutomaticTaggingField(collection);
            collection.addIndexField(lastAutomaticTaggingField);
        }
        
        IndexField lastFetchedField = collection.getIndexField(IndexField.DB_LAST_FETCHED_FIELD);
        if (lastFetchedField == null) {
        	lastFetchedField = indexFieldServices.newLastFetchedField(collection);
            collection.addIndexField(lastFetchedField);
        }
        
        IndexField computeAclEntriesField = collection.getIndexField(IndexField.DB_COMPUTE_ACL_ENTRIES_FIELD);
        if (computeAclEntriesField == null) {
        	computeAclEntriesField = indexFieldServices.newComputeACLEntriesField(collection);
            collection.addIndexField(computeAclEntriesField);
        }
        
        IndexField deletedField = collection.getIndexField(IndexField.DB_DELETED_FIELD);
        if (deletedField == null) {
        	deletedField = indexFieldServices.newDeletedField(collection);
            collection.addIndexField(deletedField);
        }   

        IndexField excludedField = collection.getIndexField(IndexField.DB_EXCLUDED_FIELD);
        if (excludedField == null) {
        	excludedField = indexFieldServices.newExcludedField(collection);
            collection.addIndexField(excludedField);
        }        
        IndexField updateIndexField = collection.getIndexField(IndexField.DB_UPDATE_INDEX_FIELD);
        if (updateIndexField == null) {
        	updateIndexField = indexFieldServices.newUpdateIndexField(collection);
            collection.addIndexField(updateIndexField);
        } 
        IndexField aclEntryField = collection.getIndexField(IndexField.DB_ACL_ENTRY_FIELD);
        if (aclEntryField == null) {
        	aclEntryField = indexFieldServices.newAclEntryField(collection);
            collection.addIndexField(aclEntryField);
        } 
        IndexField dtField = collection.getIndexField(IndexField.DB_DT_FIELD);
        if (dtField == null) {
        	dtField = indexFieldServices.newDTField(collection);
            collection.addIndexField(dtField);
        }   
        IndexField blField = collection.getIndexField(IndexField.DB_BL_FIELD);
        if (blField == null) {
        	blField = indexFieldServices.newBLField(collection);
            collection.addIndexField(blField);
        }         
        IndexField metaContentField = collection.getIndexField(IndexField.DB_META_CONTENT_FIELD);
        if (metaContentField == null) {
        	metaContentField = indexFieldServices.newMetaContentField(collection);
            collection.addIndexField(metaContentField);
        } 
        IndexField metaExternalField = collection.getIndexField(IndexField.DB_META_EXTERNAL_FIELD);
        if (metaExternalField == null) {
        	metaExternalField = indexFieldServices.newMetaExternalField(collection);
            collection.addIndexField(metaExternalField);
        } 
        IndexField freeRecordTagField = collection.getIndexField(IndexField.DB_RECORD_TAG_FREE_FIELD);
        if (freeRecordTagField == null) {
        	freeRecordTagField = indexFieldServices.newRecordTagFreeField(collection);
            collection.addIndexField(freeRecordTagField);
        }
        IndexField skosRecordTagField = collection.getIndexField(IndexField.DB_RECORD_TAG_SKOS_FIELD);
        if (skosRecordTagField == null) {
        	skosRecordTagField = indexFieldServices.newRecordTagSkosField(collection);
            collection.addIndexField(skosRecordTagField);
        }
        
        boolean freeTextTaggingCopyField = false;
        for (CopyField copyField : defaultSearchField.getCopyFieldsDest()) {
            if (copyField.isSourceAllFields() || copyField.getIndexFieldSource().equals(freeTextTaggingField)) {
                freeTextTaggingCopyField = true;
                break;
            }
        }
        if (!freeTextTaggingCopyField) {
            defaultSearchField.addCopyFieldDest(freeTextTaggingField);
        }

        // Will ensure that we have a thesaurus tagging field, recreating it if it was deleted
        IndexField thesaurusTaggingField = collection.getIndexField(IndexField.THESAURUS_TAGGING_FIELD);
        if (thesaurusTaggingField == null) {
            thesaurusTaggingField = indexFieldServices.newThesaurusTaggingField(collection);
            collection.addIndexField(thesaurusTaggingField);
        }
        boolean thesaurusTaggingCopyField = false;
        for (CopyField copyField : defaultSearchField.getCopyFieldsDest()) {
            if (copyField.isSourceAllFields() || copyField.getIndexFieldSource().equals(thesaurusTaggingField)) {
                thesaurusTaggingCopyField = true;
                break;
            }
        }
        if (!thesaurusTaggingCopyField) {
            defaultSearchField.addCopyFieldDest(thesaurusTaggingField);
        }
        
        //Assurer l'existence des champs correspondants aux champs utilisé pour l'autocomplete:
        /*for (IndexField currentIndexField : collection.getIndexFields()){
        	if (currentIndexField.getUsedForAutocomplete()){
        		IndexField associatedIndexField = collection.getIndexField(IndexField.CONNECTOR_INSTANCE_ID_FIELD);
        	}
        }*/
        
        //ManifoldCF
        FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
        FieldType stringType = fieldTypeServices.get(FieldType.STRING);
        for (String securityIndexFieldName : ManifoldCFConstants.SECURITY_INDEX_FIELDS) {
			if (collection.getIndexField(securityIndexFieldName) == null) {
				IndexField securityIndexField = new IndexField();
				securityIndexField.setName(securityIndexFieldName);
				securityIndexField.setFieldType(stringType);
				securityIndexField.setIndexed(true);
				//securityIndexField.setStored(true); // FIXME
				securityIndexField.setMultiValued(true);
				collection.addIndexField(securityIndexField);
			}
		}
        
        return super.makePersistent(collection);
    }
	@Override
	public List<RecordCollection> list(int maxResults) {
		return list("position", true, maxResults);
	}

	@Override
	public List<RecordCollection> list() {
		return list("position", true);
	}

	@Override
	public List<RecordCollection> list(String orderByProperty,
			Boolean orderByAsc) {
		return super.list(orderByProperty, orderByAsc);
	}

	@Override
	public List<RecordCollection> list(String orderByProperty,
			Boolean orderByAsc, int maxResults) {
		return super.list(orderByProperty, orderByAsc, maxResults);
	}

	@Override
	public List<RecordCollection> list(Map<String, Object> criteria) {
		return super.list(criteria, "position", true);
	}

	@Override
	public List<RecordCollection> list(Map<String, Object> criteria,
			int maxResults) {
		return super.list(criteria, "position", true, maxResults);
	}

	@Override
	public List<RecordCollection> list(Map<String, Object> criteria,
			String orderByProperty, Boolean orderByAsc) {
		return super.list(criteria, orderByProperty, orderByAsc);
	}

	@Override
	public List<RecordCollection> list(Map<String, Object> criteria,
			String orderByProperty, Boolean orderByAsc, int maxResults) {
		return super.list(criteria, orderByProperty, orderByAsc, maxResults);
	}

	/**
	 * Se base sur collection.getRecordCollectionBoost() pour calculer le boost d'un document de la collection
	 * la valeur est la somme des boost des differents champs
	 * un boost d'un champ X est le produit des boosts lies aux regex qui le document matche pour son champ X
	 */
	@Override
	public Double getBoost(RecordCollection collection, Record record) {
		if (collection.getRecordCollectionBoost().isEmpty()){
			return 1.0;
		}
		try{
			final class BoostFieldsProduct {
				private final String fieldName;
				private double product = 1.0d;
				
				public BoostFieldsProduct(String fieldName){
					this.fieldName = fieldName;
				}
				
				public String getFieldName() {
					return fieldName;
				}
				public double getProduct() {
					return product;
				}
				
				public void multiply(double product) {
					if (this.product == 0.0d){
						return;
					}
					this.product *= product;
				}
			};
			List <BoostFieldsProduct> boostFieldsProductList = new ArrayList<BoostFieldsProduct>();
			IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
			
			for (RecordCollectionBoost recordCollectionBoost : collection.getRecordCollectionBoost()){
				IndexField associatedField = recordCollectionBoost.getAssociatedField();
				String fieldName = associatedField.getName();
				List<Object> fieldContent = indexFieldServices.extractFieldValues(record, associatedField);
				
				if (fieldContent == null){
					continue;
				}
//				System.out.println("////Pour le champ " + fieldName);
				
				//ensure that boostFieldsProductList contain a unique BoostFieldsProduct with a given fieldName 
				BoostFieldsProduct currentBoostFieldProduct = null;
				for(BoostFieldsProduct boostFieldProduct : boostFieldsProductList){
					if (boostFieldProduct.getFieldName().equals(fieldName)){
						currentBoostFieldProduct = boostFieldProduct;
						break;
					}
				}
				if (currentBoostFieldProduct == null) {
					currentBoostFieldProduct = new BoostFieldsProduct(fieldName);
					boostFieldsProductList.add(currentBoostFieldProduct);
				}
				
				// le produit pour un champ de nom fieldName
				for (BoostRule rule : recordCollectionBoost.getBoostRules()) {
//					System.out.println("\tPour la regle " + rule.getRegex());
					Pattern pattern = rule.getPattern();
					boolean matches = false;
					
					for (Object content : fieldContent){
//						System.out.println("\tContenu " + content);
						Matcher matcher = pattern.matcher(content.toString());
						matches = matcher.find();
						if (matches){
							break;
						}
					}

					if (matches){
						currentBoostFieldProduct.multiply(rule.getBoost());
					}
//					System.out.println("\tboost " + currentBoostFieldProduct.getProduct());
				}
			}
			
			// la somme des produits
			double documentBoost = 0.0;
			for (BoostFieldsProduct boostFieldProduct : boostFieldsProductList) {
				documentBoost += boostFieldProduct.getProduct();
			}
//			System.out.println("Boost du doc :" + documentBoost);
			return documentBoost;
		}catch(Exception e){
			e.printStackTrace();
			return 1.0;
		}
	}

	@Override
	public boolean isDismaxRequired(RecordCollection collection) {
		boolean dismaxRequired = false;
		
		if (collection.getRecordCollectionBoost().isEmpty()) {
			Float lastBoostDismax = null;
			for (IndexField indexField : collection.getIndexFields()) {
				Float indexFieldBoostDismax = indexField.getBoostDismax();
				if (indexFieldBoostDismax != null) {
					if (lastBoostDismax == null) {
						lastBoostDismax = indexFieldBoostDismax;
					}
					if (!indexFieldBoostDismax.equals(lastBoostDismax)) {
						dismaxRequired = true;
						break;
					}
				}
			}
		}
		return dismaxRequired;
	}

	@Override
	public List<RecordCollection> listPublic() {
		return listPublic("position", true);
	}

	@Override
	public List<RecordCollection> listPublic(String orderByProperty, Boolean orderByAsc) {
		return listPublic("position", true, -1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RecordCollection> listPublic(String orderByProperty, Boolean orderByAsc, int maxResults) {
		StringBuffer sb = new StringBuffer("from " + RecordCollection.class.getName() + " o");
		sb.append(" where ");
		sb.append("o.publicCollection=?0 or o.publicCollection is null");
        sb.append(" order by " + orderByProperty);
        if (orderByAsc == null) {
            orderByAsc = Boolean.TRUE; 
        }
        sb.append(orderByAsc ? " asc" : " desc");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter(0,  Boolean.TRUE);
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
		return (List<RecordCollection>) query.getResultList();
	}
	
	
}
