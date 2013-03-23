package verse.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;


public class HangInterrupterTest {
	
	private static HangInterrupter getInterrupter() {
		HangInterrupter hi = new HangInterrupter();
		// Artficially decrease the resolution on the HangInterrupter to a
		// ridiculously low value. In production code, checking for hangs
		// this often would introduce too much overhead.		
		hi.setResolutionMillis(100);
		return hi;
	}
	
	abstract private static class InterruptTrackingThread extends Thread {
		boolean interrupted = false;
		static AtomicInteger activeCount = new AtomicInteger(0);
		public final void run() {
			activeCount.incrementAndGet();
			try {
				try {
					doRun();
				} catch (InterruptedException e) {
					// If the HangInterrupter breaks our hang, we should come here.
					interrupted = true;
				}
			} finally {
				activeCount.decrementAndGet();
				synchronized (this) {
					this.notifyAll();
				}
			}
		}
		protected abstract void doRun() throws InterruptedException;
	}
	
	private static final class SleepingThread extends InterruptTrackingThread {
		private int mSleep;
		
		SleepingThread(int sleep) {
			mSleep = sleep;
		}
		
		@Override
		public void doRun() throws InterruptedException {
			// Simulate a hang by sleeping for 1.5 secs.
			Thread.sleep(mSleep);
		}
	}
	
	private static final class BusyThread extends InterruptTrackingThread {
		private int mHowLong;
		BusyThread(int howLong) {
			mHowLong = howLong;
		}
		@Override
		protected void doRun() throws InterruptedException {
			this.setPriority(MIN_PRIORITY);
			long endTime = System.currentTimeMillis() + mHowLong;
			String txt = Integer.toString(this.hashCode()) + Long.toString(System.nanoTime());
			for (int i = 0; i < 4; ++i) {
				txt = txt + txt;
			}
			byte[] input = txt.getBytes();
			
			while (true) {
				MessageDigest hash;
				try {
					hash = MessageDigest.getInstance("SHA1");
					ByteArrayInputStream bais = new ByteArrayInputStream(input);
					DigestInputStream dis = new DigestInputStream(bais, hash);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					while (true) {
						int ch;
						try {
							ch = dis.read();
							if (ch < 0) {
								break;
							}
							baos.write(ch);
						} catch (IOException e) {
							e.printStackTrace();
							break;
						}
					}
					if (System.currentTimeMillis() > endTime) {
						return;
					}
					if (this.isInterrupted()) {
						throw new InterruptedException();
					}
					input = dis.getMessageDigest().digest();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}			
		}
	}
	
	private static final class NeverQuiteHangingThread extends InterruptTrackingThread {
		private HangInterrupter mInterrupter;
		private boolean mCancelAfterLoop;
		
		NeverQuiteHangingThread(HangInterrupter hi, boolean cancelAfterLoop) {
			mInterrupter = hi;
			mCancelAfterLoop = cancelAfterLoop;
		}
		
		@Override
		public void doRun() throws InterruptedException {
			for (int i = 1; i < 30; ++i) {
				Thread.sleep(10);
				// Postpone my interruption for another 100 ms.
				mInterrupter.interruptAfter(this, 100);
			}
			if (mCancelAfterLoop) {
				mInterrupter.cancelInterrupt(this);
				// Sleep some more, just to guarantee that once
				// we cancel, we don't get interrupted.
				Thread.sleep(250);
			}
		}
	}
	
	@Test
	public void test_basic_interrupt() {
		HangInterrupter hi = getInterrupter();
		InterruptTrackingThread th = new BusyThread(1500);
		// Set an aggressive timeout for our hanging thread.
		hi.interruptAfter(th, 100);
		// Now run the thread and see if we're interrupted.
		th.start();
		synchronized (th) {
			try { th.wait(); } catch (InterruptedException e) {	}
		}
		assertTrue(th.interrupted);
	}

	private void do_extended_interrupt(boolean cancelAfterLoop) {
		HangInterrupter hi = getInterrupter();
		NeverQuiteHangingThread th = new NeverQuiteHangingThread(hi, cancelAfterLoop);
		// Set an aggressive timeout for our hanging thread.
		hi.interruptAfter(th, 100);
		// Now run the thread and see if we are interrupted. We shouldn't be, since
		// this thread keeps postponing its interruption.
		th.start();
		synchronized (th) {
			try { th.wait(); } catch (InterruptedException e) {	}
		}
		assertFalse(th.interrupted);
	}
	
	@Test
	public void test_extended_interrupt_with_cancel() {
		do_extended_interrupt(true);
	}

	@Test
	public void test_extended_interrupt_without_cancel() {
		do_extended_interrupt(false);
	}
	
	@Test
	public void test_cancel() {
		HangInterrupter hi = getInterrupter();
		SleepingThread th = new SleepingThread(400);
		// Set an aggressive timeout for our hanging thread.
		hi.interruptAfter(th, 100);
		th.start();
		hi.cancelInterrupt(th);
		synchronized (th) {
			try { th.wait(); } catch (InterruptedException e) {	}
		}
		assertFalse(th.interrupted);
	}
	
	@Test
	public void test_interrupt_on_dead_thread() {
		HangInterrupter hi = getInterrupter();
		SleepingThread th = new SleepingThread(1);
		// Set an aggressive timeout for our hanging thread.
		hi.interruptAfter(th, 100);
		th.start();
		synchronized (th) {
			try { th.wait(); } catch (InterruptedException e) {	}
		}
		// Thread should exit before we can interrupt it.
		assertFalse(th.interrupted);
		// Release the thread object; force the weakref to it to become invalid.
		th = null;
		System.gc();
		try { Thread.sleep(200); } catch (InterruptedException e) { }
		// If we get here without exceptions, then we know the HangInterrupter handled dead
		// threads correctly.
	}
	
	@Test
	public void test_interrupt_on_interrupted_thread() {
		HangInterrupter hi = getInterrupter();
		SleepingThread th = new SleepingThread(1500);
		// Set an aggressive timeout for our hanging thread.
		hi.interruptAfter(th, 100);
		th.start();
		// Interrupt ourselves, before the HangInterrupter can.
		th.interrupt();
		synchronized (th) {
			try { th.wait(); } catch (InterruptedException e) {	}
		}
		// Wait until HangInterrupter tries to interrupt the thread as well.
		try { Thread.sleep(200); } catch (InterruptedException e) { }
		// If we get here without exceptions, then we know the HangInterrupter handled
		// interrupted threads correctly.
	}
	
	@Test
	public void test_cancel_on_unregistered_thread() {
		HangInterrupter hi = getInterrupter();
		hi.cancelInterrupt(Thread.currentThread());
		// If we don't get any exceptions, this case is handled safely.
	}
	
	@Test
	public void test_complex() {
		List<InterruptTrackingThread> threads = new ArrayList<InterruptTrackingThread>();
		HangInterrupter hi = getInterrupter();
		int expectedInterruptCount = 0;
		for (int i = 0; i < 18; ++i) {
			int lifetime = 1;
			// Some of the threads we create are going to live long enough to be interrupted...
			if (i % 3 > 0) {
				expectedInterruptCount += 1;
				lifetime = 1500;
			}
			InterruptTrackingThread th;
			// First thread will continually extend its own lifetime.
			if (i == 0) {
				th = new NeverQuiteHangingThread(hi, true);
			} else {
				// All other threads will have a lifetime that depends on the delay we chose
				// above. A few will be busy threads; the rest will sleep.
				if (i % 3 == 2) {
					th = new BusyThread(lifetime);
				} else {
					th = new SleepingThread(lifetime);
				}
			}
			hi.interruptAfter(th, 100);
			th.start();
			// We don't really care about remembering these objects, but we don't want them to
			// vanish in the garbage collector because they have no references to them--so add
			// them to the list.
			threads.add(th);
		}
		while (SleepingThread.activeCount.get() > 0) {
			try { Thread.sleep(50); } catch (InterruptedException e) { }
		}
		int n = 0;
		for (InterruptTrackingThread th: threads) {
			if (th.interrupted) {
				n += 1;
			}
		}
		assertEquals(expectedInterruptCount, n);
	}
}
