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

import com.doculibre.constellio.entities.search.advanced.AbstractSearchRule;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.entities.search.advanced.SearchRulesGroup;
import com.doculibre.constellio.utils.SimpleParams;

@SuppressWarnings("serial")
public class BooleanSearchRule extends AbstractSearchRule implements SearchRule, IndexFieldSearchRule {

	public static final String TYPE = "bool";
	
	public static final String PARAM_VALUE = "v";
	
	private String indexFieldName;
	
	private Boolean value;
	
	public BooleanSearchRule() {
		
	}
	
	public BooleanSearchRule(SimpleParams params, SearchRulesGroup parent, String lookupPrefix) {
		super(parent);
		this.indexFieldName = params.getString(lookupPrefix + DELIM + PARAM_INDEX_FIELD);
		String v = params.getString(lookupPrefix + DELIM + PARAM_VALUE);
		if (v != null) {
			value = "on".equals(v) || Boolean.valueOf(v);
		}
	}
	
	@Override
	public SearchRule cloneRule() {
		BooleanSearchRule rule = new BooleanSearchRule();
		rule.indexFieldName = indexFieldName;
		rule.value = value;
		return rule;
	}

	@Override
	public boolean isValid() {
		return indexFieldName != null;
	}

	@Override
	public SimpleParams toSimpleParams(boolean onlyType) {
		SimpleParams params = new SimpleParams();
		String prefix = getPrefix();
		params.add(prefix + DELIM + PARAM_TYPE, TYPE);
		params.add(prefix + DELIM + PARAM_INDEX_FIELD, indexFieldName);
		if (!onlyType) {
			if (value != null) {
				params.add(prefix + DELIM + PARAM_VALUE, String.valueOf(value));
			}
		}
		return params;
	}

	@Override
	public String toLuceneQuery() {
		String strValue = value != null && value ? "true" : "false";
		return indexFieldName + ":" + strValue + "";
	}

	@Override
	public String toString() {
		return toLuceneQuery();
	}

	public String getIndexFieldName() {
		return indexFieldName;
	}

	public void setIndexFieldName(String indexFieldName) {
		this.indexFieldName = indexFieldName;
	}

	public Boolean getValue() {
		return value;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((indexFieldName == null) ? 0 : indexFieldName.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		BooleanSearchRule other = (BooleanSearchRule) obj;
		if (indexFieldName == null) {
			if (other.indexFieldName != null)
				return false;
		} else if (!indexFieldName.equals(other.indexFieldName))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
