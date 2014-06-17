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

Created by: Stefan Rennick Egglestone (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

 */
package equip.ect.components.testing.stringdictionarytest;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;

/**
 * Can be used to construct dictionaries
 */
public class StringDictionaryTest implements Serializable
{
	protected String[] keys;
	protected String[] values;

	protected String message;
	protected DictionaryImpl dictionary;
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public StringDictionaryTest()
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

	public synchronized DictionaryImpl getDictionary()
	{
		return dictionary;
	}

	public synchronized String[] getKeys()
	{
		return keys;
	}

	public synchronized String getMessage()
	{
		return message;
	}

	public synchronized String[] getValues()
	{
		return values;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setKeys(final String[] keys)
	{
		final String[] oldKeys = this.keys;
		this.keys = keys;

		createDictionary();

		propertyChangeListeners.firePropertyChange("keys", oldKeys, this.keys);
	}

	public synchronized void setValues(final String[] values)
	{
		final String[] oldValues = this.values;
		this.values = values;

		createDictionary();

		propertyChangeListeners.firePropertyChange("values", oldValues, this.values);
	}

	/**
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
	}

	protected void createDictionary()
	{
		final String[] keys = getKeys();
		final String[] values = getValues();

		if (keys == null)
		{
			System.out.println("keys is null");
		}
		else
		{
			System.out.println(keys.length + " keys");

			for (final String key : keys)
			{
				System.out.println(key);
			}
		}

		if (values == null)
		{
			System.out.println("values is null");
		}
		else
		{
			System.out.println(values.length + " values");

			for (final String value : values)
			{
				System.out.println(value);
			}
		}

		if ((keys == null) || (keys.length == 0))
		{
			setMessage("You must provide some keys");
			setDictionary(null);
			return;
		}

		if ((values == null) || (values.length == 0))
		{
			setMessage("You must provide some values");
			setDictionary(null);
			return;
		}

		if (keys.length != values.length)
		{
			setMessage("You must supply the same number of keys and values");
			setDictionary(null);
			return;
		}

		final DictionaryImpl newDictionary = new DictionaryImpl();

		for (int i = 0; i < keys.length; i++)
		{
			newDictionary.put(keys[i], new StringBoxImpl(values[i]));
		}
		setDictionary(newDictionary);
		setMessage("Dictionary created successfully");
	}

	protected synchronized void setDictionary(final DictionaryImpl newDictionary)
	{
		final DictionaryImpl oldDictionary = dictionary;
		this.dictionary = newDictionary;

		propertyChangeListeners.firePropertyChange("dictionary", oldDictionary, newDictionary);

	}

	protected synchronized void setMessage(final String message)
	{
		final String oldMessage = this.message;
		this.message = message;

		propertyChangeListeners.firePropertyChange("message", oldMessage, this.message);
	}
}
