/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Nov 11, 2009
 */
package verse.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 */
public class OSInfoTest {
	
	@Test
	public void testPID() {
		assertTrue(OSInfo.pid > 0);
	}
}
