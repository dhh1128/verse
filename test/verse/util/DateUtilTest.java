/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: Daniel Hardman
 * Created: Sep 21, 2009
 */
package verse.util;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

/**
 * 
 */
public class DateUtilTest {

	@Test
	public void testToMilitaryTime() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(2003, 11, 22, 19, 04, 21);
		assertEquals(1904, DateUtil.toMilitary(cal));
	}
	
	@Test
	public void testUTC() {
		TimeZone tz = TimeZone.getDefault();
		if (tz.getOffset(new Date().getTime()) != 0) {
			assertTrue(DateUtil.militaryNowUTC() != DateUtil.militaryNow());
		}
	}
	
	@Test
	public void testMilitaryToMinutes() {
		assertEquals(17 * 60 + 59, DateUtil.militaryToSinceMidnight(1759));
		assertEquals(0, DateUtil.militaryToSinceMidnight(0));
		assertEquals(0, DateUtil.militaryToSinceMidnight(2400));
		assertEquals(0, DateUtil.militaryToSinceMidnight(4800));
		assertEquals(10, DateUtil.militaryToSinceMidnight(10));
		assertEquals((int)(3.5 * 60), DateUtil.militaryToSinceMidnight(330));
	}
	
	@Test
	public void testMilitaryMsToMillisecsSinceMidnight() {
		assertEquals(100, DateUtil.militaryMillisToSinceMidnight(1800) - DateUtil.militaryMillisToSinceMidnight(1759.599));
		assertEquals(0, DateUtil.militaryMillisToSinceMidnight(0));
		assertEquals(0, DateUtil.militaryMillisToSinceMidnight(2400));
		assertEquals(0, DateUtil.militaryMillisToSinceMidnight(4800));
		assertEquals(10 * 60000, DateUtil.militaryMillisToSinceMidnight(10));
		assertEquals((int)(3.5 * 60) * 60000, DateUtil.militaryMillisToSinceMidnight(330));
	}
	
	@Test
	public void testElapsedToFriendly() {
		assertEquals("1 h", DateUtil.elapsedMillisAsFriendlyText(3600000));
		assertEquals("1 h " + String.format("%.3f", 0.001) + " s", 
				DateUtil.elapsedMillisAsFriendlyText(3600001));
		assertEquals("1 m 23 s", DateUtil.elapsedMillisAsFriendlyText(83000));
		assertEquals("1 h 23 s", DateUtil.elapsedMillisAsFriendlyText(3623000));
	}
	
	@Test
	public void testParseFormatRoundTrip() throws ParseException {
		final String[][] LOCALES = new String[][] { 
				new String[] {"en", "US"}, 
				new String[] {"ru", "RU"},
				new String[] {"de", "DE"},
				new String[] {"ja", "JP"}
		};
		String originalCountry = System.getProperty("user.country");
		String originalLanguage = System.getProperty("user.language");
		String originalVariant = System.getProperty("user.variant");
		try {
			for (String[] localePair : LOCALES) {
				System.setProperty("user.country", localePair[1]);
				System.setProperty("user.language", localePair[0]);
				System.setProperty("user.variant", "");
				String locale = localePair[0] + "_" + localePair[1];
				assertEquals(locale, "1877-08-12", DateUtil.formatStandardDate(DateUtil.parseStandardDate("1877-08-12")));
				assertEquals(locale, "1877-08-12", DateUtil.formatStandardDate(DateUtil.parseStandardDate("1877-08-12 Z")));
				assertEquals(locale, "1877-08-12", DateUtil.formatStandardDate(DateUtil.parseStandardDate("1877-08-12 GMT")));
				assertEquals(locale, "1877-08-12", DateUtil.formatStandardDate(DateUtil.parseStandardDate("1877-08-12 UTC")));
				assertEquals(locale, "1877-08-12", DateUtil.formatStandardDate(DateUtil.parseStandardDate("1877-08-12 gmt+0")));
				assertEquals(locale, "1877-08-12 07:00", DateUtil.formatStandardDate(DateUtil.parseStandardDate("1877-08-12 gmt-7")));
				assertEquals(locale, "1877-08-12 08:00", DateUtil.formatStandardDate(DateUtil.parseStandardDate("1877-08-12 America/Los_Angeles")));
				assertEquals(locale, "1877-08-12 14:00", DateUtil.formatStandardDate(
						DateUtil.parseStandardDate("1877-08-12 07:00 america/denver")));
			}
		} finally {
			if (originalCountry == null) {
				System.clearProperty("user.country");
			} else {
				System.setProperty("user.country", originalCountry);
			}
			if (originalLanguage == null) {
				System.clearProperty("user.language");
			} else {
				System.setProperty("user.language", originalLanguage);
			}
			if (originalVariant == null) {
				System.clearProperty("user.variant");
			} else {
				System.setProperty("user.variant", originalVariant);
			}
		}
	}
	
	@Test
	public void test2400Becomes0000() {
		// Java oddly reports the time as 24:00 at midnight sometimes; this verifies
		// that we've worked around that bug.
		assertEquals("1970-01-02 00:00:01", DateUtil.formatStandardDate(new Date(86401000)));
		assertEquals("1970-01-02 01:00:01", DateUtil.formatStandardDate(new Date(86401000 + 3600000)));
	}
}
