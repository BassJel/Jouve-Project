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
package com.doculibre.constellio.wicket.panels.admin.analyzerClass;

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

import com.doculibre.constellio.entities.AnalyzerClass;
import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.services.AnalyzerClassServices;
import com.doculibre.constellio.services.ConnectorManagerServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;

@SuppressWarnings("serial")
public class AddEditAnalyzerClassPanel extends SaveCancelFormPanel {
	
	private ReloadableEntityModel<AnalyzerClass> analyzerClassModel;
	
	private Component refreshComponent;

	private boolean isCreation;
	
	public AddEditAnalyzerClassPanel(String id, AnalyzerClass analyzerClass, Component refreshComponentP) {
		super(id, true);
		this.analyzerClassModel = new ReloadableEntityModel<AnalyzerClass>(analyzerClass);
		this.refreshComponent = refreshComponentP;
		this.isCreation = analyzerClass.getId() == null;
		
		Form form = getForm();
		form.setModel(new CompoundPropertyModel(analyzerClassModel));
		form.add(new SetFocusBehavior(form));

		TextField classNameField = new RequiredTextField("className");
		classNameField.add(new StringValidator.MaximumLengthValidator(255));
		form.add(classNameField);
	}

	@Override
	public void detachModels() {
		analyzerClassModel.detach();
		super.detachModels();
	}

	@Override
	protected IModel getTitleModel() {
      return new LoadableDetachableModel() {
			@Override
			protected Object load() {
		        String titleKey = isCreation ? "add" : "edit";
		        return new StringResourceModel(titleKey, AddEditAnalyzerClassPanel.this, null).getObject();
			}
      };
	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		AnalyzerClass analyzerClass = analyzerClassModel.getObject();
		
		ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
		AnalyzerClassServices analyzerClassServices = ConstellioSpringUtils.getAnalyzerClassServices();
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		
		ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
		analyzerClass.setConnectorManager(connectorManager);
		analyzerClassServices.makePersistent(analyzerClass);
		entityManager.getTransaction().commit();
		
	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		if (refreshComponent != null) {
			AnalyzerClass analyzerClass = analyzerClassModel.getObject();
			if (refreshComponent instanceof FormComponent) {
				refreshComponent.setModelObject(analyzerClass);
			}
			target.addComponent(refreshComponent);
		} else {
			AnalyzerClassListPanel analyzerClassListPanel = (AnalyzerClassListPanel) findParent(AnalyzerClassListPanel.class);
			target.addComponent(analyzerClassListPanel);
		}

	}

}
