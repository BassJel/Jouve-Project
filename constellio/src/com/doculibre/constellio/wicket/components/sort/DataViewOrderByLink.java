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

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.markup.repeater.data.DataView;

/**
 * 
 * 
 * @author Vincent Dussault
 */
@SuppressWarnings("serial")
public class DataViewOrderByLink extends OrderByLink {

    private DataView dataView;

    /**
     * @param id
     * @param property
     * @param stateLocator
     * @param cssProvider
     */
    public DataViewOrderByLink(String id, String property, DataView dataView,
            ICssProvider cssProvider) {
        super(id, property, (ISortStateLocator) dataView.getDataProvider(),
                cssProvider);
        this.dataView = dataView;
    }

    /**
     * @param id
     * @param property
     * @param stateLocator
     */
    public DataViewOrderByLink(String id, String property, DataView dataView) {
        super(id, property, (ISortStateLocator) dataView.getDataProvider());
        this.dataView = dataView;
    }

    protected void onSortChanged() {
        dataView.setCurrentPage(0);
    }
    
}
