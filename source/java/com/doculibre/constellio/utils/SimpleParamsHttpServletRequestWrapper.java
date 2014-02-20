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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.collections.iterators.IteratorEnumeration;

public class SimpleParamsHttpServletRequestWrapper extends HttpServletRequestWrapper {
    
    private SimpleParams simpleParams;

    public SimpleParamsHttpServletRequestWrapper(HttpServletRequest request, SimpleParams simpleParams) {
        super(request);
        this.simpleParams = simpleParams;
    }
    
    @Override
    public String getParameter(String name) {
        return simpleParams.getString(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map getParameterMap() {
        Map<String, String[]> paramsMap = new HashMap<String, String[]>();
        for (String paramName : simpleParams.keySet()) {
            paramsMap.put(paramName, simpleParams.getList(paramName).toArray(new String[0]));
        }
        return Collections.unmodifiableMap(paramsMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getParameterNames() {
        Iterator<String> paramNamesIt = simpleParams.keySet().iterator();
        return new IteratorEnumeration(paramNamesIt);
    }

    @Override
    public String[] getParameterValues(String name) {
        return simpleParams.getList(name).toArray(new String[0]);
    }

}
