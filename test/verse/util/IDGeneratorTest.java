/**
 * $Id$
 * 
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 * 
 */
package verse.util;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Test;

import verse.dbc.contract_violation;

public class IDGeneratorTest {

	@Test(expected=contract_violation.class)
	public void testNullPrefix() {
		IDGenerator.getNext(null);
	}
	
	@Test
	public void testEmptyPrefix() {
		String s = IDGenerator.getNext("");
		assertNotNull(s);
		assertTrue(!s.startsWith("-"));
	}
	
	@Test
	public void testRepeatedIDsInSameMinute() {
		// Artificially freeze the system time for the duration of this unit test.	
		TimeProvider.HardCoded fake = new TimeProvider.HardCoded();
		// Set fake time to 1800 hrs on 2009-10-05
		fake.set(2009, 10, 5, 18, 0, 0, 0);
		TimeProvider old = TimeProvider.override(fake);
		try {
			String s1 = IDGenerator.getNext("x");
			assertNotNull(s1);
			assertTrue(s1.startsWith("x-"));
			String last = "";
			for (int i = 0; i < 5; ++i) {
				String s2 = IDGenerator.getNext("x");
				assertNotNull(s2);
				assertTrue(s2.startsWith(s1));
				assertTrue(!s1.equals(s2));
				assertTrue(!last.equals(s2));
				last = s2;
			}
		} finally {
			TimeProvider.override(old);
		}
	}
	
	@Test
	public void testPaddedTime() {
		// Artificially freeze the system time for the duration of this unit test.	
		TimeProvider.HardCoded fake = new TimeProvider.HardCoded();
		TimeProvider old = TimeProvider.override(fake);
		// Figure out how to generate an artificial time that's guaranteed to be in
		// the early morning, local time. There are lots of ways to do this, but I'm
		// going to pick the one that seems simplest to me right now (midnight; my
		// brain is fuzzy...)
		fake.set(2009, 10, 5, 7, 30, 0, 0);
		Calendar cal = TimeProvider.getCalendar();
		int hr = cal.get(Calendar.HOUR_OF_DAY);
		if (hr > 8) {
			try {
				TimeProvider.sleep(1000 * 3600 * (32 - hr));
			} catch (InterruptedException e) {
			}
		}
		try {
			String s1 = IDGenerator.getNext("x");
			int i = s1.indexOf('@');
			assertEquals(s1.length() - 5, i);
		} finally {
			TimeProvider.override(old);
		}
	}
}
