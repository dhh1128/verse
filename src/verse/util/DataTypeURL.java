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

import java.util.regex.Pattern;

/**
 * Looking for strings containing URL
 * @author sazhin
 *
 */
public class DataTypeURL extends DataTypeConvertable {

	private static Pattern pattURL = Pattern.compile("(\\s*|^)(((ht|f)tp(s?)://(www)?)|www)((?:[a-z0-9.-]|%[0-9A-F]{2}){3,})(?::(\\d+))?((?:\\/(?:[a-z0-9-._~!$&'()*+,;=:@]|%[0-9A-F]{2})*)*)(?:\\?((?:[a-z0-9-._~!$&'()*+,;=:\\/?@]|%[0-9A-F]{2})*))?(?:#((?:[a-z0-9-._~!$&'()*+,;=:\\/?@]|%[0-9A-F]{2})*))?(\\s*|$)");
	
	@Override
	public boolean check(String s) {
		// good pattern, need test
		// "(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?"
		if (isMatch(s, pattURL) > -1)
			return true;
		return false;
	}

}
