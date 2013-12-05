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
package com.doculibre.constellio.plugins.defaults.wicket;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;

import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.plugins.defaults.DefaultConstellioPlugin;
import com.doculibre.constellio.wicket.pages.AdminPage;
import com.doculibre.constellio.wicket.pages.BaseSearchPage;
import com.doculibre.constellio.wicket.pages.LoginPage;
import com.doculibre.constellio.wicket.pages.SearchFormPage;
import com.doculibre.constellio.wicket.pages.SearchHistoryPage;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;

@PluginImplementation
public class DefaultPageFactoryPlugin extends DefaultConstellioPlugin implements PageFactoryPlugin {

	@Override
	public Class<? extends BaseSearchPage> getSearchFormPage() {
		return SearchFormPage.class;
	}

	@Override
	public Class<? extends BaseSearchPage> getSearchResultsPage() {
		return SearchResultsPage.class;
	}

	@Override
	public Class<? extends WebPage> getSearchHistoryPage() {
		return SearchHistoryPage.class;
	}

	@Override
	public Class<? extends WebPage> getLoginPage() {
		return LoginPage.class;
	}

	@Override
	public Class<? extends AdminPage> getAdminPage() {
		return AdminPage.class;
	}

	@Override
	public boolean isSearchFormPage(Page page) {
		return getSearchFormPage().isAssignableFrom(page.getClass());
	}

	@Override
	public boolean isSearchResultsPage(Page page) {
		return getSearchResultsPage().isAssignableFrom(page.getClass());
	}

	@Override
	public boolean isSearchHistoryPage(Page page) {
		return getSearchHistoryPage().isAssignableFrom(page.getClass());
	}

	@Override
	public boolean isLoginPage(Page page) {
		return getLoginPage().isAssignableFrom(page.getClass());
	}

	@Override
	public boolean isAdminPage(Page page) {
		return getAdminPage().isAssignableFrom(page.getClass());
	}

}
