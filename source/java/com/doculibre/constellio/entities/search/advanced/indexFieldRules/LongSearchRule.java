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
package com.doculibre.constellio.entities.search.advanced.indexFieldRules;

import com.doculibre.constellio.entities.search.advanced.SearchRulesGroup;
import com.doculibre.constellio.utils.SimpleParams;

@SuppressWarnings("serial")
public class LongSearchRule extends AbstractNumericSearchRule<Long> {

	public static final String TYPE = "long";

	public LongSearchRule() {
		super();
	}

	public LongSearchRule(SimpleParams params, SearchRulesGroup parent, String lookupPrefix) {
		super(params, parent, lookupPrefix);
	}

	@Override
	protected Long fromHTTPParam(String param) {
		return Long.valueOf(param);
	}

	@Override
	protected String getType() {
		return TYPE;
	}

}
