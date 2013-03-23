/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Oct 26, 2009
 */
package verse.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 */
public class TimeWindowTest {

	@Test
	public void testHasBlackout() {
		TimeWindow tw = new TimeWindow();
		assertFalse(tw.hasBlackout());
		tw.setStartTime(1800);
		assertTrue(tw.hasBlackout());
		tw.setEndTime(300);
		assertTrue(tw.hasBlackout());
		tw.setStartTime(300);
		assertFalse(tw.hasBlackout());
	}
	
	@Test
	public void testBoundaries() {
		TimeWindow tw = new TimeWindow();
		tw.setStartTime(1200);
		assertEquals(0, tw.getEndTime());
		tw.setEndTime(2400);
		assertEquals(0, tw.getEndTime());		
	}
	
	@Test
	public void testNextActiveTime() {
		TimeProvider.HardCoded fake = new TimeProvider.HardCoded();
		// Set fake time to 100 ms before 1800 hrs on 2009-10-05
		fake.set(2009, 10, 5, 18, 0, 0, -100);
		TimeProvider old = TimeProvider.override(fake);
		try {
			TimeWindow tw = new TimeWindow(1800, 2200);
			assertEquals(1800, tw.getNextActiveTime());
			fake.set(2009, 10, 5, 18, 0, 0, 0);
			assertEquals(-1, tw.getNextActiveTime());
		} finally {
			TimeProvider.override(old);
		}
	}
	
	@Test
	public void testMillisToWait() {
		TimeProvider.HardCoded fake = new TimeProvider.HardCoded();
		// Set fake time to 100 ms before 1800 hrs on 2009-10-05
		fake.set(2009, 10, 5, 18, 0, 0, -100);
		TimeProvider old = TimeProvider.override(fake);
		try {
			TimeWindow tw = new TimeWindow(1800, 2200);
			assertEquals(100, tw.getMillisToWait());
			fake.set(2009, 10, 5, 18, 0, 0, 0);
			assertEquals(0, tw.getMillisToWait());
			fake.set(2009, 10, 5, 23, 0, 0, 0);
			assertEquals(19*60*60*1000, tw.getMillisToWait());
		} finally {
			TimeProvider.override(old);
		}
	}

	@Test
	public void testCompare() {
		TimeWindow tw1 = new TimeWindow(1200, 2300);
		TimeWindow tw2 = new TimeWindow(800, 2300);
		assertTrue("expected [0800,2300] less than [1200,2300]", tw1.compareTo(tw2) > 0);		
		tw2 = new TimeWindow(1600, 2300);
		assertTrue("expected [1200,2300] less than [1600,2300]", tw1.compareTo(tw2) < 0);		
		tw2 = new TimeWindow(1200, 2000);
		assertTrue("expected [1200,2000] less than [1200,2300]", tw1.compareTo(tw2) > 0);		
		tw2 = new TimeWindow(1200, 2300);
		assertTrue("expected [1200,2300] equal to [1200,2300]", tw1.compareTo(tw2) == 0);		
	}
	
	@Test
	public void testInsideWindow() {
		TimeWindow tw = new TimeWindow(1000, 1300); // simple window
		assertTrue("1000 is in [1000,1300]", tw.isInsideWindow(1000));
		assertTrue("1200 is in [1000,1300]", tw.isInsideWindow(1200));
		assertTrue("1300 is in [1000,1300]", tw.isInsideWindow(1300));
		assertFalse("0900 is not in [1000,1300]", tw.isInsideWindow(900));
		assertFalse("1400 is not in [1000,1300]", tw.isInsideWindow(1400));
		assertFalse("0000 is not in [1000,1300]", tw.isInsideWindow(0));
		tw = new TimeWindow(2000, 2400); // to midnight
		assertTrue("2000 is in [2000,0000]", tw.isInsideWindow(2000));
		assertTrue("2200 is in [2000,0000]", tw.isInsideWindow(2200));
		assertTrue("0000 is in [2000,0000]", tw.isInsideWindow(0));
		assertFalse("1400 is not in [2000,0000]", tw.isInsideWindow(1400));
		assertFalse("1800 is not in [2000,0000]", tw.isInsideWindow(1800));
		assertFalse("0100 is not in [2000,0000]", tw.isInsideWindow(100));
		tw = new TimeWindow(2000, 0200); // over the midnight
		assertTrue("2000 is in [2000,0200]", tw.isInsideWindow(2000));
		assertTrue("2200 is in [2000,0200]", tw.isInsideWindow(2200));
		assertTrue("0000 is in [2000,0200]", tw.isInsideWindow(0));
		assertFalse("1400 is not in [2000,0000]", tw.isInsideWindow(1400));
		assertFalse("1800 is not in [2000,0000]", tw.isInsideWindow(1800));
		assertTrue("0100 is in [2000,0000]", tw.isInsideWindow(100));
	}

	@Test
	public void testGetOverlapped() {
		TimeWindow tw1 = new TimeWindow(1000, 1300);
		TimeWindow tw2 = new TimeWindow(1100, 1600);
		assertTrue("expected the time windows [1000,1300] and [1100,1600] are overlapped", tw1.getOverlapped(tw2));
		assertEquals(1000, tw1.getStartTime());
		assertEquals(1600, tw1.getEndTime());
		tw2 = new TimeWindow(600, 1200);
		assertTrue("expected the time windows [1000,1600] and [0600,1600] are overlapped", tw1.getOverlapped(tw2));
		assertEquals(600, tw1.getStartTime());
		assertEquals(1600, tw1.getEndTime());
		tw2 = new TimeWindow(1800, 2300);
		assertFalse("expected the time windows [0600,1600] and [1800,2300] aren't overlapped", tw1.getOverlapped(tw2));		
		tw2 = new TimeWindow(2300, 500);
		assertFalse("expected the time windows [0600,1600] and [2300,0500] aren't overlapped", tw1.getOverlapped(tw2));		
		tw2 = new TimeWindow(1400, 2400);
		assertTrue("expected the time windows [0600,1600] and [1400,2400] are overlapped", tw1.getOverlapped(tw2));		
		assertEquals(600, tw1.getStartTime());
		assertEquals(0, tw1.getEndTime());
		tw2 = new TimeWindow(400, 100);
		assertTrue("expected the time windows [0600,0000] and [0400,0100] are overlapped", tw1.getOverlapped(tw2));		
		assertEquals(400, tw1.getStartTime());
		assertEquals(100, tw1.getEndTime());
	}

	@Test
	public void testOverlapAll() {
		TimeWindow tw1 = new TimeWindow(1000, 1300);
		TimeWindow tw2 = new TimeWindow(1100, 1600);
		TimeWindow tw3 = new TimeWindow(600, 1200);
		TimeWindow tw4 = new TimeWindow(1800, 2300);
		TimeWindow tw5 = new TimeWindow(2300, 500);
		TimeWindow tw6 = new TimeWindow(2300, 500);
		TimeWindow[] twarr = new TimeWindow[] { tw3, tw1, tw2, tw4, tw5, tw6 };
		TimeWindow[] twres = TimeWindow.overlapAll(twarr);
		assertEquals(2, twres.length);
		TimeWindow r1 = twres[0];
		assertEquals(600, r1.getStartTime());
		assertEquals(1600, r1.getEndTime());
		TimeWindow r2 = twres[1];
		assertEquals(1800, r2.getStartTime());
		assertEquals(500, r2.getEndTime());
	}
}
