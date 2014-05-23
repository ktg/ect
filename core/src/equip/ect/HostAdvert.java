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

 */
package equip.ect;

import equip.data.GUID;
import equip.data.StringBoxImpl;
import equip.data.TupleImpl;

public class HostAdvert extends CompInfo
{

	public static final String TYPE = "HostAdvert2";
	public static final int NO_OF_FIELDS = 3;
	public static final int NAME_INDEX = 2;
	public static final int HOST_TYPE_INDEX = 3;
	public static final int HOSTID_INDEX = 4;

	public HostAdvert(final GUID id)
	{
		super(id, TYPE, NO_OF_FIELDS);
	}

	public HostAdvert(final TupleImpl tupleImpl)
	{
		super(tupleImpl);
	}

	public GUID getHostID()
	{
		if (tuple.fields.length >= (HOSTID_INDEX + 1))
		{
			return (GUID) tuple.fields[HOSTID_INDEX];
		}
		else
		{
			return null;
		}
	}

	public String getHostName()
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

	public String getHostType()
	{
		if (tuple.fields.length >= (HOST_TYPE_INDEX + 1))
		{
			return ((StringBoxImpl) tuple.fields[HOST_TYPE_INDEX]).value;
		}
		else
		{
			return null;
		}
	}

	public void setHostID(final GUID hostID)
	{
		if (tuple.fields.length >= (HOSTID_INDEX + 1))
		{
			tuple.fields[HOSTID_INDEX] = hostID;
		}
	}

	public void setHostName(final String name)
	{
		if (tuple.fields.length >= (NAME_INDEX + 1))
		{
			tuple.fields[NAME_INDEX] = new StringBoxImpl(name);
		}
	}

	public void setHostType(final String hosttype)
	{
		if (tuple.fields.length >= (HOST_TYPE_INDEX + 1))
		{
			tuple.fields[HOST_TYPE_INDEX] = new StringBoxImpl(hosttype);
		}
	}

}