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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * 
 * 
 * @author Vincent Dussault
 */
@SuppressWarnings("serial")
public class SortablePageableListViewDataProvider implements ISortableDataProvider {

    private PageableListView pageableListView;

    private BasicSortState sortState = new BasicSortState();

    /**
     * @param list
     */
    public SortablePageableListViewDataProvider(PageableListView pageableListView) {
        super();
        this.pageableListView = pageableListView;
    }

    /**
     * @see org.apache.wicket.markup.repeater.data.IDataProvider#iterator(int,
     *      int)
     */
    @SuppressWarnings("unchecked")
	public Iterator<? extends Object> iterator(int first, int count) {
        List<? extends Object> list = pageableListView.getList();
        int toIndex = first + count;
        if (toIndex > list.size()) {
            toIndex = list.size();
        }
        return list.subList(first, toIndex).listIterator();
    }

    /**
     * @see org.apache.wicket.markup.repeater.data.IDataProvider#size()
     */
    @SuppressWarnings("unchecked")
	public int size() {
        List<? extends Object> list = pageableListView.getList();
        return list.size();
    }

    /**
     * @see org.apache.wicket.markup.repeater.data.IDataProvider#model(java.lang.Object)
     */
    public IModel model(Object object) {
        return new Model((Serializable) object);
    }

    /**
     * @see org.apache.wicket.model.IDetachable#detach()
     */
    public void detach() {
    }

    /**
     * @see ISortableDataProvider#getSortState()
     */
    public final ISortState getSortState() {
        return sortState;
    }

    /**
     * @see ISortableDataProvider#setSortState(org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState)
     */
	public final void setSortState(ISortState state) {
        if (!(state instanceof SortablePageableListViewDataProvider.BasicSortState)) {
            throw new IllegalArgumentException(
                    "argument [state] must be an instance of BasicSortState, but it is ["
                            + state.getClass().getName() + "]:["
                            + state.toString() + "]");
        }
        this.sortState = (BasicSortState) state;
    }

    public class BasicSortState extends SingleSortState {

        public void setPropertySortOrder(String property, int dir) {
            super.setPropertySortOrder(property, dir);
            
            IModel pageableListViewModel = pageableListView.getModel();
            // Forces refresh by reattaching
            pageableListViewModel.detach();
            pageableListView.setCurrentPage(0);
        }
    }

}
