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
package com.doculibre.constellio.wicket.panels.admin.categorization.rules.values;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.categorization.rules.AddEditCategorizationRulePanel;

@SuppressWarnings("serial")
public class AddEditCategorizationRuleValuePanel extends SaveCancelFormPanel {
	
	private String value;

	private int index;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public AddEditCategorizationRuleValuePanel(String id, final int index, String initialValue) {
		super(id, true);
		this.index = index;
		this.value = initialValue;
		
		TextField valueField = new RequiredTextField("value", new PropertyModel(this, "value"));
		getForm().add(valueField);
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			
			@Override
			protected Object load() {
		        String titleKey = index == -1 ? "add" : "edit";
		        return new StringResourceModel(titleKey, AddEditCategorizationRuleValuePanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		AddEditCategorizationRulePanel addEditCategorizationRulePanel = 
			(AddEditCategorizationRulePanel) findParent(AddEditCategorizationRulePanel.class);
		if (index == -1) {
			addEditCategorizationRulePanel.getValues().add(value);
		} else {
			addEditCategorizationRulePanel.getValues().set(index, value);
		}
	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		CategorizationRuleValueListPanel categorizationRuleListPanel = (CategorizationRuleValueListPanel) findParent(CategorizationRuleValueListPanel.class);
		target.addComponent(categorizationRuleListPanel);
	}

}
