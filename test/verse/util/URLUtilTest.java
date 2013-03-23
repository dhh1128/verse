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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

public class URLUtilTest {
	/**
	 * Return a string where backslashes are treated the same way the URL
	 * constructor does on the current platform.
	 */
	private static String normBack(Object url) {
		// On Windows, backslashes and forward slashes are interchangeable
		// as path separators (in nearly all cases). When you create a URL
		// from a path on Windows, backslashes are changed to forward slashes
		// by the URL constructor because slashes are the standard separator
		// in URLs. However, on *nix, backslashes are valid filename chars;
		// the URL constructor on *nix must therefore leave them unmodified.
		String txt = url.toString();
		if (OSInfo.isWindows) {
			txt = txt.replace('\\', '/');
		}
		return txt;
	}
	
	@Test
	public final void testMissingProtocol() {
		// Windows file URI
		URL url = URLUtil.create("file:///c:/tmp/path", "ftp");
		assertEquals("file:/c:/tmp/path", url.toString());
		
		// Windows absolute path
		url = URLUtil.create("c:\\tmp\\path", "file");
		assertEquals(normBack("file:c:\\tmp\\path"), url.toString());

		// Windows relative path
		url = URLUtil.create("tmp\\path", "file");
		assertEquals(normBack("file:tmp\\path"), url.toString());

		// UNIX file URI
		url = URLUtil.create("file:///tmp/path", "ftp");
		assertEquals("file:/tmp/path", url.toString());
		
		// UNIX absolute path
		url = URLUtil.create("/tmp/path", "file");
		assertEquals("file:/tmp/path", url.toString());

		// UNIX relative path
		url = URLUtil.create("tmp/path", "file");
		assertEquals("file:tmp/path", url.toString());
	}
	
	@Test
	public final void testBadProtocol() {
		URL url = URLUtil.create("foo://myhost.com:1000", "ftp");
		assertEquals("ftp://myhost.com:1000", url.toString());
		url = URLUtil.create("://myhost.com:1000", "ftp");
		assertEquals("ftp://myhost.com:1000", url.toString());
	}

	@Test
	public final void testBadPort() {
		URL url = URLUtil.create("ftp://myhost.com:100000000000000000000", "ftp");
		assertNull(url);
		url = URLUtil.create("ftp://myhost.com:text/query", "ftp");
		assertNull(url);
	}
	
	@Test
	public void testUrlToFile() throws IOException
	{
		char sep = System.getProperty("file.separator").charAt(0);
		
		URL url = URLUtil.create("file:/data/vms/test/linux64image-20111107.tar.bz2", "file");
		File f = URLUtil.urlToFile(url);
		String expected = "/data/vms/test/linux64image-20111107.tar.bz2".replace('/', sep); 
		assertEquals(expected, f.getPath());
		
		url = URLUtil.create("file://server/data/vms/test/linux64image-20111107.tar.bz2", "file");
		f = URLUtil.urlToFile(url);
		expected = "/data/vms/test/linux64image-20111107.tar.bz2".replace('/', sep); 
		assertEquals(expected, f.getPath());
		
//		url = URLUtil.create("file:///c:/data/vms/test/linux64image-20111107.tar.bz2", "file");
//		f = URLUtil.urlToFile(url);
//		expected = "/c:/data/vms/test/linux64image-20111107.tar.bz2".replace('/', sep); 
//		assertEquals(expected, f.getPath());
		
		url = URLUtil.create("file:///data/vms/test/linux64image-20111107.tar.bz2", "file");
		f = URLUtil.urlToFile(url);
		expected = "/data/vms/test/linux64image-20111107.tar.bz2".replace('/', sep); 
		assertEquals(expected, f.getPath());
	}
	public final void testUrlToFile2() throws IOException {
		checkUrlToFile("file:/path/to/file", "/path/to/file");
		checkUrlToFile("file:///path/to/file", "/path/to/file");
		checkUrlToFile("file://host/path/to/file", "/path/to/file");
		if (OSInfo.isWindows) {
			checkUrlToFile("file:/C:/path/to/file", "C:/path/to/file");
			checkUrlToFile("file://host/C:/path/to/file", "C:/path/to/file");
			checkUrlToFile("file:///C:/path/to/file", "C:/path/to/file");
		}
	}
	
	private void checkUrlToFile(String urlStr, String path) throws IOException {
		URL url = new URL(urlStr);
		File file = URLUtil.urlToFile(url);
		assertNotNull(file);
		File testFile = new File(path);
		assertEquals(testFile.getPath(), file.getPath());
	}
}
