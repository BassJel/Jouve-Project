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
import java.util.Iterator;
import java.util.Map;

import org.apache.solr.common.util.NamedList;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.AdvancedSearchEnabledRule;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.entities.search.advanced.enums.TextSearchMethod;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.TextSearchRule;
import com.doculibre.constellio.services.AutocompleteServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.components.form.TextAndValueAutoCompleteTextField;

@SuppressWarnings("serial")
public class TextSearchRulePanel extends
		AbstractIndexFieldSearchRulePanel<TextSearchRule> {

	public TextSearchRulePanel(String id, SimpleSearch search,
			TextSearchRule rule, IModel typesModel) {
		super(id, search, rule, typesModel);

		TextField valueTextField;
		if (enabledRuleModel.getObject() != null
				&& ((AdvancedSearchEnabledRule) enabledRuleModel.getObject())
						.getIndexField().isAutocompleted()) {
			IModel valueModel = new Model(rule.getTextValue());
			AutoCompleteSettings settings = new AutoCompleteSettings();
			settings.setCssClassName("autoCompleteChoices");

			final String collectionName = search.getCollectionName();
			final String indexfieldName = rule.getIndexFieldName();
			valueTextField = new TextAndValueAutoCompleteTextField<Map.Entry<String, Object>>(
					"value", valueModel, String.class, settings) {

				@Override
				protected Iterator<Map.Entry<String, Object>> getChoicesForWord(
						String word) {
					RecordCollectionServices recordCollectionServices = ConstellioSpringUtils
							.getRecordCollectionServices();
					RecordCollection collection = recordCollectionServices
							.get(collectionName);
					IndexField indexField = collection
							.getIndexField(indexfieldName);
					AutocompleteServices autocompleteServices = ConstellioSpringUtils
							.getAutocompleteServices();
					NamedList<Object> choices = autocompleteServices.suggest(
							word, indexField);
					return choices.iterator();
				}
				
				
			};
		} else {
			valueTextField = new TextField("value", new Model(
					rule.getTextValue()));

		}

		valueTextField.add(new SimpleAttributeModifier("name", rule.getPrefix()
				+ SearchRule.DELIM + TextSearchRule.PARAM_VALUE));
		add(valueTextField);

		DropDownChoice searchMethodSelect = new DropDownChoice("searchMethod",
				new Model(rule.getSearchMethod()),
				Arrays.asList(TextSearchMethod.values()),
				new IChoiceRenderer() {

					@Override
					public String getIdValue(Object object, int index) {
						return ((TextSearchMethod) object).name();
					}

					@Override
					public Object getDisplayValue(Object object) {
						return new StringResourceModel(
								((TextSearchMethod) object).name(),
								TextSearchRulePanel.this, null).getString();
					}
				});
		searchMethodSelect.add(new SimpleAttributeModifier("name", rule
				.getPrefix()
				+ SearchRule.DELIM
				+ TextSearchRule.PARAM_SEARCH_METHOD));
		add(searchMethodSelect);
	}

	@Override
	protected String getSearchRuleType() {
		return TextSearchRule.TYPE;
	}

}
