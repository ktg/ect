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
 * RTPStation, $RCSfile: RTPStation.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:28 $
 *
 * $Author: chaoticgalen $
 * Original Author: Tom Hart
 */

package equip.ect.components.rtpbroadcast;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedList;

/**
 * Provides a media 'station' from which a range of '{@link RTPChannel#RTPChannel() channels}' can
 * be set up and managed.
 */

public class RTPStation implements Serializable
{
	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/** Maximum number of channels this a station can have. */
	private final int MAXCHANNELS = 6;
	/** The default base port to broadcast from. */
	private final int DEFAULTPORT = 8888;

	/** Stores a list of the {@link RTPChannel#RTPChannel() RTPChannels} created */
	private LinkedList channels = new LinkedList();
	/** The IP of this machine (so we know where to connect to from outside). */
	private String stationAddress;

	/** Create new {@link RTPStation RTPStation} object with one channel. */
	public RTPStation()
	{

		setStationAddress();
		setNumChannels(1);
	}

	/**
	 * Create new {@link RTPStation RTPStation} object with <em>n</em> channels.
	 * 
	 * @param num
	 *            Number of blank channels this RTPStation starts with.
	 */
	public RTPStation(final int num)
	{

		setStationAddress();
		setNumChannels(num);
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * Returns an array of objects currently in <code>devices</code>.
	 * 
	 * @return array of {@link RTPChannel#RTPChannel() RTPChannels} objects.
	 */
	public RTPChannel[] getChannels()
	{
		return (RTPChannel[]) this.channels.toArray(new RTPChannel[this.channels.size()]);
	}

	/**
	 * Returns a the number of {@link RTPChannel#RTPChannel() RTPChannels} this station has.
	 * 
	 * @return number of channels.
	 */
	public int getNumChannels()
	{
		return this.channels.size();
	}

	/**
	 * Get the IP address of the local machine.
	 * 
	 * @return IP Address as a string.
	 */
	public String getStationAddress()
	{
		return stationAddress;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * Increases or decreases the number of {@link RTPChannel#RTPChannel() RTPChannels} this station
	 * has.
	 * 
	 * @param num
	 *            the new number of channels requested for this station
	 */
	public void setNumChannels(final int num)
	{

		final int oldsize = this.getNumChannels();

		if (num < 0 || num > MAXCHANNELS)
		{
			return; // check range
		}
		else if (num > oldsize)
		{
			this.addChannels(num - oldsize);
		}
		else if (num < oldsize)
		{
			this.removeChannels(oldsize - num);
		}
		propertyChangeListeners.firePropertyChange("channels", null, this.getChannels());
		propertyChangeListeners.firePropertyChange("numChannels", 0, this.getNumChannels());
	}

	/**
	 * Add some new blanks channels
	 * 
	 * @param num
	 *            Number of new channels to add.
	 */
	private void addChannels(final int num)
	{

		for (int i = 0; i < num; i++)
		{
			this.channels.addLast(new RTPChannel(this.getNextPort()));
		}
	}

	/**
	 * Find out the next port up from the base port which is not already being used by our other
	 * channels. Only works correctly if the channels have media files assigned to them, otherwise
	 * will default to the base port.
	 * 
	 * @return Next usable port in series.
	 */
	private int getNextPort()
	{
		if (this.channels.size() == 0) { return DEFAULTPORT; }

		final int pB = ((RTPChannel) this.channels.getLast()).getPortBase();
		final int numP = ((RTPChannel) this.channels.getLast()).getNumPortsUsed();
		return pB + (numP * 2);
	}

	/**
	 * Remove exisiting channels. Stops and removes <em>n</em> channels from end or list regardless
	 * of whether they're in use or not.
	 * 
	 * @param num
	 *            Number of channels to remove.
	 */
	private void removeChannels(final int num)
	{

		for (int i = 0; i < num; i++)
		{
			((RTPChannel) this.channels.getLast()).stop(); // stop channel broadcast
			this.channels.removeLast();
		}
	}

	/** Find out the ip of local machine. */
	private void setStationAddress()
	{
		try
		{
			stationAddress = (InetAddress.getLocalHost()).getHostAddress();
		}
		catch (final Exception e)
		{
		}
	}

}
