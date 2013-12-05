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
package com.doculibre.constellio.wicket.behaviors;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * Ajax Wicket behavior that will send an Ajax request to the component to update itself
 * immediately after the page finishes loading. Unlike AjaxLazyLoadPanel's behavior,
 * this one is meant for Ajax components that we sometimes wish are loaded with the page
 * (ex.: progress panel running).
 * 
 * @author vdussault
 */
@SuppressWarnings("serial")
public class AjaxOnLoadSelfUpdatingBehavior extends AbstractDefaultAjaxBehavior {

    @Override
    protected void respond(AjaxRequestTarget target) {
        target.addComponent(getComponent());
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.renderOnDomReadyJavascript(getCallbackScript().toString());
    }

}
