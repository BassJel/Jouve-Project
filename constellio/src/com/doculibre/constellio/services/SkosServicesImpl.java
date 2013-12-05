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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.doculibre.analyzer.AccentApostropheCleaner;
import com.doculibre.constellio.entities.Categorization;
import com.doculibre.constellio.entities.skos.SkosConcept;
import com.doculibre.constellio.entities.skos.SkosConceptAltLabel;
import com.doculibre.constellio.entities.skos.Thesaurus;
import com.doculibre.constellio.lucene.impl.SkosIndexHelper;
import com.doculibre.constellio.utils.ClasspathUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.ProgressInfo;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;

public class SkosServicesImpl extends BaseCRUDServicesImpl<Thesaurus> implements SkosServices {

    private static final Logger LOGGER = Logger.getLogger(SkosServicesImpl.class.getName());

    private static final String SKOS_CONCEPT_SCHEME = "//skos:ConceptScheme";
    private static final String SKOS_HAS_TOP_CONCEPT_XPATH = "skos:hasTopConcept";
    private static final String DC_TITLE_XPATH = "dc:title";
    private static final String DC_DESCRIPTION_XPATH = "dc:description";
    private static final String DC_CREATOR_XPATH = "dc:creator";
    private static final String DC_DATE_XPATH = "dc:date";
    private static final String DC_LANGUAGE_XPATH = "dc:language";
    private static final String SKOS_CONCEPT_XPATH = "//skos:Concept";
    private static final String RDF_ABOUT_ATTR_XPATH = "@rdf:about";
    private static final String SKOS_PREF_LABEL_XPATH = "skos:prefLabel";
    private static final String SKOS_NOTES_XPATH = "skos:notes";
    private static final String SKOS_BROADER_XPATH = "skos:broader";
    private static final String SKOS_ALT_LABEL_XPATH = "skos:altLabel";
    private static final String SKOS_RELATED_XPATH = "skos:related";
     private static final String SKOS_NARROWER_XPATH = "skos:narrower";

    private static final Namespace rdfNs = Namespace.getNamespace("rdf",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

    public SkosServicesImpl(EntityManager entityManager) {
        super(Thesaurus.class, entityManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Thesaurus importThesaurus(InputStream input, ProgressInfo progressInfo, List<String> errorMessages) {
        Thesaurus thesaurus = new Thesaurus();
        Map<String, SkosConcept> parsedConcepts = new HashMap<String, SkosConcept>();

        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(false);
            Document skosJdom = builder.build(input);

            SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
            collectNamespaces(skosJdom, namespaceContext);

            // Parsing thesaurus tag
            JDOMXPath currentXPath;

            // Concept scheme
            currentXPath = new JDOMXPath(SKOS_CONCEPT_SCHEME);
            currentXPath.setNamespaceContext(namespaceContext);
            Element conceptSchemeElement = (Element) currentXPath.selectSingleNode(skosJdom);

            // rdf about
            currentXPath = new JDOMXPath(RDF_ABOUT_ATTR_XPATH);
            currentXPath.setNamespaceContext(namespaceContext);
            Attribute thesaurusRdfAboutAttribute = (Attribute) currentXPath.selectSingleNode(conceptSchemeElement);
            if (thesaurusRdfAboutAttribute != null) {
            	String thesaurusRdfAbout = thesaurusRdfAboutAttribute.getValue();
            	thesaurus.setRdfAbout(thesaurusRdfAbout);
            }

            // Title
            currentXPath = new JDOMXPath(DC_TITLE_XPATH);
            currentXPath.setNamespaceContext(namespaceContext);
            Element titleElement = (Element) currentXPath.selectSingleNode(conceptSchemeElement);
            if (titleElement != null) {
            	String dcTitle = titleElement.getValue();
            	dcTitle = StringEscapeUtils.unescapeXml(dcTitle);
            	thesaurus.setDcTitle(dcTitle);
            }

            // Description
            currentXPath = new JDOMXPath(DC_DESCRIPTION_XPATH);
            currentXPath.setNamespaceContext(namespaceContext);
            Element descriptionElement = (Element) currentXPath.selectSingleNode(conceptSchemeElement);
            if (descriptionElement != null) {
            	String dcDescription = descriptionElement.getValue();
            	dcDescription = StringEscapeUtils.unescapeXml(dcDescription);
            	thesaurus.setDcDescription(dcDescription);
            }

            // Creator
            currentXPath = new JDOMXPath(DC_CREATOR_XPATH);
            currentXPath.setNamespaceContext(namespaceContext);
            Element creatorElement = (Element) currentXPath.selectSingleNode(conceptSchemeElement);
            if (creatorElement != null) {
            	String dcCreator = creatorElement.getValue();
            	dcCreator = StringEscapeUtils.unescapeXml(dcCreator);
            	thesaurus.setDcCreator(dcCreator);
            }

            // Date
            currentXPath = new JDOMXPath(DC_DATE_XPATH);
            currentXPath.setNamespaceContext(namespaceContext);
            Element dateElement = (Element) currentXPath.selectSingleNode(conceptSchemeElement);
            if (dateElement != null) {
            	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            	Date dcDate = sdf.parse(dateElement.getValue());
            	thesaurus.setDcDate(dcDate);            	
            }

            // Language
            currentXPath = new JDOMXPath(DC_LANGUAGE_XPATH);
            currentXPath.setNamespaceContext(namespaceContext);
            Element languageElement = (Element) currentXPath.selectSingleNode(conceptSchemeElement);
            if (languageElement != null) {
            	Locale dcLanguage = new Locale(languageElement.getValue());
            	thesaurus.setDcLanguage(dcLanguage);
            }

            LOGGER.finest("First pass of skos:Concept tags (collecting labels)");
            currentXPath = new JDOMXPath(SKOS_CONCEPT_XPATH);
            currentXPath.setNamespaceContext(namespaceContext);
            List<Element> skosConceptElements = currentXPath.selectNodes(skosJdom);

            // 2 passes
            progressInfo.setTotal(skosConceptElements.size() * 2);
            for (Element skosConceptElement : skosConceptElements) {
                currentXPath = new JDOMXPath(RDF_ABOUT_ATTR_XPATH);
                currentXPath.setNamespaceContext(namespaceContext);
                Attribute rdfAboutAttribute = (Attribute) currentXPath.selectSingleNode(skosConceptElement);
                // récupération de l'ID
                String rdfAbout = rdfAboutAttribute.getValue();
                LOGGER.finest("Collecting labels for " + rdfAbout);

                SkosConcept skosConcept = new SkosConcept();
                skosConcept.setThesaurus(thesaurus);
                parsedConcepts.put(rdfAbout, skosConcept);
                skosConcept.setRdfAbout(rdfAbout);

                // recupération du descripteur de la langue du thesaurus et les
                // équivalent sont dans une autre langue
                currentXPath = new JDOMXPath(SKOS_PREF_LABEL_XPATH);
                currentXPath.setNamespaceContext(namespaceContext);
                List<Element> prefLabelElements = currentXPath.selectNodes(skosConceptElement);

                for (Element prefLabelElement : prefLabelElements) {
                    String lang = prefLabelElement.getAttributeValue("lang", Namespace.XML_NAMESPACE);
                    Locale prefLabelLocale = lang != null ? new Locale(lang) : Locale.ENGLISH;
                    String prefLabel = prefLabelElement.getValue();
                    prefLabel = StringEscapeUtils.unescapeXml(prefLabel);
                    skosConcept.setPrefLabel(prefLabel, prefLabelLocale);
                }

                // Notes
                currentXPath = new JDOMXPath(SKOS_NOTES_XPATH);
                currentXPath.setNamespaceContext(namespaceContext);
                Element skosNotesElement = (Element) currentXPath.selectSingleNode(skosConceptElement);
                if (skosNotesElement != null) {
                    String skosNotes = skosNotesElement.getTextTrim();
                    skosNotes = StringEscapeUtils.unescapeXml(skosNotes);
                    skosConcept.setSkosNotes(skosNotes);
                }

                // Rejected forms
                currentXPath = new JDOMXPath(SKOS_ALT_LABEL_XPATH);
                currentXPath.setNamespaceContext(namespaceContext);
                List<Element> altLabelElements = currentXPath.selectNodes(skosConceptElement);
                for (Element altLabelElement : altLabelElements) {
                    String lang = altLabelElement.getAttributeValue("lang", Namespace.XML_NAMESPACE);
                    Locale altLabelLocale = lang != null ? new Locale(lang) : Locale.ENGLISH;
                    String altLabel = altLabelElement.getValue();
                    altLabel = StringEscapeUtils.unescapeXml(altLabel);
                    skosConcept.getAltLabels(altLabelLocale).add(altLabel);
                }
                progressInfo.incrementCurrentIndex();
            }

            LOGGER.finest("Second pass of skos:Concept tags (identifying relationships)");
            for (Element skosConceptElement : skosConceptElements) {
                currentXPath = new JDOMXPath(RDF_ABOUT_ATTR_XPATH);
                currentXPath.setNamespaceContext(namespaceContext);
                Attribute rdfAboutAttribute = (Attribute) currentXPath.selectSingleNode(skosConceptElement);
                // recupération de l'ID
                String rdfAbout = rdfAboutAttribute.getValue();
                LOGGER.finest("Processing relationships for " + rdfAbout);

                SkosConcept skosConcept = parsedConcepts.get(rdfAbout);

                // recupération des termes génériques rdf:resource
                currentXPath = new JDOMXPath(SKOS_BROADER_XPATH);
                currentXPath.setNamespaceContext(namespaceContext);
                List<Element> broaderElements = currentXPath.selectNodes(skosConceptElement);

                for (Element broaderElement : broaderElements) {
                    String resource = broaderElement.getAttributeValue("resource", rdfNs);
                    SkosConcept broader = parsedConcepts.get(resource);
                    if (broader != null) {
                    	skosConcept.addBroader(broader);
                        broader.addNarrower(skosConcept);
                    } else {
                        StringBuffer errorMsg = new StringBuffer();
                        errorMsg.append("Concept ");
                        errorMsg.append(rdfAbout);
                        errorMsg.append(" has a missing broader concept :");
                        errorMsg.append(resource);
                        LOGGER.severe(errorMsg.toString());
//                        throw new RuntimeException(errorMsg.toString());
                    }
                }
                // No broader element, therefore this is a top concept
                if (skosConcept.getBroader().isEmpty()) {
                    thesaurus.addTopConcept(skosConcept);
                }

                // Associées
                currentXPath = new JDOMXPath(SKOS_RELATED_XPATH);
                currentXPath.setNamespaceContext(namespaceContext);
                List<Element> relatedElements = currentXPath.selectNodes(skosConceptElement);
                for (Element relatedElement : relatedElements) {
                    String resource = relatedElement.getAttributeValue("resource", rdfNs);
                    SkosConcept related = parsedConcepts.get(resource);
                    skosConcept.addRelated(related);
                }
                progressInfo.incrementCurrentIndex();
            }

            LOGGER.finest("Validating skos:hasTopConcept tags");
            // Validate top concepts
            currentXPath = new JDOMXPath(SKOS_HAS_TOP_CONCEPT_XPATH);
            currentXPath.setNamespaceContext(namespaceContext);
            List<Element> hasTopConceptElements = currentXPath.selectNodes(skosJdom);
            for (Element hasTopConceptElement : hasTopConceptElements) {
                String resource = hasTopConceptElement.getAttributeValue("resource", rdfNs);
                SkosConcept topConcept = parsedConcepts.get(resource);
                if (!topConcept.getBroader().isEmpty()) {
                    StringBuffer errorMsg = new StringBuffer();
                    errorMsg.append("Top concept ");
                    errorMsg.append(resource);
                    errorMsg.append(" has broader concept(s) (");
                    for (Iterator<SkosConcept> it = topConcept.getBroader().iterator(); it.hasNext(); ) {
                    	SkosConcept broaderConcept = it.next();
                        errorMsg.append(broaderConcept.getRdfAbout());
						if (it.hasNext()) {
							errorMsg.append(", ");
						}
					}
                    errorMsg.append(")");
                    throw new RuntimeException(errorMsg.toString());
                } else {
                    LOGGER.finest(resource + " is a valid top concept");
                }
            }

            LOGGER.finest("Third pass of skos:Concept tags (validating relationships)");
            for (Element skosConceptElement : skosConceptElements) {
                currentXPath = new JDOMXPath(RDF_ABOUT_ATTR_XPATH);
                currentXPath.setNamespaceContext(namespaceContext);
                Attribute rdfAboutAttribute = (Attribute) currentXPath.selectSingleNode(skosConceptElement);
                // recupération de l'ID
                String rdfAbout = rdfAboutAttribute.getValue();
                SkosConcept skosConcept = parsedConcepts.get(rdfAbout);

                // Récupération des termes spécifiques
                // recupération des termes spécifiques avec rdf:resource
                currentXPath = new JDOMXPath(SKOS_NARROWER_XPATH);
                currentXPath.setNamespaceContext(namespaceContext);
                List<Element> narrowerElements = currentXPath.selectNodes(skosConceptElement);

                for (Element narrowerElement : narrowerElements) {
                    String resource = narrowerElement.getAttributeValue("resource", rdfNs);
                    SkosConcept narrower = parsedConcepts.get(resource);
                    if (narrower != null) {
                        if (narrower.getBroader().isEmpty()) {
                            StringBuffer errorMsg = new StringBuffer();
                            errorMsg.append("Le concept ");
                            errorMsg.append(rdfAbout);
                            errorMsg.append(" possède un concept spécifique (narrower) (");
                            errorMsg.append(narrower.getRdfAbout());
                            errorMsg.append(") qui ne possède pas un concept générique (broader) correspondant.");
                            LOGGER.severe(errorMsg.toString());
                            errorMessages.add(errorMsg.toString());
//                            throw new RuntimeException(errorMsg.toString());
                        } else if (!narrower.getBroader().contains(skosConcept)) {
                            StringBuffer errorMsg = new StringBuffer();
                            errorMsg.append("Le concept ");
                            errorMsg.append(rdfAbout);
                            errorMsg.append(" possède au moins un concept spécifique (narrower) (");
                            errorMsg.append(narrower.getRdfAbout());
                            errorMsg.append(") qui ne possède pas un concept générique (broader) correspondant. (");
                            for (Iterator<SkosConcept> it = narrower.getBroader().iterator(); it.hasNext(); ) {
                            	SkosConcept narrowerConcept = it.next();
                                errorMsg.append(narrowerConcept.getRdfAbout());
        						if (it.hasNext()) {
        							errorMsg.append(", ");
        						}
        					}
                            errorMsg.append(")");
                            LOGGER.severe(errorMsg.toString());
                            errorMessages.add(errorMsg.toString());
//                          throw new RuntimeException(errorMsg.toString());
                        } else {
                            LOGGER.finest(resource + " has a valid broader/narrower relationship");
                        }
                    } else {
                        StringBuffer errorMsg = new StringBuffer();
                        errorMsg.append("Il manque un concept spécifique (narrower) au concept générique : ");
                        errorMsg.append(rdfAbout);
                        LOGGER.severe(errorMsg.toString());
                        errorMessages.add(errorMsg.toString());
//                      throw new RuntimeException(errorMsg.toString());
                    }
                    
//                    FIXME!!!
//                    if (narrower != null) {
//                        if (narrower.getBroader() == null) {
//                            StringBuffer errorMsg = new StringBuffer();
//                            errorMsg.append("Concept ");
//                            errorMsg.append(rdfAbout);
//                            errorMsg.append(" has a narrower concept (");
//                            errorMsg.append(narrower.getRdfAbout());
//                            errorMsg.append(") that doesn't have the inverse broader relation.");
//                            LOGGER.severe(errorMsg.toString());
////                            throw new RuntimeException(errorMsg.toString());
//                        } else if (!narrower.getBroader().equals(skosConcept)) {
//                            StringBuffer errorMsg = new StringBuffer();
//                            errorMsg.append("Concept ");
//                            errorMsg.append(rdfAbout);
//                            errorMsg.append(" has a narrower concept (");
//                            errorMsg.append(narrower.getRdfAbout());
//                            errorMsg.append(") which has a broader relation that doesn't match (");
//                            errorMsg.append(narrower.getBroader().getRdfAbout());
//                            errorMsg.append(")");
////                            throw new RuntimeException(errorMsg.toString());
//                            LOGGER.severe(errorMsg.toString());
//                        } else {
//                            LOGGER.finest(resource + " has a valid broader/narrower relationship");
//                        }
//                    } else {
//                        StringBuffer errorMsg = new StringBuffer();
//                        errorMsg.append("Concept ");
//                        errorMsg.append(rdfAbout);
//                        errorMsg.append(" has a missing narrower concept :");
//                        LOGGER.severe(errorMsg.toString());
////                      throw new RuntimeException(errorMsg.toString());
//                    }
                }
            }
        } catch (JDOMException e) {
        	//FIXME Report exception "Invalid document" to UI....
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JaxenException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(input);
        }
        return thesaurus;
    }

    private static void collectNamespaces(Document doc, SimpleNamespaceContext namespaceContext) {
        List<String> collectedNamespaces = new ArrayList<String>();
        processChildren(doc.getRootElement(), collectedNamespaces, namespaceContext);
    }

    @SuppressWarnings("unchecked")
    private static void processChildren(Element element, List<String> collectedNamespaces,
        SimpleNamespaceContext namespaceContext) {
        Namespace currentNamespace = (Namespace) element.getNamespace();
        String nsUri = (currentNamespace.getURI());
        if (!exist(collectedNamespaces, nsUri)) {
            collectedNamespaces.add(nsUri.trim());
            namespaceContext.addNamespace(currentNamespace.getPrefix(), currentNamespace.getURI());
        }
        List<Namespace> additionalNs = element.getAdditionalNamespaces();
        if (!additionalNs.isEmpty())
            copyNsList(additionalNs, collectedNamespaces, namespaceContext);
        if (element.getChildren().size() > 0) {
            List<Element> elementChildren = element.getChildren();
            for (int i = 0; i < elementChildren.size(); i++) {
                processChildren(elementChildren.get(i), collectedNamespaces, namespaceContext);
            }
        }
    }

    private static boolean exist(List<String> collectedNamespaces, String nsUri) {
        if (collectedNamespaces.isEmpty())
            return false;
        for (int i = 0; i < collectedNamespaces.size(); i++) {
            if ((collectedNamespaces.get(i)).equals(nsUri)) {
                return true;
            }
        }
        return false;
    }

    private static void copyNsList(List<Namespace> additionalNs, List<String> collectedNamespaces,
        SimpleNamespaceContext namespaceContext) {
        for (int i = 0; i < additionalNs.size(); i++) {
            Namespace ns = additionalNs.get(i);
            namespaceContext.addNamespace(ns.getPrefix(), ns.getURI());
            collectedNamespaces.add(ns.getURI().trim());
        }
    }

    @Override
    public List<Categorization> importCategorizations(InputStream input, Thesaurus thesaurus) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<SkosConcept> searchPrefLabel(String input, Thesaurus thesaurus, final Locale locale) {
        List<SkosConcept> returnedSearchResults = new ArrayList<SkosConcept>();
        if (StringUtils.isNotBlank(input)) {
            input = input.trim();
            
            SkosIndexHelper indexHelper = ConstellioSpringUtils.getSkosIndexHelper();
            List<SkosConcept> searchResults = indexHelper.searchPrefLabel(input, thesaurus, locale);
            for (SkosConcept searchResult : searchResults) {
    			returnedSearchResults.add(searchResult);
			}
            indexHelper.release(searchResults);
        }
        return new HashSet<SkosConcept>(returnedSearchResults);
    }

    @Override
    public Set<SkosConcept> getByPrefLabel(String prefLabel, Thesaurus thesaurus, final Locale locale) {
        List<SkosConcept> returnedSearchResults = new ArrayList<SkosConcept>();
        if (StringUtils.isNotBlank(prefLabel)) {
            prefLabel = prefLabel.trim().toLowerCase();
            String prefLabelWoutAccents = AccentApostropheCleaner.removeAccents(prefLabel);
            
            SkosIndexHelper indexHelper = ConstellioSpringUtils.getSkosIndexHelper();
            List<SkosConcept> searchResults = indexHelper.searchPrefLabel(prefLabel, thesaurus, locale);
            for (SkosConcept searchResult : searchResults) {
        		String searchResultPrefLabel = searchResult.getPrefLabel(locale);
        		if (searchResultPrefLabel != null) {
        			searchResultPrefLabel = searchResultPrefLabel.trim().toLowerCase();
        			searchResultPrefLabel = AccentApostropheCleaner.removeAccents(searchResultPrefLabel);
        			if (searchResultPrefLabel.equals(prefLabelWoutAccents) || 
        					searchResultPrefLabel.startsWith(prefLabelWoutAccents) && searchResultPrefLabel.endsWith(")")) {
        				returnedSearchResults.add(searchResult);
        			} 
        		}
			}
            indexHelper.release(searchResults);
        }
        return new HashSet<SkosConcept>(returnedSearchResults);
    }

	@Override
	public Set<SkosConcept> getByAltLabel(String altLabel, Thesaurus thesaurus, Locale locale) {
        List<SkosConcept> returnedSearchResults = new ArrayList<SkosConcept>();
        if (StringUtils.isNotBlank(altLabel)) {
        	altLabel = altLabel.trim().toLowerCase();
        	String altLabelWoutAccents = AccentApostropheCleaner.removeAccents(altLabel);
            
            SkosIndexHelper indexHelper = ConstellioSpringUtils.getSkosIndexHelper();
            List<SkosConcept> searchResults = indexHelper.searchAltLabel(altLabel, thesaurus, locale);
            for (SkosConcept searchResult : searchResults) {
            	loop2 : for (String searchResultAltLabel : searchResult.getAltLabels(locale)) {
            		searchResultAltLabel = searchResultAltLabel.trim().toLowerCase();
            		searchResultAltLabel = AccentApostropheCleaner.removeAccents(searchResultAltLabel);
        			if (searchResultAltLabel.equals(altLabelWoutAccents)) {
        				returnedSearchResults.add(searchResult);
        				break loop2;
        			} 
				}
			}
            indexHelper.release(searchResults);
        }
        return new HashSet<SkosConcept>(returnedSearchResults);
	}

    @Override
    public Set<SkosConcept> searchAltLabels(String input, Thesaurus thesaurus, Locale locale) {
        Set<SkosConcept> returnedSearchResults = new HashSet<SkosConcept>();
        if (StringUtils.isNotBlank(input)) {
            input = input.trim();
            SkosIndexHelper indexHelper = ConstellioSpringUtils.getSkosIndexHelper();
            List<SkosConcept> searchResults = indexHelper.searchAltLabel(input, thesaurus, locale);
            for (SkosConcept searchResult : searchResults) {
				returnedSearchResults.add(searchResult);
			}
            indexHelper.release(searchResults);
        }
        return returnedSearchResults;
    }

    @Override
    public Set<SkosConcept> searchAllLabels(String input, Thesaurus thesaurus, Locale locale) {
        Set<SkosConcept> returnedSearchResults = new HashSet<SkosConcept>();
        if (StringUtils.isNotBlank(input)) {
            input = input.trim();
            SkosIndexHelper indexHelper = ConstellioSpringUtils.getSkosIndexHelper();
            List<SkosConcept> searchResults = indexHelper.searchAllLabels(input, thesaurus, locale);
            for (SkosConcept searchResult : searchResults) {
				returnedSearchResults.add(searchResult);
			}
            indexHelper.release(searchResults);
        }
        return returnedSearchResults;
    }

    @Override
    public SkosConcept getSkosConcept(Long id) {
        return (SkosConcept) getEntityManager().find(SkosConcept.class, id);
    }

    @Override
    public Thesaurus makePersistent(Thesaurus thesaurus) {
        super.makePersistent(thesaurus);
        for (SkosConcept topConcept : thesaurus.getTopConcepts()) {
			makePersistent(topConcept, true);
		}
        SkosIndexHelper indexHelper = ConstellioSpringUtils.getSkosIndexHelper();
        indexHelper.delete(thesaurus);
        indexHelper.add(thesaurus);
        return thesaurus;
    }

    private SkosConcept makePersistent(SkosConcept skosConcept, boolean recursive) {
        getEntityManager().persist(skosConcept);
        if (recursive) {
            for (SkosConcept narrowerConcept : skosConcept.getNarrower()) {
    			makePersistent(narrowerConcept, true); // Recursive call
    		}
        }
        return skosConcept;
    }

    @Override
    public Thesaurus makeTransient(Thesaurus thesaurus) {
//        for (SkosConcept topConcept : thesaurus.getTopConcepts()) {
//        	makeTransient(topConcept, false);
//		}
//        super.makeTransient(thesaurus);

//        String sqlRecordTag = "DELETE FROM RecordTag WHERE skosConcept_id IN (SELECT id FROM SkosConcept WHERE thesaurus_id=?)";
//        Query sqlQueryRecordTag = getEntityManager().createNativeQuery(sqlRecordTag);
//        sqlQueryRecordTag.setParameter(1, thesaurus.getId());
//        sqlQueryRecordTag.executeUpdate();
        
        String sqlAltLabelValues = "DELETE FROM SkosConceptAltLabel_Values WHERE skosConceptAltLabel_id IN (SELECT id FROM SkosConceptAltLabel WHERE skosConcept_id IN (SELECT id FROM SkosConcept WHERE thesaurus_id=?))";
        Query sqlQueryAltLabelValues = getEntityManager().createNativeQuery(sqlAltLabelValues);
        sqlQueryAltLabelValues.setParameter(1, thesaurus.getId());
        sqlQueryAltLabelValues.executeUpdate();
        
        String sqlAltLabel = "DELETE FROM SkosConceptAltLabel WHERE skosConcept_id IN (SELECT id FROM SkosConcept WHERE thesaurus_id=?)";
        Query sqlQueryAltLabel = getEntityManager().createNativeQuery(sqlAltLabel);
        sqlQueryAltLabel.setParameter(1, thesaurus.getId());
        sqlQueryAltLabel.executeUpdate();
        
        String sqlLabels = "DELETE FROM SkosConcept_Labels WHERE skosConcept_id IN (SELECT id FROM SkosConcept WHERE thesaurus_id=?)";
        Query sqlQueryLabels = getEntityManager().createNativeQuery(sqlLabels);
        sqlQueryLabels.setParameter(1, thesaurus.getId());
        sqlQueryLabels.executeUpdate();
        
        String sqlRelationsSource = "DELETE FROM SkosConcept_Relations WHERE sourceSkosConcept_id IN (SELECT id FROM SkosConcept WHERE thesaurus_id=?)";
        Query sqlQueryRelationsSource = getEntityManager().createNativeQuery(sqlRelationsSource);
        sqlQueryRelationsSource.setParameter(1, thesaurus.getId());
        sqlQueryRelationsSource.executeUpdate();
        
        String sqlRelationsRelated = "DELETE FROM SkosConcept_Relations WHERE relatedSkosConcept_id IN (SELECT id FROM SkosConcept WHERE thesaurus_id=?)";
        Query sqlQueryRelationsRelated = getEntityManager().createNativeQuery(sqlRelationsRelated);
        sqlQueryRelationsRelated.setParameter(1, thesaurus.getId());
        sqlQueryRelationsRelated.executeUpdate();
        
        String sqlNarrower = "DELETE FROM SkosConcept_Narrower WHERE narrowerSkosConcept_id IN (SELECT id FROM SkosConcept WHERE thesaurus_id=?)";
        Query sqlQueryNarrower = getEntityManager().createNativeQuery(sqlNarrower);
        sqlQueryNarrower.setParameter(1, thesaurus.getId());
        sqlQueryNarrower.executeUpdate();
        
        String sqlBroader = "DELETE FROM SkosConcept_Narrower WHERE broaderSkosConcept_id IN (SELECT id FROM SkosConcept WHERE thesaurus_id=?)";
        Query sqlQueryBroader = getEntityManager().createNativeQuery(sqlBroader);
        sqlQueryBroader.setParameter(1, thesaurus.getId());
        sqlQueryBroader.executeUpdate();
        
        String sqlSkosConcept = "DELETE FROM SkosConcept WHERE thesaurus_id=?";
        Query sqlQuerySkosConcept = getEntityManager().createNativeQuery(sqlSkosConcept);
        sqlQuerySkosConcept.setParameter(1, thesaurus.getId());
        sqlQuerySkosConcept.executeUpdate();
        
        String sqlThesaurus = "DELETE FROM Thesaurus WHERE id=?";
        Query sqlQueryThesaurus = getEntityManager().createNativeQuery(sqlThesaurus);
        sqlQueryThesaurus.setParameter(1, thesaurus.getId());
        sqlQueryThesaurus.executeUpdate();
        
        // Will synchronize objects with database
        getEntityManager().flush();

        SkosIndexHelper indexHelper = ConstellioSpringUtils.getSkosIndexHelper();
        indexHelper.delete(thesaurus);
        
        return thesaurus;
    }

    @Override
    public void makeTransient(SkosConcept deletedConcept) {
    	makeTransient(deletedConcept, true);
    }
    
    public void makeTransient(SkosConcept deletedConcept, boolean saveModifiedNarrower) {
        for (SkosConcept narrowerConcept : deletedConcept.getNarrower()) {
        	Set<SkosConcept> oppositeBroader = narrowerConcept.getBroader();
        	if (oppositeBroader.size() == 1 && oppositeBroader.contains(deletedConcept)) {
        		makeTransient(narrowerConcept, saveModifiedNarrower); // Recursive call
        	} else {
        		oppositeBroader.remove(deletedConcept);
        		if (saveModifiedNarrower) {
        			makePersistent(narrowerConcept, false);
        		}
        	}
		}
        getEntityManager().remove(deletedConcept);
    }

    /**
     * Copies content modifications from modified thesaurus into initial thesaurus.
     * 
     * @see com.doculibre.constellio.services.SkosServices#merge(com.doculibre.constellio.entities.skos.Thesaurus,
     *      com.doculibre.constellio.entities.skos.Thesaurus)
     */
    @Override
    public Set<SkosConcept> merge(Thesaurus initialThesaurus, Thesaurus modifiedThesaurus) {
        Set<SkosConcept> deletedConcepts = new HashSet<SkosConcept>();
        Map<String, SkosConcept> initialConcepts = initialThesaurus.getFlattenedConcepts();
        Map<String, SkosConcept> modifiedConcepts = modifiedThesaurus.getFlattenedConcepts();

        initialThesaurus.setDcTitle(modifiedThesaurus.getDcTitle());
        initialThesaurus.setDcDescription(modifiedThesaurus.getDcDescription());
        initialThesaurus.setDcDate(modifiedThesaurus.getDcDate());
        initialThesaurus.setDcCreator(modifiedThesaurus.getDcCreator());

        // Manage deleted and modified concepts
        for (SkosConcept initialConcept : initialConcepts.values()) {
            String rdfAbout = initialConcept.getRdfAbout();
            if (!modifiedConcepts.containsKey(rdfAbout)) {
                deletedConcepts.add(initialConcept);
            } else {
                SkosConcept modifiedConcept = modifiedConcepts.get(rdfAbout);
                initialConcept.setSkosNotes(modifiedConcept.getSkosNotes());

                // Broader before modifications
                Set<SkosConcept> previousBroader = initialConcept.getBroader();
                // Broader as found in the modified file
                Set<SkosConcept> modifiedBroader = modifiedConcept.getBroader();

                // Undo previous relationship
                if (!previousBroader.isEmpty() && !previousBroader.equals(modifiedBroader)) {
                    initialConcept.getBroader().clear();
                    for (SkosConcept previousBroaderConcept : previousBroader) {
                    	previousBroaderConcept.getNarrower().remove(initialConcept);
					}
                }

                // This concept still has a broader concept
                if (!modifiedBroader.isEmpty()) {
                	for (SkosConcept modifiedBroaderConcept : modifiedBroader) {
                        // Did the modified broader exist initially?
                        SkosConcept existingBroader = initialConcepts.get(modifiedBroaderConcept.getRdfAbout());
                        if (existingBroader != null) {
                            // Create the relationship if it didn't exist before
                            if (!existingBroader.getNarrower().contains(initialConcept)) {
                                existingBroader.addNarrower(initialConcept);
                            }
                        } else {
                            // Create the broader relationship with the new SkosConcept
                        	modifiedBroaderConcept.setThesaurus(initialThesaurus);
                        	modifiedBroaderConcept.addNarrower(initialConcept);
                        }
					}
                } else if (!initialThesaurus.getTopConcepts().contains(initialConcept)) {
                    // It is now a top concept
                    initialThesaurus.addTopConcept(initialConcept);
                }

                // Rebuild related from scratch
                initialConcept.getRelated().clear();
                for (SkosConcept related : modifiedConcept.getRelated()) {
                    SkosConcept existingRelated = initialConcepts.get(related.getRdfAbout());
                    if (existingRelated != null) {
                        // Add related existing skos concept
                        initialConcept.addRelated(existingRelated);
                    } else {
                        // Add related new skos concept
                        related.setThesaurus(initialThesaurus);
                        initialConcept.addRelated(related);
                    }
                }

                // Rebuild pref labels from scratch
                initialConcept.getPrefLabels().clear();
                initialConcept.getPrefLabels().addAll(modifiedConcept.getPrefLabels());

                // Rebuild alt labels from scratch
                initialConcept.getAltLabels().clear();
                for (SkosConceptAltLabel altLabel : modifiedConcept.getAltLabels()) {
                    altLabel.setSkosConcept(initialConcept);
                    initialConcept.getAltLabels().add(altLabel);
                }
            }
        }

        // Will include newly discovered concepts after processing modified and deleted concepts
        initialConcepts = initialThesaurus.getFlattenedConcepts();
        // Manage new concepts
        // First pass : Attach new concepts to the thesaurus
        for (SkosConcept modifiedConcept : modifiedConcepts.values()) {
            String rdfAbout = modifiedConcept.getRdfAbout();
            if (!initialConcepts.containsKey(rdfAbout)) {
                modifiedConcept.setThesaurus(initialThesaurus);
            }
        }

        // Will include newly discovered concepts after first pass
        initialConcepts = initialThesaurus.getFlattenedConcepts();
        // Second pass : Attach new concepts to their broader/narrower/related
        for (SkosConcept modifiedConcept : modifiedConcepts.values()) {
            // Newly added
            if (modifiedConcept.getThesaurus().equals(initialThesaurus)) {
                // Broader / narrower
                Set<SkosConcept> broader = modifiedConcept.getBroader();
                if (!broader.isEmpty()) {
                	for (SkosConcept broaderConcept : broader) {
                        SkosConcept existingBroader = initialConcepts.get(broaderConcept.getRdfAbout());
                        if (!existingBroader.getNarrower().contains(modifiedConcept)) {
                            existingBroader.addNarrower(modifiedConcept);
                        }
					}
                } else if (!initialThesaurus.getTopConcepts().contains(modifiedConcept)) {
                    initialThesaurus.addTopConcept(modifiedConcept);
                }

                // Related
                Set<SkosConcept> relatedModifiedThesaurus = new HashSet<SkosConcept>();
                relatedModifiedThesaurus.addAll(modifiedConcept.getRelated());
                // Rebuild from scratch
                modifiedConcept.getRelated().clear();
                for (SkosConcept related : relatedModifiedThesaurus) {
                    SkosConcept existingRelated = initialConcepts.get(related.getRdfAbout());
                    // Add related existing skos concept
                    modifiedConcept.addRelated(existingRelated);
                }
            }
        }
        return deletedConcepts;
    }
    
    private static SkosConcept getFirstBroaderConcept(SkosConcept skosConcept) {
    	SkosConcept firstBroaderConcept;
        Set<SkosConcept> broader = skosConcept.getBroader();
        if (!broader.isEmpty()) {
        	firstBroaderConcept = broader.iterator().next();
        } else {
        	firstBroaderConcept = null;
        }
        return firstBroaderConcept;
    }

    private static void printNice(SkosConcept skosConcept) {
        StringBuffer indent = new StringBuffer();
        SkosConcept indentConcept = skosConcept;
        
        SkosConcept broaderConcept = getFirstBroaderConcept(indentConcept);
        while (broaderConcept != null) {
            indent.append("  ");
            indentConcept = broaderConcept = getFirstBroaderConcept(indentConcept);
        }
        Locale fr = new Locale("fr");
        Locale en = new Locale("en");

        System.out.println(indent + "rdfAbout : " + skosConcept.getRdfAbout());
        System.out.println(indent + "prefLabel (fr) : " + skosConcept.getPrefLabel(fr));
        System.out.println(indent + "prefLabel (en) : " + skosConcept.getPrefLabel(en));
        for (String altLabel : skosConcept.getAltLabels(fr)) {
            System.out.println(indent + "altLabel (fr) : " + altLabel);
        }
        for (String altLabel : skosConcept.getAltLabels(en)) {
            System.out.println(indent + "altLabel (en) : " + altLabel);
        }
        for (SkosConcept related : skosConcept.getRelated()) {
            System.out.println(indent + "Related : " + related.getRdfAbout());
        }
        for (SkosConcept narrower : skosConcept.getNarrower()) {
            System.out.println(indent + "Narrower : ");
            // Recursive call
            printNice(narrower);
        }
    }

    public static void main(String[] args) throws Exception {
        SkosServices skosServices = ConstellioSpringUtils.getSkosServices();

        File webinfDir = ClasspathUtils.getWebinfDir();
        File skosDir = new File(webinfDir, "skos");
        File tagSkosFile = new File(skosDir, "tagskos.rdf");

        ProgressInfo progressInfo = new ProgressInfo();
        Thesaurus thesaurus = skosServices.importThesaurus(new FileInputStream(tagSkosFile), progressInfo, new ArrayList<String>());
        System.out.println(thesaurus.getDcTitle());
        System.out.println(thesaurus.getDcDescription());
        System.out.println(thesaurus.getDcCreator());
        System.out.println(thesaurus.getDcDate());
        System.out.println(thesaurus.getDcLanguage());
        for (SkosConcept skosConcept : thesaurus.getTopConcepts()) {
            printNice(skosConcept);
        }

        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        skosServices.makePersistent(thesaurus);
        entityManager.getTransaction().commit();
        //        
        // if (!entityManager.getTransaction().isActive()) {
        // entityManager.getTransaction().begin();
        // }
        // skosServices.makeTransient(thesaurus);
        // entityManager.getTransaction().commit();
    }

}
