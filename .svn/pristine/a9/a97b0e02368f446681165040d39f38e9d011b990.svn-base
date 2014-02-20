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
package com.doculibre.constellio.feedprotocol.xml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.doculibre.constellio.feedprotocol.model.ParseFeedException;
import com.doculibre.constellio.feedprotocol.parse.xml.FeedValidator;

public class FeedValidatorTest {

	@Test
	public void valid() throws ParseFeedException {
		FeedValidator validator = new FeedValidator();

		String validXml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + "<!DOCTYPE gsafeed PUBLIC \"-//Google//DTD GSA Feeds//EN\" \"\">"
				+ "<gsafeed>" + "	<header>" + "		<datasource>web</datasource>" + "		<feedtype>incremental</feedtype>" + "	</header>" + "	<group>"
				+ "		<record url=\"http://www.corp.enterprise.com/hello02\" mimetype=\"text/plain\"/>" + "	</group>" + "</gsafeed>";
		StringReader stringReader = new StringReader(validXml);
		validator.validate(new InputSource(stringReader));
	}

	@Test
	public void withoutDoctype() throws ParseFeedException {
		FeedValidator validator = new FeedValidator();

		String validXml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + "<gsafeed>" + "	<header>" + "		<datasource>web</datasource>"
				+ "		<feedtype>incremental</feedtype>" + "	</header>" + "	<group>"
				+ "		<record url=\"http://www.corp.enterprise.com/hello02\" mimetype=\"text/plain\"/>" + "	</group>" + "</gsafeed>";
		StringReader stringReader = new StringReader(validXml);
		try {
			validator.validate(new InputSource(stringReader));
		} catch (ParseFeedException e) {
			return;
		}
	}

	@Test
	public void invalidElement() throws SAXException, IOException, ParserConfigurationException, TransformerFactoryConfigurationError,
			TransformerException {
		FeedValidator validator = new FeedValidator();

		String validXml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + "<!DOCTYPE gsafeed PUBLIC \"-//Google//DTD GSA Feeds//EN\" \"\">"
				+ "<gsafeed>" + "	<error/>" + "</gsafeed>";
		StringReader stringReader = new StringReader(validXml);
		try {
			validator.validate(new InputSource(stringReader));
		} catch (ParseFeedException e) {
			return;
		}
		Assert.assertTrue("The invalid feed was not rejected", false);
	}

	@Test
	public void emptyDocument() {
		FeedValidator validator = new FeedValidator();

		String validXml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + "<!DOCTYPE gsafeed PUBLIC \"-//Google//DTD GSA Feeds//EN\" \"\">"
				+ "<gsafeed>" + "</gsafeed>";
		StringReader stringReader = new StringReader(validXml);
		try {
			validator.validate(new InputSource(stringReader));
		} catch (ParseFeedException e) {
			return;
		}
		Assert.assertTrue("The invalid feed was not rejected", false);
	}

	@Test
	public void invalidDtd() throws ParseFeedException {
		FeedValidator validator = new FeedValidator();

		String validXml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
				+ "<!DOCTYPE gsafeed PUBLIC \"-//OTHER//DTD TTTTTTTTTTTTT tttt//EN\" \"\">" + "<gsafeed>" + "	<header>"
				+ "		<datasource>web</datasource>" + "		<feedtype>incremental</feedtype>" + "	</header>" + "	<group>"
				+ "		<record url=\"http://www.corp.enterprise.com/hello02\" mimetype=\"text/plain\"/>" + "	</group>" + "</gsafeed>";
		StringReader stringReader = new StringReader(validXml);
		try {
			validator.validate(new InputSource(stringReader));
		} catch (ParseFeedException e) {
			return;
		}
	}
}
