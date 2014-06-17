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

 Created by: Stefan Rennick Egglestone(University of Nottingham)
 Contributors:
 Stefan Rennick Egglestone(University of Nottingham)

 */
package equip.ect.components.dataprocessing;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Calculates a rolling average of its input value. <h3>Usage</h3>
 * <P>
 * RollingAverage samples the value on property <i>input</i> when triggered by the placement of any
 * value on property <i>triggerSample</i>. It keeps track of the number of samples specified in
 * property <i>sampleCount</i>, and places the mean of its stored samples onto property
 * <i>output</i>.
 * </P>
 * <P>
 * This means that RollingAverage is useful for "smoothing out" continously-changing values produced
 * by other components.
 * </P>
 * <P>
 * </P>
 * 
 * @classification Data/Numeric
 * @defaultOutputProperty output
 * @defaultInputProperty input
 * @author stef
 */
@ECTComponent
@Category("Data/Processing")
public class TimedAverage implements Serializable, Runnable
{
	private final CircularList buffer = new CircularList(1000);

	private double value;

	private double average = 0.0;

	private boolean running;

	private double frequency = 30;

	private long sleep = 33;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public TimedAverage()
	{
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized double getAverage()
	{
		return average;
	}

	public double getFrequency()
	{
		return frequency;
	}

	public int getSize()
	{
		return buffer.maxSize();
	}

	public boolean getRunning()
	{
		return running;
	}

	public synchronized double getValue()
	{
		return value;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	@Override
	public void run()
	{
		while (running)
		{
			try
			{
				buffer.add(value);

				recalcOutput();

				try
				{
					Thread.sleep(sleep);
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void setFrequency(final double newFreq)
	{
		if (newFreq == frequency) { return; }

		final double oldFreq = frequency;
		frequency = newFreq;
		sleep = (long) (1000 / frequency);

		propertyChangeListeners.firePropertyChange("frequency", oldFreq, newFreq);
	}

	public void setSize(final int newSize)
	{
		final int oldSize = buffer.maxSize();
		if (oldSize == newSize) { return; }
		if (newSize <= 0) { return; }

		propertyChangeListeners.firePropertyChange("average", getAverage(), value);
		average = value;

		buffer.setMaxSize(newSize);
		propertyChangeListeners.firePropertyChange("size", oldSize, newSize);
	}

	public void setRunning(final boolean newRunning)
	{
		if (running == newRunning) { return; }
		running = newRunning;

		propertyChangeListeners.firePropertyChange("running", !running, running);

		if (running)
		{
			new Thread(this).start();
		}
	}

	public void setValue(final double newValue)
	{
		final double oldValue = value;
		value = newValue;

		propertyChangeListeners.firePropertyChange("value", oldValue, newValue);
	}

	protected void recalcOutput()
	{
		final double oldAverage = average;
		double total = 0;
		for (final double value : buffer)
		{
			total += value;
		}
		if (buffer.size() > 0)
		{
			average = total / buffer.size();
		}
		else
		{
			average = 0;
		}

		propertyChangeListeners.firePropertyChange("average", oldAverage, average);
	}
}