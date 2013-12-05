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
package com.doculibre.constellio.wicket.panels.admin.featuredLink;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import wicket.contrib.tinymce.TinyMceBehavior;
import wicket.contrib.tinymce.settings.TinyMCESettings;

import com.doculibre.constellio.entities.FeaturedLink;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.FeaturedLinkServices;
import com.doculibre.constellio.utils.AnalyzerUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.locale.LocaleNameLabel;
import com.doculibre.constellio.wicket.components.locale.MultiLocaleComponentHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class AddEditFeaturedLinkPanel extends SaveCancelFormPanel implements IHeaderContributor {

	private ReloadableEntityModel<FeaturedLink> featuredLinkModel;

	public AddEditFeaturedLinkPanel(String id, FeaturedLink featuredLink) {
		super(id, false);
		this.featuredLinkModel = new ReloadableEntityModel<FeaturedLink>(featuredLink);

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(featuredLinkModel));

		IModel localesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				return collection.getLocales();
			}
		};
		MultiLocaleComponentHolder titleHolder = 
			new MultiLocaleComponentHolder("linkTitle", "title", featuredLinkModel, localesModel) {
				@Override
				protected void onPopulateItem(ListItem item, IModel componentModel, Locale locale) {
					TextField titleLocaleField = new RequiredTextField("titleLocale", componentModel);
					titleLocaleField.add(new StringValidator.MaximumLengthValidator(50));
					item.add(titleLocaleField);
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
		form.add(titleHolder);

		MultiLocaleComponentHolder descriptionHolder = 
			new MultiLocaleComponentHolder("description", featuredLinkModel, localesModel) {
			@Override
			protected void onPopulateItem(ListItem item, IModel componentModel, Locale locale) {
				TextArea descriptionLocaleField = new TextArea("descriptionLocale", componentModel);
//				descriptionLocaleField.add(new StringValidator.MaximumLengthValidator(50));
				item.add(descriptionLocaleField);

				TinyMCESettings tinyMCESettings = new TinyMCESettings(TinyMCESettings.Theme.advanced);
				tinyMCESettings.setToolbarLocation(TinyMCESettings.Location.top);
				descriptionLocaleField.add(new TinyMceBehavior(tinyMCESettings));
				
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
		form.add(descriptionHolder);

		IModel keywordsModel = new Model() {
			@Override
			public Object getObject() {
				FeaturedLink featuredLink = featuredLinkModel.getObject();
				StringBuffer keywordsSB = new StringBuffer();
				for (Iterator<String> it = featuredLink.getKeywords().iterator(); it.hasNext();) {
					String keyword = it.next();
					keywordsSB.append(keyword);
					if (it.hasNext()) {
						keywordsSB.append("\n");
					}
				}
				return keywordsSB.toString();
			}

			@Override
			public void setObject(Serializable object) {
				String keywordsText = (String) object;
				FeaturedLink featuredLink = featuredLinkModel.getObject();
				featuredLink.getKeywords().clear();
				if (keywordsText != null) {
					AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
					RecordCollection collection = collectionAdminPanel.getCollection();
					
					StringTokenizer st = new StringTokenizer(keywordsText, "\n");
					while (st.hasMoreTokens()) {
						String keyword = StringUtils.trim(st.nextToken());
						String keywordAnalyzed = AnalyzerUtils.analyze(keyword, collection);
						featuredLink.getKeywords().add(keyword);
						featuredLink.getKeywordsAnalyzed().add(keywordAnalyzed);
					}
				}
			}
		};
		form.add(new TextArea("keywords", keywordsModel));
	}

	@Override
	public void detachModels() {
		featuredLinkModel.detach();
		super.detachModels();
	}

	/**
	 * This is needed because even though {@link TinyMceBehavior} implements
	 * IHeaderContributor, the header doesn't get contributed when the component
	 * is first rendered though an AJAX call.
	 * 
	 * @see https://issues.apache.org/jira/browse/WICKET-618 (which was closed
	 *      WontFix)
	 */
	@Override
	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference(TinyMCESettings.javaScriptReference());
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			@Override
			protected Object load() {
				String titleKey = featuredLinkModel.getObject().getId() == null ? "add" : "edit";
				return new StringResourceModel(titleKey, AddEditFeaturedLinkPanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		return new FeaturedLinkListPanel(AddEditFeaturedLinkPanel.this.getId());
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		FeaturedLink featuredLink = featuredLinkModel.getObject();

		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		RecordCollection collection = collectionAdminPanel.getCollection();
		featuredLink.setRecordCollection(collection);

		FeaturedLinkServices featuredLinkServices = ConstellioSpringUtils.getFeaturedLinkServices();
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		featuredLinkServices.makePersistent(featuredLink);
		entityManager.getTransaction().commit();
	}

}
