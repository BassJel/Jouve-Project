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
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;
import com.doculibre.constellio.wicket.panels.admin.indexField.AddEditIndexFieldPanel;
import com.doculibre.constellio.wicket.panels.admin.indexField.dto.CopyFieldDTO;

@SuppressWarnings("serial")
public class CopyFieldListPanel extends SingleColumnCRUDPanel {
	
	private boolean dest;

	public CopyFieldListPanel(String id, final boolean dest) {
		super(id);

		this.dest = dest;
		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				List<CopyFieldDTO> copyFields;
				AddEditIndexFieldPanel addEditIndexFieldPanel = 
					(AddEditIndexFieldPanel) findParent(AddEditIndexFieldPanel.class);
				if (dest) {
					copyFields = addEditIndexFieldPanel.getCopyFieldsDest();
				} else {
					copyFields = addEditIndexFieldPanel.getCopyFieldsSource();
				}
				return copyFields;
			}
		});
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddEditCopyFieldPanel(id, -1, new CopyField(), dest);
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel valueModel, int index) {
		return new AddEditCopyFieldPanel(id, index, ((CopyFieldDTO) valueModel.getObject()).toCopyField(), dest);
	}

	@Override
	protected String getDetailsLabel(Object object) {
		String detailsLabel;
		CopyFieldDTO copyField = (CopyFieldDTO) object;
		if (dest) {
		    if (copyField.isSourceAllFields()) {
		        detailsLabel = "*";
		    } else {
	            detailsLabel = copyField.getIndexFieldSource().getName();
		    }
		} else {
			detailsLabel = copyField.getIndexFieldDest().getName();
		}
		return detailsLabel;
	}

	@Override
	protected WebMarkupContainer createDeleteLink(String id, final IModel entityModel, final int index) {
		return new AjaxLink(id) {
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				List<CopyFieldDTO> copyFields = (List<CopyFieldDTO>) CopyFieldListPanel.this.getModelObject();
				copyFields.remove(index);
				target.addComponent(CopyFieldListPanel.this);				
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator() {
				return new AjaxCallDecorator() {
					@Override
					public CharSequence decorateScript(CharSequence script) {
						String confirmMsg = getLocalizer().getString("confirmDelete", CopyFieldListPanel.this);
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

}
