/*
 <COPYRIGHT>

 Copyright (c) 2005, University of Nottingham
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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Jan Humble (University of Nottingham)
 Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.arrayplayer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;

/**
 * Outputs one object at a time from a given array.
 * 
 * <H3>Description</H3> <B>ArrayPlayer</B> is designed to play back elements from an array in the
 * same way as a music player. You are able to play back elements either sequentially or randomly,
 * loop, and specify the frequency of output.
 * 
 * <H3>Usage</H3> Set the 'array' property to the array containing the elements you want to
 * playback. The current element will be set to the 'output' property.<BR>
 * Set 'frequency' to specify how often a new element of the array is to be set to the output.<BR>
 * Set 'loop' to true if you desire to loop the play process.<BR>
 * Set 'random' to true if you wish to replay elements at random.<BR>
 * 
 * @classification Behaviour/Timing
 * 
 * @defaultInputProperty array
 * @defaultOutputProperty output
 * 
 * @author humble
 */
public class ArrayPlayer implements Serializable
{

	private class PlayThread extends Thread
	{

		@Override
		public void run()
		{
			int counter = 0;
			while (playing)
			{
				synchronized (playlist)
				{
					if ((playlist != null) && (playlist.size() > 0))
					{
						if (counter < playlist.size())
						{
							setOutput(playlist.get(counter++));
						}
						else
						{
							counter = 0;
							if (!loop)
							{
								setPlaying(false);
								break;
							}
							else
							{
								setOutput(playlist.get(counter++));
							}
						}
					}
					else
					{
						setOutput(null);
					}
				}

				try
				{
					sleep(frequency);
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}

			}
		}

	}

	private Object[] array = null;

	private Object output = null;

	private int frequency = 3000;

	private boolean loop = true;

	private boolean random = false;

	private Vector playlist;

	private PlayThread playThread;

	private boolean playing = false;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public ArrayPlayer()
	{
		super();
		this.playlist = new Vector();
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * @return Returns the array.
	 */
	public Object[] getArray()
	{
		return array;
	}

	/**
	 * @return Returns the frequency.
	 */
	public int getFrequency()
	{
		return frequency;
	}

	/**
	 * @return Returns the output.
	 */
	public Object getOutput()
	{
		return output;
	}

	/**
	 * @return Returns the loop.
	 */
	public boolean isLoop()
	{
		return loop;
	}

	public boolean isPlaying()
	{
		return this.playing;
	}

	/**
	 * @return Returns the random.
	 */
	public boolean isRandom()
	{
		return random;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * @param array
	 *            The array to set.
	 */
	public synchronized void setArray(final Object[] newArray)
	{
		final Object[] old = this.array;
		this.array = newArray;

		if (newArray != null)
		{
			this.playlist = new Vector(newArray.length);

			for (final Object element : newArray)
			{
				playlist.addElement(element);
			}
		}
		else
		{
			playlist = new Vector();
		}

		if (random)
		{
			Collections.shuffle(playlist);
		}
		propertyChangeListeners.firePropertyChange("array", old, newArray);
	}

	/**
	 * @param frequency
	 *            The frequency to set.
	 */
	public void setFrequency(final int frequency)
	{
		final int old = this.frequency;
		this.frequency = frequency;
		propertyChangeListeners.firePropertyChange("frequency", old, frequency);
	}

	/**
	 * @param loop
	 *            The loop to set.
	 */
	public void setLoop(final boolean loop)
	{
		final boolean old = this.loop;
		this.loop = loop;
		propertyChangeListeners.firePropertyChange("loop", old, loop);
	}

	public synchronized void setPlaying(final boolean playing)
	{
		final boolean alreadyPlaying = this.playing;
		this.playing = playing;
		if (!alreadyPlaying && playing)
		{
			play();
		}
		propertyChangeListeners.firePropertyChange("playing", alreadyPlaying, playing);
	}

	/**
	 * @param random
	 *            The random to set.
	 */
	public void setRandom(final boolean random)
	{
		final boolean randomBefore = this.random;
		this.random = random;

		propertyChangeListeners.firePropertyChange("random", randomBefore, random);
		setArray(array);
	}

	private void play()
	{
		playThread = new PlayThread();
		playThread.start();
	}

	private void setOutput(final Object output)
	{
		final Object old = this.output;
		this.output = output;
		propertyChangeListeners.firePropertyChange("output", old, output);
	}

}
