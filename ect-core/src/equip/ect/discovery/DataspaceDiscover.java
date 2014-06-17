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
package equip.ect.discovery;

import equip.discovery.DiscoveryClientAgent;
import equip.discovery.DiscoveryClientAgentImpl;
import equip.discovery.DiscoveryEventListenerImpl;

public class DataspaceDiscover
{

	private class MyDiscoveryEventListenerImpl extends DiscoveryEventListenerImpl
	{

		protected String[] urls;

		@Override
		public void discoveryEvent(final DiscoveryClientAgent agent, final String url)
		{
			System.err.println("- Discovered: " + url);
			printAll(agent);
		}

		@Override
		public void discoveryRemoveEvent(final DiscoveryClientAgent agent, final String url)
		{
			System.err.println("- Lost: " + url);
			printAll(agent);
		}

		public String[] getUrls()
		{
			return urls;
		}

		public void printAll(final DiscoveryClientAgent agent)
		{
			synchronized (this)
			{
				urls = agent.getKnownServers();
				int i;
				System.err.println("Known servers:");
				for (i = 0; urls != null && i < urls.length; i++)
				{
					System.err.println("  " + urls[i]);
				}
				this.notifyAll();
			}
			if (handler != null)
			{
				handler.call(urls);
			}
		}

	}

	public static final String DEFAULT_GROUP = "ect.default";

	/** sample main - as a helper for another server */
	public static void main(final String[] args)
	{

		if (args.length > 1)
		{
			System.err.println("Usage: DataspaceDiscovery.ExampleClient [<group>]");
			System.exit(-1);
		}

		if (args.length > 0)
		{
			new DataspaceDiscover(args[0]);
		}
		else
		{
			new DataspaceDiscover("");
		}

	}

	protected StartManager handler = null;
	protected MyDiscoveryEventListenerImpl listen = new MyDiscoveryEventListenerImpl();
	protected DiscoveryClientAgent agent = new DiscoveryClientAgentImpl();

	protected String serviceType = "equip.data.DataProxy:2.0";

	protected String defaultGroup = DEFAULT_GROUP;

	public DataspaceDiscover(String group)
	{

		if (group == null || group.length() == 0)
		{
			group = defaultGroup;
		}

		// go...
		agent.startDefault(listen, new String[] { serviceType }, new String[] { group });

	}

	public String getFirstDataspace()
	{
		synchronized (listen)
		{
			while (listen.getUrls() == null || listen.getUrls().length == 0)
			{
				try
				{
					listen.wait();
				}
				catch (final InterruptedException e)
				{
					System.err.println("Wait interrupted: " + e);
					return null;
				}
			}
			return listen.getUrls()[0];
		}
	}

	public void restart()
	{

		agent.startDefault(listen, new String[] { serviceType }, new String[] { defaultGroup });

	}

	public void setHandler(final StartManager handler)
	{

		this.handler = handler;
	}

}