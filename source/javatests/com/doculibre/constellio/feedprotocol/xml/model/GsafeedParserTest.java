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
package com.doculibre.constellio.feedprotocol.xml.model;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.doculibre.constellio.feedprotocol.model.Feed;
import com.doculibre.constellio.feedprotocol.model.ParseFeedException;
import com.doculibre.constellio.feedprotocol.parse.xml.FeedParser;

public class GsafeedParserTest {
	
	private static final ClassPathResource FULL_EXAMPLE_PATH = new ClassPathResource("examples/fullContentFeed.xml", GsafeedParserTest.class);
	private static final ClassPathResource INCREMENTAL_CONTENT_PATH = new ClassPathResource("examples/incrementalContentFeed.xml", GsafeedParserTest.class);
	private static final ClassPathResource INCREMENTAL_WEB_PATH = new ClassPathResource("examples/webFeed.xml", GsafeedParserTest.class);
	private static final ClassPathResource METADATA_URL_PATH = new ClassPathResource("examples/webFeedMetadata.xml", GsafeedParserTest.class);

	@Before
	public void setup() {
	}

	@Test
	public void parseFullTest() throws ParseFeedException, IOException {
		FeedParser gsafeedParser = new FeedParser();
		InputStream inputStream = null;
		try {
			inputStream = FULL_EXAMPLE_PATH.getInputStream();
			if (inputStream == null) {
				throw new IOException("Could not find: " + FULL_EXAMPLE_PATH.getPath());
			}
			Feed gsafeed = gsafeedParser.parse("sample", "full", inputStream);
			System.out.print("");
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	@Test
	public void parseIncrementalTest() throws ParseFeedException, IOException {
		FeedParser gsafeedParser = new FeedParser();
		InputStream inputStream = null;
		try {
			inputStream = INCREMENTAL_CONTENT_PATH.getInputStream();
			if (inputStream == null) {
				throw new IOException("Could not find: " + INCREMENTAL_CONTENT_PATH.getPath());
			}
			Feed gsafeed = gsafeedParser.parse("hello", "incremental", inputStream);
			System.out.print("");
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	@Test
	public void parseWebTest() throws ParseFeedException, IOException {
		FeedParser gsafeedParser = new FeedParser();
		InputStream inputStream = null;
		try {
			inputStream = INCREMENTAL_WEB_PATH.getInputStream();
			if (inputStream == null) {
				throw new IOException("Could not find: " + INCREMENTAL_WEB_PATH.getPath());
			}
			Feed gsafeed = gsafeedParser.parse("web", "incremental", inputStream);
			System.out.print("");
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	@Test
	public void parseWebMetadataTest() throws ParseFeedException, IOException {
		FeedParser gsafeedParser = new FeedParser();
		InputStream inputStream = null;
		try {
			inputStream = METADATA_URL_PATH.getInputStream();
			if (inputStream == null) {
				throw new IOException("Could not find: " + METADATA_URL_PATH.getPath());
			}
			Feed gsafeed = gsafeedParser.parse("example3", "metadata-and-url", inputStream);
			System.out.print("");
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
}
