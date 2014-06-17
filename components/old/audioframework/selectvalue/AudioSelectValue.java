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
package equip.ect.components.audioframework.selectvalue;

import java.io.IOException;
import java.util.Vector;

import equip.ect.components.audioframework.common.AbstractAudioProcessor;
import equip.ect.components.audioframework.common.Frame;
import equip.ect.components.audioframework.common.FrameProcessor;

/**
 * Audio processor which exposes one sample from the input frame (part of Media/Audio/Analysis
 * framework).
 * 
 * <H3>Description</H3> Audio processing component which can receive audio frames (via sink) and
 * selects the a value in the audio frame, then applying a threshold test to set thresholdExceeded.
 * 
 * <H3>Installation</H3> See AudioCaptureManager component for video capture requirements.
 * 
 * <H3>Configuration</H3> Configured by setting configuration property. Allows value of the form
 * "SelectValue(0,0.5)", where the first (int) parameter is the index of the value to be selected
 * from the frame, and the second float parameter specifies the value for the threshold test.Also
 * allows pre-chains of audio processing steps as per the AudioProcessor component (which this
 * effectively extends).
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
 * @classification Media/Audio/Analysis
 * @defaultInputProperty sink
 * @defaultOutputProperty value
 * @technology JMF audio processing
 */
public class AudioSelectValue extends AbstractAudioProcessor
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
					System.out.println("AudioSelectValue process frame: " + frame);
				}
				final float ave = 0;
				// get as 1channel float
				final Frame aframe = frame.getInFormat(1, Float.TYPE);

				final float data[] = (float[]) aframe.data;

				value = data[index] / data[0];
				if (debug)
				{
					System.out.println("value = " + value + " (ref = " + data[0] + ")");
				}
				setValue(value);
				return frame;
			}
			catch (final Exception e)
			{
				System.err.println("ERROR in AudioSelectValue.processFrame: " + e);
				e.printStackTrace(System.err);
			}
			return frame;
		}
	}

	/**
	 * index
	 */
	protected int index;
	/**
	 * count (fraction)
	 */
	protected float value;
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
	public AudioSelectValue() throws IOException
	{
		super("AudioSelectValue");
		setConfiguration("SelectValue(" + index + "," + threshold + ")");
	}

	/**
	 * getter
	 */
	public boolean getThresholdExceeded()
	{
		return thresholdExceeded;
	}

	/**
	 * getter
	 */
	public float getValue()
	{
		return value;
	}

	/**
	 * check if exceeded
	 */
	protected synchronized void checkValue()
	{
		final boolean t = (value > threshold);
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
		System.out.println("getProcessor(AudioSelectValue): " + name);
		if (name.equals("SelectValue"))
		{
			final float vals[] = parseFloatArgs(args, 2);
			if (vals == null) { return null; }
			index = (int) vals[0];
			threshold = vals[1];
			return new MyFrameProcessor();
		}
		System.err.println("AudioSelectValue asked for unknown processor: " + name);
		args.setElementAt(((String) (args2.elementAt(0))) + "|SelectValue*", 0);
		return null;
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

	/**
	 * set - internal
	 */
	protected synchronized void setValue(final float c)
	{
		final Object old = new Float(this.value);
		this.value = c;
		// every value?!
		propertyChangeListeners.firePropertyChange("value", /* null */old, new Float(c));
		checkValue();
	}
}
