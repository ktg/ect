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
  Dagmar Kern (University of Sussex)
  Mark Stringer (University of Sussex)
  Jan Humble (University of Nottingham)
  Tom Hart (University of Nottingham)
  Chris Greenhalgh (University of Nottingham)
  Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.components.phidgets;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import equip.ect.Category;
import equip.ect.Coerce;
import equip.ect.ECTComponent;
import equip.ect.NoSuchPropertyException;

/**
 * Used to control a PhidgetInterfaceKit. <H3>Summary</H3> This component can be used to connect to
 * and control a PhidgetInterfaceKit. <H3>Description</H3> This component can be used to connect to
 * any of the variety of available PhidgetInterfaceKits. These devices can be used to gather both
 * analog and digital inputs, and to produce digital outputs. For more information, see
 * http://www.phidgets.com. <H3>Installation</H3> <h4>Installing the Phidget library</h4> Before
 * using any Phidget component, you should install the Phidget library. This can be downloaded from
 * http://www.phidgets.com. Make sure you get version 2.1 of the library, and not version 2.0 - this
 * component will not work reliably with version 2.0, due to bugs in this library version. <h4>
 * Installing a PhidgetInterfaceKit board</h4> Before using this component, you should (in this
 * order)
 * <ol>
 * <li>connect as sensors that are required to your PhidgetInterfaceKit board
 * <li>connect your board, via USB, to your computer
 * </ol>
 * <H3>Configuration</H3> If you only have one PhidgetInterfaceKit board connected to your computer,
 * then simply create a PhidgetInterfaceKit component, and set its <i>configured</i> property to
 * true. The component will then connect to the board and create the correct number of
 * <i>digitalin</i>,<i>digitalout</i> and <i>analogin</i> properties for the board that you have
 * used. If you have more than one board connected, find out the serial number of the board you wish
 * to connect, specify this using the <i>configSerialNumber</i> property, and then set the
 * <i>configured</i> property to <tt>true</tt>. <H3>Usage</H3> <i>digitalin</i> and <i>analogin</i>
 * properties will automatically update to match the signals being received by the board. You can
 * set any of the <i>digitalout</i> properties to a value in the range 0.0-1.0, and the board will
 * then change the relevant digital output.
 * 
 * @technology Phidgets
 * @displayName PhidgetInterfaceKit
 * @classification Hardware/Input & Output
 * @preferred
 */
@ECTComponent
@Category("Hardware/Phidgets")
public class PhidgetInterfaceKit extends PhidgetBase implements InputChangeListener, SensorChangeListener
{

	protected int numDigitalIn = 0;
	protected int numDigitalOut = 0;
	protected int numAnalogIn = 0;

	static final int MAX_SENSOR_VALUE = 1000;

	static final String DIGITAL_IN_PREFIX = "digitalin";
	static final String DIGITAL_OUT_PREFIX = "digitalout";
	static final String ANALOG_IN_PREFIX = "analogin";

	private boolean digitalouts[];

	private InterfaceKitPhidget phid;

	public PhidgetInterfaceKit()
	{
		super();

		try
		{
			phid = new InterfaceKitPhidget();
			absphid = phid;

			phid.addInputChangeListener(this);
			phid.addSensorChangeListener(this);
		}
		catch (final PhidgetException e)
		{
			// ?
		}
	}

	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		if (name.startsWith(DIGITAL_OUT_PREFIX))
		{
			try
			{
				final int ix = new Integer(name.substring(DIGITAL_OUT_PREFIX.length())).intValue();
				final Boolean bval = (Boolean) Coerce.toClass(value, Boolean.class);
				final boolean val = (bval != null) && bval.booleanValue();
				// System.out.println("Set output "+ix+" to "+val);
				digitalouts[ix] = val;
				if (connected)
				{
					phid.setOutputState(ix, val);
				}
			}
			catch (final Exception e)
			{
				System.err.println("ERROR: " + e);
				e.printStackTrace(System.err);
			}
		}
		super.dynSetProperty(name, value);
	}

	@Override
	public void inputChanged(final InputChangeEvent oe)
	{
		try
		{
			dynSetProperty(DIGITAL_IN_PREFIX + oe.getIndex(), new Boolean(oe.getState()));
		}
		catch (final NoSuchPropertyException e)
		{
		}
	}

	@Override
	public void sensorChanged(final SensorChangeEvent se)
	{
		try
		{
			dynSetProperty(ANALOG_IN_PREFIX + se.getIndex(), getModifiedSensorValue(se.getValue()));
		}
		catch (final NoSuchPropertyException e)
		{
		}
	}

	@Override
	public void stop()
	{
		phid.removeInputChangeListener(this);
		phid.removeSensorChangeListener(this);

		super.stop();
	}

	Float getModifiedSensorValue(final int originalSensorValue)
	{
		return new Float((1.0f * originalSensorValue) / MAX_SENSOR_VALUE);
	}

	@Override
	protected void detachment()
	{
	}

	@Override
	protected void firstAttachment()
	{
		// dynamically create the correct number
		// of properties for this phidget

		try
		{
			numDigitalOut = phid.getOutputCount();
			numDigitalIn = phid.getInputCount();
			numAnalogIn = phid.getSensorCount();

			digitalouts = new boolean[numDigitalOut];

			for (int i = 0; i < numDigitalOut; i++)
			{
				dynsup.addProperty(DIGITAL_OUT_PREFIX + i, Boolean.class, new Boolean(false), false);
				phid.setOutputState(i, digitalouts[i]);
			}

			for (int i = 0; i < numDigitalIn; i++)
			{
				dynsup.addProperty(DIGITAL_IN_PREFIX + i, Boolean.class, new Boolean(false), true);
				dynSetProperty(DIGITAL_IN_PREFIX + i, new Boolean(phid.getInputState(i)));
			}

			for (int i = 0; i < numAnalogIn; i++)
			{
				dynsup.addProperty(ANALOG_IN_PREFIX + i, Float.class, new Float(0.0f), true);
				dynSetProperty(ANALOG_IN_PREFIX + i, getModifiedSensorValue(phid.getSensorValue(i)));
			}
		}
		catch (final PhidgetException e)
		{
			// ?
		}
		catch (final NoSuchPropertyException e)
		{
			// ?
		}
	}

	@Override
	protected void subsequentAttachment()
	{
		waitForABit(CONNECTION_DELAY);

		try
		{
			for (int i = 0; i < digitalouts.length; i++)
			{
				phid.setOutputState(i, digitalouts[i]);
				waitForABit(OPERATION_DELAY);
			}
		}
		catch (final PhidgetException e)
		{
		}
	}

}
