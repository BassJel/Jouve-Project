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

import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.BooleanSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.DateSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.DoubleSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.FloatSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.IndexFieldSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.IntegerSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.LongSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.TextSearchRule;
import com.doculibre.constellio.utils.SimpleParams;

public class SearchRulesFactory {

	public static SearchRule constructSearchRule(SimpleParams params,
			SearchRulesGroup parent, String prefix) {
		
		String type = params.getString(prefix + SearchRule.DELIM + SearchRule.PARAM_TYPE);
		SearchRule rule;
		if (type == null) {
			rule = null;
			
		} else if (TextSearchRule.TYPE.equals(type)) {
			rule = new TextSearchRule(params, parent, prefix);
			
		} else if (SearchRulesGroup.TYPE.equals(type)) {
			rule = new SearchRulesGroup(params, parent, prefix);
			
		} else if (IntegerSearchRule.TYPE.equals(type)) {
			rule = new IntegerSearchRule(params, parent, prefix);
			
		} else if (DoubleSearchRule.TYPE.equals(type)) {
			rule = new DoubleSearchRule(params, parent, prefix);
			
		} else if (LongSearchRule.TYPE.equals(type)) {
			rule = new LongSearchRule(params, parent, prefix);
			
		} else if (FloatSearchRule.TYPE.equals(type)) {
			rule = new FloatSearchRule(params, parent, prefix);
			
		} else if (DateSearchRule.TYPE.equals(type)) {
			rule = new DateSearchRule(params, parent, prefix);
			
		} else if (BooleanSearchRule.TYPE.equals(type)) {
			rule = new BooleanSearchRule(params, parent, prefix);
			
		} else {
			rule = null;
		}
		return rule;

	}
	
	public static IndexFieldSearchRule constructSearchRule(IndexField field) {
		String type = field.getFieldType().getName();
		IndexFieldSearchRule rule;
		if (FieldType.STRING.equals(type) || type.contains(FieldType.TEXT)) {
			rule = new TextSearchRule();
			
		} else if (FieldType.INTEGER.equals(type)) {
			rule = new IntegerSearchRule();
			
		} else if (FieldType.DOUBLE.equals(type)) {
			rule = new DoubleSearchRule();
			
		} else if (FieldType.LONG.equals(type)) {
			rule = new LongSearchRule();
			
		} else if (FieldType.FLOAT.equals(type)) {
			rule = new FloatSearchRule();
			
		} else if (FieldType.DATE.equals(type)) {
			rule = new DateSearchRule();
			
		} else if (FieldType.BOOLEAN.equals(type)) {
			rule = new BooleanSearchRule();
			
		} else {
			return null;
		}
		
		rule.setIndexFieldName(field.getName());
		return rule;
	}

	public static SearchRule getDefaultFieldSearchRule() {
		TextSearchRule textSearchRule =  new TextSearchRule();
		textSearchRule.setIndexFieldName(TextSearchRule.DEFAULT_FIELD);
		return textSearchRule;
	}
	
	public static SearchRule getDefaultSearchRule() {
		return getDefaultFieldSearchRule();
	}
	
	public static SearchRule getInitialSearchRuleFor(RecordCollection collection) {
//		return getDefaultSearchRule();
		SearchRule rule = null;
		
		if (collection != null) {
			int nbRules = collection.getAdvancedSearchInitialRulesNumber();
			if (nbRules == 1) {
				SearchRulesGroup group =  new SearchRulesGroup();
				group.addNestedSearchRule(getDefaultSearchRule());
				rule = group;
			} else {
				SearchRulesGroup group =  new SearchRulesGroup();
				for(int i = 0 ; i < nbRules ; i++) {
					group.addNestedSearchRule(getDefaultSearchRule());
				}
				rule = group;
			}
		}
		return rule;
	}
	
	public static SearchRule getDefaultSearchRulesGroup() {
		SearchRulesGroup group =  new SearchRulesGroup();
		group.addNestedSearchRule(getDefaultSearchRule());
		return group;
	}
}
