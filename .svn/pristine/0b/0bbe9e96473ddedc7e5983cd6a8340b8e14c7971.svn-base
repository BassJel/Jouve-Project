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
package org.apache.solr.highlight;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;
import com.doculibre.constellio.entities.IndexField;

/**
 * @since solr 1.3
 */
public class ParsedContentSolrHighlighter extends DefaultSolrHighlighter {

	public ParsedContentSolrHighlighter() {
		super();
	}

	public ParsedContentSolrHighlighter(SolrCore solrCore) {
		super(solrCore);
	}
	
    /**
     * Generates a list of Highlighted query fragments for each item in a list
     * of documents, or returns null if highlighting is disabled.
     * 
     * @param docs
     *            query results
     * @param query
     *            the query
     * @param req
     *            the current request
     * @param defaultFields
     *            default list of fields to summarize
     * @return NamedList containing a NamedList for each document, which in
     *         turns contains sets (field, summary) pairs.
     */
    @SuppressWarnings("unchecked")
    public NamedList<Object> doHighlighting(DocList docs, Query query, SolrQueryRequest req,
        String[] defaultFields) throws IOException {
        SolrParams params = req.getParams();
        if (!isHighlightingEnabled(params))
            return null;

        SolrIndexSearcher searcher = req.getSearcher();
        IndexSchema schema = searcher.getSchema();
        NamedList fragments = new SimpleOrderedMap();
        String[] fieldNames = getHighlightFields(query, req, defaultFields);
        Document[] readDocs = new Document[docs.size()];
        {
            // pre-fetch documents using the Searcher's doc cache
            Set<String> fset = new HashSet<String>();
            for (String f : fieldNames) {
                fset.add(f);
            }
            // fetch unique key if one exists.
            SchemaField keyField = schema.getUniqueKeyField();
            if (null != keyField)
                fset.add(keyField.getName());
            searcher.readDocs(readDocs, docs, fset);
        }

        // Highlight each document
        DocIterator iterator = docs.iterator();
        for (int i = 0; i < docs.size(); i++) {
            int docId = iterator.nextDoc();
            Document doc = readDocs[i];
            NamedList docSummaries = new SimpleOrderedMap();
            for (String fieldName : fieldNames) {
                fieldName = fieldName.trim();

                // begin
                String[] docTexts = doc.getValues(fieldName);
                //Highlight only the parsed content, instead of all fields
                if (IndexField.DEFAULT_SEARCH_FIELD.equals(fieldName)) {
                	docTexts = doc.getValues(IndexField.PARSED_CONTENT_FIELD);
                }                	
                
//                IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
//                String collectionName = params.get(ConstellioSolrQueryParams.COLLECTION_NAME);
//            	RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
//                RecordCollection collection = collectionServices.get(collectionName);
//                IndexField defaultSearchField = collection.getDefaultSearchIndexField();
//
//                List<String> defaultSearchFieldDocTextsList = new ArrayList<String>();
//                for (CopyField copyField : defaultSearchField.getCopyFieldsDest()) {
//					IndexField sourceIndexField = copyField.getIndexFieldSource();
//					if (sourceIndexField != null) {
//						String sourceIndexFieldName = sourceIndexField.getName();
//		                String[] copyFieldValues = doc.getValues(sourceIndexFieldName);
//		                if (copyFieldValues != null) {
//		                	for (int k = 0; k < copyFieldValues.length; k++) {
//								String copyFieldValue = copyFieldValues[k];
//								if (!defaultSearchFieldDocTextsList.contains(copyFieldValue)) {
//									defaultSearchFieldDocTextsList.add(copyFieldValue);
//								}
//							}
//		                }
//					}
//				}
//                docTexts = defaultSearchFieldDocTextsList.toArray(new String[0]);
                
//                if ((docTexts == null || docTexts.length == 0)) {
//                    RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
//                    Long recordId = new Long(doc.getField(IndexField.RECORD_ID_FIELD).stringValue());
//                    Record record;
//                    try {
//                    	record = recordServices.get(recordId, collection);
//					} catch (Exception e) {
//						record = null;
//						e.printStackTrace();
//					}
//                    if (record != null) {
//                        List<Object> fieldValues = indexFieldServices.extractFieldValues(record, defaultSearchField);
//
//                        List<String> docTextsList = new ArrayList<String>();
//                        for (Object fieldValue : fieldValues) {
//                            String strFieldValue = fieldValue != null ? fieldValue.toString() : null;
//                            if (StringUtils.isNotBlank(strFieldValue)) {
//                                docTextsList.add(strFieldValue);
//                            }
//                        }
//
//                        if (!docTextsList.isEmpty()) {
//                            docTexts = docTextsList.toArray(new String[0]);
//                        }
//                    }
//                }
//                // end
                
                if (docTexts == null) continue;

                TokenStream tstream = null;
                int numFragments = getMaxSnippets(fieldName, params);
                boolean mergeContiguousFragments = isMergeContiguousFragments(fieldName, params);

                String[] summaries = null;
                List<TextFragment> frags = new ArrayList<TextFragment>();
                for (int j = 0; j < docTexts.length; j++) {
                    // create TokenStream
                    try {
                        // attempt term vectors
                        tstream = TokenSources.getTokenStreamWithOffsets(searcher.getIndexReader(), docId, fieldName);
                    } catch (IllegalArgumentException e) {
                        // fall back to anaylzer
                        tstream = new TokenOrderingFilter(schema.getAnalyzer().tokenStream(fieldName,
                            new StringReader(docTexts[j])), 10);
                    }

                    Highlighter highlighter;
                    if (Boolean.valueOf(req.getParams().get(HighlightParams.USE_PHRASE_HIGHLIGHTER))) {
                        // wrap CachingTokenFilter around TokenStream for reuse
                        tstream = new CachingTokenFilter(tstream);

                        // get highlighter
                        highlighter = getPhraseHighlighter(query, fieldName, req,
                            (CachingTokenFilter) tstream);

                        // after highlighter initialization, reset tstream since construction of highlighter
                        // already used it
                        tstream.reset();
                    } else {
                        // use "the old way"
                        highlighter = getHighlighter(query, fieldName, req);
                    }

                    int maxCharsToAnalyze = params.getFieldInt(fieldName, HighlightParams.MAX_CHARS,
                        Highlighter.DEFAULT_MAX_CHARS_TO_ANALYZE);
                    if (maxCharsToAnalyze < 0) {
                        highlighter.setMaxDocCharsToAnalyze(docTexts[j].length());
                    } else {
                        highlighter.setMaxDocCharsToAnalyze(maxCharsToAnalyze);
                    }

                    try {
                        TextFragment[] bestTextFragments = highlighter.getBestTextFragments(tstream,
                            docTexts[j], mergeContiguousFragments, numFragments);
                        for (int k = 0; k < bestTextFragments.length; k++) {
                            if ((bestTextFragments[k] != null) && (bestTextFragments[k].getScore() > 0)) {
                                frags.add(bestTextFragments[k]);
                            }
                        }
                    } catch (InvalidTokenOffsetsException e) {
                        throw new RuntimeException(e);
                    }
                }
                // sort such that the fragments with the highest score come first
                Collections.sort(frags, new Comparator<TextFragment>() {
                    public int compare(TextFragment arg0, TextFragment arg1) {
                        return Math.round(arg1.getScore() - arg0.getScore());
                    }
                });

                // convert fragments back into text
                // TODO: we can include score and position information in output as snippet attributes
                if (frags.size() > 0) {
                    ArrayList<String> fragTexts = new ArrayList<String>();
                    for (TextFragment fragment : frags) {
                        if ((fragment != null) && (fragment.getScore() > 0)) {
//                            fragTexts.add(fragment.toString());
                            fragTexts.add(StringEscapeUtils.escapeHtml(fragment.toString()));
                        }
                        if (fragTexts.size() >= numFragments)
                            break;
                    }
                    summaries = fragTexts.toArray(new String[0]);
                    if (summaries.length > 0)
                        docSummaries.add(fieldName, summaries);
                }
                // no summeries made, copy text from alternate field
                if (summaries == null || summaries.length == 0) {
                    String alternateField = req.getParams().getFieldParam(fieldName,
                        HighlightParams.ALTERNATE_FIELD);
                    if (alternateField != null && alternateField.length() > 0) {
                        String[] altTexts = doc.getValues(alternateField);
                        if (altTexts != null && altTexts.length > 0) {
                            int alternateFieldLen = req.getParams().getFieldInt(fieldName,
                                HighlightParams.ALTERNATE_FIELD_LENGTH, 0);
                            if (alternateFieldLen <= 0) {
                                docSummaries.add(fieldName, altTexts);
                            } else {
                                List<String> altList = new ArrayList<String>();
                                int len = 0;
                                for (String altText : altTexts) {
                                    altList.add(len + altText.length() > alternateFieldLen ? altText
                                        .substring(0, alternateFieldLen - len) : altText);
                                    len += altText.length();
                                    if (len >= alternateFieldLen)
                                        break;
                                }
                                docSummaries.add(fieldName, altList);
                            }
                        }
                    }
                }

            }
            String printId = schema.printableUniqueKey(doc);
            fragments.add(printId == null ? null : printId, docSummaries);
        }
        return fragments;
    }

}
