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

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.SolrParams;
import static org.apache.solr.common.params.DisMaxParams.*;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.FunctionQParserPlugin;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;

import static org.apache.solr.util.SolrPluginUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A generic enxtensible query parser based on disjunction-max capability.
 * (Once named DisMaxQParser)
 */
public abstract class UserQParser extends QParser {

  /**
   * A field we can't ever find in any schema, so we can safely tell DisjunctionMaxQueryParser to use it as our
   * defaultField, and map aliases from it to any field in our schema.
   */
  private static String IMPOSSIBLE_FIELD_NAME = "\uFFFC\uFFFC\uFFFC";

  //TODO
  private static final Pattern WILDCARD_STRIP_CHARS = Pattern.compile("[^a-zA-Z0-9*?]", Pattern.CASE_INSENSITIVE);//for prefix, wildcard, fuzzy

  /**
   * Defaults to true but may be overridden.  If false then the user query is treated as a Solr/Lucene query.
   * Some features of this QParser aren't applied because they become irrelevant/problematic like
   * min-should-match and phrase boosting.
   * @return
   */
  protected boolean isSimpleSyntax() {
    return true;
  }

  // TODO move tese two to QParser where it belongs, then make getParam(x) use it.
  private SolrParams aggregatedParams;
  public SolrParams getParams() {
    return aggregatedParams;
  }
  private SolrParams getNonLocalParams() {
    return super.params;
  }

  public UserQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
    aggregatedParams = localParams == null ? getNonLocalParams() : new DefaultSolrParams(localParams, getNonLocalParams());
  }

  Map<String, Float> queryFields;
  Query parsedUserQuery;


  private String[] boostParams;
  private List<Query> boostQueries;
  private Query altUserQuery;

  public Query parse() throws ParseException {

    queryFields = parseFieldBoosts(getParams().getParams(QF)); //set field now to ensure it gets done

    /* the main query we will execute.  we disable the coord because
     * this query is an artificial construct
     */
    BooleanQuery query = new BooleanQuery(true);

    String userQuery = getString();
    if (userQuery == null || userQuery.trim().length() < 1) {
      // If no query is specified, we may have an alternate
      String altQ = getParams().get(ALTQ);
      if (altQ != null) {
        //TODO use disjunction max or just the default as we're doing now?
        altUserQuery = subQuery(altQ, null).parse();
        query.add(altUserQuery, BooleanClause.Occur.MUST);
      } else {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "missing query string");
      }
    } else { //we have a user query
      boolean simpleSyntax = isSimpleSyntax();

      parsedUserQuery = doParse(userQuery, simpleSyntax);

      //--finally put this on our query
      query.add(parsedUserQuery, BooleanClause.Occur.MUST);

      //--apply phrase boost feature
      if (simpleSyntax) //only seems to make sense for simple syntax
        applyPhraseBoost(query);
    }

    //TODO should be a feature available for any QParser, like how FQ is.
    applyBoostingQuery(query);

    //TODO (same commment as above)
    applyBoostingFunctions(query);

    return query;
  }

  /**
   * Possibly rewrites then parses the query.
   * @param userQuery cached result from getString() earlier.  Should be non-null.
   * @param simpleSyntax cached result from call earlier
   * @return non-null Query parsed from the user's query
   * @throws ParseException
   */
  protected Query doParse(String userQuery, boolean simpleSyntax) throws ParseException {
    if (simpleSyntax)
      userQuery = rewriteSimpleSyntaxQuery(userQuery); //extensibility point

    //--construct a parser that can create DisjunctionMaxQueries
    DisjunctionMaxQueryParser queryParser = buildDisMaxQueryParser(queryFields);
    queryParser.setPhraseSlop(getParams().getInt(QS, 0));
    
    //Nouha :
    queryParser.setAllowLeadingWildcard(true);
    //Au lieu de:
//    queryParser.setWildcardStripChars(WILDCARD_STRIP_CHARS);
    //TODO document this option
//    queryParser.setAllowLeadingWildcard(getParams().getBool("allowLeadingWildcard",false));
    //fin Nouha

    //--use AND/OR instead of min-should-match for NON-simple syntax
    final String minShouldMatch = getParams().get(MM, "100%").trim();

    boolean doMM = false;
    if (!simpleSyntax) {
      applyDefaultOperator(queryParser);
    } else {
      if (minShouldMatch.equals("100%"))
        queryParser.setDefaultOperator(QueryParser.Operator.AND);
      else if (minShouldMatch.equals("0%"))
        queryParser.setDefaultOperator(QueryParser.Operator.OR);
      else
        doMM = true; //therefore do min-should-match
    }

    Query parsedQuery = queryParser.parse(userQuery);

    //--apply min-should-match logic to already-parsed query
    if (parsedQuery instanceof BooleanQuery && doMM) {
      assert simpleSyntax;

      //TODO why flatten; what does it accomplish?
      BooleanQuery t = new BooleanQuery();
      //flattenBooleanQuery(t, (BooleanQuery) parsedUserQuery);
      t = (BooleanQuery)parsedQuery;

      setMinShouldMatch(t, minShouldMatch);
      parsedQuery = t;
    }

    //--parse it
    return parsedQuery;
  }

  //TODO I've seen code like this in some other QParser; it should be factored out
  private void applyDefaultOperator(QueryParser queryParser) {
    QueryParser.Operator defaultOp;
    final String opParam = getParam(QueryParsing.OP);
    //TODO use a simple enum to both reduce this code and to error if doesn't match
    if (opParam != null) {
      defaultOp = ("AND".equals(opParam) ? QueryParser.Operator.AND : QueryParser.Operator.OR);
    } else {
      // try to get default operator from schema
      String operator = getReq().getSchema().getQueryParserDefaultOperator();
      defaultOp = ("AND".equals(operator) ?
          QueryParser.Operator.AND : QueryParser.Operator.OR);
    }
    queryParser.setDefaultOperator(defaultOp);
  }

  /**
   * Only called if {@link #isSimpleSyntax()} just returned true.
   */
  protected abstract String rewriteSimpleSyntaxQuery(String userQuery);

  protected DisjunctionMaxQueryParser buildDisMaxQueryParser(Map<String, Float> phraseFields) {
    final float tiebreaker = getParams().getFloat(TIE, 0.0f);
    DisjunctionMaxQueryParser queryParser =
        new DisjunctionMaxQueryParser(req.getSchema(), IMPOSSIBLE_FIELD_NAME);
    queryParser.addAlias(IMPOSSIBLE_FIELD_NAME, tiebreaker, phraseFields);
    return queryParser;
  }

  /**
   * if the userQuery already has some quotes, strip them out. we've already done the phrases they asked for in the main
   * part of the query, this is to boost docs that may not have matched those phrases but do match looser phrases.
   */
  protected void applyPhraseBoost(BooleanQuery query) throws ParseException {
    //TODO optimization: if clause-count of parsedUserQuery is 1 then skip ??
    final Map<String, Float> phraseFields = parseFieldBoosts(getParams().getParams(PF));
    QueryParser queryParser = buildDisMaxQueryParser(phraseFields);
    queryParser.setPhraseSlop(getParams().getInt(PS, 0));
    //note: default operator AND/OR doesn't matter; we will only have one clause (a phrase)

    String userPhraseQuery = partialEscape(getString()).toString().replace("\"", "");
    Query phrase = queryParser.parse("\"" + userPhraseQuery + "\"");
    if (null != phrase) {
      query.add(phrase, BooleanClause.Occur.SHOULD);
    }
  }

  private void applyBoostingQuery(BooleanQuery query) throws ParseException {
    boostParams = getParams().getParams(BQ);
    //List<Query> boostQueries = U.parseQueryStrings(req, boostParams);
    boostQueries = null;
    if (boostParams != null && boostParams.length > 0) {
      boostQueries = new ArrayList<Query>();
      for (String qs : boostParams) {
        if (qs.trim().length() == 0) continue;
        Query q = subQuery(qs, null).parse();
        boostQueries.add(q);
      }
    }
    if (null != boostQueries) {
      if (1 == boostQueries.size() && 1 == boostParams.length) {
        /* legacy logic */
        Query f = boostQueries.get(0);
        if (1.0f == f.getBoost() && f instanceof BooleanQuery) {
          /* if the default boost was used, and we've got a BooleanQuery
           * extract the subqueries out and use them directly
           */
          for (Object c : ((BooleanQuery) f).clauses()) {
            query.add((BooleanClause) c);
          }
        } else {
          query.add(f, BooleanClause.Occur.SHOULD);
        }
      } else {
        for (Query f : boostQueries) {
          query.add(f, BooleanClause.Occur.SHOULD);
        }
      }
    }
  }

  private void applyBoostingFunctions(BooleanQuery query) throws ParseException {
    String[] boostFuncs = getParams().getParams(BF);
    if (null != boostFuncs && 0 != boostFuncs.length) {
      for (String boostFunc : boostFuncs) {
        if (null == boostFunc || "".equals(boostFunc)) continue;
        Map<String, Float> ff = parseFieldBoosts(boostFunc);
        for (String f : ff.keySet()) {
          Query fq = subQuery(f, FunctionQParserPlugin.NAME).parse();
          Float b = ff.get(f);
          if (null != b) {
            fq.setBoost(b);
          }
          query.add(fq, BooleanClause.Occur.SHOULD);
        }
      }
    }
  }

  @Override
  public String[] getDefaultHighlightFields() {
    return queryFields.keySet().toArray(new String[queryFields.keySet().size()]);
  }

  @Override
  public Query getHighlightQuery() throws ParseException {
    return parsedUserQuery;
  }

  public void addDebugInfo(NamedList<Object> debugInfo) {
    super.addDebugInfo(debugInfo);
    debugInfo.add("altquerystring", altUserQuery);
    if (null != boostQueries) {
      debugInfo.add("boost_queries", boostParams);
      debugInfo.add("parsed_boost_queries",
          QueryParsing.toString(boostQueries, req.getSchema()));
    }
    debugInfo.add("boostfuncs", req.getParams().getParams(BF));
  }
}
