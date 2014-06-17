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
package equip.ect.components.dynamicpropertiestest;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.StringTokenizer;

import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertiesSupport;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.NoSuchPropertyException;

/**
 * A simple-as-possible bean with one in and one out. A useful starting point for coding.
 */
public class DynamicPropertiesTest implements Serializable, DynamicProperties
{
	/**
	 * dynamic properties support
	 */
	protected DynamicPropertiesSupport dynsup;
	/**
	 * input value
	 */
	protected String input;
	/**
	 * output value
	 */
	protected String output;
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public DynamicPropertiesTest()
	{
		dynsup = new DynamicPropertiesSupport(propertyChangeListeners);
		setInput("a");
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
	 * get all properties' {@link DynamicPropertyDescriptors}
	 */
	@Override
	public DynamicPropertyDescriptor[] dynGetProperties()
	{
		return dynsup.dynGetProperties();
	}

	/**
	 * get one property by name
	 */
	@Override
	public Object dynGetProperty(final String name) throws NoSuchPropertyException
	{
		return dynsup.dynGetProperty(name);
	}

	/**
	 * get one property by name
	 */
	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		dynsup.dynSetProperty(name, value);
	}

	/**
	 * input getter
	 */
	public synchronized String getInput()
	{
		return input;
	}

	/**
	 * output getter
	 */
	public synchronized String getOutput()
	{
		return output;
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
	public synchronized void setInput(final String input)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final String oldInput = this.input;
		this.input = input;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("input", oldInput, this.input);
		// behaviour... (doesn't have to be done like this)
		updateOutputs();
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
	protected synchronized void setOutput(final String output)
	{
		// could suppress no-change setting
		// if (output==this.output || (output!=null && this.output!=null &&
		// output.equals(this.output)) return;
		final String oldOutput = this.output;
		this.output = output;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("output", oldOutput, this.output);
	}

	/**
	 * behaviour
	 */
	protected void updateOutputs()
	{
		// echo input to output
		setOutput(input);

		// dynamic properties??
		final DynamicPropertyDescriptor props[] = dynsup.dynGetProperties();
		// new properties?
		StringTokenizer toks = new StringTokenizer(input, " ");
		while (toks.hasMoreTokens())
		{
			final String tok = toks.nextToken();
			// new?
			boolean found = false;
			for (int i = 0; i < props.length && !found; i++)
			{
				if (props[i].getName().equals(tok))
				{
					found = true;
				}
			}
			if (!found)
			{
				System.out.println("Add dynamic property " + tok + "...");
				dynsup.addProperty(tok, Object.class, null);
			}
		}
		// remove properties?
		for (final DynamicPropertyDescriptor prop : props)
		{
			toks = new StringTokenizer(input, " ");
			boolean found = false;
			while (toks.hasMoreTokens())
			{
				final String tok = toks.nextToken();
				if (prop.getName().equals(tok))
				{
					found = true;
				}
			}
			if (!found)
			{
				System.out.println("Remove dynamic property " + prop.getName() + "...");
				try
				{
					dynsup.removeProperty(prop.getName());
				}
				catch (final NoSuchPropertyException e)
				{
					System.err.println("ERROR removing dyn property " + prop.getName() + ": " + e);
					e.printStackTrace(System.err);
				}
			}
		}
	}
}
