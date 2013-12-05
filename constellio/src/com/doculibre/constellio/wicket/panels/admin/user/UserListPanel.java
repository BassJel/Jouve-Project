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
package com.doculibre.constellio.wicket.panels.admin.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class UserListPanel extends SingleColumnCRUDPanel {

	public UserListPanel(String id) {
		super(id);
		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				UserServices userServices = ConstellioSpringUtils.getUserServices();
				List<ConstellioUser> users = new ArrayList<ConstellioUser>(userServices.list());
				return users;
			}
		});
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddEditUserPanel(id, new ConstellioUser());
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
		ConstellioUser user = (ConstellioUser) entityModel.getObject();
		return new AddEditUserPanel(id, user);
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		ConstellioUser user = (ConstellioUser) entity;
		String details = user.getFirstName() + " " + user.getLastName() + " (" + user.getUsername() + ")";
		return details;
	}

	@Override
	protected BaseCRUDServices<ConstellioUser> getServices() {
		return ConstellioSpringUtils.getUserServices();
	}

    @Override
    protected boolean isDeleteLink(IModel rowItemModel, int index) {
        boolean visible = super.isDeleteLink(rowItemModel, index);
        if (visible) {
            ConstellioUser user = (ConstellioUser) rowItemModel.getObject();
            ConstellioUser currentUser = ConstellioSession.get().getUser();
            visible = !user.equals(currentUser);
        }
        return visible;
    }

}
