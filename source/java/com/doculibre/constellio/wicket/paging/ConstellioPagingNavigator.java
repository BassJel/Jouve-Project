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
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.data.DataView;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.wicket.behaviors.StyleClassAppender;
import com.doculibre.constellio.wicket.data.FacetsDataProvider;
import com.doculibre.constellio.wicket.data.SearchResultsDataProvider;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;

@SuppressWarnings("serial")
public class ConstellioPagingNavigator extends PagingNavigator {

    public ConstellioPagingNavigator(String id, IPageable pageable) {
        super(id, pageable);
        initComponents();
    }

    public ConstellioPagingNavigator(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
        super(id, pageable, labelProvider);
        initComponents();
    }

    private void initComponents() {
    	String styleClass = getStyleClass();
    	if (styleClass != null) {
    		this.add(new StyleClassAppender(styleClass));
    	}
    }
    
    @Override
    public boolean isVisible() {
        return getPageable().getPageCount() > 1;
    }

    @Override
    protected PagingNavigation newNavigation(IPageable pageable, IPagingLabelProvider labelProvider) {
        PagingNavigation pagingNavigation = new PagingNavigation("navigation", pageable, labelProvider)
		{
			private static final long serialVersionUID = 1L;

			public boolean isEnabled() {
				return super.isEnabled() && ConstellioPagingNavigator.this.isEnabled() &&
				ConstellioPagingNavigator.this.isEnableAllowed();
			}

			@Override
			protected Link newPagingNavigationLink(String id,
					IPageable pageable, int pageIndex) {
				return ConstellioPagingNavigator.this.newPagingNavigationLink(id, pageable, pageIndex);
			}
			
		}; 
        pagingNavigation.setViewSize(5);
        return pagingNavigation;
    }

    @Override
    protected Link newPagingNavigationIncrementLink(String id, final IPageable pageable, final int increment) {
        DataView dataView = (DataView) pageable;
        SimpleSearch simpleSearch;
        if (dataView.getDataProvider() instanceof FacetsDataProvider) {
            FacetsDataProvider dataProvider = (FacetsDataProvider) dataView.getDataProvider();
            simpleSearch = dataProvider.getSimpleSearch();
        } else {
            SearchResultsDataProvider dataProvider = (SearchResultsDataProvider) dataView.getDataProvider();
            simpleSearch = dataProvider.getSimpleSearch();
        }
        SimpleSearch clone = simpleSearch.clone();
        int page = clone.getPage();
        int newPage = page + increment;
        clone.setPage(newPage);

        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
        return new BookmarkablePageLink(id, pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone)) {
            public boolean isEnabled() {
                return super.isEnabled() && ConstellioPagingNavigator.this.isEnabled()
                    && ConstellioPagingNavigator.this.isEnableAllowed();
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
                return pageable.getCurrentPage() <= 0;
            }

            /**
             * @return True if it is referring to the last page of the underlying PageableListView.
             */
            public boolean isLast() {
                return pageable.getCurrentPage() >= (pageable.getPageCount() - 1);
            }

            @Override
            public boolean isVisible() {
                return super.isVisible() && isEnabled();
            }
        }.setAutoEnable(true);
    }

    @Override
    protected Link newPagingNavigationLink(String id, final IPageable pageable, final int pageNumber) {
        DataView dataView = (DataView) pageable;
        SimpleSearch simpleSearch;
        if (dataView.getDataProvider() instanceof FacetsDataProvider) {
            FacetsDataProvider dataProvider = (FacetsDataProvider) dataView.getDataProvider();
            simpleSearch = dataProvider.getSimpleSearch();
        } else {
            SearchResultsDataProvider dataProvider = (SearchResultsDataProvider) dataView.getDataProvider();
            simpleSearch = dataProvider.getSimpleSearch();
        }
        SimpleSearch clone = simpleSearch.clone();
        if (pageNumber == -1) {
        	clone.setPage(pageable.getPageCount()-1);
        } else {
        	clone.setPage(pageNumber);
        }

        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
        return new BookmarkablePageLink(id, pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone)) {
            public boolean isEnabled() {
            	boolean enabled = super.isEnabled() && ConstellioPagingNavigator.this.isEnabled()
                        && ConstellioPagingNavigator.this.isEnableAllowed();
            	if (enabled) {
            		int currentPage = pageable.getCurrentPage();
                	if (!"first".equals(getId()) && !"last".equals(getId()) && currentPage == pageNumber) {
                		enabled = false;
                	}
            	}
            	return enabled;
            }

            @Override
            public boolean isVisible() {
            	boolean visible = super.isVisible();
            	if (visible) {
            		int currentPage = pageable.getCurrentPage();
            		int pageCount = pageable.getPageCount();
                	if ("first".equals(getId()) && currentPage == 0) {
                		visible = false;
                	} else if ("last".equals(getId()) && currentPage == (pageCount - 1)) {
                		visible = false;
                	}
            	}
            	return visible;
            }
        };
    }
    
    protected String getStyleClass() {
    	return "pagination";
    }

}
