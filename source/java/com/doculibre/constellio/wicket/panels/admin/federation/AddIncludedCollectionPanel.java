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
package com.doculibre.constellio.wicket.panels.admin.federation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.CollectionFederation;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.models.EntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class AddIncludedCollectionPanel extends SaveCancelFormPanel {

    private EntityModel<CollectionFederation> federationModel;

    private IModel includableCollectionsModel;

    private DropDownChoice includedCollectionField;

    public AddIncludedCollectionPanel(String id, CollectionFederation federation) {
        super(id, true);
        this.federationModel = new EntityModel<CollectionFederation>(federation);

        Form form = getForm();
        form.setModel(new CompoundPropertyModel(federationModel));
        form.add(new SetFocusBehavior(form));

        includableCollectionsModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                List<RecordCollection> includableCollections = new ArrayList<RecordCollection>();
                RecordCollectionServices collectionServices = ConstellioSpringUtils
                    .getRecordCollectionServices();
                FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
                AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                RecordCollection collection = collectionAdminPanel.getCollection();
                Set<RecordCollection> includedCollections = collection.getIncludedCollections();
                List<RecordCollection> allCollections = collectionServices.list();
                for (RecordCollection existingCollection : allCollections) {
                    if (!existingCollection.isOpenSearch() && !existingCollection.equals(collection)
                        && !includedCollections.contains(existingCollection)) {
                        if (existingCollection.isFederationOwner()) {
                            List<RecordCollection> existingColletionIncludedCollections = federationServices
                                .listIncludedCollections(existingCollection);
                            if (!existingColletionIncludedCollections.contains(collection)) {
                                includableCollections.add(existingCollection);
                            }
                        } else {
                            includableCollections.add(existingCollection);
                        }
                    }
                }
                return includableCollections;
            }
        };

        includedCollectionField = new DropDownChoice("includedCollection", includableCollectionsModel,
            new ChoiceRenderer() {
                @Override
                public Object getDisplayValue(Object object) {
                    RecordCollection collection = (RecordCollection) object;
                    Locale displayLocale = collection.getDisplayLocale(getLocale());
                    return collection.getTitle(displayLocale);
                }
            });
        includedCollectionField.setRequired(true);
        form.add(includedCollectionField);
    }

    @Override
    public void detachModels() {
        federationModel.detach();
        super.detachModels();
    }

    @Override
    protected IModel getTitleModel() {
        return new LoadableDetachableModel() {
            @Override
            protected Object load() {
                String titleKey = "add";
                return new StringResourceModel(titleKey, AddIncludedCollectionPanel.this, null).getObject();
            }
        };
    }

    @Override
    protected Component newReturnComponent(String id) {
        return new IncludedCollectionListPanel(getId());
    }

    @Override
    protected void onSave(AjaxRequestTarget target) {
        CollectionFederation federation = federationModel.getObject();

        FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
        
        AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
        RecordCollection federationOwner = collectionAdminPanel.getCollection();
        RecordCollection includedCollection = federation.getIncludedCollection();
        federation.setOwnerCollection(federationOwner);

        List<String> conflicts = new ArrayList<String>();
        for (IndexField indexField : federationOwner.getIndexFields()) {
            String indexFieldName = indexField.getName();
            if (federationServices.isConflict(indexFieldName, federationOwner, includedCollection)) {
                conflicts.add(indexFieldName);
            }
        }
        if (conflicts.isEmpty()) {
            IndexingManager indexingManager = IndexingManager.get(federationOwner);
            indexingManager.stopManaging();

            EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
            if (!entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }
            federationServices.makePersistent(federation);
            solrServices.updateSchemaFields(federationOwner);
            solrServices.initCore(federationOwner);
            collectionServices.markSynchronized(federationOwner);
            entityManager.getTransaction().commit();
            
            indexingManager.startIndexing(false);
        } else {
            for (String indexFieldName : conflicts) {
                error(getLocalizer().getString("conflict", this) + " : " + indexFieldName);
            }
        }
    }

}
