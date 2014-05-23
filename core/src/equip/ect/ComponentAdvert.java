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

 */
package equip.ect;

import equip.data.GUID;
import equip.data.StringBoxImpl;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;

public class ComponentAdvert extends CompInfo
{

	public static final String TYPE = "ComponentAdvert2";

	public static final int NO_OF_FIELDS = 6;

	public static final int NAME_INDEX = 2;
	public static final int HOSTID_INDEX = 3;
	public static final int CONTAINERID_INDEX = 4;
	public static final int COMPID_INDEX = 5;
	public static final int CAPABILITY_ID_INDEX = 6;
	public static final int COMPONENT_REQUEST_ID_INDEX = 7;

	public static final String COMPOUND_COMPONENT_HOST_ID = "CompoundComponent";

	public ComponentAdvert(final GUID id)
	{
		super(id, TYPE, NO_OF_FIELDS);
	}

	public ComponentAdvert(final TupleImpl tuple)
	{
		super(tuple);
	}

	public ComponentAdvert[] copyCollectAsComponentAdvert(final DataspaceBean dataspace)
			throws DataspaceInactiveException
	{

		final equip.data.ItemData[] ret = dataspace.copyCollect(this.tuple);
		if (ret != null)
		{
			final ComponentAdvert[] returnvals = new ComponentAdvert[ret.length];

			for (int i = 0; i < ret.length; i++)
			{
				returnvals[i] = new ComponentAdvert((TupleImpl) ret[i]);
			}
			return returnvals;
		}
		else
		{
			return null;
		}
	}

	public GUID getCapabilityID()
	{
		if (tuple.fields.length >= (CAPABILITY_ID_INDEX + 1))
		{
			return ((GUID) tuple.fields[CAPABILITY_ID_INDEX]);
		}
		else
		{
			return null;
		}

	}

	public GUID getComponentID()
	{
		if (tuple.fields.length >= (COMPID_INDEX + 1))
		{
			return ((GUID) tuple.fields[COMPID_INDEX]);
		}
		else
		{
			return null;
		}

	}

	public String getComponentName()
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

	public GUID getComponentRequestID()
	{
		if (tuple.fields.length >= (COMPONENT_REQUEST_ID_INDEX + 1))
		{
			return ((GUID) tuple.fields[COMPONENT_REQUEST_ID_INDEX]);
		}
		else
		{
			return null;
		}

	}

	public GUID getContainerID()
	{
		if (tuple.fields.length >= (CONTAINERID_INDEX + 1))
		{
			return (GUID) tuple.fields[CONTAINERID_INDEX];
		}
		else
		{
			return null;
		}
	}

	public String getHostID()
	{
		if (tuple.fields.length >= (HOSTID_INDEX + 1))
		{
			return ((StringBoxImpl) tuple.fields[HOSTID_INDEX]).value;
		}
		else
		{
			return null;
		}
	}

	public void setCapabilityID(final GUID capId)
	{
		if (tuple.fields.length >= (CAPABILITY_ID_INDEX + 1))
		{
			tuple.fields[CAPABILITY_ID_INDEX] = capId;
		}
	}

	public void setComponentID(final GUID beanid)
	{
		if (tuple.fields.length >= (COMPID_INDEX + 1))
		{
			tuple.fields[COMPID_INDEX] = beanid;
		}
	}

	public void setComponentName(final String name)
	{
		if (tuple.fields.length >= (NAME_INDEX + 1))
		{
			tuple.fields[NAME_INDEX] = new StringBoxImpl(name);
		}
	}

	public void setComponentRequestID(final GUID compReqId)
	{
		if (tuple.fields.length >= (COMPONENT_REQUEST_ID_INDEX + 1))
		{
			tuple.fields[COMPONENT_REQUEST_ID_INDEX] = compReqId;
		}
	}

	public void setContainerID(final GUID containerID)
	{
		if (tuple.fields.length >= (CONTAINERID_INDEX + 1))
		{
			tuple.fields[CONTAINERID_INDEX] = containerID;
		}
	}

	public void setHostID(final String hostID)
	{
		if (tuple.fields.length >= (HOSTID_INDEX + 1))
		{
			tuple.fields[HOSTID_INDEX] = new StringBoxImpl(hostID);
		}
	}

}