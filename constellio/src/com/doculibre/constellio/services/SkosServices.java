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
package com.doculibre.constellio.services;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.doculibre.constellio.entities.Categorization;
import com.doculibre.constellio.entities.skos.SkosConcept;
import com.doculibre.constellio.entities.skos.Thesaurus;
import com.doculibre.constellio.utils.ProgressInfo;

public interface SkosServices extends BaseCRUDServices<Thesaurus> {
    
    Thesaurus importThesaurus(InputStream input, ProgressInfo progressInfo, List<String> errorMessages);
    
    List<Categorization> importCategorizations(InputStream input, Thesaurus thesaurus);
    
    Set<SkosConcept> searchPrefLabel(String input, Thesaurus thesaurus, Locale locale);
    
    Set<SkosConcept> searchAltLabels(String input, Thesaurus thesaurus, Locale locale);

    Set<SkosConcept> searchAllLabels(String input, Thesaurus thesaurus, Locale locale);
    
    SkosConcept getSkosConcept(Long id);
    
    Set<SkosConcept> merge(Thesaurus initialThesaurus, Thesaurus modifiedThesaurus);

    void makeTransient(SkosConcept deletedConcept);

    Set<SkosConcept> getByPrefLabel(String prefLabel, Thesaurus thesaurus, Locale locale);

	Set<SkosConcept> getByAltLabel(String altLabel, Thesaurus thesaurus, Locale locale);

}
