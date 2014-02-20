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
package com.doculibre.constellio.wicket.panels.admin.fieldType;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import com.doculibre.constellio.entities.Analyzer;
import com.doculibre.constellio.entities.AnalyzerFilter;
import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.entities.FieldTypeClass;
import com.doculibre.constellio.services.ConnectorManagerServices;
import com.doculibre.constellio.services.FieldTypeClassServices;
import com.doculibre.constellio.services.FieldTypeServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.analyzer.AddEditAnalyzerFieldPanel;
import com.doculibre.constellio.wicket.panels.admin.fieldTypeClass.AddEditFieldTypeClassPanel;

@SuppressWarnings("serial")
public class AddEditFieldTypePanel extends SaveCancelFormPanel {
	
	private ReloadableEntityModel<FieldType> fieldTypeModel;
	
	private Component refreshComponent;
	
	private AddEditAnalyzerFieldPanel queryAnalyzerPanel;
	
	private AddEditAnalyzerFieldPanel analyzerPanel;

	public AddEditFieldTypePanel(String id, FieldType fieldType, Component refreshComponentP) {
		super(id, true);
		this.fieldTypeModel = new ReloadableEntityModel<FieldType>(fieldType);
		this.refreshComponent = refreshComponentP;

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(fieldTypeModel));

		TextField nameField = new RequiredTextField("name");
		nameField.add(new StringValidator.MaximumLengthValidator(255));
		form.add(nameField);
		
		final ModalWindow fieldTypeClassModal = new ModalWindow("fieldTypeClassModal");
		form.add(fieldTypeClassModal);
		fieldTypeClassModal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);

		IModel fieldTypeClassesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				FieldTypeClassServices fieldTypeClassServices = ConstellioSpringUtils.getFieldTypeClassServices();
				return fieldTypeClassServices.list();
			}
		};

		IChoiceRenderer fieldTypeClassRenderer = new ChoiceRenderer("className");

		final DropDownChoice fieldTypeClassField = new DropDownChoice("fieldTypeClass", fieldTypeClassesModel,
				fieldTypeClassRenderer);
		form.add(fieldTypeClassField);
		fieldTypeClassField.setOutputMarkupId(true);
		
		AjaxLink addFieldTypeClassLink = new AjaxLink("addFieldTypeClassLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				AddEditFieldTypeClassPanel addEditFieldTypeClassPanel = 
					new AddEditFieldTypeClassPanel(fieldTypeClassModal.getContentId(), new FieldTypeClass(), fieldTypeClassField);
				fieldTypeClassModal.setContent(addEditFieldTypeClassPanel);
				fieldTypeClassModal.show(target);
			}
		};
		form.add(addFieldTypeClassLink);
		
		analyzerPanel = new AddEditAnalyzerFieldPanel("analyzerPanel", new LoadableDetachableModel() {
			@Override
			protected Object load() {
				FieldType fieldType = fieldTypeModel.getObject();
				return fieldType.getAnalyzer() != null ? fieldType.getAnalyzer() : new Analyzer();
			}
		}); 
		form.add(analyzerPanel);
		
		queryAnalyzerPanel = new AddEditAnalyzerFieldPanel("queryAnalyzerPanel", new LoadableDetachableModel() {
			@Override
			protected Object load() {
				FieldType fieldType = fieldTypeModel.getObject();
				return fieldType.getQueryAnalyzer() != null ? fieldType.getQueryAnalyzer() : new Analyzer();
			}
		});
		form.add(queryAnalyzerPanel);
		
		TextField precisionStepField = new TextField("precisionStep", Integer.class);
		form.add(precisionStepField);
		
		TextField positionIncrementGapField = new TextField("positionIncrementGap", Integer.class);
		form.add(positionIncrementGapField);
		
		final CheckBox sortMissingLastCheckbox = new CheckBox("sortMissingLast");
		form.add(sortMissingLastCheckbox);
		
		final CheckBox omitNormsCheckbox = new CheckBox("omitNorms");
		form.add(omitNormsCheckbox);
		
		final CheckBox multiValuedCheckbox = new CheckBox("multiValued");
		form.add(multiValuedCheckbox);
		
	}

	@Override
	public void detachModels() {
		fieldTypeModel.detach();
		super.detachModels();
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			
			@Override
			protected Object load() {
				String titleKey = fieldTypeModel.getObject().getId() == null ? "add" : "edit";
				return new StringResourceModel(titleKey, AddEditFieldTypePanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		return new FieldTypeListPanel(
				AddEditFieldTypePanel.this.getId());
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		FieldType fieldType = fieldTypeModel.getObject();
		
		Analyzer analyzer = (Analyzer) analyzerPanel.getModelObject();
		Analyzer queryAnalyzer = (Analyzer) queryAnalyzerPanel.getModelObject();
		if (queryAnalyzer.getTokenizerClass() != null && analyzer.getTokenizerClass() == null) {
			error(getLocalizer().getString("queryAnalyzerWithoutIndexAnalyzerError", this));
			return;
		}
		
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}

		// Has been set
		if (analyzer.getTokenizerClass() != null || analyzer.getAnalyzerClass() != null) {
			fieldType.setAnalyzer(analyzer);
			Set<AnalyzerFilter> previousFilters = analyzer.getFilters();
			List<AnalyzerFilter> newFilters = analyzerPanel.getFilters();
			for (Iterator<AnalyzerFilter> it = previousFilters.iterator(); it.hasNext();) {
				AnalyzerFilter previousFilter = it.next();
				if (!newFilters.contains(previousFilter)) {
					// Workaround...
					entityManager.remove(previousFilter);
					it.remove();
				}
			}
			for (AnalyzerFilter newFilter : newFilters) {
				if (!previousFilters.contains(newFilter)) {
					newFilter.setAnalyzer(analyzer);
					analyzer.getFilters().add(newFilter);
				}
			}
		}
		
		// Has been set
		if (queryAnalyzer.getTokenizerClass() != null || queryAnalyzer.getAnalyzerClass() != null) {
			fieldType.setQueryAnalyzer(queryAnalyzer);
			Set<AnalyzerFilter> previousFilters = queryAnalyzer.getFilters();
			List<AnalyzerFilter> newFilters = queryAnalyzerPanel.getFilters();
			for (Iterator<AnalyzerFilter> it = previousFilters.iterator(); it.hasNext();) {
				AnalyzerFilter previousFilter = it.next();
				if (!newFilters.contains(previousFilter)) {
					// Workaround...
					entityManager.remove(previousFilter);
					it.remove();
				}
			}
			for (AnalyzerFilter newFilter : newFilters) {
				if (!previousFilters.contains(newFilter)) {
					newFilter.setAnalyzer(queryAnalyzer);
					queryAnalyzer.getFilters().add(newFilter);
				}
			}
		}
		
		ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils.getConnectorManagerServices();
		FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
		
		ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
		fieldType.setConnectorManager(connectorManager);
		fieldTypeServices.makePersistent(fieldType);
		entityManager.getTransaction().commit();
		
		//Ajout du fieldtype au schema
		SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
		solrServices.updateSchemaFieldTypes();

		if (refreshComponent != null) {
			if (refreshComponent instanceof FormComponent) {
				refreshComponent.setModelObject(fieldType);
			}
			target.addComponent(refreshComponent);
		}

	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		if (refreshComponent != null) {
			target.addComponent(refreshComponent);
		}
	}

}
