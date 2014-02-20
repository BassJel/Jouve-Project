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
package com.doculibre.constellio.wicket.utils;

import org.apache.wicket.Component;
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;

import com.doculibre.constellio.wicket.pages.BaseConstellioPage;

public class WicketResourceUtils {
	
	public static String urlFor(String basePageRelativeUrl) {
		return RequestCycle.get().urlFor(new ResourceReference(
				BaseConstellioPage.class, 
				basePageRelativeUrl)).toString();
	}
	
	public static Component findOutputMarkupIdParent(Component child) {
	    Component matchingParent;
	    Component firstParent = child.getParent();
	    if (firstParent != null) {
	    	if (firstParent.getOutputMarkupId()) {
	    		matchingParent = firstParent;
	    	} else {
		        // Starts with current object despite what method name lets you think...
		        matchingParent = (Component) firstParent.visitParents(Component.class, new IVisitor() {
		            @Override
		            public Object component(Component component) {
		                if (component.getOutputMarkupId()) {
		                    return component;
		                } else {
		                    return CONTINUE_TRAVERSAL;
		                }
		            }
		        });
	    	}
	    } else {
	        matchingParent = null;
	    }
	    return matchingParent;
	}

}
