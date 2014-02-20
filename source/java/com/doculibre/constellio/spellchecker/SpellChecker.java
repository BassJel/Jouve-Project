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
package com.doculibre.constellio.spellchecker;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.util.NamedList;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.AsciiUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.stibocatalog.hunspell.Hunspell;

public class SpellChecker {

	public static enum DICTIONARIES {
		fr_FR, en_US, es_MX
	};

	private Map<DICTIONARIES, Hunspell.Dictionary> dictionariesMap = new HashMap<DICTIONARIES, Hunspell.Dictionary>();

	public SpellChecker(String dictionnariesFolder) {
		try {
			// Load the dictionnaries
			for (DICTIONARIES dictName : DICTIONARIES.values()) {
				dictionariesMap.put(dictName, Hunspell.getInstance().getDictionary(
						dictionnariesFolder + File.separator + dictName.toString()));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ListOrderedMap suggest(SimpleSearch simpleSearch, Locale locale) {
		ListOrderedMap suggestionsForSentence = new ListOrderedMap();
		String sentence = simpleSearch.getQuery();
		String collectionName = simpleSearch.getCollectionName();
		addSuggestions(sentence, collectionName, suggestionsForSentence, null, locale);
		return suggestionsForSentence;
	}

	public ListOrderedMap suggest(String sentence, String collectionName) {
		ListOrderedMap suggestionsForSentence = new ListOrderedMap();
		addSuggestions(sentence, collectionName, suggestionsForSentence, null, null);
		return suggestionsForSentence;
	}
	
	public NamedList<Object> suggestNamedList(String sentence, String collectionName) {
		NamedList<Object> nl = new NamedList<Object>();
		addSuggestions(sentence, collectionName, null, nl, null);
		return nl;
	}
	
	private void addSuggestions(String sentence, String collectionName, ListOrderedMap orderedMap, NamedList<Object> namedList, Locale locale) {
		if (sentence.length() > 250) {
			throw new IllegalArgumentException("String is too long, rejected by spell checker");
		}

		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		RecordCollection collection = collectionServices.get(collectionName);
		if (collection != null && collection.isSpellCheckerActive()) {
			String spellCheckerLanguage;
			if (locale != null) {
				spellCheckerLanguage = locale.getLanguage();
			} else {
				spellCheckerLanguage = collection.getSpellCheckerLanguage();
			}
			DICTIONARIES dictionary;
			if (Locale.ENGLISH.getLanguage().equals(spellCheckerLanguage)) {
				dictionary = DICTIONARIES.en_US;
			} else if ("es".equals(spellCheckerLanguage)) {
				dictionary = DICTIONARIES.es_MX;
			} else {
				dictionary = DICTIONARIES.fr_FR;
			}

			Hunspell.Dictionary hunspellDictionary = this.dictionariesMap.get(dictionary);
			String[] words = sentence.split(" ");
			if (words.length > 0) {
				for (int i = 0; i < words.length; i++) {
					String currentWord = words[i];
					currentWord = StringUtils.remove(currentWord, '(');
					currentWord = StringUtils.remove(currentWord, ')');
                    String currentWordWOAccent = AsciiUtils.convertNonAscii(currentWord);
					if (hunspellDictionary.misspelled(currentWord)) {
						List<String> suggestionsForWord = hunspellDictionary.suggest(currentWord);
						if (suggestionsForWord.size() > 0) {
							boolean ignoreSuggestionForWord = false;
							for (String suggestionForWord : suggestionsForWord) {
							    String sugWOAccent = AsciiUtils.convertNonAscii(suggestionForWord);
			                    if (currentWordWOAccent.toLowerCase().equals(sugWOAccent.toLowerCase())) {
									ignoreSuggestionForWord = true;
								}
							}
							if (orderedMap != null) {
								orderedMap.put(currentWord, ignoreSuggestionForWord ? null : suggestionsForWord);
							} else {
								addNamedList(namedList, currentWord, ! ignoreSuggestionForWord, ignoreSuggestionForWord ? null : suggestionsForWord);
							}
						}
					} else {
						if (orderedMap != null) {
							orderedMap.put(currentWord, null);
						} else {
							addNamedList(namedList, currentWord, false, null);
						}
					}
				}
			}
		}
	}

	private void addNamedList(NamedList<Object> mainList, String word, boolean missSpelled, List<String> suggestions) {
		NamedList<Object> nlWord = new NamedList<Object>();
		nlWord.add("frequency", missSpelled ? 0 : 1 );
		NamedList<Object> suggestionsNamedList = new NamedList<Object>();
		if (suggestions != null) {
			for(String suggestedWord : suggestions) {
				NamedList<Object> nlSuggestedWord = new NamedList<Object>();
				nlSuggestedWord.add("frequency", 1);
				suggestionsNamedList.add(suggestedWord, nlSuggestedWord);
			}
		}
		nlWord.add("suggestions", suggestionsNamedList);
		mainList.add(word, nlWord);
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		System.out.println(System.getProperty("os.arch"));
		SpellChecker spellChecker = new SpellChecker("./dictionaries");
		ListOrderedMap suggestion = spellChecker
				.suggest("jestion de proget", "web");
		List<String> keys = suggestion.keyList();
		for (String key : keys) {
			if (suggestion.get(key) != null) {
				System.out.print(key + " : ");
				List<String> keySugs = (List<String>) suggestion.get(key);
				for (String sug : keySugs) {
					System.out.print(sug + ", ");
				}
				System.out.println();
			} else {
				System.out.println(key);
			}
		}
	}

}
