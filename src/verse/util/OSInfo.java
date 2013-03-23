/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Oct 22, 2009
 */
package verse.util;

import java.io.File;
import java.io.IOException;

/**
 * Return information about the operating system.
 */
public class OSInfo {

	/**
	 * <p>
	 * Detect whether we're running on some flavor of Windows.
	 * </p>
	 * <p>
	 * Note that in the vast majority of cases, it's better to call one of the
	 * supports...() methods if you must turn on/off a feature or behavior
	 * depending on whether the OS supports it. This is because logic about what
	 * features are supported by a particular OS can be convoluted, change over
	 * time, and be difficult to test; it's best to let this class do such work
	 * rather than re-implementing that logic yourself.
	 * </p>
	 */
	public static final boolean isWindows;

	/**
	 * <p>
	 * <code>true</code> if we're running on some flavor of Linux.
	 * </p>
	 * <p>
	 * Note that in the vast majority of cases, it's better to call one of the
	 * supports...() methods if you must turn on/off a feature or behavior
	 * depending on whether the OS supports it. This is because logic about what
	 * features are supported by a particular OS can be convoluted, change over
	 * time, and be difficult to test; it's best to let this class do such work
	 * rather than re-implementing that logic yourself.
	 * </p>
	 */
	public static final boolean isLinux;
	
	/**
	 * <code>true</code> if we're running on a Mac.
	 */
	public static final boolean isMac;
	
	/**
	 * <p>
	 * Detect whether we're running on an OS other than Windows or Linux.
	 * </p>
	 * <p>
	 * Note that in the vast majority of cases, it's better to call one of the
	 * supports...() methods if you must turn on/off a feature or behavior
	 * depending on whether the OS supports it. This is because logic about what
	 * features are supported by a particular OS can be convoluted, change over
	 * time, and be difficult to test; it's best to let this class do such work
	 * rather than re-implementing that logic yourself.
	 * </p>
	 */
	public static final boolean isOther;

	public static final boolean is32bit;
	public static final boolean is64bit;
	public static final boolean isppc;
	
	/**
	 * Return the PID for the currently running process. Usually this string is
	 * numeric, but it is not guaranteed to be so on all platforms.
	 */
	public static final int pid;

	public enum ShellStyle { BASH, WINDOWS_CMD };
	
	/**
	 * What type of shell is used by the current OS?
	 */
	public static final ShellStyle shellStyle;

	static {
		String osName = System.getProperty("os.name").toLowerCase().replace(
				" ", "");
		if (osName.indexOf("linux") != -1) {
			isWindows = false;
			isMac = false;
			isLinux = true;
			isOther = false;
		} else if (osName.indexOf("mac") != -1) {
			isMac = true;
			isWindows = false;
			isLinux = false;
			isOther = false;
		} else {
			isLinux = false;
			isMac = false;
			String windir = System.getenv("windir");
			isWindows = (!str_util.is_null_or_empty(windir));
			isOther = !isWindows;
		}
		shellStyle = isWindows ? ShellStyle.WINDOWS_CMD : ShellStyle.BASH;
		pid = findPid();
		System.setProperty("pid", Integer.toString(pid));

		/*
			sun.arch.data.model doesn't return the arch type, so we have to get os.arch regardless
		*/
		
/*		String model = System.getProperty("sun.arch.data.model");
		if (model != null && !model.isEmpty()) {
			if (model.equals("32")) {
				is32bit = true;
				is64bit = false;
				isppc = false;
			} else if (model.equals("64")) {
				is32bit = false;
				is64bit = true;
				isppc = false;
			} else {
				is32bit = false;
				is64bit = false;
				isppc = false;
			}
		} else
*/ 
		{
			String arch = System.getProperty("os.arch");
			if (arch.equals("x86") || arch.equals("i386")) {
				is32bit = true;
				is64bit = false;
				isppc = false;
			} else if (arch.equals("amd64") || arch.equals("x86_64")) {
				is32bit = false;
				is64bit = true;
				isppc = false;
			} else if (arch.equals("ppc")) {
				is32bit = false;
				is64bit = false;
				isppc = true;
			} else {
				is32bit = false;
				is64bit = false;
				isppc = false;
			}
		}
	}
	
	/**
	 * @return A string that is used to distinguish the platform and architecture
	 * of various binaries. This string may be added to a binary name or embedded
	 * in folder structure.
	 */
	public static String getPlatformSpecificBinaryTag() {
		if (isMac) {
			return "osx_universal";
		} else if (isWindows) {
			return is32bit ? "win_32" : "win_x64";
		} else if (isLinux) {
			if (is32bit)
				return "linux_i386";
			if (is64bit)
				return "linux_x86-64";
			if (isppc)
				return "linux_ppc";
		}
		return is32bit ? "x_i386" : "x_x86-64";
	}
	
	/**
	 * @return extension (if any) commonly associated with native platform binaries -- .exe
	 * on windows, the empty string on other platforms.
	 */
	public static String getBinarySuffix() {
		return isWindows ? ".exe" : "";
	}
	
	private static int findPid() {
		// There is no perfect, well-supported way to do this. We have to try
		// several variations.
		
		// First see if pid was passed on cmdline or config file.
		String pid = System.getProperty("pid");
		if (str_util.is_null_or_empty(pid)) {
			
			// This method works on Sun JVMs on all platforms. It should always
			// work on Windows, since we use a Sun JVM there.
			String beanName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
			if (!str_util.is_null_or_empty(beanName)) {
				String[] x = beanName.split("@");
				if (x != null && x.length > 1) {
					pid = x[0];
				}
			}
			
			if (str_util.is_null_or_empty(pid)) {
				if (!isWindows) {
					
					// Try using /proc/self -- which is a symbolic link to our
					// pid file.
					try {
						pid = new File("/proc/self").getCanonicalFile().getName();
						Long.parseLong(pid);
					} catch (Throwable e) {
						pid = null;
					}
					
					// Still no dice? Use the shell.
					if (str_util.is_null_or_empty(pid)) {
						String[] cmd = new String[] { "/bin/sh", "-c", "echo $PPID" };
						try {
							Process p = Runtime.getRuntime().exec(cmd);
							StringBuilder sb = new StringBuilder();
							int c;
							while ((c = p.getInputStream().read()) != -1) {
								if (Character.isDigit(c)) {
									sb.append((char)c);
								} else {
									break;
								}
							}
							pid = sb.toString();
						} catch (IOException e) {
						}
					}
				}
			}
		}
		try {
			return Integer.parseInt(pid);
		} catch (Throwable e) {
			return 0;
		}
	}

	/**
	 * @return a name for this OS.
	 */
	public static String getName() {
		return System.getProperty("os.name");
	}
	
}
