package com.doculibre.constellio.plugins.api.wicket.search;

import java.util.List;

import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.plugins.api.ConstellioPlugin;

public interface TransfertSearchResultsPlugin extends ConstellioPlugin {

	String getLabelText();
	
	String transfert(Record record);
	
	void cancel(Record record, String id);
	
	void afterTransfert(List<Record> records, List<String> ids);
}
