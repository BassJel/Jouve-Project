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
package com.doculibre.constellio.wicket.panels.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebRequestCycle;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.entities.search.SearchedFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.plugins.api.wicket.search.HiddenSearchFormParamsPlugin;
import com.doculibre.constellio.services.AutocompleteServices;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.SimpleParams;
import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.components.form.TextAndValueAutoCompleteTextField;
import com.doculibre.constellio.wicket.components.form.WordsAndValueAutoCompleteRenderer;
import com.doculibre.constellio.wicket.pages.BaseConstellioPage;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.search.advanced.AdvancedSearchPanel;

@SuppressWarnings("serial")
public class SearchFormPanel extends AjaxPanel {

	private WebMarkupContainer searchForm;
	private ListView hiddenFields;
	private TextField queryField;
	private DropDownChoice languageDropDown;
	private RadioGroup searchTypeField;
	private Button searchButton;
	private WebMarkupContainer simpleSearchFormDiv;
	private WebMarkupContainer advancedSearchFormDiv;
	private AdvancedSearchPanel advancedSearchPanel;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SearchFormPanel(String id, final IModel simpleSearchModel) {
		super(id);

		searchForm = new WebMarkupContainer("searchForm",
				new CompoundPropertyModel(simpleSearchModel));

		simpleSearchFormDiv = new WebMarkupContainer("simpleSearchFormDiv") {
			@Override
			public boolean isVisible() {
				SimpleSearch search = (SimpleSearch) simpleSearchModel.getObject();
				return search.getAdvancedSearchRule() == null;
			}
		};
		advancedSearchFormDiv = new WebMarkupContainer("advancedSearchFormDiv") {
			@Override
			public boolean isVisible() {
				SimpleSearch search = (SimpleSearch) simpleSearchModel.getObject();
				return search.getAdvancedSearchRule() != null;
			}
		};

		advancedSearchPanel = new AdvancedSearchPanel("advanceForm",
				simpleSearchModel);

		searchForm.add(new AttributeModifier("action",
				new LoadableDetachableModel() {
					@Override
					protected Object load() {
				        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
						return urlFor(pageFactoryPlugin.getSearchResultsPage(), new PageParameters());
					}
				}));

		hiddenFields = new ListView("hiddenFields",
				new LoadableDetachableModel() {
					@Override
					protected Object load() {
						List<SimpleParam> hiddenParams = new ArrayList<SimpleParam>();
						HiddenSearchFormParamsPlugin hiddenSearchFormParamsPlugin = PluginFactory
								.getPlugin(HiddenSearchFormParamsPlugin.class);

						if (hiddenSearchFormParamsPlugin != null) {
							WebRequestCycle webRequestCycle = (WebRequestCycle) RequestCycle.get();
							HttpServletRequest request = webRequestCycle.getWebRequest().getHttpServletRequest();
							SimpleParams hiddenSimpleParams = hiddenSearchFormParamsPlugin.getHiddenParams(request);
							for (String paramName : hiddenSimpleParams.keySet()) {
								for (String paramValue : hiddenSimpleParams.getList(paramName)) {
									hiddenParams.add(new SimpleParam(paramName, paramValue));
								}
							}
						}

						SimpleSearch simpleSearch = (SimpleSearch) simpleSearchModel.getObject();
						SimpleSearch clone = simpleSearch.clone();

						SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils
								.getSearchInterfaceConfigServices();
						SearchInterfaceConfig config = searchInterfaceConfigServices
								.get();
						if (!config.isKeepFacetsNewSearch()) {
							// Will be true if we just clicked on a delete link
							// on the CurrentSearchPanel
							if (!clone.isRefinedSearch()) {
								clone.getSearchedFacets().clear();
								clone.setCloudKeyword(null);
							}
							// We must click on a delete link on
							// CurrentSearchPanel so that it is considered
							// again as refined
							clone.setRefinedSearch(false);
						}

						clone.initFacetPages();

						List<String> ignoredParamNames = Arrays.asList("query", "searchType", "page", "singleSearchLocale");
						SimpleParams searchParams = clone.toSimpleParams();
						for (String paramName : searchParams.keySet()) {
							if (!ignoredParamNames.contains(paramName) && !paramName.contains(SearchRule.ROOT_PREFIX)) {
								List<String> paramValues = searchParams.getList(paramName);
								for (String paramValue : paramValues) {
									SimpleParam hiddenParam = new SimpleParam(paramName, paramValue);
									hiddenParams.add(hiddenParam);
								}
							}
						}
						return hiddenParams;
					}
				}) {
			@Override
			protected void populateItem(ListItem item) {
				SimpleParam hiddenParam = (SimpleParam) item.getModelObject();
				if (hiddenParam.value != null) {
					item.add(new SimpleAttributeModifier("name", hiddenParam.name));
					item.add(new SimpleAttributeModifier("value", hiddenParam.value));
				} else {
					item.setVisible(false);
				}
			}
		};

		SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils
				.getSearchInterfaceConfigServices();
		SearchInterfaceConfig config = searchInterfaceConfigServices.get();

		if (config.isSimpleSearchAutocompletion()
				&& ((SimpleSearch) simpleSearchModel.getObject()).getAdvancedSearchRule() == null) {
			AutoCompleteSettings settings = new AutoCompleteSettings();
			settings.setCssClassName("simpleSearchAutoCompleteChoices");
			IModel model = new Model(((SimpleSearch) simpleSearchModel.getObject()).getQuery());
			
			WordsAndValueAutoCompleteRenderer render = new WordsAndValueAutoCompleteRenderer() {
				@Override
				protected String getTextValue(String word, Object value) {
					return word;
				}
			};
			
			queryField = new TextAndValueAutoCompleteTextField("query", model, String.class, settings, render) {
				@Override
				public String getInputName() {
					return super.getId();
				}

				@Override
				protected Iterator getChoicesForWord(String word) {
					SimpleSearch simpleSearch = (SimpleSearch) simpleSearchModel.getObject();
					String collectionName = simpleSearch.getCollectionName();
					AutocompleteServices autocompleteServices = ConstellioSpringUtils.getAutocompleteServices();
					RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
					RecordCollection collection = collectionServices.get(collectionName);
					List<String> suggestions = autocompleteServices.suggestSimpleSearch(word, collection, getLocale());
					return suggestions.iterator();
				}
				
				@Override
				protected boolean supportMultipleWords() {
					return false;
				}
			};
		} else {
			queryField = new TextField("query") {
				@Override
				public String getInputName() {
					return super.getId();
				}
			};
		}

		searchTypeField = new RadioGroup("searchType") {
			@Override
			public String getInputName() {
				return super.getId();
			}
		};

		IModel languages = new LoadableDetachableModel() {
			protected Object load() {
				Set<String> localeCodes = new HashSet<String>();
				SimpleSearch simpleSearch = (SimpleSearch) simpleSearchModel.getObject();
				RecordCollectionServices recordCollectionServices = ConstellioSpringUtils
						.getRecordCollectionServices();
				RecordCollection collection = recordCollectionServices
						.get(simpleSearch.getCollectionName());
				List<Locale> searchableLocales = ConstellioSpringUtils.getSearchableLocales();
				if (!collection.isOpenSearch()) {
					localeCodes.add("");
					if (!searchableLocales.isEmpty()) {
						for (Locale searchableLocale : searchableLocales) {
							localeCodes.add(searchableLocale.getLanguage());
						}
					} else {
						IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
						IndexField languageField = indexFieldServices.get(IndexField.LANGUAGE_FIELD, collection);
						for (String localeCode : indexFieldServices.suggestValues(languageField)) {
							localeCodes.add(localeCode);
						}
					}
				} else {
					localeCodes.add("");
					if (!searchableLocales.isEmpty()) {
						for (Locale searchableLocale : searchableLocales) {
							localeCodes.add(searchableLocale.getLanguage());
						}
					} else {
						for (Locale availableLocale : Locale.getAvailableLocales()) {
							localeCodes.add(availableLocale.getLanguage());
						}
					}
				}
				List<Locale> locales = new ArrayList<Locale>();
				for (String localeCode : localeCodes) {
					locales.add(new Locale(localeCode));
				}
				Collections.sort(locales, new Comparator<Locale>() {
					@Override
					public int compare(Locale locale1, Locale locale2) {
						Locale locale1Display;
						Locale locale2Display;
						SearchInterfaceConfig config = ConstellioSpringUtils.getSearchInterfaceConfigServices().get();
						if (config.isTranslateLanguageNames()) {
							locale1Display = locale2Display = getLocale();
						} else {
							locale1Display = locale1;
							locale2Display = locale2;
						}

						List<Locale> searchableLocales = ConstellioSpringUtils.getSearchableLocales();
						if (searchableLocales.isEmpty()) {
							searchableLocales = ConstellioSpringUtils.getSupportedLocales();
						}

						Integer indexOfLocale1;
						Integer indexOfLocale2;
						if (locale1.getLanguage().equals(getLocale().getLanguage())) {
							indexOfLocale1 = Integer.MIN_VALUE;
						} else {
							indexOfLocale1 = searchableLocales.indexOf(locale1);
						}
						if (locale2.getLanguage().equals(getLocale().getLanguage())) {
							indexOfLocale2 = Integer.MIN_VALUE;
						} else {
							indexOfLocale2 = searchableLocales.indexOf(locale2);
						}

						if (indexOfLocale1 == -1) {
							indexOfLocale1 = Integer.MAX_VALUE;
						}
						if (indexOfLocale2 == -1) {
							indexOfLocale2 = Integer.MAX_VALUE;
						}
						if (!indexOfLocale1.equals(Integer.MAX_VALUE) || !indexOfLocale2.equals(Integer.MAX_VALUE)) {
							return indexOfLocale1.compareTo(indexOfLocale2);
						} else if (StringUtils.isBlank(locale1.getLanguage())) {
							return Integer.MIN_VALUE;
						} else {
							return locale1.getDisplayLanguage(locale1Display)
									.compareTo(locale2.getDisplayLanguage(locale2Display));
						}
					}
				});
				return locales;
			}
		};

		IChoiceRenderer languageRenderer = new ChoiceRenderer() {
			@Override
			public Object getDisplayValue(Object object) {
				Locale locale = (Locale) object;
				String text;
				if (locale.getLanguage().isEmpty()) {
					text = (String) new StringResourceModel("all", SearchFormPanel.this, null).getObject();
				} else {
					Locale localeDisplay;
					SearchInterfaceConfig config = ConstellioSpringUtils.getSearchInterfaceConfigServices().get();
					if (config.isTranslateLanguageNames()) {
						localeDisplay = getLocale();
					} else {
						localeDisplay = locale;
					}
					text = StringUtils.capitalize(locale.getDisplayLanguage(localeDisplay));
				}
				return text;
			}

			@Override
			public String getIdValue(Object object, int index) {
				return ((Locale) object).getLanguage();
			}
		};

		IModel languageModel = new Model() {
			@Override
			public Object getObject() {
				SimpleSearch simpleSearch = (SimpleSearch) simpleSearchModel.getObject();
				Locale singleSearchLocale = simpleSearch.getSingleSearchLocale();
				if (singleSearchLocale == null) {
					SearchedFacet facet = simpleSearch.getSearchedFacet(IndexField.LANGUAGE_FIELD);
					List<String> values = facet == null ? new ArrayList<String>() : facet.getIncludedValues();
					singleSearchLocale = values.isEmpty() ? null : new Locale(values.get(0));
				}
				if (singleSearchLocale == null) {
					singleSearchLocale = getLocale();
				}
				return singleSearchLocale;
			}

			@Override
			public void setObject(Object object) {
				Locale singleSearchLocale = (Locale) object;
				SimpleSearch simpleSearch = (SimpleSearch) simpleSearchModel.getObject();
				simpleSearch.setSingleSearchLocale(singleSearchLocale);
			}
		};

		languageDropDown = new DropDownChoice("singleSearchLocale", languageModel, languages, languageRenderer) {
			@Override
			public String getInputName() {
				return "singleSearchLocale";
			}

			@Override
			public boolean isVisible() {
				SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils
						.getSearchInterfaceConfigServices();
				SearchInterfaceConfig config = searchInterfaceConfigServices.get();
				return config.isLanguageInSearchForm();
			}

			@Override
			protected CharSequence getDefaultChoice(Object selected) {
				return "";
			};
		};

		searchButton = new Button("searchButton") {
			@Override
			public String getMarkupId() {
				return super.getId();
			}

			@Override
			public String getInputName() {
				return super.getId();
			}
		};

		String submitImgUrl = ""
				+ urlFor(new ResourceReference(BaseConstellioPage.class,
						"images/ico_loupe.png"));

		add(searchForm);
		searchForm.add(simpleSearchFormDiv);
		searchForm.add(advancedSearchFormDiv);
		searchForm.add(hiddenFields);

		queryField.add(new SetFocusBehavior(queryField));

		addChoice(SimpleSearch.ALL_WORDS);
		addChoice(SimpleSearch.AT_LEAST_ONE_WORD);
		addChoice(SimpleSearch.EXACT_EXPRESSION);
		simpleSearchFormDiv.add(queryField);
		simpleSearchFormDiv.add(searchTypeField);
		simpleSearchFormDiv.add(languageDropDown);
		simpleSearchFormDiv.add(searchButton);
		advancedSearchFormDiv.add(advancedSearchPanel);
		searchButton.add(new SimpleAttributeModifier("src", submitImgUrl));
	}

	private void addChoice(String id) {
		searchTypeField.add(new Radio(id, new Model(id)) {
			@Override
			public String getValue() {
				return super.getId();
			}

			protected boolean getStatelessHint() {
				return true;
			}
		});
		searchTypeField.add(new Label("lbl_" + id, new StringResourceModel(
				"searchType." + id, this, null)));
	}

	public TextField getSearchTxtField() {
		return queryField;
	}

	public RadioGroup getSearchTypeField() {
		return searchTypeField;
	}

	public Button getSearchButton() {
		return searchButton;
	}

	private static class SimpleParam implements Serializable {

		String name;
		String value;

		private SimpleParam(String name, String value) {
			this.name = name;
			this.value = value;
		}

	}

}
