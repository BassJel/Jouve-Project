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

import javax.persistence.Entity;

/**
 * Contains Solr's cache related configuration options
 * 
 * @see http://wiki.apache.org/solr/SolrCaching
 * @author francisbaril
 * 
 */
@SuppressWarnings("serial")
@Entity
public class Cache extends BaseConstellioEntity implements Cloneable {

	private String cacheClass;

	private long size;

	private long initialSize;

	private long autowarmCount;

	/**
	 * Optionnal
	 */
	private Long minSize;

	/**
	 * Optionnal
	 */
	private Long acceptableSize;

	/**
	 * Optionnal
	 */
	private Boolean cleanupThread;

	/**
	 * Optionnal
	 */
	private String regeneratorClass;

	public String getCacheClass() {
		return cacheClass;
	}

	public void setCacheClass(String cacheClass) {
		this.cacheClass = cacheClass;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(long initialSize) {
		this.initialSize = initialSize;
	}

	public long getAutowarmCount() {
		return autowarmCount;
	}

	public void setAutowarmCount(long autowarmCount) {
		this.autowarmCount = autowarmCount;
	}

	public Long getMinSize() {
		return minSize;
	}

	public void setMinSize(Long minSize) {
		this.minSize = minSize;
	}

	public Long getAcceptableSize() {
		return acceptableSize;
	}

	public void setAcceptableSize(Long acceptableSize) {
		this.acceptableSize = acceptableSize;
	}

	public Boolean isCleanupThread() {
		return cleanupThread;
	}

	public void setCleanupThread(Boolean cleanupThread) {
		this.cleanupThread = cleanupThread;
	}

	public void setRegeneratorClass(String regeneratorClass) {
		this.regeneratorClass = regeneratorClass;
	}

	public String getRegeneratorClass() {
		return regeneratorClass;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Cache clone = (Cache) super.clone();
		clone.setId(null);
		return clone;
	}
}
