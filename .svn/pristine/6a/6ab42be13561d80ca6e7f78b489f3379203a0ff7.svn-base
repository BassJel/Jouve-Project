package com.doculibre.constellio.wicket.utils;

import javax.servlet.http.Cookie;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

public class CookieUtils {
	
	public static final int DEFAULT_EXPIRY = 60 * 60 * 24 * 365; // 1 year in seconds
	
	public static Cookie[] getCookies() {
		return ((WebRequest) RequestCycle.get().getRequest()).getCookies();
	}

	public static Cookie getCookie(String cookieName) {
		Cookie match = null;
		Cookie[] cookies = getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookieName.equals(cookie.getName())) {
					match = cookie;
					break;
				}
			}
		}
		return match;
	}

	public static String getCookieValue(String cookieName) {
		Cookie cookie = getCookie(cookieName);
		return cookie != null ? cookie.getValue() : null;
	}
	
	public static void setCookie(String cookieName, String cookieValue) {
		setCookie(cookieName, cookieValue, DEFAULT_EXPIRY);
	}
	
	public static void setCookie(String cookieName, String cookieValue, int expiryInSeconds) {
		Cookie cookie = getCookie(cookieName);
		if (cookie == null) {
			cookie = new Cookie(cookieName, cookieValue);
		}
		cookie.setMaxAge(expiryInSeconds);
		((WebResponse) RequestCycle.get().getResponse()).addCookie(cookie);
	}
	
	public static void clearCookie(String cookieName) {
		Cookie cookie = getCookie(cookieName);
		if (cookie != null) {
			((WebResponse) RequestCycle.get().getResponse()).clearCookie(cookie);
		}
	}

}
