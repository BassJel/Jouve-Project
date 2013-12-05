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
package com.doculibre.constellio.wicket.panels.admin.facets.values;

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

import com.doculibre.constellio.entities.CollectionFacet;
import com.doculibre.constellio.entities.I18NLabel;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.FacetServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.locale.LocaleNameLabel;
import com.doculibre.constellio.wicket.components.locale.MultiLocaleComponentHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class EditFacetLabelledValuesPanel extends AjaxPanel {
	
	private ReloadableEntityModel<CollectionFacet> facetModel;
	
	private String filter;
	
	private WebMarkupContainer valuesContainer;

	public EditFacetLabelledValuesPanel(String id, CollectionFacet facet) {
		super(id);
		this.facetModel = new ReloadableEntityModel<CollectionFacet>(facet);
		
		String titleKey = facet.isQueryFacet() ? "queries" : "labelledValues";
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
				List<String> facetValues;
				FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
				CollectionFacet facet = facetModel.getObject();
				if (facet.isFieldFacet()) {
					facetValues = facetServices.suggestValues(facet, filter);
				} else {
					facetValues = new ArrayList<String>();
					for (I18NLabel labelledValue : facet.getLabelledValues()) {
						facetValues.add(labelledValue.getKey());
					}
				}
				return facetValues;
			}
		};
		
		valuesContainer.add(new ListView("items", labelledValuesModel) {
			@Override
			protected void populateItem(ListItem item) {
				final String facetValue = (String) item.getModelObject();
				final IModel newFacetValueModel = new Model(facetValue);
                AjaxEditableLabel editableLabel = new AjaxEditableLabel("value", newFacetValueModel) {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target) {
                        CollectionFacet facet = facetModel.getObject();
                        String newFacetValue = (String) newFacetValueModel.getObject();
                        if (newFacetValue == null || !newFacetValue.equals(facetValue)) {
                            FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
                            for (I18NLabel i18nLabel : facet.getLabelledValues()) {
                                if (i18nLabel.getKey().equals(facetValue)) {
                                    if (newFacetValue == null) {
                                        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                                        if (!entityManager.getTransaction().isActive()) {
                                            entityManager.getTransaction().begin();
                                        }
                                        facet.getLabels().remove(i18nLabel);
                                        facetServices.makePersistent(facet);
                                        entityManager.getTransaction().commit();
                                        entityManager.clear();
                                    } else {
                                        i18nLabel.setKey(newFacetValue);
                                        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                                        if (!entityManager.getTransaction().isActive()) {
                                            entityManager.getTransaction().begin();
                                        }
                                        facetServices.makePersistent(facet);
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
					new MultiLocaleComponentHolder("labels", facetValue, facetModel, "labelledValue", localesModel) {
						@Override
						protected void onPopulateItem(ListItem item, IModel componentModel, Locale locale) {
							AjaxEditableLabel editableLabel = new AjaxEditableLabel("editableLabel", componentModel) {
								@Override
								protected void onSubmit(AjaxRequestTarget target) {
									CollectionFacet facet = facetModel.getObject();
									FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
									EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
									if (!entityManager.getTransaction().isActive()) {
										entityManager.getTransaction().begin();
									}
									facetServices.makePersistent(facet);
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
						CollectionFacet facet = facetModel.getObject();
						FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
						EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
						if (!entityManager.getTransaction().isActive()) {
							entityManager.getTransaction().begin();
						}
						
						for (Iterator<I18NLabel> it = facet.getLabelledValues().iterator(); it.hasNext();) {
							I18NLabel labelledValue = it.next();
							if (labelledValue.getKey().equals(facetValue)) {
								it.remove();
							}
						}
						
						facetServices.makePersistent(facet);
						entityManager.getTransaction().commit();
						
						target.addComponent(valuesContainer);
					}

					@Override
					protected IAjaxCallDecorator getAjaxCallDecorator() {
						return new AjaxCallDecorator() {
							@Override
							public CharSequence decorateScript(CharSequence script) {
								String confirmMsg = getLocalizer().getString("confirmDelete", EditFacetLabelledValuesPanel.this);
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
		facetModel.detach();
		super.detachModels();
	}
	
	private class FilterForm extends Form {

		public FilterForm(String id) {
			super(id);
			CollectionFacet facet = facetModel.getObject();
			setVisible(facet.isFieldFacet());
			
			final TextField filterField = 
				new TextField("filterField", new PropertyModel(EditFacetLabelledValuesPanel.this, "filter"));
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
			CollectionFacet facet = facetModel.getObject();
			setVisible(facet.isQueryFacet());
			
			TextField keyField = new TextField("key");
			add(keyField);
			
			RecordCollection collection = facet.getRecordCollection();
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
							CollectionFacet facet = facetModel.getObject();
							RecordCollection collection = facet.getRecordCollection();
							return collection.getLocales().size() > 1;
						}
					});
				}
			});
			
			add(new AjaxButton("addButton") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form form) {
					CollectionFacet facet = facetModel.getObject();
					facet.getLabelledValues().add(label);
					
					FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
					EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
					if (!entityManager.getTransaction().isActive()) {
						entityManager.getTransaction().begin();
					}
					facetServices.makePersistent(facet);
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
