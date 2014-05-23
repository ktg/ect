/*
 <COPYRIGHT>

 Copyright (c) 2004-2006, University of Nottingham
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
 Tom Rodden (University of Nottingham)
 Chris Greenhalgh (University of Nottingham)
 Jan Humble (University of Nottingham)
 Shahram Izadi (University of Nottingham)
 Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.util.Map;

import equip.data.ByteArrayBoxImpl;
import equip.data.DictionaryImpl;
import equip.data.GUID;
import equip.data.StringBoxImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;

public class CapabilityExporter
{
	private ContainerManager containerManager = null;

	private GUID containerID = null;

	public CapabilityExporter()
	{
	}

	public CapabilityExporter(final ContainerManager cm)
	{
		containerManager = cm;
		//this.hostID = cm.hostID;
		this.containerID = cm.id;
	}

	// export capability using specified ids
	public Capability exportCapability(final Class<?> beanClass, final GUID capabilityGUID, final GUID containerGUID,
			final String hostname, final DataspaceBean dataSpace, final Map<String, String> beanContent)
	{

		if (dataSpace == null || beanClass == null) { return null; }

		BeanInfo beaninfo = null;

		java.beans.Introspector.setBeanInfoSearchPath(new String[] { "." });
		try
		{
			beaninfo = java.beans.Introspector.getBeanInfo(beanClass, beanClass.getSuperclass());
		}
		catch (final IntrospectionException e)
		{
			e.printStackTrace();
			return null;
		}

		//final PropertyDescriptor[] props = beaninfo.getPropertyDescriptors();
		final BeanDescriptor beandesc = beaninfo.getBeanDescriptor();

		// System.out.println("got bean descriptor");
		// System.out.println("default ouput " + beandesc.getValue("defaultOutputProperty"));

		// Build the capability to be exported to Equip
		final Capability capability = new Capability(capabilityGUID == null ? dataSpace.allocateId() : capabilityGUID);

		capability.setCapabilityName(beandesc.getName());
		capability.setCapablityClass(beandesc.getBeanClass().toString());
		capability.setContainerID(containerGUID);
		capability.setHostID(hostname);

		BeanDescriptorHelper.copyInformation(beaninfo, capability, true);

		if (beanContent != null)
		{
			String classification = beanContent.get(BeanJarContent.CLASSIFICATION_KEY);
			if (classification == null)
			{
				classification = (String) beandesc.getValue(Capability.CLASSIFICATION);
				if (classification != null)
				{
					System.out.println("Bean " + beanClass + " has beaninfo classification " + classification);
				}
			}

			if (classification != null)
			{
				capability.setClassification(classification);
			}
			// include properties

			final String shortDescription = (String) beanContent.get(BeanJarContent.SHORT_DESCRIPTION_KEY);
			if (shortDescription != null)
			{
				// a short description has been picked up from a manifest entry
				// definining a bean.
				// so, no bean info exists for that bean, so add short
				// description
				// in manually

				final DictionaryImpl di = (DictionaryImpl) (capability.tuple.fields[CompInfo.ATTRIBUTES_INDEX]);
				di.put("shortDescription", new StringBoxImpl(shortDescription));

			}

			final String htmlDescription = (String) beanContent.get(BeanJarContent.HTML_DESCRIPTION_KEY);
			if (htmlDescription != null)
			{
				// an html description has been picked up from a manifest entry
				// definining a bean.
				// so, no bean info exists for that bean, so add short
				// description
				// in manually

				final DictionaryImpl di = (DictionaryImpl) (capability.tuple.fields[CompInfo.ATTRIBUTES_INDEX]);
				di.put("htmlDescription", new StringBoxImpl(htmlDescription));

			}

			final String defaultInputProperty = (String) beanContent.get(BeanJarContent.DEFAULT_INPUT_PROPERTY_KEY);
			if (defaultInputProperty != null)
			{

				final DictionaryImpl di = (DictionaryImpl) (capability.tuple.fields[CompInfo.ATTRIBUTES_INDEX]);
				di.put("defaultInputProperty", new StringBoxImpl(defaultInputProperty));

			}

			final String defaultOutputProperty = (String) beanContent.get(BeanJarContent.DEFAULT_OUTPUT_PROPERTY_KEY);

			// System.out.println("got bean content " + defaultOutputProperty + " + from hash");

			if (defaultOutputProperty != null)
			{

				final DictionaryImpl di = (DictionaryImpl) (capability.tuple.fields[CompInfo.ATTRIBUTES_INDEX]);
				di.put("defaultOutputProperty", new StringBoxImpl(defaultOutputProperty));

			}

			final String iconFile = (String) beanContent.get(BeanJarContent.ICON_KEY);
			if (iconFile != null)
			{
				final byte[] iconData = ContainerManagerHelper.loadByteArrayFromFile(	iconFile,
																						beanClass.getClassLoader());
				if (iconData != null)
				{
					final DictionaryImpl di = (DictionaryImpl) (capability.tuple.fields[CompInfo.ATTRIBUTES_INDEX]);
					di.put("icon", new ByteArrayBoxImpl(iconData));

				}
			}
		}

		try
		{
			capability.addtoDataSpace(dataSpace);
			return capability;
		}
		catch (final DataspaceInactiveException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	// export capability allocate new ids
	public Capability exportCapability(final Class<?> beanClass, final Map<String, String> beanContent)
	{
		if (containerManager != null)
		{
			// already exported??
			for(final Class<?> c: containerManager.capabilityClasses.values())
			{
				if (c.equals(beanClass))
				{
					System.out.println("Class " + beanClass.getName() + " already exported");
					return null;
				}
			}
			final Capability cap = exportCapability(beanClass, null, containerID, containerManager.hostName,
													containerManager.dataspace, beanContent);

			// System.out.println("ready to export " + cap.getCapabilityName());
			//final int count = cap.getAttributeCount();
			// System.out.println("found " + count + " attributes");

			if (cap != null)
			{
				containerManager.capabilityClasses.put(cap.getID(), beanClass);
				return cap;
			}
		}
		return null;
	}
}
