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
  Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.components.delayline;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Vector;

/**
 * Propagates input to output after a delay, analogous to a digital delay-line. <H3>Description</H3>
 * A succession of values can be supplied to a property of this component. The component queues
 * them, and then places them in sequence onto another property after a specified delay. <H3>Usage</H3>
 * <P>
 * Specify the delay you require (in milliseconds) using property <i>delay_ms</i>. Every value you
 * then supply to property <i>input</i> will then appear on property <i>output</i> after this delay
 * has passed.
 * </P>
 * <P>
 * Specifying any value to property <i>triggerClear</i> will cause <i>output</i> to be reset and any
 * queue values to be dropped. Specifying the value of <tt>true</tt> to property <i>hold</i> will
 * cause queue of values to be paused until <i>hold</i> is set back to value <tt>false</tt>.
 * </P>
 * 
 * @displayName DelayLine
 * @classification Behaviour/Timing
 * @preferred
 * @defaultInputProperty input
 * @defaultOutputProperty output
 */
public class DelayLine implements Serializable
{
	/**
	 * a delayed item record
	 */
	protected class Item
	{
		/**
		 * value
		 */
		Object value;
		/**
		 * entry time
		 */
		long inputTime;

		/**
		 * cons - sets inputTime to current system time
		 */
		Item(final Object value)
		{
			this.value = value;
			this.inputTime = System.currentTimeMillis();
		}

		/**
		 * cons - specified inputTime
		 */
		Item(final Object value, final long inputTime)
		{
			this.value = value;
			this.inputTime = inputTime;
		}
	}

	/**
	 * queue of Item, in inputTime order
	 */
	protected Vector queue = new Vector();

	/**
	 * internal output thread
	 */
	protected Thread outputThread;

	/**
	 * condition variable for stopped
	 */
	protected boolean stopped = false;

	/**
	 * input value
	 */
	protected Object input;
	/**
	 * delay in milliseconds value
	 */
	protected int delay_ms;
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
	protected Object output;
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public DelayLine()
	{
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
	 * delay getter
	 */
	public synchronized int getDelay_ms()
	{
		return delay_ms;
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
	public synchronized Object getInput()
	{
		return input;
	}

	/**
	 * output getter
	 */
	public synchronized Object getOutput()
	{
		return output;
	}

	/**
	 * clear getter
	 */
	public synchronized Object getTriggerClear()
	{
		return null;
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
	 * delay setter
	 */
	public synchronized void setDelay_ms(final int v)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final int old = this.delay_ms;
		this.delay_ms = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("delay_ms", old, v);
		// behaviour... (doesn't have to be done like this)
		updateOutputs();
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
	public synchronized void setInput(final Object input)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final Object oldInput = this.input;
		this.input = input;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("input", oldInput, this.input);

		// discard if clearing
		if (clear) { return; }
		// add to queue - effective add time offset by current hold period
		if (!hold)
		{
			queue.addElement(new Item(input, System.currentTimeMillis() - hold_ms));
		}
		else
		{
			// hold - as if at start of hold
			queue.addElement(new Item(input, hold_start - hold_ms));
		}
		// behaviour... (doesn't have to be done like this)
		updateOutputs();
	}

	public synchronized void setTriggerClear(final Object ob)
	{
		// cause delay line state to be dropped on provision of any
		// object

		if (ob != null)
		{
			queue.removeAllElements();
			setOutput(null);
			updateOutputs();
		}
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
	 * output thread function
	 */
	protected synchronized void outputThreadFn()
	{
		try
		{
			while (!stopped)
			{
				// nothing to do?
				if (hold || queue.size() == 0)
				{
					// wait until awoken
					this.wait();
				}
				else
				{
					// delivery time of next output
					final Item next = (Item) queue.elementAt(0);
					final long now = System.currentTimeMillis();
					final long wait_ms = next.inputTime + hold_ms + delay_ms - now;
					if (wait_ms <= 0)
					{
						// deliver
						this.queue.removeElementAt(0);
						setOutput(next.value);
					}
					else
					{
						// wait until then (or awoken)
						this.wait(wait_ms);
					}
				}
			}
		}
		catch (final InterruptedException e)
		{
			System.err.println("WARNING: DelayLine output thread interrupted (exiting): " + e);
		}
	}

	/**
	 * output internal setter - NB protected, so that it cannot be called by the framework
	 */
	protected synchronized void setOutput(final Object output)
	{
		// could suppress no-change setting
		// if (output==this.output || (output!=null && this.output!=null &&
		// output.equals(this.output)) return;
		final Object oldOutput = this.output;
		this.output = output;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("output", oldOutput, this.output);
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
