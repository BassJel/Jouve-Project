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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.IntegerSearchRule;
import com.doculibre.constellio.wicket.components.holders.TextFieldHolder;

@SuppressWarnings("serial")
public class IntegerSearchRulePanel extends AbstractNumericSearchRulePanel<IntegerSearchRule> {

	public IntegerSearchRulePanel(String id, SimpleSearch search,
			IntegerSearchRule rule, IModel typesModel) {
		super(id, search, rule, typesModel);
	}

	@Override
	public Panel getInputPanel(String id, String prefix, IModel model) {
		TextFieldHolder panel = new TextFieldHolder(id, model);
		panel.getTextField().add(new SimpleAttributeModifier("name", prefix));
		panel.getTextField().add(new SimpleAttributeModifier("style", "width:80px"));
		return panel;
	}

	@Override
	protected String getSearchRuleType() {
		return IntegerSearchRule.TYPE;
	}

}
