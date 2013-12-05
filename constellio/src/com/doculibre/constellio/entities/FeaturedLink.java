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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyClass;

@SuppressWarnings("serial")
@Entity
public class FeaturedLink extends ConstellioLabelledEntity {

	private static final String LABEL_TITLE = "title";
	private static final String LABEL_DESCRIPTION = "description";

	private RecordCollection recordCollection;

    private Set<String> keywords = new HashSet<String>();

    private Set<String> keywordsAnalyzed = new HashSet<String>();

	private Set<I18NLabel> labels = new HashSet<I18NLabel>();

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public RecordCollection getRecordCollection() {
		return recordCollection;
	}

	public void setRecordCollection(RecordCollection recordCollection) {
		this.recordCollection = recordCollection;
	}

    @ElementCollection(targetClass = String.class)
    @CollectionTable(name =  "FeaturedLink_Keywords", joinColumns = @JoinColumn(name = "featuredLink_id"))
    @MapKeyClass(String.class)
    @Column(name="keyword", length = 10 * 1024)
	public Set<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}
	
	public void addKeyword(String keyword) {
		this.keywords.add(keyword);
	}

    @ElementCollection(targetClass = String.class)
    @CollectionTable(name =  "FeaturedLink_KeywordsAnalyzed", joinColumns = @JoinColumn(name = "featuredLink_id"))
    @MapKeyClass(String.class)
    @Column(name="keyword", length = 10 * 1024)
	public Set<String> getKeywordsAnalyzed() {
		return keywordsAnalyzed;
	}

	public void setKeywordsAnalyzed(Set<String> keywordsAnalyzed) {
		this.keywordsAnalyzed = keywordsAnalyzed;
	}
	
	public void addKeywordAnalyzed(String keywordAnalyzed) {
		this.keywordsAnalyzed.add(keywordAnalyzed);
	}

	public String getTitle(Locale locale) {
		return getLabel(LABEL_TITLE, locale);
	}

	public void setTitle(String value, Locale locale) {
		setLabel(LABEL_TITLE, value, locale);
	}

	public String getDescription(Locale locale) {
		String description = getLabel(LABEL_DESCRIPTION, locale);
		return description;
	}

	public void setDescription(String value, Locale locale) {
		setLabel(LABEL_DESCRIPTION, value, locale);
	}

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "FeaturedLink_Labels", joinColumns = { @JoinColumn(name = "featuredLink_id") }, inverseJoinColumns = { @JoinColumn(name = "label_id") })
	@Override
	protected Set<I18NLabel> getLabels() {
		return this.labels;
	}

	@Override
	protected void setLabels(Set<I18NLabel> labels) {
		this.labels = labels;
	}

}
