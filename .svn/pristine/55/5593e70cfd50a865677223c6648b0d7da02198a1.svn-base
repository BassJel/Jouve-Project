package com.doculibre.constellio.wicket.panels.admin.searchInterface.context;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.searchInterface.SearchInterfaceContext;
import com.doculibre.constellio.services.SearchInterfaceContextServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.locale.LocaleNameLabel;
import com.doculibre.constellio.wicket.components.locale.MultiLocaleComponentHolder;
import com.doculibre.constellio.wicket.models.EntityModel;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;

@SuppressWarnings("serial")
public class AddEditSearchInterfaceContextPanel extends SaveCancelFormPanel {
	
	private EntityModel<SearchInterfaceContext> contextModel;
	private ReloadableEntityModel<SearchInterfaceContext> parentContextModel;
	
	private TextField contextNameField;
	private TextField curlValueField;
	private Component parentContextLabel;
	private CheckBox externalFilesField;
	private MultiLocaleComponentHolder headTagHtmlContentHolder;
	private MultiLocaleComponentHolder headerHtmlContentHolder;
	private MultiLocaleComponentHolder footerHtmlContentHolder;

	public AddEditSearchInterfaceContextPanel(String id, SearchInterfaceContext context) {
		super(id, false);
		this.contextModel = new EntityModel<SearchInterfaceContext>(context);

		SearchInterfaceContext parentContext = context.getParentContext();
		if (parentContext != null) {
			parentContextModel = new ReloadableEntityModel<SearchInterfaceContext>(parentContext);
			parentContextLabel = new Label("parentContext", new PropertyModel(parentContextModel, "contextName"));
		} else {
			parentContextLabel = new WebMarkupContainer("parentContext").setVisible(false);
		}

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(contextModel));

		final IModel localesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				return ConstellioSpringUtils.getSupportedLocales();
			}
		};
		
		contextNameField = new RequiredTextField("contextName");
		curlValueField = new RequiredTextField("curlValue");
		externalFilesField = new CheckBox("externalFiles");
		
		headTagHtmlContentHolder = 
				new MultiLocaleComponentHolder("headTagHtmlContent", "headTagHtmlContent", contextModel, "includedHtmlContent", localesModel) {
					@Override
					protected void onPopulateItem(ListItem item, IModel componentModel, Locale locale) {
						TextArea headTagHtmlContentLocaleField = new TextArea("headTagHtmlContentLocale", componentModel);
						AjaxLink copyLink = newCopyFromParentContextLink("copyLink", SearchInterfaceContext.HEAD_TAG_HTML_CONTENT, locale, headTagHtmlContentLocaleField);
						item.add(copyLink);
						item.add(headTagHtmlContentLocaleField);
						item.add(new LocaleNameLabel("localeName", locale, true) {
							@SuppressWarnings("unchecked")
							@Override
							public boolean isVisible() {
								List<Locale> locales = (List<Locale>) localesModel.getObject();
								return locales.size() > 1;
							}
						});
					}
		};
		
		headerHtmlContentHolder = 
				new MultiLocaleComponentHolder("headerHtmlContent", "headerHtmlContent", contextModel, "includedHtmlContent", localesModel) {
					@Override
					protected void onPopulateItem(ListItem item, IModel componentModel, Locale locale) {
						TextArea headerHtmlContentLocaleField = new TextArea("headerHtmlContentLocale", componentModel);
						item.add(headerHtmlContentLocaleField);
						AjaxLink copyLink = newCopyFromParentContextLink("copyLink", SearchInterfaceContext.HEADER_HTML_CONTENT, locale, headerHtmlContentLocaleField);
						item.add(copyLink);
						item.add(new LocaleNameLabel("localeName", locale, true) {
							@SuppressWarnings("unchecked")
							@Override
							public boolean isVisible() {
								List<Locale> locales = (List<Locale>) localesModel.getObject();
								return locales.size() > 1;
							}
						});
					}
		};
			
		footerHtmlContentHolder = 
				new MultiLocaleComponentHolder("footerHtmlContent", "footerHtmlContent", contextModel, "includedHtmlContent", localesModel) {
					@Override
					protected void onPopulateItem(ListItem item, IModel componentModel, Locale locale) {
						TextArea footerHtmlContentLocaleField = new TextArea("footerHtmlContentLocale", componentModel);
						item.add(footerHtmlContentLocaleField);
						AjaxLink copyLink = newCopyFromParentContextLink("copyLink", SearchInterfaceContext.FOOTER_HTML_CONTENT, locale, footerHtmlContentLocaleField);
						item.add(copyLink);
						item.add(new LocaleNameLabel("localeName", locale, true) {
							@SuppressWarnings("unchecked")
							@Override
							public boolean isVisible() {
								List<Locale> locales = (List<Locale>) localesModel.getObject();
								return locales.size() > 1;
							}
						});
					}
		};
			
		form.add(contextNameField);
		form.add(curlValueField);
		form.add(parentContextLabel);
		form.add(externalFilesField);
		form.add(headTagHtmlContentHolder);
		form.add(headerHtmlContentHolder);
		form.add(footerHtmlContentHolder);
	}
	
	private AjaxLink newCopyFromParentContextLink(String id, final String propertyName, final Locale locale, final Component component) {
		component.setOutputMarkupId(true);
		return new AjaxLink(id) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				String copiedValue;
				SearchInterfaceContext parentContext = parentContextModel.getObject();
				if (SearchInterfaceContext.HEAD_TAG_HTML_CONTENT.equals(propertyName)) {
					copiedValue = parentContext.getEffectiveHeadTagHtmlContent(locale);
				} else if (SearchInterfaceContext.HEADER_HTML_CONTENT.equals(propertyName)) {
					copiedValue = parentContext.getEffectiveHeaderHtmlContent(locale);
				} else if (SearchInterfaceContext.FOOTER_HTML_CONTENT.equals(propertyName)) {
					copiedValue = parentContext.getEffectiveFooterHtmlContent(locale);
				} else {
					copiedValue = null;
				}
				if (copiedValue != null) {
					component.setModelObject(copiedValue);
					target.addComponent(component);
				}
			}

			@Override
			public boolean isVisible() {
				boolean visible = super.isVisible();
				if (visible && parentContextModel == null) {
					visible = false;
				}
				return visible;
			}
		};
	}
	
	@Override
	public void detachModels() {
		if (parentContextModel != null) {
			parentContextModel.detach();
		}
		super.detachModels();
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		SearchInterfaceContext context = contextModel.getObject();
		if (context.getId() == null && parentContextModel != null) {
			SearchInterfaceContext parentContext = parentContextModel.getObject();
			context.setParentContext(parentContext);
		}
		
		SearchInterfaceContextServices contextServices = ConstellioSpringUtils.getSearchInterfaceContextServices();
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		contextServices.makePersistent(context);
		entityManager.getTransaction().commit();
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			@Override
			protected Object load() {
				String titleKey = contextModel.getObject().getId() == null ? "add" : "edit";
				return new StringResourceModel(titleKey, AddEditSearchInterfaceContextPanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		SearchInterfaceContextListPanel replacement;
		if (parentContextModel != null) {
			SearchInterfaceContext parentContext = parentContextModel.getObject();
			replacement = new SearchInterfaceContextListPanel(id, parentContext);
		} else {
			replacement = new SearchInterfaceContextListPanel(id);
		}
		return replacement;
	}
	
}
