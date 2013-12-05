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
package com.doculibre.constellio.wicket.panels.admin.facets;

import java.util.Arrays;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.CollectionFacet;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.FacetServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.locale.LocaleNameLabel;
import com.doculibre.constellio.wicket.components.locale.MultiLocaleComponentHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.models.admin.AdminCollectionIndexFieldsModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class AddEditFacetPanel extends SaveCancelFormPanel {

	private ReloadableEntityModel<CollectionFacet> facetModel;
	
	private IModel indexFieldOptionsModel;
	
	private IChoiceRenderer indexFieldRenderer;

	public AddEditFacetPanel(String id, CollectionFacet facet) {
		super(id, true);
		this.facetModel = new ReloadableEntityModel<CollectionFacet>(facet);
		this.indexFieldOptionsModel = new AdminCollectionIndexFieldsModel(this);
		
		this.indexFieldRenderer = new ChoiceRenderer() {
			@Override
			public Object getDisplayValue(Object object) {
				IndexField indexField = (IndexField) object;
				return indexField.getName();
			}
		};

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(facetModel));

		final WebMarkupContainer contextFieldsContainer = new WebMarkupContainer("contextFields");
		form.add(contextFieldsContainer);
		contextFieldsContainer.setOutputMarkupId(true);

		IModel localesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				return collection.getLocales();
			}
		};
		MultiLocaleComponentHolder nameHolder = 
			new MultiLocaleComponentHolder("name", facetModel, localesModel) {
				@Override
				protected void onPopulateItem(ListItem item, IModel componentModel, Locale locale) {
					TextField nameField = new RequiredTextField("nameLocale", componentModel);
					item.add(nameField);
					item.add(new LocaleNameLabel("localeName", locale, true) {
						@Override
						public boolean isVisible() {
							AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
							RecordCollection collection = collectionAdminPanel.getCollection();
							return collection.getLocales().size() > 1;
						}
					});
				}
		};
		form.add(nameHolder);
		
		IChoiceRenderer facetTypeRenderer = new ChoiceRenderer() {
			@Override
			public Object getDisplayValue(Object object) {
				String key = "facetType." + object;
				return getLocalizer().getString(key, AddEditFacetPanel.this);
			}
		};
		DropDownChoice facetType = 
			new DropDownChoice(
					"facetType", 
					Arrays.asList(CollectionFacet.FACET_TYPES), 
					facetTypeRenderer);
		form.add(facetType);
		facetType.setOutputMarkupId(true);
		facetType.add(new OnChangeAjaxBehavior() {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.addComponent(contextFieldsContainer);
			}
		});
        
        CheckBox hideEmptyValues = new CheckBox("hideEmptyValues");
        CheckBox sortable = new CheckBox("sortable");
        CheckBox multiValued = new CheckBox("multiValued");
        form.add(hideEmptyValues);
        form.add(sortable);
        form.add(multiValued);

		//FIXME Bug when changing the type of an existing facet
		facetType.setEnabled(facet.getId() == null);
		
		addFieldFacetFields(contextFieldsContainer);
		addQueryFacetFields(contextFieldsContainer);
		addClusterFacetFields(contextFieldsContainer);
		addCloudKeywordFields(contextFieldsContainer);
	}
	
	private void addFieldFacetFields(WebMarkupContainer container) {
		WebMarkupContainer fieldFacetFieldsContainer = new WebMarkupContainer("fieldFacetFields") {
			@Override
			public boolean isVisible() {
				CollectionFacet facet = facetModel.getObject();
				return facet.isFieldFacet();
			}
		};
		container.add(fieldFacetFieldsContainer);
		
		DropDownChoice facetField = 
			new DropDownChoice(
					"facetField", 
					indexFieldOptionsModel, 
					indexFieldRenderer);
		fieldFacetFieldsContainer.add(facetField);
	}
	
	private void addQueryFacetFields(WebMarkupContainer container) {}
	
	private void addClusterFacetFields(WebMarkupContainer container) {
		WebMarkupContainer clusterFacetFieldsContainer = new WebMarkupContainer("clusterFacetFields") {
			@Override
			public boolean isVisible() {
				CollectionFacet facet = facetModel.getObject();
				return facet.isClusterFacet();
			}
		};
		container.add(clusterFacetFieldsContainer);
		
		DropDownChoice clusteringEngine = 
			new DropDownChoice(
					"clusteringEngine", 
					Arrays.asList(CollectionFacet.CLUSTERING_ENGINES));
		clusterFacetFieldsContainer.add(clusteringEngine);
		
		DropDownChoice carrotTitleField = 
			new DropDownChoice(
					"carrotTitleField", 
					indexFieldOptionsModel, 
					indexFieldRenderer);
		clusterFacetFieldsContainer.add(carrotTitleField);
		carrotTitleField.setNullValid(true);
		
		DropDownChoice carrotUrlField = 
			new DropDownChoice(
					"carrotUrlField", 
					indexFieldOptionsModel, 
					indexFieldRenderer);
		clusterFacetFieldsContainer.add(carrotUrlField);
		carrotUrlField.setNullValid(true);
		
		DropDownChoice carrotSnippetField = 
			new DropDownChoice(
					"carrotSnippetField", 
					indexFieldOptionsModel, 
					indexFieldRenderer);
		clusterFacetFieldsContainer.add(carrotSnippetField);
		carrotSnippetField.setNullValid(true);
		
		CheckBox clusteringUseSearchResults = new CheckBox("clusteringUseSearchResults");
		clusterFacetFieldsContainer.add(clusteringUseSearchResults);
		
		CheckBox clusteringUseCollection = new CheckBox("clusteringUseCollection");
		clusterFacetFieldsContainer.add(clusteringUseCollection);
		
		CheckBox clusteringUseDocSet = new CheckBox("clusteringUseDocSet");
		clusterFacetFieldsContainer.add(clusteringUseDocSet);
		
		CheckBox carrotProduceSummary = new CheckBox("carrotProduceSummary");
		clusterFacetFieldsContainer.add(carrotProduceSummary);
		
		TextField carrotNumDescriptions = new TextField("carrotNumDescriptions");
		clusterFacetFieldsContainer.add(carrotNumDescriptions);
		
		CheckBox carrotOutputSubclusters = new CheckBox("carrotOutputSubclusters");
		clusterFacetFieldsContainer.add(carrotOutputSubclusters);
	}
	
	private void addCloudKeywordFields(WebMarkupContainer container) {
		WebMarkupContainer cloudKeywordFacetFieldsContainer = new WebMarkupContainer("cloudKeywordFacetFields") {
			@Override
			public boolean isVisible() {
				CollectionFacet facet = facetModel.getObject();
				return facet.isCloudKeywordFacet();
			}
		};
		container.add(cloudKeywordFacetFieldsContainer);
		
		DropDownChoice facetField = 
			new DropDownChoice(
					"facetField", 
					indexFieldOptionsModel, 
					indexFieldRenderer);
		cloudKeywordFacetFieldsContainer.add(facetField);
	}

	@Override
	public void detachModels() {
		facetModel.detach();
		indexFieldOptionsModel.detach();
		super.detachModels();
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			
			@Override
			protected Object load() {
		        String titleKey = facetModel.getObject().getId() == null ? "add" : "edit";
		        return new StringResourceModel(titleKey, AddEditFacetPanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		RecordCollection collection = collectionAdminPanel.getCollection();
		
		CollectionFacet facet = facetModel.getObject();
		if (facet.getId() == null) {
            collection.addCollectionFacet(facet);
		}
		
		FacetServices facetServices = ConstellioSpringUtils.getFacetServices();
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		facetServices.makePersistent(facet);
		entityManager.getTransaction().commit();
	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		target.addComponent(collectionAdminPanel);
	}

}
