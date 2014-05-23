/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Nottingham
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Nottingham
   nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

</COPYRIGHT>

Created by: James Mathrick (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  James Mathrick (University of Nottingham)

 */
/**
 * BootGlobals.java
 * Global class providing constants and helper methods.
 * @author jym
 */

package equip.ect.webstart;


class BootGlobals
{

	public static final boolean DEBUG = true;
	/**
	 * default dataspace URL - must have blank for host and port specified
	 */
	public static final String DEFAULT_DS_URL = "equip://:9123/";
	/**
	 * default service type URL for dataspace discovery
	 */
	public static final String DEFAULT_SERVICE = "equip.data.DataProxy:2.0";

	/**
	 * directory for dataspace persistence information
	 */
	public static final String DS_DIR_NAME = "dataspacePersistence";
	/**
	 * dataspace config file name
	 */
	public static final String DS_CONFIG_FILE = "equip.eqconf";
	/**
	 * {@link equip.ect.webstart.Boot} class configuration file, in java Property format.
	 */
	public static final String PROP_FILE_NAME = "boot.prop";
	public static final String SECRET_FILE_NAME = "shared.secret";
	public static final String NULL_VALUE = "none";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	/**
	 * Boot property file property name for list of {@link Installation}s to auto-start
	 */
	public static final String STARTUP_PROP = "startup";
	public static final String SECRET_PROP = "secret";
	public static final String GROUP_PROP = "groupName";
	/**
	 * URL to download/update from
	 */
	public static final String CONFIG_URL_PROP = "download";
	/**
	 * Installation property file property - should clear out persistence data on restart
	 */
	public static final String COLD_START_PROP = "coldStart";
	public static final String EXEC_PROP = "componentExecutable";
	public static final String COMPONENT_DIR_PROP = "componentDirectory";
	public static final String PERSIST_FILE_PROP = "persistFile";
	public static final String CONTAINER_NAME = "containerName";
	public static final String JAVA_CONTAINER = "java";

	public static final int SECRET_LENGTH = 100;
	public static final long DISCOVER_TIMEOUT = 9000;
	public static final long DISCOVER_WAIT = 1000;

	public static final String DOWNLOAD_EPOCH = "downloadEpoch";
	public static final String DOWNLOAD_EPOCH_TUPLETYPE = "downloadEpoch-1.0";
	public static final String UPLOAD_REQUEST_TUPLETYPE = "uploadRequest-1.0";

	public static final String NO_DISCOVERY_DS_URL = "fixedDataspaceUrl";

	private static java.io.File localFilestoreRoot = null;

	public static void addProperty(final java.util.Properties prop, final String key, final String val)
	{
		if (DEBUG)
		{
			System.err.println("Add prop: " + key);
		}
		final String existing = prop.getProperty(key);
		if (existing == null)
		{
			prop.setProperty(key, val);
		}
		else
		{
			prop.setProperty(key, existing + "," + val);
		}
	}

	public static String clobberGroupName(final String groupName)
	{
		// clobber group name to folder name
		final String safePrefix = "group";
		final StringBuffer safe = new StringBuffer(groupName);
		int index = 0;
		while (index < safe.length())
		{
			if (Character.isLetterOrDigit(safe.charAt(index)) || safe.charAt(index) == '_')
			{
				index++;
			}
			else
			{
				safe.deleteCharAt(index);
			}
		}
		// NB. potentially one could use a group name with *no* safe chars
		// thus group folder would initially be called "group" ... fixed in Installation():92
		return safePrefix + safe.toString();
	}

	public static java.io.File getLocalFilestoreRoot()
	{
		if (localFilestoreRoot != null) { return localFilestoreRoot; }
		String home = ".";
		try
		{
			home = System.getProperty("user.home");
		}
		catch (final Exception e)
		{
		}
		try
		{
			home = new java.io.File(home).getCanonicalPath();
		}
		catch (final java.io.IOException e)
		{
			System.err.println("ERROR: Unable to get canonical path for " + home + ": " + e);
		}
		// graceful-ish degradation
		localFilestoreRoot = new java.io.File(home + getSeparator() + "ect" + getSeparator());
		boolean mkdirSuccess = false;;
		if (!localFilestoreRoot.exists())
		{
			mkdirSuccess = localFilestoreRoot.mkdir();
		}
		if (DEBUG)
		{
			System.err.println("Using as filestore root: " + localFilestoreRoot.getAbsolutePath() + ", success: "
					+ mkdirSuccess);
		}
		return localFilestoreRoot;
	}

	// Fix in OSX for java's so-called portability
	public static String getSeparator()
	{
		final String sep = "" + java.io.File.separatorChar;
		// sep = (sep.equals(":"))? "/":sep;
		return sep;
	}

	public static String listSubDirs(final java.io.File fn)
	{
		// Shortcut stupidity
		if (fn == null || !fn.isDirectory()) { return ""; }
		// Find the directory
		final java.util.ArrayList foo = new java.util.ArrayList();
		final java.io.File[] files = fn.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			if (!files[i].isDirectory())
			{
				continue;
			}
			foo.add(files[i].getName());
		}
		// Compose the string
		final StringBuffer bar = new StringBuffer();
		for (int i = 0; i < foo.size(); i++)
		{
			if (i > 0)
			{
				bar.append(",");
			}
			bar.append((String) (foo.get(i)));
		}
		return bar.toString();
	}

	public static String listSubDirsWithPropFiles(final java.io.File fn)
	{
		// Shortcut stupidity
		if (fn == null || !fn.isDirectory()) { return ""; }
		// Find the directory
		final java.util.ArrayList foo = new java.util.ArrayList();
		final java.io.File[] files = fn.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			if (!files[i].isDirectory())
			{
				continue;
			}
			if (!new java.io.File(files[i], PROP_FILE_NAME).exists())
			{
				continue;
			}
			foo.add(files[i].getName());
		}
		// Compose the string
		final StringBuffer bar = new StringBuffer();
		for (int i = 0; i < foo.size(); i++)
		{
			if (i > 0)
			{
				bar.append(",");
			}
			bar.append((String) (foo.get(i)));
		}
		return bar.toString();
	}

	public static java.util.Properties loadPropertyFile(final java.io.File filename)
	{
		if (DEBUG)
		{
			System.err.println("Load prop file: " + filename.getAbsolutePath());
		}
		// shortcut
		if (!filename.exists()) { return null; }

		// Do the do
		final java.util.Properties prop = new java.util.Properties();
		try
		{
			final java.io.FileInputStream in = new java.io.FileInputStream(filename);
			prop.load(in);
			in.close();
			return prop;
		}
		// Evil errors
		catch (final java.io.IOException e)
		{
			if (DEBUG)
			{
				System.err.println(e.getMessage());
			}
			return null;
		}
	}

	// Helper methods
	public static String[] parseStringArray(final String s)
	{
		if (s == null || s.equals("")) { return new String[0]; }
		final java.util.ArrayList foo = new java.util.ArrayList();
		int count = 0;
		while (s.indexOf(',', count) > 0)
		{
			final int t = s.indexOf(',', count);
			foo.add(s.substring(count, t));
			count = t + 1;
		}
		foo.add(s.substring(count, s.length()));
		if (foo.size() == 0) { return new String[] { s }; }
		final String[] bar = new String[foo.size()];
		for (int i = 0; i < foo.size(); i++)
		{
			bar[i] = (String) (foo.get(i));
		}
		return bar;
	}

	public static void savePropertyFile(final java.io.File filename, final java.util.Properties prop)
	{
		if (DEBUG)
		{
			System.err.println("Save prop file: " + filename.getAbsolutePath());
		}
		try
		{
			final java.io.FileOutputStream out = new java.io.FileOutputStream(filename, false);
			prop.store(out, "");
			out.close();
		}
		catch (final java.io.IOException e)
		{
			// ah well, never mind, better luck next discovery
			if (DEBUG)
			{
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * default (and empty) cons.
	 */
	public BootGlobals()
	{
	}
}
