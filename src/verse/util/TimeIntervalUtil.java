package verse.util;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeIntervalUtil 
{
	private static final Pattern TIME_UNIT_PARSER = Pattern.compile("(?i)\\s*(\\d+)\\s*((?:DAY|HOUR|MINUTE|MIN|SECOND|SEC)(S)?)", Pattern.CASE_INSENSITIVE);
	
	public static Pair<Long, TimeUnit> parseTimeInterval(String val) throws ParseException
	{
		if(str_util.is_null_or_empty(val))
		{
			return null;
		}
		
		Matcher m = TIME_UNIT_PARSER.matcher(val);
		if(m.matches())
		{
			Long rtnL = Long.valueOf(m.group(1));
			
			String unitName = m.group(2).toUpperCase();
			if(unitName.equals("MIN") || unitName.equals("MINS"))
			{
				unitName = "MINUTES";
			}
			else if(unitName.equals("SEC") || unitName.equals("SECS"))
			{
				unitName = "SECONDS";
			}
			else if(unitName.charAt(unitName.length()-1) == 'S'){}
			else
			{
				unitName += "S";
			}
			
			TimeUnit rtnU = TimeUnit.valueOf(unitName);
			
			return new Pair<Long, TimeUnit>(rtnL, rtnU);
		}
		
		throw new ParseException(val, 0);
	}
	
	public static String formatTimeInterval(long val, TimeUnit unit)
	{
		if(val < 0 || unit == null)
		{
			return "";
		}
		
		return val + " " + unit.name();
	}

}
