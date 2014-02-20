package com.doculibre.constellio.wicket.components.searchInterfaceContext;

import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.resource.ByteArrayResource;

import com.doculibre.constellio.entities.searchInterface.SearchInterfaceContextParam;
import com.doculibre.constellio.services.SearchInterfaceContextServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

@SuppressWarnings("serial")
public class SearchInterfaceContextParamResourceReference extends ResourceReference {
	
	private long contextParamId;

	public SearchInterfaceContextParamResourceReference(SearchInterfaceContextParam contextParam) {
		super("SearchInterfaceContextParam_" + contextParam.getId() + "_" + contextParam.getLastModified().getTime());
    	this.contextParamId = contextParam.getId();
	}

    @Override
    protected Resource newResource() {
    	SearchInterfaceContextServices contextServices = ConstellioSpringUtils.getSearchInterfaceContextServices();
    	SearchInterfaceContextParam contextParam = contextServices.getParam(contextParamId);
        
    	Resource contextParamResource;
        byte[] contextParamValueBytes = contextParam.getBinaryValue();
        String contentType = contextParam.getContentType();
        // Convert resource path to absolute path relative to base package
        if (contextParamValueBytes != null) {
            contextParamResource = new ByteArrayResource(contentType, contextParamValueBytes);
        } else {
        	contextParamResource = null;
        }
        return contextParamResource;
    }
    
}
