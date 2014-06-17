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

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Shahram Izadi (University of Nottingham)
  Jan Humble (University of Nottingham)
  Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect;

import equip.data.*;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceEventListener;
import equip.data.beans.DataspaceInactiveException;

import java.io.IOException;
import java.io.Serializable;

public class CompInfo
{
	public Tuple tuple = new TupleImpl();

	public static final int TYPE_INDEX = 0;
	public static final int ATTRIBUTES_INDEX = 1;

	public CompInfo()
	{
		tuple = new TupleImpl();
	}

	public CompInfo(final GUID id, final String type, final int numFields)
	{
		tuple.id = id;
		tuple.name = type;

		tuple.fields = new equip.runtime.ValueBase[numFields + 2];
		tuple.fields[TYPE_INDEX] = new StringBoxImpl(type);
		tuple.fields[ATTRIBUTES_INDEX] = new DictionaryImpl();
	}

	public CompInfo(final TupleImpl tuple)
	{
		this.tuple = tuple;
	}

	public DataSession addPatterntoDataSpace(final DataspaceBean dataspace, final DataspaceEventListener eventListner)
			throws DataspaceInactiveException
	{
		return dataspace.addDataspaceEventListener(tuple, false, eventListner);
	}

	public void addtoDataSpace(final DataspaceBean dataspace) throws DataspaceInactiveException
	{
		dataspace.add(tuple);
	}

	public void addtoDataSpacePersistent(final DataspaceBean dataspace, final Lease lease)
			throws DataspaceInactiveException
	{
		dataspace.addPersistent(tuple, lease);
	}

	public CompInfo[] copyCollect(final DataspaceBean dataspace) throws DataspaceInactiveException
	{

		final ItemData[] ret = dataspace.copyCollect(this.tuple);
		if (ret != null)
		{
			final CompInfo[] returnvals = new CompInfo[ret.length];

			for (int i = 0; i < ret.length; i++)
			{
				returnvals[i] = new CompInfo((TupleImpl) ret[i]);
			}
			return returnvals;
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean equals(final Object other)
	{
		if (other != null && other instanceof CompInfo)
		{
			return (this.getID().equals(((CompInfo) other).getID()));
		}
		return false;
	}

	public void forceSetAttribute(final String paramName, final equip.runtime.ValueBase value)
	{
		setAttribute(paramName, value);
	}

	public int getAttributeCount()
	{
		final DictionaryImpl d = (DictionaryImpl) (tuple.fields[ATTRIBUTES_INDEX]);
		return d.entries.length;
	}

	public String getAttributeName(final int paramNo)
	{
		final DictionaryImpl d = (DictionaryImpl) (tuple.fields[ATTRIBUTES_INDEX]);
		if (paramNo < 0 || paramNo >= d.entries.length)
		{
			return null;
		}
		return d.entries[paramNo].name;
	}

	public equip.runtime.ValueBase getAttributeValue(final int paramNo)
	{
		final DictionaryImpl d = (DictionaryImpl) (tuple.fields[ATTRIBUTES_INDEX]);
		if (paramNo < 0 || paramNo >= d.entries.length)
		{
			return null;
		}
		return d.entries[paramNo].value;
	}

	public equip.runtime.ValueBase getAttributeValue(final String name)
	{
		final DictionaryImpl d = (DictionaryImpl) (tuple.fields[ATTRIBUTES_INDEX]);
		return d.get(name);
	}

	public GUID getID()
	{
		return this.tuple.id;
	}

	public String getType()
	{
		return this.tuple.name;
	}

	public void setAttribute(final String paramName, final equip.runtime.ValueBase value)
	{

		((DictionaryImpl) (tuple.fields[ATTRIBUTES_INDEX])).put(paramName, value);
	}

	public void setAttribute(final String paramName, final Serializable svalue) throws IOException
	{

		((DictionaryImpl) (tuple.fields[ATTRIBUTES_INDEX])).put(paramName, new SerializedObjectImpl(svalue));
	}

	public void setID(final GUID setId)
	{
		this.tuple.id = setId;
	}

	public void setType(final String typeName)
	{
		this.tuple.name = typeName;
	}

	public void updateinDataSpace(final DataspaceBean dataspace) throws DataspaceInactiveException
	{
		// do async
		final UpdateEvent event = new UpdateEventImpl();
		event.initFromItem2(tuple, null/* agent */, false/* local */, false/* unreliable */, 0/* priority */);
		dataspace.getDataProxy().queueEvent(event);
	}
}
