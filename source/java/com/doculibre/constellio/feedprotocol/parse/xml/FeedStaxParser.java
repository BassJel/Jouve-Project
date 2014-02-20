package com.doculibre.constellio.feedprotocol.parse.xml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import com.doculibre.constellio.feedprotocol.model.Feed;
import com.doculibre.constellio.feedprotocol.model.FeedContent;
import com.doculibre.constellio.feedprotocol.model.FeedGroup;
import com.doculibre.constellio.feedprotocol.model.FeedMeta;
import com.doculibre.constellio.feedprotocol.model.FeedMetadata;
import com.doculibre.constellio.feedprotocol.model.FeedRecord;
import com.doculibre.constellio.feedprotocol.model.ParseFeedException;
import com.doculibre.constellio.feedprotocol.model.impl.FeedContentImpl;
import com.doculibre.constellio.feedprotocol.model.impl.FeedGroupImpl;
import com.doculibre.constellio.feedprotocol.model.impl.FeedImpl;
import com.doculibre.constellio.feedprotocol.model.impl.FeedMetaImpl;
import com.doculibre.constellio.feedprotocol.model.impl.FeedMetadataImpl;
import com.doculibre.constellio.feedprotocol.model.impl.FeedRecordImpl;

public class FeedStaxParser {
	
	// Strings for XML tags.
		private static final String XML_GSAFEED = "gsafeed";

		private static final String XML_HEADER = "header";
		// FeedServlet reads this
		public static final String XML_DATASOURCE = "datasource";
		// FeedServlet reads this
		public static final String XML_FEEDTYPE = "feedtype";
		// FeedServlet reads this
		public static final String XML_DATA = "data";

		private static final String XML_GROUP = "group";
		private static final String XML_RECORD = "record";
		private static final String XML_METADATA = "metadata";
		private static final String XML_META = "meta";
		private static final String XML_CONTENT = "content";

		private static final String XML_GROUP_ACTION = "action";

		private static final String XML_RECORD_ACTION = "action";
		private static final String XML_RECORD_URL = "url";
		private static final String XML_RECORD_DISPLAYURL= "displayurl";
		private static final String XML_RECORD_MIMETYPE = "mimetype";
		private static final String XML_RECORD_LAST_MODIFIED = "last-modified";
		private static final String XML_RECORD_LOCK = "lock";
		private static final String XML_RECORD_AUTHMETHOD = "authmethod";

		private static final String XML_META_NAME = "name";
		private static final String XML_META_CONTENT = "content";

		private static final String XML_CONTENT_ENCODING = "encoding";
		

	public Feed parse(String datasource, String feedtype, InputStream is)
			throws ParseFeedException, XMLStreamException, IOException {
		Reader reader = new InputStreamReader(is);

		XMLInputFactory2 xmlFactory = (XMLInputFactory2) XMLInputFactory2
				.newInstance();
		xmlFactory.configureForLowMemUsage();
		xmlFactory.setProperty("javax.xml.stream.supportDTD", false);
		XMLStreamReader2 xmlReader = (XMLStreamReader2) xmlFactory
				.createXMLStreamReader(reader);
		
		return parseGsafeedElement(datasource, feedtype, xmlReader);
	}
	
	private Feed parseGsafeedElement(String datasource, String feedtype, XMLStreamReader2 xmlReader) throws XMLStreamException, ParseFeedException  {
		List<FeedGroup> groups = new ArrayList<FeedGroup>();
		while (xmlReader.hasNext()) {
			xmlReader.next();
			if (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
				if (XML_GSAFEED.equals(xmlReader.getLocalName())) {
					//Ignored
				}
				if (XML_HEADER.equals(xmlReader.getLocalName())) {
					//datasource
					//feedtype
				}
				if (XML_GROUP.equals(xmlReader.getLocalName())) {
					FeedGroup group = this.parseGroupElement(datasource, feedtype, xmlReader);
					groups.add(group);
				}
			}
		}
		return new FeedImpl(datasource, feedtype, groups);
	}

	private FeedGroup parseGroupElement(String datasource, String feedtype, XMLStreamReader2 xmlReader) throws ParseFeedException, XMLStreamException {
		List<FeedRecord> records = new ArrayList<FeedRecord>();
		String action = xmlReader.getAttributeValue("", XML_GROUP_ACTION);
		while (xmlReader.hasNext()) {
			xmlReader.next();
			if (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
				if (XML_RECORD.equals(xmlReader.getLocalName())) {
					FeedRecord record = this.parseRecordElement(datasource, feedtype, action, xmlReader);
					records.add(record);
				}
			}
			if (xmlReader.getEventType() == XMLStreamConstants.END_ELEMENT) {
				if (XML_GROUP.equals(xmlReader.getLocalName())) {
					break;
				}
			}		
		}	
		
		FeedGroup group = new FeedGroupImpl(action, records);
		return group;
	}

	private FeedRecord parseRecordElement(String datasource, String feedtype, String groupAction, XMLStreamReader2 xmlReader)
			throws ParseFeedException, XMLStreamException {
		//in record element
		
		List<FeedContent> contents = new ArrayList<FeedContent>();
		List<FeedMetadata> metadatas = new ArrayList<FeedMetadata>();
		
		String url = xmlReader.getAttributeValue("", XML_RECORD_URL);
		String displayurl =  xmlReader.getAttributeValue("", XML_RECORD_DISPLAYURL);
		String action = xmlReader.getAttributeValue("", XML_RECORD_ACTION);
		String mimetype =  xmlReader.getAttributeValue("", XML_RECORD_MIMETYPE);
		String authmethod = xmlReader.getAttributeValue("", XML_RECORD_AUTHMETHOD);
		String lastModified = xmlReader.getAttributeValue("", XML_RECORD_LAST_MODIFIED);
		String lock = xmlReader.getAttributeValue("", XML_RECORD_LOCK);

		while (xmlReader.hasNext()) {
			xmlReader.next();
			if (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
				if (XML_METADATA.equals(xmlReader.getLocalName())) {
					FeedMetadata metadata = parseMetadataElement(xmlReader);
					metadatas.add(metadata);
				}
				if (XML_CONTENT.equals(xmlReader.getLocalName())) {
					FeedContent content = parseContentElement(xmlReader);
					contents.add(content);
				}
			}
			if (xmlReader.getEventType() == XMLStreamConstants.END_ELEMENT) {
				if (XML_RECORD.equals(xmlReader.getLocalName())) {
					break;
				}
			}
		}
		
		FeedRecord record = new FeedRecordImpl(datasource, feedtype, groupAction, url, displayurl, action, mimetype, lastModified, lock, authmethod,
				contents, metadatas);		
		return record;
	}

	private FeedMetadata parseMetadataElement(XMLStreamReader2 xmlReader) throws ParseFeedException, XMLStreamException {
		//in metadata element
		List<FeedMeta> metas = new ArrayList<FeedMeta>();
		while (xmlReader.hasNext()) {
			xmlReader.next();
			if (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
				if (XML_META.equals(xmlReader.getLocalName())) {
					FeedMeta meta = this.parseMetaElement(xmlReader);
					metas.add(meta);
				}
			}
			if (xmlReader.getEventType() == XMLStreamConstants.END_ELEMENT) {
				if (XML_METADATA.equals(xmlReader.getLocalName())) {
					break;
				}
			}
		}
		FeedMetadata metadata = new FeedMetadataImpl(metas);
		return metadata;
	}

	private FeedMeta parseMetaElement(XMLStreamReader2 xmlReader) throws ParseFeedException {
		//in meta element
		String name = xmlReader.getAttributeValue("", XML_META_NAME);
		String content = xmlReader.getAttributeValue("", XML_META_CONTENT);

		FeedMeta meta = new FeedMetaImpl(name, content);
		return meta;
	}

	private FeedContent parseContentElement(XMLStreamReader2 xmlReader) throws ParseFeedException, XMLStreamException {
		//in content element
		String encoding = xmlReader.getAttributeValue("", XML_CONTENT_ENCODING);
		while (xmlReader.hasNext()) {
			xmlReader.next();
			if (xmlReader.getEventType() == XMLStreamConstants.CHARACTERS || xmlReader.getEventType() == XMLStreamConstants.CDATA) {
				File temp = null;
				try {
					temp = File.createTempFile("constellio-feedprocessor", ".bin");
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(temp);
						Writer writer = new OutputStreamWriter(new BufferedOutputStream(fos));
						xmlReader.getText(writer, false);
						writer.flush();
					} finally {
						IOUtils.closeQuietly(fos);
					}
					FeedContent content = new FeedContentImpl(temp, encoding);
					
					return content;
				} catch (Exception e) {
					FileUtils.deleteQuietly(temp);
					e.printStackTrace();
				}
			} 
			if (xmlReader.getEventType() == XMLStreamConstants.END_ELEMENT) {
				if (XML_CONTENT.equals(xmlReader.getLocalName())) {
					break;
				}
			}
		}
		return null;
	}
}
