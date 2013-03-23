/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Dec 17, 2009
 */
package verse.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 */
public class ThreadUtilTest {

	@Test
	public void testWaitForAnyNull() {
		assertNull(ThreadUtil.waitForAny());
		assertNull(ThreadUtil.waitForAny((Object[])null));
		assertNull(ThreadUtil.waitForAny(null, null));
	}
	
	@Test(timeout=500)
	public void testWaitForAnyTimeout() {
		long n = System.nanoTime();
		Object signalled = ThreadUtil.waitForAny(10, "a", "b");
		n = System.nanoTime() - n;
		assertNull(signalled);
		assertTrue(n < 200000000);
	}
	
	@Test
	public void testWaitForAny() {
		Object a = new Object();
		Object b = new Object();
		
		class SignalThread extends Thread {
			Object obj;
			SignalThread(Object o) {
				obj = o;
			}
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				synchronized (obj) {
					obj.notifyAll();
				}
				//System.err.println("Notified: " + System.nanoTime() );
			}
		}
		
		//System.err.println("Before a: " + System.nanoTime() );
		new SignalThread(a).start();
		//System.err.println("During a: " + System.nanoTime() );
		Object signalled = ThreadUtil.waitForAny(2500, a, b);
		//System.err.println("After a: " + System.nanoTime() );
		assertEquals(a, signalled);
		
		//System.err.println("Before b: " + System.nanoTime() );
		new SignalThread(b).start();
		//System.err.println("During b: " + System.nanoTime() );
		signalled = ThreadUtil.waitForAny(2500, a, b);
		//System.err.println("After b: " + System.nanoTime() );
		assertEquals(b, signalled);
	}
}
