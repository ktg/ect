/*
 <COPYRIGHT>

 Copyright (c) 2002-2005, Swedish Institute of Computer Science AB
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

import equip.data.DeleteEvent;
import equip.data.Event;
import equip.data.GUID;
import equip.data.ItemData;
import equip.data.StringBoxImpl;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceEvent;
import equip.data.beans.DataspaceEventListener;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.*;
import equip.ect.apps.editor.Info;
import equip.ect.discovery.DataspaceDiscover;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataspaceMonitor
{
	private static DataspaceMonitor monitor;
	private static final int TIMEOUT = 5000;
	private static final String PROPERTY_NAME = "PropertySetter";

	public static String[] getCategories(final Capability cap)
	{
		String classification = cap.getClassification();
		if (classification == null)
		{
			classification = "Uncategorized";
		}
		return classification.split("/");
	}

	public static DataspaceMonitor getMonitor()
	{
		return getMonitor(null);
	}

	public static DataspaceMonitor getMonitor(final String url)
	{
		if (monitor == null)
		{
			monitor = new DataspaceMonitor(url, false);
		}
		return monitor;
	}

	private class MyCapabilityListener extends MyDSListener
	{
		@Override
		void handleEvent(final DataspaceEvent dsEvent, final Event event)
		{
			if (event instanceof equip.data.AddEvent)
			{
				final Capability cap = new Capability((TupleImpl) dsEvent.getAddItem());
				capabilities.put(cap.getID().toString(), cap);
				fireEvent(DataspaceConfigurationListener.class, configurationListeners, "capabilityAdded",
						new Class[]{cap.getClass()}, new Object[]{cap});
			}
			else if (event instanceof equip.data.UpdateEvent)
			{
				final Capability cap = new Capability((TupleImpl) dsEvent.getAddItem());
				capabilities.put(cap.getID().toString(), cap);
				fireEvent(DataspaceConfigurationListener.class, configurationListeners, "capabilityUpdated",
						new Class[]{cap.getClass()}, new Object[]{cap});
			}
			else if (event instanceof equip.data.DeleteEvent)
			{
				final Capability cap = new Capability((TupleImpl) dsEvent.getOldValue());
				capabilities.remove(cap.getID().toString());
				fireEvent(DataspaceConfigurationListener.class, configurationListeners, "capabilityDeleted",
						new Class[]{cap.getClass()}, new Object[]{cap});
			}
			else
			{
				System.out.println("Unkown equip event detected");
			}
		}
	}

	/*
	 * Listen for Component Adverts
	 */
	private class MyComponentAdvertListener extends MyDSListener
	{

		@Override
		void handleEvent(final DataspaceEvent dsEvent, final Event event)
		{

			if (event instanceof equip.data.AddEvent)
			{

				final TupleImpl item = (TupleImpl) dsEvent.getAddItem();
				final ComponentAdvert compAdvert = new ComponentAdvert(item);

				final String stringGUID = compAdvert.getComponentID().toString();

				components.put(stringGUID, compAdvert);
				// componentNames.put(stringGUID, compAdvert.getComponentName());

				fireEvent(ComponentListener.class, compListeners, "componentAdvertAdded",
						new Class[]{compAdvert.getClass()}, new Object[]{compAdvert});

			}
			else if (event instanceof equip.data.UpdateEvent)
			{
				final TupleImpl item = (TupleImpl) dsEvent.getUpdateItem();

				final ComponentAdvert compAdvert = new ComponentAdvert(item);

				final String stringGUID = compAdvert.getComponentID().toString();

				components.put(stringGUID, compAdvert);
				// componentNames.put(stringGUID, compAdvert.getComponentName());

				fireEvent(ComponentListener.class, compListeners, "componentAdvertUpdated",
						new Class[]{compAdvert.getClass()}, new Object[]{compAdvert});
			}
			else if (event instanceof equip.data.DeleteEvent)
			{
				final ComponentAdvert compAdvert = new ComponentAdvert((TupleImpl) dsEvent.getOldValue());
				components.remove(compAdvert.getComponentID().toString());
				// componentNames.remove(compAdvert.getComponentID().toString());
				fireEvent(ComponentListener.class, compListeners, "componentAdvertDeleted",
						new Class[]{compAdvert.getClass()}, new Object[]{compAdvert});
			}
			else
			{
				System.out.println("Unkown equip event detected");
			}
		}
	}

	private class MyComponentPropertyListener extends MyDSListener
	{

		private boolean includeUpdateEvents;

		MyComponentPropertyListener()
		{
			this(true);
		}

		MyComponentPropertyListener(final boolean inclUpdates)
		{
			this.includeUpdateEvents = inclUpdates;
		}

		@Override
		void handleEvent(final DataspaceEvent dsEvent, final Event event)
		{
			if (event instanceof equip.data.AddEvent)
			{
				final TupleImpl item = (TupleImpl) dsEvent.getAddItem();
				final ComponentProperty compProp = new ComponentProperty(item);
				Map<String, ComponentProperty> props = properties.get(compProp.getComponentID().toString());
				if (props == null)
				{
					props = new HashMap<>();
					properties.put(compProp.getComponentID().toString(), props);
				}
				props.put(compProp.getID().toString(), compProp);
				fireEvent(ComponentPropertyListener.class, compPropListeners, "componentPropertyAdded",
						new Class[]{compProp.getClass()}, new Object[]{compProp});

				// is it a ref?
				if (compProp.getConnectionPointType().equals(ComponentProperty.CONNECTION_POINT_PROPERTY_REFERENCE))
				{
					try
					{
						final GUID ref = compProp.getPropertyReference();
						if (ref != null)
						{
							List<GUID> v = sourcePropertyReferencedBy.get(ref);
							if (v == null)
							{
								v = new ArrayList<>();
								sourcePropertyReferencedBy.put(ref, v);
							}
							v.add(compProp.getID());
						}
					}
					catch (final ConnectionPointTypeException e)
					{
						System.err.println("ERROR: " + e);
						e.printStackTrace(System.err);
					}
				}

				checkReferringProperties(compProp.getID());

			}
			else if (event instanceof equip.data.UpdateEvent)
			{
				if (this.includeUpdateEvents)
				{
					final TupleImpl item = (TupleImpl) dsEvent.getUpdateItem();
					final ComponentProperty compProp = new ComponentProperty(item);
					final Map<String, ComponentProperty> props = properties.get(compProp.getComponentID().toString());
					if (props != null)
					{
						props.put(compProp.getID().toString(), compProp);
					}
					fireEvent(ComponentPropertyUpdateListener.class, compPropListeners, "componentPropertyUpdated",
							new Class[]{compProp.getClass()}, new Object[]{compProp});

					checkReferringProperties(compProp.getID());
				}
			}
			else if (event instanceof equip.data.DeleteEvent)
			{
				final TupleImpl item = (TupleImpl) dsEvent.getOldValue();

				final ComponentProperty compProp = new ComponentProperty(item);
				final Map<String, ComponentProperty> props = properties.get(compProp.getComponentID().toString());
				if (props != null)
				{
					props.remove(compProp.getID().toString());
				}

				fireEvent(ComponentPropertyListener.class, compPropListeners, "componentPropertyDeleted",
						new Class[]{compProp.getClass()}, new Object[]{compProp});

				// is it a ref?
				if (compProp.getConnectionPointType().equals(ComponentProperty.CONNECTION_POINT_PROPERTY_REFERENCE))
				{
					try
					{
						final GUID ref = compProp.getPropertyReference();
						if (ref != null)
						{
							final List<GUID> v = sourcePropertyReferencedBy.get(ref);
							if (v != null)
							{
								// remove
								v.remove(compProp.getID());
								if (v.size() == 0)
								{
									sourcePropertyReferencedBy.remove(ref);
								}
							}
						}
					}
					catch (final ConnectionPointTypeException e)
					{
						System.err.println("ERROR: " + e);
						e.printStackTrace(System.err);
					}
				}

				checkReferringProperties(compProp.getID());

			}
			else
			{
				System.out.println("Unkown equip event detected");
			}
		}

		/**
		 * check refering properties on change
		 */
		void checkReferringProperties(final GUID id)
		{
			final List<GUID> v = sourcePropertyReferencedBy.get(id);
			if (v == null)
			{
				return;
			}
			for (final GUID rid : v)
			{
				try
				{
					final TupleImpl t = (TupleImpl) dataspace.getItem(rid);
					if (t != null && t.name.equals(ComponentProperty.TYPE))
					{
						// System.out.println("Change to property "+id+"
						// triggers checking update on property "+rid);
						final ComponentProperty compProp = new ComponentProperty(t);
						final Map<String, ComponentProperty> props = properties.get(compProp.getComponentID().toString());
						if (props == null)
						{
							continue;
						}
						if (props.get(compProp.getID().toString()) == null)
						{
							continue;
						}
						// already notified, we assume
						fireEvent(ComponentPropertyListener.class, compPropListeners, "componentPropertyUpdated",
								new Class[]{compProp.getClass()}, new Object[]{compProp});
					}
					else
					{
						System.err.println("Unable to check update on property " + rid + " (" + t + ", name="
								+ (t != null ? t.name : "null") + ")");
					}
				}
				catch (final DataspaceInactiveException e)
				{
				}
			}
		}
	}

	/*
	 * Listen for Component Adverts
	 */
	private class MyComponentRequestListener extends MyDSListener
	{

		@Override
		void handleEvent(final DataspaceEvent dsEvent, final Event event)
		{

			if (event instanceof equip.data.AddEvent)
			{
				final TupleImpl item = (TupleImpl) dsEvent.getAddItem();
				final ComponentRequest compReq = new ComponentRequest(item);
				fireEvent(DataspaceConfigurationListener.class, configurationListeners, "componentRequestAdded",
						new Class[]{compReq.getClass()}, new Object[]{compReq});
			}
			else if (event instanceof equip.data.UpdateEvent)
			{
			}
			else if (event instanceof equip.data.DeleteEvent)
			{
				final TupleImpl item = (TupleImpl) dsEvent.getOldValue();
				final ComponentRequest compReq = new ComponentRequest(item);
				fireEvent(DataspaceConfigurationListener.class, configurationListeners, "componentRequestDeleted",
						new Class[]{compReq.getClass()}, new Object[]{compReq});
			}
			else
			{
				System.out.println("Unkown equip event detected");
			}
		}
	}

	/*
	 * RDFStatement Listener
	 */
	private class MyContainerListener extends MyDSListener
	{
		@Override
		void handleEvent(final DataspaceEvent dsEvent, final Event event)
		{
			if (event instanceof equip.data.AddEvent)
			{
				// maybe do something in the future
			}
			else if (event instanceof DeleteEvent)
			{
				// maybe do something in the future
			}
		}
	}

	/**
	 * listen for DS stuff and pipe to swing sync'd handler
	 */
	abstract class MyDSListener implements DataspaceEventListener, Runnable
	{

		static final int POLL_TIME_MS = 100;
		static final int MAX_WORK_TIME_MS = 100;
		List<DataspaceEvent> events = new ArrayList<>();

		MyDSListener()
		{
			final javax.swing.Timer t = new javax.swing.Timer(POLL_TIME_MS, new java.awt.event.ActionListener()
			{
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent ae)
				{
					// System.out.println("Check events...");
					MyDSListener.this.run();
				}
			});
			t.setRepeats(true);
			t.start();
		}

		@Override
		public void dataspaceEvent(final DataspaceEvent dsEvent)
		{
			synchronized (this)
			{
				events.add(dsEvent);
				/*
				 * if (events.size()==1) { javax.swing.Timer t = new javax.swing.Timer(100, new
				 * java.awt.event.ActionListener() { public void
				 * actionPerformed(java.awt.event.ActionEvent ae) { MyDSListener.this.run(); } });
				 * t.setRepeats(false); t.start(); //SwingUtilities.invokeLater(this); }
				 */
			}
		}

		@Override
		public void run()
		{
			boolean done = false;
			final long start = System.currentTimeMillis();
			while (!done)
			{
				DataspaceEvent dsEvent = null;
				boolean ignore = false;
				synchronized (this)
				{
					if (events.size() == 0)
					{
						done = true;
						continue;
					}
					dsEvent = events.get(0);
					events.remove(0);
					if (events.size() == 0)
					{
						done = true;
						// System.err.println("sole event:
						// "+dsEvent.getEvent());
					}
					else
					{
						// is this event already out of date?
						final ItemData updateItem = dsEvent.getUpdateItem();
						if (updateItem != null)
						{
							for (final DataspaceEvent dsEvent2 : events)
							{
								final ItemData updateItem2 = dsEvent2.getUpdateItem();
								if (updateItem2 != null && updateItem2.id.equals(updateItem.id))
								{
									System.err.println("Ignore superceded update on " + updateItem.id);
									ignore = true;
									break;
								}
							}
							// debug (temp)
							// if (!ignore)
							// System.err.println("Update not superceded up
							// "+updateItem.id);
						}
						// else
						// System.err.println("event: "+dsEvent.getEvent());
					}
				}
				try
				{
					if (!ignore)
					{
						handleEvent(dsEvent, dsEvent.getEvent());
					}
				}
				catch (final Exception e)
				{
					System.err.println("ERROR handling event: " + e);
					e.printStackTrace(System.err);
				}
				final long now = System.currentTimeMillis();
				if (now - start > MAX_WORK_TIME_MS)
				{
					System.err.println("Timeout poll");
					break;
				}
			}
		}

		final void fireEvent(final Class<?> listenerClass, final List<?> listeners, final String methodName,
		                     final Class<?>[] paramTypes, final Object[] args)
		{
			if (listeners == null)
			{
				return;
			}
			try
			{
				final java.lang.reflect.Method method = listenerClass.getMethod(methodName, paramTypes);
				for (Object listener : listeners)
				{
					if (listenerClass.isAssignableFrom(listener.getClass()))
					{
						method.invoke(listener, args);
					}
				}
			}
			catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException nsme)
			{
				nsme.printStackTrace();
			}
		}

		abstract void handleEvent(DataspaceEvent dsEvent, Event event);
	}

	/*
	 * Listen for Property Link Requests
	 */
	private class MyPropertyLinkRequestListener extends MyDSListener
	{
		@Override
		void handleEvent(final DataspaceEvent dsEvent, final Event event)
		{
			if (event instanceof equip.data.AddEvent)
			{
				final TupleImpl item = (TupleImpl) dsEvent.getAddItem();
				final PropertyLinkRequest linkReq = new PropertyLinkRequest(item);
				links.put(linkReq.getID().toString(), linkReq);
				fireEvent(DataspaceConfigurationListener.class, configurationListeners, "propertyLinkRequestAdded",
						new Class[]{linkReq.getClass()}, new Object[]{linkReq});

			}
			else if (event instanceof equip.data.UpdateEvent)
			{
				final TupleImpl item = (TupleImpl) dsEvent.getAddItem();
				final PropertyLinkRequest linkReq = new PropertyLinkRequest(item);
				links.put(linkReq.getID().toString(), linkReq);
				fireEvent(DataspaceConfigurationListener.class, configurationListeners, "propertyLinkRequestUpdated",
						new Class[]{linkReq.getClass()}, new Object[]{linkReq});
			}
			else if (event instanceof equip.data.DeleteEvent)
			{
				final TupleImpl item = (TupleImpl) dsEvent.getOldValue();
				final PropertyLinkRequest linkReq = new PropertyLinkRequest(item);
				links.remove(linkReq.getID().toString());
				fireEvent(DataspaceConfigurationListener.class, configurationListeners, "propertyLinkRequestDeleted",
						new Class[]{linkReq.getClass()}, new Object[]{linkReq});
			}
			else
			{
				System.out.println("Unkown equip event detected");
			}
		}
	}

	/*
	 * RDFStatement Listener
	 */
	class MyRDFStatementListener extends MyDSListener
	{
		@Override
		void handleEvent(final DataspaceEvent dsEvent, final Event event)
		{
			if (event instanceof equip.data.AddEvent)
			{
				// affects node representation(s) in tree
				final TupleImpl tuple = (TupleImpl) dsEvent.getAddItem();
				if (tuple != null)
				{
					final RDFStatement rdf = new RDFStatement(tuple);

					System.out.println("firing metadata addition event");
					System.out.println(rdf.getSubject());
					System.out.println(rdf.getObject());
					System.out.println(rdf.getPredicate());
					fireEvent(ComponentMetadataListener.class, metadataListeners, "componentMetadataAdded",
							new Class[]{Object.class}, new Object[]{rdf});
				}
			}
			else if (event instanceof equip.data.UpdateEvent)
			{
				final TupleImpl tuple = (TupleImpl) dsEvent.getUpdateItem();
				if (tuple != null)
				{
					final RDFStatement rdf = new RDFStatement(tuple);
					fireEvent(ComponentMetadataListener.class, metadataListeners, "componentMetadataUpdated",
							new Class[]{Object.class}, new Object[]{rdf});
				}
			}
			else if (event instanceof equip.data.DeleteEvent)
			{

				final TupleImpl tuple = (TupleImpl) dsEvent.getOldValue();

				if (tuple != null)
				{
					final RDFStatement rdf = new RDFStatement(tuple);
					fireEvent(ComponentMetadataListener.class, metadataListeners, "componentMetadataDeleted",
							new Class[]{Object.class}, new Object[]{rdf});
				}
			}
		}
	}

	private final Map<String, Capability> capabilities = new HashMap<>();
	private final Map<String, ComponentAdvert> components = new HashMap<>();
	private final Map<String, Map<String, ComponentProperty>> properties = new HashMap<>();
	private final Map<String, PropertyLinkRequest> links = new HashMap<>();
	private final List<ComponentListener> compListeners = new ArrayList<>();
	private final List<ComponentPropertyListener> compPropListeners = new ArrayList<>();
	private final List<ComponentMetadataListener> metadataListeners = new ArrayList<>();
	private final List<DataspaceConfigurationListener> configurationListeners = new ArrayList<>();
	// Equip
	private DataspaceBean dataspace = new DataspaceBean();
	private String url;
	/**
	 * map of referenced source property GUID -> Vector of referring source property GUID
	 */
	private Map<GUID, List<GUID>> sourcePropertyReferencedBy = new HashMap<GUID, List<GUID>>();

	private DataspaceMonitor(final String url, final boolean connect)
	{
		this.url = url;
		if (connect && url != null)
		{
			startListening();
		}
	}

	public void addComponentListener(final ComponentListener compListener)
	{
		compListeners.add(compListener);
	}

	public void addComponentMetadataListener(final ComponentMetadataListener metadataListener)
	{
		metadataListeners.add(metadataListener);
	}

	public void addComponentPropertyListener(final ComponentPropertyListener compPropListener)
	{
		compPropListeners.add(compPropListener);
	}

	public void addDataspaceConfigurationListener(final DataspaceConfigurationListener configurationListener)
	{
		configurationListeners.add(configurationListener);
	}

	public void addDataspaceListener(final String name, final DataspaceEventListener listener)
	{
		final equip.data.TupleImpl item = new TupleImpl(null);
		item.id = null;
		item.name = null;
		item.fields[0] = new StringBoxImpl(name);
		try
		{
			dataspace.addDataspaceEventListener(item, false, listener);
		}
		catch (final DataspaceInactiveException ex1)
		{
			System.err.println("Error: Unable to add event listener, dataspace inactive.");
		}
	}

	public final void clearBuffer()
	{
		this.components.clear();
		this.properties.clear();
	}

	public Collection<Capability> getCapabilities()
	{
		return copyCollect(Capability.TYPE, Capability.class);
	}

	public ComponentAdvert getComponentAdvert(final String id)
	{
		return components.get(id);
	}

	public final Map<String, ComponentAdvert> getComponentAdverts()
	{
		return this.components;
	}

	public Capability getComponentCapability(final ComponentAdvert ca)
	{
		try
		{
			final GUID capID = ca.getCapabilityID();
			if (capID != null)
			{ // might be null for dyn components
				final TupleImpl tuple = (TupleImpl) dataspace.getItem(capID);
				if (tuple != null)
				{
					return new Capability(tuple);
				}
			}
		}
		catch (final DataspaceInactiveException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public final Map<String, Map<String, ComponentProperty>> getComponentProperties()
	{
		return this.properties;
	}

	/*
	 * public final ComponentProperty getComponentProperty(String id) { return (ComponentProperty)
	 * properties.get(id); }
	 */

	/**
	 * Returns an array of all the properties for a specific component id.
	 */
	public Collection<ComponentProperty> getComponentProperties(final String compID)
	{
		final Map<String, ComponentProperty> props = getComponentProperties().get(compID);
		if (props == null)
		{
			return null;
		}
		return props.values();
	}

	public ComponentProperty getComponentProperty(final String componentID, final String propertyName)
	{
		final Collection<ComponentProperty> props = getComponentProperties(componentID);
		if (props != null)
		{
			for (final ComponentProperty prop : props)
			{
				if (prop.getPropertyName().equals(propertyName))
				{
					return prop;
				}
			}
		}
		return null;
	}

	public final DataspaceBean getDataspace()
	{
		return dataspace;
	}

	public Collection<Capability> getMatchingClassCapabilities(final String regex)
	{
		final List<Capability> matches = new ArrayList<Capability>();
		for (final Capability cap : getCapabilities())
		{
			if (cap.getClassification().matches(regex))
			{
				matches.add(cap);
			}
		}
		return matches;
	}

	public PropertyLinkRequest getPropertyLink(final String beanid)
	{
		return links.get(beanid);
	}

	public List<PropertyLinkRequest> getPropertyLinks()
	{
		return copyCollect(PropertyLinkRequest.TYPE, PropertyLinkRequest.class);
	}

	/**
	 * Convenient method for acquiring PropertyLinkRequests from specific source and/or target
	 */
	public Collection<PropertyLinkRequest> getPropertyLinks(final String sourceComponentID, final String targetComponentID)
	{
		final PropertyLinkRequest template = new PropertyLinkRequest((GUID) null);

		final GUID sourceGUID = getComponentAdvert(sourceComponentID).getComponentID();
		final GUID targetGUID = getComponentAdvert(targetComponentID).getComponentID();

		template.setSourceComponentID(sourceGUID);
		template.setDestComponentID(targetGUID);
		final List<PropertyLinkRequest> links = new ArrayList<PropertyLinkRequest>();
		try
		{
			final CompInfo[] results = template.copyCollect(dataspace);
			for (CompInfo result : results)
			{
				links.add(new PropertyLinkRequest((TupleImpl) result.tuple));
			}
		}
		catch (final DataspaceInactiveException e)
		{
			Info.message(this, "Warning: Doing search on an inactive dataspace");
			return null;
		}
		return links;
	}

	public Capability getCapability(final String className)
	{
		final Collection<Capability> capabilities = getCapabilities();
		for(Capability capability: capabilities)
		{
			if(className.equals(capability.getCapabilityClass()))
			{
				return capability;
			}
		}
		return null;
	}

	public ComponentAdvert createComponent(String className)
	{
		try
		{
			final Capability capability = getCapability(className);
			if (capability == null)
			{
				System.out.println("Couldn't find capability of " + className);
				return null;
			}

			GUID requestID = dataspace.allocateId();

			final ComponentRequest request = new ComponentRequest(requestID);
			request.setRequestID(requestID.toString());
			request.setHostID(DataspaceUtils.stringToGUID(capability.getHostID()));
			request.setContainerID(capability.getContainerID());
			request.setCapabilityID(capability.getID());

			dataspace.addPersistent(request.tuple, null);

			long start = System.currentTimeMillis();
			System.out.println("Request = " + requestID.toString());
			while ((System.currentTimeMillis() - start) < TIMEOUT)
			{
				Thread.sleep(300);
				for(ComponentAdvert component: getComponentAdverts().values())
				{
					System.out.println("Component has request " + component.getComponentRequestID().toString());
					if(component.getComponentRequestID().equals(requestID))
					{
						return component;
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/*
	 * 
	 */
	public PropertyLinkRequest createLink(final ComponentProperty source, final ComponentProperty dest)
	{
		final PropertyLinkRequest linkreq = new PropertyLinkRequest(dataspace.allocateId());
		linkreq.setSourceComponentID(source.getComponentID());
		linkreq.setSourcePropertyName(source.getPropertyName());
		linkreq.setSourcePropID(source.getID());
		linkreq.setDestComponentID(dest.getComponentID());
		linkreq.setDestinationPropertyName(dest.getPropertyName());
		linkreq.setDestinationPropID(dest.getID());

		try
		{

			linkreq.addtoDataSpacePersistent(dataspace, /* lease */null);
			System.out.println("Issued Link Request...");
			System.out.println("Source : " + source.getPropertyName() + ", Comp. id: " + source.getComponentID());
			System.out.println("Destination : " + dest.getPropertyName() + ", Comp. id: " + dest.getComponentID()
					+ "\n-");
		}
		catch (final DataspaceInactiveException ex)
		{
			System.out.println("Error: Can't add Link to dataspace - inactive");
			ex.printStackTrace();
		}

		return linkreq;
	}

	public void removeComponentListener(final ComponentListener compListener)
	{
		compListeners.remove(compListener);
	}

	public void removeComponentMetadataListener(final ComponentMetadataListener metadataListener)
	{
		metadataListeners.remove(metadataListener);
	}

	public void removeComponentPropertyListener(final ComponentPropertyListener compPropListener)
	{
		compPropListeners.remove(compPropListener);
	}

	public void removeDataspaceConfigurationListener(final DataspaceConfigurationListener configurationListener)
	{
		configurationListeners.remove(configurationListener);
	}

	public String getPropertyValue(final GUID propertyID)
	{
		try
		{
			final ComponentProperty template = new ComponentProperty(propertyID);
			final ComponentProperty property = template.copyCollectAsComponentProperty(dataspace)[0];
			final Object value = property.getPropertyValue(dataspace);
			return Coerce.toClass(value, String.class);
		}
		catch (final ConnectionPointTypeException ex)
		{
			System.err.println("ERROR: " + ex);
			ex.printStackTrace(System.err);
			return null;
		}
		catch (final Exception cnfe)
		{
			System.err.println("Warning: Could not get value for property '" + propertyID.toString() + "'");
			cnfe.printStackTrace();
			return null;
		}
	}

	public String getPropertyValue(final ComponentProperty property)
	{
		try
		{
			final Object value = property.getPropertyValue(dataspace);
			return Coerce.toClass(value, String.class);
		}
		catch (final ConnectionPointTypeException ex)
		{
			System.err.println("ERROR: " + ex);
			ex.printStackTrace(System.err);
			return null;
		}
		catch (final Exception cnfe)
		{
			System.err.println("Warning: Could not get value for property '" + property.getPropertyName() + "'");
			cnfe.printStackTrace();
			return null;
		}
	}


	public boolean setProperty(final ComponentProperty targetProperty, final String value)
	{
		try
		{
			String actualValue = getPropertyValue(targetProperty);
			System.out.println("Value = " + actualValue + ". Setting to " + value);
			if((value == null && actualValue == null) || (value != null && value.equals(actualValue)))
			{
				// Already set
				return true;
			}

			final GUID propertyID = dataspace.allocateId();
			final GUID componentID = dataspace.allocateId();
			final GUID linkID = dataspace.allocateId();
			final ComponentProperty prop = new ComponentProperty(propertyID);
			prop.setPropertyName(PROPERTY_NAME);
			prop.setPropertyClass(String.class);
			prop.setPropertyValue(value);
			prop.setComponentID(componentID);
			final PropertyLinkRequest link = new PropertyLinkRequest(linkID);
			link.setSourcePropertyName(PROPERTY_NAME);
			link.setSourcePropID(propertyID);
			link.setSourceComponentID(componentID);
			link.setDestinationPropertyName(targetProperty.getPropertyName());
			link.setDestinationPropID(targetProperty.getID());
			link.setDestComponentID(targetProperty.getComponentID());

			prop.addtoDataSpace(dataspace);
			link.addtoDataSpace(dataspace);

			long start = System.currentTimeMillis();
			while((System.currentTimeMillis() - start) < TIMEOUT)
			{
				Thread.sleep(100);
				actualValue = getPropertyValue(targetProperty.getID());
				System.out.println("Value = " + actualValue + ". Setting to " + value);
				if((value == null && actualValue == null) || (value != null && value.equals(actualValue)))
				{
					break;
				}
			}

			dataspace.delete(linkID);
			dataspace.delete(propertyID);

			actualValue = getPropertyValue(targetProperty.getID());

			return (value == null && actualValue == null) || (value != null && value.equals(actualValue));
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public void startListening()
	{
		startListening(url, true);
	}

	public void startListening(final String url)
	{
		startListening(url, true);
	}

	public void startListening(String url, final boolean clearBuffer)
	{
		// async connect
		this.url = url;
		dataspace.setRetryConnect(true);

		if (!url.contains(":"))
		{
			System.out.println("Using discovery with group " + url);
			url = new DataspaceDiscover(url).getFirstDataspace();
			System.out.println("-> " + url);
		}

		if (clearBuffer)
		{
			clearBuffer();
		}

		try
		{
			// should remove any existing data space listeners first...
			dataspace.setDataspaceUrl(url);
			addDataspaceListener(ComponentAdvert.TYPE, new MyComponentAdvertListener());
			addDataspaceListener(ComponentProperty.TYPE, new MyComponentPropertyListener());
			addDataspaceListener(RDFStatement.TYPE, new MyRDFStatementListener());
			addDataspaceListener(Capability.TYPE, new MyCapabilityListener());
			addDataspaceListener(PropertyLinkRequest.TYPE, new MyPropertyLinkRequestListener());
			addDataspaceListener(ComponentRequest.TYPE, new MyComponentRequestListener());
			addDataspaceListener(Container.TYPE, new MyContainerListener());

		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private <T> List<T> copyCollect(final String type, final Class<T> itemClass)
	{
		final equip.data.TupleImpl item = new TupleImpl(null);
		item.id = null;
		item.name = null;
		item.fields[0] = new StringBoxImpl(type);
		try
		{
			final ItemData[] items = dataspace.copyCollect(item);
			if (items == null || items.length < 1)
			{
				return Collections.emptyList();
			}
			final java.lang.reflect.Constructor<T> constructor = itemClass.getConstructor(TupleImpl.class);
			final List<T> results = new ArrayList<>(items.length);
			for (final ItemData item2 : items)
			{
				try
				{
					results.add(constructor.newInstance(item2));
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
			return results;
		}
		catch (final NoSuchMethodException nsme)
		{
			nsme.printStackTrace();
			return Collections.emptyList();
		}
		catch (final DataspaceInactiveException ex)
		{
			Info.message("Warning: Doing copy collect on inactive dataspace");
			return Collections.emptyList();
		}
	}
}