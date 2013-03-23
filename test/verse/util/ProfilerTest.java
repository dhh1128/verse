/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Oct 9, 2009
 */
package verse.util;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 */
public class ProfilerTest {
	
	Profiler doSomethingProfiler = new Profiler("ProfilerTest.doSomething");
	Profiler doSomethingElseProfiler = new Profiler("ProfilerTest.doSomethingElse");
	private static boolean oldEnabledState = false;
	
	@Before
	public void setUp() {
		doSomethingProfiler.reset();
	}
	
	@BeforeClass
	public static void enableProfiler() {
		oldEnabledState = Profiler.getEnabled();
		// Make sure profiler is enabled for the duration of our tests.
		Profiler.setEnabled(true);
	}
	
	@AfterClass
	public static void disableProfiler() {
		Profiler.setEnabled(oldEnabledState);
	}
	
	void doSomething(long delay) {
		long startTime = doSomethingProfiler.enter();
		try {
			if (delay > 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} finally {
			doSomethingProfiler.exit(startTime);
		}
	}
	
	@Test
	public void testEnabled() {
		Profiler.setEnabled(false);
		try {
			for (int i = 0; i < 3; ++i) {
				doSomething(0);
			}
			// We should be disabled by default.
			assertEquals(0, doSomethingProfiler.getCallCount());
			assertEquals(0, doSomethingProfiler.getNanosecs());
			Profiler.setEnabled(true);
			for (int i = 0; i < 3; ++i) {
				doSomething(0);
			}
			assertEquals(3, doSomethingProfiler.getCallCount());
			assertTrue(doSomethingProfiler.getNanosecs() > 0);
		} finally {
			Profiler.setEnabled(true);
		}
	}
	
	@Test
	public void testElapsed() {
		for (int i = 0; i < 3; ++i) {
			doSomething(2);
		}
		assertEquals(3, doSomethingProfiler.getCallCount());
		assertTrue(doSomethingProfiler.getNanosecs() >= 5000000);
	}
	
	@Test
	public void testSummary() {
		for (int i = 0; i < 3; ++i) {
			doSomething(0);
		}
		String txt = doSomethingProfiler.getSummary();
		assertTrue(txt.indexOf("doSomething") != -1);
		assertTrue(txt.indexOf("\n") == -1);
		doSomethingElseProfiler.exit(System.nanoTime() - 1000000);
		txt = Profiler.getSummaryOfAll();
		assertTrue(txt.indexOf("\n") != -1);
		assertTrue(txt.indexOf("doSomethingElse") != -1);
	}
}
