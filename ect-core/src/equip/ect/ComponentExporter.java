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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import equip.data.GUID;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;

public class ComponentExporter
{

	private DataspaceBean dataspace = null;
	protected Map<Object, MappingObject> objectMappers = null;
	//private GUID hostID = null;
	//private GUID containerID = null;
	private ContainerManager cm = null;

	public ComponentExporter(final ContainerManager cm)
	{
		this.dataspace = cm.dataspace;
		this.objectMappers = cm.componentMappings;
		//this.hostID = cm.hostID;
		//this.containerID = cm.id;
		this.cm = cm;
	}

	public ComponentExporter(final DataspaceBean ds)
	{
		dataspace = ds;
		this.objectMappers = new HashMap<>();
	}

	public ComponentExporter(final String dataspaceUrl)
	{
		dataspace = new DataspaceBean();
		try
		{
			dataspace.setDataspaceUrl(dataspaceUrl);
		}
		catch (final Exception e)
		{
			System.err.println("Exporter exception: " + e);
		}
		if (!dataspace.isActive())
		{
			System.err.println("Exporter could not connect to dataspace.");
		}
		this.objectMappers = new HashMap<Object, MappingObject>();
	}

	public void export(final Serializable bean, final GUID capId, final GUID compReqId, final GUID compId,
			final ComponentProperty[] compProps, final MappingObject parent)
	{
		//final String name = "untitled bean";
		final MappingObject map = new MappingObject(bean, dataspace, cm, capId, compReqId, compId, compProps, parent);
		objectMappers.put(map.getBeanID(), map);
	}

	public void export(final Serializable bean, final String name, final GUID capId, final GUID compReqId,
			final GUID compId, final ComponentProperty[] compProps, final MappingObject parent)
	{
		final MappingObject map = new MappingObject(bean, dataspace, cm, capId, compReqId, compId, compProps, parent);
		objectMappers.put(name, map);
	}

	public GUID getBeanID(final Serializable bean)
	{
		// find MappingObject for this bean
		for(final MappingObject map: objectMappers.values())
		{
			if (map.getBean() == bean) { return map.getBeanID(); }
		}
		return null;
	}

	public void link(final Object bean1, final String prop1, final Object bean2, final String prop2)
	{
		// Check validity and get GUIDs..

		if (!(objectMappers.containsKey(bean1)))
		{
			System.err.println("No such FROM component.");
			return;
		}
		final MappingObject mapObj1 = objectMappers.get(bean1);
		final ComponentProperty prop1ID = mapObj1.getPropImpl(prop1);

		if (prop1ID == null)
		{
			System.err.println("No such FROM property.");
			return;
		}
		if (!(objectMappers.containsKey(bean2)))
		{
			System.err.println("No such TO component.");
			return;
		}

		final MappingObject mapObj2 = objectMappers.get(bean2);
		final ComponentProperty prop2ID = mapObj2.getPropImpl(prop2);

		if (prop2ID == null)
		{
			System.err.println("No such TO property.");
			return;
		}

		final PropertyLinkRequest linkreq = new PropertyLinkRequest(dataspace.allocateId());
		linkreq.setSourceComponentID(mapObj1.getBeanID());
		linkreq.setSourcePropertyName(prop1ID.getPropertyName());
		linkreq.setSourcePropID(prop1ID.getID());
		linkreq.setDestComponentID(mapObj2.getBeanID());
		linkreq.setDestinationPropertyName(prop2ID.getPropertyName());
		linkreq.setDestinationPropID(prop2ID.getID());

		try
		{
			linkreq.addtoDataSpace(dataspace);
		}
		catch (final DataspaceInactiveException ex)
		{
			System.out.println("Dataspace Inactive");
			ex.printStackTrace();

		}
	}

	public void unexport(final Serializable bean)
	{
		// find MappingObject for this bean
		for(final Object key: objectMappers.keySet())
		{
			final MappingObject map = objectMappers.get(key);
			if (map.getBean() == bean)
			{
				objectMappers.remove(key);
				map.stop();
				return;
			}
		}
		System.err.println("ERROR: ComponentExporter.unexport unknown bean " + bean);
	}
}