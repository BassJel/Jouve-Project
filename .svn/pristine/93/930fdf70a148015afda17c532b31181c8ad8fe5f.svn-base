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
package com.doculibre.constellio.wicket.panels.admin.spellchecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class SpellCheckerConfigPanel extends AjaxPanel {

	private IModel collectionModel;
	
	public SpellCheckerConfigPanel(String id) {
		super(id);
		
		collectionModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				return collection;
			}
		};

		Form form = new Form("form", new CompoundPropertyModel(collectionModel));
		add(form);
		
		final CheckBox activeCheckbox = new CheckBox("spellCheckerActive");
		form.add(activeCheckbox);

		IModel languagesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				List<String> supportedLanguages = new ArrayList<String>();
				List<Locale> supportedLocales = ConstellioSpringUtils.getSupportedLocales();
				for (Locale supportedLocale : supportedLocales) {
					supportedLanguages.add(supportedLocale.getLanguage());
				}
				return supportedLanguages;
			}
		};

		IChoiceRenderer languagesRenderer = new ChoiceRenderer() {
			@Override
			public Object getDisplayValue(Object object) {
				return new Locale((String) object).getDisplayLanguage(getLocale());
			}
		};

		final DropDownChoice languageField = new DropDownChoice("spellCheckerLanguage", languagesModel,
				languagesRenderer) {
			@Override
			public boolean isEnabled() {
				Boolean spellCheckerActive = (Boolean) activeCheckbox.getModelObject();
				return Boolean.TRUE.equals(spellCheckerActive);
			}
		};
		form.add(languageField);
		languageField.setOutputMarkupId(true);
		
		activeCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.addComponent(languageField);
			}
		});

		Button submitButton = new Button("submitButton") {
			@Override
			public void onSubmit() {
				RecordCollection collection = (RecordCollection) collectionModel.getObject();

				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
				if (!entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().begin();
				}
				collectionServices.makePersistent(collection);
				entityManager.getTransaction().commit();
			}
		};
		form.add(submitButton);
	}

	@Override
	public void detachModels() {
		collectionModel.detach();
		super.detachModels();
	}

}
