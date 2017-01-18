/*
 <COPYRIGHT>

 Copyright (c) 2004-2006, University of Nottingham
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

 Created by: Shahram Izadi (University of Nottingham)
 Contributors:
 Chris Greenhalgh (University of Nottingham)
 Shahram Izadi (University of Nottingham)
 Jan Humble (University of Nottingham)
 */
package equip.ect;

import java.io.IOException;

import equip.data.BooleanBox;
import equip.data.BooleanBoxImpl;
import equip.data.GUID;
import equip.data.ItemData;
import equip.data.StringBoxImpl;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;
import equip.runtime.ValueBase;

public class ComponentProperty extends CompInfo
{

	public static final String TYPE = "ComponentProperty3";
	public static final int NO_OF_FIELDS = 5;
	public static final int NAME_INDEX = 2;
	public static final int CLASS_INDEX = 3;
	public static final int VAL_INDEX = 4;
	public static final int ID_INDEX = 5;
	public static final int TYPE_INDEX = 6;

	/**
	 * standard connection point types
	 */
	public static final String CONNECTION_POINT_PROPERTY_VALUE = "propertyvalue";
	public static final String CONNECTION_POINT_PROPERTY_REFERENCE = "propertyreference";

	// dataspace??

	public ComponentProperty(final GUID id)
	{
		super(id, TYPE, NO_OF_FIELDS);
		// default connection point type?!
	}

	public ComponentProperty(final TupleImpl tuple)
	{
		super(tuple);
	}

	public ComponentProperty cloneAsComponentProperty()
	{
		final TupleImpl tup = new TupleImpl();
		tup.fields = new equip.runtime.ValueBase[tuple.fields.length];
		System.arraycopy(tuple.fields, 0, tup.fields, 0, tuple.fields.length);
		tup.name = tuple.name;
		tup.id = tuple.id;
		return new ComponentProperty(tup);
	}

	/**
	 * like CompInfo copy collect, but wrap tuples into ComponentProperty objects
	 */
	public ComponentProperty[] copyCollectAsComponentProperty(final DataspaceBean dataspace)
			throws DataspaceInactiveException
	{

		final equip.data.ItemData[] ret = dataspace.copyCollect(this.tuple);
		if (ret != null)
		{
			final ComponentProperty[] returnvals = new ComponentProperty[ret.length];

			for (int i = 0; i < ret.length; i++)
			{
				returnvals[i] = new ComponentProperty((TupleImpl) ret[i]);
			}
			return returnvals;
		}
		else
		{
			return null;
		}
	}

	public GUID getComponentID()
	{
		if (tuple.fields.length >= (ID_INDEX + 1))
		{
			return (GUID) tuple.fields[ID_INDEX];
		}
		else
		{
			return null;
		}
	}

	public String getConnectionPointType()
	{
		if (tuple.fields.length >= (TYPE_INDEX + 1))
		{
			if (tuple.fields[TYPE_INDEX] == null) { return null; }
			return ((StringBoxImpl) tuple.fields[TYPE_INDEX]).value;
		}
		else
		{
			return null;
		}
	}

	public String getPropertyClass()
	{
		if (tuple.fields.length >= (CLASS_INDEX + 1))
		{
			return ((StringBoxImpl) tuple.fields[CLASS_INDEX]).value;
		}
		else
		{
			return null;
		}
	}

	public String getPropertyName()
	{
		if (tuple.fields.length >= (NAME_INDEX + 1))
		{
			return ((StringBoxImpl) tuple.fields[NAME_INDEX]).value;
		}
		else
		{
			return null;
		}
	}

	public GUID getPropertyReference() throws ConnectionPointTypeException
	{
		final String type = getConnectionPointType();
		if (type == null || !type.equals(CONNECTION_POINT_PROPERTY_REFERENCE))
		{
			// return null;
			throw new ConnectionPointTypeException("getPropertyReference not allowed on " + type);
		}
		if (tuple.fields.length >= (VAL_INDEX + 1))
		{
			return (GUID) tuple.fields[VAL_INDEX];
		}
		else
		{
			return null;
		}
	}

	public ValueBase getPropertyValue() throws ConnectionPointTypeException
	{
		final String type = getConnectionPointType();
		if (type == null || !type.equals(CONNECTION_POINT_PROPERTY_VALUE))
		{
			// return null;
			throw new ConnectionPointTypeException("getPropertyValue not allowed on " + type);
		}
		if (tuple.fields.length >= (VAL_INDEX + 1))
		{
			return tuple.fields[VAL_INDEX];
		}
		else
		{
			return null;
		}
	}

	/**
	 * try chasing references (if any)
	 */
	public ValueBase getPropertyValue(final DataspaceBean dataspace) throws ConnectionPointTypeException
	{
		final String type = getConnectionPointType();
		if (type != null && type.equals(CONNECTION_POINT_PROPERTY_REFERENCE))
		{
			final GUID ref = getPropertyReference();
			if (ref == null) { return null; }
			ItemData item = null;
			try
			{
				item = dataspace.getItem(ref);
			}
			catch (final DataspaceInactiveException e)
			{
			}
			if (item == null)
			{
				System.out.println("WARNING: property reference " + ref + " not found in local dataspace");
				return null;
			}
			if (item instanceof TupleImpl && item.name.equals(TYPE))
			{
				final ComponentProperty pref = new ComponentProperty((TupleImpl) item);
				return pref.getPropertyValue(dataspace);
			}
		}
		// normal getter
		return getPropertyValue();
	}

	public String getPropertyValueAsString() throws ConnectionPointTypeException
	{
		try
		{
			final Object obj = getPropertyValue();
			if (obj != null)
			{
				final Object ovalue = Coerce.toClass(obj, String.class);

				final String value = ovalue == null ? null : ((ovalue instanceof String) ? (String) ovalue : ovalue
						.toString());
				return value;
			}
			else
			{
				return null;
			}
		}
		catch (final ConnectionPointTypeException cpte)
		{
			throw cpte;
		}
		catch (final Exception cnfe)
		{
			System.err.println("Warning: Could not get value for property '" + getPropertyName() + "'");
			cnfe.printStackTrace();
			return "";
		}
	}

	public boolean isReadonly()
	{

		final equip.runtime.ValueBase v = getAttributeValue("readonly");
		if (v != null && (v instanceof BooleanBox)) { return ((BooleanBox) v).value; }
		return false;
	}

	// containerID
	public void setComponentID(final GUID containerID)
	{
		if (tuple.fields.length >= (ID_INDEX + 1))
		{
			tuple.fields[ID_INDEX] = containerID;
		}
	}

	public void setConnectionPointType(final String name)
	{
		if (tuple.fields.length >= (TYPE_INDEX + 1))
		{
			tuple.fields[TYPE_INDEX] = new StringBoxImpl(name);
		}
	}

	public void setPropertyClass(final Class<?> propclass)
	{
		if (tuple.fields.length >= (CLASS_INDEX + 1))
		{
			tuple.fields[CLASS_INDEX] = new StringBoxImpl(propclass.getName());
		}
	}

	public void setPropertyClass(final String classAsStr)
	{
		if (tuple.fields.length >= (CLASS_INDEX + 1))
		{
			tuple.fields[CLASS_INDEX] = new StringBoxImpl(classAsStr);
		}
	}

	public void setPropertyName(final String name)
	{
		if (tuple.fields.length >= (NAME_INDEX + 1))
		{
			tuple.fields[NAME_INDEX] = new StringBoxImpl(name);
		}
	}

	public void setPropertyReference(final GUID ref) throws ConnectionPointTypeException
	{
		final String type = getConnectionPointType();
		if (type == null || !type.equals(CONNECTION_POINT_PROPERTY_REFERENCE)) { throw new ConnectionPointTypeException(
				"setPropertyReference not allowed on " + type); }
		if (tuple.fields.length >= (VAL_INDEX + 1))
		{
			tuple.fields[VAL_INDEX] = ref;
		}
	}

	public void setPropertyValue(final Object value) throws ClassNotFoundException, IOException,
			ConnectionPointTypeException
	{
		final String type = getConnectionPointType();
		if (type == null)
		{
			// default
			setConnectionPointType(CONNECTION_POINT_PROPERTY_VALUE);
		}
		else if (!type.equals(CONNECTION_POINT_PROPERTY_VALUE)) { throw new ConnectionPointTypeException(
				"setPropertyValue not allowed on " + type); }
		if (tuple.fields.length >= (VAL_INDEX + 1))
		{
			tuple.fields[VAL_INDEX] = Coerce.toClass(value, equip.runtime.ValueBase.class);
		}
	}

	public void setReadonly(final boolean ro)
	{
		forceSetAttribute("readonly", new BooleanBoxImpl(ro));
	}
}