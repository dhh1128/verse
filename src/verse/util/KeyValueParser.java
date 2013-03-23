package verse.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

//fix import io.InputStreamUtil;

public class KeyValueParser 
{

	public static Map<String, String> parse(Reader input) throws IOException 
	{
		BufferedReader bufferedInput = new BufferedReader(input);
		Map<String, String> rtnMap = new HashMap<String, String>();
		StringBuilder keyBuilder = new StringBuilder(1024);
		StringBuilder valueBuilder = new StringBuilder(1024);
		StringBuilder curBuilder = keyBuilder;
		
		int lineCount = 0;
		
		String line = null;
		
		while((line = bufferedInput.readLine()) != null)
		{
			lineCount++;
			
			if(str_util.is_null_or_empty(line))
			{
				continue;
			}
			
			for(char c: line.toCharArray())
			{
				switch(c)
				{
					case '=':
						if(curBuilder == keyBuilder)
						{
							curBuilder = valueBuilder;
							break;
						}
					default:
						curBuilder.append(c);
				}
			}
			
			String keyValue = keyBuilder.toString().trim();
			if(str_util.is_null_or_empty(keyValue))
			{
				throw new IOException(String.format("Key is empty. (line %s)", lineCount));
			}
			
			rtnMap.put(keyValue, valueBuilder.toString().trim());
			keyBuilder.setLength(0);
			valueBuilder.setLength(0);
			curBuilder = keyBuilder;
		}
		
		return rtnMap;
	}

	public static Map<String, String> parse(InputStream input) throws IOException
	{
		return null; //fix validate(new InputStreamReader(InputStreamUtil.removeUtf8Bom(input), "UTF-8"));
	}
	
	public static Map<String, String> parse(InputStream input, String charsetName) throws IOException
	{
		return null; //fix validate(new InputStreamReader(InputStreamUtil.removeUtf8Bom(input), charsetName));
	}

	public static Map<String, String> parse(File file) throws IOException
	{
		return parse(new FileInputStream(file));
	}
	
	public static Map<String, String> parse(File file, String charsetName) throws IOException
	{
		return parse(new FileInputStream(file), charsetName);
	}
}
