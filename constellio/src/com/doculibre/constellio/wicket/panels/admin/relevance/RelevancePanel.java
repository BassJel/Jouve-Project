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
package com.doculibre.constellio.wicket.panels.admin.relevance;


import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;

import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.relevance.collection.RecordCollectionRelevancePanel;
import com.doculibre.constellio.wicket.panels.admin.relevance.indexField.IndexFieldRelevancePanel;
import com.doculibre.constellio.wicket.panels.admin.relevance.query.QueryRelevancePanel;
import com.doculibre.constellio.wicket.panels.admin.relevance.results.ResultsRelevancePanel;

@SuppressWarnings("serial")
public class RelevancePanel extends AjaxPanel {

	private Component queryBoost;
	private Component fieldBoost;
	private Component documentBoost;
	private Component resultsRelevance;

	public RelevancePanel(String id) {
		super(id);

		documentBoost = new RecordCollectionRelevancePanel("documentBoost").add(new SimpleAttributeModifier("style", "width:100%"));
        this.add(documentBoost);

		fieldBoost = new IndexFieldRelevancePanel("fieldBoost").add(new SimpleAttributeModifier("style", "width:100%"));
        this.add(fieldBoost);
        
		queryBoost = new QueryRelevancePanel("queryBoost").add(new SimpleAttributeModifier("style", "width:100%"));
        this.add(queryBoost);
        
        resultsRelevance = new ResultsRelevancePanel("resultsRelevance").add(new SimpleAttributeModifier("style", "width:100%"));
        this.add(resultsRelevance);
	}

}
