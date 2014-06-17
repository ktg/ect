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
package equip.ect.components.visionframework.booleanpixelcount;

import java.io.IOException;
import java.util.Vector;

import equip.ect.components.visionframework.common.AbstractVideoProcessor;
import equip.ect.components.visionframework.common.Frame;
import equip.ect.components.visionframework.common.FrameProcessor;

/**
 * Count 'true' pixels in input image (part of Media/Video/Analysis framework ).
 * 
 * <H3>Description</H3> Video processing component which can receive video frames (via sink), and
 * calculate fraction of pixels which are 'true', then applying a threshold test to set
 * thresholdExceeded.
 * 
 * <H3>Installation</H3> See JMFVideoCaptureManager for video capture requirements.
 * 
 * <H3>Configuration</H3> Configured by setting "configuration" property. Example value
 * "BooleanPixelCount(0.5)" for a threshold value of 50% of pixels being true to trigger output.
 * 
 * <H3>Usage</H3> Typically, link to sink from the source property of a JMFVideoCaptureDevice
 * component (created by an JMFVideoCaptureManager component) or from the source property of a
 * chained SimpleVideoProcessor component.
 * <p>
 * View the component web page using the URL in the configUrl property, which includes access to
 * most recent input and processed image(s).
 * 
 * <H3>Technical Details</H3> Part of the visionframework set of components; extends
 * AbstractVideoProcessor.
 * 
 * @technology JMF video processing
 * @displayName VideoBooleanPixelCount
 * @classification Media/Video/Analysis
 * @defaultInputProperty sink
 * @defaultOutputProperty count
 */
public class BooleanPixelCount extends AbstractVideoProcessor
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
				System.out.println("BooleanPixelCount process frame: " + frame);
				final int count = frame.getTrueCount();
				System.out.println("Count = " + count + "/" + (frame.width * frame.height));
				setCount(1.0f * count / (frame.width * frame.height));
				return frame;
			}
			catch (final Exception e)
			{
				System.err.println("ERROR in BooleaPixelCount.processFrame: " + e);
				e.printStackTrace(System.err);
			}
			return frame;
		}
	}

	/**
	 * count (fraction)
	 */
	protected float count;
	/**
	 * threshold
	 */
	protected float threshold = 0.5f;
	/**
	 * output - threshold exceeded
	 */
	protected boolean thresholdExceeded;

	/**
	 * cons
	 */
	public BooleanPixelCount() throws IOException
	{
		super("BooleanPixelCount");
		setConfiguration("BooleanPixelCount(" + threshold + ")");
	}

	/**
	 * getter
	 */
	public float getCount()
	{
		return count;
	}

	/**
	 * getter
	 */
	public float getThreshold()
	{
		return threshold;
	}

	/**
	 * getter
	 */
	public boolean getThresholdExceeded()
	{
		return thresholdExceeded;
	}

	/**
	 * setter
	 */
	public synchronized void setThreshold(final float t)
	{
		final Object old = new Float(threshold);
		threshold = t;
		propertyChangeListeners.firePropertyChange("threshold", old, new Float(t));
		checkCount();
	}

	/**
	 * check if exceeded
	 */
	protected synchronized void checkCount()
	{
		final boolean t = (count > threshold);
		if (t != thresholdExceeded)
		{
			setThresholdExceeded(t);
		}
	}

	/**
	 * get a processor given name and vector of (String) args (zeroeth el is name again); override!.
	 * 
	 * @return null on error
	 */
	@Override
	protected FrameProcessor getProcessor(final String name, final Vector args)
	{
		if (!name.equals("BooleanPixelCount"))
		{
			System.err.println("BooleanPixelCount asked for unknown processor: " + name);
			args.setElementAt("*BooleanPixelCount*", 0);
			return null;
		}
		// args1 = threshold
		if (args.size() > 2)
		{
			args.setElementAt("*TOO*MANY*ARGS*" + ((String) args.elementAt(2)), 2);
			return null;
		}
		else if (args.size() < 2)
		{
			args.addElement("*" + threshold + "*");
			return null;
		}
		try
		{
			final float t = new Float((String) args.elementAt(1)).floatValue();
			setThreshold(t);
			return new MyFrameProcessor();
		}
		catch (final Exception e)
		{
			args.setElementAt("*ERROR*" + ((String) args.elementAt(1)), 1);
			System.err.println("Error parsing float " + args.elementAt(1) + ": " + e);
		}
		return null;
	}

	/**
	 * set - internal
	 */
	protected synchronized void setCount(final float c)
	{
		final Object old = new Float(this.count);
		this.count = c;
		propertyChangeListeners.firePropertyChange("count", old, new Float(c));
		checkCount();
	}

	/**
	 * setter - internal
	 */
	protected synchronized void setThresholdExceeded(final boolean t)
	{
		final Object old = new Boolean(thresholdExceeded);
		thresholdExceeded = t;
		propertyChangeListeners.firePropertyChange("thresholdExceeded", old, new Boolean(t));
	}
}
