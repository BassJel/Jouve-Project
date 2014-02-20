package com.doculibre.constellio.entities.searchInterface;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.doculibre.constellio.entities.BaseConstellioEntity;

@SuppressWarnings("serial")
@Entity
public class SearchInterfaceContextParam extends BaseConstellioEntity {
	
	public static final String TEXT_PARAM = "text"; 
	public static final String IMG_PARAM = "img"; 
	public static final String TEXT_FILE_PARAM = "textFile"; 
	public static final String BINARY_FILE_PARAM = "binaryFile"; 
	public static final String REQUEST_PARAM = "requestParam"; 
	
	private String paramName;
	private String paramType;
	private String contentType;
	private String textValue;
	private byte[] binaryValue;
	private String fileName;
	private long fileLength;
	private Date lastModified;

	private SearchInterfaceContext searchInterfaceContext;
	
	public SearchInterfaceContextParam() {
		super();
	}
	
	public SearchInterfaceContextParam(SearchInterfaceContextParam copyFrom) {
		super();
		this.paramName = copyFrom.paramName;
		this.paramType = copyFrom.paramType;
		this.contentType = copyFrom.contentType;
		this.textValue = copyFrom.textValue;
		this.binaryValue = copyFrom.binaryValue;
		this.fileName = copyFrom.fileName;
		this.fileLength = copyFrom.fileLength;
		this.lastModified = copyFrom.lastModified;
	}
	
	public String getParamName() {
		return paramName;
	}

	public void setParamName(String key) {
		this.paramName = key;
	}
	
	public String getParamType() {
		return paramType;
	}
	
	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	@Column (length = 10 * 1024)
	public String getTextValue() {
		return textValue;
	}
	
	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	@Lob
	@Column (length = 1024 * 1024)
	public byte[] getBinaryValue() {
		return binaryValue;
	}
	
	public void setBinaryValue(byte[] binaryValue) {
		this.binaryValue = binaryValue;
	}

	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public long getFileLength() {
		return fileLength;
	}
	
	public void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}
	
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	public SearchInterfaceContext getSearchInterfaceContext() {
		return searchInterfaceContext;
	}
	
	public void setSearchInterfaceContext(SearchInterfaceContext searchInterfaceContext) {
		this.searchInterfaceContext = searchInterfaceContext;
	}
	
	@Transient
	public boolean isTextParam() {
		return TEXT_PARAM.equals(paramType) || isRequestParam();
	}
	
	@Transient
	public boolean isRequestParam() {
		return REQUEST_PARAM.equals(paramType);
	}
	
	@Transient
	public boolean isImgParam() {
		return IMG_PARAM.equals(paramType);
	}
	
	@Transient
	public boolean isTextFileParam() {
		return TEXT_FILE_PARAM.equals(paramType);
	}
	
	@Transient
	public boolean isBinaryFileParam() {
		return BINARY_FILE_PARAM.equals(paramType) || isImgParam() || isTextFileParam();
	}

	@Transient
	public SearchInterfaceContextParam prepareChild() {
		SearchInterfaceContextParam child = new SearchInterfaceContextParam();
		child.setParamName(paramName);
		child.setParamType(paramType);
		return child;
	}

	@Override
	public String toString() {
		return "SearchInterfaceContextParam [paramName=" + paramName + ", paramType=" + paramType
				+ ", mimeType=" + contentType + ", textValue=" + textValue + ", fileName=" + fileName
				+ ", fileLength=" + fileLength + ", lastModified=" + lastModified + "]";
	}

	public boolean isSameValues(SearchInterfaceContextParam other) {
		if (!Arrays.equals(binaryValue, other.binaryValue))
			return false;
		if (contentType == null) {
			if (other.contentType != null)
				return false;
		} else if (!contentType.equals(other.contentType))
			return false;
		if (fileLength != other.fileLength)
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (paramName == null) {
			if (other.paramName != null)
				return false;
		} else if (!paramName.equals(other.paramName))
			return false;
		if (paramType == null) {
			if (other.paramType != null)
				return false;
		} else if (!paramType.equals(other.paramType))
			return false;
		if (textValue == null) {
			if (other.textValue != null)
				return false;
		} else if (!textValue.equals(other.textValue))
			return false;
		return true;
	}
	
}
