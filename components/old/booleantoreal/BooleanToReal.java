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
package equip.ect.components.booleantoreal;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * A simple component which maps boolean (i.e. true or false) values to decimal number values.<br>
 * <H3>Summary</H3> A simple component which maps boolean (i.e. true or false) values to decimal
 * number values.<br>
 * <H3>Installation</H3> This component has no special installation requirements.<br>
 * <H3>Configuration</H3> To configure set the 'false out value' and 'true out value' appropriately.
 * See 'Usage' e for details.<br>
 * <H3>Usage</H3> The property 'input property' takes a boolean value. This value will be mapped to
 * the appropriate numeric value and placed in the property 'output property'<br>
 * 
 * The property 'false out value' defines the numeric value to be outputed when the input equals
 * false.<br>
 * The property 'true out value' defines the numeric value to be outputed when the input equals
 * true.<br>
 * 
 * @classification Behaviour/Simple Mapping
 * @preferred
 */
public class BooleanToReal implements Serializable
{
	protected boolean input;
	protected double output;
	protected double trueValue = 1.0;
	protected double falseValue = 1.0;

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public BooleanToReal()
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

	public synchronized double getFalseValue()
	{
		return falseValue;
	}

	public synchronized boolean getInput()
	{
		return input;
	}

	public synchronized double getOutput()
	{
		return output;
	}

	public synchronized double getTrueValue()
	{
		return trueValue;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setFalseValue(final double falseValue)
	{
		final double oldFalseValue = this.falseValue;
		this.falseValue = falseValue;

		propertyChangeListeners
				.firePropertyChange("falsevalue", new Double(oldFalseValue), new Double(this.falseValue));

		updateOutput();
	}

	public synchronized void setInput(final boolean input)
	{
		final boolean oldInput = this.input;
		this.input = input;

		propertyChangeListeners.firePropertyChange("input", new Boolean(oldInput), new Boolean(this.input));

		updateOutput();
	}

	public synchronized void setTrueValue(final double trueValue)
	{
		final double oldTrueValue = this.trueValue;
		this.trueValue = trueValue;

		propertyChangeListeners.firePropertyChange("truevalue", new Double(oldTrueValue), new Double(this.trueValue));

		updateOutput();
	}

	public synchronized void stop()
	{
	}

	protected synchronized void setOutput(final double output)
	{
		final double oldOutput = this.output;
		this.output = output;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("output", new Double(oldOutput), new Double(this.output));
	}

	protected void updateOutput()
	{
		if (input == true)
		{
			setOutput(trueValue);
		}
		else
		{
			setOutput(falseValue);
		}
	}
}
