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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.http.WebRequest;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RawContent;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.RawContentServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.servlets.ComputeSearchResultClickServlet;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.data.SearchResultsDataProvider;
import com.doculibre.constellio.wicket.models.RecordModel;
import com.doculibre.constellio.wicket.panels.elevate.ElevatePanel;

@SuppressWarnings("serial")
public class PopupSearchResultPanel extends Panel {

    // private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    public PopupSearchResultPanel(String id, SolrDocument doc, final SearchResultsDataProvider dataProvider) {
        super(id);

        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        String solrServerName = dataProvider.getSimpleSearch().getCollectionName();
        RecordCollection collection = collectionServices.get(solrServerName);

        IndexField uniqueKeyField = collection.getUniqueKeyIndexField();
        IndexField defaultSearchField = collection.getDefaultSearchIndexField();
        IndexField urlField = collection.getUrlIndexField();
        IndexField titleField = collection.getTitleIndexField();

        // title
        String documentID = (String) getFieldValue(doc, uniqueKeyField.getName());

        String documentTitle = (String) getFieldValue(doc, titleField.getName());

        if (StringUtils.isBlank(documentTitle)) {
            if (urlField == null) {
                documentTitle = (String) getFieldValue(doc, uniqueKeyField.getName());
            } else {
                documentTitle = (String) getFieldValue(doc, urlField.getName());
            }
        }
        if (documentTitle.length() > 120) {
            documentTitle = documentTitle.substring(0, 120) + " ...";
        }

        // content
        RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
        Record record = recordServices.get(doc);

        RawContentServices rawContentServices = ConstellioSpringUtils.getRawContentServices();
        List<RawContent> rawContents = rawContentServices.getRawContents(record);
        StringBuilder content = new StringBuilder();
        for (RawContent raw : rawContents) {
            byte[] bytes = raw.getContent();
            content.append(new String(bytes));
        }

        String documentContent = content.toString();

        // date
        String documentLastModified = getFieldValue(doc, IndexField.LAST_MODIFIED_FIELD);

        // Description du document dans extrait:
        QueryResponse response = dataProvider.getQueryResponse();
        Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
        final String recordURL = record.getUrl();
        Map<String, List<String>> fieldsHighlighting = highlighting.get(recordURL);

        String extrait = getExcerptFromHighlight(defaultSearchField.getName(), fieldsHighlighting);

        final ModalWindow detailsDocumentModal = new ModalWindow("detailsDocumentModal");
        add(detailsDocumentModal);
        detailsDocumentModal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);

        detailsDocumentModal.setContent(new PopupDetailsPanel(detailsDocumentModal.getContentId(),
            documentContent, documentLastModified));
        detailsDocumentModal.setCookieName("detailsDocumentModal");

        String modalTitle = documentTitle;
        detailsDocumentModal.setTitle(modalTitle);

        AjaxLink detailsDocumentLink = new AjaxLink("detailsDocumentLink") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                detailsDocumentModal.show(target);
            }
        };
        add(detailsDocumentLink);

        final RecordModel recordModel = new RecordModel(record);
        AttributeAppender computeClickAttributeModifier = new AttributeAppender("onclick", true,
            new LoadableDetachableModel() {
                @Override
                protected Object load() {
                	Record record = recordModel.getObject();
                    SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
                    WebRequest webRequest = (WebRequest) RequestCycle.get().getRequest();
                    HttpServletRequest httpRequest = webRequest.getHttpServletRequest();
                    return ComputeSearchResultClickServlet.getCallbackJavascript(httpRequest, simpleSearch, record);
                }
                
                @Override
                protected void onDetach() {
                	recordModel.detach();
                	super.onDetach();
                }
            }, ";") {
            @Override
            protected String newValue(String currentValue, String appendValue) {
                return appendValue + currentValue;
            }
        };
        detailsDocumentLink.add(computeClickAttributeModifier);

        Label subjectLabel = new Label("subject", documentTitle);
        detailsDocumentLink.add(subjectLabel.setEscapeModelStrings(false));

        Label extraitLbl = new Label("documentContent", extrait);
        add(extraitLbl.setEscapeModelStrings(false));
        add(new Label("date", "Date : " + documentLastModified));

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

    /**
     * Will never return null
     * 
     * @param doc
     * @param fieldName
     * @return
     */
    private String getFieldValue(SolrDocument doc, String fieldName) {
        Collection<Object> values = doc.getFieldValues(fieldName);
        if (values == null) {
            return "";
        }
        return StringUtils.join(values.toArray(), ", ");
    }

}
