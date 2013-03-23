/**
 * $Id$
 * 
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 * 
 */
package verse.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * Utility functions for all types of containers -- collections, iterable, and arrays.
 */
public class ContainerUtil {

	/**
	 * @return true if collection is null or empty.
	 */
	public static <T> boolean is_null_or_empty(Collection<T> items) {
		return items == null || items.isEmpty();
	}
	
	/**
	 * @return true if iterable is null, returns a null iterator,
	 * or has an iterator where .hasNext() is false. 
	 */
	public static <T> boolean is_null_or_empty(Iterable<T> items) {
		if (items == null) {
			return true;
		}
		Iterator<T> it = items.iterator();
		return it == null || !it.hasNext();
	}
	
	/**
	 * @return true if array is null or empty.
	 */
	public static <T> boolean is_null_or_empty(T[] items) {
		return items == null || items.length == 0;
	}
}
