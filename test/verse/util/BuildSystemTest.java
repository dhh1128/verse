/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Mar 1, 2010
 */
package verse.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import org.junit.Test;

import verse.io.path_util;

/**
 * 
 */
public class BuildSystemTest {
	
	/*
	UNIT TEST TEMPORARILY DISABLED
	By: Stas Shaposhnikov
	Which: testGetCodeRoot
	Where: globally
	When: 2011-09-30
	Ticket: 4752
	Owner: stas.shaposhnikov@dsr-company.com
	Why: this method works incorrectly when code aspect is inaccessible 
	*/
	//@Test
	public void testGetCodeRoot() {
		String cr = BuildSystem.getCodeRoot(BuildSystem.class);
		File f = new File(path_util.combine(cr, BuildSystem.MY_COMPONENT));
		assertTrue(f.isDirectory());
	}
	
	/*
	UNIT TEST TEMPORARILY DISABLED
	By: Stas Shaposhnikov
	Which: testGetBuildRoot
	Where: globally
	When: 2011-09-30
	Ticket: 4752
	Owner: stas.shaposhnikov@dsr-company.com
	Why: this method works incorrectly when code aspect is inaccessible 
	*/
	//@Test
	public void testGetBuildRoot() {
		String cr = BuildSystem.getBuildRoot(BuildSystem.class);
		File f = new File(path_util.combine(cr, BuildSystem.MY_COMPONENT));
		assertTrue(f.isDirectory());
	}

	@Test
	public void testRunningWithinComponentCodeFolder() throws URISyntaxException, IOException {
		java.net.URL url = BuildSystemTest.class.getResource("./");
		if (url != null) {
			String componentBuildFolder = null;
			File f = new File(url.toURI());
			while (true) {
				f = f.getParentFile();
				if (f == null) {
					break;
				} else if (f.getName().equals(BuildSystem.MY_COMPONENT)) {
					componentBuildFolder = path_util.normFolder(f.getCanonicalPath());
					break;
				}
			}
			if (componentBuildFolder != null) {
				String otherPath = componentBuildFolder + "test/com/perfectsearchcorp/util/BuildSystemTest.java";
				boolean isAlsoCodeFolder = new File(otherPath).exists();
				boolean withinCode = BuildSystem.runningWithinComponentCodeFolder();
				if (isAlsoCodeFolder != withinCode) {
					fail(String.format("runningWithinComponentCodeFolder = %s, but exists(\"%s\") = %s", Boolean.toString(withinCode), otherPath, Boolean.toString(isAlsoCodeFolder)));
				}
			}
		} else {
			System.out.println("skipping testRunningWithinComponentCodeFolder()");
		}
	}
	
	@Test
	public void testBuildDate() throws ParseException {
		assertTrue(BuildSystem.getBuildDate(BuildSystem.class).getTime() > 
			DateUtil.parseStandardDate("2010-03-01").getTime());
	}
	
	@Test
	public void testGetURLForClass() {
		URL url = BuildSystem.getURLForClass(BuildSystem.class);
		assertTrue(url != null);
		assertTrue( (url.toString().contains("psjbase.jar")) || (url.toString().contains("BuildSystem.class")) );
	}
	
	@Test
	public void testGetLastModified() throws ParseException {
		Date date = BuildSystem.getLastModifiedDate(BuildSystem.class);
		assertTrue(date != null);
		assertTrue( date.getTime() > DateUtil.parseStandardDate("2005-03-01").getTime() );
	}

	@Test
	public void testGetSize() {
		long size = BuildSystem.getSize(BuildSystem.class);
		assertTrue(size > 0);
	}
	
}
