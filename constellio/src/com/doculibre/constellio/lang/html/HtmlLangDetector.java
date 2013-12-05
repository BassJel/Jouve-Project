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
package com.doculibre.constellio.lang.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@SuppressWarnings("rawtypes")
public class HtmlLangDetector {

	public static final Log LOG = LogFactory.getLog(HtmlLangDetector.class);

	private static final String LANG_MAPPING_FILE = "langMapping.properties";

	/* A static Map of ISO-639 language codes */
	private static final Map<String, String> LANGUAGES_MAP = new HashMap<String, String>();
	static {
		try {
			final Properties mappingProperties = new Properties();
			final String packageName = StringUtils.substringBeforeLast(HtmlLangDetector.class.getCanonicalName(), ".");
			final String mappingPath = "/" + StringUtils.replace(StringUtils.defaultString(packageName), ".", "/") + "/" + LANG_MAPPING_FILE;
			InputStream mappingStream = null;
			try {
				mappingStream = HtmlLangDetector.class.getResourceAsStream(mappingPath);
				mappingProperties.load(new InputStreamReader(mappingStream));
			} finally {
				IOUtils.closeQuietly(mappingStream);
			}
			final Enumeration keys = mappingProperties.keys();
			while (keys.hasMoreElements()) {
				final String key = (String) keys.nextElement();
				LANGUAGES_MAP.put(key, key);
				final String[] values = mappingProperties.getProperty(key).split(",", 0);
				for (String value : values) {
					LANGUAGES_MAP.put(value.toLowerCase(), key);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<String> parse(Node node) {
		List<String> langs = new ArrayList<String>();
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			// Check for the lang HTML attribute
			final String htmlLang = parseLanguage(((Element) node).getAttribute("lang"));
			if (StringUtils.isNotBlank(htmlLang)) {
				langs.add(htmlLang);
			}

			// Check for Meta
			if ("meta".equalsIgnoreCase(node.getNodeName())) {
				NamedNodeMap attrs = node.getAttributes();

				// Check for the dc.language Meta
				for (int i = 0; i < attrs.getLength(); i++) {
					Node attrnode = attrs.item(i);
					if ("name".equalsIgnoreCase(attrnode.getNodeName())) {
						if ("dc.language".equalsIgnoreCase(attrnode.getNodeValue())) {
							Node valueattr = attrs.getNamedItem("content");
							if (valueattr != null) {
								final String dublinCore = parseLanguage(valueattr.getNodeValue());
								if (StringUtils.isNotBlank(dublinCore)) {
									langs.add(dublinCore);
								}
							}
						}
					}
				}

				// Check for the http-equiv content-language
				for (int i = 0; i < attrs.getLength(); i++) {
					Node attrnode = attrs.item(i);
					if ("http-equiv".equalsIgnoreCase(attrnode.getNodeName())) {
						if ("content-language".equalsIgnoreCase(attrnode.getNodeValue().toLowerCase())) {
							Node valueattr = attrs.getNamedItem("content");
							if (valueattr != null) {
								final String httpEquiv = parseLanguage(valueattr.getNodeValue());
								if (StringUtils.isNotBlank(httpEquiv)) {
									langs.add(httpEquiv);
								}
							}
						}
					}
				}

				// Check for the <meta name="language" content="xyz" /> tag
				for (int i = 0; i < attrs.getLength(); i++) {
					Node attrnode = attrs.item(i);
					if ("name".equalsIgnoreCase(attrnode.getNodeName())) {
						if ("language".equalsIgnoreCase(attrnode.getNodeValue().toLowerCase())) {
							Node valueattr = attrs.getNamedItem("content");
							if (valueattr != null) {
								final String metaLanguage = parseLanguage(valueattr.getNodeValue());
								if (StringUtils.isNotBlank(metaLanguage)) {
									langs.add(metaLanguage);
								}
							}
						}
					}
				}
			}
		}

		if (langs.isEmpty()) {
			// Recurse
			NodeList children = node.getChildNodes();
			for (int i = 0; children != null && i < children.getLength(); i++) {
				langs.addAll(parse(children.item(i)));
				if (!langs.isEmpty()) {
					break;
				}
			}
		}

		return langs;
	}

	public String getLang(String htmlContent) {

		DOMParser parser = new DOMParser();
		final StringReader contentReader = new StringReader(htmlContent);
		final InputSource input = new InputSource(contentReader);
		try {
			parser.parse(input);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> langs = this.parse(parser.getDocument());
		if (!langs.isEmpty()) {
			return langs.get(0);
		}
		return null;
	}

	/**
	 * Parse a language string and return an ISO 639 primary code, or
	 * <code>null</code> if something wrong occurs, or if no language is found.
	 */
	private final static String parseLanguage(final String lang) {
		if (lang == null) {
			return null;
		}

		String code = null;
		String language = null;

		// First, split multi-valued values
		final String langs[] = lang.split(",| |;|\\.|\\(|\\)|=", -1);

		int i = 0;
		while ((language == null) && (i < langs.length)) {
			// Then, get the primary code
			code = langs[i].split("-")[0];
			code = code.split("_")[0];
			// Find the ISO 639 code
			language = (String) LANGUAGES_MAP.get(code.toLowerCase());
			i++;
		}
		return language;
	}

	public static void main(String[] args) throws Exception {
		//java.net.URL url = new java.net.URL("http://www.gouv.qc.ca");
		java.net.URL url = new java.net.URL("http://andrew.triumf.ca/multilingual/samples/french.meta.html");
		java.net.URLConnection connection = url.openConnection();
		java.io.InputStream is = null;
		String content = null;
		try {
			is = connection.getInputStream();
			content = IOUtils.toString(is);
		} finally {
			IOUtils.closeQuietly(is);
		}

		HtmlLangDetector langDetector = new HtmlLangDetector();
		System.out.println(langDetector.getLang(content));
	}
}
