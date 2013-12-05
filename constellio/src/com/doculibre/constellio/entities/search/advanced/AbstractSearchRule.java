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
import java.util.Arrays;
import java.util.List;

import com.doculibre.constellio.utils.SimpleParams;


@SuppressWarnings("serial")
public abstract class AbstractSearchRule implements SearchRule {

	private SearchRulesGroup parent;
	
	public AbstractSearchRule() {
	}
	
	public AbstractSearchRule(SearchRulesGroup parent) {
		this.parent = parent;
	}
	
	@Override
	public String getPrefix() {
		if (parent != null) {
			String parentPrefix = parent.getPrefix();
			int rank = getRank() + 1;
			if (rank == 0) {
				rank = parent.getNestedRules().size() + 1;
			}
			return parentPrefix + "_" + rank;
		}
		return ROOT_PREFIX;
	}

	@Override
	public int getLevel() {
		return parent == null ? 0 : (parent.getLevel() + 1); 
	}

	public SearchRulesGroup getParent() {
		return parent;
	}
	
	public void setParent(SearchRulesGroup parent) {
		this.parent = parent;
	}
	
	@Override
	public List<SearchRule> toList() {
		return Arrays.asList(new SearchRule[]{this});
	}

	@Override
	public SearchRule getRootSearchRule() {
		return parent == null ? this : parent.getRootSearchRule();
	}
	
	@Override
	public final SearchRule cloneFullHierarchy() {
		List<Integer> position = getPositionOf(this);
		SearchRule rootRule = getRootSearchRule();
		SearchRule clonedRootRule = rootRule.cloneRule();
		return getRule(clonedRootRule, position);
	}

	private static SearchRule getRule(SearchRule anyHierarchyRule, List<Integer> position) {
		SearchRule root = anyHierarchyRule.getRootSearchRule();
		SearchRule rule = root;
		for(Integer i : position) {
			rule = ((SearchRulesGroup)rule).getNestedRules().get(i);
		}
		return rule;
	}
	
	@Override
	public final List<Integer> getPosition() {
		return getPositionOf(this);
	}
	
	private static List<Integer> getPositionOf(SearchRule rule) {
		List<Integer> position = new ArrayList<Integer>();
		SearchRule current = rule;
		while(current.getParent() != null) {
			SearchRulesGroup parent = current.getParent();
			int i = 0;
			for(SearchRule child : ((SearchRulesGroup) parent).getNestedRules()) {
				if (child == current) {
					position.add(0, i);
					break;
				}
				i++;
			}
			current = current.getParent();
		}
		return position;
	}
	
	@Override
	public final int getRank() {
		int i = 0;
		boolean found = false;
		for(SearchRule brother : parent.getNestedRules()) {
			if (brother == this) {
				found = true;
				break;
			}
			i++;
		}
		return found ? i : -1;
	}
	
	@Override
	public final void replaceWith(SearchRule newRule) {
		if (parent != null) {
			int rank = getRank();
			parent.setNestedSearchRule(rank, newRule);
			
		}
	}
	
	protected void addIfDifferent(SimpleParams params, String suffixe, Object from, Object to) {
		if (from == null && to != null) {
			params.add(getPrefix() + "_" + suffixe, to.toString());
		} else if (from != null && to != null && !from.equals(to)) {
			params.add(getPrefix() + "_" + suffixe, to.toString());
		}
	}
}
