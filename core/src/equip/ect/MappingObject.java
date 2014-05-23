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
 Jan Humble (University of Nottingham)
 Shahram Izadi (University of Nottingham)
 Chris Allsop (University of Nottingham)
 James Mathrick (University of Nottingham)
 */
package equip.ect;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import equip.data.BooleanBoxImpl;
import equip.data.GUID;
import equip.data.StringBox;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;

class MappingObject
{
	private Serializable bean;

	// Remove tar private HashMap propIDs = new HashMap();

	// private HashMap propVals = new HashMap();
	// Maps between the Property Names and the ComponentProperties that place
	// them in Equip
	private Map<String, ComponentProperty> propImpls = new HashMap<String, ComponentProperty>();

	// Maps between IDs of the tuples in Equip and the corresponding Property
	// Descriptors
	private Map<GUID, PropertyDescriptor> propDescs = new HashMap<GUID, PropertyDescriptor>();

	// Maps between IDs of the tuples in Equip and the LinkedList of
	// corresponding Property Descriptors
	private Map<GUID, List<PropertyDescriptor>> setPropDescs = new HashMap<GUID, List<PropertyDescriptor>>();

	// Maps property name to Vector of Serializable (children) (which are
	// current exported)
	private Map<String, List<Serializable>> childProperties = new HashMap<String, List<Serializable>>();

	private equip.data.GUID beanid = null;

	private Bean2Equip toEquip;

	private Equip2Bean fromEquip;

	private ContainerManager containerman = null;

	/**
	 * static copy for mapping values
	 */
	private static ContainerManager scontainerman = null;

	private ComponentAdvert compinst;

	// the capability id and component request associated with
	// the instantiated component (contained within advert issued)
	// can be null if component is not instantiated from a
	// capabilty or in response to a request
	private GUID capId = null;

	private GUID compReqId = null;

	// /support recovery of guids after container shutdown
	private GUID compId = null;

	private ComponentProperty[] compProps = null;

	private DataspaceBean dataspace;

	private java.beans.BeanInfo beaninf = null;

	/**
	 * if this is a subcomponent, then the MappingObject of the parent
	 */
	private MappingObject parent;

	/**
	 * if this is a subcomponent, then the persistentChild property (if any)
	 */
	private String persistentChild;

	/**
	 * name of property required on a persistent child (sub)component
	 */
	public final static String PERSISTENT_CHILD = "persistentChild";

	/**
	 * name of getter required on a persistent child (sub)component
	 */
	public final static String GET_PERSISTENT_CHILD = "getPersistentChild";

	/**
	 * name of pseudo-property on a child with a Strinified parent GUID
	 */
	public final static String PARENT = "parent";

	/**
	 * static mapping of property values; String -> java.lang.ref.WeakReference(value)
	 */
	protected static Map<String, WeakReference<Object>> propertyValueMap = new HashMap<String, WeakReference<Object>>();

	/**
	 * map a raw property value to a publishable/serializable one
	 */
	public static Object mapPropertyValueOnGet(final Object value, final Class<?> type)
	{
		if (value == null) { return null; }
		if (type != null && (type.isInterface() || (type.isArray() && type.getComponentType().isInterface())))
		{
			if (!type.isInstance(value))
			{
				System.err.println("ERROR: interface/array property type " + type + " has value type "
						+ value.getClass());
				return value;
			}
			// debug
			System.out.println("mapPropertyValueOnGet for interface/array type " + type);

			synchronized (propertyValueMap)
			{
				int size = 1;
				Object result = null;
				if (type.isArray())
				{
					size = Array.getLength(value);
					result = Array.newInstance(String.class, size);
				}
				for (int i = 0; i < size; i++)
				{
					Object val = value;
					Object res = null;
					if (type.isArray())
					{
						val = Array.get(value, i);
					}
					// look in propertyValueMap to see if already present
					for(String key: propertyValueMap.keySet())
					{
						final WeakReference<?> ref = propertyValueMap.get(key);
						final Object val2 = ref.get();
						if (val2 == null)
						{
							// dead
							;
						}
						else if (val2 == val)
						{
							// matches
							System.out.println("Map property value " + val + " to (existing) " + key);
							res = key;
						}
					}
					if (res == null)
					{
						// new key
						final String key = "localinterface:" + val.getClass().getName() + '@'
								+ Integer.toHexString(val.hashCode()) + '@' + scontainerman.id.toString();
						propertyValueMap.put(key, new WeakReference<Object>(val));
						System.out.println("Map property value " + val + " to (new) " + key);
						res = key;
					}
					if (type.isArray())
					{
						Array.set(result, i, res);
					}
					else
					{
						result = res;
					}
				}
				return result;
			}
		}
		if (value instanceof Serializable) { return value; }
		if (value instanceof equip.runtime.ValueBase) { return value; }
		return value;
	}

	/**
	 * map a publishable value to a raw one
	 */
	public static Object mapPropertyValueOnSet(final Object value, final Class<?> cls) throws ClassNotFoundException,
			java.io.IOException
	{
		final Object val = mapPropertyValueOnSetLocal(value, cls);
		return Coerce.toClass(val, cls);
	}

	/**
	 * map a publishable value to a raw one - local refs only
	 */
	protected static Object mapPropertyValueOnSetLocal(Object value, final Class<?> type)
	{
		if (value == null) { return null; }

		if (type != null && (type.isInterface() || (type.isArray() && type.getComponentType().isInterface())))
		{
			// debug
			System.out.println("mapPropertyValueOnSet for interface/array type " + type);
			if (type.isInstance(value))
			{
				// there already
				return value;
			}
			if (type.isArray() && type.getComponentType().isInstance(value))
			{
				// singleton to array
				final Object result = Array.newInstance(type.getComponentType(), 1);
				Array.set(result, 0, value);
				return result;
			}
			if (!type.isArray() && value.getClass().isArray())
			{
				// array to singleton
				if (Array.getLength(value) == 0) { return null; }
				value = Array.get(value, 0);
				if (type.isInstance(value)) { return value; }
			}

			// general case via string (in case it got dictionaried for example)
			String[] keys = new String[0];
			try
			{
				keys = (String[]) Coerce.toClass(value, keys.getClass());
			}
			catch (final Exception e)
			{
				System.err.println("ERROR coercing to String[] for local interface mapping: " + e);
				e.printStackTrace(System.err);
				return null;
			}
			if (keys == null) { return null; }

			synchronized (propertyValueMap)
			{
				Object result = null;
				if (type.isArray())
				{
					result = Array.newInstance(type.getComponentType(), keys.length);
				}

				for (int i = 0; i < keys.length; i++)
				{
					Object res = null;
					final String key = keys[i];

					final WeakReference<Object> ref = propertyValueMap.get(key);
					if (ref == null)
					{
						System.err.println("WARNING: unknown local reference: " + key);
						res = null;
					}
					else
					{
						res = ref.get();
						if (res == null)
						{
							// dead
							System.err.println("WARNING: mapPropertyValueOnSet gave expired ref (" + key + ")");
						}
					}
					System.out.println("Map property key " + key + " to native " + res);

					if (type.isArray())
					{
						Array.set(result, i, res);
					}
					else
					{
						result = res;
					}
				}
				return result;
			}
		}
		return value;
	}

	/**
	 * cache of class name to BeanInfo map
	 */
	protected Map<String, BeanInfo> beanInfoCache = new HashMap<String, BeanInfo>();

	public MappingObject(final Serializable bean, final ContainerManager cm, final GUID capId, final GUID compReqId,
			final GUID compId, final ComponentProperty[] compProps, final MappingObject parent)
	{
		this(bean, cm.dataspace, cm, capId, compReqId, compId, compProps, parent);
	}

	public MappingObject(final Serializable bean, final DataspaceBean dataspace, final ContainerManager cm,
			final GUID capId, final GUID compReqId, final GUID compId, final ComponentProperty[] compProps,
			final MappingObject parent)
	{
		this.capId = capId;
		this.compReqId = compReqId;
		this.compId = compId;
		this.compProps = compProps;
		this.parent = parent;
		containerman = cm;
		if (scontainerman == null)
		{
			scontainerman = cm;
		}
		buildMappingObject(bean, dataspace);
	}

	public void addPropDesc(final equip.data.GUID id, final PropertyDescriptor descriptor)
	{
		this.propDescs.put(id, descriptor);
	}

	public void addSetPropDesc(final equip.data.GUID id, final PropertyDescriptor descriptor)
	{
		List<PropertyDescriptor> list = this.setPropDescs.get(id);
		if (list == null)
		{
			list = new LinkedList<PropertyDescriptor>();
			this.setPropDescs.put(id, list);
		}
		list.add(descriptor);
	}

	public synchronized void buildMappingObject(final Serializable bean, final equip.data.beans.DataspaceBean dataspace)
	{
		final Class<?> beanClass = bean.getClass();
		this.dataspace = dataspace;
		this.bean = bean;
		this.beanid = (compId == null || compId.isNull()) ? dataspace.allocateId() : compId;

		java.beans.Introspector.setBeanInfoSearchPath(new String[] { "." });
		// java.beans.BeanInfo beaninf = null;
		try
		{
			/**
			 * ORIGINAL STATEMENT, changed by Chris Allsopp 14:29 8/09/2004 to allow properties to
			 * be exposed for subcomponent inheritance
			 */
			// beaninf = java.beans.Introspector.getBeanInfo(
			// beanClass, beanClass.getSuperclass());
			beaninf = java.beans.Introspector.getBeanInfo(beanClass, Object.class);
		}
		catch (final IntrospectionException ex1)
		{

			System.err.println("Cannot introspect bean.");
			return;
		}

		final BeanDescriptor beaninformation = beaninf.getBeanDescriptor();

		// ComponentAdvert
		compinst = new ComponentAdvert(beanid);
		compinst.setHostID("Default"); // todo fix me
		compinst.setComponentName(beaninformation.getName());
		compinst.setCapabilityID(capId);
		compinst.setComponentRequestID(compReqId);
		// not properties
		BeanDescriptorHelper.copyInformation(beaninf, compinst, false);

		if (this.containerman == null)
		{
			compinst.setContainerID((GUID) null);
			compinst.setHostID("unknown host");
		}
		else
		{
			compinst.setContainerID(containerman.id);
			compinst.setHostID(containerman.hostID.toString());
		}
		compinst.setComponentID(beanid);

		// Throw it in...
		try
		{
			compinst.addtoDataSpace(dataspace);
		}
		catch (final DataspaceInactiveException ex)
		{
			System.out.println("Dataspace inactive");
			ex.printStackTrace();
		}

		if (bean instanceof InstantiateListener)
		{
			((InstantiateListener) bean).instantiated(compReqId, compinst);
		}

		// Make the propImpls...
		Object propertyValue = null;
		java.beans.PropertyDescriptor[] props = beaninf.getPropertyDescriptors();

		/*
		 * Create a temporary storage for property update events created before the Bean2Equip
		 * component adds itself as listener. This is in order to avoid race conditions of e g a set
		 * property value call which asynchronously creates new dynamic properties.
		 */
		final List<PropertyChangeEvent> dummyPropertyChangeEvents = new ArrayList<PropertyChangeEvent>();
		final PropertyChangeListener dummyPropertyChangeListener = new PropertyChangeListener()
		{

			@Override
			public void propertyChange(final PropertyChangeEvent pce)
			{
				// Just check if source uses DynamicProperties
				// should check if the property itself is dynamic
				// but how?
				if (pce.getSource() instanceof DynamicProperties)
				{
					if (!dummyPropertyChangeEvents.contains(pce))
					{
						// System.out.println("*** got update:"
						// + pce.getSource().getClass().getName());
						dummyPropertyChangeEvents.add(pce);
					}
				}
			}

		};
		Bean2Equip.addPropertyChangeListener(bean, dummyPropertyChangeListener);

		// set initial values?! - not for Persistable components (do sets first,
		// in case of side-effects)
		if (compProps != null && !(bean instanceof Persistable))
		{
			// do config..., configured, other
			for (int stage = 0; stage < 3; stage++)
			{
				for (int i = 0; i < props.length; i++)
				{
					if ((stage == 1 && props[i].getName().equals("configured"))
							|| (stage == 2 && !props[i].getName().startsWith("config"))
							|| (stage == 0 && !props[i].getName().equals("configured") && props[i].getName()
									.startsWith("config")))
					{
						// do at this stage!
						final java.lang.reflect.Method setMethod = props[i].getWriteMethod();
						if (setMethod != null)
						{
							boolean found = false;
							int pi;
							for (pi = 0; pi < compProps.length; pi++)
							{
								if (compProps[pi].getPropertyName().equals(props[i].getName()))
								{
									found = true;
									break;
								}
							}
							if (found)
							{

								Object value = null;
								try
								{
									value = compProps[pi].getPropertyValue();
								}
								catch (final Exception e)
								{

									System.out.println("Error getting property value " + props[i].getName() + ": " + e);
									e.printStackTrace(System.err);
								}
								if (value != null)
								{

									try
									{
										setMethod.invoke(bean, new Object[] { MappingObject
												.mapPropertyValueOnSet(value, props[i].getPropertyType()) });
										System.out.println("Set initial value of " + props[i].getName() + " to "
												+ value);
									}
									catch (final Exception e)
									{
										System.err.println("ERROR setting initial value of " + props[i].getName()
												+ ": " + e);
										e.printStackTrace(System.err);
									}
								}
							}
						}
					}
				}
			}
		}

		// now do dynamic property descriptors (may have been created at last
		// stage)
		java.beans.PropertyDescriptor[] dprops = new java.beans.PropertyDescriptor[0];
		final DynamicProperties dbean = (bean instanceof DynamicProperties) ? (DynamicProperties) bean : null;
		if (dbean != null)
		{
			dprops = dbean.dynGetProperties();
			System.out.println("Bean implements DynamicProperties with " + dprops.length + " initial properties");
		}
		// set initial values?! - not for Persistable components (do sets first,
		// in case of side-effects)
		if (compProps != null && !(bean instanceof Persistable) && dbean != null)
		{
			// do config..., configured, other
			for (int stage = 0; stage < 3; stage++)
			{
				for (int i = 0; i < dprops.length; i++)
				{
					if ((stage == 1 && dprops[i].getName().equals("configured"))
							|| (stage == 2 && !dprops[i].getName().startsWith("config"))
							|| (stage == 0 && !dprops[i].getName().equals("configured") && dprops[i].getName()
									.startsWith("config")))
					{
						final java.lang.reflect.Method setMethod = dprops[i].getWriteMethod();
						if (setMethod != null)
						{
							boolean found = false;
							int pi;
							for (pi = 0; pi < compProps.length; pi++)
							{
								if (compProps[pi].getPropertyName().equals(dprops[i].getName()))
								{
									found = true;
									break;
								}
							}
							if (found)
							{

								Object value = null;
								try
								{
									value = compProps[pi].getPropertyValue();
								}
								catch (final Exception e)
								{

									System.out
											.println("Error getting property value " + dprops[i].getName() + ": " + e);
									e.printStackTrace(System.err);
								}
								if (value != null)
								{

									try
									{
										final DynamicPropertyDescriptor dprop = (DynamicPropertyDescriptor) dprops[i];
										// invoke
										dprop.writeProperty(dbean, MappingObject.mapPropertyValueOnSet(value, dprops[i]
												.getPropertyType()));
										System.out.println("Set initial value of " + dprops[i].getName() + " to "
												+ (value instanceof StringBox ? ((StringBox) value).value : value));
									}
									catch (final Exception e)
									{
										System.err.println("ERROR setting initial value of " + dprops[i].getName()
												+ ": " + e);
										e.printStackTrace(System.err);
									}
								}
							}
						}
					}
				}
			}
		}
		// concat dprops with props
		final java.beans.PropertyDescriptor p2[] = new java.beans.PropertyDescriptor[props.length + dprops.length];
		System.arraycopy(props, 0, p2, 0, props.length);
		System.arraycopy(dprops, 0, p2, props.length, dprops.length);
		props = p2;

		for (final PropertyDescriptor prop : props)
		{
			final DynamicPropertyDescriptor dprop = (prop instanceof DynamicPropertyDescriptor) ? (DynamicPropertyDescriptor) prop
					: null;

			// get current value and make property item in equip
			final java.lang.reflect.Method method = prop.getReadMethod();
			if (method == null)
			{
				continue;
			}
			// get current value and make property item in equip
			final java.lang.reflect.Method writemethod = prop.getWriteMethod();
			final boolean readonly = writemethod == null;

			try
			{
				final Object rawValue = dprop != null ? dprop.readProperty(dbean) : method
						.invoke(bean, (Object[]) null);
				propertyValue = mapPropertyValueOnGet(rawValue, prop.getPropertyType());
			}
			catch (final Exception ex2)
			{
				System.err.println("error getting the property value: " + ex2);
				ex2.printStackTrace(System.err);
				continue;
			}

			final Class<?> cls = prop.getPropertyType();
			final String propertyname = prop.getName();

			// persistentChild
			if (propertyname.equals(PERSISTENT_CHILD))
			{
				try
				{
					persistentChild = (String) Coerce.toClass(propertyValue, String.class);
				}
				catch (final Exception e)
				{
					System.err.println("ERROR getting persistent child property as string: " + e);
					e.printStackTrace(System.err);
				}
			}

			// what if it is an array of sub-components??
			propertyValue = checkForSubComponents(propertyname, propertyValue, cls);

			// Set up the ComponentProperty
			ComponentProperty propitem = null;
			if (compProps != null)
			{
				propitem = findComponentProperty(propertyname, cls.getName());
			}

			if (propitem == null)
			{
				propitem = new ComponentProperty(dataspace.allocateId());
			}
			BeanDescriptorHelper.copyInformation(prop, propitem);

			if (readonly)
			{
				propitem.setReadonly(true);
			}
			propitem.setComponentID(beanid);
			propitem.setPropertyClass(cls);
			propitem.setPropertyName(propertyname);
			try
			{
				propitem.setPropertyValue(propertyValue);
			}
			catch (final Exception ex3)
			{
				System.out.println("Problem Serialising Value");
				ex3.printStackTrace();
			}

			// Put it in the space
			try
			{
				// dataspace.add(propertyitem);
				propitem.addtoDataSpace(dataspace);
			}
			catch (final DataspaceInactiveException ex)
			{
				System.out.println("Dataspace inacative");
				ex.printStackTrace();
			}

			final GUID thispropertyID = propitem.getID();

			// Update the various Hashes tables
			// this.propIDs.put(propertyname, thispropertyID);
			this.propImpls.put(propertyname, propitem);
			// this.propVals.put(propertyitem.id, propertyValue);
			this.addPropDesc(thispropertyID, prop);
			// end of moved code

		}

		// parent pseudo-property (not seen by component)
		if (parent != null)
		{
			final String propertyname = PARENT;
			final Class<String> cls = String.class;
			// Set up the ComponentProperty
			ComponentProperty propitem = null;
			if (compProps != null)
			{
				propitem = findComponentProperty(propertyname, cls.getName());
			}

			if (propitem == null)
			{
				propitem = new ComponentProperty(dataspace.allocateId());
			}
			// extra info??!
			BeanDescriptorHelper.parentProperty(propitem);

			propitem.setReadonly(true);
			propitem.setComponentID(beanid);
			propitem.setPropertyClass(cls);
			propitem.setPropertyName(propertyname);
			try
			{
				propitem.setPropertyValue(parent.getBeanID().toString());
			}
			catch (final Exception ex3)
			{
				System.out.println("Problem Serialising parent beanid Value");
				ex3.printStackTrace();
			}

			// Put it in the space
			try
			{
				// dataspace.add(propertyitem);
				propitem.addtoDataSpace(dataspace);
			}
			catch (final DataspaceInactiveException ex)
			{
				System.out.println("Dataspace inacative");
				ex.printStackTrace();
			}

			// Update the various Hashes tables
			// this.propIDs.put(propertyname, thispropertyID);
			this.propImpls.put(propertyname, propitem);

			if (persistentChild == null)
			{
				System.err.println("ERROR: child component " + beanid
						+ " has no persistentChild property! - persistence will not work");
			}
		}

		// update startup data map
		{
			final ComponentProperty cprops[] = (ComponentProperty[]) this.propImpls.values()
					.toArray(new ComponentProperty[this.propImpls.values().size()]);
			containerman.updateStartupData(	compReqId == null ? beanid : compReqId, beaninformation.getName(), beanid,
											cprops);
		}

		/* remove dummy listeners as Bean2Equip should take over */
		Bean2Equip.removePropertyChangeListener(bean, dummyPropertyChangeListener);

		// create The event mappers to and from Equip
		// Note significant state info shared between these
		toEquip = new Bean2Equip(this, dataspace);
		fromEquip = new Equip2Bean(this, dataspace);

		/* report back missing update events to Bean2Equip */
		for(PropertyChangeEvent pce: dummyPropertyChangeEvents)
		{
			toEquip.propertyChange(pce);
			// System.out.println("*** Populating Bean2Equip with property
			// change event: " + pce.getPropertyName() + "=" +
			// pce.getNewValue());;
		}
		// System.out.println("*** Finished creating mapping object: " +
		// this.getBean());
	}

	public Serializable getBean()
	{
		return this.bean;
	}

	public equip.data.GUID getBeanID()
	{
		return this.beanid;
	}

	public ContainerManager getContainerManager()
	{
		return containerman;
	}

	public Equip2Bean getFromEquip()
	{
		return fromEquip;
	}

	public MappingObject getParent()
	{
		return parent;
	}

	public String getPersistentChild()
	{
		return persistentChild;
	}

	/*
	 * public HashMap getPropVals() { return propVals; }
	 */
	public java.beans.PropertyDescriptor getPropDesc(final equip.data.GUID id)
	{
		if (!(propDescs.containsKey(id))) { return null; }
		return (java.beans.PropertyDescriptor) (propDescs.get(id));
	}

	public ComponentProperty getPropImpl(final String propname)
	{
		if (!(propImpls.containsKey(propname))) { return null; }
		return (ComponentProperty) (propImpls.get(propname));
	}

	public Iterator<PropertyDescriptor> getSetPropDesc(final equip.data.GUID id)
	{
		final List<PropertyDescriptor> list = this.setPropDescs.get(id);
		if (list == null) { return null; }
		return list.listIterator();
	}

	public Bean2Equip getToEquip()
	{
		return toEquip;
	}

	public void removeSetPropDesc(final equip.data.GUID id, final PropertyDescriptor descriptor)
	{
		final List<PropertyDescriptor> list = this.setPropDescs.get(id);
		if (list == null)
		{
			System.err.println("Unknown property " + id + " in removeSetPropDesc");
			return;
		}
		if (!list.contains(descriptor))
		{
			System.err.println("Unknown PropertyDescriptor " + descriptor + " in removeSetPropDesc");
			return;
		}
		list.remove(descriptor);
	}

	public void setContainerManager(final ContainerManager containerman)
	{
		this.containerman = containerman;
	}

	/**
	 * stop mapping for ever
	 */
	public synchronized void stop()
	{
		System.out.println("Stop MappingObject for bean " + this.bean);
		toEquip.stop();
		fromEquip.stop();
		// withdraw advert
		try
		{
			System.out.println("Remove component advert " + compinst.getID());
			dataspace.delete(compinst.getID());
		}
		catch (final DataspaceInactiveException ex)
		{
			System.out.println("Dataspace inactive");
			ex.printStackTrace();
		}
		// remove properties (name -> ComponentProperty)
		for(final ComponentProperty prop: propImpls.values())
		{
			checkForSubComponents(prop.getPropertyName(), null, null);

			try
			{
				System.out.println("Remove component property " + prop.getID());
				dataspace.delete(prop.getID());
			}
			catch (final DataspaceInactiveException e)
			{
				System.err.println("Dataspace inactive");
			}
		}
		// stop bean
		// java.beans.MethodDescriptor[] methods =
		// beaninf.getMethodDescriptors();
		// int i;
		// for (i = 0; i < methods.length; i++) {
		java.lang.reflect.Method method = null;
		try
		{
			method = bean.getClass().getMethod("stop", (Class[]) null);
		}
		catch (final NoSuchMethodException e)
		{
		}
		if (method == null)
		{
			try
			{
				method = bean.getClass().getMethod("Stop", (Class[]) null);
			}
			catch (final NoSuchMethodException e)
			{
			}
		}
		if (method != null)
		{
			System.out.println("Calling " + method.getName() + " method...");
			try
			{
				method.invoke(bean, (Object[]) null);
			}
			catch (final Exception e)
			{
				System.err.println("ERROR calling " + method.getName() + " on bean " + bean + ": " + e);
				e.printStackTrace(System.err);
			}
		}
		else
		{
			System.out.println("NOTE: no stop method on bean " + bean);
		}

		bean = null;
		beaninf = null;
	}

	/**
	 * called by Bean2Equip on possible new field - doesn't currently check for initial value in
	 * persistence (partly because it probably won't still be there because of the way it is
	 * currently updated)
	 */
	void checkForNewDynamicProperty(final String propertyname, final Object newValue)
	{
		if (!(bean instanceof DynamicProperties)) { return; }

		final java.beans.PropertyDescriptor[] props = beaninf.getPropertyDescriptors();
		for (int i = 0; props != null && i < props.length; i++)
		{
			if (props[i].getName().equals(propertyname))
			{
				// ordinary property
				return;
			}
		}

		final DynamicProperties dbean = (DynamicProperties) bean;
		final DynamicPropertyDescriptor dds[] = dbean.dynGetProperties();
		DynamicPropertyDescriptor dprop = null;
		for (int i = 0; i < dds.length && dprop == null; i++)
		{
			if (dds[i].getName().equals(propertyname))
			{
				dprop = dds[i];
			}
		}
		if (dprop == null) { return; }
		System.out.println("Discovered new dynamic property \'" + dprop.getName() + "\' on change event");

		// from MappingObject
		Object propertyValue = null;
		try
		{
			// UPDATE TUPLE and linked objects
			propertyValue = mapPropertyValueOnGet(newValue, dprop.getPropertyType());
		}
		catch (final Exception ex2)
		{
			System.err.println("error getting the property value: " + ex2);
			ex2.printStackTrace(System.err);
			return;
		}

		final Class<?> cls = dprop.getPropertyType();

		// what if it is an array of sub-components??
		propertyValue = this.checkForSubComponents(propertyname, propertyValue, cls);

		// Set up the ComponentProperty
		ComponentProperty propitem = null;
		propitem = new ComponentProperty(dataspace.allocateId());
		BeanDescriptorHelper.copyInformation(dprop, propitem);

		propitem.setComponentID(beanid);
		propitem.setPropertyClass(cls);
		propitem.setPropertyName(propertyname);
		propitem.setAttribute("dynamic", new BooleanBoxImpl(true));
		try
		{
			propitem.setPropertyValue(propertyValue);
		}
		catch (final Exception ex3)
		{
			System.out.println("Problem Serialising Value");
			ex3.printStackTrace();
		}

		// Put it in the space
		try
		{
			// dataspace.add(propertyitem);
			propitem.addtoDataSpace(dataspace);
		}
		catch (final DataspaceInactiveException ex)
		{
			System.out.println("Dataspace inacative");
			ex.printStackTrace();
		}

		final GUID thispropertyID = propitem.getID();

		// Update the various Hashes tables
		// this.propIDs.put(propertyname, thispropertyID);
		this.propImpls.put(propertyname, propitem);
		// this.propVals.put(propertyitem.id, propertyValue);
		this.addPropDesc(thispropertyID, dprop);

		// update startup data map
		{
			final ComponentProperty cprops[] = (ComponentProperty[]) this.propImpls.values()
					.toArray(new ComponentProperty[this.propImpls.values().size()]);
			final BeanDescriptor beaninformation = beaninf.getBeanDescriptor();
			containerman.updateStartupData(	compReqId == null ? beanid : compReqId, beaninformation.getName(), beanid,
											cprops);
		}
	}

	/**
	 * called by Bean2Equip when property becomes null
	 */
	boolean checkForRemovedDynamicProperty(final String propertyname)
	{
		if (!(bean instanceof DynamicProperties)) { return false; }

		final java.beans.PropertyDescriptor[] props = beaninf.getPropertyDescriptors();
		for (int i = 0; props != null && i < props.length; i++)
		{
			if (props[i].getName().equals(propertyname))
			{
				// ordinary property
				return false;
			}
		}

		final DynamicProperties dbean = (DynamicProperties) bean;
		final DynamicPropertyDescriptor dds[] = dbean.dynGetProperties();
		DynamicPropertyDescriptor dprop = null;
		for (int i = 0; i < dds.length && dprop == null; i++)
		{
			if (dds[i].getName().equals(propertyname))
			{
				dprop = dds[i];
			}
		}
		if (dprop != null)
		{
			// still here
			return false;
		}

		System.out.println("Removing dynamic property \"" + propertyname + "\" on change event");

		final ComponentProperty prop = this.getPropImpl(propertyname);

		checkForSubComponents(prop.getPropertyName(), null, null);

		try
		{
			System.out.println("Remove component property " + prop.getID());
			dataspace.delete(prop.getID());
		}
		catch (final DataspaceInactiveException e)
		{
			System.err.println("Dataspace inactive");
		}
		// Update the various Hashes tables
		this.propImpls.remove(propertyname);
		// this.propVals.put(propertyitem.id, propertyValue);
		this.propDescs.remove(prop.getID());

		// should really be left in persistence stuff in case it comes back - oh
		// well
		return true;
	}

	/**
	 * check if value is an array of subcomponents; if so recursively export; unexport any elements
	 * no longer present.
	 */
	protected synchronized Object checkForSubComponents(final String propName, final Object propValue,
			final Class<?> firstTimeClass)
	{
		List<Serializable> exported = null;
		if (firstTimeClass != null)
		{
			if (firstTimeClass.isArray())
			{
				final Class<?> compClass = firstTimeClass.getComponentType();
				// hack heuristic: array, not primitive, implements
				// Serializable, but not java... (e.g. java.lang.String)
				if (!compClass.isPrimitive() && Serializable.class.isAssignableFrom(compClass)
						&& compClass.getName().indexOf("java.") == -1)
				{
					beaninf = getBeanInfo(compClass);
					if (beaninf != null)
					{
						// should probably be more constraints here!!
						exported = new ArrayList<Serializable>();
						childProperties.put(propName, exported);

					}
				}
			}
		}
		else
		{
			exported = childProperties.get(propName);
		}

		if (exported == null) { return propValue; }

		final List<Serializable> toExport = new ArrayList<Serializable>();

		int i, len;
		if (propValue != null)
		{
			len = java.lang.reflect.Array.getLength(propValue);
			for (i = 0; i < len; i++)
			{
				final Serializable el = (Serializable) java.lang.reflect.Array.get(propValue, i);
				toExport.add(el);
			}
		}

		// new exports?
		int j;
		for (i = 0; i < toExport.size(); i++)
		{
			final Serializable sub = toExport.get(i);
			// already exported?
			boolean done = false;
			for (j = 0; j < exported.size() && !done; j++)
			{
				final Serializable subj = exported.get(j);
				if (subj == sub)
				{
					done = true;
				}
			}
			if (!done)
			{
				exportSubComponent(sub);
				exported.add(sub);
			}
		}
		// unexports?
		for (j = 0; j < exported.size(); j++)
		{
			final Serializable subj = exported.get(j);
			// still there?
			boolean present = false;
			for (i = 0; i < toExport.size() && !present; i++)
			{
				final Serializable sub = toExport.get(i);
				if (sub == subj)
				{
					present = true;
				}
			}
			if (!present)
			{
				unexportSubComponent(subj);
				exported.remove(j);
				j--;
			}
		}
		// map value - to string for now (bad!)
		final StringBuffer mapval = new StringBuffer();
		for (i = 0; i < toExport.size(); i++)
		{
			mapval.append(containerman.getComponentExporter().getBeanID(toExport.get(i))
					.toString());
			mapval.append(" ");
		}
		return mapval;
	}

	protected void exportSubComponent(final Serializable sub)
	{
		System.out.println("Export sub-component " + sub);
		// correct ids for subcomponents?
		// persistentChild property??
		ComponentStartupData data = null;
		try
		{
			final java.lang.reflect.Method getter = sub.getClass().getMethod(GET_PERSISTENT_CHILD, (Class[]) null);
			final String persistentChild = (String) getter.invoke(sub, (Object[]) null);
			// System.out.println("- persistentChild="+persistentChild);
			if (persistentChild != null)
			{
				data = containerman.getStartupData(getBeanID(), persistentChild);
			}
		}
		catch (final NoSuchMethodException e)
		{
			// ignore
		}
		catch (final Exception e)
		{
			System.err.println("ERROR trying to get persistentChild value from component " + sub + ": " + e);
			e.printStackTrace(System.err);
		}
		if (data != null)
		{
			// System.out.println("Export with startup data");
			containerman.getComponentExporter().export(sub, /* cap */null, /* compreq */
			null, data.getComponentGUID(), data.getProperties(), this);
		}
		else
		{
			containerman.getComponentExporter().export(sub, null, null, null, null, this);
		}
	}

	/**
	 * get bean info for class name
	 */
	protected java.beans.BeanInfo getBeanInfo(final Class<?> clazz)
	{
		final String classname = clazz.getName();
		if (beanInfoCache.containsKey(classname)) { return (java.beans.BeanInfo) beanInfoCache.get(classname); }
		java.beans.BeanInfo beaninf = null;
		try
		{
			beaninf = java.beans.Introspector.getBeanInfo(clazz, clazz.getSuperclass());
			beanInfoCache.put(classname, beaninf);
		}
		catch (final IntrospectionException ex1)
		{
			// Hashtable allows nulls
			beanInfoCache.put(classname, null);
			System.err.println("Cannot introspect class " + classname);
		}
		return beaninf;
	}

	protected void unexportSubComponent(final Serializable sub)
	{
		System.out.println("Unexport sub-component " + sub);
		containerman.getComponentExporter().unexport(sub);
	}

	private ComponentProperty findComponentProperty(final String name, final String className)
	{
		if (name != null && className != null)
		{
			for (final ComponentProperty compProp : compProps)
			{
				if (className.equals(compProp.getPropertyClass()) && name.equals(compProp.getPropertyName())) { return compProp; }
			}
		}
		return null;
	}
}
