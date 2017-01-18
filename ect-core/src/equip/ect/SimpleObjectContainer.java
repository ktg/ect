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

import equip.data.GUID;
import equip.data.beans.DataspaceBean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.net.InetAddress;

/**
 * A supporting class to allow an application to explicitly publish one or more objects as
 * components.
 */
public class SimpleObjectContainer
{
	/**
	 * fixed time for fixed (fake) component request
	 */
	public static final int FIXED_TIME_S = 5;
	protected String hostname;
	/**
	 * container manager helper
	 */
	protected ContainerManagerHelper containerManagerHelper;
	/**
	 * container manager
	 */
	protected ContainerManager containerManager;
	/**
	 * dataspace
	 */
	protected DataspaceBean dataspaceBean;
	/**
	 * cons
	 */
	public SimpleObjectContainer(final String dataspaceUrl, final String hostname, final String persistFile)
			throws java.io.IOException
	{
		this(dataspaceUrl, hostname, persistFile, null);
	}

	/**
	 * cons
	 */
	public SimpleObjectContainer(final String dataspaceUrl, final String hostname, final String persistFile,
			final String dataspaceSecret) throws java.io.IOException
	{
		if (dataspaceSecret != null)
		{
			System.setProperty("DataspaceSecret", dataspaceSecret);
		}
		this.hostname = hostname;
		containerManagerHelper = new ContainerManagerHelper(dataspaceUrl, persistFile + ".components", persistFile,
				hostname);
		containerManager = containerManagerHelper.getContainerManager();
		dataspaceBean = containerManagerHelper.getDataSpaceBean();
	}

	/**
	 * test main
	 */
	public static void main(final String[] args)
	{
		try
		{
			String dataspaceUrl = "equip://:9123";
			String dataspaceSecret = null;
			if (args.length > 0)
			{
				dataspaceUrl = args[0];
			}
			if (args.length > 1)
			{
				dataspaceSecret = args[1];
			}
			final InetAddress localhost = InetAddress.getLocalHost();
			final SimpleObjectContainer container = new SimpleObjectContainer(dataspaceUrl,
					"SimpleObjectContainer.main on " + localhost.getHostName(),
					"SimpleObjectContainer.main.persist.xml", dataspaceSecret);

			// purely scripted object
			final SimpleDynamicComponent dynobj = new SimpleDynamicComponent();
			dynobj.addProperty("in", String.class, "inval");
			dynobj.addProperty("out", String.class, "outval");
			dynobj.addPropertyChangeListener(new PropertyChangeListener()
			{
				@Override
				public void propertyChange(final PropertyChangeEvent event)
				{
					try
					{
						final String name = event.getPropertyName();
						System.out.println("Property " + name + " changed to " + event.getNewValue());
						if (name.equals("in"))
						{
							dynobj.dynSetProperty("out", event.getNewValue());
						}
					}
					catch (final Exception e)
					{
						System.err.println("ERROR: " + e);
						e.printStackTrace(System.err);
					}
				}
			});

			container.exportComponent(dynobj, "testobj");
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * export an object as a component; optional persistent name
	 */
	public GUID exportComponent(final Serializable component, final String persistentName)
	{
		// fake up a request id from the persistent name, if present
		GUID requestId;
		requestId = dataspaceBean.allocateId();
		if (persistentName != null)
		{
			// assume host id ok
			requestId.proc_id = hostname.hashCode();
			requestId.item_id = persistentName.hashCode();
			requestId.time_s = FIXED_TIME_S;
			System.out.println("Using fixed request ID: " + requestId);
		}
		final ComponentStartupData data = containerManager.getStartupData(requestId);
		final GUID compId = data != null ? data.getComponentGUID() : dataspaceBean.allocateId();
		final ComponentProperty[] compProps = data != null ? data.getProperties() : null;
		// hmm. this will try to set values to old ones...?!
		final MappingObject map = new MappingObject(component, dataspaceBean, containerManager, null, requestId,
				compId, compProps, null);
		System.out.println("Exported as " + compId);
		return compId;
	}

	/**
	 * get dataspace
	 */
	public DataspaceBean getDataspaceBean()
	{
		return dataspaceBean;
	}
}
