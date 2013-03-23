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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class ThreadUtil {

	/**
	 * Waits for any one of multiple objects to be signalled (analogous to
	 * Windows' WaitForMultipleObjects() function).
	 * 
	 * @param objects
	 * @return the object that was signalled.
	 */
	public static Object waitForAny(Object... objects) {
		return waitForAny(0, objects);
	}

	/**
	 * Waits for any one of multiple objects to be signalled (analogous to
	 * Windows' WaitForMultipleObjects() function).
	 * 
	 * @param objects
	 * @param millisecs
	 *            timeout
	 * @return the object that was signalled, or null if wait was abandoned due
	 *         to timeout or interrupt.
	 */
	public static Object waitForAny(long millisecs, Object... objects) {

		class SignalledObject {
			Object obj = null;
		}
		;

		final SignalledObject sobj = new SignalledObject();

		class XThread extends Thread {
			private Object lock;
			private List<XThread> threads;

			XThread(Object lock, XThread prev) {
				this.lock = lock;
				this.threads = (prev == null) ? new ArrayList<XThread>()
						: prev.threads;
				this.threads.add(this);
			}

			@Override
			public void run() {
				boolean signalled = false;
				synchronized (lock) {
					try {
						lock.wait();
						signalled = true;
					} catch (InterruptedException e) {
					}
				}
				synchronized (threads) {
					if (!threads.isEmpty()) {
						if (signalled && sobj.obj == null) {
							sobj.obj = lock;
							synchronized(sobj)
							{
								sobj.notifyAll();
							}
						}
						for (XThread th : threads) {
							if (th != this) {
								th.interrupt();
							}
						}
						threads.clear();
					}
				}
			}
		}

		if (objects != null) {
			synchronized (sobj) {
				XThread prev = null;
				for (Object obj : objects) {
					if (obj != null) {
						XThread next = new XThread(obj, prev);
						next.start();
						prev = next;
					}
				}
				if (prev != null) {
					try {
						sobj.wait(millisecs);
					} catch (InterruptedException e) {
					}
					prev.interrupt();
				}
			}
		}

		return sobj.obj;

	}
}
