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
package com.doculibre.constellio.entities.relevance;

import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.doculibre.constellio.entities.BaseConstellioEntity;

@SuppressWarnings("serial")
@Entity
public class BoostRule extends BaseConstellioEntity {
	private double boost = 1;
	
	private String regex = "";
	
	@Transient
	private Pattern pattern = null;
	
	
	private RecordCollectionBoost recordCollectionBoost;
	
	public double getBoost() {
		return boost;
	}
	
	public void setBoost(double boost) {
		this.boost = boost;
	}

	public void setRegex(String regex) {
		this.regex = regex;
		this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}
	
	public String getRegex() {
		return regex;
	}
	
	@Transient
	public Pattern getPattern() {
		if (pattern == null){
			this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		}
		return pattern;
	}

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public RecordCollectionBoost getRecordCollectionBoost() {
		return recordCollectionBoost;
	}
	
	public void setRecordCollectionBoost(RecordCollectionBoost recordCollectionBoost) {
		this.recordCollectionBoost = recordCollectionBoost;
	}

	
}
