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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.BaseConstellioEntity;

@SuppressWarnings("serial")
public class EntityListModel<T extends BaseConstellioEntity> extends LoadableDetachableModel {
	
	List<EntityModel<T>> entityModels = new ArrayList<EntityModel<T>>();
	
	private EntityList entityList = new EntityList();

	public EntityListModel() {
		super();
	}

	public EntityListModel(Collection<T> object) {
		super();
		entityList.addAll(object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getObject() {
		return (List<T>) super.getObject();
	}
	
	@Override
	protected Object load() {
		return entityList;
	}
	
	@Override
	public void detach() {
		for (EntityModel<T> entityModel : entityModels) {
			entityModel.detach();
		}
		super.detach();
	}

	private class EntityList extends AbstractList<T> implements Serializable {

		@Override
		public T get(int index) {
			return entityModels.get(index).getObject();
		}

		@Override
		public int size() {
			return entityModels.size();
		}

		@Override
		public void add(int index, T element) {
			entityModels.add(index, new EntityModel<T>(element));
		}

		@Override
		public T remove(int index) {
			T entity = entityModels.get(index).getObject();
			entityModels.get(index).detach();
			entityModels.remove(index);
			return entity;
		}

		@Override
		public T set(int index, T element) {
			entityModels.get(index).detach();
			entityModels.set(index, new EntityModel<T>(element));
			return element;
		}
		
	}

}
