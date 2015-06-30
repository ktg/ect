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

import com.phidgets.MotorControlPhidget;
import com.phidgets.PhidgetException;

import equip.ect.Category;
import equip.ect.Coerce;
import equip.ect.ECTComponent;
import equip.ect.NoSuchPropertyException;

/**
 * @classification Hardware/Output
 * @preferred
 * @technology Phidgets
 */
@ECTComponent
@Category("Hardware/Phidgets")
public class PhidgetMotor extends PhidgetBase
{

	//static final int MAX_SERVO_VALUE = 180;

	static final String ACCEL_PREFIX = "accel";

	private float motorouts[];

	private MotorControlPhidget phid;

	public PhidgetMotor()
	{
		super();

		try
		{
			phid = new MotorControlPhidget();
			absphid = phid;
		}
		catch (final PhidgetException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		if (name.startsWith(ACCEL_PREFIX))
		{
			try
			{
				final int ix = Integer.parseInt(name.substring(ACCEL_PREFIX.length()));
				final Float fval = Coerce.toClass(value, Float.class);
				final float val = (fval == null) ? 0.0f : fval;
				// System.out.println("Set servo output "+ix+" to "+val);
				motorouts[ix] = val;
				if (connected)
				{
					phid.setAcceleration(ix, val);
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
			int numberOfServoOutputs = phid.getMotorCount();
			motorouts = new float[numberOfServoOutputs];

			// initial properties
			for (int i = 0; i < numberOfServoOutputs; i++)
			{
				dynsup.addProperty(ACCEL_PREFIX + i, Float.class, new Float(0.0));
				motorouts[i] = 0.0f;

				phid.setAcceleration(i, motorouts[i]);
			}
		}
		catch (final PhidgetException e)
		{
			e.printStackTrace();
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

		for (int i = 0; i < motorouts.length; i++)
		{
			try
			{
				// System.out.println("Setting position: " + motorouts[i]);
				phid.setAcceleration(i, (motorouts[i]));

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
