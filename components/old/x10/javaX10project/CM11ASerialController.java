/*
 * Copyright 2002-2004, Wade Wassenberg  All rights reserved.
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

package equip.ect.components.x10.javaX10project;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

import equip.ect.components.x10.javaX10project.util.LogHandler;
import equip.ect.components.x10.javaX10project.util.ThreadSafeQueue;

/**
 * CM11ASerialController is an X10 Controller that bridges x10 hardware and software by
 * communicating via a SerialPort with the x10 "CM11A" module. <BR>
 * <BR>
 * 
 * This class requires the javax.comm package from Su.
 * 
 * @author Wade Wassenberg
 * 
 * @version 1.0
 */

public class CM11ASerialController implements Runnable, Controller, Serializable
{

	class DelayThread extends Thread
	{

		protected long delay;

		DelayThread(final long delay)
		{
			super();
			this.delay = delay;
		}

		@Override
		public void run()
		{
			try
			{
				sleep(delay);
				initiateNextCommandOrPair();
			}
			catch (final InterruptedException ie)
			{
				System.out.println("Thread interrupted " + ie.getMessage());
			}
		}
	}

	/**
	 * OK byte - the x10 "CM11A" protocol OK byte.
	 * 
	 */

	public static final byte OK = ((byte) 0x00);

	/**
	 * READY byte - the x10 "CM11A" protocol READY byte.
	 * 
	 */

	public static final byte READY = ((byte) 0x55);

	/**
	 * TIME byte - the x10 "CM11A" protocol TIME byte.
	 * 
	 */

	public static final byte TIME = ((byte) 0x9B);

	/**
	 * TIME_POLL byte - the x10 "CM11A" protocol TIME_POLL byte.
	 * 
	 */

	public static final byte TIME_POLL = ((byte) 0xA5);

	/**
	 * DATA_POLL byte - the x10 "CM11A" protocol DATA_POLL byte.
	 * 
	 */

	public static final byte DATA_POLL = ((byte) 0x5A);

	/**
	 * PC_READY byte - the x10 "CM11A" protocol PC_READY byte.
	 * 
	 */

	public static final byte PC_READY = ((byte) 0xC3);

	private static final long DELAY_TIME = 1000; // ms
	public DataInputStream fromX10;
	public DataOutputStream toX10;
	private SerialPort sp;
	private boolean running;
	private ThreadSafeQueue queue;

	private boolean requestClear = false;

	/**
	 * CM11ASerialController constructs and starts the Controller on the specified comport. On a
	 * Windows based PC, the comport is of the form "COM1".
	 * 
	 * @param comport
	 *            the communications port in which the "CM11A" module is connected.
	 * @exception IOException
	 *                if an error occurs while trying to connect to the specified Communications
	 *                Port.
	 * 
	 */
	public CM11ASerialController(final String comport) throws IOException
	{
		try
		{

			final CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(comport);

			sp = (SerialPort) cpi.open("JavaX10Controller", 10000);

			sp.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			fromX10 = new DataInputStream(sp.getInputStream());
			toX10 = new DataOutputStream(sp.getOutputStream());

		}
		catch (final NoSuchPortException nspe)
		{
			throw new IOException("No Such Port: " + nspe.getMessage());
		}
		catch (final PortInUseException piue)
		{
			throw new IOException("Port in use: " + piue.getMessage());
		}
		catch (final UnsupportedCommOperationException ucoe)
		{
			throw new IOException("Unsupported comm operation: " + ucoe.getMessage());
		}

		queue = new ThreadSafeQueue();

		new Thread(this).start();

	}

	/**
	 * addCommand adds a command to the queue to be dispatched.
	 * 
	 * @param command
	 *            the Command to be dispatched.
	 * 
	 */
	@Override
	public void addCommand(final Command command)
	{

		if (queue.peek() != null)
		{
			synchronized (this)
			{

				queue.destroyCommand(command);
				queue.enqueue(command);
			}

		}
		else
		{
			synchronized (this)
			{
				queue.destroyCommand(command);
				queue.enqueue(command);
			}
			new DelayThread(DELAY_TIME).start();
		}
	}

	@Override
	public void addCommandPair(final CommandPair pair)
	{

		if (queue.peek() != null)
		{
			synchronized (this)
			{
				queue.destroyCommandPair(pair);
				queue.enqueue(pair);
			}
		}
		else
		{
			synchronized (this)
			{
				queue.destroyCommandPair(pair);
				queue.enqueue(pair);
			}
			new DelayThread(DELAY_TIME).start();
		}
	}

	public boolean executingCommands()
	{
		return (queue.peek() != null);
	}

	/**
	 * run is the thread loop that constantly blocks and reads events off of the serial port from
	 * the "CM11A" module.
	 */
	@Override
	public void run()
	{
		running = true;
		try
		{
			while (running)
			{
				final byte nextByte = fromX10.readByte();

				switch (nextByte)
				{
					case TIME_POLL:
						setInterfaceTime();
						break;
					case DATA_POLL:
						System.err.println("**Fatal Error!** Failed to handle data poll");
						// removed data poll method - should never have been called
						break;
					default:
						handleChecksum(nextByte);
				}

				if (running && (fromX10.available() == 0))
				{
					initiateNextCommandOrPair();
				}
			}
			sp.close();
			doNotify();
		}
		catch (final IOException ioe)
		{
			if (sp != null) // shutdownNow was not invoked
			{
				LogHandler.logException(ioe, 1);
			}
		}
	}

	/**
	 * shutdownNow shuts down the controller and closes the serial port immediately. shutdown(long)
	 * is the preferred method of shutting down the controller, but this method provides an
	 * immediate, unclean, non-graceful means to shut down the controller.
	 * 
	 */

	public void shutdownNow()
	{
		final SerialPort sp = this.sp;
		this.sp = null;
		sp.close();
	}

	private synchronized void doNotify()
	{
		notifyAll();
	}

	private synchronized void doWait(final long millis) throws InterruptedException
	{
		wait(millis);
	}

	private byte getChecksum(final short packet)
	{
		final byte header = (byte) ((packet >> 8) & 0x00FF);
		final byte code = (byte) (packet & 0x00FF);
		return ((byte) ((header + code) & 0xFF));
	}

	private synchronized void handleChecksum(final byte checksum)
	{

		Command nextCommand;
		CommandPair pair = null;
		final Object item = queue.peek();

		if (item != null)
		{

			if (item instanceof Command)
			{
				nextCommand = (Command) item;

			}
			else
			{
				pair = (CommandPair) item;
				nextCommand = ((!pair.getFirst().isExecuting()) ? pair.getFirst() : pair.getSecond());
			}

			nextCommand.setExecuting(true);
			// some scenarios which may crop up can be changed to a quicker command
			if (nextCommand.getFunctionByte() == Command.DIM && nextCommand.getLevel() == 100)
			{
				// change 100% dim commands to a 'turn straight off' command
				nextCommand = new Command(nextCommand.getAddressString(), Command.OFF);
			}

			System.out.println("Executing... " + nextCommand.toString());
			final byte address = getChecksum(nextCommand.getAddress());

			if (checksum == address)
			{
				try
				{
					toX10.writeByte(OK);
					toX10.flush();
					byte ready = fromX10.readByte();

					if (ready == READY)
					{

						final short func = nextCommand.getFunction();
						toX10.writeShort(func);
						toX10.flush();

						if (fromX10.readByte() == getChecksum(nextCommand.getFunction()))
						{
							toX10.writeByte(OK);
							toX10.flush();
							ready = fromX10.readByte();
							if (ready == READY)
							{
								if (pair == null)
								{
									queue.dequeue(); // dequeue normal command

									// dont dequeue if we havent finished with the pair yet
								}
								else if (pair.getSecond().isExecuting())
								{
									queue.dequeue(); // dequeue pair command
								}
							}
						}
					}
				}
				catch (final IOException ioe)
				{

					if (sp != null) // shutdownNow was not invoked
					{
						LogHandler.logException(ioe, 1);
					}
				}
			}
			else
			{
				LogHandler.logMessage("CheckSum: " + Integer.toHexString(checksum), 2);
			}
		}
	}

	private synchronized void initiateNextCommand(final Command nextCommand)
	{

		try
		{
			final short data = nextCommand.getAddress();
			toX10.writeShort(data);
			toX10.flush();

		}
		catch (final IOException ioe)
		{
			if (sp != null)
			{
				LogHandler.logException(ioe, 1);
			}
		}
	}

	private synchronized void initiateNextCommandOrPair()
	{

		Command command;

		final Object head = queue.peek();
		if (head != null)
		{
			if (head instanceof Command)
			{
				command = (Command) head;
				initiateNextCommand(command);

			}
			else if (head instanceof CommandPair)
			{
				final CommandPair pair = (CommandPair) head;
				pair.setExecuting(true);
				command = ((!pair.getFirst().isExecuting()) ? pair.getFirst() : pair.getSecond());
				initiateNextCommand(command);
			}
			else
			{
				System.err.println("**Fatal error!** Unexpected class found on queue!");
				queue.dequeue(); // dequeue the erroneous class and try to continue
			}
		}
	}

	private void setInterfaceTime() throws IOException
	{
		toX10.writeByte(TIME);
		toX10.writeByte(0);
		toX10.writeByte(0);
		toX10.writeByte(0);
		toX10.writeByte(0);
		toX10.writeByte(0);
		toX10.writeByte(0);
	}
}
