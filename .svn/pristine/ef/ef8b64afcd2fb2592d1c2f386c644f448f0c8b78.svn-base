package com.doculibre.constellio.wicket.panels.admin.stats;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.StatsServices;
import com.doculibre.constellio.stats.report.StatsConstants;
import com.doculibre.constellio.stats.report.StatsReport;
import com.doculibre.constellio.stats.report.StatsReport.StatsReportColumn;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.components.links.DownloadInputStreamLink;
import com.doculibre.constellio.wicket.panels.search.query.SimpleSearchQueryPanel;

@SuppressWarnings("serial")
public class CollectionStatsReportPanel extends Panel {

	private String collectionName;
	private String reportType;
	private Date startDate;
	private Date endDate;
	private int rows;
	private boolean includeFederatedCollections;

	public CollectionStatsReportPanel(String id, String collectionName, String reportType, Date startDate,
			Date endDate, int rows, boolean includeFederatedCollections) {
		super(id);
		this.collectionName = collectionName;
		this.reportType = reportType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.rows = rows;
		this.includeFederatedCollections = includeFederatedCollections;
		initComponents();
	}

	private void initComponents() {
		final IModel solrDocumentListModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				SolrDocumentList list = null;
				try {
					list = getList();
				} catch (SolrServerException e) {
					throw new WicketRuntimeException(e);
				}
				return list;
			}
		};

		final StatsReport statsReport = new StatsReport(collectionName, reportType, startDate, endDate, includeFederatedCollections);
		final IModel reportXLSBytesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				SolrDocumentList solrDocumentList = (SolrDocumentList) solrDocumentListModel.getObject();
				StringResourceModel reportTypeLabelModel = new StringResourceModel("statsType." + reportType,
						findParent(CollectionStatsPanel.class), null);
				String reportTitle = (String) reportTypeLabelModel.getObject();
				statsReport.writeXLS(baos, solrDocumentList, reportTitle, getLocale());
				return baos.toByteArray();
			}
		};
		IModel inputStreamModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				byte[] reportBytes = (byte[]) reportXLSBytesModel.getObject();
				return new ByteArrayInputStream(reportBytes);
			}

			@Override
			protected void onDetach() {
				reportXLSBytesModel.detach();
				super.onDetach();
			}
		};
		IModel fileNameModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				StringResourceModel reportTypeLabelModel = new StringResourceModel("statsType." + reportType,
						findParent(CollectionStatsPanel.class), null);
				StringBuffer reportName = new StringBuffer((String) reportTypeLabelModel.getObject());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				reportName.append("_");
				reportName.append(sdf.format(startDate));
				reportName.append("_");
				reportName.append(sdf.format(endDate));
				reportName.append(".xls");
				return reportName.toString();
			}
		};
		IModel lengthModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				byte[] reportBytes = (byte[]) reportXLSBytesModel.getObject();
				return reportBytes.length;
			}
		};
		add(new DownloadInputStreamLink("downloadLink", inputStreamModel, fileNameModel, new Model(
				"application/ms-excel"), lengthModel, new Model(new Date())));

		add(new ListView("columnTitles", statsReport.getColumns(false)) {
			@Override
			protected void populateItem(ListItem item) {
				StatsReportColumn column = (StatsReportColumn) item.getModelObject();
				String columnTitle = column.getLabel(getLocale());
				item.add(new Label("title", columnTitle));
			}
		});

		add(new ListView("lines", solrDocumentListModel) {
			@Override
			protected void populateItem(ListItem item) {
				final SolrDocument solrDocument = (SolrDocument) item.getModelObject();
				item.add(new ListView("columns", statsReport.getColumns(false)) {
					@Override
					protected void populateItem(ListItem item) {
						StatsReportColumn column = (StatsReportColumn) item.getModelObject();
						if (!StatsConstants.DATA_QUERY.equals(column.getColumnData())) {
							String value = column.getValue(solrDocument, getLocale());
							item.add(new MultiLineLabel("value", value));
						} else {
							String queryWithParams = (String) solrDocument.getFieldValue(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH);
							SimpleSearch simpleSearch = SimpleSearch.toSimpleSearch(queryWithParams);
							item.add(new SimpleSearchQueryPanel("value", simpleSearch));
						}
					}
				});
			}
		});
	}

	private SolrDocumentList getList() throws SolrServerException {
		SolrDocumentList result;
		StatsServices statsServices = ConstellioSpringUtils.getStatsServices();
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = collectionServices.get(collectionName);
		if (reportType.equals(StatsConstants.REQUEST_LOG)) {
			result = statsServices.getQueries(collection, startDate, endDate, includeFederatedCollections, rows);
		} else if (reportType.equals(StatsConstants.MOST_POPULAR_REQUEST)) {
			result = statsServices.getMostPopularQueries(collection, startDate, endDate, includeFederatedCollections, rows);
		} else if (reportType.equals(StatsConstants.MOST_POPULAR_REQUEST_WITH_RESULTS)) {
			result = statsServices.getMostPopularQueriesWithResults(collection, startDate, endDate, includeFederatedCollections, rows);
		} else if (reportType.equals(StatsConstants.MOST_POPULAR_REQUEST_WITHOUT_RESULTS)) {
			result = statsServices.getMostPopularQueriesWithoutResults(collection, startDate, endDate, includeFederatedCollections, rows);
		} else if (reportType.equals(StatsConstants.MOST_POPULAR_REQUEST_WITHOUT_MOUSE_CLICK)) {
			result = statsServices.getMostPopularQueriesWithoutClick(collection, startDate, endDate, includeFederatedCollections, rows);
		} else if (reportType.equals(StatsConstants.MOST_POPULAR_REQUEST_WITH_MOUSE_CLICK)) {
			result = statsServices.getMostPopularQueriesWithClick(collection, startDate, endDate, includeFederatedCollections, rows);
		} else {
			result = new SolrDocumentList();
		}
		return result;
	}

}
