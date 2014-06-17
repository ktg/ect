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

Created by: Stefan Rennick Egglestone (University of Nottingham)
Contributors:
  Stefan Rennick Egglestone (University of Nottingham)

 */

package equip.ect.components.dictionary;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Hashtable;

import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;

/**
 * @classification Data/Dictionary
 * @preferred
 */

public class DictionaryToText implements Serializable
{
	protected DictionaryImpl[] dictionaryArray;

	protected DictionaryImpl dictionary;

	protected String text;

	protected boolean includeKeys;

	protected String[] textArray;

	protected String[] keyOrder;

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public DictionaryToText()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized DictionaryImpl getDictionary()
	{
		return dictionary;
	}

	public synchronized DictionaryImpl[] getDictionaryArray()
	{
		return dictionaryArray;
	}

	public synchronized boolean getIncludeKeys()
	{
		return includeKeys;
	}

	public synchronized String[] getKeyOrder()
	{
		return keyOrder;
	}

	public synchronized String getText()
	{
		return text;
	}

	public synchronized String[] getTextArray()
	{
		return textArray;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setDictionary(final DictionaryImpl newValue)
	{
		final DictionaryImpl oldValue = this.dictionary;
		this.dictionary = newValue;

		propertyChangeListeners.firePropertyChange("dictionary", oldValue, newValue);

		calculateText();
	}

	public synchronized void setDictionaryArray(final DictionaryImpl[] newValue)
	{
		final DictionaryImpl[] oldValue = this.dictionaryArray;
		this.dictionaryArray = newValue;

		propertyChangeListeners.firePropertyChange("dictionaryArray", oldValue, newValue);

		calculateTextArray();
	}

	public synchronized void setIncludeKeys(final boolean newValue)
	{
		final boolean oldValue = this.includeKeys;
		this.includeKeys = newValue;

		propertyChangeListeners.firePropertyChange("includeKeys", new Boolean(oldValue), new Boolean(newValue));
	}

	public synchronized void setKeyOrder(final String[] newValue)
	{
		final String[] oldValue = this.keyOrder;
		this.keyOrder = newValue;

		propertyChangeListeners.firePropertyChange("keyOrder", oldValue, newValue);

		calculateText();
		calculateTextArray();
	}

	protected void calculateText()
	{
		final DictionaryImpl newValue = getDictionary();

		if (newValue != null)
		{
			final String newText = constructText(newValue);
			setText(newText);
		}
	}

	protected void calculateTextArray()
	{
		final DictionaryImpl[] dictionaryArray = getDictionaryArray();

		if (dictionaryArray != null)
		{
			final String[] newText = new String[dictionaryArray.length];

			for (int i = 0; i < newText.length; i++)
			{
				newText[i] = constructText(dictionaryArray[i]);
			}

			setTextArray(newText);
		}
	}

	protected String constructText(final DictionaryImpl newValue)
	{
		if ((keyOrder == null) || (newValue == null)) { return null; }

		String textSoFar = "";
		final boolean shouldIncludeKey = getIncludeKeys();

		// iterate through all keys in dictionary,
		// generating a string to represent the dictionary

		// hashtables are easier to work with than dictionaries
		final Hashtable hash = newValue.getHashtable();

		for (final String element : keyOrder)
		{
			if (hash.containsKey(element))
			{
				if (shouldIncludeKey)
				{
					textSoFar = textSoFar + " " + element;
				}

				final Object value = hash.get(element);

				if (value instanceof String)
				{
					textSoFar = textSoFar + " " + ((String) value);
				}
				else
				{
					if (value instanceof StringBoxImpl)
					{
						textSoFar = textSoFar + " " + ((StringBoxImpl) value).value;
					}
					else
					{
						textSoFar = textSoFar + " " + (value.toString());
					}
				}
			}
		}
		return textSoFar;
	}

	protected synchronized void setText(final String newValue)
	{
		final String oldValue = this.text;
		this.text = newValue;

		propertyChangeListeners.firePropertyChange("text", oldValue, newValue);
	}

	protected synchronized void setTextArray(final String[] newValue)
	{
		final String[] oldValue = this.textArray;
		this.textArray = newValue;

		propertyChangeListeners.firePropertyChange("textArray", oldValue, newValue);
	}
}
