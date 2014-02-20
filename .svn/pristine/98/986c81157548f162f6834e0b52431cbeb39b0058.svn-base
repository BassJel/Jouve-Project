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
package com.doculibre.constellio.entities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;

@SuppressWarnings("serial")
@Entity
public class RawContent extends BaseConstellioEntity {
	
	private byte[] content;
	
	private long length; 
	
//	private Record record;
	
	private Long recordId;

	public RawContent() {}
	
	@Basic (fetch = FetchType.LAZY)
	@Lob
	@Column (length = 10 * 1024 * 1024)
	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
		this.setLength(content.length);
	}

//  @ManyToOne (fetch = FetchType.LAZY)
//	@JoinColumn(nullable = false, updatable = false)
//	public Record getRecord() {
//		return record;
//	}
//
//	public void setRecord(Record record) {
//		this.record = record;
//	}

	public long getLength() {
		return length;
	}

	@Column (name = "record_id")
	public Long getRecordId() {
		return recordId;
	}

	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	private void setLength(long length) {
		this.length = length;
	}
}
