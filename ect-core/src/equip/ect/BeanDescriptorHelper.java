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
package equip.ect;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.util.Enumeration;

import equip.data.BooleanBoxImpl;
import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;

/**
 * helper class for publishing BeanDescriptor info and similar. Chris Greenhalgh 2005-04-24
 */
public class BeanDescriptorHelper
{
	/**
	 * Dictionary property name
	 */
	static final public String NAME = "name";
	/**
	 * Dictionary property name
	 */
	static final public String DISPLAY_NAME = "displayName";
	/**
	 * Dictionary property name
	 */
	static final public String SHORT_DESCRIPTION = "shortDescription";
	/**
	 * Dictionary property name
	 */
	static final public String EXPERT = "expert";
	/**
	 * Dictionary property name
	 */
	static final public String PREFERRED = "preferred";
	/**
	 * Dictionary property name
	 */
	static final public String HIDDEN = "hidden";
	/**
	 * Dictionary property name
	 */
	static final public String TYPE = "type";
	/**
	 * Dictionary property name
	 */
	static final public String PROPERTIES = "properties";

	/**
	 * copy info to CompInfo's attributes
	 */
	static public void copyInformation(final BeanInfo bi, final CompInfo ci, final boolean includeProperties)
	{
		final DictionaryImpl d = (DictionaryImpl) (ci.tuple.fields[CompInfo.ATTRIBUTES_INDEX]);
		final BeanDescriptor bd = bi.getBeanDescriptor();
		copyInformation(bd, d);
		if (includeProperties)
		{
			d.put(PROPERTIES, describeProperties(bi.getPropertyDescriptors()));
		}
	}

	/**
	 * populate Dictionary with info from a FeatureDescriptor
	 */
	static public void copyInformation(final FeatureDescriptor fd, final DictionaryImpl d)
	{
		if (fd == null || d == null) { return; }
		if (fd.getName() != null)
		{
			d.put(NAME, new StringBoxImpl(fd.getName()));
		}
		if (fd.getDisplayName() != null)
		{
			d.put(DISPLAY_NAME, new StringBoxImpl(fd.getDisplayName()));
		}
		if (fd.getShortDescription() != null)
		{

			d.put(SHORT_DESCRIPTION, new StringBoxImpl(fd.getShortDescription()));
		}
		d.put(EXPERT, new BooleanBoxImpl(fd.isExpert()));
		d.put(PREFERRED, new BooleanBoxImpl(fd.isPreferred()));
		d.put(HIDDEN, new BooleanBoxImpl(fd.isHidden()));
		final Enumeration<String> names = fd.attributeNames();
		while (names.hasMoreElements())
		{
			final String name = names.nextElement();
			final Object value = fd.getValue(name);
			if (value != null)
			{
				d.put(name, new StringBoxImpl(value.toString()));
			}
		}
		if (fd instanceof PropertyDescriptor)
		{
			d.put(TYPE, new StringBoxImpl(((PropertyDescriptor) fd).getPropertyType().getName()));
		}
	}

	/**
	 * copy info to CompInfo's attributes
	 */
	static public void copyInformation(final PropertyDescriptor fd, final CompInfo ci)
	{
		final DictionaryImpl d = (DictionaryImpl) (ci.tuple.fields[CompInfo.ATTRIBUTES_INDEX]);
		copyInformation(fd, d);
	}

	/**
	 * create Dictionary representing property array
	 */
	static public DictionaryImpl describeProperties(final PropertyDescriptor ps[])
	{
		final DictionaryImpl d = new DictionaryImpl();
		for (int pi = 0; ps != null && pi < ps.length; pi++)
		{
			final DictionaryImpl dp = new DictionaryImpl();
			copyInformation(ps[pi], dp);
			d.put(ps[pi].getName(), dp);
		}
		return d;
	}

	/**
	 * add standard information for a parent pseudo-property
	 */
	static public void parentProperty(final CompInfo ci)
	{
		final DictionaryImpl d = (DictionaryImpl) (ci.tuple.fields[CompInfo.ATTRIBUTES_INDEX]);
		d.put(PREFERRED, new BooleanBoxImpl(false));
		d.put(EXPERT, new BooleanBoxImpl(true));
		d.put(HIDDEN, new BooleanBoxImpl(true));
		d.put(NAME, new StringBoxImpl("parent"));
		d.put(DISPLAY_NAME, new StringBoxImpl("Parent GUID"));
		d.put(SHORT_DESCRIPTION, new StringBoxImpl("The unique ID (GUID) of this sub-component's parent component"));
	}
}
