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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Jan Humble (University of Nottingham)

 */
package equip.ect.components.motesfactory;

import se.sics.commx.motes.MotesDeviceHandler;
import se.sics.commx.motes.MotesEvent;
import se.sics.commx.motes.MotesListener;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Vector;

/**
 * Factory to detect and manage deployed Smart-It devices.
 * <p/>
 * <H3>Description</H3> Factory for MotesInstances, which manages serial port and communication on
 * their behalf.
 * <p/>
 * Sensor boards attached to the Motes, include accelerometer, light, movement (PIR), temperature,
 * and touch sensors.
 * <p/>
 * Motes are discovered upon the first identifiable message received at the base station.
 * <p/>
 * <H3>Installation</H3>
 * Attach a Motes station to a serial port in order to receive messages from other Motes. Make sure
 * the protocol and message format in the Motes PICs are compatible.
 * <p/>
 * <H3>Configuration</H3>
 * Specify the configuration such as comm port, baud rate, etc for your specific property. Upon
 * launching the component, settings should be read from the properties file, and run a serial
 * device listener on the specified comm port.
 * <p/>
 * <H3>Usage</H3>
 * Install the Smart-It base station, run the component and wait for Smart-It messages.
 * <p/>
 * <H3>Technical Details</H3>
 * The Smart-Its device handler uses java.commx comm package to listen in to serial comm port
 * events.
 * <p/>
 * There is no current standard for smart-it PIC code, and message parsing is based on ACCORDs
 * (www.sics.se/accord) original implementation. The PIC code is resides within the ECT repository
 * (ect\java\resources\smartits).
 *
 * @author humble
 * @classification Hardware/Input
 * @technology Motes
 */
public class MotesFactory implements MotesListener, Serializable
{

	static int deviceIDcounter = 1;
	/**
	 * singleton device
	 */
	private static MotesDeviceHandler device = null;
	/**
	 * Vector of MotesInstance
	 */
	protected Vector children = new Vector();
	protected String status = "new";
	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * Default constructor.<br>
	 */
	public MotesFactory()
	{

		// Now set the component up
		try
		{
			synchronized (MotesFactory.class)
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
					device = new MotesDeviceHandler(new java.io.File("config/motes.properties").getAbsolutePath());

					device.setDeviceID(deviceIDcounter++);
					device.setVerbose(true);
				}
				if (!device.openConnection())
				{
					System.out.println("Could not open connection to " + "Motes Receiver!");
					// System.exit(0);
					status = "Could not open connection";
				}
				else
				{
					status = "OK";
				}
				System.out.println("Adding device Listener");
				device.addDeviceListener(this);
			}
		}
		catch (final Exception e)
		{
			System.out.println("Could not communicate with Motes Receiver: " + e);
			status = "ERROR: " + e;
			// System.exit(0);
		}
	}

	/**
	 * The main method of this application.
	 * <p/>
	 * Create one instance.
	 */

	public static void main(final String[] args)
	{
		final MotesFactory mf = new MotesFactory();
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public MoteInstance[] getChildren()
	{
		synchronized (this)
		{
			return (MoteInstance[]) children.toArray(new MoteInstance[children.size()]);
		}
	}

	public String getStatus()
	{
		return status;
	}

	public void moteDetected(final MotesEvent event)
	{
		final MoteInstance inst = getChild(event.getDeviceID());
		if (inst != null)
		{
			inst.setOutDetect(((Integer) event.getValue()).intValue());
		}
	}

	public void newAccelerometerXValue(final MotesEvent event)
	{
		final MoteInstance inst = getChild(event.getDeviceID());
		if (inst != null)
		{
			final int currentAccXValue = ((Short) event.getValue()).shortValue();
			inst.setOutAccX(currentAccXValue);
		}
	}

	public void newAccelerometerYValue(final MotesEvent event)
	{
		final MoteInstance inst = getChild(event.getDeviceID());
		if (inst != null)
		{
			final short currentAccYValue = ((Short) event.getValue()).shortValue();
			inst.setOutAccY(currentAccYValue);
		}
	}

	public void newLightValue(final MotesEvent event)
	{
		final MoteInstance inst = getChild(event.getDeviceID());
		if (inst != null)
		{
			final short currentValue = ((Short) event.getValue()).shortValue();
			inst.setOutLight(currentValue);
		}
	}

	public void newMagnetXValue(final MotesEvent event)
	{
		final MoteInstance inst = getChild(event.getDeviceID());
		if (inst != null)
		{
			final short currentMagXValue = ((Short) event.getValue()).shortValue();
			inst.setOutMagX(currentMagXValue);
		}
	}

	public void newMagnetYValue(final MotesEvent event)
	{
		final MoteInstance inst = getChild(event.getDeviceID());
		if (inst != null)
		{
			final short currentMagYValue = ((Short) event.getValue()).shortValue();
			inst.setOutMagY(currentMagYValue);
		}
	}

	public void newTemperatureValue(final MotesEvent event)
	{
		final MoteInstance inst = getChild(event.getDeviceID());
		if (inst != null)
		{
			final short currentTempValue = ((Short) event.getValue()).shortValue();
			inst.setOutTemp(currentTempValue);
		}
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * stop
	 */
	public void stop()
	{
		try
		{
			synchronized (MotesFactory.class)
			{
				if (device != null)
				{
					device.removeDeviceListener(this);
					device.closeConnection();
					System.err.println("MotesFactory closed device");
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

	protected MoteInstance getChild(final int id)
	{
		MoteInstance oldValue[] = null;
		MoteInstance newValue[] = null;
		MoteInstance newChild = null;
		boolean changed = false;
		synchronized (this)
		{
			oldValue = getChildren();
			int i;
			for (i = 0; i < oldValue.length; i++)
			{
				if (oldValue[i].getDeviceID() == id)
				{
					return oldValue[i];
				}
			}

			try
			{
				System.out.println("Create MotesInstance " + id);
				newChild = new MoteInstance(id);
				children.addElement(newChild);
				newValue = getChildren();
				changed = true;
			}
			catch (final Exception e)
			{
				System.err.println("ERROR creating MotesInstance " + id + ": " + e);
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
