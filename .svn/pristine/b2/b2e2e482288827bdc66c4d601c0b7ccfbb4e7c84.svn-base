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
package com.doculibre.constellio.wicket.panels.admin.collectionPermission;

import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.CollectionPermission;
import com.doculibre.constellio.entities.ConstellioGroup;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.CollectionPermissionServices;
import com.doculibre.constellio.services.GroupServices;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class AddEditCollectionPermissionPanel extends SaveCancelFormPanel {

	private ReloadableEntityModel<CollectionPermission> collectionPermissionModel;

	private DropDownChoice userField;
	private DropDownChoice groupField;
	
	private boolean isCreation;
	
	public AddEditCollectionPermissionPanel(String id, CollectionPermission collectionPermission) {
		super(id, true);
		this.collectionPermissionModel = new ReloadableEntityModel<CollectionPermission>(collectionPermission);
		this.isCreation = collectionPermission.getId() == null;
		
		Form form = getForm();
		form.setModel(new CompoundPropertyModel(collectionPermissionModel));
		
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
		userField = new DropDownChoice("constellioUser", usersModel, userRenderer);
		form.add(userField);
		userField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.addComponent(groupField);
			}
		});
		
		IModel groupsModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				GroupServices groupServices = ConstellioSpringUtils.getGroupServices();
				return new ArrayList<ConstellioGroup>(groupServices.list());
			}
		};
		IChoiceRenderer groupRenderer = new ChoiceRenderer("name");
		groupField = new DropDownChoice("constellioGroup", groupsModel, groupRenderer);
		form.add(groupField);
		groupField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.addComponent(userField);
			}
		});
		
		form.add(new CheckBox("search"));
        form.add(new CheckBox("collaboration"));
		form.add(new CheckBox("admin"));
		
		form.add(new AbstractFormValidator() {
			@Override
			public void validate(Form form) {
				CollectionPermission collectionPermission = collectionPermissionModel.getObject();
				if (collectionPermission.getConstellioUser() == null && 
						collectionPermission.getConstellioGroup() == null) {
					error(userField, "userOrGroupRequired");
					error(groupField, "userOrGroupRequired");
				}
			}
			
			@Override
			public FormComponent[] getDependentFormComponents() {
				return null;
			}
		});

	}

	@Override
	public void detachModels() {
		collectionPermissionModel.detach();
		super.detachModels();
	}

	@Override
	protected IModel getTitleModel() {
        return new LoadableDetachableModel() {
			@Override
			protected Object load() {
		        String titleKey = isCreation ? "add" : "edit";
		        return new StringResourceModel(titleKey, AddEditCollectionPermissionPanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		RecordCollection collection = collectionAdminPanel.getCollection();
		
		CollectionPermission collectionPermission = collectionPermissionModel.getObject();
		collectionPermission.setRecordCollection(collection);

		CollectionPermissionServices collectionPermissionServices = ConstellioSpringUtils.getCollectionPermissionServices();
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		collectionPermissionServices.makePersistent(collectionPermission);
		entityManager.getTransaction().commit();
	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		target.addComponent(collectionAdminPanel);
	}

}
