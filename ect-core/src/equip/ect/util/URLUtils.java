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

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.util;

// helper class to encode urls...works differently to
// java.net version which is for HTML form encoding.
// this class ignores url syntax characters and
// replaces space with %20 rather than +

public class URLUtils
{

	public static String decode(final String url)
	{
		try
		{
			final int len = url.length();
			final StringBuilder decodeURL = new StringBuilder();
			char c;
			for (int i = 0; i < len; i++)
			{
				c = url.charAt(i);
				if (c == '%')
				{
					c = (char) Integer.parseInt(url.substring(i + 1, i + 3), 16);
					i += 2;
				}
				decodeURL.append(c);
			}
			return decodeURL.toString();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static String encode(final String s)
	{
		return encode(s, "$-_.!*'()&+,/:;=?@");
	}

	private static String encode(final String url, String reservedChars)
	{
		try
		{
			if (url == null) { return null; }
			if (reservedChars == null)
			{
				reservedChars = "";
			}
			final StringBuilder sb = new StringBuilder();
			char c;
			for (int i = 0; i < url.length(); i++)
			{
				c = url.charAt(i);
				if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
						|| (reservedChars.indexOf(c) > -1))
				{
					sb.append(c);
					continue;
				}
				if (c > 15)
				{
					sb.append("%").append(Integer.toHexString(c));
				}
				else
				{
					sb.append("%0").append(Integer.toHexString(c));
				}
			}
			return sb.toString();
		}
		catch (final Exception e)
		{
			System.out.println("error encoding url: " + url);
			return null;
		}
	}
}
