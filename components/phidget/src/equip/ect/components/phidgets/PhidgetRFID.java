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
import com.phidgets.RFIDPhidget;
import com.phidgets.event.TagGainEvent;
import com.phidgets.event.TagGainListener;
import com.phidgets.event.TagLossEvent;
import com.phidgets.event.TagLossListener;

import equip.ect.Category;
import equip.ect.Coerce;
import equip.ect.ECTComponent;
import equip.ect.NoSuchPropertyException;

/**
 * Used to control a PhidgetRFID reader. <H3>Summary</H3> This component can connect to and receive
 * data from a PhidgetRFID reader. <H3>Description</H3> This component can connect to a PhidgetRFID
 * reader. It will then receive and display information from the RFID reader about any RFID tags
 * which are placed near the reader. <H3>Installation</H3> <h4>Installing the Phidget library</h4>
 * Before using any Phidget component, you should install the Phidget library. This can be
 * downloaded from http://www.phidgets.com. Make sure you get version 2.1 of the library, and not
 * version 2.0 - this component will not work reliably with version 2.0, due to bugs in this library
 * version. <h4>Installing a PhidgetRFID reader</h4> Before using this component, you should connect
 * your RFID reader via USB to your computer. <H3>Configuration</H3> If you only have one PhidgetRID
 * reader connected to your computer, then simply create a PhidgetRFID component, and set its
 * <i>configured</i> property to true. The component will then connect to the reader. If you have
 * more than one reader connected, find out the serial number of the reader you wish to connect,
 * specify this using the <i>configSerialNumber</i> property, and then set the <i>configured</i>
 * property to <tt>true</tt>. <H3>Usage</H3> <i>lastTag</i> property will be updated to the ID of a
 * tag when you place it near the reader
 * 
 * @classification Hardware/Input & Output
 * @technology Phidgets
 * @preferred
 */
@ECTComponent
@Category("Hardware/Phidgets")
public class PhidgetRFID extends PhidgetBase implements TagGainListener, TagLossListener
{
	static final int NUM_DIGITAL_OUT = 2;// on newer readers

	static final String DIGITAL_OUT_PREFIX = "digitalout";

	private boolean digitalouts[];

	private RFIDPhidget phid;

	protected boolean antennaOn = false;

	protected boolean ledOn = false;

	protected String currentTag;

	public PhidgetRFID()
	{
		super();

		try
		{
			phid = new RFIDPhidget();
			absphid = phid;

			phid.addTagGainListener(this);
			phid.addTagLossListener(this);

		}
		catch (final PhidgetException e)
		{
			e.printStackTrace();
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

	public boolean getAntennaOn()
	{
		return antennaOn;
	}

	public synchronized String getCurrentTag()
	{
		return currentTag;
	}

	public boolean getLedOn()
	{
		return ledOn;
	}

	public synchronized void setAntennaOn(final boolean newValue)
	{
		try
		{
			phid.setAntennaOn(newValue);
		}
		catch (final PhidgetException e)
		{
		}

		final boolean oldValue = this.antennaOn;
		this.antennaOn = newValue;

		propertyChangeListeners.firePropertyChange("antennaOn", oldValue, newValue);
	}

	public void setLedOn(final boolean newValue)
	{
		try
		{
			phid.setLEDOn(newValue);
		}
		catch (final PhidgetException e)
		{
		}

		final boolean oldValue = this.ledOn;
		this.ledOn = newValue;

		propertyChangeListeners.firePropertyChange("ledOn", oldValue, newValue);
	}

	@Override
	public void stop()
	{
		phid.removeTagGainListener(this);
		phid.removeTagLossListener(this);

		super.stop();
	}

	@Override
	public void tagGained(final TagGainEvent oe)
	{
		System.out.println("tag gained");
		setCurrentTag(oe.getValue());
	}

	@Override
	public void tagLost(final TagLossEvent oe)
	{
		setCurrentTag(null);
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
			digitalouts = new boolean[phid.getOutputCount()];

			for (int i = 0; i < digitalouts.length; i++)
			{
				digitalouts[i] = false;

				dynsup.addProperty(DIGITAL_OUT_PREFIX + i, Boolean.class, new Boolean(false));
				phid.setOutputState(i, digitalouts[i]);
			}

			subsequentAttachment();
		}
		catch (final PhidgetException e)
		{
			System.err.println("ERROR initialising: " + e);
			e.printStackTrace(System.err);
		}
	}

	protected void setCurrentTag(final String newValue)
	{
		final String oldValue = this.currentTag;
		this.currentTag = newValue;

		propertyChangeListeners.firePropertyChange("currentTag", oldValue, newValue);
	}

	@Override
	protected void subsequentAttachment()
	{
		// if the user has made changes to properties whilst the
		// phidget is detached, then update the phidget once it
		// reattaches

		waitForABit(CONNECTION_DELAY);

		try
		{
			phid.setAntennaOn(getAntennaOn());
			waitForABit(OPERATION_DELAY);

			phid.setLEDOn(getLedOn());
			waitForABit(OPERATION_DELAY);

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
