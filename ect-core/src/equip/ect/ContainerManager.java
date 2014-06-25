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

 Created by: Tom Rodden (University of Nottingham)
 Contributors:
 Tom Rodden (University of Nottingham)
 Chris Greenhalgh (University of Nottingham)
 Shahram Izadi (University of Nottingham)
 Jan Humble (University of Nottingham)
 Ted Phelps (University of Sussex)

 */
package equip.ect;

import equip.data.GUID;
import equip.data.StringBoxImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class ContainerManager implements Runnable
{
	public static void main(final String[] argsv)
	{
		if (argsv.length != 2)
		{
			System.err.println("Usage: java ComponentExporter <dataspaceUrl> <componentdir>");
			System.exit(-1);
		}

		// Connect to DataSpace
		final DataspaceBean dataspace = new DataspaceBean();

		try
		{
			dataspace.setDataspaceUrl(argsv[0]);
		}
		catch (final DataspaceInactiveException ex)
		{
			System.err.println("Exporter could not connect to dataspace.");
			System.exit(-1);
		}

		/*
		 * ContainerManager containerManager1 = new ContainerManager(dataspace, argsv[1],
		 * "test machine");
		 */
		// at home dir = "D:/Equator/Infrastructure/classes";
	}

	// Dataspace
	public DataspaceBean dataspace;

	// Component Directory
	public String componentdirname;

	File componentdir;

	// ID of Container
	GUID id, hostID;

	String hostName;

	// Component Capabilities
	public Map<GUID, Class<?>> capabilityClasses = Collections.synchronizedMap(new HashMap<GUID, Class<?>>());

	// Launched Components
	protected Map<Object, MappingObject> componentMappings = Collections.synchronizedMap(new HashMap<Object, MappingObject>());

	CapabilityExporter capexport = null;

	ComponentLauncher comlauncher = null;

	private final Object startupDataMapLock = new Object();

	private Map<GUID, ComponentStartupData> startupDataMap = null;

	private ContainerManagerHelper containerManagerHelper = null;

	public ContainerManager(final DataspaceBean dspace, final String dirname, final String hostname, final GUID id)
	{
		this(dspace, dirname, hostname, id, null, null);
	}

	public ContainerManager(final DataspaceBean dspace, final String dirname, final String hostname, final GUID id,
	                        final Map<GUID, Class<?>> capabilityClasses, final Map<GUID, ComponentStartupData> startupDataMap)
	{

		this.dataspace = dspace;
		this.id = id;
		this.hostName = hostname;
		this.hostID = dspace.allocateId(); // check this
		this.capabilityClasses = capabilityClasses == null ? new HashMap<GUID, Class<?>>() : capabilityClasses;
		this.startupDataMap = startupDataMap == null ? new HashMap<GUID, ComponentStartupData>() : startupDataMap;
		this.componentdirname = dirname;
		componentdir = new File(componentdirname);

		if (!componentdir.isDirectory())
		{
			System.out.println("Open Dir " + componentdirname + "not a directory");
			return;
		}

		exportContainer(id, dataspace);

		// Export the Capabilities
		capexport = new CapabilityExporter(this);

		// Start the Launcher
		comlauncher = new ComponentLauncher(this);
		comlauncher.runLaunch(dataspace, componentdir);

		final Thread myThread = new Thread(this);
		myThread.start();
	}

	/**
	 * Exports the container item to the dataspace with container info.
	 */
	public Container exportContainer(final GUID containerID, final DataspaceBean dataspace)
	{
		final Container container = new Container(containerID);
		container.setContainerName("Container hosted on " + hostName);
		container.setAttribute("launchDate", new StringBoxImpl(Calendar.getInstance().getTime().toString()));
		container.setHostID(hostName);

		try
		{
			container.addtoDataSpace(dataspace);
		}
		catch (final DataspaceInactiveException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return container;
	}

	public ComponentExporter getComponentExporter()
	{
		return comlauncher.getComponentExporter();
	}

	/**
	 * getContainerID
	 *
	 * @return double
	 */
	public GUID getContainerID()
	{
		return this.id;
	}

	public ContainerManagerHelper getContainerManagerHelper()
	{
		return containerManagerHelper;
	}

	public GUID getHostID()
	{
		return this.hostID;
	}

	public ComponentStartupData getStartupData(final GUID componentRequestGUID)
	{
		synchronized (startupDataMapLock)
		{
			return startupDataMap.get(componentRequestGUID);
		}
	}

	/**
	 * look for startup data for a subcomponent, given its parent id and persistentChild value
	 */
	public ComponentStartupData getStartupData(final GUID parentGUID, final String persistentChild)
	{
		synchronized (startupDataMapLock)
		{
			final String parent = parentGUID.toString();
			System.out.println("Looking for startup data for " + parent + "/" + persistentChild);

			for (final ComponentStartupData data : startupDataMap.values())
			{
				// System.out.println("try "+data.getComponentGUID());
				// look for parent & persistentChild properties
				boolean foundParent = false, foundChild = false;
				final ComponentProperty props[] = data.getProperties();
				for (int j = 0; j < props.length && (!foundParent || !foundChild); j++)
				{
					final String propertyname = props[j].getPropertyName();
					if (propertyname == null)
					{
						continue;
					}
					if (propertyname.equals(MappingObject.PARENT))
					{
						try
						{
							final String propertyvalue = (String) Coerce.toClass(props[j].getPropertyValue(),
									String.class);
							if (propertyvalue.equals(parent))
							{
								foundParent = true;
							}
						}
						catch (final Exception e)
						{
							System.err.println("ERROR checking parent property value: " + e);
							e.printStackTrace(System.err);
						}
					}
					else if (propertyname.equals(MappingObject.PERSISTENT_CHILD))
					{
						try
						{
							final String propertyvalue = (String) Coerce.toClass(props[j].getPropertyValue(),
									String.class);
							if (propertyvalue.equals(persistentChild))
							{
								foundChild = true;
							}
						}
						catch (final Exception e)
						{
							System.err.println("ERROR checking parent property value: " + e);
							e.printStackTrace(System.err);
						}
					}
					/*
					 * try { System.out.println("Property
					 * "+propertyname+"="+props[j].getPropertyValue()+" (parent? "+foundParent+ ",
					 * child? "+foundChild+")"); } catch(Exception e) { System.out.println("Property
					 * "+propertyname+"=?"+e+" (parent? "+foundParent+ ", child? "+foundChild+")");
					 * }
					 */
				}
				if (foundParent && foundChild)
				{
					System.out.println("Found!");
					return data;
				}
			}
		}
		System.out.println("Not found");
		return null;
	}

	/**
	 * run
	 */
	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (final InterruptedException e)
			{
				// the VM doesn't want us to sleep anymore,
				// so get back to work
			}
		}
	}

	public void setContainerManagerHelper(final ContainerManagerHelper containerManagerHelper)
	{
		this.containerManagerHelper = containerManagerHelper;
	}

	/**
	 * add/update startup date map, esp. for a subcomponent
	 */
	public void updateStartupData(final GUID requestGUID, final String name, final GUID componentGUID,
	                              final ComponentProperty props[])
	{
		synchronized (startupDataMapLock)
		{
			ComponentStartupData data = startupDataMap.get(requestGUID);
			if (data == null)
			{
				// a bit iffy - partial knowledge
				data = new ComponentStartupData(name, /* jar - unknown */
						null, componentGUID, (requestGUID.equals(componentGUID) ? null : requestGUID), props, (String) null/* unknown */);
				startupDataMap.put(requestGUID, data);
				// also under component GUID
				if (!requestGUID.equals(componentGUID))
				{
					startupDataMap.put(componentGUID, data);
				}

				System.out.println("Adding startup data for " + (componentGUID.equals(requestGUID) ? "sub" : "")
						+ " component " + componentGUID);
			}
			else
			{
				// may be a new version of the bean with a different set of
				// properties??
				data.setProperties(props);
			}
		}
	}

	/**
	 * update startup data map persistence file (only if exists)
	 */
	public void updateStartupDataPersistFile(final GUID requestGUID, final File file)
	{
		synchronized (startupDataMapLock)
		{
			final ComponentStartupData data = startupDataMap.get(requestGUID);
			if (data != null)
			{
				if (file == data.getPersistFile()
						|| (file != null && data.getPersistFile() != null && file.equals(data.getPersistFile())))
				{
					// unchanged
					return;
				}
				System.err.println("Component persistFile for " + requestGUID + " changed from "
						+ data.getPersistFile() + " to " + file);
				// may be a new version of the bean with a different set of
				// properties??
				data.setPersistFile(file);
			}
		}
	}

	/**
	 * update startup data map value (as string) (only if exists)
	 */
	public void updateStartupDataValue(final GUID requestGUID, final GUID propertyGUID, final String value)
	{
		synchronized (startupDataMapLock)
		{
			final ComponentStartupData data = startupDataMap.get(requestGUID);
			if (data == null)
			{
				System.err.println("Warning: updateStartupDataValue could not find ComponentStartupData for "
						+ requestGUID);
				return;
			}
			final ComponentProperty props[] = data.getProperties();
			for (final ComponentProperty prop : props)
			{
				if (prop.getID().equals(propertyGUID))
				{
					try
					{
						prop.setPropertyValue(value);
						// debug
						// System.out.println("Updated startup data value to
						// "+value);
					}
					catch (final Exception e)
					{
						System.err.println("ERROR updating startup data value: " + e);
						e.printStackTrace(System.err);
					}
					break;
				}
			}
		}
	}

}
