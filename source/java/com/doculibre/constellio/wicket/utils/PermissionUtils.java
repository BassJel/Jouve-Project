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
package com.doculibre.constellio.wicket.utils;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.session.ConstellioSession;

public class PermissionUtils {
    
    public boolean hasCurrentUserSearchPermission(String collectionName) {
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordCollection collection = collectionServices.get(collectionName);
        return hasCurrentUserSearchPermission(collection);
    }    
    
    public boolean hasCurrentUserSearchPermission(RecordCollection collection) {
        boolean result;
        ConstellioSession session = ConstellioSession.get();
        ConstellioUser user = session.getUser();
        if (collection != null) {
            if (user == null && !collection.hasSearchPermission()) {
                result = true;
            } else if (user != null && user.hasSearchPermission(collection)) {
                result = true;
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }
    
    public boolean hasCurrentUserCollaborationPermission(String collectionName) {
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordCollection collection = collectionServices.get(collectionName);
        return hasCurrentUserCollaborationPermission(collection);
    }    
    
    public boolean hasCurrentUserCollaborationPermission(RecordCollection collection) {
        boolean result;
        ConstellioSession session = ConstellioSession.get();
        ConstellioUser user = session.getUser();
        if (collection != null) {
            if (user != null && user.hasCollaborationPermission(collection)) {
                result = true;
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }
    
    public boolean hasCurrentUserAdminPermission(String collectionName) {
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        RecordCollection collection = collectionServices.get(collectionName);
        return hasCurrentUserAdminPermission(collection);
    }    
    
    public boolean hasCurrentUserAdminPermission(RecordCollection collection) {
        boolean result;
        ConstellioSession session = ConstellioSession.get();
        ConstellioUser user = session.getUser();
        if (collection != null) {
            if (user != null && user.hasAdminPermission(collection)) {
                result = true;
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

}
