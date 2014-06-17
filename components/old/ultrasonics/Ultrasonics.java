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

 Created by: Shahram Izadi (University of Nottingham)
 Contributors:
 Shahram Izadi (University of Nottingham)
 Jan Humble (University of Nottingham)

 */
package equip.ect.components.ultrasonics;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;

/**
 * Bristol cheap ultrasonic positioning system.
 * 
 * <H3>Description</H3> <B>Ultrasonics</B> allows interfacing to the ultrasonic positioning system
 * developed by Bristol University. It is very rudimentary, affirming a chirper position every
 * second or so.
 * 
 * <H3>Installation</H3> Attach the ultrasonics base to a serial connector.
 * <P>
 * Make sure that the receiver microphones point directly toward the area you intend to perform
 * positioning within---this improves the reliability of the receivers. The transmitting tags also
 * need to point in the general direction of the receivers, and it's best to attach them somewhere
 * where they won't be occluded too often.
 * <P>
 * In order for a chirper to be located, a minimum of four receivers must be visible. This is
 * because the system has to find four unknowns - x, y, z and the distance offset between the
 * chirper and the nearest receiver. Having five or six receivers visible improves the reliability
 * of tracking, in case one or two receivers fail to detect the chirper.
 * <P>
 * As far as receiver placement goes, we would recommend concentrating receivers around the areas
 * you expect will need most coverage. This should help to minimize the number of missed readings
 * that occur.
 * 
 * <H3>Configuration</H3>
 * Set the location of the 6 receivers in the space in a config file or in the 'calibration'
 * property.
 * <P>
 * The units for positioning are centimetres. The co-ordinate system you use can be chosen
 * abitrarily - decide a convenient place for the origin and directions for the X, Y and Z axes, and
 * then measure the receiver positions within this system. It's important that the co-ordinates are
 * input in the right order into the program---seems obvious but easy to get wrong!
 * <P>
 * The file format usually contains one line per receiver, where each line is a space-separated list
 * of x, y and z co-ordinates in centimetres. The first line of the file may be a number indicating
 * the number of receivers in the system. For example, for 4 receivers the file might look as
 * follows:
 * <P>
 * 4 0.0 0.0 0.0 100.0 0.0 0.0 50.3 75.0 10.5 100.0 90.0 10.0
 * <P>
 * 
 * <H3>Usage</H3>
 * Set the proper comm port and baud rate to start reporting chirper positions. See Installation for
 * proper calibration of the system.
 * 
 * <H3>Technical Details</H3> http://www.cs.bris.ac.uk/home/duff/ultrasonics.html
 * <P>
 * We are currently using 19200 baud, though 9600 is a possibility if you have an older ultrasonics
 * box (though this seems unlikely). Other settings: 8 data bits, 1 stop bit, no parity, RTS/CTS
 * handshaking disabled should work.
 * <P>
 * The only job of the ultrasonics box is to send the relative times at which chirps reach it. It
 * sits and waits until a chirp is detected reaching one of the receivers, calls this time "0", and
 * then starts a timer. As the chirp arrives at each other receiver, the time is noted. If a chirp
 * doesn't reach a particular receiver, it will time out and give an "infinite" reading.
 * <P>
 * Once all receivers have either been timed or have timed out, the box sends a delimiter followed
 * by the six relative times. One of the times will be zero, and the others some greater value. The
 * format of the buffer is actually '$U' followed by eight pairs of bytes, but the last two pairs of
 * bytes are ignored in a six receiver system.
 * <P>
 * The conversion of the byte pairs into relative distances is as below - basically, it converts a
 * clock count into a time, then multiplies by the speed of sound to give a relative distance. The
 * "* 100.0" factor at the end is to convert into centimetres:
 * <P>
 * rel_distance = (byte0 + byte1*256.0) / 2500000.0 * 343.0 * 100.0
 * <P>
 * Once the relative distances have been read, the tracking algorithm works out the additional
 * offset needed to convert them into absolute distances, and multilaterates to find the
 * transmitting tag's position.
 * 
 * @defaultOutputProperty position
 * 
 * @author Shahram Izadi (University of Nottingham)
 * 
 */
public class Ultrasonics implements Runnable, Serializable
{

	public static final String DEFAULT_PORT = "COM1";

	public static final int DEFAULT_BAUD_RATE = 19200;

	public final static String arrayToString(final double[] array)
	{
		final DecimalFormat df = new DecimalFormat("0.0");
		final StringBuffer sb = new StringBuffer("(" + df.format(array[0]));
		for (int i = 1; i < array.length; i++)
		{
			sb.append(", " + df.format(array[i]));
		}
		return sb.append(")").toString();
	}

	/*
	 * native call to calculate transmitter positions returns array: x, y, z, error
	 */
	public static native double[] findPos(double dist[], double rx[], int iterLim, double precision);

	public static void main(final String[] args)
	{
		final Ultrasonics us = new Ultrasonics();
		us.setBaudRate(19200);
		us.setPortName(args[0]);
		us.setStarted(true);

	}

	/* values for receiver distance calculations */
	private double recFreq = 2500000.0;

	private double speedOfSound = 343.0;

	private int numOfReceivers = 6;

	private boolean playback = false;

	// private String receiversFile = new String("receivers.txt");

	/* num of iterations for position calculations */
	private int iterLim = 35;

	/* precision of coordinates */
	private double precision = 1e-3;

	/* receiver positions - generated by autocalibration or by hand */
	private double rx[] = new double[numOfReceivers * 3];

	private String receiverDistances = new String();

	private String calibration = "4\n" + "0.0   0.0   0.0\n" + "100.0   0.0   0.0\n" + "50.3   75.0   10.5\n"
			+ "100.0   90.0   10.0\n";

	/* outputed coordinates */
	private double x = 0.0;

	private double y = 0.0;

	private double z = 0.0;

	/* error calculation */
	private double error = 0.0;

	/* serial io */
	private SerialPort port = null;

	private String portName = DEFAULT_PORT;

	private int baudRate = DEFAULT_BAUD_RATE;

	private InputStream is = null;

	private OutputStream os = null;

	/* file io - for replaying previous recorded readings */
	private String fileName = null;

	private int interval = 100; // sleep in ms between readings

	public static final int STATE_STARTED = 0;

	public static final int STATE_STOP_REQUEST = 1;

	public static final int STATE_STOPPED = 2;

	private int state = STATE_STOPPED;

	private Object stateLock = new Object();

	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private double[] position;

	private boolean started;

	public static final String DEFAULT_READER_CALIBRATION_FILE = "receivers.txt";

	static
	{
		try
		{
			System.loadLibrary("findpos_jni");
			final javax.comm.CommDriver driver = (javax.comm.CommDriver) Class.forName("com.sun.comm.Win32Driver")
					.newInstance();
			driver.initialize();
		}
		catch (final Throwable t)
		{
			t.printStackTrace();
		}
	}

	/*
	 * protected void readReceiverFile(String filename) { try { BufferedReader br = new
	 * BufferedReader(new FileReader(filename)); String line = null; StringTokenizer tokens = null;
	 * String token = null; int num = 0; while ((line = br.readLine()) != null) { tokens = new
	 * StringTokenizer(line, " "); while (tokens.hasMoreTokens()) { if ((token =
	 * tokens.nextToken()).equalsIgnoreCase("error:")) { error =
	 * Double.parseDouble(tokens.nextToken()); break; } try { rx[num++] = Double.parseDouble(token);
	 * } catch (Exception e) { } } } } catch (Exception e) {
	 * System.out.println("WARNING: unable to load " + "ultrasonic receiver positions\n" + e); } }
	 */

	public Ultrasonics()
	{

	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public int getBaudRate()
	{
		return baudRate;
	}

	public String getCalibration()
	{
		return calibration;
	}

	public double getError()
	{
		return error;
	}

	public String getFileName()
	{
		return fileName;
	}

	public int getInterval()
	{
		return interval;
	}

	public String getPortName()
	{
		return portName;
	}

	public double[] getPosition()
	{
		return position;
	}

	public String getReceiverDistances()
	{
		return receiverDistances;
	}

	public int getState()
	{
		synchronized (stateLock)
		{
			return state;
		}
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public double getZ()
	{
		return z;
	}

	/**
	 * @return Returns the playback.
	 */
	public boolean isPlayback()
	{
		return playback;
	}

	public boolean isStarted()
	{
		return getState() == STATE_STARTED;
	}

	public void loadDefaultCalibrationFile()
	{
		final String newCalibration = readReceiverCalibrationFile(DEFAULT_READER_CALIBRATION_FILE);
		if (newCalibration != null)
		{
			setCalibration(newCalibration);
		}
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	@Override
	public void run()
	{
		// readReceiverPositions(receiversFile);
		readReceiverCalibration(getCalibration());
		if (playback)
		{
			if (fileName != null && new File(fileName).exists())
			{

				readFile();
			}
		}
		else
		{
			readSerial();
		}
		setState(STATE_STOPPED);
		propertyChangeListeners.firePropertyChange("started", true, false);
	}

	public void setBaudRate(final int baudRate)
	{
		propertyChangeListeners.firePropertyChange("baudRate", this.baudRate, baudRate);
		this.baudRate = baudRate;
	}

	public void setCalibration(final String calibration)
	{
		final String old = this.calibration;
		this.calibration = calibration;
		propertyChangeListeners.firePropertyChange("calibration", old, calibration);
	}

	public void setFileName(final String fileName)
	{
		propertyChangeListeners.firePropertyChange("filename", this.fileName, fileName);
		this.fileName = fileName;
	}

	public void setInterval(final int interval)
	{
		propertyChangeListeners.firePropertyChange("interval", this.interval, interval);
		this.interval = interval;
	}

	/**
	 * @param playback
	 *            The playback to set.
	 */
	public void setPlayback(final boolean playback)
	{
		final boolean old = this.playback;
		this.playback = playback;
		propertyChangeListeners.firePropertyChange("playback", old, playback);
	}

	public void setPortName(final String portName)
	{
		propertyChangeListeners.firePropertyChange("portName", this.portName, portName);
		this.portName = portName;
	}

	// /setters and getters
	public synchronized void setStarted(final boolean started)
	{
		final boolean old = this.started;
		this.started = started;

		if (started && !isStarted())
		{
			startReceiver();
		}
		else
		{
			stopReceiver();
		}
		propertyChangeListeners.firePropertyChange("started", old, started);
	}

	public boolean startReceiver()
	{
		synchronized (stateLock)
		{
			if (state == STATE_STOPPED)
			{
				state = STATE_STARTED;
				System.out.println("Ultrasonics: Starting receiver ...");
				new Thread(this).start();
				return true;
			}
		}
		return false;
	}

	public void stop()
	{
		stopReceiver();
	}

	public void stopReceiver()
	{
		setState(STATE_STOP_REQUEST);
		System.out.println("Ultrasonics: Receiver stopped");
	}

	protected double[] calculateDistances(final byte data[])
	{
		try
		{
			if (data.length >= (numOfReceivers * 2))
			{
				final double result[] = new double[numOfReceivers];
				short s = -1;
				for (int i = 0; i < numOfReceivers * 2; i += 2)
				{
					s = ((short) ((data[i + 1] << 8) + (data[i] << 0)));
					result[i / 2] = (s / recFreq) * speedOfSound * 100.0;
				}
				updateDistances(result);
				return result;
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	protected double[] calculatePosition(final double dist[])
	{
		try
		{
			final double result[] = findPos(dist, rx, iterLim, precision);
			if (result != null && result.length == 4)
			{
				updatePosition(result[0], result[1], result[2], result[3]);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	protected boolean initPort(final String portName, final int baud)
	{
		enumPorts(portName);
		if (port != null)
		{
			try
			{
				port.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				is = port.getInputStream();
				os = port.getOutputStream();
				return true;
			}
			catch (final Exception e)
			{
				System.err.println("Ultrasonics: Error initialising port: " + portName + " " + baud);
				e.printStackTrace();
			}
		}
		return false;
	}

	protected void readFile()
	{
		try
		{
			final BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = null;
			StringTokenizer tokens = null;
			int num = 0;
			final double data[] = new double[6];
			while ((line = br.readLine()) != null && getState() == STATE_STARTED)
			{
				tokens = new StringTokenizer(line, " ");
				num = 0;
				try
				{
					while (tokens.hasMoreTokens())
					{
						data[num++] = Double.parseDouble(tokens.nextToken());
					}

					if (testData(data))
					{
						calculatePosition(data);
					}
					updateDistances(data);
					Thread.sleep(interval);
				}
				catch (final Exception e)
				{
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	protected String readReceiverCalibrationFile(final String filename)
	{
		FileReader fr;
		try
		{
			fr = new FileReader(filename);
		}
		catch (final FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			System.out.println("Ultrasonics: Calibration file could not be loaded.");
			return null;
		}
		final StringBuffer sb = new StringBuffer();
		try
		{
			for (int ch = fr.read(); ch > -1; ch = fr.read())
			{
				sb.append((char) ch);
			}
		}
		catch (final IOException e)
		{
			System.out.println("Ultrasonics: IOError while reading calibration file.");
		}
		return sb.toString();
	}

	protected void readSerial()
	{
		if (initPort(portName, baudRate))
		{
			char readChar = ' ', prevChar = ' ';
			StringBuffer strbuff = new StringBuffer();
			// System.out.println("Reading serial ...");
			while (getState() == STATE_STARTED)
			{
				// System.out.println("Trying ...");
				try
				{
					prevChar = readChar;
					// System.out.print('.' + (char)is.read());
					// Thread.sleep(100);

					if (is.available() > 0)
					{
						readChar = (char) (is.read() & 0xFF);
						// System.out.print(readChar);
						if (readChar == 'U' && prevChar == '$')
						{
							final double dist[] = calculateDistances(new String(strbuff).getBytes());
							System.out.println("Check data integrity " + arrayToString(dist));
							if (testData(dist))
							{
								System.out.println("Calc positions ...");
								calculatePosition(dist);
							}
							// System.out.println("Calc positions ...");
							updateDistances(dist);
							strbuff = new StringBuffer();
						}
						else
						{
							strbuff.append(readChar);
						}
					}
					else
					{
						Thread.sleep(1000);
					}

				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
			System.out.println("Ultrasonics: Closing all!");
			closeAll();
		}
	}

	protected void setPosition(final double[] position)
	{
		final DecimalFormat df = new DecimalFormat("0.00");
		final double[] old = this.position;
		this.position = position;
		propertyChangeListeners.firePropertyChange("position", old, position);
	}

	// return whether we have good data that should be processed
	protected boolean testData(final double data[])
	{
		for (final double element : data)
		{
			if (element < 0) { return false; }
		}
		return true;
	}

	protected void updateDistances(final double data[])
	{
		final DecimalFormat df = new DecimalFormat("0.0");
		String receiverDistances = new String();
		for (int i = 0; i < 6; i++)
		{
			receiverDistances += df.format(data[i]) + " ";
		}
		propertyChangeListeners.firePropertyChange("receiverDistances", this.receiverDistances, receiverDistances);
		this.receiverDistances = receiverDistances;
	}

	protected void updatePosition(final double x, final double y, final double z, final double error)
	{
		setPosition(new double[] { x, y, z, error });
		/*
		 * This was not so good as there is no guarantee on synchronized position + error values per
		 * reading.
		 * 
		 * DecimalFormat df = new DecimalFormat("0.00"); double[] old = this.position; this.position
		 * = new double[] {x, y, z, error};
		 * 
		 * 
		 * this.x = position[0]; this.y = position[1]; this.z = position[2]; this.error = error;
		 * propertyChangeListeners.firePropertyChange("x", new Double(old[0]), new
		 * Double(position[0])); propertyChangeListeners.firePropertyChange("y", new Double(old[1]),
		 * new Double(position[1])); propertyChangeListeners.firePropertyChange("z", new
		 * Double(old[2]), new Double(position[2]));
		 * 
		 * propertyChangeListeners.firePropertyChange("error", new Double( this.error), new
		 * Double(df.format(error))); System.out.println("Ultrasonics: posX=" + position[0] +
		 * " posY=" + position[1] + " posZ=" + position[2]);
		 */
	}

	private void closeAll()
	{
		try
		{
			is.close();
			os.close();
			port.close();
			port = null;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	private void enumPorts(final String portName)
	{
		try
		{
			CommPortIdentifier portId = null;
			final Enumeration portList = CommPortIdentifier.getPortIdentifiers();
			while (portList.hasMoreElements())
			{

				portId = (CommPortIdentifier) portList.nextElement();
				if (portId.getPortType() == 1 && portId.getName().equalsIgnoreCase(portName))
				{
					port = (SerialPort) portId.open("ULTRASONICS", 100);
					return;
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	private void readReceiverCalibration(final String calibration)
	{
		try
		{
			final BufferedReader br = new BufferedReader(new StringReader(calibration));
			String line = null;
			StringTokenizer tokens = null;
			String token = null;
			int num = 0;
			while ((line = br.readLine()) != null)
			{
				tokens = new StringTokenizer(line, " ");
				while (tokens.hasMoreTokens())
				{
					if ((token = tokens.nextToken()).equalsIgnoreCase("error:"))
					{
						error = Double.parseDouble(tokens.nextToken());
						break;
					}
					try
					{
						rx[num++] = Double.parseDouble(token);
					}
					catch (final Exception e)
					{
					}
				}
			}
		}
		catch (final Exception e)
		{
			System.out.println("WARNING: unable to load " + "ultrasonic receiver positions\n" + e);
		}

	}

	private void setState(final int state)
	{
		synchronized (stateLock)
		{
			this.state = state;
		}
	}
}
