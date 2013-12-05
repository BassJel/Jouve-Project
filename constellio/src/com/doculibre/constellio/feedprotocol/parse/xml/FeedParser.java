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
package com.doculibre.constellio.feedprotocol.parse.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

//TODO Do a SAX implementation
public class FeedParser {

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
	private static final String XML_RECORD_MIMETYPE = "mimetype";
	private static final String XML_RECORD_LAST_MODIFIED = "last-modified";
	private static final String XML_RECORD_LOCK = "lock";
	private static final String XML_RECORD_AUTHMETHOD = "authmethod";

	private static final String XML_META_NAME = "name";
	private static final String XML_META_CONTENT = "content";

	private static final String XML_CONTENT_ENCODING = "encoding";

	private FeedValidator validator = new FeedValidator();

	public Feed parse(String datasource, String feedtype, InputStream is) throws ParseFeedException {
		Document xmlDoc = validator.validate(new InputSource(is));
		Element gsafeedElement = xmlDoc.getDocumentElement();
		return parseGsafeedElement(datasource, feedtype, gsafeedElement);
	}

	private Feed parseGsafeedElement(String datasource, String feedtype, Element gsafeedElement) throws ParseFeedException {
		NodeList headerNodeList = gsafeedElement.getElementsByTagName(XML_HEADER);
		if (headerNodeList.getLength() == 0) {
			throw new ParseFeedException("No 'header' element found in 'gsafeed' element");
		} else if (headerNodeList.getLength() > 1) {
			throw new ParseFeedException("Multiple 'header' elements found in 'gsafeed' element");
		}

		// According to the specification the header element is ignored
		// Ref
		// http://code.google.com/intl/fr-FR/apis/searchappliance/documentation/46/feedsguide.html#system

		NodeList groupsNodeList = gsafeedElement.getElementsByTagName(XML_GROUP);
		if (groupsNodeList.getLength() < 1) {
			throw new ParseFeedException("No 'group' element found in 'gsafeed' element");
		}
		List<FeedGroup> groups = this.parseGroupElements(datasource, feedtype, groupsNodeList);

		return new FeedImpl(datasource, feedtype, groups);
	}

	private List<FeedGroup> parseGroupElements(String datasource, String feedtype, NodeList groupsNodeList) throws ParseFeedException {
		List<FeedGroup> groups = new ArrayList<FeedGroup>();
		for (int i = 0; i < groupsNodeList.getLength(); i++) {
			Element groupElement = (Element) groupsNodeList.item(i);

			String action = getAttrValueIfPresent(groupElement, XML_GROUP_ACTION);

			NodeList recordsNodeList = groupElement.getElementsByTagName(XML_RECORD);
			List<FeedRecord> records = this.parseRecordElements(datasource, feedtype, action, recordsNodeList);

			FeedGroup group = new FeedGroupImpl(action, records);
			groups.add(group);
		}
		return groups;
	}

	private List<FeedRecord> parseRecordElements(String datasource, String feedtype, String groupAction, NodeList recordsNodeList)
			throws ParseFeedException {
		List<FeedRecord> records = new ArrayList<FeedRecord>();
		for (int i = 0; i < recordsNodeList.getLength(); i++) {
			Element recordElement = (Element) recordsNodeList.item(i);

			String action = getAttrValueIfPresent(recordElement, XML_RECORD_ACTION);
			String authmethod = getAttrValueIfPresent(recordElement, XML_RECORD_AUTHMETHOD);

			NodeList contentsNodeList = recordElement.getElementsByTagName(XML_CONTENT);
			List<FeedContent> contents = this.parseContentElements(contentsNodeList);

			String lastModified = getAttrValueIfPresent(recordElement, XML_RECORD_LAST_MODIFIED);
			String lock = getAttrValueIfPresent(recordElement, XML_RECORD_LOCK);

			NodeList metadatasNodeList = recordElement.getElementsByTagName(XML_METADATA);
			List<FeedMetadata> metadatas = this.parseMetadataElements(metadatasNodeList);

			String mimetype = getAttrValueIfPresent(recordElement, XML_RECORD_MIMETYPE);
			String url = getAttrValueIfPresent(recordElement, XML_RECORD_URL);

			FeedRecord record = new FeedRecordImpl(datasource, feedtype, groupAction, url, action, mimetype, lastModified, lock, authmethod,
					contents, metadatas);
			records.add(record);
		}
		return records;
	}

	private List<FeedMetadata> parseMetadataElements(NodeList metadatasNodeList) throws ParseFeedException {
		List<FeedMetadata> metadatas = new ArrayList<FeedMetadata>();
		for (int i = 0; i < metadatasNodeList.getLength(); i++) {
			Element metadataElement = (Element) metadatasNodeList.item(i);
			NodeList metasNodeList = metadataElement.getElementsByTagName(XML_META);
			if (metasNodeList.getLength() < 1) {
				throw new ParseFeedException("No 'meta' element found in 'metadata' element");
			}
			List<FeedMeta> metas = this.parseMetaElements(metasNodeList);
			FeedMetadata metadata = new FeedMetadataImpl(metas);
			metadatas.add(metadata);
		}
		return metadatas;
	}

	private List<FeedMeta> parseMetaElements(NodeList metasNodeList) throws ParseFeedException {
		List<FeedMeta> metas = new ArrayList<FeedMeta>();
		for (int i = 0; i < metasNodeList.getLength(); i++) {
			Element metaElement = (Element) metasNodeList.item(i);

			String name = getAttrValueIfPresent(metaElement, XML_META_NAME);
			String content = getAttrValueIfPresent(metaElement, XML_META_CONTENT);

			FeedMeta meta = new FeedMetaImpl(name, content);
			metas.add(meta);
		}
		return metas;
	}

	private List<FeedContent> parseContentElements(NodeList contentsNodeList) throws ParseFeedException {
		List<FeedContent> contents = new ArrayList<FeedContent>();
		for (int i = 0; i < contentsNodeList.getLength(); i++) {
			Element contentElement = (Element) contentsNodeList.item(i);

			String encoding = getAttrValueIfPresent(contentElement, XML_CONTENT_ENCODING);
			NodeList nodeList = contentElement.getChildNodes();
			//System.out.println("Length: " + nodeList.getLength());
			if (nodeList.getLength() == 1) {
				Node childNode = nodeList.item(0);
				if (childNode.getNodeType() == Node.TEXT_NODE || childNode.getNodeType() == Node.CDATA_SECTION_NODE) {
					String value = contentElement.getChildNodes().item(0).getNodeValue();
					//System.out.println("Value: " + value);
					FeedContent content = new FeedContentImpl(value, encoding);
					contents.add(content);
				}
			}
		}
		return contents;
	}

	private String getAttrValueIfPresent(Element elementName, String attributeName) {
		String value = null;
		Attr attr = elementName.getAttributeNode(attributeName);
		if (attr != null && attr.getSpecified()) {
			value = attr.getValue();
		}
		return value;
	}
}
