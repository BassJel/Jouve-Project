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

import java.io.File;

import com.doculibre.constellio.feedprotocol.model.FeedContent;
import com.doculibre.constellio.feedprotocol.model.ParseFeedException;

public class FeedContentImpl implements FeedContent {

	private static final String BASE64BINARY = "base64binary";

	private final File value;
	private final ENCODING encoding;

	public FeedContentImpl(File value) throws ParseFeedException {
		this(value, BASE64BINARY);
	}

	public FeedContentImpl(File value, String encoding) throws ParseFeedException {
		this.value = value;

		if (encoding == null) {
			this.encoding = ENCODING.BASE64BINARY;
		} else if (encoding.equals(BASE64BINARY)) {
			this.encoding = ENCODING.BASE64BINARY;
		} else {
			throw new ParseFeedException("Invalid encoding: " + encoding);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.doculibre.search.protocol.feed.model.impl.Content#getValue()
	 */
	public File getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.doculibre.search.protocol.feed.model.impl.Content#getEncoding()
	 */
	public ENCODING getEncoding() {
		return encoding;
	}
}
