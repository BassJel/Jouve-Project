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

import java.util.List;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.model.IDetachable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * 
 * @author Vincent Dussault
 */
@SuppressWarnings("serial")
public abstract class SortableListModel<T> implements IDetachable {

	/** keeps track of whether this model is attached or detached */
	private transient boolean attached = false;

	/** temporary, transient object. */
	private transient List<T> transientModelObject;

	/** Logger. */
	private static final Logger log = LoggerFactory.getLogger(SortableListModel.class);
    
    /**
     * 
     */
    public SortableListModel() {
        super();
    }

	@Override
	public void detach() {
		if (attached) {
			attached = false;
			transientModelObject = null;

			if (log.isDebugEnabled()) {
				log.debug("removed transient object for " + this + ", requestCycle " +
						RequestCycle.get());
			}
			onDetach();
		}
	}

    public List<T> getObject() {
        return getObject(null, null);
    }

	/**
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	public List<T> getObject(String orderByProperty, Boolean orderByAsc) {
		if (!attached)
		{
			attached = true;
			transientModelObject = load(orderByProperty, orderByAsc);

			if (log.isDebugEnabled())
			{
				log.debug("loaded transient object " + transientModelObject + " for " + this +
						", requestCycle " + RequestCycle.get());
			}

			onAttach();
		}
		return transientModelObject;
	}

	/**
	 * Gets the attached status of this model instance
	 * 
	 * @return true if the model is attached, false otherwise
	 */
	public final boolean isAttached() {
		return attached;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":attached=").append(attached).append(":tempModelObject=[").append(
				this.transientModelObject).append("]");
		return sb.toString();
	}

	/**
	 * Loads and returns the (temporary) model object.
	 * 
	 * @return the (temporary) model object
	 */
	protected abstract List<T> load(String orderByProperty, Boolean orderByAsc);

	/**
	 * Attaches to the current request. Implement this method with custom behavior, such as loading
	 * the model object.
	 */
	protected void onAttach() {
	}

	/**
	 * Detaches from the current request. Implement this method with custom behavior, such as
	 * setting the model object to null.
	 */
	protected void onDetach() {
	}
    
}
