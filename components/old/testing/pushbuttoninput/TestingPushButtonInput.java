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
package equip.ect.components.testing.pushbuttoninput;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import equip.ect.components.testing.common.TestingBaseClass;

/**
 * Test input which is a push button generating true/false.
 * 
 * @author Chris Greenhalgh
 */
public class TestingPushButtonInput extends TestingBaseClass
{

	/**
	 * testing main - no args. Makes a single instance.
	 */
	public static void main(final String[] args)
	{
		new TestingPushButtonInput();
	}

	/**
	 * the widget
	 */
	private JButton button;

	/**
	 * the value
	 */
	protected boolean value = false;

	/**
	 * main cons, no args.
	 */
	public TestingPushButtonInput()
	{
		super();
		// make GUI and show
		final Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		button = new JButton("value");
		contentPane.add(button, BorderLayout.CENTER);
		// handlers for GUI input
		button.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent event)
			{
				intSetValue(button.getModel().isArmed(), false);
			}
		});
		this.pack();
		this.setVisible(true);
	}

	/**
	 * value getter
	 */
	public synchronized boolean getValue()
	{
		return value;
	}

	/**
	 * button value, boolean
	 */
	public synchronized void setValue(final boolean val)
	{
		intSetValue(val, true);
	}

	/**
	 * value setter - internal
	 */
	protected synchronized void intSetValue(final boolean value, final boolean updateWidget)
	{
		if (this.value == value)
		{
			// no change
			return;
		}
		final Object old = new Boolean(this.value);
		this.value = value;
		// update gui - may not be swing thread
		if (updateWidget)
		{
			runSwing(new Runnable()
			{
				@Override
				public void run()
				{
					if (!stopped)
					{
						button.getModel().setArmed(value);
					}
				}
			});
		}
		// fire change event
		propertyChangeListeners.firePropertyChange("value", old, new Boolean(value));
	}
}
