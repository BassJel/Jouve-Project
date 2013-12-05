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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.doculibre.constellio.feedprotocol.model.Feed;
import com.doculibre.constellio.feedprotocol.parse.xml.FeedParser;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.google.enterprise.connector.pusher.GsaFeedConnection;

public class FeedServlet extends HttpServlet {

	private static final Logger LOG = Logger.getLogger(FeedServlet.class.getName());

	private static final long serialVersionUID = 1L;
	
	private ThreadPoolExecutor threadPoolExecutor;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("FeedServlet Started");
		
		int feedProcessorThreads = ConstellioSpringUtils.getFeedProcessorThreads();
		threadPoolExecutor = new ThreadPoolExecutor(
				feedProcessorThreads, 
				feedProcessorThreads, 
				5, 
				TimeUnit.MINUTES, 
				new ArrayBlockingQueue<Runnable>(feedProcessorThreads + 1),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.print("Service online!");
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		LOG.fine("FeedServlet: doPost(...)");
		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		PrintWriter out = null;
		try {
			out = response.getWriter();
			if (isMultipart) {
				ServletFileUpload upload = new ServletFileUpload();

				String datasource = null;
				String feedtype = null;

				FileItemIterator iter = upload.getItemIterator(request);
				while (iter.hasNext()) {
					FileItemStream item = iter.next();
					//Disabled to allow easier update from HTML forms
					//if (item.isFormField()) {
						if (item.getFieldName().equals(FeedParser.XML_DATASOURCE)) {
							InputStream itemStream = null;
							try {
								itemStream = item.openStream();
								datasource = IOUtils.toString(itemStream);
							} finally {
								IOUtils.closeQuietly(itemStream);
							}
						} else if (item.getFieldName().equals(FeedParser.XML_FEEDTYPE)) {
							InputStream itemStream = null;
							try {
								itemStream = item.openStream();
								feedtype = IOUtils.toString(itemStream);
							} finally {
								IOUtils.closeQuietly(itemStream);
							}
						} else if (item.getFieldName().equals(FeedParser.XML_DATA)) {
							try {
								if (StringUtils.isBlank(datasource)) {
									throw new IllegalArgumentException("Datasource is blank");
								}
								if (StringUtils.isBlank(feedtype)) {
									throw new IllegalArgumentException("Feedtype is blank");
								}

								InputStream contentStream = null;
								try {
									contentStream = item.openStream();
									final Feed feed = new FeedParser().parse(datasource, feedtype, contentStream);

									Callable<Object> processFeedTask = new Callable<Object>() {
										@Override
										public Object call() throws Exception {
											// FIXME, we presume a unique datasource name
											FeedProcessor feedProcessor = new FeedProcessor(feed);
											feedProcessor.processFeed();
											return null;
										}
									};
									threadPoolExecutor.submit(processFeedTask);
									
									out.append(GsaFeedConnection.SUCCESS_RESPONSE);
									return;
								} catch (Exception e) {
									LOG.log(Level.SEVERE, "Exception while processing contentStream", e);
								} finally {
									IOUtils.closeQuietly(contentStream);
								}
							} finally {
								IOUtils.closeQuietly(out);
							}
						}
					//}
				}
			}
		} catch (Throwable e) {
			LOG.log(Level.SEVERE, "Exception while uploading", e);
		} finally {
			IOUtils.closeQuietly(out);
		}
		out.append(GsaFeedConnection.INTERNAL_ERROR_RESPONSE);
	}
}
