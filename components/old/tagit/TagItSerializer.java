/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Sussex
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Sussex
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

Created by: Ted Phelps (University of Sussex)
Contributors:
  Ted Phelps (University of Sussex)

 */
package equip.ect.components.tagit;

/*
 * Tag-It RF-ID tag reader marshaling and unmarshaling code, $RCSfile: TagItSerializer.java,v $
 *
 * $Revision: 1.3 $
 * $Date: 2012/04/03 12:27:27 $
 *
 * $Author: chaoticgalen $
 * Original Author: Ted Phelps
 *
 * $Log: TagItSerializer.java,v $
 * Revision 1.3  2012/04/03 12:27:27  chaoticgalen
 * Tidying up. Fixed xml reading/writing in Java 6. Some new components
 *
 * Revision 1.2  2005/07/20 16:52:16  cgreenhalgh
 * ensure serial initialised; change config property names to fit convention
 *
 * Revision 1.1  2005/05/03 11:54:40  cgreenhalgh
 * Import from dumas cvs
 *
 * Revision 1.7  2005/04/28 15:59:22  cmg
 * add BSD license boilerplates
 *
 * Revision 1.6  2004/08/20 14:06:35  phelps
 * Added configurePort so that different reader models can use different
 * serial port settings
 *
 * Revision 1.5  2004/08/20 12:46:47  phelps
 * Added the getName() method
 *
 * Revision 1.4  2004/08/19 16:04:08  phelps
 * feed() and digest() now return true iff a complete frame was found.
 * Added the reset() method.
 *
 * Revision 1.3  2004/08/19 12:23:14  phelps
 * Use a listener interface to send back interpreted frame information
 *
 * Revision 1.2  2004/08/18 17:10:23  phelps
 * Added numbers to identify the different request types.
 *
 * Revision 1.1  2004/08/18 13:45:36  phelps
 * Initial
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

public abstract class TagItSerializer
{
	public static final int BUFFER_SIZE = 2048;
	public static final int TYPE_VERSION = 0;
	public static final int TYPE_INVENTORY = 1;

	protected byte[] buffer = new byte[BUFFER_SIZE];
	protected int offset = 0;
	protected TagItSerializerListener listener;

	public void addListener(final TagItSerializerListener listener) throws TooManyListenersException
	{
		if (this.listener != null) { throw new TooManyListenersException(); }

		this.listener = listener;
	}

	public abstract void configurePort(SerialPort port) throws UnsupportedCommOperationException;

	/* Returns true if a complete frame was read */
	public abstract boolean digest();

	/* Returns true if a complete frame was read */
	public boolean feed(final InputStream in) throws IOException
	{
		int length;

		int max = in.available();
		if (max > BUFFER_SIZE - offset)
		{
			max = BUFFER_SIZE - offset;
		}

		if (max == 0)
		{
			System.out.println("WARNING: TagIt says no data available");
			max = 1;
		}
		/* Read bytes from the input stream into the frame construction buffer */
		length = in.read(buffer, offset, max);
		offset += length;

		/* Hunt around for complete frames */
		return digest();
	}

	public abstract String getName();

	public void reset()
	{
		offset = 0;
	}

	public abstract void sendRequest(OutputStream out, int type) throws IOException;
}
