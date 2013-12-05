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
package com.doculibre.constellio.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.solr.client.solrj.SolrServer;

import com.doculibre.analyzer.FrenchAccentPlurielAnalyzer;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.SolrServices;

public class AnalyzerUtils {

    public static String analyze(String input, RecordCollection collection) {
    	SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
    	SolrServer server = solrServices.getSolrServer(collection);
        return analyze(input, IndexField.DEFAULT_SEARCH_FIELD, server, false);
    }

    public static String analyze(String input, String indexFieldName, SolrServer server) {
        return analyze(input, indexFieldName, server, false);
    }

	public static String analyze(String input, String indexFieldName, SolrServer server, boolean queryAnalyzer) {
    	return analyzePhrase(input, true);
//        SolrQuery query = new SolrQuery();
//        query.setQueryType("/analysis/field");
//        query.setParam(AnalysisParams.FIELD_NAME, indexFieldName);
//        query.setParam(AnalysisParams.FIELD_VALUE, input);
//        if (queryAnalyzer) {
//            query.setParam(AnalysisParams.QUERY, input);
//        }
//
//        QueryResponse queryResponse;
//        try {
//            queryResponse = server.query(query);
//        } catch (SolrServerException e) {
//            throw new RuntimeException(e);
//        }
//
//        NamedList<Object> result = (NamedList<Object>) queryResponse.getResponse().get("analysis");
//        NamedList<NamedList> fieldNames = (NamedList<NamedList>) result.get("field_names");
//        NamedList<NamedList> fieldValues = fieldNames.get(indexFieldName);
//
//        StringBuffer sb = new StringBuffer();
//
//        String partName = queryAnalyzer ? "query" : "index";
//        NamedList<List<NamedList>> part = (NamedList<List<NamedList>>) fieldValues.get(partName);
//        for (Iterator<Entry<String, List<NamedList>>> it = part.iterator(); it.hasNext();) {
//            Entry<String, List<NamedList>> entry = it.next();
//            List<NamedList> namedListValues = entry.getValue();
//            for (int i = 0; i < namedListValues.size(); i++) {
//                NamedList namedListValue = namedListValues.get(i);
//                String text = (String) namedListValue.get("text");
//                sb.append(text);
//                if (i < namedListValues.size() - 1) {
//                    sb.append(" ");
//                }
//            }
//        }
//        return sb.toString();
    }

    public static String analyzePhrase(String phrase) {
        return analyzePhrase(phrase, true);
    }

    // Fait par Rida, moddif par N
    public static String analyzePhrase(String phrase, boolean useStopWords) {
        if (StringUtils.isNotBlank(phrase)) {
            String analysedPhrase;
            Analyzer analyzer = getDefaultAnalyzer(useStopWords);

            StringBuilder norm = new StringBuilder();
	  	    TokenStream tokens;
			try {
				tokens = analyzer.reusableTokenStream( "", new StringReader( phrase ) );
		  	    tokens.reset();
		  	      
		  	    CharTermAttribute termAtt = tokens.addAttribute(CharTermAttribute.class);
		  	    while( tokens.incrementToken() ) {
		  	        norm.append( termAtt.buffer(), 0, termAtt.length() );
		  	    }
		  	    
	            analysedPhrase = norm.toString().trim();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
            return analysedPhrase;
        } else {
            return phrase;
        }
    }
    
    public static Analyzer getDefaultAnalyzer() {
    	return getDefaultAnalyzer(true);
    }
    
    public static Analyzer getDefaultAnalyzer(boolean useStopWords) {
        Analyzer analyzer;
        if (useStopWords) {
            analyzer = new FrenchAccentPlurielAnalyzer(Version.LUCENE_34);
        } else {
            Set<String> emptyStpWords = new HashSet<String>();
            analyzer = new FrenchAccentPlurielAnalyzer(Version.LUCENE_34, emptyStpWords);
        }
//        if (useStopWords) {
//            analyzer = new FrenchAccentAnalyzer(Version.LUCENE_34);
//        } else {
//            Set<String> emptyStpWords = new HashSet<String>();
//            analyzer = new FrenchAccentAnalyzer(Version.LUCENE_34, emptyStpWords);
//        }
        return analyzer;
    }

    public static void main(String[] args) {
//        String phrase = "  Réda     Bendjëlloun   ";
//        String phraseAnalysee = "reda bendjelloun";
//
//        Assert.assertEquals(phraseAnalysee, analyzePhrase(phrase));
    	System.out.println(analyzePhrase("carra"));
    	System.out.println(analyzePhrase("CARRA"));
    }

}
