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
package com.doculibre.constellio.wicket.pages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.FeaturedLink;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.services.FeaturedLinkServices;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.StatsServices;
import com.doculibre.constellio.services.SynonymServices;
import com.doculibre.constellio.spellchecker.SpellChecker;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.NumberFormatUtils;
import com.doculibre.constellio.utils.SimpleParams;
import com.doculibre.constellio.wicket.application.ConstellioApplication;
import com.doculibre.constellio.wicket.data.SearchResultsDataProvider;
import com.doculibre.constellio.wicket.panels.facets.FacetsPanel;
import com.doculibre.constellio.wicket.panels.header.BaseSearchResultsPageHeaderPanel;
import com.doculibre.constellio.wicket.panels.results.SearchResultsPanel;
import com.doculibre.constellio.wicket.panels.search.SearchFormPanel;
import com.doculibre.constellio.wicket.panels.spellchecker.SpellCheckerPanel;
import com.doculibre.constellio.wicket.panels.thesaurus.ThesaurusSuggestionPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;
import com.doculibre.constellio.wicket.utils.SimpleParamsUtils;
import com.ibm.icu.text.DecimalFormat;

@SuppressWarnings("serial")
public class SearchResultsPage extends BaseSearchPage {

    private SearchResultsPanel searchResultsPanel;

    private SearchResultsDataProvider dataProvider;

    public SearchResultsPage(PageParameters params) {
        super(parse(params), params);
        initComponents();
    }

    public static PageParameters getParameters(SimpleSearch simpleSearch) {
        SimpleParams simpleParams = simpleSearch.toSimpleParams(false);
        PageParameters pageParameters = SimpleParamsUtils.toPageParameters(simpleParams);
        return pageParameters;
    }

    public static SimpleSearch parse(PageParameters pageParams) {
    	return parse(pageParams, true);
    }

    public static SimpleSearch parse(PageParameters pageParams, boolean searchAllIfEmpty) {
        SimpleParams simpleParams = SimpleParamsUtils.toSimpleParams(pageParams);
        SimpleSearch simpleSearch = SimpleSearch.toSimpleSearch(simpleParams);
        if (StringUtils.isBlank(simpleSearch.getLuceneQuery(false))) {
        	boolean searchAll;
        	if (!searchAllIfEmpty) {
        		String queryParam = pageParams.getString("query");
        		searchAll = queryParam != null;
        	} else {
        		searchAll = true;
        	}
        	if (searchAll) {
                simpleSearch.setQuery(SimpleSearch.SEARCH_ALL);
        	}
        }
        return simpleSearch;
    }

    @Override
    protected void onBeforeRender() {
        SimpleSearch simpleSearch = getSimpleSearch();
        if (StringUtils.isEmpty(simpleSearch.getCollectionName())) {
            setResponsePage(ConstellioApplication.get().getHomePage());
        } else {
            super.onBeforeRender();
            ConstellioSession.get().addSearchHistory(simpleSearch);
            QueryResponse queryResponse = dataProvider.getQueryResponse();
            if (queryResponse != null) {
                StatsServices statsServices = ConstellioSpringUtils.getStatsServices();
                if (!statsServices.isIgnored(simpleSearch)) {
                    String ipAddress = getWebRequestCycle().getWebRequest().getHttpServletRequest().getRemoteAddr();
                    String searchLogDocId = statsServices.logSearch(simpleSearch, queryResponse, ipAddress);
                    simpleSearch.setSearchLogDocId(searchLogDocId);
                }
            }
        }
    }

    private void initComponents() {
        final SimpleSearch simpleSearch = getSimpleSearch();
        String collectionName = simpleSearch.getCollectionName();
        ConstellioUser currentUser = ConstellioSession.get().getUser();
        RecordCollectionServices recordCollectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = recordCollectionServices.get(collectionName) ;
		boolean userHasPermission = false;
		if (collection != null){
			userHasPermission = (! collection.hasSearchPermission()) || (currentUser != null && currentUser.hasSearchPermission(collection));
		}		
		if (StringUtils.isEmpty(collectionName) || ! userHasPermission) {
            setResponsePage(ConstellioApplication.get().getHomePage());
        } else {
        	
            final IModel suggestedSearchKeyListModel = new LoadableDetachableModel() {
                @Override
                protected Object load() {
                    ListOrderedMap suggestedSearch = new ListOrderedMap();
                    if (simpleSearch.isQueryValid() && simpleSearch.getQuery() != null) {
                        SpellChecker spellChecker = new SpellChecker(ConstellioApplication.get()
                            .getDictionaries());
                        try {
                        	if (!simpleSearch.getQuery().equals("*:*")) {
                        		suggestedSearch = spellChecker.suggest(simpleSearch.getQuery(), simpleSearch
                            		.getCollectionName());
                        	}
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                            // Échec du spellchecker, pas besoin de faire planter la page
                        }
                    }
                    return suggestedSearch;
                }
            };

            BaseSearchResultsPageHeaderPanel headerPanel = (BaseSearchResultsPageHeaderPanel) getHeaderComponent();
            headerPanel.setAdvancedForm(simpleSearch.getAdvancedSearchRule() != null);
            SearchFormPanel searchFormPanel = headerPanel.getSearchFormPanel();

            final ThesaurusSuggestionPanel thesaurusSuggestionPanel = new ThesaurusSuggestionPanel(
                "thesaurusSuggestion", simpleSearch, getLocale());
            add(thesaurusSuggestionPanel);

            SpellCheckerPanel spellCheckerPanel = new SpellCheckerPanel("spellChecker", searchFormPanel
                .getSearchTxtField(), searchFormPanel.getSearchButton(), suggestedSearchKeyListModel) {
                @SuppressWarnings("unchecked")
                public boolean isVisible() {
                    boolean visible = false;
                    if (dataProvider != null && !thesaurusSuggestionPanel.isVisible()) {
                        RecordCollectionServices collectionServices = ConstellioSpringUtils
                            .getRecordCollectionServices();
                        RecordCollection collection = collectionServices
                            .get(simpleSearch.getCollectionName());
                        if (collection != null && collection.isSpellCheckerActive() && simpleSearch.getAdvancedSearchRule() == null) {
                            ListOrderedMap spell = (ListOrderedMap) suggestedSearchKeyListModel.getObject();
                            if (spell.size() > 0/* && dataProvider.size() == 0 */) {
                                for (String key : (List<String>) spell.keyList()) {
                                    if (spell.get(key) != null) {
                                        return visible = true;
                                    }
                                }
                            }
                        }
                    }
                    return visible;
                }
            };
            add(spellCheckerPanel);

            dataProvider = new SearchResultsDataProvider(simpleSearch, 10);

            WebMarkupContainer searchResultsSection = new WebMarkupContainer("searchResultsSection") {
                @Override
                public boolean isVisible() {
                    return StringUtils.isNotBlank(simpleSearch.getLuceneQuery());
                }
            };
            add(searchResultsSection);

            IModel detailsLabelModel = new LoadableDetachableModel() {
                @Override
                protected Object load() {
                    String detailsLabel;
                    QueryResponse queryResponse = dataProvider.getQueryResponse();
                    long start;
                    long nbRes;
                    double elapsedTimeSeconds;
                    if (queryResponse != null) {
                        start = queryResponse.getResults().getStart();
                        nbRes = dataProvider.size();
                        elapsedTimeSeconds = (double) queryResponse.getElapsedTime() / 1000;
                    } else {
                        start = 0;
                        nbRes = 0;
                        elapsedTimeSeconds = 0;
                    }
                    long end = start + 10;
                    if (nbRes < end) {
                        end = nbRes;
                    }

                    String pattern = "#.####";
                    DecimalFormat elapsedTimeFormatter = new DecimalFormat(pattern);
                    String elapsedTimeStr = elapsedTimeFormatter.format(elapsedTimeSeconds);

                    String forTxt = new StringResourceModel("for", SearchResultsPage.this, null).getString();
                    String noResultTxt = new StringResourceModel("noResult", SearchResultsPage.this, null)
                        .getString();
                    String resultsTxt = new StringResourceModel("results", SearchResultsPage.this, null)
                        .getString();
                    String ofTxt = new StringResourceModel("of", SearchResultsPage.this, null).getString();
                    String secondsTxt = new StringResourceModel("seconds", SearchResultsPage.this, null)
                        .getString();

                    String queryTxt = " ";
                    if (simpleSearch.isQueryValid() && simpleSearch.getQuery() != null && simpleSearch.getAdvancedSearchRule() == null) {
                    	queryTxt = " " + forTxt + " " + simpleSearch.getQuery() + " ";
                    }

                    if (nbRes > 0) {
                        Locale locale = getLocale();
                        detailsLabel = resultsTxt + " " + NumberFormatUtils.format(start + 1, locale) + " - "
                            + NumberFormatUtils.format(end, locale) + " " + ofTxt + " "
                            + NumberFormatUtils.format(nbRes, locale) + queryTxt + "(" + elapsedTimeStr + " "
                            + secondsTxt + ")";
                    } else {
                        detailsLabel = noResultTxt + " " + queryTxt + "(" + elapsedTimeStr + " " + secondsTxt
                            + ")";
                    }

                    String collectionName = dataProvider.getSimpleSearch().getCollectionName();
                    if (collectionName != null) {
                        RecordCollectionServices collectionServices = ConstellioSpringUtils
                            .getRecordCollectionServices();
                        RecordCollection collection = collectionServices.get(collectionName);
                        Locale displayLocale = collection.getDisplayLocale(getLocale());
                        String collectionTitle = collection.getTitle(displayLocale);
                        detailsLabel = collectionTitle + " > " + detailsLabel;
                    }
                    return detailsLabel;
                }
            };
            Label detailsLabel = new Label("detailsRes", detailsLabelModel);
            add(detailsLabel);

            final IModel sortOptionsModel = new LoadableDetachableModel() {

                @Override
                protected Object load() {
                    List<SortChoice> choices = new ArrayList<SortChoice>();
                    choices.add(new SortChoice(null, null, null));
                    String collectionName = dataProvider.getSimpleSearch().getCollectionName();
                    if (collectionName != null) {
                        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
                        RecordCollectionServices collectionServices = ConstellioSpringUtils
                            .getRecordCollectionServices();
                        RecordCollection collection = collectionServices.get(collectionName);
                        for (IndexField indexField : indexFieldServices.getSortableIndexFields(collection)) {
                            String label = indexField.getLabel(IndexField.LABEL_TITLE, ConstellioSession.get().getLocale());
                            if (label == null || label.isEmpty()) {
                                label = indexField.getName();
                            }
                            choices.add(new SortChoice(indexField.getName(), label, "asc"));
                            choices.add(new SortChoice(indexField.getName(), label, "desc"));
                        }
                    }
                    return choices;
                }
            };

            IChoiceRenderer triChoiceRenderer = new ChoiceRenderer() {
                @Override
                public Object getDisplayValue(Object object) {
                    SortChoice choice = (SortChoice) object;
                    String displayValue;
                    if (choice.title == null) {
                        displayValue = new StringResourceModel("sort.relevance", SearchResultsPage.this, null)
                            .getString();
                    } else {
                        String order = new StringResourceModel("sortOrder." + choice.order,
                            SearchResultsPage.this, null).getString();
                        displayValue = choice.title + " " + order;
                    }
                    return displayValue;
                }
            };
            IModel value = new Model(new SortChoice(simpleSearch.getSortField(), simpleSearch.getSortField(),
                simpleSearch.getSortOrder()));
            DropDownChoice sortField = new DropDownChoice("sortField", value, sortOptionsModel,
                triChoiceRenderer) {
                @Override
                protected boolean wantOnSelectionChangedNotifications() {
                    return true;
                }

                @Override
                protected void onSelectionChanged(Object newSelection) {
                    SortChoice choice = (SortChoice) newSelection;
                    if (choice.name == null) {
                        simpleSearch.setSortField(null);
                        simpleSearch.setSortOrder(null);
                    } else {
                        simpleSearch.setSortField(choice.name);
                        simpleSearch.setSortOrder(choice.order);
                    }
                    simpleSearch.setPage(0);

                    PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                    RequestCycle.get().setResponsePage(pageFactoryPlugin.getSearchResultsPage(),
                        SearchResultsPage.getParameters(simpleSearch));
                }

                @Override
                public boolean isVisible() {
                    return ((List<?>) sortOptionsModel.getObject()).size() > 1;
                }
            };
            searchResultsSection.add(sortField);
            sortField.setNullValid(false);

            add(new AjaxLazyLoadPanel("facetsPanel") {
                @Override
                public Component getLazyLoadComponent(String markupId) {
                    return new FacetsPanel(markupId, dataProvider);
                }
            });

            final IModel featuredLinkModel = new LoadableDetachableModel() {
                @Override
                protected Object load() {
                    FeaturedLink suggestion;
                    RecordCollectionServices collectionServices = ConstellioSpringUtils
                        .getRecordCollectionServices();
                    FeaturedLinkServices featuredLinkServices = ConstellioSpringUtils
                        .getFeaturedLinkServices();
                    Long featuredLinkId = simpleSearch.getFeaturedLinkId();
                    if (featuredLinkId != null) {
                        suggestion = featuredLinkServices.get(featuredLinkId);
                    } else {
                        String collectionName = simpleSearch.getCollectionName();
                        if (simpleSearch.getAdvancedSearchRule() == null) {
	                        String text = simpleSearch.getQuery();
	                        RecordCollection collection = collectionServices.get(collectionName);
	                        suggestion = featuredLinkServices.suggest(text, collection);
	                        if (suggestion == null) {
	                            SynonymServices synonymServices = ConstellioSpringUtils.getSynonymServices();
	                            List<String> synonyms = synonymServices.getSynonyms(text, collectionName);
	                            if (!synonyms.isEmpty()) {
	                                for (String synonym : synonyms) {
	                                    if (!synonym.equals(text)) {
	                                        suggestion = featuredLinkServices.suggest(synonym, collection);
	                                    }
	                                    if (suggestion != null) {
	                                        break;
	                                    }
	                                }
	                            }
	                        }
                        } else {
                        	suggestion = new FeaturedLink();
                        }
                    }
                    return suggestion;
                }
            };
            IModel featuredLinkTitleModel = new LoadableDetachableModel() {
                @Override
                protected Object load() {
                    FeaturedLink featuredLink = (FeaturedLink) featuredLinkModel.getObject();
                    return featuredLink.getTitle(getLocale());
                }
            };
            final IModel featuredLinkDescriptionModel = new LoadableDetachableModel() {
                @Override
                protected Object load() {
                    FeaturedLink featuredLink = (FeaturedLink) featuredLinkModel.getObject();
                    StringBuffer descriptionSB = new StringBuffer();
                    String description = featuredLink.getDescription(getLocale());
                    if (description != null) {
                        descriptionSB.append(description);
                        String lookFor = "${";
                        int indexOfLookFor = -1;
                        while ((indexOfLookFor = descriptionSB.indexOf(lookFor)) != -1) {
                            int indexOfEnclosingQuote = descriptionSB.indexOf("}", indexOfLookFor);
                            String featuredLinkIdStr = descriptionSB.substring(indexOfLookFor
                                + lookFor.length(), indexOfEnclosingQuote);

                            int indexOfTagBodyStart = descriptionSB.indexOf(">", indexOfEnclosingQuote) + 1;
                            int indexOfTagBodyEnd = descriptionSB.indexOf("</a>", indexOfTagBodyStart);
                            String capsuleQuery = descriptionSB.substring(indexOfTagBodyStart,
                                indexOfTagBodyEnd);
                            if (capsuleQuery.indexOf("<br/>") != -1) {
                                capsuleQuery = StringUtils.remove(capsuleQuery, "<br/>");
                            }
                            if (capsuleQuery.indexOf("<br />") != -1) {
                                capsuleQuery = StringUtils.remove(capsuleQuery, "<br />");
                            }

                            try {
                                String linkedCapsuleURL = getFeaturedLinkURL(new Long(featuredLinkIdStr));
                                descriptionSB.replace(indexOfLookFor, indexOfEnclosingQuote + 1,
                                    linkedCapsuleURL);
                            } catch (NumberFormatException e) {
                                // Ignore
                            }
                        }
                    }
                    return descriptionSB.toString();
                }

                private String getFeaturedLinkURL(Long featuredLinkId) {
                    SimpleSearch clone = simpleSearch.clone();
                    clone.setFeaturedLinkId(featuredLinkId);
                    PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                    String url = RequestCycle.get().urlFor(pageFactoryPlugin.getSearchResultsPage(), getParameters(clone))
                        .toString();
                    return url;
                }
            };

            WebMarkupContainer featuredLink = new WebMarkupContainer("featuredLink", featuredLinkModel) {
                @Override
                public boolean isVisible() {
                    boolean visible = super.isVisible();
                    if (visible) {
                        if (featuredLinkModel.getObject() != null) {
                            String description = (String) featuredLinkDescriptionModel.getObject();
                            visible = StringUtils.isNotEmpty(description);
                        } else {
                            visible = false;
                        }
                    }
                    DataView dataView = (DataView) searchResultsPanel.getDataView();
                    return visible && dataView.getCurrentPage() == 0;
                }
            };
            searchResultsSection.add(featuredLink);
            featuredLink.add(new Label("title", featuredLinkTitleModel));
            featuredLink.add(new WebMarkupContainer("description", featuredLinkDescriptionModel) {
                @Override
                protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                    String descriptionHTML = (String) getModel().getObject();
                    replaceComponentTagBody(markupStream, openTag, descriptionHTML);
                }
            });

            searchResultsSection.add(searchResultsPanel = new SearchResultsPanel("resultatsRecherchePanel",
                dataProvider));
        }
    }

    public SearchResultsPanel getSearchResultsPanel() {
        return searchResultsPanel;
    }

    private static class SortChoice implements Serializable {

        public SortChoice(String name, String title, String order) {
            super();
            this.name = name;
            this.title = title;
            this.order = order;
        }

        private String name;
        private String title;
        private String order;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((order == null) ? 0 : order.hashCode());
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
            SortChoice other = (SortChoice) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (order == null) {
                if (other.order != null)
                    return false;
            } else if (!order.equals(other.order))
                return false;
            return true;
        }

    }
}
