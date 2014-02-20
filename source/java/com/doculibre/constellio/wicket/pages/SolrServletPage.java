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
package com.doculibre.constellio.wicket.pages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequestCycle;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.solr.handler.ConstellioSolrQueryParams;
import com.doculibre.constellio.utils.CharSetUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.SimpleParams;
import com.doculibre.constellio.wicket.servlet.SolrServletEmulator;
import com.doculibre.constellio.wicket.utils.SimpleParamsUtils;

@Deprecated
public class SolrServletPage extends WebPage {

    public SolrServletPage(PageParameters parameters) {
        super(parameters);
        
        String collectionName = parameters.getString(SimpleSearch.COLLECTION_NAME);
        String username = parameters.getString("username");
        String password = parameters.getString("password");
        
        if (StringUtils.isBlank(collectionName)) {
            throw new WicketRuntimeException(SimpleSearch.COLLECTION_NAME + " parameter is required");
        } 
        
        SimpleParams simpleParams = SimpleParamsUtils.toSimpleParams(parameters);
        simpleParams.remove("username");
        simpleParams.remove("password");

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        UserServices userServices = ConstellioSpringUtils.getUserServices();
        RecordCollection collection = collectionServices.get(collectionName);
        
        if (collection == null) {
            throw new WicketRuntimeException("Invalid collection name : " + collectionName);
        }
        
        ConstellioUser user;
        if (StringUtils.isNotBlank(username)) {
            user = userServices.get(username);
            if (user != null) {
                String passwordHash = ConstellioUser.getHash(password);
                if (!user.getPasswordHash().equals(passwordHash)) {
                    throw new WicketRuntimeException("Invalid password");
                }
            } else {
                throw new WicketRuntimeException("Invalid username : " + username);
            }
        } else {
            user = null;
        }
        
        if (user != null) {
        	simpleParams.add(ConstellioSolrQueryParams.USER_ID, String.valueOf(user.getId()));
        }

        simpleParams.encodeURL(CharSetUtils.UTF_8);
        String solrQuery = SimpleParamsUtils.appendParams("", simpleParams);
        
        WebRequestCycle requestCycle = (WebRequestCycle) RequestCycle.get();
        HttpServletRequest request = requestCycle.getWebRequest().getHttpServletRequest();
        HttpServletResponse response = requestCycle.getWebResponse().getHttpServletResponse();
        SolrServletEmulator solrServletEmulator = new SolrServletEmulator();
        solrServletEmulator.writeResponse(solrQuery, collection, user, request, response);

//        StringWriter stringWriter = new StringWriter();
//        Set<String> contentTypeHolder = new HashSet<String>();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        solrServletEmulator.writeResponse(solrQuery, collection, user, baos, contentTypeHolder);
//        String result = baos.toString();
//        
//        try {
//            Document xml = DocumentHelper.parseText(result);
//            OutputFormat format = OutputFormat.createPrettyPrint();
//            StringWriter prettyStringWriter = new StringWriter();
//            XMLWriter xmlWriter = new XMLWriter(prettyStringWriter, format);
//            xmlWriter.write(xml);
//            String xmlAsString = prettyStringWriter.toString();
//            Response response = RequestCycle.get().getResponse();
//            response.setCharacterEncoding("UTF-8");
//            IOUtils.write(xmlAsString, response.getOutputStream());
//        } catch (DocumentException e) {
//            Response response = RequestCycle.get().getResponse();
//            if (!contentTypeHolder.isEmpty()) {
//            	String contentType = contentTypeHolder.iterator().next();
//            	response.setContentType(contentType);
//            }
////            response.setCharacterEncoding("UTF-8");
//            try {
//				IOUtils.write(baos.toByteArray(), response.getOutputStream());
//			} catch (IOException e1) {
//	            throw new WicketRuntimeException(result, e1);
//			}
////            throw new WicketRuntimeException(result, e);
//        } catch (IOException e) {
//            throw new WicketRuntimeException(result, e);
//        } catch (Exception e) {
//            throw new WicketRuntimeException(result, e);
//        }
    }

    @Override
    protected void onRender(MarkupStream markupStream) {
        // Do nothing since response writer was already used
    }

}
