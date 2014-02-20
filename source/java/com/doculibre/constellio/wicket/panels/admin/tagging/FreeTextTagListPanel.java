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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.FreeTextTagServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;

@SuppressWarnings("serial")
public class FreeTextTagListPanel extends SingleColumnCRUDPanel {

	public FreeTextTagListPanel(String id) {
		super(id);
		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
			    FreeTextTagServices taggingServices = ConstellioSpringUtils.getFreeTextTagServices();
				return taggingServices.list();
			}
		});
	}
	
	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddEditFreeTextTagPanel(id, new FreeTextTag(), null);
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
	    FreeTextTag freeTextTag = (FreeTextTag) entityModel.getObject();
		return new AddEditFreeTextTagPanel(id, freeTextTag, null);
	}

	@Override
	protected String getDetailsLabel(Object entity) {
	    FreeTextTag freeTextTag = (FreeTextTag) entity;
		return freeTextTag.getFreeText();
	}

	@Override
	protected BaseCRUDServices<FreeTextTag> getServices() {
		return ConstellioSpringUtils.getFreeTextTagServices();
	}

}
