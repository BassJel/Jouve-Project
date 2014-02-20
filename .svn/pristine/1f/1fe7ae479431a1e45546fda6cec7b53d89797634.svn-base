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
package com.doculibre.constellio.wicket.models;

import java.util.ArrayList;
import java.util.List;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class VisibleCollectionsModel extends SortableListModel<RecordCollection> {

    @Override
    protected List<RecordCollection> load(String orderByProperty, Boolean orderByAsc) {
    	if (orderByProperty == null) {
    		orderByProperty = "position";
    		orderByAsc = true;
    	}
    	
        List<RecordCollection> visibleCollections = new ArrayList<RecordCollection>();
        ConstellioUser currentUser = ConstellioSession.get().getUser();
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        for (RecordCollection collection : collectionServices.listPublic(orderByProperty, orderByAsc)) {
            boolean visible = false;
            if (!collection.hasSearchPermission()) {
                visible = true;
            } else if (currentUser != null && currentUser.hasSearchPermission(collection)) {
                visible = true;
            } else {
                visible = false;
            }
            if (visible) {
                visibleCollections.add(collection);
            }
        }
        return visibleCollections;
    }

}
