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
package com.doculibre.constellio.wicket.panels.admin.tokenizerClass;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.TokenizerClass;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.TokenizerClassServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;

@SuppressWarnings("serial")
public class TokenizerClassListPanel extends SingleColumnCRUDPanel {

	public TokenizerClassListPanel(String id) {
		super(id);
		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				TokenizerClassServices tokenizerClassServices = ConstellioSpringUtils.getTokenizerClassServices();
				return tokenizerClassServices.list();
			}
		});
	}
	
	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddEditTokenizerClassPanel(id, new TokenizerClass(), null);
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
		TokenizerClass tokenizerClass = (TokenizerClass) entityModel.getObject();
		return new AddEditTokenizerClassPanel(id, tokenizerClass, null);
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		TokenizerClass tokenizerClass = (TokenizerClass) entity;
		return tokenizerClass.getClassName();
	}

	@Override
	protected BaseCRUDServices<TokenizerClass> getServices() {
		return ConstellioSpringUtils.getTokenizerClassServices();
	}

}
