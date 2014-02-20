package com.doculibre.constellio.wicket.panels.admin.indexing;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderResponse;

public class OpenWindowOnLoadBehavior extends AbstractDefaultAjaxBehavior {
    @Override
    protected void respond(AjaxRequestTarget target) {
        ModalWindow window = (ModalWindow) getComponent();
        window.show(target);
    }
    @Override
    public void renderHead(IHeaderResponse response) {
        response.renderOnLoadJavascript(getCallbackScript().toString());
    }
}
