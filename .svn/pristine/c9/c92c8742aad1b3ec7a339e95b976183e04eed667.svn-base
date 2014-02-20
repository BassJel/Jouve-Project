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

/**
 * org.apache.lucene.analysis.br.BrazilianAnalyzer
 * org.apache.lucene.analysis.cn.ChineseAnalyzer
 * org.apache.lucene.analysis.cjk.CJKAnalyzer
 * org.apache.lucene.analysis.cz.CzechAnalyzer
 * org.apache.lucene.analysis.nl.DutchAnalyzer
 * org.apache.lucene.analysis.fr.FrenchAnalyzer
 * org.apache.lucene.analysis.de.GermanAnalyzer
 * org.apache.lucene.analysis.el.GreekAnalyzer
 * org.apache.lucene.analysis.KeywordAnalyzer
 * org.apache.lucene.index.memory.PatternAnalyzer
 * org.apache.lucene.analysis.PerFieldAnalyzerWrapper
 * org.apache.lucene.analysis.query.QueryAutoStopWordAnalyzer
 * org.apache.lucene.analysis.ru.RussianAnalyzer
 * org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper
 * org.apache.lucene.analysis.SimpleAnalyzer
 * org.apache.lucene.analysis.snowball.SnowballAnalyzer
 * org.apache.lucene.analysis.standard.StandardAnalyzer
 * org.apache.lucene.analysis.StopAnalyzer
 * org.apache.lucene.analysis.th.ThaiAnalyzer
 * org.apache.lucene.analysis.WhitespaceAnalyzer
 * 
 * @author Vincent Dussault
 */
@SuppressWarnings("serial")
@Entity
public class AnalyzerClass extends BaseConstellioEntity {
	
	@SuppressWarnings("deprecation")
	public static final String[] DEFAULT_VALUES = {
		org.apache.lucene.analysis.ar.ArabicAnalyzer.class.getName(),
		org.apache.lucene.analysis.bg.BulgarianAnalyzer.class.getName(),
		org.apache.lucene.analysis.br.BrazilianAnalyzer.class.getName(),
		org.apache.lucene.analysis.ca.CatalanAnalyzer.class.getName(),
		org.apache.lucene.analysis.cjk.CJKAnalyzer.class.getName(),
		org.apache.lucene.analysis.cn.ChineseAnalyzer.class.getName(),
		org.apache.lucene.analysis.cz.CzechAnalyzer.class.getName(),
		org.apache.lucene.analysis.da.DanishAnalyzer.class.getName(),
		org.apache.lucene.analysis.de.GermanAnalyzer.class.getName(),
		org.apache.lucene.analysis.el.GreekAnalyzer.class.getName(),
		org.apache.lucene.analysis.en.EnglishAnalyzer.class.getName(),
		org.apache.lucene.analysis.es.SpanishAnalyzer.class.getName(),
		org.apache.lucene.analysis.eu.BasqueAnalyzer.class.getName(),
		org.apache.lucene.analysis.fa.PersianAnalyzer.class.getName(),
		org.apache.lucene.analysis.fi.FinnishAnalyzer.class.getName(),
		org.apache.lucene.analysis.fr.FrenchAnalyzer.class.getName(),
		org.apache.lucene.analysis.gl.GalicianAnalyzer.class.getName(),
		org.apache.lucene.analysis.hi.HindiAnalyzer.class.getName(),
		org.apache.lucene.analysis.hu.HungarianAnalyzer.class.getName(),
		org.apache.lucene.analysis.hy.ArmenianAnalyzer.class.getName(),
		org.apache.lucene.analysis.id.IndonesianAnalyzer.class.getName(),
		org.apache.lucene.analysis.it.ItalianAnalyzer.class.getName(),
		org.apache.lucene.analysis.lv.LatvianAnalyzer.class.getName(),
		org.apache.lucene.analysis.miscellaneous.PatternAnalyzer.class.getName(),
		org.apache.lucene.analysis.nl.DutchAnalyzer.class.getName(),
		org.apache.lucene.analysis.no.NorwegianAnalyzer.class.getName(),
		org.apache.lucene.analysis.pt.PortugueseAnalyzer.class.getName(),
		org.apache.lucene.analysis.query.QueryAutoStopWordAnalyzer.class.getName(),
		org.apache.lucene.analysis.ro.RomanianAnalyzer.class.getName(),
		org.apache.lucene.analysis.ru.RussianAnalyzer.class.getName(),
		org.apache.lucene.analysis.snowball.SnowballAnalyzer.class.getName(),
		org.apache.lucene.analysis.sv.SwedishAnalyzer.class.getName(),
		org.apache.lucene.analysis.th.ThaiAnalyzer.class.getName(),
		org.apache.lucene.analysis.tr.TurkishAnalyzer.class.getName()
	};

	private String className;

	private ConnectorManager connectorManager;

	@Column(nullable = false)
	public String getClassName() {
		return className;
	}

	public void setClassName(String fieldTypeClass) {
		this.className = fieldTypeClass;
	}

	@ManyToOne 
	@JoinColumn(nullable = false, updatable = false)
	public ConnectorManager getConnectorManager() {
		return connectorManager;
	}

	public void setConnectorManager(ConnectorManager connectorManager) {
		this.connectorManager = connectorManager;
	}

}
