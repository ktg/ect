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
  Tom Rodden (University of Nottingham)
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.components.smartit;

/*
 * SmartItsDefault, $RCSfile: SmartItsDefault.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:28 $
 *
 * $Author: chaoticgalen $
 * Copyright (c) 2001, SICS - Swedish Institute of Computer Science AB
 */

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Publishes sensor data from SmartIts devices.
 * <p>
 * This Java Bean is a Transformer for the ACCORD Toolkit. Through Equip it publishes a number of
 * properties that can be used to connect with other Transformers.
 * <p>
 * This is a <i>Physical-to-Digital Transformer</i>, it takes a physical property and transforms it
 * into some digital information. More technically and specifically it receives sensor data from the
 * default SmartIts devices (with the default sensor board) and publishes the values of these
 * sensors.
 * <p>
 * The Transformer has the following properties that it can export through Equip:
 * <ul>
 * <li><b>outDetect</b> - a int set to the last detected SmartIs.
 * <li><b>outPIR</b> - an int set to Passive IR sensor value
 * <li><b>outTouch</b> - an int set to touch sensor value
 * <li><b>outVLight</b> - an int set to vertical light sensor value
 * <li><b>outHLight</b> - an int set to horizontal light sensor value
 * <li><b>outAccXL</b> - an int set to accelerometer x dir sensor long value
 * <li><b>outAccYL</b> - an int set to accelerometer y dir sensor long value
 * <li><b>outAccX</b> - an int set to accelerometer x dir sensor value
 * <li><b>outAccY</b> - an int set to accelerometer y dir sensor value
 * <li><b>outTemp</b> - an int set to temp sensor value
 * <li><b>image</b> - a TransfereableMediaObject in the form of an image. This is the graphical
 * representation of this Transformer in in those editors that use it.</li>
 * </ul>
 */
public class SmartItsDefault implements SmartItsListener, Serializable
{

	/**
	 * The main method of this application.
	 * <p>
	 * Takes a list of arguments:
	 * <ol>
	 * <li>transformer name property</li>
	 * <li>id of smartit to publish from</li>
	 * <li>'detect' - publish detect property</li>
	 * <li>'pir' - publish PIR property</li>
	 * <li>'touch' - publish touch property</li>
	 * <li>'vertLight' - publish VLight property</li>
	 * <li>'horizLight' - publish HLight property</li>
	 * <li>'accXLong' - publish accXLong property</li>
	 * <li>'accYLong' - publish accYLong property</li>
	 * <li>'accX' - publish accX property</li>
	 * <li>'accY' - publish accY property</li>
	 * <li>'temp' - publish temp property</li>
	 * </ol>
	 * <p>
	 * It instantiate an instance of this class also and publishes it in equip.
	 * 
	 * @param args
	 *            a vector of Strings that are the arguemnts given to the application when started.
	 */

	public static void main(final String[] args)
	{
		/*
		 * String possibleArgs =
		 * "detect pir touch vertLight horizLight accXLong accYLong accX accY temp"; String[]
		 * exportList = new String[args.length-1]; exportList[0] = "image"; boolean useDetect =
		 * false, usePIR = false, useTouch = false, useVLight = false, useHLight = false, useAccXL =
		 * false, useAccYL = false, useAccX = false, useAccY = false, useTemp = false; if
		 * (args.length < 3) {
		 * System.err.println("Usage: SmartItsDefault [<transf name>] [smartID] [detect]"+
		 * "[pir] [touch] [vertLight] [horizLight] [accXLong] [accYLong]"+ "[accX] [accY] [temp]");
		 * }
		 * 
		 * int deviceID = Integer.parseInt(args[1]); for (int i=2; i < (args.length); i++) { if
		 * (possibleArgs.indexOf(args[i]) == -1) {
		 * System.err.println("Usage: SmartItsDefault [<transf name>] [smartID] [detect]"+
		 * "[pir] [touch] [vertLight] [horizLight] [accXLong] [accYLong]"+ "[accX] [accY] [temp]");
		 * System.exit(0); } else { if (args[i].equals("detect")) { useDetect = true;
		 * exportList[i-1] = "outDetect"; } else if (args[i].equals("pir")) { usePIR = true;
		 * exportList[i-1] = "outPIR"; } else if (args[i].equals("touch")) { useTouch = true;
		 * exportList[i-1] = "outTouch"; } else if (args[i].equals("vertLight")) { useVLight = true;
		 * exportList[i-1] = "outVLight"; } else if (args[i].equals("horizLight")) { useHLight =
		 * true; exportList[i-1] = "outHLight"; } else if (args[i].equals("accXLong")) { useAccXL =
		 * true; exportList[i-1] = "outAccXL"; } else if (args[i].equals("accYLong")) { useAccYL =
		 * true; exportList[i-1] = "outAccYL"; } else if (args[i].equals("accX")) { useAccX = true;
		 * exportList[i-1] = "outAccX"; } else if (args[i].equals("accY")) { useAccY = true;
		 * exportList[i-1] = "outAccY"; } else if (args[i].equals("temp")) { useTemp = true;
		 * exportList[i-1] = "outTemp"; } } }
		 */

		final SmartItsDefault si = new SmartItsDefault();
		// EquipBean.share(si, exportList, args[0], args[0]);
	}

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	// Bound properties for this transformers, see above for explanation
	private int outDetect, outPIR, outTouch, outVLight, outHLight, outAccXL, outAccYL, outAccX, outAccY, outTemp;
	private static SmartItsDeviceHandler device = null;
	private int deviceID;
	private boolean useDetect = true, usePIR = true, useTouch = true, useVLight = true, useHLight = true,
			useAccXL = true, useAccYL = true, useAccX = true, useAccY = true, useTemp = true;

	private static int deviceIDcounter = 1;

	private int oldDetectValue = 0;

	private int oldPIRValue = 0;

	private int oldTouchValue = 0;

	private int oldVLightValue = 0;

	private int oldHLightValue = 0;

	private int oldAccXLValue = 0;

	private int oldAccYLValue = 0;

	private int oldAccXValue = 0;

	private int oldAccYValue = 0;

	private int oldTempValue = 0;

	/**
	 * Default constructor.<br>
	 * Takes boolean arguments saying what sensor values to use. Opens connection to Smart-Its
	 * server. Will look for a graphical representation image based on the name argument (+ '.gif').
	 */
	public SmartItsDefault()
	{
		// set ID for this component
		this.deviceID = deviceIDcounter;

		// Now set the component up
		try
		{
			if (device == null)
			{
				device = new SmartItsDeviceHandler("smartits.properties");

				device.setDeviceID(deviceIDcounter++);
				device.setVerbose(true);

				if (!device.openConnection())
				{
					System.out.println("Could not open connection to " + "SmartIts Receiver!");
					// System.exit(0);
				}
			}
			device.addDeviceListener(this);

		}
		catch (final Exception e)
		{
			System.out.println("Could not communicate with SmartIts Receiver: " + e);
			// System.exit(0);
		}
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
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

	public void newAccelerometerXLongValue(final SmartItsEvent event)
	{
		if (useAccXL)
		{
			final int currentAccXLValue = event.getValue();

			if (this.deviceID == event.getDeviceID())
			{
				// Generate only event if the value changed
				if (currentAccXLValue != oldAccXLValue)
				{

					System.out.println("SmartIts, AccXL: " + currentAccXLValue);
					setOutAccXL(currentAccXLValue);
				}
				oldAccXLValue = currentAccXLValue;
			}
		}
	}

	public void newAccelerometerXValue(final SmartItsEvent event)
	{
		if (useAccX)
		{
			final int currentAccXValue = event.getValue();

			if (this.deviceID == event.getDeviceID())
			{
				// Generate only event if the value changed
				if (currentAccXValue != oldAccXValue)
				{

					System.out.println("SmartIts, AccX: " + currentAccXValue);
					setOutAccX(currentAccXValue);
				}
				oldAccXValue = currentAccXValue;
			}
		}
	}

	public void newAccelerometerYLongValue(final SmartItsEvent event)
	{
		if (useAccYL)
		{
			final int currentAccYLValue = event.getValue();

			if (this.deviceID == event.getDeviceID())
			{
				// Generate only event if the value changed
				if (currentAccYLValue != oldAccYLValue)
				{

					System.out.println("SmartIts, AccYL: " + currentAccYLValue);
					setOutAccYL(currentAccYLValue);
				}
				oldAccYLValue = currentAccYLValue;
			}
		}
	}

	public void newAccelerometerYValue(final SmartItsEvent event)
	{
		if (useAccY)
		{
			final int currentAccYValue = event.getValue();

			if (this.deviceID == event.getDeviceID())
			{
				// Generate only event if the value changed
				if (currentAccYValue != oldAccYValue)
				{

					System.out.println("SmartIts, AccY: " + currentAccYValue);
					setOutAccY(currentAccYValue);
				}
				oldAccYValue = currentAccYValue;
			}
		}
	}

	public void newLightHorizontalValue(final SmartItsEvent event)
	{
		if (useHLight)
		{
			final int currentHLightValue = event.getValue();

			if (this.deviceID == event.getDeviceID())
			{
				// Generate only event if the value changed
				if (currentHLightValue != oldHLightValue)
				{

					System.out.println("SmartIts, HLight: " + currentHLightValue);
					setOutHLight(currentHLightValue);
				}
				oldHLightValue = currentHLightValue;
			}
		}
	}

	public void newLightVerticalValue(final SmartItsEvent event)
	{
		if (useVLight)
		{
			final int currentVLightValue = event.getValue();

			if (this.deviceID == event.getDeviceID())
			{
				// Generate only event if the value changed
				if (currentVLightValue != oldVLightValue)
				{

					System.out.println("SmartIts, VLight: " + currentVLightValue);
					setOutVLight(currentVLightValue);
				}
				oldVLightValue = currentVLightValue;
			}
		}
	}

	public void newPIRValue(final SmartItsEvent event)
	{
		// System.out.println("PIR IS eventid is " + event.getDeviceID() +
		// "   -- this id"+deviceID);
		if (usePIR)
		{
			final int currentPIRValue = event.getValue();
			if (this.deviceID == event.getDeviceID())
			{
				// Generate only event if the value changed
				if (currentPIRValue != oldPIRValue)
				{
					System.out.println("SmartIts, PIR: " + currentPIRValue);
					setOutPIR(currentPIRValue);
				}
				oldPIRValue = currentPIRValue;
			}
		}
	}

	public void newTemperatureValue(final SmartItsEvent event)
	{
		if (useTemp)
		{
			final int currentTempValue = event.getValue();

			if (this.deviceID == event.getDeviceID())
			{
				// Generate only event if the value changed
				if (currentTempValue != oldTempValue)
				{

					System.out.println("SmartIts, Temp: " + currentTempValue);
					setOutTemp(currentTempValue);
				}
				oldTempValue = currentTempValue;
			}
		}
	}

	public void newTouchValue(final SmartItsEvent event)
	{
		if (useTouch)
		{
			final int currentTouchValue = event.getValue();

			if (deviceID == event.getDeviceID())
			{
				// Generate only event if the value changed
				if (currentTouchValue != oldTouchValue)
				{

					System.out.println("SmartIts, Touch: " + currentTouchValue);
					setOutTouch(currentTouchValue);
				}
				oldTouchValue = currentTouchValue;
			}
		}
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
		final int oldAccX = outAccX;
		this.outAccX = newAccX;
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
		final int oldAccXL = outAccXL;
		this.outAccXL = newAccXL;
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
		final int oldAccY = outAccY;
		this.outAccY = newAccY;
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
		final int oldAccYL = outAccYL;
		this.outAccYL = newAccYL;
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
		final int oldDetect = outDetect;
		this.outDetect = newDetect;
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
		final int oldHLight = outHLight;
		this.outHLight = newHLight;
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
		final int oldPIR = outPIR;
		this.outPIR = newPIR;
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
		final int oldTemp = outTemp;
		this.outTemp = newTemp;
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
		final int oldTouch = outTouch;
		this.outTouch = newTouch;
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
		final int oldVLight = outVLight;
		this.outVLight = newVLight;
		propertyChangeListeners.firePropertyChange("outVLight", oldVLight, outVLight);
	}

	public void smartItsDetected(final SmartItsEvent event)
	{
		if (useDetect)
		{
			final int currentDetectValue = event.getValue();

			if (this.deviceID == event.getDeviceID())
			{
				// Generate only event if the value changed
				if (currentDetectValue != oldDetectValue)
				{

					System.out.println("SmartIts, Detect: " + currentDetectValue);
					setOutDetect(currentDetectValue);
				}
				oldDetectValue = currentDetectValue;
			}
		}
	}
}
