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
package com.doculibre.constellio.feedprotocol.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.doculibre.constellio.feedprotocol.model.FeedMeta;
import com.doculibre.constellio.feedprotocol.model.FeedMetadata;
import com.doculibre.constellio.feedprotocol.model.ParseFeedException;

public class FeedMetadataImpl implements FeedMetadata {
	
	private final List<FeedMeta> metas;
	
	public FeedMetadataImpl(List<FeedMeta> metas) throws ParseFeedException {		
		if (CollectionUtils.isEmpty(metas)) {
			throw new ParseFeedException("Metas is empty");
		} 
		List<FeedMeta> metasTemp = new ArrayList<FeedMeta>();
		metasTemp.addAll(metas);
		this.metas = Collections.unmodifiableList(metas);
	}

	/* (non-Javadoc)
	 * @see com.doculibre.search.protocol.feed.model.impl.Metadata#getMetas()
	 */
	public List<FeedMeta> getMetas() {
		return metas;
	} 
}
