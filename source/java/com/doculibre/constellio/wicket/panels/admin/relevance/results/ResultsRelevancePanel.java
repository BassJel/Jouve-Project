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
package com.doculibre.constellio.wicket.panels.admin.relevance.results;

import javax.persistence.EntityManager;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.relevance.ResultsRelevance;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class ResultsRelevancePanel extends AjaxPanel {
	
	private Form form;
	
	private Integer minClicks = null;

	private Integer maxResults = null;

	public ResultsRelevancePanel(String id) {
		super(id);
		form = new Form("form");
		
		form.setModel(new CompoundPropertyModel(this));// a cause de repetitions

		TextField minClicks	= new RequiredTextField("minClicks", Integer.class);
		form.add(minClicks);
		
		TextField maxResults	= new RequiredTextField("maxResults", Integer.class);
		form.add(maxResults);
		
//		initFields();
		
		Button synchronizeSolrconfigLink = new Button("submitButton") {
			@Override
            public void onSubmit() {
                updateSolrConfigFile(true);
            }

            @Override
            public boolean isVisible() {
                boolean visible = super.isVisible();
                if (visible) {
                    AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                    RecordCollection collection = collectionAdminPanel.getCollection();
                    ResultsRelevance resultsRelevance = collection.getResultsRelevance();
                    visible = resultsRelevance == null || !resultsRelevance.isActive();
                }
                return visible;
            }
		};
		form.add(synchronizeSolrconfigLink);
		
		Button backToDefaultDistance = new Button("cancelButton") {
            @Override
            public void onSubmit() {
                updateSolrConfigFile(false);
            }

            @Override
            public boolean isVisible() {
                boolean visible = super.isVisible();
                if (visible) {
                    AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                    RecordCollection collection = collectionAdminPanel.getCollection();
                    ResultsRelevance resultsRelevance = collection.getResultsRelevance();
                    visible = resultsRelevance != null && resultsRelevance.isActive();
                }
                return visible;
            }
		};
		form.add(backToDefaultDistance);
		
		this.add(form);
	}
	
	private void initFields() {
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		RecordCollection collection = collectionAdminPanel.getCollection();
		ResultsRelevance resultsRelevance = collection.getResultsRelevance();
		if (resultsRelevance != null){
			setMinClicks(resultsRelevance.getMinClicks());
			setMaxResults(resultsRelevance.getMaxResults());
		}else {
			setMinClicks(0);
			setMaxResults(0);
		}
	}

	protected void updateSolrConfigFile(boolean useResultsRelevance) {
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		RecordCollection collection = collectionAdminPanel.getCollection();
		ResultsRelevance resultsRelevance = collection.getResultsRelevance();
        
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        
		if (resultsRelevance == null){
			if (!useResultsRelevance){
				//Nothing to do
				return;
			} else {
				resultsRelevance = new ResultsRelevance();
				resultsRelevance.setRecordCollection(collection);
				collection.setResultsRelevance(resultsRelevance);
				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
			    collectionServices.makePersistent(collection, false);
			}
		}
		if (useResultsRelevance){
			resultsRelevance.setMaxResults(maxResults);
			resultsRelevance.setMinClicks(minClicks);
		}
		resultsRelevance.setActive(useResultsRelevance);

        entityManager.persist(resultsRelevance);
        entityManager.getTransaction().commit();
	}

	public int getMinClicks() {
		if (minClicks == null){
			initFields();
		}
		return minClicks;
	}

	public void setMinClicks(int minClicks) {
		this.minClicks = minClicks;
	}

	public int getMaxResults() {
		if(maxResults == null){
			initFields();
		}
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

}
