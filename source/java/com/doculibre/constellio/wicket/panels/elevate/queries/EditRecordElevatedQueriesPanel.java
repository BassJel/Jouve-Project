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
package com.doculibre.constellio.wicket.panels.elevate.queries;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.ElevateServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.models.RecordModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.search.query.SimpleSearchQueryPanel;

@SuppressWarnings("serial")
public class EditRecordElevatedQueriesPanel extends AjaxPanel {
	
	private SimpleSearch simpleSearch;
	
	private RecordModel recordModel;
    
	private WebMarkupContainer queriesContainer;
	
	private List<String> queries = new ArrayList<String>();

	public EditRecordElevatedQueriesPanel(String id, Record record, final SimpleSearch simpleSearch) {
		super(id);
		this.recordModel = new RecordModel(record);
		this.simpleSearch = simpleSearch;
		final String collectionName = simpleSearch.getCollectionName();

    	ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = collectionServices.get(collectionName);
    	List<String> elevatedQueries = elevateServices.getElevatedQueries(record, collection);
    	queries.addAll(elevatedQueries);
		
		String titleKey = "queries";
		IModel titleModel = new StringResourceModel(titleKey, this, null);
		add(new Label("panelTitle", titleModel));
		
		queriesContainer = new WebMarkupContainer("queriesContainer");
		add(queriesContainer);
		queriesContainer.setOutputMarkupId(true);
		
		add(new AddQueryForm("addForm"));
		
		queriesContainer.add(new ListView("queries", queries) {
			@Override
			protected void populateItem(ListItem item) {
				final String query = (String) item.getModelObject();
				ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
				SimpleSearch simpleSearch = elevateServices.toSimpleSearch(query);
				item.add(new SimpleSearchQueryPanel("query", simpleSearch));
				
				item.add(new AjaxLink("deleteLink") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						queries.remove(query);
						target.addComponent(queriesContainer);
					}

					@Override
					protected IAjaxCallDecorator getAjaxCallDecorator() {
						return new AjaxCallDecorator() {
							@Override
							public CharSequence decorateScript(CharSequence script) {
								String confirmMsg = getLocalizer().getString("confirmDelete", EditRecordElevatedQueriesPanel.this);
								return "if (confirm('" + confirmMsg + "')) {" + script + "} else { return false; }";
							}
						};
					}
				});
			}
		});
		
		Form form = new Form("form");
		add(form);
		form.add(new AjaxButton("submitButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
            	ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				RecordCollection collection = collectionServices.get(collectionName);
            	Record record = recordModel.getObject();
            	List<String> originalQueries = elevateServices.getElevatedQueries(record, collection);
            	for (String originalQuery : originalQueries) {
					if (!queries.contains(originalQuery)) {
						SimpleSearch originalQuerySimpleSearch = elevateServices.toSimpleSearch(originalQuery);
						elevateServices.cancelElevation(record, originalQuerySimpleSearch);
					}
				}

            	for (String query : queries) {
					SimpleSearch querySimpleSearch = elevateServices.toSimpleSearch(query);
            		if (!elevateServices.isElevated(record, querySimpleSearch)) {
            			elevateServices.elevate(record, querySimpleSearch);
            		}
				}
            	ModalWindow.closeCurrent(target);
            }
        });
		form.add(new AjaxButton("cancelButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
            	ModalWindow.closeCurrent(target);
            }
        }.setDefaultFormProcessing(false));
	}

	@Override
	public void detachModels() {
		recordModel.detach();
		super.detachModels();
	}
	
	public class AddQueryForm extends Form {
		
		private String query;

		public AddQueryForm(String id) {
			super(id);
			
			final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
			feedbackPanel.setOutputMarkupId(true);
			add(feedbackPanel);
			
			final TextField queryField = new TextField("query", new PropertyModel(this, "query"));
			queryField.setOutputMarkupId(true);
			add(queryField);
			
			add(new AjaxButton("addButton") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form form) {
					if (StringUtils.isNotBlank(query)) {
						ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
						SimpleSearch clone = simpleSearch.clone();
						clone.setQuery(query);
						String newElevateQueryId = elevateServices.toElevateQueryId(clone);
						boolean alreadyElevated = false;
						for (String queryParams : queries) {
							SimpleSearch elevatedSimpleSearch = SimpleSearch.toSimpleSearch(queryParams);
							String elevateQueryId = elevateServices.toElevateQueryId(elevatedSimpleSearch);
							if (newElevateQueryId.equals(elevateQueryId)) {
								alreadyElevated = true;
								break;
							}
						}
						if (!alreadyElevated) {
							queries.add(clone.toSimpleParams().toString());
							queryField.clearInput();
							query = null;
							target.addComponent(queryField);
						} else {
							error(getLocalizer().getString("alreadyElevated", this));
						}
						target.addComponent(feedbackPanel);
						target.addComponent(queriesContainer);
					}
				}
			});
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}
		
	}

}
