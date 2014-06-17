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
package equip.ect.components.countertimer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * A counter/timer. Emits regularly and/or counts/emits on input change.
 */
public class CounterTimer implements Serializable
{
	/**
	 * internal output thread
	 */
	protected Thread outputThread;

	/**
	 * condition variable for stopped
	 */
	protected boolean stopped = false;

	/**
	 * default maxcount
	 */
	protected static final int MAX_COUNT = 1000000;

	/**
	 * input value
	 */
	protected boolean input;
	/**
	 * count same value
	 */
	protected boolean countsame = true;
	/**
	 * count false value
	 */
	protected boolean countfalse = true;
	/**
	 * count
	 */
	protected int countout;
	/**
	 * max count
	 */
	protected int maxcount;
	/**
	 * output width in milliseconds value
	 */
	protected int pulse_width_ms;
	/**
	 * timer interval in milliseconds value
	 */
	protected int interval_ms;
	/**
	 * start time
	 */
	protected long last_count_ms;
	/**
	 * hold state
	 */
	protected boolean hold;
	/**
	 * time held for so far in previous completed hold periods
	 */
	public long hold_ms;
	/**
	 * time at which current hold began
	 */
	public long hold_start;
	/**
	 * clear state
	 */
	protected boolean clear;
	/**
	 * output value
	 */
	protected boolean pulseout;
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public CounterTimer()
	{
		// initialise
		last_count_ms = System.currentTimeMillis();
		maxcount = MAX_COUNT;
		// start internal thread
		outputThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				outputThreadFn();
			}
		});
		outputThread.start();
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
	 * clear getter
	 */
	public synchronized boolean getClear()
	{
		return clear;
	}

	/**
	 * getter
	 */
	public synchronized boolean getCountfalse()
	{
		return countfalse;
	}

	/**
	 * getter
	 */
	public synchronized int getCountout()
	{
		return countout;
	}

	/**
	 * getter
	 */
	public synchronized boolean getCountsame()
	{
		return countsame;
	}

	/**
	 * hold getter
	 */
	public synchronized boolean getHold()
	{
		return hold;
	}

	/**
	 * input getter
	 */
	public synchronized boolean getInput()
	{
		return input;
	}

	/**
	 * getter
	 */
	public synchronized int getInterval_ms()
	{
		return interval_ms;
	}

	/**
	 * getter
	 */
	public synchronized int getMaxcount()
	{
		return maxcount;
	}

	/**
	 * getter
	 */
	public synchronized int getPulse_width_ms()
	{
		return pulse_width_ms;
	}

	/**
	 * output getter
	 */
	public synchronized boolean getPulseout()
	{
		return pulseout;
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
	 * clear setter
	 */
	public synchronized void setClear(final boolean v)
	{
		// could suppress no-change setting
		if (clear == v) { return; }
		final boolean old = this.clear;
		this.clear = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("clear", old, v);
		// act
		if (this.clear)
		{
			setPulseout(false);
			setCountout(0);
			last_count_ms = System.currentTimeMillis();
			hold_start = last_count_ms;
			hold_ms = 0;
		}
		// behaviour...
		updateOutputs();
	}

	/**
	 * setter
	 */
	public synchronized void setCountfalse(final boolean v)
	{
		// could suppress no-change setting
		if (v == this.countfalse) { return; }
		final boolean old = this.countfalse;
		this.countfalse = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("countfalse", old, v);
		// passive
	}

	/**
	 * setter
	 */
	public synchronized void setCountsame(final boolean v)
	{
		// could suppress no-change setting
		if (v == this.countsame) { return; }
		final boolean old = this.countsame;
		this.countsame = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("countsame", old, v);
		// passive
	}

	/**
	 * hold setter
	 */
	public synchronized void setHold(final boolean v)
	{
		// could suppress no-change setting
		if (hold == v) { return; }
		final boolean old = this.hold;
		this.hold = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("hold", old, v);
		// act
		if (this.hold)
		{
			// start hold
			hold_start = System.currentTimeMillis();
		}
		else
		{
			// end hold
			hold_ms = hold_ms + (System.currentTimeMillis() - hold_start);
		}
		// behaviour...
		updateOutputs();
	}

	/**
	 * input setter
	 */
	public synchronized void setInput(final boolean input)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final boolean oldInput = this.input;
		this.input = input;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("input", oldInput, this.input);

		// discard if clearing
		if (clear) { return; }

		if (input == oldInput)
		{
			if (countsame)
			{
				countOne(true);
			}
		}
		else if (input == false)
		{
			if (countfalse)
			{
				countOne(true);
			}
		}
		else
		{
			countOne(true);
		}
	}

	/**
	 * setter
	 */
	public synchronized void setInterval_ms(final int v)
	{
		// could suppress no-change setting
		if (v == this.interval_ms) { return; }
		final int old = this.interval_ms;
		this.interval_ms = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("interval_ms", old, v);
		// behaviour... (doesn't have to be done like this)
		updateOutputs();
	}

	/**
	 * setter
	 */
	public synchronized void setMaxcount(final int v)
	{
		// could suppress no-change setting
		if (v == this.maxcount) { return; }
		final int old = this.maxcount;
		this.maxcount = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("maxcount", old, v);
		if (this.countout > this.maxcount)
		{
			setCountout(0);
		}
	}

	/**
	 * setter
	 */
	public synchronized void setPulse_width_ms(final int v)
	{
		// could suppress no-change setting
		if (v == this.pulse_width_ms) { return; }
		final int old = this.pulse_width_ms;
		this.pulse_width_ms = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("pulse_width_ms", old, v);
		// behaviour... (doesn't have to be done like this)
		updateOutputs();
	}

	/**
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
		// stop internal thread
		stopped = true;
		this.notifyAll();
	}

	/**
	 * internal updated
	 */
	protected synchronized void countOne(final boolean resetPulse)
	{
		setCountout(this.countout + 1);
		if (resetPulse)
		{
			last_count_ms = System.currentTimeMillis();
			hold_start = last_count_ms;
			hold_ms = 0;
			setPulseout(true);
			updateOutputs();
		}
	}

	/**
	 * output thread function
	 */
	protected synchronized void outputThreadFn()
	{
		try
		{
			while (!stopped)
			{
				boolean waitForever = true;
				long wait_ms = 0;
				final long now = System.currentTimeMillis();

				if (!hold && interval_ms > 0)
				{
					waitForever = false;
					// fire next timer?
					final long next_timer_ms = last_count_ms + hold_ms + interval_ms;
					if (now >= next_timer_ms)
					{
						// fire timer - simple version with cumulative slip
						countOne(true);
						wait_ms = interval_ms;
					}
					else
					{
						wait_ms = next_timer_ms - now;
					}
				}
				if (pulseout == true)
				{
					// clear pulse
					final long clear_pulse_ms = last_count_ms + pulse_width_ms;
					if (now >= clear_pulse_ms)
					{
						setPulseout(false);
					}
					else
					{
						if (waitForever)
						{
							waitForever = false;
							wait_ms = clear_pulse_ms - now;
						}
						else if (clear_pulse_ms - now < wait_ms)
						{
							wait_ms = clear_pulse_ms - now;
						}
					}
				}
				if (waitForever)
				{
					this.wait();
				}
				else
				{
					this.wait(wait_ms);
				}
			}
		}
		catch (final InterruptedException e)
		{
			System.err.println("WARNING: CounterTimer output thread interrupted (exiting): " + e);
		}
	}

	/**
	 * internal setter
	 */
	protected synchronized void setCountout(int v)
	{
		if (v > this.maxcount)
		{
			v = 0;
		}

		// could suppress no-change setting
		if (v == this.countout) { return; }
		final int old = this.countout;
		this.countout = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("countout", old, v);
	}

	/**
	 * output internal setter - NB protected, so that it cannot be called by the framework
	 */
	protected synchronized void setPulseout(final boolean v)
	{
		// could suppress no-change setting
		// if (output==this.output || (output!=null && this.output!=null &&
		// output.equals(this.output)) return;
		final boolean old = this.pulseout;
		this.pulseout = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("pulseout", old, v);
	}

	/**
	 * behaviour
	 */
	protected synchronized void updateOutputs()
	{
		// notify output thread
		this.notifyAll();
	}
}
