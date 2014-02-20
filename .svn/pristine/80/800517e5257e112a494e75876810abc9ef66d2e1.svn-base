package com.doculibre.constellio.utils;

import org.apache.commons.lang.StringUtils;

public class ConstellioStringUtils {
	
	public static boolean isEmpty(String text) {
		boolean empty;
		if (StringUtils.isNotBlank(text)) {
			text = StringUtils.replace(text, "\r", "");
			text = StringUtils.replace(text, "\n", "");
			text = StringUtils.replace(text, "\t", "");
			text = StringUtils.replace(text, " ", "");
			char[] chars = text.toCharArray();
			for (char c : chars) {
				if (Character.isSpaceChar(c) || Character.isWhitespace(c)) {
					text = StringUtils.replace(text, "" + c, "");
				}
			}
			empty = StringUtils.isBlank(text);
		} else {
			empty = true;
		}
		return empty;
	}

}
