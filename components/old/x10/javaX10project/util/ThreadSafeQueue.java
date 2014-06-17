/*
 * Copyright 2002-2003, Wade Wassenberg  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package equip.ect.components.x10.javaX10project.util;

import java.io.Serializable;
import java.util.LinkedList;

import equip.ect.components.x10.X10_Constants;
import equip.ect.components.x10.javaX10project.Command;
import equip.ect.components.x10.javaX10project.CommandPair;

/**
 * ThreadSafeQueue this is an implementation of a First In First Out (FIFO) data structure. This
 * class uses synchronization to provide thread safety for adding and removing objects to and from
 * the queue. All of the methods in this class are synchronized, therefore temporary thread blocking
 * might occur when calling any of these methods.
 * 
 * 
 * @author Wade Wassenberg
 * 
 * @version 1.0
 */

public class ThreadSafeQueue implements Serializable, /* Temp */X10_Constants
{

	/**
	 * queue LinkedList the list of objects in the queue
	 * 
	 */

	LinkedList queue;

	/**
	 * ThreadSafeQueue constructs an empty ThreadSafeQueue
	 * 
	 * 
	 */

	public ThreadSafeQueue()
	{
		queue = new LinkedList();
	}

	/**
	 * dequeue removes and returns the first object in the queue without blocking. If the queue is
	 * empty, null is returned.
	 * 
	 * @return Object - the next object in the queue or null if the queue is empty.
	 * 
	 */

	public synchronized Object dequeue()
	{
		if (queue.size() < 1)
		{
			return (null);
		}
		else
		{
			final Object element = queue.removeFirst();
			return (element);
		}
	}

	/**
	 * dequeue removes the specified object from the queue if and only if the specified object is
	 * the first object in the queue.
	 * 
	 * @param toDequeue
	 *            the object to dequeue
	 * 
	 */

	public synchronized void dequeue(final Object toDequeue)
	{
		if (queue.size() > 0)
		{
			if (queue.getFirst() == toDequeue)
			{
				queue.removeFirst();
			}
		}
	}

	/**
	 * dequeueNextAvailable blocks until the next object becomes available to the queue. If the
	 * queue is not empty, the first object is removed from the queue and returned without blocking.
	 * 
	 * @return Object - the next available object in the queue.
	 * @exception InterruptedException
	 *                if the blocked thread is interrupted before an object becomes available.
	 * 
	 */

	public synchronized Object dequeueNextAvailable() throws InterruptedException
	{
		while (queue.size() < 1)
		{
			wait();
		}
		final Object element = queue.removeFirst();
		return element;
	}

	public synchronized void destroyCommand(final Command toDestroy)
	{
		Command cmd;
		Object item;
		for (int i = 0; i < queue.size(); i++)
		{
			item = queue.get(i);
			if (item instanceof Command)
			{
				cmd = (Command) queue.get(i);
				if (!cmd.isExecuting() && cmd.equals(toDestroy))
				{
					// destroy this match
					item = queue.remove(i);
				}
			}
		}
	}

	public synchronized void destroyCommandPair(final CommandPair toDestroy)
	{
		CommandPair pair;
		Object item;
		for (int i = 0; i < queue.size(); i++)
		{
			item = queue.get(i);
			if (item instanceof CommandPair)
			{
				pair = (CommandPair) queue.get(i);

				if (!pair.isExecuting() && pair.equals(toDestroy))
				{
					// destroy this match
					item = queue.remove(i);
					// System.out.println("Destroying... " + item);
				}
			}
		}
	}

	/**
	 * empty completely removes all objects that are currently in the queue.
	 * 
	 * 
	 */
	public synchronized void empty()
	{

		while (dequeue() != null)
		{
			;
		}
	}

	/**
	 * enqueue adds the specified object to the end of the queue
	 * 
	 * @param element
	 *            the object to be added to the end of the queue
	 * 
	 */

	public synchronized void enqueue(final Object element)
	{
		queue.add(element);
		try
		{
			notifyAll();
		}
		catch (final IllegalMonitorStateException imse)
		{
		}
	}

	/**
	 * peek returns the next available object in the queue without physically removing the object
	 * from the queue.
	 * 
	 * @return Object the next available object in the queue or null if the queue is empty.
	 * 
	 */

	public synchronized Object peek()
	{

		if (queue.size() < 1)
		{
			return (null);
		}
		else
		{
			return (queue.getFirst());
		}
	}

	@Override
	public String toString()
	{

		final StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < queue.size(); i++)
		{
			buffer.append(queue.get(i).toString());

			if (i < queue.size() - 1)
			{
				buffer.append(" ---> ");
			}
		}
		return buffer.toString();
	}
}
