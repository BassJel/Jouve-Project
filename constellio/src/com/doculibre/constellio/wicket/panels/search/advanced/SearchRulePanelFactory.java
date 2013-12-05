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
package com.doculibre.constellio.wicket.panels.search.advanced;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.entities.search.advanced.SearchRulesGroup;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.BooleanSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.DateSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.DoubleSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.FloatSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.IntegerSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.LongSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.TextSearchRule;
import com.doculibre.constellio.wicket.panels.search.advanced.searchRulePanels.BooleanSearchRulePanel;
import com.doculibre.constellio.wicket.panels.search.advanced.searchRulePanels.DateSearchRulePanel;
import com.doculibre.constellio.wicket.panels.search.advanced.searchRulePanels.DoubleSearchRulePanel;
import com.doculibre.constellio.wicket.panels.search.advanced.searchRulePanels.FloatSearchRulePanel;
import com.doculibre.constellio.wicket.panels.search.advanced.searchRulePanels.IntegerSearchRulePanel;
import com.doculibre.constellio.wicket.panels.search.advanced.searchRulePanels.LongSearchRulePanel;
import com.doculibre.constellio.wicket.panels.search.advanced.searchRulePanels.SearchRulesGroupPanel;
import com.doculibre.constellio.wicket.panels.search.advanced.searchRulePanels.TextSearchRulePanel;

public class SearchRulePanelFactory {

	public static Component constructPanel(String id, SimpleSearch search, SearchRule rule, IModel typesModel) {
		Component panel;
		if (rule instanceof DateSearchRule) {
			panel = new DateSearchRulePanel(id, search, (DateSearchRule) rule, typesModel);
			
		} else if (rule instanceof DoubleSearchRule) {
			panel = new DoubleSearchRulePanel(id, search, (DoubleSearchRule) rule, typesModel);
			
		} else if (rule instanceof FloatSearchRule) {
			panel = new FloatSearchRulePanel(id, search, (FloatSearchRule) rule, typesModel);
			
		} else if (rule instanceof IntegerSearchRule) {
			panel = new IntegerSearchRulePanel(id, search, (IntegerSearchRule) rule, typesModel);
		
		} else if (rule instanceof LongSearchRule) {
			panel = new LongSearchRulePanel(id, search, (LongSearchRule) rule, typesModel);
			
		} else if (rule instanceof SearchRulesGroup) {
			panel = new SearchRulesGroupPanel(id, search, (SearchRulesGroup) rule);
			
		} else if (rule instanceof TextSearchRule) {
			panel = new TextSearchRulePanel(id, search, (TextSearchRule) rule, typesModel);
			
		} else if (rule instanceof BooleanSearchRule) {
			panel = new BooleanSearchRulePanel(id, search, (BooleanSearchRule) rule, typesModel);
			
		} else {
			return new Label(id, "Unrecognized rule : " + rule.getClass().getSimpleName());
		}
		return panel;
	}
	
}
