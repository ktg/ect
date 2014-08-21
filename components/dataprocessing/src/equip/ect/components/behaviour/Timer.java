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

 Created by: Stefan Rennick Egglestone(University of Nottingham)
 Contributors:
 Stefan Rennick Egglestone(University of Nottingham)

 */
package equip.ect.components.behaviour;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Date;

/**
 * Produces timing signal at specified intervals
 *
 * @author stef
 */
@ECTComponent
@Category("Timing")
public class Timer implements Runnable, Serializable
{
	private enum TimeSpan
	{
		day(86400000),
		hour(3600000),
		min(60000),
		sec(1000);

		private final long length;
		TimeSpan(long length)
		{
			this.length = length;
		}
	}

	private boolean output = false;
	private boolean running = false;
	private int delay = 1000;
	private boolean repeat = false;
	private boolean reset = false;

	private String countdown;

	private static final long sleep = 100;

	// DelayRunnable dr = null;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public Timer()
	{
	}

	@Override
	public void run()
	{
		long startTime = System.currentTimeMillis();
		while (running)
		{
			long now = System.currentTimeMillis();
			long difference = (now - startTime);
			if(difference > delay)
			{
				// Do something
				setOutput(!getOutput());
				if(!repeat)
				{
					setRunning(false);
				}
			}

			long remaining = delay - difference;

			setCountdown(humanTimeSpan(remaining));

			try
			{
				Thread.sleep(sleep);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private String humanTimeSpan(long duration)
	{
		for(TimeSpan timeSpan: TimeSpan.values())
		{
			if(duration<timeSpan.length)
			{
				continue;
			}
			long numberOfUnits = duration / timeSpan.length;
			return numberOfUnits + " " + timeSpan.name() + ((numberOfUnits>1)?"s":"");
		}

		return "";
	}

	public String getCountdown()
	{
		return countdown;
	}

	public void setCountdown(final String countdown)
	{
		String oldCountdown = this.countdown;
		if(oldCountdown == null || !oldCountdown.equals(countdown))
		{
			this.countdown = countdown;

			propertyChangeListeners.firePropertyChange("countdown", oldCountdown, countdown);
		}
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public int getDelay()
	{
		return delay;
	}

	public synchronized boolean getOutput()
	{
		return output;
	}

	public boolean getRepeat()
	{
		return repeat;
	}

	public synchronized boolean getRunning()
	{
		return running;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setDelay(final int newDelay)
	{
		if (delay != newDelay)
		{
			final int oldDelay = delay;

			delay = newDelay;
			propertyChangeListeners.firePropertyChange("delay", oldDelay, newDelay);
		}
	}

	public synchronized void setOutput(final boolean newValue)
	{
		if (output != newValue)
		{

			final boolean oldValue = output;
			output = newValue;

			propertyChangeListeners.firePropertyChange("output", oldValue, newValue);
		}
	}

	public boolean getReset()
	{
		return reset;
	}

	public void setReset(final boolean value)
	{
		if (value != reset)
		{
			final boolean oldValue = reset;
			reset = value;

			propertyChangeListeners.firePropertyChange("reset", oldValue, reset);
		}
	}

	public void setRepeat(final boolean value)
	{
		if (value != repeat)
		{
			final boolean oldValue = repeat;
			repeat = value;

			propertyChangeListeners.firePropertyChange("repeat", oldValue, repeat);
		}
	}

	public synchronized void setRunning(final boolean newValue)
	{
		if (newValue != running)
		{
			final boolean oldValue = running;
			running = newValue;

			propertyChangeListeners.firePropertyChange("running", oldValue, newValue);

			if (newValue)
			{
				// start the timer running and
				// register this object as an action listener
				startTimer();
			}
		}
	}

	public void stop()
	{
		// called by ECT when component being destroyed
		running = false;
	}

	private void startTimer()
	{
		if(reset)
		{
			setOutput(false);
		}
		Thread thread = new Thread(this);
		thread.start();
	}
}