package verse.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.junit.Test;

import verse.io.path_util;

public class KeyValueParserTest 
{
	@Test
	public void test_readEmptyReader() throws IOException
	{
		Map<String,String> testMap = KeyValueParser.parse(new StringReader(""));
		assertNotNull(testMap);
		assertTrue(testMap.isEmpty());
	}
	
	@Test
	public void test_readSingleLineReader() throws IOException
	{
		Map<String,String> testMap = KeyValueParser.parse(new StringReader("test=value"));
		assertNotNull(testMap);
		assertFalse(testMap.isEmpty());
		assertTrue(testMap.containsKey("test"));
		assertEquals("value", testMap.get("test"));
	}
	
	@Test
	public void test_readMutliLineReader() throws IOException
	{
		Map<String,String> testMap = KeyValueParser.parse(new StringReader("test=value\ntest2=VALUE\r\ntest3=Best\rtest4=D"));
		assertNotNull(testMap);
		assertFalse(testMap.isEmpty());
		assertTrue(testMap.containsKey("test"));
		assertEquals("value", testMap.get("test"));
		assertTrue(testMap.containsKey("test2"));
		assertEquals("VALUE", testMap.get("test2"));
		assertTrue(testMap.containsKey("test3"));
		assertEquals("Best", testMap.get("test3"));
		assertTrue(testMap.containsKey("test4"));
		assertEquals("D", testMap.get("test4"));
	}
	
	@Test
	public void test_handleSimpleWhiteSpace() throws IOException
	{
		Map<String,String> testMap = KeyValueParser.parse(new StringReader(" 	 	test 	 \t= 	 	value 	 	"));
		assertNotNull(testMap);
		assertFalse(testMap.isEmpty());
		assertTrue(testMap.containsKey("test"));
		assertEquals("value", testMap.get("test"));
	}
	
	@Test
	public void test_handleBlankVal() throws IOException
	{
		Map<String,String> testMap = KeyValueParser.parse(new StringReader("test="));
		assertNotNull(testMap);
		assertFalse(testMap.isEmpty());
		assertTrue(testMap.containsKey("test"));
		assertEquals("", testMap.get("test"));
	}
	
	@Test
	public void test_handleBlankKey() throws IOException
	{
		try
		{
			KeyValueParser.parse(new StringReader("=value"));
			fail("Should throw an exception if a key has no value");
		}
		catch(IOException e){}
	}
	
	@Test
	public void test_handleBlankLine() throws IOException
	{
		Map<String,String> testMap = KeyValueParser.parse(new StringReader("\n\n\n\n\n\ntest=value\n\n\n\n\n"));
		assertNotNull(testMap);
		assertFalse(testMap.isEmpty());
		assertTrue(testMap.containsKey("test"));
		assertEquals("value", testMap.get("test"));
		assertEquals(1, testMap.keySet().size());
	}
	
	@Test
	public void test_readStream() throws IOException
	{
		Map<String,String> testMap = KeyValueParser.parse(new ByteArrayInputStream("test=value".getBytes("UTF-8")));
		assertNotNull(testMap);
		assertFalse(testMap.isEmpty());
		assertTrue(testMap.containsKey("test"));
		assertEquals("value", testMap.get("test"));
	}
	
	@Test
	public void test_readNonUtf8Stream() throws IOException
	{
		Map<String,String> testMap = KeyValueParser.parse(new ByteArrayInputStream("test=v\u5C1Dlue".getBytes("ISO-8859-1")));
		assertNotNull(testMap);
		assertFalse(testMap.isEmpty());
		assertTrue(testMap.containsKey("test"));
		assertFalse("v\u5C1Dlue".equals(testMap.get("test")));
		
		testMap = KeyValueParser.parse(new ByteArrayInputStream("test=v\u5C1Dlue".getBytes("UTF-16")), "UTF-16");
		assertNotNull(testMap);
		assertFalse(testMap.isEmpty());
		assertTrue(testMap.containsKey("test"));
		assertEquals("v\u5C1Dlue", testMap.get("test"));
	}
	
	@Test
	public void test_readFile() throws IOException
	{
		String testFilePath = path_util.combine(BuildDiskLayout.getInstance().getComponentTestAspect("psjbase"),"resources", "java-junit-tests-files", "KeyValueParserTest.txt");
		File testFile = new File(testFilePath);
		assertTrue(testFile.exists());
		Map<String,String> testMap = KeyValueParser.parse(testFile);
		
		assertNotNull(testMap);
		assertFalse(testMap.isEmpty());
		assertTrue(testMap.containsKey("dir"));
		assertEquals("c:\\user\\dei\\test", testMap.get("dir"));
		
		assertTrue(testMap.containsKey("chinese_fortune"));
		assertEquals("\u4E2D\u56FD\u8D22\u5BCC", testMap.get("chinese_fortune"));
	}
}
