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
package com.doculibre.constellio.wicket.components.holders;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public class TextFieldHolder extends AjaxPanel {

	private TextField textField;
	
	public TextFieldHolder(String id, IModel model) {
		super(id);
		add(textField = newTextField("textField", model));
	}

	protected TextField newTextField(String id, IModel model) {
		return new TextField(id, model);
	}

	public TextField getTextField() {
		return textField;
	}
	
}