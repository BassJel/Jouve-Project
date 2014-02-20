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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;

import com.doculibre.constellio.entities.acl.PolicyACL;
import com.doculibre.constellio.entities.relevance.RecordCollectionBoost;
import com.doculibre.constellio.entities.relevance.ResultsRelevance;
import com.doculibre.constellio.entities.skos.Thesaurus;
import com.doculibre.constellio.utils.ConstellioNameUtils;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
public class RecordCollection extends ConstellioLabelledEntity {

    public static final String LABEL_TITLE = "title";
    public static final String LABEL_DESCRIPTION = "description";

    public static final String QUERY_PARSER_OPERATOR_AND = "AND";
    public static final String QUERY_PARSER_OPERATOR_OR = "OR";

    private String name;
    
	private String remoteURL;

    private String openSearchURL;

    private Date lastIndexingDate;

    private String spellCheckerLanguage = Locale.FRENCH.getLanguage();

    private boolean spellCheckerActive = true;

    private boolean clusteringEnabled = true;

    private String queryParserOperator = QUERY_PARSER_OPERATOR_AND;

    private boolean synchronizationRequired = false;

    private byte[] iconFileContent;

    private Thesaurus thesaurus;

    private Set<ConnectorInstance> connectorInstances = new HashSet<ConnectorInstance>();

    private Set<CollectionFederation> ownerCollectionFederations = new HashSet<CollectionFederation>();

    private Set<CollectionFederation> includedCollectionFederations = new HashSet<CollectionFederation>();

    private Set<CredentialGroup> credentialGroups = new HashSet<CredentialGroup>();

    // document boost rules :
    private Set<RecordCollectionBoost> recordCollectionBoosts = new HashSet<RecordCollectionBoost>();

    // query results management (max number of results, ...)
    private ResultsRelevance resultsRelevance = null;

    private Set<Categorization> categorizations = new HashSet<Categorization>();

    private Set<IndexField> indexFields = new HashSet<IndexField>();

    private Set<FeaturedLink> featuredLinks = new HashSet<FeaturedLink>();

    private Set<SynonymList> synonymLists = new HashSet<SynonymList>();

    private Set<CollectionPermission> collectionPermissions = new HashSet<CollectionPermission>();

    private Set<PolicyACL> policyACLs = new HashSet<PolicyACL>();

    private List<CollectionFacet> collectionFacets = new ArrayList<CollectionFacet>();

    private Set<I18NLabel> labels = new HashSet<I18NLabel>();

    private Set<Locale> locales = new HashSet<Locale>();

    private Integer position;

    private SolrConfig solrConfiguration;

    private CollectionStatsFilter statsFilter;

	private List<AdvancedSearchEnabledRule> advancedSearchEnabledRules;
	
    private boolean advancedSearchEnabled = true;
    
    private int advancedSearchInitialRulesNumber = 2;
	
    private Set<FederationRecordIndexingRequired> federationIndexingRequired = new HashSet<FederationRecordIndexingRequired>();

    private Set<FederationRecordDeletionRequired> federationDeletionRequired = new HashSet<FederationRecordDeletionRequired>();

    private List<SearchResultField> searchResultFields = new ArrayList<SearchResultField>();
    
    private Boolean publicCollection = Boolean.TRUE;
    
    private int numShards = 1;
    
    //private int replicationFactor  = 1;

	/**
     * Provides a name for this connector manager. This name cannot be blank
     * (null/empty/blank) and must be unique for the connection manager.
     * This names relates to the one for the connector manager.
     * 
     * @return
     */
    @Column(nullable = false, updatable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Name provided cannot blank (null/empty/blank)");
        }
        if (!ConstellioNameUtils.isValidName(name)) {
            throw new IllegalArgumentException("Name does not validate against regular expression : \""
                + ConstellioNameUtils.NAME_PATTERN + "\" :" + name);
        }
        this.name = name;
    }

    @Transient
    public boolean isRemote() {
        return remoteURL != null;
    }

    public String getRemoteURL() {
        return remoteURL;
    }

    public void setRemoteURL(String remoteURL) {
        this.remoteURL = remoteURL;
    }

    @Transient
    public boolean isOpenSearch() {
        return openSearchURL != null;
    }

    public String getOpenSearchURL() {
        return openSearchURL;
    }

    public void setOpenSearchURL(String openSearchURL) {
        this.openSearchURL = openSearchURL;
    }

    public String getQueryParserOperator() {
        return queryParserOperator;
    }

    public void setQueryParserOperator(String queryParserOperator) {
        if (StringUtils.isBlank(queryParserOperator)) {
            queryParserOperator = null;
        } else if (!queryParserOperator.equals(QUERY_PARSER_OPERATOR_AND)
            && !queryParserOperator.equals(QUERY_PARSER_OPERATOR_OR)) {
            throw new IllegalArgumentException("queryParserOperator is invalid : " + queryParserOperator);
        }
        this.queryParserOperator = queryParserOperator;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastIndexingDate() {
        return lastIndexingDate;
    }

    public void setLastIndexingDate(Date lastIndexingDate) {
        this.lastIndexingDate = lastIndexingDate;
    }

    public String getSpellCheckerLanguage() {
        return spellCheckerLanguage;
    }

    public void setSpellCheckerLanguage(String spellCheckerLanguage) {
        this.spellCheckerLanguage = spellCheckerLanguage;
    }

    public boolean isSpellCheckerActive() {
        return spellCheckerActive;
    }

    public void setSpellCheckerActive(boolean spellCheckerActive) {
        this.spellCheckerActive = spellCheckerActive;
    }

    public boolean isClusteringEnabled() {
        return clusteringEnabled;
    }

    public void setClusteringEnabled(boolean clusteringEnabled) {
        this.clusteringEnabled = clusteringEnabled;
    }

    public boolean isSynchronizationRequired() {
        return synchronizationRequired;
    }

    public void setSynchronizationRequired(boolean synchronizationRequired) {
        this.synchronizationRequired = synchronizationRequired;
    }

    @Lob
    @Column(length = 1024 * 1024)
    public byte[] getIconFileContent() {
        return iconFileContent;
    }

    public void setIconFileContent(byte[] iconFileContent) {
        this.iconFileContent = iconFileContent;
    }

    // FIXME Validate Cascade...
    @OneToMany(mappedBy = "recordCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<ConnectorInstance> getConnectorInstances() {
        return connectorInstances;
    }

    public void setConnectorInstances(Set<ConnectorInstance> connectorInstances) {
        this.connectorInstances = connectorInstances;
    }

    // FIXME Validate Cascade...
    @OneToMany(mappedBy = "recordCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<RecordCollectionBoost> getRecordCollectionBoost() {
        return recordCollectionBoosts;
    }

    public void setRecordCollectionBoost(Set<RecordCollectionBoost> recordCollectionDocumentBoosts) {
        this.recordCollectionBoosts = recordCollectionDocumentBoosts;
    }

    public void addRecordCollectionBoost(RecordCollectionBoost recordCollectionDocumentBoosts) {
        this.recordCollectionBoosts.add(recordCollectionDocumentBoosts);
        recordCollectionDocumentBoosts.setRecordCollection(this);
    }

    public void addConnectorInstance(ConnectorInstance connectorInstance) {
        this.connectorInstances.add(connectorInstance);
        connectorInstance.setRecordCollection(this);
    }

    public Object getConnectorInstance(String connectorInstanceName) {
        ConnectorInstance result = null;
        for (ConnectorInstance connectorInstance : connectorInstances) {
            if (connectorInstance.getName().equals(connectorInstanceName)) {
                result = connectorInstance;
                break;
            }
        }
        return result;
    }

    @OneToMany(mappedBy = "includedCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<CollectionFederation> getOwnerCollectionFederations() {
        return ownerCollectionFederations;
    }

    public void setOwnerCollectionFederations(Set<CollectionFederation> ownerCollections) {
        this.ownerCollectionFederations = ownerCollections;
    }

    public void addOwnerCollectionFederation(CollectionFederation federation) {
        this.ownerCollectionFederations.add(federation);
        federation.setIncludedCollection(this);
    }

    @Transient
    public Set<RecordCollection> getOwnerCollections() {
        Set<RecordCollection> ownerCollections = new HashSet<RecordCollection>();
        for (CollectionFederation federation : ownerCollectionFederations) {
            ownerCollections.add(federation.getOwnerCollection());
        }
        return ownerCollections;
    }

    @OneToMany(mappedBy = "ownerCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<CollectionFederation> getIncludedCollectionFederations() {
        return includedCollectionFederations;
    }

    public void setIncludedCollectionFederations(Set<CollectionFederation> includedCollections) {
        this.includedCollectionFederations = includedCollections;
    }

    public void addIncludedCollectionFederation(CollectionFederation federation) {
        this.includedCollectionFederations.add(federation);
        federation.setOwnerCollection(this);
    }

    @Transient
    public Set<RecordCollection> getIncludedCollections() {
        Set<RecordCollection> includedCollections = new HashSet<RecordCollection>();
        for (CollectionFederation federation : includedCollectionFederations) {
            includedCollections.add(federation.getIncludedCollection());
        }
        return includedCollections;
    }

    @Transient
    public boolean isFederationOwner() {
        return !includedCollectionFederations.isEmpty();
    }

    @Transient
    public boolean isIncludedInFederation() {
        return !ownerCollectionFederations.isEmpty();
    }

    @Transient
    public int getDepthInFederation() {
        int depthInFederation = 0;
        for (RecordCollection ownerCollection : getOwnerCollections()) {
            if (ownerCollection.isIncludedInFederation()) {
                // Recursive call
                int parentDepth = ownerCollection.getDepthInFederation();
                if (parentDepth + 1 > depthInFederation) {
                    depthInFederation = parentDepth + 1;
                }
            } else if (depthInFederation == 0) {
                depthInFederation = 1;
            }
        }
        return depthInFederation;
    }

    @OneToMany(mappedBy = "recordCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<CredentialGroup> getCredentialGroups() {
        return credentialGroups;
    }

    public void setCredentialGroups(Set<CredentialGroup> credentialGroups) {
        this.credentialGroups = credentialGroups;
    }

    public void addCredentialGroup(CredentialGroup credentialGroup) {
        this.credentialGroups.add(credentialGroup);
        credentialGroup.setRecordCollection(this);
    }

    // FIXME Validate Cascade...
    @OneToMany(mappedBy = "recordCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<Categorization> getCategorizations() {
        return categorizations;
    }

    public void setCategorizations(Set<Categorization> subindexFields) {
        this.categorizations = subindexFields;
    }

    public void addCategorization(Categorization categorization) {
        this.categorizations.add(categorization);
        categorization.setRecordCollection(this);
    }

    public IndexField getIndexField(String name) {
        IndexField match = null;
        for (IndexField indexField : indexFields) {
            if (indexField.getName().equals(name) || indexField.getName().equals(name + "*")) {
                match = indexField;
                break;
            }
        }
        return match;
    }

    // FIXME Validate Cascade...
    @OneToMany(mappedBy = "recordCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<IndexField> getIndexFields() {
        return indexFields;
    }

    public void setIndexFields(Set<IndexField> indexFields) {
        this.indexFields = indexFields;
    }

    public void addIndexField(IndexField indexField) {
        this.indexFields.add(indexField);
        indexField.setRecordCollection(this);
    }

    public String getTitle(Locale locale) {
        return getLabel(LABEL_TITLE, locale);
    }

    public void setTitle(String value, Locale locale) {
        setLabel(LABEL_TITLE, value, locale);
    }

    public String getDescription(Locale locale) {
        return getLabel(LABEL_DESCRIPTION, locale);
    }

    public void setDescription(String value, Locale locale) {
        setLabel(LABEL_DESCRIPTION, value, locale);
    }

    @OrderBy(value = "id")
    @OneToMany(mappedBy = "recordCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<FeaturedLink> getFeaturedLinks() {
        return featuredLinks;
    }

    public void setFeaturedLinks(Set<FeaturedLink> featuredLinks) {
        this.featuredLinks = featuredLinks;
    }

    public void addFeaturedLink(FeaturedLink featuredLink) {
        this.featuredLinks.add(featuredLink);
        featuredLink.setRecordCollection(this);
    }

    @OneToMany(mappedBy = "recordCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<SynonymList> getSynonymLists() {
        return synonymLists;
    }

    public void setSynonymLists(Set<SynonymList> synonymLists) {
        this.synonymLists = synonymLists;
    }

    public void addSynonymList(SynonymList synonymList) {
        this.synonymLists.add(synonymList);
        synonymList.setRecordCollection(this);
    }

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderColumn(name = "facetIndex")
    @JoinColumn(name = "recordCollection_id", nullable = false)
    public List<CollectionFacet> getCollectionFacets() {
        return collectionFacets;
    }

    public void setCollectionFacets(List<CollectionFacet> collectionFacets) {
        this.collectionFacets = collectionFacets;
    }

    public void addCollectionFacet(CollectionFacet collectionFacet) {
        this.collectionFacets.add(collectionFacet);
        collectionFacet.setRecordCollection(this);
    }

    public CollectionFacet getCollectionFacet(Long id) {
        CollectionFacet match = null;
        for (CollectionFacet collectionFacet : collectionFacets) {
            match = id.equals(collectionFacet.getId()) ? collectionFacet : null;
            if (match != null) {
                break;
            }
        }
        return match;
    }

    public CollectionFacet getFieldFacet(String indexFieldName) {
        CollectionFacet match = null;
        for (CollectionFacet collectionFacet : collectionFacets) {
            if (collectionFacet.isFieldFacet()) {
                IndexField facetField = collectionFacet.getFacetField();
                if (facetField.getName().equals(indexFieldName)) {
                    match = collectionFacet;
                    break;
                }
            }
        }
        return match;
    }

    @OneToMany(mappedBy = "recordCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<CollectionPermission> getCollectionPermissions() {
        return collectionPermissions;
    }

    public void setCollectionPermissions(Set<CollectionPermission> collectionPermissions) {
        this.collectionPermissions = collectionPermissions;
    }

    public void addCollectionPermission(CollectionPermission collectionPermission) {
        this.collectionPermissions.add(collectionPermission);
        collectionPermission.setRecordCollection(this);
    }

    @OneToMany(mappedBy = "recordCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<PolicyACL> getPolicyACLs() {
        return policyACLs;
    }

    public void setPolicyACLs(Set<PolicyACL> policyACLs) {
        this.policyACLs = policyACLs;
    }

    public void addPolicyACL(PolicyACL policyACL) {
        this.policyACLs.add(policyACL);
        policyACL.setRecordCollection(this);
    }

    public boolean hasFieldFacet(String fieldName) {
        boolean facetExists = false;
        for (CollectionFacet facet : getCollectionFacets()) {
            if (facet.isFieldFacet()) {
                IndexField indexField = facet.getFacetField();
                if (indexField.getName().equals(fieldName)) {
                    facetExists = true;
                    break;
                }
            }
        }
        return facetExists;
    }

    @OneToOne(mappedBy = "recordCollection", fetch = FetchType.LAZY)
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @ElementCollection
    @CollectionTable(name = "RecordCollection_Locales", joinColumns = @JoinColumn(name = "recordCollection_id"))
    @MapKeyColumn(name = "locale", length = 128)
    @MapKeyClass(Locale.class)
    public Set<Locale> getLocales() {
        return locales;
    }

    public void setLocales(Set<Locale> locales) {
        this.locales = locales;
    }

    public void addLocale(Locale locale) {
        this.locales.add(locale);
    }

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(name = "RecordCollection_Labels", joinColumns = { @JoinColumn(name = "recordCollection_id") }, inverseJoinColumns = { @JoinColumn(name = "label_id") })
    public Set<I18NLabel> getLabels() {
        return this.labels;
    }

    @Override
    protected void setLabels(Set<I18NLabel> labels) {
        this.labels = labels;
    }

    public boolean hasManyConnectorTypes() {
        ConnectorType firstConnectorType = null;
        for (ConnectorInstance connectorInstance : connectorInstances) {
            if (firstConnectorType == null) {
                firstConnectorType = connectorInstance.getConnectorType();
            } else if (!firstConnectorType.equals(connectorInstance.getConnectorType())) {
                return true;
            }
        }
        return false;
    }

    public Locale getDisplayLocale(Locale locale) {
        Locale displayLocale;
        if (locales.contains(locale)) {
            displayLocale = locale;
        } else if (!locales.isEmpty()) {
            displayLocale = locales.iterator().next();
        } else {
            displayLocale = locale;
        }
        return displayLocale;
    }

    public boolean hasSearchPermission() {
        boolean hasPermission = false;
        for (CollectionPermission collectionPermission : getCollectionPermissions()) {
            if (collectionPermission.isSearch()) {
                hasPermission = true;
                break;
            }
        }
        return hasPermission;
    }

    public boolean hasAdminPermission() {
        boolean hasPermission = false;
        for (CollectionPermission collectionPermission : getCollectionPermissions()) {
            if (collectionPermission.isAdmin()) {
                hasPermission = true;
                break;
            }
        }
        return hasPermission;
    }

    @Transient
    public IndexField getUniqueKeyIndexField() {
        return getIndexField(IndexField.UNIQUE_KEY_FIELD);
    }

    @Transient
    public IndexField getDefaultSearchIndexField() {
        return getIndexField(IndexField.DEFAULT_SEARCH_FIELD);
    }

    @Transient
    public IndexField getTitleIndexField() {
        return getIndexField(IndexField.TITLE_FIELD);
    }

    @Transient
    public IndexField getUrlIndexField() {
        return getIndexField(IndexField.URL_FIELD);
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getPosition() {
        return position;
    }

    @OneToOne(cascade = { CascadeType.ALL })
    public SolrConfig getSolrConfiguration() {
        return solrConfiguration;
    }

    public void setSolrConfiguration(SolrConfig solrConfiguration) {
        this.solrConfiguration = solrConfiguration;
    }

    @OneToOne(cascade = { CascadeType.ALL })
	public CollectionStatsFilter getStatsFilter() {
		return statsFilter;
	}

	public void setStatsFilter(CollectionStatsFilter statsFilter) {
		this.statsFilter = statsFilter;
	}

    @Transient
    public List<IndexField> getIndexedIndexFields() {
        List<IndexField> acceptedFields = new ArrayList<IndexField>();
        for (IndexField indexField : this.getIndexFields()) {
            if (indexField.isIndexed()) {
                acceptedFields.add(indexField);
            }
        }
        return acceptedFields;
    }

    public void setResultsRelevance(ResultsRelevance resultsRelevance) {
        this.resultsRelevance = resultsRelevance;
    }

    @OneToOne(cascade = { CascadeType.ALL })
    public ResultsRelevance getResultsRelevance() {
        return resultsRelevance;
    }

	@OneToMany(mappedBy="recordCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
	public List<AdvancedSearchEnabledRule> getAdvancedSearchEnabledRules() {
		return advancedSearchEnabledRules;
	}

	public void setAdvancedSearchEnabledRules(
			List<AdvancedSearchEnabledRule> advancedSearchEnabledRules) {
		this.advancedSearchEnabledRules = advancedSearchEnabledRules;
	}

	public boolean isAdvancedSearchEnabled() {
		return advancedSearchEnabled;
	}

	public void setAdvancedSearchEnabled(boolean advancedSearchEnabled) {
		this.advancedSearchEnabled = advancedSearchEnabled;
	}

	public int getAdvancedSearchInitialRulesNumber() {
		return advancedSearchInitialRulesNumber;
	}

	public void setAdvancedSearchInitialRulesNumber(
			int advancedSearchInitialRulesNumber) {
		this.advancedSearchInitialRulesNumber = advancedSearchInitialRulesNumber;
	}

    @OneToMany(mappedBy = "ownerCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<FederationRecordIndexingRequired> getFederationIndexingRequired() {
        return federationIndexingRequired;
    }

    public void setFederationIndexingRequired(Set<FederationRecordIndexingRequired> federationIndexingRequired) {
        this.federationIndexingRequired = federationIndexingRequired;
    }

    @OneToMany(mappedBy = "ownerCollection", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<FederationRecordDeletionRequired> getFederationDeletionRequired() {
        return federationDeletionRequired;
    }

    public void setFederationDeletionRequired(Set<FederationRecordDeletionRequired> federationDeletionRequired) {
        this.federationDeletionRequired = federationDeletionRequired;
    }

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderColumn(name = "searchResultFieldIndex")
    @JoinColumn(name = "recordCollection_id", nullable = false)
    public List<SearchResultField> getSearchResultFields() {
        return searchResultFields;
    }

    public void setSearchResultFields(List<SearchResultField> searchResultFields) {
        this.searchResultFields = searchResultFields;
    }

    public void addSearchResultField(SearchResultField searchResultField) {
        this.searchResultFields.add(searchResultField);
        searchResultField.setRecordCollection(this);
    }
    
    @Transient
    public boolean isPublicCollection() {
    	return !Boolean.FALSE.equals(publicCollection);
    }

    public Boolean getPublicCollection() {
		return publicCollection;
	}

	public void setPublicCollection(Boolean publicCollection) {
		this.publicCollection = publicCollection;
	}
	
	public int getNumShards() {
		return numShards;
	}

	public void setNumShards(int numShards) {
		this.numShards = numShards;
	}
	
//	public int getReplicationFactor() {
//		return replicationFactor;
//	}
//
//	public void setReplicationFactor(int replicationFactor) {
//		this.replicationFactor = replicationFactor;
//	}
//

}
