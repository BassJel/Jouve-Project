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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.doculibre.constellio.entities.I18NLabel;
import com.doculibre.constellio.entities.skos.SkosConcept;
import com.doculibre.constellio.entities.skos.SkosConceptAltLabel;
import com.doculibre.constellio.entities.skos.Thesaurus;
import com.doculibre.constellio.lucene.BaseLuceneIndexHelper;
import com.doculibre.constellio.services.SkosServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

@SuppressWarnings("serial")
public class SkosIndexHelperImpl extends BaseLuceneIndexHelper<SkosConcept> implements SkosIndexHelper {

    private static final String ID = "id";
    private static final String THESAURUS_ID = "thesaurus_id";
    private static final String PREF_LABEL = "prefLabel";
    private static final String ALT_LABEL = "altLabel";
    
    public SkosIndexHelperImpl() {
        super("skos");
    }

    @Override
    protected Field[] createIndexFields() {
        List<Field> fields = new ArrayList<Field>();
        fields.add(new Field(ID, "", Field.Store.YES, Field.Index.NOT_ANALYZED));
        fields.add(new Field(THESAURUS_ID, "", Field.Store.YES, Field.Index.NOT_ANALYZED));
        Field prefLabelField = createDefaultIndexField(PREF_LABEL);
        prefLabelField.setBoost(4.0F);
        fields.add(prefLabelField);
        fields.add(createDefaultIndexField(ALT_LABEL));
        
        for (Locale locale : ConstellioSpringUtils.getSupportedLocales()) {
        	String suffix = "_" + locale.getLanguage();
            Field prefLabelLocaleField = createDefaultIndexField(PREF_LABEL + suffix);
            prefLabelLocaleField.setBoost(4.0F);
            fields.add(prefLabelLocaleField);
            fields.add(createDefaultIndexField(ALT_LABEL + suffix));
        }
        
        return fields.toArray(new Field[0]);
    }

    @Override
    protected String[] createSearchFields(Field[] indexFields) {
    	List<String> fieldNames = new ArrayList<String>();
    	fieldNames.add(PREF_LABEL);
    	fieldNames.add(ALT_LABEL);
        for (Locale locale : ConstellioSpringUtils.getSupportedLocales()) {
        	String suffix = "_" + locale.getLanguage();
            fieldNames.add(PREF_LABEL + suffix);
            fieldNames.add(ALT_LABEL + suffix);
        }
        return fieldNames.toArray(new String[0]);
    }

    @Override
    protected List<SkosConcept> getAll() {
        List<SkosConcept> skosConcepts = new ArrayList<SkosConcept>();
        SkosServices skosServices = ConstellioSpringUtils.getSkosServices();
        for (Thesaurus thesaurus : skosServices.list()) {
            for (SkosConcept topConcept : thesaurus.getTopConcepts()) {
                addConceptAndSubConcepts(topConcept, skosConcepts);
            }
        }
        return skosConcepts;
    }
    
    private void addConceptAndSubConcepts(SkosConcept skosConcept, List<SkosConcept> skosConcepts) {
        skosConcepts.add(skosConcept);
        for (SkosConcept narrower : skosConcept.getNarrower()) {
            // recursive call
            addConceptAndSubConcepts(narrower, skosConcepts);
        }
    }

    @Override
    protected String getUniqueIndexFieldName() {
        return ID;
    }

    @Override
    protected String getUniqueIndexFieldValue(SkosConcept object) {
        return object.getId().toString();
    }

    @Override
    protected void populateIndexField(SkosConcept skosConcept, Field indexField, Document doc) {
        String indexFieldName = indexField.name();
        List<Locale> supportedLocales = ConstellioSpringUtils.getSupportedLocales();
        if (indexFieldName.equals(ID)) {
            indexField.setValue(skosConcept.getId().toString());
        } else if (indexFieldName.equals(THESAURUS_ID)) {
            indexField.setValue(skosConcept.getThesaurus().getId().toString());
        } else if (indexFieldName.equals(PREF_LABEL)) {
            boolean first = true;
            for (I18NLabel prefLabel : skosConcept.getPrefLabels()) {
                for (Locale locale : prefLabel.getValues().keySet()) {
                    if (supportedLocales.contains(locale)) {
                        if (first) {
                            indexField.setValue(prefLabel.getValue(locale));
                            first = false;
                        } else {
                            Field extraField = createDefaultIndexField(PREF_LABEL);
                            extraField.setValue(prefLabel.getValue(locale));
                            doc.add(extraField);
                        }
                    }
                }
            }
        } else if (indexFieldName.equals(ALT_LABEL)) {
            boolean first = true;
            for (SkosConceptAltLabel altLabel : skosConcept.getAltLabels()) {
                Locale locale = altLabel.getLocale();
                if (supportedLocales.contains(locale)) {
                    for (String altLabelValue : skosConcept.getAltLabels(locale)) {
                        if (first) {
                            indexField.setValue(altLabelValue);
                            first = false;
                        } else {
                            Field extraField = createDefaultIndexField(ALT_LABEL);
                            extraField.setValue(altLabelValue);
                            doc.add(extraField);
                        }
                    }
                }
            }
        } else if (indexFieldName.startsWith(PREF_LABEL)) {
        	if (indexFieldName.indexOf("_") != -1) {
        		String language = StringUtils.substringAfter(indexFieldName, "_");
        		Locale locale = new Locale(language);
        		String prefLabel = skosConcept.getPrefLabel(locale);
        		if (StringUtils.isNotBlank(prefLabel)) {
        			indexField.setValue(prefLabel);
        		}
        	}
        } else if (indexFieldName.startsWith(ALT_LABEL)) {
    		String language = StringUtils.substringAfter(indexFieldName, "_");
    		Locale locale = new Locale(language);
    		String prefLabel = skosConcept.getPrefLabel(locale);
    		if (StringUtils.isNotBlank(prefLabel)) {
                boolean first = true;
                for (String altLabelValue : skosConcept.getAltLabels(locale)) {
                    if (first) {
                        indexField.setValue(altLabelValue);
                        first = false;
                    } else {
                        Field extraField = createDefaultIndexField(indexFieldName);
                        extraField.setValue(altLabelValue);
                        doc.add(extraField);
                    }
                }
    		}
        }
    }

    @Override
    protected SkosConcept toObject(int docId, Document doc) {
        Long id = new Long(doc.getFieldable(ID).stringValue());
        SkosServices skosServices = ConstellioSpringUtils.getSkosServices();
        return skosServices.getSkosConcept(id);
    }

    @Override
    public void add(Thesaurus thesaurus) {
        delete(thesaurus);
        try {
            Directory directory = FSDirectory.open(getIndexDir());
            Analyzer analyzer = getAnalyzerProvider().getAnalyzer(Locale.FRENCH);
            IndexWriter indexWriter = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);

            List<SkosConcept> skosConcepts = new ArrayList<SkosConcept>();
            for (SkosConcept topConcept : thesaurus.getTopConcepts()) {
                addConceptAndSubConcepts(topConcept, skosConcepts);
            }
            for (SkosConcept skosConcept : skosConcepts) {
                add(skosConcept, indexWriter);
            }
            indexWriter.optimize();
            indexWriter.close();
            directory.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Thesaurus thesaurus) {
        try {
            Directory directory = FSDirectory.open(getIndexDir());
            if (IndexReader.indexExists(directory)) {
                IndexReader indexReader = IndexReader.open(directory, false);
                Term term = new Term(THESAURUS_ID, thesaurus.getId().toString());
                indexReader.deleteDocuments(term);
                indexReader.close();
            }
            directory.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SkosConcept> searchAltLabel(String input, Thesaurus thesaurus, Locale locale) {
    	input = adjustInputWildcard(input);
        StringBuffer luceneQuery = new StringBuffer();
        luceneQuery.append(THESAURUS_ID + ":" + thesaurus.getId());
        luceneQuery.append(" AND ");
        if (locale != null) {
        	String suffix = "_" + locale.getLanguage();
            luceneQuery.append(ALT_LABEL + suffix + ":" + input);
        } else {
            luceneQuery.append(ALT_LABEL + ":" + input);
        }
        return super.search(luceneQuery.toString());
    }

    @Override
    public List<SkosConcept> searchPrefLabel(String input, Thesaurus thesaurus, Locale locale) {
    	input = adjustInputWildcard(input);
        StringBuffer luceneQuery = new StringBuffer();
        luceneQuery.append(THESAURUS_ID + ":" + thesaurus.getId());
        luceneQuery.append(" AND ");
        if (locale != null) {
        	String suffix = "_" + locale.getLanguage();
            luceneQuery.append(PREF_LABEL + suffix + ":" + input);
        } else {
            luceneQuery.append(PREF_LABEL + ":" + input);
        }
        return super.search(luceneQuery.toString());
    }

    @Override
    public List<SkosConcept> searchAllLabels(String input, Thesaurus thesaurus, Locale locale) {
    	input = adjustInputWildcard(input);
        String adjustedInput = getAndQuery(input);

        StringBuffer luceneQuery = new StringBuffer();
        luceneQuery.append(THESAURUS_ID + ":" + thesaurus.getId());
        luceneQuery.append(" AND ");
        if (locale != null) {
        	String suffix = "_" + locale.getLanguage();
            luceneQuery.append("(");
            luceneQuery.append(PREF_LABEL + suffix + ":");
            luceneQuery.append("(");
            luceneQuery.append(adjustedInput);
            luceneQuery.append(")");
            luceneQuery.append(" OR ");
            luceneQuery.append(ALT_LABEL + suffix + ":");
            luceneQuery.append("(");
            luceneQuery.append(adjustedInput);
            luceneQuery.append(")");
            luceneQuery.append(")");
        } else {
            luceneQuery.append("(");
            luceneQuery.append(PREF_LABEL + ":");
            luceneQuery.append("(");
            luceneQuery.append(adjustedInput);
            luceneQuery.append(")");
            luceneQuery.append(" OR ");
            luceneQuery.append(ALT_LABEL + ":");
            luceneQuery.append("(");
            luceneQuery.append(adjustedInput);
            luceneQuery.append(")");
            luceneQuery.append(")");
        }
        return super.search(luceneQuery.toString());
    }
    
    private String adjustInputWildcard(String input) {
    	String result;
    	if (input.endsWith("*")) {
//    		String beforeWildCard = StringUtils.substringBefore(input, "*");
//    		result = analyze(beforeWildCard);
    		result = input;
    	} else {
    		result = "\"" + input + "\"";
    	}
    	return result;
    }
    
    public static void main(String[] args) {
        SkosIndexHelperImpl indexHelper = new SkosIndexHelperImpl();
        indexHelper.rebuild();
    }

}
