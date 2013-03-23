/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 */
package verse.util;

import java.io.File;
import java.io.IOException;

public class FileUtil 
{
	/**
	 * Deletes a non-empty directory by recursively deleting all files in all sub-directories
	 * 
	 * @param path The path of the director to be deleted, must be a directory
	 * @return Whether deleting the directory was successful or not
	 * @throws IOException When the path parameter don't point to a directory
	 */
	public static boolean deleteNonEmptyDir(String path) throws IOException
	{
		return deleteNonEmptyDir(new File(path));
	}
	
	/**
	 * Deletes a non-empty directory by recursively deleting all files in all sub-directories
	 * 
	 * @param dir The file object that points to the director to be deleted, must be a directory
	 * @return Whether deleting the directory was successful or not
	 * @throws IOException When the dir parameter don't point to a directory
	 */
	public static boolean deleteNonEmptyDir(File dir) throws IOException
	{
	    if (dir.isDirectory()) 
	    {
	    	deleteNonEmptyDirR(dir);
	    }
	    else
	    {
	    	throw new IOException("File must be a directory");
	    }
	    
	    return dir.delete();
	}
	
	private static boolean deleteNonEmptyDirR(File dir) throws IOException
	{
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) 
	        {
	            boolean success = deleteNonEmptyDirR(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }
	    
	    return dir.delete();
	}

}
