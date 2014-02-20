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
package com.doculibre.constellio.filters;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

public class LocalRequestFilter implements Filter {

    private String[] ignoredPrefixes;

    private static final String[] ACCEPTED_HOSTS = { "127.0.0.1", "localhost", "0:0:0:0:0:0:0:1" };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        Set<String> acceptedAndDynamicHosts = new HashSet<String>();
        acceptedAndDynamicHosts.addAll(Arrays.asList(ACCEPTED_HOSTS));
        
        // Solution adapted from http://www.exampledepot.com/egs/java.net/Local.html
        String hostName = InetAddress.getLocalHost().getHostName();
        InetAddress addrs[] = InetAddress.getAllByName(hostName);
        for (InetAddress addr : addrs) {
            String hostAddress = addr.getHostAddress();
            acceptedAndDynamicHosts.add(hostAddress);
        }

        boolean valid;
        if (isIgnoredRequest(request) || isFileRequest(request)) {
            valid = true;
        } else {
            valid = false;
            String remoteHost = request.getRemoteHost();
            for (String acceptedHost : acceptedAndDynamicHosts) {
                if (remoteHost.equals(acceptedHost)) {
                    valid = true;
                }
            }
        }

        if (valid) {
            // Pass control on to the next filter
            chain.doFilter(request, response);
        } else {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            throw new ServletException("Cannot send request to a servlet from outside the Web application : "
                + httpRequest.getRequestURL());
        }
    }

    private boolean isIgnoredRequest(ServletRequest request) {
        boolean ignoredRequest = false;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contextPath = httpRequest.getContextPath();
        String requestURI = httpRequest.getRequestURI();
        String contextRelativePath = requestURI.substring(contextPath.length());
        for (String ignoredPrefix : ignoredPrefixes) {
            ignoredRequest = contextRelativePath.startsWith(ignoredPrefix);
            if (ignoredRequest) {
                break;
            }
        }
        return ignoredRequest;
    }

    private boolean isFileRequest(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession httpSession = httpRequest.getSession();
        ServletContext servletContext = httpSession.getServletContext();
        String contextPath = httpRequest.getContextPath();
        String requestURI = httpRequest.getRequestURI();
        String contextRelativePath = requestURI.substring(contextPath.length());
        String possibleFilePath = servletContext.getRealPath(contextRelativePath);
        return new File(possibleFilePath).exists();
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        String ignoredPrefixesStr = config.getInitParameter("ignoredPrefixes");
        if (StringUtils.isBlank(ignoredPrefixesStr)) {
            ignoredPrefixes = new String[] { "/app", "/computeSearchResultClick" };
        } else {
            ignoredPrefixes = StringUtils.split(ignoredPrefixesStr, ",");
        }
    }

    @Override
    public void destroy() {
    }

}
