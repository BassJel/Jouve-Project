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
package com.doculibre.constellio.wicket.panels;

import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class AjaxPanel extends Panel {

	public AjaxPanel(String id, IModel model) {
		super(id, model);
		initComponents();
	}

	public AjaxPanel(String id) {
		super(id);
		initComponents();
	}

	private void initComponents() {
		setOutputMarkupId(true);
		add(new AbstractBehavior() {
			@Override
			public void renderHead(IHeaderResponse response) {
//				StringBuffer js = new StringBuffer();
//				js.append("if (makeNiceTitles) {\r\n");
//				js.append("    makeNiceTitles();\r\n");
//				js.append("}");
//				response.renderJavascript(js, "niceTitles");
				super.renderHead(response);
			}
		});
	}

}
