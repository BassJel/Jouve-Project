package com.doculibre.constellio.wicket.panels.admin.indexField.dto;

import org.apache.wicket.model.IDetachable;

import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.wicket.models.EntityModel;

@SuppressWarnings("serial")
public class CopyFieldDTO implements IDetachable {
	
	private Long copyFieldId;

	private EntityModel<IndexField> indexFieldSourceModel;

	private EntityModel<IndexField> indexFieldDestModel;
	
	private Integer maxChars;
	
	private Boolean sourceAllFields = false;
	
	public CopyFieldDTO(CopyField copyField) {
		this.copyFieldId = copyField.getId();
		this.indexFieldSourceModel = new EntityModel<IndexField>(copyField.getIndexFieldSource());
		this.indexFieldDestModel = new EntityModel<IndexField>(copyField.getIndexFieldDest());
		this.maxChars = copyField.getMaxChars();
		this.sourceAllFields = copyField.isSourceAllFields();
	}
	
	public CopyField toCopyField() {
		CopyField copyField = new CopyField();
		copyField.setId(copyFieldId);
		copyField.setIndexFieldSource(getIndexFieldSource());
		copyField.setIndexFieldDest(getIndexFieldDest());
		copyField.setMaxChars(getMaxChars());
		copyField.setSourceAllFields(isSourceAllFields());
		return copyField;
	}

	public IndexField getIndexFieldSource() {
		return indexFieldSourceModel.getObject();
	}

	public void setIndexFieldSource(IndexField indexFieldSource) {
        if (this.indexFieldSourceModel == null) {
            this.indexFieldSourceModel = new EntityModel<IndexField>(indexFieldSource);
        } else {
            this.indexFieldSourceModel.setObject(indexFieldSource);
        }
	}

	public IndexField getIndexFieldDest() {
		return indexFieldDestModel.getObject();
	}

	public void setIndexFieldDest(IndexField indexFieldDest) {
        if (this.indexFieldDestModel == null) {
            this.indexFieldDestModel = new EntityModel<IndexField>(indexFieldDest);
        } else {
            this.indexFieldDestModel.setObject(indexFieldDest);
        }
	}

	public Integer getMaxChars() {
		return maxChars;
	}

	public void setMaxChars(Integer maxChars) {
		this.maxChars = maxChars;
	}

	public Boolean isSourceAllFields() {
		return sourceAllFields;
	}

	public void setSourceAllFields(Boolean sourceAllFields) {
		this.sourceAllFields = sourceAllFields;
	}

	@Override
	public void detach() {
		indexFieldSourceModel.detach();
		indexFieldDestModel.detach();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((copyFieldId == null) ? 0 : copyFieldId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CopyFieldDTO other = (CopyFieldDTO) obj;
		if (copyFieldId == null) {
			if (other.copyFieldId != null)
				return false;
		} else if (!copyFieldId.equals(other.copyFieldId))
			return false;
		return true;
	}

}
