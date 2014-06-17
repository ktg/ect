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
 * RTPPlayer, $RCSfile: RTPPlayer.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:28 $
 *
 * $Author: chaoticgalen $
 * Original Author: Tom Hart
 */

package equip.ect.components.rtpviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.net.InetAddress;

import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.control.BufferControl;
import javax.media.protocol.DataSource;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;

/** Provides a window to view streamed RTP media */

public class RTPPlayer implements ReceiveStreamListener, SessionListener, ControllerListener, Serializable
{

	/**
	 * GUI class for the rendering the streamed media to the viewer frame.
	 */
	class PlayerPanel extends Panel
	{

		Component vc, cc;

		PlayerPanel(final Player p)
		{
			setLayout(new BorderLayout());
			if ((vc = p.getVisualComponent()) != null)
			{
				add("Center", vc);
			}
			if ((cc = p.getControlPanelComponent()) != null)
			{
				add("South", cc);
			}
		}

		@Override
		public Dimension getPreferredSize()
		{
			int w = 0, h = 0;
			if (vc != null)
			{
				final Dimension size = vc.getPreferredSize();
				w = size.width;
				h = size.height;
			}
			if (cc != null)
			{
				final Dimension size = cc.getPreferredSize();
				if (w == 0)
				{
					w = size.width;
				}
				h += size.height;
			}
			if (w < 160)
			{
				w = 160;
			}
			return new Dimension(w, h);
		}
	}

	/** GUI class creating a viewer window. */
	class PlayerWindow extends Frame
	{

		Player player;
		ReceiveStream stream;

		PlayerWindow(final Player p, final ReceiveStream strm)
		{
			player = p;
			stream = strm;
		}

		@Override
		public void addNotify()
		{
			super.addNotify();
			pack();
		}

		public void close()
		{
			player.close();
			setVisible(false);
			dispose();
		}

		public void initialize()
		{
			add(new PlayerPanel(player));
		}
	}

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	String address = null;
	int port;

	int ttl = 1;
	boolean view = false;

	RTPManager mgr = null;
	PlayerWindow pWindow = null;

	boolean dataReceived = false;

	Object dataSync = new Object();

	/** Create new RTPPlayer object */
	public RTPPlayer()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/** ControllerListener for the player window. */
	@Override
	public synchronized void controllerUpdate(final ControllerEvent ce)
	{

		final Player p = (Player) ce.getSourceController();

		if (p == null) { return; }

		// Get this when the internal players are realized.
		if (ce instanceof RealizeCompleteEvent)
		{
			if (pWindow == null)
			{
				System.err.println("Error");
				System.exit(0);
			}
			pWindow.initialize();
			pWindow.setVisible(true);
			p.start();
		}

		if (ce instanceof ControllerErrorEvent)
		{
			p.removeControllerListener(this);
			if (pWindow != null)
			{
				pWindow.close();
			}
		}

	}

	/**
	 * Get the current value of the server address. @ return The IP as a String.
	 */
	public String getAddress()
	{
		return this.address;
	}

	/**
	 * Get the currently defined port number.
	 * 
	 * @return Port number.
	 */
	public int getPort()
	{
		return this.port;
	}

	/**
	 * Get whether the stream is currently being viewed. @ return On/Off.
	 */
	public boolean getView()
	{
		return this.view;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * Set the IP of the server streaming the media.
	 * 
	 * @param addr
	 *            IP address of server.
	 */
	public void setAddress(final String addr)
	{
		propertyChangeListeners.firePropertyChange("address", this.address, addr);
		this.address = addr;
	}

	/**
	 * Set the port number to connect to stream.
	 * 
	 * @param port
	 *            Port number.
	 */
	public void setPort(final int port)
	{
		propertyChangeListeners.firePropertyChange("port", this.port, port);
		this.port = port;
	}

	/**
	 * Set whether the stream should be visible (a new window created).
	 * 
	 * @param view
	 *            Turn the stream view on/off.
	 */
	public void setView(final boolean view)
	{
		propertyChangeListeners.firePropertyChange("view", this.view, view);
		this.view = view;
		if (this.view)
		{
			this.initialize();
		}
		else
		{
			this.close();
		}
	}

	/**
	 * ReceiveStreamListener. Handles the stream data, creating and removing the viewer windows
	 * relevant to the stream.
	 */
	@Override
	public synchronized void update(final ReceiveStreamEvent e)
	{

		final RTPManager mgr = (RTPManager) e.getSource();
		final Participant participant = e.getParticipant(); // could be null.
		ReceiveStream stream = e.getReceiveStream(); // could be null.

		if (e instanceof RemotePayloadChangeEvent)
		{

			System.err.println("Error attempting to change Payload");
			System.exit(0);

		}

		else if (e instanceof NewReceiveStreamEvent)
		{

			try
			{
				stream = ((NewReceiveStreamEvent) e).getReceiveStream();
				final DataSource ds = stream.getDataSource();

				// Find out the formats.
				final RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
				// create a player by passing datasource to the Media Manager
				final Player p = javax.media.Manager.createPlayer(ds);
				if (p == null) { return; }

				p.addControllerListener(this);
				p.realize();
				pWindow = new PlayerWindow(p, stream);

				System.err.println("ADDED STREAM THING");

				// Notify intialize() that a new stream had arrived.
				synchronized (dataSync)
				{
					dataReceived = true;
					dataSync.notifyAll();
				}

			}
			catch (final Exception ee)
			{
				System.err.println("Error: " + ee.getMessage());
				return;
			}

		}

		else if (e instanceof StreamMappedEvent)
		{

			if (stream != null && stream.getDataSource() != null)
			{
				final DataSource ds = stream.getDataSource();
				// Find out the formats.
				final RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
			}
		}

		else if (e instanceof ByeEvent)
		{

			if (pWindow != null)
			{
				pWindow.close();
			}
		}

	}

	/** SessionListener. */
	@Override
	public synchronized void update(final SessionEvent e)
	{
		if (e instanceof NewParticipantEvent)
		{
			final Participant p = ((NewParticipantEvent) e).getParticipant();
		}
	}

	/** Close the player window and the session managers. */
	protected void close()
	{

		try
		{
			pWindow.close();
		}
		catch (final Exception e)
		{
		}

		// close the RTP session.
		if (mgr != null)
		{
			mgr.removeTargets("");
			mgr.dispose();
			mgr = null;
		}

	}

	/** Set up the connection to the RTP server and create a new session. */
	protected boolean initialize()
	{

		try
		{
			InetAddress ipAddr;

			SessionAddress localAddr = new SessionAddress();
			SessionAddress destAddr;

			mgr = RTPManager.newInstance();
			mgr.addSessionListener(this);
			mgr.addReceiveStreamListener(this);

			ipAddr = InetAddress.getByName(address);

			if (ipAddr.isMulticastAddress())
			{
				localAddr = new SessionAddress(ipAddr, port, ttl);
				destAddr = new SessionAddress(ipAddr, port, ttl);
			}
			else
			{
				localAddr = new SessionAddress(InetAddress.getLocalHost(), port);
				destAddr = new SessionAddress(ipAddr, port);
			}

			mgr.initialize(localAddr);

			// You can try out some other buffer size to see
			// if you can get better smoothness.
			final BufferControl bc = (BufferControl) mgr.getControl("javax.media.control.BufferControl");
			if (bc != null)
			{
				bc.setBufferLength(350);
			}

			mgr.addTarget(destAddr);

		}
		catch (final Exception e)
		{
			System.err.println("Cannot create the RTP Session: " + e.getMessage());
			return false;
		}

		// wait for data to arrive before moving on.

		final long then = System.currentTimeMillis();
		final long waitingPeriod = 60000; // wait for 60 secs.

		try
		{
			synchronized (dataSync)
			{
				while (!dataReceived && System.currentTimeMillis() - then < waitingPeriod)
				{
					if (!dataReceived)
					{
						dataSync.wait(1000);
					}
				}
			}
		}
		catch (final Exception e)
		{
		}

		if (!dataReceived)
		{
			System.err.println("No RTP data was received.");
			close();
			return false;
		}

		return true;
	}

}
