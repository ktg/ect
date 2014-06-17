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

/**
 * Produces timing signal at specified intervals
 *
 * @author stef
 * @classification Behaviour/Timing
 * @defaultOutputValue output
 */
@ECTComponent
@Category("Timing")
public class Timer implements Serializable, ActionListener
{
	private boolean output = false;
	private boolean running = false;
	private int delay = 1000;
	private javax.swing.Timer timer = null;
	private boolean repeat = false;

	// DelayRunnable dr = null;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public Timer()
	{
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		setOutput(!getOutput());
		setRunning(timer.isRunning());
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

			if (getRunning())
			{
				stopTimer();
				startTimer();
			}
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
			else
			{
				stopTimer();
			}
		}
	}

	public void stop()
	{
		// called by ECT when component being destroyed
		stopTimer();
	}

	private void startTimer()
	{
		timer = new javax.swing.Timer(delay, this);
		timer.setRepeats(repeat);
		timer.start();
	}

	private void stopTimer()
	{
		if (timer != null)
		{
			timer.removeActionListener(this);
			timer.stop();
			timer = null;
		}
	}
}