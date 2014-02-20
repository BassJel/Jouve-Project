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
package com.doculibre.constellio.wicket.panels.facets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.doculibre.analyzer.AccentApostropheCleaner;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.FacetValue;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SearchedFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.plugins.api.wicket.facets.FacetDisplayPlugin;
import com.doculibre.constellio.search.SolrFacetUtils;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.NumberFormatUtils;
import com.doculibre.constellio.wicket.behaviors.StyleClassAppender;
import com.doculibre.constellio.wicket.data.FacetsDataProvider;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;
import com.doculibre.constellio.wicket.paging.ConstellioFacetsPagingNavigator;
import com.doculibre.constellio.wicket.renderers.StringResourceChoiceRenderer;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class FacetPanel extends Panel {

    private SearchableFacet searchableFacet;

    IModel possibleValuesModel;
    private List<FacetValue> selectedValues = new ArrayList<FacetValue>();

    private Map<String, Check> checkboxes = new HashMap<String, Check>();

    private Form facetForm;
    private DropDownChoice sortField;
    private Button refineButton;
    private Button excludeButton;
    private CheckGroup selectedValuesCheckGroup;
    private WebMarkupContainer listViewContainer;
    private PagingNavigator pagingNavigator;
    private Link allResultsLink;

    private PageableListView possibleValuesListView;

    public FacetPanel(String id, final SearchableFacet searchableFacet,
        final FacetsDataProvider dataProvider, final FacetsDataProvider notIncludedDataProvider) {
        super(id, new CompoundPropertyModel(searchableFacet));
        this.searchableFacet = searchableFacet;

        possibleValuesModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
                String collectionName = simpleSearch.getCollectionName();
                ConstellioUser user = ConstellioSession.get().getUser();
                List<FacetValue> possibleValues = new ArrayList<FacetValue>();
                List<FacetValue> includedFacetPossibleValues = SolrFacetUtils.getPossibleValues(
                    searchableFacet, dataProvider, collectionName, user);
                List<FacetValue> notIncludedFacetPossibleValues = SolrFacetUtils.getPossibleValues(
                    searchableFacet, notIncludedDataProvider, collectionName, user);
                possibleValues.addAll(includedFacetPossibleValues);
                for (FacetValue notIncludedFacetPossibleValue : notIncludedFacetPossibleValues) {
                    boolean alreadyIncluded = false;
                    loop2: for (FacetValue possibleValue : possibleValues) {
                        if (possibleValue.getValue().equals(notIncludedFacetPossibleValue.getValue())) {
                            alreadyIncluded = true;
                            break loop2;
                        }
                    }
                    if (!alreadyIncluded) {
                        possibleValues.add(notIncludedFacetPossibleValue);
                    }
                }

                SearchedFacet searchedFacet = simpleSearch.getSearchedFacet(searchableFacet.getName());
                // We already applied a value for this facet
                if (searchedFacet != null) {
                    List<FacetValue> deletedValues = new ArrayList<FacetValue>();
                    for (FacetValue facetValue : possibleValues) {
                        if (searchedFacet.getExcludedValues().contains(facetValue.getValue())) {
                            // To avoid concurrent modifications
                            deletedValues.add(facetValue);
                        }
                    }
                    for (FacetValue deletedValue : deletedValues) {
                        // Will remove this value from the possible values
                        possibleValues.remove(deletedValue);
                    }

                    for (FacetValue facetValue : possibleValues) {
                        if (searchedFacet.getIncludedValues().contains(facetValue.getValue())) {
                            // Aura pour effet de cocher la case au chargement de la
                            // page
                            selectedValues.add(facetValue);
                        }
                    }
                    applySort(possibleValues, simpleSearch.getFacetSort(searchableFacet.getName()),
                        dataProvider);
                }
                return possibleValues;
            }
        };

        facetForm = new StatelessForm("facetForm");

        List<String> sortOptions = new ArrayList<String>();
        sortOptions.add(SearchableFacet.SORT_ALPHA);
        sortOptions.add(SearchableFacet.SORT_NB_RESULTS);

        IChoiceRenderer sortChoiceRenderer = new StringResourceChoiceRenderer(this);

        sortField = new DropDownChoice("sort", new Model() {
            @Override
            public Object getObject() {
                SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
                return simpleSearch.getFacetSort(searchableFacet.getName());
            }

            @Override
            public void setObject(final Object object) {
                SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
                simpleSearch.setFacetSort(searchableFacet.getName(), (String) object);
                possibleValuesModel.detach();
            }
        }, sortOptions, sortChoiceRenderer);
        sortField.setVisible(!searchableFacet.isQuery() && searchableFacet.isSortable());
        sortField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.addComponent(FacetPanel.this);
            }
        });

        refineButton = new Button("refineButton") {
            @Override
            public void onSubmit() {
                SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
                SimpleSearch clone = simpleSearch.clone();

                for (SearchedFacet searchedFacet : clone.getSearchedFacets()) {
                    if (searchedFacet.getSearchableFacet().equals(searchableFacet)) {
                        List<String> includedValuesNotAlreadySelected = new ArrayList<String>();
                        for (String includedValue : searchedFacet.getIncludedValues()) {
                            boolean includedValueSelected = false;
                            for (FacetValue selectedValue : selectedValues) {
                                if (selectedValue.getValue().equals(includedValue)) {
                                    includedValueSelected = true;
                                    break;
                                }
                            }
                            if (!includedValueSelected) {
                                includedValuesNotAlreadySelected.add(includedValue);
                            }
                        }
                        for (String includedValueNotAlreadySelected : includedValuesNotAlreadySelected) {
                            searchedFacet.getIncludedValues().remove(includedValueNotAlreadySelected);
                        }
                    }
                }
                for (FacetValue selectedValue : selectedValues) {
                    clone.addSearchedFacet(searchableFacet, selectedValue);
                }

                List<SearchedFacet> deletedSearchedFacets = new ArrayList<SearchedFacet>();
                for (SearchedFacet searchedFacet : clone.getSearchedFacets()) {
                    if (searchedFacet.getIncludedValues().isEmpty()
                        && searchedFacet.getExcludedValues().isEmpty()) {
                        deletedSearchedFacets.add(searchedFacet);
                    }
                }
                for (SearchedFacet deletedSearchedFacet : deletedSearchedFacets) {
                    clone.getSearchedFacets().remove(deletedSearchedFacet);
                }

                // ConstellioSession.get().addSearchHistory(clone);

                clone.setPage(0);

                PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                setResponsePage(pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone));
            }

            @Override
            public boolean isVisible() {
                return getPossibleValues().size() > 1;
            }
        };

        excludeButton = new Button("excludeButton") {
            @Override
            public void onSubmit() {
                SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
                SimpleSearch clone = simpleSearch.clone();
                for (FacetValue selectedValue : selectedValues) {
                    clone.excludeSearchedFacet(searchableFacet, selectedValue);
                }

                List<SearchedFacet> deletedSearchedFacets = new ArrayList<SearchedFacet>();
                for (SearchedFacet searchedFacet : clone.getSearchedFacets()) {
                    if (searchedFacet.getIncludedValues().isEmpty()
                        && searchedFacet.getExcludedValues().isEmpty()) {
                        deletedSearchedFacets.add(searchedFacet);
                    }
                }
                for (SearchedFacet deletedSearchedFacet : deletedSearchedFacets) {
                    clone.getSearchedFacets().remove(deletedSearchedFacet);
                }

                // ConstellioSession.get().addSearchHistory(clone);

                clone.setPage(0);

                PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                setResponsePage(pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone));
            }

            @Override
            public boolean isVisible() {
                return getPossibleValues().size() > 1 && !searchableFacet.isQuery();
            }
        };

        selectedValuesCheckGroup = new CheckGroup("selectedValuesCheckGroup", selectedValues);

        listViewContainer = new WebMarkupContainer("listViewContainer");
        listViewContainer.setOutputMarkupId(true);

        possibleValuesListView = new PageableListView("possibleValues", possibleValuesModel,
            Integer.MAX_VALUE) {
            @Override
            protected void populateItem(ListItem item) {
                FacetValue possibleValue = (FacetValue) item.getModelObject();
                final String value = possibleValue.getValue();
                int numFound = possibleValue.getDocCount();
                Check checkbox = checkboxes.get(value);
                if (checkbox == null) {
                    checkbox = new Check("selectFacet", new Model(possibleValue)) {
                        @Override
                        public boolean isVisible() {
                            return searchableFacet.isMultiValued() && getPossibleValues().size() > 1;
                        }
                    };
                    checkboxes.put(value, checkbox);
                }
                item.add(checkbox);

                SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
                SimpleSearch cloneAddFacet = simpleSearch.clone();
                if (searchableFacet != null && !searchableFacet.isMultiValued()) {
                    cloneAddFacet.removeSearchedFacet(searchableFacet);
                }
                cloneAddFacet.addSearchedFacet(searchableFacet, possibleValue);
                cloneAddFacet.setPage(0);

                PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                Link addFacetLink = new BookmarkablePageLink("addFacetLink", pageFactoryPlugin.getSearchResultsPage(),
                    SearchResultsPage.getParameters(cloneAddFacet));
                item.add(addFacetLink);
                if (!searchableFacet.isMultiValued()) {
                    SearchedFacet searchedFacet = simpleSearch.getSearchedFacet(searchableFacet.getName());
                    if (searchedFacet != null && searchedFacet.getIncludedValues().contains(value)) {
                        item.add(new StyleClassAppender("singleValuedFacetSelected"));
                    }
                }

                SimpleSearch cloneDeleteFacet = simpleSearch.clone();
                cloneDeleteFacet.excludeSearchedFacet(searchableFacet, possibleValue);
                Link deleteFacetLink = new BookmarkablePageLink("deleteFacetLink", pageFactoryPlugin.getSearchResultsPage(),
                    SearchResultsPage.getParameters(cloneDeleteFacet));
                item.add(deleteFacetLink);
                SearchedFacet facet = simpleSearch.getSearchedFacet(searchableFacet.getName());

                boolean deleteFacetLinkVisible;
                FacetDisplayPlugin facetDisplayPlugin = PluginFactory.getPlugin(FacetDisplayPlugin.class);
                if (facetDisplayPlugin == null
                    || !facetDisplayPlugin.isDisplayAllResultsLink(searchableFacet)) {
                    deleteFacetLinkVisible = facet != null && !searchableFacet.isMultiValued()
                        && facet.getIncludedValues().contains(value);
                } else {
                    deleteFacetLinkVisible = false;
                }
                deleteFacetLink.setVisible(deleteFacetLinkVisible);

                RecordCollectionServices collectionServices = ConstellioSpringUtils
                    .getRecordCollectionServices();
                String collectionName = simpleSearch.getCollectionName();
                RecordCollection collection = collectionServices.get(collectionName);
                Locale displayLocale = collection.getDisplayLocale(getLocale());

                String numFoundFormatted = NumberFormatUtils.format(numFound, displayLocale);
                Label countLabel = new Label("count", possibleValue.getLabel(displayLocale) + " ("
                    + numFoundFormatted + ")");
                addFacetLink.add(countLabel);
            }
        };

        FacetDisplayPlugin facetDisplayPlugin = PluginFactory.getPlugin(FacetDisplayPlugin.class);
        if (facetDisplayPlugin == null || facetDisplayPlugin.isPageable(searchableFacet)) {
            possibleValuesListView.setRowsPerPage(10);
        }

        int currentPage = dataProvider.getSimpleSearch().getFacetPage(searchableFacet.getName());
        possibleValuesListView.setCurrentPage(currentPage);
        pagingNavigator = new ConstellioFacetsPagingNavigator("pagingNavigator", possibleValuesListView,
            dataProvider, searchableFacet.getName());

        final SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
        SimpleSearch cloneAllResultsFacet = simpleSearch.clone();
        cloneAllResultsFacet.setPage(0);
        cloneAllResultsFacet.removeSearchedFacet(searchableFacet);

        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
        allResultsLink = new BookmarkablePageLink("allResultsLink", pageFactoryPlugin.getSearchResultsPage(),
            SearchResultsPage.getParameters(cloneAllResultsFacet)) {
            @Override
            public boolean isVisible() {
                boolean visible = super.isVisible();
                if (visible) {
                    SearchedFacet searchedFacet = simpleSearch.getSearchedFacet(searchableFacet.getName());
                    if (searchedFacet != null) {
                        FacetDisplayPlugin facetDisplayPlugin = PluginFactory
                            .getPlugin(FacetDisplayPlugin.class);
                        if (facetDisplayPlugin == null
                            || !facetDisplayPlugin.isDisplayAllResultsLink(searchableFacet)) {
                            visible = false;
                        } else {
                            visible = !searchedFacet.getExcludedValues().isEmpty()
                                || !searchedFacet.getIncludedValues().isEmpty();
                        }
                    }
                }
                return visible;
            }
        };

        WebMarkupContainer multiValuedFacetOptions = new WebMarkupContainer("multiValuedFacetOptions") {
            @Override
            public boolean isVisible() {
                return searchableFacet.isMultiValued();
            }
        };

        WebMarkupContainer facetHeader = new WebMarkupContainer("facetHeader") {
            @Override
            public boolean isVisible() {
                return searchableFacet.isMultiValued() || searchableFacet.isSortable();
            }
        };

        WebMarkupContainer facetFooter = new WebMarkupContainer("facetFooter") {
            @Override
            public boolean isVisible() {
                return pagingNavigator.isVisible() || allResultsLink.isVisible();
            }
        };

        add(facetForm);
        facetForm.add(facetHeader);
        facetForm.add(selectedValuesCheckGroup);
        facetForm.add(facetFooter);
        facetHeader.add(sortField);
        facetHeader.add(multiValuedFacetOptions);
        multiValuedFacetOptions.add(refineButton);
        multiValuedFacetOptions.add(excludeButton);
        selectedValuesCheckGroup.add(listViewContainer);
        listViewContainer.add(possibleValuesListView);
        facetFooter.add(pagingNavigator);
        facetFooter.add(allResultsLink);
    }

    private void applySort(List<FacetValue> possibleValues, final String sort,
        final FacetsDataProvider dataProvider) {
        Collections.sort(possibleValues, new Comparator<FacetValue>() {
            public int compare(FacetValue o1, FacetValue o2) {
                int result;
                SearchableFacet searchableFacet = o1.getSearchableFacet();
                if (searchableFacet.isQuery()) {
                    result = o1.getValue().compareTo(o2.getValue());
                } else {
                    if (SearchableFacet.SORT_ALPHA.equals(sort)) {
                        RecordCollectionServices collectionServices = ConstellioSpringUtils
                            .getRecordCollectionServices();
                        SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
                        String collectionName = simpleSearch.getCollectionName();
                        RecordCollection collection = collectionServices.get(collectionName);
                        Locale displayLocale = collection.getDisplayLocale(getLocale());
                        String label1 = AccentApostropheCleaner.removeAccents(o1.getLabel(displayLocale));
                        String label2 = AccentApostropheCleaner.removeAccents(o2.getLabel(displayLocale));
                        result = label1.compareTo(label2);
                    } else {
                        // Sort descending
                        result = new Integer(o2.getDocCount()).compareTo(o1.getDocCount());
                    }
                }
                return result;
            }
        });
    }

    public SearchableFacet getSearchableFacet() {
        return searchableFacet;
    }

    @SuppressWarnings("unchecked")
    private List<FacetValue> getPossibleValues() {
        return (List<FacetValue>) possibleValuesModel.getObject();
    }

    @Override
    public boolean isVisible() {
        SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils
            .getSearchInterfaceConfigServices();
        boolean isLanguageInSearchForm = searchInterfaceConfigServices.get().isLanguageInSearchForm();
        boolean hasPossibleValues = getPossibleValues().size() > 0;
        return (!isLanguageInSearchForm || !IndexField.LANGUAGE_FIELD.equals(searchableFacet.getName()))
            && super.isVisible() && hasPossibleValues;
    }

}
