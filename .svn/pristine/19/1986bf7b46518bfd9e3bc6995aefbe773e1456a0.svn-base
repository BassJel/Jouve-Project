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
package com.doculibre.constellio.wicket.panels.admin.searchResultField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchResultField;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;

@SuppressWarnings("serial")
public class AddSearchResultFieldPanel extends SaveCancelFormPanel {

    private ReloadableEntityModel<SearchResultField> searchResultFieldModel;

    private DropDownChoice indexFieldField;

    public AddSearchResultFieldPanel(String id, SearchResultField searchResultField) {
        super(id, true);
        this.searchResultFieldModel = new ReloadableEntityModel<SearchResultField>(searchResultField);

        Form form = getForm();
        form.setModel(new CompoundPropertyModel(searchResultFieldModel));

        IModel indexFieldsModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                Set<IndexField> indexFields = new HashSet<IndexField>();
                SearchResultFieldListPanel searchResultListPanel = (SearchResultFieldListPanel) findParent(SearchResultFieldListPanel.class);
                RecordCollection collection = searchResultListPanel.getCollection();
                List<SearchResultField> existingSearchResultFields = collection.getSearchResultFields();
                for (IndexField indexField : collection.getIndexFields()) {
                    boolean exists = false;
                    loop2: for (SearchResultField existingSearchResultField : existingSearchResultFields) {
                        if (existingSearchResultField.getIndexField().equals(indexField)) {
                            exists = true;
                            break loop2;
                        }
                    }
                    if (!exists) {
                        indexFields.add(indexField);
                    }
                }
                return new ArrayList<IndexField>(indexFields);
            }
        };
        IChoiceRenderer indexFieldRenderer = new ChoiceRenderer() {
            @Override
            public Object getDisplayValue(Object object) {
                IndexField indexField = (IndexField) object;
                return indexField.getName();
            }
        };
        indexFieldField = new DropDownChoice("indexField", indexFieldsModel, indexFieldRenderer);
        form.add(indexFieldField);
    }

    @Override
    public void detachModels() {
        searchResultFieldModel.detach();
        super.detachModels();
    }

    @Override
    protected IModel getTitleModel() {
        return new LoadableDetachableModel() {
            @Override
            protected Object load() {
                String titleKey = "add";
                return new StringResourceModel(titleKey, AddSearchResultFieldPanel.this, null).getObject();
            }
        };
    }

    @Override
    protected Component newReturnComponent(String id) {
        return null;
    }

    @Override
    protected void onSave(AjaxRequestTarget target) {
        SearchResultFieldListPanel searchResultListPanel = (SearchResultFieldListPanel) findParent(SearchResultFieldListPanel.class);
        RecordCollection collection = searchResultListPanel.getCollection();

        SearchResultField searchResultField = searchResultFieldModel.getObject();
        collection.addSearchResultField(searchResultField);

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        
        collectionServices.makePersistent(collection, false);
        entityManager.getTransaction().commit();
    }

    @Override
    protected void defaultReturnAction(AjaxRequestTarget target) {
        super.defaultReturnAction(target);
        SearchResultFieldListPanel searchResultListPanel = (SearchResultFieldListPanel) findParent(SearchResultFieldListPanel.class);
        target.addComponent(searchResultListPanel);
    }

}
