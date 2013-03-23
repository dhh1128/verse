/**
 * $Id$
 * 
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 * 
 */
package verse.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ContainerUtilTest {
	
	@SuppressWarnings("cast")
	@Test
	public void testIsNullOrEmpty() {
		String[] NULL_ARRAY = null;
		List<String> NULL_LIST = null;
		Iterable<String> NULL_ITERABLE = (Iterable<String>)NULL_LIST;
		
		assertTrue(ContainerUtil.is_null_or_empty(NULL_ARRAY));
		assertTrue(ContainerUtil.is_null_or_empty(NULL_LIST));
		assertTrue(ContainerUtil.is_null_or_empty(NULL_ITERABLE));
		
		assertTrue(ContainerUtil.is_null_or_empty(new String[] {}));
		assertTrue(ContainerUtil.is_null_or_empty(new ArrayList<String>()));
		assertTrue(ContainerUtil.is_null_or_empty((Iterable<String>)new ArrayList<String>()));
		
		assertFalse(ContainerUtil.is_null_or_empty(new String[] {""}));
		List<String> nonEmptyList = new ArrayList<String>();
		nonEmptyList.add("");
		assertFalse(ContainerUtil.is_null_or_empty(nonEmptyList));
		assertFalse(ContainerUtil.is_null_or_empty((Iterable<String>)nonEmptyList));
	}

}
