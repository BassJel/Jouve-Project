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
package com.doculibre.constellio.wicket.models;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

@SuppressWarnings("serial")
public class ToArrayListModel extends LoadableDetachableModel {
	
	private IModel collectionModel;
	
	public ToArrayListModel(IModel collectionModel) {
		this.collectionModel = collectionModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object load() {
		ArrayList<Object> arrayList;
		Collection<Object> collection = (Collection<Object>) collectionModel.getObject();
		if (collection instanceof ArrayList) {
			arrayList = (ArrayList<Object>) collection;
		} else {
			arrayList = new ArrayList<Object>(collection);
		}
		return arrayList;
	}

	@Override
	public void detach() {
		collectionModel.detach();
		super.detach();
	}

}
