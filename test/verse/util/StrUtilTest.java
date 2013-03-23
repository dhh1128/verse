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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class StrUtilTest {

	@Test
	public void testSqueeze() {
		assertEquals("fyi 2 foo", str_util.squeeze("fyi 2 foo\r\n"));
		assertNull(str_util.squeeze(null));
		assertEquals("", str_util.squeeze(" \t\r\n \t \t \t \n"));
		assertEquals("", str_util.squeeze(""));
		assertEquals("This is a test", str_util.squeeze(" \rThis  is\ta test\n\n\t  \t\n"));
	}
	
	@Test
	public void testIndexOfAny() {
		assertEquals(-1, str_util.indexOfAny("abcdef", " "));
		assertEquals(0, str_util.indexOfAny("abcdef", " ", true));
		assertEquals(0, str_util.indexOfAny("abcdef", " b", true));
		assertEquals(1, str_util.indexOfAny("abcdef", " b"));
		assertEquals(-1, str_util.indexOfAny(null, " b"));
		assertEquals(-1, str_util.indexOfAny("abcdef", null));
		assertEquals(-1, str_util.indexOfAny("", " b"));
		assertEquals(-1, str_util.indexOfAny("abcdef", ""));
	}
	
	private static final long KB = 1024L;
	private static final long MB = KB * KB;
	private static final long GB = KB * MB;
	private static final long TB = KB * GB;
    private static final long PB = KB * TB;
	
	@Test
	public void testParseSize() {
	    assertEquals(0L, str_util.parseSize("0TB"));
	    assertEquals(5L * KB, str_util.parseSize("5k"));
        assertEquals(5L * KB, str_util.parseSize("5K"));
        assertEquals(5L * KB, str_util.parseSize("5Kb"));
        assertEquals(5L * KB, str_util.parseSize("5KB"));
        assertEquals(5000L, str_util.parseSize("5000"));
        assertEquals(10L * MB, str_util.parseSize("10m"));
        assertEquals(10L * MB, str_util.parseSize("10M"));
        assertEquals(10L * MB, str_util.parseSize("10mB"));
        assertEquals(10L * MB, str_util.parseSize("10MB"));
        assertEquals(100L * GB, str_util.parseSize("100g"));
        assertEquals(100L * GB, str_util.parseSize("100G"));
        assertEquals(100L * GB, str_util.parseSize("100Gb"));
        assertEquals(100L * GB, str_util.parseSize("100GB"));
        assertEquals(1000L * TB, str_util.parseSize("1000t"));
        assertEquals(1000L * TB, str_util.parseSize("1000T"));
        assertEquals(1000L * TB, str_util.parseSize("1000tB"));
        assertEquals(1000L * TB, str_util.parseSize("1000TB"));
        assertEquals(3 * PB, str_util.parseSize("3p"));
        assertEquals(3 * PB, str_util.parseSize("3P"));
        assertEquals(3 * PB, str_util.parseSize("3Pb"));
        assertEquals(3 * PB, str_util.parseSize("3PB"));
	}
	
	@Test
	public void testJoin() {
		assertEquals("a,b,c", str_util.join(",", new String[]{"a","b","c"}));
	}

	@Test
	public void testCountAny() {
		assertEquals(3, str_util.countAny("abc123xyz", "0123456789"));
	}
	
	@Test
	public void testWrap() {
		// Wrapping should not remove trailing lines, but it should
		// remove trailing spaces and normalize line endings.
		assertEquals("abc\nx\nfoo\n\n", str_util.wrap("abc x \rfoo\n\r\n", 1, "\n"));
		assertNull(str_util.wrap(null, 25));
		assertEquals("", str_util.wrap("", 25));
		// Wrapping something that doesn't fit should have no effect. 
		assertEquals("abc", str_util.wrap("abc", 1));
		assertEquals("x\nabc", str_util.wrap("x abc", 1, "\n"));
		assertEquals("abc\nx", str_util.wrap("abc x", 1, "\n"));
		assertEquals("abc\nx", str_util.wrap("abc x", 1, "\n"));
		// Wrapping too short for words should give one word per line.
		assertEquals("abc\nxyz", str_util.wrap("abc xyz", 1, "\n"));
		assertEquals(
				"no indent\n  indented line with\n  a whole bunch of\n  words", str_util.wrap(
				"no indent\n  indented line with a whole bunch of words", 20, "\n"));
		String x = str_util.wrap(
				"-jar showcase.jar [--] [--conf=PATH] [--ir=FOLDER] [--lfn=FNAMEPAT] [--lf=FOLDER] [--log=LEVEL] [--loop-count=LOOP-COUNT] [--ov=XPATH=Value;...]", 75, "\n");
		assertEquals(
				"-jar showcase.jar [--] [--conf=PATH] [--ir=FOLDER] [--lfn=FNAMEPAT]\n[--lf=FOLDER] [--log=LEVEL] [--loop-count=LOOP-COUNT]\n[--ov=XPATH=Value;...]", x);
	}
	
	@Test
	public void testPad() {
		assertEquals("abc", str_util.padLeft("abc", 3));
		assertEquals("abc", str_util.padRight("abc", 3));
		assertEquals("abc", str_util.padLeft("abc", 3, 'x'));
		assertEquals("abc", str_util.padRight("abc", 3, 'x'));
		assertEquals(" abc", str_util.padLeft("abc", 4));
		assertEquals("abc ", str_util.padRight("abc", 4));
		assertEquals("xabc", str_util.padLeft("abc", 4, 'x'));
		assertEquals("abcx", str_util.padRight("abc", 4, 'x'));
		assertEquals("    ", str_util.padLeft(null, 4));
		assertEquals("    ", str_util.padRight("", 4));
		assertEquals("xxxx", str_util.padLeft("", 4, 'x'));
		assertEquals("xxxx", str_util.padRight(null, 4, 'x'));
	}
	
	@Test
	public void testIndexOfCloseQuote() {
		assertEquals(4, str_util.indexOfCloseQuote("'abc'", 0));
		assertEquals(-1, str_util.indexOfCloseQuote("'abc", 0));
		assertEquals(10, str_util.indexOfCloseQuote("abc\"x'y\\\"z\"", 3));
		assertEquals(-1, str_util.indexOfCloseQuote("'abc\n'", 0));
	}
	
	@Test
	public void testIndexOfCloseGroup() {
		assertEquals(6, str_util.indexOfCloseGroup("((abc))", 0));
		assertEquals(5, str_util.indexOfCloseGroup("[[abc]]", 1));
		assertEquals(11, str_util.indexOfCloseGroup("{abc,\n{xyz}}", 0));
		assertEquals(14, str_util.indexOfCloseGroup("<a, <b>, 'd >'>>", 0, false, true));
		assertEquals(12, str_util.indexOfCloseGroup("<a, <b>, 'd >'>>", 0, false, false));
		assertEquals(6, str_util.indexOfCloseGroup("<a, <b>, 'd >'>>", 4, false, true));
	}
	
	@Test
	public void testArrayFromLines() {
		assertArrayEquals(new String[] {"abc", "xyz"},
				str_util.getArrayFromLines("abc\r\n  \nxyz\n"));
		assertArrayEquals(new String[] {"abc", "", "xyz"}, 
				str_util.getArrayFromLines("abc\r\n  \nxyz\n", true, true));
		assertArrayEquals(new String[] {"abc\r", "", "  ", "xyz"}, 
				str_util.getArrayFromLines("abc\r\n\n  \nxyz\n", false, true));
	}
	
	@Test
	public void testTrim() 
	{
		assertEquals("trim",str_util.trimRight("trims", 1));
		assertEquals("rims",str_util.trimLeft("trims", 1));
		assertEquals("",str_util.trimRight("trims", 10));
		assertEquals("",str_util.trimRight("trims", 5));
		
		
		assertEquals("",str_util.trim("'''''''''''", "'"));
		assertEquals("d",str_util.trim("''''''d'''''", "'"));
		assertEquals("",str_util.trim("''#'''''''$", "'$#"));
		assertEquals("d''d",str_util.trim("''''''d''d'''''", "'"));
		assertEquals("test",str_util.trim("test", "'"));
	}
	
	@Test
	public void testIsNullOrEmpty () {
		CharSequence cs = "abcd";
		assertFalse(str_util.is_null_or_empty(cs));
		cs = "";
		assertTrue(str_util.is_null_or_empty(cs));
		cs = null;
		assertTrue(str_util.is_null_or_empty(cs));
	}
	
	@Test
	public void testParseBooleanLenient() {
		CharSequence cs = "123";
		assertTrue(str_util.parseBooleanLenient(cs));
		cs = "abcd";
		assertFalse(str_util.parseBooleanLenient(cs));
		cs = "t";
		assertTrue(str_util.parseBooleanLenient(cs));
		cs = "true";
		assertTrue(str_util.parseBooleanLenient(cs));
		cs = "y";
		assertTrue(str_util.parseBooleanLenient(cs));
		cs = "yes";
		assertTrue(str_util.parseBooleanLenient(cs));
		cs = "on";
		assertTrue(str_util.parseBooleanLenient(cs));
		cs = "checked";
		assertTrue(str_util.parseBooleanLenient(cs));
		cs = "-12";
		assertTrue(str_util.parseBooleanLenient(cs));
	}
	
	@Test
	public void testIndexOf() {
		CharSequence cs = "Test text";
		char c = 'x';
		assertEquals(str_util.indexOf(cs, c), 7);
		c = 'j';
		assertEquals(str_util.indexOf(cs, c), -1);
	}
	
	@Test
	public void testLastIndexOfAny() {
		CharSequence hs = "Test Text";
		CharSequence any = "t";
		assertEquals(str_util.lastIndexOfAny(hs, any), 8);
		any = "j";
		assertEquals(str_util.lastIndexOfAny(hs, any), -1);
		
	}
	
	@Test
	public void testReplaseAll() {
		CharSequence hs = "Text to testing. Text to testing.";
		Pattern patt = Pattern.compile("to");
		CharSequence rw = "for";
		assertEquals(str_util.replaceAll(hs, patt, rw), "Text for testing. Text for testing.");
	}
	
	@Test
	public void testCount() {
		CharSequence hs = "Text for testing.";
		assertEquals(str_util.count(hs, 't'), 3);
	}
	
	@Test
	public void testCapitalize() {
		assertEquals(str_util.capitalize("test text"), "Test text");
	}
	
	@Test
	public void testToTitleCase() {
		assertEquals(str_util.toTitleCase("Text for testing. Text for testing."),
				"Text For Testing. Text For Testing.");
	}
	
	@Test
	public void testToCamelCase() {
		assertEquals(str_util.toCamelCase("Text For Testing. Text For Testing.", " "), "TextForTesting.TextForTesting.");
	}
	
	@Test
	public void testIsWrapChar() {
		assertFalse(str_util.isWrapChar('c'));
		assertTrue(str_util.isWrapChar('\n'));
		assertTrue(str_util.isWrapChar('\r'));
	}
	
	@Test
	public void testGetListFromLines() {
		List<String> ls = str_util.getListFromLines("Text for testing. \nText for testing. \nText for testing.");
		assertTrue(ls.size() == 3);
		ls = str_util.getListFromLines("Text for testing. Text for testing. Text for testing.");
		assertTrue(ls.size() == 1);
	}
	
	@Test
	public void testGetLinesFromArray() {
		String[] srtArr = {"Text for testing 1.", "Text for testing 2.", "Text for testing 3."};
		assertEquals(str_util.getLinesFromArray(srtArr), "Text for testing 1.\nText for testing 2.\nText for testing 3.");
	}
	
	@Test
	public void testGetLinesFromList() {
		List<String> items = new ArrayList<String>();
		items.add("Text for testing 1.");
		items.add("Text for testing 2.");
		items.add("Text for testing 3.");
		assertEquals(str_util.getLinesFromList(items), "Text for testing 1.\nText for testing 2.\nText for testing 3.");
	}
	
	@Test
	public void testGetRegexesFromLines() {
		String lines = "^\\s*(t(rue)?|y(es)?|on|checked|-[1-9]\\d*)\\s*$\n" +
				"<meta.*http-equiv=['|\"]Content-Type['|\"].*charset=[^'|\"]*\n" +
				"<meta.*http-equiv=['|\"]Content-Type['|\"].*charset=";
		Pattern[] patterns = str_util.getRegexesFromLines(lines);
		assertTrue(patterns.length == 3);
	}
	
	@Test
	public void testMatchAny(){
		Pattern[] patterns = {Pattern.compile("\\w.*"), Pattern.compile("\\d.*")};
		String potentialMatch = "This is small example text.";
		Matcher m = str_util.matchAny(patterns, potentialMatch);
		assertNotNull(m);
		
		m = str_util.matchAny(Arrays.asList(patterns), potentialMatch);
		assertNotNull(m);
	}
	
	@Test
	public void testFormatSize() 
	{
		assertEquals(str_util.formatSize(254897114), "243.09 MB");
		assertEquals(str_util.formatSize(254897114, "MB"), "243.09 MB");
		assertEquals(str_util.formatSize(254897114, "GB"), "0.24 GB");
		assertEquals(str_util.formatSize(2548971140L, "GB"), "2.37 GB");
		assertEquals(str_util.formatSize(67517534208L, "GB"), "62.88 GB");
	}
	
	@Test
	public void testSafeEquals() {
		assertTrue(str_util.safeEquals("test text", "test text"));
		assertTrue(str_util.safeEquals("", ""));
		assertTrue(str_util.safeEquals(null, null));
		assertFalse(str_util.safeEquals("test text", null));
		assertFalse(str_util.safeEquals("test text", ""));
		assertFalse(str_util.safeEquals("", null));
	}
	
	@Test
	public void testEscapeEntities() {
		assertEquals(str_util.escapeEntities("<tag>text</tag>"), "&lt;tag&gt;text&lt;/tag&gt;");
	}
}
