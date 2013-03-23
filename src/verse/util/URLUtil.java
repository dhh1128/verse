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
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

/**
 * @author dimm
 *
 */
public class URLUtil 
{
	static public void pingHost(URL url) throws IOException 
	{
		if(url == null) throw new IllegalArgumentException("unable to ping null url");
		try {
			Socket testSocket;
			int port = url.getPort();
			if (port < 0) {
				port = url.getDefaultPort();
				if (port < 0) {
					port = 80;
				}
			}
			testSocket = new Socket(url.getHost(), port);
			testSocket.close();
		    return;
		} catch (IOException e)	{
			throw e;
		}
	}

	/**
	 * Create a new URL by specified string.
	 */
	public static URL create(String urlstr, String defaultProto) 
	{
		try {
			return new URL(urlstr);
		} catch (MalformedURLException e) {
			String msg = e.getMessage();
			if (msg.contains("no protocol")) {
				if (urlstr.indexOf(":") == -1) {
					urlstr = defaultProto + ":" + urlstr;
				} else {
					urlstr = defaultProto + urlstr;
				}
			} else if (msg.contains("unknown protocol")) {
				String proto = urlstr.substring(0, urlstr.indexOf(":"));
				if (proto.length() > 1) {
					urlstr = urlstr.replace(proto, defaultProto);
				} else {
					urlstr = defaultProto + ":" + urlstr;
				}
			}
			try {
				return new URL(urlstr);
			} catch (MalformedURLException x) {
				return null;
			}
		}
	}
	
	public static File urlToFile(URL url) throws IOException
	{
		if(!url.getProtocol().toLowerCase().equals("file"))
		{
			throw new IOException("Can only create a File object for URL using the 'file' scheme");
		}
		return new File(url.getPath()); 
	}
}
