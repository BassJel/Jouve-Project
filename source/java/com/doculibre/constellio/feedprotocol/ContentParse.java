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
package com.doculibre.constellio.feedprotocol;

import java.util.List;

import com.doculibre.constellio.entities.RecordMeta;

public class ContentParse {

	private String content;
	private List<String> md5;
	private List<RecordMeta> metas;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public List<String> getMd5() {
		return md5;
	}
	public void setMd5(List<String> md5) {
		this.md5 = md5;
	}
	
	public List<RecordMeta> getMetas() {
		return metas;
	}
	public void setMetas(List<RecordMeta> metas) {
		this.metas = metas;
	}
}
