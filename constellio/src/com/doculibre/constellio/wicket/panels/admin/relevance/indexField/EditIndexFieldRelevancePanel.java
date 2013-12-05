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
package com.doculibre.constellio.wicket.panels.admin.relevance.indexField;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.helper.connector.type.form.element.FormElementValidationService;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class EditIndexFieldRelevancePanel extends SaveCancelFormPanel  {

	private String name = "";
	private String boost = "1.0";
	private IModel indexField;

	public EditIndexFieldRelevancePanel(String id, IModel indexField) {
		super(id, true);
		
		this.indexField = indexField;
		setName(((IndexField)indexField.getObject()).getName());
		setIndexBoost(((IndexField) indexField.getObject()).getBoost().toString());

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(this));// a cause de repetitions

		Label name	= new Label("name");// grace a setModel plus besoin de : new PropertyModel(this, "regex")
		form.add(name);
		
		TextField indexBoost	= new TextField("boost");
		indexBoost.add(new IValidator(){
			@Override
			public void validate(IValidatable validatable) {
				String indexBoost = (String) validatable.getValue();
				String errorMessage = FormElementValidationService.validateContent(new StringBuilder(indexBoost), FormElementValidationService.ContentType.DOUBLE, ConstellioSession.get().getLocale());
				if (! errorMessage.isEmpty()){
					validatable.error(new ValidationError().setMessage(errorMessage));
				} else {
					double boostTodouble = Double.parseDouble(indexBoost);
					if (boostTodouble < 0) {
						validatable.error(new ValidationError().setMessage(indexBoost + " < 0"));
					}
				}
			}
		});
		form.add(indexBoost);
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		// persister dans la BD
		if (this.getIndexBoost() == null){
			return;
		}
		Double newIndexBoost = Double.parseDouble(this.getIndexBoost());
		if (!((IndexField)this.indexField.getObject()).getBoost().equals(newIndexBoost)){
			((IndexField)this.indexField.getObject()).setBoost(newIndexBoost.floatValue());
			//persister dans la BD
			updateIndexField((IndexField)this.indexField.getObject(), target);
		}
	}

    private void updateIndexField(IndexField indexField, AjaxRequestTarget target) {
        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        indexFieldServices.merge(indexField);
        entityManager.getTransaction().commit();

        defaultReturnAction(target);
    }

	//appelé a chaque fois que je fais modif
	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		// pr dire a la mere de se raffraichir
		target.addComponent(this.findParent(IndexFieldRelevancePanel.class));
	}




	@Override
	protected IModel getTitleModel() {
		return new Model();
	}




	public String getName() {
		return name;
	}




	public void setName(String name) {
		this.name = name;
	}



	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}




	public void setIndexBoost(String indexBoost) {
		this.boost = indexBoost;
	}




	public String getIndexBoost() {
		return boost;
	}


}
