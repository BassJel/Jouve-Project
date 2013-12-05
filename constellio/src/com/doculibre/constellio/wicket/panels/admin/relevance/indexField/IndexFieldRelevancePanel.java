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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceUtils;
import com.doculibre.constellio.wicket.models.EntityListModel;
import com.doculibre.constellio.wicket.models.SortableListModel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.CRUDPanel;

@SuppressWarnings("serial")
public class IndexFieldRelevancePanel extends CRUDPanel<IndexField> {// SingleColumnCRUDPanel

	private EntityListModel<IndexField> collectionSortedIndexFieldListModel = null;
	private Form form;

	public IndexFieldRelevancePanel(String id) {
		super(id, 10);//  enleve car index donne dans createEditContent ne
					// correspond pas sinon
		form = new Form("form");
		this.add(form);
		AjaxButton synchronizeSolrconfigLink = new AjaxButton("submitButton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {
				 onSave();
                if (!Session.get().getFeedbackMessages().hasMessage(
                    new ErrorLevelFeedbackMessageFilter(FeedbackMessage.ERROR))) {
                    defaultReturnAction(target);
                }
			}
		};
		form.add(synchronizeSolrconfigLink);
	}
	
    protected void defaultReturnAction(AjaxRequestTarget target) {
        // pr dire a la mere de se raffraichir
        target.addComponent(this);
    }
	
    protected void onSave() {
        RecordCollection collection = getRecordCollection();
//        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
        
//        boolean disMaxRequired = collectionServices.isDismaxRequired(collection);
//        updateSolrConfigFile(disMaxRequired);

		SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
		try {
        	ConstellioPersistenceUtils.beginTransaction();                
	        recordServices.markRecordsForUpdateIndex(collection);
	        if (collection.isFederationOwner()) {
	            List<RecordCollection> includedCollections = federationServices.listIncludedCollections(collection);
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
    }

//	protected void updateSolrConfigFile(boolean useDismaxDistance) {
//		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
//		RecordCollection collection = collectionAdminPanel.getCollection();
//		SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
//		if (useDismaxDistance){
//			solrServices.updateDismax(collection);
//		} else {
//			solrServices.resetDefaultDistance(collection);
//		}
//        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
//        if (!entityManager.getTransaction().isActive()) {
//            entityManager.getTransaction().begin();
//        }
//        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
//		collectionServices.markSynchronized(collection);
//        entityManager.getTransaction().commit();
//        
//        IndexingManager indexingManager = IndexingManager.get(collection);
//        if (!indexingManager.isActive()) {
//            indexingManager.startIndexing();
//        }
//	}

	// choix du panel qui s affiche apres clic create
	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return null;
	}

	// choix du panel qui s affiche apres clic edit
	@Override
	protected WebMarkupContainer createEditContent(final String id,
			IModel entityModel, final int index) {
		return new EditIndexFieldRelevancePanel(id, new Model() {
			@Override
			public Object getObject() {
				return collectionSortedIndexFieldListModel.getObject().get(index);
			}

			@Override
			public void setObject(Object object) {
				super.setObject(object);
			}

		});
	}

	// definition des colonnes a afficher
	@Override
	protected List<IColumn> getDataColumns() {
		List<IColumn> dataColumns = new ArrayList<IColumn>();

		dataColumns.add(new PropertyColumn(new StringResourceModel("field", this, null), "name"));
		dataColumns.add(new PropertyColumn(new StringResourceModel("boost", this, null), "boost"));

		return dataColumns;
	}

	@Override
	protected boolean isDeleteLink(IModel rowItemModel, int index) {
		return false;
	}


	// load est appelé pour obtenir la liste des elements
	@Override
	protected SortableListModel<IndexField> getSortableListModel() {
		return new SortableListModel<IndexField>() {
			@Override
			protected List<IndexField> load(String orderByProperty,
					Boolean orderByAsc) {
				if (collectionSortedIndexFieldListModel == null) {
					AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
					RecordCollection collection = collectionAdminPanel
							.getCollection();
					collectionSortedIndexFieldListModel = new EntityListModel<IndexField>();
					for (IndexField indexField : collection.getIndexedIndexFields()) {
						collectionSortedIndexFieldListModel.getObject().add(indexField);
					}
					Collections.sort(collectionSortedIndexFieldListModel.getObject(),
							new Comparator<IndexField>() {
								@Override
								public int compare(IndexField o1, IndexField o2) {
									return o1.getName().compareTo(o2.getName());
								}
							});
				}
				return collectionSortedIndexFieldListModel.getObject();
			}
		};
	}

	@Override
	protected WebMarkupContainer createDeleteLink(String id,
			IModel rowItemModel, int index) {
		return super.createDeleteLink(id, rowItemModel, index);
	}

	@Override
	protected boolean isAddLink() {
		return false;
	}

	@Override
	protected IColumn getDeleteLinkColumn() {
		return null;
	}

    @Override
    public void detachModels() {
        if (collectionSortedIndexFieldListModel != null) {
            collectionSortedIndexFieldListModel.detach();
        }
        super.detachModels();
    }

    //rien à faire car bouton n existe pas
	@Override
	protected void onClickDeleteLink(IModel rowItemModel,
			AjaxRequestTarget target, int index) {
		
	}

    private RecordCollection getRecordCollection() {
        AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
        RecordCollection collection = collectionAdminPanel.getCollection();
        return collection;
    }
}



/*extends CRUDPanel<RecordCollectionFieldBoost> {

    private AjaxButton submitButton;
    private Form form;

    public IndexFieldRelevancePanel(String id) {
        super(id, 10);
        form = new Form("form");
        this.add(form);
        setModel(new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ArrayList<RecordCollectionFieldBoost> list = new ArrayList<RecordCollectionFieldBoost>();
                AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                RecordCollection collection = collectionAdminPanel.getCollection();
                for (RecordCollectionFieldBoost element : collection.getRecordCollectionFieldBoost()) {
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

        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        recordServices.markRecordsForUpdateIndex(collection);
        if (collection.isFederationOwner()) {
            List<RecordCollection> includedCollections = federationServices
                .listIncludedCollections(collection);
            for (RecordCollection includedCollection : includedCollections) {
                recordServices.markRecordsForUpdateIndex(includedCollection);
            }
        }
        entityManager.getTransaction().commit();
        
    }
    

    private RecordCollection getRecordCollection() {
        AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
        RecordCollection collection = collectionAdminPanel.getCollection();
        return collection;
    }

    // choix du panel qui s affiche apres clic create
    @Override
    protected WebMarkupContainer createAddContent(String id) {
    	RecordCollectionFieldBoost recordCollectionFieldBoost = new RecordCollectionFieldBoost();
        return new AddEditRecordCollectionFieldBoost(id, recordCollectionFieldBoost, -1);
    }

    // choix du panel qui s affiche apres clic edit
    @Override
    protected WebMarkupContainer createEditContent(String id, IModel rowItemModel, int index) {
    	RecordCollectionFieldBoost recordCollectionFieldBoost = (RecordCollectionFieldBoost) rowItemModel.getObject();
        return new AddEditRecordCollectionFieldBoost(id, recordCollectionFieldBoost, index);
    }

    // definition des colonnes a afficher
    @Override
    protected List<IColumn> getDataColumns() {
        List<IColumn> dataColumns = new ArrayList<IColumn>();

        dataColumns.add(new PropertyColumn(new Model("Name"), "name"));
        dataColumns.add(new PropertyColumn(new Model("Associated Field"), "associatedField.name"));

        return dataColumns;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onClickDeleteLink(IModel rowItemModel, AjaxRequestTarget target, int index) {
        List<RecordCollectionFieldBoost> recordCollectionFieldBoosts = (List<RecordCollectionFieldBoost>) IndexFieldRelevancePanel.this
            .getModelObject();

        RecordCollectionFieldBoost recordToDelete = recordCollectionFieldBoosts.get(index);

        AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
        RecordCollection collection = collectionAdminPanel.getCollection();

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();

        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        collection.getRecordCollectionFieldBoost().remove(recordToDelete);
        collectionServices.makePersistent(collection, false); // false : pas de
        // modif du
        // schema.xml
        entityManager.getTransaction().commit();

        recordCollectionFieldBoosts.remove(index);
        target.addComponent(this);
    }

    // load est appelé pour obtenir la liste des elements
    @Override
    protected SortableListModel<RecordCollectionFieldBoost> getSortableListModel() {
        return new SortableListModel<RecordCollectionFieldBoost>() {
            @SuppressWarnings("unchecked")
            @Override
            protected List<RecordCollectionFieldBoost> load(String orderByProperty, Boolean orderByAsc) {
                return (List<RecordCollectionFieldBoost>) IndexFieldRelevancePanel.this
                .getModelObject();
            }
        };
    }

    @Override
    protected WebMarkupContainer createDeleteLink(String id, IModel rowItemModel, int index) {
        return super.createDeleteLink(id, rowItemModel, index);
    }

}*/
