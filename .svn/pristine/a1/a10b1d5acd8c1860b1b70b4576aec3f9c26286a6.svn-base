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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.doculibre.constellio.entities.acl.RecordPolicyACLEntry;
import com.doculibre.constellio.entities.skos.SkosConcept;
import com.doculibre.constellio.entities.skos.Thesaurus;
import com.google.enterprise.connector.spi.SpiConstants;

/**
 * Represents a record that will be indexed, and therefore will represent a search result.
 * Important, this is exactly not a record as submitted by the feed protocol. There are two
 * differences : web feeds a send to the web connector and only parsed/text content is stored (no binary
 * data).
 * A record's url must be unique for a given connector instance.
 */
@SuppressWarnings("serial")
public class Record extends BaseConstellioEntity {

    private String url;

    private String authmethod;

    private Date lastModified;

    private Date lastFetched;

    private Date lastIndexed;

    private Date lastAutomaticTagging;

    private String mimetype;

    private String lang;

    private boolean updateIndex = true;

    private boolean deleted = false;

    private boolean excluded = false;

    private boolean excludedEffective = false;

    private boolean publicRecord = true;

    private boolean computeACLEntries = true;

    private Double boost = 1.0;
    
    private List<String> md5;

    private ParsedContent parsedContent;

    private ConnectorInstance connectorInstance;

    private List<RecordMeta> contentMetas = new ArrayList<RecordMeta>();

    private List<RecordMeta> externalMetas = new ArrayList<RecordMeta>();

    private Set<RecordTag> recordTags = new HashSet<RecordTag>();

    private Set<RecordPolicyACLEntry> recordPolicyACLEntries = new HashSet<RecordPolicyACLEntry>();

    @Column(nullable = false, updatable = false, length = 5000)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Convenience method
     * 
     * @return
     */
    @Transient
    public RecordMeta getUrlMeta() {
        List<RecordMeta> results = getMetas(SpiConstants.PROPNAME_DISPLAYURL);
        if (!results.isEmpty()) {
            return results.get(0);
        } else {
            results = getMetas(SpiConstants.PROPNAME_CONTENTURL);
            if (!results.isEmpty()) {
                return results.get(0);
            }
        }
        return null;
    }

    /**
     * Convenience method
     * 
     * @return
     */
    @Transient
    public String getDisplayUrl() {
        RecordMeta urlMeta = getUrlMeta();
        return urlMeta != null ? urlMeta.getContent() : getUrl();
    }

    /**
     * Convenience method
     * 
     * @return
     */
    @Transient
    public String getDisplayTitle() {
        List<RecordMeta> titleMetas = getMetas(SpiConstants.PROPNAME_TITLE);
        if (titleMetas.isEmpty()) {
            // Just in case...
            titleMetas = getMetas("title");
        }
        if (!titleMetas.isEmpty()) {
        	RecordMeta meta = titleMetas.get(0);
        	if (meta != null) {
        		return StringUtils.trim(meta.getContent());
        	}
        }
        return null;
    }

    public String getAuthmethod() {
        return authmethod;
    }

    public void setAuthmethod(String authmethod) {
        this.authmethod = authmethod;
    }

    @Column(nullable = false)
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(Date lastFetched) {
        this.lastFetched = lastFetched;
    }

    public Date getLastIndexed() {
        return lastIndexed;
    }

    public void setLastIndexed(Date lastIndexed) {
        this.lastIndexed = lastIndexed;
    }

    public Date getLastAutomaticTagging() {
        return lastAutomaticTagging;
    }

    public void setLastAutomaticTagging(Date lastAutomaticTagging) {
        this.lastAutomaticTagging = lastAutomaticTagging;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isUpdateIndex() {
        return updateIndex;
    }

    public void setUpdateIndex(boolean updateIndex) {
        this.updateIndex = updateIndex;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public boolean isExcludedEffective() {
        return excludedEffective;
    }

    public void setExcludedEffective(boolean excludedEffective) {
        this.excludedEffective = excludedEffective;
    }

    public boolean isPublicRecord() {
        return publicRecord;
    }

    public void setPublicRecord(boolean publicRecord) {
        this.publicRecord = publicRecord;
    }

    public boolean isComputeACLEntries() {
        return computeACLEntries;
    }

    public void setComputeACLEntries(boolean computeACLEntries) {
        this.computeACLEntries = computeACLEntries;
    }

    public Double getBoost() {
        return boost;
    }

    public void setBoost(Double boost) {
        this.boost = boost;
    }

    public List<String> getMd5() {
		return md5;
	}

	public void setMd5(List<String> md5) {
		this.md5 = md5;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    public ParsedContent getParsedContent() {
        return parsedContent;
    }

    public void setParsedContent(ParsedContent parsedContent) {
        this.parsedContent = parsedContent;
    }

    public void setParsedContent(String content) {
        if (this.parsedContent == null) {
            this.parsedContent = new ParsedContent();
            this.parsedContent.setRecord(this);
        }
        this.parsedContent.setContent(content);
    }

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    public ConnectorInstance getConnectorInstance() {
        return connectorInstance;
    }

    public void setConnectorInstance(ConnectorInstance connectorInstance) {
        this.connectorInstance = connectorInstance;
    }

    // OnDelete is not allowed cuz the relation is not inverse.
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinTable(name = "Record_ContentMetas", joinColumns = { @JoinColumn(name = "record_id") }, inverseJoinColumns = { @JoinColumn(name = "meta_id") })
    @OrderColumn
    public List<RecordMeta> getContentMetas() {
        return contentMetas;
    }

    public void setContentMetas(List<RecordMeta> contentMetas) {
        this.contentMetas = contentMetas;
    }

    public void addContentMeta(RecordMeta contentMeta) {
        contentMeta.setRecord(this);
        this.contentMetas.add(contentMeta);
    }

    // OnDelete is not allowed cuz the relation is not inverse.
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinTable(name = "Record_ExternalMetas", joinColumns = { @JoinColumn(name = "record_id") }, inverseJoinColumns = { @JoinColumn(name = "meta_id") })
    @OrderColumn
    public List<RecordMeta> getExternalMetas() {
        return externalMetas;
    }

    public void setExternalMetas(List<RecordMeta> externalMetas) {
        this.externalMetas = externalMetas;
    }

    public void addExternalMeta(RecordMeta externalMeta) {
        externalMeta.setRecord(this);
        this.externalMetas.add(externalMeta);
    }

    public List<RecordMeta> getMetas(String metaName) {
        List<RecordMeta> metas = new ArrayList<RecordMeta>();
        for (RecordMeta contentMeta : contentMetas) {
        	if (contentMeta != null) {
            	ConnectorInstanceMeta connectorInstanceMeta = contentMeta.getConnectorInstanceMeta();
                if (connectorInstanceMeta != null && connectorInstanceMeta.getName().equalsIgnoreCase(metaName)) {
                    metas.add(contentMeta);
                }
        	}
        }
        for (RecordMeta externalMeta : externalMetas) {
        	if (externalMeta != null) {
            	ConnectorInstanceMeta connectorInstanceMeta = externalMeta.getConnectorInstanceMeta();
                if (connectorInstanceMeta != null && connectorInstanceMeta.getName().equalsIgnoreCase(metaName)) {
                    metas.add(externalMeta);
                }
        	}
        }
        return metas;
    }

    public List<String> getMetaContents(String metaName) {
        List<RecordMeta> metas = getMetas(metaName);
        List<String> contents = new ArrayList<String>();
        for (RecordMeta meta : metas) {
            contents.add(meta.getContent());
        }
        return contents;
    }
    
    public boolean hasMetaContent(String metaName, String metaContent) {
        List<RecordMeta> metas = getMetas(metaName);
        return metas.contains(metaContent);
    }
    
    public void clearMetaContent(String metaName) {
        for (Iterator<RecordMeta> contentMetaIt = contentMetas.iterator(); contentMetaIt.hasNext();) {
        	RecordMeta contentMeta = contentMetaIt.next();
            if (contentMeta.getConnectorInstanceMeta().getName().equalsIgnoreCase(metaName)) {
                contentMetaIt.remove();
            }
        }
        for (Iterator<RecordMeta> externalMetaIt = externalMetas.iterator(); externalMetaIt.hasNext();) {
        	RecordMeta externalMeta = externalMetaIt.next();
            if (externalMeta.getConnectorInstanceMeta().getName().equalsIgnoreCase(metaName)) {
            	externalMetaIt.remove();
            }
        }
    }

    public void addRecordTag(RecordTag recordTag) {
        this.recordTags.add(recordTag);
        recordTag.setRecord(this);
    }

    @OneToMany(mappedBy = "record", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
    public Set<RecordTag> getRecordTags() {
        return recordTags;
    }

    public void setRecordTags(Set<RecordTag> recordTags) {
        this.recordTags = recordTags;
    }

    @Transient
    public Set<RecordTag> getIncludedRecordTags() {
        Set<RecordTag> includedRecordTags = new HashSet<RecordTag>();
        for (RecordTag recordTag : recordTags) {
            if (!recordTag.isExcluded()) {
                includedRecordTags.add(recordTag);
            }
        }
        return Collections.unmodifiableSet(includedRecordTags);
    }

    public Set<RecordTag> getIncludedRecordTags(Thesaurus thesaurus, boolean ignoreExcluded) {
        Set<RecordTag> includedRecordTags = new HashSet<RecordTag>();
        for (RecordTag recordTag : recordTags) {
            if (ignoreExcluded || !recordTag.isExcluded()) {
                boolean ajouter = true;
                if (thesaurus == null) {
                    ajouter = recordTag.getFreeTextTag() != null;
                } else {
                    Thesaurus thesaurusTag = recordTag.getSkosConcept() == null ? null : recordTag
                        .getSkosConcept().getThesaurus();
                    ajouter = thesaurusTag != null
                        && thesaurus.getRdfAbout().equals(thesaurusTag.getRdfAbout());
                }

                if (ajouter) {
                    includedRecordTags.add(recordTag);
                }
            }
        }
        return Collections.unmodifiableSet(includedRecordTags);
    }

    public Set<RecordTag> getIncludedRecordTags(Thesaurus thesaurus) {
        return getIncludedRecordTags(thesaurus, false);
    }

    @Transient
    public Set<FreeTextTag> getFreeTextTags(boolean ignoreExcluded) {
        Set<FreeTextTag> freeTextTags = new HashSet<FreeTextTag>();
        for (RecordTag recordTag : recordTags) {
            if (recordTag.getFreeTextTag() != null) {
                if (ignoreExcluded || !recordTag.isExcluded()) {
                    freeTextTags.add(recordTag.getFreeTextTag());
                }
            }
        }
        return Collections.unmodifiableSet(freeTextTags);
    }

    /**
     * Convenience method
     * 
     * @param freeTextTag
     */
    public void addFreeTextTag(FreeTextTag freeTextTag, boolean manual) {
        if (!hasFreeTextTag(freeTextTag)) {
            RecordTag recordTag = new RecordTag();
            recordTag.setFreeTextTag(freeTextTag);
            recordTag.setRecord(this);
            recordTag.setManual(manual);
            recordTag.setExcluded(false);
            addRecordTag(recordTag);
        }
    }

    /**
     * Convenience method
     * 
     * @param freeTextTag
     * @return
     */
    public boolean hasFreeTextTag(FreeTextTag freeTextTag) {
        boolean exists = false;
        for (RecordTag recordTag : recordTags) {
            if (freeTextTag.equals(recordTag.getFreeTextTag())) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    public boolean hasFreeTextTag(String freeText) {
        boolean exists = false;
        for (RecordTag recordTag : recordTags) {
            FreeTextTag freeTextTag = recordTag.getFreeTextTag();
            if (freeTextTag != null && freeTextTag.getFreeText().equals(freeText)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    /**
     * Convenience method
     * 
     * @param freeTextTag
     */
    public void removeFreeTextTag(FreeTextTag freeTextTag) {
        for (Iterator<RecordTag> it = recordTags.iterator(); it.hasNext();) {
            RecordTag recordTag = it.next();
            if (freeTextTag.equals(recordTag.getFreeTextTag())) {
                // it.remove();
                recordTag.setExcluded(true);
                recordTag.setManual(true);
            }
        }
    }

    @Transient
    public Set<SkosConcept> getSkosConcepts(boolean ignoreExcluded) {
        Set<SkosConcept> skosConcepts = new HashSet<SkosConcept>();
        for (RecordTag recordTag : recordTags) {
            if (recordTag.getSkosConcept() != null) {
                if (ignoreExcluded || !recordTag.isExcluded()) {
                    skosConcepts.add(recordTag.getSkosConcept());
                }
            }
        }
        return Collections.unmodifiableSet(skosConcepts);
    }

    @Transient
    public boolean isManuallyTagged() {
        boolean result = false;
        for (RecordTag recordTag : recordTags) {
            if (recordTag.getSkosConcept() != null) {
                if (!recordTag.isExcluded()) {
                    if (recordTag.isManual()) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public SkosConcept getSkosConcept(String prefLabel, Locale locale, boolean ignoreExcluded) {
        SkosConcept result = null;
        for (SkosConcept skosConcept : getSkosConcepts(ignoreExcluded)) {
            String skosConceptPrefLabel = skosConcept.getPrefLabel(locale);
            if (prefLabel.equals(skosConceptPrefLabel)) {
                result = skosConcept;
                break;
            }
        }
        return result;
    }

    /**
     * Convenience method
     * 
     * @param skosConcept
     */
    public void addSkosConcept(SkosConcept skosConcept, boolean manual) {
        if (!hasSkosConcept(skosConcept)) {
            RecordTag recordTag = new RecordTag();
            recordTag.setSkosConcept(skosConcept);
            recordTag.setRecord(this);
            recordTag.setManual(manual);
            recordTag.setExcluded(false);
            addRecordTag(recordTag);
        }
    }

    /**
     * Convenience method
     * 
     * @param skosConcept
     * @return
     */
    public boolean hasSkosConcept(SkosConcept skosConcept) {
        boolean exists = false;
        for (RecordTag recordTag : recordTags) {
            if (skosConcept.equals(recordTag.getSkosConcept())) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    /**
     * Convenience method
     * 
     * @param skosConcept
     */
    public void removeSkosConceptTag(SkosConcept skosConcept) {
        for (Iterator<RecordTag> it = recordTags.iterator(); it.hasNext();) {
            RecordTag recordTag = it.next();
            if (skosConcept.equals(recordTag.getSkosConcept())) {
                recordTag.setExcluded(true);
                recordTag.setManual(true);
                // it.remove();
            }
        }
    }

    @OneToMany(mappedBy = "record", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<RecordPolicyACLEntry> getRecordPolicyACLEntries() {
        return recordPolicyACLEntries;
    }

    public void setRecordPolicyACLEntries(Set<RecordPolicyACLEntry> recordPolicyACLEntries) {
        this.recordPolicyACLEntries = recordPolicyACLEntries;
    }

    public void addRecordPolicyACLEntry(RecordPolicyACLEntry recordPolicyACLEntry) {
        this.recordPolicyACLEntries.add(recordPolicyACLEntry);
        recordPolicyACLEntry.setRecord(this);
    }

}
