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
package com.doculibre.constellio.wicket.panels.admin.indexField;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.indexing.IndexingManager;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class AdminIndexFieldsPanel extends AjaxPanel {
	
	public AdminIndexFieldsPanel(String id) {
		super(id);
		
		IModel queryParserOperatorModel = new Model() {
			@Override
			public Object getObject() {
				return getRecordCollection().getQueryParserOperator();
			}

			@Override
			public void setObject(Object object) {
				getRecordCollection().setQueryParserOperator((String) object);
			}
		};
		List<String> queryParserOperatorChoices = new ArrayList<String>();
		queryParserOperatorChoices.add(RecordCollection.QUERY_PARSER_OPERATOR_AND);
		queryParserOperatorChoices.add(RecordCollection.QUERY_PARSER_OPERATOR_OR);
		DropDownChoice queryParserOperatorField = new DropDownChoice("queryParserOperator", queryParserOperatorModel, queryParserOperatorChoices);
		add(queryParserOperatorField);
		queryParserOperatorField.setNullValid(true);
		queryParserOperatorField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				RecordCollection collection = getRecordCollection();

				EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
				if (!entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().begin();
				}
				collectionServices.makePersistent(collection);
				entityManager.getTransaction().commit();
			}
		});
		
		add(new Link("applyChangesLink") {
			@Override
			public void onClick() {
				RecordCollection collection = getRecordCollection();
				
				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
				
				solrServices.updateSchemaFields(collection);
				solrServices.initCore(collection);
				
				EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
				if (!entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().begin();
				}
				collectionServices.markSynchronized(collection);
				entityManager.getTransaction().commit();
		        
		        IndexingManager indexingManager = IndexingManager.get(collection);
		        if (!indexingManager.isActive()) {
		            indexingManager.startIndexing();
		        }
			}

			@Override
			public boolean isVisible() {
				return super.isVisible() && getRecordCollection().isSynchronizationRequired();
			}
		});
		
		add(new IndexFieldListPanel("indexFieldListPanel"));
	}
	
	private RecordCollection getRecordCollection() {
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		RecordCollection collection = collectionAdminPanel.getCollection();
		return collection;	
	}

}
