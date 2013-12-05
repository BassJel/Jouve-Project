package com.doculibre.constellio.wicket.panels.admin.searchInterface.autocomplete;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.services.AutocompleteServices;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.holders.ImgLinkHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.pages.BaseConstellioPage;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;

@SuppressWarnings("serial")
public class AdminAutocompletePanel extends SaveCancelFormPanel {

	private ReloadableEntityModel<SearchInterfaceConfig> configModel;
	private List<String> blacklistedTerms;

	public AdminAutocompletePanel(String id) {
		super(id, false);
		
		SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();
		AutocompleteServices autocompleteServices = ConstellioSpringUtils.getAutocompleteServices();
		
		SearchInterfaceConfig config = searchInterfaceConfigServices.get();
		configModel = new ReloadableEntityModel<SearchInterfaceConfig>(config);
		blacklistedTerms = autocompleteServices.getBlacklistedAutocompleteTerms();
		
		Form form = getForm();
		form.setModel(new CompoundPropertyModel(configModel));
		
        CheckBox simpleSearchAutocompletionField = new CheckBox("simpleSearchAutocompletion");
        TextField autoCompleteMinQueriesField = new TextField("autocompleteMinQueries", Integer.class);
        
        final WebMarkupContainer blacklistedTermsContainer = new WebMarkupContainer("blacklistedTermsContainer");
        blacklistedTermsContainer.setOutputMarkupId(true);
        
        ListView blacklistedTermsListView = new ListView("blacklistedTerms", blacklistedTerms) {
			@Override
			protected void populateItem(ListItem item) {
				final String term = (String) item.getModelObject();
				Label termLabel = new Label("term", term);
				ImgLinkHolder deleteLinkHolder = new ImgLinkHolder("deleteLink") {
					@Override
					protected WebMarkupContainer newLink(String id) {
						return new AjaxLink(id) {
							@Override
							public void onClick(AjaxRequestTarget target) {
								blacklistedTerms.remove(term);
								
								AutocompleteServices autocompleteServices = ConstellioSpringUtils.getAutocompleteServices();
								autocompleteServices.cancelBlacklistedAutocomplete(term);
								
								target.addComponent(blacklistedTermsContainer);
							}
						};
					}
					
					@Override
					protected Component newImg(String id) {
                        return new NonCachingImage(id, new ResourceReference(BaseConstellioPage.class, "images/ico_poubelle.png"));
					}
				};
				item.add(termLabel);
				item.add(deleteLinkHolder);
			}
		};
		
        form.add(simpleSearchAutocompletionField);
		form.add(autoCompleteMinQueriesField);
		form.add(blacklistedTermsContainer);
		blacklistedTermsContainer.add(blacklistedTermsListView);
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();
		SearchInterfaceConfig config = configModel.getObject();
		searchInterfaceConfigServices.makePersistent(config);
		entityManager.getTransaction().commit();
	}

	@Override
	protected IModel getTitleModel() {
		return new StringResourceModel("title", this, null);
	}

	@Override
	protected Component newReturnComponent(String id) {
		return new AdminAutocompletePanel(id);
	}

	@Override
	public void detachModels() {
		configModel.detach();
		super.detachModels();
	}

}
