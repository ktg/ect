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

 */
package equip.ect.components.atomblogger;

// vim: expandtab sw=4 ts=4 sts=4:

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Utility class to generate WSSE header for Atom API
 * 
 */
public class WSSE
{

	// nonces are random by their very nature
	private static Random _r;

	/**
	 * @param password
	 *            Seed for random object
	 * @return byte array containing a nonce
	 */
	public static byte[] generateNonce(final String password)
	{
		if (_r == null)
		{
			_r = new Random(password.hashCode() + new Date().getTime());
		}
		try
		{
			final String nonce = Long.toString(_r.nextLong());
			return nonce.getBytes();
		}
		catch (final Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return current date and time in required format (W3DTF)
	 */
	public static String generateTimestamp()
	{
		final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		return dateFormatter.format(new Date());
	}

	/**
	 * Generate WSSE header attributes for authenticating user
	 * 
	 * @param username
	 * @param password
	 * @return correctly formatted WSSE header
	 */
	public static String getWSSEHeader(final String username, final String password) throws Exception
	{

		final byte[] nonce = generateNonce(password);
		final String created = generateTimestamp();
		final String password64 = getBase64Digest(nonce, created.getBytes("UTF-8"), password.getBytes("UTF-8"));

		final StringBuffer header = new StringBuffer("UsernameToken Username=\"");
		header.append(username);
		header.append("\", ");
		header.append("PasswordDigest=\"");
		header.append(password64);
		header.append("\", ");
		header.append("Nonce=\"");
		header.append(new sun.misc.BASE64Encoder().encode(nonce));
		header.append("\", ");
		header.append("Created=\"");
		header.append(created);
		header.append("\"");

		return header.toString();
	}

	/**
	 * @param nonce
	 *            the nonce
	 * @param created
	 *            W3DTF timestamp
	 * @param password
	 *            password being authenticated
	 * @return Base64-encoded passwordDigest attribute to be used in WSSE header
	 */
	private static synchronized String getBase64Digest(final byte[] nonce, final byte[] created, final byte[] password)
	{
		try
		{
			final MessageDigest messageDigester = MessageDigest.getInstance("SHA-1");
			messageDigester.reset();
			messageDigester.update(nonce);
			messageDigester.update(created);
			messageDigester.update(password);
			return new sun.misc.BASE64Encoder().encode(messageDigester.digest());
		}
		catch (final Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
