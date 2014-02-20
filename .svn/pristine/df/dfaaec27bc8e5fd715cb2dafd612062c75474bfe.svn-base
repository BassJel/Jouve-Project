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
package com.doculibre.constellio.wicket.panels.admin.searchInterface.theme;

import java.util.Arrays;

import javax.persistence.EntityManager;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.services.SearchInterfaceConfigServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public class ThemeConfigPanel extends AjaxPanel {

    private ReloadableEntityModel<SearchInterfaceConfig> configModel;

    public ThemeConfigPanel(String id) {
        super(id);
        
        SearchInterfaceConfigServices mySearchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();
        SearchInterfaceConfig config = mySearchInterfaceConfigServices.get();
        configModel = new ReloadableEntityModel<SearchInterfaceConfig>(config);
        
        Form form = new Form("form") {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                EntityManager entityManager = ConstellioPersistenceContext
                        .getCurrentEntityManager();
                if (!entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().begin();
                }
                
                SearchInterfaceConfigServices searchInterfaceConfigServices = ConstellioSpringUtils.getSearchInterfaceConfigServices();
                SearchInterfaceConfig config = configModel.getObject();
                searchInterfaceConfigServices.makePersistent(config);
                entityManager.getTransaction().commit();
            }
        };
        form.setModel(new CompoundPropertyModel(configModel));
        
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        
        IChoiceRenderer skinRenderer = new ChoiceRenderer() {
            @Override
            public Object getDisplayValue(Object object) {
                return getLocalizer().getString("skin." + object, ThemeConfigPanel.this);
            }
        };
        RadioChoice skinChoice = new RadioChoice("skin", Arrays.asList(SearchInterfaceConfig.SKINS), skinRenderer);

        add(form);
        form.add(feedbackPanel); 
        form.add(skinChoice);
    }

}
