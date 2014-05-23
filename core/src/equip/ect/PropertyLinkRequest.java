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

public class PropertyLinkRequest extends CompInfo
{

	public static final String TYPE = "PropertyLinkRequest2";
	public static final int NO_OF_FIELDS = 6;
	public static final int SOURCEPROPNAME_INDEX = 2;
	public static final int SOURCEPROPID_INDEX = 3;
	public static final int SOURCECOMPID_INDEX = 4;
	public static final int DESTPROPNAME_INDEX = 5;
	public static final int DESTPROPID_INDEX = 6;
	public static final int DESTCOMPID_INDEX = 7;

	public PropertyLinkRequest(final GUID id)
	{
		super(id, TYPE, NO_OF_FIELDS);
	}

	public PropertyLinkRequest(final TupleImpl tuple)
	{
		super(tuple);
	}

	public GUID getDestComponentID()
	{
		if (tuple.fields.length >= (DESTCOMPID_INDEX + 1))
		{
			return ((GUID) tuple.fields[DESTCOMPID_INDEX]);
		}
		else
		{
			return null;
		}
	}

	public String getDestinationPropertyName()
	{
		if (tuple.fields.length >= (DESTPROPNAME_INDEX + 1))
		{
			return ((StringBoxImpl) tuple.fields[DESTPROPNAME_INDEX]).value;
		}
		else
		{
			return null;
		}
	}

	public GUID getDestinationPropID()
	{
		if (tuple.fields.length >= (DESTPROPID_INDEX + 1))
		{
			return ((GUID) tuple.fields[DESTPROPID_INDEX]);
		}
		else
		{
			return null;
		}
	}

	public GUID getSourceComponentID()
	{
		if (tuple.fields.length >= (SOURCECOMPID_INDEX + 1))
		{
			return ((GUID) tuple.fields[SOURCECOMPID_INDEX]);
		}
		else
		{
			return null;
		}
	}

	public String getSourcePropertyName()
	{
		if (tuple.fields.length >= (SOURCEPROPNAME_INDEX + 1))
		{
			return ((StringBoxImpl) tuple.fields[SOURCEPROPNAME_INDEX]).value;
		}
		else
		{
			return null;
		}
	}

	public GUID getSourcePropID()
	{
		if (tuple.fields.length >= (SOURCEPROPID_INDEX + 1))
		{
			return ((GUID) tuple.fields[SOURCEPROPID_INDEX]);
		}
		else
		{
			return null;
		}
	}

	public void setDestComponentID(final GUID compID)
	{
		if (tuple.fields.length >= (DESTCOMPID_INDEX + 1))
		{
			tuple.fields[DESTCOMPID_INDEX] = compID;
		}
	}

	public void setDestinationPropertyName(final String name)
	{
		if (tuple.fields.length >= (DESTPROPNAME_INDEX + 1))
		{
			tuple.fields[DESTPROPNAME_INDEX] = new StringBoxImpl(name);
		}
	}

	public void setDestinationPropID(final GUID compID)
	{
		if (tuple.fields.length >= (DESTPROPID_INDEX + 1))
		{
			tuple.fields[DESTPROPID_INDEX] = compID;
		}
	}

	public void setSourceComponentID(final GUID compID)
	{
		if (tuple.fields.length >= (SOURCECOMPID_INDEX + 1))
		{
			tuple.fields[SOURCECOMPID_INDEX] = compID;
		}
	}

	public void setSourcePropertyName(final String name)
	{
		if (tuple.fields.length >= (SOURCEPROPNAME_INDEX + 1))
		{
			tuple.fields[SOURCEPROPNAME_INDEX] = new StringBoxImpl(name);
		}
	}

	public void setSourcePropID(final GUID compID)
	{
		if (tuple.fields.length >= (SOURCEPROPID_INDEX + 1))
		{
			tuple.fields[SOURCEPROPID_INDEX] = compID;
		}
	}
}