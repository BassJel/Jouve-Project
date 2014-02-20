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
package com.doculibre.constellio.wicket.models;

import java.util.Locale;

import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.wicket.session.ConstellioSession;

/**
 * @author Francis Baril
 * 
 * Ce model existe pour rapidement (et temporairement) corriger les classes
 * javas hors du contexte de Wicket ayant du texte hardcodé. Il serait
 * avantageux de retravailler davantage ces pages pour utiliser des fichiers de
 * propriétés.
 */
@SuppressWarnings("serial")
public class EnglishFrenchModel extends LoadableDetachableModel {

	private String frenchMessage;
	private String englishMessage;

	public EnglishFrenchModel(String english, String french) {
		this.frenchMessage = french;
		this.englishMessage = english;
	}

	@Override
	protected Object load() {
		return ConstellioSession.get().getLocale().getLanguage().equals(Locale.FRENCH.getLanguage()) ? frenchMessage
				: englishMessage;
	}

}
