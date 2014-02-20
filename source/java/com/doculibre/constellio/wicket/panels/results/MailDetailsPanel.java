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
package com.doculibre.constellio.wicket.panels.results;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;

import com.doculibre.constellio.entities.search.Email;
import com.doculibre.constellio.wicket.components.links.AJAXDownload;

@SuppressWarnings("serial")
public class MailDetailsPanel extends Panel {

	public MailDetailsPanel(String id, final Email email) {
		super(id, new CompoundPropertyModel(email));
		
		add(new Label("froms").setEscapeModelStrings(false));

		String messageContentText = email.getMessageContentHtml();
		String text = email.getMessageContentText();
		//FIXME : hack car les fichiers textes attachés sont ajouté comme contenu texte
		if (text.length() > messageContentText.length()){
			messageContentText = text;
		}
		
		messageContentText = StringUtils.replace(messageContentText, ">>", "&gt;&gt;");
		messageContentText = StringUtils.replace(messageContentText, "\n>", "\n&gt;");
		messageContentText = StringUtils.replace(messageContentText, "\n", "\n<br />");

//		String encodedString;
//		try {
//			encodedString = new String(messageContentText.getBytes(), email.getContentEncoding());
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			encodedString = messageContentText;
//		}
//		add(new Label("messageContent", encodedString).setEscapeModelStrings(false));
		
		add(new Label("messageContent", messageContentText).setEscapeModelStrings(false));
		
		add(new ListView("linksList", email.getAttachments()) {
			@Override
			protected void populateItem(final ListItem item) {
				final Email.AttachmentClass element = (Email.AttachmentClass) item.getModelObject();
				
				final AJAXDownload download = new AJAXDownload()
				{
					@Override
					protected IResourceStream getResourceStream()
					{
						return createResourceStream(element);
					}
					
					@Override
					protected String getFileName()
					{
						return element.getAttachmentName();
					}

					private IResourceStream createResourceStream(
							final Email.AttachmentClass element) {
						IResourceStream resourceStream = new IResourceStream(){
							private Locale locale = null;
							private InputStream inputStream = null;

							@Override
							public void close() throws IOException {
								if (inputStream != null){
									IOUtils.closeQuietly(inputStream);
								}
							}

							@Override
							public String getContentType() {
								return element.getAttachmentType();
							}

							@Override
							public InputStream getInputStream()
									throws ResourceStreamNotFoundException {
								if (inputStream == null){
									inputStream = new ByteArrayInputStream(element.getAttachmentContent());
								}
								return inputStream;
							}

							@Override
							public Locale getLocale() {
								return locale;
							}

							@Override
							public long length() {
								return ((Integer) element.getAttachmentContent().length).longValue();
							}

							@Override
							public void setLocale(Locale locale) {
								this.locale = locale;
							}

							@Override
							public Time lastModifiedTime() {
								// TODO Auto-generated method stub
								return null;
							}
							
						};
						return resourceStream;
					}
				};
				item.add(download);

				AjaxLink link;
				item.add(link = new AjaxLink("attachmentLinks") {
					@Override
					public void onClick(AjaxRequestTarget target)
					{
						download.initiate(target);
					}
				});
				link.add(new Label("urlTitle", element.getAttachmentName()));
				
				/*Link link;
				add(link = new DownloadInputStreamLink("attachmentLinks",
						new LoadableDetachableModel() {
							@Override
							protected Object load() {
								ByteArrayInputStream in = new ByteArrayInputStream(
										element.getAttachmentContent());
								return in;
							}

						}, element.getAttachmentName(), element
								.getAttachmentType(),
						((Integer) element.getAttachmentContent().length)
								.longValue(), new Date()));
				link.add(new Label("urlTitle", element.getAttachmentName()));*/
				item.add(link);
			}
		});

		
		add(new Label("recipients").setEscapeModelStrings(false));
		
		//FIXME 1 by 1
		String flags = StringUtils.join(email.getFlags(), ", ");
		//FIXME i18n
		flags = StringUtils.replace(flags, "seen", getLocalizer().getString("seen", this));
		flags = StringUtils.replace(flags, "flagged", getLocalizer().getString("flagged", this));
		add(new Label("flags", flags).setEscapeModelStrings(false));
		
//		add(new Label("folderNames").setEscapeModelStrings(false));
		add(new Label("receivedDate").setEscapeModelStrings(false));
		
	}

}
