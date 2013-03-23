package verse.util;

import java.util.ArrayList;
/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: startsev
 * Created: 
 */

import java.util.Vector;
import java.util.regex.Pattern;

public class DataTypeDateTime extends DataTypeConvertable{

	private static ArrayList<Pattern> patternsDateTime = new ArrayList<Pattern>();
	private static String[] patternsDateTimeStr = {
			"([01]?[0-9]|2[0-3])(:[0-5][0-9])(:[0-5][0-9])?",
			"((0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d))",
			"((0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d))",
			"(((19|20)\\d\\d)/(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01]))",
			"(((19|20)\\d\\d)/(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012]))",
			"((0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[012])-((19|20)\\d\\d))",
			"((0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])-((19|20)\\d\\d))",
			"(((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]))",
			"(((19|20)\\d\\d)-(0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[012]))",
			"((0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.((19|20)\\d\\d))",
			"((0?[1-9]|1[012])\\.(0?[1-9]|[12][0-9]|3[01])\\.((19|20)\\d\\d))",
			"(((19|20)\\d\\d)\\.(0?[1-9]|1[012])\\.(0?[1-9]|[12][0-9]|3[01]))",
			"(((19|20)\\d\\d)\\.(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012]))" };

	static {
		for (int i = 0; i < patternsDateTimeStr.length; i++)
			patternsDateTime.add(Pattern.compile(patternsDateTimeStr[i]));
	}
	
	@Override
	public boolean check(String s) {
		for (int i = 0; i < patternsDateTimeStr.length; i++) {
			if (isMatch(s, patternsDateTime.get(i)) > -1)
				return true;
		}
		return false;
	}
	
	/**
	 * Vector<String> checking
	 * 
	 * @param v
	 *            the Vector<String> for checking
	 * @return Vector<String> containing the date/time, otherwise null
	 */
	public Vector<String> check(Vector<String> v) {
		Vector<String> result = new Vector<String>();
		for (int i = 0; i < v.size(); i++) {
			if (check(v.get(i)))
				result.add(v.get(i));
		}
		if (result.size() < 1)
			return null;
		else
			return result;
	}
	
}
