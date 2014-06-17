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

Created by: Ian Taylor (University of Nottingham)
Contributors:
  Shahram Izadi (University of Nottingham)
  Ian Taylor (University of Nottingham)
 */
package equip.ect;

import equip.data.GUID;
import equip.data.StringBoxImpl;
import equip.data.Tuple;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;

public class Requester
{

	public static void main(final String[] args)
	{

		final String testSpace = "equip://:9123/";
		new Requester(testSpace);

	}

	DataspaceBean equipDSBean = new DataspaceBean();

	public Requester(final String dataspaceURL)
	{
		try
		{
			equipDSBean.setDataspaceUrl(dataspaceURL);
		}
		catch (final Exception e)
		{
			e.printStackTrace();

		}

		publishRequest("[128.243.22.24:0.2426:1:1076083432]", "ChatBean");
		// publishRequest("component", "chat");

	}

	public void publishRequest(final String hostID, final String name)
	{

		// Build Request Tuple
		final Tuple item = new equip.data.TupleImpl(new StringBoxImpl("componentRequest"), new StringBoxImpl(hostID),
				new StringBoxImpl("name:" + name));

		final GUID newGUID = this.equipDSBean.allocateId();
		item.id = newGUID;
		item.name = "componentRequest";

		// add to the Dataspace

		try
		{
			equipDSBean.add(item);
		}
		catch (final DataspaceInactiveException ex)
		{
			ex.printStackTrace();
		}
	}

}
