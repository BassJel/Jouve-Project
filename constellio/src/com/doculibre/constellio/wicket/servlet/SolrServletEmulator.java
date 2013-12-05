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
package com.doculibre.constellio.wicket.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.FastWriter;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.BinaryQueryResponseWriter;
import org.apache.solr.response.BinaryResponseWriter;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.servlet.SolrRequestParsers;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchResultField;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.FacetServicesImpl;
import com.doculibre.constellio.services.SearchResultFieldServices;
import com.doculibre.constellio.services.StatsServices;
import com.doculibre.constellio.servlets.ConstellioServletUtils;
import com.doculibre.constellio.servlets.ServletsConstants;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.spellchecker.SpellChecker;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.NamedListUtils;
import com.doculibre.constellio.wicket.application.ConstellioApplication;

public class SolrServletEmulator {
    
	private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final Logger LOGGER = Logger.getLogger(SolrServletEmulator.class.getName());

    @SuppressWarnings("deprecation")
    public void writeResponse(String solrQuery, RecordCollection collection, ConstellioUser user,
        HttpServletRequest request, HttpServletResponse response) {
    	OutputStream outputStream;
		try {
			outputStream = response.getOutputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        if (user != null && !user.hasSearchPermission(collection)) {
            throw new RuntimeException("The user doesn't have search permission on the collection");
        } else if (user == null && collection.hasSearchPermission()) {
            throw new RuntimeException("The collection requires a user with search permission");
        } else {
        	SimpleSearch simpleSearch = new SimpleSearch();
            simpleSearch.setCollectionName(collection.getName());

            if (solrQuery.contains("facet=constellio")) {
                solrQuery = solrQuery.replace("facet=constellio", "facet=on");
                solrQuery = addConstellioFacets(simpleSearch, solrQuery, collection, user);
                LOGGER.info("Using constellio facets, new query is " + solrQuery);
            }
            ModifiableSolrParams solrParams = new ModifiableSolrParams(SolrRequestParsers
                .parseQueryString(solrQuery));
            
            String queryType = solrParams.get("qt");
            if (queryType != null
                && (queryType.toLowerCase().equals("spellchecker") || queryType.toLowerCase().equals(
                    "spellcheck"))) {
                writeSpellCheckerResponse(simpleSearch, solrParams, collection, user, outputStream);

            } else {
                String cdf = solrParams.get("cdf");
                if (cdf != null && cdf.equals("true")) {
                    SearchResultFieldServices searchResultFieldServices = ConstellioSpringUtils
                        .getSearchResultFieldServices();
                    List<SearchResultField> fields = searchResultFieldServices.list();
                    List<String> fieldNames = new ArrayList<String>();
                    for (SearchResultField field : fields) {
                        if (field.getRecordCollection().equals(collection)) {
                            fieldNames.add(field.getIndexField().getName());
                        }
                    }
                    solrParams.add("fl", StringUtils.join(fieldNames.toArray(), ","));
                    solrParams.remove("cdf");
                }
                final SolrCore core = SolrCoreContext.getCores().getCore(collection.getName());
//                SolrServletRequest solrReq = new SolrServletRequest(core, request);
                SolrQueryRequest solrReq = new LocalSolrQueryRequest(core, solrParams);
                SolrQueryResponse solrRsp = new SolrQueryResponse();
                try {

                  SolrRequestHandler handler = core.getRequestHandler(solrReq.getQueryType());
                  if (handler==null) {
                    LOGGER.log(Level.WARNING, "Unknown Request Handler '" + solrReq.getQueryType()
                    + "' :" + solrReq);
//                    log.warn("Unknown Request Handler '" + solrReq.getQueryType() +"' :" + solrReq);
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,"Unknown Request Handler '" + solrReq.getQueryType() + "'", true);
                  }
                  core.execute(handler, solrReq, solrRsp );
                  if (solrRsp.getException() == null) {
                      String ipAddress = request.getRemoteAddr();
                	  
                	  simpleSearch.setQuery(solrParams.get("q"));
                      QueryResponse qrsp = new QueryResponse();
                      qrsp.setResponse(getParsedResponse(solrReq, solrRsp));
                      
                      StatsServices statsServices = ConstellioSpringUtils.getStatsServices();
                      statsServices.logSearch(simpleSearch, qrsp, ipAddress);
                    
                      QueryResponseWriter responseWriter = core.getQueryResponseWriter(solrReq);
                    // Now write it out
                    final String ct = responseWriter.getContentType(solrReq, solrRsp);
                    // don't call setContentType on null
                    if (null != ct) response.setContentType(ct); 
                    if (responseWriter instanceof BinaryQueryResponseWriter) {
                      BinaryQueryResponseWriter binWriter = (BinaryQueryResponseWriter) responseWriter;
                      binWriter.write(outputStream, solrReq, solrRsp);
                    } else {
                      String charset = ContentStreamBase.getCharsetFromContentType(ct);
                      Writer out = (charset == null || charset.equalsIgnoreCase("UTF-8"))
                        ? new OutputStreamWriter(outputStream, UTF8)
                        : new OutputStreamWriter(outputStream, charset);
                      out = new FastWriter(out);
                      responseWriter.write(out, solrReq, solrRsp);
                      out.flush();
                    }
                  } else {
                    Exception e = solrRsp.getException();
                    LOGGER.log(Level.SEVERE, SolrException.toStr(e), e);
//                    outputStream.write(ExceptionUtils.getFullStackTrace(e).getBytes());
                    int rc=500;
                    if (e instanceof SolrException) {
                       rc=((SolrException)e).code();
                    }
                    sendErr(rc, SolrException.toStr(e), response, outputStream);
                  }
                } catch (SolrException e) {
                	if (!e.logged) 
                		LOGGER.log(Level.SEVERE, SolrException.toStr(e), e);
//	                try {
//	                	outputStream.write(ExceptionUtils.getFullStackTrace(e).getBytes());
//	                } catch (IOException e1) {
//	                	throw new RuntimeException(e1);
//	                }
//                  if (!e.logged) SolrException.log(log,e);
                  sendErr(e.code(), SolrException.toStr(e), response, outputStream);
                } catch (Throwable e) {
                	LOGGER.log(Level.SEVERE, SolrException.toStr(e), e);
//	                try {
//	                	outputStream.write(ExceptionUtils.getFullStackTrace(e).getBytes());
//	                } catch (IOException e1) {
//	                	throw new RuntimeException(e1);
//	                }
//                  SolrException.log(log,e);
                  sendErr(500, SolrException.toStr(e), response, outputStream);
                } finally {
                  // This releases the IndexReader associated with the request
                  solrReq.close();
                }
            }
        }
    }
    
    /**
     * Parse the solr response to named list (need to create solrj query respond).
     * 
     * @param req
     *          The request.
     * @param rsp
     *          The response.
     * @return The named list.
     */
    public NamedList<Object> getParsedResponse(SolrQueryRequest req, SolrQueryResponse rsp) {
      try {
        BinaryResponseWriter writer = new BinaryResponseWriter();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writer.write(bos, req, rsp);
        BinaryResponseParser parser = new BinaryResponseParser();
        return parser.processResponse(new ByteArrayInputStream(bos.toByteArray()), "UTF-8");
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }


    final void sendErr(int rc, String msg, HttpServletResponse response, OutputStream outputStream) {
//        try {
            // hmmm, what if this was already set to text/xml?
            try {
                response.setContentType(QueryResponseWriter.CONTENT_TYPE_TEXT_UTF8);
                // response.setCharacterEncoding("UTF-8");
            } catch (Exception e) {
            }
            try {
                response.setStatus(rc);
            } catch (Exception e) {
            }
//            PrintWriter writer = response.getWriter();
            PrintWriter writer = new PrintWriter(outputStream);
            writer.write(msg);
//        } catch (IOException e) {
//            LOGGER.log(Level.SEVERE, SolrException.toStr(e), e);
//        }
    }

    /**
     * Add Constellio's facets to the query params
     * 
     * @param solrParams
     * @param collection
     * @param user
     */
    private String addConstellioFacets(SimpleSearch simpleSearch, String solrParams, RecordCollection collection, ConstellioUser user) {
        simpleSearch.setCollectionName(collection.getName());
        simpleSearch.setQuery("not important");

        StringBuffer queryWithFacets = new StringBuffer(solrParams);

        String fakeQueryWithFacets = FacetServicesImpl.toSolrQuery(simpleSearch, 0, 100, true, false, null,
            null, user).toString();
        for (String paramValue : fakeQueryWithFacets.split("&")) {
            String[] splitted = paramValue.split("[\\=]");
            if (splitted.length == 2) {
                String param = splitted[0];
                if (param.equals("facet.field") || param.equals("facet.query")) {
                    if (!solrParams.contains(paramValue)) {
                        queryWithFacets.append("&" + paramValue);
                    }
                }
            }
        }
        return queryWithFacets.toString();
    }

    @SuppressWarnings("unchecked")
    private void writeSpellCheckerResponse(SimpleSearch simpleSearch, SolrParams solrParams, RecordCollection collection,
        ConstellioUser user, OutputStream outputStream) {
        long start = Calendar.getInstance().getTimeInMillis();
        String query = solrParams.get("q");

        ConstellioApplication.initializeIfRequired();
        NamedList<Object> successfulNamedList = ConstellioServletUtils.getSuccessfulNamedList(solrParams);

        String dictionaries = ConstellioApplication.get().getDictionaries();
        SpellChecker spellChecker = new SpellChecker(dictionaries);

        if (query.equals("*:*")) {
            throw new RuntimeException("Cannot run spell checking on :*:");
        }

        if (collection == null) {
            throw new RuntimeException("Must specify collection name");
        }

        NamedList<Object> suggestedWords;
        try {
            suggestedWords = spellChecker.suggestNamedList(query, collection.getName());
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Error while querying spell checker", e);
            suggestedWords = new NamedList<Object>();
        }
        successfulNamedList.add("spellcheck", suggestedWords);

        NamedList<Object> responseHeader = (NamedList<Object>) successfulNamedList
            .get(ServletsConstants.RESPONSE_HEADER);
        int qTime = new Long(Calendar.getInstance().getTimeInMillis() - start).intValue();
        responseHeader.add("QTime", qTime);

        NamedListUtils.convertResponseNamedListToXML(successfulNamedList, outputStream);
    }

}
