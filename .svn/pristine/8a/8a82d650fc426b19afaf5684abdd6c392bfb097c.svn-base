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
package com.doculibre.constellio.entities.search.advanced.indexFieldRules;

import org.apache.commons.lang.StringUtils;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.AbstractSearchRule;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.entities.search.advanced.SearchRulesGroup;
import com.doculibre.constellio.entities.search.advanced.enums.TextSearchMethod;
import com.doculibre.constellio.search.SynonymUtils;
import com.doculibre.constellio.utils.SimpleParams;

@SuppressWarnings("serial")
public class TextSearchRule extends AbstractSearchRule implements IndexFieldSearchRule {

	public static final String TYPE = "txt";

	public static final String DEFAULT_FIELD = "DF"; 
	
	public static final String PARAM_VALUE = "v";
	public static final String PARAM_SEARCH_METHOD = "m";
	
	private String indexFieldName;

	private String textValue;
	
	private String collectionName;
	
	private TextSearchMethod searchMethod = TextSearchMethod.DEFAULT;

	public TextSearchRule() {
		
	}
	
	public TextSearchRule(SimpleParams params, SearchRulesGroup parent, String lookupPrefix) {
		super(parent);
		this.indexFieldName = params.getString(lookupPrefix + DELIM + PARAM_INDEX_FIELD);
		this.textValue = params.getString(lookupPrefix + DELIM + PARAM_VALUE);
		this.collectionName = params.getString(SimpleSearch.COLLECTION_NAME);
		String mParam = params.getString(lookupPrefix + DELIM + PARAM_SEARCH_METHOD);
		if (mParam != null) {
			this.searchMethod = TextSearchMethod.valueOf(mParam);
		}
	}

	@Override
	public SearchRule cloneRule() {
		TextSearchRule rule = new TextSearchRule();
		rule.textValue = textValue;
		rule.indexFieldName = indexFieldName;
		rule.searchMethod = searchMethod;
		return rule;
	}

	public String getIndexFieldName() {
		return indexFieldName;
	}

	public String getTextValue() {
		return textValue;
	}

	@Override
	public boolean isValid() {
		return indexFieldName != null && textValue != null && searchMethod != null && !textValue.trim().isEmpty();
	}

	public void setIndexFieldName(String indexFieldName) {
		this.indexFieldName = indexFieldName;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	public TextSearchMethod getSearchMethod() {
		return searchMethod;
	}

	public void setSearchMethod(TextSearchMethod searchMethod) {
		this.searchMethod = searchMethod;
	}
	
	@Override
	public SimpleParams toSimpleParams(boolean onlyType) {
		SimpleParams params = new SimpleParams();
		String prefix = getPrefix();
		params.add(prefix + DELIM + PARAM_TYPE, TYPE);
		params.add(prefix + DELIM + PARAM_INDEX_FIELD, indexFieldName);
		if (!onlyType) {
			params.add(prefix + DELIM + PARAM_VALUE, textValue);
			if (searchMethod != null) {
				params.add(prefix + DELIM + PARAM_SEARCH_METHOD, searchMethod.name());
			}
		}
		return params;
	}
	
	@Override
	public String toLuceneQuery() {
		String query;
		
		if (searchMethod == TextSearchMethod.EXACT) {
			
			query = textValue;
			if (collectionName != null) {
				try {
					query = SynonymUtils.addSynonyms(textValue, collectionName, true);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			query = "";
			if (searchMethod == TextSearchMethod.EXCEPT) {
				query += "* : * NOT (";
			}
			String operator ;
			if (searchMethod == TextSearchMethod.EXCEPT) {
				operator = "OR";
			} else {
				operator = searchMethod.name();
			}
			
			
            String[] terms = textValue.split(" ");
            for (int i = 0; i < terms.length; i++) {
                String term = terms[i];
                String termAndSynonyms;
                if (StringUtils.isNotEmpty(term) && StringUtils.isNotEmpty(collectionName)) {
	                try {
	                	termAndSynonyms = SynonymUtils.addSynonyms(term, collectionName, true);
	    			} catch(Exception e) {
	    				termAndSynonyms = term;
	    				e.printStackTrace();
	    			}
                }else {
                	termAndSynonyms = term;
                }
                if (term.equals(termAndSynonyms)) {
                	query += term;
                } else {
                	query += "(" + termAndSynonyms + ")";
                }
                if (i < terms.length - 1) {
                	query += " " + operator + " ";
                }
            }
			if (searchMethod == TextSearchMethod.EXCEPT) {
				query += ")";
			}
		}
		if (query.equals(textValue)) {
			query = "\"" + query + "\"";
		} else {
			query = "(" + query + ")";
		}
		System.out.println(query);
		if (DEFAULT_FIELD.equals(indexFieldName)) {
			return query;
		} else {
			return indexFieldName + ":" + query;
		}
		
	}

	@Override
	public String toString() {
		return toLuceneQuery();
	}
 
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((indexFieldName == null) ? 0 : indexFieldName.hashCode());
		result = prime * result
				+ ((searchMethod == null) ? 0 : searchMethod.hashCode());
		result = prime * result
				+ ((textValue == null) ? 0 : textValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextSearchRule other = (TextSearchRule) obj;
		if (indexFieldName == null) {
			if (other.indexFieldName != null)
				return false;
		} else if (!indexFieldName.equals(other.indexFieldName))
			return false;
		if (searchMethod != other.searchMethod)
			return false;
		if (textValue == null) {
			if (other.textValue != null)
				return false;
		} else if (!textValue.equals(other.textValue))
			return false;
		return true;
	}

}
