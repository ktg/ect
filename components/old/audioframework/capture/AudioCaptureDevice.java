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
package equip.ect.components.audioframework.capture;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.ResourceUnavailableException;
import javax.media.control.FormatControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;

import equip.ect.components.audioframework.common.Frame;
import equip.ect.components.audioframework.common.FrameHandler;
import equip.ect.components.audioframework.common.PushFrameSource;

/**
 * Represents a single JMF video capture device. Created dynamically by {@link AudioCaptureManager}.
 */
public class AudioCaptureDevice implements Serializable
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
					System.out.println("AudioCaptureDevice frame " + frameNo + "!");

					final boolean ok = true;

					final Buffer b = new Buffer();
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
					if (ok)
					{
						final AudioFormat audioFormat = (AudioFormat) b.getFormat();
						final double sampleRate = audioFormat.getSampleRate();
						final int channels = audioFormat.getChannels();
						// probably byte[]
						if (!(b.getData() instanceof byte[]) || audioFormat.getSampleSizeInBits() != 16)
						{
							System.err.println("Sorry - cannot deal with format " + audioFormat + " (class "
									+ audioFormat.getDataType().getName());
						}
						else
						{
							final byte bdata[] = (byte[]) b.getData();
							final short data[] = new short[bdata.length / 2];
							final int offset = audioFormat.getSigned() == AudioFormat.SIGNED ? 0 : -32768;
							if (audioFormat.getEndian() == AudioFormat.BIG_ENDIAN)
							{
								for (int i = 0; i < data.length; i++)
								{
									data[i] = (short) (((bdata[i + i] << 8) & 0xff00 | ((bdata[i + i + 1]) & 0xff)) + offset);
								}
							}
							else
							{
								for (int i = 0; i < data.length; i++)
								{
									data[i] = (short) (((bdata[i + i + 1] << 8) & 0xff00 | ((bdata[i + i]) & 0xff)) + offset);
								}
							}
							final Frame frame = new Frame(sampleRate, channels, data);
							// ....!!
							final AudioCaptureDevice h = handler;
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
							System.out.println("- AudioCaptureDevice frame " + frameNo + " handled");
							// little pause
							Thread.sleep(10);
						}
					}
					else
					{
						System.err.println("ERROR handling AudioCaptureDevice frame");
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
		protected boolean debug = false;
		/**
		 * map
		 */
		static HashMap devices = new HashMap();

		/**
		 * get by locator
		 */
		static synchronized RealCaptureDevice get(final AudioCaptureDevice handler, final MediaLocator locator)
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
		AudioCaptureDevice handler;
		/**
		 * status
		 */
		protected String status = "new";
		/**
		 * capture device
		 */
		protected CaptureDevice cap;
		/**
		 * final JMF output format
		 */
		protected AudioFormat outputFormat;
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
			FormatControl[] fconts = cap.getFormatControls();
			outputFormat = null;
			if (fconts == null || fconts.length == 0)
			{
				if (cap instanceof FormatControl)
				{
					System.out.println("Note: device implements FormatControl");
					fconts = new FormatControl[] { (FormatControl) cap };
				}
				else
				{
					System.err.println("Warning: device has no FormatControl");
				}
			}

			double bestOutputFormatScore = 0;
			final int i = 0;
			{
				// first format control only
				if (debug)
				{
					System.out.println("- format control " + fconts[i]);
				}
				final Format[] formats = fconts[i].getSupportedFormats();
				int j;
				final int bestWidth = 0;
				for (j = 0; j < formats.length; j++)
				{
					final double score = scoreFormat(formats[j]);
					if (debug)
					{
						System.out.println("- " + formats[j] + " datatype " + formats[j].getDataType().getName()
								+ " scores " + score);
					}
					if (score > bestOutputFormatScore)
					{
						bestOutputFormatScore = score;
						outputFormat = (AudioFormat) formats[j];
						if (debug)
						{
							System.out.println("-- best so far");
						}
					}
				}
				if (debug)
				{
					System.out.println("Best format: " + outputFormat);
				}
				fconts[i].setFormat(outputFormat);
				// System.out.println("  supports "+formats[j]);
			}
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
	protected boolean debug = false;
	/**
	 * real device
	 */
	RealCaptureDevice device;
	/**
	 * heuristics for choosing format
	 */
	static final int TARGET_CHANNELS = 1;
	/**
	 * heuristics for choosing format
	 */
	static final double TARGET_SAMPLE_RATE = 22050;
	/**
	 * heuristics for choosing format
	 */
	static final double TARGET_BITS_PER_SAMPLE = 16;

	/**
	 * heuristics for choosing format
	 */
	static double scoreFormat(final Format f)
	{
		double score = 0;
		if (f instanceof AudioFormat)
		{
			final AudioFormat audiof = (AudioFormat) f;
			if (audiof.getDataType().equals(Float.class))
			{
				score = score + 2;
			}
			if (audiof.getDataType().equals(Short.class))
			{
				score = score + 1;
			}
			score = score + (3 - Math.abs(audiof.getSampleRate() - TARGET_SAMPLE_RATE) / TARGET_SAMPLE_RATE);
			score = score + (3 - Math.abs(audiof.getChannels() - TARGET_CHANNELS));
			score = score + (16 - Math.abs(audiof.getSampleSizeInBits() - TARGET_BITS_PER_SAMPLE));
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
	 * Default constructor - called by AudioCaptureManager.<br>
	 */
	public AudioCaptureDevice(final MediaLocator locator)
	{
		this.locator = locator;
		device = RealCaptureDevice.get(this, locator);
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
