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

 */
package equip.ect.http;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * simple embedded web server abstract base class. chris greenhalgh 7 april 2001
 */
public class SimpleHttpServer extends AbstractHttpServer
{

	public static void main(final String[] args)
	{
		try
		{
			if (args.length != 2)
			{
				System.err.println("Usage: SimpleHttpServer port path");
				System.exit(-1);
			}
			final int port = Integer.parseInt(args[0]);
			final String dir = args[1];
			new SimpleHttpServer(port, dir);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR (SimpleHttpServer.main): " + e);
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}

	/**
	 * dir
	 */
	protected String dir;

	/**
	 * cons
	 */
	public SimpleHttpServer(final int port, final String dir) throws IOException
	{
		super(port);
		this.dir = new File(dir).getCanonicalPath();
	}

	/**
	 * http get - override
	 */
	@Override
	protected void getHttp(final InputStream in, final OutputStream out, final String path,
			final java.util.Hashtable headers, final boolean head) throws IOException
	{
		System.gc();

		File file = new File(dir + path);
		file = file.getCanonicalFile();

		System.out.println((head ? "Head" : "Get") + " " + path + " ("
				+ (file.isDirectory() ? "directory" : file.exists() ? "file" : "unknown"));

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
			writeHeader(out, "text/html");
			if (head) { return; }
			final PrintWriter pout = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
			pout.println("<html><head><title>Directory " + path + "</title></head><body>\n");
			pout.println("<h1>Directory " + path + "</h1>\n");
			final File[] files = file.listFiles();
			for (final File file2 : files)
			{
				pout.println("<p><a href=\"" + file2.getName() + (file2.isDirectory() ? "/" : "") + "\">"
						+ file2.getName() + (file2.isDirectory() ? "/" : "") + "</a></p>\n");
			}
			pout.println("</body></html>\n");
			pout.flush();
			return;
		}

		String mimeType = "application/binary";
		if (path.endsWith(".txt") || path.endsWith(".log"))
		{
			mimeType = "text/plain";
		}
		if (path.endsWith(".html") || path.endsWith(".htm"))
		{
			mimeType = "text/html";
		}
		if (path.endsWith(".xml"))
		{
			mimeType = "text/xml";
		}

		writeHeader(out, mimeType, file.length(), file.lastModified());
		if (head) { return; }
		System.err.println("- Returning file: " + file);

		final OutputStream rawout = out;
		final FileInputStream rawfile = new FileInputStream(file);
		int num, cnt;
		final byte[] data = new byte[4096];
		long todo = file.length();
		while (todo > 0)
		{
			num = (int) todo;
			if (num > data.length)
			{
				num = data.length;
			}
			cnt = rawfile.read(data);
			if (cnt > 0)
			{
				rawout.write(data, 0, cnt);
				todo = todo - cnt;
			}
			else
			{
				System.err.println("ERROR: short file: " + file);
				break;
			}
		}
		rawfile.close();

		System.err.println("- done");
	}
}
