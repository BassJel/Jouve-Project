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
package com.doculibre.constellio.wicket.panels.admin.collection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioNameUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.components.locale.LocaleNameLabel;
import com.doculibre.constellio.wicket.components.locale.MultiLocaleComponentHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.tabs.AdminTopMenuPanel;
import com.doculibre.constellio.wicket.validators.DuplicateItemValidator;

@SuppressWarnings("serial")
public class AddEditCollectionPanel extends SaveCancelFormPanel {
    
    private boolean edit;

	private ReloadableEntityModel<RecordCollection> collectionModel;

    public AddEditCollectionPanel(String id, RecordCollection collection) {
		super(id, false);
		this.collectionModel = new ReloadableEntityModel<RecordCollection>(collection);
		edit = collection.getId() != null;

		final Form form = getForm();
		form.setModel(new CompoundPropertyModel(collectionModel));
		form.add(new SetFocusBehavior(form));

		TextField nameField = new RequiredTextField("name");
		form.add(nameField);
		nameField.setEnabled(collection.getId() == null);
		nameField.setOutputMarkupId(true);
		nameField.add(new StringValidator.MaximumLengthValidator(50));
        nameField.add(new PatternValidator(ConstellioNameUtils.NAME_PATTERN));
		nameField.add(new DuplicateItemValidator() {
			@Override
			protected boolean isDuplicate(Object value) {
				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				return collectionServices.get((String) value) != null;
			}
		});
		
		TextField openSearchURL = new TextField("openSearchURL");
		form.add(openSearchURL);
		
		final WebMarkupContainer titleContainer = new WebMarkupContainer("titleContainer");
		form.add(titleContainer);
		titleContainer.setOutputMarkupId(true);
		
		MultiLocaleComponentHolder titleHolder = 
			new MultiLocaleComponentHolder("title", collectionModel, new PropertyModel(collectionModel, "locales")) {
				@Override
				protected void onPopulateItem(ListItem item, IModel componentModel, Locale locale) {
					TextField titleLocaleField = new RequiredTextField("titleField", componentModel);
					item.add(titleLocaleField);
					titleLocaleField.setOutputMarkupId(true);
					titleLocaleField.add(new StringValidator.MaximumLengthValidator(255));
					item.add(new LocaleNameLabel("localeName", locale, true) {
						@Override
						public boolean isVisible() {
							return collectionModel.getObject().getLocales().size() > 1;
						}
					});
				}
		};
		titleContainer.add(titleHolder);
		
		final WebMarkupContainer descriptionContainer = new WebMarkupContainer("descriptionContainer");
		form.add(descriptionContainer);
		descriptionContainer.setOutputMarkupId(true);

		MultiLocaleComponentHolder descriptionHolder = 
			new MultiLocaleComponentHolder("description", collectionModel, new PropertyModel(collectionModel, "locales")) {
				@Override
				protected void onPopulateItem(ListItem item, IModel componentModel, Locale locale) {
					TextArea descriptionLocaleField = new TextArea("descriptionLocale", componentModel);
					item.add(descriptionLocaleField);
					descriptionLocaleField.setOutputMarkupId(true);
					item.add(descriptionLocaleField);
					item.add(new LocaleNameLabel("localeName", locale, true) {
						@Override
						public boolean isVisible() {
							return collectionModel.getObject().getLocales().size() > 1;
						}
					});
				}
		};
		descriptionContainer.add(descriptionHolder);
		
		List<Locale> supportedLocales = ConstellioSpringUtils.getSupportedLocales();
		if (collection.getId() == null) {
			collection.setLocales(new HashSet<Locale>(supportedLocales));
		}
		
		CheckGroup localesCheckGroup = new CheckGroup("localesCheckGroup", new PropertyModel(collectionModel, "locales"));
		form.add(localesCheckGroup);
		localesCheckGroup.setOutputMarkupId(true);
		localesCheckGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.addComponent(titleContainer);
				target.addComponent(descriptionContainer);
			}
		});

		final Map<Locale, Check> localeChecks = new HashMap<Locale, Check>();
		localesCheckGroup.add(new ListView("locales", supportedLocales) {
			@Override
			protected void populateItem(ListItem item) {
				final Locale locale = (Locale) item.getModelObject();
				Check localeCheck = localeChecks.get(locale);
				if (localeCheck == null) {
					localeCheck = new Check("localeCheck", new Model(locale));
					localeChecks.put(locale, localeCheck);
				}
				item.add(localeCheck);
				item.add(new LocaleNameLabel("localeName", locale));
			}
		});
		
		CheckBox publicCollectionCheck = new CheckBox("publicCollection");
		form.add(publicCollectionCheck);
	}

	@Override
	public void detachModels() {
		collectionModel.detach();
		super.detachModels();
	}

    @Override
    protected IModel getTitleModel() {
        return new LoadableDetachableModel() {
			
			@Override
			protected Object load() {
		        String titleKey = edit ? "edit" : "add";
		        return new StringResourceModel(titleKey, AddEditCollectionPanel.this, null).getObject();
			}
		};
    }

    @Override
    protected void defaultReturnAction() {
        RecordCollection collection = collectionModel.getObject();
        AdminTopMenuPanel adminTabsPanel = (AdminTopMenuPanel) findParent(AdminTopMenuPanel.class);
        if (collection.getId() != null && !collection.isOpenSearch() && !edit) {
            AdminCollectionPanel adminCollectionPanel = new AdminCollectionPanel(TabbedPanel.TAB_PANEL_ID, collection);
            adminTabsPanel.replaceTabContent(adminCollectionPanel);
            adminCollectionPanel.setSelectedTab(0);
        } else {
            super.defaultReturnAction();
        }
    }

    @Override
    protected Component newReturnComponent(String id) {
        return new CollectionListPanel(id);
    }

    @Override
    protected void onSave(AjaxRequestTarget target) {
        RecordCollection collection = collectionModel.getObject();
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        collectionServices.makePersistent(collection, false);
        entityManager.getTransaction().commit();
        
        IndexingManager indexingManager = IndexingManager.get(collection);
        if (!indexingManager.isActive()) {
            indexingManager.startIndexing();
        }
    }

}
