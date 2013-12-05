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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.doculibre.constellio.entities.search.advanced.SearchRulesGroup;
import com.doculibre.constellio.utils.SimpleParams;

@SuppressWarnings("serial")
public class DateSearchRule extends AbstractNumericSearchRule<Date> {

	public static final String TYPE = "date";

	public DateSearchRule() {
		super();
	}

	public DateSearchRule(SimpleParams params, SearchRulesGroup parent, String lookupPrefix) {
		super(params, parent, lookupPrefix);
	}

	@Override
	protected Date cloneValue(Date value) {
		return new Date(value.getTime());
	}

	@Override
	protected Date fromHTTPParam(String param) {
		if (param.contains("/")) {
			try {
				return new SimpleDateFormat("dd/MM/yy").parse(param);
			} catch(Throwable t) {
				return null;
			}
		} else {
			try {
				return new Date(Long.valueOf(param));
			} catch(Exception e) {
				return null;
			}
		}

	}

	@Override
	protected String getType() {
		return TYPE;
	}

	@Override
	protected String toHTTPParam(Date value) {
		return String.valueOf(value.getTime());
	}
	
	@Override
	protected String toMinLuceneParam(Date value) {
		return new SimpleDateFormat("yyyy-MM-dd").format(value) + "T00:00:00.000Z";
	}
	
	@Override
	protected String toMaxLuceneParam(Date value) {
		return new SimpleDateFormat("yyyy-MM-dd").format(value) + "T23:59:59.999Z";
	}
}
