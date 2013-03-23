/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 */
package verse.util;

import static org.junit.Assert.*;

import org.junit.Test;

import verse.io.path_util;

public class BuildDiskLayoutTest 
{
	@Test
	public final void testGetAspects() 
	{
		String folder = path_util.lastNamedSegment(BuildDiskLayout.getInstance().getSandboxRoot());
		assertTrue(BuildDiskLayout.ROOT_DIRECTORY_PATTERN.matcher(folder).matches());
		
		folder = path_util.lastNamedSegment(BuildDiskLayout.getInstance().getCodeAspect());
		assertTrue(folder.matches("[\\/]?code[\\/]?"));
		
		folder = path_util.lastNamedSegment(BuildDiskLayout.getInstance().getBuiltAspect());
		assertTrue(folder.matches("[\\/]?built\\.[^\\/]+[\\/]?"));
		
		folder = path_util.lastNamedSegment(BuildDiskLayout.getInstance().getTestAspect());
		assertTrue(folder.matches("[\\/]?test[\\/]?"));
		
		folder = path_util.lastNamedSegment(BuildDiskLayout.getInstance().getRunAspect());
		assertTrue(folder.matches("[\\/]?run[\\/]?"));
	}
	
	

}
