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
package equip.ect.components.thresholder;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * A simple-as-possible bean with one in and one out. A useful starting point for coding.
 * 
 * @preferred
 */
public class Thresholder implements Serializable
{
	protected double input;
	protected boolean output;
	protected double threshold = 0.5;

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public Thresholder()
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

	public synchronized double getInput()
	{
		return input;
	}

	public synchronized boolean getOutput()
	{
		return output;
	}

	public synchronized double getThreshold()
	{
		return threshold;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setInput(final double input)
	{
		final double oldInput = this.input;
		this.input = input;

		propertyChangeListeners.firePropertyChange("input", new Double(oldInput), new Double(this.input));

		updateOutput();
	}

	public synchronized void setThreshold(final double threshold)
	{
		final double oldThreshold = this.threshold;
		this.threshold = threshold;

		propertyChangeListeners.firePropertyChange("threshold", new Double(oldThreshold), new Double(this.threshold));

		updateOutput();
	}

	public synchronized void stop()
	{
	}

	protected synchronized void setOutput(final boolean output)
	{
		final boolean oldOutput = this.output;
		this.output = output;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("output", new Boolean(oldOutput), new Boolean(this.output));
	}

	protected void updateOutput()
	{
		if (input < threshold)
		{
			setOutput(false);
		}
		else
		{
			setOutput(true);
		}
	}
}
