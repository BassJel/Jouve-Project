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
package com.doculibre.constellio.wicket.panels.admin.indexField.copyField;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.models.EntityModel;
import com.doculibre.constellio.wicket.models.admin.AdminCollectionIndexFieldsModel;
import com.doculibre.constellio.wicket.panels.admin.indexField.AddEditIndexFieldPanel;
import com.doculibre.constellio.wicket.panels.admin.indexField.dto.CopyFieldDTO;

@SuppressWarnings("serial")
public class AddEditCopyFieldPanel extends Panel {
	
	private EntityModel<CopyField> copyFieldModel;

	public AddEditCopyFieldPanel(String id, final int index, CopyField copyField, final boolean dest) {
		super(id);
		this.copyFieldModel = new EntityModel<CopyField>(copyField);

		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);
		
		Form form = new Form("form", new CompoundPropertyModel(copyFieldModel));
		add(form);
		form.add(new SetFocusBehavior(form));

        String titleKey = copyField.getId() == null ? "add" : "edit";
        IModel titleModel = new StringResourceModel(titleKey, this, null);
        form.add(new Label("title", titleModel));
		
		IModel indexFieldsModel = new AdminCollectionIndexFieldsModel(this) {
			@Override
			protected boolean accept(IndexField potentialIndexField) {
				boolean accept;
				AddEditIndexFieldPanel addEditIndexFieldPanel = 
					(AddEditIndexFieldPanel) findParent(AddEditIndexFieldPanel.class);
				IndexField indexField = addEditIndexFieldPanel.getIndexField();
				if (indexField.getId() != null) {
					accept = !potentialIndexField.equals(indexField);
				} else {
					accept = true;
				}
				return accept;
			}
		};

		IChoiceRenderer indexFieldRenderer = new ChoiceRenderer() {
			@Override
			public Object getDisplayValue(Object object) {
				IndexField indexField = (IndexField) object;
				return indexField.getName();
			}
		};
		
		DropDownChoice indexFieldSource = new DropDownChoice("indexFieldSource", indexFieldsModel, indexFieldRenderer);
		indexFieldSource.setVisible(dest);
		form.add(indexFieldSource);
        
        final CheckBox sourceAllFieldsCheckbox = new CheckBox("sourceAllFields");
        sourceAllFieldsCheckbox.setVisible(dest);
        form.add(sourceAllFieldsCheckbox);
		
		DropDownChoice indexFieldDest = new DropDownChoice("indexFieldDest", indexFieldsModel, indexFieldRenderer);
		indexFieldDest.setRequired(true);
		indexFieldDest.setVisible(!dest);
		form.add(indexFieldDest);

		AjaxButton submitButton = new AjaxButton("submitButton") {
			@SuppressWarnings("unchecked")
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				CopyField copyField = copyFieldModel.getObject();
				
				CopyFieldListPanel copyFieldListPanel = 
					(CopyFieldListPanel) findParent(CopyFieldListPanel.class);
				
				List<CopyFieldDTO> copyFields = (List<CopyFieldDTO>) copyFieldListPanel.getModelObject();
				if (index == -1) {
					copyFields.add(new CopyFieldDTO(copyField));
				} else {
					copyFields.set(index, new CopyFieldDTO(copyField));
				}
				
				ModalWindow modalWindow = (ModalWindow) findParent(ModalWindow.class);
				modalWindow.close(target);

				target.addComponent(copyFieldListPanel);
			}

			@Override
			protected void onError(final AjaxRequestTarget target, Form form) {
				target.addComponent(feedback);
			}
		};
		form.add(submitButton);

		Button cancelButton = new AjaxButton("cancelButton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {
				ModalWindow modalWindow = (ModalWindow) findParent(ModalWindow.class);
				modalWindow.close(target);
			}
		}.setDefaultFormProcessing(false);
		form.add(cancelButton);
	}

	@Override
	public void detachModels() {
		copyFieldModel.detach();
		super.detachModels();
	}

}
