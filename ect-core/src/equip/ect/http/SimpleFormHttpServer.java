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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * server which generates simple dynamic html forms over {@link SimpleFormModel}s, using
 * {@link SimpleFormProvider} interface to link to underlying logic/component
 */
public class SimpleFormHttpServer extends AbstractHttpServer implements SimpleFormProvider
{
	/**
	 * test main
	 */
	public static void main(final String[] args)
	{
		try
		{
			final SimpleFormModel model = new SimpleFormModel();
			model.addProperty("Text", "text property", "some text");
			model.addProperty("Bool", "boolean property", true);

			final SimpleFormHttpServer server = new SimpleFormHttpServer("Test server", model);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR in main: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * title
	 */
	protected String title;
	/**
	 * provider(if any)
	 */
	protected SimpleFormProvider provider;
	/**
	 * model (if any)
	 */
	protected SimpleFormModel model;

	/**
	 * cons
	 */
	public SimpleFormHttpServer(final String title, final SimpleFormModel model) throws IOException
	{
		super();
		this.title = title;
		this.model = model;
	}

	/**
	 * cons
	 */
	public SimpleFormHttpServer(final String title, final SimpleFormProvider provider) throws IOException
	{
		super();
		this.title = title;
		this.provider = provider;
	}

	/**
	 * get {@link SimpleFormModel} to show - return our internal one
	 */
	@Override
	public SimpleFormModel getModel()
	{
		return model;
	}

	/**
	 * set {@link SimpleFormModel} - just swallow it?
	 */
	@Override
	public SimpleFormModel setModel(final SimpleFormModel model)
	{
		this.model = model;
		return model;
	}

	// add implementation here... :-)
	/**
	 * http get - override
	 */
	@Override
	protected void getHttp(final InputStream in, final OutputStream out, final String path) throws IOException
	{
		writeHeader(out);

		boolean refresh = false;
		final OutputStreamWriter outs = new OutputStreamWriter(out);
		System.err.println("Get \"" + path + "\"");

		// set?
		if (model == null)
		{
			model = provider.getModel();
		}
		if (path.startsWith("/action?"))
		{
			// bool to false
			final Iterator i = model.nameIterator();
			while (i.hasNext())
			{
				final String name = (String) i.next();
				final Object value = model.getValue(name);
				if (value instanceof Boolean)
				{
					model.setValue(name, false);
				}
			}
			// split properties
			final StringTokenizer toks = new StringTokenizer(path.substring(8), "&");
			while (toks.hasMoreTokens())
			{
				final String t = toks.nextToken();
				final int pos = t.indexOf("=");
				if (pos < 0)
				{
					System.err.println("ERROR in token - no =: " + t);
					continue;
				}
				final String name = t.substring(0, pos);
				final String encval = t.substring(pos + 1);
				final String sval = URLDecoder.decode(encval, "UTF-8");
				System.out.println("Set " + name + "=" + sval);

				final Object value = model.getValue(name);
				if (value == null)
				{
					System.out.println("ERROR: unknown property " + name);
				}
				else
				{
					if (value instanceof Boolean)
					{
						model.setValue(name, true);
					}
					else
					{
						model.setValue(name, sval);
					}
				}
			}
			if (provider != null)
			{
				model = provider.setModel(model);
			}
			refresh = true;
		}
		else if (provider != null)
		{
			model = provider.getModel();
		}

		// output
		outs.write("<html><head>\n");
		if (refresh)
		{
			outs.write("<meta HTTP-EQUIV=\"REFRESH\" CONTENT=\"0; url=" + getBaseURL() + "\">\n");
		}
		outs.write("<title>" + title + "</title></head><body>\n<H1>" + title + "</H1>\n");
		outs.write("<p><a href=\"" + getBaseURL() + "\">Refresh</a></p>\n");
		outs.write("<FORM ACTION=\"" + getBaseURL() + "action\" METHOD=GET>\n");

		// print
		try
		{
			final Iterator i = model.nameIterator();
			while (i.hasNext())
			{
				final String name = (String) i.next();
				final Object value = model.getValue(name);

				outs.write("<p>" + name + " (" + model.getDescription(name) + "): ");
				if (value instanceof Boolean)
				{
					outs.write("<INPUT TYPE=\"checkbox\" NAME=\"" + name + "\" VALUE=\"true\" "
							+ (((Boolean) value).booleanValue() ? "CHECKED" : "") + ">");
				}
				else
				{
					outs.write("<INPUT TYPE=\"text\" NAME=\"" + name + "\" VALUE=\"" + value.toString() + "\">");
				}
				outs.write("</p>\n");
			}
			outs.write("<INPUT TYPE=\"submit\"> <INPUT TYPE=reset>\n");

			outs.write("</form></body></html>");
			outs.flush();
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}
}
