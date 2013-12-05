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
package com.doculibre.constellio.lucene;

import java.io.Serializable;
import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import com.doculibre.analyzer.FrenchAnalyzer;

@SuppressWarnings("serial")
public class AnalyzerProvider implements Serializable {
	
	public final Analyzer getAnalyzer(Locale locale) {
	    if (locale.getLanguage().equals(Locale.FRENCH.getLanguage())) {
	        return new FrenchAnalyzer(Version.LUCENE_34);
	    } else {
	        return new StandardAnalyzer(Version.LUCENE_34);
	    }
	}

}
