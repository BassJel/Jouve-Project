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
package com.doculibre.constellio.utils;

import javax.servlet.http.HttpServletRequest;

public class ContextUrlUtils {
	
    public static String getContextUrl(final HttpServletRequest req) 
    { 
        String protocol = req.isSecure() ? "https://" : "http://"; 
        String hostname = req.getServerName(); 
        int port = req.getServerPort(); 
        StringBuffer url = new StringBuffer(128); 
        url.append(protocol); 
        url.append(hostname); 
        if ((port != 80) && (port != 443)) 
        { 
            url.append(":"); 
            url.append(port); 
        } 
        String ctx = req.getSession().getServletContext().getContextPath(); 
        if (!ctx.startsWith("/")) 
        { 
        url.append('/'); 
        } 
        url.append(ctx); 
        if (!ctx.endsWith("/")) 
        { 
        url.append('/'); 
        } 
        return url.toString(); 
    } 
}
