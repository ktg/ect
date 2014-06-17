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

Created by: Tom Hart (University of Nottingham)
Contributors:
  Tom Hart (University of Nottingham)

 */
/*
 * RTPChannel, $RCSfile: RTPChannel.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:28 $
 *
 * $Author: chaoticgalen $
 * Original Author: Tom Hart
 */

package equip.ect.components.rtpbroadcast;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;

import javax.media.Codec;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.Owned;
import javax.media.Player;
import javax.media.Processor;
import javax.media.control.QualityControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.rtcp.SourceDescription;

public class RTPChannel implements Serializable
{
	class StateListener implements ControllerListener
	{

		@Override
		public void controllerUpdate(final ControllerEvent ce)
		{

			// If there was an error during configure or
			// realize, the processor will be closed
			if (ce instanceof ControllerClosedEvent)
			{
				setFailed();
			}

			// All controller events, send a notification
			// to the waiting thread in waitForState method.
			if (ce instanceof ControllerEvent)
			{
				synchronized (getStateLock())
				{
					getStateLock().notifyAll();
				}
			}
		}
	}

	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private final int DEFAULTPORT = 8888;
	private Processor processor = null;
	private RTPManager rtpMgrs[];
	private DataSource dataOutput = null;

	private MediaLocator locator;
	private String locatorAsString;
	private String destAddress;
	private int portBase;
	private String portsUsed;
	private int numPortsUsed;

	private boolean broadcasting = false;

	private Integer stateLock = new Integer(0);

	private boolean failed = false;

	public RTPChannel()
	{
		this.destAddress = this.defaultAddress();
		this.portBase = DEFAULTPORT;
		this.locator = null;

	}

	public RTPChannel(final int pB)
	{
		this.destAddress = this.defaultAddress();
		this.portBase = pB;
		this.locator = null;

	}

	public RTPChannel(final String locator, final String destAddress, final int pb)
	{
		setMediaLocation(locator);
		this.destAddress = destAddress;
		this.portBase = pb;
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * Whether the channel is currently set to broadcast data. @ return Current state of
	 * broadcasting.
	 */
	public boolean getBroadcasting()
	{
		return this.broadcasting;
	}

	/**
	 * Returns the currectn destination.
	 * 
	 * @return The IP as a String.
	 */
	public String getDestAddress()
	{
		return this.destAddress;
	}

	/**
	 * Returns the path to the file current set to stream.
	 * 
	 * @return The file path.
	 */
	public String getMediaLocation()
	{
		return this.locatorAsString;
	}

	/**
	 * Returns the number of ports currently in use by this channel.
	 * 
	 * @return Number of ports in use.
	 */
	public int getNumPortsUsed()
	{
		return this.numPortsUsed;
	}

	/**
	 * Return the number of the current base port.
	 * 
	 * @return base port number.
	 */
	public int getPortBase()
	{
		return this.portBase;
	}

	/**
	 * Returns a list of the ports currently being used by this channel to broadcast on.
	 * 
	 * @return List of ports as a String.
	 */
	public String getPortsUsed()
	{
		return this.portsUsed;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * Turn on or off the streaming from this channel. When true the channel will stream the media
	 * file, when set to false it will stop.
	 * 
	 * @param state
	 *            Whether to broadcast or not.
	 */
	public void setBroadcasting(final boolean state)
	{
		propertyChangeListeners.firePropertyChange("broadcasting", this.broadcasting, state);
		this.broadcasting = state;
		this.checkState();
	}

	/**
	 * Set the destination address of the stream, either a specific node or a multicast address.
	 * 
	 * @param destAddress
	 *            The IP of the destination machine.
	 */
	public void setDestAddress(final String destAddress)
	{
		if (this.destAddress.equals(destAddress)) { return; // skip if trying to set to same value;
		}

		propertyChangeListeners.firePropertyChange("destAddress", this.destAddress, destAddress);
		this.destAddress = destAddress;
		this.restart();
	}

	/**
	 * Set the path to the media file to be streamed on this channel.
	 * 
	 * @param locator
	 *            The location of the file.
	 */
	public void setMediaLocation(final String locator)
	{
		final MediaLocator tempLoc = new MediaLocator(locator);

		propertyChangeListeners.firePropertyChange("mediaLocation", this.locatorAsString, locator);
		this.locatorAsString = locator;
		this.locator = tempLoc;
		this.restart();

	}

	/**
	 * Set the first port number on which this channel will send data. The first stream will use
	 * this port, any subsequent streams will use port portBase + (<em>n</em>*2). A list of these
	 * ports is given in the {@link RTPChannel#portsUsed portsUsed} variable.
	 * 
	 * @param portBase
	 *            Number of the base port to use.
	 */
	public void setPortBase(final int portBase)
	{
		if (this.portBase == portBase) { return; // skip if trying to set to same value;
		}

		propertyChangeListeners.firePropertyChange("portBase", this.portBase, portBase);
		this.portBase = portBase;
		this.restart();
	}

	/** Stops the broadcast if it's active. */
	public void stop()
	{
		synchronized (this)
		{
			if (processor != null)
			{
				processor.stop();
				processor.close();
				processor = null;
				for (final RTPManager rtpMgr : rtpMgrs)
				{
					rtpMgr.removeTargets("Session ended.");
					rtpMgr.dispose();
				}
			}
			// if (this.getBroadcasting() != false) setBroadcasting(false);
		}
	}

	/*
	 * For JPEG and H263, we know that they only work for particular sizes. So we'll perform extra
	 * checking here to make sure they are of the right sizes.
	 */
	Format checkForVideoSizes(final Format original, final Format supported)
	{

		int width, height;
		final Dimension size = ((VideoFormat) original).getSize();
		final Format jpegFmt = new Format(VideoFormat.JPEG_RTP);
		final Format h263Fmt = new Format(VideoFormat.H263_RTP);

		if (supported.matches(jpegFmt))
		{
			// For JPEG, make sure width and height are divisible by 8.
			width = (size.width % 8 == 0 ? size.width : (size.width / 8) * 8);
			height = (size.height % 8 == 0 ? size.height : (size.height / 8) * 8);
		}
		else if (supported.matches(h263Fmt))
		{
			// For H.263, we only support some specific sizes.
			if (size.width < 128)
			{
				width = 128;
				height = 96;
			}
			else if (size.width < 176)
			{
				width = 176;
				height = 144;
			}
			else
			{
				width = 352;
				height = 288;
			}
		}
		else
		{
			return supported;
		}

		return (new VideoFormat(null, new Dimension(width, height), Format.NOT_SPECIFIED, null, Format.NOT_SPECIFIED))
				.intersects(supported);
	}

	Integer getStateLock()
	{
		return stateLock;
	}

	void setFailed()
	{
		failed = true;
	}

	/*
	 * Setting the encoding quality to the specified value on the JPEG encoder. 0.5 is a good
	 * default.
	 */
	void setJPEGQuality(final Player p, final float val)
	{

		final Control cs[] = p.getControls();
		QualityControl qc = null;
		final VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);

		// Loop through the controls to find the Quality control for
		// the JPEG encoder.
		for (final Control element : cs)
		{

			if (element instanceof QualityControl && element instanceof Owned)
			{
				final Object owner = ((Owned) element).getOwner();

				// Check to see if the owner is a Codec.
				// Then check for the output format.
				if (owner instanceof Codec)
				{
					final Format fmts[] = ((Codec) owner).getSupportedOutputFormats(null);
					for (final Format fmt : fmts)
					{
						if (fmt.matches(jpegFmt))
						{
							qc = (QualityControl) element;
							qc.setQuality(val);
							System.err.println("- Setting quality to " + val + " on " + qc);
							break;
						}
					}
				}
				if (qc != null)
				{
					break;
				}
			}
		}
	}

	/* Stuff to handle the processor's state changes. */

	/** Check which state the channel is in a stop/start stream as appropriate. */
	private boolean checkState()
	{
		if (this.broadcasting == true)
		{
			this.restart();
		}
		else if (this.broadcasting == false)
		{
			this.stop();
		}
		return this.broadcasting;
	}

	private String createProcessor()
	{
		if (locator == null) { return "Locator is null"; }

		DataSource ds;

		// Set up our datasource...
		try
		{
			ds = javax.media.Manager.createDataSource(locator);
		}
		catch (final Exception e)
		{
			return "Couldn't create DataSource, error: " + e.getMessage();
		}

		// Now build our processor from it...
		try
		{
			processor = javax.media.Manager.createProcessor(ds);
		}
		catch (final NoProcessorException e)
		{
			return "Couldn't create processor, error: " + e.getMessage();
		}
		catch (final IOException e)
		{
			return "IOException creating processor, error: " + e.getMessage();
		}

		// Wait for it to configure...
		boolean result = waitForState(processor, Processor.Configured);
		if (result == false) { return "Couldn't configure processor"; }

		// Get each of the tracks from the processor
		final TrackControl[] tracks = processor.getTrackControls();

		// Check we have at least one track otherwise what're we trying to broadcast!?
		if (tracks == null || tracks.length < 1) { return "Couldn't find tracks in processor"; }

		// Set the output content descriptor to RAW_RTP
		// This will limit the supported formats reported from
		// Track.getSupportedFormats to only valid RTP formats.
		final ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
		processor.setContentDescriptor(cd);

		Format supported[];
		Format chosen;
		boolean atLeastOneTrack = false;

		// Set up each track
		// NB first is usually our audio, second is usually our video
		for (int i = 0; i < tracks.length; i++)
		{
			final Format format = tracks[i].getFormat();
			if (tracks[i].isEnabled())
			{

				supported = tracks[i].getSupportedFormats();

				if (supported.length > 0)
				{
					if (supported[0] instanceof VideoFormat)
					{
						chosen = checkForVideoSizes(tracks[i].getFormat(), supported[0]);
					}
					else
					{
						chosen = supported[0];
					}

					tracks[i].setFormat(chosen);
					System.err.println("Track " + i + " is set to transmit as:");
					System.err.println("  " + chosen);
					atLeastOneTrack = true;
				}
				else
				{
					tracks[i].setEnabled(false);
				}
			}
			else
			{
				tracks[i].setEnabled(false);
			}
		}

		if (!atLeastOneTrack) { return "Couldn't set any of the tracks to a valid RTP format"; }

		// Realize the processor. This will internally create a flow
		// graph and attempt to create an output datasource for JPEG/RTP
		// audio frames.
		result = waitForState(processor, Controller.Realized);
		if (result == false) { return "Couldn't realize processor"; }

		// Set the image (JPEG) quality to .5.
		setJPEGQuality(processor, 0.5f);

		// Get the output data source of the processor
		dataOutput = processor.getDataOutput();

		return null;
	}

	private String createTransmitter()
	{

		final PushBufferDataSource pbds = (PushBufferDataSource) dataOutput;
		final PushBufferStream pbss[] = pbds.getStreams();

		// create managers for each of the streams in our file
		// typically one or two
		rtpMgrs = new RTPManager[pbss.length];
		SessionAddress localAddr, destAddr;
		InetAddress ipAddr;
		SendStream sendStream;
		int port;
		final SourceDescription srcDesList[];
		portsUsed = "";
		numPortsUsed = 0;

		for (int i = 0; i < pbss.length; i++)
		{
			try
			{
				rtpMgrs[i] = RTPManager.newInstance();

				// get next even num port to send on
				port = portBase + 2 * i;
				ipAddr = InetAddress.getByName(destAddress);

				// Set the address/port we're sending from
				// Includes savage port hack to let you send and receive on same machine,
				// will be fixed later!
				localAddr = new SessionAddress(InetAddress.getLocalHost(), port - 100);
				// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				// set the address/port we're broadcasting too
				destAddr = new SessionAddress(ipAddr, port);

				rtpMgrs[i].initialize(localAddr);
				rtpMgrs[i].addTarget(destAddr);

				portsUsed += port + ", ";
				numPortsUsed++;

				// System.err.println( "Created RTP session: " + destAddress + " " + port);

				// start the stream running
				sendStream = rtpMgrs[i].createSendStream(dataOutput, i);
				sendStream.start();
			}
			catch (final Exception e)
			{
				return e.getMessage();
			}
		}
		propertyChangeListeners.firePropertyChange("portsUsed", null, portsUsed);
		propertyChangeListeners.firePropertyChange("numPortsUsed", 0, numPortsUsed);

		return null;
	}

	/**
	 * Set up a default destination address. Gets the local IP address and sets the last byte to
	 * 255, i.e. a multicast address.
	 * 
	 * @return local multicast address as a String (or, on error, a default value of 192.168.1.255).
	 */
	private String defaultAddress()
	{
		String a = "192.168.1.255";
		try
		{
			a = (InetAddress.getLocalHost()).getHostAddress();
		}
		catch (final Exception e)
		{
		}

		String b = a.substring(0, a.lastIndexOf('.'));
		b += ".255";
		return b;
	}

	/**
	 * Configures the stream when any changes take place, preparing it for broadcast (starts
	 * broadcasting if flagged as true).
	 */
	private boolean restart()
	{
		this.stop();

		final String err = this.setup();
		if (err != null)
		{
			propertyChangeListeners.firePropertyChange("mediaLocation", this.locatorAsString, err);
			this.locatorAsString = err;
			return false;
		}
		if (this.broadcasting == true)
		{
			this.start();
		}
		return this.broadcasting;
	}

	/**
	 * Starts the broadcast. Returns null if transmission started ok. Otherwise it returns a string
	 * with the reason why the setup failed.
	 */
	private synchronized String setup()
	{
		String result;

		// Create a processor for the specified media locator
		// and program it to output JPEG/RTP
		result = createProcessor();
		if (result != null) { return result; }

		// Create an RTP session to transmit the output of the
		// processor to the specified IP address and port no.
		result = createTransmitter();
		if (result != null)
		{
			processor.close();
			processor = null;
			return result;
		}
		// processor.start();
		return null;
	}

	/* End of stuff to handle the processor's state changes. */

	private synchronized void start()
	{
		processor.start();
	}

	private synchronized boolean waitForState(final Processor p, final int state)
	{
		p.addControllerListener(new StateListener());
		failed = false;

		// Call the required method on the processor
		if (state == Processor.Configured)
		{
			p.configure();
		}
		else if (state == Controller.Realized)
		{
			p.realize();
		}

		// Wait until we get an event that confirms the
		// success of the method, or a failure event.
		// See StateListener inner class
		while (p.getState() < state && !failed)
		{
			synchronized (getStateLock())
			{
				try
				{
					getStateLock().wait();
				}
				catch (final InterruptedException ie)
				{
					return false;
				}
			}
		}

		if (failed)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

}
