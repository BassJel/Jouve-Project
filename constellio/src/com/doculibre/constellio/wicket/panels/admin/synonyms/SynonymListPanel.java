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
package com.doculibre.constellio.wicket.panels.admin.synonyms;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SynonymList;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.services.SynonymServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;

@SuppressWarnings("serial")
public class SynonymListPanel extends SingleColumnCRUDPanel {

	public SynonymListPanel(String id) {
		super(id);
		
		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				return new ArrayList<SynonymList>(collection.getSynonymLists());
			}
		});
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddEditSynonymsPanel(id, new SynonymList());
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
		SynonymList synonymList = (SynonymList) entityModel.getObject();
		return new AddEditSynonymsPanel(id, synonymList);
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		StringBuffer sb = new StringBuffer();
		SynonymList synonymList = (SynonymList) entity;
		for (Iterator<String> it = synonymList.getSynonyms().iterator(); it.hasNext();) {
			String synonym = (String) it.next();
			sb.append(synonym);
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	@Override
	protected BaseCRUDServices<SynonymList> getServices() {
		return ConstellioSpringUtils.getSynonymServices();
	}

	@Override
	protected void onClickDeleteLink(IModel entityModel,
			AjaxRequestTarget target, int index) {
		super.onClickDeleteLink(entityModel, target, index);
		if (SolrServices.synonymsFilterActivated){
			//necessaire si : utilisation d un autre filtre des synonymes
			AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
			RecordCollection collection = collectionAdminPanel.getCollection();
			SynonymServices synonymServices = ConstellioSpringUtils.getSynonymServices();
			synonymServices.writeSynonymsFile(collection);
			//relancer la lecture des synonymes
			SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
			solrServices.initCore(collection);
		}
	}
	
	

}
