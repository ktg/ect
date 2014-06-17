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
package equip.ect.components.dictionaryarraymerge;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import equip.data.DictionaryImpl;
import equip.data.GUID;
import equip.data.beans.DataspaceBean;
import equip.ect.Coerce;
import equip.ect.ContainerManager;
import equip.ect.IActiveComponent;

/**
 * A component which merges all of the values linked to its input into a single output array.<br>
 * <H3>Description</H3> A component which merges all of the values linked to its input into a single
 * output array.<br>
 * 
 * <H3>Installation</H3> No specific requirements.<br>
 * 
 * <H3>Configuration</H3> No specific requirements.<br>
 * 
 * <H3>Usage</H3> Connect all the desired properties (of type dictionary) to the 'value' property of
 * the component. The 'value' property of the component will contain a dictionary array containing
 * all the dictionary values connected to the 'value' property of the component. Note that ECT
 * built-in coercions will map any non-dictionary value to a dictionary with a single element called
 * "value" (and vice versa). Note that the input merging behaviour will mean that the value(s)
 * cannot be effectively set using the graphical editors (the specified value will just be
 * temporarily merged into the value array).<br>
 * 
 * <H3>Technical Details</H3> Uses facilities of ect.IActiveComponent to monitor all
 * concurrent incoming links in order to combine values, rather than relying on default calling of
 * value setter.<br>
 * 
 * @classification Data/Dictionary
 * @defaultInputProperty value
 * @defaultOutputProperty value
 * @preferred
 */
public class DictionaryArrayMerge implements Serializable, IActiveComponent
{
	static boolean debug = false;
	/**
	 * current requests to merge and their most recent values
	 */
	protected Hashtable valueRequests = new Hashtable();
	/**
	 * input/output value
	 */
	protected DictionaryImpl value[];
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public DictionaryArrayMerge()
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
	 * The input/output value merge point.
	 */
	public synchronized DictionaryImpl[] getValue()
	{
		return value;
	}

	/**
	 * initialise - IActiveComponent
	 */
	@Override
	public void initialise(final ContainerManager cmgr, final DataspaceBean dataspace)
	{
		// noop
	}

	/**
	 * IActiveComponent - unused - property link request added to this component
	 */
	@Override
	public void linkToAdded(final String propertyName, final GUID requestId)
	{
		if (debug)
		{
			System.out.println("DictionaryArrayMerge: linkToAdded(" + propertyName + ", " + requestId + ")");
		}
	}

	/**
	 * IActiveComponent - unused - property link request added to this component
	 */
	@Override
	public void linkToDeleted(final String propertyName, final GUID requestId)
	{
		if (debug)
		{
			System.out.println("DictionaryArrayMerge: linkToDeleted(" + propertyName + ", " + requestId + ")");
		}
	}

	/**
	 * IActiveComponent - unused - attempt to set a property due to a link add / source update
	 */
	@Override
	public boolean linkToUpdated(final String propertyName, final GUID requestId, final Object value)
	{
		try
		{
			if (debug)
			{
				System.out.println("DictionaryArrayMerge: linkToUpdated(" + propertyName + ", " + requestId + ", ("
						+ (value != null ? value.getClass().getName() : "null") + ")"
						+ Coerce.toClass(value, String.class) + ")");
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR in DictionaryArrayMerge.linkToUpdated debug output: " + e);
		}
		if (!propertyName.equals("value")) { return false; }
		synchronized (this)
		{
			if (value == null)
			{
				// includes delete
				valueRequests.remove(requestId);
			}
			else
			{
				// merge to publish?!
				valueRequests.put(requestId, value);
			}
		}
		mergeValue();
		return true;
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
	 * The input/output value merge point.
	 * 
	 * @preferred
	 */
	public void setValue(final DictionaryImpl value[])
	{
		DictionaryImpl old[] = null;
		synchronized (this)
		{
			// could suppress no-change setting
			// if (input==this.input || (input!=null && this.input!=null &&
			// input.equals(this.input)) return;
			old = this.value;
			this.value = value;
		}
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("value", old, value);
	}

	/**
	 * stop - called when component request is removed.
	 */
	@Override
	public synchronized void stop()
	{
		// noop
	}

	/**
	 * merge values
	 */
	protected void mergeValue()
	{
		final Vector ps = new Vector();
		synchronized (this)
		{
			if (valueRequests.size() == 0)
			{
				// leave it alone
				return;
			}
			final Enumeration ve = valueRequests.elements();
			while (ve.hasMoreElements())
			{
				final DictionaryImpl[] d = (DictionaryImpl[]) ve.nextElement();
				for (final DictionaryImpl element : d)
				{
					if (element != null)
					{
						ps.addElement(element);
					}
				}
			}
		}
		final DictionaryImpl ds[] = (DictionaryImpl[]) ps.toArray(new DictionaryImpl[ps.size()]);
		setValue(ds);
	}
}
