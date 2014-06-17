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
package equip.ect.components.ui;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Test input which is a slider generating values in the range 0.0f - 1.0f. Rate limited to 10Hz.
 * 
 * @author Chris Greenhalgh
 */
@ECTComponent
@Category("ui")
public class SliderInput extends UIBase implements ActionListener
{
	/**
	 * the widget
	 */
	private JSlider slider = new JSlider(0, MAX_VALUE, 0);

	/**
	 * max value of slider (not output, which has max 1.0f)
	 */
	public static int MAX_VALUE = 1000;

	/**
	 * the value (as per slider)
	 */
	protected int value = 0;

	/**
	 * next value being set (delayed by timer)
	 */
	protected int nextValue = 0;

	/**
	 * fire change Timer
	 */
	protected Timer changeTimer = null;

	/**
	 * min change interval, ms
	 */
	protected int MIN_CHANGE_INTERVAL_MS = 100;

	/**
	 * main cons, no args.
	 */
	public SliderInput()
	{
		super();
		// make GUI and show
		final Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(slider, BorderLayout.CENTER);
		changeTimer = new Timer(MIN_CHANGE_INTERVAL_MS, this);
		// handlers for GUI input
		slider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent event)
			{
				synchronized (SliderInput.this)
				{
					if (stopped) { return; }
					nextValue = slider.getValue();
					if (!changeTimer.isRunning())
					{
						// set now
						intSetValue(slider.getValue(), false);
						// set timer - to delay a subsequent set
						changeTimer.start();
					}
				}
			}
		});
		this.pack();
		this.setVisible(true);
	}

	/**
	 * timer action performed
	 */
	@Override
	public synchronized void actionPerformed(final ActionEvent ae)
	{
		if (stopped)
		{
			// stop
			if (changeTimer.isRunning())
			{
				changeTimer.stop();
			}
			return;
		}
		if (nextValue != value)
		{
			intSetValue(nextValue, false);
		}
		else
		{
			// fired no change - stop
			changeTimer.stop();
		}
	}

	/**
	 * value getter
	 */
	public synchronized float getValue()
	{
		return value * 1.0f / MAX_VALUE;
	}

	/**
	 * value of slider - range 0.0f to 1.0f
	 */
	public synchronized void setValue(final float val)
	{
		intSetValue((int) (MAX_VALUE * val), true);
		// external set widget update should have set nextValue
	}

	/**
	 * value setter - internal
	 */
	protected synchronized void intSetValue(final int value, final boolean updateWidget)
	{
		if (this.value == value)
		{
			// no change
			return;
		}
		final Object old = new Float(this.getValue());
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
						slider.setValue(value);
					}
				}
			});
		}
		// fire change event
		propertyChangeListeners.firePropertyChange("value", old, new Float(getValue()));
	}
}
