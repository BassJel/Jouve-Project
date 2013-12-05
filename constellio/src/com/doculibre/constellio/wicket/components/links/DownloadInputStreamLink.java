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
package com.doculibre.constellio.wicket.components.links;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;

public class DownloadInputStreamLink extends Link {
	/**
  * 
  */
	private static final long serialVersionUID = 1L;

	// /**
	// * File to stream
	// */
	// private final byte[] bytes;
	//
	// /**
	// * File name to stream
	// */
	// private final String fileName;

	/**
	 * Bytes model
	 */
	private final IModel inputStreamModel;

	/**
	 * File name model
	 */
	private final IModel fileNameModel;

	/**
	 * Content type model
	 */
	private final IModel contentTypeModel;

	/**
	 * Length model
	 */
	private final IModel lengthModel;

	/**
	 * Last modified time model
	 */
	private final IModel lastModifiedDateModel;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param bytes
	 *            file to stream to client
	 * @param fileName
	 *            name of the file
	 */
	public DownloadInputStreamLink(String id, IModel inputStreamModel, String fileName, String contentType,
			Long length, Date lastModifiedDate) {
		this(id, inputStreamModel, new Model(fileName), new Model(contentType), new Model(length), new Model(
				lastModifiedDate));
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param bytes
	 *            file to stream to client
	 * @param fileName
	 *            name of the file
	 */
	public DownloadInputStreamLink(String id, IModel inputStreamModel, IModel fileNameModel,
			IModel contentTypeModel, IModel lengthModel, IModel lastModifiedDateModel) {
		super(id);
		this.inputStreamModel = inputStreamModel;
		this.fileNameModel = fileNameModel;
		this.contentTypeModel = contentTypeModel;
		this.lengthModel = lengthModel;
		this.lastModifiedDateModel = lastModifiedDateModel;
	}

	/**
	 * 
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	public void onClick() {
		RequestCycle.get().setRequestTarget(
				new InputStreamRequestTarget(inputStreamModel, fileNameModel, contentTypeModel, lengthModel,
						lastModifiedDateModel));
	}

	public static class InputStreamRequestTarget extends ResourceStreamRequestTarget {

		private IModel inputStreamModel;
		private IModel fileNameModel;
		private IModel contentTypeModel;
		private IModel lengthModel;
		private IModel lastModifiedDateModel;

		@SuppressWarnings("serial")
		public InputStreamRequestTarget(final IModel inputStreamModel, final IModel fileNameModel,
				final IModel contentTypeModel, final IModel lengthModel, final IModel lastModifiedDateModel) {
			super(new IResourceStream() {
				public void close() throws IOException {
					InputStream in = (InputStream) inputStreamModel.getObject();
					in.close();
				}

				public String getContentType() {
					String contentType = (String) contentTypeModel.getObject();
					return contentType;
				}

				public InputStream getInputStream() throws ResourceStreamNotFoundException {
					InputStream in = (InputStream) inputStreamModel.getObject();
					return in;
				}

				public Locale getLocale() {
					return null;
				}

				public long length() {
					Number length = (Number) lengthModel.getObject();
					return length != null ? length.longValue() : 0;
				}

				public void setLocale(Locale locale) {

				}

				public Time lastModifiedTime() {
					Date lastModifiedDate = (Date) lastModifiedDateModel.getObject();
					return lastModifiedDate != null ? Time.valueOf(lastModifiedDate) : null;
				}
			});
			this.fileNameModel = fileNameModel;
			this.contentTypeModel = contentTypeModel;
			this.lengthModel = lengthModel;
			this.lastModifiedDateModel = lastModifiedDateModel;
		}

		@Override
		public String getFileName() {
			String fileName = (String) fileNameModel.getObject();
			return fileName;
		}

		@Override
		public void detach(RequestCycle requestCycle) {
			if (inputStreamModel != null) {
				inputStreamModel.detach();
			}
			if (fileNameModel != null) {
				fileNameModel.detach();
			}
			if (contentTypeModel != null) {
				contentTypeModel.detach();
			}
			if (lengthModel != null) {
				lengthModel.detach();
			}
			if (lastModifiedDateModel != null) {
				lastModifiedDateModel.detach();
			}
			super.detach(requestCycle);
		}

	}

}
