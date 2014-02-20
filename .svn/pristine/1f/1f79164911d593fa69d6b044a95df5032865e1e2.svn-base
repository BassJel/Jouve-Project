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
package com.doculibre.constellio.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

@SuppressWarnings("serial")
@Entity
public class SearchInterfaceConfig extends BaseConstellioEntity {
    
    public static final String SKIN_RED = "red";
    public static final String SKIN_BLUE = "blue";
    
    public static final String[] SKINS = {
        SKIN_RED,
        SKIN_BLUE
    };

	public static final long LOGO_MAXIMUM_SIZE = 1048576;
	
	public static final String GA_UA_PLACEHOLDER = "UA-XXXXX-X";
	
	public static final String GA_HEADER_DEFAULT = "" +
		"<script type=\"text/javascript\">" +
		"\n" +  
		" var _gaq = _gaq || [];" +
		"\n" +  
		" _gaq.push(['_setAccount', '" + GA_UA_PLACEHOLDER + "']);" +
		"\n" +  
		" _gaq.push(['_trackPageview']);" +
		"\n" +  
		"  (function() {" +
		"\n" +  
		"   var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;" +
		"\n" +  
		"   ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';" +
		"\n" +  
		"   var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);" +
		"\n" +  
		"  })();" + 
		"\n" +  
		"</script>";
	
	private boolean languageInSearchForm;
	
	private boolean currentSearchFacet = true;
    
    private boolean descriptionAsExcerpt = false;
    
    private boolean showCollectionsInResultFacets = true;
    
    private boolean resultsInNewWindow = true;
    
    private boolean alwaysDisplayTags = true;
    
    private boolean keepFacetsNewSearch = false;
    
    private boolean useIconsInSearchResults = true;

    private boolean hideEmptyFacetColumn = false;
    
    private boolean simpleSearchAutocompletion = true;
    
    private Integer autocompleteMinQueries = new Integer(40);

	private boolean translateLanguageNames = true;
    
    private boolean advancedSearchEnabled = true;

    private byte[] logoSmallContent;

    private byte[] logoLargeContent;
     
    private String skin = SKIN_RED; // Default
    
    private int truncateDisplayUrlAfter;
    
    private boolean useGoogleAnalytics = false;
    
    private String googleAnalyticsUA = null;
    
    private String googleAnalyticsHeader = GA_HEADER_DEFAULT;

	public boolean isLanguageInSearchForm() {
		return languageInSearchForm;
	}

	public void setLanguageInSearchForm(boolean languageInSearchForm) {
		this.languageInSearchForm = languageInSearchForm;
	}

	public boolean isCurrentSearchFacet() {
		return currentSearchFacet;
	}

	public void setCurrentSearchFacet(boolean currentSearchFacet) {
		this.currentSearchFacet = currentSearchFacet;
	}
    
    public boolean isDescriptionAsExcerpt() {
        return descriptionAsExcerpt;
    }

    public void setDescriptionAsExcerpt(boolean descriptionAsExcerpt) {
        this.descriptionAsExcerpt = descriptionAsExcerpt;
    }

	public boolean isShowCollectionsInResultFacets() {
		return showCollectionsInResultFacets;
	}

	public void setShowCollectionsInResultFacets(
			boolean showCollectionsInResultFacets) {
		this.showCollectionsInResultFacets = showCollectionsInResultFacets;
	}

    public boolean isResultsInNewWindow() {
        return resultsInNewWindow;
    }

    public void setResultsInNewWindow(boolean resultsInNewWindow) {
        this.resultsInNewWindow = resultsInNewWindow;
    }

    public boolean isAlwaysDisplayTags() {
        return alwaysDisplayTags;
    }

    public void setAlwaysDisplayTags(boolean alwaysDisplayTags) {
        this.alwaysDisplayTags = alwaysDisplayTags;
    }

    public boolean isKeepFacetsNewSearch() {
        return keepFacetsNewSearch;
    }

    public void setKeepFacetsNewSearch(boolean refinedSearch) {
        this.keepFacetsNewSearch = refinedSearch;
    }

    public boolean isUseIconsInSearchResults() {
        return useIconsInSearchResults;
    }

    public void setUseIconsInSearchResults(boolean useIconsInSearchResults) {
        this.useIconsInSearchResults = useIconsInSearchResults;
    }

    @Transient
	public boolean isHideEmptyFacetColumn() {
		return hideEmptyFacetColumn;
	}

	public void setHideEmptyFacetColumn(boolean hideEmptyFacetColumn) {
		this.hideEmptyFacetColumn = hideEmptyFacetColumn;
	}

	public boolean isSimpleSearchAutocompletion() {
		return simpleSearchAutocompletion;
	}

	public void setSimpleSearchAutocompletion(boolean simpleSearchAutocompletion) {
		this.simpleSearchAutocompletion = simpleSearchAutocompletion;
	}
    
    public Integer getAutocompleteMinQueries() {
		return autocompleteMinQueries;
	}

	public void setAutocompleteMinQueries(Integer autoCompleteMinQueries) {
		this.autocompleteMinQueries = autoCompleteMinQueries;
	}

    @Lob
    @Column (length = 1024 * 1024)
    public byte[] getLogoSmallContent() {
        return logoSmallContent;
    }
    
    public void setLogoSmallContent(byte[] logoSmallContent) {
        this.logoSmallContent = logoSmallContent;
    }

    @Lob
    @Column (length = 1024 * 1024)
    public byte[] getLogoLargeContent() {
        return logoLargeContent;
    }
    
    public void setLogoLargeContent(byte[] logoLargeContent) {
        this.logoLargeContent = logoLargeContent;
    }

    public boolean isTranslateLanguageNames() {
        return translateLanguageNames;
    }

    public void setTranslateLanguageNames(boolean translateLanguageNames) {
        this.translateLanguageNames = translateLanguageNames;
    }

    public boolean isAdvancedSearchEnabled() {
        return advancedSearchEnabled;
    }

    public void setAdvancedSearchEnabled(boolean advancedSearchEnabled) {
        this.advancedSearchEnabled = advancedSearchEnabled;
    }
    
    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public int getTruncateDisplayUrlAfter() {
		return truncateDisplayUrlAfter;
	}

	public void setTruncateDisplayUrlAfter(int truncateDisplayUrlAfter) {
		this.truncateDisplayUrlAfter = truncateDisplayUrlAfter;
	}

	public boolean isUseGoogleAnalytics() {
		return useGoogleAnalytics;
	}

	public void setUseGoogleAnalytics(boolean useGoogleAnalytics) {
		this.useGoogleAnalytics = useGoogleAnalytics;
	}

	public String getGoogleAnalyticsUA() {
		return googleAnalyticsUA;
	}

	public void setGoogleAnalyticsUA(String googleAnalyticsUA) {
		this.googleAnalyticsUA = googleAnalyticsUA;
	}

	@Column(length = 10 * 1024)
	public String getGoogleAnalyticsHeader() {
		return googleAnalyticsHeader;
	}

	public void setGoogleAnalyticsHeader(String googleAnalyticsHeader) {
		this.googleAnalyticsHeader = googleAnalyticsHeader;
	}
	
}
