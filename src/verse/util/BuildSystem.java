/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Feb 26, 2010
 */
package verse.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.text.ParseException;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//fix import app.ManifestInfo;
import verse.io.path_util;

/**
 * Make it easy to find components, the code root, and so forth.
 */
public class BuildSystem {
	
	private static final Class<?> MY_CLASS = BuildSystem.class;
	
	private static int runningWithinCode = -1;
	
	/**
	 * @return true if the code is running from within the component's code
	 * folder. This is typically true in the eclipse IDE, and false for
	 * Ant builds.
	 */
	public static boolean runningWithinComponentCodeFolder() {
		if (runningWithinCode == -1) {
			synchronized (MY_CLASS) {
				if (runningWithinCode == -1) {
					// If we're running from an eclipse build, it's easy...
					java.net.URL url = MY_CLASS.getResource("./");
					if (url != null) {
						String path;
						try {
							path = (new File(url.toURI())).getAbsolutePath();
						} catch (URISyntaxException e) {
							path = url.getPath();
						}
						path = path_util.normFolder(path);
						path = path.replace("/bin/classes/src/", "/src/");
						File f = new File(path + MY_CLASS.getSimpleName() + ".java");
						runningWithinCode = f.isFile() ? 1 : 0;					
					} else {
						runningWithinCode = 0;
					}
				}
			}
		}
		return runningWithinCode == 1;
	}

	/**
	 * @param cls
	 * @return Which folder immediately under a component's code folder should
	 * 		contain the source for this class? If the class is unit-test related,
	 * 		it goes in {component code folder}/test; otherwise it goes in
	 * 		{component code folder}/src.
	 */
	public static String getSourceCategoryForClass(Class<?> cls) {
		String classType = "src";
		String ltxt = getRelativePathForClass(cls).toLowerCase();
		if (ltxt.endsWith("test") || ltxt.indexOf("/test") > -1) {
			classType = "test";
		}
		return classType;
	}
	
	/**
	 * @param cls
	 * @return The path to a class's implementation (.java) or compiled (.class)
	 * 		form, starting from either {component code folder}/{src|test}, or
	 * 		from {component build folder}/bin/classes.
	 */
	public static String getRelativePathForClass(Class<?> cls) {
		return cls.getCanonicalName().replace('.', '/');
	}
	
	private static final Pattern ECLIPSE_CODESOURCE_PAT = Pattern.compile("^(.*/)[^/]+/bin/classes/(src|test)/$", Pattern.CASE_INSENSITIVE);
	/**
	 * @return the path that contained source code for components when the 
	 * 		specified class was compiled.
	 * @param cls The class we're interested in.
	 * See https://www.assembla.com/wiki/show/ps-share/Code_Organization.
	 */
	public static String getCodeRoot(Class<?> cls) {
		String s =  getCodeRootFromFile(cls);
		if(s == null)
		{
//fix			s = getManifestAttribute(cls, ManifestInfo.CODE_ROOT_KEY, null);
		}
		if (s == null) {
			s = getRootFromCodeSource(cls);
/*			if(checkCodeRoot(new File(s), cls))
				return s;
			else
				return null;*/
		}
		return s;
	}
	
	private static String getCodeRootFromFile(Class<?> cls) {
		String buildRoot = getBuildRoot(cls);
		File codeRootFile = new File(buildRoot + "/coderoot.txt");
		if(codeRootFile.isFile())
		{
			try
			{
				FileReader reader;
				reader = new FileReader(codeRootFile);
	
				char[] buf = new char[(int)codeRootFile.length()];
				reader.read(buf);
				
				return path_util.normSeparators(new String(buf).trim());
			}
			catch(Throwable e)
			{
				return null;
			}
		}
		return null;
	}
	
	/**
	 * @return the path that contained built binaries for components when the 
	 * 		specified class was compiled.
	 * @param cls The class we're interested in.
	 * See https://www.assembla.com/wiki/show/ps-share/Code_Organization.
	 */
	public static String getBuildRoot(Class<?> cls) {
//fix		String s = getManifestAttribute(cls, ManifestInfo.BUILD_ROOT_KEY, null);
        String s = null; //fix
		if (s == null) {
			s = getRootFromCodeSource(cls);
		}
		return s;
	}
	
	private static String getRootFromCodeSource(Class<?> cls) {
		ProtectionDomain pd = cls.getProtectionDomain();
		if (pd != null) {
			CodeSource cs = pd.getCodeSource();
			if (cs != null) {
				URL url = cs.getLocation();
				if (url != null) {
					return normalizeRootPath(url);
				}
			}
		}
		return null;
	}
	
	private static String normalizeRootPath(URL url) {
		String ret = url.toString();
		Matcher m = ECLIPSE_CODESOURCE_PAT.matcher(url.toString());
		if (m.matches()) {
			try {
				url = new URL(m.group(1));
			} catch (MalformedURLException e) {
				// Do nothing
				e.printStackTrace();
			}
			ret = url.toString();
		}
		if (url.getProtocol().equals("file")) {
			try {
				ret = (new File(url.toURI())).getAbsolutePath();
				ret = path_util.normSeparators(ret);
			} catch (URISyntaxException e) {
				// Do nothing
			}
		}
		return ret;
	}
	
	/**
	 * @param component
	 * @return The fully qualified path to the code folder for a given component,
	 * 		when the specified class was compiled.
	 * @param cls The class we're interested in.
	 * See https://www.assembla.com/wiki/show/ps-share/Code_Organization.
	 */
	public static String getComponentCodeFolder(String component, Class<?> cls) {
		return path_util.combine(getCodeRoot(cls), component);
	}
	
	/**
	 * @param component
	 * @return The fully qualified path to the build folder for a given component,
	 * 		when the specified class was compiled.
	 * @param cls The class we're interested in.
	 * See https://www.assembla.com/wiki/show/ps-share/Code_Organization.
	 */
	public static String getComponentBuildFolder(String component, Class<?> cls) {
		return path_util.combine(getBuildRoot(cls), component);
	}

	/**
	 * @param component
	 * @param cls The class we're interested in.
	 * @return The fully qualified path to the source code from which a given
	 * 		class was compiled.
	 */
	public static String getPathToJavaCodeForClass(String component, Class<?> cls) {
		return getPathToJavaCodeForClass(getCodeRoot(cls),	component, cls); 
	}
	
	private static String getPathToJavaCodeForClass(String codeRoot, String component, Class<?> cls) {
		return path_util.combine(codeRoot, component,
				getSourceCategoryForClass(cls),
				getRelativePathForClass(cls) + ".java");
	}
	
	/**
	 * @return the URL to the specified class. 
	 * If some jar contains specified class, the url of this jar is returned.
	 * Otherwise return url of the .class file. 
	 * If no URL was found, null is returned.
	 * @param cls The class we're interested in.
	 */
	public static URL getURLForClass(Class<?> cls) {
		URL url = BuildSystem.getJarURLForClass(cls);
		if (url == null) {
			url = cls.getResource(cls.getSimpleName() + ".class");
		}
		return url;
	}
	
	public static URL getJarURLForClass(Class<?> cls) {
		ProtectionDomain pd = cls.getProtectionDomain();
		if (pd != null) {
			CodeSource cs = pd.getCodeSource();
			if (cs != null && cs.getLocation().getPath().toLowerCase().endsWith("jar")) {
				return cs.getLocation();
			}
		}
		return null;
	}
	
	/**
	 * @return the name of the jar that contains a particular class.
	 */
	public static String getJarNameForClass(Class<?> cls, String defaultValue) {
		URL url = getJarURLForClass(cls);
		if (url != null) {
			String txt = url.toString();
			int i = txt.indexOf('?');
			if (i > -1) {
				txt = txt.substring(0, i);
			}
			if (txt.endsWith(".jar")) {
				i = str_util.lastIndexOfAny(txt, "/\\");
				if (i > -1) {
					txt = txt.substring(i + 1, txt.length() - 4);
				}
				return txt;
			}
		}
		return defaultValue;
	}
	
	private static final Date INIT_DATE = new Date();
	
	public static final String MY_COMPONENT = "psjbase";
	
	/**
	 * @return the date at which the code for the specified class was compiled.
	 * If we haven't such information, return the current date.
	 * If getting of date failed, return null.
	 * @param cls The class we're interested in.
	 * See https://www.assembla.com/wiki/show/ps-share/Code_Organization.
	 */
	public static Date getBuildDate(Class<?> cls) {
		String s = getManifestAttribute(cls, "Build-Timestamp", null);
		if (s != null) {
			try {
				return DateUtil.parseStandardDate(s);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			return INIT_DATE;
		}
		return null;
	}
	
	/**
	 * @return the last modification date for the specified class.
	 * If some jar contains specified class, the last modified date of this jar is returned.
	 * Otherwise the last modified date of the .class file is returned. 
	 * If class location wasn't found, null is returned.
	 * @param cls The class we're interested in.
	 */
	public static Date getLastModifiedDate(Class<?> cls) {
		URL url = getURLForClass(cls);
		if (url != null) {
			try {
				File urlFile = URLUtil.urlToFile(url);
				return new Date(urlFile.lastModified());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * @return the size of the executable.
	 * If some jar contains specified class, the size of this jar is returned.
	 * Otherwise the size of the .class file is returned. 
	 * If no class location wasn't found, zero is returned.
	 * @param cls The class we're interested in.
	 */
	public static long getSize(Class<?> cls) {
		URL url = getURLForClass(cls);
		if (url != null) {
			try {
				File urlFile = URLUtil.urlToFile(url);
				return urlFile.length();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return 0L;
	}

	/**
	 * @return a string like "win_x64" or "linux_i386" that describes the
	 * platform that was used to compile the code for the specified class.
	 * Usually, since java is purely platform- and architecture-agnostic,
	 * this is uninteresting. However, it may be useful in cases where
	 * the JNI is invoked.
	 * 
	 * @param cls The class we're interested in.
	 * See https://www.assembla.com/wiki/show/ps-share/Code_Organization.
	 */
	public static String getBuildTimeBuildArch(Class<?> cls) {
		return getManifestAttribute(cls, "Build-Arch", null);
	}

	/**
	 * @param component
	 * @param cls
	 * @return The fully qualified path to the .class file built for this
	 * 		class, before it was moved or added to a .jar.
	 */
	public static String getPathToBuiltClassForClass(String component, Class<?> cls) {
		return path_util.combine(getComponentCodeFolder(component, cls),
				cls.getCanonicalName().replace('.', '/')) + ".java";
	}
	
	private static String getManifestAttribute(Class<?> cls, String attrName, String defaultValue) 
	{
		String cr = defaultValue;
		Attributes attr = null;//fix ManifestInfo.getAttributesForJar(BuildSystem.getJarURLForClass(cls));
		if (attr != null) {
			cr = attr.getValue(attrName);
		}
		return cr;
	}
}
