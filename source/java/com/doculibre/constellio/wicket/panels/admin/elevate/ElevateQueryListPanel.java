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
package com.doculibre.constellio.wicket.panels.admin.elevate;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.ElevateServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;
import com.doculibre.constellio.wicket.panels.admin.elevate.modal.DocIdsPanel;
import com.doculibre.constellio.wicket.panels.admin.elevate.modal.ElevateQueryDocIdsPanel;
import com.doculibre.constellio.wicket.panels.search.query.SimpleSearchQueryPanel;

@SuppressWarnings("serial")
public class ElevateQueryListPanel extends AjaxPanel {

	public ElevateQueryListPanel(String id) {
		super(id);
        
        add(new DocIdsPanel("excludedDocIdsPanel", null, false));
		
		IModel elevateQueriesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				String solrCoreName = collection.getName();
				
				ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
				return elevateServices.getQueries(solrCoreName);
			}
		};
		
		add(new ListView("queries", elevateQueriesModel) {
			@Override
			protected void populateItem(ListItem item) {
				final String queryText = (String) item.getModelObject();
				ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
				final SimpleSearch querySimpleSearch = elevateServices.toSimpleSearch(queryText);
				
				int nbDocs = elevateServices.getElevatedDocIds(querySimpleSearch).size();
				item.add(new Label("nbDocs", "" + nbDocs));
				
				final ModalWindow detailsModal = new ModalWindow("detailsModal");
				item.add(detailsModal);
				detailsModal.setInitialHeight(SingleColumnCRUDPanel.MODAL_HEIGHT);
				detailsModal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
				
				WebMarkupContainer detailsLink = new AjaxLink("detailsLink") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						detailsModal.setContent(new ElevateQueryDocIdsPanel(detailsModal.getContentId(), queryText));
//						detailsModal.setTitle(queryText);
						detailsModal.setWindowClosedCallback(new WindowClosedCallback() {
							@Override
							public void onClose(AjaxRequestTarget target) {
								target.addComponent(ElevateQueryListPanel.this);
							}
						});
						detailsModal.show(target);
					}
				};
				item.add(detailsLink);
				detailsLink.add(new SimpleSearchQueryPanel("queryText", querySimpleSearch));
				
				WebMarkupContainer deleteLink = new AjaxLink("deleteLink") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
						elevateServices.deleteQuery(querySimpleSearch);
						target.addComponent(ElevateQueryListPanel.this);
					}

					@Override
					protected IAjaxCallDecorator getAjaxCallDecorator() {
						return new AjaxCallDecorator() {
							@Override
							public CharSequence decorateScript(CharSequence script) {
								String confirmMsg = getLocalizer().getString("confirmDelete", ElevateQueryListPanel.this);
								return "if (confirm('" + confirmMsg + "')) {" + script + "} else { return false; }";
							}
						};
					}
				};
				item.add(deleteLink);
			}
		});
	}

}
