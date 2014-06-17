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

import java.io.Serializable;
import java.util.Vector;

/**
 * Allows a user to add and remove items from a queue. <H3>Summary</H3>
 * <P>
 * FIFOQueue is a component that allows any number of items of data to be stored, ready for use by
 * other components. FIFO stands for <B>F</B>irst <B>I</B>n <B>F</B>irst <B>O</B>ut, which refers to
 * the order in which data which has been added to a FIFOQueue can be retrieved. Items of data are
 * added into a FIFOQueue component one at a time, and are retrieved from the component in the same
 * order in which they were added (ie the first item added is retrieved first, then the second item
 * etc.)
 * </P>
 * <P>
 * Note that it is also possible to randomize the order of data items in a FIFOQueue.
 * </P>
 * <H3>Usage</H3>
 * <P>
 * To add an item of data, provide the item of data using the <i>newObject</i> property. The item of
 * data will be added to those held in the <i>queue</i> property. The <i>topOfQueue</i> property
 * (which always records the next item available for retrieval from the queue) will also change if
 * the new item of data is being added to an empty queue.
 * </P>
 * <P>
 * To retrieve the next item of available data from the queue, supply any value to the
 * <i>triggerDiscardTopOfQueue</i> property. This will trigger an item of data to be removed from
 * the <i>queue</i> property, and will cause the <i>topOfQueue</i> property to be updated to reflect
 * the next item of available data. The item of data which has just been retrieved from the queue
 * will appeare on property <i>lastDiscardedObject</i>.
 * </P>
 * <P>
 * To randomize the order of items in the queue, provide any value to the
 * <i>triggerRandomizeQueue</i> property.
 * </P>
 * <P>
 * To clear the queue of data, provide any value to the <i>triggerDiscardQueue</i> property.
 * </P>
 * 
 * @classification Behaviour/Queue
 * @preferred
 * @defaultOutputProperty lastDiscardedObject
 * @defaultInputProperty newObject
 */

public class FIFOQueue extends AbstractQueue implements Serializable
{

	@Override
	protected void calculateNewTopOfQueue()
	{
		final Object[] queue = getQueue();

		if ((queue == null) || (queue.length == 0))
		{
			setTopOfQueue(null);
		}
		else
		{
			final Object topOfQueue = getTopOfQueue();

			if (topOfQueue == null)
			{
				// queue was previously empty,
				// so new item is new top of queue

				setTopOfQueue(queue[0]);
			}
			else
			{
				if (!(topOfQueue.equals(queue[0])))
				{
					setTopOfQueue(queue[0]);
				}
			}
		}
	}

	/**
	 * removes object at top of queue
	 */

	@Override
	protected Object popTopOfQueue()
	{
		// fifo so remove object from start of queue
		// (ie array index 0)

		final Object[] queue = getQueue();

		if ((queue == null) || (queue.length == 0))
		{
			// nothing in the queue so nothing to pop!
			return null;
		}

		if (queue.length == 1)
		{
			// one item in the queue so popping it leaves
			// an empty queue

			setQueue(null);
		}

		final Vector v = new Vector();

		for (final Object element : queue)
		{
			v.add(element);
		}

		// get rid of top object and shift queue up the vector

		final Object popped = v.remove(0);

		final Object[] newQueue = new Object[v.size()];

		for (int i = 0; i < newQueue.length; i++)
		{
			newQueue[i] = v.elementAt(i);
		}

		setQueue(newQueue);

		return popped;
	}
}
