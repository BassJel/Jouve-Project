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
package com.doculibre.constellio.feedprotocol;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.tika.fork.ForkParser;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.entities.RawContent;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordMeta;
import com.doculibre.constellio.entities.RecordTag;
import com.doculibre.constellio.feedprotocol.model.Feed;
import com.doculibre.constellio.feedprotocol.model.FeedContent;
import com.doculibre.constellio.feedprotocol.model.FeedGroup;
import com.doculibre.constellio.feedprotocol.model.FeedMeta;
import com.doculibre.constellio.feedprotocol.model.FeedMetadata;
import com.doculibre.constellio.feedprotocol.model.FeedRecord;
import com.doculibre.constellio.feedprotocol.tika.parsers.EmlParser;
import com.doculibre.constellio.lang.LangDetectorUtil;
import com.doculibre.constellio.services.ConnectorInstanceServices;
import com.doculibre.constellio.services.FreeTextTagServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.status.StatusManager;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceUtils;
import com.ibm.icu.util.StringTokenizer;
import java.io.File;

public class FeedProcessor {

	private static final Logger LOG = Logger.getLogger(FeedProcessor.class
			.getName());

	private final Feed feed;

	private static final Set<Long> deletingConnectorInstanceIds = new HashSet<Long>();

	private static final ForkParser PARSER = new ForkParser();

	 static {
		 PARSER.setPoolSize(8);
		 PARSER.setJavaCommand("java -Xmx256m");
	 }
	 
	public FeedProcessor(Feed feed) {
		this.feed = feed;
	}

	public static void deleting(ConnectorInstance connectorInstance) {
		deletingConnectorInstanceIds.add(connectorInstance.getId());
	}

	private static synchronized boolean checkValid(Long connectorInstanceId) {
		boolean valid = !deletingConnectorInstanceIds
				.contains(connectorInstanceId);
		if (!valid) {
			ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils
					.getConnectorInstanceServices();
			ConnectorInstance connectorInstance = connectorInstanceServices
					.get(connectorInstanceId);
			// Delete effective
			if (connectorInstance == null) {
				deletingConnectorInstanceIds.remove(connectorInstanceId);
			}
		}
		return valid;
	}

	private String feedCollectionName() {
		try {
			ConstellioPersistenceUtils.beginTransaction();
			ConnectorInstance connectorInstance = retrieveConnectorInstance();
			return connectorInstance.getRecordCollection().getName();
		} finally {
			ConstellioPersistenceUtils.finishTransaction(true);
		}
	}

	public void processFeed() throws FeedException {
		RecordServices recordServices = ConstellioSpringUtils
				.getRecordServices();
		String collectionName = feedCollectionName();
		ReadWriteLock collectionLock = recordServices.getLock(collectionName);
		collectionLock.readLock().lock();
		try {
			ConstellioPersistenceUtils.beginTransaction();

			ConnectorInstance connectorInstance = retrieveConnectorInstance();
			if (connectorInstance == null) {
				throw new FeedException(
						"Feed refers to an invalid datasource: "
								+ feed.getDatasource());
			} else if (checkValid(connectorInstance.getId())) {
				if (feed.getFeedtype() == Feed.FEEDTYPE.FULL) {
					deleteRecordsForConnectorInstance(connectorInstance);
				}

				List<Record> persistentRecords = new ArrayList<Record>();
				final Long connectorInstanceId = connectorInstance.getId();
				for (FeedGroup group : feed.getGroups()) {
					final FeedGroup.ACTION groupAction = group.getAction();
					for (final FeedRecord record : group.getRecords()) {
						// Connector may be deleted at any time...
						if (checkValid(connectorInstanceId)) {
							processRecord(record, groupAction,
									connectorInstanceId, persistentRecords);
						}
					}
				}
				recordServices.makePersistent(persistentRecords,
						connectorInstance);

				SolrServer solrServer = SolrCoreContext
						.getSolrServer(connectorInstance);
				try {
					solrServer.commit();
				} catch (Throwable e) {
					try {
						solrServer.rollback();
					} catch (SolrServerException e1) {
						throw new RuntimeException(e);
					} catch (IOException e1) {
						throw new RuntimeException(e);
					}
				}
			}
		} finally {
			try {
				ConstellioPersistenceUtils.finishTransaction(true);
			} finally {
				collectionLock.readLock().unlock();
			}
		}
	}

	private ConnectorInstance retrieveConnectorInstance() {
		ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils
				.getConnectorInstanceServices();
		return connectorInstanceServices.get(feed.getDatasource());
	}

	private void deleteRecordsForConnectorInstance(
			ConnectorInstance connectorInstance) {
		final RecordServices recordServices = ConstellioSpringUtils
				.getRecordServices();
		recordServices.deleteRecords(connectorInstance);
	}

	private void processRecord(FeedRecord record, FeedGroup.ACTION groupAction,
			Long connectorInstanceId, List<Record> persistentRecords) {
		final ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils
				.getConnectorInstanceServices();
		final ConnectorInstance connectorInstance = connectorInstanceServices
				.get(connectorInstanceId);
		if (record.getAction() == FeedRecord.ACTION.ADD) {
			this.addRecord(feed, record, connectorInstance, persistentRecords);
		} else if (record.getAction() == FeedRecord.ACTION.DELETE) {
			this.deleteRecord(record, connectorInstance);
		} else {
			if (groupAction == FeedGroup.ACTION.ADD) {
				this.addRecord(feed, record, connectorInstance,
						persistentRecords);
			} else if (groupAction == FeedGroup.ACTION.DELETE) {
				this.deleteRecord(record, connectorInstance);
			}
		}
	}

	private void addRecord(Feed feed, FeedRecord feedRecord,
			ConnectorInstance connectorInstance, List<Record> persistentRecords) {
		try {
			final RecordServices recordServices = ConstellioSpringUtils
					.getRecordServices();
			final Record existingRecord = recordServices.get(
					feedRecord.getUrl(), connectorInstance);

			if (existingRecord != null) {
				if (feed.getFeedtype() == Feed.FEEDTYPE.INCREMENTAL) {
					if (CollectionUtils.isNotEmpty(feedRecord.getContents())) {
						// Updates content
						// RawContentServices rawContentServices =
						// ConstellioSpringUtils.getRawContentServices();
						// List<FeedContent> feedContent =
						// feedRecord.getContents();
						// rawContentServices.setRawContents(existingRecord,
						// rawContents);

						// if (this.isMetadataAndUrl(rawContents)) {
						// FIXME
						// Google Connector Manager generated the content !
						// See
						// https://code.google.com/p/google-enterprise-connector-manager/issues/detail?id=229
						// Skip content and update connector metadata only
						// } else {
						ContentParse contentParse = this.asContentParse(
								feedRecord.getUrl(), feedRecord.getContents(),
								connectorInstance);
						if(!contentParse.getContent().isEmpty()){
							final String parsedContentString = contentParse.getContent();
							existingRecord.setParsedContent(contentParse.getContent());
							existingRecord.setMd5(contentParse.getMd5());

							// Add language
							String lang = LangDetectorUtil.getInstance().detect(
									parsedContentString);
							// if
							// (feedRecord.getMimetype().toLowerCase().contains("html")
							// && StringUtils.isNotBlank(rawContentString)) {
							// final HtmlLangDetector htmlLangDetector = new
							// HtmlLangDetector();
							// String rawText =
							// lang = htmlLangDetector.getLang(rawContentString);
							// if (StringUtils.isBlank(lang)) {
							// lang =
							// LangDetectorUtil.getInstance().detect(parsedContentString);
							// }
							// } else {
							// lang =
							// LangDetectorUtil.getInstance().detect(parsedContentString);
							// }
							// Update language
							existingRecord.setLang(lang);
							
							System.out.println(parsedContentString);
							System.out.println("DETECT LANG "+lang);
							
							// Reset metadata
							existingRecord.getContentMetas().clear();
							for (RecordMeta meta : contentParse.getMetas()) {
								existingRecord.addContentMeta(meta);
							}
							// }
						}
					}
					if (CollectionUtils.isNotEmpty(feedRecord.getMetadatas())) {
						// Updates metadatas
						// Reset metadata
						existingRecord.getExternalMetas().clear();
						for (RecordMeta meta : asMetaSet(
								feedRecord.getMetadatas(), connectorInstance)) {
							ConnectorInstanceMeta connectorInstanceMeta = meta
									.getConnectorInstanceMeta();
							if (connectorInstanceMeta.getName().equals(
									ConstellioFeedConstants.PROPNAME_BOOST)) {
								try {
									final double boost = Double
											.parseDouble(meta.getContent());
									existingRecord.setBoost(boost);
								} catch (NumberFormatException e) {
									LOG.severe("Could not format boost value to double for record : "
											+ feedRecord.getUrl());
								}
							}
							existingRecord.addExternalMeta(meta);
						}
					}
					// FIXME Not sure what to do here. The documentation is not
					// clear.
					existingRecord.setAuthmethod(feedRecord.getAuthmethod()
							.toString());
					existingRecord.setLastModified(feedRecord.getLastModified()
							.getTime());
					existingRecord.setLastFetched(new Date());
					existingRecord.setUpdateIndex(true);
					existingRecord.setMimetype(feedRecord.getMimetype());

					// Remove non-manual tags
					for (Iterator<RecordTag> tagIt = existingRecord
							.getRecordTags().iterator(); tagIt.hasNext();) {
						RecordTag tag = tagIt.next();
						if (!tag.isManual()) {
							tagIt.remove();
						}
					}
					// Add keywords
					List<String> keywords = existingRecord
							.getMetaContents("Keywords");
					if (keywords != null) {
						FreeTextTagServices freeTextTagServices = ConstellioSpringUtils
								.getFreeTextTagServices();
						for (String keyword : keywords) {
							StringTokenizer st = new StringTokenizer(keyword,
									",");
							while (st.hasMoreTokens()) {
								String keywordToken = st.nextToken().trim();
								if (StringUtils.isNotBlank(keywordToken)) {
									FreeTextTag tag = freeTextTagServices
											.get(keywordToken);
									if (tag == null) {
										tag = new FreeTextTag();
										tag.setFreeText(keywordToken);
										freeTextTagServices.makePersistent(tag);
									}
									RecordTag recordTag = new RecordTag();
									recordTag.setFreeTextTag(tag);
									recordTag.setRecord(existingRecord);
									existingRecord.getRecordTags().add(
											recordTag);
								}
							}
						}
					}

					try {
						// recordServices.merge(existingRecord);
						persistentRecords.add(existingRecord);
						StatusManager.traversing(existingRecord, false);
					} catch (Exception e) {
						LOG.log(Level.SEVERE,
								"Exception while trying to merge existing record",
								e);
					}
				} else {
					Record record = this
							.asRecord(feedRecord, connectorInstance);
					record.setId(existingRecord.getId());
					try {
						// recordServices.merge(record);
						persistentRecords.add(record);
						StatusManager.traversing(record, false);
					} catch (Exception e) {
						LOG.log(Level.SEVERE,
								"Exception while trying to merge record", e);
					}
				}
			} else {
				Record record = this.asRecord(feedRecord, connectorInstance);
				try {
					// recordServices.makePersistent(record);
					persistentRecords.add(record);
					StatusManager.traversing(record, true);
				} catch (Exception e) {
					LOG.log(Level.SEVERE,
							"Exception while trying to make record persistent",
							e);
				}
			}
		} catch (Throwable e) {
			LOG.log(Level.SEVERE, "Exception while trying to add record", e);
		}
	}

	private void deleteRecord(FeedRecord feedRecord,
			ConnectorInstance connectorInstance) {
		final RecordServices recordServices = ConstellioSpringUtils
				.getRecordServices();
		final Record record = recordServices.get(feedRecord.getUrl(),
				connectorInstance);
		if (record != null) {
			record.setDeleted(true);
			recordServices.makeTransient(record);
		}
	}

	// private Set<RawContent> asRawContents(List<FeedContent> feedContents) {
	// final Set<RawContent> rawContents = new HashSet<RawContent>();
	// for (FeedContent feedContent : feedContents) {
	// RawContent rawContent = new RawContent();
	// if (feedContent.getEncoding() == FeedContent.ENCODING.BASE64BINARY) {
	// byte[] decodedContent = new byte[0];
	// try {
	// decodedContent = Base64.decode(feedContent.getValue());
	// } catch (Base64DecoderException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// rawContent.setContent(decodedContent);
	// } else {
	// rawContent.setContent(feedContent.getValue().getBytes());
	// }
	// rawContents.add(rawContent);
	// }
	// return rawContents;
	// }

	private ContentParse asContentParse(String url,
			List<FeedContent> feedContents, ConnectorInstance connectorInstance) {
		ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils
				.getConnectorInstanceServices();
		ContentParse contentParse = new ContentParse();
		List<RecordMeta> metas = new ArrayList<RecordMeta>();
		contentParse.setMetas(metas);

		List<String> md5s = new ArrayList<String>();
		StringBuffer contentBuffer = new StringBuffer();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			throw new RuntimeException(e1);
		}
		for (FeedContent feedContent : feedContents) {
			InputStream input = null;
			try {
				Metadata metadata = new Metadata();
				ContentHandler handler = new BodyContentHandler(-1);
				ParseContext parseContext = new ParseContext();

				if (feedContent.getEncoding() == FeedContent.ENCODING.BASE64BINARY) {
					input = new BufferedInputStream(new Base64InputStream(new FileInputStream(feedContent.getValue()), false, 80,
							new byte[] { (byte) '\n' }));
				} else {
					input = new BufferedInputStream(new FileInputStream(feedContent.getValue()));
				}
				// MD5 on the fly
				DigestInputStream dis = new DigestInputStream(input, md);

				if (connectorInstance.getConnectorType().getName()
						.equals("mailbox-connector")) {
					// FIXME : a supprimer et ajouter un Detector qui detecte
					// correctement les fichiers eml
					// CompositeParser parser = new AutoDetectParser();
					// Map<String, Parser> parsers = parser.getParsers();
					// parsers.put("text/plain", new
					// EmlParser());//message/rfc822
					// parser.setParsers(parsers);
					// Autre pb avec detection des fichiers eml
					Parser parser = new EmlParser();
					parser.parse(dis, handler, metadata, parseContext);
				} else {
					// IOUtils.copy(input, new FileOutputStream(new
					// File("C:/tmp/test.pdf")));
					PARSER.parse(dis, handler, metadata, parseContext);
				}

				md5s.add(Base64.encodeBase64String(md.digest()));

				for (String name : metadata.names()) {
					for (String content : metadata.getValues(name)) {
						if (!"null".equals(content)) {
							RecordMeta meta = new RecordMeta();
							ConnectorInstanceMeta connectorInstanceMeta = connectorInstance
									.getOrCreateMeta(name);
							if (connectorInstanceMeta.getId() == null) {
								connectorInstanceServices
										.makePersistent(connectorInstance);
							}
							meta.setConnectorInstanceMeta(connectorInstanceMeta);
							meta.setContent(content);
							metas.add(meta);
						}
					}
				}

				String contentString = handler.toString();
				// remove the duplication of white space, Bin
				contentBuffer.append(contentString.replaceAll("(\\s){2,}","$1"));
			} catch (Throwable e) {
				LOG.warning("Could not parse document "
						+ StringUtils.defaultString(url) + " for connector : "
						+ connectorInstance.getName() + " Message: "
						+ e.getMessage());
			} finally {
				IOUtils.closeQuietly(input);
				if (feedContent != null) {
					FileUtils.deleteQuietly(feedContent.getValue());
				}
			}
		}
		contentParse.setContent(contentBuffer.toString());
		contentParse.setMd5(md5s);

		return contentParse;
	}

	private List<RecordMeta> asMetaSet(List<FeedMetadata> feedMetadatas,
			ConnectorInstance connectorInstance) {
		ConnectorInstanceServices connectorInstanceServices = ConstellioSpringUtils
				.getConnectorInstanceServices();
		// FIXME (metas): 1. improve by considering the order of arrival
		// 2. Also by considering two values that have the same content
		List<RecordMeta> metas = new ArrayList<RecordMeta>();
		for (FeedMetadata feedMetadata : feedMetadatas) {
			for (FeedMeta feedMeta : feedMetadata.getMetas()) {
				RecordMeta meta = new RecordMeta();
				meta.setContent(feedMeta.getContent());
				ConnectorInstanceMeta connectorInstanceMeta = connectorInstance
						.getOrCreateMeta(feedMeta.getName());
				if (connectorInstanceMeta.getId() == null) {
					connectorInstanceServices.makePersistent(connectorInstance);
				}
				meta.setConnectorInstanceMeta(connectorInstanceMeta);
				metas.add(meta);
			}
		}
		return metas;
	}

	private Record asRecord(FeedRecord feedRecord,
			ConnectorInstance connectorInstance) {
		Record record = new Record();
		record.setAuthmethod(feedRecord.getAuthmethod().toString());
		record.setPublicRecord(feedRecord.isPublicRecord());

		// RawContentServices rawContentServices =
		// ConstellioSpringUtils.getRawContentServices();
		// Set<RawContent> rawContents =
		// asRawContents(feedRecord.getContents());
		// rawContentServices.setRawContents(record, rawContents);

		ContentParse contentParse = asContentParse(feedRecord.getUrl(),
				feedRecord.getContents(), connectorInstance);
		final String parsedContentString = contentParse.getContent();
		record.setParsedContent(parsedContentString);
		record.setMd5(contentParse.getMd5());

		// String rawContentString;
		// if (!rawContents.isEmpty()) {
		// rawContentString = new
		// String(rawContents.iterator().next().getContent());
		// } else {
		// rawContentString = null;
		// }

		// Add language
		String lang = LangDetectorUtil.getInstance()
				.detect(parsedContentString);
		// if (feedRecord.getMimetype().toLowerCase().contains("html") &&
		// StringUtils.isNotBlank(rawContentString)) {
		// final HtmlLangDetector htmlLangDetector = new HtmlLangDetector();
		// lang = htmlLangDetector.getLang(rawContentString);
		// if (StringUtils.isBlank(lang)) {
		// lang = LangDetectorUtil.getInstance().detect(parsedContentString);
		// }
		// } else {
		// lang = LangDetectorUtil.getInstance().detect(parsedContentString);
		// }
		record.setLang(lang);

		// Add content metadata
		for (RecordMeta meta : contentParse.getMetas()) {
			record.addContentMeta(meta);
		}

		record.setLastModified(feedRecord.getLastModified().getTime());
		record.setLastFetched(new Date());
		record.setMimetype(feedRecord.getMimetype());
		record.setUrl(feedRecord.getUrl());

		record.setConnectorInstance(connectorInstance);
		List<RecordMeta> recordMetas = asMetaSet(feedRecord.getMetadatas(),
				connectorInstance);
		for (RecordMeta meta : recordMetas) {
			ConnectorInstanceMeta connectorInstanceMeta = meta
					.getConnectorInstanceMeta();
			if (connectorInstanceMeta.getName().equals(
					ConstellioFeedConstants.PROPNAME_BOOST)) {
				try {
					final double boost = Double.parseDouble(meta.getContent());
					record.setBoost(boost);
				} catch (NumberFormatException e) {
					LOG.severe("Could not format boost value to double for record : "
							+ feedRecord.getUrl());
				}
			}
			record.addExternalMeta(meta);
		}

		List<String> keywords = record.getMetaContents("Keywords");
		if (keywords != null) {
			FreeTextTagServices freeTextTagServices = ConstellioSpringUtils
					.getFreeTextTagServices();
			for (String keyword : keywords) {
				StringTokenizer st = new StringTokenizer(keyword, ",");
				while (st.hasMoreTokens()) {
					String keywordToken = st.nextToken().trim();
					if (StringUtils.isNotBlank(keywordToken)) {
						FreeTextTag tag = freeTextTagServices.get(keywordToken);
						if (tag == null) {
							tag = new FreeTextTag();
							tag.setFreeText(keywordToken);
							freeTextTagServices.makePersistent(tag);
						}
						RecordTag recordTag = new RecordTag();
						recordTag.setFreeTextTag(tag);
						recordTag.setRecord(record);
						record.getRecordTags().add(recordTag);
					}
				}
			}
		}

		return record;
	}

//	/**
//	 * Detects that the connector manager only sent a metadata update.
//	 * 
//	 * @param rawContents
//	 * @return
//	 */
//	private boolean isMetadataAndUrl(Set<RawContent> rawContents) {
//		if (rawContents != null) {
//			if (rawContents.size() == 1) {
//				RawContent rawContent = rawContents.toArray(new RawContent[1])[0];
//				byte[] rawContentBytes = rawContent.getContent();
//				String rawContentString = new String(rawContentBytes);
//				if (StringUtils.startsWith(rawContentString, "<html><title>")
//						&& StringUtils.endsWith(rawContentString,
//								"</title></html>")) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}
}
