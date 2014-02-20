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
package com.doculibre.constellio.wicket.panels.signIn;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authentication.panel.SignInPanel;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class ConstellioSignInPanel extends SignInPanel {

    public ConstellioSignInPanel(String id) {
        super(id);
        
        final FeedbackPanel feedbackPanel = (FeedbackPanel) get("feedback");
        feedbackPanel.setOutputMarkupId(true);
        
        Form signinForm = (Form) get("signInForm"); 
        signinForm.add(new SetFocusBehavior(signinForm));
        
        signinForm.add(new AjaxButton("submitButton", signinForm) {
            /**
             * Will be called after the onSubmit() method of the signinForm
             * 
             * @see org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget, org.apache.wicket.markup.html.form.Form)
             */
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                if (signIn(getUsername(), getPassword())) {
                    ConstellioUser currentUser = ConstellioSession.get().getUser();
                    if (currentUser.isAdmin()) {
                        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
                        setResponsePage(pageFactoryPlugin.getAdminPage());
                    } else {
                        ModalWindow.closeCurrent(target);
                    }    
                } else {
                    target.addComponent(feedbackPanel);
                }
            }
            
            @Override
            protected void onError(final AjaxRequestTarget target, final Form form) {
                target.addComponent(feedbackPanel);
            }
        });
        
        signinForm.add(new AjaxButton("cancelButton", signinForm) {
            /**
             * Will be called after the onSubmit() method of the signinForm
             * 
             * @see org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget, org.apache.wicket.markup.html.form.Form)
             */
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                ModalWindow.closeCurrent(target);
            }
        }.setDefaultFormProcessing(false));
    }

}
