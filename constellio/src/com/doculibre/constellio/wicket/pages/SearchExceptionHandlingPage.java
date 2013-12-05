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

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;

import com.doculibre.constellio.wicket.application.ConstellioApplication;

public class SearchExceptionHandlingPage extends BaseConstellioPage {
    
    public SearchExceptionHandlingPage() {
        super();
    }

    @Override
    protected void onBeforeRender() {
        HttpServletRequest httpRequest = ((WebRequest) RequestCycle.get().getRequest()).getHttpServletRequest();
        String referer = httpRequest.getHeader("referer");
        if (referer != null) {
            RequestCycle.get().setRequestTarget(new RedirectRequestTarget(referer));
        } else {
            setResponsePage(ConstellioApplication.get().getHomePage());
        }
    }

}
