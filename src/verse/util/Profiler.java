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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import verse.dbc.precondition;

/**
 * A class that allows code performance to be studied. Sample usage:
 * 
 * <pre>
 * static final Profiler profilerForX = new Profiler(Foo.class.getMethod("doX"));
 * void doX() {
 *     long startTime = profilerForX.enter();
 *     try {
 *     
 *         ... do something where performance is interesting ...
 *     
 *     } finally {
 *         profilerForX.exit(startTime);
 *     }
 * }
 * </pre>
 */
public class Profiler {

	private static final AtomicBoolean enabled;
	private final AtomicLong nanosecs;
	private final AtomicLong calls;
	private final String name;

	/**
	 * Create a profiler for a named block of code (typically a method). Be sure
	 * to use the <code>static</code> and <code> final</code> keywords.
	 * 
	 * @param name
	 *            A friendly name for the method or block of code that you are profiling.
	 */
	public Profiler(String name) {
		precondition.checkNotNullOrEmpty(name, "name");
		this.name = name;
		this.nanosecs = new AtomicLong(0);
		this.calls = new AtomicLong(0);
		register(this);
	}

	private static final List<Profiler> all;

	static {
		all = new ArrayList<Profiler>(20);
		enabled = new AtomicBoolean(false);
	}

	/**
	 * @return whether profiling is turned on or off. It's off by default.
	 */
	public static boolean getEnabled() {
		return enabled.get();
	}

	/**
	 * Turn profiling on or off.
	 * 
	 * @param value
	 *            desired state
	 */
	public static void setEnabled(boolean value) {
		enabled.set(value);
	}

	private static void register(Profiler p) {
		synchronized (all) {
			all.add(p);
		}
	}

	/**
	 * Call at the top of a function to record entry.
	 * 
	 * @return milliseconds at time of entry. Must be used later to call
	 *         {@link #exit(long)}.
	 */
	public long enter() {
		if (enabled.get()) {
			calls.incrementAndGet();
			long time = System.nanoTime();
			//System.out.printf("nanotime=%d%n", time);
			return time;
		}
		return 0;
	}

	/**
	 * Call at the exit from a function. Be sure to use a <code>try { ... }
	 * finally { profiler.exit(startTime); }</code> pattern in the function or
	 * block to guarantee the exit logic is called.
	 * 
	 * @param nanoTimeAtEnter
	 *            value returned from {@link #enter()}.
	 */
	public void exit(long nanoTimeAtEnter) {
		if (nanoTimeAtEnter != 0) {
			long elapsed = System.nanoTime() - nanoTimeAtEnter;
			// Don't allow an elapsed time of zero nanosecs. On today's processors
			// the fastest non-virtual function calls in C++ compiled for release
			// execute in about 100 to 400 nanosecs. The only things that execute 
			// in true nanosec time are arithmetic, bitwise ops, and if statements.
			// Since we should never be profiling those things and we have Java's
			// overhead anyway, any elapsed values of 0 probably mean that the timer
			// resolution is not fine enough to detect any change.
			if (elapsed < 1) {
				elapsed = 1;
			}
			nanosecs.addAndGet(elapsed);
		}
	}

	/**
	 * @return How many times has the profiled block been called?
	 */
	public long getCallCount() {
		return calls.get();
	}

	/**
	 * @return Total nanoseconds elapsed during all calls to the profiled block.
	 */
	public long getNanosecs() {
		return nanosecs.get();
	}
	
	/**
	 * @return How many seconds elapsed per call that was made. Typically this
	 * number is a very small decimal because average function call time is
	 * in the microsec range.
	 */
	public double getSecsPerCall() {
		double ccount = calls.get();
		if (ccount == 0) {
			return 0;
		}
		return getNanosecs() / (ccount * (1000 * 1000 * 1000));
	}
	
	/**
	 * Convenience method to dump all Profiler data to stdout.
	 */
	public static void dumpAll() {
		class ProfileDumper extends Profiler.Selector {
			long cumulative;
			int maxLabelSize = 0;
			@Override
			public boolean select(Profiler p) {
				cumulative += p.getNanosecs();
				System.out.println(p.getSummary(cumulative, maxLabelSize));
				return false;
			}
			public void summarize() {
				System.out.printf("%s\ncumulative time (probably has overlaps):%d nanosecs (%.2f secs)\n",
						str_util.padLeft("-", 84, '-'),
						cumulative,
						cumulative / (1000 * 1000 * 1000.0));
			}
		}
		ProfileDumper pd = new ProfileDumper();
		Collections.sort(all, new Comparator<Profiler>() {
			@Override
			public int compare(Profiler p1, Profiler p2) {
				return p1.name.compareToIgnoreCase(p2.name);
			}
		});
		for (Profiler p: all) {
			pd.cumulative += p.getNanosecs();
			int n = p.name.length();
			if (n > pd.maxLabelSize) {
				pd.maxLabelSize = n;
			}
		}
		Profiler.getSummaryOfAll(pd);
		pd.summarize();
	}

	/**
	 * @return A friendly description of data about the profiled block.
	 * 
	 */
	public String getSummary() {
		return getSummary(0, 30);
	}
	
	/**
	 * @param cumulative Used to calculate what % of total time was spent in block.
	 * If <= 0, ignored.
	 * @return A friendly description of data about the profiled block.
	 */
	public String getSummary(long cumulative, int maxLabelSize) {
		long ccount = calls.get();
		if (ccount == 0) {
			return String.format("%s:         0 calls", 
					str_util.padRight(name, maxLabelSize));
		}
		long elapsed = nanosecs.get();
		double secs = elapsed / (1000 * 1000 * 1000.0);
		String pct = cumulative <= 0 ? "" : String.format(" -- %s", 
				str_util.padLeft(String.format("%.1f%%", elapsed * 100.0 / cumulative), 5));
		return String.format(
				"%s: %9d calls in %12d nanosecs (%.2f secs%s); %5.3f millisecs/call", 
				str_util.padRight(name, maxLabelSize),
				ccount, elapsed, secs, pct,
				((double) elapsed / ccount) / 1000000);
	}

	/**
	 * @return A summary of each registered Profiler -- one summary per line.
	 */
	public static String getSummaryOfAll() {
		return getSummaryOfAll(null);
	}

	/**
	 * A class that can be used to select which {@link Profiler}s are
	 * summarized. See {@link Profiler#getSummaryOfAll(Selector)}.
	 */
	public static abstract class Selector {
		/**
		 * @param p
		 *            The Profiler to evaluate.
		 * @return <code>true</code> if the profiler should be selected.
		 */
		public abstract boolean select(Profiler p);
	}

	/**
	 * @param sel
	 *            A {@link Profiler.Selector} that can be used to filter which
	 *            Profilers are summarized.
	 * @return A summary of each registered Profiler -- one summary per line.
	 */
	public static String getSummaryOfAll(Selector sel) {
		StringBuilder sb = new StringBuilder();
		synchronized (all) {
			for (Profiler p : all) {
				if (sel == null || sel.select(p)) {
					sb.append(p.getSummary());
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}

	public void reset() {
		nanosecs.set(0);
		calls.set(0);
	}
}
