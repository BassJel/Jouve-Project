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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;

import com.doculibre.constellio.utils.SimpleParams;

public class SimpleParamsUtils {
    
    public static String appendParams(String url, SimpleParams simpleParams) {
        StringBuffer sb = new StringBuffer(url);
        for (String paramName : simpleParams.keySet()) {
            for (String paramValue : simpleParams.getList(paramName)) {
                if (sb.indexOf("?") == -1 && StringUtils.isNotBlank(url)) {
                    sb.append("?");
                } else if (sb.length() > 0){
                    sb.append("&");
                }
                sb.append(paramName + "=" + paramValue);
            }
        }
        return sb.toString();
    }
    
    @SuppressWarnings("unchecked")
    public static SimpleParams toSimpleParams(HttpServletRequest request) {
        SimpleParams params = new SimpleParams();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            List<String> paramValues = Arrays.asList(request.getParameterValues(paramName));
            params.add(paramName, paramValues);
        }
        return params;
    }
    
    @SuppressWarnings("unchecked")
    public static SimpleParams toSimpleParams(PageParameters pageParameters) {
        SimpleParams params = new SimpleParams();
        for (String paramName : (Set<String>) pageParameters.keySet()) {
            String[] paramValues = pageParameters.getStringArray(paramName);
            if (paramValues != null) {
                params.add(paramName, paramValues);
            } else {
            	params.add(paramName, new String[0]);
            }
        }
        return params;
    }
    
    public static PageParameters toPageParameters(SimpleParams simpleParams) {
        PageParameters pageParameters = new PageParameters();
        for (String paramName : simpleParams.keySet()) {
            List<String> paramValues = simpleParams.getList(paramName);
            for (String paramValue : paramValues) {
                pageParameters.add(paramName, paramValue);
            }
        }
        return pageParameters;
    }

}
