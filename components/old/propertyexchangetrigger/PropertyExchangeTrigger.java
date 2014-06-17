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

Created by: Alastair Hampshire (University of Nottingham)
Contributors:
  Alastair Hampshire (University of Nottingham)
  Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.propertyexchangetrigger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Simple component which copies an input property to an output property on receiving a trigger.<br>
 * <H3>Summary</H3> Simple component which copies an input property to an output property on
 * receiving a trigger.<br>
 * <H3>Usage</H3> Set the input property value as desired. On changing the value of the trigger
 * property, the output property will be set to the value of the input property.<br>
 * Setting the value of the clear property to any value will cause the output property value to be
 * set to the value of clearedOutputValue.<br>
 * 
 * @classification Behaviour/Timing
 * @preferred
 */
public class PropertyExchangeTrigger implements Serializable
{
	/**
	 * input value
	 */
	protected Object input = null;
	/**
	 * output value
	 */

	protected Object output = null;
	protected Object clearedOutputValue = null;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public PropertyExchangeTrigger()
	{
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized Object getClear()
	{
		return null;
	}

	public Object getClearedOutputValue()
	{
		return clearedOutputValue;
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

	public synchronized Object getTrigger()
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

	public synchronized void setClear(final Object clear)
	{
		// could suppress no-change setting
		// if (output==this.output || (output!=null && this.output!=null &&
		// output.equals(this.output)) return;
		// String oldTrigger = this.trigger;
		// this.trigger = newTrigger;

		setOutput(getClearedOutputValue());

		// fire property change - make sure the name matches the bean info name
		// propertyChangeListeners.firePropertyChange("trigger", oldTrigger, newTrigger);
	}

	public void setClearedOutputValue(final Object clearedOutputValue)
	{
		final Object oldValue = this.clearedOutputValue;
		this.clearedOutputValue = clearedOutputValue;

		propertyChangeListeners.firePropertyChange("clearedOutputValue", oldValue, this.clearedOutputValue);
	}

	/**
	 * The input value, which is cascaded to output. Blah input <b>blah</b>.
	 * 
	 * @preferred
	 */
	public synchronized void setInput(final Object input)
	{
		final Object oldInput = this.input;
		this.input = input;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("input", oldInput, this.input);
	}

	public synchronized void setTrigger(final Object newTrigger)
	{
		// first, clear output, then place input onto
		// it

		// we clear it first to ensure that ECT will spot
		// the new value being placed onto it

		if (newTrigger != null)
		{
			setOutput(null);
			setOutput(input);
		}
	}

	/**
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
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
}
