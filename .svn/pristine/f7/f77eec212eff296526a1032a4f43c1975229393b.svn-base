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
package com.doculibre.constellio.feedprotocol;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RFC822DateUtil {
	
	private static final TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone("GMT+0");
	private static final Calendar GMT_CALENDAR = Calendar.getInstance(TIME_ZONE_GMT);
	private static final SimpleDateFormat RFC822_DATE_FORMAT = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss z", Locale.ENGLISH);

	static {
		RFC822_DATE_FORMAT.setCalendar(GMT_CALENDAR);
		RFC822_DATE_FORMAT.setLenient(true);
		RFC822_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	public static String format(Date date) {
		return RFC822_DATE_FORMAT.format(date);
	}
	
	public static Date parse(String date) throws ParseException {
		return RFC822_DATE_FORMAT.parse(date);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(parse("toto"));
	}
	
}
