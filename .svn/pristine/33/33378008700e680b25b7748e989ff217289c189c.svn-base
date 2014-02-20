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

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.doculibre.constellio.feedprotocol.model.ParseFeedException;

public class FeedValidator {

	public Document validate(InputSource inputSource) throws ParseFeedException {
		try {

			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			domFactory.setValidating(true);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			builder.setEntityResolver(FeedEntityResolver.getInstance());
			builder.setErrorHandler(new FeedErrorHandler());
			Document xmlDoc = builder.parse(inputSource);
			DocumentType documentType = xmlDoc.getDoctype();

			if (!documentType.getName().equals("gsafeed") || !documentType.getPublicId().equals("-//Google//DTD GSA Feeds//EN")) {
				throw new ParseFeedException("Invalid DTD");
			}
			
			return xmlDoc;
		} catch (ParserConfigurationException e) {
			throw new ParseFeedException(e);
		} catch (SAXException e) {
			throw new ParseFeedException(e);
		} catch (IOException e) {
			throw new ParseFeedException(e);
		}

	}
}
