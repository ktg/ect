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
package equip.ect.components.audioframework.average;

import java.io.IOException;
import java.util.Vector;

import equip.ect.components.audioframework.common.AbstractAudioProcessor;
import equip.ect.components.audioframework.common.Frame;
import equip.ect.components.audioframework.common.FrameProcessor;

/**
 * Calculate average value of audio samples (part of Media/Audio/Analysis framework ).
 * 
 * <H3>Description</H3> Audio processing component which can receive audio frames (via sink) and
 * calculate average value, then applying a threshold test to set thresholdExceeded.
 * 
 * <H3>Installation</H3> See AudioCaptureManager component for audio capture requirements.
 * 
 * <H3>Configuration</H3> Configured by setting configuration property. Allows value of the form
 * "Average(0.5)", where the float parameter specifies the value for average. Also allows pre-chains
 * of audio processing steps as per the AudioProcessor component (which this effectively extends).
 * 
 * <H3>Usage</H3> Typically, link to sink from the source property of a AudioCaptureDevice component
 * (created by an AudioCaptureManager component) or from the source property of a chained
 * AudioProcessor component.
 * <p>
 * View the component web page using the URL in the configUrl property, which includes some
 * visualisation facilities.
 * 
 * <H3>Technical Details</H3> Part of the audioframework set of components; extends
 * AbstractAudioProcessor.
 * 
 * @technology JMF audio processing
 * @classification Media/Audio/Analysis
 * @defaultInputProperty sink
 * @defaultOutputProperty average
 */
public class AudioAverage extends AbstractAudioProcessor
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
					System.out.println("AudioAverage process frame: " + frame);
				}
				float ave = 0;
				// get as 1channel float
				final Frame aframe = frame.getInFormat(1, Float.TYPE);
				// get as 1 sample
				final Frame aaframe = aframe.getSubsampled(((float[]) aframe.data).length);
				ave = ((float[]) aaframe.data)[0];
				if (debug)
				{
					System.out.println("Ave = " + average);
				}
				setAverage(ave);
				return frame;
			}
			catch (final Exception e)
			{
				System.err.println("ERROR in AudioAverage.processFrame: " + e);
				e.printStackTrace(System.err);
			}
			return frame;
		}
	}

	/**
	 * count (fraction)
	 */
	protected float average;
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
	public AudioAverage() throws IOException
	{
		super("AudioAverage");
		setConfiguration("Average(" + threshold + ")");
	}

	/**
	 * getter
	 */
	public float getAverage()
	{
		return average;
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
	 * internal setter
	 */
	public synchronized void intSetThreshold(final float t)
	{
		final Object old = new Float(threshold);
		threshold = t;
		propertyChangeListeners.firePropertyChange("threshold", old, new Float(t));
		checkAverage();
	}

	/**
	 * setter - broken since it forgets all other configuration
	 */
	public synchronized void setThreshold(final float t)
	{
		setConfiguration("Average(" + t + ")");
	}

	/**
	 * check if exceeded
	 */
	protected synchronized void checkAverage()
	{
		final boolean t = (average > threshold);
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
		final Vector args2 = (Vector) args.clone();
		final FrameProcessor p = super.getProcessor(name, args2);
		if (p != null) { return p; }
		System.out.println("getProcessor(AudioAverage): " + name);
		if (name.equals("Average"))
		{
			final float vals[] = parseFloatArgs(args, 1);
			if (vals == null) { return null; }
			intSetThreshold(vals[0]);
			return new MyFrameProcessor();
		}
		System.err.println("AudioAverage asked for unknown processor: " + name);
		args.setElementAt(((String) (args2.elementAt(0))) + "|Average*", 0);
		return null;
	}

	/**
	 * set - internal
	 */
	protected synchronized void setAverage(final float c)
	{
		final Object old = new Float(this.average);
		this.average = c;
		// every value?!
		propertyChangeListeners.firePropertyChange("average", /* null */old, new Float(c));
		checkAverage();
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