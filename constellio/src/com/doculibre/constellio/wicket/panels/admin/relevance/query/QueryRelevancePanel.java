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
package com.doculibre.constellio.wicket.panels.admin.relevance.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.EntityListModel;
import com.doculibre.constellio.wicket.models.SortableListModel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.CRUDPanel;

@SuppressWarnings("serial")
public class QueryRelevancePanel extends CRUDPanel<IndexField> {// SingleColumnCRUDPanel

	private EntityListModel<IndexField> collectionSortedIndexFieldListModel = null;
	private Form form;

	public QueryRelevancePanel(String id) {
		super(id, 10);//  enleve car index donne dans createEditContent ne
					// correspond pas sinon
		form = new Form("form");
		form.setOutputMarkupId(true);
		
		this.add(form);

		AjaxButton saveButton = new AjaxButton("saveButton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
				updateSolrConfigFile(solrServices.usesDisMax(collection));
				target.addComponent(form);
			}
		};
		form.add(saveButton);
		
		AjaxButton enableDismaxButton = new AjaxButton("enableDismaxButton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {
				updateSolrConfigFile(true);
				target.addComponent(form);
			}

			@Override
			public boolean isVisible() {
				boolean visible = super.isVisible();
				if (visible) {
					AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
					RecordCollection collection = collectionAdminPanel.getCollection();
					SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
					visible = !solrServices.usesDisMax(collection);
				}
				return visible;
			}
		};
		form.add(enableDismaxButton);
		
		AjaxButton disableDismaxButton = new AjaxButton("disableDismaxButton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {
				updateSolrConfigFile(false);
				target.addComponent(form);
			}

			@Override
			public boolean isVisible() {
				boolean visible = super.isVisible();
				if (visible) {
					AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
					RecordCollection collection = collectionAdminPanel.getCollection();
					SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
					visible = solrServices.usesDisMax(collection);
				}
				return visible;
			}
		};
		form.add(disableDismaxButton);
		
	}

	protected void updateSolrConfigFile(boolean useDismaxDistance) {
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		RecordCollection collection = collectionAdminPanel.getCollection();
		SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
		if (useDismaxDistance){
			solrServices.updateDismax(collection);
		} else {
			solrServices.resetDefaultDistance(collection);
		}
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		collectionServices.markSynchronized(collection);
        entityManager.getTransaction().commit();
        
        IndexingManager indexingManager = IndexingManager.get(collection);
        if (!indexingManager.isActive()) {
            indexingManager.startIndexing();
        }
	}

	// choix du panel qui s affiche apres clic create
	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return null;
	}

	// choix du panel qui s affiche apres clic edit
	@Override
	protected WebMarkupContainer createEditContent(final String id,
			IModel entityModel, final int index) {
		return new EditQueryRelevancePanel(id, new Model() {
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
		dataColumns.add(new PropertyColumn(new StringResourceModel("boostDismax", this, null), "boostDismax"));

		return dataColumns;
	}

	@Override
	protected boolean isDeleteLink(IModel rowItemModel, int index) {
		return false;
	}

	// jamais appelé car isDeleteLink == false
	@Override
	protected void onClickDeleteLink(IModel rowItemModel,
			AjaxRequestTarget target, int index) {

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

}
