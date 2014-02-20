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
package com.doculibre.constellio.wicket.panels.admin.analyzer;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.Analyzer;
import com.doculibre.constellio.entities.AnalyzerClass;
import com.doculibre.constellio.entities.AnalyzerFilter;
import com.doculibre.constellio.entities.TokenizerClass;
import com.doculibre.constellio.services.AnalyzerClassServices;
import com.doculibre.constellio.services.TokenizerClassServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.models.EntityListModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.analyzer.filter.FilterListPanel;
import com.doculibre.constellio.wicket.panels.admin.analyzerClass.AddEditAnalyzerClassPanel;
import com.doculibre.constellio.wicket.panels.admin.tokenizerClass.AddEditTokenizerClassPanel;

@SuppressWarnings("serial")
public class AddEditAnalyzerFieldPanel extends AjaxPanel {

	private EntityListModel<AnalyzerFilter> filtersModel = new EntityListModel<AnalyzerFilter>();

    public AddEditAnalyzerFieldPanel(String id, IModel analyzerModel) {
		super(id, new CompoundPropertyModel(analyzerModel));
		
		Analyzer analyzer = (Analyzer) analyzerModel.getObject();
		filtersModel.getObject().addAll(analyzer.getFilters());
		
		final ModalWindow analyzerClassModal = new ModalWindow("analyzerClassModal");
		add(analyzerClassModal);
		analyzerClassModal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);

		IModel analyzerClassesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AnalyzerClassServices analyzerClassServices = ConstellioSpringUtils.getAnalyzerClassServices();
				return analyzerClassServices.list();
			}
		};

		IChoiceRenderer analyzerClassRenderer = new ChoiceRenderer("className");

		final DropDownChoice analyzerClassField = new DropDownChoice("analyzerClass", analyzerClassesModel,
				analyzerClassRenderer);
		add(analyzerClassField);
		analyzerClassField.setOutputMarkupId(true);
		
		AjaxLink addAnalyzerClassLink = new AjaxLink("addAnalyzerClassLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				AddEditAnalyzerClassPanel addEditAnalyzerClassPanel = 
					new AddEditAnalyzerClassPanel(analyzerClassModal.getContentId(), new AnalyzerClass(), analyzerClassField);
				analyzerClassModal.setContent(addEditAnalyzerClassPanel);
				analyzerClassModal.show(target);
			}
		};
		add(addAnalyzerClassLink);

		
		final ModalWindow tokenizerClassModal = new ModalWindow("tokenizerClassModal");
		add(tokenizerClassModal);
		tokenizerClassModal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);

		IModel tokenizerClassesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				TokenizerClassServices tokenizerClassServices = ConstellioSpringUtils.getTokenizerClassServices();
				return tokenizerClassServices.list();
			}
		};

		IChoiceRenderer tokenizerClassRenderer = new ChoiceRenderer("className");

		final DropDownChoice tokenizerClassField = new DropDownChoice("tokenizerClass", tokenizerClassesModel,
				tokenizerClassRenderer);
		add(tokenizerClassField);
		tokenizerClassField.setOutputMarkupId(true);
		
		AjaxLink addTokenizerClassLink = new AjaxLink("addTokenizerClassLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				AddEditTokenizerClassPanel addEditTokenizerClassPanel = 
					new AddEditTokenizerClassPanel(tokenizerClassModal.getContentId(), new TokenizerClass(), tokenizerClassField);
				tokenizerClassModal.setContent(addEditTokenizerClassPanel);
				tokenizerClassModal.show(target);
			}
		};
		add(addTokenizerClassLink);
		
		
		add(new FilterListPanel("filtersPanel"));
	}
	
	public List<AnalyzerFilter> getFilters() {
		return filtersModel.getObject();
	}

    @Override
    public void detachModels() {
        filtersModel.detach();
        super.detachModels();
    }

}
