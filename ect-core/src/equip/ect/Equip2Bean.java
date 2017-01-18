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

Created by: James Mathrick (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Shahram Izadi (University of Nottingham)
  Jan Humble (University of Nottingham)
  James Mathrick (University of Nottingham)

 */
package equip.ect;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import equip.data.DataSession;
import equip.data.GUID;
import equip.data.ItemData;
import equip.data.StringBoxImpl;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceEventListener;
import equip.data.beans.DataspaceInactiveException;

/**
 * @author imt
 */
class Equip2Bean implements DataspaceEventListener
{
	private static final boolean debug = false;
	private static final int KIND_MY_PROPERTIES = 1;
	private static final int KIND_REFS_TO_MY_PROPERTIES = 2;
	private static final int KIND_LINK_SOURCES = 3;
	private static final int KIND_LINK_SOURCE_REFS = 4;
	private static final int KIND_UPDATE_OF_REFERENCED = 5;
	private static final String[] kind_names = new String[]{"0", "my properties", "refs to my properties", "link sources",
			"link source refs", "update of referenced"};
	private final MappingObject parent;
	private final DataspaceBean dataspace;
	private final Serializable bean;
	// private DataSession linksession = null;
	private final Map<GUID, List<GUID>> sourcePropertyReferencedBy = new HashMap<>();
	private final Map<GUID, ReferringPropertyInfo> destPropertySessions = new HashMap<>();
	private final Map<GUID, DataSession> linkSessions = new HashMap<>();
	private final Map<GUID, Map<GUID, DataSession>> linkRefSessions = new HashMap<>();
	private final Map<GUID, int[]> knownItems = new HashMap<>();
	/**
	 * list of delayed sets
	 */
	private final List<DelayedSet> delayedSets = new ArrayList<>();
	private GUID beanID = null;
	private DataSession propsession = null;
	private equip.data.beans.DataspaceEvent lastUpdateEvent;

	Equip2Bean(final MappingObject parent, final DataspaceBean dspace)
	{

		this.dataspace = dspace;
		this.parent = parent;
		this.bean = parent.getBean();
		this.beanID = parent.getBeanID();

		// Build pattern for the property tuple
		final ComponentProperty proptemplate = new ComponentProperty((GUID) null);
		proptemplate.setComponentID(parent.getBeanID());

		// Build pattern for the link tuple
		// PropertyLinkRequest linktemplate = new PropertyLinkRequest((GUID)null);
		// linktemplate.setDestComponentID(parent.getBeanID());
		// leave to discovery of properties

		// Put Patterns in the Space
		try
		{
			propsession = proptemplate.addPatterntoDataSpace(dataspace,
					new MyDataspaceEventListener(KIND_MY_PROPERTIES));
			// linksession = linktemplate.addPatterntoDataSpace(dataspace, this);
		}
		catch (final DataspaceInactiveException ex)
		{
			System.out.println("Dataspace inacative ");
			ex.printStackTrace();
		}
	}

	@Override
	public void dataspaceEvent(final equip.data.beans.DataspaceEvent event)
	{
		dataspaceEvent(event, 0, null, null);
	}

	public void stop()
	{
		try
		{
			// this.dataspace.removeDataspaceEventListener(linksession);
			this.dataspace.removeDataspaceEventListener(propsession);
		}
		catch (final DataspaceInactiveException ex)
		{
			System.out.println("Dataspace inacative ");
			ex.printStackTrace();
		}
	}

	private void addDestPropertySession(final GUID id)
	{
		// look also for links to ourselves
		addDestPropertySession(id, true, null);
	}

	private void addDestPropertySession(final GUID id, final boolean includeLinks, final GUID ref)
	{
		final ReferringPropertyInfo info = new ReferringPropertyInfo();
		info.id = id;
		info.refid = ref;

		// Build pattern for the property tuple
		final ComponentProperty proptemplate = new ComponentProperty((GUID) null);
		proptemplate.setConnectionPointType(ComponentProperty.CONNECTION_POINT_PROPERTY_REFERENCE);
		try
		{
			proptemplate.setPropertyReference(id);
		}
		catch (final ConnectionPointTypeException e)
		{
		}
		final PropertyLinkRequest linktemplate = new PropertyLinkRequest((GUID) null);
		linktemplate.setDestinationPropID(id);
		try
		{
			destPropertySessions.put(id, info);
			info.refSession = proptemplate.addPatterntoDataSpace(dataspace, new MyDataspaceEventListener(
					KIND_REFS_TO_MY_PROPERTIES));
			if (includeLinks)
			{
				info.linkSession = linktemplate.addPatterntoDataSpace(dataspace, this);
			}
		}
		catch (final DataspaceInactiveException e)
		{
		}
	}

	// DataspaceEventListener method
	private void dataspaceEvent(final equip.data.beans.DataspaceEvent event, final int kind,
	                            final PropertyLinkRequest link, final PropertyDescriptor prop)
	{
		synchronized (ContainerManager.class)
		{
			synchronized (parent)
			{ // also sync with MappingObject - for prop change events
				// Add Events
				if (event.getEvent() instanceof equip.data.AddEvent)
				{
					if (debug)
					{
						System.out.println("Add Event" + event);
					}

					final ItemData additem = event.getAddItem();
					final equip.data.TupleImpl added = (equip.data.TupleImpl) (additem);
					final String tupleType = ((StringBoxImpl) added.fields[0]).value;

					if (tupleType.matches(PropertyLinkRequest.TYPE))
					{
						// there is a bit of this when MappingObject must not be locked
						this.linkAdded(new PropertyLinkRequest(added));
					}
					else if (tupleType.equals(ComponentProperty.TYPE))
					{
						// there is a bit of this when MappingObject must not be locked
						this.propUdate(new ComponentProperty(added), true, false, kind, link, prop);
					}
				}

				// UPDATE Events
				if (event.getEvent() instanceof equip.data.UpdateEvent)
				{
					if (event == lastUpdateEvent)
					{
						return;
					}
					lastUpdateEvent = event;

					final ItemData updateditem = event.getUpdateItem();
					final equip.data.TupleImpl updated = (equip.data.TupleImpl) (updateditem);
					final String tupleType = ((StringBoxImpl) updated.fields[0]).value;

					if (tupleType.equals(ComponentProperty.TYPE))
					{
						// there is a bit of this when MappingObject must not be locked
						this.propUdate(new ComponentProperty(updated), false, false, kind, link, prop);
					}
				}

				if (event.getEvent() instanceof equip.data.DeleteEvent)
				{
					if (debug)
					{
						System.out.println("DELETE Event" + event);
					}
					// remove of property link request...
					final ItemData delitem = event.getOldValue();
					if (delitem == null)
					{
						System.err.println("Delete " + event.getDeleteId() + " -> no old value :-(");
						return;
					}

					final equip.data.TupleImpl deleted = (equip.data.TupleImpl) (delitem);
					final String tupleType = ((StringBoxImpl) deleted.fields[0]).value;

					if (tupleType.matches(PropertyLinkRequest.TYPE))
					{
						this.linkDeleted(new PropertyLinkRequest(deleted));
					}
					else if (tupleType.equals(ComponentProperty.TYPE))
					{
						this.propUdate(new ComponentProperty(deleted), false, true, kind, link, prop);
					}
				}
			}// sync parent
			// do delayed sets without mapping object lock
			while (delayedSets.size() > 0)
			{
				final DelayedSet set = delayedSets.get(0);
				delayedSets.remove(0);
				set.invoke();
			}
		}// sync container
	}

	private GUID findLocalDestination(GUID id)
	{
		ReferringPropertyInfo info;
		while (true)
		{
			info = destPropertySessions.get(id);
			if (info == null)
			{
				System.err.println("WARNING: mapping link destination " + id + " to local property seems to fail");
			}
			if (info != null && info.refid != null)
			{
				// debug
				if (debug)
				{
					System.out.println("Link to " + id + " -> " + info.refid);
				}
				id = info.refid;
			}
			else
			{
				break;
			}
		}
		return id;
	}

	private boolean isSetValuePopupLink(final GUID source) throws DataspaceInactiveException
	{
		String propertyName = null;

		final TupleImpl sourceItem = (TupleImpl) (dataspace.getItem(source));
		if (sourceItem == null)
		{
			return false;
		}
		final ComponentProperty cp = new ComponentProperty(sourceItem);

		propertyName = cp.getPropertyName();

		return propertyName.equals("SetValuePopup");
	}

	/*
	 * NOT USED?! public void propImplUpdated(equip.data.GUID propImplID,
	 * equip.data.SerializedObjectImpl value) {
	 *
	 * // Get the value Serializable deserialized = null; try { deserialized = (Serializable)
	 * value.getValue(); } catch (ClassNotFoundException ex) { ex.printStackTrace(); } catch
	 * (IOException ex) { ex.printStackTrace(); }
	 *
	 * // Set the Property
	 *
	 * // or SetPropDesc??? PropertyDescriptor descriptor = this.parent.getPropDesc(propImplID);
	 *
	 * // Object bean = this.bean; try { descriptor.getWriteMethod().invoke(bean, new Object[] {
	 * MappingObject.mapPropertyValueOnSet(deserialized, descriptor.getPropertyType() )}); } catch
	 * (Exception e) { System.out.println(" Equip2Bean Execption in setting value");
	 * e.printStackTrace();
	 *
	 * } }
	 */
	private void linkAdded(final PropertyLinkRequest linkReq)
	{

		// retain enough info to reverse....!!

		final GUID source = linkReq.getSourcePropID();
		GUID dest = linkReq.getDestinationPropID();

		// does it map to us by reference?!
		dest = findLocalDestination(dest);

		final PropertyDescriptor targetdescriptor = this.parent.getPropDesc(dest);

		// Add this Impl as being linked to the propertyELement

		this.parent.addSetPropDesc(source, targetdescriptor);

		// Build pattern for the property tuple

		if (bean instanceof IActiveComponent)
		{
			try
			{
				if (!isSetValuePopupLink(source))
				{

					// a link to a non-existent dynamic property? fall back to specified name
					final String targetName = (targetdescriptor != null) ? targetdescriptor.getName() : linkReq
							.getDestinationPropertyName();
					((IActiveComponent) bean).linkToAdded(targetName, linkReq.getID());
				}
			}
			catch (final DataspaceInactiveException e)
			{
				e.printStackTrace();
			}
		}
		final ComponentProperty proptemplate = new ComponentProperty((GUID) null);
		proptemplate.setID(source);
		// proptemplate.setID(source);
		try
		{
			if (debug)
			{
				System.out.println("Add source property listener for " + source + " via link " + linkReq.getID());
			}
			// this will raise a setting callback if already present in DS
			final DataSession session = proptemplate.addPatterntoDataSpace(dataspace, new MyDataspaceEventListener(
					KIND_LINK_SOURCES, linkReq, targetdescriptor));
			linkSessions.put(linkReq.getID(), session);
		}
		catch (final DataspaceInactiveException ex)
		{
			System.out.println("Equip2Bean Dataspace inacative ");
			ex.printStackTrace();
		}
	}

	private void linkDeleted(final PropertyLinkRequest linkReq)
	{

		final GUID source = linkReq.getSourcePropID();
		GUID dest = linkReq.getDestinationPropID();
		// Build pattern for the property tuple

		// does it map to us by reference?!
		dest = findLocalDestination(dest);

		final PropertyDescriptor targetdescriptor = this.parent.getPropDesc(dest);

		if (bean instanceof IActiveComponent)
		{
			try
			{
				if (!isSetValuePopupLink(source))
				{
					((IActiveComponent) bean).linkToDeleted(targetdescriptor.getName(), linkReq.getID());
				}
			}

			catch (final DataspaceInactiveException e)
			{
			}
		}

		// remove source component monitor
		try
		{
			final DataSession session = linkSessions.get(linkReq.getID());
			if (session == null)
			{
				System.err.println("Unknown PropertyLinkRequest deleted: " + linkReq.getID());
			}
			else
			{
				if (debug)
				{
					System.out
							.println("Remove source property listener for " + source + " via link " + linkReq.getID());
				}
				dataspace.removeDataspaceEventListener(session);
				linkSessions.remove(linkReq.getID());
			}
		}
		catch (final DataspaceInactiveException ex)
		{
			System.out.println("Equip2Bean Dataspace inacative ");
			ex.printStackTrace();
		}

		// Remove this Impl as being linked to the propertyELement

		this.parent.removeSetPropDesc(source, targetdescriptor);
	}

	private void propUdate(final ComponentProperty prop, boolean added, final boolean deleted, final int kind,
	                       final PropertyLinkRequest link, final PropertyDescriptor targetproperty)
	{
		// note that persistent compound component properties can report redundant adds (should be
		// treated
		// as updates); these may appear as REFS_TO_MY_PROPERTIES and as LINK_SOURCES and
		// LINK_SOURCE_REFS
		// ....
		if (added)
		{
			int[] count = knownItems.computeIfAbsent(prop.getID(), k -> new int[1]);
			count[0]++;
			if (count[0] > 1)
			{
				if (debug)
				{
					System.err.println("NOTE: Downrate add to update on known item " + prop.getID());
				}
				added = false;
			}
		}
		else if (deleted)
		{
			final int[] count = knownItems.remove(prop.getID());
			if (count == null)
			{
				System.err.println("WARNING: delete for (now) unknown item " + prop.getID());
			}
		}

		if (debug)
		{
			System.out.println("propUdate of " + prop.getID() + " "
					+ (added ? "added" : (deleted ? "deleted" : "updated")) + ", kind " + kind_names[kind] + ", link "
					+ (link != null ? link.getID().toString() : "no link") + ", target "
					+ (targetproperty != null ? targetproperty.getName() : "none"));
		}

		if (kind == 0)
		{
			System.err.println("propUdate kind = 0");
		}
		// is it one of our own properties?! - make sure i am watching for references to it
		if (kind == KIND_MY_PROPERTIES && prop.getComponentID().equals(beanID))
		{
			if (added)
			{
				// debug
				if (debug)
				{
					System.out.println("Add referenceto monitor for my property " + prop.getPropertyName() + " ("
							+ prop.getID() + ")");
				}

				addDestPropertySession(prop.getID());
			}
			else if (deleted)
			{
				if (debug)
				{
					System.out.println("Remove referenceto monitor for my property " + prop.getPropertyName() + " ("
							+ prop.getID() + ")");
				}
				removeDestPropertySession(prop.getID());
			}
		}
		// is it a reference to one of my properties (possibly indirectly)? make sure i watch for
		// links to it
		final String type = prop.getConnectionPointType();
		// update references
		if (kind == KIND_REFS_TO_MY_PROPERTIES && (added || deleted)
				&& type.equals(ComponentProperty.CONNECTION_POINT_PROPERTY_REFERENCE))
		{
			try
			{
				final GUID ref = prop.getPropertyReference();
				if (ref != null)
				{
					if (debug)
					{
						System.out.println((deleted ? "delete" : "add") + " property " + prop.getID()
								+ " which is ref to " + ref);
					}

					// does it refer to something we are interested in?
					final ReferringPropertyInfo info = destPropertySessions.get(prop.getID());
					if (info == null && !deleted)
					{

						if (debug)
						{
							System.out
									.println("Add referenceto monitor for refproperty " + prop.getID() + " -> " + ref);
						}
						// yes - we should be interested in it and in links to it
						addDestPropertySession(prop.getID(), true, ref);
					}
					else if (info != null && deleted)
					{
						if (debug)
						{
							System.out.println("Remove referenceto monitor for refproperty " + prop.getID() + " -> "
									+ ref);
						}
						removeDestPropertySession(prop.getID());
					}
				}
			}
			catch (final ConnectionPointTypeException e)
			{
			}
		}

		// handle normal set, including source properties which are references to other properties
		Object value = null;
		try
		{
			// update references
			if ((kind == KIND_LINK_SOURCE_REFS || kind == KIND_LINK_SOURCES) && (added || deleted)
					&& type.equals(ComponentProperty.CONNECTION_POINT_PROPERTY_REFERENCE))
			{
				final GUID ref = prop.getPropertyReference();
				// debug
				if (ref != null)
				{
					List<GUID> v = sourcePropertyReferencedBy.get(ref);
					// not my own properties (or ones which ref them), so should be a source
					if (!deleted /* && !destPropertySessions.containsKey(ref) */)
					{
						if (v == null)
						{
							v = new ArrayList<GUID>();
							sourcePropertyReferencedBy.put(ref, v);
						}
						if (!v.contains(prop.getID()))
						{
							v.add(prop.getID());
						}

						// listen to it
						Map<GUID, DataSession> h = linkRefSessions.computeIfAbsent(link.getID(), k -> new HashMap<>());
						final ComponentProperty proptemplate = new ComponentProperty((GUID) null);
						proptemplate.setID(ref);
						try
						{
							if (debug)
							{
								System.out.println("Listen for referenced property " + ref + " via link "
										+ link.getID());
							}
							final DataSession session = proptemplate
									.addPatterntoDataSpace(dataspace, new MyDataspaceEventListener(
											KIND_LINK_SOURCE_REFS, link, targetproperty));
							h.put(ref, session);
						}
						catch (final DataspaceInactiveException e)
						{
						}
					}
					else if (deleted)
					{
						if (v != null)
						{
							v.remove(prop.getID());
							if (v.size() == 0)
							{
								sourcePropertyReferencedBy.remove(ref);
							}
						}
						final Map<GUID, DataSession> h = linkRefSessions.get(link.getID());
						if (h != null)
						{
							// stop listening to it
							final DataSession session = (DataSession) h.remove(ref);
							if (h.size() == 0)
							{
								linkRefSessions.remove(link.getID());
							}
							try
							{
								if (debug)
								{
									System.out.println("Stop listening for referenced property " + ref + " via link "
											+ link.getID());
								}
								dataspace.removeDataspaceEventListener(session);
							}
							catch (final DataspaceInactiveException e)
							{
							}
						}
					}
				}
			}

			if (kind == KIND_LINK_SOURCE_REFS || kind == KIND_UPDATE_OF_REFERENCED)
			{
				// make sure propUdate is called on any properties which reference this
				final List<GUID> v = sourcePropertyReferencedBy.get(prop.getID());
				if (!deleted && v != null)
				{
					for (GUID id : v)
					{
						try
						{
							final TupleImpl t = (TupleImpl) dataspace.getItem(id);
							if (t != null && t.name.equals(ComponentProperty.TYPE))
							{
								if (debug)
								{
									System.out.println("Change to property " + prop.getID()
											+ " triggers checking update on property " + id);
								}
								final ComponentProperty p2 = new ComponentProperty(t);
								propUdate(p2, false, false, KIND_UPDATE_OF_REFERENCED, link, targetproperty);
							}
							else
							{
								System.err.println("Unable to check update on property " + id + " (" + t + ", name="
										+ (t != null ? t.name : "null") + ")");
							}
						}
						catch (final DataspaceInactiveException e)
						{
						}
					}
				}
			}

			if (kind != KIND_UPDATE_OF_REFERENCED && kind != KIND_LINK_SOURCES)
			{
				return;
			}

			// value
			if (type.equals(ComponentProperty.CONNECTION_POINT_PROPERTY_VALUE)
					|| type.equals(ComponentProperty.CONNECTION_POINT_PROPERTY_REFERENCE))
			{
				value = prop.getPropertyValue(dataspace);
			}
			else
			{
				System.out.println("WARNING: unhandled connection type: " + type);
			}
		}
		catch (final ConnectionPointTypeException ex)
		{
			System.err.println("ERROR: " + ex);
			ex.printStackTrace(System.err);
			return;
		}
		// catch (IOException ex) {
		//
		// System.out.println("I/O Error for  " + prop.getPropertyName());
		// return;
		// }
		// catch (ClassNotFoundException ex) {
		// System.out.println("Class not found for  " + prop.getPropertyName());
		// return;
		// }

		// propertylinkrequest to "blame"?
		final GUID requestId = link.getID();

		// object oldbalue = descriptor.getReadMethod().invoke()
		// Object oldvalue = descriptor.getReadMethod().invoke(bean, null);

		if (targetproperty != null && targetproperty.getWriteMethod() != null)
		{
			// mapping object must not be locked during this call
			// - delay
			try
			{
				delayedSets.add(new DelayedSet(targetproperty, targetproperty.getWriteMethod(), bean, MappingObject
						.mapPropertyValueOnSet(value, targetproperty.getPropertyType()), requestId, deleted));
			}
			catch (final Exception e)
			{
				System.err.println("ERROR mapping value " + value + " to property type " + targetproperty.getPropertyType()
						+ ": " + e);
				e.printStackTrace(System.err);
			}
		}

	}

	private void removeDestPropertySession(final GUID id)
	{
		// deleted
		final ReferringPropertyInfo info = destPropertySessions.get(id);
		if (info != null)
		{
			// debug
			// System.out.println("Removed referenceto monitor for my property "+prop.getPropertyName());
			destPropertySessions.remove(id);
			try
			{
				if (info.linkSession != null)
				{
					this.dataspace.removeDataspaceEventListener(info.linkSession);
				}
				if (info.refSession != null)
				{
					this.dataspace.removeDataspaceEventListener(info.refSession);
				}
			}
			catch (final DataspaceInactiveException e)
			{
			}
			// recurse?!
		}
	}

	class MyDataspaceEventListener implements DataspaceEventListener
	{
		int kind;
		PropertyLinkRequest link;
		PropertyDescriptor prop;

		MyDataspaceEventListener(final int kind)
		{
			this.kind = kind;
			this.link = null;
		}

		MyDataspaceEventListener(final int kind, final PropertyLinkRequest link, final PropertyDescriptor prop)
		{
			this.kind = kind;
			this.link = link;
			this.prop = prop;
		}

		@Override
		public void dataspaceEvent(final equip.data.beans.DataspaceEvent event)
		{
			Equip2Bean.this.dataspaceEvent(event, kind, link, prop);
		}
	}

	class ReferringPropertyInfo
	{
		GUID id;
		DataSession refSession;
		DataSession linkSession;
		GUID refid;
	}

	/**
	 * a delayed set
	 */
	protected class DelayedSet
	{
		java.beans.PropertyDescriptor descriptor;
		java.lang.reflect.Method method;
		Object target;
		Object value;
		GUID requestId;
		boolean deleted;

		DelayedSet(final java.beans.PropertyDescriptor descriptor, final java.lang.reflect.Method method,
		           final Object target, final Object value, final GUID requestId, final boolean deleted)
		{
			this.descriptor = descriptor;
			this.method = method;
			this.target = target;
			this.value = value;
			this.requestId = requestId;
			this.deleted = deleted;
		}

		void invoke()
		{
			try
			{
				if (target instanceof IActiveComponent)
				{

					final boolean done = ((IActiveComponent) target).linkToUpdated(descriptor.getName(), requestId,
							deleted ? null : value);
					if (done)
					{
						return;
						// drop through to regular set
					}

				}
				if (deleted)
				{
					// no op?! (backward compatible)
					return;
				}
				if (descriptor instanceof DynamicPropertyDescriptor)
				{
					final DynamicPropertyDescriptor dprop = (DynamicPropertyDescriptor) descriptor;
					// invoke
					dprop.writeProperty((DynamicProperties) target, value);
				}
				else
				{
					// mapping object must not be locked during this call
					method.invoke(target, value);
				}
			}
			catch (final Exception e)
			{

				System.out.println("Equip2Bean Exception in setting value");
				e.printStackTrace();

			}
		}
	}
}
