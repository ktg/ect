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

Created by: Jan Humble (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Shahram Izadi (University of Nottingham)
  Jan Humble (University of Nottingham)

 */
package equip.ect;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import equip.data.GUID;
import equip.data.ItemData;
import equip.data.StringBoxImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceEvent;
import equip.data.beans.DataspaceEventListener;
import equip.data.beans.DataspaceInactiveException;

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

public class ComponentLauncher implements DataspaceEventListener
{

	/**
	 * . This simple test main program puts in a listener of type capability and for every
	 * capability that is offered by the container manager it calls the launcher to set the
	 * component running and publish the elements.
	 */

	public static void main(final String[] argsv)
	{

		if (argsv.length != 1)
		{
			System.err.println("Usage: java ComponentExporter <dataspaceUrl>");
			System.exit(-1);
		}

		final DataspaceBean dataspace = new DataspaceBean();
		try
		{
			dataspace.setDataspaceUrl(argsv[0]);
		}
		catch (final Exception e)
		{
			System.err.println("COmponent Launcher Main exception: " + e);
		}
		if (!dataspace.isActive())
		{
			System.err.println("Exporter could not connect to dataspace.");
			System.exit(-1);
		}

		// ContainerManager conm = new ContainerManager(dataspace,
		// "D:/Equator/Infrastructure/classes",
		// "test Host");

		// componentLancher1.launch("ChatBean", "namer1");
		// componentLancher1.launch("ChatBean", "namer2");

		// Build the pattern for capability
		//final Capability capability = new Capability((GUID) null);

		// DataHandler dh = new DataHandler(conm);
		/*
		 * try { //capability.addPatterntoDataSpace(conm.dataspace, dh); } catch
		 * (DataspaceInactiveException ex) { }
		 */

	}

	// private File componentdirectory = null;
	ContainerManager conmanager;
	ComponentExporter exporter;

	// ComponentRequest GUID -> Serializable bean
	Map<GUID, Serializable> launchedComponents = Collections.synchronizedMap(new HashMap<GUID, Serializable>());

	public ComponentLauncher(final ContainerManager cm)
	{
		this.conmanager = cm;
		this.exporter = new ComponentExporter(cm);
		// runLaunch(conmanager.dataspace, conmanager.componentdir);
	}

	public ComponentLauncher(final DataspaceBean dsBean, final String dirname)
	{
		final File classdir = new File(dirname);
		this.exporter = new ComponentExporter(dsBean);
		if (!classdir.isDirectory())
		{
			System.out.println("Open Dir " + dirname + "not a directory");
			return;
		}
		runLaunch(dsBean, classdir);
	}

	// DataspaceEventListener method
	@Override
	public void dataspaceEvent(final equip.data.beans.DataspaceEvent event)
	{
		synchronized (ContainerManager.class)
		{
			try
			{
				// Add Events
				if (event.getEvent() instanceof equip.data.AddEvent)
				{
					// Pulll ou the details from the tuple

					final equip.data.TupleImpl added = (equip.data.TupleImpl) (event.getAddItem());
					final String tupleType = ((StringBoxImpl) added.fields[0]).value;

					if (tupleType.equals(ComponentRequest.TYPE))
					{
						final ComponentRequest compreq = new ComponentRequest(added);
						System.out.println("received component request " + compreq.getID());
						System.out.println("container id " + compreq.getContainerID());
						if (compreq.getContainerID().equals(this.conmanager.getContainerID()))
						{
							if (this.conmanager.capabilityClasses.containsKey(compreq.getCapabilityID()))
							{
								Serializable comp = null;
								final ComponentStartupData data = conmanager.getStartupData(compreq.getID());
								if (data == null)
								{
									comp = launch(	conmanager.capabilityClasses.get(compreq.getCapabilityID()),
													compreq.getCapabilityID(), compreq.getID(), null, null);
								}
								else
								{
									comp = launch(	conmanager.capabilityClasses.get(compreq.getCapabilityID()),
													compreq.getCapabilityID(), compreq.getID(),
													data.getComponentGUID(), data.getProperties());
									if (comp != null && comp instanceof Persistable && data.getPersistFile() != null)
									{
										try
										{
											((Persistable) comp).load(data.getPersistFile(), conmanager);
										}
										catch (final Exception e)
										{
											System.out.println("error reloading component: " + compreq.getID() + "\n"
													+ e);
										}
									}
								}
								this.launchedComponents.put(added.id, comp);
							}
						}
					}
				}

				// UPDATE Events
				if (event.getEvent() instanceof equip.data.UpdateEvent)
				{
					// Not sure what to do
				}

				if (event.getEvent() instanceof equip.data.DeleteEvent)
				{

					final GUID deleteId = event.getDeleteId();
					if (deleteId == null) { return; }
					// Should kill the component.
					final Serializable comp = (Serializable) this.launchedComponents.get(deleteId);
					if (comp == null) { return; }
					this.launchedComponents.remove(deleteId);

					this.sink(comp);
				}
			}
			catch (final Exception e)
			{
				System.err.println("ERROR: handling dataspace event: " + e);
				e.printStackTrace(System.err);
			}
		}
	}

	/**
	 * launch top-level component, unnamed
	 */
	public Serializable launch(final Class<?> componentClass, final GUID capId, final GUID compReqId, final GUID compId,
			final ComponentProperty[] compProps)
	{
		// Launch the component
		Serializable newcomponent = null;
		try
		{
			System.err.println("New instance of class " + componentClass.getName() + "...");
			newcomponent = (Serializable) componentClass.newInstance();
			System.err.println("= " + newcomponent);
			launchActiveComponent(newcomponent);
		}
		catch (final IllegalAccessException ex1)
		{
			System.out.println("Access Error");
			return null;
		}
		catch (final InstantiationException ex1)
		{
			System.out.println("InstantiationException Error");
			return null;
		}
		catch (final Exception e)
		{
			System.err.println("ERROR creating new instance of class " + componentClass.getName() + ": " + e);
		}
		System.err.println("After new instance");
		// export the instance
		exporter.export(newcomponent, capId, compReqId, compId, compProps, null);
		return newcomponent;
	}

	/**
	 * launch top-level component, named
	 */
	public void launch(final Class<?> componentClass, final String name, final GUID capId, final GUID compReqId,
			final GUID compId, final ComponentProperty[] compProps)
	{
		Serializable newcomponent = null;
		try
		{
			newcomponent = (Serializable) componentClass.newInstance();
			launchActiveComponent(newcomponent);
		}
		catch (final IllegalAccessException ex1)
		{
			System.out.println("Access Error");
			return;
		}
		catch (final InstantiationException ex1)
		{
			System.out.println("InstantiationException Error");
			return;
		}
		// export the instance
		exporter.export(newcomponent, name, capId, compReqId, compId, compProps, null);
	}

	public void launch(final String classname, final String name, final GUID capId, final GUID compReqId,
			final GUID compId, final ComponentProperty[] compProps)
	{

		// Check we have the cabaility
		Class<?> componentClass = null;
		try
		{
			componentClass = Class.forName(classname);
		}
		catch (final ClassNotFoundException ex)
		{
			System.out.println("Class Not Found");
			return;
		}
		launch(componentClass, name, capId, compReqId, compId, compProps);
	}

	public void runLaunch(final DataspaceBean dsBean, final File classdir)
	{
		// Register with Equip
		// Build pattern for the property tuple
		final ComponentRequest template = new ComponentRequest((GUID) null);

		template.setContainerID(this.conmanager.getContainerID());

		// Put Patterns in the Space
		try
		{
			// equip.data.beans.DataspaceBean dsBean = this.slave.getDataspace();
			template.addPatterntoDataSpace(dsBean, this);

		}
		catch (final DataspaceInactiveException ex)
		{
			System.out.println("Dataspace inacative ");
			ex.printStackTrace();
		}

	}

	/**
	 * opposite of launch :-)
	 */
	public void sink(final Serializable comp)
	{
		exporter.unexport(comp);
	}

	ComponentExporter getComponentExporter()
	{
		return exporter;
	}

	/**
	 * handle launch of active component
	 */
	protected void launchActiveComponent(final Serializable c)
	{
		if (c instanceof IActiveComponent)
		{
			final IActiveComponent ac = (IActiveComponent) c;
			ac.initialise(conmanager, conmanager.dataspace);
		}
	}
}