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
package com.doculibre.constellio.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.clustering.ClusteringComponent;
import org.apache.solr.handler.clustering.ClusteringParams;
import org.apache.solr.handler.clustering.carrot2.CarrotParams;

import com.doculibre.constellio.entities.CollectionFacet;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.solr.handler.ConstellioSolrQueryParams;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class ClusteringServicesImpl implements ClusteringServices {

	@SuppressWarnings("unchecked")
	@Override
	public List<SimpleOrderedMap<Object>> cluster(SimpleSearch simpleSearch, CollectionFacet facet, int start, int row, ConstellioUser user) {
//		String luceneQuery = simpleSearch.getLuceneQuery();
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        String solrServerName = simpleSearch.getCollectionName();
        RecordCollection collection = collectionServices.get(solrServerName);
		
		//Remarque useDismax est à false car nous allons utiliser le type de requete "/clustering"
        //         donc pas besoin d'ajouter les paramtres necessaires à dismax
		SolrQuery query = SearchServicesImpl.toSolrQuery(simpleSearch, false, true, true);
		query.setRequestHandler("/clustering");
		
		query.setParam(ClusteringComponent.COMPONENT_NAME, Boolean.TRUE);
		query.setParam(ClusteringParams.USE_COLLECTION, facet.isClusteringUseCollection());
		query.setParam(ClusteringParams.USE_SEARCH_RESULTS, facet.isClusteringUseSearchResults());
		query.setParam(ClusteringParams.USE_DOC_SET, facet.isClusteringUseDocSet());
		query.setParam(ClusteringParams.ENGINE_NAME, facet.getClusteringEngine());
		
		//The maximum number of labels to produce
		query.setParam(CarrotParams.NUM_DESCRIPTIONS, "" + maxClusters);
//		query.setParam(CarrotParams.NUM_DESCRIPTIONS, "" + facet.getCarrotNumDescriptions());
		
		query.setParam(CarrotParams.OUTPUT_SUB_CLUSTERS, facet.isCarrotOutputSubclusters());
        query.setParam(ConstellioSolrQueryParams.LUCENE_QUERY, simpleSearch.getLuceneQuery());
        query.setParam(ConstellioSolrQueryParams.COLLECTION_NAME, simpleSearch.getCollectionName());
        query.setParam(ConstellioSolrQueryParams.SIMPLE_SEARCH, simpleSearch.toSimpleParams().toString());
        if (user != null) {
	        query.setParam(ConstellioSolrQueryParams.USER_ID, "" + user.getId());
		}
		
		//If true, then the snippet field (if no snippet field, then the title field) will be highlighted and the highlighted text will be used for the snippet. 
		query.setParam(CarrotParams.PRODUCE_SUMMARY, facet.isCarrotProduceSummary());
		if (facet.getCarrotTitleField() != null) {
			IndexField indexField = facet.getCarrotTitleField();
			query.setParam(CarrotParams.TITLE_FIELD_NAME, indexField.getName());
		}
		if (facet.getCarrotUrlField() != null) {
			IndexField indexField = facet.getCarrotUrlField();
			query.setParam(CarrotParams.URL_FIELD_NAME, indexField.getName());
		}
		if (facet.getCarrotSnippetField() != null) {
			IndexField indexField = facet.getCarrotSnippetField();
			query.setParam(CarrotParams.SNIPPET_FIELD_NAME, indexField.getName());
		}
		
		// Requête Lucene
//		query.setQuery(luceneQuery);

		// nb résultats par page
		query.setRows(row);

		// page de début
		query.setStart(start);
		//Les resultats ne vont pas etre affichés
		/*query.setHighlight(true);
		query.setHighlightFragsize(100);
		query.setHighlightSnippets(2);*/
        

        if (collection.isOpenSearch()) {
            query.setParam("openSearchURL", collection.getOpenSearchURL());
        }

		SolrServer server = SolrCoreContext.getSolrServer(solrServerName);
		
		QueryResponse queryResponse;
		try {
			queryResponse = server.query(query);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
		
		NamedList<Object> values = queryResponse.getResponse();
		Object clusters = values.get("clusters");
		return (List<SimpleOrderedMap<Object>>) clusters;
	}

	@Override
	public List<String> getDocIds(String clusterValue, SimpleOrderedMap<Object> cluster) {
		List<String> docIds = new ArrayList<String>();
		return docIds;
	}
	
	

}
