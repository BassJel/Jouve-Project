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
package com.doculibre.constellio.opensearch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.EntityUtils;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.utils.CharSetUtils;

@SuppressWarnings("serial")
public class OpenSearchSolrServer extends SolrServer {

    private static final Logger LOGGER = Logger.getLogger(OpenSearchSolrServer.class.getName());

    // private static final Namespace NS_NUTCH = new Namespace("nutch",
    // "http://www.nutch.org/opensearchrss/1.0/");
    private static final Namespace NS_OPENSEARCH = new Namespace("opensearch",
        "http://a9.com/-/spec/opensearchrss/1.0/");

    @Override
    public NamedList<Object> request(SolrRequest request) throws SolrServerException, IOException {
        SolrParams params = request.getParams();
        if (params == null) {
            params = new ModifiableSolrParams();
        }

        String openSearchURL = params.get("openSearchURL");
        if (openSearchURL == null) {
            throw new SolrServerException("openSearchURL param is missing");
        }
        String query = params.get(CommonParams.Q);
        int start = params.getInt(CommonParams.START, 0);
        int hitsPerPage = params.getInt(CommonParams.ROWS, 10);
        String lang = params.get("lang");

        Map<String, String> paramsMap = new HashMap<String, String>();
        if (SimpleSearch.SEARCH_ALL.equals(query)) {
            query = "url:http";
        }
        paramsMap.put("query", query);
        if (StringUtils.isNotBlank(lang)) {
            paramsMap.put("lang", "" + lang);
        }
        paramsMap.put("start", "" + start);
        paramsMap.put("hitsPerPage", "" + hitsPerPage);
        // FIXME
        paramsMap.put("hitsPerDup", "" + Integer.MAX_VALUE);
        Element rootElement = sendGet(openSearchURL, paramsMap);

        SolrDocumentList solrDocumentList = parse(rootElement);
        SimpleOrderedMap<Object> result = new SimpleOrderedMap<Object>();
        result.add("response", solrDocumentList);
        return result;
    }
	
	public static Element sendGet(String openSearchServerURLStr, Map<String, String> paramsMap) {
		if (paramsMap == null) {
			paramsMap = new HashMap<String, String>();
		}
		
		try {
			HttpParams params = new BasicHttpParams();
			for (Iterator<String> it = paramsMap.keySet().iterator(); it.hasNext();) {
				String paramName = (String) it.next();
				String paramValue = (String) paramsMap.get(paramName);
				params.setParameter(paramName, paramValue);
			}

			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, CharSetUtils.UTF_8);
			HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1");
			HttpProtocolParams.setUseExpectContinue(params, true);

			BasicHttpProcessor httpproc = new BasicHttpProcessor();
			// Required protocol interceptors
			httpproc.addInterceptor(new RequestContent());
			httpproc.addInterceptor(new RequestTargetHost());
			// Recommended protocol interceptors
			httpproc.addInterceptor(new RequestConnControl());
			httpproc.addInterceptor(new RequestUserAgent());
			httpproc.addInterceptor(new RequestExpectContinue());

			HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

			HttpContext context = new BasicHttpContext(null);
			URL openSearchServerURL = new URL(openSearchServerURLStr);
			String host = openSearchServerURL.getHost();
			int port = openSearchServerURL.getPort();
			if (port == -1) {
				port = 80;
			}
			HttpHost httpHost = new HttpHost(host, port);

			DefaultHttpClientConnection conn = new DefaultHttpClientConnection();
			ConnectionReuseStrategy connStrategy = new DefaultConnectionReuseStrategy();

			context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
			context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, httpHost);

			try {				
				boolean firstParam = true;
				for (Iterator<String> it = paramsMap.keySet().iterator(); it.hasNext();) {
					String paramName = (String) it.next();
					String paramValue = (String) paramsMap.get(paramName);
					if (paramValue != null) {
			            try {
			            	paramValue = URLEncoder.encode(paramValue, CharSetUtils.ISO_8859_1);
			            } catch (UnsupportedEncodingException e) {
			                throw new RuntimeException(e);
			            }
					}

					if (firstParam) {
						openSearchServerURLStr += "?";
						firstParam = false;
					} else {
						openSearchServerURLStr += "&";
					}
					openSearchServerURLStr += paramName + "=" + paramValue;
				}

				if (!conn.isOpen()) {
					Socket socket = new Socket(host, port);
					conn.bind(socket, params);
				}
				BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("GET", openSearchServerURLStr);
				LOGGER.fine(">> Request URI: " + request.getRequestLine().getUri());

				request.setParams(params);
				httpexecutor.preProcess(request, httpproc, context);
				HttpResponse response = httpexecutor.execute(request, conn, context);
				response.setParams(params);
				httpexecutor.postProcess(response, httpproc, context);

				LOGGER.fine("<< Response: " + response.getStatusLine());
				String entityText = EntityUtils.toString(response.getEntity());
				LOGGER.fine(entityText);
				LOGGER.fine("==============");
				if (!connStrategy.keepAlive(response, context)) {
					conn.close();
				} else {
					LOGGER.fine("Connection kept alive...");
				}

				try {
	                Document xml = DocumentHelper.parseText(entityText);
	                return xml.getRootElement();
                } catch (RuntimeException e) {
                    LOGGER.severe("Error caused by text : " + entityText);
                    throw e;
                }
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    @SuppressWarnings("unchecked")
    private static SolrDocumentList parse(Element rootElement) throws IOException {
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        Element channelElement = rootElement.element("channel");
        String totalResultsStr = channelElement.elementText(new QName("totalResults", NS_OPENSEARCH));
        String startIndexStr = channelElement.elementText(new QName("startIndex", NS_OPENSEARCH));
        long numFound = Long.parseLong(totalResultsStr);
        long start = Long.parseLong(startIndexStr);
        solrDocumentList.setNumFound(numFound);
        solrDocumentList.setStart(start);

        for (Iterator<Element> it = channelElement.elementIterator("item"); it.hasNext();) {
            Element itemElement = it.next();
            String title = itemElement.elementText("title");
            String description = itemElement.elementText("description");
            String link = itemElement.elementText("link");
            
            title = CharSetUtils.convert(title, CharSetUtils.UTF_8, CharSetUtils.ISO_8859_1);
            description = CharSetUtils.convert(description, CharSetUtils.UTF_8, CharSetUtils.ISO_8859_1);
            link = CharSetUtils.convert(link, CharSetUtils.UTF_8, CharSetUtils.ISO_8859_1);

            SolrDocument solrDocument = new SolrDocument();
            solrDocument.addField("title", title);
            solrDocument.addField("description", description);
            solrDocument.addField("link", link);
            solrDocumentList.add(solrDocument);
        }
        return solrDocumentList;
    }

    private static void printResults(SolrDocumentList solrDocumentList) {
        System.out.println("numFound:" + solrDocumentList.getNumFound());
        System.out.println("start:" + solrDocumentList.getStart());
        for (SolrDocument solrDocument : solrDocumentList) {
            System.out.println("**************");
            System.out.println("title:" + solrDocument.getFieldValue("title"));
            System.out.println("description:" + solrDocument.getFieldValue("description"));
            System.out.println("link:" + solrDocument.getFieldValue("link"));
        }
    }

    public static void main(String[] args) throws Exception {
        SolrDocumentList solrDocumentList;
        //        
        // InputStream inputXML = OpenSearchSolrServer.class.getResourceAsStream("opensearch_example.xml");
        // SAXReader saxReader = new SAXReader();
        // Document doc = saxReader.read(inputXML);
        // IOUtils.closeQuietly(inputXML);
        //
        // System.out.println("Mock request");
        // solrDocumentList = parse(doc.getRootElement());
        // printResults(solrDocumentList);

        System.out.println("Real request");
        OpenSearchSolrServer solrServer = new OpenSearchSolrServer();
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.add("openSearchURL", "http://recherched.gouv.qc.ca/internet/opensearch");
        params.add(CommonParams.Q, "doculibre");
        params.add(CommonParams.START, "5");
        params.add(CommonParams.ROWS, "10");
        params.add("lang", "en");
        NamedList<Object> results = solrServer.request(new QueryRequest(params));
        solrDocumentList = (SolrDocumentList) results.get("response");
        printResults(solrDocumentList);

        QueryResponse queryResponse = solrServer.query(params);
        solrDocumentList = queryResponse.getResults();
        printResults(solrDocumentList);
    }

}
