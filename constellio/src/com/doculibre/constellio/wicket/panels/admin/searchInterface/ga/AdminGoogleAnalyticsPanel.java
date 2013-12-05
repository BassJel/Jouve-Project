package com.doculibre.constellio.wicket.panels.admin.searchInterface.ga;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;

import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;

@SuppressWarnings("serial")
public class AdminGoogleAnalyticsPanel extends SaveCancelFormPanel {

	private ReloadableEntityModel<SearchInterfaceConfig> configModel;

    private CheckBox useGoogleAnalyticsField;
    private TextField googleAnalyticsUAField;
    private TextArea googleAnalyticsHeaderField;

    public AdminGoogleAnalyticsPanel(String id) {
		super(id, false);
		
		SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();
		
		SearchInterfaceConfig config = searchInterfaceConfigServices.get();
		configModel = new ReloadableEntityModel<SearchInterfaceConfig>(config);
		
		Form form = getForm();
		form.setModel(new CompoundPropertyModel(configModel));
		
        useGoogleAnalyticsField = new CheckBox("useGoogleAnalytics");
        googleAnalyticsUAField = new TextField("googleAnalyticsUA");
        googleAnalyticsHeaderField = new TextArea("googleAnalyticsHeader");
		
        form.add(useGoogleAnalyticsField);
		form.add(googleAnalyticsUAField);
		form.add(googleAnalyticsHeaderField);
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		SearchInterfaceConfig config = configModel.getObject();
		boolean valid = true;
		if (config.isUseGoogleAnalytics()) {
			String googleAnalyticsUA = config.getGoogleAnalyticsUA();
			String googleAnalyticsHeader = config.getGoogleAnalyticsHeader();
			
			if (StringUtils.isEmpty(googleAnalyticsUA) || googleAnalyticsUA.equals(SearchInterfaceConfig.GA_UA_PLACEHOLDER)) {
				googleAnalyticsUAField.error((IValidationError)new ValidationError().addMessageKey("Required"));
				valid = false;
			}
			if (StringUtils.isEmpty(googleAnalyticsHeader)) {
				googleAnalyticsHeaderField.error((IValidationError)new ValidationError().addMessageKey("Required"));
				valid = false;
			}
		} 
		if (valid) {
			EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
			if (!entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().begin();
			}
			SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();
			searchInterfaceConfigServices.makePersistent(config);
			entityManager.getTransaction().commit();
		}
	}

	@Override
	protected IModel getTitleModel() {
		return new StringResourceModel("title", this, null);
	}

	@Override
	protected Component newReturnComponent(String id) {
		return new AdminGoogleAnalyticsPanel(id);
	}

	@Override
	public void detachModels() {
		configModel.detach();
		super.detachModels();
	}

}
