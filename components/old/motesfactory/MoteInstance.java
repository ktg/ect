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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Represents a single Mote particle - created by MotesFactory.
 * 
 * <H3>Description</H3> Represents a single SmartIt - created by SmartItsFactory.
 * <p>
 * This is pretty much a dummy component controlled by MotesFactory.
 * <P>
 * Sensor boards attached to the Motes, include accelerometer, light, movement (PIR), temperature,
 * and touch sensors.
 * <P>
 * Motes are discovered upon the first identifiable message received at the base station.
 * 
 * <H3>Installation</H3>
 * Attach a Mote station to a serial port in order to receive messages from other Motes. Make sure
 * the protocol and message format in the Smart-It PICs are compatible.
 * 
 * <H3>Configuration</H3>
 * See MotesFactory for configuration details.
 * 
 * <H3>Usage</H3>
 * Deploy through MotesFactory.
 * 
 * <H3>Technical Details</H3> There is no current standard for Motes PIC code.
 * 
 * @classification Hardware/Input
 * @technology Motes
 */
public class MoteInstance implements Serializable
{

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	// Bound properties for this transformers, see above for explanation
	// Bound properties for this transformers, see above for explanation
	private int outDetect, outLight, outTemp, outAccX, outAccY, outMagX, outMagY;

	private int deviceID;

	/**
	 * Default constructor - called by SmartItsFactory.<br>
	 */
	public MoteInstance(final int id)
	{
		// set ID for this component
		this.deviceID = id;
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * get device id
	 */
	public int getDeviceID()
	{
		return deviceID;
	}

	/**
	 * Used to read the bound property outAccX
	 * 
	 * @see #setOutAccX(int)
	 */
	public int getOutAccX()
	{
		return this.outAccX;
	}

	/**
	 * Used to read the bound property outAccY
	 * 
	 * @see #setOutAccY(int)
	 */
	public int getOutAccY()
	{
		return this.outAccY;
	}

	/**
	 * Used to read the bound property outDetect
	 * 
	 * @see #setOutDetect(int)
	 */
	public int getOutDetect()
	{
		return this.outDetect;
	}

	/**
	 * Used to read the bound property outVLight
	 * 
	 * @see #setOutVLight(int)
	 */
	public int getOutLight()
	{
		return this.outLight;
	}

	/**
	 * Used to read the bound property outMagX
	 * 
	 * @see #setOutMagX(int)
	 */
	public int getOutMagX()
	{
		return this.outMagX;
	}

	/**
	 * Used to read the bound property outMagY
	 * 
	 * @see #setOutMagY(int)
	 */
	public int getOutMagY()
	{
		return this.outMagY;
	}

	/**
	 * Used to read the bound property outTemp
	 * 
	 * @see #setOutTemp(int)
	 */
	public int getOutTemp()
	{
		return this.outTemp;
	}

	/**
	 * persistent child id - string
	 */
	public String getPersistentChild()
	{
		return new Integer(this.deviceID).toString();
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * Sets the bound property outAccX to a new value
	 * 
	 * @param newAccX
	 *            a new value from accelerometer
	 * @see #getOutAccX()
	 */
	public void setOutAccX(final int newAccX)
	{
		if (newAccX == outAccX) { return; }
		final int oldAccX = outAccX;
		this.outAccX = newAccX;
		// System.out.println("SmartIts "+deviceID+", AccX: " + newAccX);
		propertyChangeListeners.firePropertyChange("outAccX", oldAccX, outAccX);
	}

	/**
	 * Sets the bound property outAccY to a new value
	 * 
	 * @param newAccY
	 *            a new value from accelerometer
	 * @see #getOutAccY()
	 */
	public void setOutAccY(final int newAccY)
	{
		if (newAccY == outAccY) { return; }
		final int oldAccY = outAccY;
		this.outAccY = newAccY;
		// System.out.println("SmartIts "+deviceID+", AccY: " + newAccY);
		propertyChangeListeners.firePropertyChange("outAccY", oldAccY, outAccY);
	}

	public void setOutDetect(final int newDetect)
	{
		if (newDetect == this.outDetect) { return; }
		final int oldDetect = outDetect;
		this.outDetect = newDetect;
		System.out.println("SmartIts " + deviceID + ", Detect: " + newDetect);
		propertyChangeListeners.firePropertyChange("outDetect", oldDetect, outDetect);
	}

	public void setOutLight(final int newLight)
	{
		if (newLight == outLight) { return; }
		final int oldLight = outLight;
		this.outLight = newLight;
		// System.out.println("SmartIts "+deviceID+", Light: " + newLight);
		propertyChangeListeners.firePropertyChange("outLight", oldLight, outLight);
	}

	/**
	 * Sets the bound property outMagX to a new value
	 * 
	 * @param newMagX
	 *            a new value from accelerometer
	 * @see #getOutMagX()
	 */
	public void setOutMagX(final int newMagX)
	{
		if (newMagX == outMagX) { return; }
		final int oldMagX = outMagX;
		this.outMagX = newMagX;
		// System.out.println("SmartIts "+deviceID+", MagX: " + newMagX);
		propertyChangeListeners.firePropertyChange("outMagX", oldMagX, outMagX);
	}

	/**
	 * Sets the bound property outMagY to a new value
	 * 
	 * @param newMagY
	 *            a new value from accelerometer
	 * @see #getOutMagY()
	 */
	public void setOutMagY(final int newMagY)
	{
		if (newMagY == outMagY) { return; }
		final int oldMagY = outMagY;
		this.outMagY = newMagY;
		// System.out.println("SmartIts "+deviceID+", MagY: " + newMagY);
		propertyChangeListeners.firePropertyChange("outMagY", oldMagY, outMagY);
	}

	/**
	 * Sets the bound property outTemp to a new value
	 * 
	 * @param newTemp
	 *            a new value from temperature sensor
	 * @see #getOutTemp()
	 */
	public void setOutTemp(final int newTemp)
	{
		if (newTemp == outTemp) { return; }
		final int oldTemp = outTemp;
		this.outTemp = newTemp;
		System.out.println("motes " + deviceID + ", Temp: " + newTemp);
		propertyChangeListeners.firePropertyChange("outTemp", oldTemp, outTemp);
	}

}
