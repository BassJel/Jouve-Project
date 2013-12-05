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
package com.doculibre.constellio.wicket.panels.admin.elevate.modal;

import org.apache.wicket.markup.html.panel.Panel;

import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.ElevateServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.search.query.SimpleSearchQueryPanel;

@SuppressWarnings("serial")
public class ElevateQueryDocIdsPanel extends Panel {

	public ElevateQueryDocIdsPanel(String id, String queryText) {
		super(id);

		ElevateServices elevateServices = ConstellioSpringUtils.getElevateServices();
		SimpleSearch querySimpleSearch = elevateServices.toSimpleSearch(queryText);
		add(new SimpleSearchQueryPanel("queryText", querySimpleSearch));
		add(new DocIdsPanel("elevatedDocIdsPanel", queryText, true));
	}

}
