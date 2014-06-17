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

Created by: Jan Humble (University of Nottingham)
Contributors:
  Jan Humble (University of Nottingham)

 */
package equip.ect.components.serialdevice;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.CommPortOwnershipListener;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

public class SerialDevice implements Serializable, SerialPortEventListener, CommPortOwnershipListener
{

	public static int stringToDataBits(final String dataBitsString)
	{
		if (dataBitsString.equals("5"))
		{
			return SerialPort.DATABITS_5;
		}
		else if (dataBitsString.equals("6"))
		{
			return SerialPort.DATABITS_6;
		}
		else if (dataBitsString.equals("7"))
		{
			return SerialPort.DATABITS_7;
		}
		else if (dataBitsString.equals("8")) { return SerialPort.DATABITS_8; }
		return -1;
	}

	public static int stringToParity(final String parityString)
	{
		if (parityString.equalsIgnoreCase("EVEN"))
		{
			return SerialPort.PARITY_EVEN;
		}
		else if (parityString.equalsIgnoreCase("MARK"))
		{
			return SerialPort.PARITY_MARK;
		}
		else if (parityString.equalsIgnoreCase("NONE"))
		{
			return SerialPort.PARITY_NONE;
		}
		else if (parityString.equalsIgnoreCase("ODD"))
		{
			return SerialPort.PARITY_ODD;
		}
		else if (parityString.equalsIgnoreCase("SPACE")) { return SerialPort.PARITY_SPACE; }
		return -1;
	}

	public static int stringToStopBits(final String stopBitsString)
	{
		if (stopBitsString.equals("1"))
		{
			return SerialPort.STOPBITS_1;
		}
		else if (stopBitsString.equals("1.5"))
		{
			return SerialPort.STOPBITS_1_5;
		}
		else if (stopBitsString.equals("2")) { return SerialPort.STOPBITS_2; }
		return -1;
	}

	private static char[] byteToCharArray(final byte[] byteArray)
	{
		final char[] charArray = new char[byteArray.length];
		for (int i = 0; i < byteArray.length; i++)
		{
			charArray[i] = (char) byteArray[i];
		}
		// System.arraycopy(byteArray, 0, charArray, 0, byteArray.length);
		return charArray;
	}

	protected InputStream inputStream = null;

	protected OutputStream outputStream = null;

	protected SerialPort serialPort;

	private CommPortIdentifier portId;

	// Serial port parameters
	protected String serialPortName = "COM5";

	protected int baudRate = 9600;

	protected int dataBits = SerialPort.DATABITS_8;

	protected int stopBits = SerialPort.STOPBITS_1;

	protected int parity = SerialPort.PARITY_NONE;
	protected char[] inputCharArray, outputCharArray;
	protected String inputString, outputString;
	protected boolean connected = false;
	protected int inputInt, outputInt;

	protected byte[] inputByteArray, outputByteArray;

	// reader states
	protected static final int DEVICE_ON = 1;

	protected static final int DEVICE_OFF = 0;

	protected int readerState;

	static
	{
		final String drivername = "com.sun.comm.Win32Driver";
		try
		{
			final javax.comm.CommDriver driver = (javax.comm.CommDriver) Class.forName(drivername).newInstance();
			driver.initialize();
		}
		catch (final Exception e)
		{
			System.out.println("ERROR initialising javax.comm driver " + drivername + ": " + e.getMessage());
		}
	}

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public SerialDevice()
	{
		this.readerState = DEVICE_OFF;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * Close the port and clean up associated elements.
	 */
	public boolean closeConnection()
	{

		// Check to make sure sPort has reference to avoid a NPE.
		if (serialPort != null)
		{
			System.out.println("Closing connection on " + serialPortName + " ...");
			try
			{
				if (inputStream != null)
				{
					inputStream.close(); // close the i/o streams.
				}
			}
			catch (final IOException e)
			{
				System.out.println(e.getMessage());
			}

			// Close the port.
			serialPort.close();
			System.out.println("Closed OK!");
			// Remove the ownership listener.
			portId.removePortOwnershipListener(this);
		}
		readerState = DEVICE_OFF;
		return true; // this is strange, something needs to be thrown

	}

	/**
	 * @return Returns the baudRate.
	 */
	public int getBaudRate()
	{
		return baudRate;
	}

	public boolean getConnected()
	{
		return this.connected;
	}

	/**
	 * @return Returns the dataBits.
	 */
	public int getDataBits()
	{
		return dataBits;
	}

	/**
	 * @return Returns the inputByteArray.
	 */
	public byte[] getInputByteArray()
	{
		return inputByteArray;
	}

	/**
	 * @return Returns the inputCharArray.
	 */
	public char[] getInputCharArray()
	{
		return inputCharArray;
	}

	/**
	 * @return Returns the inputInt.
	 */
	public int getInputInt()
	{
		return inputInt;
	}

	/**
	 * @return Returns the inputString.
	 */
	public String getInputString()
	{
		return inputString;
	}

	/**
	 * @return Returns the outputByteArray.
	 */
	public byte[] getOutputByteArray()
	{
		return outputByteArray;
	}

	/**
	 * @return Returns the outputCharArray.
	 */
	public char[] getOutputCharArray()
	{
		return outputCharArray;
	}

	/**
	 * @return Returns the outputInt.
	 */
	public int getOutputInt()
	{
		return outputInt;
	}

	/**
	 * @return Returns the outputString.
	 */
	public String getOutputString()
	{
		return outputString;
	}

	/**
	 * @return Returns the parity.
	 */
	public int getParity()
	{
		return parity;
	}

	/**
	 * @return Returns the serPortName.
	 */
	public String getSerialPortName()
	{
		return serialPortName;
	}

	/**
	 * @return Returns the stopBits.
	 */
	public int getStopBits()
	{
		return stopBits;
	}

	public void handleClearToSend()
	{
	}

	/**
	 * Default action for handling read buffer. Override to change action.
	 */
	public void handleReadBuffer(final byte[] buffer)
	{
		System.out.println("HANDLING BUFFER");
		updateValues(buffer);
	}

	/**
	 * Attempts to open a serial connection and streams using the parameters in the SerialParameters
	 * object. If it is unsuccesfull at any step it returns the port to a closed state, throws a
	 * <code>SerialConnectionException</code>, and returns.
	 * 
	 * Gives a timeout of 30 seconds on the portOpen to allow other applications to reliquish the
	 * port if have it open and no longer need it.
	 */
	public boolean openConnection()
	{

		System.out.println("Opening device on '" + serialPortName + "' ...");

		// Obtain a CommPortIdentifier object for the port you want to open.
		try
		{
			portId = CommPortIdentifier.getPortIdentifier(serialPortName);
		}
		catch (final NoSuchPortException e)
		{
			System.out.println("No such port '" + serialPortName + "'");
			// throw new SerialConnectionException(e.getMessage());
			return false;
		}

		if (portId == null)
		{
			System.out.println("Could not open port");
			return false;
		}

		// Open the port represented by the CommPortIdentifier object. Give
		// the open call a relatively long timeout of 30 seconds to allow
		// a different application to reliquish the port if the user
		// wants to.
		try
		{
			serialPort = (SerialPort) portId.open("commx", 10000);
		}
		catch (final PortInUseException e)
		{
			System.out.println(e.getMessage());
			return false;
			// throw new SerialConnectionException(e.getMessage());
		}

		// Set the parameters of the connection. If they won't set, close the
		// port before throwing an exception.
		try
		{
			serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
		}
		catch (final UnsupportedCommOperationException e)
		{
			System.out.println(e.getMessage());
			return false;
		}

		// Open the input and output streams for the connection. If they won't
		// open, close the port before throwing an exception.
		try
		{
			outputStream = serialPort.getOutputStream();
			inputStream = serialPort.getInputStream();
		}
		catch (final IOException e)
		{
			// serialPort.close();
			System.out.println(e.getMessage());
		}

		// Add this object as an event listener for the serial port.
		try
		{
			serialPort.addEventListener(this);
		}
		catch (final TooManyListenersException e)
		{
			serialPort.close();
			System.out.println(e.getMessage());
			return false;
		}

		// Set notifyOnDataAvailable to true to allow event driven input.
		serialPort.notifyOnDataAvailable(true);

		// Set notifyOnBreakInterrup to allow event driven break handling.
		serialPort.notifyOnBreakInterrupt(true);

		// Set receive timeout to allow breaking out of polling loop during
		// input handling.
		try
		{
			serialPort.enableReceiveTimeout(30);
		}
		catch (final UnsupportedCommOperationException e)
		{
			System.out.println(e.getMessage());
		}

		// Add ownership listener to allow ownership event handling.
		portId.addPortOwnershipListener(this);

		readerState = DEVICE_ON;

		System.out.println("Opened connection to serial port: " + serialPortName);
		return true;
	}

	/**
	 * Handles ownership events. If a PORT_OWNERSHIP_REQUESTED event is received a dialog box is
	 * created asking the user if they are willing to give up the port. No action is taken on other
	 * types of ownership events.
	 */
	@Override
	public void ownershipChange(final int type)
	{
		if (type == CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED)
		{
			System.out.println("Somebody else wants this port! " + serialPortName);
		}
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * Send a one second break signal.
	 */
	public void sendBreak()
	{
		serialPort.sendBreak(1000);
	}

	@Override
	public void serialEvent(final SerialPortEvent e)
	{
		final int BUFFER_SIZE = 100;
		int newData = 0;
		byte[] data = new byte[BUFFER_SIZE];
		// Determine type of event.
		switch (e.getEventType())
		{
		// ntRead data until -1 is returned. If \r is received substitute
		// \n for correct newline handling.
			case SerialPortEvent.DATA_AVAILABLE:
				// System.out.println("Getting event");
				int counter = 0;
				while (newData != -1)
				{
					try
					{
						newData = inputStream.read();
						System.out.print(newData);
						if (newData == -1)
						{
							break;
						}
						if (counter >= BUFFER_SIZE)
						{
							final byte[] newDataBuffer = new byte[data.length + BUFFER_SIZE];
							System.arraycopy(data, 0, newDataBuffer, 0, data.length);
							data = newDataBuffer;
						}
						data[counter++] = (byte) newData;
					}
					catch (final IOException ex)
					{
						System.out.println(ex.getMessage());
						return;
					}
				}
				handleReadBuffer(data);
				break;
			// If break event append BREAK RECEIVED message.
			case SerialPortEvent.BI:
				System.out.println("\n--- BREAK RECEIVED ---\n");
				break;
			case SerialPortEvent.CTS:
				handleClearToSend();
				break;
		}
	}

	/**
	 * @param baudRate
	 *            The baudRate to set.
	 */
	public void setBaudRate(final int baudRate)
	{
		final int old = this.baudRate;
		this.baudRate = baudRate;
		propertyChangeListeners.firePropertyChange("baudRate", old, baudRate);
	}

	public synchronized void setConnected(final boolean connected)
	{
		final boolean connectedBefore = this.connected;

		if (connected)
		{
			if (!connectedBefore && openConnection())
			{
				this.connected = connected;
			}
		}
		else
		{
			if (closeConnection())
			{
				this.connected = connected;
			}
		}
		propertyChangeListeners.firePropertyChange("connected", connectedBefore, connected);
	}

	/**
	 * @param dataBits
	 *            The dataBits to set.
	 */
	public void setDataBits(final int dataBits)
	{
		final int old = this.dataBits;
		this.dataBits = dataBits;
		propertyChangeListeners.firePropertyChange("dataBits", old, dataBits);
	}

	/**
	 * @param inputByteArray
	 *            The inputByteArray to set.
	 */
	public void setInputByteArray(final byte[] inputByteArray)
	{
		final byte[] old = this.inputByteArray;

		this.inputByteArray = inputByteArray;
		writeObject(inputByteArray);
		propertyChangeListeners.firePropertyChange("inputByteArray", old, inputByteArray);
	}

	/**
	 * @param inputCharArray
	 *            The inputCharArray to set.
	 */
	public void setInputCharArray(final char[] inputCharArray)
	{
		final char[] old = this.inputCharArray;
		this.inputCharArray = inputCharArray;
		propertyChangeListeners.firePropertyChange("inputCharArray", old, inputCharArray);
		writeObject(inputCharArray);

	}

	/**
	 * @param inputInt
	 *            The inputInt to set.
	 */
	public void setInputInt(final int inputInt)
	{
		final int old = this.inputInt;
		this.inputInt = inputInt;
		propertyChangeListeners.firePropertyChange("inputInt", old, inputInt);
		writeObject(new Integer(inputInt));

	}

	/**
	 * @param inputString
	 *            The inputString to set.
	 */
	public void setInputString(final String inputString)
	{
		final String old = this.inputString;
		this.inputString = inputString;
		writeObject(inputString);
		propertyChangeListeners.firePropertyChange("inputString", old, inputString);
	}

	/**
	 * @param parity
	 *            The parity to set.
	 */
	public void setParity(final int parity)
	{
		final int old = this.parity;
		this.parity = parity;
		propertyChangeListeners.firePropertyChange("parity", old, parity);
	}

	/**
	 * @param serPortName
	 *            The serPortName to set.
	 */
	public void setSerialPortName(final String serPortName)
	{
		final String old = this.serialPortName;
		this.serialPortName = serPortName;
		propertyChangeListeners.firePropertyChange("serialPortName", old, serialPortName);
	}

	/**
	 * @param stopBits
	 *            The stopBits to set.
	 */
	public void setStopBits(final int stopBits)
	{
		final int old = this.stopBits;
		this.stopBits = stopBits;
		propertyChangeListeners.firePropertyChange("stopBits", old, stopBits);
	}

	@Override
	protected void finalize() throws Throwable
	{
		closeConnection();
		super.finalize();
	}

	protected byte[] objectToByteArray(final Object data)
	{
		byte[] bytes = null;
		if (data instanceof String)
		{
			bytes = ((String) data).getBytes();
		}
		else if (data instanceof char[])
		{
			bytes = (byte[]) data;
		}
		else if (data instanceof byte[])
		{
			bytes = (byte[]) data;
		}
		else if (data instanceof Integer)
		{
			bytes = new byte[] { ((Integer) data).byteValue() };
		}
		else if (data instanceof Byte)
		{
			bytes = new byte[] { ((Byte) data).byteValue() };
		}
		else if (data instanceof Character)
		{
			bytes = new byte[] { (byte) ((Character) data).charValue() };
		}
		return bytes;
	}

	protected boolean writeObject(final Object data)
	{
		if (outputStream != null)
		{
			final byte[] bytes = objectToByteArray(data);
			if (bytes != null)
			{
				try
				{
					outputStream.write(bytes);
					outputStream.flush();
					return true;
				}
				catch (final IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}
			return false;
		}
		return false;
	}

	/**
	 * @param outputByteArray
	 *            The outputByteArray to set.
	 */
	private void setOutputByteArray(final byte[] outputByteArray)
	{
		final byte[] old = this.outputByteArray;
		this.outputByteArray = outputByteArray;
		propertyChangeListeners.firePropertyChange("outputByteArray", old, outputByteArray);
	}

	/**
	 * @param outputCharArray
	 *            The outputCharArray to set.
	 */
	private void setOutputCharArray(final char[] outputCharArray)
	{
		final char[] old = this.outputCharArray;
		this.outputCharArray = outputCharArray;
		propertyChangeListeners.firePropertyChange("outputCharArray", old, outputCharArray);
	}

	/**
	 * @param outputInt
	 *            The outputInt to set.
	 */
	private void setOutputInt(final int outputInt)
	{
		final int old = this.outputInt;
		this.outputInt = outputInt;
		propertyChangeListeners.firePropertyChange("outputInt", old, outputInt);
	}

	/**
	 * @param outputString
	 *            The outputString to set.
	 */
	private void setOutputString(final String outputString)
	{
		final String old = this.outputString;
		this.outputString = outputString;
		propertyChangeListeners.firePropertyChange("outputString", old, outputString);
	}

	private synchronized void updateValues(final byte[] buffer)
	{
		this.setOutputString(new String(buffer));
		this.setOutputByteArray(buffer);
		this.setOutputCharArray(byteToCharArray(buffer));
	}
}
