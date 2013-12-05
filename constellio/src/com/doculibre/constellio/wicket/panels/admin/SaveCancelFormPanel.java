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
package com.doculibre.constellio.wicket.panels.admin;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.ErrorLevelFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.utils.WicketResourceUtils;

@SuppressWarnings("serial")
public abstract class SaveCancelFormPanel extends Panel {

    private Form form;
    private FeedbackPanel feedbackPanel;
    private Label titleLabel;
    private Button submitButton;
    private Button cancelButton;

    public SaveCancelFormPanel(String id, boolean ajax) {
        super(id);

        form = new Form("form");
        form.add(new SetFocusBehavior(form));
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        titleLabel = new Label("title", getTitleModel());

        if (ajax) {
            submitButton = new AjaxButton("submitButton") {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form) {
                    onSave(target);
                    if (!Session.get().getFeedbackMessages().hasMessage(
                        new ErrorLevelFeedbackMessageFilter(FeedbackMessage.ERROR))) {
                        defaultReturnAction(target);
                    }
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form form) {
                    target.addComponent(feedbackPanel);
                }
            };
            cancelButton = new AjaxButton("cancelButton") {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form) {
                    onCancel(target);
                    defaultReturnAction(target);
                }
            };
        } else {
            submitButton = new Button("submitButton") {
                @Override
                public void onSubmit() {
                    onSave(null);
                    if (!Session.get().getFeedbackMessages().hasMessage(
                        new ErrorLevelFeedbackMessageFilter(FeedbackMessage.ERROR))) {
                        defaultReturnAction();
                    }
                }
            };
            cancelButton = new Button("cancelButton") {
                @Override
                public void onSubmit() {
                    onCancel(null);
                    defaultReturnAction();
                }
            };
        }
        cancelButton.setDefaultFormProcessing(false);

        add(form);
        form.add(feedbackPanel);
        form.add(titleLabel);
        form.add(submitButton);
        form.add(cancelButton);
    }

    public Form getForm() {
        return form;
    }

    public FeedbackPanel getFeedbackPanel() {
        return feedbackPanel;
    }

    public Label getTitleLabel() {
        return titleLabel;
    }

    public Button getSubmitButton() {
        return submitButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    /**
     * Called if this panel doesn't use Ajax.
     */
    protected void defaultReturnAction() {
        this.replaceWith(newReturnComponent(getId()));
    }

    /**
     * Called if this panel is uses Ajax.
     * 
     * @param target
     */
    protected void defaultReturnAction(AjaxRequestTarget target) {
        ModalWindow modal = (ModalWindow) findParent(ModalWindow.class);
        if (modal != null) {
            Component refreshParent = WicketResourceUtils.findOutputMarkupIdParent(modal);
            ModalWindow.closeCurrent(target);
            if (refreshParent != null) {
                target.addComponent(refreshParent);
            }
        } else {
            Component refreshParent = WicketResourceUtils.findOutputMarkupIdParent(this);
            if (refreshParent != null) {
                this.replaceWith(newReturnComponent(getId()));
                target.addComponent(refreshParent);
            }
        }
    }

    protected abstract void onSave(AjaxRequestTarget target);

    protected void onCancel(AjaxRequestTarget target) {
    }

    protected abstract IModel getTitleModel();

    protected abstract Component newReturnComponent(String id);

}
