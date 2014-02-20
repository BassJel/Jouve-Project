package com.doculibre.constellio.solr.handler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.RequestHandlerUtils;
import org.apache.solr.handler.UpdateRequestHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;

import com.doculibre.constellio.solr.handler.component.StatsConstants;

public class ComputeClickRequestHandler extends UpdateRequestHandler {

	@Override
	public void init(NamedList args) {
		super.init(args);
	}

	// ////////////////////// SolrInfoMBeans methods //////////////////////

	@Override
	public String getDescription() {
		return "Add documents using http params";
	}

	@Override
	public String getSource() {
		return "$URL: https://svn.apache.org/repos/asf/lucene/dev/branches/lucene_solr_4_4/solr/core/src/java/org/apache/solr/handler/ParamUpdateRequestHandler.java $";
	}

	@Override
	public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
		// TODO Auto-generated method stub
		SolrParams params = req.getParams();

		String recordUrl = params.get("url");
		String searchLogDocId = params.get("searchLogDocId");
		String clickLogDocId = generateClickLogDocId(recordUrl, searchLogDocId);
		String collectionName = params.get("collectionName");
		String displayUrl = params.toString();

		SolrInputDocument doc = new SolrInputDocument();
		doc.setField(StatsConstants.INDEX_FIELD_ID, clickLogDocId);
		doc.setField(StatsConstants.INDEX_FIELD_COLLECTION_NAME, collectionName);
		doc.setField(StatsConstants.INDEX_FIELD_SEARCH_LOG_DOC_ID, searchLogDocId);
		doc.setField(StatsConstants.INDEX_FIELD_RECORD_URL, recordUrl);
		doc.setField(StatsConstants.INDEX_FIELD_DISPLAY_URL, displayUrl);
		doc.setField(StatsConstants.INDEX_FIELD_CLICK_DATE, new Date());

		UpdateRequestProcessorChain processorChain = req.getCore().getUpdateProcessingChain(params.get(UpdateParams.UPDATE_CHAIN));

		UpdateRequestProcessor processor = processorChain.createProcessor(req, rsp);
		try {
			AddUpdateCommand addCmd = new AddUpdateCommand(req);
			addCmd.commitWithin = params.getInt(UpdateParams.COMMIT_WITHIN, -1);
			addCmd.overwrite = params.getBool(UpdateParams.OVERWRITE, true);
			addCmd.solrDoc = doc;
			processor.processAdd(addCmd);
			// Perhaps commit from the parameters
			RequestHandlerUtils.handleCommit(req, processor, params, false);
			RequestHandlerUtils.handleRollback(req, processor, params, false);

		} finally {
			// finish the request
			processor.finish();
		}
	}

	private static String generateClickLogDocId(String url, String simpleSearchId) {
		String uniqueId = simpleSearchId + "____" + url + "_" + newRandomString();
		uniqueId = digest(uniqueId.getBytes());
		return uniqueId;
	}

	private static String newRandomString() {
		long timeMillis = System.currentTimeMillis();
		int randomInt = (int) (Math.random() * Integer.MAX_VALUE);
		return "" + timeMillis + randomInt;
	}

	private static String digest(byte[] content) {
		String digestString;
		try {
			MessageDigest shaDigester = MessageDigest.getInstance("SHA");
			shaDigester.update(content);
			byte[] shaDigest = shaDigester.digest();
			digestString = new String(Base64.encodeBase64(shaDigest));
			digestString = escape(digestString);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		return digestString;
	}

	private static String escape(String text) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			// These characters are part of the query syntax and must be escaped
			if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':' || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~' || c == '*'
					|| c == '?' || c == '|' || c == '&' || c == ' ' || c == '/' || c == '=') {
				sb.append('_');
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
