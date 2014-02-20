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
package com.doculibre.constellio.wicket.components.holders;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class ModalImgLinkHolder extends ModalLinkHolder {
	
	public ModalImgLinkHolder(String id) {
		super(id);
	}

	@Override
	public Component newLabel(String id, IModel labelModel) {
		Component img = newImg(id);
		IModel altModel = getAltModel();
		if (altModel != null) {
			img.add(new AttributeModifier("alt", true, altModel));
		}
		return img;
	}
	
	/**
	 * Same text
	 * 
	 * @see com.doculibre.constellio.wicket.components.holders.ModalLinkHolder#getTitleModel()
	 */
	@Override
	protected IModel getTitleModel() {
		return getAltModel();
	}

	protected IModel getAltModel() {
		return null;
	}

    protected abstract Component newImg(String id);

}
