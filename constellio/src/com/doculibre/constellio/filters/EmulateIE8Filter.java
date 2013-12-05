package com.doculibre.constellio.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Source: http://stackoverflow.com/questions/2518256/override-intranet-compatibility-mode-ie8
 * 
 * @author Vincent
 */
public class EmulateIE8Filter implements Filter {

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletResponse httpResponse = ((HttpServletResponse) response);
		httpResponse.addHeader("X-UA-Compatible", "IE=8");
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

}