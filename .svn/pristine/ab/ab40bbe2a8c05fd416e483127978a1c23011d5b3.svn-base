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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;

@SuppressWarnings("serial")
public class CollectionIndexFieldsModel extends LoadableDetachableModel {

	private IModel collectionModel;
	
	public CollectionIndexFieldsModel(IModel collectionModel) {
		this.collectionModel = collectionModel;
	}
	
	public CollectionIndexFieldsModel(RecordCollection collection) {
		collectionModel = new ReloadableEntityModel<RecordCollection>(collection);
	}

	@Override
	protected Object load() {
		RecordCollection collection = (RecordCollection) collectionModel.getObject();
		List<IndexField> indexFields = new ArrayList<IndexField>();
		for (IndexField indexField : collection.getIndexFields()) {
			if (accept(indexField)) {
				indexFields.add(indexField);
			}
		}
		Collections.sort(indexFields, new Comparator<IndexField>() {
			@Override
			public int compare(IndexField o1, IndexField o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return indexFields;
	}
	
	@Override
	public void detach() {
		collectionModel.detach();
		super.detach();
	}
	
	protected boolean accept(IndexField indexField) {
		if (indexField.getName().contains("_analyzedCopy")) {
			return acceptAnalyzedCopyIndexFields();
		}
		if (indexField.getName().contains("_sort")) {
			return acceptSortIndexFields();
		}
		return true;
	}
	
	protected boolean acceptSortIndexFields() {
		return false;
	}
	
	protected boolean acceptAnalyzedCopyIndexFields() {
		return false;
	}
}
