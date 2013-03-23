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
 * Looking for strings containing tags
 * @author startsev
 *
 */
public class DataTypeXML extends DataTypeConvertable{

	private static Pattern pattXML = Pattern.compile(".*<(\"[^\"]*\"|'[^']*'|[^'\">])*>");
	@Override
	public boolean check(String s) {
		if (isMatch(s, pattXML) > -1) {
			// it is XML!
			return true;
		}
		return false;
	}

}
