package com.doculibre.constellio.wicket.panels.search.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.search.FacetValue;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SearchedFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;

@SuppressWarnings("serial")
public class SimpleSearchQueryPanel extends Panel {
	
	public SimpleSearchQueryPanel(String id, final SimpleSearch simpleSearch) {
		super(id);
		
		String query = simpleSearch.getQuery();
		if (StringUtils.isBlank(query)) {
			query = SimpleSearch.SEARCH_ALL;
		}
		add(new Label("query", query));
		add(new ListView("facets", simpleSearch.getSearchedFacets()) {
			@Override
			protected void populateItem(ListItem item) {
				final SearchedFacet searchedFacet = (SearchedFacet) item.getModelObject();
				final SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
				if ((searchedFacet.getIncludedValues().isEmpty() && searchedFacet.getExcludedValues().isEmpty()) 
						|| IndexField.LANGUAGE_FIELD.equals(searchableFacet.getName())) {
					item.setVisible(false);
				} else {

	                Locale displayLocale = getLocale();
	                String label = searchableFacet.getLabels().get(displayLocale);
	                item.add(new Label("label", label));

	                IModel includedFacetValuesModel = new LoadableDetachableModel() {
	                    @Override
	                    protected Object load() {
	                        List<FacetValue> facetIncludedValues = new ArrayList<FacetValue>();
                            if (!IndexField.LANGUAGE_FIELD.equals(searchableFacet.getName())) {
                                boolean isCluster = searchableFacet.isCluster();
                                if (isCluster){
                                	int i = 0;
                                	for (String includedValue : searchedFacet.getIncludedValues()) {
                                    	FacetValue facetValue = new FacetValue(searchableFacet, includedValue);
                                    	String valueToClusterLabel = searchedFacet.getClustersLabels().get(i);
                                        facetValue.setValueToClusterLabel(valueToClusterLabel);
                                        facetIncludedValues.add(facetValue);
                                        i++;
                                    }
                                } else {
                                	for (String includedValue : searchedFacet.getIncludedValues()) {
                                    	FacetValue facetValue = new FacetValue(searchableFacet, includedValue);
                                        facetIncludedValues.add(facetValue);
                                    }
                                }
                            }
	                        return facetIncludedValues;
	                    }
	                };
	                item.add(new ListView("includedValues", includedFacetValuesModel) {
						@Override
						protected void populateItem(ListItem item) {
			                FacetValue facetValue = (FacetValue) item.getModelObject();
			                Locale displayLocale = getLocale();
			                item.add(new Label("label", facetValue.getLabel(displayLocale)));
						}
					});


			        IModel excludedFacetValuesModel = new LoadableDetachableModel() {
			            @Override
			            protected Object load() {
			                List<FacetValue> excludedFacetValues = new ArrayList<FacetValue>();
		                    boolean isCluster = searchableFacet.isCluster();
							if (isCluster) {
								int i = 0;
								for (String excludedValue : searchedFacet.getExcludedValues()) {
									FacetValue facetValue = new FacetValue(searchableFacet, excludedValue);
									String valueToClusterLabel = searchedFacet.getClustersLabels().get(i);
									facetValue.setValueToClusterLabel(valueToClusterLabel);
									excludedFacetValues.add(facetValue);
									i++;
								}
							} else {
								for (String excludedValue : searchedFacet.getExcludedValues()) {
									excludedFacetValues.add(new FacetValue(searchableFacet, excludedValue));
								}
							}
			                return excludedFacetValues;
			            }
			        };
			        item.add(new ListView("excludedValues", excludedFacetValuesModel) {
						@Override
						protected void populateItem(ListItem item) {
			                FacetValue facetValue = (FacetValue) item.getModelObject();
			                Locale displayLocale = getLocale();
			                item.add(new Label("label", facetValue.getLabel(displayLocale)));
						}
					});
				}
			}
		});
        
        IModel languagesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				List<Locale> languageLocales = new ArrayList<Locale>();
				Locale singleSearchLocale = simpleSearch.getSingleSearchLocale();
				if (singleSearchLocale != null && StringUtils.isNotBlank(singleSearchLocale.getLanguage())) {
					languageLocales.add(singleSearchLocale);
				} else {
					SearchedFacet languageFacet = simpleSearch.getSearchedFacet(IndexField.LANGUAGE_FIELD);
					if (languageFacet != null) {
						for (String language : languageFacet.getIncludedValues()) {
							if (StringUtils.isNotBlank(language)) {
								Locale languageLocale = new Locale(language);
								languageLocales.add(languageLocale);
							}
						}
					}
				}
				return languageLocales;
			}
		};
		add(new ListView("languages", languagesModel) {
			@Override
			protected void populateItem(ListItem item) {
				Locale languageLocale = (Locale) item.getModelObject();
				String languageLocaleLabel = StringUtils.capitalize(languageLocale.getDisplayLanguage(getLocale()));
				item.add(new Label("language", languageLocaleLabel));
			}
		});
		String searchType = simpleSearch.getSearchType();
		add(new Label("searchType", new StringResourceModel("searchType." + searchType, this, null)).setVisible(StringUtils.isNotBlank(searchType)));
	}

}
