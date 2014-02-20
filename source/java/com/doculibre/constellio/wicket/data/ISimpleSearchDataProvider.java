package com.doculibre.constellio.wicket.data;

import org.apache.wicket.markup.repeater.data.IDataProvider;

import com.doculibre.constellio.entities.search.SimpleSearch;

public interface ISimpleSearchDataProvider extends IDataProvider {
	
	SimpleSearch getSimpleSearch();

}
