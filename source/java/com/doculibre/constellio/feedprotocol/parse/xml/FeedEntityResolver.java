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
package com.doculibre.constellio.feedprotocol.parse.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;


public class FeedEntityResolver implements EntityResolver {

	private static final ClassPathResource DTD_PATH = new ClassPathResource("gsafeed.dtd", FeedEntityResolver.class);
	
	private static final FeedEntityResolver INSTANCE = new FeedEntityResolver();
	private final byte[] byteArray;

	private FeedEntityResolver() {
		InputStream inputStream = null;
		try {
			inputStream = DTD_PATH.getInputStream();
			if (inputStream == null) {
				throw new IOException("Could not find : " + DTD_PATH.getPath());
			}
			byteArray = IOUtils.toByteArray(inputStream);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new RuntimeException(ioe);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	public static FeedEntityResolver getInstance() {
		return INSTANCE;
	}

	public InputSource resolveEntity(String publicId, String systemId) {
		return new InputSource(new InputStreamReader(new ByteArrayInputStream(byteArray)));
	}
}
