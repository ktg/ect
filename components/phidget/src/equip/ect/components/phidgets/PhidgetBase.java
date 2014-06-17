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

Created by: 
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Dagmar Kern (University of Sussex)
  Mark Stringer (University of Sussex)
  Jan Humble (University of Nottingham)
  Tom Hart (University of Nottingham)
  Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.components.phidgets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.ErrorEvent;
import com.phidgets.event.ErrorListener;

import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertiesSupport;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.NoSuchPropertyException;

/**
 * common base class for phidget device components.
 */
public abstract class PhidgetBase implements Serializable, DynamicProperties, AttachListener, DetachListener,
		ErrorListener
{
	static final int CONNECTION_DELAY = 200;
	static final int OPERATION_DELAY = 50;

	protected Phidget absphid;

	/**
	 * ordinary property change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * dynamic properties support
	 */
	protected DynamicPropertiesSupport dynsup;
	boolean previouslyAttached = false;
	protected String attention = "";

	protected int configSerialNumber = -1;

	protected boolean connected;

	protected boolean configured;

	protected long serialNumber;

	public PhidgetBase()
	{
		dynsup = new DynamicPropertiesSupport(propertyChangeListeners);
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	@Override
	public void attached(final AttachEvent ae)
	{
		try
		{
			setSerialNumber(ae.getSource().getSerialNumber());
			setConnected(true);

			if (!previouslyAttached)
			{
				firstAttachment();
				previouslyAttached = true;
			}
			else
			{
				subsequentAttachment();
			}
		}
		catch (final PhidgetException e)
		{
			setConnected(false);
		}
	}

	@Override
	public void detached(final DetachEvent ae)
	{
		setConnected(false);
		detachment();
	}

	/**
	 * get all properties' {@link DynamicPropertyDescriptors}
	 */
	@Override
	public DynamicPropertyDescriptor[] dynGetProperties()
	{
		return dynsup.dynGetProperties();
	}

	/**
	 * get one property by name
	 */
	@Override
	public Object dynGetProperty(final String name) throws NoSuchPropertyException
	{
		return dynsup.dynGetProperty(name);
	}

	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		dynsup.dynSetProperty(name, value);
	}

	@Override
	public void error(final ErrorEvent ee)
	{
		// do nothing at present
	}

	public String getAttention()
	{
		return attention;
	}

	public synchronized int getConfigSerialNumber()
	{
		return configSerialNumber;
	}

	public synchronized boolean getConfigured()
	{
		return configured;
	}

	public synchronized boolean getConnected()
	{
		return connected;
	}

	public synchronized long getSerialNumber()
	{
		return serialNumber;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setAttention(final String newValue)
	{
		final String oldValue = this.attention;
		this.attention = newValue;

		propertyChangeListeners.firePropertyChange("attention", oldValue, newValue);
	}

	public synchronized void setConfigSerialNumber(final int newValue)
	{
		final int oldValue = this.configSerialNumber;
		this.configSerialNumber = newValue;

		propertyChangeListeners.firePropertyChange("configSerialNumber", oldValue, newValue);
	}

	public synchronized void setConfigured(final boolean newValue)
	{

		if (!newValue) { return; }

		if (newValue == configured) { return; }

		propertyChangeListeners.firePropertyChange("configured", false, true);

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				startConnection();
			}
		}).start();

	}

	public void stop()
	{
		System.out.println("stop called");

		absphid.addAttachListener(this);
		absphid.addErrorListener(this);
		absphid.addDetachListener(this);

		try
		{
			absphid.close();
		}
		catch (final PhidgetException e)
		{
			e.printStackTrace();
		}
	}

	protected abstract void detachment();

	protected abstract void firstAttachment();

	protected synchronized void setConnected(final boolean newValue)
	{
		final boolean oldValue = this.connected;
		this.connected = newValue;

		propertyChangeListeners.firePropertyChange("connected", oldValue, newValue);
	}

	protected synchronized void setSerialNumber(final long newValue)
	{
		final long oldValue = this.serialNumber;
		this.serialNumber = newValue;

		propertyChangeListeners.firePropertyChange("serialNumber", new Long(oldValue), new Long(newValue));
	}

	protected void startConnection()
	{
		// try and establish a connection with a phidget

		absphid.addAttachListener(this);
		absphid.addErrorListener(this);
		absphid.addDetachListener(this);

		try
		{

			if (configSerialNumber > -1)
			{
				absphid.open(configSerialNumber);
			}
			else
			{
				absphid.openAny();
			}
		}
		catch (final PhidgetException e)
		{
			setAttention("No phidget found");

			configured = false;
			propertyChangeListeners.firePropertyChange("configured", true, false);

			setConnected(false);
		}
	}

	protected abstract void subsequentAttachment();

	protected void waitForABit(final long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (final InterruptedException e)
		{
		}
	}
}
