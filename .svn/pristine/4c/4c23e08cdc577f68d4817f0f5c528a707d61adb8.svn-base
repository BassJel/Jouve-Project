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
package com.doculibre.constellio.entities.search.advanced;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.doculibre.constellio.entities.search.advanced.enums.BooleanEquation;
import com.doculibre.constellio.utils.SimpleParams;

@SuppressWarnings("serial")
public class SearchRulesGroup extends AbstractSearchRule implements SearchRule {

	public static final String TYPE = "gr";
	public static final String PARAM_EQUATION = "eq";
	
	private List<SearchRule> nestedRules = new ArrayList<SearchRule>();

	private BooleanEquation equation = BooleanEquation.DEFAULT;

	public SearchRulesGroup() {

	}

	/**
	 * Construct from HTTP Request
	 */
	public SearchRulesGroup(SimpleParams params, SearchRulesGroup parent, String lookupPrefix) {
		super(parent);
		
		String eq = params.getString(lookupPrefix + DELIM + PARAM_EQUATION);
		if (eq != null) {
			equation = BooleanEquation.valueOf(eq);
		}

		int i = 1;
		SearchRule nestedRule;
		boolean blankOnce = false;
		while ((nestedRule = SearchRulesFactory.constructSearchRule(params, this,
				lookupPrefix + "_" + i)) != null || !blankOnce) {
			if (nestedRule == null) {
				blankOnce = true;
			} else {
				nestedRules.add(nestedRule);
			}

			i++;
		}

	}

	@Override
	public SearchRule cloneRule() {
		SearchRulesGroup group = new SearchRulesGroup();
		group.equation = equation;
		for (SearchRule rule : nestedRules) {
			group.addNestedSearchRule(rule.cloneRule());
		}
		return group;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchRulesGroup other = (SearchRulesGroup) obj;
		if (equation != other.equation)
			return false;
		if (nestedRules == null) {
			if (other.nestedRules != null)
				return false;
		} else if (!nestedRules.equals(other.nestedRules))
			return false;
		return true;
	}

	public BooleanEquation getEquation() {
		return equation;
	}

	public List<SearchRule> getNestedRules() {
		return Collections.unmodifiableList(nestedRules);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((equation == null) ? 0 : equation.hashCode());
		result = prime * result
				+ ((nestedRules == null) ? 0 : nestedRules.hashCode());
		return result;
	}

	/**
	 * It is valid if it contains at least one valid element
	 */
	@Override
	public boolean isValid() {
		boolean oneNestedRuleValid = false;
		Iterator<SearchRule> itRules = nestedRules.iterator();
		while (!oneNestedRuleValid && itRules.hasNext()) {
			oneNestedRuleValid = itRules.next().isValid();
		}
		return equation != null && oneNestedRuleValid;
	}

	public void setEquation(BooleanEquation equation) {
		this.equation = equation;
	}

	@Override
	public SimpleParams toSimpleParams(boolean onlyType) {
		SimpleParams params = new SimpleParams();
		String prefix = getPrefix();
		params.add(prefix + DELIM + PARAM_TYPE, TYPE);
		if (!onlyType && equation != null) {
			params.add(prefix + DELIM + PARAM_EQUATION, equation.name());
		}
		int i = 1;
		for (SearchRule rule : nestedRules) {
			params.addAll(rule.toSimpleParams(onlyType));
			i++;
		}
		return params;
	}

	@Override
	public String toLuceneQuery() {
		String query = "(";
		boolean hasOneElement = false;
		for (SearchRule rule : nestedRules) {
			if (rule.isValid()) {
				if (hasOneElement) {
					query += " " + equation.name() + " ";
				} else {
					hasOneElement = true;
				}
				query += rule.toLuceneQuery();
			}
		}
		return query + ")";
	}

	@Override
	public String toString() {
		return toLuceneQuery();
	}

	public void addNestedSearchRule(SearchRule rule) {
		this.nestedRules.add(rule);
		rule.setParent(this);
	}
	
	public void addNestedSearchRule(int i, SearchRule rule) {
		if (i == this.nestedRules.size()) {
			this.nestedRules.add(rule);
		} else {
			this.nestedRules.add(i, rule);
		}
		rule.setParent(this);
	}
	
	public void setNestedSearchRule(int i, SearchRule rule) {
		if (i < this.nestedRules.size() && this.nestedRules.get(i) != null) {
			this.nestedRules.get(i).setParent(null);
		}
		this.nestedRules.set(i, rule);
		rule.setParent(this);
	}
	
	public void removeNestedSearchRule(SearchRule rule) {
		for(int i = 0 ; i < nestedRules.size() ; i++) {
			if (nestedRules.get(i) == rule) {
				this.nestedRules.remove(i);
				rule.setParent(null);
				break;
			}
		}
	}

	@Override
	public List<SearchRule> toList() {
		List<SearchRule> rules = new ArrayList<SearchRule>();
		rules.add(this);
		for(SearchRule nestedRule : nestedRules) {
			rules.addAll(nestedRule.toList());
		}
		return rules;
	}
	
}
