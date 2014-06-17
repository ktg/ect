/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Southampton
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Southampton
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

Created by: Mark Thompson (University of Southampton)
Contributors:
  Mark Thompson (University of Southampton)
  Chris Greenhalgh (University of Nottingham)
  Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect.components.atomblogger;

// vim: expandtab sw=4 ts=4 sts=4:

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * @author Mark Thompson &lt;mkt@ecs.soton.ac.uk&gt;
 */
public class AtomBloggerLogic
{

	/** Logging through good-ole log4j */
	private static Logger logger = Logger.getLogger(AtomBloggerLogic.class);

	void doPost(final URL endpointURL, final String userName, final String password, final String topic,
			final String content) throws IOException, UploadException, Exception
	{

		final String protocol = endpointURL.getProtocol();

		System.out.println("got protocol: " + protocol);

		if (protocol.equals("https") || protocol.equals("HTTPS"))
		{

			// the default authenticator that we provide will
			// deal with requests for usernames, passwords

			// note though - only one static useranme password
			// per jvm - so could be an issue if multiple blogging
			// compoents are used to connect to multiple blogs

			Authenticator.setDefault(new Authenticator()
			{
				@Override
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(userName, password.toCharArray());
				}
			});
		}

		final HttpURLConnection connection = (HttpURLConnection) endpointURL.openConnection();
		connection.setRequestMethod("POST");
		connection.addRequestProperty("X-WSSE", WSSE.getWSSEHeader(userName, password));
		connection.setUseCaches(false);
		connection.setDoOutput(true);
		connection.setDoInput(true);

		// REQUEST
		final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(512);
		// Beelzebub has a devil put aside for me... (no, this isn't how one is supposed to
		// serialise XML ;-))
		final PrintWriter out = new PrintWriter(byteStream, false);
		out.println("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>");
		out.println("<?xml-stylesheet href=\"http://www.blogger.com/styles/atom.css\" type=\"text/css\"?>");
		out.println("<entry version='0.3' xmlns='http://purl.org/atom/ns#'>");
		out.println("<issued>" + WSSE.generateTimestamp() + "</issued>");
		out.println("<generator url='http://www.equator.ac.uk/' version='0.01'>Stripes-script</generator>");
		out.println("<title mode='escaped'>" + topic + "</title>");
		out.println("<content mode='escaped'><![CDATA[" + content + "]]></content>");
		out.println("</entry>");
		out.flush();
		connection.setRequestProperty("Content-Length", String.valueOf(byteStream.size()));
		connection.setRequestProperty("Content-Type", "application/x.atom+xml");
		byteStream.writeTo(connection.getOutputStream());

		final int responseCode = connection.getResponseCode();
		final String responseMessage = connection.getResponseMessage();

		System.out.println("Got response code: " + responseCode);
		System.out.println("Got response message: " + responseMessage);

		// http response codes starting with a 2 (eg 200,201 etc)
		// generally indicate success

		if ((responseCode < 200) && (responseCode >= 300)) { throw (new UploadException(responseCode, responseMessage)); }
	}
}
