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
package equip.ect.components.smartitfactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Vector;

/**
 * Factory to detect and manage deployed Smart-It devices.
 * 
 * <H3>Description</H3> Factory for SmartItsInstances, which manages serial port and communication
 * on their behalf. Based on smartit.SmartItsDefault and subcomponenttest.ComponentFactory.
 * <P>
 * Sensor boards attached to the Smart-its, include accelerometer, light, movement (PIR),
 * temperature, and touch sensors.
 * <P>
 * Smart-Its are discovered upon the first identifiable message received at the base station.
 * 
 * <H3>Installation</H3>
 * Attach a Smart-It station to a serial port in order to receive messages from other Smart-Its.
 * Make sure the protocol and message format in the Smart-It PICs are compatible.
 * 
 * <H3>Configuration</H3>
 * Specify the configuration under config/smartits.properties Such as comm port, baud rate, etc for
 * your specific property. Upon launching the component, settings should be read from the properties
 * file, and run a serial device listener on the specified comm port.
 * 
 * <H3>Usage</H3>
 * Install the Smart-It base station, run the component and wait for Smart-It messages.
 * 
 * <H3>Technical Details</H3>
 * The Smart-Its device handler uses java.commx comm package to listen in to serial comm port
 * events.
 * <P>
 * There is no current standard for smart-it PIC code, and message parsing is based on ACCORDs
 * (www.sics.se/accord) original implementation. The PIC code is resides within the ECT repository
 * (ect\java\resources\smartits).
 * 
 * @classification Hardware/Input
 * @technology Smart-Its
 * @author humble
 * 
 */
public class SmartItsFactory implements SmartItsListener, Serializable
{

	/**
	 * The main method of this application.
	 * <p>
	 * Create one instance.
	 */
	public static void main(final String[] args)
	{
		new SmartItsFactory();
	}

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * singleton device
	 */
	private static SmartItsDeviceHandler device = null;

	private boolean useDetect = true, usePIR = true, useTouch = true, useVLight = true, useHLight = true,
			useAccXL = true, useAccYL = true, useAccX = true, useAccY = true, useTemp = true;

	static int deviceIDcounter = 1;

	/**
	 * Vector of SmartItsInstance
	 */
	protected Vector children = new Vector();

	protected String status = "new";

	/**
	 * Default constructor.<br>
	 */
	public SmartItsFactory()
	{

		// Now set the component up
		try
		{
			synchronized (SmartItsFactory.class)
			{
				final String drivername = "com.sun.comm.Win32Driver";
				try
				{
					final javax.comm.CommDriver driver = (javax.comm.CommDriver) Class.forName(drivername)
							.newInstance();
					driver.initialize();
				}
				catch (final Exception e)
				{
					System.out.println("ERROR initialising javax.comm driver " + drivername + ": " + e.getMessage());
				}

				if (device == null)
				{
					device = new SmartItsDeviceHandler(new java.io.File("config/smartits.properties").getAbsolutePath());

					device.setDeviceID(deviceIDcounter++);
					DeviceHandler.setVerbose(true);
				}
				if (!device.openConnection())
				{
					System.out.println("Could not open connection to " + "SmartIts Receiver!");
					// System.exit(0);
					status = "Could not open connection";
				}
				else
				{
					status = "OK";
				}

				device.addDeviceListener(this);
			}
		}
		catch (final Exception e)
		{
			System.out.println("Could not communicate with SmartIts Receiver: " + e);
			status = "ERROR: " + e;
			// System.exit(0);
		}
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public SmartItsInstance[] getChildren()
	{
		synchronized (this)
		{
			return (SmartItsInstance[]) children.toArray(new SmartItsInstance[children.size()]);
		}
	}

	public String getStatus()
	{
		return status;
	}

	public void newAccelerometerXLongValue(final SmartItsEvent event)
	{
		if (useAccXL)
		{
			final int currentAccXLValue = event.getValue();
			final SmartItsInstance inst = getChild(event.getDeviceID());
			if (inst != null)
			{
				inst.setOutAccXL(currentAccXLValue);
			}
		}
	}

	public void newAccelerometerXValue(final SmartItsEvent event)
	{
		if (useAccX)
		{
			final int currentAccXValue = event.getValue();
			final SmartItsInstance inst = getChild(event.getDeviceID());
			if (inst != null)
			{
				inst.setOutAccX(currentAccXValue);
			}
		}
	}

	public void newAccelerometerYLongValue(final SmartItsEvent event)
	{
		if (useAccYL)
		{
			final int currentAccYLValue = event.getValue();
			final SmartItsInstance inst = getChild(event.getDeviceID());
			if (inst != null)
			{
				inst.setOutAccYL(currentAccYLValue);
			}
		}
	}

	public void newAccelerometerYValue(final SmartItsEvent event)
	{
		if (useAccY)
		{
			final int currentAccYValue = event.getValue();
			final SmartItsInstance inst = getChild(event.getDeviceID());
			if (inst != null)
			{
				inst.setOutAccY(currentAccYValue);
			}
		}
	}

	public void newLightHorizontalValue(final SmartItsEvent event)
	{
		if (useHLight)
		{
			final int currentHLightValue = event.getValue();
			final SmartItsInstance inst = getChild(event.getDeviceID());
			if (inst != null)
			{
				inst.setOutHLight(currentHLightValue);
			}
		}
	}

	public void newLightVerticalValue(final SmartItsEvent event)
	{
		if (useVLight)
		{
			final int currentVLightValue = event.getValue();
			final SmartItsInstance inst = getChild(event.getDeviceID());
			if (inst != null)
			{
				inst.setOutVLight(currentVLightValue);
			}
		}
	}

	public void newPIRValue(final SmartItsEvent event)
	{
		// System.out.println("PIR IS eventid is " + event.getDeviceID() + " --
		// this id"+deviceID);
		if (usePIR)
		{
			final int currentPIRValue = event.getValue();
			final SmartItsInstance inst = getChild(event.getDeviceID());
			if (inst != null)
			{
				inst.setOutPIR(currentPIRValue);
			}
		}
	}

	public void newTemperatureValue(final SmartItsEvent event)
	{
		if (useTemp)
		{
			final int currentTempValue = event.getValue();
			final SmartItsInstance inst = getChild(event.getDeviceID());
			if (inst != null)
			{
				inst.setOutTemp(currentTempValue);
			}
		}
	}

	public void newTouchValue(final SmartItsEvent event)
	{
		if (useTouch)
		{
			final int currentTouchValue = event.getValue();
			final SmartItsInstance inst = getChild(event.getDeviceID());
			if (inst != null)
			{
				inst.setOutTouch(currentTouchValue);
			}
		}
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void smartItsDetected(final SmartItsEvent event)
	{
		if (useDetect)
		{
			final SmartItsInstance inst = getChild(event.getDeviceID());
			if (inst != null)
			{
				inst.setOutDetect(event.getValue());
			}

		}
	}

	/**
	 * stop
	 */
	public void stop()
	{
		try
		{
			synchronized (SmartItsFactory.class)
			{
				if (device != null)
				{
					device.removeDeviceListener(this);
					device.closeConnection();
					System.err.println("SmartITFactory closed device");
				}
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR closing smartit device handler: " + e);
			e.printStackTrace(System.err);
		}
		// setStatus("Stopped");
	}

	protected SmartItsInstance getChild(final int id)
	{
		SmartItsInstance oldValue[] = null;
		SmartItsInstance newValue[] = null;
		SmartItsInstance newChild = null;
		boolean changed = false;
		synchronized (this)
		{
			oldValue = getChildren();
			int i;
			for (i = 0; i < oldValue.length; i++)
			{
				if (oldValue[i].getDeviceID() == id) { return oldValue[i]; }
			}

			try
			{
				System.out.println("Create SmartItsInstance " + id);
				newChild = new SmartItsInstance(id);
				children.addElement(newChild);
				newValue = getChildren();
				changed = true;
			}
			catch (final Exception e)
			{
				System.err.println("ERROR creating SmartItsInstance " + id + ": " + e);
				e.printStackTrace(System.err);
			}
		}
		if (changed)
		{
			propertyChangeListeners.firePropertyChange("children", oldValue, newValue);
		}
		return newChild;
	}
}
