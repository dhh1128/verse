/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: Daniel Hardman
 * Created: Sep 1, 2009
 */
package verse.util;

import static org.junit.Assert.*;
import org.junit.Test;

import verse.util.str_util;

public class FriendlyGuidTest {

	@Test
	public void testSimple() {
		String guid1 = FriendlyGuid.next();
		String guid2 = FriendlyGuid.next();
		assertFalse(guid1.equals(guid2));
		assertEquals(5, str_util.count(guid1, '-'));
	}
	
	@Test
	public void testWidth() {
		String guid = FriendlyGuid.next(8);
		assertEquals(1, str_util.count(guid, '-'));
		guid = FriendlyGuid.next(8, '\0');
		assertEquals(8, guid.length());
	}

	@Test
	public void testCustom() {
		String guid = FriendlyGuid.next(8, ' ', 3);
		assertEquals(2, str_util.count(guid, ' '));
		assertEquals(10, guid.length());
	}
	
	@Test
	public void testCompare() {
		assertEquals(0, FriendlyGuid.compare("ace", "A-C-E"));
		assertTrue(FriendlyGuid.compare("ace", null) > 0);
		assertTrue(FriendlyGuid.compare(null, "ace") < 0);
		assertTrue(FriendlyGuid.compare(null, "") < 0);
		assertTrue(FriendlyGuid.compare("", null) > 0);
		assertEquals(0, FriendlyGuid.compare("", ""));
		assertTrue(FriendlyGuid.compare("ace", "") > 0);
		assertTrue(FriendlyGuid.compare("", "ace") < 0);
		assertTrue(FriendlyGuid.compare("j111", "j121") != 0);
		assertTrue(FriendlyGuid.compare("j111", "j101") != 0);
		assertTrue(FriendlyGuid.compare("jA", "j-C") != 0);
	}
}
