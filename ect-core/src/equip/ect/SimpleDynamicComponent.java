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

 */
package equip.ect;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * a simple dynamic component, using DynamicPropertySupport for properties.
 */
public class SimpleDynamicComponent implements Serializable, DynamicProperties
{
	/**
	 * property change support
	 */
	private PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	/**
	 * dynamic properties support
	 */
	protected DynamicPropertiesSupport dynsup;

	/**
	 * cons
	 */
	public SimpleDynamicComponent()
	{
		dynsup = new DynamicPropertiesSupport(propertyChangeListeners);
	}

	/**
	 * add property
	 */
	public synchronized void addProperty(final String name, final Class<?> clazz, final Object value)
	{
		dynsup.addProperty(name, clazz, value);
	}

	/**
	 * add property
	 */
	public synchronized void addProperty(final String name, final Class<?> clazz, final Object value,
			final boolean readonly)
	{
		dynsup.addProperty(name, clazz, value, readonly);
	}

	/**
	 * property listener support
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * get all properties' {@link DynamicPropertyDescriptors}
	 */
	@Override
	public DynamicPropertyDescriptor[] dynamicProperties()
	{
		return dynsup.dynamicProperties();
	}

	/**
	 * get one property by name
	 */
	@Override
	public Object getDynamicProperty(final String name) throws NoSuchPropertyException
	{
		return dynsup.getDynamicProperty(name);
	}

	/**
	 * get one property by name
	 */
	@Override
	public void setDynamicProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		dynsup.setDynamicProperty(name, value);
	}

	/**
	 * property listener support
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}
}
