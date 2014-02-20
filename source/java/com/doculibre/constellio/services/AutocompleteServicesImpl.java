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
package com.doculibre.constellio.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.TermsParams;
import org.apache.solr.common.util.NamedList;

import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.skos.SkosConcept;
import com.doculibre.constellio.entities.skos.Thesaurus;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.solr.context.SolrLogContext;
import com.doculibre.constellio.utils.AnalyzerUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.google.common.io.Files;

public class AutocompleteServicesImpl implements AutocompleteServices {
	// to separate the analysed string from the original one:
	private static final String SPECIAL_CHAR = " # ";
	// N'est pas ajouté aux indexfield entities donc ne sera jamais affiché à
	// l'utilisateur
	private static final String ANALYZED_COPY_FIELD_NAME_SUFFIX = "_analyzedCopy";

	private Boolean hasStringType(IndexField indexField) {
		// return indexField.getFieldType().getName().equals(FieldType.STRING);
		return true;
	}

	@Override
	public NamedList<Object> suggest(String q, IndexField indexField) {
		NamedList<Object> returnList = new NamedList<Object>();
		if (!indexField.isAutocompleted()) {
			return returnList;
		}
		SolrServer server = null;
		try {
			String collectionName = indexField.getRecordCollection().getName();
			server = SolrCoreContext.getSolrServer(collectionName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (!hasStringType(indexField)) {
			// FIXME voir avec Rida s'il faut analyser la requete
			return suggest(q, indexField.getName(), server, false);
		}

		// FIXME valider avec Vincent si on se base sur le champ de copie (dans
		// le cas des chaines de
		// caractere)?
		q = AnalyzerUtils.analyze(q, indexField.getName(), server, true);

		String associatedIndexFieldName = getAssociatedIndexFieldName(indexField);

		returnList = suggest(q, associatedIndexFieldName, server, true);

		return returnList;
	}

	@SuppressWarnings("unchecked")
	static private NamedList<Object> suggest(String q, String fieldName, SolrServer server, Boolean isStringField) {
		NamedList<Object> returnList = new NamedList<Object>();

		// escape special characters
		SolrQuery query = new SolrQuery();

		/*
		 * // * Set terms.lower to the input term
		 * query.setParam(TermsParams.TERMS_LOWER, q); // * Set terms.prefix to
		 * the input term query.setParam(TermsParams.TERMS_PREFIX, q); // * Set
		 * terms.lower.incl to false
		 * query.setParam(TermsParams.TERMS_LOWER_INCLUSIVE, "false"); // * Set
		 * terms.fl to the name of the source field
		 * query.setParam(TermsParams.TERMS_FIELD, fieldName);
		 */

		query.setParam(TermsParams.TERMS_FIELD, fieldName);// query.addTermsField("spell");
		query.setParam(TermsParams.TERMS_LIMIT, TERMS_LIMIT);// query.setTermsLimit(MAX_TERMS);
		query.setParam(TermsParams.TERMS, "true");// query.setTerms(true);
		query.setParam(TermsParams.TERMS_LOWER, q);// query.setTermsLower(q);
		query.setParam(TermsParams.TERMS_PREFIX, q);// query.setTermsPrefix(q);
		query.setParam(TermsParams.TERMS_MINCOUNT, TERMS_MINCOUNT);

		query.setRequestHandler(SolrServices.AUTOCOMPLETE_QUERY_NAME);

		try {
			QueryResponse qr = server.query(query);
			NamedList<Object> values = qr.getResponse();
			NamedList<Object> terms = (NamedList<Object>) values.get("terms");// TermsResponse
																				// resp
																				// =
																				// qr.getTermsResponse();
			NamedList<Object> suggestions = (NamedList<Object>) terms.get(fieldName);// items
																						// =
																						// resp.getTerms("spell");
			if (!isStringField) {
				q = AnalyzerUtils.analyzePhrase(q, false);
			}

			for (int i = 0; i < suggestions.size(); i++) {
				String currentSuggestion = suggestions.getName(i);
				// System.out.println(currentSuggestion);
				if (isStringField) {
					if (currentSuggestion.contains(q)) {
						// String suffix =
						// StringUtils.substringAfter(currentSuggestion, q);
						// if (suffix.isEmpty() &&
						// !currentSuggestion.equals(q)){
						// //q n est pas dans currentSuggestion
						// break;
						// }
						if (currentSuggestion.contains(SPECIAL_CHAR)) {
							// le resultat de recherche retourne des fois une
							// partie de la valeur existant
							// dans le champ!
							currentSuggestion = StringUtils.substringAfter(currentSuggestion, SPECIAL_CHAR);
						}
						returnList.add(currentSuggestion, suggestions.getVal(i));
					}
				} else {
					currentSuggestion = AnalyzerUtils.analyzePhrase(currentSuggestion, false);
					if (currentSuggestion.contains(q)) {
						returnList.add(currentSuggestion, suggestions.getVal(i));
					}
				}
			}

		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
		return returnList;
	}

	/*
	 * static private List<String> removeNoise(List<String> suggestedList) {
	 * List<String> returnList = new ArrayList<String>(); //supprimer le texte
	 * analysé de la réponse for (String currentPhrase : suggestedList){ String
	 * originalPhrase = StringUtils.substringBefore(currentPhrase,
	 * SPECIAL_CHAR); returnList.add(originalPhrase); } return returnList; }
	 */

	/**
	 * Creates a new field associated to the given one. The created copy field
	 * will contain the analyzed text followed by the original one. Both texts
	 * are separated by SPECIAL_CHAR
	 * 
	 * @param indexField
	 * @return the analyzed copy if possible otherwise returns null
	 */
	@Override
	public Boolean setAutoCompleteToField(IndexField indexField) {
		if (!hasStringType(indexField)) {
			indexField.setAutocompleted(true);
			return true;
		}
		indexField.setAutocompleted(true);
		IndexField associatedIndexField = getAssociatedIndexField(indexField);
		if (associatedIndexField == null) {
			associatedIndexField = new IndexField();
			associatedIndexField.setRecordCollection(indexField.getRecordCollection());
			associatedIndexField.setInternalField(true);
			associatedIndexField.setName(getAssociatedIndexFieldName(indexField));
			FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
			FieldType stringFieldType = fieldTypeServices.get(FieldType.STRING);
			associatedIndexField.setFieldType(stringFieldType);

			associatedIndexField.setAnalyzer(indexField.getAnalyzer());

			associatedIndexField.setIndexed(true);

			associatedIndexField.setMultiValued(indexField.isMultiValued());

			IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
			indexFieldServices.makePersistent(associatedIndexField);
			indexField.setAutocompleteDestination(associatedIndexField);
			indexFieldServices.makePersistent(indexField);

		}
		return true;
	}

	@Override
	public void onDocumentAddToAutoCompleteField(SolrInputDocument doc, IndexField indexField, Record record) {

		if (!hasStringType(indexField)) {
			return;
		}

		IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();

		String indexFieldName = indexField.getName();
		List<Object> fieldValues = indexFieldServices.extractFieldValues(record, indexField);
		String associatedFieldName = getAssociatedIndexFieldName(indexField);
		String serverName = indexField.getRecordCollection().getName();
		SolrServer server = SolrCoreContext.getSolrServer(serverName);
		for (Object fieldValue : fieldValues) {
			String analyzedContent;
			// FIXME Valider avec Vincent que l'on se base sur le champ source
			// (cas des String)
			// contrairement à ce qui est fait au
			// moment de la requete Cf. suggest
			analyzedContent = AnalyzerUtils.analyze((String) fieldValue, indexFieldName, server, false);
			String associatedFieldValue = analyzedContent + SPECIAL_CHAR + fieldValue;
			doc.addField(associatedFieldName, associatedFieldValue);
		}
	}

	private String getAssociatedIndexFieldName(IndexField indexField) {
		IndexField associatedIndexField = getAssociatedIndexField(indexField);
		if (associatedIndexField != null) {
			return associatedIndexField.getName();
		} else {
			return indexField.getName() + ANALYZED_COPY_FIELD_NAME_SUFFIX;
		}
	}

	@Override
	public Boolean removeAutoCompleteFromField(IndexField indexField) {

		IndexField associatedIndexField = getAssociatedIndexField(indexField);
		IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
		indexField.setAutocompleted(false);
		if (associatedIndexField != null) {
			indexFieldServices.makeTransient(associatedIndexField);
		}

		indexFieldServices.makePersistent(indexField);

		return true;
	}

	private IndexField getAssociatedIndexField(IndexField indexField) {
		return indexField.getAutocompleteDestination();
		/*
		 * IndexFieldServices indexFieldServices =
		 * ConstellioSpringUtils.getIndexFieldServices(); String name =
		 * getAssociatedIndexFieldName(indexField); RecordCollection collection
		 * = indexField.getRecordCollection(); IndexField associatedIndexField =
		 * indexFieldServices.get(name, collection); return
		 * associatedIndexField;
		 */
	}

	@Override
	public void onAutoCompleteFieldTypeChanged(IndexField indexField) {
		if (!indexField.getAutocompleted()) {
			return;
		}
		if (!hasStringType(indexField)) {
			return;
		}
		if (getAssociatedIndexField(indexField) != null) {
			// a déjà un champ associé => ne rien faire
			return;
		}
		setAutoCompleteToField(indexField);
	}

	@Override
	public List<IndexField> getAutoCompleteIndexFields(RecordCollection collection) {
		Map<String, Object> criterias = new HashMap<String, Object>();
		criterias.put("autocompleted", true);
		criterias.put("recordCollection", collection);
		IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
		return indexFieldServices.list(criterias);
	}

	private static void print(String q, NamedList<Object> suggestions) {
		System.out.println("########### " + q + " ##############");
		for (int i = 0; i < suggestions.size(); i++) {
			System.out.println(suggestions.getName(i) + "\t" + suggestions.getVal(i));
		}
	}

	// // Enlever les requetes avec facette et le reste ..
	// private NamedList<Object> filter(NamedList<Object> suggestions) {
	// NamedList<Object> returnList = new NamedList<Object>();
	// for (int i = 0; i < suggestions.size(); i++) {
	// String currentSuggestion = suggestions.getName(i);
	// if (currentSuggestion.contains("(")) {
	// if (currentSuggestion.contains("+") || currentSuggestion.contains("-")
	// || currentSuggestion.contains("(") || currentSuggestion.contains(")")) {
	// // Ne pas la traiter
	// continue;
	// }
	// }
	// returnList.add(currentSuggestion, suggestions.getVal(i));
	// }
	// return returnList;
	// }

	@Override
	public void onQueryAdd(SolrInputDocument docCompile, String query) {
		String fieldName = "query";
		SolrServer server = SolrLogContext.getSearchCompileLogSolrServer();
		String analyzedQuery;
		// FIXME valider aussi avec vincent
		analyzedQuery = AnalyzerUtils.analyze(query, fieldName, server, false);
		docCompile.addField("query_analyzedCopy", analyzedQuery + SPECIAL_CHAR + query);
	}

	private boolean isValidAutocompleteSuggestion(String input, String analyzedInput, String suggestion, String analyzedSuggestion, RecordCollection collection) {
		boolean valid = false;
		List<String> backList = getBlacklist();
		if (!backList.contains(suggestion)) {
			if ((StringUtils.isNotBlank(analyzedInput) && analyzedSuggestion.startsWith(analyzedInput)) || (StringUtils.isNotBlank(input) && suggestion.startsWith(input))) {
				valid = true;
			}
		}
		return valid;
	}

	@Override
	public List<String> suggestSimpleSearch(String input, RecordCollection collection, Locale locale) {

		List<String> suggestions = new ArrayList<String>();
		if (input.length() >= 3 && !input.contains("*:*") && !collection.isOpenSearch()) {
			try {
				SkosServices skosServices = ConstellioSpringUtils.getSkosServices();
				StatsServices statsServices = ConstellioSpringUtils.getStatsServices();

				int maxResults = 5;
				String analyzedInput = AnalyzerUtils.analyze(input, collection);
				List<String> analyzedSuggestions = new ArrayList<String>();

				Thesaurus thesaurus = collection.getThesaurus();
				if (thesaurus != null) {
					Set<SkosConcept> prefLabelSuggestions = skosServices.searchPrefLabel(input + "*", thesaurus, locale);
					for (SkosConcept thesaurusSuggestion : prefLabelSuggestions) {
						String prefLabel = thesaurusSuggestion.getPrefLabel(locale);
						prefLabel = prefLabel.toLowerCase();
						String analyzedSuggestion = AnalyzerUtils.analyze(prefLabel, collection);
						if (isValidAutocompleteSuggestion(input, analyzedInput, prefLabel, analyzedSuggestion, collection) && !analyzedSuggestions.contains(analyzedSuggestion)) {
							maxResults--;
							suggestions.add(prefLabel);
							analyzedSuggestions.add(analyzedSuggestion);
							if (maxResults == 0) {
								break;
							}
						}
					}
					if (maxResults > 0) {
						Set<SkosConcept> altLabelSuggestions = skosServices.searchAltLabels(input + "*", thesaurus, locale);
						for (SkosConcept thesaurusSuggestion : altLabelSuggestions) {
							for (String altLabel : thesaurusSuggestion.getAltLabels(locale)) {
								altLabel = altLabel.toLowerCase();
								String analyzedSuggestion = AnalyzerUtils.analyze(altLabel, collection);
								if (isValidAutocompleteSuggestion(input, analyzedInput, altLabel, analyzedSuggestion, collection) && !analyzedSuggestions.contains(analyzedSuggestion)) {
									maxResults--;
									suggestions.add(altLabel);
									analyzedSuggestions.add(analyzedSuggestion);
									if (maxResults == 0) {
										break;
									}
								}
							}
						}
					}
				}
				if (maxResults > 0) {
					List<String> mostPopularQueriesSuggestions = statsServices.getMostPopularQueriesAutocomplete(input, collection, maxResults);
					for (String mostPopularQueriesSuggestion : mostPopularQueriesSuggestions) {
						String analyzedSuggestion = AnalyzerUtils.analyze(mostPopularQueriesSuggestion, collection);
						if (isValidAutocompleteSuggestion(input, analyzedInput, mostPopularQueriesSuggestion, analyzedSuggestion, collection) && !analyzedSuggestions.contains(analyzedSuggestion)) {
							maxResults--;
							suggestions.add(mostPopularQueriesSuggestion);
							analyzedSuggestions.add(analyzedSuggestion);
							if (maxResults == 0) {
								break;
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}
		return suggestions;
	}

	@Override
	public void blacklistAutocomplete(String term) {
		List<String> backList = getBlacklist();
		if (!backList.contains(term)) {
			backList.add(term);
			writeBlacklistFile(backList);
		}
	}

	@Override
	public List<String> getBlacklistedAutocompleteTerms() {
		return getBlacklist();
	}

	@Override
	public void cancelBlacklistedAutocomplete(String term) {
		List<String> blacklist = getBlacklist();
		if (blacklist.contains(term)) {
			blacklist.remove(term);
			writeBlacklistFile(blacklist);
		}
	}

	private synchronized void writeBlacklistFile(List<String> blacklist) {
		if (blacklist != null) {
			try {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				IOUtils.writeLines(blacklist, null, output);
				SolrServicesImpl.writePlainConfigInCloud("autocomplete-blacklist.txt", output.toByteArray());
			} catch (IOException e) {
				throw new RuntimeException(e);
			} 
		}
	}

	private static List<String> getBlacklist() {
		List<String> blacklist = new ArrayList<String>();
		try {
			byte[] blackListConfig = SolrServicesImpl.readPlainConfigInCloud("autocomplete-blacklist.txt");
			if (blackListConfig != null && blackListConfig.length > 0) {
				blacklist.addAll(IOUtils.readLines(new ByteArrayInputStream(blackListConfig)));
				Collections.sort(blacklist);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return blacklist;
	}

	public static void main(String[] args) {
		String path = "C:\\Users\\bnouha1\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\constellio\\WEB-INF\\stats";
		// "C:\\Users\\bnouha1\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\constellio\\WEB-INF\\constellio\\collections";
		SolrCoreContext.init();
		SolrServer testServer = SolrCoreContext.getSolrServerUtil("statsCompile");// "mailAccents");
		if (testServer == null) {
			System.out.println("Null server!");
			return;
		}

		print("goo", suggest(AnalyzerUtils.analyzePhrase("goo"), "query_analyzedCopy", testServer, true));
		/*
		 * print ("t", suggest(AnalyzerUtils.analyzePhrase("t"),
		 * "constellio_mail_mainFolder_analyzedCopy", testServer, true)); print
		 * ("té", suggest(AnalyzerUtils.analyzePhrase("té", false),
		 * "constellio_mail_mainFolder_analyzedCopy", testServer, true)); print
		 * ("te", suggest(AnalyzerUtils.analyzePhrase("te", false),
		 * "constellio_mail_mainFolder_analyzedCopy", testServer, true)); print
		 * ("tes", suggest(AnalyzerUtils.analyzePhrase("tes", false),
		 * "constellio_mail_mainFolder_analyzedCopy", testServer, true)); print
		 * ("test", suggest(AnalyzerUtils.analyzePhrase("test", false),
		 * "constellio_mail_mainFolder_analyzedCopy", testServer, true)); print
		 * ("Test", suggest(AnalyzerUtils.analyzePhrase("Test", false),
		 * "constellio_mail_mainFolder_analyzedCopy", testServer, true)); print
		 * ("Të", suggest(AnalyzerUtils.analyzePhrase("Të", false),
		 * "constellio_mail_mainFolder_analyzedCopy", testServer, true)); print
		 * ("B", suggest(AnalyzerUtils.analyzePhrase("B", false),
		 * "constellio_mail_mainFolder_analyzedCopy", testServer, true)); print
		 * ("Bo", suggest(AnalyzerUtils.analyzePhrase("Bo", false),
		 * "constellio_mail_mainFolder_analyzedCopy", testServer, true)); print
		 * ("b", suggest(AnalyzerUtils.analyzePhrase("b", false),
		 * "constellio_mail_mainFolder_analyzedCopy", testServer, true));
		 * System.out.println("Autocomplete avec champ de type text_fr:"); print
		 * ("B", suggest("B", "doc_title", testServer, false)); print ("Boost",
		 * suggest("Boost", "doc_title", testServer, false)); print ("Böost",
		 * suggest("Böost", "doc_title", testServer, false)); print ("g",
		 * suggest("g", "doc_title", testServer, false));
		 */

		// print ("b", suggest(AnalyzerUtils.analyzePhrase("b", false),
		// "constellio_mail_mainFolder_analyzedCopy", testServer, true));

		SolrCoreContext.shutdown();
	}

}
