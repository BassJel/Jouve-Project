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
package com.doculibre.constellio.wicket.panels.admin.relevance.collection.rules;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.doculibre.constellio.entities.relevance.BoostRule;
import com.doculibre.constellio.helper.connector.type.form.element.FormElementValidationService;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.relevance.collection.AddEditRecordCollectionBoostPanel;
import com.doculibre.constellio.wicket.panels.admin.relevance.collection.dto.BoostRuleDTO;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class AddEditBoostRulePanel  extends SaveCancelFormPanel {

	private Integer boostRuleListPosition;
	private BoostRuleDTO boostRuleDTO;
	
	public AddEditBoostRulePanel(String id, BoostRule boostRule, Integer boostRuleListPosition) {
		super(id, true);
		
		this.boostRuleListPosition = boostRuleListPosition;
		this.boostRuleDTO = new BoostRuleDTO(boostRule);

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(boostRuleDTO));// pr eviter les repetitions

		TextField regex	= new TextField("regex");// grace a setModel plus besoin de : new PropertyModel(this, "regex")
		regex.add(new IValidator(){
			@Override
			public void validate(IValidatable validatable) {
				String regex = (String) validatable.getValue();
				String errorMessage = FormElementValidationService
						.validateContent(new StringBuilder(regex),
								FormElementValidationService.ContentType.REGEX,
								ConstellioSession.get().getLocale());
				if (!errorMessage.isEmpty()) {
					validatable.error(new ValidationError()
							.setMessage(errorMessage));
				}
			}
			
		});
		form.add(regex);
		
		TextField boost	= new TextField("boost", Double.class);
		boost.setRequired(true);
		form.add(boost);
	}
	
	@Override
	protected void onSave(AjaxRequestTarget target) {
		AddEditRecordCollectionBoostPanel addEditRecordCollectionBoostPanel = (AddEditRecordCollectionBoostPanel) findParent(AddEditRecordCollectionBoostPanel.class);
		if (boostRuleListPosition != null) {
			addEditRecordCollectionBoostPanel.getRules().set(boostRuleListPosition, boostRuleDTO);
		} else {
			addEditRecordCollectionBoostPanel.getRules().add(boostRuleDTO);
		}
	}
	
	
	//appelé a chaque fois que je fais modif
	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		boostRuleDTO.detach();
		super.defaultReturnAction(target);
		// pr dire a la mere de se raffraichir
		target.addComponent(this.findParent(BoostRuleListPanel.class));
	}
	
	@Override
	protected IModel getTitleModel() {
		return new Model();
	}
	
	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	public void detachModels() {
		boostRuleDTO.detach();
		super.detachModels();
	}

}
