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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordMeta;
import com.doculibre.constellio.entities.skos.SkosConcept;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.TimeZone;

/**
 * Simplify xml formatting and parsing of named lists
 * 
 * @author francisbaril
 * 
 */
public class NamedListUtils {

	// public static NamedList<Object> convertXMLToNamedList(String xml) {
	// return convertXMLToNamedList(new StringReader(xml));
	// }

	public static NamedList<Object> convertXMLToNamedList(InputStream in) {
		XMLResponseParser parser = new XMLResponseParser();
		return parser.processResponse(in, "UTF-8");
	}

	public static void convertResponseNamedListToXML(NamedList<Object> nl,
			OutputStream os) {
		Document doc = new Document();
		Element rootElement = newNamedListElement(nl);
		rootElement.setName("response");
		doc.setRootElement(rootElement);

		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		outputter.getFormat().setEncoding("UTF-8");
		try {
			outputter.output(doc, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void convertResponseNamedListToXML(NamedList<Object> nl,
			Writer w) {
		Document doc = new Document();
		Element rootElement = newNamedListElement(nl);
		rootElement.setName("response");
		doc.setRootElement(rootElement);

		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		outputter.getFormat().setEncoding("UTF-8");
		try {
			outputter.output(doc, w);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void nameElement(Element element, String name) {
		if ("doc".equals(name)) {
			element.setName("doc");
			element.setAttribute("name", "doc");
		} else if ("response".equals(name)) {
			element.setName("result");
			element.setAttribute("name", "response");
		} else {
			element.setAttribute("name", name);
		}
	}

	@SuppressWarnings("unchecked")
	private static Element newNamedListElement(NamedList<Object> nl) {
		Element nlElement = new Element("lst");
		Iterator<Map.Entry<String, Object>> entries = nl.iterator();
		while (entries.hasNext()) {
			Map.Entry<String, Object> entry = entries.next();
			if (entry.getValue() != null) {
				if ("attr".equals(entry.getKey().toLowerCase())) {
					NamedList<Object> attributes = (NamedList<Object>) entry
							.getValue();
					for (Map.Entry<String, Object> attribute : attributes) {
						// We want the name to be the first attribute (for
						// Junit)
						nlElement.setAttribute("name", "");
						Element child = toElement(attribute.getValue());
						if (child != null) {
							nlElement.setAttribute(attribute.getKey(),
									child.getText());
						}
					}
				} else {
					Element child = toElement(entry.getValue());
					if (child != null) {
						nameElement(child, entry.getKey());
						nlElement.addContent(child);
					}
				}

			}
		}
		return nlElement;
	}
	
	public static Element newListElement(List items) {
		Element nlElement = new Element("arr");
		for (Object item : items) {
			if (item != null) {
				Element child = toElement(item);
				if (child != null) {
					nlElement.addContent(child);
				}
			}
		}
		return nlElement;
	}

	private static Element newMapElement(Map<String, ?> items) {
		Element nlElement = new Element("lst");
		// Unit test prefer when field are sorted
		List<String> keys = new ArrayList<String>();
		keys.addAll(items.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			Object value = items.get(key);
			if (value != null) {
				Element child = toElement(value);
				if (child != null) {
					nameElement(child, key);
					nlElement.addContent(child);
				}
			}
		}
		return nlElement;
	}

	private static Element newSolrDocument(SolrDocument doc) {
		Element nlElement = new Element("lst");
		// Unit test prefer when field are sorted
		List<String> keys = new ArrayList<String>();
		keys.addAll(doc.getFieldNames());
		Collections.sort(keys);
		for (String key : keys) {
			Collection<Object> c = doc.getFieldValues(key);
			Object value = c.size() > 1 ? c : c.iterator().next();
			if (value != null) {
				Element child = toElement(value);
				if (child != null) {
					nameElement(child, key);
					nlElement.addContent(child);
				}
			}
		}
		return nlElement;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Element toElement(Object value) {
		String elementTag = null;
		String strValue = null;
		Element item = null;
		if (value == null) {
			return null;

		} else if (value instanceof NamedList) {
			NamedList<Object> nl = (NamedList<Object>) value;
			item = newNamedListElement(nl);

		} else if (value instanceof Integer) {
			elementTag = "int";

		} else if (value instanceof String) {
			elementTag = "str";

		} else if (value instanceof Date) {
			elementTag = "date";
			SimpleDateFormat dateParser = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss'Z'");
			dateParser.setTimeZone(TimeZone.getTimeZone("GMT"));
			strValue = dateParser.format(value);

		} else if (value instanceof Boolean) {
			elementTag = "bool";
			strValue = ((Boolean) value) ? "true" : "false";

		} else if (value instanceof Long) {
			elementTag = "long";

		} else if (value instanceof Float) {
			elementTag = "float";

		} else if (value instanceof Double) {
			elementTag = "double";

		} else if (value instanceof SolrDocumentList) {
			SolrDocumentList documentList = (SolrDocumentList) value;
			item = newListElement((List) value);
			// We want the name to be the first attribute (for Junit)
			item.setAttribute("name", "");
			item.setAttribute("numFound", "" + documentList.getNumFound());
			item.setAttribute("start", "" + documentList.getStart());

		} else if (value instanceof List) {
			item = newListElement((List) value);

		} else if (value instanceof SolrDocument) {
			// Only for unit tests
			item = newSolrDocument((SolrDocument) value);
			nameElement(item, "doc");

		} else if (value instanceof Map) {
			item = newMapElement(((SolrDocument) value).getFieldValuesMap());

		} else {
			throw new RuntimeException("UnsupportedType "
					+ value.getClass().getSimpleName());
		}

		if (item == null) {
			item = new Element(elementTag);
			item.setText(strValue != null ? strValue : String.valueOf(value));

		}
		return item;
	}

	public static NamedList<Object> toNamedList(Record record,
			SolrDocument document, ConstellioUser user, QueryResponse response,
			List<String> limitedResponseFields) {
		Locale locale = user.getLocale();
		NamedList<Object> nl = new NamedList<Object>();
		addField(nl, limitedResponseFields, "authmethod",
				record.getAuthmethod());
		addField(nl, limitedResponseFields, "boost", record.getBoost());
		addField(nl, limitedResponseFields, "connectorInstance", record
				.getConnectorInstance().getName());

		for (RecordMeta rm : record.getContentMetas()) {
			ConnectorInstanceMeta cim = rm.getConnectorInstanceMeta();
			String key = cim.getName();
			addField(nl, limitedResponseFields, key, rm.getContent());
		}
		addField(nl, limitedResponseFields, "displayTitle",
				record.getDisplayTitle());
		addField(nl, limitedResponseFields, "displayUrl",
				record.getDisplayUrl());

		for (RecordMeta rm : record.getExternalMetas()) {
			ConnectorInstanceMeta cim = rm.getConnectorInstanceMeta();
			String key = cim.getName();
			addField(nl, limitedResponseFields, key, rm.getContent());
		}

		List<String> freeTextTags = new ArrayList<String>();
		for (FreeTextTag tag : record.getFreeTextTags(false)) {
			freeTextTags.add(tag.getFreeText());
		}
		addField(nl, limitedResponseFields, "freeTextTags", freeTextTags);

		List<String> thesaurusTags = new ArrayList<String>();
		for (SkosConcept concept : record.getSkosConcepts(false)) {
			thesaurusTags.add(concept.getPrefLabel(locale));
		}
		addField(nl, limitedResponseFields, "thesaurusTags", thesaurusTags);

		addField(nl, limitedResponseFields, "id", record.getId());
		addField(nl, limitedResponseFields, "lang", record.getLang());
		addField(nl, limitedResponseFields, "lastAutomaticTagging",
				record.getLastAutomaticTagging());
		addField(nl, limitedResponseFields, "lastFetched",
				record.getLastFetched());
		addField(nl, limitedResponseFields, "lastIndexed",
				record.getLastIndexed());
		addField(nl, limitedResponseFields, "lastModified",
				record.getLastModified());
		addField(nl, limitedResponseFields, "mimeType", record.getMimetype());
		String recordURL = record.getUrl();
		addField(nl, limitedResponseFields, "url", recordURL);

		Map<String, Map<String, List<String>>> highlighting = response
				.getHighlighting();
		Map<String, List<String>> fieldsHighlighting = highlighting
				.get(recordURL);

		for (String field : fieldsHighlighting.keySet()) {
			List<String> list = fieldsHighlighting.get(field);
			addField(nl, limitedResponseFields, field + "_highlight", list);
		}

		return nl;
	}

	private static void addField(NamedList<Object> nl,
			List<String> limitedResponseFields, String field, Object value) {
		if (limitedResponseFields == null
				|| limitedResponseFields.isEmpty()
				|| limitedResponseFields.contains(field)
				|| limitedResponseFields.contains(field.replace("_highlight",
						""))) {
			nl.add(field, value);
		}
	}

}
