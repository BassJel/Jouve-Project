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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class CopyFieldServicesImpl extends BaseCRUDServicesImpl<CopyField> implements CopyFieldServices {


    public CopyFieldServicesImpl(EntityManager entityManager) {
        super(CopyField.class, entityManager);
    }

    
	@Override
	public	List<CopyField> newCopyFields(RecordCollection collection, String source, String destination, Integer maxChars) {

		List<CopyField> copyFields = new ArrayList<CopyField>();
		IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
		
		// Plusieurs destinations possibles. Normalement les traiter toutes!
		List<IndexField> destinationIndexFields = getIndexFieldAlimentedBy(destination, collection);
		if(destinationIndexFields == null){
			throw new RuntimeException("CopyField associated with destination " + destination + " not added: no indexField associated with destination");
		}
		
		// Une source suffit:
		IndexField sourceIndexField = indexFieldServices.getFirstIndexFieldAlimentedOnlyBy(source, collection);
		
		if (sourceIndexField == null) {
			// voir s'il contient des caracteres speciaux :
			if (source.equals("*")) {
				//its OK
				
			} else {
				if (source.contains("*")) {
					throw new RuntimeException("Dynamic copyFields are not considred in the current implementation");
				} else {
					throw new RuntimeException(	"CopyField associated with source "	+ source + " not added: no indexField associated with source");
				}
			}
		}

		for (IndexField destinationIndexField : destinationIndexFields){
			CopyField copyField = new CopyField();
			if(sourceIndexField == null){
				//source ==  *
				copyField.setSourceAllFields(true);
			} else {
				sourceIndexField.addCopyFieldSource(copyField);
			}
			destinationIndexField.addCopyFieldDest(copyField);
			
			if (maxChars != null){
				copyField.setMaxChars(maxChars);
			}
			copyFields.add(copyField);
		}

		
		return copyFields;
	}


	//FIXME dynamicFields pas traités 
	private List<IndexField> getIndexFieldAlimentedBy(String metaName,
			RecordCollection collection) {
		List<IndexField> indexFields = new ArrayList<IndexField>();
		
		for(IndexField indexField : collection.getIndexFields()) {
			if (indexField.getMetaNames().contains(metaName)) {
				indexFields.add(indexField);
			}
		}
		return indexFields;
	}
	
	

}
