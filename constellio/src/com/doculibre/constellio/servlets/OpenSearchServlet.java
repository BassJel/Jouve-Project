package com.doculibre.constellio.servlets;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.StatsServices;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.solr.handler.ConstellioSolrQueryParams;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.SimpleParams;
import com.doculibre.constellio.wicket.utils.SimpleParamsUtils;


/**
 * Present search results using A9's OpenSearch extensions to RSS, plus a few
 * Nutch-specific extensions.
 */
@SuppressWarnings("serial")
public class OpenSearchServlet extends HttpServlet {
	
	private static Logger LOGGER = Logger.getLogger(OpenSearchServlet.class);
	
	private static final Map<String, String> NS_MAP = new HashMap<String, String>();

	static {
		NS_MAP.put("opensearch", "http://a9.com/-/spec/opensearchrss/1.0/");
		NS_MAP.put("nutch", "http://www.nutch.org/opensearchrss/1.0/");
	}

	private static final Set<String> SKIP_DETAILS = new HashSet<String>();
	
	static {
		SKIP_DETAILS.add("url"); // redundant with RSS link
		SKIP_DETAILS.add("title"); // redundant with RSS title
	}

	public void init(ServletConfig config) throws ServletException {
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ipAddress = request.getRemoteAddr();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("query request from " + ipAddress);
		}

		// get parameters from request
		request.setCharacterEncoding("UTF-8");
		String queryString = request.getParameter("query");
		if (queryString == null || "url:http".equals(queryString)) {
			queryString = SimpleSearch.SEARCH_ALL;
		}
		
		String urlQuery = queryString; // URLEncoder.encode(queryString, "UTF-8");

		// the query language
		String queryLang = request.getParameter("lang");

		int start = 0; // first hit to display
		String startString = request.getParameter("start");
		if (startString != null) {
			start = Integer.parseInt(startString);
		}	

		int hitsPerPage = 10; // number of hits to display
		String hitsString = request.getParameter("hitsPerPage");
		if (hitsString != null) {
			hitsPerPage = Integer.parseInt(hitsString);
		}	

		String sort = request.getParameter("sort");
		boolean reverse = sort != null && "true".equals(request.getParameter("reverse"));

		// Make up query string for use later drawing the 'rss' logo.
		String params = "&hitsPerPage="
				+ hitsPerPage
				+ (queryLang == null ? "" : "&lang=" + queryLang)
				+ (sort == null ? "" : "&sort="
						+ sort
						+ (reverse ? "&reverse=true" : ""));
		
        String collectionName = request.getParameter(SimpleSearch.COLLECTION_NAME);
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        SimpleParams simpleParams = SimpleParamsUtils.toSimpleParams(request);
        simpleParams.remove("username");
        simpleParams.remove("password");

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        UserServices userServices = ConstellioSpringUtils.getUserServices();
        
        RecordCollection collection;
        if (collectionName != null) {
            collection = collectionServices.get(collectionName);
        } else {
        	collection = collectionServices.listPublic().get(0);
        }
        
        ConstellioUser user;
        if (StringUtils.isNotBlank(username)) {
            user = userServices.get(username);
            if (user != null) {
                String passwordHash = ConstellioUser.getHash(password);
                if (!user.getPasswordHash().equals(passwordHash)) {
                    throw new ServletException("Invalid password");
                }
            } else {
                throw new ServletException("Invalid username : " + username);
            }
        } else {
            user = null;
        }
        
        if (user != null) {
        	simpleParams.add(ConstellioSolrQueryParams.USER_ID, String.valueOf(user.getId()));
        }

    	SimpleSearch simpleSearch = new SimpleSearch();
        simpleSearch.setCollectionName(collection.getName());
		simpleSearch.setQuery(queryString);
		if (queryLang != null) {
			simpleSearch.setSingleSearchLocale(new Locale(queryLang));
		}

		String solrQueryString = queryString;
		if (queryLang != null) {
			solrQueryString += " AND " + IndexField.LANGUAGE_FIELD + ":" + queryLang;
		}
		SolrQuery solrQuery = new SolrQuery(solrQueryString);
		solrQuery.addHighlightField(IndexField.DEFAULT_SEARCH_FIELD);
        
        SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
        QueryResponse queryResponse;
		try {
			queryResponse = solrServer.query(solrQuery);
		} catch (SolrServerException e) {
			throw new ServletException(e);
		}
		
//		QueryResponse queryResponse = searchServices.search(simpleSearch, start, hitsPerPage, user);
		SolrDocumentList results = queryResponse.getResults();
		long numFound = results.getNumFound();
		int numReturned = results.size();
        Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
        
        StatsServices statsServices = ConstellioSpringUtils.getStatsServices();
        if (!statsServices.isIgnored(simpleSearch)) {
            statsServices.logSearch(simpleSearch, queryResponse, ipAddress);
        }
		
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("query: " + queryString);
			LOGGER.info("lang: " + queryLang);
			LOGGER.info("collection: " + collection.getName());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("total hits: " + numFound);
		}

		// generate xml results
		int end = (int) Math.min(numReturned, start + hitsPerPage);
		int length = numReturned;

		String requestUrl = request.getRequestURL().toString();
		String base = requestUrl.substring(0, requestUrl.lastIndexOf('/'));

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Document doc = factory.newDocumentBuilder().newDocument();

			Element rss = addNode(doc, doc, "rss");
			addAttribute(doc, rss, "version", "2.0");
			addAttribute(doc, rss, "xmlns:opensearch", (String) NS_MAP.get("opensearch"));
			addAttribute(doc, rss, "xmlns:nutch", (String) NS_MAP.get("nutch"));

			Element channel = addNode(doc, rss, "channel");

			addNode(doc, channel, "title", "Nutch: " + queryString);
			addNode(doc, channel, "description", "Constellio search results for query: " + queryString);
			addNode(doc, channel, "link", base + "/app/search" + "?query="
					+ urlQuery + "&start=" + start + params);

			addNode(doc, channel, "opensearch", "totalResults", "" + numFound);
			addNode(doc, channel, "opensearch", "startIndex", "" + start);
			addNode(doc, channel, "opensearch", "itemsPerPage", "" + hitsPerPage);

			addNode(doc, channel, "nutch", "query", queryString);

			if ((end < numFound) // more hits to // show
					|| ((numReturned > start + hitsPerPage))) {
				addNode(doc, channel, "nutch", "nextPage", requestUrl
						+ "?query=" + urlQuery + "&start=" + end + params);
			}

			if (((numReturned <= start + hitsPerPage))) {
				addNode(doc, channel, "nutch", "showAllHits", requestUrl + "?query=" + urlQuery + params);
			}

			for (int i = 0; i < length; i++) {
				SolrDocument result = results.get(i);
		        Map<String, List<String>> fieldsHighlighting;
		        if (highlighting != null) {
		        	fieldsHighlighting = highlighting.get(result.getFieldValue(IndexField.UNIQUE_KEY_FIELD));
		        } else {
		        	fieldsHighlighting = new HashMap<String, List<String>>();
		        }
		        
		        String title;
		        Collection<Object> titleObj = result.getFieldValues(IndexField.TITLE_FIELD);
		        if (titleObj != null && !titleObj.isEmpty()) {
		        	title = titleObj.iterator().next().toString();
		        } else {
		        	title = "";
		        }
//		        String titleHighlight = getTitleFromHighlight(IndexField.TITLE_FIELD, fieldsHighlighting);
//		        if (StringUtils.isNotBlank(titleHighlight)) {
//		        	title = titleHighlight;
//		        }

		        String url;
		        Collection<Object> urlObj = result.getFieldValues(IndexField.DISPLAY_URL_FIELD);
		        if (urlObj != null && !urlObj.isEmpty()) {
		        	url = urlObj.iterator().next().toString();
		        } else {
		        	url = "";
		        }

				if (title == null || title.equals("")) { // use url for docs w/o title
					title = url;
				}
				
				String excerpt = getExcerptFromHighlight(IndexField.DEFAULT_SEARCH_FIELD, fieldsHighlighting);
				if (excerpt == null) {
					excerpt = "";
				}

				Element item = addNode(doc, channel, "item");

				addNode(doc, item, "title", title);
				addNode(doc, item, "description", excerpt);
				addNode(doc, item, "link", url);
			}

			// dump DOM tree

			DOMSource source = new DOMSource(doc);
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");
			StreamResult result = new StreamResult(response.getOutputStream());
			response.setContentType("text/xml");
			transformer.transform(source, result);

		} catch (javax.xml.parsers.ParserConfigurationException e) {
			throw new ServletException(e);
		} catch (javax.xml.transform.TransformerException e) {
			throw new ServletException(e);
		}

	}

	private static Element addNode(Document doc, Node parent, String name) {
		Element child = doc.createElement(name);
		parent.appendChild(child);
		return child;
	}

	private static void addNode(Document doc, Node parent, String name,
			String text) {
		Element child = doc.createElement(name);
		child.appendChild(doc.createTextNode(getLegalXml(text)));
		parent.appendChild(child);
	}

	private static void addNode(Document doc, Node parent, String ns,
			String name, String text) {
		Element child = doc.createElementNS((String) NS_MAP.get(ns), ns + ":" + name);
		child.appendChild(doc.createTextNode(getLegalXml(text)));
		parent.appendChild(child);
	}

	private static void addAttribute(Document doc, Element node, String name,
			String value) {
		Attr attribute = doc.createAttribute(name);
		attribute.setValue(getLegalXml(value));
		node.getAttributes().setNamedItem(attribute);
	}

	/*
	 * Ensure string is legal xml.
	 * 
	 * @param text String to verify.
	 * 
	 * @return Passed <code>text</code> or a new string with illegal characters
	 * removed if any found in <code>text</code>.
	 * 
	 * @see http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char
	 */
	protected static String getLegalXml(final String text) {
		if (text == null) {
			return null;
		}
		StringBuffer buffer = null;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (!isLegalXml(c)) {
				if (buffer == null) {
					// Start up a buffer. Copy characters here from now on
					// now we've found at least one bad character in original.
					buffer = new StringBuffer(text.length());
					buffer.append(text.substring(0, i));
				}
			} else {
				if (buffer != null) {
					buffer.append(c);
				}
			}
		}
		return (buffer != null) ? buffer.toString() : text;
	}

	private static boolean isLegalXml(final char c) {
		return c == 0x9 || c == 0xa || c == 0xd || (c >= 0x20 && c <= 0xd7ff)
				|| (c >= 0xe000 && c <= 0xfffd)
				|| (c >= 0x10000 && c <= 0x10ffff);
	}

//    private static String getTitleFromHighlight(String titleFieldName, Map<String, List<String>> fieldsHighlighting) {
//        String title = null;
//        if (fieldsHighlighting != null) {
//            List<String> fieldHighlighting = fieldsHighlighting.get(titleFieldName);
//            if (fieldHighlighting != null) {
//                StringBuffer sb = new StringBuffer();
//                for (String val : fieldHighlighting) {
//                    val = val.replace(StringEscapeUtils.escapeHtml("<em>"), "<em>");
//                    val = val.replace(StringEscapeUtils.escapeHtml("</em>"), "</em>");
//                    val = val.replace(StringEscapeUtils.escapeHtml("<sup>"), "<sup>");
//                    val = val.replace(StringEscapeUtils.escapeHtml("</sup>"), "</sup>");
//                    sb.append(val + " ");
//                }
//                if (sb.length() > 0) {
//                    title = sb.toString().trim();
//                }
//            }
//        }
//        return title;
//    }

    private static String getExcerptFromHighlight(String defaultSearchFieldName,
        Map<String, List<String>> fieldsHighlighting) {
        String exerpt = null;
        if (fieldsHighlighting != null) {
            List<String> fieldHighlighting = fieldsHighlighting.get(defaultSearchFieldName);
            if (fieldHighlighting != null) {
                StringBuffer sb = new StringBuffer();
                for (String val : fieldHighlighting) {
                    sb.append(StringEscapeUtils.unescapeXml(val) + " ... ");
                }
                if (sb.length() > 0) {
                    exerpt = sb.toString().trim();
                }
            }
        }
        return exerpt;
    }

}
