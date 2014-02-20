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
package com.doculibre.constellio.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.junit.Test;

import com.doculibre.constellio.servlets.ServletsConstants;

/**
 * @author francisbaril
 */
public class NamedListUtilsTest {

	/**
	 * convert an XML file to a NamedList and convert it back to a XML document.
	 * Important : These documents are converted in SolrDocument, it does not
	 * test the conversion of document represented by a NamedList,
	 */
	@Test
	public void testConversions() throws IOException {

		File f = new File(NamedListUtilsTest.class
				.getResource("ANamedList.xml").getFile());

		String initialXML = readFileAsString(f);

		NamedList<Object> nl = NamedListUtils
				.convertXMLToNamedList(new FileInputStream(f));
		TestCase.assertEquals(3, nl.size());
		Iterator<Map.Entry<String, Object>> keys = nl.iterator();
		TestCase.assertEquals(ServletsConstants.RESPONSE_HEADER, keys.next().getKey());
		TestCase.assertEquals(ServletsConstants.RESPONSE, keys.next().getKey());
		File tempFile = File.createTempFile("temp", ".xml");

		NamedListUtils.convertResponseNamedListToXML(nl, new FileOutputStream(
				tempFile));
		String xml = readFileAsString(tempFile);

		System.out.println(xml);
		TestCase.assertEquals(
				initialXML.replaceAll("  ", "").replace("\n", ""),
				xml.replaceAll("  ", "").replace("\n", "").replace("\r", ""));

	}

	/**
	 * The conversion of a SolrDocument and his NamedList form should be equal.
	 * Servlets use the NamedList form
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSolrDocumentListVSNamedList() throws IOException {
		NamedList<Object> l1 = new NamedList<Object>();
		SolrDocumentList l = new SolrDocumentList();
		SolrDocument d = new SolrDocument();
		d.setField("a", 123);
		d.setField("myArray", Arrays.asList(new String[] { "A", "B", "C" }));
		d.setField("title", "ééè");
		l.add(d);
		l.setStart(22);
		l.setNumFound(1);
		l1.add(ServletsConstants.RESPONSE, l);

		NamedList<Object> l2 = new NamedList<Object>();
		NamedList<Object> nl = new NamedList<Object>();
		NamedList<Object> attr = new NamedList<Object>();
		attr.add("numFound", 1);
		attr.add("start", 22);
		nl.add("attr", attr);
		l2.add(ServletsConstants.RESPONSE, nl);
		NamedList<Object> nlDoc = new NamedList<Object>();
		nlDoc.add("a", 123);
		nlDoc.add("myArray", Arrays.asList(new String[] { "A", "B", "C" }));
		nlDoc.add("title", "ééè");
		nl.add("doc", nlDoc);

		File tempFile1 = File.createTempFile("temp", ".xml");

		NamedListUtils.convertResponseNamedListToXML(l1, new FileOutputStream(
				tempFile1));
		String xml1 = readFileAsString(tempFile1);

		File tempFile2 = File.createTempFile("temp", ".xml");

		NamedListUtils.convertResponseNamedListToXML(l2, new FileOutputStream(
				tempFile2));
		String xml2 = readFileAsString(tempFile2);
		TestCase.assertEquals(xml1, xml2);
	}

	private static String readFileAsString(File f) throws java.io.IOException {
		StringWriter sw = new StringWriter();
		IOUtils.copy(new FileReader(f), sw);
		return sw.getBuffer().toString();
	}
}
