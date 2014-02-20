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
package com.doculibre.constellio.solr.handler.component;

import java.io.IOException;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.mcf.ManifoldCFSearchComponent;
import org.apache.solr.request.SolrQueryRequest;

import com.doculibre.constellio.connector.intelliGID.IntelliGIDConnectorType;
import com.doculibre.constellio.connector.mcf.ManifoldCFConnectorType;
import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.solr.handler.ConstellioSolrQueryParams;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class ManifoldCFAuthorizationComponent extends ManifoldCFSearchComponent {

	public static final String ENABLE = "enableMcfAuthorizaion";

	@Override
	public void prepare(ResponseBuilder rb) throws IOException {
		SolrQueryRequest req = rb.req;
		SolrParams params = req.getParams();

		// A runtime param can skip
		if (!params.getBool(ENABLE, true)) {
			return;
		}
		
		boolean hasManifoldConnector = false;
		
		String collectioName = params.get(ConstellioSolrQueryParams.COLLECTION_NAME);
		RecordCollectionServices recordCollectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection recordCollection = recordCollectionServices.get(collectioName);
		if (recordCollection != null) {
			for (ConnectorInstance connector : recordCollection.getConnectorInstances()) {
				if (connector.getConnectorType().getName().equals(ManifoldCFConnectorType.CONNECTOR_TYPE_NAME)) {
					hasManifoldConnector = true;
					break;
				} else if (connector.getConnectorType().getName().equals(IntelliGIDConnectorType.CONNECTOR_TYPE_NAME)) {
					hasManifoldConnector = true;
					break;
				}
			}
		}
		
		//skip calling the component if we don't use the service (the manifoldcf server could not be up)
		if (hasManifoldConnector) {
			ConstellioUser user;
			String userIdStr = params.get(ConstellioSolrQueryParams.USER_ID);
			if (userIdStr != null) {
				UserServices userServices = ConstellioSpringUtils.getUserServices();
				try {
					user = userServices.get(new Long(userIdStr));
				} catch (NumberFormatException e) {
					user = null;
				}
			} else {
				user = null;
			}
	
			if (user != null) {
				ModifiableSolrParams newParams = new ModifiableSolrParams(params);
				newParams.add(AUTHENTICATED_USER_NAME, user.getUsername() + "@" + user.getDomain());
				req.setParams(newParams);
			} else {
				ModifiableSolrParams newParams = new ModifiableSolrParams(params);
				newParams.add(AUTHENTICATED_USER_NAME, "guest@guest");
				req.setParams(newParams);
			}
			super.prepare(rb);
		}
	}
}
