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
package com.doculibre.constellio.wicket.panels.results;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.PageCreator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.http.WebRequest;

import com.doculibre.constellio.connector.mail.utils.MailMessageUtils;
import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.ParsedContent;
import com.doculibre.constellio.entities.RawContent;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchResultField;
import com.doculibre.constellio.entities.constants.MailConnectorConstants;
import com.doculibre.constellio.entities.search.Email;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.services.RawContentServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.servlets.ComputeSearchResultClickServlet;
import com.doculibre.constellio.utils.ClasspathUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.data.SearchResultsDataProvider;
import com.doculibre.constellio.wicket.models.RecordModel;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.pages.BaseConstellioPage;
import com.doculibre.constellio.wicket.panels.elevate.ElevatePanel;

@SuppressWarnings("serial")
public class MailSearchResultPanel extends Panel {
    
    private IModel emailModel;

    public MailSearchResultPanel(String id, final SolrDocument doc, final SearchResultsDataProvider dataProvider) {
        super(id);

        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        final Long recordId = new Long(doc.getFieldValue(IndexField.RECORD_ID_FIELD).toString());
        final String collectionName = dataProvider.getSimpleSearch().getCollectionName();
        RecordCollection collection = collectionServices.get(collectionName);
        Record record = recordServices.get(recordId, collection);

        final IModel subjectModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
                Record record = recordServices.get(doc);
                String subject = record.getDisplayTitle();
                if (StringUtils.isBlank(subject)) {
                    subject = getLocalizer().getString("noSubject", MailSearchResultPanel.this);
                }
                if (subject.length() > 60) {
                    subject = subject.substring(0, 60) + " ...";
                }
                return subject;
            }
        };

        // List<byte[]> attachmentContents = new ArrayList<byte[]>();
        // String messageContent = null;
        // for (RawContent raw : rawContents) {
        // byte[] bytes = raw.getContent();
        // if (messageContent == null) {
        // messageContent = new String(bytes);
        // } else {
        // attachmentContents.add(bytes);
        // }
        // System.out.println("partial content :" + new String(bytes));
        // }
        // System.out.println("content 1 :" + messageContent);

        // Description du document dans extrait:
        QueryResponse response = dataProvider.getQueryResponse();
        Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
        final String recordURL = record.getUrl();
        Map<String, List<String>> fieldsHighlighting = highlighting.get(recordURL);

        ConnectorInstance connectorInstance = record.getConnectorInstance();
        IndexField defaultSearchField = collection.getDefaultSearchIndexField();

        String excerpt = getExcerptFromHighlight(defaultSearchField.getName(), fieldsHighlighting);

        final ModalWindow detailsMailModal = new ModalWindow("detailsMailModal");
        detailsMailModal.setPageCreator(new PageCreator() {
            @Override
            public Page createPage() {
                return new BaseConstellioPage();
            }
        });
        add(detailsMailModal);
        detailsMailModal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        detailsMailModal.setCookieName("detailsMailModal");

        IModel modalTitleModel = subjectModel;
        detailsMailModal.setTitle(modalTitleModel);

        final String displayURL = (String) doc.getFieldValue(collection.getUrlIndexField().getName());

        emailModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
                Record record = recordServices.get(doc);
                
                List<String> attachmentNames = record.getMetaContents(MailConnectorConstants.META_ATTACHMENTNAME);
                List<String> attachmentTypes = record.getMetaContents(MailConnectorConstants.META_ATTACHMENTTYPE);
                List<String> recipients = record.getMetaContents(MailConnectorConstants.META_RECIPIENTS);
                List<String> flags = record.getMetaContents(MailConnectorConstants.META_FLAGS);
                List<String> froms = record.getMetaContents(MailConnectorConstants.META_SENDER);
                // FIXME Hardcoded
                List<String> contentEncoding = record.getMetaContents("Content-Encoding");

                List<String> receivedDateList = record.getMetaContents(MailConnectorConstants.META_RECEIVEDDATE);

                // FIXME voir avec Vincent : exemple qui ne marche pas jobboom car mail contient que du html
                RawContentServices rawContentServices = ConstellioSpringUtils.getRawContentServices();
                List<RawContent> rawContents = rawContentServices.getRawContents(record);
                ParsedContent parsedContents = record.getParsedContent();
                
                Email email;
                if (rawContents.size() != 0) {
                    byte[] content = rawContents.get(0).getContent();
                    String tmpFilePath = ClasspathUtils.getCollectionsRootDir() + File.separator + "tmp.eml";
                    try {
                        email = MailMessageUtils.toEmail(content, tmpFilePath);
                    } catch (Exception e) {
                        System.out.println("Error in reading content of mail");
                        // le contenu n'a pas bien été lu correctement
                        email = new Email();
                        email.setMessageContentText(parsedContents.getContent());
                    }
                } else {
                    // le contenu n'a pas bien été lu correctement
                    email = new Email();
                    email.setMessageContentText(parsedContents.getContent());
                }

                email.setFlags(flags);

                // email.setMessageContentHtml(messageContentHtml);
                if (!receivedDateList.isEmpty()) {
                    email.setReceivedDate(receivedDateList.get(0));
                }
                email.setRecipients(recipients);
                email.setSubject((String) subjectModel.getObject());
                email.setFroms(froms);
                // email.setLanguage(language);
                if (!contentEncoding.isEmpty()) {
                    email.setContentEncoding(contentEncoding.get(0));
                }
                return email;
            }
        };

        final RecordModel recordModel = new RecordModel(record);
        AjaxLink detailsMailLink = new AjaxLink("detailsMailLink") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                Email email = (Email) emailModel.getObject();
                detailsMailModal.setContent(new MailDetailsPanel(detailsMailModal.getContentId(), email));
                detailsMailModal.show(target);
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new AjaxCallDecorator() {
                    @Override
                    public CharSequence decorateScript(CharSequence script) {
                    	Record record = recordModel.getObject();
                        SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
                        WebRequest webRequest = (WebRequest) RequestCycle.get().getRequest();
                        HttpServletRequest httpRequest = webRequest.getHttpServletRequest();
                        return script + ";" + ComputeSearchResultClickServlet.getCallbackJavascript(httpRequest, simpleSearch, record);
                    }
                };
            }
        };
        add(detailsMailLink);
        
        IModel recipientsLabelModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
                Record record = recordServices.get(doc);
                List<String> recipients = record.getMetaContents(MailConnectorConstants.META_RECIPIENTS);
                return getLocalizer().getString("to", MailSearchResultPanel.this) + " : " + recipients;
            }
        };
        
        IModel receivedDateLabelModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
                Record record = recordServices.get(doc);
                List<String> receivedDateList = record.getMetaContents(MailConnectorConstants.META_RECEIVEDDATE);
                String receivedDate;
                if (!receivedDateList.isEmpty()) {
                    receivedDate = receivedDateList.get(0);
                } else {
                    receivedDate = "";
                }
                return getLocalizer().getString("date", MailSearchResultPanel.this) + " : " + receivedDate;
            }
        };

        Label subjectLabel = new Label("subject", subjectModel);
        detailsMailLink.add(subjectLabel.setEscapeModelStrings(false));

        Label excerptLbl = new Label("messageContent", excerpt);
        add(excerptLbl.setEscapeModelStrings(false));
        add(new Label("recipient", recipientsLabelModel) {
            @Override
            public boolean isVisible() {
                boolean visible = super.isVisible();
                if (visible) {
                	RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
                    RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
                    RecordCollection collection = collectionServices.get(collectionName);
                    Record record = recordServices.get(recordId, collection);
                    List<String> recipients = record.getMetaContents(MailConnectorConstants.META_RECIPIENTS);
                    visible = !recipients.isEmpty();
                }
                return visible;
            }
        });
        // add(new Label("from", getLocalizer().getString("from", this) + " : " + froms));
        add(new Label("date", receivedDateLabelModel));
        add(new WebMarkupContainer("hasAttachmentsImg") {
            @Override
            public boolean isVisible() {
                boolean visible = super.isVisible();
                if (visible) {
                    RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
                    Record record = recordServices.get(doc);
                    List<String> attachmentNames = record.getMetaContents(MailConnectorConstants.META_ATTACHMENTNAME);
                    visible = !attachmentNames.isEmpty();
                }
                return visible;
            }
        });

        final ReloadableEntityModel<RecordCollection> collectionModel = new ReloadableEntityModel<RecordCollection>(
            collection);
        add(new ListView("searchResultFields", new LoadableDetachableModel() {
            @Override
            protected Object load() {
                RecordCollection collection = collectionModel.getObject();
                return collection.getSearchResultFields();
            }

            /**
             * Detaches from the current request. Implement this method with custom behavior, such as
             * setting the model object to null.
             */
            protected void onDetach() {
                recordModel.detach();
                collectionModel.detach();
            }
        }) {
            @Override
            protected void populateItem(ListItem item) {
                SearchResultField searchResultField = (SearchResultField) item.getModelObject();
                IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
                Record record = recordModel.getObject();
                IndexField indexField = searchResultField.getIndexField();
                Locale locale = getLocale();
                String indexFieldTitle = indexField.getTitle(locale);
                if (StringUtils.isBlank(indexFieldTitle)) {
                    indexFieldTitle = indexField.getName();
                }
                StringBuffer fieldValueSb = new StringBuffer();
                List<Object> fieldValues = indexFieldServices.extractFieldValues(record, indexField);
                Map<String, String> defaultLabelledValues = indexFieldServices.getDefaultLabelledValues(
                    indexField, locale);
                for (Object fieldValue : fieldValues) {
                    if (fieldValueSb.length() > 0) {
                        fieldValueSb.append("\n");
                    }
                    String fieldValueLabel = indexField.getLabelledValue("" + fieldValue, locale);
                    if (fieldValueLabel == null) {
                        fieldValueLabel = defaultLabelledValues.get("" + fieldValue);
                    }
                    if (fieldValueLabel == null) {
                        fieldValueLabel = "" + fieldValue;
                    }
                    fieldValueSb.append(fieldValueLabel);
                }

                item.add(new Label("indexField", indexFieldTitle));
                item.add(new MultiLineLabel("indexFieldValue", fieldValueSb.toString()));
                item.setVisible(fieldValueSb.length() > 0);
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean isVisible() {
                boolean visible = super.isVisible();
                if (visible) {
                    List<SearchResultField> searchResultFields = (List<SearchResultField>) getModelObject();
                    visible = !searchResultFields.isEmpty();
                }
                return visible;
            }
        });

        add(new ElevatePanel("elevatePanel", record, dataProvider.getSimpleSearch()));
    }

    private String getExcerptFromHighlight(String defaultSearchFieldName,
        Map<String, List<String>> fieldsHighlighting) {
        String exerpt = "";
        if (fieldsHighlighting != null) {
            List<String> fieldHighlighting = fieldsHighlighting.get(defaultSearchFieldName);
            if (fieldHighlighting != null) {
                StringBuffer sb = new StringBuffer();
                for (String val : fieldHighlighting) {
                    sb.append(StringEscapeUtils.unescapeXml(val) + " ... ");
                }
                if (sb.length() > 0) {
                    exerpt = sb.toString().trim();
                }
            }
        }
        return exerpt;
    }

    @Override
    public void detachModels() {
        emailModel.detach();
        super.detachModels();
    }
    
}
