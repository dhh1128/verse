/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Oct 16, 2009
 */
package verse.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Add this filter to a logger during testing to suppress output but record
 * what gets logged.
 */
public class TestLogFilter implements Filter {
	
	public final Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
	public final List<LogRecord> records = new ArrayList<LogRecord>();
	public boolean saveRecords = false;
	public int suppressBelowLevel = Integer.MAX_VALUE;
	
	/* (non-Javadoc)
	 * @see java.util.logging.Filter#isLoggable(java.util.logging.LogRecord)
	 */
	@Override
	public boolean isLoggable(LogRecord record) {
		Integer lvl = new Integer(record.getLevel().intValue());
		if (!counts.containsKey(lvl)) {
			counts.put(lvl, 1);
		} else {
			counts.put(lvl, counts.get(lvl) + 1);
		}
		if (saveRecords) {
			synchronized (records) {
				records.add(record);
			}
		}
		return record.getLevel().intValue() >= suppressBelowLevel;
	}

	/**
	 * @param level
	 * @return count of how many events of a particular {@link Level} have been logged.
	 */
	public int getCountForLevel(Level level) {
		Integer value = counts.get(level.intValue());
		return (value == null) ? 0 : value;
	}
	
	/**
	 * Reset count and records.
	 */
	public void reset() {
		synchronized (records) {
			records.clear();
		}
		synchronized (counts) {
			counts.clear();
		}
	}
}
