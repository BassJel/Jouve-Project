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

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.lucene.BaseLuceneIndexHelper;
import com.doculibre.constellio.services.FreeTextTagServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

@SuppressWarnings("serial")
public class FreeTextTagIndexHelperImpl extends BaseLuceneIndexHelper<FreeTextTag> implements FreeTextTagIndexHelper {

    private static final String ID = "id";
    private static final String FREE_TEXT = "freeText";
    
    public FreeTextTagIndexHelperImpl() {
        super("freeTextTags");
    }

    @Override
    protected Field[] createIndexFields() {
        List<Field> fields = new ArrayList<Field>();
        fields.add(new Field(ID, "", Field.Store.YES, Field.Index.NOT_ANALYZED));
        fields.add(createDefaultIndexField(FREE_TEXT));
        return fields.toArray(new Field[0]);
    }

    @Override
    protected List<FreeTextTag> getAll() {
        FreeTextTagServices freeTextTagServices = ConstellioSpringUtils.getFreeTextTagServices();
        return freeTextTagServices.list();
    }

    @Override
    protected String getUniqueIndexFieldName() {
        return ID;
    }

    @Override
    protected String getUniqueIndexFieldValue(FreeTextTag object) {
        return object.getId().toString();
    }

    @Override
    protected void populateIndexField(FreeTextTag object, Field indexField, Document doc) {
        String indexFieldName = indexField.name();
        if (indexFieldName.equals(ID)) {
            indexField.setValue(object.getId().toString());
        } else if (indexFieldName.equals(FREE_TEXT)) {
            indexField.setValue(object.getFreeText());
        } 
    }

    @Override
    protected FreeTextTag toObject(int docId, Document doc) {
        Long id = new Long(doc.getField(ID).stringValue());
        FreeTextTagServices freeTextTagServices = ConstellioSpringUtils.getFreeTextTagServices();
        return freeTextTagServices.get(id);
    }
    
    public static void main(String[] args) {
        FreeTextTagIndexHelperImpl indexHelper = new FreeTextTagIndexHelperImpl();
        indexHelper.rebuild();
    }

}
