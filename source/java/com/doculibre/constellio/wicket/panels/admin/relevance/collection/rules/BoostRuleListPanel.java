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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.relevance.BoostRule;
import com.doculibre.constellio.wicket.models.SortableListModel;
import com.doculibre.constellio.wicket.panels.admin.crud.CRUDPanel;
import com.doculibre.constellio.wicket.panels.admin.relevance.collection.AddEditRecordCollectionBoostPanel;
import com.doculibre.constellio.wicket.panels.admin.relevance.collection.dto.BoostRuleDTO;

@SuppressWarnings("serial")
public class BoostRuleListPanel extends CRUDPanel<BoostRuleDTO> {
	
	public BoostRuleListPanel(String id) {
		super(id, 10);

		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AddEditRecordCollectionBoostPanel addEditRecordCollectionBoostPanel = (AddEditRecordCollectionBoostPanel) findParent(AddEditRecordCollectionBoostPanel.class);
				return addEditRecordCollectionBoostPanel.getRules();
			}
		});
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddEditBoostRulePanel(id, new BoostRule(), null);
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
		AddEditRecordCollectionBoostPanel addEditRecordCollectionBoostPanel = (AddEditRecordCollectionBoostPanel) findParent(AddEditRecordCollectionBoostPanel.class);
		BoostRule boostRule = addEditRecordCollectionBoostPanel.getRules().get(index).toBoostRule();
		return new AddEditBoostRulePanel(id, boostRule, index);
	}

	@Override
	protected List<IColumn> getDataColumns() {
		List<IColumn> dataColumns = new ArrayList<IColumn>();

		dataColumns.add(new PropertyColumn(new StringResourceModel("regex", this, null), "regex"));
		dataColumns.add(new PropertyColumn(new StringResourceModel("boost", this, null), "boost"));

		return dataColumns;
	}

	@Override
	protected void onClickDeleteLink(IModel rowItemModel,
			AjaxRequestTarget target, int index) {
		AddEditRecordCollectionBoostPanel addEditRecordCollectionBoostPanel = (AddEditRecordCollectionBoostPanel) findParent(AddEditRecordCollectionBoostPanel.class);
		addEditRecordCollectionBoostPanel.getRules().remove(index);
		target.addComponent(BoostRuleListPanel.this);	
	}

	@Override
	protected SortableListModel<BoostRuleDTO> getSortableListModel() {
		return new SortableListModel<BoostRuleDTO>() {
			@Override
			protected List<BoostRuleDTO> load(String orderByProperty,
					Boolean orderByAsc) {
				AddEditRecordCollectionBoostPanel addEditRecordCollectionBoostPanel = (AddEditRecordCollectionBoostPanel) findParent(AddEditRecordCollectionBoostPanel.class);
				return addEditRecordCollectionBoostPanel.getRules();
			}
		};
	}
	
}
