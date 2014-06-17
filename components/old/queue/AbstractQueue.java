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

Created by: Stefan Rennick Egglestone (University of Nottingham)
Contributors:
Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.queue;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

public abstract class AbstractQueue
{
	protected Object newObject;

	protected Object[] queue;
	protected Object topOfQueue;
	protected Object trigger;
	protected Object lastDiscardedObject;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized Object getLastDiscardedObject()
	{
		return lastDiscardedObject;
	}

	public synchronized Object getNewObject()
	{
		return null;
	}

	public synchronized Object[] getQueue()
	{
		return queue;
	}

	public synchronized Object getTopOfQueue()
	{
		return topOfQueue;
	}

	public synchronized Object getTriggerDiscardQueue()
	{
		return null;
	}

	public synchronized Object getTriggerDiscardTopOfQueue()
	{
		return null;
	}

	public Object getTriggerRandomizeQueue()
	{
		return null;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setNewObject(final Object newObject)
	{

		// this is overridden by the different types of queue that
		// extend this class eg lifo,fifo

		if (newObject != null)
		{
			addToQueue(newObject);
		}
	}

	public synchronized void setQueue(final Object[] queue)
	{
		final Object[] oldQueue = this.queue;
		this.queue = queue;

		calculateNewTopOfQueue();

		propertyChangeListeners.firePropertyChange("queue", oldQueue, this.queue);
	}

	public synchronized void setTriggerDiscardQueue(final Object trigger)
	{
		if (trigger != null)
		{
			setQueue(null);
		}
	}

	public synchronized void setTriggerDiscardTopOfQueue(final Object trigger)
	{
		// when the user triggers this property, then discard
		// the item on top of the queue

		if (trigger != null)
		{
			final Object popped = popTopOfQueue();

			// if an object has actually been popped
			// (ie there was an object on the queue)
			// then place it in a property

			if (popped != null)
			{
				setLastDiscardedObject(popped);
			}
		}
	}

	public synchronized void setTriggerRandomizeQueue(final Object trigger)
	{
		if (trigger != null)
		{
			// puts queue in random order
			// requested by andy boucher at rca
			final Object[] queue = getQueue();

			if (queue != null)
			{
				final Hashtable hash = new Hashtable();
				final double[] randomNums = new double[queue.length];

				// first, get random number per object in queue

				for (int i = 0; i < queue.length; i++)
				{
					final double randomNum = Math.random();
					randomNums[i] = randomNum;
					hash.put(new Double(randomNum), queue[i]);
				}

				// now sort random numbers

				Arrays.sort(randomNums);

				final Object[] newQueue = new Object[queue.length];

				for (int i = 0; i < newQueue.length; i++)
				{
					newQueue[i] = hash.get(new Double(randomNums[i]));
				}
				setQueue(newQueue);
			}
		}
	}

	public synchronized void stop()
	{
	}

	/**
	 * Adds an object to the end of an existing queue
	 */

	protected void addToQueue(final Object toAdd)
	{

		final Object[] queue = getQueue();

		if ((queue == null) || queue.length == 0)
		{
			// no objects in the queue, so add a new
			// one and set this to be at the top of the queue

			final Object[] newQueue = new Object[1];
			newQueue[0] = toAdd;
			setQueue(newQueue);
		}
		else
		{
			final Vector v = new Vector();

			for (final Object element : queue)
			{
				v.add(element);
			}

			v.add(toAdd);

			final Object[] newQueue = new Object[v.size()];

			for (int i = 0; i < newQueue.length; i++)
			{
				newQueue[i] = v.elementAt(i);
			}
			setQueue(newQueue);
		}
	}

	// called to set the property indicating which object is at
	// the top of the queue
	protected abstract void calculateNewTopOfQueue();

	// called to remove an item from the top of the queue
	protected abstract Object popTopOfQueue();

	protected synchronized void setLastDiscardedObject(final Object last)
	{
		final Object oldLast = this.lastDiscardedObject;
		this.lastDiscardedObject = last;

		propertyChangeListeners.firePropertyChange("lastDiscardedObject", oldLast, this.lastDiscardedObject);
	}

	protected synchronized void setTopOfQueue(final Object topOfQueue)
	{
		final Object oldTopOfQueue = this.topOfQueue;
		this.topOfQueue = topOfQueue;

		propertyChangeListeners.firePropertyChange("topOfQueue", oldTopOfQueue, this.topOfQueue);
	}
}
