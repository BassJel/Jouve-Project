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
package com.doculibre.constellio.wicket.panels.admin.categorization.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.CategorizationRule;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.wicket.models.admin.AdminCollectionIndexFieldsModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.categorization.AddEditCategorizationPanel;
import com.doculibre.constellio.wicket.panels.admin.categorization.dto.CategorizationRuleDTO;
import com.doculibre.constellio.wicket.panels.admin.categorization.rules.values.CategorizationRuleValueListPanel;

@SuppressWarnings("serial")
public class AddEditCategorizationRulePanel extends SaveCancelFormPanel {

	private CategorizationRuleDTO categorizationRuleDTO;
	
	private List<String> values = new ArrayList<String>();

	private DropDownChoice indexField;
	
	private TextField matchRegexp;
	
	private Integer categorizationRuleListPosition;
	
	public AddEditCategorizationRulePanel(String id, CategorizationRule categorizationRule, Integer categorizationRuleListPosition) {
		super(id, true);
		this.categorizationRuleDTO = new CategorizationRuleDTO(categorizationRule);
		this.categorizationRuleListPosition = categorizationRuleListPosition;
		values.addAll(categorizationRule.getMatchRegexpIndexedValues());
		
		Form form = getForm();
		form.setModel(new CompoundPropertyModel(categorizationRuleDTO));

		IModel indexFieldsModel = new AdminCollectionIndexFieldsModel(this);

		IChoiceRenderer indexFieldRenderer = new ChoiceRenderer() {
			@Override
			public Object getDisplayValue(Object object) {
				IndexField indexField = (IndexField) object;
				return indexField.getName();
			}
		};
		
		indexField = new DropDownChoice("indexField", indexFieldsModel, indexFieldRenderer);
		indexField.setRequired(true);
		indexField.setLabel(new StringResourceModel("sourceIndexField", this, null));
		form.add(indexField);

		matchRegexp = new RequiredTextField("matchRegexp");
		form.add(matchRegexp);
		
		form.add(new CategorizationRuleValueListPanel("matchRegexpIndexedValues"));
	}

	@Override
	public void detachModels() {
		categorizationRuleDTO.detach();
		super.detachModels();
	}

	public List<String> getValues() {
		return values;
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			
			@Override
			protected Object load() {
		        String titleKey = categorizationRuleDTO.getId() == null ? "add" : "edit";
		        return new StringResourceModel(titleKey, AddEditCategorizationRulePanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		AddEditCategorizationPanel addEditCategorizationPanel = (AddEditCategorizationPanel) findParent(AddEditCategorizationPanel.class);

		categorizationRuleDTO.getMatchRegexpIndexedValues().clear();
		categorizationRuleDTO.getMatchRegexpIndexedValues().addAll(values);
		
		if (categorizationRuleListPosition != null) {
			addEditCategorizationPanel.getRules().set(categorizationRuleListPosition, categorizationRuleDTO);
		} else {
			addEditCategorizationPanel.getRules().add(categorizationRuleDTO);
		}
	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		categorizationRuleDTO.detach();
		super.defaultReturnAction(target);
		CategorizationRuleListPanel categorizationRuleListPanel = (CategorizationRuleListPanel) findParent(CategorizationRuleListPanel.class);
		target.addComponent(categorizationRuleListPanel);
		indexField.setRequired(false);
		matchRegexp.setRequired(false);
	}

}
