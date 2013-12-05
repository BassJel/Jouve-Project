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
package com.doculibre.constellio.wicket.validators;

import java.util.Map;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;

/**
 * Ce validateur facilite la validation de duplication d'identifant
 * @author francisbaril
 *
 */
@SuppressWarnings("serial")
public abstract class DuplicateItemValidator extends AbstractValidator{
	
	private String key;
	
	public DuplicateItemValidator() {
		key = "duplicate";
	}
	
	public DuplicateItemValidator(String customKey) {
		key = customKey;
	}
	
	@Override
	protected void onValidate(IValidatable validatable) {
		if (isDuplicate(validatable.getValue())) {
			error(validatable);
		}
	}
	
    @Override 
    protected String resourceKey() {
        return key;
    }
    
    @SuppressWarnings("unchecked")
	@Override 
    protected Map variablesMap(IValidatable validatable) {
        Map map = super.variablesMap(validatable);
        map.put("value", validatable.getValue());
        return map;
    }

    protected abstract boolean isDuplicate(Object value);
    
}
