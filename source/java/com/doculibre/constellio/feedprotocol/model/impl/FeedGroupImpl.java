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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.doculibre.constellio.feedprotocol.model.FeedGroup;
import com.doculibre.constellio.feedprotocol.model.FeedRecord;
import com.doculibre.constellio.feedprotocol.model.ParseFeedException;

public class FeedGroupImpl implements FeedGroup {

	private static final String ADD = "add";
	//RecordImpl must see it
	static final String DELETE = "delete";

	private final List<FeedRecord> records;
	private final ACTION action;

	public FeedGroupImpl(List<FeedRecord> records) throws ParseFeedException {
		this(ADD, records);
	}

	public FeedGroupImpl(String action, List<FeedRecord> records) throws ParseFeedException {
		if (action == null) {
			this.action = ACTION.ADD;
		} else if (action.equals(ADD)) {
			this.action = ACTION.ADD;
		} else if (action.equals(DELETE)) {
			this.action = ACTION.DELETE;
		} else {
			throw new ParseFeedException("Invalid action: " + action);
		}

		if (CollectionUtils.isEmpty((Collection) records)) {
			throw new ParseFeedException("Records is empty");
		}
		List<FeedRecord> recordsTemp = new ArrayList<FeedRecord>();
		recordsTemp.addAll(records);
		this.records = Collections.unmodifiableList(recordsTemp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.doculibre.search.protocol.feed.model.impl.Group#getRecords()
	 */
	public List<FeedRecord> getRecords() {
		return records;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.doculibre.search.protocol.feed.model.impl.Group#getAction()
	 */
	public ACTION getAction() {
		return action;
	}
}
