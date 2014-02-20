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
package com.doculibre.constellio.stats.report;

import java.util.Arrays;
import java.util.List;

public class StatsConstants {
	
	public static final String REQUEST_LOG = "requestLog";
	public static final String MOST_POPULAR_REQUEST = "mostPopularReq";
	public static final String MOST_POPULAR_REQUEST_WITHOUT_RESULTS = "mostPopularReqWOResults";
	public static final String MOST_POPULAR_REQUEST_WITH_RESULTS = "mostPopularReqWResults";
	public static final String MOST_POPULAR_REQUEST_WITHOUT_MOUSE_CLICK = "mostPopularReqWOMouseClick";
	public static final String MOST_POPULAR_REQUEST_WITH_MOUSE_CLICK = "mostPopularReqWMouseClick";

	public static final List<String> ALL_STATS = Arrays.asList(new String[] { REQUEST_LOG,
			MOST_POPULAR_REQUEST, MOST_POPULAR_REQUEST_WITHOUT_RESULTS, MOST_POPULAR_REQUEST_WITH_RESULTS,
			MOST_POPULAR_REQUEST_WITHOUT_MOUSE_CLICK, MOST_POPULAR_REQUEST_WITH_MOUSE_CLICK });

	public static final String DATA_DATE = "date";
	public static final String DATA_COLLECTION = "collection";
	public static final String DATA_QUERY = "query";
	public static final String DATA_PAGE = "page";
	public static final String DATA_NUMBER_RESULTS = "nbResults";
	public static final String DATA_RESPONSE_TIME = "responseTime";
	public static final String DATA_FREQUENCY = "frequency";
	public static final String DATA_NUMBER_CLICKED = "nbClicked";
	public static final String DATA_BEST_CLICKED = "bestClicked";

	public static final String INDEX_FIELD_ID = "id";
	public static final String INDEX_FIELD_COLLECTION_NAME = "collectionName";
	public static final String INDEX_FIELD_SIMPLE_SEARCH_ID = "simpleSearchId";
	public static final String INDEX_FIELD_SIMPLE_SEARCH = "simpleSearch";
	public static final String INDEX_FIELD_SIMPLE_SEARCH_QUERY_ANALYZED = "simpleSearchQueryAnalyzed";
	public static final String INDEX_FIELD_QUERY_TEXT = "queryText";
	public static final String INDEX_FIELD_QUERY_TEXT_ANALYZED = "queryTextAnalyzed";
	public static final String INDEX_FIELD_NUM_FOUND = "numFound";
	public static final String INDEX_FIELD_RESPONSE_TIME = "responseTime";
	public static final String INDEX_FIELD_SEARCH_DATE = "searchDate";
	public static final String INDEX_FIELD_SEARCH_PAGE = "searchPage";
	public static final String INDEX_FIELD_SEARCH_LOG_DOC_ID = "searchLogDocId";
	public static final String INDEX_FIELD_RECORD_URL = "recordUrl";
	public static final String INDEX_FIELD_DISPLAY_URL = "displayUrl";
	public static final String INDEX_FIELD_CLICK_DATE = "clickDate";
	public static final String INDEX_FIELD_SEARCH_PERIOD = "searchPeriod";
	public static final String INDEX_FIELD_HAS_RESULT = "hasResult";
	public static final String INDEX_FIELD_HAS_CLICK = "hasClick";
	public static final String INDEX_FIELD_SEARCH_COUNT = "searchCount";
	public static final String INDEX_FIELD_CLICK_COUNT = "clickCount";
	public static final String INDEX_FIELD_IP_ADDRESS = "ipAddress";

	public static final List<String> DATA_REQUEST_LOG = Arrays.asList(new String[] { DATA_DATE, 
			DATA_COLLECTION, DATA_QUERY, DATA_PAGE, DATA_NUMBER_RESULTS, DATA_RESPONSE_TIME, DATA_NUMBER_CLICKED });

	public static final List<String> DATA_MOST_POPULAR_REQUEST = Arrays.asList(new String[] {
			DATA_COLLECTION, DATA_QUERY, DATA_FREQUENCY,
			DATA_NUMBER_CLICKED, DATA_BEST_CLICKED });

	public static final List<String> DATA_MOST_POPULAR_REQUEST_WITHOUT_RESULTS = Arrays
			.asList(new String[] { DATA_COLLECTION, DATA_QUERY, DATA_FREQUENCY });

	public static final List<String> DATA_MOST_POPULAR_REQUEST_WITH_RESULTS = Arrays.asList(new String[] {
			DATA_COLLECTION, DATA_QUERY, DATA_FREQUENCY,
			DATA_NUMBER_CLICKED, DATA_BEST_CLICKED });

	public static final List<String> DATA_MOST_POPULAR_REQUEST_WITHOUT_MOUSE_CLICK = Arrays
			.asList(new String[] { DATA_COLLECTION, DATA_QUERY, DATA_FREQUENCY });

	public static final List<String> DATA_MOST_POPULAR_REQUEST_WITH_MOUSE_CLICK = Arrays
			.asList(new String[] { DATA_COLLECTION, DATA_QUERY, DATA_FREQUENCY, DATA_NUMBER_CLICKED, DATA_BEST_CLICKED });

}
