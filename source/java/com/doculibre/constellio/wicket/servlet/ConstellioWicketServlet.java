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
package com.doculibre.constellio.wicket.servlet;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;

@SuppressWarnings("serial")
public class ConstellioWicketServlet extends WicketServlet {

	@Override
	protected WicketFilter newWicketFilter() {
		return new WicketFilter() {
			@Override
			protected ClassLoader getClassLoader() {
				// PluginAwareClassLoader pluginAwareClassLoader = new
				// PluginAwareClassLoader();
				// return pluginAwareClassLoader;
				return super.getClassLoader();
			}
		};
	}

}
