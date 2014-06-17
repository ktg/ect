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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

 */
package equip.ect.components.configurationtest;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * A simple-as-possible bean with one in and one out. A useful starting point for coding.
 */
public class ConfigurationTest implements Serializable
{
	/**
	 * input value
	 */
	protected String input1;
	/**
	 * input value
	 */
	protected String input2;
	/**
	 * input value
	 */
	protected String input3 = "";
	/**
	 * input value
	 */
	protected String input4;
	/**
	 * input value
	 */
	protected String input5;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public ConfigurationTest()
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

	/**
	 * input getter
	 */
	public synchronized String getInput1()
	{
		return input1;
	}

	/**
	 * input getter
	 */
	public synchronized String getInput2()
	{
		return input2;
	}

	/**
	 * input getter
	 */
	public synchronized String getInput3()
	{
		return input3;
	}

	/**
	 * input getter
	 */
	public synchronized String getInput4()
	{
		return input4;
	}

	/**
	 * input getter
	 */
	public synchronized String getInput5()
	{
		return input5;
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
	 * input setter
	 */
	public synchronized void setInput1(final String input)
	{
		if (input3.length() != 0) { return; }
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final String oldInput = this.input1;
		this.input1 = input;
		// fire property change - make sure the name matches the bean info name
		System.err.println("Set input1");
		propertyChangeListeners.firePropertyChange("configinput1", oldInput, this.input1);
	}

	/**
	 * input setter
	 */
	public synchronized void setInput2(final String input)
	{
		if (input3.length() != 0) { return; }
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final String oldInput = this.input2;
		this.input2 = input;
		// fire property change - make sure the name matches the bean info name
		System.err.println("Set input2");
		propertyChangeListeners.firePropertyChange("configinput2", oldInput, this.input2);
	}

	/**
	 * input setter
	 */
	public synchronized void setInput3(final String input)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final String oldInput = this.input3;
		this.input3 = input;
		// fire property change - make sure the name matches the bean info name
		System.err.println("Set input3");
		propertyChangeListeners.firePropertyChange("configured", oldInput, this.input3);
	}

	/**
	 * input setter
	 */
	public synchronized void setInput4(final String input)
	{
		if (input3.length() == 0) { return; }

		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final String oldInput = this.input4;
		this.input4 = input;
		// fire property change - make sure the name matches the bean info name
		System.err.println("Set input4");
		propertyChangeListeners.firePropertyChange("input1", oldInput, this.input4);
	}

	/**
	 * input setter
	 */
	public synchronized void setInput5(final String input)
	{
		if (input3.length() == 0) { return; }
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final String oldInput = this.input5;
		this.input5 = input;
		// fire property change - make sure the name matches the bean info name
		System.err.println("Set input5");
		propertyChangeListeners.firePropertyChange("input2", oldInput, this.input5);
	}

	/**
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
	}
}
