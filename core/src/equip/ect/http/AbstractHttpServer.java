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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

/**
 * simple embedded web server abstract base class. Override get method. chris greenhalgh 7 april
 * 2001
 */
public class AbstractHttpServer
{
	/**
	 * test main
	 */
	static public void main(final String[] args)
	{
		try
		{
			if (args.length == 0)
			{
				new AbstractHttpServer();
			}
			else
			{
				final int port = new Integer(args[0]).intValue();
				new AbstractHttpServer(port);
			}
		}
		catch (final Exception e)
		{
			System.err.println("Error: " + e);
		}
	}

	/**
	 * socket
	 */
	protected ServerSocket socket;
	/**
	 * port
	 */
	protected int port;
	/**
	 * terminate
	 */
	protected boolean terminateFlag = false;

	/**
	 * cons - random port
	 */
	public AbstractHttpServer() throws IOException
	{
		init(0);
	}

	/**
	 * cons specific port
	 */
	public AbstractHttpServer(final int port) throws IOException
	{
		init(port);
	}

	/**
	 * get base URL
	 */
	public String getBaseURL()
	{
		try
		{
			return "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + socket.getLocalPort() + "/";
		}
		catch (final java.net.UnknownHostException e)
		{
			System.err.println("ERROR: getBaseURL: " + e);
		}
		return "http://localhost:" + socket.getLocalPort() + "/";
	}

	/**
	 * get port
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * terminate
	 */
	public void terminate()
	{
		synchronized (this)
		{
			if (terminateFlag) { return; }
			terminateFlag = true;
			try
			{
				socket.close();
			}
			catch (final Exception e)
			{
				System.err.println("Error terminating AbstractHttpServer: " + e);
			}
		}
	}

	/**
	 * return an input stream that will stop at the end of the header
	 */
	protected InputStream getHeaderInputStream(final InputStream in) throws IOException
	{
		byte buf[] = new byte[10000];
		int size = 0;
		do
		{
			final int b = in.read();
			if (b < 0)
			{
				// EOF
				break;
			}
			if (size == buf.length)
			{
				final byte newbuf[] = new byte[2 * buf.length];
				System.arraycopy(buf, 0, newbuf, 0, buf.length);
				buf = newbuf;
			}
			buf[size++] = (byte) b;
		}
		while ((size < 2 || buf[size - 1] != '\n' || buf[size - 2] != '\n')
				&& (size < 4 || buf[size - 1] != '\n' || buf[size - 2] != '\r' || buf[size - 3] != '\n' || buf[size - 4] != '\r'));
		return new ByteArrayInputStream(buf, 0, size);
	}

	/**
	 * http get - override
	 */
	protected void getHttp(final InputStream in, final OutputStream out, final String path) throws IOException
	{
		writeHeader(out);

		final OutputStreamWriter outs = new OutputStreamWriter(out);
		System.err.println("Get \"" + path + "\"");
		outs.write("<html><head><title>title</title></head><body>\n<H1>Get " + path + "</H1>\n");
		outs.write("<FORM ACTION=\"" + getBaseURL() + "action\" METHOD=GET>\n");
		outs.write("<p>A parameter: <INPUT TYPE=\"text\" NAME=\"param\" VALUE=\"a value\">\n");
		outs.write("<INPUT TYPE=\"submit\"> <INPUT TYPE=reset>\n");
		outs.write("</form></body></html>");
		outs.flush();
		// ... override ...
	}

	/**
	 * http get - override
	 */
	protected void getHttp(final InputStream in, final OutputStream out, final String path, final Hashtable headers)
			throws IOException
	{
		getHttp(in, out, path);
	}

	/**
	 * http get - override
	 */
	protected void getHttp(final InputStream in, final OutputStream out, final String path, final Hashtable headers,
			final boolean head) throws IOException
	{
		getHttp(in, out, path, headers);
	}

	/**
	 * client handler
	 */
	protected void handleClient(final Socket client)
	{
		final int total = 0;
		Reader in = null;

		try
		{
			System.err.println("New connection from " + client.getInetAddress().getHostAddress() + ":"
					+ client.getPort() + " (" + client.getInetAddress().getHostName() + ")");
			final InputStream ins = client.getInputStream();

			in = new InputStreamReader(getHeaderInputStream(ins));

			String firstLine = null, line;

			final Hashtable headers = new Hashtable();
			do
			{
				line = readLine(in);
				// System.out.println("Request Header: "+line);
				if (firstLine == null)
				{
					firstLine = line;
				}
				if (line == null || line.length() == 0)
				{
					break;
				}
				final int ix = line.indexOf(":");
				if (ix < 0)
				{
					headers.put(line, "");
				}
				else
				{
					headers.put(line.substring(0, ix), line.substring(ix + 1));
				}

			}
			while (true);
			if (firstLine == null)
			{
				System.err.println("- no input read");
				return;
			}
			System.err.println("- request: " + firstLine);
			if (firstLine.substring(0, 4).equals("GET "))
			{
				String path;
				path = firstLine.substring(4);
				final int end = path.indexOf(' ');
				if (end >= 0)
				{
					path = path.substring(0, end);
				}

				getHttp(client.getInputStream(), client.getOutputStream(), path, headers, false);
			}
			else if (firstLine.substring(0, 5).equals("HEAD "))
			{
				String path;
				path = firstLine.substring(5);
				final int end = path.indexOf(' ');
				if (end >= 0)
				{
					path = path.substring(0, end);
				}

				getHttp(client.getInputStream(), client.getOutputStream(), path, headers, true);
			}
			else if (firstLine.substring(0, 5).equals("POST "))
			{
				String path;
				path = firstLine.substring(5);
				final int end = path.indexOf(' ');
				if (end >= 0)
				{
					path = path.substring(0, end);
				}

				postHttp(client.getInputStream(), client.getOutputStream(), path, headers);
			}
			else
			{

				System.err.println("- can only deal with GETs, HEADs and POSTS");
				writeBadRequestHeader(client.getOutputStream());
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR handling client: " + e);
			e.printStackTrace(System.err);
			try
			{
				writeErrorHeader(client.getOutputStream(), 500, "Internal Server Error (" + e + ")");
			}
			catch (final Exception ee)
			{
			}
		}
		finally
		{
			try
			{
				in.close();
				client.close();
			}
			catch (final Exception e)
			{
			}
		}
	}

	/**
	 * init, port 0 for any
	 */
	protected void init(final int port) throws IOException
	{
		socket = new ServerSocket(port);
		this.port = socket.getLocalPort();
		System.out.println("Run AbstractHttpServer on port " + this.port);
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					while (!terminateFlag)
					{
						final Socket client = socket.accept();
						new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								handleClient(client);
							}
						}).start();
					}
				}
				catch (final Exception e)
				{
					System.err.println("AbstractHttpServer port " + port + "/" + socket.getLocalPort()
							+ " terminated: " + e);
				}
				try
				{
					socket.close();
				}
				catch (final Exception e)
				{
				}
			}
		}).start();
	}

	/**
	 * http post - override
	 */
	protected void postHttp(final InputStream in, final OutputStream out, final String path, final Hashtable headers)
			throws IOException
	{
		writeBadRequestHeader(out);
	}

	/**
	 * date to HTTP format
	 */
	protected String printDate(final long date)
	{
		// e.g.Sun, 06 Nov 1994 08:49:37 GMT
		final java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
		return df.format(new java.util.Date(date)) + " GMT";
	}

	protected String readLine(final Reader in) throws IOException
	{
		final StringBuffer b = new StringBuffer();
		while (true)
		{
			int c = in.read();
			if (c < 0)
			{
				break;
			}
			if (c == '\r')
			{
				// assume CR/LF
				c = in.read();
			}
			if (c == '\n')
			{
				break;// drop char
			}
			b.append((char) c);
		}
		return b.toString();
	}

	/**
	 * write header OK
	 */
	protected void writeBadRequestHeader(final OutputStream outs) throws IOException
	{
		writeErrorHeader(outs, 400, "Bad Request");
	}

	/**
	 * write header
	 */
	protected void writeEmptyHeader(final OutputStream outs, final int errno, final String errmsg) throws IOException
	{
		// reply header
		final OutputStreamWriter out = new OutputStreamWriter(outs);
		out.write("HTTP/1.0 " + errno + " " + errmsg + "\n");
		out.write("\n");
		out.flush();
	}

	/**
	 * write header
	 */
	protected void writeErrorHeader(final OutputStream outs, final int errno, final String errmsg) throws IOException
	{
		System.out.println("Returned error " + errno + " " + errmsg);
		// reply header
		final OutputStreamWriter out = new OutputStreamWriter(outs);
		out.write("HTTP/1.0 " + errno + " " + errmsg + "\n");
		out.write("\n");
		out.flush();
		writeSimplePage(outs, "ERROR " + errno + " " + errmsg);
	}

	/**
	 * write header OK
	 */
	protected void writeFileNotFoundHeader(final OutputStream outs) throws IOException
	{
		writeErrorHeader(outs, 404, "File Not Found");
	}

	/**
	 * write header OK
	 */
	protected void writeForbiddenHeader(final OutputStream outs) throws IOException
	{
		writeErrorHeader(outs, 402, "Forbidden");
	}

	/**
	 * write header OK with text/html mime type
	 */
	protected void writeHeader(final OutputStream outs) throws IOException
	{
		writeHeader(outs, "text/html");
	}

	/**
	 * write header OK
	 */
	protected void writeHeader(final OutputStream outs, final String mimeType) throws IOException
	{
		// reply header
		final OutputStreamWriter out = new OutputStreamWriter(outs);
		out.write("HTTP/1.0 200 OK\n");
		out.write("Content-Type: " + (mimeType == null ? "text/html" : mimeType) + "\n");
		out.write("\n");
		out.flush();
	}

	/**
	 * write header OK
	 */
	protected void writeHeader(final OutputStream outs, final String mimeType, final long length, final long modified)
			throws IOException
	{
		// reply header
		final OutputStreamWriter out = new OutputStreamWriter(outs);
		out.write("HTTP/1.0 200 OK\r\n");
		out.write("Content-Type: " + (mimeType == null ? "text/html" : mimeType) + "\r\n");
		if (length != 0)
		{
			out.write("Content-Length: " + length + "\r\n");
		}
		if (modified != 0)
		{
			out.write("Last-Modified: " + printDate(modified) + "\r\n");
		}
		out.write("\r\n");
		out.flush();
	}

	/**
	 * write header
	 */
	protected void writeSimplePage(final OutputStream outs, final String msg) throws IOException
	{
		final OutputStreamWriter out = new OutputStreamWriter(outs);
		out.write("<html><head><title>" + msg + "</title></head><body>" + msg + "</body></html>\n");
		out.flush();
	}
}
