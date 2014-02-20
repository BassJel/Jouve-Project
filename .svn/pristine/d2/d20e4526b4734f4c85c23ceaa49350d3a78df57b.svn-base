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
package com.doculibre.constellio.wicket.pages;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.admin.tabs.AdminTopMenuPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

public class AdminPage extends BaseConstellioPage {

	public AdminPage() {
	    super();
	    ConstellioSession session = ConstellioSession.get();
	    ConstellioUser user = session.getUser();
	    boolean redirect;
	    if (user == null) {
	        redirect = true;
	    } else if (user.isAdmin()) {
	        redirect = false;
	    } else {
	        redirect = true;
	        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
	        for (RecordCollection collection : collectionServices.list()) {
                if (user.hasCollaborationPermission(collection) || user.hasAdminPermission(collection)) {
                    redirect = false;
                    break;
                }
            }
	    }
	    if (redirect) {
	        setResponsePage(getApplication().getHomePage());
	    } else {
	        add(new AdminTopMenuPanel("tabbedPanel"));
	    }
	}
}
