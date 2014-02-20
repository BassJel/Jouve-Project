package com.doculibre.constellio.solr;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;

@SuppressWarnings("serial")
public abstract class LazyLoadSolrDocumentList<T> extends AbstractList<T> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(LazyLoadSolrDocumentList.class.getName());

    private int size = -1;
	private int lastLoadedStart = -1;
	private int lastLoadedEnd = -1;
	
	private ModifiableSolrParams solrParams;
	private SolrServer solrServer;
	
	private SolrDocumentList results;
	
	public LazyLoadSolrDocumentList(ModifiableSolrParams solrParams, SolrServer solrServer) {
		this.solrParams = solrParams;
		if (solrServer == null)  {
			throw new NullPointerException("SolrServer is null");
		}
		this.solrServer = solrServer;
	}
	
	@Override
	public synchronized T get(int index) {
		int listSize = size();
		if (listSize < 0) {
			throw new IllegalArgumentException("negative size: " + listSize);
		}
		if (index < 0) {
			throw new IndexOutOfBoundsException("index " + index + " must not be negative");
		}
		if (index >= listSize) {
			throw new IndexOutOfBoundsException("index " + index + " must be less than size " + listSize);
		}
		int start = index;
		int end = start + 1;
		return subList(start, end).get(0);
	}

	@Override
	public synchronized List<T> subList(int fromIndex, int toIndex) {
		init(fromIndex, toIndex);
		List<T> subList = new ArrayList<T>();
		for (SolrDocument solrDoc : results) {
			T t = convert(solrDoc);
			subList.add(t);
		}
		return subList;
	}

	@Override
	public synchronized int size() {
		if (size == -1) {
			init(0, 1);
			size = (int) results.getNumFound();
		}
		return size;
	}
	
	private synchronized void init(int start, int end) {
		if (start != lastLoadedStart || end != lastLoadedEnd) {
			LOGGER.debug("Loading " + start + " to " + end);
			lastLoadedStart = start;
			lastLoadedEnd = end;
			int rows = end > start ? end - start : 1;
			solrParams.set(CommonParams.START, start);
			solrParams.set(CommonParams.ROWS, rows);
			QueryResponse queryResponse = null;
			try {
				queryResponse = solrServer.query(solrParams);
			} catch (SolrServerException e) {
				throw new RuntimeException(e);
			}
			results = queryResponse.getResults();
		}
	}
	
	protected abstract T convert(SolrDocument solrDoc);

}
