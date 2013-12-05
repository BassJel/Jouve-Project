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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.persistence.EntityManager;

import org.apache.commons.lang.SerializationUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.Hibernate;

import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.BaseCRUDServicesImpl;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;

@SuppressWarnings("serial")
public class ReloadableEntityModel<T extends ConstellioEntity> extends LoadableDetachableModel {
		
	private Class<T> entityClass;
	private Long id;
	private ConstellioEntity entity;
	private byte[] serializedEntity;
	private IModel entityModel;
	
	public ReloadableEntityModel(Class<T> entityClass, Long id) {
		this.entityClass = entityClass;
		this.id = id;
	}
	
	public ReloadableEntityModel(T entity) {
		this.entity = entity;
	}
	
	public ReloadableEntityModel(IModel entityModel) {
		this.entityModel = entityModel;
	}

	@Override
	protected final Object load() {
		ConstellioEntity result;
		if (entityModel != null) {
			entity = (ConstellioEntity) entityModel.getObject();
			entityModel.detach();
			entityModel = null;
		} else if (serializedEntity != null) {
		    entity = (ConstellioEntity) SerializationUtils.deserialize(serializedEntity);
			result = entity;
		} 
		if (entity != null) {
		    result = entity;
		} else {
			EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
			BaseCRUDServices<T> crudServices = new BaseCRUDServicesImpl<T>(entityClass, entityManager);
			if (id != null){
				result = entity = crudServices.get(id);
			} else {
			    result = null;
			}
		}
		if (serializedEntity != null) {
			serializedEntity = null;
		}
		return result;
	}

	@Override
	public synchronized void detach() {
		if (entityModel != null) {
			entity = (ConstellioEntity) entityModel.getObject();
			entityModel.detach();
			entityModel = null;
		}
		prepareForSerialization();
		super.detach();
	}

    private void writeObject(ObjectOutputStream oos) throws IOException {
        prepareForSerialization();
        oos.defaultWriteObject();
    }
	
	@SuppressWarnings("unchecked")
    private void prepareForSerialization() {
        if (entity != null) {
            if (entity.getId() != null) {
                entityClass = (Class<T>) Hibernate.getClass(entity);
                id = entity.getId();
                entity = null;
                serializedEntity = null;
            } else {
                serializedEntity = SerializationUtils.serialize((Serializable) entity);
                entity = null;
            }
        }
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() {
		return (T) super.getObject();
	}

}
