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
import equip.data.IntBoxImpl;

public class PropertyLinkResponse extends CompInfo
{

	public static final String TYPE = "PropertyLinkResponse2";
	public static final int NO_OF_FIELDS = 4;
	public static final int LINKREQID_INDEX = 2;
	public static final int SOURCEPROPID_INDEX = 3;
	public static final int DESTPROPID_INDEX = 4;
	public static final int ACTIVATION_INDEX = 5;

	public PropertyLinkResponse(final GUID id)
	{
		super(id, TYPE, NO_OF_FIELDS);
	}

	public int getActivationCount()
	{
		if (tuple.fields.length >= (ACTIVATION_INDEX + 1))
		{
			return ((IntBoxImpl) tuple.fields[ACTIVATION_INDEX]).value;
		}
		else
		{
			return -1;
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

	public GUID getLinkRequestID()
	{
		if (tuple.fields.length >= (LINKREQID_INDEX + 1))
		{
			return ((GUID) tuple.fields[LINKREQID_INDEX]);
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

	public void setActivationCount(final int count)
	{
		if (tuple.fields.length >= (ACTIVATION_INDEX + 1))
		{
			tuple.fields[ACTIVATION_INDEX] = new IntBoxImpl(count);
		}
	}

	public void setDestinationPropID(final GUID compID)
	{
		if (tuple.fields.length >= (DESTPROPID_INDEX + 1))
		{
			tuple.fields[DESTPROPID_INDEX] = compID;
		}
	}

	public void setLinkRequestID(final GUID compID)
	{
		if (tuple.fields.length >= (LINKREQID_INDEX + 1))
		{
			tuple.fields[LINKREQID_INDEX] = compID;
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