package com.doculibre.constellio.entities.searchInterface;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.doculibre.constellio.entities.BaseConstellioEntity;
import com.doculibre.constellio.entities.I18NLabel;

@SuppressWarnings("serial")
@Entity
public class SearchInterfaceContext extends BaseConstellioEntity {

    public static final String HEAD_TAG_HTML_CONTENT = "headTagHtmlContent";
    public static final String HEADER_HTML_CONTENT = "headerHtmlContent";
    public static final String FOOTER_HTML_CONTENT = "footerHtmlContent";
	
    private String contextName;
	private String curlValue;
	private boolean externalFiles;
	private SearchInterfaceContext parentContext;
	
	private Set<I18NLabel> includedHtmlContents = new HashSet<I18NLabel>();
	
    private Set<SearchInterfaceContextParam> contextParams = new HashSet<SearchInterfaceContextParam>();

    private Set<SearchInterfaceContext> subContexts = new HashSet<SearchInterfaceContext>();

	@Column(nullable = false, unique = true)
	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	@Column(nullable = false, unique = true)
	public String getCurlValue() {
		return curlValue;
	}

	public void setCurlValue(String curlValue) {
		this.curlValue = curlValue;
	}

	public boolean isExternalFiles() {
		return externalFiles;
	}

	public void setExternalFiles(boolean externalFiles) {
		this.externalFiles = externalFiles;
	}

	@ManyToOne
	public SearchInterfaceContext getParentContext() {
		return parentContext;
	}

	public void setParentContext(SearchInterfaceContext parentContext) {
		this.parentContext = parentContext;
	}

    @OneToMany(mappedBy = "searchInterfaceContext", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
	public Set<SearchInterfaceContextParam> getContextParams() {
		return contextParams;
	}

	public void setContextParams(Set<SearchInterfaceContextParam> contextParams) {
		this.contextParams = contextParams;
	}
	
	public void addContextParam(SearchInterfaceContextParam contextParam) {
		this.contextParams.add(contextParam);
		contextParam.setSearchInterfaceContext(this);
	}

    @OneToMany(mappedBy = "parentContext", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
	public Set<SearchInterfaceContext> getSubContexts() {
		return subContexts;
	}

	public void setSubContexts(Set<SearchInterfaceContext> subContexts) {
		this.subContexts = subContexts;
	}
	
	public void addSubContext(SearchInterfaceContext subContext) {
		this.subContexts.add(subContext);
		subContext.setParentContext(this);
	}

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "SearchInterfaceContext_includedHtmlContents", joinColumns = { @JoinColumn(name = "searchInterfaceContext_id") }, inverseJoinColumns = { @JoinColumn(name = "label_id") })
	public Set<I18NLabel> getIncludedHtmlContents() {
		return this.includedHtmlContents;
	}

	protected void setIncludedHtmlContents(Set<I18NLabel> includedHtmlContents) {
		this.includedHtmlContents = includedHtmlContents;
	}
	
	public String getIncludedHtmlContent(String key, Locale locale) {
		I18NLabel matchingLabel = null;
		for (I18NLabel label : getIncludedHtmlContents()) {
			if (label.getKey().equals(key)) {
				matchingLabel = label;
				break;
			}
		}
		return matchingLabel != null ? matchingLabel.getValue(locale) : null;
	}
	
	public void setIncludedHtmlContent(String key, String value, Locale locale) {
		I18NLabel matchingLabel = null;
		for (I18NLabel label : getIncludedHtmlContents()) {
			if (label.getKey().equals(key)) {
				matchingLabel = label;
				break;
			}
		}
		if (matchingLabel == null) {
			matchingLabel = new I18NLabel();
			matchingLabel.setKey(key);
			this.getIncludedHtmlContents().add(matchingLabel);
		}
		matchingLabel.setValue(value, locale);
	}

    public String getHeadTagHtmlContent(Locale locale) {
        return getIncludedHtmlContent(HEAD_TAG_HTML_CONTENT, locale);
    }

    public void setHeadTagHtmlContent(String value, Locale locale) {
    	if (StringUtils.isBlank(value)) {
    		value = null;
    	}
    	setIncludedHtmlContent(HEAD_TAG_HTML_CONTENT, value, locale);
    }

    public String getHeaderHtmlContent(Locale locale) {
        return getIncludedHtmlContent(HEADER_HTML_CONTENT, locale);
    }

    public void setHeaderHtmlContent(String value, Locale locale) {
    	if (StringUtils.isBlank(value)) {
    		value = null;
    	}
    	setIncludedHtmlContent(HEADER_HTML_CONTENT, value, locale);
    }

    public String getFooterHtmlContent(Locale locale) {
        return getIncludedHtmlContent(FOOTER_HTML_CONTENT, locale);
    }

    public void setFooterHtmlContent(String value, Locale locale) {
    	if (StringUtils.isBlank(value)) {
    		value = null;
    	}
    	setIncludedHtmlContent(FOOTER_HTML_CONTENT, value, locale);
    }

    public String getEffectiveHeadTagHtmlContent(Locale locale) {
    	String headTagHtmlContent = getHeadTagHtmlContent(locale);
    	SearchInterfaceContext current = this;
    	while (headTagHtmlContent == null && current != null) {
    		headTagHtmlContent = current.getHeadTagHtmlContent(locale);
    		current = current.getParentContext();
    	}
    	return headTagHtmlContent;
    }

    public String getEffectiveHeaderHtmlContent(Locale locale) {
    	String headerHtmlContent = getHeaderHtmlContent(locale);
    	SearchInterfaceContext current = this;
    	while (headerHtmlContent == null && current != null) {
    		headerHtmlContent = current.getHeaderHtmlContent(locale);
    		current = current.getParentContext();
    	}
    	return headerHtmlContent;
    }

    public String getEffectiveFooterHtmlContent(Locale locale) {
    	String footerHtmlContent = getFooterHtmlContent(locale);
    	SearchInterfaceContext current = this;
    	while (footerHtmlContent == null && current != null) {
    		footerHtmlContent = current.getFooterHtmlContent(locale);
    		current = current.getParentContext();
    	}
    	return footerHtmlContent;
    }
    
    @Transient
    public Set<SearchInterfaceContextParam> getEffectiveContextParams() {
    	Set<SearchInterfaceContextParam> effectiveParams = new HashSet<SearchInterfaceContextParam>();
    	effectiveParams.addAll(getContextParams());
    	SearchInterfaceContext current = this;
    	while (current != null) {
    		Set<SearchInterfaceContextParam> parentParams = current.getContextParams();
    		for (SearchInterfaceContextParam parentParam : parentParams) {
				String parentParamName = parentParam.getParamName();
				if (this.getContextParam(parentParamName) == null) {
					effectiveParams.add(parentParam);
				}
			}
    		current = current.getParentContext();
    	}
    	return effectiveParams;
    }
	
	public SearchInterfaceContextParam getContextParam(String contextParamName) {
		SearchInterfaceContextParam match = null;
		for (SearchInterfaceContextParam contextParam : contextParams) {
			if (contextParam.getParamName().equals(contextParamName)) {
				match = contextParam;
				break;
			}
		}
		return match;
	}
	
    public SearchInterfaceContextParam getEffectiveContextParam(String contextParamName) {
		SearchInterfaceContextParam match = null;
    	SearchInterfaceContext current = this;
    	while (current != null && match == null) {
    		match = current.getContextParam(contextParamName);
    		current = current.getParentContext();
    	}
    	return match;
    }
	
	@Transient
	public boolean isInheritedContextParam(String contextParamName) {
		boolean inheritedContextParam;
		SearchInterfaceContextParam match = getContextParam(contextParamName);
		if (match == null && getEffectiveContextParam(contextParamName) != null) {
			inheritedContextParam = true;
		} else {
			inheritedContextParam = false;
		}
		return inheritedContextParam;
	}

	@Override
	public String toString() {
		return "SearchInterfaceContext [contextName=" + contextName + ", curlValue=" + curlValue
				+ ", externalFiles=" + externalFiles + ", parentContext=" + parentContext + "]";
	}

}
