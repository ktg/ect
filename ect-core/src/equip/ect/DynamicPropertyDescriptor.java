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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Implementation of {@link java.beans.PropertyDescriptor} for {@link DynamicProperties}
 * pseudo-properties
 */
public class DynamicPropertyDescriptor extends PropertyDescriptor
{
	/**
	 * dummy bean class
	 */
	static class Fake
	{
		/**
		 * fake getter
		 */
		public Object getFake()
		{
			return null;
		}

		/**
		 * fake setter
		 */
		public void setFake(final Object val)
		{
		}
		/**
		 * get read method
		 */
	}

	protected boolean readonly;
	String name;
	Class<?> clazz;

	/**
	 * cons
	 */
	public DynamicPropertyDescriptor(final String name, final Class<?> clazz) throws java.beans.IntrospectionException
	{
		this(name, clazz, false);
	}

	/**
	 * cons
	 */
	public DynamicPropertyDescriptor(final String name, final Class<?> clazz, final boolean readonly)
			throws java.beans.IntrospectionException
	{
		// point it at us instead
		super(name, Fake.class, "getFake", !readonly ? "setFake" : null);
		this.name = name;
		this.clazz = clazz;
		this.readonly = readonly;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (!(obj instanceof DynamicPropertyDescriptor)) { return false; }
		final DynamicPropertyDescriptor o = (DynamicPropertyDescriptor) obj;
		return o.name.equals(name) && o.clazz.equals(clazz);
	}

	@Override
	public Class<?> getPropertyType()
	{
		return clazz;
	}

	@Override
	public Method getReadMethod()
	{
		try
		{
			return this.getClass().getMethod("readProperty", DynamicProperties.class);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR getting DynamicPropertyDescriptor.setProperty: " + e);
		}
		return null;
	}

	@Override
	public Method getWriteMethod()
	{
		if (readonly) { return null; }
		try
		{
			return this.getClass().getMethod("writeProperty", DynamicProperties.class, Object.class);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR getting DynamicPropertyDescriptor.setProperty: " + e);
		}
		return null;
	}

	public Object readProperty(final DynamicProperties target) throws NoSuchPropertyException
	{
		return target.getDynamicProperty(name);
	}

	public void writeProperty(final DynamicProperties target, final Object value) throws NoSuchPropertyException
	{
		target.setDynamicProperty(name, value);

	}
}
