/**
 * $Id$
 * 
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 * 
 */
package verse.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import verse.dbc.precondition;

/** 
 * Generates date-based IDs that are moderately user-friendly. IDs are guaranteed
 * to be unique and to be directly representable in URLs or file system paths.
 */
public class IDGenerator {
	
    private static Map<String,String> lastDateBasedIDs = new HashMap<String, String>();
    
    /** @return an ID that begins with the specified prefix. */
    public static String getNext(String prefix) {
    	precondition.checkNotNull(prefix, "prefix");
    	Date d = TimeProvider.getDate();
    	String day = DateUtil.formatStandardDate(d, DateUtil.STD_DATE_FORMAT1);
    	int hrsAndMinutes = DateUtil.militaryNow();
    	String id = String.format("%s@%04d", day, hrsAndMinutes);
    	if (!prefix.isEmpty()) {
    		id = prefix + "-" + id;
    	}
    	synchronized (lastDateBasedIDs) {
    		String value = lastDateBasedIDs.get(prefix);
    		if (value != null) {
    			if (value.startsWith(id)) {
    				if (value.length() == id.length()) {
    					id = id + ".2";
    				} else {
    					int n = Integer.parseInt(value.substring(id.length() + 1));
    					id = id + "." + Integer.toString(n + 1);
    				}
    			}
    		}
    		lastDateBasedIDs.put(prefix, id);
    	}
    	return id;
    }
    
}
