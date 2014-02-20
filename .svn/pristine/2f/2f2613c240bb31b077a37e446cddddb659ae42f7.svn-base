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
package com.doculibre.constellio.wicket.panels.admin.categorization;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.Categorization;
import com.doculibre.constellio.entities.CategorizationRule;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.CategorizationServices;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.models.admin.AdminCollectionIndexFieldsModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.categorization.dto.CategorizationRuleDTO;
import com.doculibre.constellio.wicket.panels.admin.categorization.rules.CategorizationRuleListPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

/**
 * indexed = true
 * stored = true
 * highlighted = true
 * type = string par défaut
 * 
 * @author Vincent
 */
@SuppressWarnings("serial")
public class AddEditCategorizationPanel extends SaveCancelFormPanel {

    private ReloadableEntityModel<Categorization> categorizationModel;

    private List<CategorizationRuleDTO> rules = new ArrayList<CategorizationRuleDTO>();

    public AddEditCategorizationPanel(String id, Categorization categorization) {
        super(id, true);
        this.categorizationModel = new ReloadableEntityModel<Categorization>(categorization);
        for (CategorizationRule categorizationRule : categorization.getCategorizationRules()) {
            rules.add(new CategorizationRuleDTO(categorizationRule));
        }

        Form form = getForm();
        form.setModel(new CompoundPropertyModel(categorizationModel));

        TextField nameField = new RequiredTextField("name");
        form.add(nameField);

        form.add(new CategorizationRuleListPanel("rulesPanel"));

        IModel indexFieldsModel = new AdminCollectionIndexFieldsModel(this);

        IChoiceRenderer indexFieldRenderer = new ChoiceRenderer() {
            @Override
            public Object getDisplayValue(Object object) {
                IndexField indexField = (IndexField) object;
                return indexField.getName();
            }
        };

        DropDownChoice indexField = new DropDownChoice("indexField", indexFieldsModel, indexFieldRenderer);
        indexField.setRequired(true);
        form.add(indexField);
    }

    @Override
    public void detachModels() {
        categorizationModel.detach();
        super.detachModels();
    }

    public List<CategorizationRuleDTO> getRules() {
        return rules;
    }

    @Override
    protected IModel getTitleModel() {
        return new LoadableDetachableModel() {

            @Override
            protected Object load() {
                String titleKey = categorizationModel.getObject().getId() == null ? "add" : "edit";
                return new StringResourceModel(titleKey, AddEditCategorizationPanel.this, null).getObject();
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

        Categorization categorization = categorizationModel.getObject();
        categorization.setRecordCollection(collection);

        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        Set<CategorizationRule> previousRules = categorization.getCategorizationRules();
        List<CategorizationRuleDTO> newRulesDTO = getRules();
        List<CategorizationRule> newRules = new ArrayList<CategorizationRule>();
        for (CategorizationRuleDTO categorizationRuleDTO : newRulesDTO) {
            newRules.add(categorizationRuleDTO.toCategorizationRule());
        }
       
        for (Iterator<CategorizationRule> it = previousRules.iterator(); it.hasNext();) {
            CategorizationRule categorizationRule = it.next();
            if (!newRules.contains(categorizationRule)) {
                it.remove();
                // Workaround
            }
        }
        for (Iterator<CategorizationRule> it = newRules.iterator(); it.hasNext();) {
            CategorizationRule categorizationRule = it.next();
            if (!previousRules.contains(categorizationRule)) {
                if (categorizationRule.getId() != null) {
                    categorizationRule = entityManager.merge(categorizationRule);
                }
                categorizationRule.setCategorization(categorization);
                categorization.getCategorizationRules().add(categorizationRule);
            }
        }

        boolean updateIndexField;
        IndexField indexField = categorization.getIndexField();
        if (!indexField.isMultiValued()) {
            indexField.setMultiValued(true);
            updateIndexField = true;
        } else {
            updateIndexField = false;
        }

        CategorizationServices categorizationServices = ConstellioSpringUtils.getCategorizationServices();
        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();

        categorizationServices.makePersistent(categorization);
        if (updateIndexField) {
            indexFieldServices.makePersistent(indexField);
        }
        collectionServices.makePersistent(collection, false);
        entityManager.getTransaction().commit();
    }

    @Override
    protected void defaultReturnAction(AjaxRequestTarget target) {
        super.defaultReturnAction(target);
        AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
        target.addComponent(collectionAdminPanel);
    }

}
