package com.doculibre.constellio.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.DateUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.ParsedContent;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.RecordMeta;
import com.doculibre.constellio.entities.RecordTag;
import com.doculibre.constellio.entities.acl.PolicyACLEntry;
import com.doculibre.constellio.entities.acl.RecordPolicyACLEntry;
import com.doculibre.constellio.entities.skos.SkosConcept;
import com.doculibre.constellio.solr.LazyLoadSolrRecordList;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.solr.handler.component.ConstellioAuthorizationComponent;
import com.doculibre.constellio.solr.handler.component.ConstellioElevationComponent;
import com.doculibre.constellio.status.StatusManager;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.PartitionnedList;
import com.google.enterprise.connector.spi.SpiConstants;

public class RecordServicesSolrImpl implements RecordServices {

	private static final Logger LOGGER = Logger.getLogger(RecordServicesSolrImpl.class.getName());

	private static ConcurrentMap<String, ReadWriteLock> collectionLocks = new ConcurrentHashMap<String, ReadWriteLock>();

	private static Long generateNewId() {
		long now = System.currentTimeMillis();
		int randomInt = (int) (Math.random() * 100000);
		return new Long(now + "" + randomInt);
	}

	@Override
	public ReadWriteLock getLock(String collectionName) {
		return collectionLocks.putIfAbsent(collectionName, new ReentrantReadWriteLock(true));
	}

	@Override
	public Record get(SolrDocument doc) {
		return populateRecord(doc);
	}

	@Override
	public void refresh(Record record) {
		ACLServices aclServices = ConstellioSpringUtils.getACLServices();
		ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils.getConnectorInstanceServices();
		FreeTextTagServices freeTextTagServices = ConstellioSpringUtils.getFreeTextTagServices();
		SkosServices skosServices = ConstellioSpringUtils.getSkosServices();

		Long connectorId = new Long(record.getConnectorInstance().getId());
		ConnectorInstance connectorInstance = connectorInstanceServices.get(connectorId);
		record.setConnectorInstance(connectorInstance);

		for (RecordMeta recordMeta : record.getExternalMetas()) {
			ConnectorInstanceMeta connectorInstanceMeta = recordMeta.getConnectorInstanceMeta();
			connectorInstanceMeta = connectorInstance.getMeta(connectorInstanceMeta.getId());
			recordMeta.setConnectorInstanceMeta(connectorInstanceMeta);
		}

		for (RecordMeta recordMeta : record.getContentMetas()) {
			ConnectorInstanceMeta connectorInstanceMeta = recordMeta.getConnectorInstanceMeta();
			connectorInstanceMeta = connectorInstance.getMeta(connectorInstanceMeta.getId());
			recordMeta.setConnectorInstanceMeta(connectorInstanceMeta);
		}

		for (RecordTag recordTag : record.getRecordTags()) {
			FreeTextTag freeTextTag = recordTag.getFreeTextTag();
			if (freeTextTag != null) {
				freeTextTag = freeTextTagServices.get(freeTextTag.getId());
				recordTag.setFreeTextTag(freeTextTag);
			}
			SkosConcept skosConcept = recordTag.getSkosConcept();
			if (skosConcept != null) {
				skosConcept = skosServices.getSkosConcept(skosConcept.getId());
				recordTag.setSkosConcept(skosConcept);
			}
		}

		for (RecordPolicyACLEntry recordAclEntry : record.getRecordPolicyACLEntries()) {
			PolicyACLEntry aclEntry = recordAclEntry.getEntry();
			aclEntry = aclServices.getEntry(aclEntry.getId());
			recordAclEntry.setEntry(aclEntry);
		}
	}

	@SuppressWarnings("unchecked")
	public Record populateRecord(SolrDocument document) {
		Record record = new Record();

		record.setId(new Long(document.getFieldValue(IndexField.RECORD_ID_FIELD).toString()));

		if (document.getFieldValue(IndexField.URL_FIELD) != null) {
			record.setUrl((String) document.getFieldValue(IndexField.URL_FIELD));
		}
		if (document.getFieldValue(IndexField.DB_AUTHMETHOD_FIELD) != null) {
			record.setAuthmethod((String) document.getFieldValue(IndexField.DB_AUTHMETHOD_FIELD));
		}
		if (document.getFieldValue(IndexField.LAST_MODIFIED_FIELD) != null) {
			Object lastModifiedDate = document.getFieldValue(IndexField.LAST_MODIFIED_FIELD);
			if (lastModifiedDate instanceof Date) {
				record.setLastModified((Date) lastModifiedDate);
			}
		}
		if (document.getFieldValue(IndexField.DB_LAST_FETCHED_FIELD) != null) {
			record.setLastFetched((Date) document.getFieldValue(IndexField.DB_LAST_FETCHED_FIELD));
		}
		if (document.getFieldValue(IndexField.LAST_INDEXED_FIELD) != null) {
			record.setLastIndexed((Date) document.getFieldValue(IndexField.LAST_INDEXED_FIELD));
		}
		if (document.getFieldValue(IndexField.DB_LAST_AUTOMATIC_TAGGING_FIELD) != null) {
			record.setLastAutomaticTagging((Date) document.getFieldValue(IndexField.DB_LAST_AUTOMATIC_TAGGING_FIELD));
		}
		if (document.getFieldValue(IndexField.LANGUAGE_FIELD) != null) {
			Collection<Object> fieldValueList = document.getFieldValues(IndexField.LANGUAGE_FIELD);
			if (!fieldValueList.isEmpty()) {
				record.setLang(fieldValueList.iterator().next().toString());
			}
		}
		if (document.getFieldValue(IndexField.MIME_TYPE_FIELD) != null) {
			record.setMimetype((String) document.getFieldValue(IndexField.MIME_TYPE_FIELD));
		}
		if (document.getFieldValue(IndexField.DB_UPDATE_INDEX_FIELD) != null) {
			record.setUpdateIndex((Boolean) document.getFieldValue(IndexField.DB_UPDATE_INDEX_FIELD));
		}
		if (document.getFieldValue(IndexField.DB_DELETED_FIELD) != null) {
			record.setDeleted((Boolean) document.getFieldValue(IndexField.DB_DELETED_FIELD));
		}
		if (document.getFieldValue(IndexField.DB_EXCLUDED_FIELD) != null) {
			record.setExcluded((Boolean) document.getFieldValue(IndexField.DB_EXCLUDED_FIELD));
		}
		if (document.getFieldValue(IndexField.DB_EXCLUDED_EFFECTIVE_FIELD) != null) {
			record.setExcludedEffective((Boolean) document.getFieldValue(IndexField.DB_EXCLUDED_EFFECTIVE_FIELD));
		}
		if (document.getFieldValue(IndexField.PUBLIC_RECORD_FIELD) != null) {
			record.setPublicRecord((Boolean) document.getFieldValue(IndexField.PUBLIC_RECORD_FIELD));
		}
		if (document.getFieldValue(IndexField.DB_COMPUTE_ACL_ENTRIES_FIELD) != null) {
			record.setComputeACLEntries((Boolean) document.getFieldValue(IndexField.DB_COMPUTE_ACL_ENTRIES_FIELD));
		}
		if (document.getFieldValue(IndexField.DB_BOOST_FIELD) != null) {
			record.setBoost((Double) document.getFieldValue(IndexField.DB_BOOST_FIELD));
		}
		if (document.getFieldValue(IndexField.PARSED_CONTENT_FIELD) != null) {
			String content = (String) document.getFieldValue(IndexField.PARSED_CONTENT_FIELD);
			ParsedContent parsedContent = new ParsedContent();
			parsedContent.setContent(content);
			parsedContent.setRecord(record);
			record.setParsedContent(parsedContent);
		}
		Object connectorIdObj = document.getFieldValue(IndexField.CONNECTOR_INSTANCE_ID_FIELD);
		if (connectorIdObj != null) {
			Long connectorId = new Long(connectorIdObj.toString());
			ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils.getConnectorInstanceServices();
			ConnectorInstance connectorInstance = connectorInstanceServices.get(connectorId);
			record.setConnectorInstance(connectorInstance);

			for (String fieldName : document.getFieldNames()) {
				if (fieldName.startsWith(IndexField.DB_META_CONTENT_FIELD)) {
					List<String> fieldValueList = (List<String>) document.getFieldValue(fieldName);
					for (String fieldValueStr : fieldValueList) {
						String id = StringUtils.substringBefore(fieldValueStr, "_");
						try {
							String content = StringUtils.substringAfter(fieldValueStr, "_");
							ConnectorInstanceMeta connectorInstanceMeta = connectorInstance.getMeta(new Long(id));
							RecordMeta recordMeta = new RecordMeta();
							recordMeta.setConnectorInstanceMeta(connectorInstanceMeta);
							recordMeta.setContent(content);
							record.addContentMeta(recordMeta);
						} catch (Exception e) {
						}
					}
				} else if (fieldName.startsWith(IndexField.DB_META_EXTERNAL_FIELD)) {
					List<String> fieldValueList = (List<String>) document.getFieldValue(fieldName);
					for (String fieldValueStr : fieldValueList) {
						String id = StringUtils.substringBefore(fieldValueStr, "_");
						try {
							String content = StringUtils.substringAfter(fieldValueStr, "_");
							ConnectorInstanceMeta connectorInstanceMeta = connectorInstance.getMeta(new Long(id));
							RecordMeta recordMeta = new RecordMeta();
							recordMeta.setConnectorInstanceMeta(connectorInstanceMeta);
							recordMeta.setContent(content);
							record.addExternalMeta(recordMeta);
						} catch (Exception e) {
						}
					}
				} else if (fieldName.startsWith(IndexField.DB_RECORD_TAG_SKOS_FIELD)) {
					// recordTag_Skos_true_false
					// recordTag_Free_true_false
					SkosServices skosServices = ConstellioSpringUtils.getSkosServices();
					// String[] split = StringUtils.split(fieldName, "_");
					List<String> ids = (List<String>) document.getFieldValue(fieldName);
					for (String id : ids) {
						String[] split = StringUtils.split(id, "_");
						SkosConcept skosConcept = skosServices.getSkosConcept(new Long(split[0]));
						Boolean manual = Boolean.parseBoolean(split[1]);
						Boolean excluded = Boolean.parseBoolean(split[2]);
						RecordTag recordTag = new RecordTag();
						recordTag.setManual(manual);
						recordTag.setExcluded(excluded);
						recordTag.setSkosConcept(skosConcept);
						record.addRecordTag(recordTag);
					}
				} else if (fieldName.startsWith(IndexField.DB_RECORD_TAG_FREE_FIELD)) {
					FreeTextTagServices freeTextTagServices = ConstellioSpringUtils.getFreeTextTagServices();

					List<String> ids = (List<String>) document.getFieldValue(fieldName);
					for (String id : ids) {
						String[] split = StringUtils.split(id, "_");
						FreeTextTag freeTextTag = freeTextTagServices.get(new Long(split[0]));
						Boolean manual = Boolean.parseBoolean(split[1]);
						Boolean excluded = Boolean.parseBoolean(split[2]);
						RecordTag recordTag = new RecordTag();
						recordTag.setManual(manual);
						recordTag.setExcluded(excluded);
						recordTag.setFreeTextTag(freeTextTag);
						record.addRecordTag(recordTag);
					}
				} else if (fieldName.startsWith(IndexField.DB_ACL_ENTRY_FIELD)) {
					List<String> ids = (List<String>) document.getFieldValue(fieldName);
					for (String id : ids) {
						ACLServices aclServices = ConstellioSpringUtils.getACLServices();
						PolicyACLEntry aclEntry = aclServices.getEntry(new Long(id));
						RecordPolicyACLEntry recordACLEntry = new RecordPolicyACLEntry();
						recordACLEntry.setEntry(aclEntry);
						record.addRecordPolicyACLEntry(recordACLEntry);
					}
				}
			}
		}
		return record;
	}

	public void populateSolrDoc(Record record, SolrInputDocument solrDoc) {
		if (record.getId() == null) {
			record.setId(generateNewId());
		}
		// solrDoc.addField(IndexField.URL_FIELD, t.getUrl());
		// solrDoc.addField(IndexField.UNIQUE_KEY_FIELD, t.getUrl());
		if (record.getAuthmethod() != null) {
			solrDoc.addField(IndexField.DB_AUTHMETHOD_FIELD, record.getAuthmethod());
		}
		// if (t.getMimetype() != null) {
		// solrDoc.addField(IndexField.MIME_TYPE_FIELD, t.getMimetype());
		// }
		if (record.getBoost() != null) {
			solrDoc.addField(IndexField.DB_BOOST_FIELD, record.getBoost());
		}
		// if (t.getParsedContent() != null) {
		// solrDoc.addField(IndexField.PARSED_CONTENT_FIELD,
		// t.getParsedContent().getContent());
		// }
		// if (t.getConnectorInstance() != null) {
		// solrDoc.addField(IndexField.CONNECTOR_INSTANCE_ID_FIELD, "" +
		// t.getConnectorInstance().getId());
		// }
		// if (t.getLastModified() != null) {
		// solrDoc.addField(IndexField.LAST_MODIFIED_FIELD,
		// t.getLastModified());
		// }
		if (record.getLastAutomaticTagging() != null) {
			solrDoc.addField(IndexField.DB_LAST_AUTOMATIC_TAGGING_FIELD, record.getLastAutomaticTagging());
		}
		if (record.getLastFetched() != null) {
			solrDoc.addField(IndexField.DB_LAST_FETCHED_FIELD, record.getLastFetched());
		}
		// if (t.getLastIndexed() != null) {
		// solrDoc.addField(IndexField.LAST_INDEXED_FIELD, t.getLastIndexed());
		// }

		solrDoc.addField(IndexField.DB_COMPUTE_ACL_ENTRIES_FIELD, record.isComputeACLEntries());
		solrDoc.addField(IndexField.DB_DELETED_FIELD, record.isDeleted());
		solrDoc.addField(IndexField.DB_EXCLUDED_FIELD, record.isExcluded());
		solrDoc.addField(IndexField.DB_EXCLUDED_EFFECTIVE_FIELD, record.isExcludedEffective());
		solrDoc.addField(IndexField.DB_UPDATE_INDEX_FIELD, record.isUpdateIndex());

		// if (t.getConnectorInstance() != null) {
		// solrDoc.addField(IndexField.COLLECTION_ID_FIELD, "" +
		// t.getConnectorInstance().getRecordCollection().getId());
		// }
		if (record.getContentMetas() != null) {
			for (RecordMeta recordMeta : record.getContentMetas()) {
				solrDoc.addField(IndexField.DB_META_CONTENT_FIELD, recordMeta.getConnectorInstanceMeta().getId() + "_" + recordMeta.getContent());
			}
		}
		if (record.getExternalMetas() != null) {
			for (RecordMeta recordMeta : record.getExternalMetas()) {
				solrDoc.addField(IndexField.DB_META_EXTERNAL_FIELD, recordMeta.getConnectorInstanceMeta().getId() + "_" + recordMeta.getContent());
			}
		}
		if (record.getRecordTags() != null) {
			for (RecordTag recordTag : record.getRecordTags()) {
				StringBuffer sb = new StringBuffer();

				String id = "";
				FreeTextTag freeTextTag = recordTag.getFreeTextTag();
				if (freeTextTag != null) {
					id = "" + freeTextTag.getId();
					sb.append(IndexField.DB_RECORD_TAG_FREE_FIELD);
				}
				SkosConcept skosConcept = recordTag.getSkosConcept();
				if (skosConcept != null) {
					id = "" + freeTextTag.getId();
					sb.append(IndexField.DB_RECORD_TAG_SKOS_FIELD);
				}
				// sb.append(recordTag.getManual() + "_");
				// sb.append(recordTag.getExcluded());
				String fieldName = sb.toString();
				solrDoc.addField(fieldName, id + "_" + Boolean.TRUE.equals(recordTag.getManual()) + "_" + Boolean.TRUE.equals(recordTag.getExcluded()));
			}
		}
		if (record.getRecordPolicyACLEntries() != null) {
			for (RecordPolicyACLEntry recordPolicyACL : record.getRecordPolicyACLEntries()) {
				solrDoc.addField(IndexField.DB_ACL_ENTRY_FIELD, recordPolicyACL.getId());
			}
		}
	}

	public Record makePersistent(Record record, SolrServer solrServer) {
		RecordCollection collection = record.getConnectorInstance().getRecordCollection();
		List<Record> asList = new ArrayList<Record>();
		asList.add(record);
		makePersistent(asList, collection, solrServer, false);
		return record;
	}

	@Override
	public Record get(Long id, RecordCollection collection) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		return get(id, collection, solrServer);
	}

	private int count(String solrQuery, SolrServer solrServer) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set(CommonParams.Q, solrQuery);
		solrParams.set(ConstellioAuthorizationComponent.ENABLE, false);
		solrParams.set(ConstellioElevationComponent.ENABLE, false);

		QueryResponse queryResponse = null;
		try {
			queryResponse = solrServer.query(solrParams);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}

		return (int) queryResponse.getResults().getNumFound();
	}

	private List<Record> list(String solrQuery, SolrServer solrServer) {
		return list(solrQuery, solrServer, Integer.MAX_VALUE);
	}

	private List<Record> list(String solrQuery, SolrServer solrServer, int rows) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set(CommonParams.Q, solrQuery);
		solrParams.set(ConstellioAuthorizationComponent.ENABLE, false);
		solrParams.set(ConstellioElevationComponent.ENABLE, false);
		if (rows > 0) {
			solrParams.set(CommonParams.ROWS, rows);
		}
		return new LazyLoadSolrRecordList(solrParams, solrServer);
		//
		// List<Record> records = new ArrayList<Record>();
		// QueryResponse queryResponse = null;
		// try {
		// queryResponse = solrServer.query(solrParams);
		// } catch (SolrServerException e) {
		// throw new RuntimeException(e);
		// }
		// Iterator<SolrDocument> iter = queryResponse.getResults().iterator();
		//
		// while (iter.hasNext()) {
		// SolrDocument resultDoc = iter.next();
		// records.add(populateRecord(resultDoc));
		//
		// }
		// return records;
	}

	@Override
	public List<Record> list(Collection<Number> ids, RecordCollection collection) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		StringBuffer solrQuery = new StringBuffer();
		solrQuery.append(IndexField.RECORD_ID_FIELD);
		solrQuery.append(":(");
		for (Iterator<Number> it = ids.iterator(); it.hasNext();) {
			Number id = it.next();
			solrQuery.append(id);
			if (it.hasNext()) {
				solrQuery.append(" OR ");
			}
		}
		solrQuery.append(")");
		return list(solrQuery.toString(), solrServer);
	}

	private Record get(String solrQuery, SolrServer solrServer) {
		List<Record> results = list(solrQuery, solrServer, 1);
		return !results.isEmpty() ? results.get(0) : null;
	}

	private void delete(String solrQuery, SolrServer solrServer) {
		try {
			solrServer.deleteByQuery(solrQuery);
			// solrServer.commit();
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Record get(Long id, RecordCollection collection, SolrServer solrServer) {
		return get(IndexField.RECORD_ID_FIELD + ":" + id, solrServer);
	}

	@Override
	public Record get(String url, ConnectorInstance connectorInstance) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(connectorInstance.getRecordCollection());
		return get(url, connectorInstance, solrServer);
	}

	public Record get(String url, ConnectorInstance connectorInstance, SolrServer solrServer) {
		return get(IndexField.URL_FIELD + ":" + ClientUtils.escapeQueryChars(url) + " " + IndexField.CONNECTOR_INSTANCE_ID_FIELD + ":" + connectorInstance.getId(), solrServer);
	}

	@Override
	public Record get(String url, RecordCollection collection) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		return get(url, collection, solrServer);
	}

	public Record get(String url, RecordCollection collection, SolrServer solrServer) {
		return get(IndexField.URL_FIELD + ":" + ClientUtils.escapeQueryChars(url), solrServer);
	}

	@Override
	public Record makePersistent(Record t) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(t.getConnectorInstance().getRecordCollection());
		return makePersistent(t, solrServer);
	}

	@Override
	public Record makeTransient(Record t) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(t.getConnectorInstance().getRecordCollection());
		return makeTransient(t, solrServer);
	}

	private Record makeTransient(Record t, SolrServer solrServer) {
		try {
			solrServer.deleteById(t.getUrl());
			// solrServer.commit();
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return t;
	}

	@Override
	public int count(ConnectorInstance connectorInstance) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(connectorInstance);
		return count(connectorInstance, solrServer);
	}

	public int count(ConnectorInstance connectorInstance, SolrServer solrServer) {
		return count(IndexField.CONNECTOR_INSTANCE_ID_FIELD + ":" + connectorInstance.getId(), solrServer);
	}

	@Override
	public int count(RecordCollection collection) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		return count(collection, solrServer);
	}

	public int count(RecordCollection collection, SolrServer solrServer) {
		return count("*:*", solrServer);
	}

	@Override
	public List<Record> list(RecordCollection collection) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		return list(collection, solrServer);
	}

	public List<Record> list(RecordCollection collection, SolrServer solrServer) {
		return list("*:*", solrServer);
	}

	@Override
	public List<Record> list(ConnectorInstance connectorInstance) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(connectorInstance);
		return list(connectorInstance, solrServer);
	}

	public List<Record> list(ConnectorInstance connectorInstance, SolrServer solrServer) {
		return list(IndexField.CONNECTOR_INSTANCE_ID_FIELD + ":" + connectorInstance.getId(), solrServer);
	}

	@Override
	public List<Record> listExcluded(RecordCollection collection) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		return list(IndexField.DB_EXCLUDED_FIELD + ":true", solrServer);
	}

	/**
	 * Mettre à jour tous les documents de la collection dans Solr
	 * 
	 * @see com.doculibre.constellio.services.RecordServices#markRecordsForUpdateIndex(com.doculibre.constellio.entities.RecordCollection)
	 */
	@Override
	public void markRecordsForUpdateIndex(RecordCollection collection) {

		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		StatusManager.reindexingAll(collection);
		List<Record> records = list(collection);
		int initialCount = records.size();
		if (initialCount > 0) {
			String collectionCoreName = collection.getName();
			String tempCoreName = collectionCoreName + "_" + UUID.randomUUID().toString();

			File coresRootDir = SolrCoreContext.getSolrCoresRootDir();
			// File baseCoreDir = SolrCoreContext.getSolrCoreRootDir(null);

			File collectionCoreDir = SolrCoreContext.getSolrCoreRootDir(collectionCoreName);
			// File collectionCoreConfDir = new File(collectionCoreDir, "conf");
			// File collectionCoreDataDir = new File(collectionCoreDir, "data");
			// File collectionCoreLogsDir = new File(collectionCoreDir, "logs");
			// File collectionCoreElevateFile = new File(collectionCoreDataDir,
			// "elevate.xml");

			File tempCoreDir = new File(coresRootDir, tempCoreName);
			// File tempCoreConfDir = new File(tempCoreDir, "conf");
			File tempCoreDataDir = new File(tempCoreDir, "data");
			File tempCoreIndexDir = new File(tempCoreDataDir, "index");
			// File tempCoreLogsDir = new File(tempCoreDir, "logs");
			// File tempCoreElevateFile = new File(tempCoreDataDir,
			// "elevate.xml");
			File tempCoreWriteLockFile = new File(tempCoreIndexDir, "write.lock");

			try {
				FileUtils.copyDirectory(collectionCoreDir, tempCoreDir);
				FileUtils.deleteDirectory(tempCoreIndexDir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			// Ajouter un coeur temporaire
			rewriteSolrXml(tempCoreName, tempCoreName, false);
			SolrCoreContext.init();

			// Utiliser le coeur temporaire pour indexer
			SolrServer tempSolrServer = SolrCoreContext.getSolrServerUtil(tempCoreName);
			makePersistent(records, collection, tempSolrServer, true);
			// while (count(collection, tempSolrServer) == 0) {
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// throw new RuntimeException(e);
			// }
			// }

			// Supprimer le coeur temporaire
			SolrCoreContext.removeCore(tempCoreName);
			if (tempCoreWriteLockFile.exists()) {
				tempCoreWriteLockFile.delete();
			}
			rewriteSolrXml(tempCoreName, null, true);
			// Faire pointer le coeur de la collection vers le dossier du
			// coeur temporaire
			rewriteSolrXml(collectionCoreName, tempCoreName, false);

			SolrCoreContext.init();

			collection.setInstanceDirName(tempCoreName);
			collectionServices.makePersistent(collection, false);
			//
			// collectionCoreDir.renameTo(new File(coresRootDir,
			// collectionCoreName + ".bak"));
		}
	}

	@SuppressWarnings("unchecked")
	private static void rewriteSolrXml(String coreName, String instanceDir, boolean remove) {
		File coresRootDir = SolrCoreContext.getSolrCoresRootDir();
		File solrXmlFile = new File(coresRootDir, "solr.xml");
		Document solrXmlDocument;
		try {
			solrXmlDocument = new SAXReader().read(solrXmlFile);
			Element coresElement = solrXmlDocument.getRootElement().element("cores");
			for (Iterator<Element> it = coresElement.elementIterator("core"); it.hasNext();) {
				Element coreElement = it.next();
				if (coreName.equals(coreElement.attributeValue("name"))) {
					it.remove();
					break;
				}
			}
			if (!remove) {
				Element coreElement = DocumentHelper.createElement("core");
				coresElement.add(coreElement);
				coreElement.addAttribute("name", coreName);
				coreElement.addAttribute("instanceDir", instanceDir);
			}

			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(new FileOutputStream(solrXmlFile), format);
			writer.write(solrXmlDocument);
			writer.close();
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Supprimer tous les documents pour la collection dans Solr
	 * 
	 * @see com.doculibre.constellio.services.RecordServices#markRecordsForDeletion(com.doculibre.constellio.entities.RecordCollection)
	 */
	@Override
	public void markRecordsForDeletion(RecordCollection collection) {
		deleteRecords(collection);
	}

	/**
	 * Mettre le flag excluded à true pour ce record
	 * 
	 * @see com.doculibre.constellio.services.RecordServices#markRecordForExclusion(com.doculibre.constellio.entities.Record)
	 */
	@Override
	public void markRecordForExclusion(Record record) {
		record.setExcluded(true);
		record.setExcludedEffective(true);
		makePersistent(record);
	}

	@Override
	public void markRecordAsExcluded(Record record) {
		// Nothing to do, already excluded
	}

	@Override
	public void cancelExclusion(Record record) {
		record.setExcluded(false);
		record.setExcludedEffective(false);
		makePersistent(record);
	}

	@Override
	public void deleteRecords(ConnectorInstance connectorInstance) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(connectorInstance.getRecordCollection());
		deleteRecords(connectorInstance, solrServer);
	}

	public void deleteRecords(ConnectorInstance connectorInstance, SolrServer solrServer) {
		delete(IndexField.CONNECTOR_INSTANCE_ID_FIELD + ":" + connectorInstance.getId(), solrServer);
	}

	@Override
	public void deleteRecords(RecordCollection collection) {
		StatusManager.deletingAll(collection);
		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		deleteRecords(collection, solrServer);
	}

	public void deleteRecords(RecordCollection collection, SolrServer solrServer) {
		delete("*:*", solrServer);
	}

	/**
	 * Ne devrait jamais retourner de résultat car l'index est mis à jour en
	 * temps réel.
	 * 
	 * @see com.doculibre.constellio.services.RecordServices#getPendingExclusions(com.doculibre.constellio.entities.RecordCollection)
	 */
	@Override
	public List<Record> getPendingExclusions(RecordCollection collection) {
		return new ArrayList<Record>();
	}

	@Override
	public void deleteAutomaticRecordTags(RecordCollection collection, Date newStartTaggingDate) {
		List<Record> records = list(collection);
		for (Record record : records) {
			boolean modified = false;
			for (Iterator<RecordTag> it = record.getRecordTags().iterator(); it.hasNext();) {
				RecordTag recordTag = it.next();
				if (!recordTag.isManual()) {
					it.remove();
					modified = true;
				}
			}
			if (modified) {
				makePersistent(record);
			}
		}
	}

	/**
	 * @see com.doculibre.constellio.services.RecordServices#markRecordsForComputeACLEntries(com.doculibre.constellio.entities.RecordCollection)
	 */
	@Override
	public void markRecordsForComputeACLEntries(RecordCollection collection) {
		List<Record> records = list(collection);
		for (Record record : records) {
			record.setComputeACLEntries(true);
			makePersistent(record);
		}
	}

	/**
	 * @see com.doculibre.constellio.services.RecordServices#getConnectorInstances(java.util.List)
	 */
	@Override
	public List<ConnectorInstance> getConnectorInstances(List<Record> records) {
		List<ConnectorInstance> listConnectors = new ArrayList<ConnectorInstance>();
		for (Record record : records) {
			ConnectorInstance connectorInstance = record.getConnectorInstance();
			listConnectors.add(connectorInstance);
		}
		return listConnectors;
	}

	@Override
	public boolean isRemoveable(Record t) {
		return true;
	}

	/**
	 * @see com.doculibre.constellio.services.RecordServices#computeBoost(com.doculibre.constellio.entities.Record)
	 */
	@Override
	public Float computeBoost(Record record) {
		// 1. Calcul du boost du a l appartenance a la collection
		RecordCollection collection = record.getConnectorInstance().getRecordCollection();
		RecordCollectionServices recordCollectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		Float boostAssociatedWithCollection = recordCollectionServices.getBoost(collection, record).floatValue();
		// faire le produit des boosts des sous collections
		FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
		if (collection.isFederationOwner()) {
			List<RecordCollection> includedCollections = federationServices.listIncludedCollections(collection);
			for (RecordCollection includedCollection : includedCollections) {
				boostAssociatedWithCollection *= recordCollectionServices.getBoost(includedCollection, record).floatValue();
			}
		}

		// faire le produite des boosts des collections mères!!!
		if (collection.isIncludedInFederation()) {
			List<RecordCollection> ownerCollections = federationServices.listOwnerCollections(collection);
			for (RecordCollection includedCollection : ownerCollections) {
				boostAssociatedWithCollection *= recordCollectionServices.getBoost(includedCollection, record).floatValue();
			}
		}

		// 2. Boost fourni par le connecteur
		// FIXME quoi faire pour une collection fédérée?
		List<RecordMeta> boosts = record.getMetas(IndexFieldServices.BOOST_FIELD_PREFIX);
		Float boostAssociatedWithConnector = 1.0f;
		if (boosts != null) {
			for (RecordMeta boost : boosts) {
				boostAssociatedWithConnector *= Float.valueOf(boost.getContent());
			}
		}
		return boostAssociatedWithCollection * boostAssociatedWithConnector;
	}

	/**
	 * @see com.doculibre.constellio.services.RecordServices#computeFieldBoost(com.doculibre.constellio.entities.Record,
	 *      com.doculibre.constellio.entities.IndexField)
	 */
	@Override
	public Float computeFieldBoost(Record record, IndexField indexField) {
		Float boostAssociatedWithIndexField = 1.0f;
		String indexFieldName = indexField.getName();

		Set<String> indexFieldAssociatedMetaNames = new HashSet<String>();
		if (indexFieldName.equals(IndexField.UNIQUE_KEY_FIELD)) {
			indexFieldAssociatedMetaNames.add(SpiConstants.PROPNAME_DOCID);
		} else {
			if (indexFieldName.equals(IndexField.DEFAULT_SEARCH_FIELD)) {
				indexFieldAssociatedMetaNames.add(SpiConstants.PROPNAME_CONTENT);
			} else {
				indexFieldAssociatedMetaNames = indexField.getMetaNames();
			}

		}

		for (String metaName : indexFieldAssociatedMetaNames) {
			List<RecordMeta> boosts = record.getMetas(IndexFieldServices.BOOST_FIELD_PREFIX + metaName);

			if (boosts != null) {
				for (RecordMeta boost : boosts) {
					boostAssociatedWithIndexField *= Float.valueOf(boost.getContent());
				}
			}
		}

		// Ajout du boost des fields ajoutés via l'interface de gestion de la
		// pertinence des champs
		Float boostFieldAddedThrowInterface = indexField.getBoost();
		if (boostFieldAddedThrowInterface == null) {
			boostFieldAddedThrowInterface = new Float(0F);
		}

		return boostAssociatedWithIndexField * boostFieldAddedThrowInterface;
	}

	@Override
	public Record merge(Record t) {
		return makePersistent(t);
	}

	@Override
	public void clear() {
	}

	@Override
	public void flush() {
	}

	@Override
	public List<Record> list() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Record> list(Collection<Number> ids) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Record> list(int maxResults) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Record> list(String orderByProperty, Boolean orderByAsc) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Record> list(String orderByProperty, Boolean orderByAsc, int maxResults) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Record> list(Map<String, Object> criteria) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Record> list(Map<String, Object> criteria, int maxResults) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Record> list(Map<String, Object> criteria, String orderByProperty, Boolean orderByAsc) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Record> list(Map<String, Object> criteria, String orderByProperty, Boolean orderByAsc, int maxResults) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Record get(Long id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Record get(Map<String, Object> criteria) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int countMarkedForUpdateIndex(RecordCollection collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int countMarkedForExclusionOrDeletion(RecordCollection collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Record> listMarkedForUpdateIndex(RecordCollection collection, int maxResults) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Record> listMarkedForExclusionOrDeletion(RecordCollection collection, int maxResults) {
		return new ArrayList<Record>();
	}

	public static void main(String[] args) {
		String fieldName = "recordTag_Skos_25_true_false";
		String id = fieldName.substring(fieldName.lastIndexOf("_") + 1, fieldName.length());
		System.out.println(id);
	}

	@Override
	public List<Record> listTraversedRecordsSince(ConnectorInstance connectorInstance, Date startDate) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(connectorInstance);
		StringBuffer solrQuery = new StringBuffer();
		solrQuery.append(IndexField.CONNECTOR_INSTANCE_ID_FIELD);
		solrQuery.append(":");
		solrQuery.append(connectorInstance.getId());
		solrQuery.append(" ");
		solrQuery.append(IndexField.DB_LAST_FETCHED_FIELD);
		solrQuery.append(":[");
		solrQuery.append(DateUtil.getThreadLocalDateFormat().format(startDate));
		solrQuery.append("TO *]");
		return list(solrQuery.toString(), solrServer);
	}

	@Override
	public List<Record> listIndexedRecordsSince(RecordCollection collection, Date startDate) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		StringBuffer solrQuery = new StringBuffer();
		solrQuery.append(IndexField.COLLECTION_ID_FIELD);
		solrQuery.append(":");
		solrQuery.append(collection.getId());
		solrQuery.append(" ");
		solrQuery.append(IndexField.LAST_INDEXED_FIELD);
		solrQuery.append(":[");
		solrQuery.append(DateUtil.getThreadLocalDateFormat().format(startDate));
		solrQuery.append("TO *]");
		return list(solrQuery.toString(), solrServer);
	}

	@Override
	public List<Record> listLastTraversedRecords(ConnectorInstance connectorInstance, int maxSize) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(connectorInstance);
		List<Record> records = new ArrayList<Record>();
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set(CommonParams.Q, "*:*");
		solrParams.set(ConstellioAuthorizationComponent.ENABLE, false);
		solrParams.set(ConstellioElevationComponent.ENABLE, false);
		solrParams.set("start", 0);
		solrParams.set("rows", maxSize);
		solrParams.set("sort", IndexField.DB_LAST_FETCHED_FIELD + " desc");
		QueryResponse queryResponse = null;
		try {
			queryResponse = solrServer.query(solrParams);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
		Iterator<SolrDocument> iter = queryResponse.getResults().iterator();

		while (iter.hasNext()) {
			SolrDocument resultDoc = iter.next();
			records.add(populateRecord(resultDoc));

		}
		return records;
	}

	@Override
	public void makePersistent(List<Record> records, ConnectorInstance connectorInstance) {
		SolrServer solrServer = SolrCoreContext.getSolrServer(connectorInstance);
		makePersistent(records, connectorInstance.getRecordCollection(), solrServer, false);
	}

	// @Override
	// private void makePersistent(List<Record> records, RecordCollection
	// collection) {
	// SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
	// makePersistent(records, collection, solrServer);
	// }

	// private void makePersistent(List<Record> records, RecordCollection
	// collection, final SolrServer solrServer) {
	// makePersistent(records, collection, solrServer, false);
	// }

	private void makePersistent(List<Record> records, RecordCollection collection, final SolrServer solrServer, final boolean commit) {
		// Diviser les records en blocs de 1000
		//TODO Break up the transaction
		final int recordCount = records.size();
		final MutableInt counter = new MutableInt();
		List<List<Record>> partitions = PartitionnedList.partition(records, 1000);
		for (final List<Record> partition : partitions) {
			final List<SolrInputDocument> solrDocs = new ArrayList<SolrInputDocument>();

			try {
				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				for (Record record : partition) {
					SolrInputDocument solrDoc = new SolrInputDocument();
					// Peupler les champs de persistance
					populateSolrDoc(record, solrDoc);

					IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
					// Peupler les champs liés aux catégorisation
					indexFieldServices.populateSolrDoc(solrDoc, record, collection);

					solrDoc.setField(IndexField.LAST_INDEXED_FIELD, new Date());
					solrDocs.add(solrDoc);

					counter.increment();
					StatusManager.indexing(record, new Date(), collection);
					int sizeProcessed = counter.intValue();
					if (sizeProcessed % 1000 == 0) {
						LOGGER.debug("Indexing  " + sizeProcessed + " of " + recordCount);
					}
				}

				LOGGER.debug("Adding " + partition.size() + " documents");
				solrServer.add(solrDocs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (commit) {
			LOGGER.debug("Committing");
			commit(solrServer);
		}
	}

	private static void commit(SolrServer solrServer) {
		try {
			solrServer.commit(true, true);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
