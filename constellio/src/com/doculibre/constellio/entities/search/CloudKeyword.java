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

@SuppressWarnings("serial")
public class CloudKeyword implements Serializable, Cloneable, Comparable<CloudKeyword> {
	
	private String keyword;
	private int weight; // 0-5, 0 being not frequent and 5 being very frequent 
	
	public CloudKeyword(String keyword, int weight) {
		super();
		this.keyword = keyword;
		this.weight = weight;
	}

	public String getKeyword() {
		return keyword;
	}

	public int getWeight() {
		return weight;
	}

	@Override
	protected CloudKeyword clone() {
		CloudKeyword clone = new CloudKeyword(keyword, weight);
		return clone;
	}

	public int compareTo(CloudKeyword o) {
		return keyword.compareTo(o.keyword);
	}

	@Override
	public String toString() {
		return "[keyword: " + keyword + ", weight: " + weight + "]";
	}

}
