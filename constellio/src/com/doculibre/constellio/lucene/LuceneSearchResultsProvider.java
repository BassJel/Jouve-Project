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
package com.doculibre.constellio.lucene;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

@SuppressWarnings("serial")
public class LuceneSearchResultsProvider implements Serializable {
	
	private File indexDir;
	
	private String luceneTextQuery;
	
	private Query luceneQuery;
	
//	private String sortField;
//	
//	private Boolean sortAscending;

	private String[] searchFields;
	
	private transient Searcher indexSearcher;

	private TopDocs topDocs;
	
	private AnalyzerProvider analyzerProvider;
	
	public LuceneSearchResultsProvider(
			File indexDir, 
			String luceneTextQuery, 
			String[] searchFields, 
			String sortField,
			Boolean sortAscending,
			AnalyzerProvider analyzerProvider) {
		this.indexDir = indexDir;
		this.luceneTextQuery = luceneTextQuery;
		this.searchFields = searchFields;
//		this.sortField = sortField;
//		this.sortAscending = sortAscending;
		this.analyzerProvider = analyzerProvider;
	}
	
	private synchronized void initIfNecessary() {
		if (topDocs == null) {
	        try {
	            Directory directory = FSDirectory.open(indexDir);
	            Analyzer analyzer = analyzerProvider.getAnalyzer(Locale.FRENCH);
	    		MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_34, searchFields, analyzer);
			    parser.setAllowLeadingWildcard(true); 
			    try {
			        luceneQuery = parser.parse(luceneTextQuery);
                } catch (ParseException e) {
                    try {
                        luceneQuery = parser.parse(QueryParser.escape(luceneTextQuery));
                    } catch (ParseException e1) {
                        throw new RuntimeException(e1);
                    }
                }
                indexSearcher = new IndexSearcher(directory, true);
//              Sort sort;
//              if (sortField != null) {
//                  sort = new Sort(new SortField(sortField, Locale.CANADA_FRENCH, Boolean.FALSE.equals(sortAscending)));
//              } else {
//                  sort = null;
//              }
                  topDocs = indexSearcher.search(luceneQuery, indexSearcher.maxDoc());
	            directory.close();
	        } catch (CorruptIndexException e) {
	            throw new RuntimeException(e);
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
		}
	}
	
	public void close() {
		if (indexSearcher != null) {
			try {
				indexSearcher.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			indexSearcher = null;
		}
	}
	
	public synchronized TopDocs topDocs() {
		initIfNecessary();
		return topDocs;
	}
	
	public synchronized Document get(int docId) {
		initIfNecessary();
		try {
			return indexSearcher.doc(docId);
		} catch (CorruptIndexException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getTextQuery() {
		return luceneTextQuery;
	}

	public Query getLuceneQuery() {
		return luceneQuery;
	}
	
	public boolean isSearchBegun() {
		return StringUtils.isNotEmpty(luceneTextQuery);
	}
	
	public synchronized Searcher getIndexSearcher() {
		initIfNecessary();
		return indexSearcher;
	}
	
	public synchronized List<String> getMatchingFieldNames(int docId) {
		Set<String> matchingFieldNames = new HashSet<String>();
		try {
			Explanation explanation = getIndexSearcher().explain(luceneQuery, docId);
			if (explanation instanceof ComplexExplanation) {
				Explanation[] details = explanation.getDetails();
				if (details != null) {
					for (int i = 0; i < details.length; i++) {
						Explanation detail = details[i];
						matchingFieldNames.addAll(getMatchingFieldNames(docId, detail));
					}
				}
			} else {
				matchingFieldNames.addAll(getMatchingFieldNames(docId, explanation));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new ArrayList<String>(matchingFieldNames);
	}
	
	private List<String> getMatchingFieldNames(int docId, Explanation explanation) {
		Set<String> matchingFieldNames = new HashSet<String>();
		if (explanation instanceof ComplexExplanation) {
			Explanation[] details = explanation.getDetails();
			if (details != null) {
				for (int i = 0; i < details.length; i++) {
					Explanation detail = details[i];
					// Recursive call
					matchingFieldNames.addAll(getMatchingFieldNames(docId, detail));
				}
			}
		} 
		
		String description = explanation.getDescription();
		int indexOfFirstChar = 0;
		int indexOfColon = description.indexOf(":");
		while (indexOfColon != -1) {
			String delim = "(";
			int indexOfDelim = description.indexOf(delim, indexOfFirstChar);
			if (indexOfDelim != -1) {
				int indexOfFieldName = indexOfDelim + delim.length();
				String matchingFieldName = description.substring(indexOfFieldName, indexOfColon);
				if (matchingFieldName.startsWith("termFreq(")) {
					matchingFieldName = matchingFieldName.substring("termFreq(".length());
				}
				matchingFieldNames.add(matchingFieldName);
				indexOfFirstChar = indexOfColon + 1;
				indexOfColon = description.indexOf(":", indexOfFirstChar);
			} else {
				break;
			}
		}
		return new ArrayList<String>(matchingFieldNames);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		close();
		out.defaultWriteObject();
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

}
