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
package com.doculibre.constellio.wicket.panels.admin.group.participation;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.GroupParticipation;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;
import com.doculibre.constellio.wicket.panels.admin.group.AddEditGroupPanel;

@SuppressWarnings("serial")
public class ParticipationListPanel extends SingleColumnCRUDPanel {

	public ParticipationListPanel(String id) {
		super(id);
		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AddEditGroupPanel addEditGroupPanel = (AddEditGroupPanel) findParent(AddEditGroupPanel.class);
				return addEditGroupPanel.getParticipations();
			}
		});
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddEditParticipationPanel(id, new GroupParticipation());
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
		GroupParticipation participation = (GroupParticipation) entityModel.getObject();
		return new AddEditParticipationPanel(id, participation);
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		GroupParticipation participation = (GroupParticipation) entity;
		ConstellioUser user = participation.getConstellioUser();
		UserServices userServices = ConstellioSpringUtils.getUserServices();
		// Reload
		user = userServices.get(user.getId());
		return user.getFirstName() + " " + user.getLastName() + " (" + user.getUsername() + ")";
	}

	@Override
	protected WebMarkupContainer createDeleteLink(String id, final IModel entityModel, final int index) {
		return new AjaxLink(id) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				AddEditGroupPanel addEditGroupPanel = (AddEditGroupPanel) findParent(AddEditGroupPanel.class);
				addEditGroupPanel.getParticipations().remove(index);
				target.addComponent(ParticipationListPanel.this);				
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator() {
				return new AjaxCallDecorator() {
					@Override
					public CharSequence decorateScript(CharSequence script) {
						String confirmMsg = getLocalizer().getString("confirmDelete", ParticipationListPanel.this);
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

    @Override
    protected boolean isEditColumn() {
        return false;
    }

}
