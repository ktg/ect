/*
<COPYRIGHT>

Copyright (c) 2002-2005, Swedish Institute of Computer Science AB
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the Swedish Institute of Computer Science AB
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

Created by: Jan Humble (Swedish Institute of Computer Science AB)
Contributors:
  Jan Humble (Swedish Institute of Computer Science AB)

 */
/*
 * TransferableMultiMedia, $RCSfile: TransferableMultiMedia.java,v $
 * 
 * $Revision: 1.2 $ $Date: 2012/04/03 12:27:26 $
 * 
 * $Author: chaoticgalen $ Original Author: Jan Humble Copyright (c) 2001, Swedish
 * Institute of Computer Science AB
 */
package equip.ect.apps.editor;

public class TransferableMultiMedia extends TransferableMediaObject
{

	public static final int ANIMATION = 3;

	int delay;

	int nrFrames;

	int[] dataLengths;

	byte[][] allData;

	int[] indexes;

	/*
	 * public TransferableMultiMedia(String[] filenames, int delay) { this(filenames, delay, null);
	 * }
	 */
	public TransferableMultiMedia(final String[] filenames)
	{
		super(ANIMATION);

		this.delay = 200;
		this.nrFrames = filenames.length;
		this.dataLengths = new int[nrFrames];
		this.allData = new byte[nrFrames][];
		// this.indexes = indexes;
		this.length = 0;

		for (int i = 0; i < nrFrames; i++)
		{
			final byte[] bytes = MediaFactory.getBytesFromFile(filenames[i]);
			if (bytes != null)
			{
				allData[i] = bytes;
				dataLengths[i] = bytes.length;
				length += bytes.length;
			}
			else
			{
				dataLengths[i] = 0;
				allData[i] = null;
			}
		}
	}

	public int getDataCount()
	{
		return nrFrames;
	}

	public int getFrameDelay()
	{
		return delay;
	}

	public byte[][] getMultiData()
	{
		return allData;
	}

	public int[] getPlaybackIndexes()
	{
		return indexes;
	}

	public void setDelay(final int delay)
	{
		this.delay = delay;
	}

	public void setPlaybackIndexes(final int[] indexes)
	{
		this.indexes = indexes;
	}

}
