/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: Daniel Hardman
 * Created: Sep 18, 2009
 */
package verse.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import verse.dbc.precondition;

/**
 * Routines to work with and manipulate dates.
 */
public class DateUtil {

	/**
	 * The UTC timezone.
	 */
	public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	
	/**
	 * Check is timezone ID case sensitive. Function getTimeZone returns
	 * GMT timezone if it can't recognize given ID.
	 */
	public static final boolean TZ_ID_CASE_SENSITIVE =
		(TimeZone.getTimeZone("gmt+1").getRawOffset() == 0);
	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

	/**
	 * The date format spec used to create or validate dates as specified in RFC
	 * 1123. This is the format for dates in http Last-Modified: headers, except
	 * that http requires the time zone to be UTC.
	 */
	public static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String RFC850_PATTERN = "EEEE, dd-MM-yy HH:mm:ss zzz";
	public static final String ASCTIME_PATTERN1 = "EEE MMM dd HH:mm:ss yyyy";
	public static final String ASCTIME_PATTERN2 = "EEE MMM  d HH:mm:ss yyyy";

	public static final int MIDNIGHT_MILLISEC = 24*60*60*1000;
	
	/**
	 * Convert a date/time to minute-precise military time [0000-2400).
	 * 
	 * @return an int in the range [0000-2400).
	 */
	public static int toMilitary(Calendar cal) {
		return (cal.get(Calendar.HOUR_OF_DAY) * 100) + cal.get(Calendar.MINUTE);
	}

	/**
	 * Convert a date/time to precise military time [0000-2359.59999]. The first
	 * two digits after the decimal point represent seconds, and the last 3
	 * represent milliseconds.
	 * 
	 * @return a double in the range [0000-2359.59999].
	 */
	public static double toMilitaryMillis(Calendar cal) {
		double n = toMilitary(cal);
		n += (cal.get(Calendar.SECOND) / 100.0);
		n += (cal.get(Calendar.MILLISECOND) / 100000.0);
		return n;
	}

	/**
	 * @return the current time in the local time zone, in minute-precise
	 *         military format [0000-2400)
	 */
	public static int militaryNow() {
		Calendar cal = TimeProvider.getCalendar();
		return toMilitary(cal);
	}

	/**
	 * @return the current time in the local time zone, in millisecond-precise
	 *         military format [0000-2359.59999]
	 */
	public static double militaryMillisNow() {
		Calendar cal = TimeProvider.getCalendar();
		return toMilitaryMillis(cal);
	}

	/**
	 * @return the current time in UTC, in minute-precise military format
	 *         [0000-2400)
	 */
	public static int militaryNowUTC() {
		Calendar cal = TimeProvider.getCalendar(UTC);
		return toMilitary(cal);
	}

	/**
	 * @return the current time in UTC, in millisecond-precise military format
	 *         [0000-2359.59999]
	 */
	public static double militaryMillisNowUTC() {
		Calendar cal = TimeProvider.getCalendar(UTC);
		return toMilitaryMillis(cal);
	}

	/**
	 * Convert minute-precise military time [0000-2400) into elapsed minutes
	 * since midnight.
	 * 
	 * @param military
	 *            an int in the range [0000-2400). Invalid minutes (e.g., > 59)
	 *            are treated like 59.
	 * @return number of minutes that have elapsed since midnight.
	 */
	public static int militaryToSinceMidnight(int military) {
		precondition.checkAndExplain(military >= 0, "military time value must be >= 0, not %d", military);
		if (military >= 2400) {
			military = military % 2400;
		}
		int minutes = military % 100;
		int hours = military / 100;
		return hours * 60 + Math.min(59, minutes);
	}

	/**
	 * Convert millisecond-precise military time [0000-2359.59999] into elapsed
	 * milliseconds since midnight.
	 * 
	 * @param military
	 *            a double in the range [0000-2359.59999]. Invalid minutes or
	 *            seconds (e.g., > 59) are treated like 59.
	 * @return number of milliseconds that have elapsed since midnight.
	 */
	public static int militaryMillisToSinceMidnight(double military) {
		precondition.checkAndExplain(military >= 0.0, "military time value must be >= 0, not %f", military);
		if (military >= 2400.0) {
			military = military % 2400.0;			
		}
		int minutes = militaryToSinceMidnight((int)military);
		// We're ignoring the possibility of leap seconds by constraining to
		// 59 or less...
		int seconds = Math.min(59, ((int)(military * 100)) % 100);
		int ms = ((int)(military * 100000)) % 1000;
		return minutes * 60000 + seconds * 1000 + ms;
	}
	
	private static long extractUnit(long ms, StringBuilder sb, String unit, long divisor) {
		long value = ms / divisor;
		if (value > 0) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(value);
			sb.append(' ');
			sb.append(unit);
			ms = ms % divisor;
		}
		return ms;		
	}
	
	/**
	 * Convert a number of milliseconds into a friendly string describing
	 * a time span -- something like "3 h 20 m 19.2 s".
	 * @param ms Milliseconds of elapsed time.
	 * @return friendly text
	 */
	public static String elapsedMillisAsFriendlyText(long ms) {
		StringBuilder sb = new StringBuilder();
		precondition.checkAndExplain(ms >= 0, "ms must be, not %d", ms);
		ms = extractUnit(ms, sb, "d", 86400000);
		if (ms > 0) {
			ms = extractUnit(ms, sb, "h", 3600000);
			if (ms > 0) {
				ms = extractUnit(ms, sb, "m", 60000);
				if (ms > 0) {
					if (ms % 1000 == 0) {
						extractUnit(ms, sb, "s", 1000);
					} else {
						double dbl = ms / 1000.0;
						if (sb.length() > 0) {
							sb.append(' ');
						}
						sb.append(String.format("%.3f s", dbl));
					}
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * @return true if a DateFormat spec contains any of the specified chars
	 * in unescaped form.
	 */
	public static boolean formatContainsAny(DateFormat df, CharSequence any) {
		if (df instanceof SimpleDateFormat) {
			SimpleDateFormat sdf = (SimpleDateFormat)df;
			String pat = sdf.toPattern();
			for (int i = 0; i < pat.length(); ++i) {
				char c = pat.charAt(i);
				if (c == '\'') {
					int j = pat.indexOf('\'', i + 1);
					if (j == -1) {
						break;
					}
					i = j;
				} else {
					for (int j = 0; j < any.length(); ++j) {
						char ch = any.charAt(j);
						if (ch == c) {
							return true;
						}
					}
				}				
			}	
		}
		return false; 
	}


	public static final DateFormat STD_DATE_FORMAT1;
	public static final DateFormat STD_DATE_FORMAT2;
	public static final DateFormat STD_DATE_FORMAT3;
	public static final DateFormat STD_DATE_FORMAT4;
	private static final DateFormat STD_DATE_FORMAT5;
	private static final DateFormat STD_DATE_FORMAT6;
	public static final DateFormat STD_DATE_FORMAT7;
	private static final Pattern TZ_PAT = Pattern.compile(
			"(\\s+Z|[A-Z]{3}|(GMT|UTC) ?[-+](\\d{1,2}(:\\d\\d)?|\\d{4})|" +
			"\\s+[A-Z_]+/[A-Z_]+)\\s*$", Pattern.CASE_INSENSITIVE);

	static DateFormat newSimpleDateFormat(String fmt) {
		DateFormat df = new SimpleDateFormat(fmt);
		return newDateFormat(df);
	}
	
	static DateFormat newDateFormat(DateFormat df) {
		df.setLenient(true);
		df.setTimeZone(UTC);
		return df;
	}

	static {
		STD_DATE_FORMAT1 = newSimpleDateFormat("yyyy-MM-dd");
		STD_DATE_FORMAT2 = newSimpleDateFormat("yyyy-MM-dd kk:mm");
		STD_DATE_FORMAT3 = newSimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		STD_DATE_FORMAT4 = newSimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS");
		STD_DATE_FORMAT5 = newDateFormat(DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.SHORT));
		STD_DATE_FORMAT6 = newDateFormat(DateFormat.getDateInstance(DateFormat.SHORT));
		STD_DATE_FORMAT7 = newSimpleDateFormat("yyyy-MM-dd kk:mm:ss z");
	}
	
	/**
	 * Convenience method that looks for dates in one of several standard formats,
	 * including some variations on ISO 8601 and the short date/datetime for the
	 * current locale. Also accepts timezone identifiers on the end.
	 *  
	 * @param txt
	 * @return A parsed date, if it could be understood.
	 * @throws ParseException if format cannot be understood.
	 */
	public static Date parseStandardDate(String txt) throws ParseException {
		return parseStandardDate(txt, UTC);
	}

	/**
	 * Convenience method that looks for dates in one of several standard formats,
	 * including some variations on ISO 8601 and the short date/datetime for the
	 * current locale. Also accepts timezone identifiers on the end.
	 *  
	 * @param txt
	 * @param assumedTz TimeZone to assume, if none is given explicitly.
	 * @return A parsed date, if it could be understood.
	 * @throws ParseException if format cannot be understood.
	 */
	public synchronized static Date parseStandardDate(String txt, TimeZone assumedTz) throws ParseException {
		Matcher m = TZ_PAT.matcher(txt);
		if (m.find()) {
			txt = txt.substring(0, m.start());
			String id = m.group(1).trim();
			if (id.equalsIgnoreCase("z")) {
				assumedTz = UTC;
			} else {
				assumedTz = DateUtil.findTimeZone(id);
			}			
		}
		boolean utc = (assumedTz == null || assumedTz.hasSameRules(UTC));
		txt = txt.trim();
		DateFormat df = STD_DATE_FORMAT5;
		int hyphens = str_util.count(txt, '-');
		if (hyphens >= 2) {
			int digits = str_util.countAny(txt, "0123456789");
			if (digits >= 8) {
				txt = txt.replace('T', ' ');
				if (digits == 8) {
					df = STD_DATE_FORMAT1;
				} else if (digits <= 12) {
					df = STD_DATE_FORMAT2;
				} else if (digits <= 14) {
					df = STD_DATE_FORMAT3;
				} else {
					df = STD_DATE_FORMAT4;
					// We've claimed we are going to have 3 millisec digits.
					// If we have less than that, Java's parser misinterprets
					// them -- .23 is interpreted as .023 instead of .230. So
					// we have to pad...
					int i = str_util.lastIndexOfAny(txt, ":.,");
					if (i > -1) {
						char c = txt.charAt(i);
						if (c != ':') {
							// Handle european conventions where , is the decimal point char.
							if (c == ',') {
								txt = txt.substring(0, i) + '.' + txt.substring(i + 1);
							}
							int msWidth = txt.length() - (i + 1);
							if (msWidth < 3) {
								txt = str_util.padRight(txt, txt.length() + 3 - msWidth, '0');
							}
						}
					}
				}
			}
		}
		try {
			if (!utc) {
				df = (DateFormat)df.clone();
				df.setTimeZone(assumedTz);
			}
			return df.parse(txt);
		} catch (ParseException e) {
			df = utc ? STD_DATE_FORMAT6 : (DateFormat)STD_DATE_FORMAT6.clone();
			if (!utc) {
				df.setTimeZone(assumedTz);
			}
			return STD_DATE_FORMAT6.parse(txt);
		}
	}

	/**
	 * Convert a date to a string in an ISO standard format that preserves the
	 * correct precision.
	 */
	public static String formatStandardDate(Date d) {
		DateFormat df = getPrecisionPreservingFormat(d);
		return formatStandardDate(d, df);
	}

	/**
	 * Convert a date to a string in an ISO standard format, using a format
	 * that is as precise as the caller specifies.
	 * @param df see STD_DATE_FORMAT1 through STD_DATE_FORMAT4
	 */
	public synchronized static String formatStandardDate(Date d, DateFormat df) {
		String txt = df.format(d);
		// For some reason java sometimes reports the hour as 24:00 at midnight,
		// instead of 00:00...
		return txt.replace(" 24:", " 00:");
	}
	
	public static String formatHttpHeaderDateFormat(Date d) {
		DateFormat format = new SimpleDateFormat(
				RFC1123_PATTERN.replace(" zzz", " 'GMT'"), Locale.US);
		return format.format(d);
	}

	/**
	 * @param d
	 * @return The standard {@link DateFormat} that will preserve all precision
	 * available in a given Date.
	 */
	public /*synchronized*/ static DateFormat getPrecisionPreservingFormat(Date d) {
		DateFormat df = STD_DATE_FORMAT1;
		long millisecs = d.getTime();
		if (millisecs % 1000 != 0)
			df = STD_DATE_FORMAT4;
		else {
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTimeZone(UTC);
			cal.setTime(d);
			int secs = cal.get(Calendar.SECOND);
			if (secs != 0)
				df = STD_DATE_FORMAT3;
			else {
				int hr = cal.get(Calendar.HOUR);
				if (hr != 0 || cal.get(Calendar.MINUTE) != 0)
					df = STD_DATE_FORMAT2;
			}
		}
		return df;
	}

	/**
	 * Case insensitive implementation of getTimeZone()
	 * @param id - the ID for a TimeZone, either an abbreviation such as "PST",
	 * a full name such as "America/Los_Angeles", or a custom ID such as "GMT-8:00".
	 * Note that the support of abbreviations is for JDK 1.1.x compatibility only and
	 * full names should be used. 
	 * @return the specified TimeZone, or the GMT zone if the given ID cannot be
	 * understood.
	 */
	public static TimeZone findTimeZone(String id) {
		// First try to get timezone directly
		TimeZone ret = TimeZone.getTimeZone(id);
		if (!TZ_ID_CASE_SENSITIVE || !ret.equals(GMT)) {
			return ret;
		}
		// Search it case insensitive
		for (String zoneId : TimeZone.getAvailableIDs()) {
			if (timeZoneMatches(zoneId, id)) {
				return TimeZone.getTimeZone(zoneId);
			}
		}
		// If found nothing try to find in upper case. Ex. "gmt+1"
		return TimeZone.getTimeZone(id.toUpperCase());
	}
	
	/**
	 * Compares two timezone IDs
	 * @param zoneId timezone ID to test
	 * @param id user-specified (probably incorrect in case) timezone
	 * @return true if id matches specified timezone
	 */
	public static boolean timeZoneMatches(String zoneId, String id) {
		if (zoneId.equalsIgnoreCase(id)) {
			return true;
		}
		// Some additional cases may be added
		return false;
	}
}
