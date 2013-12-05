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
package com.doculibre.constellio.wicket.panels.admin.analyzer.filter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.AnalyzerFilter;
import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.wicket.panels.admin.analyzer.AddEditAnalyzerFieldPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;

@SuppressWarnings("serial")
public class FilterListPanel extends SingleColumnCRUDPanel {

	public FilterListPanel(String id) {
		super(id);

		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AddEditAnalyzerFieldPanel addEditAnalyzerFieldPanel = (AddEditAnalyzerFieldPanel) findParent(AddEditAnalyzerFieldPanel.class);
				return addEditAnalyzerFieldPanel.getFilters();
			}
		});
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddEditFilterPanel(id, new AnalyzerFilter(), -1);
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
		AnalyzerFilter analyzerFilter = (AnalyzerFilter) entityModel.getObject();
		return new AddEditFilterPanel(id, analyzerFilter, index);
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		AnalyzerFilter filter = (AnalyzerFilter) entity;
		return filter.getFilterClass().getClassName();
	}

	@Override
	protected WebMarkupContainer createDeleteLink(String id, final IModel entityModel, final int index) {
		return new AjaxLink(id) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				AddEditAnalyzerFieldPanel addEditAnalyzerFieldPanel = (AddEditAnalyzerFieldPanel) findParent(AddEditAnalyzerFieldPanel.class);
				addEditAnalyzerFieldPanel.getFilters().remove(index);
				target.addComponent(FilterListPanel.this);				
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator() {
				return new AjaxCallDecorator() {
					@Override
					public CharSequence decorateScript(CharSequence script) {
						String confirmMsg = getLocalizer().getString("confirmDelete", FilterListPanel.this);
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
