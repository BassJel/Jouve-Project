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
package com.doculibre.constellio.entities;

import java.util.Locale;
import java.util.Set;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class ConstellioLabelledEntity extends BaseConstellioEntity {

	@Transient
	protected abstract Set<I18NLabel> getLabels();
	
	protected abstract void setLabels(Set<I18NLabel> labels);
	
	public String getLabel(String key, Locale locale) {
		String labelStr;
	    I18NLabel matchingLabel = null;
		for (I18NLabel label : getLabels()) {
			if (label.getKey().equals(key)) {
				matchingLabel = label;
				break;
			}
		}
		if (matchingLabel != null) {
		    labelStr = matchingLabel.getValue(new Locale(locale.getLanguage()));
		} else {
		    labelStr = null;
		}
		return labelStr;
	}
	
	public void setLabel(String key, String value, Locale locale) {
		I18NLabel matchingLabel = null;
		for (I18NLabel label : getLabels()) {
			if (label.getKey().equals(key)) {
				matchingLabel = label;
				break;
			}
		}
		if (matchingLabel == null) {
			matchingLabel = new I18NLabel();
			matchingLabel.setKey(key);
			this.getLabels().add(matchingLabel);
		}
		matchingLabel.setValue(value, new Locale(locale.getLanguage()));
	}

}
