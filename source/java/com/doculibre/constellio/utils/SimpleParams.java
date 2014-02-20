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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

@SuppressWarnings("serial")
public class SimpleParams implements Serializable {

    private Map<String, List<String>> params = new HashMap<String, List<String>>();

    public SimpleParams() {
    }

    public Set<String> keySet() {
        return params.keySet();
    }

    public String getString(String paramName) {
        String stringParamValue;
        List<String> listParamValue = getList(paramName);
        if (!listParamValue.isEmpty()) {
            stringParamValue = listParamValue.get(0);
        } else {
            stringParamValue = null;
        }
        return stringParamValue;
    }

    public List<String> getList(String paramName) {
        List<String> paramValues = params.get(paramName);
        if (paramValues == null) {
            paramValues = new ArrayList<String>();
            params.put(paramName, paramValues);
        }
        return paramValues;
    }

    public void add(String paramName, String paramValue) {
        List<String> paramValues = getList(paramName);
        paramValues.add(paramValue);
    }

    public void add(String paramName, String[] paramValues) {
        for (int i = 0; i < paramValues.length; i++) {
            String paramValue = paramValues[i];
            add(paramName, paramValue);
        }
    }

    public void add(String paramName, List<String> paramValues) {
        for (int i = 0; i < paramValues.size(); i++) {
            String paramValue = paramValues.get(i);
            add(paramName, paramValue);
        }
    }

    public void addAll(SimpleParams simpleParams) {
        params.putAll(simpleParams.params);
    }
    
    public void put(String paramName, String paramValue) {
        remove(paramName);
        add(paramName, paramValue);
    }
    
    public void put(String paramName, String[] paramValues) {
        remove(paramName);
        add(paramName, paramValues);
    }
    
    public void put(String paramName, List<String> paramValues) {
        remove(paramName);
        add(paramName, paramValues);
    }

    public void remove(String paramName) {
        params.remove(paramName);
    }

    public void addAll(Map<String, Object> params) {
        for (String paramName : params.keySet()) {
            Object paramValue = params.get(paramName);
            if (paramValue instanceof String[]) {
                String[] paramValueArray = (String[]) paramValue;
                add(paramName, Arrays.asList(paramValueArray));
            } else if (paramValue instanceof String) {
                String paramValueString = (String) paramValue;
                add(paramName, paramValueString);
            } else {
                add(paramName, paramValue.toString());
            }
        }
    }

    public String toString() {
        return toString("&");
    }

    public String toString(String delim) {
        StringBuffer sb = new StringBuffer();
        
        //parameters are always in the same order
        List<String> paramKeys = new ArrayList<String>();
        paramKeys.addAll(params.keySet());
        Collections.sort(paramKeys);
        
        for (String paramName : paramKeys) {
            for (String paramValue : getList(paramName)) {
                if (sb.length() > 0) {
                    sb.append(delim);
                }
                sb.append(paramName);
                sb.append("=");
                sb.append(paramValue);
            }
        }
        return sb.toString();
    }

    public void parse(String query) {
        parse(query, "&");
    }

    public void parse(String query, String delim) {
        StringBuffer sb = new StringBuffer(query);
        if (sb.indexOf("?") != -1) {
            sb.delete(0, sb.indexOf("?"));
        }
        StringTokenizer stParams = new StringTokenizer(sb.toString(), delim);
        while (stParams.hasMoreTokens()) {
            String token = stParams.nextToken();
            StringTokenizer stNameValue = new StringTokenizer(token, "=");
            String paramName = stNameValue.nextToken().trim();
            if (stNameValue.hasMoreTokens()) {
                String paramValue = stNameValue.nextToken().trim();
                add(paramName, paramValue);
            }
        }
    }

    public SimpleParams removeNavigationParams() {

    	List<String> pageParams = new ArrayList<String>();
    	for(String param : params.keySet()) {
    		if (param.contains("page")) {
    			pageParams.add(param);
    		}
    	}
    	for(String param : pageParams) {
    		remove(param);
    	}
    	
    	return this;
    }

	public void encodeURL(String charSet) {
		List<String> paramNames = new ArrayList<String>(keySet());
		for (String paramName : paramNames) {
			List<String> paramValues = getList(paramName);
			List<String> encodedParamValues = new ArrayList<String>();
			for (String paramValue : paramValues) {
				try {
					String encodedParamValue = URLEncoder.encode(paramValue, charSet);
					encodedParamValues.add(encodedParamValue);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			}
			put(paramName, encodedParamValues);
		}
	}

	public void decodeURL(String charSet) {
		List<String> paramNames = new ArrayList<String>(keySet());
		for (String paramName : paramNames) {
			List<String> paramValues = getList(paramName);
			List<String> decodedParamValues = new ArrayList<String>();
			for (String paramValue : paramValues) {
				try {
					String decodedParamValue = URLDecoder.decode(paramValue, charSet);
					decodedParamValues.add(decodedParamValue);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			}
			put(paramName, decodedParamValues);
		}
	}

	public void convertCharSet(String fromCharSet, String toCharSet) {
		List<String> paramNames = new ArrayList<String>(keySet());
		for (String paramName : paramNames) {
			List<String> paramValues = getList(paramName);
			List<String> convertedParamValues = new ArrayList<String>();
			for (String paramValue : paramValues) {
				String convertedParamValue = CharSetUtils.convert(paramValue, fromCharSet, toCharSet);
				convertedParamValues.add(convertedParamValue);
			}
			put(paramName, convertedParamValues);
		}
	}
    
}
