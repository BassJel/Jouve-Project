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

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

public class WebappUtils {

    private static URL contextPathURL;

    public static String getProtocol() {
        return getContextPathURL().getProtocol();
    }

    public static String getHost() {
        return getContextPathURL().getHost();
    }

    public static int getPort() {
        return getContextPathURL().getPort();
    }

    public static String getContextPath() {
        return getContextPathURL().getPath();
    }
    
    public static URL getContextPathURL(HttpServletRequest httpRequest) {
        StringBuffer requestURL = httpRequest.getRequestURL();
        String contextPath = httpRequest.getContextPath();
        String requestURI = httpRequest.getRequestURI();
        String beforeContextPath = StringUtils.substringBefore(requestURL.toString(), requestURI);
        try {
            return new URL(beforeContextPath + contextPath);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static URL getContextPathURL() {
        init();
        return contextPathURL;
    }
    
    private static void init() {
        if (contextPathURL == null) {
            URL defaultConnectorManagerURL = ConstellioSpringUtils.getDefaultConnectorManagerURL();
            WebappUtils.contextPathURL = defaultConnectorManagerURL;
        }
    }

}
