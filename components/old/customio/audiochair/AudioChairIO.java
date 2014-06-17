/*
<COPYRIGHT>

Copyright (c) 2004,2005, University of Nottingham
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

Created by: Duncan Rowland (UoN)
Contributors: 
  Chris Greenhalgh (UoN)
 */
package equip.ect.components.customio.audiochair;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;

/**
 * Handle IO for Mark Paxton's Dial Device in the EQUATOR/RCA Audio Chairs. This device has a PIC
 * controller, with a custom program.
 */
public class AudioChairIO implements SerialPortEventListener, Serializable
{
	private OutputStream outputStream;
	private InputStream inputStream;
	private SerialPort serialPort;
	private Thread readThread;

	protected String configPort = "COM1";
	protected boolean configured = false;
	protected boolean running = false;
	protected int dial;
	protected boolean sitting;
	public static final int NUM_LIGHTS = 14;
	public static final int DIAL_MAX = 127;
	public static final int BRIGHTNESS_LEVELS = 4;
	protected float lights[] = new float[NUM_LIGHTS];
	protected String status = "new";

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no arg bean constructor
	 */
	public AudioChairIO()
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
				setStatus("ERROR initialising javax.comm");
				System.out.println("ERROR initialising javax.comm driver " + drivername + ": " + e.getMessage());
			}
		}
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized String getConfigPort()
	{
		return configPort;
	}

	public synchronized boolean getConfigured()
	{
		return configured;
	}

	/**
	 * rotary dial (continuous)
	 */
	public synchronized float getDial()
	{
		return dial * 1.0f / DIAL_MAX;
	}

	public float getLight0()
	{
		return lights[0];
	}

	public float getLight1()
	{
		return lights[1];
	}

	public float getLight10()
	{
		return lights[10];
	}

	public float getLight11()
	{
		return lights[11];
	}

	public float getLight12()
	{
		return lights[12];
	}

	public float getLight13()
	{
		return lights[13];
	}

	public float getLight2()
	{
		return lights[2];
	}

	public float getLight3()
	{
		return lights[3];
	}

	public float getLight4()
	{
		return lights[4];
	}

	public float getLight5()
	{
		return lights[5];
	}

	public float getLight6()
	{
		return lights[6];
	}

	public float getLight7()
	{
		return lights[7];
	}

	public float getLight8()
	{
		return lights[8];
	}

	public float getLight9()
	{
		return lights[9];
	}

	public synchronized float[] getLights()
	{
		return lights;
	}

	/**
	 * is the component active and running.
	 */
	public boolean getRunning()
	{
		return running;
	}

	/**
	 * Is someone sitting down?
	 */
	public boolean getSitting()
	{
		return sitting;
	}

	/**
	 * status of most recent operation
	 */
	public synchronized String getStatus()
	{
		return status;
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
	 * internal
	 */
	@Override
	public void serialEvent(final SerialPortEvent event)
	{

		switch (event.getEventType())
		{
			case SerialPortEvent.BI:
			case SerialPortEvent.OE:
			case SerialPortEvent.FE:
			case SerialPortEvent.PE:
			case SerialPortEvent.CD:
			case SerialPortEvent.CTS:
			case SerialPortEvent.DSR:
			case SerialPortEvent.RI:
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				break;
			case SerialPortEvent.DATA_AVAILABLE:

				// Process the input

				final byte[] readBuffer = new byte[10];

				try
				{
					while (inputStream.available() > 0)
					{
						final int numBytes = inputStream.read(readBuffer);
					}
				}
				catch (final IOException e)
				{
				}

				final String[] readStrings = new String(readBuffer).split("\n");

				if (readStrings.length < 2) { return; // ignore serial garbage
				}

				final String readString = readStrings[readStrings.length - 2];

				if (readString.startsWith("ON"))
				{
					setSitting(true);
				}
				else if (readString.startsWith("OFF"))
				{
					setSitting(false);
				}
				else
				{

					int newDialValue = -1;
					try
					{
						newDialValue = Integer.parseInt(readString);
					}
					catch (final Exception e)
					{
						newDialValue = -1;
					}

					if (newDialValue != -1)
					{ // Ignore serial garbage
						setDial(newDialValue);
					}
				}
				break;
		}
	}

	/**
	 * The RS232 port which the device is connected to.
	 */
	public synchronized void setConfigPort(final String val)
	{
		if (configured)
		{
			System.err.println("AudioChair.setConfigPort ignored - already configured\n");
			return;
		}
		final String old = configPort;
		configPort = val;
		propertyChangeListeners.firePropertyChange("configPort", old, val);
		try
		{
			CommPortIdentifier.getPortIdentifier(configPort);
			setStatus("Comm port '" + val + "' known");
		}
		catch (final NoSuchPortException e)
		{
			System.err.println("No such comm port: " + val + ": " + e);
			setStatus("No comm port '" + val + "'");
		}
	}

	/**
	 * configured
	 */
	public synchronized void setConfigured(final boolean val)
	{
		final boolean old = configured;
		if (old == val) { return; }
		configured = val;
		propertyChangeListeners.firePropertyChange("configured", old, val);
		if (configured)
		{
			start();
		}
		else
		{
			stop();
		}
	}

	/**
	 * set light 0
	 */
	public void setLight0(final float val)
	{
		setLight(0, val);
	}

	/**
	 * set light 1
	 */
	public void setLight1(final float val)
	{
		setLight(1, val);
	}

	/**
	 * set light 10
	 */
	public void setLight10(final float val)
	{
		setLight(10, val);
	}

	/**
	 * set light 11
	 */
	public void setLight11(final float val)
	{
		setLight(11, val);
	}

	/**
	 * set light 12
	 */
	public void setLight12(final float val)
	{
		setLight(12, val);
	}

	/**
	 * set light 13
	 */
	public void setLight13(final float val)
	{
		setLight(13, val);
	}

	/**
	 * set light 2
	 */
	public void setLight2(final float val)
	{
		setLight(2, val);
	}

	/**
	 * set light 3
	 */
	public void setLight3(final float val)
	{
		setLight(3, val);
	}

	/**
	 * set light 4
	 */
	public void setLight4(final float val)
	{
		setLight(4, val);
	}

	/**
	 * set light 5
	 */
	public void setLight5(final float val)
	{
		setLight(5, val);
	}

	/**
	 * set light 6
	 */
	public void setLight6(final float val)
	{
		setLight(6, val);
	}

	/**
	 * set light 7
	 */
	public void setLight7(final float val)
	{
		setLight(7, val);
	}

	/**
	 * set light 8
	 */
	public void setLight8(final float val)
	{
		setLight(8, val);
	}

	/**
	 * set light 9
	 */
	public void setLight9(final float val)
	{
		setLight(9, val);
	}

	// Set the state of the dial lights.
	// Brightness goes from 0..4 (0 is off and 4 is the brightest)
	public synchronized void setLights(final float[] val)
	{
		final float old[] = lights;
		lights = new float[NUM_LIGHTS];

		int i;
		for (i = 0; i < NUM_LIGHTS && i < val.length; i++)
		{
			lights[i] = val[i];
			if (lights[i] < 0)
			{
				lights[i] = 0;
			}
			else if (lights[i] > 1)
			{
				lights[i] = 1;
			}
		}
		for (; i < NUM_LIGHTS; i++)
		{
			lights[i] = old[i];
		}

		final StringBuffer messageString = new StringBuffer();
		messageString.append("\r\n");
		for (i = 0; i < NUM_LIGHTS; i++)
		{
			int brightness = (int) ((BRIGHTNESS_LEVELS + 1) * lights[i]);
			if (brightness > BRIGHTNESS_LEVELS)
			{
				brightness = BRIGHTNESS_LEVELS;
			}
			if (brightness < 0)
			{
				brightness = 0;
			}

			messageString.append((char) ('0' + brightness));
		}
		messageString.append("\r\n");

		try
		{
			outputStream.write(messageString.toString().getBytes());
			System.out.println("Updated lights: " + messageString);
		}
		catch (final IOException e)
		{
			setStatus("ERROR writing: " + e);
		}

		for (i = 0; i < NUM_LIGHTS; i++)
		{
			if (lights[i] != old[i])
			{
				propertyChangeListeners.firePropertyChange("light" + i, new Float(old[i]), new Float(lights[i]));
			}
		}

		propertyChangeListeners.firePropertyChange("lights", old, lights);
	}

	/**
	 * stop
	 */
	public synchronized void stop()
	{
		if (serialPort != null)
		{
			turnOff();
			try
			{
				serialPort.close();
			}
			catch (final Exception e)
			{
				setStatus("ERROR stopping serial port: " + e);
			}
			// ....??
			serialPort = null;
		}
		setRunning(false);
		setStatus("Stopped");
	}

	protected synchronized void setDial(final int val)
	{
		if (val == dial) { return; }
		final float fval = val * 1.0f / DIAL_MAX;
		final float old = getDial();
		dial = val;
		propertyChangeListeners.firePropertyChange("dial", new Float(old), new Float(fval));
	}

	/**
	 * internal setter
	 */
	protected void setLight(final int i, final float val)
	{
		final float current_lights[] = getLights();
		current_lights[i] = val;
		setLights(current_lights);
	}

	/**
	 * internal
	 */
	protected synchronized void setRunning(final boolean val)
	{
		if (running == val) { return; }
		final boolean old = running;
		running = val;
		propertyChangeListeners.firePropertyChange("running", old, val);
	}

	/**
	 * internal
	 */
	protected void setSitting(final boolean val)
	{
		final boolean old = sitting;
		sitting = val;
		propertyChangeListeners.firePropertyChange("sitting", old, val);
	}

	/**
	 * internal
	 */
	protected synchronized void setStatus(final String val)
	{
		final String old = status;
		status = val;
		System.out.println("AudioChair.setStatus: " + val);
		propertyChangeListeners.firePropertyChange("status", old, val);
	}

	protected synchronized void start()
	{

		CommPortIdentifier portId = null;
		try
		{
			portId = CommPortIdentifier.getPortIdentifier(configPort);
		}
		catch (final NoSuchPortException e)
		{
			setStatus("No comm port '" + configPort + "'");
			return;
		}
		try
		{
			serialPort = (SerialPort) portId.open("AudioChairIO", 2000);
		}
		catch (final PortInUseException e)
		{
			setStatus("Comm port '" + configPort + "' in use");
			return;
		}
		try
		{
			inputStream = serialPort.getInputStream();
			outputStream = serialPort.getOutputStream();
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			serialPort
					.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		}
		catch (final Exception e)
		{
			setStatus("ERROR initialising port " + configPort + ": " + e);
			System.err.println("AudioChair: ERROR initialising port " + configPort + ": " + e);
			e.printStackTrace(System.err);
			try
			{
				serialPort.close();
			}
			catch (final Exception e2)
			{
			}
			serialPort = null;
			return;
		}
		turnOn();
		setLights(lights);
		setRunning(true);
		setStatus("Started");
	}

	// Silence the dial.
	// Stop it reporting dial values, or seat switch events.
	protected void turnOff()
	{

		try
		{
			final String messageString = "OFF\r\n";
			outputStream.write(messageString.getBytes());
		}
		catch (final IOException e)
		{
		};

		try
		{
			Thread.sleep(100);
		}
		catch (final InterruptedException e)
		{
		}

	}

	// Start the dial reporting values if it is turned off.
	// Also, forces the dial to report its current value.
	protected void turnOn()
	{

		try
		{
			final String messageString = "ON\r\n";
			outputStream.write(messageString.getBytes());
		}
		catch (final IOException e)
		{
		};

		try
		{
			Thread.sleep(100);
		}
		catch (final InterruptedException e)
		{
		}

	}
}
