package com.doculibre.ldap;

import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;

public class TimeStringFormater {
	private static final String DAY_DELIMITER = "j";

	private static final String HOUR_DELIMITER = "h";

	private static final String MNS_DELIMITER = "mn";

	/**
	 * 
	 * @param timeString
	 *            follow this format (\\d*j)?(\\d*h)?(\\d*mn)? (j == jour)
	 * @return
	 */
	public static int getTimeInMinutes(String timeString) {
		int numberOfdays = 0;
		if (timeString.contains(DAY_DELIMITER)) {
			numberOfdays = getFirstIntBeforeDelemiter(timeString, DAY_DELIMITER);
			timeString = StringUtils.substringAfter(timeString, DAY_DELIMITER);
		}
		int numberOfHours = 0;
		if (timeString.contains(HOUR_DELIMITER)) {
			numberOfHours = getFirstIntBeforeDelemiter(timeString, HOUR_DELIMITER);
			timeString = StringUtils.substringAfter(timeString, HOUR_DELIMITER);
		}
		int numberOfMns = getFirstIntBeforeDelemiter(timeString, MNS_DELIMITER);
		return (numberOfdays * 24 + numberOfHours) * 60 + numberOfMns;
	}

	private static int getFirstIntBeforeDelemiter(String timeString, String delemiter) {
		String intBeforeDelimiter = StringUtils.substringBefore(timeString, delemiter);
		try {
			return Integer.valueOf(intBeforeDelimiter);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static Boolean isValid(String timeString) {
		return Pattern.matches("(\\d*j)?(\\d*h)?(\\d*mn)?", timeString);
	}

	public static int getTimeInSeconds(String timeString) {
		return getTimeInMinutes(timeString) * 60;
	}

	public static int getTimeInMilliSeconds(String timeString) {
		return getTimeInSeconds(timeString) * 1000;
	}

	public static void main(String[] args) {
		Assert.assertEquals(2, getTimeInMinutes("2mn"));
		Assert.assertEquals(62, getTimeInMinutes("1h2mn"));
		Assert.assertEquals(60, getTimeInMinutes("1h"));
		Assert.assertEquals(24 * 60 + 62, getTimeInMinutes("1j1h2mn"));
		Assert.assertEquals(24 * 60 + 60, getTimeInMinutes("1j1h"));
		Assert.assertEquals(24 * 60, getTimeInMinutes("1j"));

		Assert.assertEquals(Boolean.TRUE, isValid("2mn"));
		Assert.assertEquals(Boolean.TRUE, isValid("1h2mn"));
		Assert.assertEquals(Boolean.TRUE, isValid("1h"));
		Assert.assertEquals(Boolean.TRUE, isValid("1j1h2mn"));
		Assert.assertEquals(Boolean.TRUE, isValid("1j1h"));
		Assert.assertEquals(Boolean.TRUE, isValid("1j"));

		Assert.assertEquals(Boolean.FALSE, isValid("2mn1h"));
		Assert.assertEquals(Boolean.FALSE, isValid("1h2mn1j"));
		Assert.assertEquals(Boolean.FALSE, isValid("1s"));
		System.out.println("success!");
	}
}