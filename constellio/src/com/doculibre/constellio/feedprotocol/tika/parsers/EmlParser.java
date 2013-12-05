/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doculibre.constellio.feedprotocol.tika.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.MSOffice;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Defines a EML document content extractor.
 */
public class EmlParser implements Parser {

	private static final Set<MediaType> SUPPORTED_TYPES =
        Collections.unmodifiableSet(new HashSet<MediaType>(Arrays.asList(
                MediaType.application("message/rfc822"))));

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }

	/**
	 * Extracts properties and text from an EML Document input stream
	 */
	@Override
	public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
			throws IOException, SAXException, TikaException {
		XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
		xhtml.startDocument();
		
		Properties props = System.getProperties();
		Session mailSession = Session.getDefaultInstance(props, null);
		MimeMessage message;
		try {
			message = new MimeMessage(mailSession, stream);

			String subject = message.getSubject();
			String from = this.convertAddressesToString(message.getFrom());

			// Recipients :
			String to = this.convertAddressesToString(message
					.getRecipients(Message.RecipientType.TO));
			String cc = this.convertAddressesToString(message
					.getRecipients(Message.RecipientType.CC));
			String bcc = this.convertAddressesToString(message
					.getRecipients(Message.RecipientType.BCC));

			metadata.set(MSOffice.AUTHOR, from);
			metadata.set(DublinCore.TITLE, subject);
			metadata.set(DublinCore.SUBJECT, subject);

			xhtml.element("h1", subject);

			xhtml.startElement("dl");
			header(xhtml, "From", from);
			header(xhtml, "To", to.toString());
			header(xhtml, "Cc", cc.toString());
			header(xhtml, "Bcc", bcc.toString());
			xhtml.endElement("dl");

			//a supprimer si pb et a remplacer par ce qui est commenT
			adaptedExtractMultipart(xhtml, message, context);
			
			// Parse message
			/*if (message.getContent() instanceof MimeMultipart) {
				// Multipart message, call matching method
				MimeMultipart multipart = (MimeMultipart) message.getContent();
				this.extractMultipart(xhtml, multipart, context);
			} else {
				// Simple message (with no attachments), extract content with
				// Tika parser
				Parser parser = new AutoDetectParser();
				BodyContentHandler handlerContent = new BodyContentHandler();
				try {
					parser.parse(new ByteArrayInputStream(message.getContent()
							.toString().getBytes()), handlerContent, metadata, context);
				} catch (IOException e) {
					throw new TikaException(
							"Failed to parse Outlook message body", e);
				}
				xhtml.element("div", handlerContent.toString());
			}*/
		} catch (MessagingException e) {
			throw new TikaException("Error while processing message", e);
		} catch (IOException e) {
			throw new TikaException("Error while processing message", e);
		}
	}
	
	/**
     * @deprecated This method will be removed in Apache Tika 1.0.
     */
	public void parse(InputStream stream, ContentHandler handler,
			Metadata metadata) throws IOException, SAXException, TikaException {
		parse(stream, handler, metadata, new ParseContext());
	}

	private void header(XHTMLContentHandler xhtml, String key, String value)
			throws SAXException {
		if (value.length() > 0) {
			xhtml.element("dt", key);
			xhtml.element("dd", value);
		}
	}

	// Convert list of addresses into String 
	private String convertAddressesToString(Address[] addresses) {
		StringBuilder result = new StringBuilder();
		if (addresses != null) {
			String addressToAdd;
			for (int i = 0; i < addresses.length; i++) {
				addressToAdd = addresses[i].toString().replaceAll("<", "")
						.replaceAll(">", "");
				result.append(addressToAdd).append("; ");
			}
			int resultLength = result.length();
			if (resultLength > 1)
				result.delete(resultLength - 2, resultLength);
		}
		return result.toString();
	}

	// Extract body and attachments from multipart messages
	private void extractMultipart(XHTMLContentHandler xhtml,
			MimeMultipart multipart, ParseContext context) throws MessagingException, IOException,
			SAXException, TikaException {
		for (int i = 0; i < multipart.getCount(); i++) {
			MimeBodyPart bodyPart = (MimeBodyPart) multipart.getBodyPart(i);
			if (bodyPart.getDisposition() == null) {
				// BODY
				MimeMultipart subMultipart = (MimeMultipart) bodyPart
						.getContent();
				for (int j = 0; j < subMultipart.getCount(); j++) {
					MimeBodyPart subBodyPart = (MimeBodyPart) subMultipart
							.getBodyPart(j);
					if (subBodyPart.getContentType().startsWith("text/plain")) {
						xhtml.element("div", subBodyPart.getContent()
								.toString());
					}
				}
			} else if (bodyPart.getDisposition().equals(Part.ATTACHMENT)) {
				// Attachments
				InputStream fileContent = bodyPart.getInputStream();
//				String fileName = bodyPart.getFileName();

				Parser parser = new AutoDetectParser();
				Metadata attachmentMetadata = new Metadata();

				BodyContentHandler handlerAttachments = new BodyContentHandler();
				parser.parse(fileContent, handlerAttachments,
						attachmentMetadata, context);

				xhtml.element("div", handlerAttachments.toString());
			}
		}
	}
	
	
	public static void adaptedExtractMultipart(XHTMLContentHandler xhtml,
			Part part, ParseContext context) throws MessagingException, IOException,
			SAXException, TikaException {
		if (part.isMimeType("text/plain")) {
			xhtml.element("div", part.getContent().toString());
		} else if (part.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) part.getContent();
			int count = mp.getCount();
			for (int i = 0; i < count; i++) {
				adaptedExtractMultipart(xhtml, mp.getBodyPart(i), context);
			}
		} else if (part.isMimeType("message/rfc822")) {
			adaptedExtractMultipart(xhtml, (Part) part.getContent(), context);
		} else {
			Object content = part.getContent();
			if (content instanceof String) {
				xhtml.element("div", part.getContent().toString());
			} else if (content instanceof InputStream) {
				InputStream fileContent = part.getInputStream();

				Parser parser = new AutoDetectParser();
				Metadata attachmentMetadata = new Metadata();

				BodyContentHandler handlerAttachments = new BodyContentHandler();
				parser.parse(fileContent, handlerAttachments,
						attachmentMetadata, context);

				xhtml.element("div", handlerAttachments.toString());
				
			}
		}
	}
}
