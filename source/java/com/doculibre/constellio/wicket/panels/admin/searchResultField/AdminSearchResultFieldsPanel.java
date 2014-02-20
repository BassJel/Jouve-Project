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

import java.util.Locale;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public class AdminSearchResultFieldsPanel extends AjaxPanel {
    
    public AdminSearchResultFieldsPanel(String id) {
        super(id);
        
        IModel collectionsModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
                return collectionServices.list();
            }
        };
        ListView collectionsListView = new ListView("collections", collectionsModel) {
            @Override
            protected void populateItem(ListItem item) {
                RecordCollection collection = (RecordCollection) item.getModelObject();
                Locale displayLocale = collection.getDisplayLocale(getLocale());
                item.add(new Label("collection", collection.getTitle(displayLocale)));
                item.add(new SearchResultFieldListPanel("crudPanel", collection));
            }
        };
        add(collectionsListView);
    }

}
