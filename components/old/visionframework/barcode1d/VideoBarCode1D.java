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
package equip.ect.components.visionframework.barcode1d;

import java.io.IOException;
import java.util.Vector;

import equip.ect.components.visionframework.common.AbstractVideoProcessor;
import equip.ect.components.visionframework.common.Frame;
import equip.ect.components.visionframework.common.FrameProcessor;

/**
 * Attempt to read 1D barcode from image - very unreliable (works with Media/Video/Analysis
 * framework ).
 * 
 * <H3>Description</H3> An attempt to read 1D barcodes from images. Very unreliable. Also dogged by
 * problems of getting a decent image of a bar code using a normal webcam, e.g. focal distance
 * typically much longer, resolution limited.
 * 
 * <H3>Installation</H3> See JMFVideoCaptureManager for video capture requirements.
 * 
 * <H3>Configuration</H3> threshold can be set to change the black/white threshold used when looking
 * for bar codes.
 * 
 * <H3>Usage</H3> Currently looks at each input line, starting from the centre. Generally a good
 * idea to run through a Drop and Subsample fist (e.g. Drop(3),Subsample(1,10)). Currently pretty
 * knackered. Currently outputs bit sequence as string. Does not output any positional information.
 * Currently stops at the first barcode.
 * 
 * <H3>Technical Details</H3> Currently pretty knackered.
 * 
 * @technology JMF video processing
 * @classification Experimental/Video Analysis
 * @defaultInputProperty sink
 * @defaultOutputProperty data
 */
public class VideoBarCode1D extends AbstractVideoProcessor
{
	/**
	 * internal FrameProcessor
	 */
	public class MyFrameProcessor implements FrameProcessor
	{
		/**
		 * new {@link Frame}.
		 * 
		 * @return processed frame (may be the same).
		 */
		@Override
		public Frame processFrame(final Frame frame, final Frame reference)
		{
			try
			{
				if (debug)
				{
					System.out.println("BarCode1D process frame: " + frame);
				}

				final Frame f = frame.getInFormat(Frame.TYPE_1D, 1, Float.TYPE);
				final float pos[] = new float[f.width];
				for (int j = 0; j < pos.length; j++)
				{
					pos[j] = j;
				}
				// stripe
				for (int i = 0; i < f.height; i++)
				{
					final int y = ((i & 1) == 1) ? frame.height / 2 - ((i + 1) / 2) : frame.height / 2 + i / 2;
					final Frame ff = f.getRegion(0, y, f.width, 1).getPacked();
					float values[] = (float[]) ff.data;
					if (ff.offset != 0 || values.length != f.width)
					{
						values = new float[f.width];
						System.arraycopy(ff.data, ff.offset, values, 0, values.length);
					}
					final BarCode1DReader.Info info = reader.read(values, pos, 0, threshold);
					if (info.matched)
					{
						final StringBuffer b = new StringBuffer();
						for (final boolean element : info.data)
						{
							b.append(element ? '1' : '0');
						}
						final String data = b.toString();
						System.out.print("BarCode1D row " + y + " Data: " + data);

						setData(data);
						return f;
					}
				}
				setData(null);
				return f;
			}
			catch (final Exception e)
			{
				System.err.println("ERROR in BarCode1D.processFrame: " + e);
				e.printStackTrace(System.err);
			}
			return frame;
		}
	}

	/**
	 * noise threshold
	 */
	public static final float DEFAULT_THRESHOLD = 0.1f;
	/**
	 * bar code reading logic
	 */
	protected BarCode1DReader reader = new BarCode1DReader();
	/**
	 * threshold
	 */
	protected float threshold = DEFAULT_THRESHOLD;
	/**
	 * output - data
	 */
	protected String data;

	/**
	 * cons
	 */
	public VideoBarCode1D() throws IOException
	{
		super("VideoBarCode1D");
		setConfiguration("BarCode1D(" + threshold + ")");
	}

	/**
	 * getter
	 */
	public String getData()
	{
		return data;
	}

	/**
	 * getter
	 */
	public float getThreshold()
	{
		return threshold;
	}

	/**
	 * setter
	 */
	public synchronized void setThreshold(final float t)
	{
		final Object old = new Float(threshold);
		threshold = t;
		propertyChangeListeners.firePropertyChange("threshold", old, new Float(t));
	}

	/**
	 * get a processor given name and vector of (String) args (zeroeth el is name again); override!.
	 * 
	 * @return null on error
	 */
	@Override
	protected FrameProcessor getProcessor(final String name, final Vector args)
	{
		if (!name.equals("BarCode1D"))
		{
			System.err.println("VideoBarCode1D asked for unknown processor: " + name);
			args.setElementAt("*BarCode1D*", 0);
			return null;
		}
		final float vals[] = parseFloatArgs(args, 1);
		if (vals == null) { return null; }
		setThreshold(vals[0]);
		return new MyFrameProcessor();
	}

	/**
	 * setter - internal
	 */
	protected synchronized void setData(final String t)
	{
		if (data == t || (data != null && t != null && data.equals(t))) { return; }
		final Object old = data;
		data = t;
		propertyChangeListeners.firePropertyChange("data", old, t);
	}
}
