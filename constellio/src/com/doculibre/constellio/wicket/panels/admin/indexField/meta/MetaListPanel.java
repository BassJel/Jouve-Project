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
package com.doculibre.constellio.wicket.panels.admin.indexField.meta;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;
import com.doculibre.constellio.wicket.panels.admin.indexField.AddEditIndexFieldPanel;
import com.doculibre.constellio.wicket.panels.admin.indexField.dto.ConnectorInstanceMetaDTO;

@SuppressWarnings("serial")
public class MetaListPanel extends SingleColumnCRUDPanel {
	
	public MetaListPanel(String id) {
		super(id);

		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AddEditIndexFieldPanel addEditIndexFieldPanel = 
					(AddEditIndexFieldPanel) findParent(AddEditIndexFieldPanel.class);
				List<ConnectorInstanceMetaDTO> metaDTOs = addEditIndexFieldPanel.getMetas();
				return metaDTOs;
			}
		});
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddMetaPanel(id);
	}

	@Override
    protected boolean isEditLink(IModel rowItemModel, int index) {
        return false;
    }

    @Override
	protected WebMarkupContainer createEditContent(String id, IModel valueModel, int index) {
		WebMarkupContainer editContent = new WebMarkupContainer(id);
		editContent.setVisible(false);
		return editContent;
	}

	@Override
	protected String getDetailsLabel(Object object) {
		ConnectorInstanceMetaDTO meta = (ConnectorInstanceMetaDTO) object;
		String detailsLabel = meta.getName() + " (" + meta.getConnectorInstance().getDisplayName() + ")";
		return detailsLabel;
	}

	@Override
	protected WebMarkupContainer createDeleteLink(String id, final IModel entityModel, final int index) {
		return new AjaxLink(id) {
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				List<ConnectorInstanceMetaDTO> metas = (List<ConnectorInstanceMetaDTO>) MetaListPanel.this.getModelObject();
				metas.remove(index);
				target.addComponent(MetaListPanel.this);				
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator() {
				return new AjaxCallDecorator() {
					@Override
					public CharSequence decorateScript(CharSequence script) {
						String confirmMsg = getLocalizer().getString("confirmDelete", MetaListPanel.this);
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
