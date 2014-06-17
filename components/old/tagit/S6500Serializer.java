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
 * Tag-It S6500 RF-ID tag reader marshaling and unmarshaling code
 * $RCSfile: S6500Serializer.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:27 $
 *
 * $Author: chaoticgalen $
 * Original Author: Ted Phelps
 *
 * $Log: S6500Serializer.java,v $
 * Revision 1.2  2012/04/03 12:27:27  chaoticgalen
 * Tidying up. Fixed xml reading/writing in Java 6. Some new components
 *
 * Revision 1.1  2005/05/03 11:54:40  cgreenhalgh
 * Import from dumas cvs
 *
 * Revision 1.3  2005/04/28 15:59:22  cmg
 * add BSD license boilerplates
 *
 * Revision 1.2  2004/08/20 14:07:27  phelps
 * Filled in all of the interesting bits
 *
 * Revision 1.1  2004/08/20 12:46:23  phelps
 * Initial (stub implementation)
 * */

import java.io.IOException;
import java.io.OutputStream;

import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

public class S6500Serializer extends TagItSerializer
{
	private static final int MIN_FRAME_SIZE = 5;
	private static final int MAX_FRAME_SIZE = 255;
	private static final int MAX_IDS = 50;

	private static final byte VERS = 0x65;
	private static final byte[] version_payload = { VERS };
	private static final byte INV = (byte) 0xb0;
	private static final byte[] inventory_payload = { INV, 0x01, 0x00 };
	private static final byte[][] payloads = { version_payload, /* TYPE_VERSION */
	inventory_payload /* TYPE_INVENTORY */
	};

	/* Compute a propert CCITT CRC */
	public static int crc(final byte[] bytes, final int offset, final int length)
	{
		int crc, i, j;

		/* Start with the CRC preset */
		crc = 0xffff;

		/* Do the magic one bit at a time */
		for (i = offset; i < offset + length; i++)
		{
			crc ^= (bytes[i] & 0xff);
			for (j = 0; j < 8; j++)
			{
				if ((crc & 1) == 0)
				{
					crc >>= 1;
				}
				else
				{
					crc = (crc >> 1) ^ 0x8408;
				}
			}
		}

		return crc;
	}

	@Override
	public void configurePort(final SerialPort port) throws UnsupportedCommOperationException

	{
		/* 38400 8E1 */
		port.setSerialPortParams(38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);

		/* No flow control */
		port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
	}

	@Override
	public boolean digest()
	{
		int i, size, crc;
		byte[] payload;
		boolean found = false;

		/*
		 * Uncomment this to see the actual bytes of the frame System.out.print("cpu<-rfid:"); for
		 * (i = 0; i < offset; i++) { System.out.print(" " + Integer.toHexString((int)buffer[i] &
		 * 0xff)); } System.out.println(); /*
		 */
		/*
		 * Look for a valid frame by trying every possible offset until we either find something
		 * that looks like a valid frame or we run out of bytes
		 */
		i = 0;
		while (i < offset - MIN_FRAME_SIZE)
		{
			/* Skip this offset if the size is implausibly small */
			size = buffer[i] & 0xff;
			if (size < MIN_FRAME_SIZE)
			{
				i++;
				continue;
			}

			/* Skip it if the size is larger than the number of bytes read */
			if (offset - i < size)
			{
				i++;
				continue;
			}

			/* Skip it if it doesn't have a valid CRC */
			crc = crc(buffer, i, size - 2);
			if ((buffer[i + size - 2] & 0xff) != (crc & 0xff) || (buffer[i + size - 1] & 0xff) != ((crc >> 8) & 0xff))
			{
				i++;
				continue;
			}

			found = true;

			/* The frame looks good. Extract its payload */
			payload = new byte[size - 4];
			System.arraycopy(buffer, i + 2, payload, 0, size - 4);

			/* Remove the consumed bytes from the frame buffer */
			offset -= i + size;
			System.arraycopy(buffer, i + size, buffer, 0, offset);
			i = 0;

			/* Interpret the payload if there's a listener */
			if (listener != null)
			{
				switch (payload[0])
				{
					case VERS:
						digestVersion(payload);
						break;

					case INV:
						digestInventory(payload);
						break;

					default:
						System.out.println("TagIt: warning: unknown response " + payload[2]);
						break;
				}
			}
		}

		return found;
	}

	@Override
	public String getName()
	{
		return "S6500";
	}

	@Override
	public void sendRequest(final OutputStream out, final int type) throws IOException
	{
		byte[] frame;
		int length, crc;

		/*
		 * The length is the value encoded in the frame: the number of bytes in the payload plus 3
		 * for the frame (1 for the length, 1 for the COM ADR, and 2 for the CRC
		 */
		length = payloads[type].length + 4;

		/* Allocate a buffer in which to build the frame */
		frame = new byte[length];

		/* Header: frame length */
		frame[0] = (byte) length;

		/* Header: COM ADR */
		frame[1] = (byte) 0xff;

		/* Payload */
		System.arraycopy(payloads[type], 0, frame, 2, payloads[type].length);

		/* Compute the CRC of the length and payload */
		crc = crc(frame, 0, length - 2);

		/* Footer: CRC */
		frame[length - 2] = (byte) (crc & 0xff);
		frame[length - 1] = (byte) ((crc >> 8) & 0xff);

		/*
		 * Uncomment the following to see the actual bytes written System.out.print("cpu->rfid:");
		 * for (int i = 0; i < length; i++) { System.out.print(" " +
		 * Integer.toHexString((int)frame[i] & 0xff)); } System.out.println(); /*
		 */
		/* Send the frame */
		out.write(frame);
		out.flush();
	}

	private void digestInventory(final byte[] payload)
	{
		int count, i;
		long[] ids;

		/* Watch for an absence of transponders */
		if (payload[1] == 0x01)
		{
			listener.tagItInventory(new long[0]);
			return;
		}

		/* Watch for random status errors */
		if (payload[1] != 0 && payload[1] != (byte) 0x94)
		{
			System.err.println("TagIt: warning: reader error: 0x" + Integer.toHexString(payload[2]));
			return;
		}

		/* Truncate the number of ids at the maximum */
		count = payload[2];
		count = count < MAX_IDS ? count : MAX_IDS;

		/* Verify the number of ids against the payload size */
		if (payload.length < 3 + count * 10)
		{
			System.err.println("TagIt: error: malformed inventory frame");
			return;
		}

		/* Extract the tag ids */
		ids = new long[count];
		for (i = 0; i < count; i++)
		{
			ids[i] = ((long) payload[3 + i * 10 + 2] & 0xff) << 56 | ((long) payload[3 + i * 10 + 3] & 0xff) << 48
					| ((long) payload[3 + i * 10 + 4] & 0xff) << 40 | ((long) payload[3 + i * 10 + 5] & 0xff) << 32
					| ((long) payload[3 + i * 10 + 6] & 0xff) << 24 | ((long) payload[3 + i * 10 + 7] & 0xff) << 16
					| ((long) payload[3 + i * 10 + 8] & 0xff) << 8 | ((long) payload[3 + i * 10 + 9] & 0xff);
		}

		listener.tagItInventory(ids);
	}

	private void digestVersion(final byte[] payload)
	{
		listener.tagItVersion("S6500 " + (payload[2] & 0xf) + "." + (payload[3] & 0xff) + " ("
				+ ((payload[2] >> 4) & 0xf) + ")");
	}
}
