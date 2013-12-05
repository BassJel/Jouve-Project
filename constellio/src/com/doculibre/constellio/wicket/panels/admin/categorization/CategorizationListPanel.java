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
package com.doculibre.constellio.wicket.panels.admin.categorization;

import java.util.ArrayList;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.Categorization;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;

@SuppressWarnings("serial")
public class CategorizationListPanel extends SingleColumnCRUDPanel {

	public CategorizationListPanel(String id) {
		super(id);
		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				return new ArrayList<Categorization>(collection.getCategorizations());
			}
		});
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		Categorization categorization = new Categorization();
		return new AddEditCategorizationPanel(id, categorization);
	}

	@Override
	protected BaseCRUDServices<Categorization> getServices() {
		return ConstellioSpringUtils.getCategorizationServices();
	}

	@Override
	protected WebMarkupContainer createEditContent(String id,
			IModel entityModel, int index) {
		Categorization categorization = (Categorization) entityModel.getObject();
		return new AddEditCategorizationPanel(id, categorization);
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		Categorization categorization = (Categorization) entity;
		return categorization.getName();
	}

}
