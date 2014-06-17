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
 * RTPViewer, $RCSfile: RTPViewer.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:28 $
 *
 * $Author: chaoticgalen $
 * Original Author: Tom Hart
 */

package equip.ect.components.rtpviewer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.LinkedList;

/**
 * Provides a RTP media viewing control. A number of '{@link RTPPlayer#RTPPlayer() viewers}' can be
 * created and specific RTP streams assigned to each one and then viewed.
 */
public class RTPViewer implements Serializable
{
	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/** Maximum number of players the viewer can have. */
	private final int MAXPLAYERS = 6;

	/** Stores a list of the {@link RTPPlayer#RTPPlayer() RTPPlayers} created */
	private LinkedList players = new LinkedList();

	public RTPViewer()
	{
		setNumPlayers(1);
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * Returns a the number of {@link RTPPlayer#RTPPlayer() RTPPlayers} this viewer has.
	 * 
	 * @return number of players.
	 */
	public int getNumPlayers()
	{
		return this.players.size();
	}

	/**
	 * Returns an array of objects currently in <code>players</code>.
	 * 
	 * @return array of {@link RTPPlayer#RTPPlayer() RTPPlayers} objects.
	 */
	public RTPPlayer[] getPlayers()
	{
		return (RTPPlayer[]) this.players.toArray(new RTPPlayer[this.players.size()]);
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * Increases or decreases the number of {@link RTPPlayer#RTPPlayer() RTPPlayers} the viewer has.
	 * 
	 * @param num
	 *            the new number of players requested
	 */
	public void setNumPlayers(final int num)
	{

		final int oldsize = this.getNumPlayers();

		if (num < 0 || num > MAXPLAYERS)
		{
			return; // check range
		}
		else if (num > oldsize)
		{
			this.addPlayers(num - oldsize);
		}
		else if (num < oldsize)
		{
			this.removePlayers(oldsize - num);
		}
		propertyChangeListeners.firePropertyChange("players", null, this.getPlayers());
		propertyChangeListeners.firePropertyChange("numPlayers", 0, this.getNumPlayers());
	}

	/**
	 * Add some new players
	 * 
	 * @param num
	 *            Number of new players to add.
	 */
	private void addPlayers(final int num)
	{

		for (int i = 0; i < num; i++)
		{
			this.players.addLast(new RTPPlayer());
		}
	}

	/**
	 * Remove exisiting players. Stops and removes <em>n</em> players from end or list regardless of
	 * whether they're in use or not.
	 * 
	 * @param num
	 *            Number of players to remove.
	 */
	private void removePlayers(final int num)
	{

		for (int i = 0; i < num; i++)
		{
			((RTPPlayer) this.players.getLast()).close(); // shut down this player
			this.players.removeLast();
		}
	}
}
