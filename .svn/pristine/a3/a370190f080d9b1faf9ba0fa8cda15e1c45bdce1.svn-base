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
package com.doculibre.constellio.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.RecordMeta;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.ProgressInfo;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;

public class ImportExportServicesImpl implements ImportExportServices {

	@SuppressWarnings("unchecked")
	@Override
	public void importData(Directory directory, RecordCollection collection, ProgressInfo progressInfo) {
		try {
			ConnectorInstance connectorInstance = collection.getConnectorInstances().iterator().next();
			RecordServices recordServices = ConstellioSpringUtils.getRecordServices();

			String uniqueKeyMetaName = null;
			IndexField uniqueKeyIndexField = collection.getUniqueKeyIndexField();
			for (ConnectorInstanceMeta connectorInstanceMeta : uniqueKeyIndexField.getConnectorInstanceMetas()) {
				if (connectorInstance.equals(connectorInstanceMeta.getConnectorInstance())) {
					uniqueKeyMetaName = connectorInstanceMeta.getName();
					break;
				}
			}

			Pattern invalidDatePattern = Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]*)?$");

			IndexReader indexReader = DirectoryReader.open(directory);
			if (progressInfo != null) {
				progressInfo.setTotal(indexReader.numDocs());
			}
			for (int i = 0; i < indexReader.numDocs(); i++) {
				Document document = indexReader.document(i);

				Record record = new Record();
				record.setLastModified(new Date());
				record.setConnectorInstance(connectorInstance);

				for (IndexableField field : document.getFields()) {
					// for (String fieldName : (Collection<String>)
					// indexReader.getFieldNames(FieldOption.ALL)) {
					if (field != null && field.fieldType().stored() && field.binaryValue() == null) {
						String metaName = field.name();
						String metaContent = field.stringValue();

						Matcher invalidDateMatcher = invalidDatePattern.matcher(metaContent);
						if (invalidDateMatcher.matches()) {
							metaContent = metaContent + "Z";
						}

						if (uniqueKeyMetaName.equals(metaName)) {
							record.setUrl(metaContent);
						}

						RecordMeta meta = new RecordMeta();
						ConnectorInstanceMeta connectorInstanceMeta = connectorInstance.getOrCreateMeta(metaName);
						meta.setConnectorInstanceMeta(connectorInstanceMeta);
						meta.setRecord(record);
						meta.setContent(metaContent);
						record.addContentMeta(meta);
					}
				}

				try {
					recordServices.makePersistent(record);

					// if (i % 500 == 0) {
					// EntityManager entityManager =
					// ConstellioPersistenceContext.getCurrentEntityManager();
					// entityManager.getTransaction().commit();
					// entityManager.getTransaction().begin();
					// }
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (progressInfo != null) {
					progressInfo.setCurrentIndex(i);
				}
			}

			indexReader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void importData(Workbook workbook, RecordCollection collection, ProgressInfo progressInfo) {
		ConnectorInstance connectorInstance = collection.getConnectorInstances().iterator().next();
		RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
		Sheet sheet = workbook.getSheet(0);

		if (progressInfo != null) {
			progressInfo.setTotal(sheet.getRows());
		}

		List<String> metaNames = new ArrayList<String>();
		for (int column = 0; column < sheet.getColumns(); column++) {
			Cell cell = sheet.getCell(column, 0);
			String metaName = cell.getContents();
			metaNames.add(metaName);
		}

		for (int row = 1; row < sheet.getRows(); row++) {
			Record record = new Record();
			record.setConnectorInstance(connectorInstance);
			for (int column = 0; column < sheet.getColumns(); column++) {
				Cell cell = sheet.getCell(column, row);
				RecordMeta meta = new RecordMeta();
				meta.setRecord(record);
				record.getContentMetas().add(meta);
				String metaName = metaNames.get(column);
				String metaContent = cell.getContents();
				ConnectorInstanceMeta connectorInstanceMeta = connectorInstance.getOrCreateMeta(metaName);
				meta.setConnectorInstanceMeta(connectorInstanceMeta);
				meta.setContent(metaContent);
			}

			Record existingRecord = recordServices.get(record.getUrl(), connectorInstance.getRecordCollection());
			if (existingRecord == null) {
				recordServices.makePersistent(record);
			}
			EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
			entityManager.flush();

			if (progressInfo != null) {
				progressInfo.setCurrentIndex(row - 1);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void convertData(Directory directory, OutputStream output) {
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(output);
			WritableSheet sheet = workbook.createSheet("fields", 0);

			WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
			WritableCellFormat arial10format = new WritableCellFormat(arial10font);
			IndexReader indexReader = DirectoryReader.open(directory);

//			{
//			int column = 0;
//			for (String fieldName : (Collection<String>) indexReader.getFieldNames()) {
//				Label label = new Label(column, 0, fieldName, arial10format);
//				sheet.addCell(label);
//				column++;
//			}
//			}

			int row = 1;
			for (int i = 0; i < indexReader.numDocs() /* && i != 502 */; i++) {
				Document document = indexReader.document(i);
				int column = 0;
				for (IndexableField field : document.getFields()) {
					if(row==1){
						Label label = new Label(column, 0, field.name(), arial10format);
						sheet.addCell(label);
					}
					
					if (field != null && field.fieldType().stored() && field.binaryValue() == null) {
						String indexedContent = field.stringValue();
						indexedContent = convertText(indexedContent);
						Label label = new Label(column, row, indexedContent, arial10format);
						sheet.addCell(label);
					}
					column++;
				}
				row++;
				// if (i == 502) {
				// break;
				// }
			}

			indexReader.close();
			workbook.write();
			workbook.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (RowsExceededException e) {
			throw new RuntimeException(e);
		} catch (WriteException e) {
			throw new RuntimeException(e);
		}
	}

	private static String convertText(String string) {
		// here, the conversion takes place automatically,
		// thanks to Java
		StringReader reader = new StringReader(string);
		StringWriter writer = new StringWriter();
		try {
			BufferedReader bufferedreader = new BufferedReader(reader);
			BufferedWriter bufferedwriter = new BufferedWriter(writer);
			String line;
			while ((line = bufferedreader.readLine()) != null) {
				bufferedwriter.write(line);
				bufferedwriter.newLine();
			}
			bufferedreader.close();
			bufferedwriter.close();
		} catch (IOException e) {/* HANDLE EXCEPTION */
		}
		return writer.toString();
	}

	@Override
	public void exportData(RecordCollection collection, OutputStream output) {
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(output);
			WritableSheet sheet = workbook.createSheet("fields", 0);

			WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
			WritableCellFormat arial10format = new WritableCellFormat(arial10font);

			{
				int column = 0;
				for (IndexField indexField : collection.getIndexFields()) {
					for (String metaName : indexField.getMetaNames()) {
						Label label = new Label(column, 0, metaName, arial10format);
						sheet.addCell(label);
						column++;
					}
				}
			}

			int row = 1;
			RecordServices recordServices = ConstellioSpringUtils.getRecordServices();
			List<Record> collectionRecords = recordServices.list(collection);
			for (Record record : collectionRecords) {
				int column = 0;
				for (IndexField indexField : collection.getIndexFields()) {
					for (String metaName : indexField.getMetaNames()) {
						List<String> indexedContentList = record.getMetaContents(metaName);
						// FIXME
						String indexedContent = StringUtils.join(indexedContentList, ", ");
						Label label = new Label(column, row, indexedContent, arial10format);
						sheet.addCell(label);
						column++;
					}
				}
				row++;
			}

			workbook.write();
			workbook.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (RowsExceededException e) {
			throw new RuntimeException(e);
		} catch (WriteException e) {
			throw new RuntimeException(e);
		}
	}

}
