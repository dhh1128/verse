/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 */
package verse.util;

import java.io.File;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.regex.Pattern;

import verse.io.path_util;

/**
 * This class exposes the internal build disk layout.  Should only be used by
 * class that only run during the build process (normally just testing)
 * 
 * @author Devin
 *
 */
public class BuildDiskLayout 
{
	private final String SANDBOX_ROOT;
	
	private BuildDiskLayout() 
	{
		SANDBOX_ROOT = 
			path_util.normFolder(
                    path_util.addTrailingSep(
                            findRootFromWithin(
                                    classPhysicalFileContainer(getClass()))));
	}
		 
	/**
	* SingletonHolder is loaded on the first execution of Singleton.getInstance() 
	* or the first access to SingletonHolder.INSTANCE, not before.
	*/
	private static class SingletonHolder 
	{ 
		public static final BuildDiskLayout instance = new BuildDiskLayout();
	}
	 
	public static BuildDiskLayout getInstance() 
	{
		return SingletonHolder.instance;
	}
	
	public String getSandboxRoot()
	{
		return SANDBOX_ROOT;
	}
	
	public String getCodeAspect() throws NotInSandboxException, FolderNotFoundException
	{
		if(SANDBOX_ROOT == null)throw new NotInSandboxException("Not running from a sandbox.");
		
		String rtn = path_util.normFolder(path_util.combine(SANDBOX_ROOT + "code"));
		
		checkFolder(rtn);
		
		return rtn;
	}
	
	public String getComponentCodeAspect(String component) throws NotInSandboxException, FolderNotFoundException
	{
		if(SANDBOX_ROOT == null)throw new NotInSandboxException("Not running from a sandbox.");
		
		String rtn = path_util.normFolder(path_util.combine(path_util.combine(getCodeAspect(), component)));
		
		checkFolder(rtn);
		
		return rtn;
	}
	
	public String getBuiltAspect() throws NotInSandboxException
	{
		if(SANDBOX_ROOT == null)throw new NotInSandboxException("Not running from a sandbox.");
		
		String rtn = path_util.normFolder(path_util.combine(SANDBOX_ROOT, "built." + getDefaulatPlatformVariant()));
		
		checkFolder(rtn);
		
		return rtn;
	}
	
	public String getBuiltAspect(String targetPlatform) throws NotInSandboxException
	{
		if(SANDBOX_ROOT == null)throw new NotInSandboxException("Not running from a sandbox.");
		
		String rtn = path_util.normFolder(path_util.combine(SANDBOX_ROOT, "built." + targetPlatform));
		
		checkFolder(rtn);
		
		return rtn;
	}
	
	public String getComponentBuiltAspect(String component) throws NotInSandboxException, FolderNotFoundException
	{
		if(SANDBOX_ROOT == null)throw new NotInSandboxException("Not running from a sandbox.");
		
		String rtn = path_util.normFolder(path_util.combine(SANDBOX_ROOT, "built." + getDefaulatPlatformVariant(), component));
		
		checkFolder(rtn);
		
		return rtn;
	}
	
	public String getComponentBuiltAspect(String component, String targetPlatform) throws NotInSandboxException, FolderNotFoundException
	{
		if(SANDBOX_ROOT == null)throw new NotInSandboxException("Not running from a sandbox.");
		
		String rtn = path_util.normFolder(path_util.combine(SANDBOX_ROOT, "built." + targetPlatform, component));
		
		checkFolder(rtn);
		
		return rtn;
	}
	
	public String getTestAspect() throws NotInSandboxException
	{
		if(SANDBOX_ROOT == null)throw new NotInSandboxException("Not running from a sandbox.");
		
		String rtn = path_util.normFolder(path_util.combine(SANDBOX_ROOT, "test"));
		
		checkFolder(rtn);
		
		return rtn;
	}
	
	public String getComponentTestAspect(String component) throws NotInSandboxException, FolderNotFoundException
	{
		if(SANDBOX_ROOT == null)throw new NotInSandboxException("Not running from a sandbox.");
		
		String rtn = path_util.normFolder(path_util.combine(getTestAspect(), component));
		
		checkFolder(rtn);
		
		return rtn;
	}
	
	public String getRunAspect() throws NotInSandboxException
	{
		if(SANDBOX_ROOT == null)throw new NotInSandboxException("Not running from a sandbox.");
		
		String rtn = path_util.normFolder(path_util.combine(SANDBOX_ROOT, "run"));
		
		checkFolder(rtn);
		
		return rtn;
	}
	
	public String getComponentRunAspect(String component) throws NotInSandboxException, FolderNotFoundException
	{
		if(SANDBOX_ROOT == null)throw new NotInSandboxException("Not running from a sandbox.");
		
		String rtn = path_util.normFolder(path_util.combine(getRunAspect(), component));
		
		checkFolder(rtn);
		
		return rtn;
	}
	
	public static String getDefaulatPlatformVariant()
	{
		String rtn = "";
		
		if(OSInfo.isMac)
		{
			rtn = "osx_universal";
		}
		else if(OSInfo.isLinux)
		{
			rtn = "linux";
			if(OSInfo.is32bit)
			{
				rtn += "_i686";
			}
			else
			{
				rtn += "_x86-64";
			}
		}
		else if(OSInfo.isWindows)
		{
			rtn = "win";
			if(OSInfo.is32bit)
			{
				rtn += "_32";
			}
			else
			{
				rtn += "_x64";
			}
		}
		else
		{
			rtn = "unknown";
		}
		
		return rtn;
	}
	
	public static final Pattern ROOT_DIRECTORY_PATTERN = Pattern.compile("^[^.]+\\..+\\.[^.]+$", Pattern.CASE_INSENSITIVE);
	
	public static String findRootFromWithin(String startPath)
	{
		startPath = new File(startPath).getAbsolutePath();
		startPath = path_util.normFolder(startPath);
		
		String[] segments = startPath.split("[\\/]");
		
		for(int i = 0; i < segments.length; i++)
		{
			if(ROOT_DIRECTORY_PATTERN.matcher(segments[i]).matches())
			{
				return str_util.join("/", Arrays.copyOfRange(segments, 0, i+1));
			}
		}
		
		return null;
		
	}
	
	public static String classPhysicalFileContainer(Class<?> cls)
	{
		ProtectionDomain pd = cls.getProtectionDomain();
		if (pd != null) {
			CodeSource cs = pd.getCodeSource();
			if (cs != null) {
				String url = cs.getLocation().getPath();
				if (url != null) {
					return url;
				}
			}
		}
		return null;
		
	}
	
	public void checkFolder(String path) throws FolderNotFoundException
	{
		if(!(new File(path).exists()))
		{
			new FolderNotFoundException("Component '%s' was not found in the run root");
		}
	}
	
	public static class FolderNotFoundException extends RuntimeException
	{
		private static final long serialVersionUID = 994712259410643123L;

		public FolderNotFoundException() {
			super();
		}

		public FolderNotFoundException(String arg0, Throwable arg1) {
			super(arg0, arg1);
		}

		public FolderNotFoundException(String message) {
			super(message);
		}

		public FolderNotFoundException(Throwable cause) {
			super(cause);
		}
	}
	
	public static class NotInSandboxException extends RuntimeException
	{
		private static final long serialVersionUID = 994712259410643123L;

		public NotInSandboxException() {
			super();
		}

		public NotInSandboxException(String arg0, Throwable arg1) {
			super(arg0, arg1);
		}

		public NotInSandboxException(String message) {
			super(message);
		}

		public NotInSandboxException(Throwable cause) {
			super(cause);
		}
	}
}
