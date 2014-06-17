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
package equip.ect.components.dictionarymerge;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import equip.data.DictionaryEntry;
import equip.data.DictionaryImpl;

/**
 * A component which merges the values in its two input Dictionary items, eg for adding metadata to
 * a value.<br>
 * <H3>Description</H3> A component which merges the values in its two input Dictionary items, e.g.
 * for adding metadata to a value.<br>
 * 
 * <H3>Installation</H3> No specific requirements.<br>
 * 
 * <H3>Configuration</H3> E.g. set input2 with the required metadata.<br>
 * 
 * <H3>Usage</H3> A component which merges the values in its two input Dictionary items, e.g. for
 * adding metadata to a value.<br>
 * Specify the desired dictionaries in 'input1' and 'input2' property.<br>
 * The 'output' property will contain a dictionary containing all entries from both 'input1' and
 * 'input2'.<br>
 * In the case of a conflict (i.e. input1 and input2 containing the same entry), the value of
 * input2's entry will be used.<br>
 * Note that ECT built-in coercions will map any non-dictionary value to a dictionary with a single
 * element called "value" (and vice versa).<br>
 * 
 * <H3>Technical Details</H3> Not very exciting<br>
 * 
 * @classification Data/Dictionary
 * @defaultInputProperty input1
 * @defaultOutputProperty output
 * @preferred
 */
public class DictionaryMerge implements Serializable
{
	/**
	 * input1 value
	 */
	protected DictionaryImpl input1;
	/**
	 * input2 value
	 */
	protected DictionaryImpl input2;
	/**
	 * output value
	 */
	protected DictionaryImpl output;
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public DictionaryMerge()
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
	public synchronized DictionaryImpl getInput1()
	{
		return input1;
	}

	/**
	 * input getter
	 */
	public synchronized DictionaryImpl getInput2()
	{
		return input2;
	}

	/**
	 * output getter
	 */
	public synchronized DictionaryImpl getOutput()
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
	 * The first input value.
	 * 
	 * @preferred
	 */
	public void setInput1(final DictionaryImpl input1)
	{
		DictionaryImpl old = null;
		synchronized (this)
		{
			// could suppress no-change setting
			// if (input==this.input || (input!=null && this.input!=null &&
			// input.equals(this.input)) return;
			old = this.input1;
			this.input1 = input1;
		}
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("input1", old, input1);
		// behaviour... (doesn't have to be done like this)
		updateOutput();
	}

	/**
	 * The first input value.
	 * 
	 * @preferred
	 */
	public void setInput2(final DictionaryImpl input2)
	{
		DictionaryImpl old = null;
		synchronized (this)
		{
			// could suppress no-change setting
			// if (input==this.input || (input!=null && this.input!=null &&
			// input.equals(this.input)) return;
			old = this.input2;
			this.input2 = input2;
		}
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("input2", old, input2);
		// behaviour... (doesn't have to be done like this)
		updateOutput();
	}

	/**
	 * stop - called when component request is removed.
	 */
	public synchronized void stop()
	{
		// noop
	}

	/**
	 * output internal setter - NB protected, so that it cannot be called by the framework
	 */
	protected void setOutput(final DictionaryImpl output)
	{
		DictionaryImpl oldOutput = null;
		synchronized (this)
		{
			// could suppress no-change setting
			// if (output==this.output || (output!=null && this.output!=null &&
			// output.equals(this.output)) return;
			oldOutput = this.output;
			this.output = output;
		}
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("output", oldOutput, output);
	}

	/**
	 * behaviour
	 */
	protected void updateOutput()
	{
		final DictionaryImpl output = new DictionaryImpl();
		synchronized (this)
		{
			if (input1 != null && input1.entries != null)
			{
				for (final DictionaryEntry entrie : input1.entries)
				{
					output.put(entrie.name, entrie.value);
				}
			}
			if (input2 != null && input2.entries != null)
			{
				for (final DictionaryEntry entrie : input2.entries)
				{
					output.put(entrie.name, entrie.value);
				}
			}
		}
		setOutput(output);
	}
}
