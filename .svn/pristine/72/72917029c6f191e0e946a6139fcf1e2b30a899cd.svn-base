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
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.AdvancedSearchEnabledRule;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.entities.search.advanced.SearchRulesFactory;
import com.doculibre.constellio.entities.search.advanced.SearchRulesGroup;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.TextSearchRule;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.SimpleParams;
import com.doculibre.constellio.wicket.session.ConstellioSession;
import com.doculibre.constellio.wicket.utils.SimpleParamsUtils;

/**
 * Render the advanced search panel table, it manage delete, add rule and add
 * group buttons
 * 
 * @author francisbaril
 * 
 */
@SuppressWarnings("serial")
public class AdvancedSearchPanel extends Panel {

	private static final CompressedResourceReference MYPAGE_JS = new CompressedResourceReference(
			AdvancedSearchPanel.class, "UrlCompletionScript.js");

	public AdvancedSearchPanel(String id, final IModel simpleSearchModel) {
		super(id);

		add(HeaderContributor.forJavaScript(MYPAGE_JS));

		final IModel ruleTypesModel = new LoadableDetachableModel() {
			
			protected Object load() {
				RecordCollectionServices recordCollectionServices = ConstellioSpringUtils
				.getRecordCollectionServices();
				
				SimpleSearch search = (SimpleSearch) simpleSearchModel.getObject();
				RecordCollection recordCollection = recordCollectionServices.get(search.getCollectionName());
				
				List<RuleTypeChoice> choices = new ArrayList<RuleTypeChoice>();
				
				for(AdvancedSearchEnabledRule enabledRule : recordCollection.getAdvancedSearchEnabledRules()) {
					IndexField indexField = enabledRule.getIndexField();
					
					SearchRule rule = SearchRulesFactory.constructSearchRule(indexField);
					if (rule != null) {
						SimpleParams params = rule.toSimpleParams(true);
						
						RuleTypeChoice choice = new RuleTypeChoice();
						String title = indexField.getLabel(IndexField.LABEL_TITLE, ConstellioSession.get().getLocale());
						choice.name = title != null ? title : indexField.getName();
						choice.params = params;
						choices.add(choice);
					}
				}
				
				Collections.sort(choices);
				
				RuleTypeChoice defaultFieldSearch = new RuleTypeChoice();
				defaultFieldSearch.name = new StringResourceModel("defaultField", AdvancedSearchPanel.this, null).getString();
				defaultFieldSearch.params = SearchRulesFactory.getDefaultFieldSearchRule().toSimpleParams(true);
				choices.add(0, defaultFieldSearch);
				
				return choices;
			}
			
		};
		
		IModel rulesModel = new LoadableDetachableModel() {

			@Override
			protected Object load() {
				SimpleSearch simpleSearch = (SimpleSearch) simpleSearchModel
						.getObject();
				SearchRule rootSearchRule = simpleSearch
						.getAdvancedSearchRule();
				
				List<SearchRule> rules;
				if (rootSearchRule == null) {
					rules = new ArrayList<SearchRule>();
				} else {
					rules = rootSearchRule.toList();
				}


				return rules;
			}
		};
		add(new ListView("rules", rulesModel) {

			@Override
			protected void populateItem(ListItem item) {
				SimpleSearch simpleSearch = (SimpleSearch) simpleSearchModel
						.getObject();
				final SimpleSearch clonedSimpleSearch = simpleSearch.clone();
				final SearchRule rule = (SearchRule) item.getModelObject();

				Component ruleComponent = SearchRulePanelFactory
						.constructPanel("rule", clonedSimpleSearch, rule, ruleTypesModel);
				//int leftSpace = 25 * Math.max(0, rule.getLevel() - 1);
				int leftSpace = 25 * rule.getLevel();
				ruleComponent.add(new SimpleAttributeModifier("style",
						"margin-left:" + leftSpace + "px;"));
				item.add(ruleComponent);
				item.add(newDeleteButton(clonedSimpleSearch, rule));
				item.add(newAddRuleButton(clonedSimpleSearch, rule));
				item.add(newAddGroupButton(clonedSimpleSearch, rule));
			}

		});
	}

	private Button newDeleteButton(final SimpleSearch clonedSimpleSearch,
			final SearchRule rule) {
		
		SimpleParams withRowDeleted = new SimpleParams();
		String restrictedPrefix = rule.getPrefix();

		if (rule.getParent() == null) {
			RecordCollectionServices recordCollectionServices = ConstellioSpringUtils.getRecordCollectionServices();
			RecordCollection recordcollection = recordCollectionServices.get(clonedSimpleSearch.getCollectionName());
			clonedSimpleSearch.setAdvancedSearchRule(SearchRulesFactory.getInitialSearchRuleFor(recordcollection));
			withRowDeleted = clonedSimpleSearch.toSimpleParams(true);
		} else {
			withRowDeleted = clonedSimpleSearch.toSimpleParams(true);
			List<String> params = new ArrayList<String>();
			params.addAll(withRowDeleted.keySet());
			for(String param : params) {
				if (param.contains(rule.getPrefix())) {
					withRowDeleted.remove(param);
				}
			}
		}
		String url = urlFor(
				getPage().getClass(),
				SimpleParamsUtils.toPageParameters(withRowDeleted)).toString();
		Button delete = new Button("delete");
		delete.add(new SimpleAttributeModifier("name", "delete"));
		delete.add(new SimpleAttributeModifier("onclick", "goToUrl('" + url
				+ "', '" + SearchRule.ROOT_PREFIX + "', '" + restrictedPrefix
				+ "')"));
		return delete;
	}

	private Button newAddRuleButton(final SimpleSearch clonedSimpleSearch,
			final SearchRule rule) {
		Button addRule = new Button("addRule");
		
		SearchRule addedSearchRule;
		
		if (rule instanceof TextSearchRule) {
			addedSearchRule = rule.cloneRule();
		} else {
			addedSearchRule = SearchRulesFactory.getDefaultSearchRule();
		}
		
		String url = getAddRuleUrl(clonedSimpleSearch, rule, addedSearchRule);
		addRule.add(new SimpleAttributeModifier("name", "addRule"));
		addRule.add(new SimpleAttributeModifier("onclick", url));
		return addRule;
	}

	private Button newAddGroupButton(final SimpleSearch clonedSimpleSearch,
			final SearchRule rule) {

		Button addGroup = new Button("addGroup");
		String url = getAddRuleUrl(clonedSimpleSearch, rule,
				SearchRulesFactory.getDefaultSearchRulesGroup());
		addGroup.add(new SimpleAttributeModifier("name", "addGroup"));
		addGroup.add(new SimpleAttributeModifier("onclick", url));
		return addGroup;
	}

	private String getAddRuleUrl(final SimpleSearch clonedSimpleSearch,
			final SearchRule rule, final SearchRule addedRule) {
		SearchRule clonedRule = rule.cloneFullHierarchy();
		if (clonedRule.getParent() == null
				&& !(clonedRule instanceof SearchRulesGroup)) {
			SearchRulesGroup group = new SearchRulesGroup();
			group.addNestedSearchRule(clonedRule);
			group.addNestedSearchRule(addedRule);
			clonedRule = group;
		} else if (clonedRule instanceof SearchRulesGroup) {
			((SearchRulesGroup) clonedRule).addNestedSearchRule(addedRule);

		} else {
			SearchRulesGroup parent = clonedRule.getParent();

			int rank = clonedRule.getRank();
			parent.addNestedSearchRule(rank + 1, addedRule);
		}

		clonedSimpleSearch
				.setAdvancedSearchRule(clonedRule.getRootSearchRule());
		String url = urlFor(
				getPage().getClass(),
				SimpleParamsUtils.toPageParameters(clonedSimpleSearch
						.toSimpleParams(true))).toString();
		return "goToUrl('" + url + "', '" + SearchRule.ROOT_PREFIX + "')";
	}

}
