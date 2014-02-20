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

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;

/**
 * 
 * 
 * @author Vincent Dussault
 */
@SuppressWarnings("serial")
public class AutoHidePagingNavigator extends AjaxPagingNavigator {

	private IPageable pageable;

	public AutoHidePagingNavigator(String id, IPageable pageable,
			IPagingLabelProvider labelProvider) {
		super(id, pageable, labelProvider);
		this.pageable = pageable;
	}

	public AutoHidePagingNavigator(String id, IPageable pageable) {
		super(id, pageable);
		this.pageable = pageable;
	}

	public boolean isVisible() {
		return pageable.getPageCount() > 1;
	}

	protected void onBeforeRender() {
		super.onBeforeRender();

		Link firstLink = (Link) get("first");
		firstLink.setVisible(pageable.getPageCount() > 2
				&& pageable.getCurrentPage() > 0);

		Link prevLink = (Link) get("prev");
		prevLink.setVisible(pageable.getCurrentPage() > 0);

		Link nextLink = (Link) get("next");
		nextLink
				.setVisible(pageable.getCurrentPage() < pageable.getPageCount() - 1);

		Link lastLink = (Link) get("last");
		lastLink.setVisible(pageable.getPageCount() > 2
				&& pageable.getCurrentPage() < pageable.getPageCount() - 1);
	}

}
