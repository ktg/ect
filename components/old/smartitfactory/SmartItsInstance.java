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

/**
 * Represents a single SmartIt - created by SmartItsFactory.
 * 
 * <H3>Description</H3> Represents a single SmartIt - created by SmartItsFactory.
 * <p>
 * Based on combination of smartit.SmartItsDefault and subcomponenttest. This is pretty much a dummy
 * component controlled by the SmartItsFactory.
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
 * See SmartItsFactory for configuration details.
 * 
 * <H3>Usage</H3>
 * Deploy through SmartItsFactory.
 * 
 * <H3>Technical Details</H3>
 * There is no current standard for smart-it PIC code, and message parsing is based on ACCORDs
 * (www.sics.se/accord) original implementation. The PIC code is resides within the ECT repository
 * (ect\java\resources\smartits).
 * 
 * @classification Hardware/Input
 * @technology Smart-Its
 */
public class SmartItsInstance implements Serializable
{

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	// Bound properties for this transformers, see above for explanation
	private int outDetect, outPIR, outTouch, outVLight, outHLight, outAccXL, outAccYL, outAccX, outAccY, outTemp;

	private int deviceID;

	/**
	 * Default constructor - called by SmartItsFactory.<br>
	 */
	public SmartItsInstance(final int id)
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
	 * Used to read the bound property outAccXL
	 * 
	 * @see #setOutAccXL(int)
	 */
	public int getOutAccXL()
	{
		return this.outAccXL;
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
	 * Used to read the bound property outAccYL
	 * 
	 * @see #setOutAccYL(int)
	 */
	public int getOutAccYL()
	{
		return this.outAccYL;
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
	 * Used to read the bound property outHLight
	 * 
	 * @see #setOutHLight(int)
	 */
	public int getOutHLight()
	{
		return this.outHLight;
	}

	/**
	 * Used to read the bound property outPIR
	 * 
	 * @see #setOutPIR(int)
	 */
	public int getOutPIR()
	{
		return this.outPIR;
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
	 * Used to read the bound property outTouch
	 * 
	 * @see #setOutTouch(int)
	 */
	public int getOutTouch()
	{
		return this.outTouch;
	}

	/**
	 * Used to read the bound property outVLight
	 * 
	 * @see #setOutVLight(int)
	 */
	public int getOutVLight()
	{
		return this.outVLight;
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
		System.out.println("SmartIts " + deviceID + ", AccX: " + newAccX);
		propertyChangeListeners.firePropertyChange("outAccX", oldAccX, outAccX);
	}

	/**
	 * Sets the bound property outAccXL to a new value
	 * 
	 * @param newAccXL
	 *            a new value from accelerometer
	 * @see #getOutAccXL()
	 */
	public void setOutAccXL(final int newAccXL)
	{
		if (newAccXL == outAccXL) { return; }
		final int oldAccXL = outAccXL;
		this.outAccXL = newAccXL;
		System.out.println("SmartIts " + deviceID + ", AccXL: " + newAccXL);
		propertyChangeListeners.firePropertyChange("outAccXL", oldAccXL, outAccXL);
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
		System.out.println("SmartIts " + deviceID + ", AccY: " + newAccY);
		propertyChangeListeners.firePropertyChange("outAccY", oldAccY, outAccY);
	}

	/**
	 * Sets the bound property outAccYL to a new value
	 * 
	 * @param newAccYL
	 *            a new value from accelerometer
	 * @see #getOutAccYL()
	 */
	public void setOutAccYL(final int newAccYL)
	{
		if (newAccYL == outAccYL) { return; }
		final int oldAccYL = outAccYL;
		this.outAccYL = newAccYL;
		System.out.println("SmartIts " + deviceID + ", AccYL: " + newAccYL);
		propertyChangeListeners.firePropertyChange("outAccYL", oldAccYL, outAccYL);
	}

	/**
	 * Sets the bound property outDetect to a new value
	 * 
	 * @param newDetect
	 *            a new value from
	 * @see #getOutDetect()
	 */
	public void setOutDetect(final int newDetect)
	{
		if (newDetect == this.outDetect) { return; }
		final int oldDetect = outDetect;
		this.outDetect = newDetect;
		System.out.println("SmartIts " + deviceID + ", Detect: " + newDetect);
		propertyChangeListeners.firePropertyChange("outDetect", oldDetect, outDetect);
	}

	/**
	 * Sets the bound property outHLight to a new value
	 * 
	 * @param newHLight
	 *            a new value from light sensor
	 * @see #getOutHLight()
	 */
	public void setOutHLight(final int newHLight)
	{
		if (newHLight == outHLight) { return; }
		final int oldHLight = outHLight;
		this.outHLight = newHLight;
		System.out.println("SmartIts " + deviceID + ", HLight: " + newHLight);
		propertyChangeListeners.firePropertyChange("outHLight", oldHLight, outHLight);
	}

	/**
	 * Sets the bound property outPIR to a new value
	 * 
	 * @param newPIR
	 *            a new value from passive IR sensor
	 * @see #getOutPIR()
	 */
	public void setOutPIR(final int newPIR)
	{
		if (newPIR == this.outPIR) { return; }
		final int oldPIR = outPIR;
		this.outPIR = newPIR;
		System.out.println("SmartIts " + deviceID + ", PIR: " + newPIR);
		propertyChangeListeners.firePropertyChange("outPIR", oldPIR, outPIR);
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
		System.out.println("SmartIts " + deviceID + ", Temp: " + newTemp);
		propertyChangeListeners.firePropertyChange("outTemp", oldTemp, outTemp);
	}

	/**
	 * Sets the bound property outTouch to a new value
	 * 
	 * @param newTouch
	 *            a new value from touch sensor
	 * @see #getOutTouch()
	 */
	public void setOutTouch(final int newTouch)
	{
		if (newTouch == this.outTouch) { return; }
		final int oldTouch = outTouch;
		this.outTouch = newTouch;
		System.out.println("SmartIts " + deviceID + ", Touch: " + newTouch);
		propertyChangeListeners.firePropertyChange("outTouch", oldTouch, outTouch);
	}

	/**
	 * Sets the bound property outVLight to a new value
	 * 
	 * @param newVLight
	 *            a new value from light sensor
	 * @see #getOutVLight()
	 */
	public void setOutVLight(final int newVLight)
	{
		if (newVLight == outVLight) { return; }
		final int oldVLight = outVLight;
		this.outVLight = newVLight;
		System.out.println("SmartIts " + deviceID + ", VLight: " + newVLight);
		propertyChangeListeners.firePropertyChange("outVLight", oldVLight, outVLight);
	}
}
