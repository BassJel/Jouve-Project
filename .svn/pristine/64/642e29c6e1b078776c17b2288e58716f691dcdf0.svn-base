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
package com.doculibre.constellio.wicket.panels.admin.group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioGroup;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.GroupParticipation;
import com.doculibre.constellio.services.GroupServices;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.group.participation.ParticipationListPanel;
import com.doculibre.constellio.wicket.panels.admin.server.AdminServerPanel;

@SuppressWarnings("serial")
public class AddEditGroupPanel extends SaveCancelFormPanel {

	private ReloadableEntityModel<ConstellioGroup> groupModel;

	private List<GroupParticipation> participations = new ArrayList<GroupParticipation>();

	public AddEditGroupPanel(String id, ConstellioGroup group) {
		super(id, true);
		this.groupModel = new ReloadableEntityModel<ConstellioGroup>(group);
		participations.addAll(group.getParticipations());

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(groupModel));

		form.add(new ParticipationListPanel("participationsPanel"));
		
		TextField name = new RequiredTextField("name");
		form.add(name);
	}

	@Override
	public void detachModels() {
		groupModel.detach();
		super.detachModels();
	}

	public List<GroupParticipation> getParticipations() {
		return participations;
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			
			@Override
			protected Object load() {
				String titleKey = groupModel.getObject().getId() == null ? "add" : "edit";
				return new StringResourceModel(titleKey, AddEditGroupPanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		ConstellioGroup group = groupModel.getObject();

		GroupServices groupServices = ConstellioSpringUtils.getGroupServices();
		UserServices userServices = ConstellioSpringUtils.getUserServices();
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}

		Set<GroupParticipation> previousParticipations = group.getParticipations();
		List<GroupParticipation> newParticipations = getParticipations();
		for (Iterator<GroupParticipation> it = previousParticipations.iterator(); it.hasNext();) {
			GroupParticipation participation = it.next();
			if (!newParticipations.contains(participation)) {
				// Workaround
				it.remove();
			}
		}
		for (Iterator<GroupParticipation> it = newParticipations.iterator(); it.hasNext();) {
			GroupParticipation participation = it.next();
			if (!previousParticipations.contains(participation)) {
				participation.setConstellioGroup(group);
				group.getParticipations().add(participation);
				ConstellioUser user = participation.getConstellioUser();
				// Reload
				user = userServices.get(user.getId());
				participation.setConstellioUser(user);
			}
		}

		groupServices.makePersistent(group);
		entityManager.getTransaction().commit();		
	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		AdminServerPanel serverAdminPanel = (AdminServerPanel) findParent(AdminServerPanel.class);
		target.addComponent(serverAdminPanel);
	}


}
