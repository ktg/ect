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
  Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.components.phidgets;

import com.phidgets.PhidgetException;
import com.phidgets.ServoPhidget;

import equip.ect.Category;
import equip.ect.Coerce;
import equip.ect.ECTComponent;
import equip.ect.NoSuchPropertyException;

/**
 * Used to control a 1-motor or 4-motor Phidget Servo board. <H3>Summary</H3> This component can be
 * used to connect and to control a PhidgetServo board. <H3>Description</H3> This component can be
 * used to connect to a 1-motor or 4-motor PhidgetServo board. This means that it can be used to
 * control the motor position of any servo motors that are connected to this board. For more
 * information, see http://www.phidgets.com. <H3>Installation</H3> <h4>Installing the Phidget
 * library</h4> Before using any Phidget component, you should install the Phidget library. This can
 * be downloaded from http://www.phidgets.com. Make sure you get version 2.1 of the library, and not
 * version 2.0 - this component will not work reliably with version 2.0, due to bugs in this library
 * version. <h4>Installing a PhidgetServo board</h4> Before using this component, you should (in
 * this order)
 * <ol>
 * <li>connect as many motors as are required to your PhidgetServo board
 * <li>connect your board, via USB, to your computer
 * </ol>
 * <H3>Configuration</H3> If you only have one PhidgetServo board connected to your computer, then
 * simply create a PhidgetServo component, and set its <i>configured</i> property to true. The
 * component will then connect to the board and create the correct number of <i>servoout</i>
 * properties for the board that you have used. If you have more than one board connected, find out
 * the serial number of the board you wish to connect, specify this using the
 * <i>configSerialNumber</i> property, and then set the <i>configured</i> property to <tt>true</tt>.
 * <H3>Usage</H3> To control the position of any servo motors attached to the board, set the values
 * of the <i>servoout</i> properties to any value in the range 0.0-1.0 (corresponds to a positional
 * range of 0-180 deg).
 * 
 * @classification Hardware/Output
 * @preferred
 * @technology Phidgets
 */
@ECTComponent
@Category("Hardware/Input & Output")
public class PhidgetServo extends PhidgetBase
{
	private int numberOfServoOutputs;

	static final int MAX_SERVO_VALUE = 180;

	static final String SERVO_OUT_PREFIX = "servoout";

	private float servoouts[];

	private ServoPhidget phid;

	public PhidgetServo()
	{
		super();

		try
		{
			phid = new ServoPhidget();
			absphid = phid;
		}
		catch (final PhidgetException e)
		{
			// ?
		}
	}

	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		if (name.startsWith(SERVO_OUT_PREFIX))
		{
			try
			{
				final int ix = new Integer(name.substring(SERVO_OUT_PREFIX.length())).intValue();
				final Float fval = (Float) Coerce.toClass(value, Float.class);
				final float val = (fval == null) ? 0.0f : fval.floatValue();
				// System.out.println("Set servo output "+ix+" to "+val);
				servoouts[ix] = val;
				if (connected)
				{
					phid.setPosition(ix, val * MAX_SERVO_VALUE);
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
	protected void detachment()
	{
	}

	@Override
	protected void firstAttachment()
	{
		try
		{
			numberOfServoOutputs = phid.getMotorCount();
			servoouts = new float[numberOfServoOutputs];

			// initial properties
			for (int i = 0; i < numberOfServoOutputs; i++)
			{
				dynsup.addProperty(SERVO_OUT_PREFIX + i, Float.class, new Float(0.0));
				servoouts[i] = 0.0f;

				phid.setPosition(i, servoouts[i] * MAX_SERVO_VALUE);
			}
		}
		catch (final PhidgetException e)
		{

		}
	}

	@Override
	protected void subsequentAttachment()
	{
		// if any servo positions have changed whilst
		// the phidget has been disconnected, then resupply them to the phidget

		// even when the phidget reports that it has reconnected,
		// in some cases it does not seem to be ready to accept
		// changes. So wait for a bit!

		waitForABit(CONNECTION_DELAY);

		for (int i = 0; i < servoouts.length; i++)
		{
			try
			{
				// System.out.println("Setting position: " + servoouts[i]);
				phid.setPosition(i, (servoouts[i]) * MAX_SERVO_VALUE);

				// I'm not convinced by the phidget libraries ability
				// to deal with rapid request

				waitForABit(OPERATION_DELAY);
			}
			catch (final PhidgetException e)
			{
				System.out.println("phidget exception");
				e.printStackTrace();
			}
		}
	}
}
