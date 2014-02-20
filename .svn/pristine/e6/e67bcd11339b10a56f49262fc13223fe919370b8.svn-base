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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;

import com.doculibre.constellio.feedprotocol.RFC822DateUtil;
import com.doculibre.constellio.feedprotocol.model.FeedContent;
import com.doculibre.constellio.feedprotocol.model.FeedMetadata;
import com.doculibre.constellio.feedprotocol.model.FeedRecord;
import com.doculibre.constellio.feedprotocol.model.ParseFeedException;
import com.google.enterprise.connector.spi.Value;

public class FeedRecordImpl implements FeedRecord {
	
	private static final String ADD = "add";
	private static final String DELETE = "delete";
	
	private static final String NONE = "none";
	private static final String HTTPBASIC = "httpbasic";
	private static final String NTLM = "ntlm";
	private static final String HTTPSSO = "httpsso";
	
	private final String url;
	private final String displayurl;
	private final ACTION action; // = ACTION_ADD;
	private final String mimetype;
	// RFC822
	private final Calendar lastModified;
	private final boolean locked; // = false;
	private final AUTHMETHOD authmethod; // = AUTHMETHOD_NONE;
	private boolean publicRecord;

	private final List<FeedContent> contents;
	private final List<FeedMetadata> metadatas;

	public FeedRecordImpl(String datasource, String feedtype, String groupActionStr, String url, String displayurl, String action, String mimetype, String lastModified, String lock, String authmethod, List<FeedContent> contents, List<FeedMetadata> metadatas) throws ParseFeedException {		
		if (StringUtils.isBlank(datasource)) {
			throw new ParseFeedException("Blank datasource");
		}
		
		if (StringUtils.isBlank(feedtype)) {
			throw new ParseFeedException("Blank feedType");
		} else if (!feedtype.equals(FeedImpl.FULL) && !feedtype.equals(FeedImpl.INCREMENTAL) && !feedtype.equals(FeedImpl.METADATA_AND_URL)) {
			throw new ParseFeedException("Invalid feedType: " + feedtype);
		}
		
		if (StringUtils.isBlank(url)) {
			throw new ParseFeedException("Blank url");
		}
		this.url = url;
		
		this.displayurl = url;

		if (action == null) {
			this.action = null;
		} else if (action.equals(ADD)) {
			this.action = ACTION.ADD;
		} else if (action.equals(DELETE)) {
			this.action = ACTION.DELETE;
		} else {
			throw new ParseFeedException("Invalid action: " + action);
		}

		this.mimetype = mimetype;

		if (lastModified != null) {
			Calendar calendar = null;
			try {
				calendar = Value.iso8601ToCalendar(lastModified);
			} catch (ParseException e) {
				try {
					calendar = new GregorianCalendar();
					calendar.setTime(RFC822DateUtil.parse(lastModified));
				} catch (Exception ee) {
					Log.warn("Cannot parse last-modified with ISO 8601 OR RFC822 date format: " + lastModified);
				}
			}
			this.lastModified = calendar;
		} else {
			//Implied if null
			this.lastModified = new GregorianCalendar();
		}

		if (lock == null) {
			this.locked = false;
		} else {
			this.locked = Boolean.parseBoolean(lock);
		}

		if (authmethod == null) {
			this.authmethod = AUTHMETHOD.NONE;
			this.publicRecord = true;
		} else if (authmethod.equals(NONE)) {
			this.authmethod = AUTHMETHOD.NONE;
            this.publicRecord = true;
		} else if (authmethod.equals(NTLM)) {
			this.authmethod = AUTHMETHOD.NTLM;
            this.publicRecord = false;
		} else if (authmethod.equals(HTTPBASIC)) {
			this.authmethod = AUTHMETHOD.HTTPBASIC;
            this.publicRecord = false;
		} else if (authmethod.equals(HTTPSSO)) {
			this.authmethod = AUTHMETHOD.HTTPSSO;
            this.publicRecord = false;
		} else {
			throw new ParseFeedException("Invalid authmethod: " + authmethod);
		} 
		
		List<FeedContent> contentsTemp = new ArrayList<FeedContent>();
		contentsTemp.addAll(contents);
		this.contents = Collections.unmodifiableList(contentsTemp);
	
		List<FeedMetadata> metadatasTemp = new ArrayList<FeedMetadata>();
		metadatasTemp.addAll(metadatas);
		this.metadatas = Collections.unmodifiableList(metadatasTemp);
		
		validateAction(datasource, feedtype, groupActionStr);
	}
	
	private void validateAction(String datasource, String feedtype, String groupActionStr) throws ParseFeedException {
		//http://code.google.com/apis/searchappliance/documentation/46/feedsguide.html#metadata
		if (feedtype.equals(FeedImpl.INCREMENTAL) && datasource.equals(FeedImpl.WEB)) {
			if (metadatas.size() != 0 || contents.size() != 0){
				throw new ParseFeedException("For feedtype == incremental AND datasource == web, no metadata or content is allowed");
			}
		} else if (feedtype.equals(FeedImpl.INCREMENTAL)) {
			//Both metadata and content is allowed, but one must be present
			if (metadatas.size() == 0 && contents.size() == 0){
				//if not a delete record
				if (this.action != ACTION.DELETE) {
					//if not record operation is specified and if not a group delete, 
					if (!(this.action == null && groupActionStr.equals(FeedGroupImpl.DELETE))){
						throw new ParseFeedException("For feedtype == incremental and non-delete action, please provide either metadata or content for the record");
					}
				}
			}
		} else if (feedtype.equals(FeedImpl.METADATA_AND_URL)) {
			if (contents.size() != 0){
				throw new ParseFeedException("For feedtype == metadata-and-url, no content is allowed");
			}
		}  else if (feedtype.equals(FeedImpl.FULL)) {
			//Both metadata and content is allowed, but metadata must come with content
			if (metadatas.size() > 0 && contents.size() == 0){
				throw new ParseFeedException("For feedtype == full, if the feed contains metadata, you must also provide content for each record");
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.doculibre.search.protocol.feed.model.impl.Record#getUrl()
	 */
	public String getUrl() {
		return url;
	}
	
	

	/* (non-Javadoc)
	 * @see com.doculibre.search.protocol.feed.model.impl.Record#getAction()
	 */
	public ACTION getAction() {
		return action;
	}

	/* (non-Javadoc)
	 * @see com.doculibre.search.protocol.feed.model.impl.Record#getMimetype()
	 */
	public String getMimetype() {
		return mimetype;
	}

	/* (non-Javadoc)
	 * @see com.doculibre.search.protocol.feed.model.impl.Record#getLastModified()
	 */
	public Calendar getLastModified() {
		return lastModified;
	}

	/* (non-Javadoc)
	 * @see com.doculibre.search.protocol.feed.model.impl.Record#isLock()
	 */
	public boolean isLocked() {
		return locked;
	}

	/* (non-Javadoc)
	 * @see com.doculibre.search.protocol.feed.model.impl.Record#getAuthmethod()
	 */
	public AUTHMETHOD getAuthmethod() {
		return authmethod;
	}

	/* (non-Javadoc)
	 * @see com.doculibre.search.protocol.feed.model.impl.Record#getContents()
	 */
	public List<FeedContent> getContents() {
		return contents;
	}

	/* (non-Javadoc)
	 * @see com.doculibre.search.protocol.feed.model.impl.Record#getMetadatas()
	 */
	public List<FeedMetadata> getMetadatas() {
		return metadatas;
	}

    @Override
    public boolean isPublicRecord() {
        return publicRecord;
    }

	@Override
	public String getDisplayurl() {
		return displayurl;
	}
    
}
