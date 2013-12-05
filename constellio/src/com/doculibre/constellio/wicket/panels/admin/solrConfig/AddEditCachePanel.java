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
package com.doculibre.constellio.wicket.panels.admin.solrConfig;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.lang.PropertyResolver;

import com.doculibre.constellio.entities.Cache;
import com.doculibre.constellio.entities.SolrConfig;
import com.doculibre.constellio.services.SolrConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;

@SuppressWarnings("serial")
public class AddEditCachePanel extends SaveCancelFormPanel {

	private static final String LRU_CACHE = "solr.search.LRUCache";
	private static final String FAST_LRU_CACHE = "solr.search.FastLRUCache";
	
	private ReloadableEntityModel<Cache> cacheModel;
	private IModel solrConfigModel;

	private boolean isEdit;
	private String property;

	public AddEditCachePanel(String id, IModel solrConfigModel, String property) {
		super(id, true);
		Cache cache = (Cache) PropertyResolver.getValue(property,
				solrConfigModel.getObject());
		if (cache == null) {
			cache = new Cache();
		} else {
			isEdit = true;
		}
		this.property = property;
		this.cacheModel = new ReloadableEntityModel<Cache>(cache);
		this.solrConfigModel = solrConfigModel;

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(cacheModel));
		
		final List<String> cacheClasses = Arrays.asList(new String[] {LRU_CACHE, FAST_LRU_CACHE});
		
		if (cache.getCacheClass() == null) {
			cache.setCacheClass(FAST_LRU_CACHE);
		}
		
		DropDownChoice classTextField = new DropDownChoice("cacheClass", cacheClasses);
		classTextField.setRequired(true);
		classTextField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.addComponent(getForm());
			}
		});
		TextField sizeTextField = new RequiredTextField("size", Long.class);
		TextField initialSizeTextField = new RequiredTextField("initialSize",
				Long.class);
		TextField autowarmCountTextField = new RequiredTextField("autowarmCount",
				Double.class);
		TextField minSizeTextField = new TextField("minSize",
				Long.class) {
			
			@Override
			public boolean isVisible() {
				return FAST_LRU_CACHE.equals(cacheModel.getObject().getCacheClass());
			}
			
		};
		TextField acceptableSizeTextField = new TextField("acceptableSize",
				Long.class) {
			
			@Override
			public boolean isVisible() {
				return FAST_LRU_CACHE.equals(cacheModel.getObject().getCacheClass());
			}
			
		};
		CheckBox cleanupThreadCheckBox = new CheckBox("cleanupThread") {
			
			@Override
			public boolean isVisible() {
				return FAST_LRU_CACHE.equals(cacheModel.getObject().getCacheClass());
			}
			
		};
		TextField regeneratorClassTextField = new TextField("regeneratorClass",
				String.class) {
			
			@Override
			public boolean isVisible() {
				//User's custom cache are not supported
				return false;
			}
			
		};
		
		form.add(classTextField);
		form.add(sizeTextField);
		form.add(initialSizeTextField);
		form.add(autowarmCountTextField);
		form.add(minSizeTextField);
		form.add(acceptableSizeTextField);
		form.add(cleanupThreadCheckBox);
		form.add(regeneratorClassTextField);
		
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		SolrConfig config = ((SolrConfig)solrConfigModel.getObject());
		if (!isEdit) {
			Cache cache = cacheModel.getObject();
			PropertyResolver.setValue(property, config, cache, null);
		}
		EntityManager entityManager = ConstellioPersistenceContext
				.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		SolrConfigServices mySolrConfigServices = ConstellioSpringUtils.getSolrConfigServices();
		mySolrConfigServices.makePersistent(config);
		entityManager.getTransaction().commit();
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {

			@Override
			protected Object load() {
				String titleKey = !isEdit ? "add" : "edit";
				IModel titleModel = new StringResourceModel(titleKey,
						AddEditCachePanel.this, null);
				return titleModel.getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		SolrConfigPanel configPanel = (SolrConfigPanel) findParent(SolrConfigPanel.class);
		target.addComponent(configPanel);
	}

	@Override
	public void detachModels() {
		super.detachModels();
		this.cacheModel.detach();
		this.solrConfigModel.detach();
	}

}
