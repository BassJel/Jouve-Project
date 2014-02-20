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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.doculibre.constellio.connector.intelliGID.documentList.util.HttpClientHelper;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.entities.SearchResultField;
import com.doculibre.constellio.entities.constants.MetaConstants;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.intelligid.IntelliGIDServiceInfo;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.servlets.ComputeSearchResultClickServlet;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.ContextUrlUtils;
import com.doculibre.constellio.wicket.data.SearchResultsDataProvider;
import com.doculibre.constellio.wicket.models.RecordModel;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.pages.smb.SmbServletPage;
import com.doculibre.constellio.wicket.panels.elevate.ElevatePanel;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class DefaultSearchResultPanel extends Panel {

	private static final List<String> BROWSER_ACCEPTED_PROTOCOLS = Arrays.asList(new String[] { "http", "https", "ftp", "ftps", "file" });

	private static final String POPUP_LINK = "popupLink";

	private WebMarkupContainer toggleSummaryLink;
	private Label summaryLabel;

	public DefaultSearchResultPanel(String id, SolrDocument doc, final SearchResultsDataProvider dataProvider) {
		super(id);

		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
		SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();

		String collectionName = dataProvider.getSimpleSearch().getCollectionName();

		RecordCollection collection = collectionServices.get(collectionName);
		Record record = recordServices.get(doc);
		if (record != null) {
			SearchInterfaceConfig searchInterfaceConfig = searchInterfaceConfigServices.get();

			IndexField uniqueKeyField = collection.getUniqueKeyIndexField();
			IndexField defaultSearchField = collection.getDefaultSearchIndexField();
			IndexField urlField = collection.getUrlIndexField();
			IndexField titleField = collection.getTitleIndexField();

			if (urlField == null) {
				urlField = uniqueKeyField;
			}
			if (titleField == null) {
				titleField = urlField;
			}

			final String recordURL = record.getUrl();
			final String displayURL;

			if (record.getDisplayUrl().startsWith("/get?file=")) {
				HttpServletRequest req = ((WebRequest) getRequest()).getHttpServletRequest();
				displayURL = ContextUrlUtils.getContextUrl(req) + record.getDisplayUrl();

			} else {
				displayURL = record.getDisplayUrl();
			}

			String title = record.getDisplayTitle();

			final String protocol = StringUtils.substringBefore(displayURL, ":");
			boolean linkEnabled = isLinkEnabled(protocol);

			// récupération des champs highlighté à partir de la clé unique
			// du document, dans le cas de Nutch c'est l'URL
			QueryResponse response = dataProvider.getQueryResponse();
			Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
			Map<String, List<String>> fieldsHighlighting = highlighting.get(recordURL);

			String titleHighlight = getTitleFromHighlight(titleField.getName(), fieldsHighlighting);
			if (titleHighlight != null) {
				title = titleHighlight;
			}

			String excerpt = null;
			String description = getDescription(record);
			String summary = getSummary(record);

			if (StringUtils.isNotBlank(description) && searchInterfaceConfig.isDescriptionAsExcerpt()) {
				excerpt = description;
			} else {
				excerpt = getExcerptFromHighlight(defaultSearchField.getName(), fieldsHighlighting);
				if (excerpt == null) {
					excerpt = description;
				}
			}

			toggleSummaryLink = new WebMarkupContainer("toggleSummaryLink");
			add(toggleSummaryLink);
			toggleSummaryLink.setVisible(StringUtils.isNotBlank(summary));
			toggleSummaryLink.add(new AttributeModifier("onclick", new LoadableDetachableModel() {
				@Override
				protected Object load() {
					return "toggleSearchResultSummary('" + summaryLabel.getMarkupId() + "')";
				}
			}));

			summaryLabel = new Label("summary", summary);
			add(summaryLabel);
			summaryLabel.setOutputMarkupId(true);
			summaryLabel.setVisible(StringUtils.isNotBlank(summary));

			ExternalLink titleLink;
			if (displayURL.startsWith("file://")) {
				HttpServletRequest req = ((WebRequest) getRequest()).getHttpServletRequest();
				String newDisplayURL = ContextUrlUtils.getContextUrl(req) + "app/getSmbFile?" + SmbServletPage.RECORD_ID + "=" + record.getId() + "&" + SmbServletPage.COLLECTION
						+ "=" + collectionName;
				titleLink = new ExternalLink("titleLink", newDisplayURL);
			} else {
				titleLink = new ExternalLink("titleLink", displayURL);
			}

			final RecordModel recordModel = new RecordModel(record);
			AttributeModifier computeClickAttributeModifier = new AttributeModifier("onmousedown", true, new LoadableDetachableModel() {
				@Override
				protected Object load() {
					Record record = recordModel.getObject();
					SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
					WebRequest webRequest = (WebRequest) RequestCycle.get().getRequest();
					HttpServletRequest httpRequest = webRequest.getHttpServletRequest();
					return ComputeSearchResultClickServlet.getCallbackJavascript(httpRequest, simpleSearch, record);
				}
			});
			titleLink.add(computeClickAttributeModifier);
			titleLink.setEnabled(linkEnabled);

			boolean resultsInNewWindow;
			PageParameters params = RequestCycle.get().getPageParameters();
			if (params != null && params.getString(POPUP_LINK) != null) {
				resultsInNewWindow = params.getBoolean(POPUP_LINK);
			} else {
				resultsInNewWindow = searchInterfaceConfig.isResultsInNewWindow();
			}
			titleLink.add(new SimpleAttributeModifier("target", resultsInNewWindow ? "_blank" : "_self"));

			// Add title
			title = StringUtils.remove(title, "\n");
			title = StringUtils.remove(title, "\r");
			if (StringUtils.isEmpty(title)) {
				title = StringUtils.defaultString(displayURL);
				title = StringUtils.substringAfterLast(title, "/");
				title = StringUtils.substringBefore(title, "?");
				try {
					title = URLDecoder.decode(title, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (title.length() > 120) {
					title = title.substring(0, 120) + " ...";
				}
			}

			Label titleLabel = new Label("title", title);
			titleLink.add(titleLabel.setEscapeModelStrings(false));
			add(titleLink);

			Label excerptLabel = new Label("excerpt", excerpt);
			add(excerptLabel.setEscapeModelStrings(false));
			// add(new ExternalLink("url", url,
			// url).add(computeClickAttributeModifier).setEnabled(linkEnabled));
			if (displayURL.startsWith("file://")) {
				// Creates a Windows path for file URLs
				String urlLabel = StringUtils.substringAfter(displayURL, "file:");
				urlLabel = StringUtils.stripStart(urlLabel, "/");
				urlLabel = "\\\\" + StringUtils.replace(urlLabel, "/", "\\");
				try {
					urlLabel = URLDecoder.decode(urlLabel, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				add(new Label("url", urlLabel));
			} else {
				add(new Label("url", displayURL));
			}

			final ReloadableEntityModel<RecordCollection> collectionModel = new ReloadableEntityModel<RecordCollection>(collection);
			add(new ListView("searchResultFields", new LoadableDetachableModel() {
				@Override
				protected Object load() {
					RecordCollection collection = collectionModel.getObject();
					return collection.getSearchResultFields();
				}

				/**
				 * Detaches from the current request. Implement this method with
				 * custom behavior, such as setting the model object to null.
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
					Map<String, String> defaultLabelledValues = indexFieldServices.getDefaultLabelledValues(indexField, locale);
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

			// md5
			ConstellioSession session = ConstellioSession.get();
			ConstellioUser user = session.getUser();
			// TODO Provide access to unauthenticated users ?
			String md5 = "";
			if (user != null) {
				IntelliGIDServiceInfo intelligidServiceInfo = ConstellioSpringUtils.getIntelliGIDServiceInfo();
				if (intelligidServiceInfo != null) {
					Collection<Object> md5Coll = doc.getFieldValues(IndexField.MD5);
					if (md5Coll != null) {
						for (Object md5Obj : md5Coll) {
							try {
								String md5Str = new String(Hex.encodeHex(Base64.decodeBase64(md5Obj.toString())));
								InputStream is = HttpClientHelper.get(intelligidServiceInfo.getIntelligidUrl() + "/connector/checksum", "md5=" + URLEncoder.encode(md5Str, "ISO-8859-1"),
										"username=" + URLEncoder.encode(user.getUsername(), "ISO-8859-1"), "password=" + URLEncoder.encode(Base64.encodeBase64String(ConstellioSession.get().getPassword().getBytes())), "ISO-8859-1");
								try {
									Document xmlDocument = new SAXReader().read(is);
									Element root = xmlDocument.getRootElement();
									for (Iterator<Element> it = root.elementIterator("fichier"); it.hasNext();) {
										Element fichier = it.next();
										String url = fichier.attributeValue("url");
										md5 += "<a href=\"" + url + "\">" + url + "</a> ";
									}
								} finally {
									IOUtils.closeQuietly(is);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			Label md5Label = new Label("md5", md5) {
				public boolean isVisible() {
					boolean visible = super.isVisible();
					if (visible) {
						visible = StringUtils.isNotBlank(this.getModelObjectAsString());
					}
					return visible;
				}
			};
			md5Label.setEscapeModelStrings(false);
			add(md5Label);

			add(new ElevatePanel("elevatePanel", record, dataProvider.getSimpleSearch()));
		} else {
			setVisible(false);
		}
	}

	protected String getDescription(Record record) {
		return getFirstMetaContent(record, MetaConstants.DESCRIPTION);
	}

	protected String getSummary(Record record) {
		return getFirstMetaContent(record, MetaConstants.SUMMARY);
	}

	protected String getFirstMetaContent(Record record, String meta) {
		List<String> metas = record.getMetaContents(meta);
		return metas.isEmpty() ? null : metas.get(0);
	}

	private String getTitleFromHighlight(String titleFieldName, Map<String, List<String>> fieldsHighlighting) {
		String title = null;
		if (fieldsHighlighting != null) {
			List<String> fieldHighlighting = fieldsHighlighting.get(titleFieldName);
			if (fieldHighlighting != null) {
				StringBuffer sb = new StringBuffer();
				for (String val : fieldHighlighting) {
					val = val.replace(StringEscapeUtils.escapeHtml("<em>"), "<em>");
					val = val.replace(StringEscapeUtils.escapeHtml("</em>"), "</em>");
					val = val.replace(StringEscapeUtils.escapeHtml("<sup>"), "<sup>");
					val = val.replace(StringEscapeUtils.escapeHtml("</sup>"), "</sup>");
					sb.append(val + " ");
				}
				if (sb.length() > 0) {
					title = sb.toString().trim();
				}
			}
		}
		return title;
	}

	private String getExcerptFromHighlight(String defaultSearchFieldName, Map<String, List<String>> fieldsHighlighting) {
		String exerpt = null;
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

	protected boolean isLinkEnabled(String protocol) {
		return BROWSER_ACCEPTED_PROTOCOLS.contains(protocol) || protocol.startsWith("/get?file=");
	}

}
