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
package com.doculibre.constellio.wicket.panels.results;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class PopupDetailsPanel extends Panel {

	public PopupDetailsPanel(String id, String documentContent, String documentLastModified) {
		super(id);
		
		
//		documentContent = StringUtils.replace(documentContent, ">>", "&gt;&gt;");
//		documentContent = StringUtils.replace(documentContent, "\n>", "\n&gt;");
//		documentContent = StringUtils.replace(documentContent, "\n", "\n<br />");
//
//		final boolean isTextMessage = documentContent.toLowerCase().indexOf("<div") == -1 || documentContent.toLowerCase().indexOf("<html") == -1;
		
		add(new Label("documentContent", documentContent) {
			@Override
			public boolean isVisible() {
				return true;//isTextMessage;
			}			
		}.setEscapeModelStrings(false));
		
		
		add(new Label("date", documentLastModified).setEscapeModelStrings(false));
		
	}

}
