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
package equip.ect.components.testing.arraytest;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * A simple-as-possible bean with one in and one out. A useful starting point for coding.
 */
public class ArrayTest implements Serializable
{
	/**
	 * input value
	 */
	protected String string;
	/**
	 * input value
	 */
	protected String string1D[];
	/**
	 * input value
	 */
	protected int i;
	/**
	 * input value
	 */
	protected int i1D[];
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public ArrayTest()
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
	public synchronized int getInt()
	{
		return i;
	}

	/**
	 * input getter
	 */
	public synchronized int[] getInt1D()
	{
		return i1D;
	}

	/**
	 * input getter
	 */
	public synchronized String getString()
	{
		return string;
	}

	/**
	 * input getter
	 */
	public synchronized String[] getString1D()
	{
		return string1D;
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
	public synchronized void setInt(final int input)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final int oldInput = this.i;
		this.i = input;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("int", oldInput, this.i);
	}

	/**
	 * input setter
	 */
	public synchronized void setInt1D(final int[] input)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final int[] oldInput = this.i1D;
		this.i1D = input;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("int1D", oldInput, this.i1D);
	}

	/**
	 * input setter
	 */
	public synchronized void setString(final String input)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final String oldInput = this.string;
		this.string = input;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("string", oldInput, this.string);
	}

	/**
	 * input setter
	 */
	public synchronized void setString1D(final String[] input)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final String oldInput[] = this.string1D;
		this.string1D = input;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("string1D", oldInput, this.string1D);
	}

	/**
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
	}
}
