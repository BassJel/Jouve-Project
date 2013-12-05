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
package com.doculibre.constellio.solr.handler.cluster;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocSet;
import org.apache.lucene.search.Query;


/**
 * Experimental.  Subject to change before the next release.
 *
 **/
public abstract class DocumentClusteringEngine extends ClusteringEngine {

  /**
   * Experimental.  Subject to change before the next release
   *
   * Cluster all the documents in the index.  Clustering is often an expensive task that can take a long time.
   * @param solrParams The params controlling clustering
   * @return The clustering results
   */
  public abstract NamedList cluster(SolrParams solrParams);

  /**
   *  Experimental.  Subject to change before the next release
   *
   *
   * Cluster the set of docs.  Clustering of documents is often an expensive task that can take a long time.
   * @param docs The docs to cluster.  If null, cluster all docs as in {@link #cluster(org.apache.solr.common.params.SolrParams)}
   * @param solrParams The params controlling the clustering
   * @return The results.
   */
  public abstract NamedList cluster(DocSet docs, SolrParams solrParams);


}