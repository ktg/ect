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
  Olov Stahl

 */
package equip.ect.components.ipergobjectproxy.part.j2se;

import equip.ect.components.ipergobjectproxy.part.TestSetPropertyObject;
import org.iperg.platform.core.IpEvent;
import org.iperg.platform.core.IpEventHandler;
import org.iperg.platform.core.IpUrl;
import org.iperg.platform.networking.IpLocalProcess;

/**
 * Test server for TestSetPropertyObject, i.e. remote property setting. Based on
 * ObjectServerTest.java.
 **/
public class TestSetPropertyObjectServer implements IpEventHandler, Runnable
{
	/**
	 * app main
	 */
	public static void main(final String[] args)
	{
		try
		{
			final Thread thread = new Thread(new TestSetPropertyObjectServer());
			thread.start();
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * a single test object
	 */
	TestSetPropertyObject obj;

	/**
	 * process event handler
	 */
	public void handleEvent(final IpEvent event)
	{
		// noop
	}

	@Override
	public void run()
	{
		// non-standard extension
		// IpManager.setDebugLevel(10);

		IpLocalProcess.getInstance().setEventHandler(this);

		try
		{
			// Listen to incoming connections on port 1000. Does not
			// block. Whenever a connection is accepted, our eventCb
			// will be called with a IpProcessEvent argument.
			final IpUrl url = new IpUrl("tcp://:1000");
			IpLocalProcess.getInstance().listen(url);
			System.out.println("Listening to address " + url);
			// Also listen for Bluetooth connections
			// url = new IpUrl("btspp://localhost:27012f0c68af4fbf8dbe6bbaf7ab651b");
			// System.out.println("Listening to address " + url);
			// IpLocalProcess.getInstance().listen(url);
		}
		catch (final Exception e)
		{
			System.out.println("Exception " + e.getClass().getName());
			e.printStackTrace();
			// System.exit(-1);
		}

		obj = new TestSetPropertyObject();
		int count = 0;

		while (true)
		{
			try
			{
				Thread.sleep(1000);
				count++;
				obj.setProperty(TestSetPropertyObject.INPUT, "input: " + count);
				System.out.println("Object updated, " + count);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
}
