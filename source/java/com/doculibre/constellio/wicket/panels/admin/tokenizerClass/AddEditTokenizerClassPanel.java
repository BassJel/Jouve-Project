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
package com.doculibre.constellio.wicket.panels.admin.tokenizerClass;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.TokenizerClass;
import com.doculibre.constellio.services.ConnectorManagerServices;
import com.doculibre.constellio.services.TokenizerClassServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.tabs.AdminLeftMenuPanel;

@SuppressWarnings("serial")
public class AddEditTokenizerClassPanel extends SaveCancelFormPanel {
	
	private ReloadableEntityModel<TokenizerClass> tokenizerClassModel;
	
	private Component refreshComponent;

	public AddEditTokenizerClassPanel(String id, TokenizerClass tokenizerClass, Component refreshComponentP) {
		super(id, true);
		this.tokenizerClassModel = new ReloadableEntityModel<TokenizerClass>(tokenizerClass);
		this.refreshComponent = refreshComponentP;

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(tokenizerClassModel));

		TextField classNameField = new RequiredTextField("className");
		classNameField.add(new StringValidator.MaximumLengthValidator(255));
		form.add(classNameField);
	}

	@Override
	public void detachModels() {
		tokenizerClassModel.detach();
		super.detachModels();
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		TokenizerClass tokenizerClass = tokenizerClassModel.getObject();
		
		ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
		TokenizerClassServices tokenizerClassServices = ConstellioSpringUtils.getTokenizerClassServices();
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		
		ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
		tokenizerClass.setConnectorManager(connectorManager);
		tokenizerClassServices.makePersistent(tokenizerClass);
		entityManager.getTransaction().commit();
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			@Override
			protected Object load() {
				String titleKey = tokenizerClassModel.getObject().getId() == null ? "add" : "edit";
				return new StringResourceModel(titleKey, AddEditTokenizerClassPanel.this, null);
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
		if (refreshComponent != null) {
			TokenizerClass tokenizerClass = tokenizerClassModel.getObject();
			if (refreshComponent instanceof FormComponent) {
				refreshComponent.setModelObject(tokenizerClass);
			}
			target.addComponent(refreshComponent);
		} else {
			AdminLeftMenuPanel parent = (AdminLeftMenuPanel) findParent(AdminLeftMenuPanel.class);
			AddEditTokenizerClassPanel.this.replaceWith(new TokenizerClassListPanel(
					AddEditTokenizerClassPanel.this.getId()));
			target.addComponent(parent);
		}
	}

}
