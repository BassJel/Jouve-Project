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
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.doculibre.analyzer.FrenchAnalyzer;
import com.doculibre.constellio.utils.ClasspathUtils;

@SuppressWarnings("serial")
public abstract class BaseLuceneIndexHelper<T> implements IndexHelper<T> {

	public static final String NULL_VALUE = "_null_";
    private static final int TAILLE_CHAINE_NON_FRAGMENTEE = 250;
    private static final int TAILLE_FRAGMENT = 80;
    private static final int NB_BEST_FRAGMENT = 5;
    private static final String FRAGMENT_SEP = "...";
    public static final int NUMBER_LENGTH = 9;

	private File indexDir;

	private String[] searchFields;
	
	private AnalyzerProvider analyzerProvider = new AnalyzerProvider();

	public BaseLuceneIndexHelper(String indexDirName) {
		File indexesDir = ClasspathUtils.getLuceneIndexesDir();
		this.indexDir = new File(indexesDir, indexDirName);

		createIndexIfNecessary();
		Field[] indexFields = createIndexFields();
		searchFields = createSearchFields(indexFields);
	}

	public AnalyzerProvider getAnalyzerProvider() {
		return analyzerProvider;
	}

	@Override
	public synchronized boolean isEmpty() {
		boolean empty;
        try {
            Directory directory = FSDirectory.open(indexDir);
            IndexReader indexReader = IndexReader.open(directory, false);
            empty = indexReader.numDocs() <= 1;
            indexReader.close();
            directory.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return empty;
	}
	
	@Override
	public synchronized boolean isIndexed(T object) {
		return getDocNum(object) != -1;
	}
	
	@Override
	public synchronized void add(T object) {
        try {
	    	Directory directory = FSDirectory.open(indexDir);
	    	Analyzer analyzer = analyzerProvider.getAnalyzer(Locale.FRENCH);
			IndexWriter indexWriter = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
			add(object, indexWriter);
			indexWriter.close();
			directory.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	protected synchronized void add(T object, IndexWriter indexWriter) {
		int docNum = getDocNum(object);
		if (docNum == -1) {
        	Document doc = new Document();
        	Field[] indexFields = createIndexFields();
        	for (Field indexField : indexFields) {
				populateIndexField(object, indexField, doc);
				if (StringUtils.isEmpty(indexField.stringValue())) {
					indexField.setValue(NULL_VALUE);
				}
				doc.add(indexField);
			}
        	try {
				indexWriter.addDocument(doc);
			} catch (CorruptIndexException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("Document déjà existant! (docNum:" + docNum + ")");
		}
	}
	
	@Override
	public synchronized void update(T object) {
		delete(object);
		add(object);
	}

	@Override
	public synchronized void addOrUpdate(T object) {
		update(object);
	}
	
	@Override
	public synchronized void delete(T object) {
		int docNum = getDocNum(object);
		if (docNum != -1) {
	        try {
	    		String uniqueIndexFieldName = getUniqueIndexFieldName();
	    		String uniqueIndexFieldValue = getUniqueIndexFieldValue(object);
	            Directory directory = FSDirectory.open(indexDir);
	            if (IndexReader.indexExists(directory)) {
	                IndexReader indexReader = IndexReader.open(directory, false);
	                Term term = new Term(uniqueIndexFieldName, uniqueIndexFieldValue);
	                indexReader.deleteDocuments(term);
	                indexReader.close();
	            }
                directory.close();
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
		}
	}
	
	@Override
	public synchronized void deleteAll() {
        try {
            Directory directory = FSDirectory.open(indexDir);
            Analyzer analyzer = analyzerProvider.getAnalyzer(Locale.FRENCH);
            IndexWriter indexWriter = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
            indexWriter.deleteAll();
            indexWriter.addDocument(new Document());
            indexWriter.optimize();
            indexWriter.close();
            directory.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	@Override
	public List<T> search(String freeTextQuery) {
		return search(freeTextQuery, true);
	}

	protected List<T> search(String freeTextQuery, boolean manageWildcards) {
		return search(freeTextQuery, manageWildcards, null, null);
	}
	
	@Override
	public List<T> search(String freeTextQuery, String sortField, Boolean sortAscending) {
		return search(freeTextQuery, true, sortField, sortAscending);
	}
	
	protected List<T> search(String freeTextQuery, boolean manageWildcards, String sortField, Boolean sortAscending) {
		String luceneTextQuery = freeTextQuery;
		LuceneSearchResultsProvider searchResultsProvider = 
			new LuceneSearchResultsProvider(
					indexDir, 
					luceneTextQuery, 
					searchFields, 
					sortField, 
					sortAscending, 
					analyzerProvider);
		return new ListSearchResults(searchResultsProvider);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void release(List<T> searchResults) {
		if (searchResults instanceof BaseLuceneIndexHelper.ListSearchResults) {
			ListSearchResults listSearchResults = (ListSearchResults) searchResults;
			listSearchResults.searchResultsProvider.close();
		}
	}
	
	protected synchronized int getDocNum(T object) {
		int docNum;
		String uniqueIndexFieldName = getUniqueIndexFieldName();
		String uniqueIndexFieldValue = getUniqueIndexFieldValue(object);
		if (uniqueIndexFieldValue != null) {
			String query = uniqueIndexFieldName + ":" + uniqueIndexFieldValue;
	        try {
	            Directory directory = FSDirectory.open(indexDir);
	            Analyzer analyzer = analyzerProvider.getAnalyzer(Locale.FRENCH);
	            QueryParser multiFielsQP = new QueryParser(Version.LUCENE_34, uniqueIndexFieldName, analyzer);
	            Query luceneQuery = multiFielsQP.parse(query);
	            Searcher indexSearcher = new IndexSearcher(directory, true);
	            TopDocs topDocs = indexSearcher.search(luceneQuery, indexSearcher.maxDoc());
	            if (topDocs.totalHits > 0) {
	            	docNum = topDocs.scoreDocs[0].doc;
	            } else {
	            	docNum = -1;
	            }
	            indexSearcher.close();
	            directory.close();
	        } catch (ParseException e) {
	            throw new RuntimeException(e);
	        } catch (CorruptIndexException e) {
	            throw new RuntimeException(e);
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
		} else {
			docNum = -1;
		}
        return docNum;
	}
	
	public File getIndexDir() {
		return indexDir;
	}

    private void createIndexIfNecessary() {
        try {
            Directory directory = FSDirectory.open(indexDir);
            if (!IndexReader.indexExists(directory)) {
    	    	Analyzer analyzer = analyzerProvider.getAnalyzer(Locale.FRENCH);
    			IndexWriter indexWriter = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
    			indexWriter.addDocument(new Document());
    			indexWriter.close();
            }
            directory.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected String[] createSearchFields(Field[] indexFields) {
    	String[] searchFields = new String[indexFields.length];
    	for (int i = 0; i < searchFields.length; i++) {
			Field indexField = indexFields[i];
			searchFields[i] = indexField.name();
		}
    	return searchFields;
    }
    
    @SuppressWarnings("unchecked")
	public String highlight(String strToHighlight, String fieldName, List<T> resultatsRecherche) {
		if (resultatsRecherche instanceof BaseLuceneIndexHelper.ListSearchResults) {
			ListSearchResults listResultatsRecherche = (ListSearchResults) resultatsRecherche;
			Query luceneQuery = listResultatsRecherche.searchResultsProvider.getLuceneQuery();
			return highlight(strToHighlight, fieldName, luceneQuery);
		} else {
            throw new RuntimeException("La liste passée en argument doit être la même qui a été retournée par la méthode search()");
		}	
	}	    
    
	public String highlight(String strToHighlight, String fieldName, Query luceneQuery) {
    	String highlightedText;
		Analyzer analyzer = analyzerProvider.getAnalyzer(Locale.FRENCH);
        try {
	        Directory directory = FSDirectory.open(indexDir);
            IndexReader indexReader = IndexReader.open(directory, true);
            Query rewrittenLuceneQuery = luceneQuery.rewrite(indexReader);
	        QueryScorer luceneScorer = new QueryScorer(rewrittenLuceneQuery);
	        SimpleHTMLFormatter luceneFormatter = new SimpleHTMLFormatter(
	                "<span class=\"hit\">", "</span>");
			Highlighter luceneHighlighter = new Highlighter(luceneFormatter, luceneScorer);
			
			
	        Fragmenter luceneFragmenter;
	        // Si la chaine à highlighter est sup à 250 carac
	        if (strToHighlight.length() > TAILLE_CHAINE_NON_FRAGMENTEE) {
	            // Création de best fragments de 100 carac chaque
	            luceneFragmenter = new SimpleFragmenter(TAILLE_FRAGMENT);
	        } else {
	            // Toute la chaine est highlighté
	            luceneFragmenter = new SimpleFragmenter(Integer.MAX_VALUE);
	        }
	        luceneHighlighter.setTextFragmenter(luceneFragmenter);

	        TokenStream luceneTokenStream = analyzer.tokenStream(fieldName, new StringReader(strToHighlight));
	        String fragment = null;
	        if (strToHighlight.length() > TAILLE_CHAINE_NON_FRAGMENTEE) {
				fragment = luceneHighlighter.getBestFragments(luceneTokenStream, strToHighlight, NB_BEST_FRAGMENT, FRAGMENT_SEP);
	        } else {
	            fragment = luceneHighlighter.getBestFragment(luceneTokenStream, strToHighlight);
	        }
	        
	        if(StringUtils.isBlank(fragment) && fieldName.equalsIgnoreCase("titre")){
	        	fragment = strToHighlight;
	        }
	        indexReader.close();
	        directory.close();
			
			highlightedText = fragment;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidTokenOffsetsException e) {
			throw new RuntimeException(e);
		}
		return highlightedText;
    }

	protected static Field[] createDefaultIndexFields(String[] indexFieldNames) {
		Field[] fields = new Field[indexFieldNames.length];
		for (int i = 0; i < indexFieldNames.length; i++) {
			String indexFieldName = indexFieldNames[i];
			Field field = createDefaultIndexField(indexFieldName);
			fields[i] = field;
		}
		return fields;
	}

	protected static Field createDefaultIndexField(String indexFieldName) {
		return new Field(indexFieldName, "", Field.Store.YES, Field.Index.ANALYZED);
	}
	
	public synchronized final void rebuild() {
		deleteAll();
        try {
	    	Directory directory = FSDirectory.open(indexDir);
	    	Analyzer analyzer = analyzerProvider.getAnalyzer(Locale.FRENCH);
			IndexWriter indexWriter = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
			List<T> objects = getAll();
			for (T object : objects) {
				add(object, indexWriter);
			}
            indexWriter.optimize();
			indexWriter.close();
			directory.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	public static String adjustIfNumber(Object value) {
		String result;
		if (value != null) {
			String strValue = value.toString();
			try {
				Integer.parseInt(strValue);
				StringBuffer sb = new StringBuffer(strValue);
				for (int i = 0; i < NUMBER_LENGTH - strValue.length(); i++) {
					sb.insert(0, "0");
				}
				result = sb.toString();
			} catch (Exception e) {
				result = strValue;
			}
		} else {
			result = null;
		}
		return result;
	}

	public static String adjustIfDate(String texteLibre) {
		String result;
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(texteLibre.trim());
			result = DateTools.dateToString(date, Resolution.DAY);
		} catch (Exception e) {
			result = texteLibre;
		}
		return result;
	}
	
	protected abstract List<T> getAll();
    
    protected abstract Field[] createIndexFields();
    
    protected abstract void populateIndexField(T object, Field indexField, Document doc);
    
    protected abstract String getUniqueIndexFieldName();
    
    protected abstract String getUniqueIndexFieldValue(T object);
	
	protected abstract T toObject(int docId, Document doc); 
	
    public class ListSearchResults extends AbstractList<T> implements Serializable {
    	
    	private LuceneSearchResultsProvider searchResultsProvider;
    	
    	private ListSearchResults(LuceneSearchResultsProvider searchResultsProvider) {
    		this.searchResultsProvider = searchResultsProvider;
    	}

		public LuceneSearchResultsProvider getSearchResultsProvider() {
			return searchResultsProvider;
		}

		@Override
    	public int size() {
			int size;
			if (searchResultsProvider.isSearchBegun()) {
				size = searchResultsProvider.topDocs().totalHits; 
			} else {
				size = 0;
			}
    		return size;
    	}
    	
    	@Override
    	public T get(int index) {
    		TopDocs topDocs = searchResultsProvider.topDocs();
    		int docId = topDocs.scoreDocs[index].doc;
    		Document doc = searchResultsProvider.get(docId);
    		return toObject(docId, doc);
    	}
    	
    	public List<String> getMatchingFieldNames(int index) {
    		TopDocs topDocs = searchResultsProvider.topDocs();
    		int docId = topDocs.scoreDocs[index].doc;
    		return searchResultsProvider.getMatchingFieldNames(docId);
    	}
    	
    	private void writeObject(ObjectOutputStream out) throws IOException {
    		searchResultsProvider.close();
    		out.defaultWriteObject();
    	}

    	@Override
    	protected void finalize() throws Throwable {
    		searchResultsProvider.close();
    		super.finalize();
    	}
    	
    }

    protected String getAndQuery(String query) {
        String[] queryWords = query.split(" ");
        if (queryWords.length > 1) {
            StringBuffer sb = new StringBuffer();
            for (String queryWord : queryWords) {
                if (queryWord.length() > 1) {
                    sb.append("+" + queryWord + " ");
                }
            }
            query = sb.toString();
        }
        return query;
    }
    
    public String analyze(String str) {
    	try {
            return analyze(str, analyzerProvider.getAnalyzer(Locale.FRENCH));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
	public static String analyze(String str, Analyzer analyzer) throws IOException {
		if( analyzer == null ) {
	        return str;
	      }
	      StringBuilder norm = new StringBuilder();
	      TokenStream tokens = analyzer.reusableTokenStream( "", new StringReader( str ) );
	      tokens.reset();
	      
	      CharTermAttribute termAtt = tokens.addAttribute(CharTermAttribute.class);
	      while( tokens.incrementToken() ) {
	        norm.append( termAtt.buffer(), 0, termAtt.length() );
	      }
	      return norm.toString();
    }
    
    public static String escape(String text) {
    	String result = text;
    	// "\\" must be first
		String[] escapable = new String[] { "\\", "+", "-", "&&", "||", "!", "(",
				")", "{", "}", "[", "]", "^", "\"", "~", /*"*",*/ "?", ":" };
		for (int i = 0; i < escapable.length; i++) {
			result = result.replace(escapable[i], "\\" + escapable[i]);
		}
		return result;
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println(analyze("mét", new FrenchAnalyzer(Version.LUCENE_34)));
	}
	
}
