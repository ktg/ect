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
package equip.ect.http;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * simple embedded web server which appends POSTed data in history subhierarchy to files there. For
 * installation clients to upload logs to the master. chris greenhalgh 2005-04-19
 */
public class UploadHttpServer extends DownloadHttpServer
{

	public static void main(final String[] args)
	{
		try
		{
			if (args.length != 2)
			{
				System.err.println("Usage: UploadHttpServer port path");
				System.exit(-1);
			}
			final int port = Integer.parseInt(args[0]);
			final String dir = args[1];
			new UploadHttpServer(port, dir);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR (UploadHttpServer.main): " + e);
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}

	/**
	 * cons
	 */
	public UploadHttpServer(final int port, final String dir) throws IOException
	{
		super(port, dir);
	}

	/**
	 * http post - override
	 */
	@Override
	protected void postHttp(final InputStream in, final OutputStream out, final String path, final Hashtable headers)
			throws IOException
	{
		if (path.startsWith("/history/"))
		{
			// download-specific - skip /download

			File file = new File(dir + path);
			file = file.getCanonicalFile();
			if (!file.getPath().substring(0, dir.length()).equals(dir))
			{
				System.err.println("- post file out of allowed scope: " + file);
				writeForbiddenHeader(out);
				return;
			}

			// ensure directory exists
			if (path.endsWith("/"))
			{
				System.err.println("- post files, not directories: " + file);
				writeForbiddenHeader(out);
				return;
			}

			final File parent = file.getParentFile();
			if (!parent.isDirectory())
			{
				if (!parent.mkdirs())
				{
					System.err.println("- post unable to create dir " + parent);
					writeForbiddenHeader(out);
					return;
				}
			}

			try
			{
				final boolean created = !file.exists();
				// append!
				final String slen = ((String) headers.get("Content-Length")).trim();
				int length = -1;
				if (slen != null)
				{
					try
					{
						length = new Integer(slen).intValue();
					}
					catch (final NumberFormatException e)
					{
						System.err.println("- post error in Content-Length format: " + slen + ": " + e);
						writeBadRequestHeader(out);
						return;
					}
				}

				final BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(file, true));
				final byte data[] = new byte[4096];
				int got = 0;
				while (length < 0 || got < length)
				{
					int want = data.length;
					if (length >= 0 && length - got < want)
					{
						want = length - got;
					}
					// System.out.println("Read "+want+" bytes...");
					final int cnt = in.read(data, 0, want);
					if (cnt > 0)
					{
						// System.out.println("Bytes "+data[0]+","+data[1]+","+data[2]+","+data[3]+",...");
						bout.write(data, 0, cnt);
						got += cnt;
					}
					else
					{
						break;
					}
				}
				bout.flush();
				bout.close();

				if (length < 0 || got == length)
				{
					System.out.println("Uploaded " + file + " OK - " + got + " bytes");
					// not really an error
					writeHeader(out);
					writeSimplePage(out, "Uploaded " + file + " OK - " + (created ? "created, " : "") + "got " + got
							+ " bytes, now " + file.length() + " bytes");
				}
				else
				{
					writeErrorHeader(out, 400, "Bad Request (not enough data)");
				}
			}
			catch (final Exception e)
			{
				System.err.println("ERROR handling post: " + e);
				e.printStackTrace(System.err);
				writeErrorHeader(out, 500, "Server error (" + e + ")");
			}
		}
		else
		{
			writeForbiddenHeader(out);
		}
	}
}
