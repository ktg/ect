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

 */
package equip.ect;

import java.net.InetAddress;

/**
 * helper for exporting a processing applet as a component from within the processing environment.
 */
public class ProcessingHelper
{
	static SimpleObjectContainer container;
	static PublicFieldsProxyComponent proxy;

	/**
	 * init - call from Processing applet setup() with "this" as object argument; everything except
	 * object can be null. Note, instanceName should not be null if you want persistence to work, ie
	 * for the re-run applet to have the same links.
	 */
	public static void init(final Object object)
	{
		init(object, null);
	}

	/**
	 * init - call from Processing applet setup() with "this" as object argument; everything except
	 * object can be null. Note, instanceName should not be null if you want persistence to work, ie
	 * for the re-run applet to have the same links.
	 */
	public static void init(final Object object, final String instanceName)
	{
		init(object, instanceName, null);
	}

	/**
	 * init - call from Processing applet setup() with "this" as object argument; everything except
	 * object can be null. Note, instanceName should not be null if you want persistence to work, ie
	 * for the re-run applet to have the same links.
	 */
	public static void init(final Object object, final String instanceName, final String dataspaceUrl)
	{
		init(object, instanceName, dataspaceUrl, null);
	}

	/**
	 * init - call from Processing applet setup() with "this" as object argument; everything except
	 * object can be null. Note, instanceName should not be null if you want persistence to work, ie
	 * for the re-run applet to have the same links.
	 */
	public static void init(final Object object, final String instanceName, String dataspaceUrl,
			final String dataspaceSecret)
	{
		try
		{
			// redirect stderr to stdout; Processing halts the applet as soon as something comes out
			// of stderr!
			System.setErr(System.out);

			if (dataspaceUrl == null)
			{
				dataspaceUrl = "equip://:9123";
			}
			System.out.println("Dataspace:" + dataspaceUrl + " (secret=" + dataspaceSecret + ")");

			String name = object.getClass().getName();
			if (instanceName != null)
			{
				name = instanceName;
			}
			String hostname = name;
			try
			{
				final InetAddress localhost = InetAddress.getLocalHost();
				hostname = hostname + " on " + localhost.getHostName();
			}
			catch (final Exception e)
			{
				System.err.println("ERROR getting local hostname: " + e);
			}
			System.out.println("Create container...");
			container = new SimpleObjectContainer(dataspaceUrl, hostname, name + ".persist.xml", dataspaceSecret);

			System.out.println("Create proxy...");
			// purely scripted object
			proxy = new PublicFieldsProxyComponent(object);

			System.out.println("Export proxy...");
			container.exportComponent(proxy, "processingproxy");
			// ok
			System.out.println("Exported proxy...");
			proxy.exported();
			System.out.println("done");
		}
		catch (final Exception e)
		{
			System.err.println("ERROR initialising: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * copy changes to fields into the application. Call from a regular operation in the applet such
	 * as draw or loop (older versions).
	 */
	public static void poll()
	{
		if (proxy != null)
		{
			proxy.poll();
		}
	}
}
