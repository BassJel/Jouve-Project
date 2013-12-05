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

import java.io.Serializable;

@SuppressWarnings("serial")
public class ProgressInfo implements Serializable {

	private int total;

	private int currentIndex = -1;

	public ProgressInfo() {
		super();
	}

	public synchronized int getTotal() {
		return total;
	}
	
	public synchronized void setTotal(int total) {
		this.total = total;
	}
	
	public synchronized int getCurrentIndex() {
		return currentIndex;
	}

	public synchronized void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

    public synchronized void incrementCurrentIndex() {
        this.currentIndex++;
    }

}
