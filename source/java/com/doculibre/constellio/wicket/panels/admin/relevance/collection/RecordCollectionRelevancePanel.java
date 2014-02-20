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
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.feedback.ErrorLevelFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.relevance.RecordCollectionBoost;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceUtils;
import com.doculibre.constellio.wicket.models.SortableListModel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.CRUDPanel;

@SuppressWarnings("serial")
public class RecordCollectionRelevancePanel extends CRUDPanel<RecordCollectionBoost> {

    private AjaxButton submitButton;
    private Form form;

    public RecordCollectionRelevancePanel(String id) {
        super(id, 10);
        form = new Form("form");
        this.add(form);
        setModel(new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ArrayList<RecordCollectionBoost> list = new ArrayList<RecordCollectionBoost>();
                AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                RecordCollection collection = collectionAdminPanel.getCollection();
                for (RecordCollectionBoost element : collection.getRecordCollectionBoost()) {
                    list.add(element);
                }
                return list;
            }
        });
        
        submitButton = new AjaxButton("submitButton") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form form) {
                onSave();
                if (!Session.get().getFeedbackMessages().hasMessage(
                    new ErrorLevelFeedbackMessageFilter(FeedbackMessage.ERROR))) {
                    defaultReturnAction(target);
                }
            }
        };

        form.add(submitButton);
    }

    protected void defaultReturnAction(AjaxRequestTarget target) {
        // pr dire a la mere de se raffraichir
        target.addComponent(this);
    }

    protected void onSave() {
        RecordCollection collection = getRecordCollection();
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        FederationServices federationServices = ConstellioSpringUtils.getFederationServices();

		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		try {
        	ConstellioPersistenceUtils.beginTransaction();                
	        recordServices.markRecordsForUpdateIndex(collection);
	        if (collection.isFederationOwner()) {
	            List<RecordCollection> includedCollections = federationServices
	                .listIncludedCollections(collection);
	            for (RecordCollection includedCollection : includedCollections) {
	                recordServices.markRecordsForUpdateIndex(includedCollection);
	            }
	        }
            try {
				solrServer.commit();
			} catch (Throwable t) {
				try {
					solrServer.rollback();
				} catch (Exception e) {
					throw new RuntimeException(t);
				}
			}
		} finally {
			ConstellioPersistenceUtils.finishTransaction(false);
		}
        
        /*EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();

        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        // updateRecordBoost
        ConstellioSpringUtils.getRecordServices().updateRecordBoost(collection);// markRecordsForUpdateIndex
                                                                                // pr relancer l idx

        entityManager.getTransaction().commit();*/
    }
    

    private RecordCollection getRecordCollection() {
        AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
        RecordCollection collection = collectionAdminPanel.getCollection();
        return collection;
    }

    // choix du panel qui s affiche apres clic create
    @Override
    protected WebMarkupContainer createAddContent(String id) {
        RecordCollectionBoost recordCollectionBoost = new RecordCollectionBoost();
        return new AddEditRecordCollectionBoostPanel(id, recordCollectionBoost, -1);
    }

    // choix du panel qui s affiche apres clic edit
    @Override
    protected WebMarkupContainer createEditContent(String id, IModel rowItemModel, int index) {
        RecordCollectionBoost recordCollectionBoost = (RecordCollectionBoost) rowItemModel.getObject();
        return new AddEditRecordCollectionBoostPanel(id, recordCollectionBoost, index);
    }

    // definition des colonnes a afficher
    @Override
    protected List<IColumn> getDataColumns() {
        List<IColumn> dataColumns = new ArrayList<IColumn>();

        dataColumns.add(new PropertyColumn(new StringResourceModel("name", this, null), "name"));
        dataColumns.add(new PropertyColumn(new StringResourceModel("associatedField", this, null), "associatedField.name"));

        return dataColumns;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onClickDeleteLink(IModel rowItemModel, AjaxRequestTarget target, int index) {
        List<RecordCollectionBoost> recordCollectionBoosts = (List<RecordCollectionBoost>) RecordCollectionRelevancePanel.this
            .getModelObject();

        RecordCollectionBoost recordToDelete = recordCollectionBoosts.get(index);

        AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
        RecordCollection collection = collectionAdminPanel.getCollection();

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();

        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        collection.getRecordCollectionBoost().remove(recordToDelete);
        collectionServices.makePersistent(collection, false); // false : pas de
        // modif du
        // schema.xml
        entityManager.getTransaction().commit();

        recordCollectionBoosts.remove(index);
        target.addComponent(this);
    }

    // load est appelé pour obtenir la liste des elements
    @Override
    protected SortableListModel<RecordCollectionBoost> getSortableListModel() {
        return new SortableListModel<RecordCollectionBoost>() {
            @SuppressWarnings("unchecked")
            @Override
            protected List<RecordCollectionBoost> load(String orderByProperty, Boolean orderByAsc) {
                return (List<RecordCollectionBoost>) RecordCollectionRelevancePanel.this
                .getModelObject();
            }
        };
    }

    @Override
    protected WebMarkupContainer createDeleteLink(String id, IModel rowItemModel, int index) {
        return super.createDeleteLink(id, rowItemModel, index);
    }

}
