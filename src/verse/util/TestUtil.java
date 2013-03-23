/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: Daniel Hardman
 * Created: Sep 14, 2009
 */
package verse.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import verse.io.path_util;

/**
 * Utilities used during testing.
 */
public class TestUtil {

	public static void assertApproxEquals(double a, double b) {
		assertApproxEquals(a, b, 10000);
	}

	public static void assertApproxEquals(double a, double b, int partsPer) {
		if (!approxEquals(a, b, partsPer)) {
			throw new java.lang.AssertionError(String.format(
					"%g not equal to %g within 1 part in %d", a, b, partsPer));
		}
	}

	public static boolean approxEquals(double a, double b, int partsPer) {
		if (partsPer < 2 || partsPer > 10000000)
			throw new IllegalArgumentException(
					"partsPer should be between 2 and 10,000,000");
		double within = 1.0 / partsPer;
		return (Math.abs(a - b) <= within);
	}

	/** 
	 * Used in various unit tests to check a large piece of text for 
	 * the existence or non-existence of listed fragments. 
	 */
	public static String checkTextOccurrence(String text, String[] fragments, boolean shouldOccur, String ctx) {
		StringBuilder sb = new StringBuilder();
		for (String txt: fragments) {
			if (text.indexOf(txt) == -1) {
				if (shouldOccur) {
					sb.append(String.format("Expected to find \"%s\" in %s.%n", txt, ctx));
				}
			} else if (!shouldOccur) {
				sb.append(String.format("Expected NOT to find \"%s\" in %s.%n", txt, ctx));
			}
		}
		return (sb.length() > 0) ? sb.toString() : null;
	}

	/** Used to test sample files. */
	public static interface SampleFileValidator {
		boolean validate(String path);
		String describeSample();
	}
	
	abstract public static class AbstractSampleFileValidator implements SampleFileValidator {
		@Override
		public abstract String describeSample();
		@Override
		public boolean validate(String path) {
			boolean valid = false;
			Logger logger = Logger.getLogger("");
			TestLogFilter filter = new TestLogFilter();
			filter.suppressBelowLevel = Level.WARNING.intValue();
			try {
				logger.setFilter(filter);
				valid = doValidate(logger, path);
				if (valid && filter.getCountForLevel(Level.SEVERE) > 0 ||
						filter.getCountForLevel(Level.WARNING) > 0) {
					valid = false;
				}
			} finally {
				logger.setFilter(null);
			}
			return valid;
		}
		abstract protected boolean doValidate(Logger logger, String path); 
	}
	
	/**
	 * Scan a folder for sample files that match a regex; run each through a validator.
	 * @param fileMatcher regex to use to select samples
	 * @param component where cls comes from, e.g., "psjbase"
	 * @param validator
	 * @return number of files that fail validation.
	 */
	public static int validateSampleFiles(Pattern fileMatcher, String component, /*Class<?> cls,*/ SampleFileValidator validator) {
		int badCount = 0;
		String path = getUnitTestFilesPath(component, "sample-conf-files");
		if (path != null) {
			File f = new File(path);
			for (File item : getFiles(f, fileMatcher, new ArrayList<File>())) 
			{
				boolean ok = validator.validate(item.getAbsolutePath());
				if (!ok) {
					System.err.print(String.format(
							"%s is not a valid %s file.%n", item.getAbsolutePath(), validator.describeSample()));
					badCount += 1;
				}
			}
		}
		return badCount;
	}
	
	private static List<File> getFiles(File director, Pattern fileMatcher, List<File> list)
	{
		for(File f: director.listFiles()) 
		{
			if(f.isFile())
			{
				if(fileMatcher.matcher(f.getAbsolutePath()).matches()) list.add(f);
			}
			if(f.isDirectory())
			{
				getFiles(f, fileMatcher, list);
			}
		}
        
		return list;
	}

	/**
	 * Turn on all logging. Useful when debugging unit tests.
	 */
	public static void logEverything(Logger logger) {
		// JUnit appears to be lazy about hooking up a console handler, and when
		// it hooks one up, it inits to write only errors and warnings. We want
		// to A) force JUnit to create its handler; B) guarantee it's at the
		// right level. To do this, create a new ConsoleHandler. Then set all
		// handlers to level=ALL. Then remove our new ConsoleHandler; somehow
		// JUnit is set correctly from then on.
		logger.setLevel(Level.ALL);
		logger.info("now logging everything");
		Handler handler = new ConsoleHandler();
		logger.addHandler(handler);
		for (Handler h : logger.getHandlers()) {
			h.setLevel(Level.ALL);
		}
	}
	
	public static String getTestPath(String component /*, Class<?> testClass*/) {
		return getTestPath(component, /*testClass,*/ null);
	}

	public static String getTestPath(String component, /*Class<?> testClass,*/ String relative) {
		String path = BuildDiskLayout.getInstance().getComponentTestAspect(component);
		//String path = path_util.normFolder(new File(BuildSystem.
		//		getPathToJavaCodeForClass(component, testClass)).getParent());
		if (!str_util.is_null_or_empty(relative)) {
			path = path_util.combine(path, relative);
		}
		File mightExist = new File(path);
		if (mightExist.exists()) {
			return path;
		}
		return null;
	}
	
	public static String getUnitTestFilesPath(String component, String relative) {
		return getTestPath(component, path_util.combine("resources", "java-junit-tests-files", relative));
	}
}
