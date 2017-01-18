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
 James Mathrick (University of Nottingham)
 */
package equip.ect;

import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import equip.data.beans.DataspaceInactiveException;

class Bean2Equip implements PropertyChangeListener
{
	public static void addPropertyChangeListener(final Serializable bean, final PropertyChangeListener listener)
	{
		// Add bean property listener...
		try
		{
			final java.beans.EventSetDescriptor[] foo = java.beans.Introspector.getBeanInfo(bean.getClass())
					.getEventSetDescriptors();
			for (EventSetDescriptor aFoo : foo)
			{
				if (!(aFoo.getListenerType().equals(PropertyChangeListener.class)))
				{
					continue;
				}
				// System.out.println("*** Bean2Equip: Adding property listener");
				aFoo.getAddListenerMethod().invoke(bean, listener);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void removePropertyChangeListener(final Serializable bean, final PropertyChangeListener listener)
	{
		// Add bean property listener...
		try
		{
			final java.beans.EventSetDescriptor[] foo = java.beans.Introspector.getBeanInfo(bean.getClass())
					.getEventSetDescriptors();
			for (EventSetDescriptor aFoo : foo)
			{
				if (aFoo.getListenerType().equals(PropertyChangeListener.class))
				{
					aFoo.getRemoveListenerMethod().invoke(bean, listener);
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	// Mapping variables
	// private HashMap propVals = null;
	private equip.data.beans.DataspaceBean dataspace = null;
	private Serializable bean = null;
	private MappingObject parent = null;

	//private HashMap links = new HashMap();

	Bean2Equip(final MappingObject parent, final equip.data.beans.DataspaceBean dataspace)
	{
		this.dataspace = dataspace;
		this.bean = parent.getBean();
		this.parent = parent;
		// this.propVals = this.parent.getPropVals();
		addPropertyChangeListener(bean, this);

	}

	public Serializable getBean()
	{
		return this.bean;
	}

	public equip.data.beans.DataspaceBean getDataspace()
	{
		return this.dataspace;
	}

	/**
	 * callback on bean property change
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt)
	{
		synchronized (parent)
		{
			// equip.data.GUID propID =
			// this.parent.getPropImplID(evt.getPropertyName());
			// Get the Property
			ComponentProperty prop = this.parent.getPropImpl(evt.getPropertyName());
			if (prop == null)
			{
				// could be a new dynamic property has come into existence
				if (bean instanceof DynamicProperties)
				{
					// System.out.println("*** Checking dyn prop: " + evt.getPropertyName());
					this.parent.checkForNewDynamicProperty(evt.getPropertyName(), evt.getNewValue());
				}
				return;
			}
			if (evt.getNewValue() == null && bean instanceof DynamicProperties)
			{
				if (this.parent.checkForRemovedDynamicProperty(evt.getPropertyName()))
				{
					// removed
					return;
				}
			}
			final java.beans.PropertyDescriptor propdesc = this.parent.getPropDesc(prop.getID());

			// clone tuple (which has been given to DS)
			prop = prop.cloneAsComponentProperty();

			// UPDATE TUPLE and linked objects
			Class<?> type = null;
			if (propdesc != null)
			{
				type = propdesc.getPropertyType();
			}
			else
			{
				System.err.println("WARNING: could not find property descriptor for changing property "
						+ evt.getPropertyName());
				if (evt.getNewValue() != null)
				{
					type = evt.getNewValue().getClass();
				}
			}

			Object newvalue = MappingObject.mapPropertyValueOnGet(evt.getNewValue(), type);

			// what if it is an array of sub-components??
			newvalue = this.parent.checkForSubComponents(evt.getPropertyName(), newvalue, null);

			try
			{
				prop.setPropertyValue(newvalue);
			}
			catch (final Exception ex1)
			{
				System.out.println("Error setting property value for " + prop.getPropertyName());
				ex1.printStackTrace();
				return;
			}

			// Update in Equip
			try
			{
				// it is important that this update is async to avoid
				// race/deadlock/livelock
				prop.updateinDataSpace(dataspace);
			}
			catch (final DataspaceInactiveException ex2)
			{
				System.out.println("DataSpace inactive when updating  " + prop.getPropertyName());
				ex2.printStackTrace();
				return;

			}
			// update startup data
			this.parent.getContainerManager().updateStartupDataValue(prop.getComponentID(), prop.getID(),
					newvalue == null ? "" : newvalue.toString());
		}
	}

	public void stop()
	{
		// remove bean property listener...
		removePropertyChangeListener(bean, this);
	}
}
