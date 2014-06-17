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
 * Tag-It S6000 RF-ID tag reader marshaling and unmarshaling code
 * $RCSfile: S6000Serializer.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:27 $
 *
 * $Author: chaoticgalen $
 * Original Author: Ted Phelps
 *
 * $Log: S6000Serializer.java,v $
 * Revision 1.2  2012/04/03 12:27:27  chaoticgalen
 * Tidying up. Fixed xml reading/writing in Java 6. Some new components
 *
 * Revision 1.1  2005/05/03 11:54:40  cgreenhalgh
 * Import from dumas cvs
 *
 * Revision 1.10  2005/04/28 15:59:22  cmg
 * add BSD license boilerplates
 *
 * Revision 1.9  2004/08/20 14:07:14  phelps
 * Added configurePort to use 19200 8N1 with no flow control.  Other misc tweaks.
 *
 * Revision 1.8  2004/08/20 12:46:47  phelps
 * Added the getName() method
 *
 * Revision 1.7  2004/08/19 16:03:22  phelps
 * Return from digest() iff a complete frame is found.  Rearranged the
 * digest loop a bit to make it reentrant and to fix a thinko.
 *
 * Revision 1.6  2004/08/19 13:16:34  phelps
 * Fixed a minor thinko
 *
 * Revision 1.5  2004/08/19 12:45:37  phelps
 * Reduced the verbosity
 *
 * Revision 1.4  2004/08/19 12:26:10  phelps
 * Interpret the contents of the frame and pass the result to the listener.
 *
 * Revision 1.3  2004/08/18 17:09:52  phelps
 * Can now send version and inventory requests
 *
 * Revision 1.2  2004/08/18 16:31:56  phelps
 * Swiped the S6000 CRC calculator from librfid and verified that it
 * produces the "correct" CRCs.
 *
 * Revision 1.1  2004/08/18 13:45:36  phelps
 * Initial
 */

import java.io.IOException;
import java.io.OutputStream;

import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

public class S6000Serializer extends TagItSerializer
{
	/* This table is used to compute the bogus CRC below */
	private static final int[] crc_table = { 0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7, 0x8108,
											0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef, 0x1231, 0x0210,
											0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6, 0x9339, 0x8318, 0xb37b,
											0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de, 0x2462, 0x3443, 0x0420, 0x1401,
											0x64e6, 0x74c7, 0x44a4, 0x5485, 0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee,
											0xf5cf, 0xc5ac, 0xd58d, 0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6,
											0x5695, 0x46b4, 0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d,
											0xc7bc, 0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
											0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b, 0x5af5,
											0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12, 0xdbfd, 0xcbdc,
											0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a, 0x6ca6, 0x7c87, 0x4ce4,
											0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41, 0xedae, 0xfd8f, 0xcdec, 0xddcd,
											0xad2a, 0xbd0b, 0x8d68, 0x9d49, 0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13,
											0x2e32, 0x1e51, 0x0e70, 0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a,
											0x9f59, 0x8f78, 0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e,
											0xe16f, 0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
											0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e, 0x02b1,
											0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256, 0xb5ea, 0xa5cb,
											0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d, 0x34e2, 0x24c3, 0x14a0,
											0x0481, 0x7466, 0x6447, 0x5424, 0x4405, 0xa7db, 0xb7fa, 0x8799, 0x97b8,
											0xe75f, 0xf77e, 0xc71d, 0xd73c, 0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657,
											0x7676, 0x4615, 0x5634, 0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9,
											0xb98a, 0xa9ab, 0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882,
											0x28a3, 0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
											0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92, 0xfd2e,
											0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9, 0x7c26, 0x6c07,
											0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1, 0xef1f, 0xff3e, 0xcf5d,
											0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8, 0x6e17, 0x7e36, 0x4e55, 0x5e74,
											0x2e93, 0x3eb2, 0x0ed1, 0x1ef0 };

	private static final int MIN_FRAME_SIZE = 7;
	private static final int MAX_FRAME_SIZE = 4095;
	private static final int MAX_IDS = 50;

	private static final byte FRAME_START = (byte) 0xd5;
	private static final byte VERS = 0x11;
	private static final byte[] version_payload = { 0x04, 0x00, VERS };
	private static final byte INV = (byte) 0xfe;
	private static final byte[] inventory_payload = { 0x3, 0x00, INV, 0x00, 0x00, 0x00, 0x04 };
	private static final byte[][] payloads = { version_payload, /* TYPE_VERSION */
	inventory_payload /* TYPE_INVENTORY */
	};

	/*
	 * Compute the CRC of an array of bytes. Note that this is *not* a proper CCITT CRC because TI
	 * have chosen to use a common, but incorrect, variation of the algorithm. For more information
	 * on how CRC-CCITT should be computed, see Joe Geluso's web page:
	 * 
	 * http://www.joegeluso.com/software/articles/ccitt.htm
	 */
	public static int crc(final byte[] bytes, final int offset, final int length)
	{
		int crc, i;

		/* Start with the CRC preset */
		crc = 0xffff;

		/* Use the table to compute the changes for each byte */
		for (i = offset; i < offset + length; i++)
		{
			crc = ((crc << 8) ^ crc_table[(crc >> 8) ^ (bytes[i] & 0xff)]) & 0xffff;
		}

		/* For some mystical reason we need to do this */
		return crc ^ 0xffff;
	}

	@Override
	public void configurePort(final SerialPort port) throws UnsupportedCommOperationException
	{
		/* 19200:8N1 */
		port.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

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
		 * Look for a valid frame by trying every possible offset until we either find a start frame
		 * marker and corresponding CRC, or we run out of bytes.
		 */
		i = 0;
		while (i < offset - MIN_FRAME_SIZE)
		{
			/* Skip this offset if it doesn't begin with the start-of-frame marker */
			if (buffer[i] != FRAME_START)
			{
				i++;
				continue;
			}

			/* Skip it if it has an implausible size */
			size = (buffer[i + 1] & 0xff) << 8 | buffer[i + 2];
			if (size + 3 < MIN_FRAME_SIZE || size + 3 > MAX_FRAME_SIZE)
			{
				i++;
				continue;
			}

			/* Skip it if the size is larger than the number of bytes read */
			if (offset - i < size + 3)
			{
				i++;
				continue;
			}

			/* Skip it if it doesn't have a valid CRC */
			crc = crc(buffer, i + 1, size);
			if (((buffer[i + size + 1] & 0xff) != ((crc >> 8) & 0xff)) || (buffer[i + size + 2] & 0xff) != (crc & 0xff))
			{
				i++;
				continue;
			}

			found = true;

			/* Ok, the frame looks good. Extract its payload */
			payload = new byte[size - 2];
			System.arraycopy(buffer, i + 3, payload, 0, size - 2);

			/* Remove the consumed bytes from the frame buffer */
			offset -= i + size + 3;
			System.arraycopy(buffer, i + size + 3, buffer, 0, offset);
			i = 0;

			/* Interpret the payload if there's a listener */
			if (listener != null)
			{
				switch (payload[2])
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
		return "S6000";
	}

	@Override
	public void sendRequest(final OutputStream out, final int type) throws IOException
	{
		byte[] frame;
		int length, crc;

		/*
		 * The length is the value encoded in the frame: the number of bytes following the header,
		 * or the length of the payload plus 2 for the CRC.
		 */
		length = payloads[type].length + 2;

		/* Allocate enough room for the header (3 bytes) and the rest of the frame */
		frame = new byte[length + 3];

		/* The header is the frame start byte followed by the length */
		frame[0] = FRAME_START;
		frame[1] = (byte) ((length >> 8) & 0xff);
		frame[2] = (byte) (length & 0xff);

		/* Add the payload */
		System.arraycopy(payloads[type], 0, frame, 3, payloads[type].length);

		/* Compute the CRC of the length and payload bytes */
		crc = crc(frame, 1, length);

		/* And append that to the frame */
		frame[length + 1] = (byte) ((crc >> 8) & 0xff);
		frame[length + 2] = (byte) (crc & 0xff);

		/*
		 * Uncomment this to see the actual bytes of the frame System.out.print("cpu->rfid:"); for
		 * (int i = 0; i < frame.length; i++) { System.out.print(" " +
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

		/* Check the service code */
		if (payload[0] != 0x03)
		{
			System.err.println("TagIt: error: reader error");
			return;
		}

		/* No transponders found? */
		if (payload[5] == 0x06)
		{
			listener.tagItInventory(new long[0]);
			return;
		}

		/* Truncate the number of ids at the maximum */
		count = payload[6] & 0xff;
		count = count < MAX_IDS ? count : MAX_IDS;

		/* Sanity check the number of ids */
		if (payload.length < 7 + count * 4)
		{
			System.err.println("TagIt: error: malformed inventory frame");
			return;
		}

		/* Extract the tag ids */
		ids = new long[count];
		for (i = 0; i < count; i++)
		{
			ids[i] = ((long) payload[7 + i * 4] & 0xff) << 24 | ((long) payload[8 + i * 4] & 0xff) << 16
					| ((long) payload[9 + i * 4] & 0xff) << 8 | ((long) payload[10 + i * 4] & 0xff);
		}

		listener.tagItInventory(ids);
	}

	private void digestVersion(final byte[] payload)
	{
		listener.tagItVersion("S6000 " + (payload[3] & 0xff) + "." + (payload[4] & 0xff) + "." + (payload[5] & 0xff)
				+ " " + (payload[6] == 0 ? "(Standard)" : "(Engineering)"));

	}
}
