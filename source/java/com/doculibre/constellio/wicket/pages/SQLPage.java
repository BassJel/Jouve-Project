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
package com.doculibre.constellio.wicket.pages;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class SQLPage extends BaseConstellioPage {

    private IModel queryModel = new Model();
    private Form form;
    private FeedbackPanel feedbackPanel;
    private TextArea textArea;
    private Button submitButton;

    public SQLPage() {
        super();
        ConstellioSession session = ConstellioSession.get();
        ConstellioUser user = session.getUser();
        boolean redirect;
        if (user == null) {
            redirect = true;
        } else if (user.isAdmin()) {
            redirect = false;
        } else {
            redirect = true;
            RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
            for (RecordCollection collection : collectionServices.list()) {
                if (user.hasCollaborationPermission(collection) || user.hasAdminPermission(collection)) {
                    redirect = false;
                    break;
                }
            }
        }
        if (redirect) {
            setResponsePage(getApplication().getHomePage());
        } else {
            form = new Form("form");
            feedbackPanel = new FeedbackPanel("feedback");
            textArea = new TextArea("query", queryModel);
            submitButton = new Button("submitButton") {
                @Override
                public void onSubmit() {
                    String sql = (String) queryModel.getObject();
                    if (StringUtils.isNotBlank(sql)) {
                        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
                        if (!entityManager.getTransaction().isActive()) {
                            entityManager.getTransaction().begin();
                        }
                        Query sqlQuery = entityManager.createNativeQuery(sql);
                        try {
                            int rowCount = sqlQuery.executeUpdate();
                            entityManager.getTransaction().commit();
                            info(rowCount + " " + getLocalizer().getString("affectedRows", this));
                        } catch (Exception e) {
                            String stack = ExceptionUtils.getFullStackTrace(e);
                            error(stack);
                        }
                    }
                }
            };
            
            add(form);
            form.add(feedbackPanel);
            form.add(textArea);
            form.add(submitButton);
        }
    }

}
