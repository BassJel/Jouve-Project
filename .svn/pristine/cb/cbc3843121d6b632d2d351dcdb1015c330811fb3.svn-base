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
package com.doculibre.constellio.wicket.panels.admin.searchInterface;

import javax.persistence.EntityManager;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.FileSizeUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public class LogoConfigPanel extends AjaxPanel {

    public LogoConfigPanel(String id) {
        super(id);

        Form form = new Form("form");
        add(form);

        form.add(new FeedbackPanel("feedback"));
        
        final FileUploadField logoSmallField = new FileUploadField("logoSmall");
        form.add(logoSmallField);

        final FileUploadField logoLargeField = new FileUploadField("logoLarge");
        form.add(logoLargeField);

        Button submitButton = new Button("submitButton") {
            @Override
            public void onSubmit() {
                SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils
                    .getSearchInterfaceConfigServices();
                SearchInterfaceConfig searchInterfaceConfig = searchInterfaceConfigServices.get();

                FileUpload uploadLogoSmall = logoSmallField.getFileUpload();
                FileUpload uploadLogoLarge = logoLargeField.getFileUpload();

                if (uploadLogoSmall != null) {
                	if (uploadLogoSmall.getSize() <= SearchInterfaceConfig.LOGO_MAXIMUM_SIZE) {
                		searchInterfaceConfig.setLogoSmallContent(uploadLogoSmall.getBytes());
                	} else {
                		String size = FileSizeUtils.formatSize(uploadLogoSmall.getSize(), 3);
                		String maxSize = FileSizeUtils.formatSize(SearchInterfaceConfig.LOGO_MAXIMUM_SIZE, 3);
                		String[] params = new String[] {size, maxSize};
                		String error = new StringResourceModel("logoSmallTooBig", LogoConfigPanel.this, null, params).getString();
                		error(error);
                		uploadLogoSmall = null;
                	}
                    
                }
                if (uploadLogoLarge != null) {
                    
                	if (uploadLogoLarge.getSize() <= SearchInterfaceConfig.LOGO_MAXIMUM_SIZE) {
                		searchInterfaceConfig.setLogoLargeContent(uploadLogoLarge.getBytes());
                	} else {
                		String size = FileSizeUtils.formatSize(uploadLogoLarge.getSize(), 3);
                		String maxSize = FileSizeUtils.formatSize(SearchInterfaceConfig.LOGO_MAXIMUM_SIZE, 3);
                		String[] params = new String[] {size, maxSize};
                		String error = new StringResourceModel("logoLargeTooBig", LogoConfigPanel.this, null, params).getString();
                		error(error);
                		uploadLogoLarge = null;
                	}
                }
                if (uploadLogoSmall != null || uploadLogoLarge != null) {
                    EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                    if (!entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().begin();
                    }
                    searchInterfaceConfigServices.makePersistent(searchInterfaceConfig);
                    entityManager.getTransaction().commit();
                }
            }
        };
        form.add(submitButton);
    }

}
