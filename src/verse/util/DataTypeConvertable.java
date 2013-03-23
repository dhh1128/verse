/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: startsev
 * Created: 
 */

package verse.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DataTypeConvertable {

	/**
	 * Entity can be found and validated by this method 
	 * @param s string to check
	 * @return result of validation
	 */
	public abstract boolean check(String s);
	
	/**
	 * Matching
	 * 
	 * @param s
	 *            target to check
	 * @param patt
	 *            Pattern object
	 * @return -1 if no match; start position otherwise
	 */
	protected int isMatch(String s, Pattern patt) {
		try {
			Matcher matcher = patt.matcher(s);
			if (matcher.find())
				return matcher.start();
			else
				return -1;
		} catch (RuntimeException e) {
			return -1;
		}
	}
	
	/**
	 * Matching
	 * 
	 * @param s
	 *            target to check
	 * @param pattern
	 *            String object
	 * @return -1 if no match; start position otherwise
	 */
	protected int isMatch(String s, String pattern) {
		try {
			Pattern patt = Pattern.compile(pattern);
			Matcher matcher = patt.matcher(s);

			if (matcher.find())
				return matcher.start();
			else
				return -1;
		} catch (RuntimeException e) {
			return -1;
		}
	}

	
}
