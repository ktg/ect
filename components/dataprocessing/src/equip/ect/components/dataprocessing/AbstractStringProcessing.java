/*
 <COPYRIGHT>

 Copyright (c) 2006, University of Nottingham
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

package equip.ect.components.dataprocessing;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public abstract class AbstractStringProcessing implements Serializable
{
	String input = null;
	String output = null;

	String[] inputArray = null;
	String[] outputArray = null;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized String getInput()
	{
		return input;
	}

	public synchronized String[] getInputArray()
	{
		return inputArray;
	}

	public synchronized String getOutput()
	{
		return output;
	}

	public synchronized String[] getOutputArray()
	{
		return outputArray;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setInput(final String newValue)
	{
		final String oldValue = this.input;
		this.input = newValue;

		propertyChangeListeners.firePropertyChange("input", oldValue, newValue);

		if (newValue != null)
		{
			updateOutput();
		}
	}

	public synchronized void setInputArray(final String[] newValue)
	{
		final String[] oldValue = this.inputArray;
		this.inputArray = newValue;

		propertyChangeListeners.firePropertyChange("inputArray", oldValue, newValue);

		if (newValue != null)
		{
			updateOutputArray();
		}
	}

	void updateOutput()
	{
		// first, determine if input is an array or a single item

		setOutput(operate(getInput()));
	}

	void updateOutputArray()
	{
		final String[] inputArray = getInputArray();

		if (inputArray != null)
		{
			final String[] tempArray = new String[inputArray.length];

			for (int i = 0; i < inputArray.length; i++)
			{
				tempArray[i] = operate(inputArray[i]);
			}
			setOutputArray(tempArray);
		}
	}

	protected abstract String operate(String input);

	protected synchronized void setOutput(final String newValue)
	{
		final String oldValue = this.output;
		this.output = newValue;

		propertyChangeListeners.firePropertyChange("output", oldValue, newValue);
	}

	protected synchronized void setOutputArray(final String[] newValue)
	{
		final String[] oldValue = this.outputArray;
		this.outputArray = newValue;

		propertyChangeListeners.firePropertyChange("outputArray", oldValue, newValue);
	}
}
