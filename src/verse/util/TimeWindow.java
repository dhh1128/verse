/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Oct 23, 2009
 */
package verse.util;

import java.util.Arrays;

import verse.dbc.precondition;

/**
 * Encapsulate semantics about what time of day an activity can and cannot take
 * place.
 */
public class TimeWindow implements Comparable<TimeWindow> {

	/**
	 * Construct a TimeWindow with no start/end time.
	 */
	public TimeWindow() {
	}

	/**
	 * Construct a TimeWindow with the specified boundaries.
	 * 
	 * @param start
	 *            See {@link #setStartTime(int)}.
	 * @param end
	 *            See {@link #setEndTime(int)}.
	 */
	public TimeWindow(int start, int end) {
		setStartTime(start);
		setEndTime(end);
	}

	/**
	 * @return <code>true</code> if this time window includes any blackout
	 *         period.
	 */
	public boolean hasBlackout() {
		return getStartTime() != getEndTime();
	}

	private boolean mUTC = true;

	/**
	 * @return Get whether this time window is UTC-based.
	 */
	public boolean getUTC() {
		return mUTC;
	}

	/**
	 * Set whether this time window is UTC-based.
	 * 
	 * @param value
	 *            The new value of the property.
	 */
	public void setUTC(boolean value) {
		mUTC = value;
	}

	private int mStartTime = -1;

	/**
	 * @return Get time (expressed in military notation, 0000-2400) at which
	 *         activity can start, or -1 if there are no constraints.
	 */
	public int getStartTime() {
		int start = mStartTime;
		if (start == -1 && mEndTime != -1) {
			start = 0;
		}
		return start;
	}

	/**
	 * <p>
	 * Set time (expressed in military notation, 0000-2400) at which activity
	 * can start, or -1 if there are no constraints. (If either start or end
	 * time is set to -1, the other boundary is automatically set to -1 as
	 * well.)
	 * </p>
	 * <p>
	 * If start time < end time, then a simple range exists (e.g., 0800-1200).
	 * However, it is also valid to have end time < start time (e.g.,
	 * 1600-0400). In that case, activity continues through midnight.
	 * </p>
	 * <p>
	 * Start time and end time define a half-open range -- that is, start time
	 * is included in the valid range, and end time is not. As soon as one of
	 * these values is set, the other value defaults to 0000 until it is
	 * adjusted. Any value > 2400 is adjusted with modulus (4800 = 2400 = 0).
	 * </p>
	 */
	public void setStartTime(int value) {
		precondition.checkAndExplain(value >= -1, "value must be >= -1");
		if (value > 0) {
			value = value % 2400;
		}
		mStartTime = value;
		if (value == -1) {
			mEndTime = value;
		}
	}

	private int mEndTime = -1;

	/**
	 * @return Get UTC time at which activity must stop, or -1 if there are no
	 *         constraints.
	 */
	public int getEndTime() {
		int end = mEndTime;
		if (end == -1 && mStartTime != -1) {
			end = 0;
		}
		return end;
	}

	/**
	 * <p>
	 * Set time (expressed in military notation, 0000-2400) at which activity
	 * must stop, or -1 if there are no constraints. (If either start or end
	 * time is set to -1, the other boundary is automatically set to -1 as
	 * well.)
	 * </p>
	 * <p>
	 * If start time < end time, then a simple range exists (e.g., 0800-1200).
	 * However, it is also valid to have end time < start time (e.g.,
	 * 1600-0400). In that case, activity continues through midnight.
	 * </p>
	 * <p>
	 * Start time and end time define a half-open range -- that is, start time
	 * is included in the valid range, and end time is not. As soon as one of
	 * these values is set, the other value defaults to 0000 until it is
	 * adjusted. Any value > 2400 is adjusted with modulus (4800 = 2400 = 0).
	 * </p>
	 */
	public void setEndTime(int value) {
		precondition.checkAndExplain(value >= -1, "value must be >= -1");
		if (value > 0) {
			value = value % 2400;
		}
		mEndTime = value;
		if (value == -1) {
			mStartTime = value;
		}
	}

	/**
	 * Convenience method to make code less verbose.
	 */
	private static int sinceMidnight(double militaryMillis) {
		return DateUtil.militaryMillisToSinceMidnight(militaryMillis);
	}

	/**
	 * @return The next time (expressed in military notation, 0000-2400) that
	 *         activity is valid, or -1 if immediately.
	 */
	public int getNextActiveTime() {
		if (!hasBlackout()) {
			return -1;
		}
		int now = (int) getNow();
		int start = getStartTime();
		int end = getEndTime();
		// If we crawl in the middle of the day... range is like 0800-2200
		if (start < end) {
			if (now >= start) {
				return (now < end) ? -1 : start;
			}
			return start;
		// else we crawl across midnight... range is like 2200-0800
		}		
		if (now >= start || now < end) {
			return -1;
		}		
		return end;
	}

	private double getNow() {
		double now = getUTC() ? DateUtil.militaryMillisNowUTC() : DateUtil.militaryMillisNow();
		return now;
	}

	/**
	 * Given restrictions of {@link #getStartTime()} and {@link #getEndTime()},
	 * how long should we wait until activity resumes?
	 * 
	 * @return number of milliseconds to delay, or 0 if none.
	 */
	public int getMillisToWait() {
		if (!hasBlackout()) {
			return 0;
		}
		double now = getNow();
		int start = getStartTime();
		int end = getEndTime();
		// If we crawl in the middle of the day... range is like 0800-2200
		if (start < end) {
			if (now >= start) {
				if (now < end) {
					return 0;
				}
				return DateUtil.MIDNIGHT_MILLISEC - sinceMidnight(now) + sinceMidnight(start);
			}
			return sinceMidnight(start) - sinceMidnight(now);
			// else our window wraps around midnight... range like 2200-0800
		}		
		if (now >= start || now < end) {
			return 0;
		}
		return DateUtil.MIDNIGHT_MILLISEC - sinceMidnight(now)	+ sinceMidnight(end);
	}

	@Override
	public int compareTo(TimeWindow obj) {
		Integer t = getStartTime();
		int rc = t.compareTo(obj.getStartTime());
		if (rc == 0) {
			t = getEndTime();
			return t.compareTo(obj.getEndTime());
		} else {
			return rc;
		}
	}

	public boolean isInsideWindow(int time) {
		if (!isMidnightOver()) {
			return getStartTime() <= time && time <= getEndTime();
		} else {
			return (getStartTime() <= time && time < 2400) || (time >= 0 && time <= getEndTime());
		}
	}

	public boolean isMidnightOver() {
		return getEndTime() < getStartTime(); 
	}

	public boolean isUnbounded() {
		return getStartTime() == -1 || getEndTime() == -1; 
	}

	/**
	 * 
	 * @param obj an object to get overlapped time window with. 
	 * @return true if the obj is overlapped with <code>this</code> object, false - otherwise.
	 */
	public boolean getOverlapped(TimeWindow obj) {
		if (obj.isUnbounded()) {
			setStartTime(-1);
			return true;
		}
		if (isInsideWindow(obj.getStartTime()) && isInsideWindow(obj.getEndTime())) {
			return true;
		}
		if (isInsideWindow(obj.getStartTime()) && !isInsideWindow(obj.getEndTime())) {
			setEndTime(obj.getEndTime());
			return true;
		}
		if (!isInsideWindow(obj.getStartTime()) && isInsideWindow(obj.getEndTime())) {
			setStartTime(obj.getStartTime());
			return true;
		}
		if (obj.isInsideWindow(getStartTime()) && obj.isInsideWindow(getEndTime())) {
			setStartTime(obj.getStartTime());
			setEndTime(obj.getEndTime());
			return true;
		}
		return false;
	}

	public static TimeWindow[] overlapAll(TimeWindow[] orig) {
		TimeWindow[] result = new TimeWindow[orig.length];
		result[0] = orig[0];
		int j = 0;
		for (int i = 1; i < orig.length; i++) {
			TimeWindow prev = result[j];
			TimeWindow next = orig[i];
			if (!prev.getOverlapped(next)) {
				result[++j] = next;
			}
		}
		if (orig.length == j + 1) {
			return result;
		} else {
			return overlapAll(Arrays.copyOf(result, j + 1));
		}
	}

	@Override
	public String toString() {
		return mStartTime + " - " + mEndTime;
	}
}
