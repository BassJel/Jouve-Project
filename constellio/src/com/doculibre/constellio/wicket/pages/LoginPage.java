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

import org.apache.wicket.RequestCycle;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.wicket.panels.signIn.ConstellioSignInPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;


public class LoginPage extends BaseConstellioPage {

	public LoginPage() {
		super();
		ConstellioUser user = ConstellioSession.get().getUser();
		if (user != null) {
	    	PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
			if (user.isAdmin()) {
				RequestCycle.get().setResponsePage(pageFactoryPlugin.getAdminPage());
			} else {
				RequestCycle.get().setResponsePage(pageFactoryPlugin.getSearchFormPage());
			}
		}
		ConstellioSignInPanel signInPanel = new ConstellioSignInPanel("signInPanel");
		add(signInPanel);
	}

}
