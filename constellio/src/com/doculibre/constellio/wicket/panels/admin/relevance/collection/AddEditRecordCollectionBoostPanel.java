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
package com.doculibre.constellio.wicket.panels.admin.relevance.collection;

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
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.relevance.BoostRule;
import com.doculibre.constellio.entities.relevance.RecordCollectionBoost;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.EntityModel;
import com.doculibre.constellio.wicket.models.admin.AdminCollectionIndexFieldsModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.relevance.collection.dto.BoostRuleDTO;
import com.doculibre.constellio.wicket.panels.admin.relevance.collection.rules.BoostRuleListPanel;

@SuppressWarnings("serial")
public class AddEditRecordCollectionBoostPanel extends SaveCancelFormPanel {

    private EntityModel<RecordCollectionBoost> recordCollectionBoostModel;

    private List<BoostRuleDTO> rules = new ArrayList<BoostRuleDTO>();

    public AddEditRecordCollectionBoostPanel(String id, RecordCollectionBoost recordCollectionBoost, int index) {
        super(id, true);

        this.recordCollectionBoostModel = new EntityModel<RecordCollectionBoost>(recordCollectionBoost);

        if (recordCollectionBoost.getId() != null) {
            for (BoostRule rule : recordCollectionBoost.getBoostRules()) {
                rules.add(new BoostRuleDTO(rule));
            }
        }

        Form form = getForm();
        form.setModel(new CompoundPropertyModel(recordCollectionBoostModel));// pr eviter les repetitions

        TextField name = new TextField("name");// grace a setModel plus besoin de : new PropertyModel(this,
        form.add(name);

        // IModel collectionFieldsNames = getFields();
        // DropDownChoice metaName = new DropDownChoice("metaName", collectionFieldsNames);
        // metaName.setRequired(true);

        IModel indexFieldsModel = new AdminCollectionIndexFieldsModel(this);
        IChoiceRenderer indexFieldRenderer = new ChoiceRenderer() {
            @Override
            public Object getDisplayValue(Object object) {
                IndexField indexField = (IndexField) object;
                return indexField.getName();
            }
        };

        DropDownChoice associatedField = new DropDownChoice("associatedField", indexFieldsModel, indexFieldRenderer);
        associatedField.setRequired(true);

        form.add(associatedField);

        // tous les regex
        form.add(new BoostRuleListPanel("rulesPanel"));
    }

    @Override
    protected void onSave(AjaxRequestTarget target) {
        RecordCollectionBoost recordCollectionBoost = recordCollectionBoostModel.getObject();
 
        AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
        RecordCollection collection = collectionAdminPanel.getCollection();
        if (recordCollectionBoost.getId() == null) {
            collection.addRecordCollectionBoost(recordCollectionBoost);
        }
        
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        Set<BoostRule> previousRules = recordCollectionBoost.getBoostRules();
        List<BoostRuleDTO> newRulesDTO = getRules();
        List<BoostRule> newRules = new ArrayList<BoostRule>();
        for (BoostRuleDTO boostRuleDTO : newRulesDTO) {
            newRules.add(boostRuleDTO.toBoostRule());
        }
       
        for (Iterator<BoostRule> it = previousRules.iterator(); it.hasNext();) {
            BoostRule boostRule = it.next();
            if (!newRules.contains(boostRule)) {
                it.remove();
                // Workaround
            }
        }
        for (Iterator<BoostRule> it = newRules.iterator(); it.hasNext();) {
            BoostRule boostRule = it.next();
            if (!previousRules.contains(boostRule)) {
                if (boostRule.getId() != null) {
                    boostRule = entityManager.merge(boostRule);
                }
                boostRule.setRecordCollectionBoost(recordCollectionBoost);
                recordCollectionBoost.getBoostRules().add(boostRule);
            }
        }

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        
        // ajouter ce qui reste (meme 2 fois pas de pb car c 1 set)
        collectionServices.makePersistent(collection, false); // false : pas de
        // modif du
        // schema.xml
        entityManager.getTransaction().commit();
    }

    // appelé a chaque fois que je fais modif
    @Override
    protected void defaultReturnAction(AjaxRequestTarget target) {
        super.defaultReturnAction(target);
        // pr dire a la mere de se raffraichir
        target.addComponent(this.findParent(RecordCollectionRelevancePanel.class));
    }

    public List<BoostRuleDTO> getRules() {
        return rules;
    }

    @Override
    protected IModel getTitleModel() {
        return new Model();
    }

    @Override
    protected Component newReturnComponent(String id) {
        return null;
    }

    @Override
    public void detachModels() {
        recordCollectionBoostModel.detach();
        super.detachModels();
    }

}
