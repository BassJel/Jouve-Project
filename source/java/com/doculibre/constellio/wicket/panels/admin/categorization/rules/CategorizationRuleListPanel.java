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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.CategorizationRule;
import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.wicket.panels.admin.categorization.AddEditCategorizationPanel;
import com.doculibre.constellio.wicket.panels.admin.categorization.dto.CategorizationRuleDTO;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;

@SuppressWarnings("serial")
public class CategorizationRuleListPanel extends SingleColumnCRUDPanel {

	public CategorizationRuleListPanel(String id) {
		super(id);

		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AddEditCategorizationPanel addEditCategorizationPanel = (AddEditCategorizationPanel) findParent(AddEditCategorizationPanel.class);
				return addEditCategorizationPanel.getRules();
			}
		});
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddEditCategorizationRulePanel(id, new CategorizationRule(), null);
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
		AddEditCategorizationPanel addEditCategorizationPanel = (AddEditCategorizationPanel) findParent(AddEditCategorizationPanel.class);
		CategorizationRule categorizationRule = addEditCategorizationPanel.getRules().get(index).toCategorizationRule();
		return new AddEditCategorizationRulePanel(id, categorizationRule, index);
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		CategorizationRuleDTO categorizationRule = (CategorizationRuleDTO) entity;
		return categorizationRule.getMatchRegexp();
	}

	@Override
	protected WebMarkupContainer createDeleteLink(String id, final IModel entityModel, final int index) {
		return new AjaxLink(id) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				AddEditCategorizationPanel addEditCategorizationPanel = (AddEditCategorizationPanel) findParent(AddEditCategorizationPanel.class);
				addEditCategorizationPanel.getRules().remove(index);
				target.addComponent(CategorizationRuleListPanel.this);				
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator() {
				return new AjaxCallDecorator() {
					@Override
					public CharSequence decorateScript(CharSequence script) {
						String confirmMsg = getLocalizer().getString("confirmDelete", CategorizationRuleListPanel.this);
						return "if (confirm('" + confirmMsg + "')) {" + script + "} else { return false; }";
					}
				};
			}
		};
	}

	@Override
	protected IModel getTitleModel() {
		return null;
	}

	@Override
	protected BaseCRUDServices<? extends ConstellioEntity> getServices() {
		return null;
	}

}
