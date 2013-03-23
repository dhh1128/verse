package verse.util;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class HangInterrupter {

	private static final int DEFAULT_RESOLUTION_MILLIS = 10000;
	private static final int MIN_RESOLUTION_MILLIS = 100;
	private static final int MAX_RESOLUTION_MILLIS = 3600 * 1000;
	private AtomicInteger mResolutionMillis = new AtomicInteger(
			DEFAULT_RESOLUTION_MILLIS);

	/**
	 * Change the resolution with which the HangInterrupter checks for hung
	 * threads.
	 * 
	 * @param value How often should the list of audited threads be checked? Cannot
	 * be < 100 ms, or > 1 hour.
	 */
	public void setResolutionMillis(int value) {
		if (value < MIN_RESOLUTION_MILLIS) {
			value = MIN_RESOLUTION_MILLIS;
		} else if (value > MIN_RESOLUTION_MILLIS) {
			value = MAX_RESOLUTION_MILLIS;
		}
		mResolutionMillis.set(value);
	}

	public int getResolutionMillis() {
		return mResolutionMillis.get();
	}

	private static final class Entry {
		WeakReference<Thread> thread;
		long timeout = 0;
	}

	private List<Entry> mEntries = null;
	private Object mEntriesLock = new Object();
	private AtomicInteger mActiveEntryCount = new AtomicInteger(0);

	private Thread mAuditThread = null;

	private static final class AuditThread extends Thread {
		HangInterrupter mInterrupter;

		AuditThread(HangInterrupter hi) {
			this.setDaemon(true);
			this.setName("Hang fail-safe " + Integer.toString(hi.hashCode()));
			mInterrupter = hi;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(mInterrupter.getResolutionMillis());
					// See if we have anything to do.
					int n = mInterrupter.mActiveEntryCount.get();
					if (n > 0) {
						int found = 0;
						long now = System.currentTimeMillis();
						// Look for entries that appear to have timed out.
						for (Entry e : mInterrupter.mEntries) {
							if (e.timeout > 0) {
								found += 1;
								if (e.timeout < now) {
									// We did a quick sanity test; the timeout
									// for this thread seems to have passed.
									// Lock the entry and check more carefully.
									synchronized (e) {
										// Still looks timed out?
										if (e.timeout > 0 && e.timeout < now) {
											found += 1;
											Thread th = e.thread.get();
											// Is the thread still alive?
											if (th != null) {
												boolean remove = true;
												try {
													th.interrupt();
												} catch (SecurityException ex) {
													// This type of exception gets thrown if we don't
													// have privileges to interrupt the thread. In such
													// a case, there's no point in retrying later, so we
													// should just remove the entry from our list.
												} catch (Throwable ex) {
													// Any other exception caused by calling .interrupt()
													// might not happen on our next attempt, so just leave
													// the entry alone and keep trying.
													remove = false;
												}
												if (remove) {
													e.timeout = 0;
												}
											} else {
												// Thread
												e.timeout = 0;
											}
										}
									}
								}
							}
							// If we've found all the entries we need to
							// consider, stop iterating. When we have a list of
							// 10 or 20 entries, but only 1 or 2 are active,
							// this should prevent a lot of useless iteration.
							// There is a small chance that a new entry will be
							// added and an old one removed while we're
							// iterating, but this is unlikely to happen often,
							// and when it does, the worst consequence is a
							// delay in our detection of a hung thread, so the
							// optimization seems worthwhile.
							if (found == n) {
								break;
							}
						}
					}
				} catch (InterruptedException e) {
					// If someone interrupts *this* thread, just exit cleanly.
					return;
				}
			}
		}
	}

	/**
	 * Register a thread for interruption if it is still running after the specified
	 * timeout has elapsed -- or, if the thread is already registered, extend its
	 * timeout by the specified amount.
	 * 
	 * @param th
	 *            Thread to be interrupted.
	 * @param timeout
	 *            How far into the future (starting from now) should the thread
	 *            be allowed to run without being considered hung?
	 */
	public void interruptAfter(Thread th, long timeout) {
		// Do first test without locking; optimistic (lazy) locking is faster.
		if (mEntries == null) {
			synchronized (mEntriesLock) {
				// Double-check again, now that we've locked.
				if (mEntries == null) {
					// Pre-allocate 10 entries, so the list is unlikely to need
					// to grow for a while.
					Entry[] entries = new Entry[10];
					for (int i = 0; i < entries.length; ++i) {
						entries[i] = new Entry();
					}
					// A CopyOnWriteArrayList allows iteration without any locks.
					// It only requires locking when adding/removing items. We
					// never remove, so adding is the only thing we have to worry
					// about. By allocating 10 entries right off the bat, and recycling
					// them, we get fast access in almost all cases, without any
					// locking.
					mEntries = new CopyOnWriteArrayList<Entry>(entries);
					mAuditThread = new AuditThread(this);
					mAuditThread.start();
				}
			}
		} else {
			// See if this is a thread we are already auditing.
			for (Entry e : mEntries) {
				// Only consider entries that are active. 
				if (e.timeout > 0) {
					// No locking is needed here; we never change the thread member
					// of an active entry.
					Thread audited = e.thread.get();
					if (audited == th) {
						e.timeout = System.currentTimeMillis() + timeout;
						return;
					}
				}
			}			
		}
		Entry used = null;
		for (Entry e : mEntries) {
			// Same optimistic (lazy) locking pattern.
			if (e.timeout == 0) {
				synchronized (e) {
					if (e.timeout == 0) {
						// Reserve the entry for our use.
						e.timeout = System.currentTimeMillis() + timeout;
						used = e;
					}
				}
			}
			if (used != null) {
				break;
			}
		}
		// Couldn't find an available slot in existing array?
		if (used == null) {
			synchronized (mEntriesLock) {
				used = new Entry();
				used.timeout = System.currentTimeMillis() + timeout;
				mEntries.add(used);
			}
		}
		used.thread = new WeakReference<Thread>(th);
		// Increment our count of how many entries are active. This is an
		// optimization that allows the audit thread not to iterate over lots of
		// inactive entries every time it wakes up.
		mActiveEntryCount.incrementAndGet();
	}
		
	public void cancelInterrupt(Thread th) {
		if (mEntries != null) {
			// See if this is a thread we are already auditing.
			for (Entry e : mEntries) {
				// Only consider entries that are active. 
				if (e.timeout > 0) {
					// No locking is needed here; we never change the thread
					// member of an active entry.
					Thread audited = e.thread.get();
					if (audited == th) {
						// Release this entry so it can be recycled.
						e.timeout = 0;
						return;
					}
				}
			}
		}
	}

}
