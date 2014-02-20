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
package com.doculibre.constellio.wicket.panels.admin.importing;

import java.io.File;
import java.io.IOException;

import javax.persistence.EntityManager;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.ImportExportServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.ProgressInfo;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.progress.ProgressPanel;
import com.doculibre.constellio.wicket.models.EntityModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public class ImportPanel extends AjaxPanel {
	
	private EntityModel<RecordCollection> importCollectionModel;

	private IModel progressInfoModel;
	private ProgressPanel progressPanel;
	private ProgressInfo progressInfo;
	private Form importForm;
	private TextField indexPathField;
	private AjaxButton importButton;

	public ImportPanel(String id, RecordCollection collection) {
		super(id);
		
		importCollectionModel = new EntityModel<RecordCollection>(collection);
		
		importForm = new Form("importForm");
		add(importForm);
		importForm.setOutputMarkupId(true);
		
		indexPathField = new RequiredTextField("indexPath", new Model());
		importForm.add(indexPathField);
		
		progressInfo = new ProgressInfo();
        progressInfoModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return progressInfo;
            }
        };
        progressPanel = new ProgressPanel("progressPanel", progressInfoModel);
		importForm.add(progressPanel);
		progressPanel.setVisible(false);
		
		importButton = new AjaxButton("importButton", importForm) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				progressPanel.start(target);
				final String indexPath = indexPathField.getModelObjectAsString();
				
				new Thread() {
					@Override
					public void run() {
						try {
							EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
							if (!entityManager.getTransaction().isActive()) {
								entityManager.getTransaction().begin();
							}
							ImportExportServices importExportServices = ConstellioSpringUtils.getImportExportServices();
							importCollectionModel.detach();
							RecordCollection collection = importCollectionModel.getObject();
							
							File indexDir = new File(indexPath);
							Directory directory = FSDirectory.open(indexDir);
							
							importExportServices.importData(directory, collection, progressInfo);

							entityManager.getTransaction().commit();
							entityManager.close();
						} catch (IOException e) {
							throw new WicketRuntimeException(e);
						}
					}
				}.start();
			}
		};
		importForm.add(importButton);
	}

	@Override
	public void detachModels() {
		importCollectionModel.detach();
		super.detachModels();
	}
	
	public static void main(String[] args) {
		int currentIndex = 324;
		int total = 3254;
		int progressPercent;
		if (total > 0) {
			progressPercent = (int) (100 * (currentIndex + 1) / (double) total);
		} else {
			progressPercent = 0;
		}
		System.out.println(progressPercent);
		System.out.println(100 * (currentIndex + 1) / (double) total);
	}

}
