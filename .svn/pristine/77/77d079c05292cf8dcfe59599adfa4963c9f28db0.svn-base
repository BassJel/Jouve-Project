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
package com.doculibre.constellio.wicket.panels.admin.resultPanels;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.models.SortableListModel;
import com.doculibre.constellio.wicket.panels.admin.crud.CRUDPanel;
import com.doculibre.constellio.wicket.panels.results.DefaultSearchResultPanel;

@SuppressWarnings("serial")
public class ConnectorResultClassesListPanel extends CRUDPanel<ConnectorInstance> {

	private ReloadableEntityModel<RecordCollection> collectionModel;
	
	public ConnectorResultClassesListPanel(String id, RecordCollection collection) {
		super(id);
		this.collectionModel = new ReloadableEntityModel<RecordCollection>(collection);
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new WebMarkupContainer(id);
	}

	@Override
	protected WebMarkupContainer createEditContent(String id,
			IModel rowItemModel, int index) {
		ConnectorInstance connector = (ConnectorInstance) rowItemModel.getObject();
		return new AddEditResultClassPanel(id, connector);
	}

	@Override
	protected List<IColumn> getDataColumns() {
		List<IColumn> columns = new ArrayList<IColumn>();
		columns.add(new PropertyColumn(new StringResourceModel("displayName", this, null), "displayName"));
		
		columns.add(new AbstractColumn(new StringResourceModel("class", this, null)) {
			@Override
			public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
				cellItem.add(new Label(componentId, new LoadableDetachableModel() {
					@Override
					protected Object load() {
						ConnectorInstance connector = (ConnectorInstance) rowModel.getObject();
						String clazz = null;
						if (connector.getSearchResultPanelClassName() != null ) {
							clazz = connector.getSearchResultPanelClassName();
						} else if (connector.getConnectorType().getSearchResultPanelClassName() != null) {
							clazz = connector.getConnectorType().getSearchResultPanelClassName();
						} else {
							clazz = DefaultSearchResultPanel.class.getCanonicalName();
						}
						return clazz;
					}
				}));
			}
		});
		return columns;
	}

	@Override
	protected void onClickDeleteLink(IModel rowItemModel,
			AjaxRequestTarget target, int index) {
		
	}

	@Override
	protected SortableListModel<ConnectorInstance> getSortableListModel() {
		return new SortableListModel<ConnectorInstance>() {

			@Override
			protected List<ConnectorInstance> load(String orderByProperty,
					Boolean orderByAsc) {
				List<ConnectorInstance> connectors = new ArrayList<ConnectorInstance>();
				connectors.addAll(collectionModel.getObject().getConnectorInstances());
				return connectors;
			}
		};
	}

	@Override
	protected boolean isAddLink() {
		return false;
	}

	@Override
	protected IColumn getDeleteLinkColumn() {
		return null;
	}

}
