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
package com.doculibre.constellio.wicket.components.sort;

import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;

/**
 * 
 * 
 * @author Vincent Dussault
 */
@SuppressWarnings("serial")
public class AutoHidePagingNavigation extends PagingNavigation {

    /**
     * @param id
     * @param pageable
     */
    public AutoHidePagingNavigation(String id, IPageable pageable) {
        super(id, pageable);
    }
    /**
     * @param id
     * @param pageable
     * @param labelProvider
     */
    public AutoHidePagingNavigation(String id, IPageable pageable,
            IPagingLabelProvider labelProvider) {
        super(id, pageable, labelProvider);
    }
    
    
    /**
     * @see org.apache.wicket.Component#isVisible()
     */
    public boolean isVisible() {
        return pageable.getPageCount() > 1;
    }
    
}
