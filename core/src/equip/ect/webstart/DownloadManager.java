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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.webstart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Does per-installation downloading and update of components and other common dependency files.
 * Copies recursively from a remote directory.
 */
public class DownloadManager
{
	/**
	 * config url
	 */
	protected String configUrl;
	/**
	 * installation directory
	 */
	protected File installation;
	/**
	 * download element name
	 */
	public static final String DOWNLOAD = "download";
	/**
	 * cache element name
	 */
	public static final String CACHE = "cache";
	/**
	 * local directory element name
	 */
	public static final String DIRECTORY = "directory";
	/**
	 * local directory name attribute
	 */
	public static final String PATH = "path";
	/**
	 * remote url element name
	 */
	public static final String URL = "url";
	/**
	 * os arch attribute name
	 */
	public static final String IF_OS_NAME = "ifosname";
	/**
	 * os arch attribute name
	 */
	public static final String IF_OS_ARCH = "ifosarch";
	/**
	 * os arch attribute name
	 */
	public static final String IF_OS_VERSION = "ifosversion";
	/**
	 * temp file suffix
	 */
	public static final String TEMP_SUFFIX = ".downloading";
	/**
	 * backup file suffix
	 */
	public static final String BACKUP_SUFFIX = ".backup";
	/**
	 * last modified file suffix
	 */
	public static final String TIMESTAMP_SUFFIX = ".timestamp";

	/**
	 * copy to local file
	 */
	public static boolean copyToLocalFile(final InputStream in, final File localFile, final int size)
	{
		try
		{
			final FileOutputStream fos = new FileOutputStream(localFile);
			final byte[] buffer = new byte[1024 * 64];
			int actual = 0;
			final DownloadProgress progress = new DownloadProgress(localFile, size);
			while ((actual = in.read(buffer)) != -1)
			{
				fos.write(buffer, 0, actual);
				progress.incrementValue(actual);
			}
			in.close();
			fos.flush();
			fos.close();
			progress.dispose();
			return true;
		}
		catch (final Exception e)
		{
			System.err.println("ERROR copying remote file to local file " + localFile + ": " + e);
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * testing main, usage <localdir> <url>
	 */
	public static void main(final String args[])
	{
		final DownloadManager man = new DownloadManager(new File(args[0]), args[1]);
		man.update();
	}

	/**
	 * cons.
	 * 
	 * @param installation
	 *            root directory of this installation
	 * @param configUrl
	 *            URL of remote directory to copy
	 */
	public DownloadManager(final File installation, final String configUrl)
	{
		this.configUrl = configUrl;
		this.installation = installation;
	}

	/**
	 * check os
	 */
	public boolean checkOs(final String os, final String ifos)
	{
		if (os == null || ifos == null) { return true; }
		if (ifos.endsWith("*"))
		{
			// wildcard
			return os.startsWith(ifos.substring(0, ifos.length() - 1));
		}
		return os.equals(ifos);
	}

	/**
	 * get attribute if present
	 */
	public String extractAttribute(final Node node, final String attribute)
	{
		if (node != null)
		{
			final NamedNodeMap attrs = node.getAttributes();
			final Attr attr = (Attr) attrs.getNamedItem(attribute);
			if (attr != null) { return attr.getValue(); }
		}
		return null;
	}

	/**
	 * get element text
	 */
	public String extractElementText(final Node node)
	{
		final StringBuffer buf = new StringBuffer();
		final NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			final Node childNode = children.item(i);
			if (childNode != null)
			{
				final String text = childNode.getNodeValue();
				if (text != null)
				{
					buf.append(text);
				}
				if (childNode.hasChildNodes())
				{
					buf.append(extractElementText(childNode));
				}
			}
		}
		return buf.toString();
	}

	/**
	 * get element of given tag if present
	 */
	public Node extractNode(final Node parent, final String element)
	{
		if (parent != null) { return extractNode(parent.getChildNodes(), element); }
		return null;
	}

	/**
	 * get element of given tag if present
	 */
	public Node extractNode(final NodeList children, final String element)
	{
		if (children != null)
		{
			for (int i = 0; i < children.getLength(); i++)
			{
				final Node childNode = children.item(i);
				if (childNode != null && childNode.getNodeName().equals(element)) { return childNode; }
			}
		}
		return null;
	}

	/**
	 * update - blocks/
	 * 
	 * @return true if updated OK; false if error/failure
	 */
	public boolean update()
	{
		// parses cache to HashMap of String(path) -> Vector of URL
		return updateDir(installation, configUrl.replace('\\', '/'), 0);
	}

	/**
	 * hacky approx to reading an XML attribute with 1 char lookahead
	 */
	protected String readAttribute(final BufferedReader in, final int[] c) throws IOException
	{
		while (Character.isWhitespace((char) c[0]))
		{
			c[0] = in.read();
		}
		final StringBuffer b = new StringBuffer();
		boolean inQuote = false;
		while (c[0] >= 0)
		{
			if (c[0] == '>' || (c[0] == '/' && !inQuote))
			{
				if (b.length() == 0)
				{
					b.append((char) c[0]);
					c[0] = in.read();
				}
				return b.toString();
			}
			if (c[0] == '"')
			{
				inQuote = !inQuote;
			}
			if (Character.isWhitespace((char) c[0]) && !inQuote) { return b.toString(); }
			b.append((char) c[0]);
			c[0] = in.read();
		}
		return b.toString();
	}

	/**
	 * copy from remote directory
	 */
	protected boolean updateDir(final File localDir, String remoteUrl, final int level)
	{
		boolean error = false;
		if (!remoteUrl.endsWith("/"))
		{
			remoteUrl = remoteUrl + "/";
		}
		System.out.println("Download from " + remoteUrl + " to " + localDir + "...");
		final Vector fileNames = new Vector();
		if (remoteUrl.startsWith("http"))
		{
			try
			{
				// <a ... href="...">
				final URL remote = new URL(remoteUrl);
				URLConnection remoteConn = null;

				// System.out.println("Check header of remote "+remote);
				remoteConn = remote.openConnection();
				remoteConn.connect();

				final String status = remoteConn.getHeaderField(null);
				if (status.indexOf(" 2") < 0)
				{
					System.err.println("ERROR reading directory from url " + remoteUrl + ", http status " + status);
					try
					{
						remoteConn.getInputStream().close();
					}
					catch (final Exception ee)
					{
					}
					error = true;
					return !error;
				}
				System.out.println("Parsing " + remoteUrl + "...");
				final BufferedReader in = new BufferedReader(new InputStreamReader(remoteConn.getInputStream()));
				final int c[] = new int[1];
				c[0] = in.read();
				do
				{
					if (c[0] < 0)
					{
						// eof
						break;
					}
					if (c[0] != '<')
					{
						c[0] = in.read();
						continue;
					}
					c[0] = in.read();
					if (c[0] != 'a' && c[0] != 'A')
					{
						c[0] = in.read();
						continue;
					}
					c[0] = in.read();
					if (!Character.isWhitespace((char) c[0]))
					{
						c[0] = in.read();
						continue;
					}
					while (true)
					{
						final String a = readAttribute(in, c);
						if (a.equals(">") || a.equals(""))
						{
							break;
						}
						if (a.toLowerCase().startsWith("href=\""))
						{
							final String url = a.substring(6, a.length() - 1);
							if (url.indexOf("?") >= 0 || url.startsWith("/") || url.startsWith(".")
									|| url.startsWith("\\"))
							{
								System.out.println("Ignore URL with query: " + url);
							}
							else
							{
								System.out.println("Found possible filename: " + url);
								fileNames.addElement(url);
							}
						}
						else
						{
							System.out.println("Ignore attribute: " + a);
						}
					}
				}
				while (true);
				in.close();
			}
			catch (final Exception e)
			{
				System.err.println("ERROR reading directory from url " + remoteUrl + ": " + e);
				error = true;
				return !error;
			}
		}
		else
		{
			// file?!
			try
			{
				final File dir = new File(new URI(remoteUrl));
				if (!dir.isDirectory())
				{
					System.err.println("Download: " + dir + " is not a directory (URI was " + remoteUrl + ")");
					error = true;
					return !error;
				}
				final File files[] = dir.listFiles();
				for (final File file : files)
				{
					if (file.isDirectory())
					{
						System.out.println("Found remote subdirectory " + file.getName());
						fileNames.addElement(file.getName() + File.separator);
					}
					else
					{
						System.out.println("Found remote file " + file.getName());
						fileNames.addElement(file.getName());
					}
				}
			}
			catch (final Exception e)
			{
				System.err.println("ERROR checking contents of 'remote' directory " + localDir + ": " + e);
				error = true;
				return !error;
			}
		}
		try
		{
			final String localFiles[] = localDir.list();
			final Hashtable prune = new Hashtable();
			if (level >= 2)
			{
				// only prume in java/X/ subdirectories and below
				for (final String localFile : localFiles)
				{
					prune.put(localFile, localFile);
				}
			}
			final Enumeration fe = fileNames.elements();
			while (fe.hasMoreElements())
			{
				String name = (String) fe.nextElement();
				boolean dirFlag = false;
				if (name.endsWith("/") || name.endsWith("\\"))
				{
					dirFlag = true;
					name = name.substring(0, name.length() - 1);
				}

				if (dirFlag)
				{
					prune.remove(name);
					final File localSubDir = new File(localDir, name);
					if (!localSubDir.exists())
					{
						localSubDir.mkdirs();
						if (!localSubDir.exists())
						{
							System.err.println("Download: cannot make local cache directory " + localSubDir);
							error = true;
							continue;
						}
					}
					if (!updateDir(localSubDir, remoteUrl + name + "/", level + 1))
					{
						error = true;
					}
				}
				else
				{
					prune.remove(name);
					prune.remove(name + BACKUP_SUFFIX);
					prune.remove(name + TIMESTAMP_SUFFIX);

					try
					{
						if (!updateFile(localDir, remoteUrl, name))
						{
							error = true;
						}
					}
					catch (final Exception e)
					{
						System.err.println("ERROR updating file " + name + " from " + remoteUrl + ": " + e);
						error = true;
						// leave?!
					}
				}
			}
			// prune
			final java.util.Iterator pi = prune.values().iterator();
			while (pi.hasNext())
			{
				final String file = (String) pi.next();
				final File f = new File(localDir, file);
				if (f.delete())
				{
					System.out.println("Deleted old file " + f);
				}
				else
				{
					System.out.println("Unable to delete old file " + f);
				}
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR Downloading: " + e);
			e.printStackTrace(System.err);
			error = true;
		}
		return !error;
	}

	protected boolean updateFile(final File localDir, final String remoteDirUrl, final String filename)
			throws MalformedURLException, java.io.FileNotFoundException, java.net.URISyntaxException,
			IOException
	{
		boolean error = false;
		final File local = new File(localDir, filename);
		if (local.exists())
		{
			System.out.println("Local file " + filename + " exists: lastModified=" + local.lastModified() + " length="
					+ local.length());
		}
		else
		{
			System.out.println("Local file " + filename + " does not exist");
		}
		long localLastModified = 0;
		// not trusting local timestamps on files
		final File localTimestamp = new File(localDir, filename + TIMESTAMP_SUFFIX);
		if (local.exists() && localTimestamp.exists())
		{
			try
			{
				final BufferedReader in = new BufferedReader(new java.io.FileReader(localTimestamp));
				localLastModified = new Long(in.readLine()).longValue();
				in.close();
			}
			catch (final Exception e)
			{
				System.err.println("ERROR reading timestamp from file " + localTimestamp + ": " + e);
				e.printStackTrace(System.err);
			}
		}
		else if (local.exists())
		{
			localLastModified = local.lastModified();
		}

		InputStream remoteInputStream = null;
		int contentLength = 0;
		long lastModified = 0;
		if (remoteDirUrl.startsWith("http:"))
		{
			final URL remote = new URL(remoteDirUrl + filename);
			URLConnection remoteConn = null;

			// System.out.println("Check header of remote "+remote);
			remoteConn = remote.openConnection();
			remoteConn.setIfModifiedSince(localLastModified);
			remoteConn.connect();

			final String status = remoteConn.getHeaderField(null);

			// System.out.println("Status: "+status);
			// java.util.Map header = remoteConn.getHeaderFields();
			// java.util.Iterator hi = header.keySet().iterator();
			// while(hi.hasNext())
			// {
			// Object key = hi.next();
			// Object value = header.get(key);
			// System.out.println(key+" = "+value+" ("+value.getClass().getName()+")");
			// }
			contentLength = remoteConn.getHeaderFieldInt("Content-Length", -1);
			lastModified = remoteConn.getHeaderFieldDate("Last-Modified", 0);
			System.out.println("HTTP Length=" + contentLength + " lastModified=" + lastModified);

			if ((status != null && status.indexOf("304") >= 0)
					|| ((status == null || status.indexOf(" 2") >= 0) && local.exists()
							&& localLastModified >= lastModified && lastModified != 0 && (contentLength < 0 || local
							.length() == contentLength)))
			{
				System.out.println("Local file " + filename + " is up to date");
				try
				{
					remoteConn.getInputStream().close();
				}
				catch (final Exception ee)
				{
				}
				return !error;
			}
			if (status.indexOf(" 2") < 0)
			{
				System.out.println("Error checking remote " + remote + " (status " + status + ")");
				error = true;
				return !error;
			}
			remoteInputStream = remoteConn.getInputStream();
		}
		else
		{
			// local file
			final File remoteFile = new File(new File(new URI(remoteDirUrl)), filename);
			if (!remoteFile.exists() || !remoteFile.canRead())
			{
				System.err.println("Cannot read 'remote' file " + remoteFile);
				error = true;
				return !error;
			}
			lastModified = remoteFile.lastModified();
			contentLength = (int) remoteFile.length();
			System.out.println("File Length=" + contentLength + " lastModified=" + lastModified);
			if (local.length() == contentLength && localLastModified >= lastModified && lastModified != 0)
			{
				System.out.println("Local file " + filename + " is up to date");
				return !error;
			}
			remoteInputStream = new FileInputStream(remoteFile);
		}

		// copy to local file
		final File localTemp = new File(localDir, filename + TEMP_SUFFIX);
		if (localTemp.exists())
		{
			// delete old local temp file
			localTemp.delete();
		}
		if (!copyToLocalFile(remoteInputStream, localTemp, contentLength))
		{
			System.out.println("ERROR copying remote file to local temp file: " + remoteDirUrl + "/" + filename
					+ " -> " + localTemp);
			error = true;
			return !error;
		}
		// System.out.println("Local file "+localTemp+": lastModified="+localTemp.lastModified()+" length="+localTemp.length());
		if (contentLength >= 0 && localTemp.length() != contentLength)
		{
			System.out.println("Downloaded file length mismatch: " + localTemp.length() + " vs " + contentLength
					+ " - skipping file");
			error = true;
			return !error;
		}
		// ok - replace
		if (local.exists())
		{
			// move away
			final File backup = new File(localDir, filename + BACKUP_SUFFIX);
			if (backup.exists())
			{
				backup.delete();
			}
			if (!local.renameTo(backup))
			{
				System.err.println("WARNING: unable to rename local file " + local + " to " + backup);
			}
		}
		if (!localTemp.renameTo(local))
		{
			System.err.println("ERROR: unable to rename downloaded file " + localTemp + " to " + local);
			error = true;
			return !error;
		}
		if (lastModified > 0)
		{
			try
			{
				final java.io.FileWriter out = new java.io.FileWriter(localTimestamp);
				out.write(new Long(lastModified).toString() + "\n");
				out.close();
			}
			catch (final Exception e)
			{
				System.err.println("ERROR writing timestamp file " + localTimestamp + ": " + e);
				e.printStackTrace(System.err);
			}
		}
		System.out.println("Replaced " + local + " from " + remoteDirUrl);
		return !error;
	}

}
