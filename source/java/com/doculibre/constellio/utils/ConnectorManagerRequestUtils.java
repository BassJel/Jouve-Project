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

import java.io.StringWriter;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.doculibre.constellio.entities.ConnectorManager;

public class ConnectorManagerRequestUtils {

    private static final Logger LOGGER = Logger.getLogger(ConnectorManagerRequestUtils.class.getName());
	
	public static Element sendGet(ConnectorManager connectorManager, String servletPath, Map<String, String> paramsMap) {
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
			HttpProtocolParams.setContentCharset(params, "UTF-8");
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
			URL connectorManagerURL = new URL(connectorManager.getUrl());
			HttpHost host = new HttpHost(connectorManagerURL.getHost(), connectorManagerURL.getPort());

			DefaultHttpClientConnection conn = new DefaultHttpClientConnection();
			ConnectionReuseStrategy connStrategy = new DefaultConnectionReuseStrategy();

			context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
			context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);

			try {				
				String target = connectorManager.getUrl() + servletPath;
				boolean firstParam = true;
				for (Iterator<String> it = paramsMap.keySet().iterator(); it.hasNext();) {
					String paramName = (String) it.next();
					String paramValue = (String) paramsMap.get(paramName);

					if (firstParam) {
						target += "?";
						firstParam = false;
					} else {
						target += "&";
					}
					target += paramName + "=" + paramValue;
				}

				if (!conn.isOpen()) {
					Socket socket = new Socket(host.getHostName(), host.getPort());
					conn.bind(socket, params);
				}
				BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("GET", target);
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
                } catch (Exception e) {
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

	public static Element sendPost(ConnectorManager connectorManager, String servletPath, Document document) {
		try {
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, "UTF-8");
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

			URL connectorManagerURL = new URL(connectorManager.getUrl());
			HttpHost host = new HttpHost(connectorManagerURL.getHost(), connectorManagerURL.getPort());

			DefaultHttpClientConnection conn = new DefaultHttpClientConnection();
			ConnectionReuseStrategy connStrategy = new DefaultConnectionReuseStrategy();

			context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
			context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);

			try {
				HttpEntity requestBody;
				if (document != null) {
//			        OutputFormat format = OutputFormat.createPrettyPrint();
				    OutputFormat format = OutputFormat.createCompactFormat();
                    StringWriter stringWriter = new StringWriter();
			        XMLWriter xmlWriter = new XMLWriter(stringWriter, format);
			        xmlWriter.write(document);
			        String xmlAsString = stringWriter.toString();
					requestBody = new StringEntity(xmlAsString, "UTF-8");
				} else {
					requestBody = null;
				}
				if (!conn.isOpen()) {
					Socket socket = new Socket(host.getHostName(), host.getPort());
					conn.bind(socket, params);
				}

				String target = connectorManager.getUrl() + servletPath;

				BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", target);
				request.setEntity(requestBody);
				LOGGER.info(">> Request URI: " + request.getRequestLine().getUri());

				request.setParams(params);
				httpexecutor.preProcess(request, httpproc, context);
				HttpResponse response = httpexecutor.execute(request, conn, context);
				response.setParams(params);
				httpexecutor.postProcess(response, httpproc, context);

				LOGGER.info("<< Response: " + response.getStatusLine());
				String entityText = EntityUtils.toString(response.getEntity());
				LOGGER.info(entityText);
				LOGGER.info("==============");
				if (!connStrategy.keepAlive(response, context)) {
					conn.close();
				} else {
					LOGGER.info("Connection kept alive...");
				}

				Document xml = DocumentHelper.parseText(entityText);
				return xml.getRootElement();
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
