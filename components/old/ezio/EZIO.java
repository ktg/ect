/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Sussex
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Sussex
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

Created by: Ted Phelps (University of Sussex)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Ted Phelps (University of Sussex)

 */
package equip.ect.components.ezio;

/*
 * EZIO component
 * $RCSfile: EZIO.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:28 $
 *
 * $Author: chaoticgalen $
 * Original Author: Ted Phelps
 *
 * $Log: EZIO.java,v $
 * Revision 1.2  2012/04/03 12:27:28  chaoticgalen
 * Tidying up. Fixed xml reading/writing in Java 6. Some new components
 *
 * Revision 1.1  2005/05/03 11:54:38  cgreenhalgh
 * Import from dumas cvs
 *
 * Revision 1.8  2005/04/28 15:59:14  cmg
 * add BSD license boilerplates
 *
 * Revision 1.7  2005/04/27 16:31:13  cmg
 * change to new property config naming convention, and added configured property in place of isRunning setter
 *
 * Revision 1.6  2004/10/12 13:54:05  phelps
 * Added a stop method, plus load and persist for the Persistable interface
 *
 * Revision 1.5  2004/08/24 11:10:16  phelps
 * Converted the analog outputs to floats in the range [0.0, 1.0] rather
 * than integers in the range [0, 255].
 *
 * Revision 1.4  2004/08/24 10:52:55  phelps
 * Converting EZIO inputs into properties
 *
 * Revision 1.3  2004/08/24 09:53:22  phelps
 * Now reading all EZIO inputs and recording their values
 *
 * Revision 1.2  2004/08/23 17:07:27  phelps
 * Fixed the baud rate and finished off the basic state machine.
 *
 * Revision 1.1  2004/08/23 16:41:11  phelps
 * Initial: extracted interesting bits from the tagit component
 */

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

import equip.ect.ContainerManager;
import equip.ect.PersistenceManager;

public class EZIO implements Serializable, SerialPortEventListener
{
	public static final String PERSISTENCE_FILE_TAG = "EZIO";
	public static final String STATUS_STOPPED = "stopped";
	public static final String STATUS_INIT = "initializing";
	public static final String STATUS_RUNNING = "running";

	private static final int[] masks = { 0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, 0x80 };
	private static final int STATE_START = 0;
	private static final int STATE_REQ_PENDING = 1;
	private static final int STATE_PAUSE = 2;
	private static final int STATE_RESET = 3;

	private static final int MAJOR_TIMEOUT = 1000;
	private static final int MINOR_TIMEOUT = 100;

	private String status = STATUS_STOPPED;
	private int state = STATE_START;
	private CommPortIdentifier id;
	private SerialPort port;
	private Timer timer = new Timer();
	private TimerTask task;
	private int delay = 50;
	private String[] inputs = { "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "R" };
	private int[] values = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private byte[] buffer = new byte[16];
	private int offset = 0;

	private File persistFile;
	private PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	protected boolean configured = false;

	public EZIO()
	{
		// ensure javax.comm initialised
		final String drivernames[] = new String[] { "com.sun.comm.Win32Driver" };
		for (final String drivername : drivernames)
		{
			try
			{
				final java.lang.Object driver = Class.forName(drivername).newInstance();
				final java.lang.reflect.Method init = driver.getClass().getMethod("initialize", new Class[0]);
				init.invoke(driver, new Object[0]);
				// ((javax.comm.Driver)driver).initialize();
				System.out.println("Initialised javax.comm driver " + drivername + " OK");
				break;
			}
			catch (final Exception e)
			{
				System.out.println("ERROR initialising javax.comm driver " + drivername + ": " + e.getMessage());
			}
		}
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public float getA0()
	{
		return values[0] / 255f;
	}

	public float getA1()
	{
		return values[1] / 255f;
	}

	public float getA2()
	{
		return values[2] / 255f;
	}

	public float getA3()
	{
		return values[3] / 255f;
	}

	public float getA4()
	{
		return values[4] / 255f;
	}

	public float getA5()
	{
		return values[5] / 255f;
	}

	public float getA6()
	{
		return values[6] / 255f;
	}

	public float getA7()
	{
		return values[7] / 255f;
	}

	public boolean getConfigured()
	{
		return configured;
	}

	public boolean getD0()
	{
		return (values[8] & 0x01) != 0;
	}

	public boolean getD1()
	{
		return (values[8] & 0x02) != 0;
	}

	public boolean getD2()
	{
		return (values[8] & 0x04) != 0;
	}

	public boolean getD3()
	{
		return (values[8] & 0x08) != 0;
	}

	public boolean getD4()
	{
		return (values[8] & 0x10) != 0;
	}

	public boolean getD5()
	{
		return (values[8] & 0x20) != 0;
	}

	public boolean getD6()
	{
		return (values[8] & 0x40) != 0;
	}

	public boolean getD7()
	{
		return (values[8] & 0x80) != 0;
	}

	public int getDelay()
	{
		return delay;
	}

	public boolean getIsRunning()
	{
		return port != null;
	}

	public String getPort()
	{
		final CommPortIdentifier id = this.id;

		if (id == null)
		{
			return null;
		}
		else
		{
			return id.getName();
		}
	}

	public String getStatus()
	{
		return status;
	}

	public synchronized void load(final File persistFile, final ContainerManager manager) throws IOException
	{
		ObjectInputStream in;
		int major, minor;
		String port;
		int delay;
		boolean isRunning;

		System.out.println("EZIO: info: loading state from " + persistFile);

		/* Bail if we lack state */
		if (persistFile == null) { return; }

		/* Remember the state file so that we don't create endless numbers of them */
		this.persistFile = persistFile;

		in = new ObjectInputStream(new FileInputStream(persistFile));
		try
		{
			/* Make sure we understand the file type */
			if (!PERSISTENCE_FILE_TAG.equals(in.readObject()))
			{
				System.err.println("EZIO: error: corrupt state file: " + persistFile);
				in.close();
				return;
			}

			/* Get the file version number */
			major = in.readInt();
			minor = in.readInt();

			/*
			 * A change in the major version number indicates that format has changed in unknowable
			 * ways. Bail if we don't recognize it
			 */
			if (major > 1)
			{
				System.err.println("EZIO: warning: persistence file too new: " + major + "." + minor + "; ignoring");
				in.close();
				return;
			}

			/*
			 * A change in the minor version number indicates that fields have been added to the end
			 * of the file. Read what we can and output a warning
			 */
			if (minor > 0)
			{
				System.err.println("EZIO: warning: persistence file too new: " + major + "." + minor
						+ "; some data may be lost");
			}

			port = (String) in.readObject();
			delay = in.readInt();
			isRunning = (in.readInt() != 0);
		}
		catch (final ClassNotFoundException e)
		{
			System.err.println("EZIO: error: unable to read state information:");
			e.printStackTrace();
			in.close();
			return;
		}

		in.close();

		/* Debugging */
		System.out.println("EZIO: info: initializing from " + persistFile + ":");
		System.out.println("EZIO: info:   port=" + port);
		System.out.println("EZIO: info:   delay=" + delay);
		System.out.println("EZIO: info:   isRunning=" + isRunning);

		/* Record the state */
		setPort(port);
		setDelay(delay);
		setIsRunning(isRunning);
	}

	public synchronized File persist(final ContainerManager manager) throws IOException
	{
		ObjectOutputStream out;

		/* Make sure we have a file in which to store our state */
		if (persistFile == null)
		{
			persistFile = File
					.createTempFile("EZIO", ".dat",
									PersistenceManager.getPersistenceManager().COMPONENT_PERSISTENCE_DIRECTORY);
		}

		out = new ObjectOutputStream(new FileOutputStream(persistFile));

		/* Output the file format and major.minor version numbers */
		out.writeObject(PERSISTENCE_FILE_TAG);
		out.writeInt(1);
		out.writeInt(0);

		/* Write the port, delay and isRunning information */
		out.writeObject(getPort());
		out.writeInt(getDelay());
		out.writeInt(getIsRunning() ? 1 : 0);
		out.close();

		return persistFile;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	@Override
	public void serialEvent(final SerialPortEvent event)
	{
		final int type = event.getEventType();
		String typeName;
		TimerTask task;
		int count, i;

		if (type == SerialPortEvent.BI)
		{
			typeName = "BI";
		}
		else if (type == SerialPortEvent.CD)
		{
			typeName = "CD";
		}
		else if (type == SerialPortEvent.CTS)
		{
			typeName = "CTS";
		}
		else if (type == SerialPortEvent.DATA_AVAILABLE)
		{
			typeName = "DATA_AVAILABLE";
		}
		else if (type == SerialPortEvent.DSR)
		{
			typeName = "DSR";
		}
		else if (type == SerialPortEvent.FE)
		{
			typeName = "FE";
		}
		else if (type == SerialPortEvent.OE)
		{
			typeName = "OE";
		}
		else if (type == SerialPortEvent.OUTPUT_BUFFER_EMPTY)
		{
			typeName = "OUTPUT_BUFFER_EMPTY";
		}
		else if (type == SerialPortEvent.PE)
		{
			typeName = "PE";
		}
		else if (type == SerialPortEvent.RI)
		{
			typeName = "RI";
		}
		else
		{
			typeName = "<unknown>";
		}

		// System.out.println("TagIt: info: serial event: " + typeName);

		if (type == SerialPortEvent.DATA_AVAILABLE)
		{
			/* Disable the timer task */
			synchronized (this)
			{
				task = this.task;
				this.task = null;
			}

			/* If we're too late then bail */
			if (task == null) { return; }

			/* Cancel it */
			task.cancel();

			try
			{
				/* Read the input's value */
				count = port.getInputStream().read(buffer, offset, buffer.length - offset);
				/*
				 * Uncomment this to get a hex dump of the serial port input
				 * System.out.print("cpu<-ezio:"); for (i = 0; i < count; i++) {
				 * System.out.print(' ' + Integer.toHexString((int)buffer[offset + i] & 0xff)); }
				 * System.out.println(); /*
				 */
				offset += count;

				/* Parital read? */
				if (offset < inputs.length)
				{
					/* Enable a shorter timer to wait for the remaining data */
					task = new TimerTask()
					{
						@Override
						public void run()
						{
							requestTimeout(this);
						}
					};

					synchronized (this)
					{
						if (this.task != null)
						{
							System.err.println("EZIO: error: task race: " + this.task);
							return;
						}

						this.task = task;
						timer.schedule(this.task, MINOR_TIMEOUT);
					}

					return;
				}
				else if (offset > inputs.length)
				{
					System.err.println("EZIO: warning: cruft on serial line");
				}

				/* Record all inputs */
				for (i = 0; i < 8; i++)
				{
					setAx(i, buffer[i] & 0xff);
				}

				setDx(buffer[8] & 0xff);
				offset = 0;

				/*
				 * Uncomment this to get a dump of the current EZIO state for (i = 0; i <
				 * inputs.length; i++) { System.out.print(inputs[i] + '=' + values[i] + ' '); }
				 * System.out.println(); /*
				 */

				/* Arrange to read the next input after a judicious pause */
				transitionToState(STATE_PAUSE);
			}
			catch (final IOException e)
			{
				e.printStackTrace(System.err);
			}
		}
		else
		{
			System.err.println("EZIO: warning: unanticipated serial event: " + typeName);
		}
	}

	/**
	 * configured means it should be running
	 */
	public synchronized void setConfigured(final boolean c)
	{
		final boolean oldValue = configured;
		configured = c;
		propertyChangeListeners.firePropertyChange(EZIOBeanInfo.CONFIGURED_PROPERTY_NAME, oldValue, c);

		if (c && !getIsRunning())
		{
			setIsRunning(c);
		}
	}

	public synchronized void setDelay(final int delay)
	{
		final int oldDelay = this.delay;

		/* Discard redundant updates */
		if (oldDelay == delay) { return; }

		this.delay = delay;
		propertyChangeListeners.firePropertyChange(EZIOBeanInfo.DELAY_PROPERTY_NAME, oldDelay, delay);
	}

	public synchronized void setDx(final int value)
	{
		final int oldValue = values[8];
		int i;

		/* Discard redundant updates */
		if (oldValue == value) { return; }

		/* Record the new value */
		values[8] = value;

		/* Determine which digital inputs have changed */
		for (i = 0; i < 8; i++)
		{
			if ((oldValue & masks[i]) != (value & masks[i]))
			{
				propertyChangeListeners.firePropertyChange(	EZIOBeanInfo.DIGITAL_PROPERTY_NAMES[i],
															(oldValue & masks[i]) != 0, (value & masks[i]) != 0);
			}
		}
	}

	public synchronized void setIsRunning(final boolean isRunning)
	{
		/* Discard redundant updates */
		if ((port != null) == isRunning) { return; }

		if (port != null)
		{
			/* Cancel any pending timer task */
			if (task != null)
			{
				task.cancel();
				task = null;
			}

			/* Close the port */
			port.removeEventListener();
			port.close();
			port = null;

			/* Clear the state */
			/* FIX THIS: what state? */

			setStatus(STATUS_STOPPED);
			propertyChangeListeners.firePropertyChange(EZIOBeanInfo.IS_RUNNING_PROPERTY_NAME, true, false);
		}
		else
		{
			/* Bail if the port identifier isn't set */
			if (id == null)
			{
				System.err.println("EZIO: error: serial port not specified");
				return;
			}

			try
			{
				/* Open the serial port */
				port = (SerialPort) id.open("ECT:EZIO", 100);

				/* Configure it to 57600:8N1 */
				port.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				/* Disable flow control */
				port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

				/* Listen for changes to the port */
				port.addEventListener(this);
				port.notifyOnDataAvailable(true);

				/* Reset the state */
				state = STATE_START;

				/* Start up the state machine */
				transitionToState(STATE_REQ_PENDING);
			}
			catch (final TooManyListenersException e)
			{
				port.close();
				port = null;
				e.printStackTrace(System.err);
				return;
			}
			catch (final UnsupportedCommOperationException e)
			{
				port.close();
				port = null;
				e.printStackTrace(System.err);
				return;
			}
			catch (final PortInUseException e)
			{
				System.out.println(e);
				return;
			}

			propertyChangeListeners.firePropertyChange(EZIOBeanInfo.IS_RUNNING_PROPERTY_NAME, false, true);
		}
	}

	public synchronized void setPort(final String port)
	{
		String oldPort;
		CommPortIdentifier id;

		/* Look up the old port name */
		if (this.id == null)
		{
			oldPort = null;
		}
		else
		{
			oldPort = this.id.getName();
		}

		/* Discard redundant updates */
		if (oldPort == null && port == null || oldPort != null && oldPort.equals(port)) { return; }

		/* Discard updates if we're running */
		if (this.port != null)
		{
			System.err.println("EZIO: warning: cannot change port while running");
			return;
		}

		/* Make sure we can find the given port and that it's a serial port */
		try
		{
			id = CommPortIdentifier.getPortIdentifier(port);
			if (id.getPortType() != CommPortIdentifier.PORT_SERIAL)
			{
				System.err.println("EZIO: error: " + id.getName() + " is not a serial port");
			}
			else
			{
				this.id = id;
				propertyChangeListeners.firePropertyChange(EZIOBeanInfo.PORT_PROPERTY_NAME, oldPort, port);
			}
		}
		catch (final NoSuchPortException e)
		{
			System.out.println(e);
		}
	}

	public synchronized void stop()
	{
		if (port != null)
		{
			/* Cancel any pending timer task */
			if (task != null)
			{
				task.cancel();
				task = null;
			}

			/* Close the port */
			port.removeEventListener();
			port.close();
			port = null;

			/* Clear the state */
			/* FIX THIS */

			/* We're no longer running */
			setStatus(STATUS_STOPPED);
			propertyChangeListeners.firePropertyChange(EZIOBeanInfo.IS_RUNNING_PROPERTY_NAME, true, false);
		}
	}

	private void pauseTimeout(final TimerTask task)
	{
		synchronized (this)
		{
			if (this.task != task)
			{
				System.out.println("(4) Race averted");
				return;
			}

			this.task = null;
		}

		transitionToState(STATE_REQ_PENDING);
	}

	/* Persistable interface */

	private void requestTimeout(final TimerTask task)
	{
		synchronized (this)
		{
			if (this.task != task)
			{
				System.out.println("(1) Race averted");
				return;
			}

			this.task = null;
		}

		System.out.println("Operation timed out");
		transitionToState(STATE_RESET);
	}

	private void sendRequest(final byte[] bytes)
	{
		TimerTask task;
		final int i;

		/* Create a new timer task */
		task = new TimerTask()
		{
			@Override
			public void run()
			{
				requestTimeout(this);
			}
		};

		/*
		 * Uncomment this to get a hex dump of the serial port output
		 * System.out.print("cpu->ezio:"); for (i = 0; i < bytes.length; i++) { System.out.print(' '
		 * + Integer.toHexString((int)bytes[i] & 0xff)); } System.out.println(); /*
		 */

		/* Sanity check: there should be no pending timer task */
		synchronized (this)
		{
			if (this.task != null)
			{
				System.err.println("EZIO: error: sendRequest called with pending task: " + this.task);
				return;
			}

			this.task = task;
		}

		/* Send the request */
		try
		{
			port.getOutputStream().write(bytes);
		}
		catch (final IOException e)
		{
			e.printStackTrace(System.err);
			return;
		}

		/* Schedule the timer */
		synchronized (this)
		{
			if (this.task == task)
			{
				timer.schedule(this.task, MINOR_TIMEOUT);
			}
		}
	}

	private synchronized void setAx(final int index, final int value)
	{
		final int oldValue = values[index];

		/* Discard redundant updates */
		if (oldValue == value) { return; }

		values[index] = value;
		propertyChangeListeners.firePropertyChange(	EZIOBeanInfo.ANALOG_PROPERTY_NAMES[index],
													new Float(oldValue / 255f), new Float(value / 255f));
	}

	private synchronized void setStatus(final String status)
	{
		final String oldStatus = this.status;

		/* Discard redundant updates */
		if (oldStatus.equals(status)) { return; }

		this.status = status;
		propertyChangeListeners.firePropertyChange(EZIOBeanInfo.STATUS_PROPERTY_NAME, oldStatus, status);
	}

	private synchronized void transitionToState(final int state)
	{
		TimerTask task;
		StringBuffer sb;
		int i;

		/* System.out.println("Transition: " + this.state + "->" + state); */

		switch (state)
		{
			case STATE_START:
				System.err.println("EZIO: error: cannot transition to start state\n");
				return;

			case STATE_REQ_PENDING:
				/* Make sure we're in a valid state */
				if (this.state != STATE_START && this.state != STATE_PAUSE)
				{
					System.err.println("EZIO: error: illegal transition: " + this.state + "->" + state);
					transitionToState(STATE_RESET);
					return;
				}

				if (this.state == STATE_START)
				{
					setStatus(STATUS_INIT);
				}

				/* Construct and send a request that reads all inputs */
				sb = new StringBuffer();
				for (i = 0; i < inputs.length; i++)
				{
					sb.append(inputs[i]);
				}
				sendRequest(sb.toString().getBytes());

				this.state = state;
				break;

			case STATE_PAUSE:
				/* If we get this far then we've communicated with the EZIO board */
				setStatus(STATUS_RUNNING);

				if (this.state != STATE_REQ_PENDING)
				{
					System.err.println("EZIO: error: illegal transition: " + this.state + "->" + state);
					transitionToState(STATE_RESET);
					return;
				}

				task = new TimerTask()
				{
					@Override
					public void run()
					{
						pauseTimeout(this);
					}
				};

				/* Sanity check: there should be no pending timer task */
				synchronized (this)
				{
					if (this.task != null)
					{
						System.err.println("EZIO: error: entering pause state with pending task: " + this.task);
						Thread.currentThread();
						Thread.dumpStack();
						return;
					}

					this.task = task;
					timer.schedule(task, delay);
					this.state = state;
				}

				return;

			case STATE_RESET:
				/* No much we can do to reset */
				this.state = STATE_START;
				transitionToState(STATE_REQ_PENDING);
				offset = 0;
				break;

			default:
				System.err.println("EZIO: unknown state: " + state);
				transitionToState(STATE_RESET);
				return;
		}
	}
}
