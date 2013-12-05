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
package com.doculibre.constellio.wicket.panels.thesaurus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.skos.SkosConcept;
import com.doculibre.constellio.entities.skos.Thesaurus;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SkosServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;

@SuppressWarnings("serial")
public class ThesaurusSuggestionPanel extends Panel {

    private WebMarkupContainer prefLabelLink;
    private Component prefLabelLabel;
    private WebMarkupContainer semanticNetworkLink;
    private Component semanticNetworkLabel;
    private ModalWindow semanticNetworkModal;
    private WebMarkupContainer disambiguation;

    public ThesaurusSuggestionPanel(String id, final SimpleSearch simpleSearch, final Locale displayLocale) {
        super(id);

        boolean visible = false;

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        SkosServices skosServices = ConstellioSpringUtils.getSkosServices();

        String collectionName = simpleSearch.getCollectionName();
        if (simpleSearch.isQueryValid()) {
            String query = simpleSearch.getQuery();
            if (StringUtils.isNotBlank(query) && !SimpleSearch.SEARCH_ALL.equals(query)) {
                RecordCollection collection = collectionServices.get(collectionName);
                if (collection != null) {
                    Thesaurus thesaurus = collection.getThesaurus();
                    if (thesaurus != null) {
                    	Locale locale = collection.getDisplayLocale(getLocale());
                        Set<SkosConcept> prefLabelMatches = skosServices.getByPrefLabel(query, thesaurus, locale);
                        Set<SkosConcept> altLabelMatches = skosServices.searchAltLabels(query, thesaurus, locale);
                        if (prefLabelMatches.size() == 1) {
                            SkosConcept skosConcept = prefLabelMatches.iterator().next();
                            final ReloadableEntityModel<SkosConcept> skosConceptModel = new ReloadableEntityModel<SkosConcept>(
                                skosConcept);

                            prefLabelLink = new WebMarkupContainer("prefLabelLink");
                            prefLabelLink.setVisible(false);
                            prefLabelLabel = new WebMarkupContainer("prefLabelLabel");
                            prefLabelLink.setVisible(false);

                            semanticNetworkLink = new AjaxLink("semanticNetworkLink") {
                                @Override
                                public void onClick(AjaxRequestTarget target) {
                                    SkosConcept skosConcept = skosConceptModel.getObject();
                                    semanticNetworkModal.setContent(new SkosConceptModalPanel(
                                        semanticNetworkModal.getContentId(), skosConcept) {
                                        @Override
                                        protected AbstractLink newDetailsLink(String id,
                                            SkosConcept skosConcept) {
                                            String prefLabel = "" + skosConcept.getPrefLabel(displayLocale);
                                            prefLabel = prefLabel.toLowerCase();
                                            SimpleSearch clone = simpleSearch.clone();
                                            clone.setQuery(prefLabel);

                                            PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                                            return new BookmarkablePageLink(id, pageFactoryPlugin.getSearchResultsPage(),
                                                SearchResultsPage.getParameters(clone));
                                        }
                                    });
                                    semanticNetworkModal.show(target);
                                }

                                @Override
                                public void detachModels() {
                                    skosConceptModel.detach();
                                    super.detachModels();
                                }
                            };

                            String prefLabel = "" + skosConcept.getPrefLabel(displayLocale);
                            prefLabel = prefLabel.toLowerCase();
                            semanticNetworkLabel = new Label("semanticNetworkLabel", prefLabel);
                            semanticNetworkModal = new ModalWindow("semanticNetworkModal");

                            disambiguation = new WebMarkupContainer("disambiguation");
                            disambiguation.setVisible(false);

                            add(prefLabelLink);
                            prefLabelLink.add(prefLabelLabel);
                            add(semanticNetworkLink);
                            semanticNetworkLink.add(semanticNetworkLabel);
                            add(semanticNetworkModal);
                            add(disambiguation);

                            visible = true;
                        } else if (prefLabelMatches.size() > 1) {
                            prefLabelLink = new WebMarkupContainer("prefLabelLink");
                            prefLabelLink.setVisible(false);
                            prefLabelLabel = new WebMarkupContainer("prefLabelLabel");
                            prefLabelLink.setVisible(false);

                            semanticNetworkLink = new WebMarkupContainer("semanticNetworkLink");
                            semanticNetworkLink.setVisible(false);
                            semanticNetworkLabel = new WebMarkupContainer("semanticNetworkLabel");
                            semanticNetworkLabel.setVisible(false);
                            semanticNetworkModal = new ModalWindow("semanticNetworkModal");
                            semanticNetworkModal.setVisible(false);

                            List<String> disambiguationPrefLabels = new ArrayList<String>();
                            for (SkosConcept skosConcept : prefLabelMatches) {
                                String prefLabel = "" + skosConcept.getPrefLabel(displayLocale);
                                prefLabel = prefLabel.toLowerCase();
                                disambiguationPrefLabels.add(prefLabel);
                            }
                            disambiguation = new ListView("disambiguation", disambiguationPrefLabels) {
                                @Override
                                protected void populateItem(ListItem item) {
                                    String prefLabel = (String) item.getModelObject();
                                    SimpleSearch clone = simpleSearch.clone();
                                    clone.setQuery(prefLabel);

                                    PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                                    Link prefLabelLink = new BookmarkablePageLink("prefLabelLink",
                                        pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone));
                                    Label prefLabelLabel = new Label("prefLabel", prefLabel);
                                    item.add(prefLabelLink);
                                    prefLabelLink.add(prefLabelLabel);
                                }
                            };

                            add(prefLabelLink);
                            prefLabelLink.add(prefLabelLabel);
                            add(semanticNetworkLink);
                            add(semanticNetworkModal);
                            add(disambiguation);

                            visible = true;
                        } else if (!altLabelMatches.isEmpty()) {
                            SkosConcept firstAltLabelConcept = altLabelMatches.iterator().next();
                            String prefLabel = "" + firstAltLabelConcept.getPrefLabel(displayLocale);
                            prefLabel = prefLabel.toLowerCase();
                            SimpleSearch clone = simpleSearch.clone();
                            clone.setQuery(prefLabel);

                            PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                            prefLabelLink = new BookmarkablePageLink("prefLabelLink",
                                pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone));
                            prefLabelLabel = new Label("prefLabelLabel", prefLabel);

                            semanticNetworkLink = new WebMarkupContainer("semanticNetworkLink");
                            semanticNetworkLink.setVisible(false);
                            semanticNetworkLabel = new WebMarkupContainer("semanticNetworkLabel");
                            semanticNetworkLabel.setVisible(false);
                            semanticNetworkModal = new ModalWindow("semanticNetworkModal");
                            semanticNetworkModal.setVisible(false);

                            disambiguation = new WebMarkupContainer("disambiguation");
                            disambiguation.setVisible(false);

                            add(prefLabelLink);
                            prefLabelLink.add(prefLabelLabel);
                            add(semanticNetworkLink);
                            add(semanticNetworkModal);
                            add(disambiguation);

                            visible = true;
                        }
                    }
                }
            }
        }
        setVisible(visible);
    }

}
