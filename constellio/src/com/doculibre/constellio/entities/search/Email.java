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
package com.doculibre.constellio.entities.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Email implements Serializable {

	public class AttachmentClass implements Serializable{
		private final String  attachmentName;
		private final String attachmentType;
		private final byte[] attachmentContent;
		public AttachmentClass(String attachmentName, String attachmentType,
				byte[] attachmentContent) {
			super();
			this.attachmentName = attachmentName;
			this.attachmentType = attachmentType;
			this.attachmentContent = attachmentContent;
		}
		public String getAttachmentName() {
			return attachmentName;
		}
		public String getAttachmentType() {
			return attachmentType;
		}
		public byte[] getAttachmentContent() {
			return attachmentContent;
		}
	}
	
	private List<AttachmentClass> attachments;
	
	public List<AttachmentClass> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<String> attachmentNames, List<String> attachmentTypes, List<byte[]> attchmentContents) {
		this.attachments = new ArrayList<AttachmentClass>();
		if (attachmentNames.size() == attachmentTypes.size() && attachmentTypes.size() == attchmentContents.size()){
			for(int i =0; i < attachmentNames.size(); i++){
				AttachmentClass newAttachment = new AttachmentClass(attachmentNames.get(i), attachmentTypes.get(i), attchmentContents.get(i));
				this.attachments.add(newAttachment);
			}
		}
	}
	private String folderNames;
	private List<String> recipients;
//	private String messageContent;
	private String messageContentText;
	private String messageContentHtml;
	private List<String> froms;
	private String receivedDate;
	private String sentDate;
	private Integer size;
	private String subject;
	private List<String> flags;
	private String language;
	private String contentEncoding = "UTF-8"; 
	
	public String getContentEncoding() {
		return contentEncoding;
	}
	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public String getFolderNames() {
		return folderNames;
	}
	public void setFolderNames(String folderNames) {
		this.folderNames = folderNames;
	}
	public List<String> getRecipients() {
		return recipients;
	}
	public void setRecipients(List<String> recipients) {
		this.recipients = recipients;
	}

	
	public String getMessageContentText() {
		return messageContentText;
	}
	public void setMessageContentText(String messageContentText) {
		this.messageContentText = messageContentText;
	}
	public String getMessageContentHtml() {
		return messageContentHtml;
	}
	public void setMessageContentHtml(String messageContentHtml) {
		this.messageContentHtml = messageContentHtml;
	}
	public List<String> getFroms() {
		return froms;
	}
	public void setFroms(List<String> froms) {
		this.froms = froms;
	}
	public String getReceivedDate() {
		return receivedDate;
	}
	public void setReceivedDate(String receivedDate) {
		this.receivedDate = receivedDate;
	}
	public String getSentDate() {
		return sentDate;
	}
	public void setSentDate(String sentDate) {
		this.sentDate = sentDate;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public List<String> getFlags() {
		return flags;
	}
	public void setFlags(List<String> flags) {
		this.flags = flags;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
}
