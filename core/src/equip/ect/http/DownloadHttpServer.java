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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * simple embedded web server tailored to allow Installation clients to download from the master.
 * chris greenhalgh 2005-04-19
 */
public class DownloadHttpServer extends SimpleHttpServer
{

	public static void main(final String[] args)
	{
		try
		{
			if (args.length != 2)
			{
				System.err.println("Usage: DownloadHttpServer port path");
				System.exit(-1);
			}
			final int port = Integer.parseInt(args[0]);
			final String dir = args[1];
			new DownloadHttpServer(port, dir);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR (DownloadHttpServer.main): " + e);
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}

	/**
	 * cons
	 */
	public DownloadHttpServer(final int port, final String dir) throws IOException
	{
		super(port, dir);
	}

	/**
	 * http get - override
	 */
	@Override
	protected void getHttp(final InputStream in, final OutputStream out, final String path, final Hashtable headers,
			final boolean head) throws IOException
	{
		if (path.startsWith("/download/"))
		{
			// download-specific - skip /download

			File file = new File(dir + path.substring(9));
			file = file.getCanonicalFile();
			if (!file.getPath().substring(0, dir.length()).equals(dir))
			{
				System.err.println("- Returned file out of allowed scope: " + file);
				writeFileNotFoundHeader(out);
				return;
			}

			if (!file.canRead())
			{
				System.err.println("- Could not read file: " + file);
				writeFileNotFoundHeader(out);
				return;
			}

			if (file.isDirectory())
			{
				// tailor
				writeHeader(out, "text/html");
				if (head) { return; }
				final PrintWriter pout = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
				pout.println("<html><head><title>Directory " + path + "</title></head><body>\n");
				pout.println("<h1>Directory " + path + "</h1>\n");
				final File[] files = file.listFiles();
				for (int fi = 0; fi < files.length; fi++)
				{
					// limit higher-level returns
					if ((path.equals("/download/") && !files[fi].getName().equals("java"))
							|| (path.equals("/download/java/") && !files[fi].getName().equals("common")
									&& !files[fi].getName().equals("config") && !files[fi].getName().equals("tools") && !files[fi]
									.getName().equals("components")) || files[fi].getName().endsWith(".timestamp")
							|| files[fi].getName().endsWith(".downloading") || files[fi].getName().endsWith(".backup"))
					{
						// ignore/suppress
						System.out.println("Suppress local file/dir " + files[fi]);
					}
					else
					{
						pout.println("<p><a href=\"" + files[fi].getName() + (files[fi].isDirectory() ? "/" : "")
								+ "\">" + files[fi].getName() + (files[fi].isDirectory() ? "/" : "") + "</a></p>\n");
					}
				}
				pout.println("</body></html>\n");
				pout.flush();
				return;
			}
			super.getHttp(in, out, path.substring(9), headers, head);
		}
		else
		{
			super.getHttp(in, out, path, headers, head);
		}
	}
}
