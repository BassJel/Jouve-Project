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
package com.doculibre.constellio.wicket.panels.spellchecker;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.string.JavascriptUtils;

@SuppressWarnings("serial")
public class SpellCheckerPanel extends Panel implements IHeaderContributor {
	
	private TextField searchTxtField;
	private Button searchButton;

	public SpellCheckerPanel(String id, TextField searchTxtField,
			Button searchButton, IModel suggestionModel) {
		super(id, suggestionModel);
		
		this.searchTxtField = searchTxtField;
		this.searchButton = searchButton;
		
		initComponents();
	}

	private void initComponents() {
		searchTxtField.setOutputMarkupId(true);
		searchButton.setOutputMarkupId(true);
		
		IModel suggestedSearchKeyListModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				ListOrderedMap suggestedSearch = getSuggestedSearch();
				return suggestedSearch.keyList();
			}
		};
		
		// Tags <li>
		add(new ListView("suggestion", suggestedSearchKeyListModel) {
			@SuppressWarnings("unchecked")
			@Override
			protected void populateItem(ListItem item) {
				ListOrderedMap suggestedSearch = getSuggestedSearch();
				String originalWord = (String) item.getModelObject();
				List<String> suggestionsForWord = (List<String>) suggestedSearch.get(originalWord);
				
				boolean hasSuggestionForWord = suggestionsForWord != null && !suggestionsForWord.isEmpty();
				boolean hasManySuggestionsForWord = hasSuggestionForWord && suggestionsForWord.size() > 1;
				
				final int wordIndex = item.getIndex();
				// <li> suggested word
				Component suggestedWordComponent;
				if (hasSuggestionForWord) {
					suggestedWordComponent = new Label("motSuggere", originalWord);
					suggestedWordComponent.add(new SimpleAttributeModifier("id", "mot" + wordIndex));
				} else {
					suggestedWordComponent = new WebMarkupContainer("motSuggere");
					suggestedWordComponent.setVisible(false);
				}
				if (hasManySuggestionsForWord) {
					suggestedWordComponent.add(new SimpleAttributeModifier("rel", "suggestion" + item.getIndex()));
				}
				item.add(suggestedWordComponent);
				suggestedWordComponent.add(getLaunchSuggestedSearchOnclickModifier());
				
				// <li> vide pour avoir une flèche en cas de suggestions multiples
				//WebMarkupContainer flecheMotSuggereContainer = new WebMarkupContainer("flecheMotSuggere");
				//flecheMotSuggereContainer.setVisible(plusieursMotsSuggeres);
				//flecheMotSuggereContainer.add(new SimpleAttributeModifier("rel", "suggestion" + item.getIndex()));
				//item.add(flecheMotSuggereContainer);
				
				// <li> si pas de suggestion
				Component noSuggestionWordComponent;
				if (!hasSuggestionForWord) {
					noSuggestionWordComponent = new Label("motNonSuggere", originalWord);
				} else {
					noSuggestionWordComponent = new WebMarkupContainer("motNonSuggere");
					noSuggestionWordComponent.setVisible(false);
				}
				item.add(noSuggestionWordComponent);
				noSuggestionWordComponent.add(getLaunchSuggestedSearchOnclickModifier());
			}
		});
		
		// Tags <div>
		add(new ListView("autresSuggestionsMot", suggestedSearchKeyListModel) {
			@SuppressWarnings("unchecked")
			@Override
			protected void populateItem(ListItem item) {
				ListOrderedMap suggestedSearch = getSuggestedSearch();
				String originalWord = (String) item.getModelObject();
				List<String> suggestedWords = (List<String>) suggestedSearch.get(originalWord);
				
				boolean hasSuggestedWord = suggestedWords != null && !suggestedWords.isEmpty();
				boolean hasManySuggestedWords = hasSuggestedWord && suggestedWords.size() > 1;
				
				final int wordIndex = item.getIndex();
				if (hasManySuggestedWords) {
					item.add(new SimpleAttributeModifier("id", "suggestion" + wordIndex));
					item.add(new ListView("lienRemplacerMot", suggestedWords) {
						@Override
						protected void populateItem(ListItem item) {
							String suggestedWord = (String) item.getModelObject();
							
							WebMarkupContainer lien = new WebMarkupContainer("lien");
							item.add(lien);
							lien.add(new Label("libelle", suggestedWord));

							StringBuffer jsReplaceWord = new StringBuffer("remplacerMot('");
							jsReplaceWord.append(JavascriptUtils.escapeQuotes(suggestedWord));
							jsReplaceWord.append("', ");
							jsReplaceWord.append(wordIndex);
							jsReplaceWord.append(");");
							if (!hasManySuggestions()) {
							    jsReplaceWord.append(getLaunchSuggestedSearchJSCall());
							}
							lien.add(new SimpleAttributeModifier("onclick", jsReplaceWord));
						}
					});
				} else {
					item.setVisible(false);
				}
			}
		});
	}
	
	
	public void renderHead(IHeaderResponse response) {
		StringBuffer jsSuggestedSearch = new StringBuffer("rechercheSuggeree = \"");
		String suggestedSearch = getSuggestedSearchAsString();
		jsSuggestedSearch.append(suggestedSearch);
		jsSuggestedSearch.append("\";");
		CharSequence jsSuggestedSearchEscaped = JavascriptUtils.escapeQuotes(jsSuggestedSearch.toString());
		JavascriptUtils.writeJavascript(response.getResponse(), jsSuggestedSearchEscaped);
	}
	
	private ListOrderedMap getSuggestedSearch() {
		return (ListOrderedMap) getModelObject();
	}
	
	@SuppressWarnings("unchecked")
	private String getSuggestedSearchAsString() {
		StringBuffer sb = new StringBuffer();
		ListOrderedMap suggestedSearch = (ListOrderedMap) getModelObject();
		for (String motOriginal : (Set<String>) suggestedSearch.keySet()) {
			List<String> suggestedWords = (List<String>) suggestedSearch.get(motOriginal);
			if (suggestedWords == null || suggestedWords.isEmpty()) {
				sb.append(motOriginal);
			} else {
				// First word
				sb.append(suggestedWords.get(0));
			}
			sb.append(" ");
		}
		if (sb.length() > 0) {
			// Supprimer le dernier espace
			sb.replace(sb.length() - 1, sb.length(), "");
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
    public boolean hasManySuggestions() {
	    boolean manySuggestions = false;
	    boolean firstSuggestion = true;
        ListOrderedMap suggestedSearch = (ListOrderedMap) getModelObject();
        for (String motOriginal : (Set<String>) suggestedSearch.keySet()) {
            List<String> suggestedWords = (List<String>) suggestedSearch.get(motOriginal);
            if (suggestedWords != null && suggestedWords.size() > 1) {
                if (firstSuggestion) {
                    firstSuggestion = false;
                } else {
                    manySuggestions = true;
                    break;
                }
            }
        }    
        return manySuggestions;
	}

	@Override
	public boolean isVisible() {
		// Y a-t-il une suggestion?
		return getModelObject() != null;
	}
	
	private SimpleAttributeModifier getLaunchSuggestedSearchOnclickModifier() {
		return new SimpleAttributeModifier("onclick", getLaunchSuggestedSearchJSCall());
	}
	
	private String getLaunchSuggestedSearchJSCall() {
        StringBuffer jsLaunchSuggestedSearchJSCall = new StringBuffer("lancerRechercheSuggeree('");
        jsLaunchSuggestedSearchJSCall.append(searchTxtField.getMarkupId());
        jsLaunchSuggestedSearchJSCall.append("', '");
        jsLaunchSuggestedSearchJSCall.append(searchButton.getMarkupId());
        jsLaunchSuggestedSearchJSCall.append("');");
        return jsLaunchSuggestedSearchJSCall.toString();
	} 

}
