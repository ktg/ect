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
package equip.ect;

import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * defualt helper implementation of {@link DynamicProperties}.
 */
public class DynamicPropertiesSupport implements DynamicProperties
{
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners;

	/**
	 * properties
	 */
	protected Map<String, Object> properties = new HashMap<String, Object>();

	/**
	 * descriptors
	 */
	protected Map<String, DynamicPropertyDescriptor> descriptors = new HashMap<String, DynamicPropertyDescriptor>();

	/**
	 * cons
	 */
	public DynamicPropertiesSupport(final PropertyChangeSupport propertyChangeListeners)
	{
		this.propertyChangeListeners = propertyChangeListeners;
	}

	/**
	 * add property
	 */
	public synchronized void addProperty(final String name, final Class<?> clazz, final Object value)
	{
		addProperty(name, clazz, value, false);
	}

	/**
	 * add property
	 */
	public synchronized void addProperty(final String name, final Class<?> clazz, final Object value,
			final boolean readonly)
	{
		try
		{
			final DynamicPropertyDescriptor desc = new DynamicPropertyDescriptor(name, clazz, readonly);
			descriptors.put(name, desc);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR creating DynamicPropertyDescriptor \"" + name + "\": " + e);
			e.printStackTrace(System.err);
		}
		Object old = null;
		if (value == null)
		{
			old = properties.remove(name);
		}
		else
		{
			old = properties.put(name, value);
		}
		// notify
		notifyChange(name, old, value);
	}

	/**
	 * get all properties
	 */
	@Override
	public synchronized DynamicPropertyDescriptor[] dynGetProperties()
	{
		return (DynamicPropertyDescriptor[]) descriptors.values().toArray(	new DynamicPropertyDescriptor[descriptors
																					.size()]);
	}

	/**
	 * get one property by name
	 */
	@Override
	public synchronized Object dynGetProperty(final String name) throws NoSuchPropertyException
	{
		if (!descriptors.containsKey(name)) { throw new NoSuchPropertyException("Property \"" + name + "\""); }
		return properties.get(name);
	}

	/**
	 * get one property by name
	 */
	@Override
	public synchronized void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		if (!descriptors.containsKey(name)) { throw new NoSuchPropertyException("Property \"" + name + "\""); }
		final Object old = properties.put(name, value);
		// notify
		notifyChange(name, old, value);
	}

	/**
	 * get one property by name
	 */
	public synchronized void dynSetProperty(final String name, final Object value, final boolean forceNotify)
			throws NoSuchPropertyException
	{
		if (!descriptors.containsKey(name)) { throw new NoSuchPropertyException("Property \"" + name + "\""); }
		final Object old = properties.put(name, value);
		// notify
		notifyChange(name, forceNotify ? null : old, value);
	}

	/**
	 * remove property
	 */
	public synchronized void removeProperty(final String name) throws NoSuchPropertyException
	{
		if (!descriptors.containsKey(name)) { throw new NoSuchPropertyException("Property \"" + name + "\""); }
		descriptors.remove(name);
		final Object old = properties.remove(name);
		// notify
		notifyChange(name, old, null);
	}

	/**
	 * change
	 */
	protected void notifyChange(final String name, final Object old, final Object now)
	{
		propertyChangeListeners.firePropertyChange(name, old, now);
	}
}
