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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@SuppressWarnings("serial")
@Entity
public class AnalyzerFilter extends BaseConstellioEntity {

	private Boolean ignoreCase;

	private Boolean expand;

	private Boolean enablePositionIncrements;

	private String language;

	// stopwords.txt
	private String wordsText;
	
	// synonyms.txt
	private String synonymsText;
	
	// protwords.txt
	private String protectedText;

	private Integer generateWordParts;

	private Integer generateNumberParts;

	private Integer catenateWords;

	private Integer catenateNumbers;

	private Integer catenateAll;

	private Integer splitOnCaseChange;
	
	private String delimiter;
	 
	private String encoder;
	
	private String pattern;
	 
	private String replacement;
	 
	private String replace;

	private Boolean inject;

	private FilterClass filterClass;

	private Analyzer analyzer;

	public Boolean getIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(Boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public Boolean getExpand() {
		return expand;
	}

	public void setExpand(Boolean expand) {
		this.expand = expand;
	}

	public Boolean getEnablePositionIncrements() {
		return enablePositionIncrements;
	}

	public void setEnablePositionIncrements(Boolean enablePositionIncrements) {
		this.enablePositionIncrements = enablePositionIncrements;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getWordsText() {
		return wordsText;
	}

	public void setWordsText(String wordsText) {
		this.wordsText = wordsText;
	}

	public String getSynonymsText() {
		return synonymsText;
	}

	public void setSynonymsText(String synonymsText) {
		this.synonymsText = synonymsText;
	}

	public String getProtectedText() {
		return protectedText;
	}

	public void setProtectedText(String protectedText) {
		this.protectedText = protectedText;
	}

	public Integer getGenerateWordParts() {
		return generateWordParts;
	}

	public void setGenerateWordParts(Integer generateWordParts) {
		this.generateWordParts = generateWordParts;
	}

	public Integer getGenerateNumberParts() {
		return generateNumberParts;
	}

	public void setGenerateNumberParts(Integer generateNumberParts) {
		this.generateNumberParts = generateNumberParts;
	}

	public Integer getCatenateWords() {
		return catenateWords;
	}

	public void setCatenateWords(Integer catenateWords) {
		this.catenateWords = catenateWords;
	}

	public Integer getCatenateNumbers() {
		return catenateNumbers;
	}

	public void setCatenateNumbers(Integer catenateNumbers) {
		this.catenateNumbers = catenateNumbers;
	}

	public Integer getCatenateAll() {
		return catenateAll;
	}

	public void setCatenateAll(Integer catenateAll) {
		this.catenateAll = catenateAll;
	}

	public Integer getSplitOnCaseChange() {
		return splitOnCaseChange;
	}

	public void setSplitOnCaseChange(Integer splitOnCaseChange) {
		this.splitOnCaseChange = splitOnCaseChange;
	}

	@ManyToOne
	@JoinColumn(nullable = false)
	public FilterClass getFilterClass() {
		return filterClass;
	}

	public void setFilterClass(FilterClass filterClass) {
		this.filterClass = filterClass;
	}

	@ManyToOne
	@JoinColumn(nullable = false)
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getEncoder() {
		return encoder;
	}

	public void setEncoder(String encoder) {
		this.encoder = encoder;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	@Column(name = "replaceCol")
	public String getReplace() {
		return replace;
	}

	public void setReplace(String replace) {
		this.replace = replace;
	}

	public Boolean getInject() {
		return inject;
	}

	public void setInject(Boolean inject) {
		this.inject = inject;
	}

}
