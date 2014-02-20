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
package com.doculibre.constellio.wicket.components.tinymce;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;

import wicket.contrib.tinymce.TinyMceBehavior;
import wicket.contrib.tinymce.settings.TinyMCESettings;

@SuppressWarnings("serial")
public class TinyMCEModalWindow extends ModalWindow implements IHeaderContributor {

	public TinyMCEModalWindow(String id) {
		super(id);
		setCssClassName(ModalWindow.CSS_CLASS_GRAY);
	}

	/**
	 * This is needed because even though {@link TinyMceBehavior} implements IHeaderContributor,
	 * the header doesn't get contributed when the component is first rendered though an AJAX call.
	 * @see https://issues.apache.org/jira/browse/WICKET-618 (which was closed WontFix) 
	 */
	@Override
	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference(TinyMCESettings.javaScriptReference());
	}

}
