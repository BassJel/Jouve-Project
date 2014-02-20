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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.entities.AdvancedSearchEnabledRule;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.IndexFieldSearchRule;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.SimpleParams;
import com.doculibre.constellio.wicket.utils.SimpleParamsUtils;

/**
 * This panel include components specific to the search rule. All components of
 * this panel (and super-panel) should have a name set to '$prefix_$param' where
 * prefix is gived by the search rule's position in the search rule tree.
 * 
 * @author francisbaril
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractSearchRulePanel<T extends SearchRule> extends Panel {

	protected IModel enabledRuleModel;
	
	@SuppressWarnings("unchecked")
	public AbstractSearchRulePanel(String id, final SimpleSearch search,
			final T rule, IModel ruleTypesChoice) {
		super(id);
		
		enabledRuleModel = new LoadableDetachableModel() {
			
			@Override
			protected Object load() {
				return getEnabledRuleFor(rule, search);
			}
		}; 
			
			
		
		SimpleSearch clonedSearch = search.clone();
		
		SimpleParams currentRuleParams = rule.toSimpleParams(true);
		
		List<RuleTypeChoice> baseChoices = (List<RuleTypeChoice>) ruleTypesChoice.getObject();
		RuleTypeChoice selectedChoice = null;
		List<RuleTypeChoice> adaptedChoices = new ArrayList<RuleTypeChoice>();
		for(RuleTypeChoice baseChoice : baseChoices) {
			boolean isSelected = true;
			RuleTypeChoice adaptedChoice = new RuleTypeChoice();
			adaptedChoice.name = baseChoice.name;
			clonedSearch.setAdvancedSearchRule(rule.getRootSearchRule());
			adaptedChoice.params.addAll(search.toSimpleParams(true));
			for(String param : baseChoice.params.keySet()) {
				String paramName = param.replace(SearchRule.ROOT_PREFIX, "");
				String adaptedName = rule.getPrefix() + paramName;
				String value = baseChoice.params.getString(param);
				
				adaptedChoice.params.remove(adaptedName);
				adaptedChoice.params.add(adaptedName, value);
				isSelected &= value.equals(currentRuleParams.getString(adaptedName));
			}
			
			if (isSelected) {
				selectedChoice = adaptedChoice;
			}
			adaptedChoices.add(adaptedChoice);
		}
		IChoiceRenderer renderer = new IChoiceRenderer() {

			@Override
			public Object getDisplayValue(Object object) {
				RuleTypeChoice choice = (RuleTypeChoice) object;
				return choice.name;
			}

			@Override
			public String getIdValue(Object object, int index) {
				RuleTypeChoice choice = (RuleTypeChoice) object;
				
				String url = urlFor(
						getPage().getClass(),
						SimpleParamsUtils.toPageParameters(choice.params)).toString();
				return url;
			}

		};
		DropDownChoice typeSelect = new DropDownChoice("type", new Model(selectedChoice), adaptedChoices, renderer);
		typeSelect.add(new SimpleAttributeModifier("onChange", "goToUrl(this.value, '" + SearchRule.ROOT_PREFIX + "', '" + rule.getPrefix() + "')"));
		//We dont want this value to be submit, because it is very long and not usefull
		typeSelect.add(new SimpleAttributeModifier("name", ""));
		add(typeSelect);
		
		HiddenField searchRuleTypeHiddenField = new HiddenField(
				"searchRuleType", new Model(getSearchRuleType()));
		searchRuleTypeHiddenField.add(new SimpleAttributeModifier("name", rule
				.getPrefix() + SearchRule.DELIM + SearchRule.PARAM_TYPE));
		add(searchRuleTypeHiddenField);
	}

	/**
	 * Used to set the name of a hidden field, these hidden fields allow the
	 * search rule hierarchy to be reconstructed on a new request
	 * 
	 * @return
	 */
	protected abstract String getSearchRuleType();

	private AdvancedSearchEnabledRule getEnabledRuleFor(T rule, SimpleSearch search) {
		RecordCollectionServices recordCollectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = recordCollectionServices.get(search.getCollectionName());
		AdvancedSearchEnabledRule enabledRule = null;
		for(AdvancedSearchEnabledRule ruleIt : collection.getAdvancedSearchEnabledRules()) {
			if (rule instanceof IndexFieldSearchRule) {
				if (ruleIt.getIndexField() != null && ruleIt.getIndexField().getName().equals(((IndexFieldSearchRule) rule).getIndexFieldName())) {
					enabledRule = ruleIt;
				}
			}
		}
		return enabledRule;
	}
	
	@Override
	protected void detachModel() {
		super.detachModel();
		//enabledRuleModel.detach();
	}
}
