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

import java.io.Serializable;
import java.util.List;

import com.doculibre.constellio.utils.SimpleParams;

public interface SearchRule extends Serializable{

	public static final String ROOT_PREFIX = "af";
	public static final String PARAM_TYPE = "t";
	
	public static final String DELIM = "_";
	
	SearchRule cloneRule();

	SearchRule cloneFullHierarchy();
	
	boolean isValid();

	SimpleParams toSimpleParams(boolean onlyType);

	String toLuceneQuery();

	String getPrefix();

	int getLevel();
	
	void setParent(SearchRulesGroup group);
	
	List<SearchRule> toList();
	
	SearchRulesGroup getParent();
	
	SearchRule getRootSearchRule();
	
	void replaceWith(SearchRule newRule);
	
	List<Integer> getPosition();
	
	int getRank();
	
}
