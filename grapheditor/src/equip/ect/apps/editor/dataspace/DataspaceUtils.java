/*
 <COPYRIGHT>

 Copyright (c) 2002-2006, Swedish Institute of Computer Science AB
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 - Neither the name of the Swedish Institute of Computer Science AB
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

 Created by: Jan Humble (Swedish Institute of Computer Science AB)
 Contributors:
 Chris Greenhalgh (University of Nottingham)
 Jan Humble (Swedish Institute of Computer Science AB)
 Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.apps.editor.dataspace;

import equip.data.ByteArrayBox;
import equip.data.GUID;
import equip.data.ItemData;
import equip.data.StringBoxImpl;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.BeanDescriptorHelper;
import equip.ect.Capability;
import equip.ect.Coerce;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.ConnectionPointTypeException;
import equip.ect.Container;
import equip.ect.RDFStatement;
import equip.ect.apps.editor.Info;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DataspaceUtils
{
	private static final Pattern guidPattern = Pattern
			.compile("^\\[(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+):(\\d+)\\.(\\d+):(\\d+):(\\d+)\\]");

	public static String getActiveRDFName(final DataspaceBean dataspace, final GUID id)
	{
		final RDFStatement template = new RDFStatement(null, RDFStatement.GUIDToUrl(id), RDFStatement.ECT_ACTIVE_TITLE,
				null);

		RDFStatement rdfActiveNames[];
		try
		{
			rdfActiveNames = template.copyCollectAsRDFStatement(dataspace);
		}
		catch (final DataspaceInactiveException e)
		{
			return null;
		}
		if ((rdfActiveNames == null) || (rdfActiveNames.length == 0))
		{
			return null;
		}

		return rdfActiveNames[0].getObject();
	}

	public static String getCapabilityDisplayName(final GUID guid)
	{
		final Collection<Capability> caps = DataspaceMonitor.getMonitor().getCapabilities();

		if (caps == null)
		{
			return null;
		}

		for (final Capability cap : caps)
		{
			if (cap.getID() == guid)
			{
				final Object displayName = cap.getAttributeValue(BeanDescriptorHelper.DISPLAY_NAME);

				if (displayName != null)
				{
					return (((StringBoxImpl) displayName).value);
				}
				else
				{
					return (cap.getCapabilityName());
				}
			}
		}
		return guid.toString();
	}

	public static String getCurrentName(final ComponentAdvert compAdv)
	{
		if(compAdv == null)
		{
			return null;
		}
		final Object displayName = compAdv.getAttributeValue(BeanDescriptorHelper.DISPLAY_NAME);

		if ((displayName != null) && (displayName instanceof StringBoxImpl))
		{
			return (((StringBoxImpl) displayName).value);
		}
		else
		{
			return (compAdv.getComponentName());
		}
	}

	/*
	 * public static String getHostID(ComponentAdvert comp, DataspaceBean dataspace) { GUID
	 * capabilityID = comp.getCapabilityID();
	 * 
	 * if (capabilityID == null) { return null; } equip.data.ItemData cap = null; try { cap =
	 * dataspace.getItem(capabilityID); if (cap != null) { return (new Capability((TupleImpl)
	 * cap)).getHostID(); } else { Info
	 * .message("WARNING: No Capabilities for this ComponentAdvert"); return null; } } catch
	 * (DataspaceInactiveException dsie) { return null; } }
	 */

	/**
	 * get default name for GUID url
	 */
	public static String getDefaultName(final DataspaceBean dataspace, final String url)
	{
		final GUID guid = RDFStatement.urlToGUID(url);
		if (guid == null)
		{
			return url;
		}
		try
		{
			// what is it?
			final ItemData item = dataspace.getItem(guid);
			if (item == null || !(item instanceof TupleImpl))
			{
				System.err.println("Warning: getDefaultName for " + guid + " unknown ("
						+ (item == null ? "null" : item.getClass().getName()));
				return url;
			}

			if (item.name.equals(ComponentAdvert.TYPE))
			{
				final ComponentAdvert comp = new ComponentAdvert((TupleImpl) item);
				return comp.getComponentName();
			}
			if (item.name.equals(Capability.TYPE))
			{
				final Capability cap = new Capability((TupleImpl) item);
				return cap.getCapabilityName() + " hosted on " + cap.getHostID();
			}
			if (item.name.equals(ComponentProperty.TYPE))
			{
				final ComponentProperty prop = new ComponentProperty((TupleImpl) item);
				return prop.getPropertyName();
			}

			return item.name;
		}
		catch (final Exception e)
		{
			System.err.println("ERROR getting default name for " + url + ": " + e);
			e.printStackTrace(System.err);
		}
		return url;
	}

	/**
	 * get display string for GUID url
	 */
	public static String getDisplayString(final DataspaceBean dataspace, final String url)
	{
		try
		{
			final StringBuilder buf = new StringBuilder();
			final RDFStatement template = new RDFStatement(null, url, RDFStatement.DC_TITLE, null);
			final RDFStatement names[] = template.copyCollectAsRDFStatement(dataspace);
			int i;
			for (i = 0; i < names.length; i++)
			{
				if (i > 0)
				{
					buf.append(", ");
				}
				final String name = names[i].getObject();
				buf.append(name);
			}
			if (i > 0)
			{
				buf.append(" (");
			}
			buf.append(getDefaultName(dataspace, url));
			buf.append(", ");
			buf.append(url);
			if (i > 0)
			{
				buf.append(")");
			}
			return buf.toString();
		}
		catch (final Exception e)
		{
			System.err.println("ERROR doing getDisplayString for " + url + ": " + e);
			e.printStackTrace(System.err);
		}
		return url;
	}

	/**
	 * get first name GUID url
	 */
	public static String getFirstName(final DataspaceBean dataspace, final String url)
	{
		try
		{
			final RDFStatement template = new RDFStatement(null, url, RDFStatement.DC_TITLE, null);
			final RDFStatement names[] = template.copyCollectAsRDFStatement(dataspace);
			if (names.length > 0)
			{
				return names[0].getObject();
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR doing getDisplayString for " + url + ": " + e);
			e.printStackTrace(System.err);
		}
		return getDefaultName(dataspace, url);
	}

	public static String getHostID(final ComponentAdvert comp, final DataspaceBean dataspace)
	{

		final GUID containerID = comp.getContainerID();
		if (containerID == null)
		{
			Info.message("WARNING => advert without container info");
			return null;
		}

		try
		{
			ItemData item = dataspace.getItem(containerID);
			if (item != null)
			{
				return (new Container((TupleImpl) item)).getHostID();
			}
			else
			{
				Info.message("WARNING => No container in dataspace for " + comp.getComponentName());
				return null;
			}
		}
		catch (final DataspaceInactiveException dsie)
		{
			return null;
		}
	}

	public static Image getIcon(final ComponentAdvert comp, final DataspaceBean dataspace, final int width,
	                            final int height, final Component component)
	{
		try
		{

			final ItemData item = dataspace.getItem(comp.getCapabilityID());
			if (item != null)
			{

				final Capability cap = new Capability((TupleImpl) item);
				final ByteArrayBox bab = (ByteArrayBox) cap.getAttributeValue("icon");
				if (bab != null)
				{

					final Image image = Toolkit.getDefaultToolkit().createImage(bab.value);
					if (image != null)
					{

						try
						{
							final MediaTracker tracker = new MediaTracker(component);

							tracker.addImage(image, 0);
							tracker.waitForID(0);

						}
						catch (final Exception e)
						{
							Info.message(e.getMessage());
						}

						return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
					}
				}
			}
		}
		catch (final DataspaceInactiveException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static String getPropValueAsString(final ComponentProperty prop)
	{
		return getPropValueAsString(prop, DataspaceMonitor.getMonitor().getDataspace());
		/*
		 * try { String type = prop.getConnectionPointType(); if
		 * (type.equals(ComponentProperty.CONNECTION_POINT_PROPERTY_VALUE)) return
		 * prop.getPropertyValueAsString(); System.err.println("WARNING: component property type
		 * "+type+" unhandled"); } catch (ConnectionPointTypeException ex) {
		 * System.err.println("ERROR: "+ex); ex.printStackTrace(System.err); } return "ERROR";
		 */
	}

	public static String getPropValueAsString(final ComponentProperty prop, final DataspaceBean dataspace)
	{
		try
		{
			final Object value = prop.getPropertyValue(dataspace);
			return Coerce.toClass(value, String.class);
		}
		catch (final ConnectionPointTypeException ex)
		{
			System.err.println("ERROR: " + ex);
			ex.printStackTrace(System.err);
			return "ERROR";
		}
		catch (final Exception cnfe)
		{
			System.err.println("Warning: Could not get value for property '" + prop.getPropertyName() + "'");
			cnfe.printStackTrace();
			return "";
		}
	}

	public static String[] getRDFDefinedNames(final DataspaceBean dataspace, final GUID id)
	{
		final RDFStatement template = new RDFStatement(null, RDFStatement.GUIDToUrl(id), RDFStatement.DC_TITLE, null);
		RDFStatement rdfNames[];
		try
		{
			rdfNames = template.copyCollectAsRDFStatement(dataspace);
		}
		catch (final DataspaceInactiveException e)
		{
			return null;
		}
		if (rdfNames == null)
		{
			return null;
		}
		final String[] names = new String[rdfNames.length];
		for (int i = 0; i < rdfNames.length; i++)
		{
			names[i] = rdfNames[i].getObject();
		}
		return names;
	}

	public static GUID stringToGUID(final String guids)
	{
		if (guids == null)
		{
			return null;
		}
		final Matcher matcher = guidPattern.matcher(guids);
		if (!matcher.matches())
		{
			System.err.println("WARNING: GUID string " + guids + " does not match GUID pattern");
			return null;
		}
		try
		{
			final GUID guid = new equip.data.GUIDImpl();
			guid.host_id = (new Integer(matcher.group(1)) << 24)
					| (new Integer(matcher.group(2)) << 16)
					| (new Integer(matcher.group(3)) << 8) | (new Integer(matcher.group(4)));
			guid.proc_id = (new Integer(matcher.group(5)) << 16)
					| (new Integer(matcher.group(6)));
			guid.item_id = (new Integer(matcher.group(7)));
			guid.time_s = (new Integer(matcher.group(8)));

			// test/temp check
			if (!guid.toString().equals(guids))
			{
				System.err.println("WARNING: GUID string " + guids + " -> GUID " + guid + " - reverse check failed");
			}
			return guid;
		}
		catch (final Exception e)
		{
			System.err.println("WARNING: GUID string " + guids + " raises exception: " + e);
			e.printStackTrace(System.err);
			// return null;
		}
		return null;
	}

}
