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

import java.io.Serializable;
import java.util.Arrays;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.entities.search.advanced.enums.MathEquation;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.AbstractNumericSearchRule;
import com.doculibre.constellio.utils.SimpleParams;
import com.doculibre.constellio.wicket.utils.SimpleParamsUtils;

@SuppressWarnings("serial")
public abstract class AbstractNumericSearchRulePanel<T extends AbstractNumericSearchRule<?>>
		extends AbstractIndexFieldSearchRulePanel<T> {

	private SimpleParams params;
	private DropDownChoice equationSelect;
	
	public AbstractNumericSearchRulePanel(String id, SimpleSearch search,
			T rule, IModel typesModel) {
		super(id, search, rule, typesModel);

		equationSelect = new DropDownChoice("equation",
				new Model(rule.getEquation()),
				Arrays.asList(getAvailableEquations()), new IChoiceRenderer() {

					@Override
					public String getIdValue(Object object, int index) {
						return ((MathEquation) object).name();
					}

					@Override
					public Object getDisplayValue(Object object) {
						return new StringResourceModel(((MathEquation) object)
								.name(), AbstractNumericSearchRulePanel.this,
								null).getString();
					}
				});
		equationSelect.add(new SimpleAttributeModifier("name", rule.getPrefix()
				+ SearchRule.DELIM + AbstractNumericSearchRule.PARAM_EQUATION));

		add(equationSelect);
		params = search.toSimpleParams(true);

		add(getInputPanel("comp", rule.getPrefix() + SearchRule.DELIM + AbstractNumericSearchRule.PARAM_VALUE_1, new Model(
				(Serializable) rule.getComparisonValue())));

		boolean comp2Visible = rule.getEquation().need2Param();
		IModel labelBetweenModel = new StringResourceModel("and",
				AbstractNumericSearchRulePanel.this, null);
		add(new Label("and", labelBetweenModel).setVisible(comp2Visible));

		add(getInputPanel("comp2", rule.getPrefix() + SearchRule.DELIM + AbstractNumericSearchRule.PARAM_VALUE_2,
				new Model((Serializable) rule.getSecondComparisonValue())).setVisible(
						comp2Visible));
	}
	
	@Override
	protected void onBeforeRender() {

		//refresh the page when the value change
		String url = urlFor(
				getPage().getClass(),
				SimpleParamsUtils.toPageParameters(params)).toString();
		String jsCode = "goToUrl('" + url +"' , '" + SearchRule.ROOT_PREFIX + "')";
		
		equationSelect.add(new SimpleAttributeModifier("onChange", jsCode));
		
		super.onBeforeRender();
	}
	
	
	public abstract Panel getInputPanel(String id, String prefix, IModel model);

	protected MathEquation[] getAvailableEquations() {
		return MathEquation.values();
	}

}
