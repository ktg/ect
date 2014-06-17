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
package equip.ect.components.visionframework.capture;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.PlugIn;
import javax.media.PlugInManager;
import javax.media.ResourceUnavailableException;
import javax.media.control.FormatControl;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;

import equip.ect.components.visionframework.common.Frame;
import equip.ect.components.visionframework.common.FrameHandler;
import equip.ect.components.visionframework.common.PushFrameSource;

/**
 * Represents a single JMF video capture device. Created dynamically by
 * {@link JMFVideoCaptureManager}.
 */
public class JMFVideoCaptureDevice implements Serializable
{
	/**
	 * PushFrameSource class to expose
	 */
	protected class MySource implements PushFrameSource
	{
		/**
		 * register a {@link FrameHandler}
		 */
		@Override
		public void registerFrameHandler(final FrameHandler handler)
		{
			intRegisterFrameHandler(handler);
		}

		/**
		 * unregister
		 */
		@Override
		public void unregisterFrameHandler(final FrameHandler handler)
		{
			intUnregisterFrameHandler(handler);
		}
	}

	/**
	 * nested class for non-stop capture device
	 */
	protected static class RealCaptureDevice
	{
		/**
		 * transfer handler - new frame from JMF
		 */
		protected class MyTransferHandler implements BufferTransferHandler
		{
			@Override
			public void transferData(final PushBufferStream stream)
			{
				try
				{
					frameNo++;
					// System.out.println("JMFVideoCaptureDevice frame "+frameNo+"!");

					boolean ok = true;

					Buffer b = new Buffer();
					stream.read(b);
					if (false)
					{
						System.out.println("Read buffer " + b);
						System.out.println("  data = " + b.getData());
						System.out.println("  duration = " + b.getDuration());
						final int f = b.getFlags();
						System.out.println("  Flags = " + f + " " + ((f & Buffer.FLAG_DISCARD) != 0 ? "DISCARD " : "")
								+ ((f & Buffer.FLAG_EOM) != 0 ? "EOM " : "")
								+ ((f & Buffer.FLAG_FLUSH) != 0 ? "FLUSH " : "")
								+ ((f & Buffer.FLAG_KEY_FRAME) != 0 ? "KEY_FRAME " : "")
								+ ((f & Buffer.FLAG_NO_DROP) != 0 ? "NO_DROP " : "")
								+ ((f & Buffer.FLAG_NO_SYNC) != 0 ? "NO_SYNC " : "")
								+ ((f & Buffer.FLAG_NO_WAIT) != 0 ? "NO_WAIT " : "")
								+ ((f & Buffer.FLAG_RELATIVE_TIME) != 0 ? "RELATIVE_TIME " : "")
								+ ((f & Buffer.FLAG_RTP_MARKER) != 0 ? "RTP_MARKER " : "")
								+ ((f & Buffer.FLAG_SID) != 0 ? "SID " : "")
								+ ((f & Buffer.FLAG_SILENCE) != 0 ? "SILENCE " : "")
								+ ((f & Buffer.FLAG_SYSTEM_MARKER) != 0 ? "SYSTEM_MARKER " : "")
								+ ((f & Buffer.FLAG_SYSTEM_TIME) != 0 ? "SYSTEM_TIME " : ""));
						System.out.println("  format = " + b.getFormat());
						System.out.println("  header = " + b.getHeader());
						System.out.println("  length = " + b.getLength());
						System.out.println("  sequence number = " + b.getSequenceNumber());
						System.out.println("  timestamp = " + b.getTimeStamp());
					}
					if (rgbCodec != null)
					{
						final Buffer b2 = b;
						b = new Buffer();
						b.setFlags(0);
						if (rgbCodec.process(b2, b) != PlugIn.BUFFER_PROCESSED_OK)
						{
							System.err.println("ERROR doing transcode to rgb");
							ok = false;
						}
						else
						{
							if (false)
							{
								System.out.println("RGB buffer " + b);
								System.out.println("  data = " + b.getData());
								System.out.println("  duration = " + b.getDuration());
								final int f = b.getFlags();
								System.out.println("  Flags = " + f + " "
										+ ((f & Buffer.FLAG_DISCARD) != 0 ? "DISCARD " : "")
										+ ((f & Buffer.FLAG_EOM) != 0 ? "EOM " : "")
										+ ((f & Buffer.FLAG_FLUSH) != 0 ? "FLUSH " : "")
										+ ((f & Buffer.FLAG_KEY_FRAME) != 0 ? "KEY_FRAME " : "")
										+ ((f & Buffer.FLAG_NO_DROP) != 0 ? "NO_DROP " : "")
										+ ((f & Buffer.FLAG_NO_SYNC) != 0 ? "NO_SYNC " : "")
										+ ((f & Buffer.FLAG_NO_WAIT) != 0 ? "NO_WAIT " : "")
										+ ((f & Buffer.FLAG_RELATIVE_TIME) != 0 ? "RELATIVE_TIME " : "")
										+ ((f & Buffer.FLAG_RTP_MARKER) != 0 ? "RTP_MARKER " : "")
										+ ((f & Buffer.FLAG_SID) != 0 ? "SID " : "")
										+ ((f & Buffer.FLAG_SILENCE) != 0 ? "SILENCE " : "")
										+ ((f & Buffer.FLAG_SYSTEM_MARKER) != 0 ? "SYSTEM_MARKER " : "")
										+ ((f & Buffer.FLAG_SYSTEM_TIME) != 0 ? "SYSTEM_TIME " : ""));
								System.out.println("  format = " + b.getFormat());
								System.out.println("  header = " + b.getHeader());
								System.out.println("  length = " + b.getLength());
								System.out.println("  sequence number = " + b.getSequenceNumber());
								System.out.println("  timestamp = " + b.getTimeStamp());
							}
						}
					}
					if (ok)
					{
						// we are sure it will be packed RGB
						final Dimension size = rgbOutputFormat.getSize();
						final Frame frame = new Frame(Frame.TYPE_RGB, 1, size.width, size.height, b.getOffset(),
								rgbOutputFormat.getPixelStride(), rgbOutputFormat.getLineStride(), b.getData());
						// ....!!
						final JMFVideoCaptureDevice h = handler;
						if (h != null)
						{
							try
							{
								h.handleFrame(frame);
							}
							catch (final Exception e)
							{
								System.err.println("ERROR handling frame: " + e);
								e.printStackTrace(System.err);
							}
						}
						// System.out.println("- JMFVideoCaptureDevice frame "+frameNo+" handled");
						// little pause
						Thread.sleep(delayMs);
					}
					else
					{
						System.err.println("ERROR handling JMFVideoCaptureDevice frame");
					}
				}
				catch (final Exception e)
				{
					System.err.println("ERROR reading buffer: " + e);
					e.printStackTrace(System.err);
				}
			}
		}

		/**
		 * release
		 */
		static synchronized void release(final RealCaptureDevice dev)
		{
			dev.handler = null;
		}

		/**
		 * debug
		 */
		protected boolean debug = true;
		/**
		 * map
		 */
		static HashMap devices = new HashMap();

		/**
		 * get by locator
		 */
		static synchronized RealCaptureDevice get(final JMFVideoCaptureDevice handler, final MediaLocator locator)
		{
			RealCaptureDevice dev = (RealCaptureDevice) devices.get(locator.toString());
			if (dev == null)
			{
				dev = new RealCaptureDevice(locator);
				devices.put(locator.toString(), dev);
			}
			if (dev.handler != null)
			{
				System.err.println("Capture device " + locator + " already in use");
				return null;
			}
			dev.handler = handler;
			return dev;
		}

		/**
		 * handler
		 */
		JMFVideoCaptureDevice handler;
		/**
		 * status
		 */
		protected String status = "new";
		/**
		 * capture device
		 */
		protected CaptureDevice cap;
		/**
		 * rgb code - may be null if not required
		 */
		protected Codec rgbCodec;
		/**
		 * final JMF output format
		 */
		protected RGBFormat rgbOutputFormat;
		/**
		 * captureing
		 */
		protected boolean capturing = false;
		/**
		 * frame no.
		 */
		protected int frameNo = 0;
		/**
		 * media locator
		 */
		protected MediaLocator locator;

		/**
		 * cons
		 */
		RealCaptureDevice(final MediaLocator locator)
		{
			// set ID for this component
			this.locator = locator;
			try
			{
				initDevice();
				initFormat();
				// just to test
				// startCapture();
				if (rgbCodec != null)
				{
					rgbCodec.open();
				}
				cap.connect();

				status = "OK";
			}
			catch (final Exception e)
			{
				System.err.println("ERROR creating JMF capture device: " + e);
				e.printStackTrace(System.err);
				status = "ERROR: " + e;
			}
		}

		/**
		 * create capture device and any initial codec to get a suitable format
		 */
		protected void initDevice() throws IOException, NoDataSourceException
		{
			System.out.println("Get capture device " + locator);
			cap = (CaptureDevice) Manager.createDataSource(locator);

			if (!(cap instanceof PushBufferDataSource))
			{
				System.err.println("Unsupported capture device type (not PushBufferDataSource) ("
						+ cap.getClass().getName() + ")");
				status = "Unsupported capture device type (not PushBufferDataSource) (" + cap.getClass().getName()
						+ ")";
				return;
			}
			System.out.println("- PushBufferDataSource");
			final PushBufferStream streams[] = ((PushBufferDataSource) cap).getStreams();
			System.out.println("  " + streams.length + " streams");
			int i;
			for (i = 0; i < streams.length; i++)
			{
				streams[i].setTransferHandler(new MyTransferHandler());
			}
		}

		/**
		 * try to set format for preferred size and format
		 */
		protected void initFormat()
		{
			// not sure what multiple format controls are for
			final FormatControl[] fconts = cap.getFormatControls();
			final RGBFormat bestFormat = null;
			Format bestOutputFormat = null;
			int bestOutputFormatScore = 0;
			int i = 0;
			{
				// first format control only
				if (debug)
				{
					System.out.println("- format control " + fconts[i]);
				}
				final Format[] formats = fconts[i].getSupportedFormats();
				int j;
				int bestWidth = 0;
				for (j = 0; j < formats.length; j++)
				{
					final Dimension size = ((VideoFormat) formats[j]).getSize();
					if (debug)
					{
						System.out.println("output " + size.width + "x" + size.height + " format: " + formats[j]
								+ " (class " + formats[j].getClass().getName() + ")");
						System.out.println("Frame Rate: " + ((VideoFormat) formats[j]).getFrameRate());
					}
					final int score = scoreFormat(formats[j]);
					if (debug)
					{
						System.out.println("- scores " + score);
					}
					if (score > bestOutputFormatScore
							|| (score == bestOutputFormatScore && Math.abs(bestWidth - targetWidth) > Math
									.abs(size.width - targetWidth)))
					{
						bestOutputFormatScore = score;
						bestOutputFormat = formats[j];
						bestWidth = size.width;
						if (debug)
						{
							System.out.println("-- best so far");
						}
					}
				}
				if (debug)
				{
					System.out.println("Best format: " + bestOutputFormat);
				}
				bestOutputFormat = new RGBFormat(((RGBFormat) bestOutputFormat).getSize(),
						((RGBFormat) bestOutputFormat).getMaxDataLength(),
						((RGBFormat) bestOutputFormat).getDataType(), 24.0f,
						((RGBFormat) bestOutputFormat).getBitsPerPixel(), ((RGBFormat) bestOutputFormat).getRedMask(),
						((RGBFormat) bestOutputFormat).getGreenMask(), ((RGBFormat) bestOutputFormat).getBlueMask(),
						((RGBFormat) bestOutputFormat).getPixelStride(),
						((RGBFormat) bestOutputFormat).getLineStride(), ((RGBFormat) bestOutputFormat).getFlipped(),
						((RGBFormat) bestOutputFormat).getEndian());
				fconts[i].setFormat(bestOutputFormat);
				// System.out.println("  supports "+formats[j]);
			}

			rgbCodec = null;
			rgbOutputFormat = null;
			if (bestOutputFormatScore < REQUIRED_FORMAT_SCORE)
			{
				if (debug)
				{
					System.out.println("To RGB Codecs for " + bestOutputFormat);
				}
				final Vector codecs = PlugInManager.getPlugInList(	bestOutputFormat, new RGBFormat(),
																	PlugInManager.CODEC);
				bestOutputFormatScore = 0;
				for (i = 0; i < codecs.size(); i++)
				{
					final String codecName = (String) codecs.elementAt(i);
					if (debug)
					{
						System.out.print("  " + codecName);
					}
					final ClassLoader cl = this.getClass().getClassLoader();
					try
					{
						final Codec codec = (Codec) cl.loadClass(codecName).getConstructor(null).newInstance(null);
						if (debug)
						{
							System.out.print("Codec " + codec + " (class ");
							System.out.println(codec.getClass().getName()); // ClassInfo.printClassInfo(codec);
							System.out.println(")");
						}
						codec.setInputFormat(bestOutputFormat);
						final Format[] outfs = codec.getSupportedOutputFormats(bestOutputFormat);
						if (debug)
						{
							System.out.println("Supported output formats (" + outfs.length + ")");
						}
						int j;
						for (j = 0; j < outfs.length; j++)
						{
							if (debug)
							{
								System.out.println("output format: " + outfs[j] + " (class "
										+ outfs[j].getClass().getName() + ")");
							}
							if (outfs[j] instanceof RGBFormat)
							{
								int score = 0;
								final RGBFormat rgbf = (RGBFormat) outfs[j];
								if (rgbf.getBitsPerPixel() == TARGET_BPP && rgbf.getRedMask() == TARGET_RED_MASK
										&& rgbf.getFlipped() == 0)
								{
									score = 1;
								}
								if (score > bestOutputFormatScore)
								{
									rgbOutputFormat = rgbf;
									rgbCodec = codec;
									bestOutputFormatScore = score;
								}
							}
						}
					}
					catch (final Exception e)
					{
						System.err.println("ERROR instantiating codec2: " + e);
						e.printStackTrace(System.err);
					}
				}
				rgbCodec.setOutputFormat(rgbOutputFormat);
			}
			else
			{
				rgbOutputFormat = (RGBFormat) bestOutputFormat;
			}

			System.out.println("Output format " + bestOutputFormat);
			System.out.println("RGB Output format " + rgbOutputFormat);
		}

		/**
		 * start
		 */
		protected synchronized void startCapture() throws IOException, ResourceUnavailableException
		{
			if (capturing) { return; }
			System.err.println("Starting capture " + locator);
			System.out.println("Start Device = " + cap + " (class " + cap.getClass().getName() + ")");
			capturing = true;
			cap.start();
		}

		/**
		 * stop capture
		 */
		protected synchronized void stopCapture() throws IOException
		{
			if (!capturing) { return; }
			System.err.println("Stopping capture " + locator);
			capturing = false;
			cap.stop();
		}
	}

	/**
	 * debug
	 */
	protected boolean debug = true;
	/**
	 * real device
	 */
	RealCaptureDevice device;
	/**
	 * frame delay ms
	 */
	protected static int delayMs = 0;
	/**
	 * heuristics for choosing format
	 */
	static final int TARGET_BPP = 32; // 24
	/**
	 * heuristics for choosing format - red in low byte; should match Format.RED_SHIFT
	 */
	static final int TARGET_RED_MASK = 0xff0000; // 1
	/**
	 * heuristics for choosing format
	 */
	static final int targetWidth = 320;
	// static final int targetWidth = 640;
	/**
	 * heuristics for choosing format
	 */
	static final int REQUIRED_FORMAT_SCORE = 7;

	/**
	 * heuristics for choosing format
	 */
	static int scoreFormat(final Format f)
	{
		int score = 0;
		if (f instanceof RGBFormat)
		{
			score = 4;
			final RGBFormat rgbf = (RGBFormat) f;
			System.out.println("Red Mask: " + rgbf.getRedMask());
			if (rgbf.getBitsPerPixel() == 24)
			{
				score = 5; // 5;
				// if (rgbf.getRedMask()==TARGET_RED_MASK)
				// score = 7;
			}
			else if (rgbf.getBitsPerPixel() == 32)
			{
				score = 6;
				if (rgbf.getRedMask() == TARGET_RED_MASK)
				{
					score = 7;
				}
			}
		}
		else if (f instanceof YUVFormat)
		{
			score = 3;
		}
		else if (f instanceof JPEGFormat)
		{
			score = 2;
		}
		else if (f instanceof VideoFormat)
		{
			score = 1;
		}
		return score;
	}

	/**
	 * handlers ({@link FrameHandler})
	 */
	protected Vector handlers = new Vector();
	/**
	 * source property
	 */
	protected MySource source = new MySource();
	/**
	 * media locator
	 */
	protected MediaLocator locator;
	/**
	 * Property Change
	 */
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * Default constructor - called by JMFVideoCaptureManager.<br>
	 */
	public JMFVideoCaptureDevice(final MediaLocator locator)
	{
		this.locator = locator;
		device = RealCaptureDevice.get(this, locator);

		// AXH test
		try
		{
			startCapture();
		}
		catch (final IOException ioe)
		{
			System.out.println("Failed to start capture.");
		}
		catch (final javax.media.ResourceUnavailableException ioe)
		{
			System.out.println("Failed to start capture.");
		}
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * get
	 */
	public int getDelayMs()
	{
		return delayMs;
	}

	/**
	 * media locator getter
	 */
	public synchronized String getLocator()
	{
		return locator.toString();
	}

	/**
	 * persistent child id - string
	 */
	public String getPersistentChild()
	{
		return getLocator().toString();
	}

	/**
	 * source getter
	 */
	public synchronized PushFrameSource getSource()
	{
		return source;
	}

	/**
	 * status getter
	 */
	public String getStatus()
	{
		return device == null ? "device not available" : device.status;
	}

	/**
	 * register a {@link FrameHandler}
	 */
	public synchronized void intRegisterFrameHandler(final FrameHandler handler)
	{

		// synchronized(handlers)
		{
			handlers.addElement(handler);
			if (handlers.size() == 1)
			{
				try
				{
					startCapture();
				}
				catch (final Exception e)
				{
					System.err.println("ERROR starting capture: " + e);
					e.printStackTrace(System.err);
				}
			}
		}
	}

	/**
	 * unregister
	 */
	public synchronized void intUnregisterFrameHandler(final FrameHandler handler)
	{
		// synchronized(handlers)
		{
			// note could be in handleFrame callback
			final int i = handlers.indexOf(handler);
			if (i >= 0)
			{
				handlers.removeElementAt(i);
				if (handlers.size() == 0)
				{
					try
					{
						stopCapture();
					}
					catch (final Exception e)
					{
						System.err.println("ERROR stopping capture: " + e);
						e.printStackTrace(System.err);
					}
				}
			}
			else
			{
				System.out.println("NOTE: unregisterFrameHandler for unknown handler " + handler);
			}
		}
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * set
	 */
	public void setDelayMs(final int d)
	{
		final int od = delayMs;
		delayMs = d;
		propertyChangeListeners.firePropertyChange("delayMs", new Integer(od), new Integer(d));
	}

	/**
	 * stop
	 */
	public synchronized void stop()
	{
		propertyChangeListeners.firePropertyChange("source", source, null);
		source = null;
		// stopped = true;
		if (handlers.size() > 0)
		{
			handlers.removeAllElements();
			try
			{
				stopCapture();
				// ....
			}
			catch (final IOException e)
			{
				System.err.println("ERROR stopping capture " + locator + ": " + e);
				e.printStackTrace(System.err);
			}
		}
		if (device != null)
		{
			RealCaptureDevice.release(device);
		}
		device = null;
	}

	/**
	 * a new frame; NB actual pushing of frames should be outside critical section
	 */
	protected void handleFrame(final Frame frame)
	{
		FrameHandler hs[] = null;
		synchronized (this)
		{
			hs = (FrameHandler[]) handlers.toArray(new FrameHandler[handlers.size()]);
		}
		// pass on to registered handlers
		// synchronized(handlers)
		{
			for (final FrameHandler handler : hs)
			{
				boolean retain = false;
				try
				{
					// no lock during actual handling chain
					retain = handler.handleFrame(frame);
					if (retain == false)
					{
						System.out.println("NOTE: frame handler " + handler + " requests removal");
					}
				}
				catch (final Exception e)
				{
					System.err.println("ERROR in frame handler: " + e);
					e.printStackTrace(System.err);
					retain = false;
				}
				if (!retain)
				{
					synchronized (this)
					{
						handlers.removeElement(handler);
						if (handlers.size() == 0)
						{
							try
							{
								stopCapture();
							}
							catch (final Exception e)
							{
								System.err.println("ERROR stopping capture: " + e);
								e.printStackTrace(System.err);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * start
	 */
	protected synchronized void startCapture() throws IOException, ResourceUnavailableException
	{
		if (device != null)
		{
			device.startCapture();
		}
	}

	/**
	 * stop capture
	 */
	protected synchronized void stopCapture() throws IOException
	{
		if (device != null)
		{
			device.stopCapture();
		}
	}
}
