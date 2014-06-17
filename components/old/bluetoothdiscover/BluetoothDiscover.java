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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

 */
package equip.ect.components.bluetoothdiscover;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Vector;

import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertiesSupport;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.NoSuchPropertyException;

/**
 * Attempts to discover nearby bluetooth device addresses using any local radio(s). <h3>Description</h3>
 * This component uses Windows BluetoothAPI to provide a list of bluetooth devices in range. It can
 * only detect unauthenticated bluetooth devices with discovery enabled, and devices that have
 * already been authenticated with the machine that it is running on. Note that bluetooth discovery
 * and connection timeouts are both relatively slow (of the order of 5-10 seconds). <h3>Installation
 * </h3> Requires a Bluetooth radio on the machine that it runs on. May require WindowsXP and
 * service pack 2 (SP2) to run. Probably requires WindowsXP and SP2 and Microsoft Platform SDK to
 * recompile the native DLL. <h3>Configuration</h3>
 * <ul>
 * <li>set the configPollinterval property to adjust the target poll rate (e.g. in range 20-120
 * seconds); the bluetooth discovery time is set to approximately half of the configPollinterval; at
 * short timeout discovery will find random subsets of the devices in range on each poll.</li>
 * <li>set the configured property to <code>true</code> to cause polling to occur.</li>
 * <li>you can also, optionally set the configWatchfor property to a comma-separated list of
 * bluetooth addresses of interest. The component will create a boolean dynamic property for each
 * such address (property name is address with ':' mapped to '_').</li>
 * </ul>
 * <h3>Usage</h3> The status property will give some indication of status and (low-level) failure,
 * e.g. lack of bluetooth device, and with each attempted poll, the pollcount property will
 * increment to show that is doing something. If the poll was successful, the devices property and
 * any dynamic properties created through configWatchfor will update. The devices property is a
 * comma-separated list of addresses of bluetooth devices discovered or connectable in the last
 * poll, in the form <code>12:34:56:78:9a:bc</code>. The dynamic properties will be set to true if
 * that address was dicovered.
 * 
 * @author Chris Greenhalgh
 * @classification Hardware/Input
 * @technology Bluetooth
 */
public class BluetoothDiscover implements Serializable, DynamicProperties, Runnable
{
	/**
	 * dynamic properties support
	 */
	protected DynamicPropertiesSupport dynsup;
	protected boolean stopped = false;
	/**
	 * input value
	 */
	protected String watchfor;
	/**
	 * input value
	 */
	protected boolean active;
	/**
	 * input value
	 */
	protected int pollinterval = 30;
	/**
	 * output value
	 */
	protected String status;
	/**
	 * output value
	 */
	protected int pollcount;
	/**
	 * output value
	 */
	protected String[] devices = new String[0];
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	static
	{
		System.loadLibrary("bluetoothdiscoverjni1");
	}

	/**
	 * no-args constructor (required)
	 */
	public BluetoothDiscover()
	{
		status = "new";
		dynsup = new DynamicPropertiesSupport(propertyChangeListeners);
		// setInput("a");
		// poll thread
		new Thread(this).start();
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * get all properties' {@link DynamicPropertyDescriptor}
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

	/**
	 * get one property by name
	 */
	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		dynsup.dynSetProperty(name, value);
	}

	/**
	 * input getter
	 */
	public synchronized int getConfigPollinterval()
	{
		return pollinterval;
	}

	/**
	 * input getter
	 */
	public synchronized boolean getConfigured()
	{
		return active;
	}

	/**
	 * input getter
	 */
	public synchronized String getConfigWatchfor()
	{
		return watchfor;
	}

	/**
	 * output getter
	 */
	public synchronized String[] getDevices()
	{
		return devices;
	}

	/**
	 * output getter
	 */
	public synchronized int getPollcount()
	{
		return pollcount;
	}

	/**
	 * output getter
	 */
	public synchronized String getStatus()
	{
		return status;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * thread to poll
	 */
	@Override
	public void run()
	{
		try
		{
			long lastPollTime = System.currentTimeMillis();
			while (!stopped)
			{
				synchronized (this)
				{
					while (!stopped)
					{
						final long now = System.currentTimeMillis();
						if (now < lastPollTime + pollinterval * 1000)
						{
							this.wait(lastPollTime + pollinterval * 1000 - now);
						}
						else
						{
							break;
						}
					}
				}
				// time to poll
				lastPollTime = System.currentTimeMillis();
				if (active && !stopped)
				{
					int timeout = (int) (pollinterval / (2 * 1.28));
					if (timeout < 1)
					{
						timeout = 1;
					}
					String newDevices = pollBluetooth(true, timeout, true);
					if (stopped)
					{
						break;
					}
					if (newDevices == null)
					{
						setStatus("poll failed");
						newDevices = "";
					}
					else
					{
						setStatus("poll ok: " + newDevices);
					}

					updateDevices(newDevices.toLowerCase());
				}
				else if (devices != null && !devices.equals(""))
				{
					updateDevices("");
				}
				// always leave a bit of a gap (1s)
				final long now = System.currentTimeMillis();
				if (now + 1000 < lastPollTime + pollinterval * 1000)
				{
					lastPollTime = now + 1000 - pollinterval * 1000;
				}
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR in bluetooth poll thread: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * input setter
	 */
	public synchronized void setConfigPollinterval(final int in)
	{
		if (pollinterval < 1)
		{
			pollinterval = 1;
		}
		// could suppress no-change setting
		if (in == this.pollinterval) { return; }
		final Integer oldInput = new Integer(this.pollinterval);
		this.pollinterval = in;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("configPollinterval", oldInput, new Integer(this.pollinterval));
		// behaviour... (doesn't have to be done like this)
		this.notify();
	}

	/**
	 * input setter
	 */
	public synchronized void setConfigured(final boolean in)
	{
		// could suppress no-change setting
		if (in == this.active) { return; }
		final Boolean oldInput = new Boolean(active);
		this.active = in;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("configured", oldInput, new Boolean(this.active));
		// behaviour... (doesn't have to be done like this)
		setStatus(in ? "activated" : "deactivated");
		this.notify();
	}

	/**
	 * input setter
	 */
	public synchronized void setConfigWatchfor(String in)
	{
		if (in != null)
		{
			in = in.toLowerCase();
		}
		// could suppress no-change setting
		if (in == this.watchfor || (in != null && this.watchfor != null && in.equals(this.watchfor))) { return; }
		final String oldInput = this.watchfor;
		this.watchfor = in;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("configWatchfor", oldInput, this.watchfor);
		// behaviour... (doesn't have to be done like this)
		updateOutputs();
		// ....
	}

	/**
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
		stopped = true;
		this.notify();
	}

	/**
	 * native method!
	 */
	protected native String pollBluetooth(boolean discover, int delay, boolean probeAuthenticated);

	/**
	 * output internal setter - NB protected, so that it cannot be called by the framework
	 */
	protected synchronized void setDevices(final String[] output)
	{
		// could suppress no-change setting
		if (output == this.devices || (output != null && this.devices != null && output.equals(this.devices))) { return; }
		final String[] oldOutput = this.devices;
		this.devices = output;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("devices", oldOutput, this.devices);
	}

	/**
	 * output internal setter - NB protected, so that it cannot be called by the framework
	 */
	protected synchronized void setPollcount(final int output)
	{
		// could suppress no-change setting
		if (output == this.pollcount) { return; }
		final Integer oldOutput = new Integer(this.pollcount);
		this.pollcount = output;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("pollcount", oldOutput, new Integer(this.pollcount));
	}

	/**
	 * output internal setter - NB protected, so that it cannot be called by the framework
	 */
	protected synchronized void setStatus(final String output)
	{
		// could suppress no-change setting
		if (output == this.status || (output != null && this.status != null && output.equals(this.status))) { return; }
		final String oldOutput = this.status;
		this.status = output;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("status", oldOutput, this.status);
	}

	/**
	 * update devices visible
	 */
	protected synchronized void updateDevices(final String newDevices)
	{
		setPollcount(pollcount + 1);
		// setDevices(newDevices);
		// dynamic properties??
		final Vector dv = new Vector();
		// present?
		final StringTokenizer dtk = new StringTokenizer(newDevices, ",");
		while (dtk.hasMoreTokens())
		{
			final String tok = dtk.nextToken().trim();
			dv.addElement(tok);
		}
		setDevices((String[]) dv.toArray(new String[dv.size()]));

		final DynamicPropertyDescriptor props[] = dynsup.dynGetProperties();
		for (final DynamicPropertyDescriptor prop : props)
		{
			// present?
			final StringTokenizer toks = new StringTokenizer(newDevices, ",");
			boolean found = false;
			while (!found && toks.hasMoreTokens())
			{
				final String tok = toks.nextToken().trim().replace(':', '_');
				// new?
				if (prop.getName().equals(tok))
				{
					found = true;
				}
			}
			try
			{
				dynsup.dynSetProperty(prop.getName(), new Boolean(found));
			}
			catch (final NoSuchPropertyException e)
			{
				System.err.println("ERROR: bluetoothdiscover mistaken about dyn property " + prop.getName() + ": " + e);
			}
		}
	}

	/**
	 * behaviour
	 */
	protected void updateOutputs()
	{
		// echo input to output
		// setOutput(input);

		// dynamic properties??
		final DynamicPropertyDescriptor props[] = dynsup.dynGetProperties();
		// new properties?
		StringTokenizer toks = new StringTokenizer(watchfor, ",");
		while (toks.hasMoreTokens())
		{
			final String tok = toks.nextToken().trim().replace(':', '_');
			// new?
			boolean found = false;
			for (int i = 0; i < props.length && !found; i++)
			{
				if (props[i].getName().equals(tok))
				{
					found = true;
				}
			}
			if (!found)
			{
				System.out.println("Add dynamic property " + tok + "...");
				dynsup.addProperty(tok, Boolean.class, new Boolean(false));
			}
		}
		// remove properties?
		for (final DynamicPropertyDescriptor prop : props)
		{
			toks = new StringTokenizer(watchfor, ",");
			boolean found = false;
			while (toks.hasMoreTokens())
			{
				final String tok = toks.nextToken().trim().replace(':', '_');
				if (prop.getName().equals(tok))
				{
					found = true;
				}
			}
			if (!found)
			{
				System.out.println("Remove dynamic property " + prop.getName() + "...");
				try
				{
					dynsup.removeProperty(prop.getName());
				}
				catch (final NoSuchPropertyException e)
				{
					System.err.println("ERROR removing dyn property " + prop.getName() + ": " + e);
					e.printStackTrace(System.err);
				}
			}
		}
	}
}
