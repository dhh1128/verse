/**
 * $Id$
 * 
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 * 
 */
package verse.util;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author dimm
 * Added for installer generator to determine package version from ant tasks
 */
public class ExtractVersion {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Specify a jar file to read");
			return;
		}
		try {
			String file = args[0];
			JarFile jarfile = new JarFile(file);
			Manifest manifest = jarfile.getManifest();
			Attributes attr = manifest.getMainAttributes();
			String ver = attr.getValue("Implementation-Version");
			if (ver != null) {
				System.out.println(ver);
			} else {
				System.err.println("Can't read version information");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
