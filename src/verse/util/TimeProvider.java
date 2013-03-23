/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Oct 6, 2009
 */
package verse.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * <p>
 * Many classes have functionality that depends on elapsed time, time of day and
 * so forth. This makes the functionality difficult to test quickly and at an
 * arbitrary time.
 * </p>
 * <p>
 * This class is a solution. Classes that need to fetch the current time can do
 * so through this class (see {@link #getDate()} and {@link #getTime()}. Then
 * tests change which TimeProvider is used (providing one that simulates a
 * particular time of day or a long stretch of elapsed time) -- see
 * {@link #override(TimeProvider)}. This allows the test to manipulate the
 * class's perceived time cleanly without impacting or seeing other internals.
 * </p>
 */
public class TimeProvider {

	private static TimeProvider theOverridingProvider = null;

	/**
	 * Override the current TimeProvider with a new one.
	 * 
	 * @param provider
	 *            The new provider to use. Can be null to revert to default
	 *            behavior.
	 * @return The old overriding provider.
	 */
	public static TimeProvider override(TimeProvider provider) {
		TimeProvider old = theOverridingProvider;
		theOverridingProvider = provider;
		return old;
	}
	
	public static void sleep(long millis) throws InterruptedException {
		if (theOverridingProvider != null) {
			theOverridingProvider.doSleep(millis);
		} else {
			Thread.sleep(millis);
		}
	}

	/**
	 * Override this method to change the perception of the current date/time.
	 * 
	 * @return a {@link java.util.Date}
	 */
	protected Date doGetDate() {
		return new Date();
	}
	
	protected void doSleep(long millis) throws InterruptedException {
		Thread.sleep(millis);
	}

	/**
	 * Override this method to change the perception of the current date/time.
	 * 
	 * @return a {@link java.util.Calendar}
	 */
	protected Calendar doGetCalendar() {
		return GregorianCalendar.getInstance();
	}

	/**
	 * Override this method to change the perception of the current date/time in
	 * a specific {@link TimeZone}.
	 * 
	 * @return a {@link java.util.Calendar}
	 */
	protected Calendar doGetCalendar(TimeZone tz) {
		Calendar cal = GregorianCalendar.getInstance(tz);
		return cal;
	}

	/**
	 * @return the current {@link java.util.Date}
	 */
	public static final Date getDate() {
		Date now = (theOverridingProvider != null) ? theOverridingProvider
				.doGetDate() : new Date();
		return now;
	}

	/**
	 * @return the current date/time in the Gregorian calendar, in the current
	 *         {@link TimeZone}.
	 */
	public static final Calendar getCalendar() {
		Calendar now = (theOverridingProvider != null) ? theOverridingProvider
				.doGetCalendar() : GregorianCalendar.getInstance();
		return now;
	}

	/**
	 * @return the current date/time in the Gregorian calendar, in the specified
	 *         {@link TimeZone}.
	 * @param tz
	 *            what {@link TimeZone} should be used?
	 */
	public static final Calendar getCalendar(TimeZone tz) {
		Calendar now = (theOverridingProvider != null) ? theOverridingProvider
				.doGetCalendar(tz) : GregorianCalendar.getInstance(tz);
		return now;
	}

	/**
	 * @return millisecs since Jan 1, 1970 (GMT)
	 */
	public static final long getTime() {
		return getDate().getTime();
	}

	/**
	 * A TimeProvider that returns a hard-coded value. Useful in many tests.
	 */
	public static class HardCoded extends TimeProvider {
		
		private Object mSleepLock = null;
		private Calendar mNowUTC = GregorianCalendar.getInstance(DateUtil.UTC);
		
		/**
		 * Change the value that this time provider reports.
		 * @param year
		 * @param month
		 * @param day
		 * @param hour
		 * @param minute
		 * @param second
		 * @param ms
		 */
		public void set(int year, int month, int day, int hour, int minute, int second, int ms) {
			Calendar cal = GregorianCalendar.getInstance(DateUtil.UTC);
			cal.clear();
			cal.set(year, month, day, hour, minute, second);
			cal.add(Calendar.MILLISECOND, ms);
			mNowUTC = cal;			
		}

		/**
		 * Set the date portion of the current time, without changing the time portion.
		 * @param year
		 * @param month
		 * @param day
		 */
		public void setDateOnly(int year, int month, int day) {
			mNowUTC.set(Calendar.YEAR, year);
			mNowUTC.set(Calendar.MONTH, month);
			mNowUTC.set(Calendar.DAY_OF_MONTH, day);
		}
		
		/**
		 * Set the time portion of the current time, without changing the date portion.
		 * @param hour
		 * @param minute
		 * @param second
		 * @param ms
		 */
		public void setTimeOnly(int hour, int minute, int second, int ms) {
			mNowUTC.set(Calendar.HOUR, hour);
			mNowUTC.set(Calendar.MINUTE, minute);
			mNowUTC.set(Calendar.SECOND, second);
			mNowUTC.set(Calendar.MILLISECOND, ms);
		}
		

		/* (non-Javadoc)
		 * @see perfectsearch.util.TimeProvider#doGetDate()
		 */
		@Override
		protected Date doGetDate() {
			return mNowUTC.getTime();
		}
		
		/* (non-Javadoc)
		 * @see perfectsearch.util.TimeProvider#doGetCalendar(java.util.TimeZone)
		 */
		@Override
		protected Calendar doGetCalendar(TimeZone tz) {
			Calendar cal = (Calendar)mNowUTC.clone();
			int msAdjust = tz.getOffset(cal.getTimeInMillis());
			if (msAdjust != 0) {
				cal.add(Calendar.MILLISECOND, msAdjust);
			}
			return cal;
		}
		
		/* (non-Javadoc)
		 * @see perfectsearch.util.TimeProvider#doGetCalendar()
		 */
		@Override
		protected Calendar doGetCalendar() {
			TimeZone tz = TimeZone.getDefault();
			return doGetCalendar(tz);
		}
		
		/* (non-Javadoc)
		 * @see perfectsearch.util.TimeProvider#doSleep(long)
		 */
		@Override
		protected void doSleep(long millis) throws InterruptedException {
			if (mSleepLock != null) {
				synchronized (mSleepLock) {
					mSleepLock.wait();
				}
			}
			mNowUTC.add(Calendar.MILLISECOND, (int)millis);			
		}
		
		/**
		 * Set an object that has to be signalled before any call to sleep()
		 * can return. Used in testing.
		 * @param o Set to null to revert to instant sleeps that simply
		 * 		increment the current hard-coded time.
		 */
		public void setSleepLock(Object o) {
			mSleepLock = o;
		}

	}
}
