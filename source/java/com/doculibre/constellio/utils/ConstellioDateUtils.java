package com.doculibre.constellio.utils;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

public class ConstellioDateUtils {
	
	static final long ONE_HOUR_MILLIS = 60 * 60 * 1000L;
	static final long ONE_DAY_MILLIS = 24 * ONE_HOUR_MILLIS;
	
	/**
	 * @param year
	 * @param month 1=January
	 * @param day 1 for first day
	 * @return
	 */
	public static Date toDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		return cal.getTime();
	}
	
	public static int getDayOfMonth(Date date) {
		return getField(date, Calendar.DAY_OF_MONTH);
	}
	
	public static int getDaysInMonth(Date date) {
		Calendar cal = DateUtils.toCalendar(date);
		int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		return daysInMonth;
	}
	
	public static int getDaysInYear(Date date) {
		Calendar cal = DateUtils.toCalendar(date);
		int daysInYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR);
		return daysInYear;
	}
	
	public static int getDaysBetween(Date startDate, Date endDate) {
		return (int) ((endDate.getTime() - startDate.getTime()) / ONE_DAY_MILLIS);
	}
	
	public static Date getBeginningOfDay(Date date) {
		Calendar cal = DateUtils.toCalendar(date);
		cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
		return cal.getTime();
	}
	
	public static Date getEndOfDay(Date date) {
		Calendar cal = DateUtils.toCalendar(date);
		cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
		return cal.getTime();
	}
	
	/**
	 * 0 = January
	 * 
	 * @param date
	 * @return
	 */
	public static int getMonth(Date date) {
		return getField(date, Calendar.MONTH);
	}
	
	public static int getYear(Date date) {
		return getField(date, Calendar.YEAR);
	}
	
	public static Date getFirstDayOfMonth(Date date) {
		Calendar cal = DateUtils.toCalendar(date);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}
	
	public static Date getLastDayOfMonth(Date date) {
		Calendar cal = DateUtils.toCalendar(date);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}
	
	public static boolean isFirstDayOfMonth(Date date) {
		int dayOfMonth = getField(date, Calendar.DAY_OF_MONTH);
		return dayOfMonth == 1;
	}
	
	public static boolean isLastDayOfMonth(Date date) {
		int dayOfMonth = getField(date, Calendar.DAY_OF_MONTH);
		int daysInMonth = getDaysInMonth(date);
		return dayOfMonth == daysInMonth;
	}
	
	public static Date getFirstDayOfYear(Date date) {
		Calendar cal = DateUtils.toCalendar(date);
		cal.set(Calendar.DAY_OF_YEAR, cal.getActualMinimum(Calendar.DAY_OF_YEAR));
		return cal.getTime();
	}
	
	public static Date getLastDayOfYear(Date date) {
		Calendar cal = DateUtils.toCalendar(date);
		cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
		return cal.getTime();
	}
	
	public static boolean isFirstDayOfYear(Date date) {
		int dayOfYear = getField(date, Calendar.DAY_OF_YEAR);
		return dayOfYear == 1;
	}
	
	public static boolean isLastDayOfYear(Date date) {
		int dayOfYear = getField(date, Calendar.DAY_OF_YEAR);
		int daysInYear = getDaysInYear(date);
		return dayOfYear == daysInYear;
	}
	
	public static boolean isFirstMonthOfYear(Date date) {
		int month = getField(date, Calendar.MONTH);
		return month == 0;
	}
	
	public static boolean isLastMonthOfYear(Date date) {
		int month = getField(date, Calendar.MONTH);
		return month == 11;
	}
	
	public static int getField(Date date, int field) {
		Calendar cal = DateUtils.toCalendar(date);
		return cal.get(field);
	}
	
	public static boolean isSameDay(Date date1, Date date2) {
		return DateUtils.isSameDay(date1, date2);
	}
	
	public static boolean isSameMonth(Date date1, Date date2) {
		boolean sameMonth;
		if (isSameYear(date1, date2)) {
			int month1 = getField(date1, Calendar.MONTH);
			int month2 = getField(date2, Calendar.MONTH);
			sameMonth = month1 == month2;
		} else {
			sameMonth = false;
		}
		return sameMonth;
	}
	
	public static boolean isSameYear(Date date1, Date date2) {
		int year1 = getYear(date1);
		int year2 = getYear(date2);
		return year1 == year2;
	}
	
	public static void main(String[] args) {
		Date date = toDate(2012, 2, 29);
		System.out.println(DateUtils.addYears(date, 1));
	}

}
