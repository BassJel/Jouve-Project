package com.doculibre.constellio.wicket.models;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.commons.lang.SerializationUtils;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordTag;
import com.doculibre.constellio.entities.skos.SkosConcept;

@SuppressWarnings("serial")
public class RecordTagModel extends LoadableDetachableModel {
	
	private RecordTag recordTag;
	private byte[] serializedRecordTag;
	
	private RecordModel recordModel;
	private ReloadableEntityModel<FreeTextTag> freeTextTagModel;
	private ReloadableEntityModel<SkosConcept> skosConceptModel;
	
	public RecordTagModel(RecordTag recordTag) {
		this.recordTag = recordTag;
	}

	@Override
	protected final Object load() {
		if (serializedRecordTag != null) {
			recordTag = (RecordTag) SerializationUtils.deserialize(serializedRecordTag);
			serializedRecordTag = null;
			
			Record record = recordModel.getObject();
			FreeTextTag freeTextTag = freeTextTagModel.getObject();
			SkosConcept skosConcept = skosConceptModel.getObject();
			recordTag.setRecord(record);
			recordTag.setFreeTextTag(freeTextTag);
			recordTag.setSkosConcept(skosConcept);
		}
		return recordTag;
	}

	@Override
	public void detach() {
		prepareForSerialization();
		super.detach();
	}

    private void writeObject(ObjectOutputStream oos) throws IOException {
        prepareForSerialization();
        oos.defaultWriteObject();
    }
	
    private void prepareForSerialization() {
        if (recordTag != null) {
        	Record record = recordTag.getRecord();
        	FreeTextTag freeTextTag = recordTag.getFreeTextTag();
        	SkosConcept skosConcept = recordTag.getSkosConcept();

        	if (recordModel != null) {
        		recordModel.detach();
        	}
        	if (freeTextTagModel != null) {
        		freeTextTagModel.detach();
        	}
        	if (skosConceptModel != null) {
        		skosConceptModel.detach();
        	}
    		
        	recordModel = new RecordModel(record);
        	freeTextTagModel = new ReloadableEntityModel<FreeTextTag>(freeTextTag);
        	skosConceptModel = new ReloadableEntityModel<SkosConcept>(skosConcept);
    		
        	recordModel.detach();
    		freeTextTagModel.detach();
    		skosConceptModel.detach();
        	
        	recordTag.setRecord(null);
        	recordTag.setFreeTextTag(null);
        	recordTag.setSkosConcept(null);

            serializedRecordTag = SerializationUtils.serialize(recordTag);
            recordTag = null;
        }
	}

	@Override
	public RecordTag getObject() {
		return (RecordTag) super.getObject();
	}

}
