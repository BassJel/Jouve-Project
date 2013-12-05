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

import java.io.Serializable;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SynonymList;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.services.SynonymServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class AddEditSynonymsPanel extends SaveCancelFormPanel {

	private ReloadableEntityModel<SynonymList> synonymListModel;

	public AddEditSynonymsPanel(String id, SynonymList synonymList) {
		super(id, true);
		this.synonymListModel = new ReloadableEntityModel<SynonymList>(synonymList);

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(synonymListModel));

		IModel synonymsFieldModel = new Model() {
			@Override
			public Object getObject() {
				StringBuffer sb = new StringBuffer();
				SynonymList synonymList = synonymListModel.getObject();
				for (Iterator<String> it = synonymList.getSynonyms().iterator(); it.hasNext();) {
					String synonym = (String) it.next();
					sb.append(synonym);
					if (it.hasNext()) {
						sb.append("\n");
					}
				}
				return sb.toString();
			}

			@Override
			public void setObject(Serializable object) {
				SynonymList synonymList = synonymListModel.getObject();
				synonymList.getSynonyms().clear(); // Reset
				StringTokenizer st = new StringTokenizer((String) object, "\n");
				while (st.hasMoreTokens()) {
					String synonym = (String) st.nextToken();
					synonymList.getSynonyms().add(StringUtils.trim(synonym));
				}
			}
		};
		TextArea synonyms = new TextArea("synonyms", synonymsFieldModel);
		synonyms.setOutputMarkupId(true);
		synonyms.setRequired(true);
		form.add(synonyms);
	}

	@Override
	public void detachModels() {
		synonymListModel.detach();
		super.detachModels();
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			@Override
			protected Object load() {
				SynonymList synonymList = synonymListModel.getObject();
		        String titleKey = synonymList == null || synonymList.getId() == null ? "add" : "edit";
		        return new StringResourceModel(titleKey, AddEditSynonymsPanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		RecordCollection collection = collectionAdminPanel.getCollection();
		
		SynonymList synonymList = synonymListModel.getObject();
		synonymList.setRecordCollection(collection);

		SynonymServices synonymServices = ConstellioSpringUtils.getSynonymServices();
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		synonymServices.makePersistent(synonymList);
		entityManager.getTransaction().commit();
		
		if (SolrServices.synonymsFilterActivated){
			//necessaire si : utilisation d un autre filtre des synonymes
			synonymServices.writeSynonymsFile(collection);
			//relancer la lecture des synonymes
			SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
			solrServices.initCore(collection);
		}
		
	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		target.addComponent(collectionAdminPanel);
	}

}
