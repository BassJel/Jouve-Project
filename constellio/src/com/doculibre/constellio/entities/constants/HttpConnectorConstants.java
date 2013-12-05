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
package com.doculibre.constellio.entities.constants;

import com.doculibre.constellio.entities.IndexField;

public interface HttpConnectorConstants {
    
    public static final String META_CHARSET = "constellio-http:charset";
    public static final String META_LEVEL = "constellio-http:level";
    public static final String META_DIGEST = "constellio-http:digest";
    public static final String META_BOOST = "constellio-http:boost";
    public static final String META_FETCHED = "constellio-http:fetched";
    
    public static final String FIELD_CHARSET = IndexField.normalize(META_CHARSET);
    public static final String FIELD_LEVEL = IndexField.normalize(META_LEVEL);
    public static final String FIELD_DIGEST = IndexField.normalize(META_DIGEST);
    public static final String FIELD_BOOST = IndexField.normalize(META_BOOST);
    public static final String FIELD_FETCHED = IndexField.normalize(META_FETCHED);

}
