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
package equip.ect.http;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * representation of a set of properties to be shown as fields on a form by
 * {@link SimpleFormHttpServer}. Chris Greenhalgh 2004-06-04
 */
public class SimpleFormModel
{
	/**
	 * property info
	 */
	protected class PropertyInfo
	{
		/**
		 * name
		 */
		String name;
		/**
		 * description
		 */
		String description;
		/**
		 * value
		 */
		Object value;

		/**
		 * cons
		 */
		PropertyInfo(final String name, final String description, final Object value)
		{
			this.name = name;
			this.description = description;
			this.value = value;
		}
	}

	/**
	 * properties - ordered
	 */
	protected LinkedList properties = new LinkedList();

	/**
	 * cons
	 */
	public SimpleFormModel()
	{
	}

	/**
	 * add bool property
	 */
	public void addProperty(final String name, final String description, final boolean value)
	{
		properties.add(new PropertyInfo(name, description, new Boolean(value)));
	}

	/**
	 * add text property
	 */
	public void addProperty(final String name, final String description, final String value)
	{
		properties.add(new PropertyInfo(name, description, value));
	}

	/**
	 * get property value as boolean, default to false
	 */
	public boolean getBooleanValue(final String name)
	{
		final Object v = getValue(name);
		return (v instanceof Boolean) && ((Boolean) v).booleanValue();
	}

	/**
	 * get property description
	 */
	public String getDescription(final String name)
	{
		final PropertyInfo p = getInfo(name);
		if (p != null) { return p.description; }
		return null;
	}

	/**
	 * get property value
	 */
	public Object getValue(final String name)
	{
		final PropertyInfo p = getInfo(name);
		if (p != null) { return p.value; }
		return null;
	}

	/**
	 * property name iterator
	 */
	public Iterator nameIterator()
	{
		final Iterator i = properties.iterator();
		return new Iterator()
		{
			@Override
			public boolean hasNext()
			{
				return i.hasNext();
			}

			@Override
			public Object next()
			{
				final PropertyInfo p = (PropertyInfo) i.next();
				return p.name;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * set boolean property
	 */
	public void setValue(final String name, final boolean value)
	{
		final PropertyInfo p = getInfo(name);
		p.value = new Boolean(value);
	}

	/**
	 * settext property
	 */
	public void setValue(final String name, final String value)
	{
		final PropertyInfo p = getInfo(name);
		p.value = value;
	}

	/**
	 * get property info
	 */
	protected PropertyInfo getInfo(final String name)
	{
		final Iterator i = properties.iterator();
		while (i.hasNext())
		{
			final PropertyInfo p = (PropertyInfo) i.next();
			if (p.name.equals(name)) { return p; }
		}
		return null;
	}
}
