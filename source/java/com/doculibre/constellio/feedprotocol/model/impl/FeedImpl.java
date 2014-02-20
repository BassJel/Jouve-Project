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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.doculibre.constellio.feedprotocol.model.Feed;
import com.doculibre.constellio.feedprotocol.model.FeedGroup;
import com.doculibre.constellio.feedprotocol.model.ParseFeedException;

public class FeedImpl implements Feed {
	
	static final String FULL = "full";
	static final String INCREMENTAL = "incremental";
	static final String METADATA_AND_URL = "metadata-and-url";
	
	public static final String WEB = "web";
	
	private static final String REGEX_VALIDATION = "[a-zA-Z_][a-zA-Z0-9_-]*";
	
	private final String datasource;
	private final FEEDTYPE feedtype;
	private final List<FeedGroup> groups;
	
	public FeedImpl(String datasource, String feedtype, List<FeedGroup> groups) throws ParseFeedException {
		if (StringUtils.isBlank(datasource)) {
			throw new ParseFeedException("Blank datasource");
		}
		Matcher matcher = Pattern.compile(REGEX_VALIDATION, Pattern.CASE_INSENSITIVE).matcher(datasource);
		if (!matcher.find()) {
			throw new ParseFeedException("Invalid name format (" + REGEX_VALIDATION + ") for datasource");
		}
		this.datasource = datasource;
		
		if (StringUtils.isBlank(feedtype)) {
			throw new ParseFeedException("Blank feedType");
		} else if (feedtype.equals(FULL)) {
			this.feedtype = FEEDTYPE.FULL;
		} else if (feedtype.equals(INCREMENTAL)) {
			this.feedtype = FEEDTYPE.INCREMENTAL;
		} else if (feedtype.equals(METADATA_AND_URL)) {
			this.feedtype = FEEDTYPE.METADATA_AND_URL;
		} else {
			throw new ParseFeedException("Invalid feedType: " + feedtype);
		}
		
		if (CollectionUtils.isEmpty(groups)) {
			throw new ParseFeedException("Groups is empty");
		} 
		List<FeedGroup> groupsTemp = new ArrayList<FeedGroup>();
		groupsTemp.addAll(groups);
		this.groups = Collections.unmodifiableList(groupsTemp);
	}

	public String getDatasource() {
		return datasource;
	}

	public FEEDTYPE getFeedtype() {
		return feedtype;
	}

	/* (non-Javadoc)
	 * @see com.doculibre.search.protocol.feed.model.impl.Gsafeed#getGroups()
	 */
	public List<FeedGroup> getGroups() {
		return groups;
	}
}
