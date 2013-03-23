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

public class SimpleCallback implements Callback{

	public static final long maxLength = 16*1024;
	@Override
	public boolean call(long length) {
		if (length > maxLength)
			return true;
		return false;
	}

}
