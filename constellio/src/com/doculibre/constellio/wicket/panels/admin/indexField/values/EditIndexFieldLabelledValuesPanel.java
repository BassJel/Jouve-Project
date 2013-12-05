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
package com.doculibre.constellio.wicket.panels.admin.indexField.values;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.I18NLabel;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.locale.LocaleNameLabel;
import com.doculibre.constellio.wicket.components.locale.MultiLocaleComponentHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class EditIndexFieldLabelledValuesPanel extends AjaxPanel {
	
	private ReloadableEntityModel<IndexField> indexFieldModel;
	
	private String filter;
	
	private WebMarkupContainer valuesContainer;

	public EditIndexFieldLabelledValuesPanel(String id, IndexField indexField) {
		super(id);
		this.indexFieldModel = new ReloadableEntityModel<IndexField>(indexField);
		
		String titleKey = "labelledValues";
		IModel titleModel = new StringResourceModel(titleKey, this, null);
		add(new Label("panelTitle", titleModel));
		
		valuesContainer = new WebMarkupContainer("valuesContainer");
		add(valuesContainer);
		valuesContainer.setOutputMarkupId(true);
		
		add(new FilterForm("filterForm"));
		add(new AddLabelForm("addForm"));

		final IModel localesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				return new ArrayList<Locale>(collection.getLocales());
			}
		};
		
		valuesContainer.add(new ListView("locales", localesModel) {
			@Override
			protected void populateItem(ListItem item) {
				Locale locale = (Locale) item.getModelObject();
				item.add(new LocaleNameLabel("localeName", locale, true) {
					@Override
					public boolean isVisible() {
						AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
						RecordCollection collection = collectionAdminPanel.getCollection();
						return collection.getLocales().size() > 1;
					}
				});
			}
		});
		
		IModel labelledValuesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
			    IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
                IndexField indexField = indexFieldModel.getObject();
				List<String> indexFieldValues = indexFieldServices.suggestValues(indexField);
				for (I18NLabel labelledValue : indexField.getLabelledValues()) {
					indexFieldValues.add(labelledValue.getKey());
				}
				return indexFieldValues;
			}
		};
		
		valuesContainer.add(new ListView("items", labelledValuesModel) {
			@Override
			protected void populateItem(ListItem item) {
				final String indexFieldValue = (String) item.getModelObject();
				final IModel newIndexFieldValueModel = new Model(indexFieldValue);
                AjaxEditableLabel editableLabel = new AjaxEditableLabel("value", newIndexFieldValueModel) {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target) {
                        IndexField indexField = indexFieldModel.getObject();
                        String newIndexFieldValue = (String) newIndexFieldValueModel.getObject();
                        if (newIndexFieldValue == null || !newIndexFieldValue.equals(indexFieldValue)) {
                            IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
                            for (I18NLabel i18nLabel : indexField.getLabelledValues()) {
                                if (i18nLabel.getKey().equals(indexFieldValue)) {
                                    if (newIndexFieldValue == null) {
                                        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                                        if (!entityManager.getTransaction().isActive()) {
                                            entityManager.getTransaction().begin();
                                        }
                                        indexField.getLabels().remove(i18nLabel);
                                        indexFieldServices.makePersistent(indexField, false);
                                        entityManager.getTransaction().commit();
                                        entityManager.clear();
                                    } else {
                                        i18nLabel.setKey(newIndexFieldValue);
                                        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                                        if (!entityManager.getTransaction().isActive()) {
                                            entityManager.getTransaction().begin();
                                        }
                                        indexFieldServices.makePersistent(indexField, false);
                                        entityManager.getTransaction().commit();
                                        entityManager.clear();
                                    }
                                }
                            }
                        }
                        super.onSubmit(target);
                    }
                };
                item.add(editableLabel);

				MultiLocaleComponentHolder labelledValuesHolder = 
					new MultiLocaleComponentHolder("labels", indexFieldValue, indexFieldModel, "labelledValue", localesModel) {
						@Override
						protected void onPopulateItem(ListItem item, IModel componentModel, Locale locale) {
							AjaxEditableLabel editableLabel = new AjaxEditableLabel("editableLabel", componentModel) {
								@Override
								protected void onSubmit(AjaxRequestTarget target) {
									IndexField indexField = indexFieldModel.getObject();
									IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
									EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
									if (!entityManager.getTransaction().isActive()) {
										entityManager.getTransaction().begin();
									}
									indexFieldServices.makePersistent(indexField, false);
									entityManager.getTransaction().commit();
									entityManager.clear();
									
									super.onSubmit(target);
								}
							};
							item.add(editableLabel);
						}
				};
				item.add(labelledValuesHolder);
				
				item.add(new AjaxLink("deleteLink") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						IndexField indexField = indexFieldModel.getObject();
						IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
						EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
						if (!entityManager.getTransaction().isActive()) {
							entityManager.getTransaction().begin();
						}
						
						for (Iterator<I18NLabel> it = indexField.getLabelledValues().iterator(); it.hasNext();) {
							I18NLabel labelledValue = it.next();
							if (labelledValue.getKey().equals(indexFieldValue)) {
								it.remove();
							}
						}
						
						indexFieldServices.makePersistent(indexField, false);
						entityManager.getTransaction().commit();
						
						target.addComponent(valuesContainer);
					}

					@Override
					protected IAjaxCallDecorator getAjaxCallDecorator() {
						return new AjaxCallDecorator() {
							@Override
							public CharSequence decorateScript(CharSequence script) {
								String confirmMsg = getLocalizer().getString("confirmDelete", EditIndexFieldLabelledValuesPanel.this);
								return "if (confirm('" + confirmMsg + "')) {" + script + "} else { return false; }";
							}
						};
					}
				});
			}
		});
	}
	
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	@Override
	public void detachModels() {
		indexFieldModel.detach();
		super.detachModels();
	}
	
	private class FilterForm extends Form {

		public FilterForm(String id) {
			super(id);
			final TextField filterField = 
				new TextField("filterField", new PropertyModel(EditIndexFieldLabelledValuesPanel.this, "filter"));
			add(filterField);
			filterField.add(new AjaxFormComponentUpdatingBehavior("onkeyup") {
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					target.addComponent(valuesContainer);
				}
			});
			
			add(new AjaxButton("clearButton") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form form) {
					filterField.clearInput();
					filter = null;
					target.addComponent(filterField);
					target.addComponent(valuesContainer);
				}
			});
		}
	}
	
	private class AddLabelForm extends Form {
		
		private I18NLabel label = new I18NLabel();

		public AddLabelForm(String id) {
			super(id);
			setOutputMarkupId(true);
			setModel(new CompoundPropertyModel(label));
			IndexField indexField = indexFieldModel.getObject();
			setVisible(false);
			
			TextField keyField = new TextField("key");
			add(keyField);
			
			RecordCollection collection = indexField.getRecordCollection();
			add(new ListView("locales", new ArrayList<Locale>(collection.getLocales())) {
				@Override
				protected void populateItem(ListItem item) {
					final Locale locale = (Locale) item.getModelObject();
					IModel labelModel = new Model() {
						@Override
						public Object getObject() {
							return label.getValue(locale);
						}

						@Override
						public void setObject(Object object) {
							label.setValue((String) object, locale);
						}
					};
					item.add(new TextField("label", labelModel));
					item.add(new LocaleNameLabel("localeName", locale, true) {
						@Override
						public boolean isVisible() {
						    IndexField indexField = indexFieldModel.getObject();
							RecordCollection collection = indexField.getRecordCollection();
							return collection.getLocales().size() > 1;
						}
					});
				}
			});
			
			add(new AjaxButton("addButton") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form form) {
				    IndexField indexField = indexFieldModel.getObject();
					indexField.getLabelledValues().add(label);
					
					IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
					EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
					if (!entityManager.getTransaction().isActive()) {
						entityManager.getTransaction().begin();
					}
					indexFieldServices.makePersistent(indexField, false);
					entityManager.getTransaction().commit();
					
					label = new I18NLabel();
					AddLabelForm.this.setModel(new CompoundPropertyModel(label));
					AddLabelForm.this.clearInput();
					target.addComponent(AddLabelForm.this);
					target.addComponent(valuesContainer);
				}
			});
		}
		
	}

}
