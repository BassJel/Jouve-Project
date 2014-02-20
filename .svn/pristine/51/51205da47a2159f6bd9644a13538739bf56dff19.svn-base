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
package com.doculibre.constellio.wicket.panels.search.advanced.searchRulePanels;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.IndexFieldSearchRule;
import com.doculibre.constellio.wicket.panels.search.advanced.AbstractSearchRulePanel;

@SuppressWarnings("serial")
public abstract class AbstractIndexFieldSearchRulePanel<T extends IndexFieldSearchRule> extends AbstractSearchRulePanel<T> {

	public AbstractIndexFieldSearchRulePanel(String id, SimpleSearch search,
			T rule, IModel ruleTypesChoice) {
		super(id, search, rule, ruleTypesChoice);
		
		HiddenField searchRuleIndexFieldHiddenField = new HiddenField(
				"searchRuleIndexField", new Model(rule.getIndexFieldName()));
		searchRuleIndexFieldHiddenField.add(new SimpleAttributeModifier("name", rule
				.getPrefix() + SearchRule.DELIM + IndexFieldSearchRule.PARAM_INDEX_FIELD));
		add(searchRuleIndexFieldHiddenField);
		
	}

}
