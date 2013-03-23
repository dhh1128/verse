/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: Daniel Hardman
 * Created: Sep 8, 2009
 */
package verse.io;

//import static com.perfectsearchcorp.io.TreeWalker.FILES_ONLY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Set;
import java.util.regex.Pattern;

import verse.util.OSInfo;
import verse.util.str_util;

/**
 * Utilities for manipulating file system paths and similar strings.
 */
public class path_util {

	/**
	 * @return f.getCanonicalPath(), if that method can be called without an
	 *         exception, or f.getAbsolutePath() otherwise.
	 */
	public static String getMostExplicitPath(String s) {
		return getMostExplicitPath(new File(s));
	}

	/**
	 * @return f.getCanonicalPath(), if that method can be called without an
	 *         exception, or f.getAbsolutePath() otherwise.
	 */
	public static String getMostExplicitPath(File f) {
		try {
			return f.getCanonicalPath();
		} catch (IOException e) {
			try {
				return f.getCanonicalPath();
			} catch (Exception ex) {
				return f.getAbsolutePath();
			}
		}
	}
	/**
	 * @param folder A {@link File} holding a path to a folder.
	 * @return A folder path in normalized form -- like
	 *         {@link File#getCanonicalPath()} but always uses / instead of \,
	 *         and always terminates with /. Also, unlike
	 *         File.getCanonicalPath(), this method does not throw an exception;
	 *         it degrades gracefully to call {@link File#getAbsolutePath()}
	 *         instead.
	 */
	public static String normFolder(File folder) {
		return normFolder(folder.getPath());
	}

	/**
	 * Forces all separators in a path to /. Compare {@link #defaultSeparators}
	 * ().
	 */
	public static String normSeparators(String path) {
		return path.replace('\\', '/');
	}

	/**
	 * Forces all separators in a path to {@link File#separatorChar}. Compare
	 * {@link #normSeparators}().
	 */
	public static String defaultSeparators(String path) {
		if (File.separatorChar != '/')
			path = path.replace('/', File.separatorChar);
		if (File.separatorChar != '\\')
			path = path.replace('\\', File.separatorChar);
		return path;
	}

	/**
	 * @return The location of the temp dir.
	 */
	public static String getTempPath() {
		return normFolder(System.getProperty("java.io.tmpdir"));
	}
	
	/**
	 * @param path
	 * @return <code>true</code> if path ends in a valid separator char
	 * on any OS.
	 */
	public static boolean endsWithSeparator(String path) {
		return path.endsWith("/") || path.endsWith("\\");		
	}
	
	/**
	 * @param c
	 * @return true if c is a valid separator on any OS.
	 */
	public static boolean isSeparator(char c) {
		return c == '\\' || c == '/';
	}

	/**
	 * @return Folder path guaranteed to end in separator char.
	 */
	public static String addTrailingSep(String folder) {
		if (!endsWithSeparator(folder)) {
			folder += File.separatorChar;
		}
		return folder;
	}
	
	/**
	 * @return Folder path guaranteed to end in normal '/' separator char.
	 */
	public static String addNormalTrailingSep(String folder) {
		if (!endsWithSeparator(folder)) {
			folder += '/';
		}
		return folder;
	}
	
	/**
	 * @return Folder path guaranteed NOT to end in separator char.
	 */
	public static String cutTrailingSep(String folder) {
		if (endsWithSeparator(folder)) {
			folder = folder.substring(0, folder.length() - 1);
		}
		return folder;
	}

	/**
	 * @return a folder path in normalized form -- like {@link
	 *         File#getCanonicalPath()} but always uses / instead of \, and
	 *         always terminates with /.
	 */
	public static String normFolder(String value) {
		value = getMostExplicitPath(value);
		value = addTrailingSep(value);
		value = normSeparators(value);
		return value;
	}

	/**
	 * Recursively remove a folder and all its children.
	 * 
	 * @param folder
	 *            The folder to remove. If path does not exist or is not a
	 *            folder, returns false.
	 * @return true if remove succeeds.
	 */
	public static boolean removeFolder(String folder) {
		File f = new File(folder);
		return removeFolder(f);
	}

	/**
	 * Recursively remove a directory and all its children.
	 * 
	 * @param folder
	 *            The folder to remove. If f does not exist or is not a
	 *            folder, returns false.
	 * @return true if remove succeeds.
	 */
	public static boolean removeFolder(File folder) {
		if (!folder.isDirectory()) {
			return false;
		}
		String root = normFolder(folder);
		String[] items = folder.list();
		if (items != null && items.length > 0) {
			for (String item : items) {
				File child = new File(root + item);
				if (child.isDirectory()) {
					if (!removeFolder(child)) {
						return false;
					}
				} else {
					if (!child.delete()) {
						return false;
					}
				}
			}
		}
		return folder.delete();
	}

	private static final Pattern EXTRA_DELIM_PAT = Pattern
			.compile("[/\\\\]{2,}");

	/**
	 * Combine various segments into a single path.
	 * 
	 * @param segments
	 * @return A string where each segment is delimited by a single forward
	 *         slash.
	 */
	public static String combine(String... segments) {
		if (segments == null || segments.length == 0) {
			return "";
		}
		String result = str_util.join("/", segments);
		return EXTRA_DELIM_PAT.matcher(result).replaceAll("/");
	}

	/**
	 * @param path
	 * @return last segment of a path. Might be a file name or folder name. If
	 *         the path ends in a segment delimiter or is null/empty, returns
	 *         "".
	 */
	public static String lastSegment(String path) {
		if (str_util.is_null_or_empty(path)) {
			return "";
		}
		int i = str_util.lastIndexOfAny(path, "\\/");
		if (i == -1) {
			return path;
		}
		return path.substring(i + 1);
	}

	/**
	 * Returns the last segment of the path that is not empty. If the path ends
	 * in a segment delimiter, returns the item before the delimiter.
	 * 
	 * @param path
	 * @return last non-null, non-empty segment of a path, or null if the path
	 *         is null/empty. The return value will end in a slash if
	 *         <code>path</code> does.
	 */
	public static String lastNamedSegment(String path) {
		if (str_util.is_null_or_empty(path)) {
			return null;
		}
		int i = path.length() > 1 ? str_util.lastIndexOfAny(path, "\\/", -2)
				: -1;
		if (i == -1) {
			return path;
		}
		return path.substring(i + 1);
	}

	/**
	 * A convenience method to read the full contents of a small- to
	 * medium-sized text file.
	 * 
	 * @param path
	 * @return The text of the file.
	 * @throws FileNotFoundException
	 */
	public static String getAllText(String path, String encoding)
			throws IOException {
		File f = new File(path);
		return getAllText(f, encoding);
	}

	/**
	 * A convenience method to read the full contents of a small- to
	 * medium-sized text file.
	 * 
	 * @param f
	 *            File to read.
	 * @return The text of the file.
	 * @throws FileNotFoundException
	 */
	public static String getAllText(File f, String encoding) throws IOException {
		FileInputStream fin = new FileInputStream(f);
		InputStreamReader in = new InputStreamReader(fin, encoding);
		StringBuilder sb = new StringBuilder(fin.available());
		char[] chars = new char[1024];
		int charsRead;
		while ((charsRead = in.read(chars)) != -1) {
			sb.append(chars, 0, charsRead);
		}
		in.close();
		fin.close();
		return sb.toString();
	}

	/**
	 * A convenience method to write a small- to medium-sized text file all at once.
	 * 
	 * @param text
	 * @param path
	 * @param encoding
	 * @throws IOException
	 */
	public static void setAllText(String path, String text, String encoding) throws IOException {
		setAllText(new File(path), text, encoding);
	}
	
	/**
	 * A convenience method to write a small- to medium-sized text file all at once.
	 * 
	 * @param text
	 * @param f
	 * @param encoding
	 * @throws IOException
	 */
	public static void setAllText(File f, String text, String encoding) throws IOException {
		FileOutputStream fout = new FileOutputStream(f);
		OutputStreamWriter out = new OutputStreamWriter(fout, encoding);
		out.write(text);
		out.close();
		fout.close();
	}
	
	/**
	 * Changes a file on disk, using the contents of a temporary version as the
	 * source. This allows processes to write to temp, delete existing, and then
	 * rename temp to correct name, for maximum reliability. It also allows us
	 * to compare text and only update if there are substantive changes. 
	 * 
	 * @param path
	 * @param tempPath
	 * @param onlyIfTextHasChanged
	 * @return true if the file was updated
	 * @throws IOException if file cannot be updated.
	 */
	public static boolean updateFileFromTempVersionOnDisk(String path, 
			String tempPath, boolean onlyIfTextHasChanged) throws Exception {
		
		// Does this file already exist?
		File existing = new File(path);
		if (existing.isFile()) {
			// If yes, then rather than mindlessly replacing, check
			// to see if anything has changed.
			if (onlyIfTextHasChanged) {
				String txt = path_util.getAllText(path, "UTF-8");
				txt = str_util.squeeze(txt);
				String txt2 = path_util.getAllText(tempPath, "UTF-8");
				txt2 = str_util.squeeze(txt2);
				if (txt.equals(txt2)) {
					new File(tempPath).deleteOnExit();
					return false;
				}
			}
			boolean ok = false;
			Throwable ex = null;
			try {
				existing.setWritable(true);
				if (existing.delete()) {
					ok = true;
				}
			} catch (Throwable e) {
				ex = e;
			}
			if (!ok) {
				FileNotFoundException fnf = new FileNotFoundException(
						String.format("%s cannot be deleted.", path));
				if (ex != null) {
					fnf.initCause(ex);
				}
				throw fnf;				
			}			
		}
		if (!new File(tempPath).renameTo(existing)) {
			throw new FileNotFoundException(
					String.format("%s cannot be renamed to %s.", tempPath, path));
		}
		return true;
	}

	/**
	 * Given two paths, find the relative path from base to target. No attempt
	 * is made to make paths absolute before comparison; if you a comparison
	 * based on absolute paths, call {@link #getMostExplicitPath(File)} on
	 * each parameter first.
	 * 
	 * @param to
	 *            The path we're trying to get to.
	 * @param from
	 *            The path we're starting from. This path must end in a slash if
	 *            it's a folder -- otherwise we assume it's a file, which makes
	 *            our starting point that file's container.
	 * @return The relative path, or the fully-qualified path if there is no
	 *         relationship between the two paths. The relative path always uses
	 *         forward slashes as its separator.
	 */
	public static String getRelativePath(String to, String from) {

		// We need the -1 argument to split to make sure we get a trailing
		// "" token if the base ends in the path separator and is therefore
		// a directory. We require directory paths to end in the path
		// separator -- otherwise they are indistinguishable from files.
		String[] fSegs = normSeparators(from).split("/", -1);
		String[] tSegs = normSeparators(to).split("/", 0);

		// First get all the common elements. Store them as a string,
		// and also count how many of them there are.
		String common = "";
		int commonIndex = 0;
		for (int i = 0; i < tSegs.length && i < fSegs.length; i++) {
			if (tSegs[i].equals(fSegs[i])) {
				common += tSegs[i] + "/";
				commonIndex++;
			} else
				break;
		}

		if (commonIndex == 0) {
			// Whoops -- not even a single common path element. This most
			// likely indicates differing drive letters, like C: and D:.
			// These paths cannot be relativized. Return the target path.
			return normSeparators(to);
			// This should never happen when all absolute paths
			// begin with / as in *nix.
		}

		String relative = "";
		if (fSegs.length == commonIndex) {
			// Comment this out if you prefer that a relative path not start
			// with ./
			// relative = "." + pathSeparator;
		} else {
			int numDirsUp = fSegs.length - commonIndex - 1;
			// The number of directories we have to backtrack is the length of
			// the base path MINUS the number of common path elements, minus
			// one because the last element in the path isn't a directory.
			for (int i = 1; i <= (numDirsUp); i++) {
				relative += "../";
			}
		}
		relative += to.substring(common.length());

		return normSeparators(relative);
	}
	
	/**
	 * @return true if the specified path is absolute instead of relative.
	 * On Windows, there is a quasi-absolute state (represented by paths like
	 * "\tmp" or "e:docs"); these paths are considered absolute by this function.
	 */
	public static boolean isAbsolutePath(String path) {
		if (str_util.is_null_or_empty(path)) {
			return false;
		}
		char firstChar = path.charAt(0);
		if (firstChar == '.') {
			return false;
		}
		if (OSInfo.isWindows) {
			return firstChar == '/' || firstChar == '\\' || 
				(Character.isLetter(firstChar) && path.length() > 1 && path.charAt(1) == ':');
		}
		return firstChar == '/';
	}
	
	/**
	 * @param path
	 * @xparam fileRegex A regex that controls which files are counted. May be null (= all files).
	 * @xparam recurseRegex A regex that controls which folders are recursed into. May be null (= all folders).
	 * @return A {#link CountingVisitor} describing size and number of files/folders. Root
	 * 		folder is not counted.
	 */
    /*
	public static CountingVisitor countFolder(String path, String fileRegex, String recurseRegex) {
		CountingVisitor sv = new CountingVisitor(fileRegex, recurseRegex);
		TreeWalker.walk(path, sv, FILES_ONLY);
		return sv;		
	}
	
	public static void deleteTree(String path) {
		Visitor visitor = new RegexVisitor((Pattern)null, (Pattern)null) {
			@Override
			protected boolean doVisit(String root, String fullPath, boolean isFolder) {
				File f = new File(fullPath);
				if (!f.canWrite()) {
					f.setWritable(true);
				}
				return f.delete();
			}
		};
		TreeWalker.walk(path, visitor, TreeWalker.DEPTH_FIRST | TreeWalker.VISIT_ROOT);
	}
	*/
	
	public static void deleteFile(String path) throws Throwable {
		File f = new File(path);
		try{
			f.delete();
		}catch (Throwable e)
		{
			throw e;
		}
	}
	
	private static final int BUFFERSIZE = 4096;

	/**
	 * @return Full path to the home folder for the user that owns the 
	 * current process. The folder should always exist and be writable.
	 *  
	 * <p>On Windows, this folder is %userprofile% -- typically 
	 * %SystemDrive%/Documents and Settings/%username% (pre-Vista) or
	 * %SystemDrive%/users/%username% (Vista+). On *nix, this is the
	 * full path to ~.</p> 
	 */
	public static String getUserHomeFolder() {
		// The user.home variable should work everywhere, but a bug in Sun
		// JREs makes its behavior inconsistent; see 
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4787931.
		if (OSInfo.isWindows) {
			return normFolder(System.getenv("USERPROFILE"));
		} else {
			return normFolder(System.getProperty("user.home"));
		}
	}

	/**
	 * @return Full path to a folder that all users can read and write.
	 * 
	 * <p>On Windows, this folder is %allusersprofile% -- typically 
	 * %SystemDrive%/Documents and Settings/All Users (pre-Vista) or 
	 * %SystemDrive%/ProgramData (Vista+).</p>
	 * 
	 * <p>There is no equivalent concept on *nix -- there, daemons typically 
	 * run with a special account that belongs to the application, so user-specific
	 * folders are all that's needed. If this function is called on *nix, 
	 * the temp folder is returned.</p>
	 */
	public static String getAllUsersFolder() {
		// The user.home variable should work everywhere, but a bug in Sun
		// JREs makes its behavior inconsistent; see 
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4787931.
		if (OSInfo.isWindows) {
			return normFolder(System.getenv("ALLUSERSPROFILE"));
		} else {
			return path_util.getTempPath();
		}
	}
	
	/**
	 * @return Full path to a folder where the user who owns the current
	 * process can persist application data.
	 * <p>On Windows, this folder is %appdata% or %localappdata% -- typically 
	 * %SystemDrive%/Documents and Settings/%username%/Application Data (pre-Vista) or
	 * %SystemDrive%/users/%username%/AppData/Local (Vista+). On *nix, this is the
	 * full path to ~.</p>
	 */
	public static String getUserAppDataFolder() {
		if (OSInfo.isWindows) {
			String appData = System.getenv(
					USE_OLD_WINDOWS_FOLDERS ? "APPDATA" : "LOCALAPPDATA");
			if (appData == null) {
				String profile = normFolder(System.getenv("USERPROFILE"));
				if (USE_OLD_WINDOWS_FOLDERS) {
					appData = path_util.combine(profile, "Application Data");
				} else {
					appData = path_util.combine(profile, "AppData", "Local");
				}
			} else {
				appData = normFolder(appData);
			}
			return appData;
		} else {
			return getUserHomeFolder();
		}
	}

	private static final boolean USE_OLD_WINDOWS_FOLDERS;
	static {
		if (OSInfo.isWindows) {
			String osname = System.getProperty("os.name");
			USE_OLD_WINDOWS_FOLDERS = (osname.indexOf("XP") > -1) || (osname.indexOf("2003") > -1);
		} else {
			USE_OLD_WINDOWS_FOLDERS = false;
		}
	}
	
	private static String nonWindowsSharedAppDataFolder;
	static {
		nonWindowsSharedAppDataFolder = (!OSInfo.isWindows) ? "" : null;
	}

	/**
	 * @return Full path to a folder where all users can persist application data.
	 * 
	 * <p>On Windows, this folder is %allusersprofile%/Application Data (pre-Vista) or
	 * %allusersprofile%/AppData/Local (Vista+). On *nix, this concept is problematic.
	 * Typically %tmp%/.AppData is returned. This provides mostly similar semantics
	 * to Windows, except that because the folder resides under %tmp%, it is not an ideal
	 * candidate for persistence.</p>
	 */
	public static String getSharedAppDataFolder() {
		// Commonly the following call will return
		// XP-like:  C:\Documents and Settings\All Users
		// Vista, 7: C:\ProgramData
		// Other:    /tmp
		String sharedAppDataFolder = getAllUsersFolder();
		if (OSInfo.isWindows) {
			if (USE_OLD_WINDOWS_FOLDERS) {
				// Add "Application Data" for XP-like Windows.
				// TODO: Think about more proper way to get "Application Data"
				//       directory name. I've read somewhere in web, that it
				//       can be named differently depending on locale. Actually
				//       I've never seen that.
				sharedAppDataFolder =
					normFolder(combine(sharedAppDataFolder, "Application Data"));
			}
		} else {
			synchronized (nonWindowsSharedAppDataFolder) {
				if (nonWindowsSharedAppDataFolder.isEmpty()) {
					File f = new File(combine(getTempPath(), ".AppData"));
					if (!f.exists()) {
						f.mkdirs();
					}
					nonWindowsSharedAppDataFolder = normFolder(f);
				}
			}
			sharedAppDataFolder = nonWindowsSharedAppDataFolder;
		}
		return sharedAppDataFolder;
	}
	
	/**
	 * @return Full path to a folder where software should be installed.
	 * On Windows, this is typically C:/Program Files/. On *nix, it is
	 * typically /opt/.
	 */
	public static String getInstallRoot() {
		if (OSInfo.isWindows) {
			return normFolder(System.getenv("ProgramFiles"));
		} else if (OSInfo.isMac) {
			return "/Applications/";
		} else {
			return "/opt/";
		}
	}
	
	public static String getInstalledRoot(String projectName) {
		URL url = path_util.class.getProtectionDomain().getCodeSource().getLocation();
		String path = url.getFile();
		if (path.endsWith(".jar")) {
			// Assume installed in "root/bin/foo.jar" or "root/lib/bar.jar"
			String jarDir = (new File(path)).getParent();
			jarDir = normFolder(jarDir);
			if (jarDir.endsWith("/lib/")) {
				path = (new File(jarDir)).getParent();
			} else {
				path = path_util.combine((new File(jarDir)).getParentFile().getParent(), projectName);
			}
		} else {
			// Running from source code tree: "root/../psjbase/bin/classes/src/"
			int i = path.lastIndexOf("/bin/");
			if (i != -1) {
				path = path.substring(0, i + 1);
			}
			path = path_util.combine(path_util.normSeparators((new File(path)).getParent()), projectName);
		}
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Do nothing
		}
		return path;
	}
	
	/**
	 * Performs a simple file copy. Ignorant of symbolic links and similar subtleties.
	 * Does not duplicate permissions/attributes. Consider using <a
	 * href="http://openjdk.java.net/projects/nio/javadoc/java/nio/file/Path.html#copyTo(java.nio.file.Path, java.nio.file.CopyOption...)">Java 1.7's Path.copyTo()</a>
	 * method if possible.
	 * 
	 * @param srcpath
	 * @param destpath
	 * @throws IOException
	 */
	public static void copyFile(String srcpath, String destpath) throws IOException {		
		File srcFile = new File(srcpath);
		File destFile = new File(destpath);
		{
			String msg = null;
			if (!srcFile.exists())
				msg = String.format("Cannot copy %s; it does not exist.", srcFile);
			else if (!srcFile.isFile())
				msg = String.format("Cannot copy %s; it is a directory.", srcFile);
			else if (!srcFile.canRead())
				msg = String.format("Cannot copy %s; it is unreadable.", srcFile);
			if (msg != null) {
				throw new IOException(msg);
			}
		}
		FileInputStream src = null;
		FileOutputStream dest = null;
			
		try {
			src = new FileInputStream(srcFile);
			dest = new FileOutputStream(destFile);
			byte[] buffer = new byte[BUFFERSIZE];
			int currentBytesRead;
			while ((currentBytesRead = src.read(buffer)) != -1)
				dest.write(buffer, 0, currentBytesRead);
		} finally {
			if (src != null)
				try { src.close(); } catch (Throwable e) { }
			if (dest != null)
				try { dest.close(); } catch (Throwable e) { }
		}
	}
	
	public static final boolean PATHS_COMPARE_CASE_SENSITIVE = !OSInfo.isWindows;
	
	/** 
	 * Does case-sensitive or case-insensitive compare of paths,
	 * depending on rules of current OS.
	 * @param path1
	 * @param path2
	 * @return -1, 0, 1 (same semantics as string compare)
	 */
	public static int compare(String path1, String path2) {
		return PATHS_COMPARE_CASE_SENSITIVE ? path1.compareTo(path2) :
			path1.compareToIgnoreCase(path2);
	}
	
	/**
	 * Like {{@link #compare(String, String)}, except that any form of a
	 * separator is treated as equivalent, so c:/code/foo and c:\code\foo
	 * are considered identical.
	 * 
	 * @param path1
	 * @param path2
	 * @return -1, 0, or 1, depending on whether path1 is less than, equal to,
	 * 		or greater than path2.
	 */
	public static int compareAnySeparators(String path1, String path2) {
		return compare(normSeparators(path1), normSeparators(path2));
	}
	
	public static String getName(String file) {
		int point = file.lastIndexOf('.');
		if (point != -1) {
			file = file.substring(0, point); 
		}
		return file; 
	}
	
	public static String getName(File file) {
		return getName(file.getName());
	}

	public static String getExtension(String file) {
		int point = file.lastIndexOf('.');
		if (point == -1) {
			return "";
		} else {
			return file.substring(point + 1);
		}
	}
	
	public static String getExtension(File file) {
		return getExtension(file.getName());
	}
	
	public static long directorySize(File directory) {
		long ret = 0L;
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					ret += directorySize(file);
				} else {
					ret += file.length();
				}
			}
		}
		return ret;
	}
	
	public static void delete(File path) {
		if (path == null) {
			return;
		}
		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				delete(f);
			}
		}
		path.delete();
	}
	
	public static boolean isSubdir(String root, String subdir) {
		return path_util.addTrailingSep(subdir).startsWith(path_util.addTrailingSep(root));
	}

	/**
	 * Formats paths for use in a shell command.  Basically, if a path contains a space it wraps it in quotes
	 * 
	 * @param path
	 */
	public static String formatShellPath(String path)
	{
		if(path.trim().contains(" "))
		{
			return "\"" + path.trim() + "\"";
		}
		else
		{
			return path;
		}
	}
}
