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
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.webstart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * stream pump
 */
public class StreamPump extends Thread
{
	InputStream is;
	OutputStream os;
	boolean closeOnEnd;

	/**
	 * cons - don't close on end
	 */
	public StreamPump(final InputStream is, final OutputStream os)
	{
		this(is, os, false);
	}

	/**
	 * cons
	 */
	public StreamPump(final InputStream is, final OutputStream os, final boolean closeOnEnd)
	{
		if (is == null || os == null) { return; }
		this.is = is;
		this.os = os;
		this.closeOnEnd = closeOnEnd;
		start();
	}

	/**
	 * run
	 */
	@Override
	public void run()
	{
		try
		{
			final byte data[] = new byte[1000];
			while (true)
			{
				int count;
				int total = 0;
				while (total < data.length)
				{
					int avail = is.available();
					if (avail <= 0)
					{
						if (total > 0)
						{
							break;
						}
						avail = 1;
					}
					if (avail > data.length - total)
					{
						avail = data.length - total;
					}

					count = is.read(data, total, avail);
					if (count > 0)
					{
						total = total + count;
					}
					else
					{
						break;
					}
				}
				if (total > 0)
				{
					os.write(data, 0, total);
					os.flush();
				}
				else
				{
					break;
					// not too fast?!
					// Thread.sleep(100);
				}

			}
		}
		catch (final IOException e)
		{
			System.out.println("StreamPump exception: " + e);
		}
		if (closeOnEnd)
		{
			try
			{
				os.close();
			}
			catch (final Exception e)
			{
			}
		}
	}
}
