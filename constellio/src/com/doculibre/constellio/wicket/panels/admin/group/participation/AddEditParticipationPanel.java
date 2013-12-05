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

import java.util.ArrayList;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.GroupParticipation;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.group.AddEditGroupPanel;

@SuppressWarnings("serial")
public class AddEditParticipationPanel extends SaveCancelFormPanel {

	private ReloadableEntityModel<GroupParticipation> participationModel;
	
	public AddEditParticipationPanel(String id, GroupParticipation participation) {
		super(id, true);
		this.participationModel = new ReloadableEntityModel<GroupParticipation>(participation);
		
		Form form = getForm();
		form.setModel(new CompoundPropertyModel(participationModel));

		IModel usersModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				UserServices userServices = ConstellioSpringUtils.getUserServices();
				return new ArrayList<ConstellioUser>(userServices.list());
			}
		};

		IChoiceRenderer userRenderer = new ChoiceRenderer() {
			@Override
			public Object getDisplayValue(Object object) {
				ConstellioUser user = (ConstellioUser) object;
				return user.getFirstName() + " " + user.getLastName() + " (" + user.getUsername() + ")";
			}
		};
		
		DropDownChoice userField = new DropDownChoice("constellioUser", usersModel, userRenderer);
		userField.setRequired(true);
		form.add(userField);

	}

	@Override
	public void detachModels() {
		participationModel.detach();
		super.detachModels();
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			@Override
			protected Object load() {
		        String titleKey = participationModel.getObject().getId() == null ? "add" : "edit";
		        return new StringResourceModel(titleKey, AddEditParticipationPanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		AddEditGroupPanel addEditGroupPanel = (AddEditGroupPanel) findParent(AddEditGroupPanel.class);
		GroupParticipation participation = participationModel.getObject();
		if (participation.getId() != null) {
			addEditGroupPanel.getParticipations().remove(participation);
		}
		addEditGroupPanel.getParticipations().add(participation);		
	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		ParticipationListPanel participationListPanel = (ParticipationListPanel) findParent(ParticipationListPanel.class);
		target.addComponent(participationListPanel);
	}

}
