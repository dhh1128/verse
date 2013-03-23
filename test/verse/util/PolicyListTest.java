/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Oct 28, 2009
 */
package verse.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import verse.util.PolicyList.DupsPolicy;
import verse.util.PolicyList.NullsPolicy;
import verse.util.PolicyList.ThreadsPolicy;

/**
 * 
 */
public class PolicyListTest {
	
	class SamplePolicyList extends PolicyList<String> {
		/**
		 * @param threads
		 * @param nulls
		 * @param dups
		 */
		public SamplePolicyList(ThreadsPolicy threads, NullsPolicy nulls,
				DupsPolicy dups) {
			super(threads, nulls, dups);
		}
		int addCount = 0;
		int removeCount = 0;
		/* (non-Javadoc)
		 * @see perfectsearch.util.PolicyList#onAdd(java.util.List)
		 */
		@Override
		protected void onAdd(List<String> internalList) {
			++addCount;
		}
		/* (non-Javadoc)
		 * @see perfectsearch.util.PolicyList#onRemove(java.util.List)
		 */
		@Override
		protected void onRemove(List<String> internalList) {
			++removeCount;
		}
	}

	@Test
	public void testNullPolicy() {
		SamplePolicyList foo = new SamplePolicyList(
				ThreadsPolicy.SINGLE_THREAD, NullsPolicy.NO_NULLS,
				DupsPolicy.NO_DUPS);
		String txt = "hi";
		assertTrue(foo.add(txt));
		assertFalse(foo.add(null));
		assertEquals(1, foo.size());
		assertEquals(1, foo.addCount);
		foo = new SamplePolicyList(
				ThreadsPolicy.SINGLE_THREAD, NullsPolicy.ALLOW_NULLS,
				DupsPolicy.NO_DUPS);
		assertTrue(foo.add(txt));
		assertTrue(foo.add(null));
		assertEquals(2, foo.size());
		assertEquals(2, foo.addCount);
	}
	
	@Test
	public void testDupsPolicy() {
		SamplePolicyList foo = new SamplePolicyList(
				ThreadsPolicy.SINGLE_THREAD, NullsPolicy.ALLOW_NULLS,
				DupsPolicy.NO_DUPS);
		String txt = "hi";
		assertTrue(foo.add(txt));
		assertTrue(foo.add(null));
		assertFalse(foo.add(txt));
		assertFalse(foo.add(null));
		assertEquals(2, foo.size());
		assertEquals(2, foo.addCount);
		foo = new SamplePolicyList(
				ThreadsPolicy.SINGLE_THREAD, NullsPolicy.ALLOW_NULLS,
				DupsPolicy.ALLOW_DUPS);
		assertTrue(foo.add(txt));
		assertTrue(foo.add(null));
		assertTrue(foo.add(txt));
		assertTrue(foo.add(null));
		assertEquals(4, foo.size());
		assertEquals(4, foo.addCount);
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testThreadsPolicy() throws Throwable {
		// Experience has shown that this test is somewhat sensitive to timing and
		// performance on the machine that runs it. That is not the intent; it should
		// succeed 100% of the time. When the test fails, we typically do not get a
		// line number for which assertion failed, and we can't debug it because
		// that alters the timing/synchronization profile significantly. Therefore
		// we need a way to pinpoint failures in this method. We use this variable
		// to track what part of the algorithm we're in when a failure happens...
		int position = 0;
		try {
			SamplePolicyList foo = new SamplePolicyList(
					ThreadsPolicy.SINGLE_THREAD, NullsPolicy.NO_NULLS,
					DupsPolicy.NO_DUPS);
			foo.add("hi");
			position = 1;
			
			// Create a list that can't return an iterator until it owns its own lock.
			final List<String> waitToIterate = new ArrayList<String>() {
				/* (non-Javadoc)
				 * @see java.util.AbstractList#iterator()
				 */
				@Override
				public synchronized Iterator<String> iterator() {
					return super.iterator();
				}
			};
			waitToIterate.add("foo");
			position = 2;
			
			class ValueFetcher implements Runnable {
				PolicyList<String> lst;
				ValueFetcher(PolicyList<String> lst) {
					this.lst = lst;
				}
				String value;
				@Override
				public void run() {
					// This call will block if lst is locked.
					value = lst.get(0);
				}
			};
			
			class Copier implements Runnable {
				PolicyList<String> lst;
				Copier(PolicyList<String> lst) {
					this.lst = lst;
				}
				@Override
				public void run() {
					lst.addAll(0, waitToIterate);
				}
			}
		
			position = 3;
			ValueFetcher fetcher = new ValueFetcher(foo);
			position = 4;
			synchronized (waitToIterate) {
				// This thread will start and immediately try to fetch the value
				// at index 0 of foo.
				Thread t2 = new Thread(fetcher);
				t2.setName("t2 -- calls foo.get(0)");
				Thread t1 = new Thread(new Copier(foo));
				t1.setName("t1 -- calls foo.addAll(waitToIterate)");
				// Since we haven't told foo that it is multithreaded, it should
				// not lock. It will get inside its addAll method and then get stuck
				// waiting for the iterator on waitForLock to return so it can
				// iterate the contents.
				t1.start();
				position = 5;
				Thread.sleep(1);
				// Start the fetcher thread; it should get data without getting
				// blocked.
				t2.start();
				position = 6;
				for (int i = 0; i < 30; ++i) {
					Thread.sleep(1);
					assertTrue(t1.isAlive());
					if (t2.isAlive()) {
						Thread.sleep(5);
					} else {
						break;
					}
				}
				position = 7;
				assertFalse(t2.isAlive());
			}
			position = 8;
			assertEquals("hi", fetcher.value);
			
			// Okay, now try when the list (foo) is synchronized.
			foo = new SamplePolicyList(
					ThreadsPolicy.MULTIPLE_THREADS, NullsPolicy.NO_NULLS,
					DupsPolicy.NO_DUPS);
			foo.add("hi");
			
			fetcher = new ValueFetcher(foo);
			// This thread will start and immediately try to fetch the value
			// at index 0 of foo.
			position = 9;
			Thread t2 = new Thread(fetcher);
			t2.setName("t2 -- calls foo.get(0)");
			synchronized (waitToIterate) {
				Thread t1 = new Thread(new Copier(foo));
				t1.setName("t1 -- calls foo.addAll(waitToIterate)");
				// Since we told foo that it is multithreaded, it should get
				// inside the addAll method, having locked itself, and then get stuck
				// waiting for the iterator on waitForLock to return so it can
				// iterate the contents.
				t1.start();
				position = 10;
				Thread.sleep(1);
				// This thread will start and try to fetch the value at index
				// 0. However, it should get stuck trying to get a read lock
				// on foo.
				t2.start();
				Thread.sleep(50);
				position = 11;
				// Thread should be deadlocked.
				assertTrue(t1.isAlive());
				position = 12;
				assertTrue(t2.isAlive());
			}
			position = 13;
			// Now that lock is released, waitToIterate will be able to return its
			// iterator, which will allow t1 to complete; t2 will then finish after t1.
			t2.join();
			position = 14;
			// In this situation addAll() should complete before the fetcher
			// can read the value from the list, so the value we get back should
			// be the value we copied in.
			assertEquals("foo", fetcher.value);
			position = 15;
		} catch (Throwable e) {
			System.err.print(String.format("Failure at position %d in testThreadsPolicy.%n", position));
			throw e;
		}
	}
}
