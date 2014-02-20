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
package com.doculibre.constellio.wicket.panels.results.tagging;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.CloseButtonCallback;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.RecordTag;
import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.entities.skos.Thesaurus;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.data.FacetsDataProvider;
import com.doculibre.constellio.wicket.data.SearchResultsDataProvider;
import com.doculibre.constellio.wicket.models.RecordModel;
import com.doculibre.constellio.wicket.models.RecordTagModel;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.pages.SearchFormPage;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class SearchResultTaggingPanel extends AjaxPanel {

    private RecordModel recordModel;

    public SearchResultTaggingPanel(String id, final SolrDocument doc,
        final IDataProvider dataProviderParam) {
        super(id);

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        final SimpleSearch simpleSearch;
        if (dataProviderParam instanceof FacetsDataProvider) {
            FacetsDataProvider dataProvider = (FacetsDataProvider) dataProviderParam;
            simpleSearch = dataProvider.getSimpleSearch();
        } else {
            SearchResultsDataProvider dataProvider = (SearchResultsDataProvider) dataProviderParam;
            simpleSearch = dataProvider.getSimpleSearch();
        }
        
        String collectionName = simpleSearch.getCollectionName();
        RecordCollection collection = collectionServices.get(collectionName);
        if (!collection.isOpenSearch()) {
            RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
            Record record = recordServices.get(doc);
            recordModel = new RecordModel(record);

            final ModalWindow taggingModal = new ModalWindow("taggingModal");
            taggingModal.setTitle(new StringResourceModel("tags", this, null));
            taggingModal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
            taggingModal.setInitialWidth(800);
            taggingModal.setInitialHeight(450);
            taggingModal.setCloseButtonCallback(new CloseButtonCallback() {
                @Override
                public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                    target.addComponent(SearchResultTaggingPanel.this);
                    return true;
                }
            });
            add(taggingModal);

            IModel thesaurusListModel = new LoadableDetachableModel() {
                @Override
                protected Object load() {
                    List<Thesaurus> thesaurusList = new ArrayList<Thesaurus>();
                    RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
                    String collectionName = simpleSearch.getCollectionName();
                    RecordCollection collection = collectionServices.get(collectionName);
                    thesaurusList.add(null);// free text tags
                    if (collection.getThesaurus() != null) {
                        thesaurusList.add(collection.getThesaurus());
                    }
                    return thesaurusList;
                }
            };

            add(new ListView("taggingLinks", thesaurusListModel) {
                @Override
                protected void populateItem(ListItem item) {
                    Thesaurus thesaurus = (Thesaurus) item.getModelObject();
                    final ReloadableEntityModel<Thesaurus> thesaurusModel = new ReloadableEntityModel<Thesaurus>(thesaurus);
                    final String thesaurusName;
                    if (thesaurus == null) {
                        thesaurusName = getLocalizer().getString("tags", this);
                    } else {
                        thesaurusName = getLocalizer().getString("thesaurus", this);
                    }

                    AjaxLink link = new AjaxLink("taggingLink") {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            Thesaurus thesaurus = thesaurusModel.getObject();
                            SearchResultEditTaggingPanel editTaggingPanel = new SearchResultEditTaggingPanel(
                                taggingModal.getContentId(), doc, dataProviderParam, thesaurus);
                            taggingModal.setContent(editTaggingPanel);
                            taggingModal.show(target);
                        }

                        @Override
                        public boolean isEnabled() {
                            boolean enabled = super.isEnabled();
                            if (enabled) {
                                Record record = recordModel.getObject();
                                if (record != null) {
                                    ConstellioUser user = ConstellioSession.get().getUser();
                                    if (user != null) {
                                        RecordCollection collection = record.getConnectorInstance()
                                            .getRecordCollection();
                                        enabled = user.hasCollaborationPermission(collection);
                                    } else {
                                        enabled = false;
                                    }
                                } else {
                                    enabled = false;
                                }
                            }
                            return enabled;
                        }

                        @Override
                        public void detachModels() {
                            thesaurusModel.detach();
                            super.detachModels();
                        }
                    };
                    item.add(link);
                    link.add(new Label("thesaurusName", thesaurusName));

                    final IModel tagsModel = new LoadableDetachableModel() {
                        @Override
                        protected Object load() {
                            Record record = recordModel.getObject();
                            Thesaurus thesaurus = thesaurusModel.getObject();
                            return new ArrayList<RecordTag>(record.getIncludedRecordTags(thesaurus));
                        }
                    };
                    item.add(new ListView("tags", tagsModel) {
                        @SuppressWarnings("unchecked")
                        @Override
                        protected void populateItem(ListItem item) {
                            RecordTag recordTag = (RecordTag) item.getModelObject();
                            final RecordTagModel recordTagModel = new RecordTagModel(recordTag);
                            Link addTagLink = new Link("addTagLink") {
                                @Override
                                public void onClick() {
                                    RecordTag recordTag = recordTagModel.getObject();
                                    SimpleSearch clone = simpleSearch.clone();
                                    clone.getTags().add(recordTag.getName(getLocale()));

                                    PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                                    if (StringUtils.isNotBlank(clone.getLuceneQuery())) {
                                        // ConstellioSession.get().addSearchHistory(clone);
                                        setResponsePage(pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage
                                            .getParameters(clone));
                                    } else {
                                        SimpleSearch newSearch = new SimpleSearch();
                                        newSearch.setCollectionName(simpleSearch.getCollectionName());
                                        newSearch.setSingleSearchLocale(simpleSearch.getSingleSearchLocale());
                                        setResponsePage(pageFactoryPlugin.getSearchFormPage(), SearchFormPage
                                            .getParameters(newSearch));
                                    }
                                }

                                @Override
                                public void detachModels() {
                                    recordTagModel.detach();
                                    super.detachModels();
                                }
                            };
                            item.add(addTagLink);
                            List<RecordTag> recordTags = (List<RecordTag>) tagsModel.getObject();
                            String tag = recordTag.getName(getLocale());
                            if (item.getIndex() < recordTags.size() - 1) {
                                tag += ";";
                            }
                            addTagLink.add(new Label("tag", tag));
                            addTagLink.setEnabled(false);
                        }
                    });
                    item.add(new WebMarkupContainer("noTags") {
                        @SuppressWarnings("unchecked")
                        @Override
                        public boolean isVisible() {
                            List<RecordTag> recordTags = (List<RecordTag>) tagsModel.getObject();
                            return super.isVisible() && recordTags.isEmpty();
                        }
                    });
                }
            });

        } else {
            setVisible(false);
        }
    }

    @Override
    public boolean isVisible() {
        boolean visible = super.isVisible();
        if (visible) {
            Record record = recordModel.getObject();
            boolean hasTags = !record.getIncludedRecordTags().isEmpty();
            if (ConstellioSession.get().isSignedIn()) {
                ConstellioUser user = ConstellioSession.get().getUser();
                // FIXME
                if (user != null) {
                    RecordCollection collection = record.getConnectorInstance().getRecordCollection();
                    if (user.hasCollaborationPermission(collection)) {
                        visible = true;
                    } else {
                        visible = hasTags;
                    }
                } else {
                	visible = false;
                }
            } else {
                SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils
                    .getSearchInterfaceConfigServices();
                SearchInterfaceConfig config = searchInterfaceConfigServices.get();
                visible = hasTags && config.isAlwaysDisplayTags();
            }
        }
        return visible;
    }

    @Override
    public void detachModels() {
        if (recordModel != null) {
            recordModel.detach();
        }
        super.detachModels();
    }

}
