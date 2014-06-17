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
package equip.ect.components.visionframework.frameexporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import equip.ect.ContainerManagerHelper;
import equip.ect.components.visionframework.common.AbstractVideoProcessor;
import equip.ect.components.visionframework.common.Frame;
import equip.ect.components.visionframework.common.FrameProcessor;

/**
 * Export image as URL on trigger (part of Media/Video/Analysis framework; see also
 * Media/Video/Capture/Camera).
 * 
 * <H3>Description</H3> Video processing component which can receive video frames (via sink) and
 * when triggered converts to a JPEG image and exports the resulting URL.
 * 
 * <H3>Installation</H3> See JMFVideoCaptureManager component for video capture requirements.
 * 
 * <H3>Configuration</H3> Configured by setting configuration property. Allows value of the form
 * "Export(0.5)", where the float parameter specifies the rate at which frames will be exported
 * (Hz). Capture of a frame is also caused by setting trigger to true. Note: does not at present
 * allow pre-chains of other audio processing steps; you must use a separate SimpleVideoProcessor.
 * 
 * <H3>Usage</H3> Typically, link to sink from the source property of a JMFVideoCaptureDevice
 * component (created by an JMFVideoCaptureManager component) or from the source property of a
 * chained SimpleVideoProcessor component.
 * <p>
 * View the component web page using the URL in the configUrl property, which includes access to
 * most recent input and processed image(s).
 * 
 * <H3>Technical Details</H3>
 * Part of the visionframework set of components; extends AbstractVideoProcessor. some visualisation
 * facilities. Includes lots of code from the camera component.
 * 
 * @technology JMF video processing
 * @classification Media/Video/Analysis
 * @defaultInputProperty sink
 * @defaultOutputProperty imageUrl
 */
public class VideoFrameExporter extends AbstractVideoProcessor
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
					System.out.println("VideoFrameExport process frame: " + frame);
				}
				newFrame(frame);
				return frame;
			}
			catch (final Exception e)
			{
				System.err.println("ERROR in VideoFrameExport.processFrame: " + e);
				e.printStackTrace(System.err);
			}
			return frame;
		}
	}

	/**
	 * last frmae
	 */
	protected Frame lastFrame;
	/**
	 * last emitted frame
	 */
	protected Frame lastEmittedFrame;
	/**
	 * last emit time
	 */
	protected long lastEmitTime;
	/**
	 * http directory
	 */
	private transient File httpDirectory = ContainerManagerHelper.getHttpDirectory();
	/**
	 * default image filename
	 */
	public static final String DEFAULT_IMAGE_NAME = "picture.jpg";
	/**
	 * image url
	 */
	protected String imageUrl;
	/**
	 * continuous rate
	 */
	protected float continuousRate = 0.0f;
	/**
	 * trigger
	 */
	protected boolean trigger;

	/**
	 * cons
	 */
	public VideoFrameExporter() throws IOException
	{
		super("VideoFrameExporter");
		setConfiguration("Export(" + continuousRate + ")");
		// continuous thread
		new Thread()
		{
			@Override
			public void run()
			{
				continuousThreadFn();
			}
		}.start();
	}

	/**
	 * getter
	 */
	public float getContinuousRate()
	{
		return continuousRate;
	}

	/**
	 * getter
	 */
	public String getImageUrl()
	{
		return imageUrl;
	}

	/**
	 * getter
	 */
	public boolean getTrigger()
	{
		return trigger;
	}

	/**
	 * setter
	 */
	public synchronized void setContinuousRate(final float r)
	{
		final Object old = new Float(continuousRate);
		continuousRate = r;
		propertyChangeListeners.firePropertyChange("continuousRate", old, new Float(r));
	}

	/**
	 * setter - internal
	 */
	public synchronized void setTrigger(final boolean t)
	{
		final Object old = new Boolean(trigger);
		trigger = t;
		propertyChangeListeners.firePropertyChange("trigger", old, new Boolean(t));
		if (t)
		{
			emitLastFrame();
		}
	}

	/**
	 * continuous thread
	 */
	protected void continuousThreadFn()
	{
		try
		{
			while (!stopped)
			{
				synchronized (this)
				{
					final long now = System.currentTimeMillis();
					if (trigger && continuousRate > 0 && (now - lastEmitTime) > 1000 / continuousRate
							&& !(lastFrame == lastEmittedFrame))
					{
						emitLastFrame();
					}
					this.wait(continuousRate > 1 ? (int) (1000 / continuousRate) : 1000);
				}
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR in VideoFrameExported continuous thread: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * emit last frame
	 */
	protected synchronized void emitLastFrame()
	{
		if (lastFrame == null)
		{
			System.err.println("WARNING: no last frame to emit");
			return;
		}
		System.out.println("Emit frame!");
		lastEmittedFrame = lastFrame;
		lastEmitTime = System.currentTimeMillis();
		try
		{
			// File image = picGrabber.grabAndDumpToFile(httpDirectory);
			final BufferedImage bufferedImage = lastEmittedFrame.getBufferedImage();
			final File file = ContainerManagerHelper.createLocalFile(DEFAULT_IMAGE_NAME, httpDirectory);
			final FileOutputStream fos = new FileOutputStream(file);
			final JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fos);
			encoder.encode(bufferedImage);
			fos.close();
			final URL url = ContainerManagerHelper.uploadToHttp(file);
			if (url != null)
			{
				setImageUrl(url.toExternalForm());
			}
			else
			{
				System.err.println("ERROR: upload image to HTTP failed for " + file);
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR emitting image: " + e);
			e.printStackTrace(System.err);
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
		if (!name.equals("Export"))
		{
			System.err.println("VideoFrameExporter asked for unknown processor: " + name);
			args.setElementAt("*Export*", 0);
			return null;
		}
		final float vals[] = parseFloatArgs(args, 1);
		if (vals == null) { return null; }
		setContinuousRate(vals[0]);
		return new MyFrameProcessor();
	}

	/**
	 * handle new incoming frame
	 */
	protected synchronized void newFrame(final Frame f)
	{
		lastFrame = f;
	}

	/**
	 * set - internal
	 */
	protected synchronized void setImageUrl(final String s)
	{
		final Object old = imageUrl;
		this.imageUrl = s;
		propertyChangeListeners.firePropertyChange("imageUrl", old, s);
	}
}
