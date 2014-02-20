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
package com.doculibre.constellio.wicket.panels.admin.tagging;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.services.FreeTextTagServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.tabs.AdminLeftMenuPanel;

@SuppressWarnings("serial")
public class AddEditFreeTextTagPanel extends AjaxPanel {
	
	private ReloadableEntityModel<FreeTextTag> freeTextTagModel;
	
	private Component refreshComponent;

	public AddEditFreeTextTagPanel(String id, FreeTextTag freeTextTag, Component refreshComponentP) {
		super(id);
		this.freeTextTagModel = new ReloadableEntityModel<FreeTextTag>(freeTextTag);
		this.refreshComponent = refreshComponentP;

		add(new FeedbackPanel("feedback"));
		
		Form form = new Form("form", new CompoundPropertyModel(freeTextTagModel));
		add(form);
		form.add(new SetFocusBehavior(form));

		String titleKey = freeTextTag.getId() == null ? "add" : "edit";
		IModel titleModel = new StringResourceModel(titleKey, this, null);
		form.add(new Label("title", titleModel));

		TextField freeTextField = new RequiredTextField("freeText");
		freeTextField.add(new StringValidator.MaximumLengthValidator(255));
		form.add(freeTextField);
		
		Button submitButton = new AjaxButton("submitButton") {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {
			    FreeTextTag freeTextTag = freeTextTagModel.getObject();
				
				FreeTextTagServices taggingServices = ConstellioSpringUtils.getFreeTextTagServices();
				EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
				if (!entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().begin();
				}
				
				taggingServices.makePersistent(freeTextTag);
				entityManager.getTransaction().commit();

				ModalWindow modal = (ModalWindow) findParent(ModalWindow.class);
				if (modal != null) {
					modal.close(target);
				}
				if (refreshComponent != null) {
					if (refreshComponent instanceof FormComponent) {
						refreshComponent.setModelObject(freeTextTag);
					}
					target.addComponent(refreshComponent);
				} else {
					AdminLeftMenuPanel parent = (AdminLeftMenuPanel) findParent(AdminLeftMenuPanel.class);
					AddEditFreeTextTagPanel.this.replaceWith(new FreeTextTagListPanel(
							AddEditFreeTextTagPanel.this.getId()));
					target.addComponent(parent);
				}
			}

		};
		form.add(submitButton);

		Button cancelButton = new AjaxButton("cancelButton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {
				ModalWindow modal = (ModalWindow) findParent(ModalWindow.class);
				if (modal != null) {
					modal.close(target);
				}
				if (refreshComponent != null) {
					target.addComponent(refreshComponent);
				} else {
					AdminLeftMenuPanel parent = (AdminLeftMenuPanel) findParent(AdminLeftMenuPanel.class);
					AddEditFreeTextTagPanel.this.replaceWith(new FreeTextTagListPanel(
							AddEditFreeTextTagPanel.this.getId()));
					target.addComponent(parent);
				}
			}
		}.setDefaultFormProcessing(false);
		form.add(cancelButton);
	}

	@Override
	public void detachModels() {
		freeTextTagModel.detach();
		super.detachModels();
	}

}
