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
package com.doculibre.constellio.solr.handler.component;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.SolrQueryParser;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.UserCredentials;
import com.doculibre.constellio.services.ACLServices;
import com.doculibre.constellio.services.ConnectorManagerServices;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.solr.handler.ConstellioSolrQueryParams;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

/**
 * A component to elevate some documents to the top of the result set.
 * 
 * @version $Id: ConstellioElevationComponent.java 949888 2010-05-31 23:24:40Z hossman $
 * @since solr 1.3
 */
public class ConstellioAuthorizationComponent extends SearchComponent implements SolrCoreAware {

    private static Logger log = LoggerFactory.getLogger(ConstellioAuthorizationComponent.class);

    // ---------------------------------------------------------------------------------
    // SearchComponent
    // ---------------------------------------------------------------------------------

    // Runtime param -- should be in common?
    public static final String ENABLE = "enableAuthorization";

    @SuppressWarnings("unused")
    private SolrParams initArgs = null;

    @SuppressWarnings("unchecked")
    @Override
    public void init(NamedList args) {
        this.initArgs = SolrParams.toSolrParams(args);
    }

    public void inform(SolrCore core) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
        SolrQueryRequest req = rb.req;
        SolrIndexSearcher searcher = req.getSearcher();
        //IndexReader reader = req.getSearcher().getReader();
        SolrParams params = req.getParams();

        // A runtime param can skip
        if (!params.getBool(ENABLE, true)) {
            return;
        }

        Query query = rb.getQuery();
        String qstr = rb.getQueryString();
        if (query == null || qstr == null) {
            return;
        }

        ConstellioUser user;
        String userIdStr = params.get(ConstellioSolrQueryParams.USER_ID);
        if (userIdStr != null) {
            UserServices userServices = ConstellioSpringUtils.getUserServices();
            try {
                user = userServices.get(new Long(userIdStr));
            } catch (NumberFormatException e) {
                user = null;
            }
        } else {
            user = null;
        }

        String collectionName = params.get(ConstellioSolrQueryParams.COLLECTION_NAME);
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
        RecordCollection collection = collectionServices.get(collectionName);

        List<TermQuery> restrictedCollectionQueries = new ArrayList<TermQuery>();
        if (collection.isFederationOwner()) {
            List<RecordCollection> includedCollections = federationServices
                .listIncludedCollections(collection);
            for (RecordCollection includedCollection : includedCollections) {
                if (includedCollection.hasSearchPermission()
                    && (user == null || !user.hasSearchPermission(includedCollection))) {
                    restrictedCollectionQueries.add(new TermQuery(new Term(IndexField.COLLECTION_ID_FIELD, ""
                        + includedCollection.getId())));
                }
            }
        }

        // User must be logged in to see private records
        if (user != null) {
            String luceneQueryStr = params.get(ConstellioSolrQueryParams.LUCENE_QUERY);
            if (StringUtils.isBlank(luceneQueryStr)) {
            	return;
            }
            
            IndexSchema schema = req.getSchema();
            SolrQueryParser queryParser = new SolrQueryParser(schema, IndexField.DEFAULT_SEARCH_FIELD);
            Query luceneQuery;
            try {
                luceneQuery = queryParser.parse(luceneQueryStr);
            } catch (ParseException e) {
                log.error("Error parsing lucene query " + luceneQueryStr, e);
                return;
            }
            // Create a new query which will only include private records
            BooleanQuery privateRecordQuery = new BooleanQuery(true);
            privateRecordQuery.add(luceneQuery, BooleanClause.Occur.MUST);
            for (TermQuery restrictionCollectionQuery : restrictedCollectionQueries) {
                privateRecordQuery.add(restrictionCollectionQuery, BooleanClause.Occur.MUST_NOT);
            }

            TermQuery privateRecordTQ = new TermQuery(new Term(IndexField.PUBLIC_RECORD_FIELD, "F"));
            privateRecordQuery.add(privateRecordTQ, BooleanClause.Occur.MUST);

            DocSet privateRecordIdDocSet = searcher.getDocSet(privateRecordQuery);

            if (privateRecordIdDocSet.size() > 0) {
                RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
                ACLServices aclServices = ConstellioSpringUtils.getACLServices();
                ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
                    .getConnectorManagerServices();
                
                List<Record> privateRecords = new ArrayList<Record>();
                DocIterator docIt = privateRecordIdDocSet.iterator();
                while (docIt.hasNext()) {
                	int docId = docIt.nextDoc();
                	Document luceneDoc = searcher.doc(docId);
                	Long recordId = new Long(luceneDoc.get(IndexField.RECORD_ID_FIELD));
                	Record record = recordServices.get(recordId, collection);
                	privateRecords.add(record);
                }
                // First pass : Remove ACL authorized records
                List<Record> unevaluatedPrivateRecords = aclServices.removeAuthorizedRecords(privateRecords,
                    user);
                if (!unevaluatedPrivateRecords.isEmpty()) {
                    Set<UserCredentials> userCredentials = user.getUserCredentials();
                    // Second pass : Ask the connector manager
                    ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
                    List<Record> authorizedRecords = connectorManagerServices.authorizeByConnector(
                        unevaluatedPrivateRecords, userCredentials, connectorManager);
                    List<Record> unauthorizedRecords = ListUtils.removeAll(unevaluatedPrivateRecords,
                        authorizedRecords);

                    if (!unauthorizedRecords.isEmpty()) {
                        // Create a new query which will exclude unauthorized records
                        BooleanQuery authorizedRecordQuery = new BooleanQuery(true);
                        authorizedRecordQuery.add(query, BooleanClause.Occur.MUST);
                        for (Record unauthorizedRecord : unauthorizedRecords) {
                            TermQuery unauthorizedRecordTQ = new TermQuery(new Term(
                                IndexField.RECORD_ID_FIELD, "" + unauthorizedRecord.getId()));
                            authorizedRecordQuery.add(unauthorizedRecordTQ, BooleanClause.Occur.MUST_NOT);
                        }
                        rb.setQuery(authorizedRecordQuery);
                    }
                }
            }
        } else {
            BooleanQuery publicRecordQuery = new BooleanQuery(true);
            publicRecordQuery.add(query, BooleanClause.Occur.MUST);
            TermQuery publicRecordTQ = new TermQuery(new Term(IndexField.PUBLIC_RECORD_FIELD, "T"));
            publicRecordQuery.add(publicRecordTQ, BooleanClause.Occur.MUST);
            for (TermQuery restrictionCollectionQuery : restrictedCollectionQueries) {
                publicRecordQuery.add(restrictionCollectionQuery, BooleanClause.Occur.MUST_NOT);
            }
            rb.setQuery(publicRecordQuery);
        }
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {
        // Do nothing -- the real work is modifying the input query
    }

    // ---------------------------------------------------------------------------------
    // SolrInfoMBean
    // ---------------------------------------------------------------------------------

    @Override
    public String getDescription() {
        return "Document authorization -- remove unauthorized documents";
    }

    @Override
    public String getVersion() {
        return "$Revision: 1 $";
    }

    @Override
    public String getSourceId() {
        return "$Id: ConstellioAuthorizationComponent.java 1 2010-10-13 23:58:00Z vdussault $";
    }

    @Override
    public String getSource() {
        return "$URL: http://svn.doculibre.com/constellio/trunk/source/java/com/doculibre/constellio/solr/handler/component/ConstellioAuthorizationComponent.java $";
    }

}
