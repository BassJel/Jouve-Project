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
package com.doculibre.constellio.wicket.paging;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.wicket.data.FacetsDataProvider;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;

@SuppressWarnings("serial")
public class ConstellioFacetsPagingNavigator extends ConstellioPagingNavigator {

	private String facetName;
	
	private FacetsDataProvider dataProvider;
	
	public ConstellioFacetsPagingNavigator(String id, IPageable pageable, FacetsDataProvider dataProvider,
			IPagingLabelProvider labelProvider, String facetName) {
		super(id, pageable, labelProvider);
		this.facetName = facetName;
		this.dataProvider = dataProvider;
	}

	public ConstellioFacetsPagingNavigator(String id, IPageable pageable, FacetsDataProvider dataProvider, String facetName) {
		super(id, pageable);
		this.facetName = facetName;
		this.dataProvider = dataProvider;
	}

    @Override
    public boolean isVisible() {
        return getPageable().getPageCount() > 1;
    }

    @Override
    protected PagingNavigation newNavigation(IPageable pageable, IPagingLabelProvider labelProvider) {
        PagingNavigation pagingNavigation = super.newNavigation(pageable, labelProvider);
        return pagingNavigation;
    }

    @Override
    protected Link newPagingNavigationIncrementLink(String id, final IPageable pageable, final int increment) {
        SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
        SimpleSearch clone = simpleSearch.clone();
        int page = clone.getFacetPage(facetName);
        int newPage = page + increment;
        clone.setFacetPage(facetName, newPage);

        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
        return new BookmarkablePageLink(id, pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone)) {
            public boolean isEnabled() {
                return super.isEnabled() && ConstellioFacetsPagingNavigator.this.isEnabled()
                    && ConstellioFacetsPagingNavigator.this.isEnableAllowed();
            }

            /**
             * Returns true if the page link links to the given page.
             * 
             * @param page
             *            ignored
             * @return True if this link links to the given page
             * @see org.apache.wicket.markup.html.link.PageLink#linksTo(org.apache.wicket.Page)
             */
            public boolean linksTo(final Page page) {
            	pageable.getCurrentPage();
                return ((increment < 0) && isFirst()) || ((increment > 0) && isLast());
            }

            /**
             * @return True if it is referring to the first page of the underlying PageableListView.
             */
            public boolean isFirst() {
            	int currentPage = pageable.getCurrentPage();
                return currentPage <= 0;
            }

            /**
             * @return True if it is referring to the last page of the underlying PageableListView.
             */
            public boolean isLast() {
            	int currentPage = pageable.getCurrentPage();
                return currentPage >= (pageable.getPageCount() - 1);
            }

            @Override
            public boolean isVisible() {
                return super.isVisible() && isEnabled();
            }
        }.setAutoEnable(true);
    }

    @Override
    protected Link newPagingNavigationLink(String id, final IPageable pageable, final int pageNumber) {
        SimpleSearch simpleSearch = dataProvider.getSimpleSearch();

        SimpleSearch clone = simpleSearch.clone();
        if (pageNumber == -1) {
        	clone.setFacetPage(facetName, pageable.getPageCount()-1);
        } else {
        	clone.setFacetPage(facetName, pageNumber);
        }

        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
        return new BookmarkablePageLink(id, pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone)) {
            public boolean isEnabled() {
                return super.isEnabled() && ConstellioFacetsPagingNavigator.this.isEnabled()
                    && ConstellioFacetsPagingNavigator.this.isEnableAllowed();
            }

            /**
             * Returns true if this PageableListView navigation link links to the given page.
             * 
             * @param page
             *            The page
             * @return True if this link links to the given page
             * @see org.apache.wicket.markup.html.link.PageLink#linksTo(org.apache.wicket.Page)
             */
            public final boolean linksTo(final Page page) {
                return pageNumber == pageable.getCurrentPage();
            }

            @Override
            public boolean isVisible() {
                return super.isVisible() && (this.isEnabled() || linksTo(getPage()));
            }
        }.setAutoEnable(true);
    }

	@Override
	protected String getStyleClass() {
		return "paginationFacet";
	}
}
