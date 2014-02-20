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
package com.doculibre.constellio.wicket.panels.admin.analyzer.filter;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.AnalyzerFilter;
import com.doculibre.constellio.entities.FilterClass;
import com.doculibre.constellio.services.FilterClassServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.analyzer.AddEditAnalyzerFieldPanel;
import com.doculibre.constellio.wicket.panels.admin.filterClass.AddEditFilterClassPanel;

@SuppressWarnings("serial")
public class AddEditFilterPanel extends SaveCancelFormPanel {

	ReloadableEntityModel<AnalyzerFilter> entityModel;

	private int index;

	public AddEditFilterPanel(String id, AnalyzerFilter filter,
			int index) {
		super(id, true);
		this.entityModel = new ReloadableEntityModel<AnalyzerFilter>(filter);
		// Ne pas utiliser filter.getID() pour déterminer si c'est en création.
		// Car cela empêche de modifier un filtre tout juste créé
		this.index = index;

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(entityModel));

		final ModalWindow filterClassModal = new ModalWindow("filterClassModal");
		form.add(filterClassModal);
		filterClassModal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);

		IModel filterClassesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				FilterClassServices filterClassServices = ConstellioSpringUtils
						.getFilterClassServices();
				return filterClassServices.list();
			}
		};

		IChoiceRenderer filterClassRenderer = new ChoiceRenderer("className");

		final DropDownChoice filterClassField = new DropDownChoice(
				"filterClass", filterClassesModel, filterClassRenderer);
		form.add(filterClassField);
		filterClassField.setOutputMarkupId(true);

		AjaxLink addFilterClassLink = new AjaxLink("addFilterClassLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				AddEditFilterClassPanel addEditAnalyzerClassPanel = new AddEditFilterClassPanel(
						filterClassModal.getContentId(), new FilterClass(),
						filterClassField);
				filterClassModal.setContent(addEditAnalyzerClassPanel);
				filterClassModal.show(target);
			}
		};
		form.add(addFilterClassLink);

		form.add(new CheckBox("ignoreCase"));
		form.add(new CheckBox("expand"));
		form.add(new CheckBox("enablePositionIncrements"));
		form.add(new CheckBox("inject"));
		form.add(new TextField("language"));
		form.add(new TextArea("wordsText"));
		form.add(new TextArea("synonymsText"));
		form.add(new TextArea("protectedText"));
		form.add(new TextField("generateWordParts", Integer.class));
		form.add(new TextField("generateNumberParts", Integer.class));
		form.add(new TextField("catenateWords", Integer.class));
		form.add(new TextField("catenateNumbers", Integer.class));
		form.add(new TextField("catenateAll", Integer.class));
		form.add(new TextField("splitOnCaseChange", Integer.class));
		form.add(new TextField("delimiter"));
		form.add(new TextField("encoder"));
		form.add(new TextField("pattern"));
		form.add(new TextField("replacement"));
		form.add(new TextField("replace"));
	}

	@Override
	public void detachModels() {
		entityModel.detach();
		super.detachModels();
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			@Override
			protected Object load() {
				String titleKey = index < 0 ? "add" : "edit";
				return new StringResourceModel(titleKey,
						AddEditFilterPanel.this, null).getObject();
			}
		};

	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		AddEditAnalyzerFieldPanel addEditAnalyzerFieldPanel = (AddEditAnalyzerFieldPanel) findParent(AddEditAnalyzerFieldPanel.class);
		AnalyzerFilter analyzerFilter = entityModel.getObject();
		if (index >= 0) {
			addEditAnalyzerFieldPanel.getFilters().remove(index);
		}
		addEditAnalyzerFieldPanel.getFilters().add(analyzerFilter);
	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		FilterListPanel filterListPanel = (FilterListPanel) findParent(FilterListPanel.class);
		target.addComponent(filterListPanel);
	}

}
