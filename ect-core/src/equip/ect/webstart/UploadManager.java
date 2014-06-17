/*
<COPYRIGHT>

Copyright (c) 2005, University of Nottingham
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

 */
package equip.ect.webstart;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * upload local log & persistence files to UploadHttpServer using POSTs. Chris Greenhalgh 2005-04-20
 */
public class UploadManager
{
	static boolean debug = false;

	/**
	 * test main
	 */
	public static void main(final String[] args)
	{
		if (args.length != 3)
		{
			System.err.println("Usage: java " + UploadManager.class.getName()
					+ " localDir uploadUrl c(urrent)|o(old)|b(oth)");
			System.exit(-1);
		}
		try
		{
			final File localRoot = new File(args[0]);
			final URL uploadRoot = new URL(args[1]);
			final boolean updateCurrentLogs = args[2].startsWith("c") || args[2].startsWith("b");
			final boolean migrateOldLogs = args[2].startsWith("o") || args[2].startsWith("b");
			final UploadManager uploader = new UploadManager();
			UploadManager.upload(localRoot, uploadRoot, updateCurrentLogs, migrateOldLogs);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * do an upload, return OK
	 */
	public static boolean upload(final File localRoot, final URL uploadRoot, final boolean updateCurrentLogs,
			final boolean migrateOldLogs)
	{
		boolean error = false;
		try
		{
			// recursive
			final File files[] = localRoot.listFiles();
			for (int fi = 0; fi < files.length; fi++)
			{
				if (files[fi].isDirectory())
				{
					if (files[fi].getName().equals("history"))
					{
						System.err.println("Don't upload the history/ subdirectory!");
						continue;
					}
					if (!upload(files[fi], new URL(uploadRoot, files[fi].getName() + "/"), updateCurrentLogs,
								migrateOldLogs))
					{
						error = true;
					}
				}
				else
				{
					String name = files[fi].getName();
					final int ei = name.lastIndexOf(".");
					final String extension = (ei < 0) ? "" : name.substring(ei);
					if (ei >= 0)
					{
						name = name.substring(0, ei);
					}
					boolean isCurrentLog = false;
					boolean isOldLog = false;

					// System.out.println("Consider file "+name+" extension "+extension);

					int numdigits = 0;
					for (numdigits = 0; name.length() - 1 - numdigits >= 0
							&& Character.isDigit(name.charAt(name.length() - 1 - numdigits)); numdigits++)
					{
					}
					final boolean hasTimestamp = (numdigits >= 10);
					long timestamp = 0;
					if (hasTimestamp)
					{
						try
						{
							timestamp = new Long(name.substring(name.length() - numdigits)).longValue();
							// System.out.println("File "+files[fi].getName()+" has timestamp "+timestamp+" ("+numdigits+" digits)");
						}
						catch (final NumberFormatException e)
						{
							System.err.println("ERROR: parsing presumed timestamp of " + numdigits + " at end of "
									+ name);
						}
					}

					if (files[fi].getName().equals("persist.xml"))
					{
						System.out.println("Found current persist.xml file: " + files[fi]);
						isCurrentLog = true;
					}
					else if (extension.equals(".txt") || extension.equals(".eqbser"))
					{
						// probably a logfile or event log
						if (!hasTimestamp)
						{
							System.out.println("Warning: found logfile without timestamp: " + files[fi]);
							isCurrentLog = true; // ?!
						}
						else
						{
							// newest of this kind??
							boolean newest = true;
							for (int f2 = 0; newest && f2 < files.length; f2++)
							{
								final String name2 = files[f2].getName();
								// System.out.println("Check newest against "+name2+" (extension? "+
								// (name2.endsWith(extension) ? "matches" : "differs")+
								// "prefix "+(name2.startsWith(name.substring(0,
								// name.length()-numdigits)) ? "matches" : "differs"));
								if (name2.endsWith(extension)
										&& name2.startsWith(name.substring(0, name.length() - numdigits)))
								{
									long timestamp2 = 0;
									try
									{
										timestamp2 = new Long(name2.substring(name.length() - numdigits, name2.length()
												- extension.length())).longValue();
										// System.out.println("Alt File "+name2+" has timestamp "+timestamp2+" ("+
										// (timestamp2 > timestamp ? "newer" : "older"));
										if (timestamp2 > timestamp)
										{
											newest = false;
										}
									}
									catch (final NumberFormatException e)
									{
										System.err.println("ERROR: parsing presumed timestamp of " + numdigits + " in "
												+ name2);
									}
								}
							}
							isCurrentLog = newest;
							isOldLog = !newest;
						}
						System.out.println("Found " + (isCurrentLog ? "current" : "old") + " log file: " + files[fi]);
					}
					if ((isCurrentLog && updateCurrentLogs) || (isOldLog && migrateOldLogs))
					{
						String uploadName = files[fi].getName();
						if (!hasTimestamp)
						{
							long mod = files[fi].lastModified();
							if (mod == 0)
							{
								System.err.println("Warning: unable to read last modified time of " + files[fi]
										+ "; using clock");
								mod = System.currentTimeMillis();
							}
							uploadName = name + "-" + mod + extension;
							System.out.println("Adding timestamp to " + files[fi].getName() + " -> " + uploadName);
						}
						if (!uploadFile(files[fi], new URL(uploadRoot, uploadName), isOldLog && migrateOldLogs))
						{
							error = true;
						}
					}
				}
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR doing upload of " + localRoot + " to " + uploadRoot + ": " + e);
			e.printStackTrace(System.err);
			error = true;
		}
		return !error;
	}

	/**
	 * upload file, true = ok
	 */
	protected static boolean uploadFile(final File localFile, final URL remoteUrl, final boolean deleteOnSuccess)
	{
		try
		{
			if (!localFile.exists() || !localFile.canRead())
			{
				System.err.println("Cannot find/read local upload file " + localFile);
				return false;
			}
			final long localLength = localFile.length();
			final long localModified = localFile.lastModified();

			// check what if anything is there already
			URLConnection connection = remoteUrl.openConnection();
			if (connection instanceof HttpURLConnection)
			{
				((HttpURLConnection) connection).setRequestMethod("HEAD");
			}
			// connection.setDoInput(true); // head?
			// connection.setDoOutput(true); // post?
			connection.connect();
			final String status = connection.getHeaderField(null);
			long remoteLength = connection.getHeaderFieldInt("Content-Length", -1);
			final long remoteModified = connection.getHeaderFieldDate("Last-Modified", 0);
			System.out.println("Check " + remoteUrl);
			System.out.println("Status: " + status);
			System.out.println("HTTP Length=" + remoteLength + " lastModified=" + remoteModified);
			try
			{
				connection.getOutputStream().close();
			}
			catch (final Exception ee)
			{
			}
			try
			{
				connection.getInputStream().close();
			}
			catch (final Exception ee)
			{
			}

			if (localLength < remoteLength)
			{
				System.err.println("ERROR: remote file already longer than local file " + localFile + " ("
						+ localLength + " vs " + remoteLength + ")");
				return false;
			}
			else if (remoteLength < 0)
			{
				remoteLength = 0;
				if (status != null && status.indexOf(" 2") >= 0)
				{
					System.out.println("Warning: upload server did not return length for file " + remoteUrl);
					return false;
				}
			}

			boolean error = false;
			if (localLength == remoteLength)
			{
				System.out.println("Local file " + localFile + " already uploaded as " + remoteUrl + " (" + localLength
						+ " bytes)");
			}
			else
			{
				// upload
				final FileInputStream fin = new FileInputStream(localFile);
				final long skipped = remoteLength > 0 ? fin.skip(remoteLength) : 0;
				if (skipped != remoteLength)
				{
					System.err.println("ERROR: Skipping to " + remoteLength + " of file " + localFile
							+ " skipped only " + skipped);
					return false;
				}

				// append by post
				connection = remoteUrl.openConnection();
				if (connection instanceof HttpURLConnection)
				{
					((HttpURLConnection) connection).setRequestMethod("POST");
				}
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.connect();

				final OutputStream rout = connection.getOutputStream();

				final byte data[] = new byte[4096];
				long at = remoteLength;
				while (at < localLength)
				{
					int size = data.length;
					if (localLength - at < size)
					{
						size = (int) (localLength - at);
					}
					final int lcnt = fin.read(data, 0, size);
					if (lcnt <= 0)
					{
						System.err.println("ERROR uploading data of file " + localFile + " (wanted " + size + ", read "
								+ lcnt + ")");
						error = true;
						break;
					}
					if (lcnt > 0)
					{
						// System.out.println("Write "+lcnt+" bytes");
						// System.out.println("Bytes "+data[0]+","+data[1]+","+data[2]+","+data[3]+",...");
						rout.write(data, 0, lcnt);
					}
					at += lcnt;
				}
				// System.out.println("Flush");
				rout.flush();
				// System.out.println("Close");
				rout.close();
				// System.out.println("Close input");
				connection.getInputStream().close();
				System.out.println("Uploaded " + (at - remoteLength) + " bytes of local file " + localFile);
				fin.close();
			}
			if (!error && deleteOnSuccess)
			{
				System.out.println("Delete (old) local file " + localFile);
				if (!localFile.delete())
				{
					System.out.println("ERROR: unable to delete local file " + localFile);
				}
			}
			return !error;
		}
		catch (final Exception e)
		{
			System.err.println("ERROR uploading file " + localFile + " to " + remoteUrl + ": " + e);
			e.printStackTrace(System.err);
			return false;
		}
	}

	/**
	 * cons
	 */
	public UploadManager()
	{
	}
}
