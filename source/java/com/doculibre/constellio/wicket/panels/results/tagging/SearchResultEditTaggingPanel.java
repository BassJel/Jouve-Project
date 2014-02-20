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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.RecordTag;
import com.doculibre.constellio.entities.skos.SkosConcept;
import com.doculibre.constellio.entities.skos.Thesaurus;
import com.doculibre.constellio.services.FreeTextTagServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.services.SkosServices;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceUtils;
import com.doculibre.constellio.wicket.models.EntityModel;
import com.doculibre.constellio.wicket.models.RecordModel;
import com.doculibre.constellio.wicket.models.RecordTagModel;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.thesaurus.SkosConceptModalPanel;

@SuppressWarnings("serial")
public class SearchResultEditTaggingPanel extends Panel {

    private EntityModel<Thesaurus> taggingSourceModel;
    private RecordModel recordModel;

    private Form filterAddForm;
    private DropDownChoice taggingSourceField;
    private TextField filterAddField;
    private Button filterAddButton;

    private WebMarkupContainer availableTagsContainer;
    private FilteredObjectsModel availableTagsModel;
    private ListView availableTagsListView;

    private WebMarkupContainer appliedTagsContainer;
    private FilteredObjectsModel appliedTagsModel;
    private ListView appliedTagsListView;

    private AjaxLink closeLink;
    
    private boolean freeTextTags;

    public SearchResultEditTaggingPanel(String id, final SolrDocument doc,
            final IDataProvider dataProvider) {
    	this(id, doc, dataProvider, null);
    }           
    
    public SearchResultEditTaggingPanel(String id, final SolrDocument doc,
        final IDataProvider dataProvider, Thesaurus source) {
        super(id);
        if (source == null) {
        	freeTextTags = true;
        	source = new Thesaurus(getLocalizer().getString("freeTextTags", SearchResultEditTaggingPanel.this));
        }
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        Record record = recordServices.get(doc);
        recordModel = new RecordModel(record);
        taggingSourceModel = new EntityModel<Thesaurus>(source);

        filterAddForm = new Form("filterAddForm");
        filterAddForm.setOutputMarkupId(true);
        
        IModel taggingSourceChoicesModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                List<Object> taggingSources = new ArrayList<Object>();
                
                taggingSources.add(new Thesaurus(getLocalizer().getString("freeTextTags", SearchResultEditTaggingPanel.this)));
                
                SkosServices skosServices = ConstellioSpringUtils.getSkosServices();
                Record record = recordModel.getObject();
                ConnectorInstance connectorInstance = record.getConnectorInstance();
                RecordCollection collection = connectorInstance.getRecordCollection();
                Map<String, Object> criteria = new HashMap<String, Object>();
                criteria.put("recordCollection", collection);
                taggingSources.addAll(skosServices.list(criteria));
                
                return taggingSources;
            }
        };
        IChoiceRenderer taggingSourceRenderer = new ChoiceRenderer("dcTitle");
        taggingSourceField = new DropDownChoice("taggingSource", taggingSourceModel, taggingSourceChoicesModel, taggingSourceRenderer);
        taggingSourceField.setVisible(isSourceSelectionVisible());
        taggingSourceField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.addComponent(availableTagsContainer);
                target.addComponent(appliedTagsContainer);
                target.addComponent(filterAddField);
                target.addComponent(filterAddButton);
            }
        });
        
        filterAddField = new TextField("filterAddField", new Model());
        filterAddField.setOutputMarkupId(true);
        filterAddField.add(new AjaxFormComponentUpdatingBehavior("onkeyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                String input = filterAddField.getModelObjectAsString();
                availableTagsModel.setFilter(input);
                appliedTagsModel.setFilter(input);
                if (isFreeTextTagSource()) {
                    target.addComponent(availableTagsContainer);
                    target.addComponent(appliedTagsContainer);
                }
            }
        });

        filterAddButton = new AjaxButton("filterAddButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                String tagName = (String) filterAddField.getModelObject();
                if (StringUtils.isNotBlank(tagName)) {
                    if (isFreeTextTagSource()) {
                        FreeTextTagServices freeTextTagServices = ConstellioSpringUtils.getFreeTextTagServices();
                        FreeTextTag existingTag = freeTextTagServices.get(tagName);
                        if (existingTag == null) {
                            FreeTextTag newTag = new FreeTextTag();
                            newTag.setFreeText(tagName);

                            EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                            if (!entityManager.getTransaction().isActive()) {
                                entityManager.getTransaction().begin();
                            }
                            freeTextTagServices.makePersistent(newTag);
                            entityManager.getTransaction().commit();

                            availableTagsModel.setFilter(null);
                            appliedTagsModel.setFilter(null);
                            filterAddField.clearInput();
                            filterAddField.setModelObject(null);
                            target.addComponent(filterAddField);
                            target.addComponent(availableTagsContainer);
                            target.addComponent(appliedTagsContainer);
                        }
                    } else {
                        target.addComponent(availableTagsContainer);
                    }
                }
            }
        };
        filterAddButton.add(new AttributeModifier("value", true, new LoadableDetachableModel() {
            @Override
            protected Object load() {
                String key = isFreeTextTagSource() ? "add" : "filter";
                return getLocalizer().getString(key, SearchResultEditTaggingPanel.this);
            }
        }));

        availableTagsContainer = new WebMarkupContainer("availableTagsContainer");
        availableTagsContainer.setOutputMarkupId(true);
        availableTagsModel = new FilteredObjectsModel() {
            @Override
            protected List<Object> filter(String filter) {
                List<Object> matches = new ArrayList<Object>();
                Thesaurus taggingSource = taggingSourceModel.getObject();

                if (taggingSource == null || taggingSource.getId() == null) {
                	if (StringUtils.isNotBlank(filter)) {
                		filter = filter + "*";
                  	}
                    FreeTextTagServices taggingServices = ConstellioSpringUtils.getFreeTextTagServices();
                    if (StringUtils.isBlank(filter)) {
                        List<FreeTextTag> first100 = taggingServices.list(100);
                        matches.addAll(first100);
                    } else {
                        Set<FreeTextTag> searchResults = taggingServices.search(filter);
                        matches.addAll(searchResults);
                    }
                } else {
                    SkosServices skosServices = ConstellioSpringUtils.getSkosServices();
                    Set<SkosConcept> searchResults = skosServices.searchAllLabels(filter, taggingSource, null);
                    matches.addAll(searchResults);
                }
                return matches;
            }
        };
        availableTagsListView = new ListView("availableTags", availableTagsModel) {
            @Override
            protected void populateItem(ListItem item) {
                ConstellioEntity tagSource = (ConstellioEntity) item.getModelObject();
                final ReloadableEntityModel<ConstellioEntity> tagSourceModel = new ReloadableEntityModel<ConstellioEntity>(
                        tagSource);
                final ModalWindow detailsModal = new ModalWindow("detailsModal");
                item.add(detailsModal);

                final AjaxLink detailsLink = new AjaxLink("detailsLink") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        ConstellioEntity tagSource = (ConstellioEntity) tagSourceModel.getObject();
                        if (tagSource instanceof SkosConcept) {
                            SkosConcept skosConcept = (SkosConcept) tagSource;
                            detailsModal.setContent(new SkosConceptModalPanel(detailsModal.getContentId(), skosConcept));
                            detailsModal.show(target);
                        }
                    }
                };
                item.add(detailsLink);
                detailsLink.setEnabled(tagSource instanceof SkosConcept);
                
                detailsLink.add(new Label("name", new LoadableDetachableModel() {
                    @Override
                    protected Object load() {
                        String name;
                        ConstellioEntity tagSource = (ConstellioEntity) tagSourceModel.getObject();
                        if (tagSource != null) {
                            if (tagSource instanceof FreeTextTag) {
                                FreeTextTag freeTextTag = (FreeTextTag) tagSource;
                                name = freeTextTag.getFreeText();
                            } else {
                                SkosConcept skosConcept = (SkosConcept) tagSource;
                                name = skosConcept.getPrefLabel(getLocale());
                            }
                        } else {
                            name = "null";
                        }
                        return name;
                    }
                }));
                
                item.add(new AjaxLink("addLink") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        Record record = recordModel.getObject();
                        ConstellioEntity tagSource = (ConstellioEntity) tagSourceModel.getObject();
                        if (tagSource instanceof FreeTextTag) {
                            FreeTextTag freeTextTag = (FreeTextTag) tagSource;
                            record.addFreeTextTag(freeTextTag, true);
                        } else {
                            SkosConcept skosConcept = (SkosConcept) tagSource;
                            record.addSkosConcept(skosConcept, true);
                        }
                        record.setUpdateIndex(true);

                        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();

            			SolrServer solrServer = SolrCoreContext.getSolrServer(record.getConnectorInstance().getRecordCollection());
            			try {
                        	ConstellioPersistenceUtils.beginTransaction();                
                            recordServices.makePersistent(record);
                            try {
								solrServer.commit();
							} catch (Throwable t) {
								try {
									solrServer.rollback();
								} catch (Exception e) {
									throw new RuntimeException(t);
								}
							}
            			} finally {
            				ConstellioPersistenceUtils.finishTransaction(false);
            			}

                        target.addComponent(availableTagsContainer);
                        target.addComponent(appliedTagsContainer);
                    }

                    @Override
                    public boolean isVisible() {
                        boolean visible = super.isVisible();
                        if (visible) {
                            Record record = recordModel.getObject();
                            if (tagSourceModel.getObject() instanceof FreeTextTag) {
                                FreeTextTag freeTextTag = (FreeTextTag) tagSourceModel.getObject();
                                visible = !record.hasFreeTextTag(freeTextTag);
                            } else {
                                SkosConcept skosConcept = (SkosConcept) tagSourceModel.getObject();
                                visible = !record.hasSkosConcept(skosConcept);
                            }
                        }
                        return visible;
                    }

                    @Override
                    public void detachModels() {
                        tagSourceModel.detach();
                        super.detachModels();
                    }
                });
            }
        };

        appliedTagsContainer = new WebMarkupContainer("appliedTagsContainer");
        appliedTagsContainer.setOutputMarkupId(true);
        appliedTagsModel = new FilteredObjectsModel() {
            @Override
            protected List<RecordTag> filter(String filter) {
                List<RecordTag> matches = new ArrayList<RecordTag>();
                Record record = recordModel.getObject();
                Thesaurus source = taggingSourceModel.getObject();
                for (RecordTag recordTag : record.getIncludedRecordTags(freeTextTags ? null : source, true)) {
//                    if (StringUtils.isEmpty(filter)
//                        || recordTag.getName(getLocale()).toLowerCase().indexOf(filter.toLowerCase()) != -1) {
                        matches.add(recordTag);
//                    }
                }
                return matches;
            }
        };
        appliedTagsListView = new ListView("appliedTags", appliedTagsModel) {
            @Override
            protected void populateItem(ListItem item) {
                RecordTag recordTag = (RecordTag) item.getModelObject();
                final RecordTagModel recordTagModel = new RecordTagModel(recordTag);

                ConstellioEntity tagSource;
                if (recordTag.getFreeTextTag() != null) {
                    tagSource = recordTag.getFreeTextTag();
                } else {
                    tagSource = recordTag.getSkosConcept();
                }
                final ReloadableEntityModel<ConstellioEntity> tagSourceModel = new ReloadableEntityModel<ConstellioEntity>(
                        tagSource);
                final ModalWindow detailsModal = new ModalWindow("detailsModal");
                item.add(detailsModal);

                final AjaxLink detailsLink = new AjaxLink("detailsLink") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        ConstellioEntity tagSource = (ConstellioEntity) tagSourceModel.getObject();
                        if (tagSource instanceof SkosConcept) {
                            SkosConcept skosConcept = (SkosConcept) tagSource;
                            detailsModal.setContent(new SkosConceptModalPanel(detailsModal.getContentId(), skosConcept));
                            detailsModal.show(target);
                        }
                    }
                };
                item.add(detailsLink);
                detailsLink.setEnabled(tagSource instanceof SkosConcept);
                
                detailsLink.add(new Label("name", new LoadableDetachableModel() {
                    @Override
                    protected Object load() {
                        String name;
                        ConstellioEntity tagSource = (ConstellioEntity) tagSourceModel.getObject();
                        if (tagSource != null) {
                            if (tagSource instanceof FreeTextTag) {
                                FreeTextTag freeTextTag = (FreeTextTag) tagSource;
                                name = freeTextTag.getFreeText();
                            } else {
                                SkosConcept skosConcept = (SkosConcept) tagSource;
                                name = skosConcept.getPrefLabel(getLocale());
                            }
                        } else {
                            name = "null";
                        }
                        return name;
                    }
                }));
                detailsLink.add(new AttributeModifier("style", true, new LoadableDetachableModel() {
                    @Override
                    protected Object load() {
                        RecordTag recordTag = recordTagModel.getObject();
                        return recordTag.isExcluded() ? "text-decoration: line-through;" : "text-decoration:none";
                    }
                }));
                item.add(new AjaxLink("removeLink") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        Record record = recordModel.getObject();
                        RecordTag recordTag = recordTagModel.getObject();
                        boolean excluded = recordTag.isExcluded();
                        if (excluded) {
                            record.getRecordTags().remove(recordTag);
                        } else {
                            recordTag.setManual(true);
                            recordTag.setExcluded(true);
                        }
                        record.setUpdateIndex(true);

                        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();

            			SolrServer solrServer = SolrCoreContext.getSolrServer(record.getConnectorInstance().getRecordCollection());
            			try {
                        	ConstellioPersistenceUtils.beginTransaction();                
                            recordServices.makePersistent(record);
                            try {
								solrServer.commit();
							} catch (Throwable t) {
								try {
									solrServer.rollback();
								} catch (Exception e) {
									throw new RuntimeException(t);
								}
							}
            			} finally {
            				ConstellioPersistenceUtils.finishTransaction(false);
            			}

                        target.addComponent(availableTagsContainer);
                        target.addComponent(appliedTagsContainer);
                    }

                    @Override
                    public void detachModels() {
                        recordTagModel.detach();
                        tagSourceModel.detach();
                        super.detachModels();
                    }
                });
            }
        };

        closeLink = new AjaxLink("closeLink") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                SearchResultTaggingPanel parent = (SearchResultTaggingPanel) findParent(SearchResultTaggingPanel.class);
                target.addComponent(parent);
                ModalWindow.closeCurrent(target);
            }
        };

        add(taggingSourceField);
        add(new Label("sourceName", source.getDcTitle()).setVisible(!isSourceSelectionVisible()));
        add(filterAddForm);
        filterAddForm.add(filterAddField);
        filterAddForm.add(filterAddButton);

        add(availableTagsContainer);
        availableTagsContainer.add(availableTagsListView);

        add(appliedTagsContainer);
        appliedTagsContainer.add(appliedTagsListView);

        add(closeLink);
    }

    @Override
    public void detachModels() {
        recordModel.detach();
        super.detachModels();
    }
    
    private boolean isFreeTextTagSource() {
        return taggingSourceModel.getObject() == null || taggingSourceModel.getObject().getId() == null;
    }

    private static abstract class FilteredObjectsModel extends LoadableDetachableModel {

        private String filter;

        public void setFilter(String filter) {
            this.filter = filter;
        }

        @Override
        protected Object load() {
            return filter(filter);
        }

        @SuppressWarnings("rawtypes")
		protected abstract List filter(String filter);

    }
    
    protected boolean isSourceSelectionVisible() {
    	return false;
    }

}
