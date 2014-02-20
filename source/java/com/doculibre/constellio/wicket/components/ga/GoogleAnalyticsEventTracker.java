package com.doculibre.constellio.wicket.components.ga;

import org.apache.wicket.AttributeModifier;

import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

@SuppressWarnings("serial")
public class GoogleAnalyticsEventTracker extends AttributeModifier {
	
	private String category;
	
	private String action;

	public GoogleAnalyticsEventTracker(String event, String category, String action) {
		super(event, true, null);
		this.category = category;
		this.action = action;
	}

	@Override
	protected String newValue(String currentValue, String replacementValue) {
		StringBuffer result = new StringBuffer();
		String newValue;
		SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();
		SearchInterfaceConfig searchInterfaceConfig = searchInterfaceConfigServices.get();
		if (searchInterfaceConfig.isUseGoogleAnalytics()) {
			newValue = getJavascriptCall(category, action);
		} else {
			newValue = null;
		}
		if (currentValue != null) {
			result.append(currentValue);
			if (!currentValue.endsWith(";")) {
				result.append(";");
			}
		}
		if (newValue != null) {
			result.append(newValue);
		}
		return result.length() > 0 ? result.toString() : null;
	}
	
	protected String getJavascriptCall(String category, String action) {
		return "_gaq.push(['_trackEvent', '" + category + "', '" + action + "']);";
	}

}
