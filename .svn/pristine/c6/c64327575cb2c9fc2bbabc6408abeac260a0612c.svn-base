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
package com.doculibre.constellio.wicket.panels.fold;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

@SuppressWarnings("serial")
public abstract class CookieFoldableSectionPanel extends FoldableSectionPanel {

    public CookieFoldableSectionPanel(String id, String sectionTitleKey) {
        super(id, sectionTitleKey);
    }

    public CookieFoldableSectionPanel(String id, IModel sectionTitleModel) {
        super(id, sectionTitleModel);
    }

    @Override
    protected void onBeforeRender() {
        // Calling super will skip cookie writing
        Boolean readOpenCookie = readOpenCookie();
        if (readOpenCookie != null) {
            super.setOpened(readOpenCookie());
        }
        super.onBeforeRender();
    }

    @Override
    public void setOpened(boolean opened) {
        super.setOpened(opened);
        writeOpenCookie();
    }

    public Boolean readOpenCookie() {
        String cookieName = getCookieName();
        Cookie cookie = getCookie(cookieName);
        return cookie != null ? Boolean.valueOf(cookie.getValue()) : null;
    }

    public void writeOpenCookie() {
        String cookieName = getCookieName();
        Cookie cookie = new Cookie(cookieName, "" + isOpened());
        save(cookie);
    }

    public void clearOpenCookie() {
        String cookieName = getCookieName();
        Cookie cookie = getCookie(cookieName);
        if (cookie != null) {
            clear(cookie);
        }
    }

    /**
     * Gets any cookies for request.
     * 
     * @param name
     *            The name of the cookie to be looked up
     * @return Any cookies for this request
     */
    protected static Cookie getCookie(String name) {
        try {
            return getWebRequest().getCookie(name);
        } catch (NullPointerException ex) {
            // Ignore any app server problem here
        }
        return null;
    }
    
    protected static Cookie[] getCookies(String prefix) {
        try {
            List<Cookie> matchingCookies = new ArrayList<Cookie>();
            Cookie[] cookies = getWebRequest().getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().startsWith(prefix)) {
                        matchingCookies.add(cookie);
                    }
                }
            }
            return matchingCookies.toArray(new Cookie[]{});
        } catch (NullPointerException ex) {
            // Ignore any app server problem here
        }
        return null;
    }

    /**
     * Persist/save the data using Cookies.
     * 
     * @param cookie
     *            The Cookie to be persisted.
     * @return The cookie provided
     */
    protected static Cookie save(final Cookie cookie) {
        if (cookie == null) {
            return null;
        } else {
            cookie.setPath(getWebRequest().getHttpServletRequest().getContextPath());
            // cookie.setPath("/");
            // cookie.setPath(getWebRequest().getContextPath());

            getWebResponse().addCookie(cookie);

            return cookie;
        }
    }

    /**
     * Convenience method for deleting a cookie by name. Delete the cookie by setting its maximum
     * age to zero.
     * 
     * @param cookie
     *            The cookie to delete
     */
    protected static void clear(final Cookie cookie) {
        if (cookie != null) {
            // Delete the cookie by setting its maximum age to zero
            cookie.setMaxAge(0);
            cookie.setValue(null);

            save(cookie);
        }
    }

    /**
     * Convenience method to get the http request.
     * 
     * @return WebRequest related to the RequestCycle
     */
    protected static WebRequest getWebRequest() {
        return (WebRequest) RequestCycle.get().getRequest();
    }

    /**
     * Convenience method to get the http response.
     * 
     * @return WebResponse related to the RequestCycle
     */
    protected static WebResponse getWebResponse() {
        return (WebResponse) RequestCycle.get().getResponse();
    }

    protected abstract String getCookieName();

}
