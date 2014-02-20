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
package com.doculibre.constellio.lucene.impl;

import java.util.List;
import java.util.Locale;

import com.doculibre.constellio.entities.skos.SkosConcept;
import com.doculibre.constellio.entities.skos.Thesaurus;
import com.doculibre.constellio.lucene.IndexHelper;

public interface SkosIndexHelper extends IndexHelper<SkosConcept> {
    
    void add(Thesaurus thesaurus);
    
    void delete(Thesaurus thesaurus);

    List<SkosConcept> searchPrefLabel(String input, Thesaurus thesaurus, Locale locale);

    List<SkosConcept> searchAltLabel(String input, Thesaurus thesaurus, Locale locale);

    List<SkosConcept> searchAllLabels(String input, Thesaurus thesaurus, Locale locale);

}
