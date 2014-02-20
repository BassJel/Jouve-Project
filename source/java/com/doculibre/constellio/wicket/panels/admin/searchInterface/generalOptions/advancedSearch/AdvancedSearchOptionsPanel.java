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
package com.doculibre.constellio.wicket.panels.admin.searchInterface.generalOptions.advancedSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.doculibre.constellio.entities.AdvancedSearchEnabledRule;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.AutocompleteServices;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.CollectionIndexFieldsModel;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public class AdvancedSearchOptionsPanel extends AjaxPanel {

	private ReloadableEntityModel<RecordCollection> collectionModel;

	public AdvancedSearchOptionsPanel(String id, IModel theCollectionModel) {
		super(id);
		this.collectionModel = new ReloadableEntityModel<RecordCollection>(
				theCollectionModel);

		AjaxCheckBox enabledCheckbox = getEnabledCheckbox();
		add(enabledCheckbox);

		ListView enabledRules = getEnabledRulesListView();
		add(enabledRules);

		DropDownChoice initialRulesNumberDropdownChoice = getInitialRulesNumberDropdownChoice();
		add(initialRulesNumberDropdownChoice);

		add(new WebMarkupContainer("enabledRulesHeader") {

			@Override
			public boolean isVisible() {
				RecordCollection collection = collectionModel.getObject();
				return collection.isAdvancedSearchEnabled();
			}
		});
	}

	private DropDownChoice getInitialRulesNumberDropdownChoice() {
		IModel model = new PropertyModel(collectionModel,
				"advancedSearchInitialRulesNumber");
		List<Integer> choices = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5 });
		DropDownChoice choice = new DropDownChoice("initialRules", model,
				choices) {
			@Override
			protected boolean wantOnSelectionChangedNotifications() {
				return true;
			}

			@Override
			protected void onSelectionChanged(Object newSelection) {
				super.onSelectionChanged(newSelection);

				EntityManager entityManager = ConstellioPersistenceContext
						.getCurrentEntityManager();
				if (!entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().begin();
				}
				Integer newInitialRules = (Integer) newSelection;
				RecordCollection collection = collectionModel.getObject();
				collection.setAdvancedSearchInitialRulesNumber(newInitialRules);
				RecordCollectionServices recordCollectionServices = ConstellioSpringUtils
						.getRecordCollectionServices();
				recordCollectionServices.merge(collection);
				entityManager.getTransaction().commit();
			}

			@Override
			public boolean isVisible() {
				RecordCollection collection = collectionModel.getObject();
				return collection.isAdvancedSearchEnabled();
			}
		};
		return choice;
	}

	private AjaxCheckBox getEnabledCheckbox() {
		AjaxCheckBox enabledCheckbox = new AjaxCheckBox("enabled",
				new PropertyModel(collectionModel, "advancedSearchEnabled")) {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				EntityManager entityManager = ConstellioPersistenceContext
						.getCurrentEntityManager();
				if (!entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().begin();
				}
				RecordCollection collection = collectionModel.getObject();
				RecordCollectionServices recordCollectionServices = ConstellioSpringUtils
						.getRecordCollectionServices();
				recordCollectionServices.merge(collection);
				entityManager.getTransaction().commit();
				target.addComponent(AdvancedSearchOptionsPanel.this);
			}

		};
		return enabledCheckbox;
	}

	private ListView getEnabledRulesListView() {

		AutocompleteServices autocompleteServices = ConstellioSpringUtils
				.getAutocompleteServices();
		final List<String> autoCompleteSupportedFields = new ArrayList<String>();
		for (IndexField indexField : autocompleteServices
				.getAutoCompleteIndexFields(collectionModel.getObject())) {
			autoCompleteSupportedFields.add(indexField.getName());
		}
//		IModel indexFieldsModel = new LoadableDetachableModel() {
//
//			@Override
//			protected Object load() {
//				RecordCollection collection = collectionModel.getObject();
//				List<IndexField> indexFields = new ArrayList<IndexField>();
//				indexFields.addAll(collection.getIndexFields());
//				Collections.sort(indexFields, new Comparator<IndexField>() {
//
//					@Override
//					public int compare(IndexField o1, IndexField o2) {
//						return o1.getName().toLowerCase()
//								.compareTo(o2.getName().toLowerCase());
//					}
//				});
//				return indexFields;
//			}
//		};
		IModel indexFieldsModel = new CollectionIndexFieldsModel(collectionModel);
		
		ListView enabledRules = new ListView("enabledRules", indexFieldsModel) {

			@Override
			protected void populateItem(ListItem item) {
				IndexField indexField = (IndexField) item.getModelObject();
				final String indexFieldName = indexField.getName();
				AdvancedSearchEnabledRule rule = null;

				for (AdvancedSearchEnabledRule ruleIt : collectionModel
						.getObject().getAdvancedSearchEnabledRules()) {
					if (ruleIt.getIndexField().equals(indexField)) {
						rule = ruleIt;
						break;
					}
				}
				final boolean enabled = rule != null;

				item.add(new Label("indexFieldName", indexFieldName));

				AjaxCheckBox enabledCheckBox = new AjaxCheckBox("enabled",
						new Model(enabled)) {

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						EntityManager entityManager = ConstellioPersistenceContext
								.getCurrentEntityManager();
						if (!entityManager.getTransaction().isActive()) {
							entityManager.getTransaction().begin();
						}

						RecordCollection collection = collectionModel
								.getObject();

						AdvancedSearchEnabledRule rule = null;
						for (AdvancedSearchEnabledRule ruleIt : collection
								.getAdvancedSearchEnabledRules()) {
							if (ruleIt.getIndexField().getName()
									.equals(indexFieldName)) {
								rule = ruleIt;
								break;
							}
						}

						if (rule == null) {
							AdvancedSearchEnabledRule enabledRule = new AdvancedSearchEnabledRule();
							enabledRule.setIndexField(collection
									.getIndexField(indexFieldName));
							enabledRule.setRecordCollection(collection);
							collection.getAdvancedSearchEnabledRules().add(
									enabledRule);

						} else {
							collection.getAdvancedSearchEnabledRules().remove(
									rule);
						}
						RecordCollectionServices recordCollectionServices = ConstellioSpringUtils
								.getRecordCollectionServices();
						recordCollectionServices.merge(collection);

						entityManager.getTransaction().commit();
						target.addComponent(AdvancedSearchOptionsPanel.this);
					}
				};
				item.add(enabledCheckBox);

				AjaxCheckBox autocompleteCheckBox = new AjaxCheckBox(
						"autocomplete", new Model(indexField.isAutocompleted())) {

					@Override
					protected void onUpdate(AjaxRequestTarget target) {

						RecordCollection collection = collectionModel
								.getObject();
						if (indexFieldName != null) {
							EntityManager entityManager = ConstellioPersistenceContext
									.getCurrentEntityManager();
							if (!entityManager.getTransaction().isActive()) {
								entityManager.getTransaction().begin();
							}
							IndexFieldServices indexFieldServices = ConstellioSpringUtils
									.getIndexFieldServices();
							AutocompleteServices autocompleteServices = ConstellioSpringUtils
									.getAutocompleteServices();
							IndexField indexField = indexFieldServices.get(
									indexFieldName, collection);
							if (indexField.isAutocompleted()) {
								autocompleteServices.removeAutoCompleteFromField(indexField);
							} else {
								autocompleteServices.setAutoCompleteToField(indexField);
							}
//							RecordCollectionServices recordCollectionServices = ConstellioSpringUtils
//									.getRecordCollectionServices();
//							recordCollectionServices.makePersistent(indexField.getRecordCollection(), true);
							indexFieldServices.makePersistent(indexField, true);
							entityManager.getTransaction().commit();
						}

						target.addComponent(AdvancedSearchOptionsPanel.this);
					}
				};

				autocompleteCheckBox.setEnabled(enabled);
				item.add(autocompleteCheckBox);

			}

			@Override
			public boolean isVisible() {
				RecordCollection collection = collectionModel.getObject();
				return collection.isAdvancedSearchEnabled();
			}

		};
		return enabledRules;
	}

	@Override
	protected void detachModel() {
		collectionModel.detach();
	}

}
