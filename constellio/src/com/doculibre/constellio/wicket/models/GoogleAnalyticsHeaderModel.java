package com.doculibre.constellio.wicket.models;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

@SuppressWarnings("serial")
public class GoogleAnalyticsHeaderModel extends LoadableDetachableModel {

	@Override
	protected Object load() {
		String googleAnalyticsHeader;
		SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();
		SearchInterfaceConfig searchInterfaceConfig = searchInterfaceConfigServices.get();
		if (searchInterfaceConfig.isUseGoogleAnalytics()) {
			googleAnalyticsHeader = searchInterfaceConfig.getGoogleAnalyticsHeader();
			String googleAnalyticsUA = searchInterfaceConfig.getGoogleAnalyticsUA();
			if (StringUtils.isNotBlank(googleAnalyticsUA)) {
				googleAnalyticsHeader = StringUtils.replace(
								googleAnalyticsHeader, 
								SearchInterfaceConfig.GA_UA_PLACEHOLDER, 
								googleAnalyticsUA);
			}
		} else {
			googleAnalyticsHeader = null;
		}
		return googleAnalyticsHeader;
	}

}
