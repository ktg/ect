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
package equip.ect.components.visionframework.simplevideoprocessor;

import java.io.IOException;
import java.util.Vector;

import equip.ect.components.visionframework.common.AbstractVideoProcessor;
import equip.ect.components.visionframework.common.Frame;
import equip.ect.components.visionframework.common.FrameProcessor;
import equip.ect.components.visionframework.common.UnsupportedFormatException;

/**
 * Configurable video processor (part of Media/Video/Analysis framework). <H3>Description</H3> Audio
 * processing component which can receive audio frames (via sink), apply various processing steps to
 * the frame, and then output it to another audio component (via source).
 * 
 * <H3>Installation</H3> See AudioCaptureManager component for video capture requirements.
 * 
 * <H3>Configuration</H3> Configured by setting configuration property. Allows value of the form
 * "PROCESSOR1(ARG1,ARG2,...),PROCESSOR2(ARG1,...),...".
 * <p>
 * Available processors are:
 * <ul>
 * <LI>RegionProcessor, emits frames limited to a sub-region of the input. Configuration syntax
 * "Region(topleftfaction,toprightfraction,widthfraction,heightfraction}", e.g.
 * "Region(0.1,0.1,0.2,0.2)".</LI>
 * <LI>SubsampleProcessor, emits frames subsampled in x and y (i.e. 1 in n pixels in each
 * direction). Configuration syntax "Subsample(xstep,ystep)", e.g. "Subsample(2,2)".</LI>
 * <LI>DropProcessor, emits 1 frame and then drop N frames, repeatedly. Configuration syntax
 * "Drop(dropNum)", e.g. "Drop(5)".</LI>
 * <LI>FormatProcessor, emits frames in the requested format, "RGB" (packed 8bpp), "YUV" (3 floats),
 * "float" (1 float) or "boolean" (1 boolean) per pixel. Syntax "Format(RGB|YUV|float|boolean)",
 * e.g. "Format(YUV)".</LI>
 * <LI>ChangeProcessor, emits frames in "float" format (1 float per pixel), where the value is the
 * square of the difference between the current and the last frame, with the (up to 3) per-pixel
 * components scaled as specified. If the input format has less than 3 components then the other
 * scale factors are ignored. Configuration syntax "Change(yscale,uscale,vscale)", e.g.
 * "Change(1.0,1.0,1.0)".</LI>
 * <LI>DifferenceProcessor, emits frames in "float" format (1 float per pixel), where the value is
 * the square of the difference between the current and the <b>reference</b> frame, with the (up to
 * 3) per-pixel components scaled as specified. If the input format has less than 3 components then
 * the other scale factors are ignored. Configuration syntax "Difference(yscale,uscale,vscale)",
 * e.g. "Difference(1.0,1.0,1.0)".</LI>
 * <LI>ColourDistanceProcessor, emits frames in "float" format (1 float per pixel), where the value
 * is the square of the difference between the current frame and a supplied reference colour, with
 * the (up to 3) per-pixel components scaled as specified. Configuration syntax
 * "ColourDistance(yref,uref,vref,yscale,uscale,vscale)", e.g.
 * "ColourDistance(0.1,0.2,0.5,1.0,1.0,0.1)".</LI>
 * <LI>ThresholdProcessor, emits frames in "boolean" format (1 boolean per pixel), where true
 * indicates that the input (coerced to 1 float per pixel) is greater than (or less than) the
 * specified threshold. Configuration syntax "Threshold(threshold)", e.g. "Threshold(0.1)", for
 * above threshold, "ThresholdBelow(threshold)", e.g. "ThesholdBelow(0.2)" for below threshold.</LI>
 * <LI>MedianProcessor, currently coerces input to "boolean" and emits frames in "boolean" format (1
 * boolean per pixel), where the output value is the commonest in a square +/-radius of the input
 * pixel. The output image will be smaller by the radius all around. Configuration syntax
 * "Median(radius)", e.g. "Median(1)".</LI>
 * <LI>MeanProcessor, currently coerces input to "float" or "YUV" format and emits frames in the
 * same format, where the output value is the average in a square +/-radius of the input pixel. The
 * output image will be smaller by the radius all around. Configuration syntax "Mean(radius)", e.g.
 * "Mean(1)".</LI>
 * <LI>ChangeGateProcessor, is a combination of Change, Threshold, and Median, which emits a frame
 * which is a copy of the input frame but with 'false' pixels after the (optional) median filter
 * forced to '0'/'false'. I.e. it 'gates' the input image to show only changing pixels.
 * Configuration syntax "ChangeGate(yscale,uscale,vscale,threshold,medianRadius)", e.g.
 * "ChangeGate(1.0,1.0,1.0,0.005,1)".</LI>
 * <LI>DifferenceGateProcessor, is a combination of Difference, Threshold, and Median, which emits a
 * frame which is a copy of the input frame but with 'false' pixels after the (optional) median
 * filter forced to '0'/'false'. I.e. it 'gates' the input image to show only pixels difference from
 * the reference image. Configuration syntax
 * "DifferenceGate(yscale,uscale,vscale,threshold,medianRadius)", e.g.
 * "DifferenceGate(1.0,1.0,1.0,0.005,1)".</LI>
 * <LI>BackgroundGateProcessor, is a combination of Change, Threshold, and FrameAverage (see below)
 * which emits a frame which is a copy of the input frame only when the input frame is considered
 * not to have changed for some specified time. (The last accepted background image is re-emitted
 * while considering subsequent frames.). Configuration syntax
 * "BackgroundGate(yscale,uscale,vscale,threshold,changeFraction,staticTimeSeconds)", e.g.
 * "ChangeGate(1.0,1.0,1.0,0.005,0.01,5.0)".</LI>
 * </UL>
 * 
 * <H3>Usage</H3>
 * Typically, link to sink from the source property of a JMFVideoCaptureDevice component (created by
 * an JMFVideoCaptureManager component) or from the source property of a chained
 * SimpleVideoProcessor component to a VideoFrameAverage or VideoFrameExporter component which
 * exposes the final processing result.
 * <p>
 * View the component web page using the URL in the configUrl property, which includes access to
 * most recent input and processed image(s).
 * <p>
 * Note: for efficiency/performance, perform Drop, Subsample and/or Region processes as close to the
 * start of the processing chain as possible.
 * 
 * <H3>Technical Details</H3>
 * Part of the visionframework set of components; extends AbstractVideoProcessor. some visualisation
 * facilities.
 * 
 * @technology JMF video processing
 * @displayName VideoProcessor
 * @classification Media/Video/Analysis
 * @defaultInputProperty sink
 * @defaultOutputProperty source
 */
public class SimpleVideoProcessor extends AbstractVideoProcessor
{
	/**
	 * internal FrameProcessor - output candidate 'background' frames, i.e. which are unchanging
	 * (subject to some distance/threshold test for at least some time)
	 */
	public class BackgroundGateProcessor implements FrameProcessor
	{
		/**
		 * threshold proc
		 */
		protected ThresholdProcessor threshold;
		/**
		 * pixel count fraction
		 */
		protected float fraction;
		/**
		 * time
		 */
		protected float timeSeconds;
		/**
		 * scales
		 */
		protected float scales[];
		/**
		 * last frame
		 */
		protected Frame candidate = null;
		/**
		 * background frame
		 */
		protected Frame background = null;
		/**
		 * candidate time
		 */
		protected long candidateTime = 0;
		/**
		 * candidate count
		 */
		protected int candidateCount = 0;
		/**
		 * min
		 */
		protected int minCandidateCount = 10;

		/**
		 * cons
		 */
		public BackgroundGateProcessor(final float element1Scale, final float element2Scale, final float element3Scale,
				final float distance, final float fraction, final float timeSeconds)
		{
			this.scales = new float[] { element1Scale, element2Scale, element3Scale };
			this.timeSeconds = timeSeconds;
			this.fraction = fraction;
			this.threshold = new ThresholdProcessor(distance, true);
			System.out.println("New ChangeProcessor(" + scales[0] + "," + scales[1] + "," + scales[2] + "," + distance
					+ "," + fraction + "," + timeSeconds + ")");
		}

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
				if (candidate == null)
				{
					System.out.println("BackgroundGateProcessor consumes first frame");
					candidate = frame;
					candidateTime = System.currentTimeMillis();
					candidateCount = 0;
					return null;
				}

				final Frame f = frame.getDistance2(candidate, scales[0], scales[1], scales[2]);
				if (f == null) { return null; }
				final Frame f2 = threshold.processFrame(f, reference);
				if (f2 == null) { return null; }
				final int count = f2.getTrueCount();
				final boolean same = (count < f2.width * f2.height * fraction);

				if (!same)
				{
					if (debug)
					{
						System.out.println("Background changed (fraction " + (count * 1.0f / f2.width * f2.height)
								+ "/" + fraction + ")");
					}
					candidate = frame;
					candidateTime = System.currentTimeMillis();
					candidateCount = 0;
					return null/* background */;
				}
				candidateCount++;
				final float duration = 0.001f * (System.currentTimeMillis() - candidateTime);
				if (duration < timeSeconds || candidateCount < minCandidateCount)
				{
					if (debug)
					{
						System.out.println("Still considering new background");
					}
					return null/* background */;
				}

				// new background!
				System.out.println("New background accepted");
				background = candidate;

				candidate = frame;
				candidateTime = System.currentTimeMillis();
				candidateCount = 0;

				return background;
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("ChangeProcessor conversion failed: " + e);
			}
			return null;
		}
	}

	/**
	 * internal FrameProcessor - output Pixels which have changed by more than threshold between
	 * current and last frame
	 */
	public class ChangeGateProcessor extends ChangeProcessor
	{
		/**
		 * radius
		 */
		protected int medianRadius;
		/**
		 * median filter
		 */
		protected MedianProcessor median;
		/**
		 * threshold proc
		 */
		protected ThresholdProcessor threshold;

		/**
		 * cons
		 */
		public ChangeGateProcessor(final float element1Scale, final float element2Scale, final float element3Scale,
				final float threshold, final int medianRadius, final boolean above)
		{
			super(element1Scale, element2Scale, element3Scale);
			this.threshold = new ThresholdProcessor(threshold, above);
			this.medianRadius = medianRadius;
			if (medianRadius > 0)
			{
				this.median = new MedianProcessor(medianRadius);
			}
			System.out.println("New ChangeGateProcessor(...)");
		}

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
				final Frame f = super.processFrame(frame, reference);
				if (f == null) { return null; }
				final Frame f2 = threshold.processFrame(f, reference);
				if (f2 == null) { return null; }

				final Frame f3 = (median == null) ? f2 : median.processFrame(f2, reference);
				if (f3 == null) { return null; }
				final Frame ff = (median == null) ? frame : frame.getRegion(medianRadius, medianRadius, frame.width - 2
						* medianRadius, frame.height - 2 * medianRadius);
				return ff.gate(f3);
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("ChangeGateProcessor conversion failed: " + e);
				last = frame;
			}
			return null;
		}
	}

	/**
	 * internal FrameProcessor - output distance between current and last frame
	 */
	public class ChangeProcessor implements FrameProcessor
	{
		/**
		 * scales
		 */
		protected float scales[];
		/**
		 * last frame
		 */
		protected Frame last = null;

		/**
		 * cons
		 */
		public ChangeProcessor(final float element1Scale, final float element2Scale, final float element3Scale)
		{
			this.scales = new float[] { element1Scale, element2Scale, element3Scale };
			System.out.println("New ChangeProcessor(" + scales[0] + "," + scales[1] + "," + scales[2] + ")");
		}

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
				if (last == null)
				{
					System.out.println("ChangeProcessor consumes first frame");
					last = frame;
					return null;
				}

				final Frame f = frame.getDistance2(last, scales[0], scales[1], scales[2]);
				last = frame;
				return f;
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("ChangeProcessor conversion failed: " + e);
				last = frame;
			}
			return null;
		}
	}

	/**
	 * internal FrameProcessor - output distance between frame and fixed colour
	 */
	public class ColourDistanceProcessor implements FrameProcessor
	{
		/**
		 * scales
		 */
		protected float scales[];

		/**
		 * cons
		 */
		public ColourDistanceProcessor(final float[] scales)
		{
			this.scales = scales;
			System.out.println("New ColourDistanceProcessor(" + scales[0] + "," + scales[1] + "," + scales[2] + ","
					+ scales[3] + "," + scales[4] + "," + scales[5] + ")");
		}

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
				final Frame f = frame.getDistance2FromConstant(	scales[0], scales[1], scales[2], scales[3], scales[4],
																scales[5]);
				return f;
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("ColourDistanceProcessor conversion failed: " + e);
			}
			return null;
		}
	}

	/**
	 * internal FrameProcessor - output Pixels which have changed by more than threshold between
	 * current and last frame
	 */
	public class DifferenceGateProcessor extends DifferenceProcessor
	{
		/**
		 * radius
		 */
		protected int medianRadius;
		/**
		 * median filter
		 */
		protected MedianProcessor median;
		/**
		 * threshold proc
		 */
		protected ThresholdProcessor threshold;

		/**
		 * cons
		 */
		public DifferenceGateProcessor(final float element1Scale, final float element2Scale, final float element3Scale,
				final float threshold, final int medianRadius, final boolean above)
		{
			super(element1Scale, element2Scale, element3Scale);
			this.threshold = new ThresholdProcessor(threshold, above);
			this.medianRadius = medianRadius;
			if (medianRadius > 0)
			{
				this.median = new MedianProcessor(medianRadius);
			}
			System.out.println("New DifferenceGateProcessor(...)");
		}

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
				final Frame f = super.processFrame(frame, reference);
				if (f == null) { return null; }
				final Frame f2 = threshold.processFrame(f, reference);
				if (f2 == null) { return null; }
				final Frame f3 = (median == null) ? f2 : median.processFrame(f2, reference);
				if (f3 == null) { return null; }
				final Frame ff = (median == null) ? frame : frame.getRegion(medianRadius, medianRadius, frame.width - 2
						* medianRadius, frame.height - 2 * medianRadius);
				return ff.gate(f3);
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("DifferenceGateProcessor conversion failed: " + e);
			}
			return null;
		}
	}

	/**
	 * internal FrameProcessor - output distance between current and last frame
	 */
	public class DifferenceProcessor implements FrameProcessor
	{
		/**
		 * scales
		 */
		protected float scales[];

		/**
		 * cons
		 */
		public DifferenceProcessor(final float element1Scale, final float element2Scale, final float element3Scale)
		{
			this.scales = new float[] { element1Scale, element2Scale, element3Scale };
			System.out.println("New DifferenceProcessor(" + scales[0] + "," + scales[1] + "," + scales[2] + ")");
		}

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
				if (reference == null)
				{
					System.out.println("DifferenceProcessor called with no reference");
					return null;
				}

				final Frame f = frame.getDistance2(reference, scales[0], scales[1], scales[2]);
				return f;
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("DifferenceProcessor conversion failed: " + e);
			}
			return null;
		}
	}

	/**
	 * internal FrameProcessor - drop N per frame passed
	 */
	public class DropProcessor implements FrameProcessor
	{
		/**
		 * drop
		 */
		protected int drop;
		/**
		 * count
		 */
		protected int count;

		/**
		 * cons
		 */
		public DropProcessor(final int drop)
		{
			this.drop = drop;
			System.out.println("New DropProcessor(" + drop + ")");
		}

		/**
		 * new {@link Frame}.
		 * 
		 * @return processed frame (may be the same).
		 */
		@Override
		public Frame processFrame(final Frame frame, final Frame reference)
		{
			if (count < drop)
			{
				count++;
				return null;
			}
			count = 0;
			return frame;
		}
	}

	/**
	 * internal FrameProcessor - get in format
	 */
	public class FormatProcessor implements FrameProcessor
	{
		/**
		 * type
		 */
		protected int type;
		/**
		 * elements per pixel
		 */
		protected int elementsPerPixel;
		/**
		 * data class
		 */
		protected Class dataClass;

		/**
		 * cons
		 */
		public FormatProcessor(final int type, final int elementsPerPixel, final Class dataClass)
		{
			this.type = type;
			this.elementsPerPixel = elementsPerPixel;
			this.dataClass = dataClass;
			System.out
					.println("New FormatProcessor(" + type + "," + elementsPerPixel + "," + dataClass.getName() + ")");
		}

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
				return frame.getInFormat(type, elementsPerPixel, dataClass);
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("FormatProcessor conversion failed: " + e);
			}
			return null;
		}
	}

	/**
	 * internal FrameProcessor - output Mean filtered version
	 */
	public class MeanProcessor implements FrameProcessor
	{
		/**
		 * radius
		 */
		protected int MeanRadius;

		/**
		 * cons
		 */
		public MeanProcessor(final int MeanRadius)
		{
			this.MeanRadius = MeanRadius;
			System.out.println("New MeanProcessor(" + MeanRadius + ")");
		}

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
				final Frame f = frame.meanFilter(MeanRadius);
				return f;
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("MeanProcessor conversion failed: " + e);
			}
			return null;
		}
	}

	/**
	 * internal FrameProcessor - output median filtered version
	 */
	public class MedianProcessor implements FrameProcessor
	{
		/**
		 * radius
		 */
		protected int medianRadius;

		/**
		 * cons
		 */
		public MedianProcessor(final int medianRadius)
		{
			this.medianRadius = medianRadius;
			System.out.println("New MedianProcessor(" + medianRadius + ")");
		}

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
				final Frame f = frame.medianFilter(medianRadius);
				return f;
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("MedianProcessor conversion failed: " + e);
			}
			return null;
		}
	}

	/**
	 * internal FrameProcessor - return sub-region
	 */
	public class RegionProcessor implements FrameProcessor
	{
		/**
		 * x top left
		 */
		protected float x;
		/**
		 * y top left
		 */
		protected float y;
		/**
		 * width
		 */
		protected float w;
		/**
		 * height
		 */
		protected float h;

		/**
		 * cons
		 */
		public RegionProcessor(final float x, final float y, final float w, final float h)
		{
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			System.out.println("New RegionProcessor(" + x + "," + y + "," + w + "," + h + ")");
		}

		/**
		 * new {@link Frame}.
		 * 
		 * @return processed frame (may be the same).
		 */
		@Override
		public Frame processFrame(final Frame frame, final Frame reference)
		{
			return frame.getRegion(	(int) (frame.width * x), (int) (frame.height * y), (int) (frame.width * w),
									(int) (frame.height * h));
		}
	}

	/**
	 * internal FrameProcessor - subsampole
	 */
	public class SubsampleProcessor implements FrameProcessor
	{
		/**
		 * x steps
		 */
		protected int xsteps;
		/**
		 * y steps
		 */
		protected int ysteps;

		/**
		 * cons
		 */
		public SubsampleProcessor(final int xsteps, final int ysteps)
		{
			this.xsteps = xsteps;
			this.ysteps = ysteps;
			System.out.println("New SubsampleProcessor(" + xsteps + "," + ysteps + ")");
		}

		/**
		 * new {@link Frame}.
		 * 
		 * @return processed frame (may be the same).
		 */
		@Override
		public Frame processFrame(final Frame frame, final Frame reference)
		{
			return frame.getSubsampled(xsteps, ysteps);
		}
	}

	/**
	 * internal FrameProcessor - threshold
	 */
	public class ThresholdProcessor implements FrameProcessor
	{
		/**
		 * value
		 */
		protected float threshold;
		/**
		 * above?
		 */
		protected boolean above;

		/**
		 * cons
		 */
		public ThresholdProcessor(final float threshold, final boolean above)
		{
			this.threshold = threshold;
			this.above = above;
			System.out.println("New ThresholdProcessor(" + threshold + "," + above + ")");
		}

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
				final Frame f = frame.getInFormat(Frame.TYPE_1D, 1, Float.TYPE);
				// like getInFormat(Frame.TYPE_1D, 1, Boolean.TYPE) but with threshold and aboveFlag
				// specified
				final int datasize = f.width * f.height * f.elementsPerPixel;
				final boolean fdata[] = new boolean[datasize];
				final Frame out = new Frame(Frame.TYPE_1D, 1, f.width, f.height, 0, 1, f.width, fdata);
				Frame.convertFloatToBoolean(f, out, threshold, above);
				return out;
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("ThresholdProcessor conversion failed: " + e);
			}
			return null;
		}
	}

	/**
	 * cons
	 */
	public SimpleVideoProcessor() throws IOException
	{
		super("SimpleVideoProcessor");
		setConfiguration("");
	}

	/**
	 * get a processor given name and vector of (String) args (zeroeth el is name again); override!.
	 * 
	 * @return null on error
	 */
	@Override
	protected FrameProcessor getProcessor(final String name, final Vector args)
	{
		System.out.println("getProcessor: " + name);
		if (name.equals("Region"))
		{
			final float vals[] = parseFloatArgs(args, 4);
			if (vals == null) { return null; }
			return new RegionProcessor(vals[0], vals[1], vals[2], vals[3]);
		}
		else if (name.equals("Subsample"))
		{
			final int vals[] = parseIntArgs(args, 2);
			if (vals == null) { return null; }
			return new SubsampleProcessor(vals[0], vals[1]);
		}
		else if (name.equals("Format"))
		{
			if (args.size() > 2)
			{
				args.setElementAt("*TOO*MANY*ARGS*" + ((String) args.elementAt(2)), 2);
				return null;
			}
			else if (args.size() < 2)
			{
				args.addElement("*RGB|float|boolean|YUV*");
				return null;
			}
			final String format = (String) args.elementAt(1);
			if (format.equals("RGB"))
			{
				return new FormatProcessor(Frame.TYPE_RGB, 1, Integer.TYPE);
			}
			else if (format.equals("float"))
			{
				return new FormatProcessor(Frame.TYPE_1D, 1, Float.TYPE);
			}
			else if (format.equals("boolean"))
			{
				return new FormatProcessor(Frame.TYPE_1D, 1, Boolean.TYPE);
			}
			else if (format.equals("YUV"))
			{
				return new FormatProcessor(Frame.TYPE_YUV, 3, Float.TYPE);
			}
			else
			{
				args.setElementAt("*RGB|float|boolean|YUV*", 1);
				return null;
			}
		}
		else if (name.equals("Change"))
		{
			final float vals[] = parseFloatArgs(args, 3);
			if (vals == null) { return null; }
			return new ChangeProcessor(vals[0], vals[1], vals[2]);
		}
		else if (name.equals("ChangeGate"))
		{
			final float vals[] = parseFloatArgs(args, 5);
			if (vals == null) { return null; }
			return new ChangeGateProcessor(vals[0], vals[1], vals[2], vals[3], (int) vals[4], true);
		}
		else if (name.equals("BackgroundGate"))
		{
			final float vals[] = parseFloatArgs(args, 6);
			if (vals == null) { return null; }
			return new BackgroundGateProcessor(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
		}
		else if (name.equals("Difference"))
		{
			final float vals[] = parseFloatArgs(args, 3);
			if (vals == null) { return null; }
			return new DifferenceProcessor(vals[0], vals[1], vals[2]);
		}
		else if (name.equals("DifferenceGate"))
		{
			final float vals[] = parseFloatArgs(args, 5);
			if (vals == null) { return null; }
			return new DifferenceGateProcessor(vals[0], vals[1], vals[2], vals[3], (int) vals[4], true);
		}
		else if (name.equals("Threshold"))
		{
			final float vals[] = parseFloatArgs(args, 1);
			if (vals == null) { return null; }
			return new ThresholdProcessor(vals[0], true);
		}
		else if (name.equals("ThresholdBelow"))
		{
			final float vals[] = parseFloatArgs(args, 1);
			if (vals == null) { return null; }
			return new ThresholdProcessor(vals[0], false);
		}
		else if (name.equals("ColourDistance"))
		{
			final float vals[] = parseFloatArgs(args, 6);
			if (vals == null) { return null; }
			return new ColourDistanceProcessor(vals);
		}
		else if (name.equals("Median"))
		{
			final int vals[] = parseIntArgs(args, 1);
			if (vals == null) { return null; }
			return new MedianProcessor(vals[0]);
		}
		else if (name.equals("Mean"))
		{
			final int vals[] = parseIntArgs(args, 1);
			if (vals == null) { return null; }
			return new MeanProcessor(vals[0]);
		}
		else if (name.equals("Drop"))
		{
			final int vals[] = parseIntArgs(args, 1);
			if (vals == null) { return null; }
			return new DropProcessor(vals[0]);
		}
		System.err.println("SimpleVideoProcessor asked for unknown processor: " + name);
		args.setElementAt(	"*Region|Subsample|Drop|Format|Change|ChangeGate|BackgroundGateProcessor|Difference|DifferenceGate|Threshold|ThresholdBelow|Median|Mean|ColourDistance*",
							0);
		return null;
	}
}
