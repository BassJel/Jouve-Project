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
package com.doculibre.constellio.wicket.panels.admin.thesaurus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.time.Duration;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.skos.Thesaurus;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.thesaurus.ImportThesaurusPlugin;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SkosServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.ProgressInfo;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.progress.ProgressPanel;
import com.doculibre.constellio.wicket.models.EntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class AddEditThesaurusPanel extends SaveCancelFormPanel {

    private File skosFile;
    private boolean importing = false;
    private boolean importCompleted = false;
    
	private EntityModel<Thesaurus> thesaurusModel;
	
	private ProgressInfo progressInfo;
	private IModel progressInfoModel;
	private ModalWindow errorsModalWindow;
	
	private FileUploadField skosFileField;
	private Button uploadButton;
	private Button deleteButton;
	private ProgressPanel progressPanel;
    private Label rdfAbout;
	private Label dcTitle;
	private Label dcDescription;
	private Label dcDate;
	private Label dcCreator;
	
	private Thesaurus newThesaurus = new Thesaurus();

	public AddEditThesaurusPanel(String id) {
		super(id, false);
		
		this.thesaurusModel = new EntityModel<Thesaurus>(new LoadableDetachableModel() {
            @Override
            protected Object load() {
                AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                RecordCollection collection = collectionAdminPanel.getCollection();
                Thesaurus thesaurus = collection.getThesaurus();
                return thesaurus != null ? thesaurus : newThesaurus;
            }
        });

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(thesaurusModel));

    	final List<String> errorMessages = new ArrayList<String>();
    	errorsModalWindow = new ModalWindow("errorsModal");
    	errorsModalWindow.setCssClassName(ModalWindow.CSS_CLASS_GRAY);

    	skosFileField = new FileUploadField("skosFile");
        uploadButton = new Button("uploadButton") {
            @Override
            public void onSubmit() {
            	errorMessages.clear();
                FileUpload upload = skosFileField.getFileUpload();
                if (upload != null) {
                    try {
                        skosFile = upload.writeToTempFile();
                    } catch (IOException e) {
                        throw new WicketRuntimeException(e);
                    }
                }
                AddEditThesaurusPanel.this.add(new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
                    @Override
                    protected void onTimer(AjaxRequestTarget target) {
                        stop();
                        if (!importing && skosFile != null && skosFile.exists()) {
                            importing = true;
                            importCompleted = false;
                            progressInfo = new ProgressInfo();
                            progressPanel.start(target); 
                            getForm().modelChanging();
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        SkosServices skosServices = ConstellioSpringUtils.getSkosServices();
                                        FileInputStream is = new FileInputStream(skosFile);
                                        AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                                        RecordCollection collection = collectionAdminPanel.getCollection();
                                        Thesaurus importedThesaurus = skosServices.importThesaurus(is, progressInfo, errorMessages);
                                        List<ImportThesaurusPlugin> importThesaurusPlugins = PluginFactory.getPlugins(ImportThesaurusPlugin.class);
                                        if (!importThesaurusPlugins.isEmpty()) {
                                            for (ImportThesaurusPlugin importThesaurusPlugin : importThesaurusPlugins) {
                                                is = new FileInputStream(skosFile);
                                                importThesaurusPlugin.afterUpload(is, importedThesaurus, collection);
                                            }
                                        }
                                        FileUtils.deleteQuietly(skosFile);
                                        thesaurusModel.setObject(importedThesaurus);
                                        importing = false;
                                        importCompleted = true;
                                    } catch (FileNotFoundException e) {
                                        throw new WicketRuntimeException(e);
                                    }
                                }
                            }.start();
                            
                        }
                    }
                });
            }
        };
        uploadButton.setDefaultFormProcessing(false); 
        
        progressInfo = new ProgressInfo();
        progressInfoModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return progressInfo;
            }
        };
		progressPanel = new ProgressPanel("progressPanel", progressInfoModel) {
            @Override
            protected void onFinished(AjaxRequestTarget target) {
//                while (!importCompleted) {
//                    // Wait for the import to be completed
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        throw new WicketRuntimeException(e);
//                    }
//                }
                target.addComponent(rdfAbout);
                target.addComponent(dcTitle);
                target.addComponent(dcDescription);
                target.addComponent(dcDate);
                target.addComponent(dcCreator);
                
                if (!errorMessages.isEmpty()) {
                	IModel errorMsgModel = new LoadableDetachableModel() {
						@Override
						protected Object load() {
							StringBuffer sb = new StringBuffer();
							for (String errorMsg : errorMessages) {
								sb.append(errorMsg);
								sb.append("\n");
							}
							return sb.toString();
						}
					};
					MultiLineLabel multiLineLabel = new MultiLineLabel(errorsModalWindow.getContentId(), errorMsgModel);
					multiLineLabel.add(new SimpleAttributeModifier("style", "text-align:left;"));
					errorsModalWindow.setContent(multiLineLabel);
                	errorsModalWindow.show(target);
                }
            }
		};
		progressPanel.setVisible(false);
		
        deleteButton = new Button("deleteButton") {
            @Override
            public void onSubmit() {
                Thesaurus thesaurus = thesaurusModel.getObject();
                if (thesaurus.getId() != null) {
                    SkosServices skosServices = ConstellioSpringUtils.getSkosServices();
                    
                    EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                    if (!entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().begin();
                    }

                    skosServices.makeTransient(thesaurus);
                    entityManager.getTransaction().commit();

                    AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                    RecordCollection collection = collectionAdminPanel.getCollection();
                    collection.setThesaurus(null);
                    AddEditThesaurusPanel.this.replaceWith(newReturnComponent(AddEditThesaurusPanel.this.getId()));
                }
            }

            @Override
            public boolean isVisible() {
                return super.isVisible() && thesaurusModel.getObject().getId() != null;
            }

            @Override
            protected String getOnClickScript() {
                String confirmMsg = getLocalizer().getString("confirmDelete",
                    AddEditThesaurusPanel.this).replace("'", "\\'");
                return "if (confirm('" + confirmMsg + "')) { return true;} else { return false; }";
            }
        };
        deleteButton.setDefaultFormProcessing(false);
		
		rdfAbout = new Label("rdfAbout");
		rdfAbout.setOutputMarkupId(true);
        
        dcTitle = new Label("dcTitle");
        dcTitle.setOutputMarkupId(true);
        
        dcDescription = new Label("dcDescription");
        dcDescription.setOutputMarkupId(true);
        
        dcDate = new Label("dcDate");
        dcDate.setOutputMarkupId(true);
        
        dcCreator = new Label("dcCreator");
        dcCreator.setOutputMarkupId(true);
        
        form.add(skosFileField);
        form.add(uploadButton);
        form.add(progressPanel);
        form.add(errorsModalWindow);
        form.add(deleteButton);
        form.add(rdfAbout);
        form.add(dcTitle);
        form.add(dcDescription);
        form.add(dcDate);
        form.add(dcCreator);
	}

	@Override
	public void detachModels() {
		thesaurusModel.detach();
		progressInfoModel.detach();
		super.detachModels();
	}

    @Override
    protected IModel getTitleModel() {
        return new LoadableDetachableModel() {
            @Override
            protected Object load() {
                Thesaurus thesaurus = thesaurusModel.getObject();
                String titleKey = thesaurus == null || thesaurus.getId() == null ? "add" : "edit";
                return getLocalizer().getString(titleKey, AddEditThesaurusPanel.this);
            }
        };
    }

    @Override
    protected Component newReturnComponent(String id) {
        return new AddEditThesaurusPanel(id);
    }

    // Nous vous recommandons d'utiliser + Ouvre un popup
    // recherchable + facettable
    @Override
    protected void onSave(AjaxRequestTarget target) {
        if (importCompleted) { 
            RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
            SkosServices skosServices = ConstellioSpringUtils.getSkosServices();
            Thesaurus importedThesaurus = thesaurusModel.getObject();

            AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
            RecordCollection collection = collectionAdminPanel.getCollection();
            Thesaurus initialThesaurus = collection.getThesaurus();
            
            EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
            if (!entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }
//            collectionServices.makePersistent(collection, false);
            if (initialThesaurus != null) {
//                Set<SkosConcept> deletedConcepts = skosServices.merge(initialThesaurus, importedThesaurus);
//                for (SkosConcept deletedConcept : deletedConcepts) {
//                    skosServices.makeTransient(deletedConcept);
//                }
//                skosServices.makePersistent(initialThesaurus);
            	skosServices.makeTransient(initialThesaurus);
            	            	
                importedThesaurus.setRecordCollection(collection);
                collection.setThesaurus(importedThesaurus);
                skosServices.makePersistent(importedThesaurus);
            } else {
                importedThesaurus.setRecordCollection(collection);
                collection.setThesaurus(importedThesaurus);
                skosServices.makePersistent(importedThesaurus);
            }
//            skosServices.makePersistent(importedThesaurus);
            entityManager.getTransaction().commit();
        }
    }

}
