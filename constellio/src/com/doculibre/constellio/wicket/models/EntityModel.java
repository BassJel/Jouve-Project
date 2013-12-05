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

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.entities.ConstellioEntity;

@SuppressWarnings("serial")
public class EntityModel<T extends ConstellioEntity> extends Model {

    private ReloadableEntityModel<T> reloadableEntityModel;

    public EntityModel() {
    }

    public EntityModel(IModel entityModel) {
        super();
        this.reloadableEntityModel = new ReloadableEntityModel<T>(entityModel);
    }

    public EntityModel(T entity) {
        this(new ReloadableEntityModel<T>(entity));
    }

    @Override
    public T getObject() {
        return reloadableEntityModel != null ? reloadableEntityModel.getObject() : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setObject(Serializable object) {
        if (object != null) {
            ConstellioEntity constellioEntity = (ConstellioEntity) object;
            if (reloadableEntityModel != null) {
                if (reloadableEntityModel.getObject() == null
                    || !reloadableEntityModel.getObject().equals(constellioEntity)) {
                    reloadableEntityModel.detach();
                    reloadableEntityModel = new ReloadableEntityModel<T>((T) constellioEntity);
                }
            } else {
                reloadableEntityModel = new ReloadableEntityModel<T>((T) constellioEntity);
            }
        } else if (reloadableEntityModel != null) {
            reloadableEntityModel.detach();
            reloadableEntityModel = null;
        }
    }

    @Override
    public void detach() {
        if (reloadableEntityModel != null) {
            reloadableEntityModel.detach();
        }
        super.detach();
    }

}
