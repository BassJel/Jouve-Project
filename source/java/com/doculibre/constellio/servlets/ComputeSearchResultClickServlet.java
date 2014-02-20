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
package com.doculibre.constellio.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.services.StatsServices;
import com.doculibre.constellio.utils.CharSetUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.SimpleParams;
import com.doculibre.constellio.utils.WebappUtils;
import com.doculibre.constellio.wicket.utils.SimpleParamsUtils;

@SuppressWarnings("serial")
public class ComputeSearchResultClickServlet extends HttpServlet {

	public static final String RECORD_COLLECTION_PARAM = "recordCollection";
	public static final String URL_PARAM = "url";
	
	public static String getCallbackJavascript(HttpServletRequest httpRequest, SimpleSearch simpleSearch, Record record) {
		RecordCollection collection = record.getConnectorInstance().getRecordCollection();
		String recordURL = record.getUrl();
		String collectionName = collection.getName();
		
        SimpleSearch clone = simpleSearch.clone();
        clone.setQuery(CharSetUtils.urlEncode(simpleSearch.getQuery(), CharSetUtils.ISO_8859_1));
        SimpleParams simpleParams = clone.toSimpleParams();

        StringBuffer servletURL = new StringBuffer();
        servletURL.append(WebappUtils.getContextPathURL(httpRequest).toString());
        servletURL.append("/computeSearchResultClick?");
        servletURL.append(URL_PARAM);
        servletURL.append("=");
        servletURL.append(CharSetUtils.urlEncode(recordURL, CharSetUtils.ISO_8859_1));
        servletURL.append("&");
        servletURL.append(RECORD_COLLECTION_PARAM);
        servletURL.append("=");
        servletURL.append(collectionName);
        String servletURLWithParams = SimpleParamsUtils.appendParams(servletURL.toString(), simpleParams);
        return "sendGet('" + servletURLWithParams + "');";
//        return "sendGet('" + servletURLWithParams + "'); window.location=this.href;";
	}

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        SimpleParams simpleParams = SimpleParamsUtils.toSimpleParams(request);
        SimpleSearch simpleSearch = SimpleSearch.toSimpleSearch(simpleParams);

        String clickCollectionName = simpleSearch.getCollectionName();
        // Collection who owns the record
        String recordCollectionName = request.getParameter(RECORD_COLLECTION_PARAM);
        String recordUrl = request.getParameter(URL_PARAM);
        String searchLogDocId = simpleSearch.getSearchLogDocId();
        String ipAddress = request.getRemoteAddr();
        
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        StatsServices statsServices = ConstellioSpringUtils.getStatsServices();
        
        RecordCollection clickCollection = collectionServices.get(clickCollectionName);
        RecordCollection recordCollection = collectionServices.get(recordCollectionName);
        Record record = recordServices.get(recordUrl, recordCollection);
        
        statsServices.logClick(clickCollection, record, searchLogDocId, ipAddress);
        
        String encoding = CharSetUtils.UTF_8;
		response.setContentType("text/xml; charset=" + encoding);
		
		PrintWriter writer = response.getWriter();

		// Make sure it is not cached by a client
		response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");

		writer.write("<?xml version=\"1.0\" encoding=\"");
		writer.write(encoding);
		writer.write("\"?>");
		writer.write("<ajax-response>");
		writer.write("</ajax-response>");
    }

}
