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

Created by: Tom Hart (University of Nottingham)
Contributors:
  Tom Hart (University of Nottingham)
  Chris Greenhalgh (University of Nottingham)

 */
/*
 * XmlServer, $RCSfile: XmlServer.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:27 $
 *
 * $Author: chaoticgalen $
 * Original Author: Tom Hart
 */
package equip.ect.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * simple server which extends AbstractHttpServer adding http headers for xml content type and file
 * length
 */
public class XmlServer extends AbstractHttpServer
{

	private String content;

	public XmlServer(final String data, final int port) throws IOException
	{
		super(port);
		this.content = data;
	}

	public String getContent()
	{
		return this.content;
	}

	public void setContent(final String content)
	{
		this.content = content;
	}

	@Override
	protected void getHttp(final InputStream in, final OutputStream out, final String path) throws IOException
	{
		writeHeader(out, "text/xml", getContent().getBytes().length);
		final OutputStreamWriter outs = new OutputStreamWriter(out);
		System.err.println("Get \"" + path + "\"");
		outs.write(getContent());
		outs.flush();
		outs.close();
		out.close();
		// ... override ...
	}

	protected void writeHeader(final OutputStream outs, final String mimeType, final int dataLength) throws IOException
	{
		final OutputStreamWriter out = new OutputStreamWriter(outs);
		out.write("HTTP/1.0 200 OK\r\n");
		out.write("Content-type: " + mimeType + "\r\n");
		out.write("Content-Length: " + dataLength + "\r\n");
		out.write("\r\n");
		out.flush();
	}

}
