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
  Tom Rodden (University of Nottingham)
  Shahram Izadi (University of Nottingham)
  Ian Taylor (University of Nottingham)
 */
package equip.ect.discovery;

import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.ContainerManager;

public class StartManager implements Runnable
{

	public static void main(final String[] args)
	{

		final StartManager startManager1 = new StartManager(args[0]);
	}

	DataspaceDiscover disc;

	String componentPath = null;

	public StartManager(final String componentPath)
	{

		this.componentPath = new String(componentPath);
		final Thread myThread = new Thread(this);
		myThread.start();
	}

	public void call(final String[] dataspaceUrls)
	{

		final DataspaceBean dataspace = new DataspaceBean();

		try
		{
			dataspace.setDataspaceUrl(dataspaceUrls[0]);
		}
		catch (final DataspaceInactiveException ex)
		{
			System.err.println("Exporter could not connect to dataspace.");
			System.exit(-1);
		}

		final ContainerManager containerManager1 = new ContainerManager(dataspace, componentPath, "testbane",
				dataspace.allocateId());

	}

	@Override
	public void run()
	{

		disc = new DataspaceDiscover("");
		disc.setHandler(this);

		System.out.println("Running...");
		while (true)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (final InterruptedException e)
			{

			}

		}

	}

}
