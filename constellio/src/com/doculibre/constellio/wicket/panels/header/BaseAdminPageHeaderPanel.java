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
package com.doculibre.constellio.wicket.panels.header;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.wicket.links.SearchLinkHolder;
import com.doculibre.constellio.wicket.links.SignInLinkHolder;
import com.doculibre.constellio.wicket.links.SignOutLinkHolder;
import com.doculibre.constellio.wicket.links.SwitchLocaleLinkHolder;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class BaseAdminPageHeaderPanel extends BasePageHeaderPanel {

    public BaseAdminPageHeaderPanel(String id, Page owner) {
        super(id, owner);
        
        add(newLogoImg("logoImg"));
        add(new SearchLinkHolder("searchLinkHolder"));
        add(new SignInLinkHolder("signInLinkHolder"));
        add(new SignOutLinkHolder("signOutLinkHolder"));
        add(new SwitchLocaleLinkHolder("switchLocaleLinkHolder"));
        add(new Label("firstNameLastName", new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ConstellioUser user = ConstellioSession.get().getUser();
                return user == null ? "" : (user.getFirstName() + " " + user.getLastName());
            }
        }));
        add(new Label("username", new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ConstellioUser user = ConstellioSession.get().getUser();
                return user.getUsername();
            }
        }));
    }
    
    protected Component newLogoImg(String id) {
        return newSmallLogo(id);
    }

}
