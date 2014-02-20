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
package com.doculibre.constellio.wicket.components.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.util.NamedList;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class TextAndValueAutoCompleteTextField<T> extends AutoCompleteTextField {

	public TextAndValueAutoCompleteTextField(String id, Class<? extends Object> type) {
		super(id, type, new WordsAndValueAutoCompleteRenderer());
	}

	public TextAndValueAutoCompleteTextField(String id) {
		super(id, new WordsAndValueAutoCompleteRenderer());
	}

	public TextAndValueAutoCompleteTextField(String id, IModel model,
			Class<? extends Object> type, AutoCompleteSettings settings) {
		super(id, model, type, new WordsAndValueAutoCompleteRenderer(), settings);
	}

	public TextAndValueAutoCompleteTextField(String id, IModel model,
			Class<? extends Object> type, boolean preselect) {
		super(id, model, type, new WordsAndValueAutoCompleteRenderer(), preselect);
	}

	public TextAndValueAutoCompleteTextField(String id, IModel model) {
		super(id, model, new WordsAndValueAutoCompleteRenderer());
	}
	
	public TextAndValueAutoCompleteTextField(String id, Class<? extends Object> type, WordsAndValueAutoCompleteRenderer renderer) {
		super(id, type, renderer);
	}

	public TextAndValueAutoCompleteTextField(String id, WordsAndValueAutoCompleteRenderer renderer) {
		super(id, renderer);
	}

	public TextAndValueAutoCompleteTextField(String id, IModel model,
			Class<? extends Object> type, AutoCompleteSettings settings, WordsAndValueAutoCompleteRenderer renderer) {
		super(id, model, type, renderer, settings);
	}

	public TextAndValueAutoCompleteTextField(String id, IModel model,
			Class<? extends Object> type, boolean preselect, WordsAndValueAutoCompleteRenderer renderer) {
		super(id, model, type, renderer, preselect);
	}

	public TextAndValueAutoCompleteTextField(String id, IModel model, WordsAndValueAutoCompleteRenderer renderer) {
		super(id, model, renderer);
	}

	@Override
	protected Iterator<Object> getChoices(String input) {
		if (input != null && !input.isEmpty()) {

			final String lastWord; 
			final String wordsBefore;
			final List<String> words = Arrays.asList(input.split(" "));
			if (supportMultipleWords()) {
				lastWord = words.get(words.size() - 1); 
				wordsBefore = input.substring(0, input.length()
						- lastWord.length());
			} else {
				lastWord = input;
				wordsBefore = "";
			}

			final Iterator<T> itChoices = getChoicesForWord(lastWord);

			return new Iterator<Object>() {

				Object nextSuggestion = getNextValue();

				@Override
				public boolean hasNext() {
					return nextSuggestion != null;
				}

				@Override
				public Object next() {
					Object suggestion;
					if (supportMultipleWords()) {
						suggestion = addPrefixAndSuffix(nextSuggestion, wordsBefore, " ");
					} else {
						suggestion = nextSuggestion;
					}
					nextSuggestion = getNextValue();
					return suggestion;
				}

				@SuppressWarnings("rawtypes")
				private Object getNextValue() {
					Object next = null;
					if (itChoices.hasNext()) {
						Object nextChoice = itChoices.next();
						String word = toWord(nextChoice);
							if (word.equals(lastWord) || (supportMultipleWords() && words.contains(word))) {
								next = getNextValue();
							} else {
								next = nextChoice;
							}
					}
					if (next instanceof NamedList) {
						final String word = ((NamedList)next).getName(0);
						final int value = Integer.valueOf(((NamedList)next).getVal(0).toString());
						next = new Map.Entry<String, Integer>() {

							@Override
							public String getKey() {
								return word;
							}

							@Override
							public Integer getValue() {
								return value;
							}

							@Override
							public Integer setValue(Integer value) {
								return null;
							}
						};
					}
					return next;
				}

				@Override
				public void remove() {
				}
			};
		} else {
			return new ArrayList<Object>().iterator();
		}
	}

	protected abstract Iterator<T> getChoicesForWord(String word);

	@SuppressWarnings("rawtypes")
	protected String toWord(Object t) {
		if (t == null) {
			return "";
		} else if (t instanceof String) {
			return (String) t;
		} else if (t instanceof Map.Entry) {
			Map.Entry entry = (Map.Entry) t;
			return entry.getKey().toString();
		} else {
			return t.toString();
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected Object addPrefixAndSuffix(Object t, final String prefix, final String suffix) {
		if (t == null) {
			return "";
		} else if (t instanceof String) {
			return prefix + ((String) t) + suffix;
		} else if (t instanceof Map.Entry) {
			final Map.Entry entry = (Map.Entry) t;
			return new Map.Entry() {

				@Override
				public Object getKey() {
					return prefix + entry.getKey() + suffix;
				}

				@Override
				public Object getValue() {
					return entry.getValue();
				}

				@SuppressWarnings("unchecked")
				@Override
				public Object setValue(Object value) {
					return entry.setValue(value);
				}
			};
		} else {
			return prefix + t.toString() + suffix;
		}
	}
	
	protected boolean supportMultipleWords() {
		return true;
	}

}
