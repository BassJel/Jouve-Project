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

import java.util.Arrays;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.entities.search.advanced.SearchRulesGroup;
import com.doculibre.constellio.entities.search.advanced.enums.BooleanEquation;

@SuppressWarnings("serial")
public class SearchRulesGroupPanel extends Panel {

	public SearchRulesGroupPanel(String id, SimpleSearch search, final SearchRulesGroup rule) {
		super(id);
		HiddenField searchRuleTypeHiddenField = new HiddenField(
				"searchRuleType", new Model(SearchRulesGroup.TYPE));
		searchRuleTypeHiddenField.add(new SimpleAttributeModifier("name", rule
				.getPrefix() + SearchRule.DELIM + SearchRule.PARAM_TYPE));
		add(searchRuleTypeHiddenField);
		DropDownChoice equationSelect =  new DropDownChoice("equation", new Model(rule.getEquation()), Arrays.asList(BooleanEquation.values()), new IChoiceRenderer() {
			
			@Override
			public String getIdValue(Object object, int index) {
				return ((BooleanEquation)object).name();
			}
			
			@Override
			public Object getDisplayValue(Object object) {
				return new StringResourceModel(((BooleanEquation)object).name(), SearchRulesGroupPanel.this, null).getString();
			}
		});
		equationSelect.add(new SimpleAttributeModifier("name", rule.getPrefix() + SearchRule.DELIM + SearchRulesGroup.PARAM_EQUATION));
		add(equationSelect);
		
		IModel labelModel = new LoadableDetachableModel() {
			
			@Override
			protected Object load() {
				String label = new StringResourceModel("theseRules", SearchRulesGroupPanel.this, null).getString();
				return label.replace("#", "" + rule.getNestedRules().size());
			}
		};
		
		add(new Label("theseRules", labelModel));
	}

}
