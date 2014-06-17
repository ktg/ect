/*
<COPYRIGHT>

Copyright (c) 2006, University of Nottingham
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

Created by: Stefan Rennick Egglestone (University of Nottingham)
Contributors:
  Stefan Rennick Egglestone (University of Nottingham)

 */

package equip.ect.components.goldsmiths;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertiesSupport;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.NoSuchPropertyException;

/**
 * @classification user/goldsmiths
 * @preferred
 */

public class AdvertBuffer implements Serializable, DynamicProperties
{
	// normal property support section

	protected class OutputRunnable implements Runnable
	{
		// periodically, selects random adverts from
		// the buffer and place them onto an output

		boolean shouldRun = true;

		@Override
		public void run()
		{
			while (shouldRun)
			{
				// System.out.println("start of loop in thread");

				synchronized (this)
				{
					final int advertCount = getAdvertCount();

					if (advertCount > 0)
					{
						// System.out.println("searching for adverts");

						final int outputCount = getOutputCount();

						final double random = Math.random();
						final double posDouble = advertCount * random;

						// round down to select an index from the buffer
						final int pos = (int) posDouble;

						// System.out.println("found data at: " + pos);

						final Object randomObject = buffer.get(pos);

						try
						{
							dynSetProperty(OUTPUT_PREFIX + currentOutput, randomObject);
						}
						catch (final NoSuchPropertyException e)
						{
							System.out.println("no such property exception");
						}

						currentOutput++;

						if (currentOutput >= outputCount)
						{
							currentOutput = 0;
						}

						/*
						 * 
						 * for(int i = 0 ; i < outputCount ; i++) {
						 * //System.out.println("processing output " + i);
						 * 
						 * double random = Math.random(); double posDouble = advertCount * random;
						 * 
						 * // round down to select an index from the buffer int pos =
						 * (int)posDouble;
						 * 
						 * //System.out.println("found data at: " + pos);
						 * 
						 * Object theObject = buffer.elementAt(pos);
						 * 
						 * try { dynSetProperty(OUTPUT_PREFIX+i, theObject); }
						 * catch(NoSuchPropertyException e) {
						 * System.out.println("no such property exception"); } }
						 */
					}
				}

				final long delay = getOutputDelay();
				try
				{
					Thread.sleep(delay);
				}
				catch (final InterruptedException e)
				{
				}
			}
		}

		public void stopLooping()
		{
			shouldRun = false;
		}
	}

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	protected DynamicPropertiesSupport dynsup;

	// dynamic property support section

	Thread outputThread;

	OutputRunnable outputRunnable;
	protected final int DEFAULT_BUFFER_SIZE = 100;

	protected final String OUTPUT_PREFIX = "output";

	protected final int DEFAULT_OUTPUT_COUNT = 4;
	protected int maximumSize = DEFAULT_BUFFER_SIZE;

	protected final List<Object> buffer = new ArrayList<Object>();

	protected Object[] newAdverts;

	protected int advertCount = 0;
	protected int outputCount = 0;
	protected int currentOutput = 0;

	protected long outputDelay = 10000;

	public AdvertBuffer()
	{
		dynsup = new DynamicPropertiesSupport(propertyChangeListeners);
		setOutputCount(DEFAULT_OUTPUT_COUNT);

		outputRunnable = new OutputRunnable();
		outputThread = new Thread(outputRunnable);

		outputThread.start();
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	@Override
	public DynamicPropertyDescriptor[] dynGetProperties()
	{
		return dynsup.dynGetProperties();
	}

	/**
	 * get one property by name
	 */
	@Override
	public Object dynGetProperty(final String name) throws NoSuchPropertyException
	{
		return dynsup.dynGetProperty(name);
	}

	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		dynsup.dynSetProperty(name, value);
	}

	public synchronized int getAdvertCount()
	{
		return advertCount;
	}

	public int getMaximumSize()
	{
		return maximumSize;
	}

	public synchronized Object[] getNewAdverts()
	{
		return newAdverts;
	}

	public synchronized int getOutputCount()
	{
		return outputCount;
	}

	public synchronized long getOutputDelay()
	{
		return outputDelay;
	}

	public synchronized Object getTriggerFlush()
	{
		return null;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setAdvertCount(final int newValue)
	{
		final int oldValue = this.advertCount;
		this.advertCount = newValue;

		propertyChangeListeners.firePropertyChange("advertCount", new Integer(oldValue), new Integer(newValue));
	}

	public synchronized void setMaximumSize(int newValue)
	{
		if (newValue <= 0)
		{
			newValue = DEFAULT_BUFFER_SIZE;
		}

		final int oldValue = this.maximumSize;
		this.maximumSize = newValue;

		propertyChangeListeners.firePropertyChange("maximumSize", new Integer(oldValue), new Integer(newValue));

		dropExcessAdverts();
	}

	public synchronized void setNewAdverts(final Object[] newValue)
	{
		final Object[] oldValue = this.newAdverts;
		this.newAdverts = newValue;

		propertyChangeListeners.firePropertyChange("newAdverts", oldValue, newValue);

		if (newValue != null)
		{
			synchronized (buffer)
			{
				for (final Object element : newValue)
				{
					buffer.add(0, element);
				}

				setAdvertCount(buffer.size());
				dropExcessAdverts();
			}
		}
	}

	public synchronized void setOutputCount(final int newValue)
	{
		final int oldValue = this.outputCount;
		this.outputCount = newValue;

		for (int i = 0; i < oldValue; i++)
		{
			try
			{
				dynsup.removeProperty(OUTPUT_PREFIX + i);
			}
			catch (final NoSuchPropertyException e)
			{
			}
		}

		for (int i = 0; i < outputCount; i++)
		{

			dynsup.addProperty(OUTPUT_PREFIX + i, String.class, null, false);

		}

		propertyChangeListeners.firePropertyChange("outputCount", new Integer(oldValue), new Integer(newValue));

		// advert buffer changes one output property at a time.
		// this index marks the current one
		currentOutput = 0;

	}

	public synchronized void setOutputDelay(final long newValue)
	{
		final long oldValue = this.outputDelay;
		this.outputDelay = newValue;

		propertyChangeListeners.firePropertyChange("outputDelay", new Long(oldValue), new Long(newValue));
	}

	public synchronized void setTriggerFlush(final Object newValue)
	{
		if (newValue != null)
		{

			buffer.clear();
			setAdvertCount(0);
			setNewAdverts(null);

			final int outputCount = getOutputCount();

			for (int i = 0; i < outputCount; i++)
			{
				try
				{
					dynSetProperty(OUTPUT_PREFIX + i, null);
				}
				catch (final NoSuchPropertyException e)
				{
					// shouldn't happen!
				}
			}
		}
	}

	public void stop()
	{
		outputRunnable.stopLooping();
		try
		{
			outputThread.join();
		}
		catch (final InterruptedException e)
		{
		}
	}

	protected synchronized void dropExcessAdverts()
	{
		if (buffer.size() > getMaximumSize())
		{
			final int numberToRemove = buffer.size() - getMaximumSize();
			final int removePos = getMaximumSize();

			for (int i = 0; i < numberToRemove; i++)
			{
				buffer.remove(removePos);
			}

			setAdvertCount(buffer.size());
		}
	}
}
