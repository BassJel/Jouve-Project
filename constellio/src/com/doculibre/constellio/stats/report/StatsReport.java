package com.doculibre.constellio.stats.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.FacetValue;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SearchedFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.StatsServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.SimpleParams;
import com.ibm.icu.util.Calendar;

@SuppressWarnings("serial")
public class StatsReport implements Serializable {
	
	private String collectionName;
	private String reportType;
	private Date startDate;
	private Date endDate;
	private boolean includeFederatedCollections;
	
	public StatsReport(String collectionName, String reportType, Date startDate, Date endDate, boolean includeFederatedCollections) {
		this.collectionName = collectionName;
		this.reportType = reportType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.includeFederatedCollections = includeFederatedCollections;
	}

	public List<StatsReportColumn> getColumns(boolean excel) {
		List<String> columnDataElements = new ArrayList<String>();
        if (reportType.equals(StatsConstants.REQUEST_LOG)) {
        	columnDataElements.addAll(StatsConstants.DATA_REQUEST_LOG);
        } else if (reportType.equals(StatsConstants.MOST_POPULAR_REQUEST)) {
        	columnDataElements.addAll(StatsConstants.DATA_MOST_POPULAR_REQUEST);
        } else if (reportType.equals(StatsConstants.MOST_POPULAR_REQUEST_WITH_RESULTS)) {
        	columnDataElements.addAll(StatsConstants.DATA_MOST_POPULAR_REQUEST_WITH_RESULTS);
        } else if (reportType.equals(StatsConstants.MOST_POPULAR_REQUEST_WITHOUT_RESULTS)) {
        	columnDataElements.addAll(StatsConstants.DATA_MOST_POPULAR_REQUEST_WITHOUT_RESULTS);
        } else if (reportType.equals(StatsConstants.MOST_POPULAR_REQUEST_WITHOUT_MOUSE_CLICK)) {
        	columnDataElements.addAll(StatsConstants.DATA_MOST_POPULAR_REQUEST_WITHOUT_MOUSE_CLICK);
        } else if (reportType.equals(StatsConstants.MOST_POPULAR_REQUEST_WITH_MOUSE_CLICK)) {
        	columnDataElements.addAll(StatsConstants.DATA_MOST_POPULAR_REQUEST_WITH_MOUSE_CLICK);
        }
    	RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
    	RecordCollection collection = collectionServices.get(collectionName);
    	if (!includeFederatedCollections || !collection.isFederationOwner()) {
        	columnDataElements.remove(StatsConstants.DATA_COLLECTION);
    	}
        
        List<StatsReportColumn> reportColumns = new ArrayList<StatsReportColumn>();
		for (String columnDataElement : columnDataElements) {
            List<StatsReportColumn> dataElementColumns = newStatsReportColumns(collectionName, columnDataElement, excel);
            reportColumns.addAll(dataElementColumns);
        }
		return reportColumns;
	}

	private List<StatsReportColumn> newStatsReportColumns(String collectionName, String columnData, boolean excel) {
		List<StatsReportColumn> columns = new ArrayList<StatsReportColumn>();
		if (StatsConstants.DATA_DATE.equals(columnData)) {
			if (excel) {
				columns.add(new DateSplitReportColumn(columnData, "day", Calendar.DAY_OF_MONTH));
				columns.add(new DateSplitReportColumn(columnData, "month", Calendar.MONTH));
				columns.add(new DateSplitReportColumn(columnData, "year", Calendar.YEAR));
				columns.add(new DateSplitReportColumn(columnData, "hour", Calendar.HOUR_OF_DAY));
			} else {
				columns.add(new SimpleReportColumn(columnData, "date", StatsConstants.INDEX_FIELD_SEARCH_DATE, Date.class));
			}
		} else if (StatsConstants.DATA_QUERY.equals(columnData)) {
			if (excel) {
				columns.add(new QuerySplitReportColumn(columnData, "query", QuerySplitReportColumn.QUERY));
				columns.add(new QuerySplitReportColumn(columnData, "facets", QuerySplitReportColumn.FACETS));
				columns.add(new QuerySplitReportColumn(columnData, "language", QuerySplitReportColumn.LANGUAGE));
				columns.add(new QuerySplitReportColumn(columnData, "searchType", QuerySplitReportColumn.SEARCH_TYPE));
			} else {
				columns.add(new SimpleReportColumn(columnData, "query", StatsConstants.INDEX_FIELD_SIMPLE_SEARCH, String.class));
			}
		} else if (StatsConstants.DATA_COLLECTION.equals(columnData)) {
			columns.add(new CollectionReportColumn(columnData, "collection"));
		} else if (StatsConstants.DATA_NUMBER_RESULTS.equals(columnData)) {
			columns.add(new SimpleReportColumn(columnData, "nbResults", StatsConstants.INDEX_FIELD_NUM_FOUND, Integer.class));
		} else if (StatsConstants.DATA_PAGE.equals(columnData)) {
			columns.add(new PageReportColumn(columnData, "page"));
		} else if (StatsConstants.DATA_RESPONSE_TIME.equals(columnData)) {
			columns.add(new SimpleReportColumn(columnData, "responseTime", StatsConstants.INDEX_FIELD_RESPONSE_TIME, Integer.class));
		} else if (StatsConstants.DATA_FREQUENCY.equals(columnData)) {
			columns.add(new SimpleReportColumn(columnData, "frequency", StatsConstants.INDEX_FIELD_SEARCH_COUNT, Integer.class));
		} else if (StatsConstants.DATA_NUMBER_CLICKED.equals(columnData)) {
			columns.add(new NbClickedReportColumn(columnData, "nbClicked"));
		} else if (StatsConstants.DATA_BEST_CLICKED.equals(columnData)) {
			columns.add(new BestClickedReportColumn(columnData, "bestClicked"));
		}
		return columns;
	}
	
	public void writeXLS(OutputStream xlsOutputStream, SolrDocumentList solrDocumentList, String reportTitle, Locale locale) {
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(xlsOutputStream);
			WritableSheet reportSheet = workbook.createSheet(reportTitle, 0);
			
			WritableCellFormat baseCellFormat = new WritableCellFormat();
			baseCellFormat.setWrap(true);
			
			CellView baseColumnView = new CellView();
			baseColumnView.setAutosize(true);
			baseColumnView.setFormat(baseCellFormat);
			
			List<StatsReportColumn> reportColumns = getColumns(true);
			
			int[] columnSizes = new int[reportColumns.size()];
			for (int i = 0; i < columnSizes.length; i++) {
				columnSizes[i] = -1;
			}
			
			final int columns = columnSizes.length;
			for (int i = 0; i < columns; i++) {
				CellView columnView = new CellView(baseColumnView);
				if (columnSizes[i] != -1) {
					columnView.setAutosize(false);
					columnView.setSize(columnSizes[i] * 256);
				}
				reportSheet.setColumnView(i, columnView);
			}
			
			WritableCellFormat headerFormat = new WritableCellFormat(baseCellFormat);
			headerFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
			headerFormat.setVerticalAlignment(VerticalAlignment.TOP);
			WritableFont headerFont = new WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE, WritableFont.BOLD);
			headerFormat.setFont(headerFont);

			int column = 0;
			for (StatsReportColumn reportColumn : reportColumns) {
				String columnTitle = reportColumn.getLabel(locale);
				reportSheet.addCell(new Label(column++, 0, columnTitle, headerFormat));
			}
			
			int row = 1;
			for (SolrDocument solrDocument : solrDocumentList) {
				WritableCellFormat cellFormat = new WritableCellFormat(baseCellFormat);
				
				column = 0;
				WritableCell cell;

				for (StatsReportColumn reportColumn : reportColumns) {
					String value = reportColumn.getValue(solrDocument, locale);
					Class<? extends Object> type = reportColumn.getType();
					if (Number.class.isAssignableFrom(type)) {
						try {
							cell = new jxl.write.Number(column, row, Double.valueOf(value), cellFormat);
						} catch (Exception e) {
							cell = new Label(column, row, value, cellFormat);
						}
						column++;
					} else if (Date.class.equals(type)) {
						try {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
							cell = new DateTime(column, row, sdf.parse(value), cellFormat);
						} catch (Exception e) {
							cell = new Label(column, row, value, cellFormat);
						}
						column++;
					} else {
						cell = new Label(column++, row, value, cellFormat);
					}
					reportSheet.addCell(cell);
				}
				row++;
			}

			workbook.write();
			workbook.close();
			
		} catch (RowsExceededException e) {
			throw new RuntimeException(e);
		} catch (WriteException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
    
    private static String getLabel(String key, Locale locale) {
        String label = null;
        List<ResourceBundle> bundles = findBundles(locale);
        for (ResourceBundle bundle : bundles) {
            try {
                label = bundle.getString(key);
                if (label != null) {
                    break;
                }
            } catch (MissingResourceException e) {
                // Ignore exception
            }
        }
        return label;
    }

    private static List<ResourceBundle> findBundles(Locale locale) {
        List<ResourceBundle> bundles = new ArrayList<ResourceBundle>();
        Class<? extends Object> currentClass = StatsReport.class;
        while (currentClass != Object.class) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(currentClass.getName(), locale);
                if (bundle != null) {
                    bundles.add(bundle);
                } 
            } catch (MissingResourceException e) {
                //Ignore error
            }
            currentClass = currentClass.getSuperclass();
        }
        return bundles;
    }
	
	public abstract class StatsReportColumn implements Serializable {
		
		private String columnData;
		private String bundleKey;
		private Class<? extends Object> type;

		public StatsReportColumn(String columnData, String bundleKey, Class<? extends Object> type) {
			this.columnData = columnData;
			this.bundleKey = bundleKey;
			this.type = type;
		}

		public String getColumnData() {
			return columnData;
		}
		
		public Class<? extends Object> getType() {
			return type;
		}

		public String getLabel(Locale locale) {
			return StatsReport.getLabel(bundleKey, locale);
		}

		public abstract String getValue(SolrDocument doc, Locale locale);
		
	}

	private class SimpleReportColumn extends StatsReportColumn {

		private String fieldName;
		
		public SimpleReportColumn(String columnData, String bundleKey, String fieldName, Class<? extends Object> type) {
			super(columnData, bundleKey, type);
			this.fieldName = fieldName;
		}
		
		@Override
		public String getValue(SolrDocument doc, Locale displayLocale) {
			String result;
			Object fieldValue = doc.getFieldValue(fieldName);
			if (fieldValue instanceof Date) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				result = sdf.format((Date) fieldValue);
			} else {
				result = fieldValue != null ? fieldValue.toString() : "";
			}
			return result;
		}
		
	}
	
	private class CollectionReportColumn extends StatsReportColumn {

		public CollectionReportColumn(String columnData, String bundleKey) {
			super(columnData, bundleKey, String.class);
		}

		@Override
		public String getValue(SolrDocument doc, Locale locale) {
			String collectionName = (String) doc.getFieldValue(StatsConstants.INDEX_FIELD_COLLECTION_NAME);
        	RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        	RecordCollection collection = collectionServices.get(collectionName);
			return collection != null ? collection.getTitle(locale) : collectionName;
		}
		
	}
	
	private class PageReportColumn extends StatsReportColumn {

		public PageReportColumn(String columnData, String bundleKey) {
			super(columnData, bundleKey, Integer.class);
		}

		@Override
		public String getValue(SolrDocument doc, Locale locale) {
			int page;
			Object pageValue = doc.getFieldValue(StatsConstants.INDEX_FIELD_SEARCH_PAGE);
			if (pageValue != null && StringUtils.isNotBlank(pageValue.toString())) {
				page = Integer.parseInt(pageValue.toString());
			} else {
				String queryWithParams = (String) doc.getFieldValue(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH);
				SimpleParams params = new SimpleParams();
				params.parse(queryWithParams);
				SimpleSearch simpleSearch = SimpleSearch.toSimpleSearch(params);
				page = simpleSearch.getPage();
			}
			return "" + (page + 1);
		}
		
	}

	private class DateSplitReportColumn extends StatsReportColumn {

		private int calendarField;
		
		public DateSplitReportColumn(String columnData, String bundleKey, int calendarField) {
			super(columnData, bundleKey, Integer.class);
			this.calendarField = calendarField;
		}
		
		@Override
		public String getValue(SolrDocument doc, Locale displayLocale) {
			String value;
			Date searchDate = (Date) doc.getFieldValue(StatsConstants.INDEX_FIELD_SEARCH_DATE);
			Calendar cal = Calendar.getInstance();
			cal.clear();
			cal.setTime(searchDate);
			if (calendarField == Calendar.HOUR_OF_DAY) {
				SimpleDateFormat hourSDF = new SimpleDateFormat("HH:mm");
				value = hourSDF.format(searchDate);
			} else if (calendarField == Calendar.MONTH){
				value = "" + (cal.get(calendarField) + 1);
			} else {
				value = "" + cal.get(calendarField);
			}
	        return value;
		}
		
	}

	private class QuerySplitReportColumn extends StatsReportColumn {
		
		private static final int QUERY = 0;
		private static final int FACETS = 1;
		private static final int LANGUAGE = 2;
		private static final int SEARCH_TYPE = 3;

		private int subType;
		
		public QuerySplitReportColumn(String columnData, String bundleKey, int subType) {
			super(columnData, bundleKey, String.class);
			this.subType = subType;
		}

		@Override
		public String getValue(SolrDocument doc, Locale displayLocale) {
			String value;
			String queryWithParams = (String) doc.getFieldValue(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH);
			SimpleParams params = new SimpleParams();
			params.parse(queryWithParams);
			SimpleSearch simpleSearch = SimpleSearch.toSimpleSearch(params);
			if (subType == QUERY) {
				value = simpleSearch.getQuery();
			} else if (subType == SEARCH_TYPE) {
				value = getSearchTypeValue(simpleSearch, displayLocale);
			} else if (subType == FACETS) {
				value = getFacetsValue(simpleSearch, displayLocale);
			} else {
				value = getLanguageValue(simpleSearch, displayLocale);
			}
			return value;
		}
		
		private String getFacetsValue(SimpleSearch simpleSearch, Locale displayLocale) {
			StringBuffer valueSB = new StringBuffer();
			
            List<FacetValue> facetIncludedValues = new ArrayList<FacetValue>();
            List<SearchedFacet> searchedFacets = simpleSearch.getSearchedFacets();
            for (SearchedFacet searchedFacet : searchedFacets) {
                SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
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
            }
            
            for (FacetValue facetValue : facetIncludedValues) {
                StringBuffer facetValueLabel = new StringBuffer();
                facetValueLabel.append(facetValue.getSearchableFacet().getLabels().get(displayLocale));
                facetValueLabel.append(" : ");
                facetValueLabel.append(facetValue.getLabel(displayLocale));
                
                if (valueSB.length() > 0) {
                	valueSB.append("\n");
                }
                valueSB.append(facetValueLabel);
            }

            List<FacetValue> excludedFacetValues = new ArrayList<FacetValue>();
            for (SearchedFacet searchedFacet : searchedFacets) {
                SearchableFacet searchableFacet = searchedFacet.getSearchableFacet();
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
            }
            
            for (FacetValue facetValue : excludedFacetValues) {
                StringBuffer facetValueLabel = new StringBuffer();
                facetValueLabel.append(facetValue.getSearchableFacet().getLabels().get(displayLocale));
                facetValueLabel.append(" (" + StatsReport.getLabel("excludedFacet", displayLocale) + ")");
                facetValueLabel.append(" : ");
                facetValueLabel.append(facetValue.getLabel(displayLocale));
                
                if (valueSB.length() > 0) {
                	valueSB.append("\n");
                }
                valueSB.append(facetValueLabel);
            }
            
            return valueSB.toString();
		}
		
		private String getLanguageValue(SimpleSearch simpleSearch, Locale displayLocale) {
			String value;
			Locale singleSearchLocale = simpleSearch.getSingleSearchLocale();
			if (singleSearchLocale != null) {
				value = StringUtils.capitalize(singleSearchLocale.getDisplayLanguage(displayLocale));
			} else {
				StringBuffer valueSB = new StringBuffer();
				SearchedFacet languageFacet = simpleSearch.getSearchedFacet(IndexField.LANGUAGE_FIELD);
				if (languageFacet != null) {
					for (String language : languageFacet.getIncludedValues()) {
						Locale languageLocale = new Locale(language);
		                if (valueSB.length() > 0) {
		                	valueSB.append("\n");
		                }
						valueSB.append(StringUtils.capitalize(languageLocale.getDisplayLanguage(displayLocale)));
					}
				}
				value = valueSB.toString();
			}		
			return value;
		}
		
		private String getSearchTypeValue(SimpleSearch simpleSearch, Locale locale) {
			return StatsReport.getLabel("searchType." + simpleSearch.getSearchType(), locale);
		}
		
	}

	public class BestClickedReportColumn extends StatsReportColumn {

		public BestClickedReportColumn(String columnData, String bundleKey) {
			super(columnData, bundleKey, String.class);
		}

		@Override
		public String getValue(SolrDocument doc, Locale locale) {
			String value = " ";
			StatsServices statsServices = ConstellioSpringUtils.getStatsServices();
			String queryWithParams = (String) doc.getFieldValue(StatsConstants.INDEX_FIELD_SIMPLE_SEARCH);
			SimpleParams params = new SimpleParams();
			params.parse(queryWithParams);
			SimpleSearch simpleSearch = SimpleSearch.toSimpleSearch(params);
			SolrDocument mostClickedDoc = statsServices.getMostClickedDocument(simpleSearch, startDate, endDate, false);
			if (mostClickedDoc != null) {
				value = (String) mostClickedDoc.getFieldValue(StatsConstants.INDEX_FIELD_DISPLAY_URL);
			} else {
				value = "";
			}
	        return value;
		}
	}

	public class NbClickedReportColumn extends StatsReportColumn {

		public NbClickedReportColumn(String columnData, String bundleKey) {
			super(columnData, bundleKey, String.class);
		}

		@Override
		public String getValue(SolrDocument doc, Locale locale) {
			String value;
			Object clickCountObj = doc.getFieldValue(StatsConstants.INDEX_FIELD_CLICK_COUNT);
			if (clickCountObj != null && StringUtils.isNotBlank(clickCountObj.toString())) {
				try {
					value = "" + Integer.parseInt(clickCountObj.toString());
				} catch (Exception e) {
					value = "";
				}
			} else {
				StatsServices statsServices = ConstellioSpringUtils.getStatsServices();
				RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
				RecordCollection collection = collectionServices.get(collectionName);
				String searchLogDocId = (String) doc.getFieldValue(StatsConstants.INDEX_FIELD_ID);
				value = "" + statsServices.getNbClicks(collection, searchLogDocId);
			}
	        return value;
		}
	}

}
